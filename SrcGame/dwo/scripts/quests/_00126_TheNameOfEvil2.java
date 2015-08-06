package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00126_TheNameOfEvil2 extends Quest
{
	private static final int ASAMAH = 32115;
	private static final int ULU_KAIMU = 32119;
	private static final int BALU_KAIMU = 32120;
	private static final int CHUTA_KAIMU = 32121;
	private static final int WARRIORS_GRAVE = 32122;
	private static final int SHILENS_STONE_STATUE = 32109;
	private static final int MUSHIKA = 32114;
	private static final int[] NPCS = {
		ASAMAH, ULU_KAIMU, BALU_KAIMU, CHUTA_KAIMU, WARRIORS_GRAVE, SHILENS_STONE_STATUE, MUSHIKA
	};

	private static final String FIRST_VERSE = "domifasolfa";
	private static final String SECOND_VERSE = "fasoltisolfa";
	private static final String THIRD_VERSE = "solfamifami";

	private static final int BONE_POWDER = 8783;

	int trys;
	String versetext = "";

	public _00126_TheNameOfEvil2()
	{
		addStartNpc(ASAMAH);
		addTalkId(ASAMAH);
		addTalkId(NPCS);
		questItemIds = new int[]{BONE_POWDER};
	}

	public static void main(String[] args)
	{
		new _00126_TheNameOfEvil2();
	}

	private String CheckVerse(String versetxt, QuestState st)
	{
		int cond = st.getCond();

		if(cond == 14 && versetxt.equalsIgnoreCase(FIRST_VERSE))
		{
			st.setCond(15);
			versetext = "";
			return "32122-first-correct.htm";
		}
		else if(cond == 15 && versetxt.equalsIgnoreCase(SECOND_VERSE))
		{
			st.setCond(16);
			versetext = "";
			return "32122-second-correct.htm";
		}
		else if(cond == 16 && versetxt.equalsIgnoreCase(THIRD_VERSE))
		{
			st.setCond(17);
			versetext = "";
			return "32122-third-correct.htm";
		}
		else
		{
			versetext = "";
			return "32122-" + cond + "-failed.htm";
		}
	}

	@Override
	public int getQuestId()
	{
		return 126;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return htmltext;
		}

		int cond = st.getCond();
		switch(event)
		{
			case "32115-3.htm":
				st.startQuest();
				break;
			case "32115-5.htm":
				if(cond < 2)
				{
					st.setCond(2);
				}
				break;
			case "32119-1.htm":
				st.setCond(3);
				break;
			case "32119-5.htm":
				st.setCond(4);
				break;
			case "32119-7.htm":
				st.setCond(5);
				break;
			case "32120-1.htm":
				st.setCond(6);
				break;
			case "32120-5.htm":
				st.setCond(7);
				break;
			case "32120-7.htm":
				st.setCond(8);
				break;
			case "32121-1.htm":
				st.setCond(9);
				break;
			case "32121-5.htm":
				st.setCond(10);
				break;
			case "32121-11.htm":
				st.setCond(12);
				break;
			case "32122-2.htm":
				st.setCond(13);
				break;
			case "32122-16.htm":
				st.setCond(14);
				break;
			case "32122-19.htm":
				st.setCond(18);
				st.giveItems(BONE_POWDER, 1);
				break;
			case "32109-4.htm":
				st.setCond(19);
				break;
			case "32109-12.htm":
				if(st.getQuestItemsCount(BONE_POWDER) == 1)
				{
					st.takeItems(BONE_POWDER, 1);
				}
				else
				{
					return "<html><body>You lost the ashes? This shouldn't have happend.</body></html>";
				}
				st.setCond(20);
				break;
			case "32115-9.htm":
				st.setCond(21);
				break;
			case "32115-18.htm":
				st.setCond(22);
				break;
			case "32114-3.htm":
				st.setCond(23);
				break;
			case "32114-6.htm":
				if(st.getState() == COMPLETED)
				{
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				}
				st.unset("cond");
				st.exitQuest(QuestType.ONE_TIME);
				st.setState(COMPLETED);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.addExpAndSp(2264190, 2572950);
				st.giveItems(729, 1); // Enchant Weapon A
				st.giveAdena(484990, true); // Adena
				break;
			case "do":
				versetext += "do";
				if(trys == 4)
				{
					trys = 0;
					return CheckVerse(versetext, st);
				}
				else
				{
					trys++;
					return "verse-" + cond + '-' + trys + ".htm";
				}
			case "re":
				versetext += "re";
				if(trys == 4)
				{
					trys = 0;
					return CheckVerse(versetext, st);
				}
				else
				{
					trys++;
					return "verse-" + cond + '-' + trys + ".htm";
				}
			case "mi":
				versetext += "mi";
				if(trys == 4)
				{
					trys = 0;
					return CheckVerse(versetext, st);
				}
				else
				{
					trys++;
					return "verse-" + cond + '-' + trys + ".htm";
				}
			case "fa":
				versetext += "fa";
				if(trys == 4)
				{
					trys = 0;
					return CheckVerse(versetext, st);
				}
				else
				{
					trys++;
					return "verse-" + cond + '-' + trys + ".htm";
				}
			case "sol":
				versetext += "sol";
				if(trys == 4)
				{
					trys = 0;
					return CheckVerse(versetext, st);
				}
				else
				{
					trys++;
					return "verse-" + cond + '-' + trys + ".htm";
				}
			case "ti":
				versetext += "ti";
				if(trys == 4)
				{
					trys = 0;
					return CheckVerse(versetext, st);
				}
				else
				{
					trys++;
					return "verse-" + cond + '-' + trys + ".htm";
				}
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

		int cond = st.getCond();
		switch(npc.getNpcId())
		{
			case ASAMAH:
				if(player.getLevel() >= 77 && player.getQuestState(_00125_TheNameofEvil1.class) != null && player.getQuestState(_00125_TheNameofEvil1.class).getState() == COMPLETED)
				{
					switch(st.getState())
					{
						case CREATED:
							return "32115-0.htm";
						case STARTED:
							return cond > 19 ? "32115-6.htm" : "32115-4.htm";
						case COMPLETED:
							htmltext = getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
							break;
					}
				}
				else
				{
					return htmltext;
				}
			case ULU_KAIMU:
				return cond >= 2 ? "32119-0.htm" : htmltext;
			case BALU_KAIMU:
				return cond > 4 ? "32120-0.htm" : htmltext;
			case CHUTA_KAIMU:
				return cond > 7 ? "32121-0.htm" : htmltext;
			case WARRIORS_GRAVE:
				if(cond == 15)
				{
					return "enter-1-0.htm";
				}
				if(cond == 16)
				{
					return "enter-2-0.htm";
				}
				if(cond == 17)
				{
					return "32122-18.htm";
				}
				if(cond >= 10 && cond <= 14)
				{
					if(cond < 11)
					{
						st.setCond(11);
					}
					return "32122-0.htm";
				}
				if(cond >= 18 || cond < 10)
				{
					return htmltext;
				}
			case SHILENS_STONE_STATUE:
				return cond >= 17 ? "32109-0.htm" : htmltext;
			case MUSHIKA:
				return cond > 21 ? "32114-0.htm" : htmltext;
		}
		return htmltext;
	}
}