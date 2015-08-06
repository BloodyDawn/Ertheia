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
package dwo.gameserver.instancemanager.castle;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.queries.Castles;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.CastleTowerSpawn;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSiegeEngine;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

public class CastleSiegeManager
{
	private static final Logger _log = LogManager.getLogger(CastleSiegeManager.class);
	private final Map<Integer, List<CastleTowerSpawn>> _controlTowers = new HashMap<>();
	private final Map<Integer, List<CastleTowerSpawn>> _flameTowers = new HashMap<>();
	private int _attackerMaxClans = 500; // Max number of clans
	private int _attackerRespawnDelay; // Time in ms. Changeable in siege.config
	private int _defenderMaxClans = 500; // Max number of clans
	private int _flagMaxCount = 1; // Changeable in siege.config
	private int _siegeClanMinLevel = 5; // Changeable in siege.config
	private int _siegeLength = 120; // Time in minute. Changeable in siege.config
	private int _bloodAllianceReward; // Number of Blood Alliance items reward for successful castle defending

	private CastleSiegeManager()
	{
		_log.log(Level.INFO, "CastleSiegeManager: Initializing.");
		load();
	}

	// =========================================================
	// Constructor

	public static CastleSiegeManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addSiegeSkills(L2PcInstance character)
	{
		for(L2Skill sk : SkillTable.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.addSkill(sk, false);
		}
	}

	/**
	 * @param clan The L2Clan of the player
	 * @return {@code true} if the clan is registered or owner of a castle
	 */
	public boolean checkIsRegistered(L2Clan clan, int castleId)
	{
		if(clan == null)
		{
			return false;
		}

		if(clan.getCastleId() > 0)
		{
			return true;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		boolean register = false;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.LOAD_SIEGE_CLAN);
			statement.setInt(1, clan.getClanId());
			statement.setInt(2, castleId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				register = true;
				break;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleSiegeManager: Exception in checkIsRegistered(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return register;
	}

	public void removeSiegeSkills(L2PcInstance character)
	{
		for(L2Skill sk : SkillTable.getInstance().getSiegeSkills(character.isNoble(), character.getClan().getCastleId() > 0))
		{
			character.removeSkill(sk);
		}
	}

	private void load()
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(new File(Config.SIEGE_CONFIGURATION_FILE));
			Properties siegeSettings = new Properties();
			siegeSettings.load(is);

			// CastleSiegeEngine setting
			_attackerMaxClans = Integer.decode(siegeSettings.getProperty("AttackerMaxClans", "500"));
			_attackerRespawnDelay = Integer.decode(siegeSettings.getProperty("AttackerRespawn", "0"));
			_defenderMaxClans = Integer.decode(siegeSettings.getProperty("DefenderMaxClans", "500"));
			_flagMaxCount = Integer.decode(siegeSettings.getProperty("MaxFlags", "1"));
			_siegeClanMinLevel = Integer.decode(siegeSettings.getProperty("SiegeClanMinLevel", "5"));
			_siegeLength = Integer.decode(siegeSettings.getProperty("SiegeLength", "120"));
			_bloodAllianceReward = Integer.decode(siegeSettings.getProperty("BloodAllianceReward", "0"));

			for(Castle castle : CastleManager.getInstance().getCastles())
			{
				List<CastleTowerSpawn> controlTowers = new ArrayList<>();
				for(int i = 1; i < 0xFF; i++)
				{
					if(!siegeSettings.containsKey(castle.getName() + "ControlTower" + i))
					{
						break;
					}
					StringTokenizer st = new StringTokenizer(siegeSettings.getProperty(castle.getName() + "ControlTower" + i).trim(), ",");
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int npcId = Integer.parseInt(st.nextToken());

						controlTowers.add(new CastleTowerSpawn(npcId, new Location(x, y, z)));
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while loading control tower(s) for " + castle.getName() + " castle.");
					}
				}

				List<CastleTowerSpawn> flameTowers = new ArrayList<>();
				for(int i = 1; i < 0xFF; i++)
				{
					if(!siegeSettings.containsKey(castle.getName() + "FlameTower" + i))
					{
						break;
					}
					StringTokenizer st = new StringTokenizer(siegeSettings.getProperty(castle.getName() + "FlameTower" + i).trim(), ",");
					try
					{
						int x = Integer.parseInt(st.nextToken());
						int y = Integer.parseInt(st.nextToken());
						int z = Integer.parseInt(st.nextToken());
						int npcId = Integer.parseInt(st.nextToken());
						List<Integer> zoneList = new ArrayList<>();

						while(st.hasMoreTokens())
						{
							zoneList.add(Integer.parseInt(st.nextToken()));
						}

						flameTowers.add(new CastleTowerSpawn(npcId, new Location(x, y, z), zoneList));
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while loading flame tower(s) for " + castle.getName() + " castle.");
					}
				}
				_controlTowers.put(castle.getCastleId(), controlTowers);
				_flameTowers.put(castle.getCastleId(), flameTowers);

				if(castle.getOwnerId() != 0)
				{
					loadTrapUpgrade(castle.getCastleId());
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CastleSiegeManager: Error while loading siege data: " + e.getMessage(), e);
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

	public List<CastleTowerSpawn> getControlTowers(int castleId)
	{
		return _controlTowers.get(castleId);
	}

	public List<CastleTowerSpawn> getFlameTowers(int castleId)
	{
		return _flameTowers.get(castleId);
	}

	public int getAttackerMaxClans()
	{
		return _attackerMaxClans;
	}

	public int getAttackerRespawnDelay()
	{
		return _attackerRespawnDelay;
	}

	public int getDefenderMaxClans()
	{
		return _defenderMaxClans;
	}

	public int getFlagMaxCount()
	{
		return _flagMaxCount;
	}

	public CastleSiegeEngine getSiege(L2Object activeObject)
	{
		return getSiege(activeObject.getX(), activeObject.getY(), activeObject.getZ());
	}

	public CastleSiegeEngine getSiege(int x, int y, int z)
	{
		for(Castle castle : CastleManager.getInstance().getCastles())
		{
			if(castle.getSiege().checkIfInZone(x, y, z))
			{
				return castle.getSiege();
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

	public int getBloodAllianceReward()
	{
		return _bloodAllianceReward;
	}

	public List<CastleSiegeEngine> getSieges()
	{
		FastList<CastleSiegeEngine> castleSiegeEngines = CastleManager.getInstance().getCastles().stream().map(Castle::getSiege).collect(Collectors.toCollection(FastList::new));
		return castleSiegeEngines;
	}

	private void loadTrapUpgrade(int castleId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.TRAPUPGRADE_SELECT);
			statement.setInt(1, castleId);
			statement.executeQuery();
			rs = statement.executeQuery();
			while(rs.next())
			{
				_flameTowers.get(castleId).get(rs.getInt("towerIndex")).setUpgradeLevel(rs.getInt("level"));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: loadTrapUpgrade(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private static class SingletonHolder
	{
		protected static final CastleSiegeManager _instance = new CastleSiegeManager();
	}
}
