package dwo.scripts.quests;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00125_TheNameofEvil1 extends Quest
{
	// NPC
	private static final int Mushika = 32114;
	private static final int Karakawei = 32117;
	private static final int UluKaimu = 32119;
	private static final int BaluKaimu = 32120;
	private static final int ChutaKaimu = 32121;

	// QUEST ITEMS
	private static final int GAZKHFRAG = 8782;
	private static final int EPITAPH = 8781;
	private static final int OrClaw = 8779;
	private static final int DienBone = 8780;

	public _00125_TheNameofEvil1()
	{
		addStartNpc(Mushika);
		addTalkId(Mushika, Karakawei, UluKaimu, BaluKaimu, ChutaKaimu);
		addKillId(22200, 22201, 22202, 22203, 22204, 22205, 22219, 22220, 22224, 22224);
		questItemIds = new int[]{OrClaw, DienBone};
	}

	public static void main(String[] args)
	{
		new _00125_TheNameofEvil1();
	}

	@Override
	public int getQuestId()
	{
		return 125;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		String htmltext = event;

		switch(event)
		{
			case "32114-05.htm":
				st.startQuest();
				break;
			case "32114-12.htm":
				st.giveItems(GAZKHFRAG, 1);
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32114-13.htm":
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32117-08.htm":
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32117-16.htm":
				st.setCond(5);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32119-20.htm":
				st.setCond(6);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32120-19.htm":
				st.setCond(7);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "32121-23.htm":
				st.giveItems(EPITAPH, 1);
				st.setCond(8);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				break;
			case "T32119":
				htmltext = "32119-05.htm";
				if(st.getInt("T32119") < 1)
				{
					st.set("T32119", "1");
				}
				break;
			case "E32119":
				htmltext = "32119-06.htm";
				if(st.getInt("E32119") < 1)
				{
					st.set("E32119", "1");
				}
				break;
			case "P32119":
				htmltext = "32119-07.htm";
				if(st.getInt("P32119") < 1)
				{
					st.set("P32119", "1");
				}
				break;
			case "U32119":
				if(st.getInt("U32119") < 1)
				{
					st.set("U32119", "1");
				}
				htmltext = getWordText32119(st);
				break;
			case "T32120":
				htmltext = "32120-05.htm";
				if(st.getInt("T32120") < 1)
				{
					st.set("T32120", "1");
				}
				break;
			case "O32120":
				htmltext = "32120-06.htm";
				if(st.getInt("O32120") < 1)
				{
					st.set("O32120", "1");
				}
				break;
			case "O32120_2":
				htmltext = "32120-07.htm";
				if(st.getInt("O32120_2") < 1)
				{
					st.set("O32120_2", "1");
				}
				break;
			case "N32120":
				if(st.getInt("N32120") < 1)
				{
					st.set("N32120", "1");
				}
				htmltext = getWordText32120(st);
				break;
			case "W32121":
				htmltext = "32121-05.htm";
				if(st.getInt("W32121") < 1)
				{
					st.set("W32121", "1");
				}
				break;
			case "A32121":
				htmltext = "32121-06.htm";
				if(st.getInt("A32121") < 1)
				{
					st.set("A32121", "1");
				}
				break;
			case "G32121":
				htmltext = "32121-07.htm";
				if(st.getInt("G32121") < 1)
				{
					st.set("G32121", "1");
				}
				break;
			case "U32121":
				if(st.getInt("U32121") < 1)
				{
					st.set("U32121", "1");
				}
				htmltext = getWordText32121(st);
				break;
		}
		return htmltext;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();

		if((npcId >= 22200 && npcId <= 22202 || npcId == 22219 || npcId == 22224) && st.getQuestItemsCount(OrClaw) < 2 && Rnd.getChance(10 * (int) Config.RATE_QUEST_DROP))
		{
			st.giveItems(OrClaw, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		if((npcId >= 22203 && npcId <= 22205 || npcId == 22220 || npcId == 22225) && st.getQuestItemsCount(DienBone) < 2 && Rnd.getChance(10 * (int) Config.RATE_QUEST_DROP))
		{
			st.giveItems(DienBone, 1);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}

		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		String htmltext = getNoQuestMsg(st.getPlayer());
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == Mushika)
		{
			QuestState qs124 = st.getPlayer().getQuestState(_00124_MeetingTheElroki.class);
			if(cond == 0)
			{
				if(st.getPlayer().getLevel() < 76)
				{
					htmltext = "32114-02.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
				else if(qs124 != null && qs124.isCompleted())
				{
					htmltext = "32114-01.htm";
				}
				else
				{
					htmltext = "32114-04.htm";
					st.exitQuest(QuestType.REPEATABLE);
				}
			}
			else if(cond == 1)
			{
				htmltext = "32114-10.htm";
			}
			else if(cond > 1 && cond < 8)
			{
				htmltext = "32114-14.htm";
			}
			else if(cond == 8)
			{
				st.unset("T32119");
				st.unset("E32119");
				st.unset("P32119");
				st.unset("U32119");
				st.unset("T32120");
				st.unset("O32120");
				st.unset("O32120_2");
				st.unset("N32120");
				st.unset("W32121");
				st.unset("A32121");
				st.unset("G32121");
				st.unset("U32121");
				st.unset("cond");

				htmltext = "32114-15.htm";
				st.addExpAndSp(898056, 1008100);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
			}
		}
		else if(npcId == Karakawei)
		{
			if(cond == 1)
			{
				htmltext = "32117-02.htm";
			}
			else if(cond == 2)
			{
				htmltext = "32117-01.htm";
			}
			else if(cond == 3 && (st.getQuestItemsCount(OrClaw) < 2 || st.getQuestItemsCount(DienBone) < 2))
			{
				htmltext = "32117-12.htm";
			}
			else if(cond == 3 && st.getQuestItemsCount(OrClaw) == 2 && st.getQuestItemsCount(DienBone) == 2)
			{
				htmltext = "32117-11.htm";
				st.takeItems(OrClaw, 2);
				st.takeItems(DienBone, 2);
				st.setCond(4);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			else if(cond > 4 && cond < 8)
			{
				htmltext = "32117-19.htm";
			}
			else if(cond == 8)
			{
				htmltext = "32117-20.htm";
			}
		}
		else if(npcId == UluKaimu)
		{
			if(cond == 5)
			{
				htmltext = "32119-01.htm";
			}
			else if(cond < 5)
			{
				htmltext = "32119-02.htm";
			}
			else if(cond > 5)
			{
				htmltext = "32119-03.htm";
			}
		}
		else if(npcId == BaluKaimu)
		{
			if(cond == 6)
			{
				htmltext = "32120-01.htm";
			}
			else if(cond < 6)
			{
				htmltext = "32120-02.htm";
			}
			else if(cond > 6)
			{
				htmltext = "32120-03.htm";
			}
		}
		else if(npcId == ChutaKaimu)
		{
			if(cond == 7)
			{
				htmltext = "32121-01.htm";
			}
			else if(cond < 7)
			{
				htmltext = "32121-02.htm";
			}
			else if(cond > 7)
			{
				htmltext = "32121-03.htm";
			}
			else if(cond == 8)
			{
				htmltext = "32121-24.htm";
			}
		}
		else
		{
			htmltext = getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME);
		}
		return htmltext;
	}

	private String getWordText32119(QuestState st)
	{
		String htmltext = "32119-04.htm";
		if(st.getInt("T32119") > 0 && st.getInt("E32119") > 0 && st.getInt("P32119") > 0 && st.getInt("U32119") > 0)
		{
			htmltext = "32119-09.htm";
		}
		return htmltext;
	}

	private String getWordText32120(QuestState st)
	{
		String htmltext = "32120-04.htm";
		if(st.getInt("T32120") > 0 && st.getInt("O32120") > 0 && st.getInt("O32120_2") > 0 && st.getInt("N32120") > 0)
		{
			htmltext = "32120-09.htm";
		}
		return htmltext;
	}

	private String getWordText32121(QuestState st)
	{
		String htmltext = "32121-04.htm";
		if(st.getInt("W32121") > 0 && st.getInt("A32121") > 0 && st.getInt("G32121") > 0 && st.getInt("U32121") > 0)
		{
			htmltext = "32121-09.htm";
		}
		return htmltext;
	}
}