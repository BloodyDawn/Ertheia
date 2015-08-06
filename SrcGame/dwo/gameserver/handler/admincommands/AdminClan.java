package dwo.gameserver.handler.admincommands;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;

import java.util.StringTokenizer;

public class AdminClan implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_clan_info", "admin_clan_changeleader"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		if(cmd.startsWith("admin_clan_info"))
		{
			String val;
			L2PcInstance player = null;
			if(st.hasMoreTokens())
			{
				val = st.nextToken();
				// From the HTML we receive player's object Id.
				if(Util.isDigit(val))
				{
					player = WorldManager.getInstance().getPlayer(Integer.parseInt(val));
					if(player == null)
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
						return false;
					}
				}
				else
				{
					player = WorldManager.getInstance().getPlayer(val);
					if(player == null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
				}
			}
			else
			{
				L2Object targetObj = activeChar.getTarget();
				if(targetObj instanceof L2PcInstance)
				{
					player = targetObj.getActingPlayer();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
			}

			L2Clan clan = player.getClan();
			if(clan == null)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
				return false;
			}

			NpcHtmlMessage html = new NpcHtmlMessage(0);
			String htm = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/admin/claninfo.htm");
			html.setHtml(htm);
			html.replace("%clan_name%", clan.getName());
			html.replace("%clan_leader%", clan.getLeaderName());
			html.replace("%clan_level%", String.valueOf(clan.getLevel()));
			html.replace("%clan_has_castle%", clan.getCastleId() > 0 ? CastleManager.getInstance().getCastleById(clan.getCastleId()).getName() : "No");
			html.replace("%clan_has_clanhall%", clan.getClanhallId() > 0 ? ClanHallManager.getInstance().getClanHallById(clan.getClanhallId()).getName() : "No");
			html.replace("%clan_has_fortress%", clan.getFortId() > 0 ? FortManager.getInstance().getFortById(clan.getFortId()).getName() : "No");
			html.replace("%clan_points%", String.valueOf(clan.getReputationScore()));
			html.replace("%clan_players_count%", String.valueOf(clan.getMembersCount()));
			html.replace("%clan_ally%", clan.getAllyId() > 0 ? clan.getAllyName() : "Not in ally");
			html.replace("%current_player_objectId%", String.valueOf(player.getObjectId()));
			html.replace("%current_player_name%", player.getName());
			activeChar.sendPacket(html);
		}
		else if(cmd.startsWith("admin_clan_changeleader"))
		{
			String val;
			L2PcInstance player = null;
			if(st.hasMoreTokens())
			{
				val = st.nextToken();
				// From the HTML we receive player's object Id.
				if(Util.isDigit(val))
				{
					player = WorldManager.getInstance().getPlayer(Integer.parseInt(val));
					if(player == null)
					{
						activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
						return false;
					}
				}
				else
				{
					player = WorldManager.getInstance().getPlayer(val);
					if(player == null)
					{
						activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
						return false;
					}
				}
			}
			else
			{
				L2Object targetObj = activeChar.getTarget();
				if(targetObj instanceof L2PcInstance)
				{
					player = targetObj.getActingPlayer();
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
			}

			L2Clan clan = player.getClan();
			if(clan == null)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
				return false;
			}

			L2ClanMember member = clan.getClanMember(player.getObjectId());
			if(member != null)
			{
				if(clan.getLeader() != null && clan.getLeader().getPlayerInstance() != null)
				{
					L2PcInstance exLeader = clan.getLeader().getPlayerInstance();
					CastleSiegeManager.getInstance().removeSiegeSkills(exLeader);
					exLeader.setClan(clan);
					exLeader.setClanPrivileges(L2Clan.CP_NOTHING);
					exLeader.broadcastUserInfo();
					exLeader.setPledgeClass(exLeader.getClan().getClanMember(exLeader.getObjectId()).calculatePledgeClass(exLeader));
					exLeader.broadcastUserInfo();
					exLeader.checkItemRestriction();
				}
				else if(clan.getLeaderId() > 0)
				{
					ThreadConnection con = null;
					FiltredPreparedStatement statement = null;
					try
					{
						con = L2DatabaseFactory.getInstance().getConnection();
						statement = con.prepareStatement(Characters.UPDATE_CHAR_CLAN_PRIVS_CHARID);
						statement.setInt(1, L2Clan.CP_NOTHING);
						statement.setInt(2, clan.getLeaderId());
						statement.execute();

						if(statement.getUpdateCount() == 0)
						{
							activeChar.sendPacket(SystemMessageId.ID_NOT_EXIST);
						}
					}
					catch(Exception e)
					{
						activeChar.sendPacket(SystemMessageId.NOT_WORKING_PLEASE_TRY_AGAIN_LATER);
					}
					finally
					{
						DatabaseUtils.closeDatabaseCS(con, statement);
					}
				}

				clan.setLeader(member);
				clan.updateClanInDB();

				player.setClan(clan);
				player.setPledgeClass(member.calculatePledgeClass(player));
				player.setClanPrivileges(L2Clan.CP_ALL);

				if(clan.getLevel() >= CastleSiegeManager.getInstance().getSiegeClanMinLevel())
				{
					CastleSiegeManager.getInstance().addSiegeSkills(player);
				}

				player.broadcastUserInfo();
				clan.broadcastClanStatus();

				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_C1);
				sm.addString(player.getName());
				clan.broadcastToOnlineMembers(sm);
				activeChar.sendPacket(sm);
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
