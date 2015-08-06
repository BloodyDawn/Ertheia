package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;

public class _00905_RefinedDragonBlood extends Quest
{
	private static final int[] SEPARATED_SOUL = {32864, 32865, 32866, 32867, 32868, 32869, 32870};
	private static final int[] BLUE = {
		22852, 22853, 22844, 22845, 22846, 22847
	};

	private static final int[] RED = {
		22848, 22849, 22850, 22851
	};

	private static final int UNREFINED_RED_DRAGON_BLOOD = 21913;
	private static final int UNREFINED_BLUE_DRAGON_BLOOD = 21914;

	private static final int REFINED_RED_DRAGON_BLOOD = 21903;
	private static final int REFINED_BLUE_DRAGON_BLOOD = 21904;

	private static final int DROP_CHANCE = 30;

	public _00905_RefinedDragonBlood()
	{
		addStartNpc(SEPARATED_SOUL);
		addTalkId(SEPARATED_SOUL);
		addKillId(BLUE);
		addKillId(RED);

		questItemIds = new int[]{UNREFINED_BLUE_DRAGON_BLOOD, UNREFINED_RED_DRAGON_BLOOD};
	}

	public static void main(String[] args)
	{
		new _00905_RefinedDragonBlood();
	}

	@Override
	public int getQuestId()
	{
		return 905;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "separated_soul_01_q0905_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(ArrayUtils.contains(SEPARATED_SOUL, npc.getNpcId()))
		{
			if(reply == 1)
			{
				return "separated_soul_01_q0905_04.htm";
			}
			else if(reply == 2 && st.getCond() == 2)
			{
				st.takeItems(UNREFINED_BLUE_DRAGON_BLOOD, -1);
				st.takeItems(UNREFINED_RED_DRAGON_BLOOD, -1);
				st.giveItems(REFINED_RED_DRAGON_BLOOD, 1);
				st.exitQuest(QuestType.DAILY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "separated_soul_01_q0905_12.htm";
			}
			else if(reply == 3 && st.getCond() == 2)
			{
				st.takeItems(UNREFINED_BLUE_DRAGON_BLOOD, -1);
				st.takeItems(UNREFINED_RED_DRAGON_BLOOD, -1);
				st.giveItems(REFINED_BLUE_DRAGON_BLOOD, 1);
				st.exitQuest(QuestType.DAILY);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "separated_soul_01_q0905_13.htm";
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
					if(player.getLevel() >= 83)
					{
						return "separated_soul_01_q0905_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "separated_soul_01_q0905_03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "separated_soul_01_q0905_06.htm";
					}
					if(st.getCond() == 2)
					{
						if(st.getBool("talked"))
						{
							return "separated_soul_01_q0905_08.htm";
						}
						else
						{
							st.set("talked", "true");
							return "separated_soul_01_q0905_07.htm";
						}
					}
					break;
				case COMPLETED:
					return "separated_soul_01_q0905_02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		return !(st != null && st.isCompleted() || player.getLevel() < 83);
	}

	@Override
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isSummon)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.getCond() == 1 && player.isInsideRadius(npc, 1500, false, false))
		{
			if(ArrayUtils.contains(BLUE, npc.getNpcId()) && Rnd.getChance(DROP_CHANCE) && st.getQuestItemsCount(UNREFINED_BLUE_DRAGON_BLOOD) < 10)
			{
				st.giveItem(UNREFINED_BLUE_DRAGON_BLOOD);
			}
			else if(ArrayUtils.contains(RED, npc.getNpcId()) && Rnd.getChance(DROP_CHANCE) && st.getQuestItemsCount(UNREFINED_RED_DRAGON_BLOOD) < 10)
			{
				st.giveItem(UNREFINED_RED_DRAGON_BLOOD);
			}

			if(st.getQuestItemsCount(UNREFINED_BLUE_DRAGON_BLOOD) >= 10 && st.getQuestItemsCount(UNREFINED_RED_DRAGON_BLOOD) >= 10)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}
}