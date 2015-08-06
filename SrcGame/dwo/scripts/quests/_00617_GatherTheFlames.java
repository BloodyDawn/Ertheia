package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00617_GatherTheFlames extends Quest
{
	// Квестовые персонажи
	private static final int HILDA = 31271;
	private static final int VULCAN = 31539;
	private static final int ROONEY = 32049;
	// Квестовые предметы
	private static final int TORCH = 7264;
	private static final int[][] DROPLIST = {
		{21381, 51}, {21653, 51}, {21387, 53}, {21655, 53}, {21390, 56}, {21656, 69}, {21389, 55}, {21388, 53},
		{21383, 51}, {21392, 56}, {21382, 60}, {21654, 52}, {21384, 64}, {21394, 51}, {21395, 56}, {21385, 52},
		{21391, 55}, {21393, 58}, {21657, 57}, {21386, 52}, {21652, 49}, {21378, 49}, {21376, 48}, {21377, 48},
		{21379, 59}, {21380, 49}, {22634, 49}, {22635, 49}, {22636, 49}, {22637, 51}, {22638, 51}, {22639, 53},
		{22640, 53}, {22641, 53}, {22642, 55}, {22643, 55}, {22644, 55}, {22645, 57}, {22646, 57}, {22647, 57},
		{22648, 60}, {22649, 61}, {21395, 61}
	};

	private static final int[] REWARDS = {6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};
	private static final int[] REWARDS2 = {6882, 6884, 6886, 6888, 6892, 6894, 6896, 6898, 6900, 7581};

	// Выставить в 1, если нужно выдавать 100% рецепты.
	// TODO:Сделать во всех квестах c рецептами и вывести в конфиг
	private static final boolean ALT_RP100 = false;

	public _00617_GatherTheFlames()
	{
		addStartNpc(VULCAN);
		addStartNpc(HILDA);
		addTalkId(VULCAN);
		addTalkId(ROONEY);
		questItemIds = new int[]{TORCH};
		for(int[] dropdata : DROPLIST)
		{
			addKillId(dropdata[0]);
		}
	}

	public static void main(String[] args)
	{
		new _00617_GatherTheFlames();
	}

	@Override
	public int getQuestId()
	{
		return 617;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		long torches = st.getQuestItemsCount(TORCH);

		if(event.equals("31539-03.htm"))
		{
			if(st.getPlayer().getLevel() >= 74)
			{
				st.startQuest();
			}
			else
			{
				htmltext = "31539-02.htm";
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		else if(event.equals("31271-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("31539-05.htm") && torches >= 1000)
		{
			htmltext = "31539-07.htm";
			st.takeItems(TORCH, 1000);
			if(ALT_RP100)
			{
				st.giveItems(REWARDS2[st.getRandom(REWARDS2.length)], 1);
			}
			else
			{
				st.giveItems(REWARDS[st.getRandom(REWARDS.length)], 1);
			}
		}
		else if(event.equals("31539-08.htm"))
		{
			st.takeItems(TORCH, -1);
			st.exitQuest(QuestType.REPEATABLE);
		}
		else if(event.startsWith("reward"))
		{
			int rewardId = Integer.parseInt(event.substring(7));
			if(rewardId > 0)
			{
				if(torches >= 1200)
				{
					st.takeItems(TORCH, 1200);
					if(ALT_RP100)
					{
						st.giveItems(rewardId + 1, 1);
					}
					else
					{
						st.giveItems(rewardId, 1);
					}
					return null;
				}
				else
				{
					htmltext = "Неверное количество предметов!";
				}
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		L2PcInstance partyMember = getRandomPartyMemberState(player, STARTED);
		if(partyMember == null)
		{
			return super.onKill(npc, st);
		}
		st = partyMember.getQuestState(getClass());
		long torches = st.getQuestItemsCount(TORCH);

		for(int[] dropdata : DROPLIST)
		{
			if(npc.getNpcId() == dropdata[0] && Rnd.getChance(dropdata[1]))
			{
				st.giveItems(TORCH, 1 * (long) Config.RATE_QUEST_DROP);
				if(torches == 999 || torches == 1199)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}

		return super.onKill(npc, st);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		QuestStateType id = st.getState();
		L2PcInstance player = st.getPlayer();
		long torches = st.getQuestItemsCount(TORCH);
		if(npcId == VULCAN)
		{
			if(id == CREATED)
			{
				if(player.getLevel() < 74)
				{
					st.exitQuest(QuestType.REPEATABLE);
					htmltext = "31539-02.htm";
				}
				else
				{
					htmltext = "31539-01.htm";
				}
			}
			else
			{
				htmltext = torches < 1000 ? "31539-05.htm" : "31539-04.htm";
			}
		}
		else if(npcId == HILDA)
		{
			if(id == CREATED)
			{
				if(player.getLevel() < 74)
				{
					st.exitQuest(QuestType.REPEATABLE);
					htmltext = "31271-01.htm";
				}
				else
				{
					htmltext = "31271-02.htm";
				}
			}
			else
			{
				htmltext = "31271-04.htm";
			}
		}
		else if(npcId == ROONEY && id == STARTED)
		{
			htmltext = torches >= 1200 ? "32049-01.htm" : "32049-02.htm";
		}
		return htmltext;
	}
}
