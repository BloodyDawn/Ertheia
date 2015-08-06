package dwo.gameserver.model.world.residence.clanhall;

import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2SpecialSiegeGuardAI;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2ResidenceHallTeleportZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author BiggBoss
 */
public abstract class ClanHallFlagWar extends ClanHallSiegeEngine
{
	protected static final int[] OUTTER_DOORS_TO_OPEN = new int[2];
	protected static final int[] INNER_DOORS_TO_OPEN = new int[2];
	protected static final Location[] FLAG_COORDS = new Location[7];
	protected static final L2ResidenceHallTeleportZone[] TELE_ZONES = new L2ResidenceHallTeleportZone[6];
	private static final String SQL_LOAD_ATTACKERS = "SELECT * FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_SAVE_ATTACKER = "INSERT INTO siegable_hall_flagwar_attackers_members VALUES (?,?,?)";
	private static final String SQL_LOAD_MEMEBERS = "SELECT object_id FROM siegable_hall_flagwar_attackers_members WHERE clan_id = ?";
	private static final String SQL_SAVE_CLAN = "INSERT INTO siegable_hall_flagwar_attackers VALUES(?,?,?,?)";
	private static final String SQL_SAVE_NPC = "UPDATE siegable_hall_flagwar_attackers SET npc = ? WHERE clan_id = ?";
	private static final String SQL_CLEAR_CLAN = "DELETE FROM siegable_hall_flagwar_attackers WHERE hall_id = ?";
	private static final String SQL_CLEAR_CLAN_ATTACKERS = "DELETE FROM siegable_hall_flagwar_attackers_members WHERE hall_id = ?";
	protected static String qn;
	protected static int ROYAL_FLAG;
	protected static int FLAG_RED;
	protected static int FLAG_YELLOW;
	protected static int FLAG_GREEN;
	protected static int FLAG_BLUE;
	protected static int FLAG_PURPLE;
	protected static int ALLY_1;
	protected static int ALLY_2;
	protected static int ALLY_3;
	protected static int ALLY_4;
	protected static int ALLY_5;
	protected static int TELEPORT_1;
	protected static int MESSENGER;
	protected static int QUEST_REWARD;

	protected static Location CENTER;

	protected Map<Integer, ClanData> _data = new HashMap<>(6);
	protected L2Clan _winner;
	private boolean _firstPhase;

	protected ClanHallFlagWar(int questId, String name, String descr, int hallId)
	{
		super(questId, name, descr, hallId);

		addStartNpc(MESSENGER);
		addFirstTalkId(MESSENGER);
		addTalkId(MESSENGER);

		for(int i = 0; i < 6; i++)
		{
			addFirstTalkId(TELEPORT_1 + i);
		}

		addKillId(ALLY_1);
		addKillId(ALLY_2);
		addKillId(ALLY_3);
		addKillId(ALLY_4);
		addKillId(ALLY_5);

		addSpawnId(ALLY_1);
		addSpawnId(ALLY_2);
		addSpawnId(ALLY_3);
		addSpawnId(ALLY_4);
		addSpawnId(ALLY_5);

		// If siege ends w/ more than 1 flag alive, winner is old owner
		_winner = ClanTable.getInstance().getClan(_hall.getOwnerId());
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		synchronized(this)
		{
			String html = event;
			L2Clan clan = player.getClan();

			if(event.startsWith("register_clan")) // Register the clan for the siege
			{
				if(!_hall.isRegistering())
				{
					if(_hall.isInSiege())
					{
						html = "messenger_registrationpassed.htm";
					}
					else
					{
						sendRegistrationPageDate(player);
						return null;
					}
				}
				else if(clan == null || !player.isClanLeader())
				{
					html = "messenger_notclannotleader.htm";
				}
				else if(getAttackers().size() >= 5)
				{
					html = "messenger_attackersqueuefull.htm";
				}
				else if(checkIsAttacker(clan))
				{
					html = "messenger_clanalreadyregistered.htm";
				}
				else if(_hall.getOwnerId() == clan.getClanId())
				{
					html = "messenger_curownermessage.htm";
				}
				else
				{
					String[] arg = event.split(" ");
					if(arg.length >= 2)
					{
						// Register passing the quest
						if(arg[1].equals("wQuest"))
						{
							if(player.destroyItemByItemId(ProcessType.CLAN, QUEST_REWARD, 1, npc, false)) // Quest passed
							{
								registerClan(clan);
								html = getFlagHtml(_data.get(clan.getClanId()).flag);
							}
							else // Quest not accoplished, try by paying
							{
								html = "messenger_noquest.htm";
							}
						}
						// Register paying the fee
						else if(arg[1].equals("wFee") && canPayRegistration())
						{
							if(player.reduceAdena(ProcessType.CLAN, 200000, npc, false)) // Fee payed
							{
								registerClan(clan);
								html = getFlagHtml(_data.get(clan.getClanId()).flag);
							}
							else // Fee couldnt be payed, try with quest
							{
								html = "messenger_nomoney.htm";
							}
						}
					}
				}
			}
			// Select the flag to defend
			else if(event.startsWith("select_clan_npc"))
			{
				if(!player.isClanLeader())
				{
					html = "messenger_onlyleaderselectally.htm";
				}
				else if(!_data.containsKey(clan.getClanId()))
				{
					html = "messenger_clannotregistered.htm";
				}
				else
				{
					String[] var = event.split(" ");
					if(var.length >= 2)
					{
						int id = 0;
						try
						{
							id = Integer.parseInt(var[1]);
						}
						catch(Exception e)
						{
							_log.log(Level.WARN, qn + "->select_clan_npc->Wrong mahum warrior id: " + var[1]);
						}
						if(id > 0 && (html = getAllyHtml(id)) != null)
						{
							_data.get(clan.getClanId()).npc = id;
							saveNpc(id, clan.getClanId());
						}
					}
					else
					{
						_log.log(Level.WARN, qn + " Siege: Not enough parameters to save clan npc for clan: " + clan.getName());
					}
				}
			}
			// View (and change ? ) the current selected mahum warrior
			else if(event.startsWith("view_clan_npc"))
			{
				ClanData cd = null;
				if(clan == null)
				{
					html = "messenger_clannotregistered.htm";
				}
				else if((cd = _data.get(clan.getClanId())) == null)
				{
					html = "messenger_notclannotleader.htm";
				}
				else
				{
					html = cd.npc == 0 ? "messenger_leaderdidnotchooseyet.htm" : getAllyHtml(cd.npc);
				}
			}
			// Register a clan member for the fight
			else if(event.equals("register_member"))
			{
				if(clan == null)
				{
					html = "messenger_clannotregistered.htm";
				}
				else if(!_hall.isRegistering())
				{
					html = "messenger_registrationpassed.htm";
				}
				else if(!_data.containsKey(clan.getClanId()))
				{
					html = "messenger_notclannotleader.htm";
				}
				else if(_data.get(clan.getClanId()).players.size() >= 18)
				{
					html = "messenger_clanqueuefull.htm";
				}
				else
				{
					ClanData data = _data.get(clan.getClanId());
					data.players.add(player.getObjectId());
					saveMember(clan.getClanId(), player.getObjectId());
					html = data.npc == 0 ? "messenger_leaderdidnotchooseyet.htm" : "messenger_clanregistered.htm";
				}
			}
			// Show cur attacker list
			else if(event.equals("view_attacker_list"))
			{
				if(_hall.isRegistering())
				{
					sendRegistrationPageDate(player);
				}
				else
				{
					html = HtmCache.getInstance().getHtm(null, "data/scripts/conquerablehalls/flagwar/" + qn + "/messenger_registeredclans.htm");
					int i = 0;
					for(Map.Entry<Integer, ClanData> clanData : _data.entrySet())
					{
						L2Clan attacker = ClanTable.getInstance().getClan(clanData.getKey());
						if(attacker == null)
						{
							continue;
						}
						html = html.replaceAll("%clan" + i + '%', clan.getName());
						html = html.replaceAll("%clanMem" + i + '%', String.valueOf(clanData.getValue().players.size()));
						i++;
					}
					if(_data.size() < 5)
					{
						for(int c = _data.size(); c < 5; c++)
						{
							html = html.replaceAll("%clan" + c + '%', "Empty pos. ");
							html = html.replaceAll("%clanMem" + c + '%', "Empty pos. ");
						}
					}
				}
			}
			return html;
		}
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		synchronized(this)
		{
			if(_hall.isInSiege())
			{
				int npcId = npc.getNpcId();
				_data.entrySet().stream().filter(integerClanDataEntry -> integerClanDataEntry.getValue().npc == npcId).forEach(integerClanDataEntry -> removeParticipant(integerClanDataEntry.getKey(), true));

				synchronized(this)
				{
					// TODO: Zoey76: previous bad implementation.
					// Converting map.keySet() to List and map.values() to List doesn't ensure that
					// first element in the key's List correspond to the first element in the values' List
					// That's the reason that values aren't copied to a List, instead using _data.get(clanIds.get(0))
					List<Integer> clanIds = new ArrayList<>(_data.keySet());
					if(_firstPhase)
					{
						// Siege ends if just 1 flag is alive
						// Hall was free before battle or owner didn't set the ally npc
						if(clanIds.size() == 1 && _hall.getOwnerId() <= 0 || _data.get(clanIds.get(0)).npc == 0)
						{
							_missionAccomplished = true;
							//_winner = ClanTable.getInstance().getClan(_data.keySet()[0]);
							//removeParticipant(_data.keySet()[0], false);
							cancelSiegeTask();
							endSiege();
						}
						else if(_data.size() == 2 && _hall.getOwnerId() > 0) // Hall has defender (owner)
						{
							cancelSiegeTask();    // No time limit now
							_firstPhase = false;
							_hall.getSiegeZone().setIsSiegeActive(false);
							for(int doorId : INNER_DOORS_TO_OPEN)
							{
								_hall.openCloseDoor(doorId, true);
							}

							_data.values().forEach(this::doUnSpawns);

							ThreadPoolManager.getInstance().scheduleGeneral(() -> {
								for(int doorId : INNER_DOORS_TO_OPEN)
								{
									_hall.openCloseDoor(doorId, false);
								}

								for(Map.Entry<Integer, ClanData> e : _data.entrySet())
								{
									doSpawns(e.getKey(), e.getValue());
								}

								_hall.getSiegeZone().setIsSiegeActive(true);
							}, 300000);
						}
					}
					else
					{
						_missionAccomplished = true;
						_winner = ClanTable.getInstance().getClan(clanIds.get(0));
						removeParticipant(clanIds.get(0), false);
						endSiege();
					}
				}
			}
			return null;
		}
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String html = null;
		if(npc.getNpcId() == MESSENGER)
		{
			if(checkIsAttacker(player.getClan()))
			{
				html = "messenger_initial.htm";
			}
			else
			{
				L2Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
				String content = HtmCache.getInstance().getHtmQuest(player.getLang(), "conquerablehalls/" + qn + "/messenger_initial.htm");
				content = content.replaceAll("%clanName%", clan == null ? "no owner" : clan.getName());
				content = content.replaceAll("%objectId%", String.valueOf(npc.getObjectId()));
				html = content;
			}
		}
		else
		{
			int index = npc.getNpcId() - TELEPORT_1;
			if(index == 0 && _firstPhase)
			{
				html = "teleporter_notyet.htm";
			}
			else
			{
				TELE_ZONES[index].checkTeleporTask();
				html = "teleporter.htm";
			}
		}
		return html;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, CENTER);
		return null;
	}

	void doSpawns(int clanId, ClanData data)
	{
		try
		{
			L2NpcTemplate mahumTemplate = NpcTable.getInstance().getTemplate(data.npc);
			L2NpcTemplate flagTemplate = NpcTable.getInstance().getTemplate(data.flag);

			if(flagTemplate == null)
			{
				_log.log(Level.WARN, qn + ": Flag L2NpcTemplate[" + data.flag + "] does not exist!");
				throw new NullPointerException();
			}
			if(mahumTemplate == null)
			{
				_log.log(Level.WARN, qn + ": Ally L2NpcTemplate[" + data.npc + "] does not exist!");
				throw new NullPointerException();
			}

			int index = 0;
			if(_firstPhase)
			{
				index = data.flag - FLAG_RED;
			}
			else
			{
				index = clanId == _hall.getOwnerId() ? 5 : 6;
			}
			Location loc = FLAG_COORDS[index];

			data.flagInstance = new L2Spawn(flagTemplate);
			data.flagInstance.setLocation(loc);
			data.flagInstance.setRespawnDelay(10000);
			data.flagInstance.setAmount(1);
			data.flagInstance.init();

			data.warrior = new L2Spawn(mahumTemplate);
			data.warrior.setLocation(loc);
			data.warrior.setRespawnDelay(10000);
			data.warrior.setAmount(1);
			data.warrior.init();
			((L2SpecialSiegeGuardAI) data.warrior.getLastSpawn().getAI()).getAlly().addAll(data.players);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ": Couldnt make clan spawns: ", e);
		}
	}

	private void fillPlayerList(ClanData data)
	{
		for(int objId : data.players)
		{
			L2PcInstance plr = WorldManager.getInstance().getPlayer(objId);
			if(plr != null)
			{
				data.playersInstance.add(plr);
			}
		}
	}

	private void registerClan(L2Clan clan)
	{
		int clanId = clan.getClanId();

		L2SiegeClan sc = new L2SiegeClan(clanId, L2SiegeClan.SiegeClanType.ATTACKER);
		getAttackers().put(clanId, sc);

		ClanData data = new ClanData();
		data.flag = ROYAL_FLAG + _data.size();
		data.players.add(clan.getLeaderId());
		_data.put(clanId, data);

		saveClan(clanId, data.flag);
		saveMember(clanId, clan.getLeaderId());
	}

	private void doUnSpawns(ClanData data)
	{
		if(data.flagInstance != null)
		{
			data.flagInstance.stopRespawn();
			data.flagInstance.getLastSpawn().getLocationController().delete();
		}
		if(data.warrior != null)
		{
			data.warrior.stopRespawn();
			data.warrior.getLastSpawn().getLocationController().delete();
		}
	}

	private void removeParticipant(int clanId, boolean teleport)
	{
		ClanData dat = _data.remove(clanId);

		if(dat != null)
		{
			// Destroy clan flag
			if(dat.flagInstance != null)
			{
				dat.flagInstance.stopRespawn();
				if(dat.flagInstance.getLastSpawn() != null)
				{
					dat.flagInstance.getLastSpawn().getLocationController().delete();
				}
			}

			if(dat.warrior != null)
			{
				// Destroy clan warrior
				dat.warrior.stopRespawn();
				if(dat.warrior.getLastSpawn() != null)
				{
					dat.warrior.getLastSpawn().getLocationController().delete();
				}
			}

			dat.players.clear();

			if(teleport)
			{
				// Teleport players outside
				dat.playersInstance.stream().filter(pc -> pc != null).forEach(pc -> pc.teleToLocation(TeleportWhereType.TOWN));
			}

			dat.playersInstance.clear();
		}
	}

	public boolean canPayRegistration()
	{
		return true;
	}

	private void sendRegistrationPageDate(L2PcInstance player)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFileQuest(player.getLang(), "conquerablehalls/" + qn + "/siege_date.htm");
		msg.replace("%nextSiege%", _hall.getSiegeDate().getTime().toString());
		player.sendPacket(msg);
	}

	public abstract String getFlagHtml(int flag);

	public abstract String getAllyHtml(int ally);

	// =============================================
	// Database access methods
	// =============================================
	@Override
	public void loadAttackers()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_LOAD_ATTACKERS);
			statement.setInt(1, _hall.getId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int clanId = rset.getInt("clan_id");

				if(ClanTable.getInstance().getClan(clanId) == null)
				{
					_log.log(Level.WARN, qn + ": Loaded an unexistent clan as attacker! Clan Id: " + clanId);
					continue;
				}

				ClanData data = new ClanData();
				data.flag = rset.getInt("flag");
				data.npc = rset.getInt("npc");

				_data.put(clanId, data);
				loadAttackerMembers(clanId);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ".loadAttackers()->", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	@Override
	public void prepareOwner()
	{
		if(_hall.getOwnerId() > 0)
		{
			registerClan(ClanTable.getInstance().getClan(_hall.getOwnerId()));
		}

		_hall.banishForeigners();
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED);
		msg.addString(getName());
		Announcements.getInstance().announceToAll(msg);
		_hall.updateSiegeStatus(ClanHallSiegeStatus.WAITING_BATTLE);

		_siegeTask = ThreadPoolManager.getInstance().scheduleGeneral(new SiegeStarts(), 3600000);
	}

	@Override
	public void startSiege()
	{
		if(getAttackers().size() < 2)
		{
			onSiegeEnds();
			getAttackers().clear();
			_hall.updateNextSiege();
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST);
			sm.addString(_hall.getName());
			Announcements.getInstance().announceToAll(sm);
			return;
		}

		// Open doors for challengers
		for(int door : OUTTER_DOORS_TO_OPEN)
		{
			_hall.openCloseDoor(door, true);
		}

		// Teleport owner inside
		if(_hall.getOwnerId() > 0)
		{
			L2Clan owner = ClanTable.getInstance().getClan(_hall.getOwnerId());
			Location loc = _hall.getZone().getSpawns().get(0); // Owner restart point
			for(L2ClanMember pc : owner.getMembers())
			{
				if(pc != null)
				{
					L2PcInstance player = pc.getPlayerInstance();
					if(player != null && player.isOnline())
					{
						player.teleToLocation(loc, false);
					}
				}
			}
		}

		// Schedule open doors closement and siege start in 2 minutes
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			for(int door : OUTTER_DOORS_TO_OPEN)
			{
				_hall.openCloseDoor(door, false);
			}

			_hall.getZone().banishNonSiegeParticipants();

			ClanHallFlagWar.super.startSiege();
		}, 300000);
	}

	@Override
	public void endSiege()
	{
		if(_hall.getOwnerId() > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(_hall.getOwnerId());
			clan.setClanhallId(0);
			_hall.free();
		}
		super.endSiege();
	}

	@Override
	public Location getInnerSpawnLoc(L2PcInstance player)
	{
		Location loc = null;
		if(player.getClanId() == _hall.getOwnerId())
		{
			loc = _hall.getZone().getSpawns().get(0);
		}
		else
		{
			ClanData cd = _data.get(player.getClanId());
			if(cd != null)
			{
				int index = cd.flag - FLAG_RED;
				if(index >= 0 && index <= 4)
				{
					loc = _hall.getZone().getChallengerSpawns().get(index);
				}
				else
				{
					throw new ArrayIndexOutOfBoundsException();
				}
			}
		}
		return loc;
	}

	@Override
	public boolean canPlantFlag()
	{
		return false;
	}

	@Override
	public void onSiegeStarts()
	{
		for(Map.Entry<Integer, ClanData> clan : _data.entrySet())
		{
			// Spawns challengers flags and npcs
			try
			{
				ClanData data = clan.getValue();
				doSpawns(clan.getKey(), data);
				fillPlayerList(data);
			}
			catch(Exception e)
			{
				endSiege();
				_log.log(Level.ERROR, qn + ": Problems in siege initialization!");
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onSiegeEnds()
	{
		if(!_data.isEmpty())
		{
			for(int clanId : _data.keySet())
			{
				if(_hall.getOwnerId() == clanId)
				{
					removeParticipant(clanId, false);
				}
				else
				{
					removeParticipant(clanId, true);
				}
			}
		}
		clearTables();
	}

	@Override
	public boolean doorIsAutoAttackable()
	{
		return false;
	}

	@Override
	public L2Clan getWinner()
	{
		return _winner;
	}

	private void loadAttackerMembers(int clanId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			List<Integer> listInstance = _data.get(clanId).players;

			if(listInstance == null)
			{
				_log.log(Level.WARN, qn + ": Tried to load unregistered clan: " + clanId + "[clan Id]");
				return;
			}

			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_LOAD_MEMEBERS);
			statement.setInt(1, clanId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				listInstance.add(rset.getInt("object_id"));

			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ".loadAttackerMembers()->", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	private void saveClan(int clanId, int flag)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_SAVE_CLAN);
			statement.setInt(1, _hall.getId());
			statement.setInt(2, flag);
			statement.setInt(3, 0);
			statement.setInt(4, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ".saveClan()->", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void saveNpc(int npc, int clanId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_SAVE_NPC);
			statement.setInt(1, npc);
			statement.setInt(2, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ".saveNpc()->", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void saveMember(int clanId, int objectId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(SQL_SAVE_ATTACKER);
			statement.setInt(1, _hall.getId());
			statement.setInt(2, clanId);
			statement.setInt(3, objectId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ".saveMember()->", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void clearTables()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(SQL_CLEAR_CLAN);
			statement.setInt(1, _hall.getId());
			statement.execute();
			statement.close();

			statement = con.prepareStatement(SQL_CLEAR_CLAN_ATTACKERS);
			statement.setInt(1, _hall.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, qn + ".clearTables()->", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	class ClanData
	{
		int flag;
		int npc;
		List<Integer> players = new ArrayList<>(18);
		List<L2PcInstance> playersInstance = new ArrayList<>(18);
		L2Spawn warrior;
		L2Spawn flagInstance;
	}
}
