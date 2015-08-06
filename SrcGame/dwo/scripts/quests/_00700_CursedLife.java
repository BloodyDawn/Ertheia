package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

//@author ANZO

public class _00700_CursedLife extends Quest
{
	// Квестовые персонажи
	private static final int ORBYU = 32560;

	// Квестовые монстры
	private static final int[] MOBS = {22602, 22603, 22604, 22605};

	// Квестовые предметы
	private static final int SWALLOWED_SKULL = 13872;
	private static final int SWALLOWED_STERNUM = 13873;
	private static final int SWALLOWED_BONES = 13874;

	public _00700_CursedLife()
	{

		addStartNpc(ORBYU);
		addTalkId(ORBYU);
		addKillId(MOBS);
		questItemIds = new int[]{SWALLOWED_SKULL, SWALLOWED_STERNUM, SWALLOWED_BONES};
	}

	public static void main(String[] args)
	{
		new _00700_CursedLife();
	}

	@Override
	public int getQuestId()
	{
		return 700;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("32560-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("32560-quit.htm"))
		{
			st.unset("cond");
			st.exitQuest(QuestType.REPEATABLE);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(st.getCond() == 1 && ArrayUtils.contains(MOBS, npc.getNpcId()))
		{
			int chance = st.getRandom(100);
			if(chance < 5)
			{
				st.giveItems(SWALLOWED_SKULL, 1);
			}
			else if(chance < 20)
			{
				st.giveItems(SWALLOWED_STERNUM, 1);
			}
			else
			{
				st.giveItems(SWALLOWED_BONES, 1);
			}
			st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return htmltext;
		}

		if(npc.getNpcId() == ORBYU)
		{
			QuestState first = player.getQuestState(_10273_GoodDayToFly.class);
			if(first != null && first.getState() == COMPLETED && st.getState() == CREATED && player.getLevel() >= 75)
			{
				htmltext = "32560-01.htm";
			}
			else
			{
				switch(st.getCond())
				{
					case 0:
						htmltext = "32560-00.htm";
						break;
					case 1:
						long count1 = st.getQuestItemsCount(SWALLOWED_BONES);
						long count2 = st.getQuestItemsCount(SWALLOWED_STERNUM);
						long count3 = st.getQuestItemsCount(SWALLOWED_SKULL);
						if(count1 > 0 || count2 > 0 || count3 > 0)
						{
							long reward = count1 * 500 + count2 * 5000 + count3 * 50000;
							st.takeItems(SWALLOWED_BONES, -1);
							st.takeItems(SWALLOWED_STERNUM, -1);
							st.takeItems(SWALLOWED_SKULL, -1);
							st.rewardItems(PcInventory.ADENA_ID, reward);
							st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
							htmltext = "32560-06.htm";
						}
						else
						{
							htmltext = "32560-04.htm";
						}
						break;
				}
			}
		}
		return htmltext;
	}
}