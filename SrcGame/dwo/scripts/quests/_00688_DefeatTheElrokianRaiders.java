package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00688_DefeatTheElrokianRaiders extends Quest
{
	// Шансы дропа
	private static int DROP_CHANCE = 50;

	private static int DINOSAUR_FANG_NECKLACE = 8785;

	public _00688_DefeatTheElrokianRaiders()
	{
		addStartNpc(32105);
		addTalkId(32105);
		addKillId(22214);
		questItemIds = new int[]{DINOSAUR_FANG_NECKLACE};
	}

	public static void main(String[] args)
	{
		new _00688_DefeatTheElrokianRaiders();
	}

	@Override
	public int getQuestId()
	{
		return 688;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		switch(event)
		{
			case "32105-02.htm":
				st.startQuest();
				break;
			case "32105-08.htm":
				if(count > 0)
				{
					st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
					st.giveItems(PcInventory.ADENA_ID, count * 3000);
				}
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.REPEATABLE);
				break;
			case "32105-06.htm":
				st.takeItems(DINOSAUR_FANG_NECKLACE, -1);
				st.giveItems(PcInventory.ADENA_ID, count * 3000);
				break;
			case "32105-07.htm":
				if(count >= 100)
				{
					st.takeItems(DINOSAUR_FANG_NECKLACE, 100);
					st.giveItems(PcInventory.ADENA_ID, 450000);
				}
				else
				{
					htmltext = "32105-04.htm";
				}
				break;
			case "None":
				htmltext = null;
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(Rnd.getChance(DROP_CHANCE) && st.getCond() == 1)
		{
			long numItems = (int) Config.RATE_QUEST_REWARD;
			if(count + numItems > 100)
			{
				numItems = 100 - count;
			}
			if(count + numItems >= 100)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
			st.giveItems(DINOSAUR_FANG_NECKLACE, numItems);
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int cond = st.getCond();
		long count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE);
		if(cond == 0)
		{
			if(st.getPlayer().getLevel() >= 75)
			{
				htmltext = "32105-01.htm";
			}
			else
			{
				htmltext = "32105-00.htm";
				st.exitQuest(QuestType.REPEATABLE);
			}
		}
		else if(cond == 1)
		{
			htmltext = count == 0 ? "32105-04.htm" : "32105-05.htm";
		}
		return htmltext;
	}
}