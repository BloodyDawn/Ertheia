package dwo.gameserver.datatables.sql;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.sql.queries.clan.ClanData;
import dwo.gameserver.datatables.sql.queries.clan.ClanNotice;
import dwo.gameserver.datatables.sql.queries.clan.ClanPrivs;
import dwo.gameserver.datatables.sql.queries.clan.ClanSkills;
import dwo.gameserver.datatables.sql.queries.clan.ClanWars;
import dwo.gameserver.datatables.sql.queries.clan.СlanSubpledges;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.instancemanager.AuctionManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.world.communitybbs.Manager.ForumsBBSManager;
import dwo.gameserver.model.world.residence.castle.CastleSiegeEngine;
import dwo.gameserver.model.world.residence.clanhall.ClanHallAuctionEngine;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortSiegeEngine;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExPledgeCount;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListAll;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanTable
{
	private static Logger _log = LogManager.getLogger(ClanTable.class);

	private final Map<Integer, L2Clan> _clans = new HashMap<>();

	private final List<ClanWar> _clanWarUpdateCache = new FastList<>();
	private final List<ClanWar> _clanWarRemoveCache = new FastList<>();

	private ClanTable()
	{
		// forums has to be loaded before clan data, because of last forum id used should have also memo included
		if(Config.COMMUNITY_TYPE > 0)
		{
			ForumsBBSManager.getInstance().initRoot();
		}

		L2Clan clan;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CLAN_DATA_CLAN_ID);
			result = statement.executeQuery();

			// Count the clans
			int clanCount = 0;

			while(result.next())
			{
				int clanId = result.getInt("clan_id");
				_clans.put(clanId, new L2Clan(clanId));
				clan = getClan(clanId);
				if(clan.getDissolvingExpiryTime() != 0)
				{
					scheduleRemoveClan(clan.getClanId());
				}
				clanCount++;
			}
			_log.log(Level.INFO, "Restored " + clanCount + " clans from the database.");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring ClanTable.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, result);
		}

		allianceCheck();
	}

	public static ClanTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public L2Clan[] getClans()
	{
		return _clans.values().toArray(new L2Clan[_clans.size()]);
	}

	/**
	 * @param clanId
	 * @return
	 */
	public L2Clan getClan(int clanId)
	{
		L2Clan clan = _clans.get(clanId);

		return clan;
	}

	public L2Clan getClanByName(String clanName)
	{
		for(L2Clan clan : getClans())
		{
			if(clan.getName().equalsIgnoreCase(clanName))
			{
				return clan;
			}
		}
		return null;
	}

	/**
	 * Creates a new clan and store clan info to database
	 *
	 * @param player
	 * @return NULL if clan with same name already exists
	 */
	public L2Clan createClan(L2PcInstance player, String clanName)
	{
		if(player == null)
		{
			return null;
		}

		if(player.getLevel() < 10)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
			return null;
		}
		if(player.getClanId() != 0)
		{
			player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN);
			return null;
		}
		if(player.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
			return null;
		}
		if(!Util.isAlphaNumeric(clanName) || clanName.length() < 2)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_INCORRECT);
			return null;
		}
		if(clanName.length() > 16)
		{
			player.sendPacket(SystemMessageId.CLAN_NAME_TOO_LONG);
			return null;
		}

		if(getClanByName(clanName) != null)
		{
			// clan name is already taken
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
			return null;
		}

		L2Clan clan = new L2Clan(IdFactory.getInstance().getNextId(), clanName);
		L2ClanMember leader = new L2ClanMember(clan, player.getName(), player.getLevel(), player.getActiveClassId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getTitle(), player.getAppearance().getSex(), player.getRace().ordinal());
		clan.setLeader(leader);
		leader.setPlayerInstance(player);
		clan.store();
		player.setClan(clan);
		player.setPledgeClass(leader.calculatePledgeClass(player));
		player.setClanPrivileges(L2Clan.CP_ALL);

		_clans.put(clan.getClanId(), clan);

		//should be update packet only
		player.sendPacket(new PledgeShowInfoUpdate(clan));
		player.sendPacket(new PledgeShowMemberListAll(clan, player));
		player.sendPacket(new ExPledgeCount(clan.getOnlineMembersCount()));
		player.sendPacket(SystemMessageId.CLAN_CREATED);
        player.broadcastUserInfo(UserInfoType.RELATION, UserInfoType.CLAN);
		return clan;
	}

	/***
	 * Удаление клана и всей информации о нем
	 * @param clanId
	 */
	public void destroyClan(int clanId)
	{
		synchronized(this)
		{
			L2Clan clan = getClan(clanId);
			if(clan == null)
			{
				return;
			}

			clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
			int castleId = clan.getCastleId();
			if(castleId == 0)
			{
				for(CastleSiegeEngine castleSiegeEngine : CastleSiegeManager.getInstance().getSieges())
				{
					castleSiegeEngine.removeSiegeClan(clan);
				}
			}
			int fortId = clan.getFortId();
			if(fortId == 0)
			{
				for(FortSiegeEngine siegeEngine : FortSiegeManager.getInstance().getSieges())
				{
					siegeEngine.removeSiegeClan(clan);
				}
			}
			int hallId = clan.getClanhallId();
			if(hallId == 0)
			{
				for(ClanHallSiegable hall : ClanHallSiegeManager.getInstance().getConquerableHalls().values())
				{
					hall.removeAttacker(clan);
				}
			}

			ClanHallAuctionEngine auction = AuctionManager.getInstance().getAuction(clan.getAuctionBiddedAt());
			if(auction != null)
			{
				auction.cancelBid(clan.getClanId());
			}

			L2ClanMember leaderMember = clan.getLeader();
			if(leaderMember == null)
			{
				clan.getWarehouse().destroyAllItems(ProcessType.CLAN, null, null);
			}
			else
			{
				clan.getWarehouse().destroyAllItems(ProcessType.CLAN, clan.getLeader().getPlayerInstance(), null);
			}

			for(L2ClanMember member : clan.getMembers())
			{
				clan.removeClanMember(member.getObjectId(), 0);
			}

			_clans.remove(clanId);

			IdFactory.getInstance().releaseId(clanId);

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(ClanData.DELETE);
				statement.setInt(1, clanId);
				statement.execute();
				statement.clearParameters();

				statement = con.prepareStatement(ClanPrivs.DELETE);
				statement.setInt(1, clanId);
				statement.execute();
				statement.clearParameters();

				statement = con.prepareStatement(ClanSkills.DELETE_CLAN_SKILLS_1);
				statement.setInt(1, clanId);
				statement.execute();
				statement.clearParameters();

				statement = con.prepareStatement(СlanSubpledges.DELETE_SUBPLEDGES);
				statement.setInt(1, clanId);
				statement.execute();
				statement.clearParameters();

				statement = con.prepareStatement(ClanWars.DELETE_CLAN_WARS_1);
				statement.setInt(1, clanId);
				statement.setInt(2, clanId);
				statement.execute();
				statement.clearParameters();

				statement = con.prepareStatement(ClanNotice.DELETE_CLAN_NOTICE);
				statement.setInt(1, clanId);
				statement.execute();
				statement.clearParameters();

				if(fortId != 0)
				{
					Fort fort = FortManager.getInstance().getFortById(fortId);
					if(fort != null)
					{
						L2Clan owner = fort.getOwnerClan();
						if(clan.equals(owner))
						{
							fort.removeOwner();
						}
					}
				}
				if(hallId != 0)
				{
					ClanHallSiegable hall = ClanHallSiegeManager.getInstance().getSiegableHall(hallId);
					if(hall != null && hall.getOwnerId() == clanId)
					{
						hall.free();
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error removing clan from DB.", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public void scheduleRemoveClan(int clanId)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(getClan(clanId) == null)
			{
				return;
			}
			if(getClan(clanId).getDissolvingExpiryTime() != 0)
			{
				destroyClan(clanId);
			}
		}, Math.max(getClan(clanId).getDissolvingExpiryTime() - System.currentTimeMillis(), 300000));
	}

	public boolean isAllyExists(String allyName)
	{
		for(L2Clan clan : getClans())
		{
			if(clan.getAllyName() != null && clan.getAllyName().equalsIgnoreCase(allyName))
			{
				return true;
			}
		}
		return false;
	}

	public void storeClanWar(ClanWar war)
	{
		if(!_clanWarUpdateCache.contains(war) && !_clanWarRemoveCache.contains(war))
		{
			_clanWarUpdateCache.add(war);
		}
	}

	// TODO: Implement bulk update
	private void storeClanWar0()
	{
		synchronized(_clanWarUpdateCache)
		{
			for(ClanWar war : _clanWarUpdateCache)
			{
				L2Clan attackerClan = getInstance().getClan(war.getAttackerClanId());
				L2Clan opposingClan = getInstance().getClan(war.getOpposingClanId());

				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(ClanWars.REPLACE_CLAN_WARS);
					statement.setInt(1, war.getAttackerClanId());
					statement.setInt(2, war.getOpposingClanId());
					statement.setString(3, war.getPeriod().toString());
					statement.setInt(4, war.getCurrentPeriodStartTime());
					statement.setInt(5, war.getLastKillTime());
					statement.setInt(6, war.getAttackersKillCounter());
					statement.setInt(7, war.getOpposersKillCounter());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Error storing clan wars data.", e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
					attackerClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP).addString(opposingClan.getName()));
					opposingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR).addString(attackerClan.getName()));
				}
			}

			_clanWarUpdateCache.clear();
		}
	}

	public void deleteClanWar(ClanWar war)
	{
		if(!_clanWarRemoveCache.contains(war))
		{
			_clanWarRemoveCache.add(war);
		}
	}

	// TODO: Implement bulk delete
	private void deleteClanWar0()
	{
		synchronized(_clanWarRemoveCache)
		{
			for(ClanWar war : _clanWarRemoveCache)
			{
				L2Clan attackerClan = getInstance().getClan(war.getAttackerClanId());
				L2Clan opposingClan = getInstance().getClan(war.getOpposingClanId());

				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(ClanWars.DELETE_CLAN_WARS);
					statement.setInt(1, war.getAttackerClanId());
					statement.setInt(2, war.getOpposingClanId());
					statement.execute();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Error removing clan wars data.", e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
					attackerClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED).addString(opposingClan.getName()));
					opposingClan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP).addString(attackerClan.getName()));
				}
			}

			_clanWarRemoveCache.clear();
		}
	}

	public void restoreWars()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ClanWars.SELECT_CLAN_WARS);
			rset = statement.executeQuery();
			while(rset.next())
			{
				L2Clan attackerClan = getClan(rset.getInt("attacker_clan"));
				L2Clan opposinClan = getClan(rset.getInt("opposing_clan"));
				ClanWar.ClanWarPeriod period = ClanWar.ClanWarPeriod.valueOf(rset.getString("period"));
				int periodStartTime = rset.getInt("period_start_time");
				int lastKillTime = rset.getInt("last_kill_time");
				int attackersKillCounter = rset.getInt("attackers_kill_counter");
				int opposersKilLCounter = rset.getInt("opposers_kill_counter");
				if(attackerClan != null && opposinClan != null)
				{
					new ClanWar(attackerClan.getClanId(), opposinClan.getClanId(), period, periodStartTime, lastKillTime, attackersKillCounter, opposersKilLCounter);
				}
				else
				{
					_log.log(Level.WARN, getClass().getSimpleName() + ": restorewars one of clans is null attacker_clan:" + attackerClan + " opposing_clan:" + opposinClan);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error restoring clan wars data.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Scheduled updates
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
			storeClanWar0();
			deleteClanWar0();
		}, 1000 * 60 * 60 * 60, 1000 * 60 * 60 * 60);
	}

	/**
	 * Check for nonexistent alliances
	 */
	private void allianceCheck()
	{
		for(L2Clan clan : getClans())
		{
			int allyId = clan.getAllyId();
			if(allyId != 0 && clan.getClanId() != allyId)
			{
				if(!_clans.containsKey(allyId))
				{
					clan.setAllyId(0);
					clan.setAllyName(null);
					clan.changeAllyCrest(0, true);
					clan.updateClanInDB();
					_log.log(Level.INFO, getClass().getSimpleName() + ": Removed alliance from clan: " + clan);
				}
			}
		}
	}

	public List<L2Clan> getClanAllies(int allianceId)
	{
		List<L2Clan> clanAllies = new ArrayList<>();
		if(allianceId != 0)
		{
			for(L2Clan clan : getClans())
			{
				if(clan != null && clan.getAllyId() == allianceId)
				{
					clanAllies.add(clan);
				}
			}
		}
		return clanAllies;
	}

	public void storeClanScore()
	{
		for(L2Clan clan : getClans())
		{
			clan.updateClanScoreInDB();
		}

		storeClanWar0();
		deleteClanWar0();
	}

	private static class SingletonHolder
	{
		protected static final ClanTable _instance = new ClanTable();
	}
}