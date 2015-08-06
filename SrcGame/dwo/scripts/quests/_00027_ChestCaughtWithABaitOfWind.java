package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00027_ChestCaughtWithABaitOfWind extends Quest
{
	// NPC List
	private static final int Lanosco = 31570;
	private static final int Shaling = 31434;
	//Quest Items
	private static final int StrangeGolemBlueprint = 7625;
	//Items
	private static final int BigBlueTreasureChest = 6500;
	private static final int BlackPearlRing = 880;

	public _00027_ChestCaughtWithABaitOfWind()
	{
		addStartNpc(Lanosco);
		addTalkId(Lanosco, Shaling);
		questItemIds = new int[]{StrangeGolemBlueprint};
	}

	public static void main(String[] args)
	{
		new _00027_ChestCaughtWithABaitOfWind();
	}

	@Override
	public int getQuestId()
	{
		return 27;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "31570-04.htm":
				st.startQuest();
				break;
			case "31570-07.htm":
				if(st.hasQuestItems(BigBlueTreasureChest))
				{
					st.takeItems(BigBlueTreasureChest, 1);
					st.giveItems(StrangeGolemBlueprint, 1);
					st.setCond(2);
					st.setState(STARTED);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					htmltext = "31570-08.htm";
				}
				break;
			case "31434-02.htm":
				if(st.hasQuestItems(StrangeGolemBlueprint))
				{
					st.takeItems(StrangeGolemBlueprint, -1);
					st.giveItems(BlackPearlRing, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else
				{
					htmltext = "31434-03.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Lanosco)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 27)
				{
					QuestState LanoscosSpecialBait = st.getPlayer().getQuestState(_00050_LanoscosSpecialBait.class);
					if(LanoscosSpecialBait != null)
					{
						if(LanoscosSpecialBait.isCompleted())
						{
							htmltext = "31570-01.htm";
						}
						else
						{
							htmltext = "31570-02.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else
					{
						htmltext = "31570-03.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else
				{
					htmltext = "31570-01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "31570-05.htm";
				if(!st.hasQuestItems(BigBlueTreasureChest))
				{
					htmltext = "31570-06.htm";
				}
			}
			else if(cond == 2)
			{
				htmltext = "31570-09.htm";
			}
		}
		else if(npcId == Shaling)
		{
			if(cond == 2)
			{
				htmltext = "31434-01.htm";
			}
		}
		return htmltext;
	}
}