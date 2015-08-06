package dwo.scripts.quests;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import org.apache.commons.lang3.ArrayUtils;

public class _00504_CompetitionfortheBanditStronghold extends Quest
{
	// Quest reward item
	private static final int TARLK_AMULET = 4332;
	private static final int TROPHY_OF_ALLIANCE = 5009;

	// Quest npc
	private static final int MESSENGER = 35437;
	private static final int[] MOBS = {20570, 20571, 20572, 20573, 20574};

	private static final ClanHallSiegable BANDIT_STRONGHOLD = ClanHallSiegeManager.getInstance().getSiegableHall(35);

	public _00504_CompetitionfortheBanditStronghold()
	{
		addStartNpc(MESSENGER);
		addTalkId(MESSENGER);
		addKillId(MOBS);
	}

	public static void main(String[] args)
	{
		new _00504_CompetitionfortheBanditStronghold();
	}

	@Override
	public int getQuestId()
	{
		return 504;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(!BANDIT_STRONGHOLD.isInSiege())
		{
			return null;
		}

		QuestState st = killer.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(!ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			return null;
		}

		if(st.isStarted() && st.getCond() == 1)
		{
			st.giveItems(TARLK_AMULET, 1);
			if(st.getQuestItemsCount(TARLK_AMULET) < 30)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String result = "azit_messenger_q0504_01.htm";
		QuestState st = player.getQuestState(getClass());
		L2Clan clan = player.getClan();

		if(st == null)
		{
			result = getNoQuestMsg(player);
		}
		else if(!BANDIT_STRONGHOLD.isWaitingBattle())
		{
			sendDatePage("azit_messenger_q0504_09.htm", player, npc);
			result = null;
		}
		else if(player.getClan() == null || player.getClan().getLevel() < 4)
		{
			result = "azit_messenger_q0504_04.htm";
		}
		else if(!player.isClanLeader())
		{
			result = "azit_messenger_q0504_05.htm";
		}
		else if(clan.getClanhallId() > 0 || clan.getFortId() > 0 || clan.getCastleId() > 0)
		{
			result = "azit_messenger_q0504_10.htm";
		}
		else
		{
			switch(st.getState())
			{
				case CREATED:
					if(BANDIT_STRONGHOLD.getSiege().getAttackers().size() >= 5)
					{
						result = "35437-3.htm";
					}
					else
					{
						result = "azit_messenger_q0504_02.htm";
						st.startQuest();
					}
					break;
				case STARTED:
					if(st.getQuestItemsCount(TARLK_AMULET) < 30)
					{
						result = "azit_messenger_q0504_07.htm";
					}
					else
					{
						st.takeItems(TARLK_AMULET, 30);
						st.rewardItems(TROPHY_OF_ALLIANCE, 1);
						st.exitQuest(QuestType.REPEATABLE);
						result = "azit_messenger_q0504_08.htm";
					}
					break;
				case COMPLETED:
					result = "azit_messenger_q0504_07a.htm";
					break;
			}
		}
		return result;
	}

	private void sendDatePage(String page, L2PcInstance player, L2Npc npc)
	{
		String result = HtmCache.getInstance().getHtmQuest(player.getLang(), "quests/504_CompetitionfortheBanditStronghold/" + page + ".htm");
		if(result != null)
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(npc.getObjectId());
			msg.setHtml(result);
			msg.replace("%nextSiege%", BANDIT_STRONGHOLD.getSiegeDate().getTime().toString());
			msg.replace("%objectId%", String.valueOf(npc.getObjectId()));

			player.sendPacket(msg);
		}
	}
}