package dwo.gameserver.network;

import dwo.config.Config;
import dwo.gameserver.LoginServerThread;
import dwo.gameserver.LoginServerThread.SessionKey;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2SecondaryAuth;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.BlowFishKeygen;
import dwo.gameserver.network.game.GameCrypt;
import dwo.gameserver.network.game.GameCryptRC4;
import dwo.gameserver.network.game.GameCryptStandart;
import dwo.gameserver.network.game.components.CharSelectInfoPackage;
import dwo.gameserver.network.game.serverpackets.ActionFail;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SeverClose;
import dwo.gameserver.network.mmocore.MMOClient;
import dwo.gameserver.network.mmocore.MMOConnection;
import dwo.gameserver.network.mmocore.ReceivablePacket;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import dwo.gameserver.util.floodprotector.FloodProtectors;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.util.AbstractQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a client connected on Game Server
 *
 * @author KenM
 */
public class L2GameClient extends MMOClient<MMOConnection<L2GameClient>> implements Runnable
{
	protected static final Logger _log = LogManager.getLogger(L2GameClient.class);
	// Task
	protected final ScheduledFuture<?> _autoSaveInDB;
	// Info
	private final InetAddress _addr;
	// floodprotectors
	private final FloodProtectors _floodProtectors = new FloodProtectors(this);
	private final AbstractQueue<ReceivablePacket<L2GameClient>> _packetQueue;
	public SessionKey _sessionId;
	protected ScheduledFuture<?> _cleanupTask;
	private GameClientState _state;
	private String _accountName;
	private L2PcInstance _activeChar;
	private ReentrantLock _activeCharLock = new ReentrantLock();
	private boolean _isAuthedGG;
	private long _connectionStartTime;
	private CharSelectInfoPackage[] _charSlotMapping;
	private L2GameServerPacket _aditionalClosePacket;

	// Crypt
	private GameCrypt _gameCrypt;

	private ClientStats _stats;

	private boolean _isDetached;

	private boolean _protocol;
	private ReentrantLock _queueLock = new ReentrantLock();
	private L2SecondaryAuth _secondaryAuth;
	private int[][] trace;
	private String _hwid;
	private int _instanceCount;
	private int _patchVersion;
	private boolean _protected;

	public L2GameClient(MMOConnection<L2GameClient> con)
	{
		super(con);
		_state = GameClientState.CONNECTED;
		_connectionStartTime = System.currentTimeMillis();

		if (Config.ENABLE_RC4)
			_gameCrypt = new GameCryptRC4();
		else
			_gameCrypt = new GameCryptStandart();

		_stats = new ClientStats();

		_packetQueue = new ArrayBlockingQueue<>(Config.CLIENT_PACKET_QUEUE_SIZE);

		_autoSaveInDB = Config.CHAR_STORE_INTERVAL > 0 ? ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoSaveTask(), 300000L, Config.CHAR_STORE_INTERVAL * 60000L) : null;

		try
		{
			_addr = con != null ? con.getInetAddress() : InetAddress.getLocalHost();
		}
		catch(UnknownHostException e)
		{
			throw new Error("Unable to determine localhost address.");
		}
	}

	public static void deleteCharByObjId(int objid)
	{
		if(objid < 0)
		{
			return;
		}
		// hooked by scripts/ai/player/CharDelete.java
		HookManager.getInstance().notifyEvent(HookType.ON_CHAR_DELETE, null, objid);
	}

	public byte[] enableCrypt()
	{
		byte[] key = null;
		if (Config.ENABLE_RC4)
		{
			try
			{
				KeyGenerator keyGenerator = KeyGenerator.getInstance("ARCFOUR");
				SecretKey secretKey = keyGenerator.generateKey();
				key = secretKey.getEncoded();
				BlowFishKeygen.fixKey(key);
			}
			catch (Exception e)
			{
				_log.error("Can not generate key!", e);
			}
		}
		else
		{
			key = BlowFishKeygen.getRandomKey();
		}
		_gameCrypt.setKey(key);
		return key;
	}

	public GameClientState getState()
	{
		return _state;
	}

	public void setState(GameClientState pState)
	{
		if(_state != pState)
		{
			_state = pState;
			_packetQueue.clear();
		}
	}

	public ClientStats getStats()
	{
		return _stats;
	}

	/**
	 * Returns cached ThreadConnection IP address, for checking detached clients.
	 * For loaded offline traders returns localhost address.
	 */
	public InetAddress getConnectionAddress()
	{
		return _addr;
	}

	public long getConnectionStartTime()
	{
		return _connectionStartTime;
	}

	@Override
	public boolean decrypt(ByteBuffer buf, int size)
	{
		_gameCrypt.decrypt(buf.array(), buf.position(), size);
		return true;
	}

	@Override
	public boolean encrypt(ByteBuffer buf, int size)
	{
		_gameCrypt.encrypt(buf.array(), buf.position(), size);
		buf.position(buf.position() + size);
		return true;
	}

	@Override
	protected void onDisconnection()
	{
		// no long running tasks here, do it async
		try
		{
			ThreadPoolManager.getInstance().executeTask(new DisconnectTask());
		}
		catch(RejectedExecutionException e)
		{
			// server is closing
		}
	}

	@Override
	protected void onForcedDisconnection()
	{
		_log.log(Level.INFO, "Client " + this + " disconnected abnormally.");
		cleanMe(true);
	}

	public L2PcInstance getActiveChar()
	{
		return _activeChar;
	}

	public void setActiveChar(L2PcInstance pActiveChar)
	{
		_activeChar = pActiveChar;
	}

	public ReentrantLock getActiveCharLock()
	{
		return _activeCharLock;
	}

	public FloodProtectors getFloodProtectors()
	{
		return _floodProtectors;
	}

	public void setGameGuardOk(boolean val)
	{
		_isAuthedGG = val;
	}

	public boolean isAuthedGG()
	{
		return _isAuthedGG;
	}

	public String getAccountName()
	{
		return _accountName;
	}

	public void setAccountName(String pAccountName)
	{
		_accountName = pAccountName;
		if(Config.SECOND_AUTH_ENABLED)
		{
			_secondaryAuth = new L2SecondaryAuth(this);
		}
	}

	public SessionKey getSessionId()
	{
		return _sessionId;
	}

	public void setSessionId(SessionKey sk)
	{
		_sessionId = sk;
	}

	public void sendPacket(L2GameServerPacket gsp)
	{
		if(_isDetached)
		{
			return;
		}

		// Packets from invisible templates sends only to GMs
		if(gsp.isInvisible() && _activeChar != null && !_activeChar.isGM())
		{
			return;
		}

		getConnection().sendPacket(gsp);
		gsp.runImpl();
	}

	private void sendActionFailed()
	{
		sendPacket(ActionFail.STATIC_PACKET);
	}

	public boolean isDetached()
	{
		return _isDetached;
	}

	public void setDetached(boolean b)
	{
		_isDetached = b;
	}

	/**
	 * Method to handle character deletion
	 *
	 * @return a byte:
	 *         <li>-1: Error: No char was found for such charslot, caught exception, etc...
	 *         <li> 0: character is not member of any clan, proceed with deletion
	 *         <li> 1: character is member of a clan, but not clan leader
	 *         <li> 2: character is clan leader
	 */
	public byte markToDeleteChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);

		if(objid < 0)
		{
			return -1;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_CLANID);
			statement.setInt(1, objid);
			rs = statement.executeQuery();

			rs.next();

			int clanId = rs.getInt(1);
			byte answer = 0;
			if(clanId != 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(clanId);

				answer = clan == null ? 0 : (byte) (clan.getLeaderId() == objid ? 2 : 1);
			}
			DatabaseUtils.closeStatement(statement);

			// Setting delete time
			if(answer == 0)
			{
				if(Config.DELETE_DAYS == 0)
				{
					deleteCharByObjId(objid);
				}
				else
				{
					statement = con.prepareStatement(Characters.UPDATE_CHAR_DELETETIME);
					statement.setLong(1, System.currentTimeMillis() + Config.DELETE_DAYS * 86400000L); // 24*60*60*1000 = 86400000
					statement.setInt(2, objid);
					statement.execute();
				}
			}

			return answer;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error updating delete time of character.", e);
			return -1;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Save the L2PcInstance to the database.
	 */
	public void saveCharToDisk()
	{
		try
		{
			L2PcInstance player = _activeChar;
			if(player != null)
			{
				player.store();
				if(Config.UPDATE_ITEMS_ON_CHAR_STORE)
				{
					player.getInventory().updateDatabase();
					player.getWarehouse().updateDatabase();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error saving character..", e);
		}
	}

	public void markRestoredChar(int charslot)
	{
		int objid = getObjectIdForSlot(charslot);
		if(objid < 0)
		{
			return;
		}
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_DELETETIME_0);
			statement.setInt(1, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring character.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public L2PcInstance loadCharFromDisk(int charslot)
	{
		int objId = getObjectIdForSlot(charslot);
		if(objId < 0)
		{
			return null;
		}

		L2PcInstance character = WorldManager.getInstance().getPlayer(objId);
		if(character != null)
		{
			// exploit prevention, should not happens in normal way
			_log.log(Level.ERROR, "Attempt of double login: " + character.getName() + '(' + objId + ") " + _accountName);
			if(character.getClient() != null)
			{
				character.getClient().closeNow();
			}
			else
			{
				character.getLocationController().delete();
			}
			return null;
		}

		character = L2PcInstance.load(objId);
		if(character != null)
		{

			// preinit some values for each login
			character.setRunning();    // running is default
			character.standUp();        // standing is default

			character.refreshOverloaded();
			character.refreshExpertisePenalty();
			character.setOnlineStatus(true, false);
		}
		else
		{
			_log.log(Level.ERROR, "could not restore in slot: " + charslot);
		}

		//setCharacter(character);
		return character;
	}

	/**
	 * @param chars
	 */
	public void setCharSelection(CharSelectInfoPackage[] chars)
	{
		_charSlotMapping = chars;
	}

	public CharSelectInfoPackage getCharSelection(int charslot)
	{
		if(_charSlotMapping == null || charslot < 0 || charslot >= _charSlotMapping.length)
		{
			return null;
		}
		return _charSlotMapping[charslot];
	}

	public L2SecondaryAuth getSecondaryAuth()
	{
		return _secondaryAuth;
	}

	public void close(L2GameServerPacket gsp)
	{
		if(getConnection() == null)
		{
			return; // ofline shop
		}
		if(_aditionalClosePacket != null)
		{
			getConnection().close(new L2GameServerPacket[]{_aditionalClosePacket, gsp});
		}
		else
		{
			getConnection().close(gsp);
		}
	}

	public void close(L2GameServerPacket[] gspArray)
	{
		if(getConnection() == null)
		{
			return; // ofline shop
		}
		getConnection().close(gspArray);
	}

	/**
	 * @param charslot
	 * @return
	 */
	private int getObjectIdForSlot(int charslot)
	{
		CharSelectInfoPackage info = getCharSelection(charslot);
		if(info == null)
		{
			_log.log(Level.WARN, this + " tried to delete Character in slot " + charslot + " but no characters exits at that slot.");
			return -1;
		}
		return info.getObjectId();
	}

	public void closeNow()
	{
		_isDetached = true; // prevents more packets execution
		close(SeverClose.STATIC_PACKET);
		synchronized(this)
		{
			if(_cleanupTask != null)
			{
				cancelCleanup();
			}
			_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), 0); //instant
		}
	}

	/**
	 * Produces the best possible string representation of this client.
	 */
	@Override
	public String toString()
	{
		try
		{
			InetAddress address = getConnection().getInetAddress();
			switch(_state)
			{
				case CONNECTED:
					return "[IP: " + (address == null ? "disconnected" : address.getHostAddress()) + ']';
				case AUTHED:
					return "[Account: " + _accountName + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + ']';
				case IN_GAME:
					return "[Character: " + (_activeChar == null ? "disconnected" : _activeChar.getName() + '[' + _activeChar.getObjectId() + ']') + " - Account: " + _accountName + " - IP: " + (address == null ? "disconnected" : address.getHostAddress()) + ']';
				default:
					throw new IllegalStateException("Missing state on switch");
			}
		}
		catch(NullPointerException e)
		{
			return "[Character read failed due to disconnect]";
		}
	}

	public void cleanMe(boolean fast)
	{
		try
		{
			synchronized(this)
			{
				if(_cleanupTask == null)
				{
					_cleanupTask = ThreadPoolManager.getInstance().scheduleGeneral(new CleanupTask(), fast ? 5 : 15000L);
				}
			}
		}
		catch(Exception e1)
		{
			_log.log(Level.ERROR, "Error during cleanup.", e1);
		}
	}

	public boolean isProtocolOk()
	{
		return _protocol;
	}

	public void setProtocolOk(boolean b)
	{
		_protocol = b;
	}

	public boolean handleCheat(String punishment)
	{
		if(_activeChar != null)
		{
			Util.handleIllegalPlayerAction(_activeChar, this + ": " + punishment, Config.DEFAULT_PUNISH);
			return true;
		}

		Logger _logAudit = LogManager.getLogger("audit");
		_logAudit.log(Level.INFO, "AUDIT: Client " + this + " kicked for reason: " + punishment);
		closeNow();
		return false;
	}

	/**
	 * Returns false if client can receive packets.
	 * True if detached, or flood detected, or queue overflow detected and queue still not empty.
	 */
	public boolean dropPacket()
	{
		if(_isDetached) // detached clients can't receive any packets
		{
			return true;
		}

		// flood protection
		if(_stats.countPacket(_packetQueue.size()))
		{
			sendActionFailed();
			return true;
		}

		return _stats.dropPacket();
	}

	/**
	 * Counts buffer underflow exceptions.
	 */
	public void onBufferUnderflow()
	{
		if(_stats.countUnderflowException())
		{
			_log.log(Level.ERROR, "Client " + this + " - Disconnected: Too many buffer underflow exceptions.");
			closeNow();
			return;
		}
		if(_state == GameClientState.CONNECTED) // in CONNECTED state kick client immediately
		{
			if(Config.PACKET_HANDLER_DEBUG)
			{
				_log.log(Level.ERROR, "Client " + this + " - Disconnected, too many buffer underflows in non-authed state.");
			}
			closeNow();
		}
	}

	/**
	 * Counts unknown packets
	 */
	public void onUnknownPacket()
	{
		if(_stats.countUnknownPacket())
		{
			_log.log(Level.ERROR, "Client " + this + " - Disconnected: Too many unknown packets.");
			closeNow();
			return;
		}
		if(_state == GameClientState.CONNECTED) // in CONNECTED state kick client immediately
		{
			if(Config.PACKET_HANDLER_DEBUG)
			{
				_log.log(Level.ERROR, "Client " + this + " - Disconnected, too many unknown packets in non-authed state.");
			}
			closeNow();
		}
	}

	/**
	 * Add packet to the queue and start worker thread if needed
	 */
	public void execute(ReceivablePacket<L2GameClient> packet)
	{
		if(_stats.countFloods())
		{
			_log.log(Level.ERROR, "Client " + this + " - Disconnected, too many floods:" + _stats.longFloods + " long and " + _stats.shortFloods + " short.");
			closeNow();
			return;
		}

		if(!_packetQueue.offer(packet))
		{
			if(_stats.countQueueOverflow())
			{
				_log.log(Level.ERROR, "Client " + this + " - Disconnected, too many queue overflows.");
				closeNow();
			}
			else
			{
				sendActionFailed();
			}

			return;
		}

		if(_queueLock.isLocked()) // already processing
		{
			return;
		}

		try
		{
			if(_state == GameClientState.CONNECTED)
			{
				if(_stats.processedPackets > 3)
				{
					if(Config.PACKET_HANDLER_DEBUG)
					{
						_log.log(Level.ERROR, "Client " + this + " - Disconnected, too many packets in non-authed state.");
					}
					closeNow();
					return;
				}

				ThreadPoolManager.getInstance().executeIOPacket(this);
			}
			else
			{
				ThreadPoolManager.getInstance().executePacket(this);
			}
		}
		catch(RejectedExecutionException e)
		{
			// if the server is shutdown we ignore
			if(!ThreadPoolManager.getInstance().isShutdown())
			{
				_log.log(Level.ERROR, "Failed executing: " + packet.getClass().getSimpleName() + " for Client: " + this);
			}
		}
	}

	public void setClientTracert(int[][] tracert)
	{
		trace = tracert;
	}

	@Override
	public void run()
	{
		if(!_queueLock.tryLock())
		{
			return;
		}

		try
		{
			int count = 0;
			while(true)
			{
				ReceivablePacket<L2GameClient> packet = _packetQueue.poll();
				if(packet == null) // queue is empty
				{
					return;
				}

				if(_isDetached) // clear queue immediately after detach
				{
					_packetQueue.clear();
					return;
				}

				try
				{
					packet.run();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Exception during execution " + packet.getClass().getSimpleName() + ", client: " + this + ',' + e.getMessage());
				}

				count++;
				if(_stats.countBurst(count))
				{
					return;
				}
			}
		}
		finally
		{
			_queueLock.unlock();
		}
	}

	public int[][] getTrace()
	{
		return trace;
	}

	private boolean cancelCleanup()
	{
		Future<?> task = _cleanupTask;
		if(task != null)
		{
			_cleanupTask = null;
			return task.cancel(true);
		}
		return false;
	}

	public void setAditionalClosePacket(L2GameServerPacket _aditionalClosePacket)
	{
		this._aditionalClosePacket = _aditionalClosePacket;
	}

	public String getHWID()
	{
		return _hwid;
	}

	public void setHWID(String hwid)
	{
		_hwid = hwid;
	}

	/**
	 * CONNECTED	- client has just connected
	 * AUTHED		- client has authed but doesnt has character attached to it yet
	 * IN_GAME		- client has selected a char and is in game
	 *
	 * @author KenM
	 */
	public static enum GameClientState
	{
		CONNECTED,
		AUTHED,
		IN_GAME
	}

	class DisconnectTask implements Runnable
	{
		@Override
		public void run()
		{
			boolean fast = true;

			try
			{
				L2PcInstance player = getActiveChar();
				if(player != null && !isDetached())
				{
					getActiveChar().storeZoneRestartLimitTime();

					setDetached(true);
					if(!player.getOlympiadController().isParticipating() && !player.isInJail() && !EventManager.isPlayerParticipant(player) && player.getVehicle() == null)
					{
						if((player.getPrivateStoreType() == PlayerPrivateStoreType.SELL || player.getPrivateStoreType() == PlayerPrivateStoreType.BUY) && Config.OFFLINE_TRADE_ENABLE || (player.isInCraftMode() || player.getPrivateStoreType() == PlayerPrivateStoreType.MANUFACTURE) && Config.OFFLINE_CRAFT_ENABLE)
						{
							player.leaveParty();

							// If the L2PcInstance has Pet, unsummon it
							if(!player.getPets().isEmpty())
							{
								for(L2Summon pet : player.getPets())
								{
									pet.setRestoreSummon(true);
									pet.getLocationController().decay();
									// dead pet wasnt unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
									if(pet != null)
									{
										pet.broadcastNpcInfo(0);
									}
								}
							}
							if(Config.OFFLINE_SET_NAME_COLOR)
							{
								player.getAppearance().setNameColor(Config.OFFLINE_NAME_COLOR);
								player.broadcastUserInfo();
							}
							if(player.getOfflineStartTime() == 0)
							{
								player.setOfflineStartTime(System.currentTimeMillis());
							}
							return;
						}
					}
					if(player.isInCombat() || player.isLocked())
					{
						fast = false;
					}
				}
				cleanMe(fast);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while disconnecting client.", e);
			}
		}
	}

	class CleanupTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				// we are going to manually save the char bellow thus we can force the cancel
				if(_autoSaveInDB != null)
				{
					_autoSaveInDB.cancel(true);
				}

				L2PcInstance player = getActiveChar();
				if(player != null) // this should only happen on ThreadConnection loss
				{
					if(player.isLocked())
					{
						_log.log(Level.WARN, "Player " + player.getName() + " still performing subclass actions during disconnect.");
					}

					HookManager.getInstance().notifyEvent(HookType.ON_DISCONNECT, player.getHookContainer(), player);

					// prevent closing again
					player.setClient(null);

					if(player.isOnline())
					{
						player.getLocationController().delete();
					}
				}
				setActiveChar(null);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while cleanup client.", e);
			}
			finally
			{
				LoginServerThread.getInstance().sendLogout(getAccountName());
			}
		}
	}

	class AutoSaveTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				L2PcInstance player = getActiveChar();
				if(player != null && player.isOnline()) // safety precaution
				{
					saveCharToDisk();
					if(!player.getPets().isEmpty())
					{
						for(L2Summon pet : player.getPets())
						{
							pet.store();
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error on AutoSaveTask.", e);
			}
		}
	}

}
