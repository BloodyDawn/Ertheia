package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;

public class _00508_TheClansReputation extends Quest
{
	// Quest NPC
	private static final int SIR_ERIC_RODEMAI = 30868;
	// Quest Items
	private static final int NUCLEUS_OF_FLAMESTONE_GIANT = 8494; // Nucleus of Flamestone Giant : Nucleus obtained by defeating Flamestone Giant
	private static final int THEMIS_SCALE = 8277; // Themis' Scale : Obtain this scale by defeating Palibati Queen Themis.
	private static final int HISILROME_HEARTH = 14883; // Nucleus of Hekaton Prime : Nucleus obtained by defeating Hekaton Prime
	private static final int TIPHON_SHARD = 8280; // Tiphon Shard : Debris obtained by defeating Tiphon, Gargoyle Lord.
	private static final int GLAKIS_NUCLEUS = 8281; // Glaki's Necleus : Nucleus obtained by defeating Glaki, the last lesser Giant.
	private static final int RAHHAS_FANG = 8282; // Rahha's Fang : Fangs obtained by defeating Rahha.
	private static final int[] items = {
		NUCLEUS_OF_FLAMESTONE_GIANT, THEMIS_SCALE, HISILROME_HEARTH, TIPHON_SHARD, GLAKIS_NUCLEUS, RAHHAS_FANG
	};
	// Quest Raid Bosses
	private static final int FLAMESTONE_GIANT = 25524;
	private static final int PALIBATI_QUEEN_THEMIS = 25252;
	private static final int SHILLIEN_PRIEST_HISILROME = 25478;
	private static final int GARGOYLE_LORD_TIPHON = 25255;
	private static final int LAST_LESSER_GIANT_GLAKI = 25245;
	private static final int RAHHA = 25051;
	private static final int[] bosses = {
		FLAMESTONE_GIANT, PALIBATI_QUEEN_THEMIS, SHILLIEN_PRIEST_HISILROME, GARGOYLE_LORD_TIPHON,
		LAST_LESSER_GIANT_GLAKI, RAHHA
	};
	// minClanPoints , maxClanPoints
	private static int[][] REWARDS = {
		{65, 65}, {143, 143}, {123, 123}, {121, 121}, {71, 71}, {94, 94}
	};
	private static int[][] RADAR = {
		{192346, 21528, -3648}, {191979, 54902, -7658}, {168288, 28368, -3632}, {171762, 55028, -5992},
		{117232, -9476, -3320}, {144218, -5816, -4722}
	};

	public _00508_TheClansReputation()
	{

		addStartNpc(SIR_ERIC_RODEMAI);
		addTalkId(SIR_ERIC_RODEMAI);
		addKillId(bosses);

		questItemIds = items;
	}

	public static void main(String[] args)
	{
		new _00508_TheClansReputation();
	}

	@Override
	public int getQuestId()
	{
		return 508;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		int cond = st.getCond();
		String htmltext = event;
		if(event.equals("30868-0.htm") && cond == 0)
		{
			st.startQuest();
		}
		else if(Util.isDigit(event))
		{
			int choice = Integer.parseInt(event);
			st.set("raid", event);
			htmltext = "30868-" + event + ".htm";
			st.addRadar(RADAR[choice - 1][0], RADAR[choice - 1][1], RADAR[choice - 1][2]);
		}
		else if(event.equals("30868-7.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = null;
		if(killer.isClanLeader())
		{
			st = killer.getQuestState(getClass());
		}
		else
		{
			L2Clan clan = killer.getClan();
			if(clan != null)
			{
				L2ClanMember leader = clan.getLeader();
				if(leader != null)
				{
					L2PcInstance pleader = leader.getPlayerInstance();
					if(pleader != null)
					{
						if(killer.isInsideRadius(pleader, 1600, true, false))
						{
							st = pleader.getQuestState(getClass());
						}
					}
				}
			}
		}
		if(st == null)
		{
			return null;
		}

		int option = st.getInt("raid");
		if(st.getCond() == 1 && st.isStarted() && option < 7 && option > 0)
		{
			int npcId = npc.getNpcId();
			if(npcId == bosses[option] && !st.hasQuestItems(items[option]))
			{
				st.giveItems(items[option], 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		String htmltext = getNoQuestMsg(talker);
		QuestState st = talker.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}

		L2Clan clan = talker.getClan();
		if(talker.getClan() == null || !talker.isClanLeader())
		{
			st.exitQuest(QuestType.REPEATABLE);
			htmltext = "30868-0a.htm";
		}
		else if(talker.getClan().getLevel() < 5)
		{
			st.exitQuest(QuestType.REPEATABLE);
			htmltext = "30868-0b.htm";
		}
		else
		{
			int cond = st.getCond();
			int raid = st.getInt("raid");
			QuestStateType id = st.getState();
			if(id == CREATED && cond == 0)
			{
				htmltext = "30868-0c.htm";
			}
			else if(id == STARTED && cond == 1 && raid < 7 && raid > 0)
			{
				long count = st.getQuestItemsCount(items[raid - 1]);
				int CLAN_POINTS_REWARD = Rnd.get(REWARDS[raid - 1][0], REWARDS[raid - 1][1]);
				if(count == 0)
				{
					htmltext = "30868-" + raid + "a.htm";
				}
				else if(count == 1)
				{
					htmltext = "30868-" + raid + "b.htm";
					st.takeItems(items[raid - 1], 1);
					clan.addReputationScore(CLAN_POINTS_REWARD, true);
					talker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(CLAN_POINTS_REWARD));
					clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				}
			}
		}
		return htmltext;
	}
}