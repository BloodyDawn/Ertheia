package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00036_MakeASewingKit extends Quest
{
	private static final int REINFORCED_STEEL = 7163;
	private static final int ARTISANS_FRAME = 1891;
	private static final int ORIHARUKON = 1893;
	private static final int SEWING_KIT = 7078;

	public _00036_MakeASewingKit()
	{
		addStartNpc(30847);
		addTalkId(30847);
		addKillId(20566);
		questItemIds = new int[]{REINFORCED_STEEL};
	}

	public static void main(String[] args)
	{
		new _00036_MakeASewingKit();
	}

	@Override
	public int getQuestId()
	{
		return 36;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		int cond = st.getCond();
		if(event.equals("30847-1.htm") && cond == 0)
		{
			st.startQuest();
		}
		else if(event.equals("30847-3.htm") && cond == 2)
		{
			st.takeItems(REINFORCED_STEEL, 5);
			st.setCond(3);
		}
		else if(event.equals("30847-4-2.htm"))
		{
			if(st.getQuestItemsCount(ORIHARUKON) >= 10 && st.getQuestItemsCount(ARTISANS_FRAME) >= 10)
			{
				st.takeItems(ORIHARUKON, 10);
				st.takeItems(ARTISANS_FRAME, 10);
				st.giveItems(SEWING_KIT, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.REPEATABLE);
			}
			else
			{
				htmltext = "30847-4-1.htm";
			}
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		if(st.getQuestItemsCount(REINFORCED_STEEL) < 5)
		{
			st.giveItems(REINFORCED_STEEL, 1);
			if(st.getQuestItemsCount(REINFORCED_STEEL) == 5)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
			else
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int cond = st.getCond();
		if(cond == 0 && !st.hasQuestItems(SEWING_KIT))
		{
			if(st.getPlayer().getLevel() >= 60)
			{
				QuestState fwear = st.getPlayer().getQuestState(_00037_PleaseMakeMeFormalWear.class);
				if(fwear != null && fwear.isStarted())
				{
					if(fwear.getCond() == 6)
					{
						htmltext = "30847-0.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else
			{
				htmltext = "30847-5.htm";
			}
		}
		else if(cond == 1 && st.getQuestItemsCount(REINFORCED_STEEL) < 5)
		{
			htmltext = "30847-1r.htm";
		}
		else if(cond == 2 && st.getQuestItemsCount(REINFORCED_STEEL) == 5)
		{
			htmltext = "30847-2.htm";
		}
		else if(cond == 3 && (st.getQuestItemsCount(ORIHARUKON) < 10 || st.getQuestItemsCount(ARTISANS_FRAME) < 10))
		{
			htmltext = "30847-havent.htm";
		}
		else if(cond == 3 && st.getQuestItemsCount(ORIHARUKON) >= 10 && st.getQuestItemsCount(ARTISANS_FRAME) >= 10)
		{
			htmltext = "30847-4-1.htm";
		}
		return htmltext;
	}
}