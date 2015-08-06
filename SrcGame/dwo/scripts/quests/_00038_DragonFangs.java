package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00038_DragonFangs extends Quest
{
	//NPC
	public static final int ROHMER = 30344;
	public static final int LUIS = 30386;
	public static final int IRIS = 30034;

	//QUEST ITEM
	public static final int FEATHER_ORNAMENT = 7173;
	public static final int TOOTH_OF_TOTEM = 7174;
	public static final int LETTER_OF_IRIS = 7176;
	public static final int LETTER_OF_ROHMER = 7177;
	public static final int TOOTH_OF_DRAGON = 7175;

	//MOBS
	public static final int LANGK_LIZARDMAN_LIEUTENANT = 20357;
	public static final int LANGK_LIZARDMAN_SENTINEL = 21100;
	public static final int LANGK_LIZARDMAN_LEADER = 20356;
	public static final int LANGK_LIZARDMAN_SHAMAN = 21101;

	//CHANCE FOR DROP
	public static final int CHANCE_FOR_QUEST_ITEMS = 100; // 100%???

	//REWARD
	public static final int BONE_HELMET = 45;
	public static final int ASSAULT_BOOTS = 1125;
	public static final int BLUE_BUCKSKIN_BOOTS = 1123;

	public _00038_DragonFangs()
	{
		addStartNpc(LUIS);
		addTalkId(IRIS, ROHMER);
		addKillId(LANGK_LIZARDMAN_LEADER, LANGK_LIZARDMAN_SHAMAN, LANGK_LIZARDMAN_SENTINEL, LANGK_LIZARDMAN_LIEUTENANT);
		questItemIds = new int[]{TOOTH_OF_TOTEM, LETTER_OF_IRIS, LETTER_OF_ROHMER, TOOTH_OF_DRAGON, FEATHER_ORNAMENT};
	}

	public static void main(String[] args)
	{
		new _00038_DragonFangs();
	}

	@Override
	public int getQuestId()
	{
		return 38;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		int cond = st.getCond();
		switch(event)
		{
			case "30386-3.htm":
				if(cond == 0)
				{
					st.startQuest();
				}
				break;
			case "30386-5.htm":
				if(cond == 2)
				{
					st.setCond(3);
					st.takeItems(FEATHER_ORNAMENT, 100);
					st.giveItems(TOOTH_OF_TOTEM, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
			case "30034-2.htm":
				if(cond == 3)
				{
					st.setCond(4);
					st.takeItems(TOOTH_OF_TOTEM, 1);
					st.giveItems(LETTER_OF_IRIS, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
			case "30344-2.htm":
				if(cond == 4)
				{
					st.setCond(5);
					st.takeItems(LETTER_OF_IRIS, 1);
					st.giveItems(LETTER_OF_ROHMER, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
			case "30034-6.htm":
				if(cond == 5)
				{
					st.setCond(6);
					st.takeItems(LETTER_OF_ROHMER, 1);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				}
				break;
			case "30034-9.htm":
				if(cond == 7)
				{
					st.takeItems(TOOTH_OF_DRAGON, 50);
					int luck = Rnd.get(3);
					if(luck == 0)
					{
						st.giveItems(BLUE_BUCKSKIN_BOOTS, 1);
						st.giveItems(PcInventory.ADENA_ID, 1500);
					}
					if(luck == 1)
					{
						st.giveItems(BONE_HELMET, 1);
						st.giveItems(PcInventory.ADENA_ID, 5200);
					}
					if(luck == 2)
					{
						st.giveItems(ASSAULT_BOOTS, 1);
						st.giveItems(PcInventory.ADENA_ID, 1500);
					}
					st.addExpAndSp(435117, 23977);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.exitQuest(QuestType.ONE_TIME);
				}
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		boolean chance = Rnd.getChance(CHANCE_FOR_QUEST_ITEMS);
		int cond = st.getCond();
		if(npcId == 20357 || npcId == 21100)
		{
			if(cond == 1 && chance && st.getQuestItemsCount(FEATHER_ORNAMENT) < 100)
			{
				st.giveItems(FEATHER_ORNAMENT, 1);
				if(st.getQuestItemsCount(FEATHER_ORNAMENT) == 100)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(2);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		if(npcId == 20356 || npcId == 21101)
		{
			if(cond == 6 && chance && st.getQuestItemsCount(TOOTH_OF_DRAGON) < 50)
			{
				st.giveItems(TOOTH_OF_DRAGON, 1);
				if(st.getQuestItemsCount(TOOTH_OF_DRAGON) == 50)
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.setCond(7);
				}
				else
				{
					st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		String htmltext = getNoQuestMsg(st.getPlayer());
		int cond = st.getCond();
		if(npcId == LUIS)
		{
			switch(cond)
			{
				case 0:
					if(st.getPlayer().getLevel() < 19)
					{
						htmltext = "30386-2.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
					else
					{
						htmltext = "30386-1.htm";
					}
					break;
				case 1:
					htmltext = "30386-6.htm";
					break;
				case 2:
					if(st.getQuestItemsCount(FEATHER_ORNAMENT) == 100)
					{
						htmltext = "30386-4.htm";
					}
					break;
				case 3:
					htmltext = "30386-7.htm";
					break;
			}
		}
		else if(npcId == IRIS)
		{
			switch(cond)
			{
				case 3:
					if(st.hasQuestItems(TOOTH_OF_TOTEM))
					{
						htmltext = "30034-1.htm";
					}
					break;
				case 4:
					htmltext = "30034-3.htm";
					break;
				case 5:
					if(st.hasQuestItems(LETTER_OF_ROHMER))
					{
						htmltext = "30034-5.htm";
					}
					break;
				case 6:
					htmltext = "30034-7.htm";
					break;
				case 7:
					if(st.getQuestItemsCount(TOOTH_OF_DRAGON) == 50)
					{
						htmltext = "30034-8.htm";
					}
					break;
			}
		}
		else if(npcId == ROHMER)
		{
			switch(cond)
			{
				case 4:
					if(st.hasQuestItems(LETTER_OF_IRIS))
					{
						htmltext = "30344-1.htm";
					}
					break;
				case 5:
					htmltext = "30344-3.htm";
					break;
			}
		}
		return htmltext;
	}
}