package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00028_ChestCaughtWithABaitOfIcyAir extends Quest
{
	int OFulle = 31572;
	int Kiki = 31442;

	int BigYellowTreasureChest = 6503;
	int KikisLetter = 7626;
	int ElvenRing = 881;

	public _00028_ChestCaughtWithABaitOfIcyAir()
	{
		addStartNpc(OFulle);
		addTalkId(OFulle, Kiki);
	}

	public static void main(String[] args)
	{
		new _00028_ChestCaughtWithABaitOfIcyAir();
	}

	@Override
	public int getQuestId()
	{
		return 28;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "31572-04.htm":
				st.startQuest();
				break;
			case "31572-07.htm":
				if(st.hasQuestItems(BigYellowTreasureChest))
				{
					st.setCond(2);
					st.takeItems(BigYellowTreasureChest, 1);
					st.giveItems(KikisLetter, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				else
				{
					htmltext = "31572-08.htm";
				}
				break;
			case "31442-02.htm":
				if(st.hasQuestItems(KikisLetter))
				{
					htmltext = "31442-02.htm";
					st.takeItems(KikisLetter, -1);
					st.giveItems(ElvenRing, 1);
					st.setCond(0);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				else
				{
					htmltext = "31442-03.htm";
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
		if(st.isCreated())
		{
			st.setState(STARTED);
			st.setCond(0);
		}
		int cond = st.getCond();
		if(npcId == OFulle)
		{
			if(cond == 0 && st.isStarted())
			{
				int PlayerLevel = st.getPlayer().getLevel();
				if(PlayerLevel < 36)
				{
					QuestState OFullesSpecialBait = st.getPlayer().getQuestState(_00051_OFullesSpecialBait.class);
					if(OFullesSpecialBait != null)
					{
						if(OFullesSpecialBait.isCompleted())
						{
							htmltext = "31572-01.htm";
						}
						else
						{
							htmltext = "31572-02.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else
					{
						htmltext = "31572-02.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else
				{
					htmltext = "31572-01.htm";
				}
			}
			else if(cond == 1)
			{
				htmltext = "31572-05.htm";
				if(!st.hasQuestItems(BigYellowTreasureChest))
				{
					htmltext = "31572-06.htm";
				}
			}
			else if(cond == 2)
			{
				htmltext = "31572-09.htm";
			}
		}
		else if(npcId == Kiki)
		{
			if(cond == 2)
			{
				htmltext = "31442-01.htm";
			}
		}
		return htmltext;
	}
}