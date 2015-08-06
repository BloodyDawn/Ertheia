package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00134_TempleMissionary extends Quest
{
	// NPCs
	private static final int Glyvka = 30067;
	private static final int Rouke = 31418;

	// Mobs
	private static final int Cruma_Marshlands_Traitor = 27339;
	private static final int[] mobs = {
		20157, 20229, 20230, 20231, 20232, 20233, 20234
	};

	// Quest Items
	private static final int Giants_Experimental_Tool_Fragment = 10335;
	private static final int Giants_Experimental_Tool = 10336;
	private static final int Giants_Technology_Report = 10337;
	private static final int Roukes_Report = 10338;

	// Items
	private static final int Badge_Temple_Missionary = 10339;

	// Chances
	private static final int Giants_Experimental_Tool_Fragment_chance = 66;
	private static final int Cruma_Marshlands_Traitor_spawnchance = 45;

	public _00134_TempleMissionary()
	{
		addStartNpc(Glyvka);
		addTalkId(Glyvka, Rouke);
		addKillId(mobs);
		addKillId(Cruma_Marshlands_Traitor);
		questItemIds = new int[]{
			Giants_Experimental_Tool_Fragment, Giants_Experimental_Tool, Giants_Technology_Report, Roukes_Report
		};
	}

	public static void main(String[] args)
	{
		new _00134_TempleMissionary();
	}

	@Override
	public int getQuestId()
	{
		return 134;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}
		if(event.equalsIgnoreCase("glyvka_q0134_03.htm") && st.getState() == CREATED)
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("glyvka_q0134_06.htm") && st.getState() == STARTED)
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("glyvka_q0134_11.htm") && st.getState() == STARTED && st.getCond() == 5)
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.unset("Report");
			st.giveAdena(15100, true);
			st.giveItems(Badge_Temple_Missionary, 1);
			st.addExpAndSp(30000, 2000);
			st.exitQuest(QuestType.ONE_TIME);
		}
		else if(event.equalsIgnoreCase("scroll_seller_rouke_q0134_03.htm") && st.getState() == STARTED)
		{
			st.setCond(3);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("scroll_seller_rouke_q0134_09.htm") && st.getState() == STARTED && st.getInt("Report") == 1)
		{
			st.setCond(5);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			st.giveItems(Roukes_Report, 1);
			st.unset("Report");
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState qs = player.getQuestState(getClass());
		if(qs == null)
		{
			return null;
		}

		if(qs.getState() == STARTED && qs.getCond() == 3)
		{
			if(npc.getNpcId() == Cruma_Marshlands_Traitor)
			{
				qs.giveItems(Giants_Technology_Report, 1);
				if(qs.getQuestItemsCount(Giants_Technology_Report) < 3)
				{
					qs.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else
				{
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					qs.setCond(4);
				}
			}
			else if(qs.getQuestItemsCount(Giants_Experimental_Tool) < 1)
			{
				if(Rnd.getChance(Giants_Experimental_Tool_Fragment_chance))
				{
					qs.giveItems(Giants_Experimental_Tool_Fragment, 1);
				}
			}
			else
			{
				qs.takeItems(Giants_Experimental_Tool, 1);
				if(Rnd.getChance(Cruma_Marshlands_Traitor_spawnchance))
				{
					qs.addSpawn(Cruma_Marshlands_Traitor, qs.getPlayer().getX(), qs.getPlayer().getY(), qs.getPlayer().getZ(), 0, true, 900000);
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.getState() == COMPLETED)
		{
			return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
		}

		int npcId = npc.getNpcId();
		if(st.getState() == CREATED)
		{
			if(npcId != Glyvka)
			{
				return getNoQuestMsg(player);
			}
			if(player.getLevel() < 35)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "glyvka_q0134_02.htm";
			}
			st.setCond(0);
			return "glyvka_q0134_01.htm";
		}

		int cond = st.getCond();

		if(npcId == Glyvka && st.getState() == STARTED)
		{
			if(cond == 1)
			{
				return "glyvka_q0134_03.htm";
			}
			if(cond == 5)
			{
				if(st.getInt("Report") == 1)
				{
					return "glyvka_q0134_09.htm";
				}
				if(st.getQuestItemsCount(Roukes_Report) > 0)
				{
					st.takeItems(Roukes_Report, -1);
					st.set("Report", "1");
					return "glyvka_q0134_08.htm";
				}
				return getNoQuestMsg(player);
			}
			return "glyvka_q0134_07.htm";
		}

		if(npcId == Rouke && st.getState() == STARTED)
		{
			if(cond == 2)
			{
				return "scroll_seller_rouke_q0134_02.htm";
			}
			if(cond == 5)
			{
				return "scroll_seller_rouke_q0134_10.htm";
			}
			if(cond == 3)
			{
				long Tools = st.getQuestItemsCount(Giants_Experimental_Tool_Fragment) / 10;
				if(Tools < 1)
				{
					return "scroll_seller_rouke_q0134_04.htm";
				}
				st.takeItems(Giants_Experimental_Tool_Fragment, Tools * 10);
				st.giveItems(Giants_Experimental_Tool, Tools);
				return "scroll_seller_rouke_q0134_05.htm";
			}
			if(cond == 4)
			{
				if(st.getInt("Report") == 1)
				{
					return "scroll_seller_rouke_q0134_07.htm";
				}
				if(st.getQuestItemsCount(Giants_Technology_Report) > 2)
				{
					st.takeItems(Giants_Experimental_Tool_Fragment, -1);
					st.takeItems(Giants_Experimental_Tool, -1);
					st.takeItems(Giants_Technology_Report, -1);
					st.set("Report", "1");
					return "scroll_seller_rouke_q0134_06.htm";
				}
				return getNoQuestMsg(player);
			}
		}
		return getNoQuestMsg(player);
	}
}