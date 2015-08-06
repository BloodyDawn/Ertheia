package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.quest.QuestType;

public class _00029_ChestCaughtWithABaitOfEarth extends Quest
{
	int Willie = 31574;
	int Anabel = 30909;

	int SmallPurpleTreasureChest = 6507;
	int SmallGlassBox = 7627;
	int PlatedLeatherGloves = 2455;

	public _00029_ChestCaughtWithABaitOfEarth()
	{
		addStartNpc(Willie);
		addTalkId(Willie, Anabel);
	}

	public static void main(String[] args)
	{
		new _00029_ChestCaughtWithABaitOfEarth();
	}

	@Override
	public int getQuestId()
	{
		return 29;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "31574-04.htm":
				st.startQuest();
				break;
			case "31574-07.htm":
				if(st.hasQuestItems(SmallPurpleTreasureChest))
				{
					st.setCond(2);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.takeItems(SmallPurpleTreasureChest, 1);
					st.giveItems(SmallGlassBox, 1);
				}
				else
				{
					htmltext = "31574-08.htm";
				}
				break;
			case "29_GiveGlassBox":
				if(st.hasQuestItems(SmallGlassBox))
				{
					htmltext = "30909-02.htm";
					st.takeItems(SmallGlassBox, -1);
					st.giveItems(PlatedLeatherGloves, 1);
					st.setCond(0);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else
				{
					htmltext = "30909-03.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = getNoQuestMsg(st.getPlayer());
		QuestStateType id = st.getState();
		if(id == CREATED)
		{
			st.setState(STARTED);
			st.setCond(0);
		}
		int cond = st.getCond();
		id = CREATED;
		if(npcId == Willie)
		{
			if(cond == 0 && id == STARTED)
			{
				int PlayerLevel = st.getPlayer().getLevel();
				if(PlayerLevel < 48)
				{
					QuestState WilliesSpecialBait = st.getPlayer().getQuestState(_00052_WilliesSpecialBait.class);
					if(WilliesSpecialBait != null)
					{
						if(WilliesSpecialBait.isCompleted())
						{
							htmltext = "31574-01.htm";
						}
						else
						{
							htmltext = "31574-02.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else
					{
						htmltext = "31574-03.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
			}
			else if(cond == 1)
			{
				htmltext = "31574-05.htm";
				if(st.getQuestItemsCount(SmallPurpleTreasureChest) == 0)
				{
					htmltext = "31574-06.htm";
				}
			}
			else if(cond == 2)
			{
				htmltext = "31574-09.htm";
			}
		}
		else if(npcId == Anabel)
		{
			if(cond == 2)
			{
				htmltext = "30909-01.htm";
			}
		}
		return htmltext;
	}
}
