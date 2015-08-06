package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 13:30
 */

public class _00142_FallenAngelRequestofDawn extends Quest
{
	// Квестовые НПЦ
	private static final int NATOOLS = 30894;
	private static final int RAYMOND = 30289;
	private static final int CASIAN = 30612;
	private static final int ROCK = 32368;

	// Квестовые предметы
	private static final int CRYPT = 10351;
	private static final int FRAGMENT = 10352;
	private static final int BLOOD = 10353;

	// Квестовые монстры
	private static final int Ant = 20079;
	private static final int AntCaptain = 20080;
	private static final int AntOverseer = 20081;
	private static final int AntRecruit = 20082;
	private static final int AntPatrol = 20084;
	private static final int AntGuard = 20086;
	private static final int AntSoldier = 20087;
	private static final int AntWarriorCaptain = 20088;
	private static final int NobleAnt = 20089;
	private static final int NobleAntLeader = 20090;
	private static final int FallenAngel = 27338;

	private boolean angelSpawned;

	public _00142_FallenAngelRequestofDawn()
	{
		addStartNpc(NATOOLS);
		addTalkId(NATOOLS, RAYMOND, CASIAN, ROCK);
		addKillId(Ant, AntCaptain, AntOverseer, AntRecruit, AntPatrol, AntGuard, AntSoldier, AntWarriorCaptain, NobleAnt, NobleAntLeader, FallenAngel);
		questItemIds = new int[]{CRYPT, FRAGMENT, BLOOD};
	}

	public static void main(String[] args)
	{
		new _00142_FallenAngelRequestofDawn();
	}

	@Override
	public int getQuestId()
	{
		return 142;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept"))
		{
			QuestState pqs = qs.getPlayer().getQuestState(_00143_FallenAngelRequestofDusk.class);
			if(pqs != null)
			{
				pqs.exitQuest(QuestType.REPEATABLE);
			}
			qs.startQuest();
			return "warehouse_chief_natools_q0142_07.htm";
		}
		if(event.equals("angel_cleanup"))
		{
			angelSpawned = false;
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == NATOOLS)
		{
			switch(reply)
			{
				case 1:
					return "warehouse_chief_natools_q0142_02.htm";
				case 2:
					return "warehouse_chief_natools_q0142_03.htm";
				case 3:
					return "warehouse_chief_natools_q0142_04.htm";
				case 4:
					return "warehouse_chief_natools_q0142_09.htm";
				case 5:
					st.setCond(2);
					st.setState(STARTED);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					st.giveItems(CRYPT, 1);
					return "warehouse_chief_natools_q0142_10.htm";
			}
		}
		else if(npcId == RAYMOND)
		{
			switch(reply)
			{
				case 3:
					return "bishop_raimund_q0142_02a.htm";
				case 4:
					return "bishop_raimund_q0142_04.htm";
				case 5:
					st.setCond(3);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "bishop_raimund_q0142_05.htm";
			}
		}
		else if(npcId == CASIAN)
		{
			switch(reply)
			{
				case 5:
					return "sage_kasian_q0142_03.htm";
				case 6:
					return "sage_kasian_q0142_05.htm";
				case 7:
					return "sage_kasian_q0142_06.htm";
				case 8:
					return "sage_kasian_q0142_07.htm";
				case 9:
					return "sage_kasian_q0142_09.htm";
				case 10:
					st.setCond(4);
					st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return "sage_kasian_q0142_10.htm";
			}
		}
		else if(npcId == ROCK)
		{
			switch(reply)
			{
				case 1:
					if(angelSpawned)
					{
						return "stained_rock_q0142_04.htm";
					}
					else
					{
						st.addSpawn(FallenAngel, -21882, 186730, -4320, 0, false, 180000);
						angelSpawned = true;
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						startQuestTimer("angel_cleanup", 180000, null, player);
						return "stained_rock_q0142_05.htm";
					}
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		int cond = st.getCond();
		if(npc.getNpcId() == FallenAngel)
		{
			if(cond == 5)
			{
				st.setCond(6);
				st.setState(STARTED);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.giveItems(BLOOD, 1);
			}
		}
		else if(cond == 4 && st.getQuestItemsCount(FRAGMENT) < 30)
		{
			st.rollAndGive(FRAGMENT, 1, 1, 30, 20);
			if(st.getQuestItemsCount(FRAGMENT) >= 30)
			{
				st.setCond(5);
				st.setState(STARTED);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		// Если уже выбрали кокурирующий квест, то шлем игрока куда подальше
		QuestState pst = st.getPlayer().getQuestState(_00143_FallenAngelRequestofDusk.class);
		if(pst != null && pst.isStarted())
		{
			st.exitQuest(QuestType.REPEATABLE);
			return getNoQuestMsg(st.getPlayer());
		}

		if(npcId == NATOOLS)
		{
			switch(cond)
			{
				case 0:
					return st.getPlayer().getLevel() < 38 ? "warehouse_chief_natools_q0142_05.htm" : "warehouse_chief_natools_q0142_01.htm";
				case 1:
					return "warehouse_chief_natools_q0142_07.htm";
				case 2:
					return "warehouse_chief_natools_q0142_11.htm";
			}
		}
		else if(npcId == RAYMOND)
		{
			switch(cond)
			{
				case 2:
					if(st.getInt("talk") == 1)
					{
						return "bishop_raimund_q0142_02a.htm";
					}
					else
					{
						st.takeItems(CRYPT, -1);
						st.set("talk", "1");
						return "bishop_raimund_q0142_02.htm";
					}
				case 3:
					return "bishop_raimund_q0142_06.htm";
				case 6:
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					st.giveAdena(92676, true);
					st.exitQuest(QuestType.ONE_TIME);
					return "bishop_raimund_q0142_07.htm";
			}
		}
		else if(npcId == CASIAN)
		{
			if(cond == 3)
			{
				return "sage_kasian_q0142_02.htm";
			}
			else if(cond == 4)
			{
				return "sage_kasian_q0142_10.htm";
			}
		}
		else if(npcId == ROCK)
		{
			if(cond <= 4)
			{
				return "stained_rock_q0142_01.htm";
			}
			else if(cond == 5)
			{
				if(st.getInt("talk") != 1)
				{
					st.takeItems(BLOOD, -1);
					st.set("talk", "1");
				}
				return "stained_rock_q0142_02.htm";
			}
			else
			{
				return "stained_rock_q0142_06.htm";
			}
		}
		return null;
	}
}