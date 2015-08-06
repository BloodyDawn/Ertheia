package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;

public class _00120_PavelsResearch extends Quest
{
	// NPCs
	private static final int Yumi = 32041;
	private static final int Weather1 = 32042; // north
	private static final int Weather2 = 32043; // east
	private static final int Weather3 = 32044; // west
	private static final int BookShelf = 32045;
	private static final int Stones = 32046;
	private static final int Wendy = 32047;

	// Items
	private static final int Earing = 6324;
	private static final int Report = 8058;
	private static final int Report2 = 8059;
	private static final int Enigma = 8060;
	private static final int Flower = 8290;
	private static final int Heart = 8291;
	private static final int Necklace = 8292;

	public _00120_PavelsResearch()
	{
		addStartNpc(Stones);
		addTalkId(BookShelf, Stones, Weather1, Weather2, Weather3, Wendy, Yumi);
		questItemIds = new int[]{Flower, Report, Report2, Enigma, Heart, Necklace};
	}

	public static void main(String[] args)
	{
		new _00120_PavelsResearch();
	}

	@Override
	public int getQuestId()
	{
		return 120;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return null;
		}
		switch(event)
		{
			case "32041 -03.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-04.htm":
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-12.htm":
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-16.htm":
				st.setCond(16);
				st.giveItems(Enigma, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-22.htm":
				st.setCond(17);
				st.takeItems(Enigma, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32041-32.htm":
				st.takeItems(Necklace, 1);
				st.giveAdena(783729, true);
				st.giveItems(Earing, 1);
				st.addExpAndSp(3447315, 272615);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				break;
			case "32042-06.htm":
				if(st.getCond() == 10)
				{
					if(st.getInt("talk") + st.getInt("talk1") == 2)
					{
						st.setCond(11);
						st.set("talk", "0");
						st.set("talk1", "0");
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						return "32042-03.htm";
					}
				}
				break;
			case "32042-10.htm":
				if(st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 3)
				{
					return "32042-14.htm";
				}
				break;
			case "32042-11.htm":
				if(st.getInt("talk") == 0)
				{
					st.set("talk", "1");
				}
				break;
			case "32042-12.htm":
				if(st.getInt("talk1") == 0)
				{
					st.set("talk1", "1");
				}
				break;
			case "32042-13.htm":
				if(st.getInt("talk2") == 0)
				{
					st.set("talk2", "1");
				}
				break;
			case "32042-15.htm":
				st.setCond(12);
				st.set("talk", "0");
				st.set("talk1", "0");
				st.set("talk2", "0");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32043-06.htm":
				if(st.getCond() == 17)
				{
					if(st.getInt("talk") + st.getInt("talk1") == 2)
					{
						st.setCond(18);
						st.set("talk", "0");
						st.set("talk1", "0");
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						return "32043-03.htm";
					}
				}
				break;
			case "32043-15.htm":
				if(st.getInt("talk") + st.getInt("talk1") == 2)
				{
					return "32043-29.htm";
				}
				break;
			case "32043-18.htm":
				if(st.getInt("talk") == 1)
				{
					return "32043-21.htm";
				}
				break;
			case "32043-20.htm":
				st.set("talk", "1");
				st.playSound(QuestSound.AMBIENT_SOUND_ED_DRONE);
				break;
			case "32043-28.htm":
				st.set("talk1", "1");
				break;
			case "32043-30.htm":
				st.setCond(19);
				st.set("talk", "0");
				st.set("talk1", "0");
				break;
			case "32044-06.htm":
				if(st.getCond() == 20)
				{
					if(st.getInt("talk") + st.getInt("talk1") == 2)
					{
						st.setCond(21);
						st.set("talk", "0");
						st.set("talk1", "0");
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					else
					{
						return "32044-03.htm";
					}
				}
				break;
			case "32044-08.htm":
				if(st.getInt("talk") + st.getInt("talk1") == 2)
				{
					return "32044-11.htm";
				}
				break;
			case "32044-09.htm":
				if(st.getInt("talk") == 0)
				{
					st.set("talk", "1");
				}
				break;
			case "32044-10.htm":
				if(st.getInt("talk1") == 0)
				{
					st.set("talk1", "1");
				}
				break;
			case "32044-17.htm":
				st.setCond(22);
				st.set("talk", "0");
				st.set("talk1", "0");
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32045-02.htm":
				st.setCond(15);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(Report, 1);
				npc.broadcastPacket(new MagicSkillUse(npc, st.getPlayer(), 5073, 5, 1500, 0));
				break;
			case "32046-04.htm":
			case "32046-05.htm":
				st.exitQuest(QuestType.REPEATABLE);
				break;
			case "32046-06.htm":
				if(st.getPlayer().getLevel() >= 70)
				{
					st.startQuest();
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "32046-00.htm";
				}
				break;
			case "32046-08.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32046-12.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(Flower, 1);
				break;
			case "32046-22.htm":
				st.setCond(10);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32046-29.htm":
				st.setCond(13);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32046-35.htm":
				st.setCond(20);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32046-38.htm":
				st.setCond(23);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(Heart, 1);
				break;
			case "32047-06.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-10.htm":
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(Flower, 1);
				break;
			case "32047-15.htm":
				st.setCond(9);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-18.htm":
				st.setCond(14);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32047-26.htm":
				st.setCond(24);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.takeItems(Heart, 1);
				break;
			case "32047-32.htm":
				st.setCond(25);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(Necklace, 1);
				break;
			case "w1_001":
				st.set("talk", "1");
				return "32042-04.htm";
			case "w1_2":
				st.set("talk1", "1");
				return "32042-05.htm";
			case "w2_001":
				st.set("talk", "1");
				return "32043-04.htm";
			case "w2_2":
				st.set("talk1", "1");
				return "32043-05.htm";
			case "w3_001":
				st.set("talk", "1");
				return "32044-04.htm";
			case "w3_2":
				st.set("talk1", "1");
				return "32044-05.htm";
		}
		return event;
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
		if(st.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}
		switch(npc.getNpcId())
		{
			case Stones:
				if(st.getState() == CREATED)
				{
					QuestState Pavel = player.getQuestState(_00114_ResurrectionOfAnOldButler.class);
					if(Pavel != null)
					{
						if(player.getLevel() >= 70 && Pavel.isCompleted())
						{
							htmltext = "32046-01.htm";
						}
						else
						{
							htmltext = "32046-00.htm";
							st.exitQuest(QuestType.REPEATABLE);
						}
					}
					else
					{
						htmltext = "32046-00.htm";
						st.exitQuest(QuestType.REPEATABLE);
					}
				}
				else
				{
					switch(st.getCond())
					{
						case 1:
							htmltext = "32046-06.htm";
							break;
						case 2:
							htmltext = "32046-09.htm";
							break;
						case 5:
							htmltext = "32046-10.htm";
							break;
						case 6:
							htmltext = "32046-13.htm";
							break;
						case 9:
							htmltext = "32046-14.htm";
							break;
						case 10:
							htmltext = "32046-23.htm";
							break;
						case 12:
							htmltext = "32046-26.htm";
							break;
						case 13:
							htmltext = "32046-30.htm";
							break;
						case 19:
							htmltext = "32046-31.htm";
							break;
						case 20:
							htmltext = "32046-36.htm";
							break;
						case 22:
							htmltext = "32046-37.htm";
							break;
						case 23:
							htmltext = "32046-39.htm";
							break;
					}
				}
				break;
			case Wendy:
				switch(st.getCond())
				{
					case 2:
					case 3:
					case 4:
						htmltext = "32047-01.htm";
						break;
					case 5:
						htmltext = "32047-07.htm";
						break;
					case 6:
						htmltext = "32047-08.htm";
						break;
					case 7:
						htmltext = "32047-11.htm";
						break;
					case 8:
						htmltext = "32047-12.htm";
						break;
					case 9:
						htmltext = "32047-15.htm";
						break;
					case 13:
						htmltext = "32047-16.htm";
						break;
					case 14:
						htmltext = "32047-19.htm";
						break;
					case 15:
						htmltext = "32047-20.htm";
						break;
					case 23:
						htmltext = "32047-21.htm";
						break;
					case 24:
						htmltext = "32047-26.htm";
						break;
					case 25:
						htmltext = "32047-33.htm";
						break;
				}
				break;
			case Yumi:
				switch(st.getCond())
				{
					case 2:
						htmltext = "32041-01.htm";
						break;
					case 3:
						htmltext = "32041-05.htm";
						break;
					case 4:
						htmltext = "32041-06.htm";
						break;
					case 7:
						htmltext = "32041-07.htm";
						break;
					case 8:
						htmltext = "32041-13.htm";
						break;
					case 15:
						htmltext = "32041-14.htm";
						break;
					case 16:
						htmltext = !st.hasQuestItems(Report2) ? "32041-17.htm" : "32041-18.htm";
						break;
					case 17:
						htmltext = "32041-22.htm";
						break;
					case 25:
						htmltext = "32041-26.htm";
						break;
				}
				break;
			case Weather1:
				switch(st.getCond())
				{
					case 10:
						htmltext = "32042-01.htm";
						break;
					case 11:
						htmltext = st.getInt("talk") + st.getInt("talk1") + st.getInt("talk2") == 3 ? "32042-14.htm" : "32042-06.htm";
						break;
					case 12:
						htmltext = "32042-15.htm";
						break;
				}
				break;
			case Weather2:
				switch(st.getCond())
				{
					case 17:
						htmltext = "32043-01.htm";
						break;
					case 18:
						htmltext = st.getInt("talk") + st.getInt("talk1") == 2 ? "32043-29.htm" : "32043-06.htm";
						break;
					case 19:
						htmltext = "32043-30.htm";
						break;
				}
				break;
			case Weather3:
				switch(st.getCond())
				{
					case 20:
						htmltext = "32044-01.htm";
						break;
					case 21:
						htmltext = "32044-06.htm";
						break;
					case 22:
						htmltext = "32044-18.htm";
						break;
				}
				break;
			case BookShelf:
				switch(st.getCond())
				{
					case 14:
						htmltext = "32045-01.htm";
						break;
					case 15:
						htmltext = "32045-03.htm";
						break;
				}
				break;
		}
		return htmltext;
	}
}