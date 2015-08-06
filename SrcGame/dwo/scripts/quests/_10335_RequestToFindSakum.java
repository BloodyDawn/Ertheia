package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

public class _10335_RequestToFindSakum extends Quest
{
	// Нпц
	private static final int BATHIS = 30332;
	private static final int KALLESIN = 33177;
	private static final int ZENATH = 33509;
	// Мобы
	private static final int Ruin_Zombie = 20026;
	private static final int Ruin_Zombie_Leader = 20029;
	private static final int Ruin_Spartoi = 20054;
	private static final int Skeleton_Tracker = 20035;
	private static final int Skeleton_Bowman = 20051;

	public _10335_RequestToFindSakum()
	{
		addStartNpc(BATHIS);
		addTalkId(BATHIS, KALLESIN, ZENATH);
		addKillId(Ruin_Zombie, Ruin_Zombie_Leader, Ruin_Spartoi, Skeleton_Tracker, Skeleton_Bowman);
	}

	public static void main(String[] args)
	{
		new _10335_RequestToFindSakum();
	}

	@Override
	public int getQuestId()
	{
		return 10335;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "captain_bathia_q10335_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == BATHIS)
		{
			if(reply == 1)
			{
				return "captain_bathia_q10335_04.htm";
			}
		}
		else if(npc.getNpcId() == KALLESIN)
		{
			if(reply == 1 && st.getCond() == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "gludio_sorrowruins_q10335_02.htm";
			}
		}
		else if(npc.getNpcId() == ZENATH)
		{
			if(reply == 1)
			{
				return "gludio_zena_q10335_02.htm";
			}
			else if(reply == 2 && st.getCond() == 3)
			{
				st.unset("_1");
				st.unset("_2");
				st.unset("_3");
				st.unset("_4");
				st.giveAdena(90000, true);
				st.addExpAndSp(350000, 84);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "gludio_zena_q10335_03.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		TIntIntHashMap moblist = new TIntIntHashMap();

		QuestState st = player.getQuestState(getClass());
		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		int ONE = st.getInt("_1");
		int TWO = st.getInt("_2");
		int THREE = st.getInt("_3");
		int FOUR = st.getInt("_4");

		if(st.getCond() == 2)
		{
			if((npc.getNpcId() == Ruin_Zombie || npc.getNpcId() == Ruin_Zombie_Leader) && ONE < 15)
			{
				ONE++;
				st.set("_1", String.valueOf(ONE));
			}
			else if(npc.getNpcId() == Ruin_Spartoi && TWO < 15)
			{
				TWO++;
				st.set("_2", String.valueOf(TWO));
			}
			else if(npc.getNpcId() == Skeleton_Tracker && THREE < 10)
			{
				THREE++;
				st.set("_3", String.valueOf(THREE));
			}
			else if(npc.getNpcId() == Skeleton_Bowman && FOUR < 10)
			{
				FOUR++;
				st.set("_4", String.valueOf(FOUR));
			}
			moblist.put(1020026, ONE);
			moblist.put(1020054, TWO);
			moblist.put(1020035, THREE);
			moblist.put(1020051, FOUR);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			if(ONE >= 15 && TWO >= 15 && THREE >= 10 && FOUR >= 10)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == BATHIS)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "captain_bathia_q10335_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "captain_bathia_q10335_02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "captain_bathia_q10335_06.htm";
					}
					if(st.getCond() == 2)
					{
						return "captain_bathia_q10335_07.htm";
					}
					if(st.getCond() == 3)
					{
						return "captain_bathia_q10335_08.htm";
					}
					break;
				case COMPLETED:
					return "captain_bathia_q10335_03.htm";
			}
		}
		else if(npc.getNpcId() == KALLESIN)
		{
			if(st.getCond() == 1)
			{
				return "gludio_sorrowruins_q10335_01.htm";
			}
			else if(st.getCond() == 2)
			{
				return "gludio_sorrowruins_q10335_03.htm";
			}
			else if(st.getCond() == 3)
			{
				return "gludio_sorrowruins_q10335_04.htm";
			}
			else if(st.isCompleted())
			{
				return "gludio_sorrowruins_q10335_05.htm";
			}
		}
		else if(npc.getNpcId() == ZENATH)
		{
			if(st.getCond() == 1)
			{
				return "gludio_zena_q10335_04.htm";
			}
			else if(st.getCond() == 2)
			{
				return "gludio_zena_q10335_05.htm";
			}
			else if(st.getCond() == 3)
			{
				return "gludio_zena_q10335_01.htm";
			}
			else if(st.isCompleted())
			{
				return "gludio_zena_q10335_06.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 23 && player.getLevel() <= 40;
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1020026, st.getInt("_1"));
			moblist.put(1020054, st.getInt("_2"));
			moblist.put(1020035, st.getInt("_3"));
			moblist.put(1020051, st.getInt("_4"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
} 