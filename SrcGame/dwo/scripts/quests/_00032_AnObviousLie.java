package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00032_AnObviousLie extends Quest
{
	//NPC
	int MAXIMILIAN = 30120;
	int GENTLER = 30094;
	int MIKI_THE_CAT = 31706;
	//MOBS
	int ALLIGATOR = 20135;
	//CHANCE FOR DROP
	int CHANCE_FOR_DROP = 30;
	//ITEMS
	int MAP = 7165;
	int MEDICINAL_HERB = 7166;
	int SPIRIT_ORES = 3031;
	int JEWEL_PART = 34992;
	int ARMOR_PART = 34991;
	//REWARDS
	int RACCOON_EAR = 7680;
	int CAT_EAR = 6843;
	int RABBIT_EAR = 7683;

	public _00032_AnObviousLie()
	{
		addStartNpc(MAXIMILIAN);
		addTalkId(MAXIMILIAN, GENTLER, MIKI_THE_CAT);
		addKillId(ALLIGATOR);
		questItemIds = new int[]{MEDICINAL_HERB, MAP};
	}

	public static void main(String[] args)
	{
		new _00032_AnObviousLie();
	}

	@Override
	public int getQuestId()
	{
		return 32;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;
		switch(event)
		{
			case "30120-1.htm":
				st.startQuest();
				break;
			case "30094-1.htm":
				st.giveItems(MAP, 1);
				st.setCond(2);
				break;
			case "31706-1.htm":
				st.takeItems(MAP, 1);
				st.setCond(3);
				break;
			case "30094-4.htm":
				if(st.getQuestItemsCount(MEDICINAL_HERB) > 19)
				{
					st.takeItems(MEDICINAL_HERB, 20);
					st.setCond(5);
				}
				else
				{
					htmltext = "У Вас недостаточно материалов.";
					st.setCond(3);
				}
				break;
			case "30094-7.htm":
				if(st.getQuestItemsCount(SPIRIT_ORES) >= 500)
				{
					st.takeItems(SPIRIT_ORES, 500);
					st.setCond(6);
				}
				else
				{
					htmltext = "У Вас недостаточно материалов.";
				}
				break;
			case "31706-4.htm":
				st.setCond(7);
				break;
			case "30094-10.htm":
				st.setCond(8);
				break;
			case "30094-13.htm":
				if(st.getQuestItemsCount(JEWEL_PART) < 100 || st.getQuestItemsCount(ARMOR_PART) < 150)
				{
					htmltext = "У Вас недостаточно материалов.";
				}
				break;
			case "cat":
			case "racoon":
			case "rabbit":
				if(st.getCond() == 8 && st.getQuestItemsCount(JEWEL_PART) >= 100 && st.getQuestItemsCount(ARMOR_PART) >= 150)
				{
					st.takeItems(JEWEL_PART, 100);
					st.takeItems(ARMOR_PART, 150);
					if(event.equalsIgnoreCase("cat"))
					{
						st.giveItems(CAT_EAR, 1);
					}
					else if(event.equalsIgnoreCase("racoon"))
					{
						st.giveItems(RACCOON_EAR, 1);
					}
					else if(event.equalsIgnoreCase("rabbit"))
					{
						st.giveItems(RABBIT_EAR, 1);
					}
					st.unset("cond");
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					htmltext = "30094-14.htm";
					st.exitQuest(QuestType.ONE_TIME);
				}
				else
				{
					htmltext = "У Вас недостаточно материалов.";
				}
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return super.onKill(npc, player, isPet);
		}
		long count = st.getQuestItemsCount(MEDICINAL_HERB);
		if(Rnd.getChance(CHANCE_FOR_DROP) && st.getCond() == 3)
		{
			if(count < 20)
			{
				st.giveItems(MEDICINAL_HERB, 1);
				if(count == 19)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(4);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == MAXIMILIAN)
		{
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() >= 45)
				{
					htmltext = "30120-0.htm";
				}
				else
				{
					htmltext = "30120-0a.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(cond == 1)
			{
				htmltext = "30120-2.htm";
			}
		}
		else if(npcId == GENTLER)
		{
			switch(cond)
			{
				case 1:
					htmltext = "30094-0.htm";
					break;
				case 2:
					htmltext = "30094-2.htm";
					break;
				case 3:
					htmltext = "30094-forgot.htm";
					break;
				case 4:
					htmltext = "30094-3.htm";
					break;
				case 5:
					htmltext = st.getQuestItemsCount(SPIRIT_ORES) >= 500 ? "30094-6.htm" : "30094-5.htm";
					break;
				case 6:
					htmltext = "30094-8.htm";
					break;
				case 7:
					htmltext = "30094-9.htm";
					break;
				case 8:
					htmltext = st.getQuestItemsCount(JEWEL_PART) >= 100 && st.getQuestItemsCount(ARMOR_PART) >= 150 ? "30094-12.htm" : "30094-11.htm";
					break;
			}
		}
		else if(npcId == MIKI_THE_CAT)
		{
			switch(cond)
			{
				case 2:
					htmltext = "31706-0.htm";
					break;
				case 3:
					htmltext = "31706-2.htm";
					break;
				case 6:
					htmltext = "31706-3.htm";
					break;
				case 7:
					htmltext = "31706-5.htm";
					break;
			}
		}
		return htmltext;
	}
}