/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.instancemanager.fort;

import dwo.config.Config;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.npc.CombatFlag;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortSiegeEngine;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class FortSiegeManager
{
	private static final Logger _log = LogManager.getLogger(FortSiegeManager.class);
	private int _attackerMaxClans = 500; // Max number of clans
	// Fort FortSiegeManager settings
	private L2TIntObjectHashMap<FastList<SiegeSpawn>> _commanderSpawnList;
	private L2TIntObjectHashMap<FastList<CombatFlag>> _flagList;
	private int _flagMaxCount = 1; // Changeable in fortsiege.ini
	private int _siegeClanMinLevel = 4; // Changeable in fortsiege.ini
	private int _siegeLength = 60; // Time in minute. Changeable in fortsiege.ini
	private int _countDownLength = 10; // Time in minute. Changeable in fortsiege.ini
	private int _suspiciousMerchantRespawnDelay = 180; // Time in minute. Changeable in fortsiege.ini
	private List<FortSiegeEngine> _siegeEngines;

	private FortSiegeManager()
	{
		_log.log(Level.INFO, "FortSiegeManager: Initializing.");
		load();
	}

	public static FortSiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Return true if character summon<BR><BR>
	 *
	 * @param activeChar The L2Character of the character can summon
	 */
	public boolean checkIfOkToSummon(L2Character activeChar, boolean isCheckOnly)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return false;
		}

		String text = "";
		L2PcInstance player = (L2PcInstance) activeChar;
		Fort fort = FortManager.getInstance().getFort(player);

		if(fort == null || fort.getFortId() <= 0)
		{
			text = "You must be on fort ground to summon this";
		}
		else if(!fort.getSiege().isInProgress())
		{
			text = "You can only summon this during a siege.";
		}
		else if(player.getClanId() != 0 && fort.getSiege().getAttackerClan(player.getClanId()) == null)
		{
			text = "You can only summon this as a registered attacker.";
		}
		else
		{
			return true;
		}

		if(!isCheckOnly)
		{
			player.sendMessage(text);
		}
		return false;
	}

	/**
	 * Return true if the clan is registered or owner of a fort<BR><BR>
	 *
	 * @param clan The L2Clan of the player
	 */
	public boolean checkIsRegistered(L2Clan clan, int fortid)
	{
		if(clan == null)
		{
			return false;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		boolean register = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id FROM fortsiege_clans where clan_id=? and fort_id=?");
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, fortid);
			rs = statement.executeQuery();

			while(rs.next())
			{
				register = true;
				break;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "FortSiegeManager: Exception in checkIsRegistered(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return register;
	}

	// =========================================================
	// Method - Private

	private void load()
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(Config.FORTSIEGE_CONFIGURATION_FILE));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);

			// CastleSiegeEngine setting
			_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "4"));
			_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "60"));
			_countDownLength = Integer.decode(siegeSettings.getProperty("CountDownLength", "10"));
			_suspiciousMerchantRespawnDelay = Integer.decode(siegeSettings.getProperty("SuspiciousMerchantRespawnDelay", "180"));

			// CastleSiegeEngine spawns settings
			_commanderSpawnList = new L2TIntObjectHashMap<>();
			_flagList = new L2TIntObjectHashMap<>();

			for(Fort fort : FortManager.getInstance().getForts())
			{
				FastList<SiegeSpawn> _commanderSpawns = new FastList<>();
				FastList<CombatFlag> _flagSpawns = new FastList<>();
				for(int i = 1; i < 5; i++)
				{
					String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Commander" + i, "");
					if(_spawnParams.isEmpty())
					{
						break;
					}
					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int heading = Integer.parseInt(st.nextToken());
						int npc_id = Integer.parseInt(st.nextToken());

						_commanderSpawns.add(new SiegeSpawn(fort.getFortId(), x, y, z, heading, npc_id, i));
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "FortSiegeManager: Error while loading commander(s) for " + fort.getName() + " fort.");
					}
				}

				_commanderSpawnList.put(fort.getFortId(), _commanderSpawns);

				for(int i = 1; i < 4; i++)
				{
					String _spawnParams = siegeSettings.getProperty(fort.getName().replace(" ", "") + "Flag" + i, "");
					if(_spawnParams.isEmpty())
					{
						break;
					}
					StringTokenizer st = new StringTokenizer(_spawnParams.trim(), ",");

					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int flag_id = Integer.parseInt(st.nextToken());

						_flagSpawns.add(new CombatFlag(fort.getFortId(), x, y, z, 0, flag_id));
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "FortSiegeManager: Error while loading flag(s) for " + fort.getName() + " fort.");
					}
				}
				_flagList.put(fort.getFortId(), _flagSpawns);
			}

		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "FortSiegeManager: Error while loading fortsiege data." + e.getMessage(), e);
		}
		finally
		{
			try
			{
				is.close();
			}
			catch(Exception e)
			{
			}
		}
	}

	public FastList<SiegeSpawn> getCommanderSpawnList(int _fortId)
	{
		return _commanderSpawnList.containsKey(_fortId) ? _commanderSpawnList.get(_fortId) : null;
	}

	public FastList<CombatFlag> getFlagList(int _fortId)
	{
		return _flagList.containsKey(_fortId) ? _flagList.get(_fortId) : null;
	}

	public int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}

	public int getFlagMaxCount()
	{
		return _flagMaxCount;
	}

	public int getSuspiciousMerchantRespawnDelay()
	{
		return _suspiciousMerchantRespawnDelay;
	}

	public FortSiegeEngine getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public FortSiegeEngine getSiege(int x, int y, int z)
	{
		for(Fort fort : FortManager.getInstance().getForts())
		{
			if(fort.getSiege().checkIfInZone(x, y, z))
			{
				return fort.getSiege();
			}
		}
		return null;
	}

	public int getSiegeClanMinLevel()
	{
		return _siegeClanMinLevel;
	}

	public int getSiegeLength()
	{
		return _siegeLength;
	}

	public int getCountDownLength()
	{
		return _countDownLength;
	}

	public List<FortSiegeEngine> getSieges()
	{
		if(_siegeEngines == null)
		{
			_siegeEngines = new FastList<>();
		}
		return _siegeEngines;
	}

	public void addSiege(FortSiegeEngine fortSiegeEngine)
	{
		if(_siegeEngines == null)
		{
			_siegeEngines = new FastList<>();
		}
		_siegeEngines.add(fortSiegeEngine);
	}

	public boolean isCombatFlag(int itemId)
	{
		return itemId == 9819;
	}

	public boolean activateCombatFlag(L2PcInstance player, L2ItemInstance item)
	{
		if(!checkIfCanPickup(player))
		{
			return false;
		}

		Fort fort = FortManager.getInstance().getFort(player);

		FastList<CombatFlag> fcf = _flagList.get(fort.getFortId());
		fcf.stream().filter(cf -> cf.itemInstance.equals(item)).forEach(cf -> cf.activate(player, item));
		return true;
	}

	public boolean checkIfCanPickup(L2PcInstance player)
	{
		// Cannot own 2 combat flag
		if(player.isCombatFlagEquipped())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addItemName(9819));
			return false;
		}

		// here check if is siege is in progress
		// here check if is siege is attacker
		Fort fort = FortManager.getInstance().getFort(player);

		if(fort == null || fort.getFortId() <= 0)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addItemName(9819));
			return false;
		}
		if(!fort.getSiege().isInProgress())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addItemName(9819));
			return false;
		}
		if(fort.getSiege().getAttackerClan(player.getClan()) == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_FORTRESS_BATTLE_OF_S1_HAS_FINISHED).addItemName(9819));
			return false;
		}
		return true;
	}

	public void dropCombatFlag(L2PcInstance player, int fortId)
	{
		Fort fort = FortManager.getInstance().getFortById(fortId);

		FastList<CombatFlag> fcf = _flagList.get(fort.getFortId());

		fcf.stream().filter(cf -> cf.playerId == player.getObjectId()).forEach(cf -> {
			cf.dropIt();
			if(fort.getSiege().isInProgress())
			{
				cf.spawnMe();
			}
		});
	}

	public static class SiegeSpawn
	{
		Location _location;
		private int _npcId;
		private int _heading;
		private int _fortId;
		private int _id;

		public SiegeSpawn(int fort_id, int x, int y, int z, int heading, int npc_id, int id)
		{
			_fortId = fort_id;
			_location = new Location(x, y, z, heading);
			_heading = heading;
			_npcId = npc_id;
			_id = id;
		}

		public int getFortId()
		{
			return _fortId;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getHeading()
		{
			return _heading;
		}

		public int getId()
		{
			return _id;
		}

		public Location getLocation()
		{
			return _location;
		}
	}

	private static class SingletonHolder
	{
		protected static final FortSiegeManager _instance = new FortSiegeManager();
	}
}
