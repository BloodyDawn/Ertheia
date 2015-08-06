package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00455_WingsOfSand extends Quest
{
	private static final int[] SEPARATED_SOUL = {32864, 32865, 32866, 32867, 32868, 32869, 32870};
	private static final int LARGE_BABY_DRAGON = 17250;
	private static final int DROP_CHANCE = 30;
	private static final int[] BOSS = {
		25718, 25719, 25720, 25721, 25722, 25723, 25724
	};

	private static final int[][] PIECES = {
		{15771, 1, 2}, {15770, 1, 2}, {15769, 1, 2}, {15637, 1, 2}, {15636, 1, 2}, {15634, 1, 2}, {15635, 1, 2},
		{15644, 1, 2}, {15642, 1, 2}, {15640, 1, 2}, {15643, 1, 2}, {15641, 1, 2}, {15639, 1, 2}, {15638, 1, 2},
		{15660, 1, 2}, {15663, 1, 2}, {15666, 1, 2}, {15667, 1, 2}, {15668, 1, 2}, {15669, 1, 2}, {15661, 1, 2},
		{15664, 1, 2}, {15670, 1, 2}, {15671, 1, 2}, {15672, 1, 2}, {15662, 1, 2}, {15665, 1, 2}, {15673, 1, 2},
		{15674, 1, 2}, {15675, 1, 2}, {15691, 1, 2}
	};

	private static final int[][] RECIPES = {
		{15818, 1}, {15817, 1}, {15815, 1}, {15816, 1}, {15825, 1}, {15823, 1}, {15821, 1}, {15824, 1}, {15822, 1},
		{15820, 1}, {15819, 1}, {15792, 1}, {15795, 1}, {15798, 1}, {15801, 1}, {15804, 1}, {15808, 1}, {15793, 1},
		{15796, 1}, {15799, 1}, {15802, 1}, {15805, 1}, {15794, 1}, {15797, 1}, {15800, 1}, {15803, 1}, {15806, 1},
		{15807, 1}, {15811, 1}, {15810, 1}, {15809, 1}
	};

	private static final int[][] ATT_CRY = {
		{9552, 1}, {9553, 1}, {9554, 1}, {9555, 1}, {9557, 1}, {9556, 1}
	};

	private static final int[][] BEWS_BEAS = {
		{6577, 1}, {6578, 1}
	};

	public _00455_WingsOfSand()
	{
		addStartNpc(SEPARATED_SOUL);
		addTalkId(SEPARATED_SOUL);
		addKillId(BOSS);
		questItemIds = new int[]{LARGE_BABY_DRAGON};
	}

	public static void main(String[] args)
	{
		new _00455_WingsOfSand();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && player.isInsideRadius(npc, 2000, false, false))
		{
			int random = Rnd.get(100);

			if(random < 10)
			{
				int[] rnd_reward = BEWS_BEAS[Rnd.get(0, BEWS_BEAS.length - 1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if(random < 30)
			{
				int[] rnd_reward = RECIPES[Rnd.get(0, RECIPES.length - 1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if(random < 50)
			{
				int[] rnd_reward = ATT_CRY[Rnd.get(0, ATT_CRY.length - 1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else
			{
				int[] rnd_reward = PIECES[Rnd.get(0, PIECES.length - 1)];
				st.giveItems(rnd_reward[0], Rnd.get(rnd_reward[1], rnd_reward[2]));
			}
		}
	}

	private void rewardPlayer2(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && player.isInsideRadius(npc, 2000, false, false))
		{
			int random = Rnd.get(100);

			if(random < 10)
			{
				int[] rnd_reward = BEWS_BEAS[Rnd.get(0, BEWS_BEAS.length - 1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if(random < 40)
			{
				int[] rnd_reward = RECIPES[Rnd.get(0, RECIPES.length - 1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else if(random < 80)
			{
				int[] rnd_reward = ATT_CRY[Rnd.get(0, ATT_CRY.length - 1)];
				st.giveItems(rnd_reward[0], rnd_reward[1]);
			}
			else
			{
				int[] rnd_reward = PIECES[Rnd.get(0, PIECES.length - 1)];
				st.giveItems(rnd_reward[0], Rnd.get(rnd_reward[1], rnd_reward[2]));
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 455;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "separated_soul_01_q0455_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(ArrayUtils.contains(SEPARATED_SOUL, npc.getNpcId()))
		{
			switch(reply)
			{
				case 1:
					return "separated_soul_01_q0455_04.htm";
				case 2:
					return "separated_soul_01_q0455_05.htm";
				case 3:
					return "separated_soul_01_q0455_06.htm";
				case 4:
					rewardPlayer(npc, player);
					st.takeItems(LARGE_BABY_DRAGON, -1);
					st.exitQuest(QuestType.DAILY);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "separated_soul_01_q0455_10.htm";
				case 5:
					return "separated_soul_01_q0455_11.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		executeForEachPlayer(player, npc, isPet, true, false);
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(ArrayUtils.contains(SEPARATED_SOUL, npc.getNpcId()))
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() < 80)
					{
						return "separated_soul_01_q0455_03.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "separated_soul_01_q0455_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "separated_soul_01_q0455_08.htm";
					}
					if(st.getCond() == 2)
					{
						return "separated_soul_01_q0455_09.htm";
					}
					if(st.getCond() == 3)
					{
						st.takeItems(LARGE_BABY_DRAGON, -1);
						rewardPlayer2(npc, player);
						st.exitQuest(QuestType.DAILY);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						return "separated_soul_01_q0455_12.htm";
					}
					break;
				case COMPLETED:
					return "separated_soul_01_q0455_02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		return !(st != null && st.isCompleted() || player.getLevel() < 80);
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());

		if(ArrayUtils.contains(BOSS, npc.getNpcId()) && st != null)
		{
			if(Rnd.getChance(DROP_CHANCE))
			{
				if(st.isStarted() && player.isInsideRadius(npc, 1500, false, false))
				{
					if(st.getCond() == 1 && !st.hasQuestItems(LARGE_BABY_DRAGON))
					{
						st.giveItem(LARGE_BABY_DRAGON);
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else if(st.getCond() == 2 && st.hasQuestItems(LARGE_BABY_DRAGON))
					{
						st.giveItem(LARGE_BABY_DRAGON);
						st.setCond(3);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
				}
			}
		}
	}
}