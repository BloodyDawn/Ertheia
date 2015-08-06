package dwo.gameserver.handler.bypasses;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

import java.util.List;

/**
 * Clan dialogs command handler.
 *
 * @author Bacek
 * @author Yorie
 */
public class Clan extends CommandHandler<String>
{
	public boolean checkInteractionDistance(L2PcInstance activeChar)
	{
		L2Npc npc = activeChar.getLastFolkNPC();
		return npc != null && activeChar.isInsideRadius(npc, L2Npc.INTERACTION_DISTANCE, false, false);
	}

	@TextCommand("pledge_levelup")
	public boolean levelUpPledge(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!player.isClanLeader() || player.getClan() == null)
		{
			return false;
		}

		int clanLevel = player.getClan().getLevel();
		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_PLEDGE_LEVEL_UP);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyPledgeLevelUp(npc, player, clanLevel);
			}
		}
		return true;
	}

	@TextCommand("create_alliance")
	public boolean createAlliance(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		String allianceName;
		if(!params.getQueryArgs().containsKey("alliance_name"))
		{
			return false;
		}

		allianceName = params.getQueryArgs().get("alliance_name");

		if(allianceName.isEmpty())
		{
			return false;
		}

		player.getClan().createAlly(player, allianceName);
		return true;
	}

	@TextCommand("create_pledge")
	public boolean createPledge(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!params.getQueryArgs().containsKey("pledge_name"))
		{
			return false;
		}

		String pledgeName = params.getQueryArgs().get("pledge_name");

		if(pledgeName.isEmpty())
		{
			return false;
		}
		ClanTable.getInstance().createClan(player, pledgeName);
		return true;
	}

	@TextCommand("create_academy")
	public boolean createAcademy(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!params.getQueryArgs().containsKey("academy_name"))
		{
			return false;
		}

		String academyName = params.getQueryArgs().get("academy_name");

		if(academyName.isEmpty())
		{
			return false;
		}

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_ACADEMY_CREATE);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyCreateAcademy(npc, player, player.getClan().createAcademy(player, academyName));
			}
		}
		return true;
	}

	@TextCommand("dismiss_pledge")
	public boolean dismissPledge(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!player.isClanLeader() || player.getClan() == null)
		{
			return false;
		}

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_PLEDGE_DISMISS);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyPledgeDismiss(npc, player);
			}
		}
		return true;
	}

	@TextCommand("revive_pledge")
	public boolean revivePledge(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!player.isClanLeader() || player.getClan() == null)
		{
			return false;
		}

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_PLEDGE_REVIVE);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyPledgeRevive(npc, player);
			}
		}
		return true;
	}

	@TextCommand("create_subpledge")
	public boolean createSubPledge(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		String pledgeName = params.getQueryArgs().get("pledge_name");
		String masterName = params.getQueryArgs().get("master_name");
		int pledgeType = Integer.parseInt(params.getQueryArgs().get("pledge_type"));

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_CREATE_SUBPLEDGE);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyCreateSubPledge(npc, player, pledgeName, masterName, pledgeType);
			}
		}
		return true;
	}

	@TextCommand("rename_subpledge")
	public boolean renameSubPledge(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		int pledgeType = Integer.parseInt(params.getQueryArgs().get("pledge_type"));
		String pledgeName = params.getQueryArgs().get("pledge_name");

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_RENAME_SUBPLEDGE);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyRenameSubPledge(npc, player, pledgeName, pledgeType);
			}
		}
		return true;
	}

	@TextCommand("update_subpledge_master")
	public boolean changeSubpledgeLeader(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!params.getQueryArgs().containsKey("pledge_type") || !params.getQueryArgs().containsKey("master_name"))
		{
			log.warn("Failed to parse update_subpldge_master. Probably bug in command handler. DELETE IF FIXED."); // TODO: DELETE IF OK.
			return false;
		}

		int pledge_type = Integer.parseInt(params.getQueryArgs().get("pledge_type"));
		String master_name = params.getQueryArgs().get("master_name");

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_UPDATE_SUBPLEDGE_MASTER);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyUpdateSubPledgeMaster(npc, player, master_name, pledge_type);
			}
		}
		return true;
	}

	@TextCommand("transfer_master")
	public boolean changeLeader(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		if(!params.getQueryArgs().containsKey("master_name"))
		{
			return false;
		}

		String masterName = params.getQueryArgs().get("master_name");

		if(masterName == null)
		{
			return false;
		}

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_PLEDGE_MASTER_TRANSFER);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyTransferPledgeMaster(npc, player, masterName);
			}
		}
		return true;
	}

	@TextCommand("upgrade_subpledge_member_count")
	public boolean upgradeMemberCount(BypassHandlerParams params)
	{
		L2PcInstance player = params.getPlayer();
		L2Npc npc = player.getLastFolkNPC();
		if(!checkInteractionDistance(player))
		{
			return false;
		}

		int pledgeType = -1;
		try
		{
			pledgeType = Integer.parseInt(params.getQueryArgs().get("pledge_type"));
		}
		catch(Exception ignored)
		{
		}

		if(pledgeType < 0)
		{
			return false;
		}

		List<Quest> quests = npc.getTemplate().getEventQuests(Quest.QuestEventType.ON_UPGRADE_SUBPLEDGE_MEMBER_COUNT);
		if(quests != null)
		{
			for(Quest quest : quests)
			{
				quest.notifyUpgradeSubpledgeMemberCount(npc, player, pledgeType);
			}
		}
		return true;
	}
}