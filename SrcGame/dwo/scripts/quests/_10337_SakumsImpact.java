package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

public class _10337_SakumsImpact extends Quest
{
	//npc
	private static final int ADVENTURE_GUILDSMAN = 31795;
	private static final int SILVAN = 33178;
	private static final int LEF = 33510;
	//mobs
	private static final int BAT = 27458;
	private static final int BAT_2 = 23023;
	private static final int BAT_3 = 20411;
	private static final int BONE_WARRIOR = 23022;
	private static final int RUIN_IMP = 20506;
	private static final int RUIN_IMP_2 = 20507;

	public _10337_SakumsImpact()
	{
		setMinMaxLevel(28, 40);
		addStartNpc(ADVENTURE_GUILDSMAN);
		addTalkId(ADVENTURE_GUILDSMAN, SILVAN, LEF);
		addKillId(BAT, BAT_2, BAT_3, BONE_WARRIOR, RUIN_IMP, RUIN_IMP_2);
	}

	public static void main(String[] args)
	{
		new _10337_SakumsImpact();
	}

	@Override
	public int getQuestId()
	{
		return 10337;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("adventurer_agent_town_21_q10337_05.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("33178-2.html"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		else if(event.equalsIgnoreCase("33510-1.html"))
		{
			st.unset("one");
			st.unset("two");
			st.unset("three");
			st.giveAdena(103000, true);
			st.addExpAndSp(470000, 160000);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.ONE_TIME);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());
		if(npc == null || st == null)
		{
			return super.onKill(npc, player, isPet);
		}

		if(st.getCond() == 2)
		{
			int ONE = st.getInt("_1");
			int TWO = st.getInt("_2");
			int THREE = st.getInt("_3");
			if((npc.getNpcId() == BAT || npc.getNpcId() == BAT_2 || npc.getNpcId() == BAT_3) && ONE < 25)
			{
				ONE++;
				st.set("_1", String.valueOf(ONE));
			}
			else if(npc.getNpcId() == BONE_WARRIOR && TWO < 15)
			{
				TWO++;
				st.set("_2", String.valueOf(TWO));
			}
			else if((npc.getNpcId() == RUIN_IMP || npc.getNpcId() == RUIN_IMP_2) && THREE < 20)
			{
				THREE++;
				st.set("_3", String.valueOf(THREE));
			}

			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1027458, ONE);
			moblist.put(1023022, TWO);
			moblist.put(1020506, THREE);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));

			if(ONE >= 25 && TWO >= 15 && THREE >= 20)
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
		if(npc.getNpcId() == ADVENTURE_GUILDSMAN)
		{
			switch(st.getState())
			{
				case CREATED:
					if(canBeStarted(player))
					{
						return "adventurer_agent_town_21_q10337_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "adventurer_agent_town_21_q10337_02.html";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "adventurer_agent_town_21_q10337_06.html";
					}
					break;
				case COMPLETED:
					return "adventurer_agent_town_21_q10337_03.html";
			}
		}
		else if(npc.getNpcId() == SILVAN)
		{
			if(st.getCond() == 1)
			{
				return "33178.html";
			}
			else if(st.getCond() == 2)
			{
				return "33178-3.html";
			}
		}
		else if(npc.getNpcId() == LEF)
		{
			if(st.getCond() == 3)
			{
				return "33510.html";
			}
			else if(st.isCompleted())
			{
				return "33510-2.html";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 28 && player.getLevel() <= 40;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1027458, st.getInt("_1"));
			moblist.put(1023022, st.getInt("_2"));
			moblist.put(1020506, st.getInt("_3"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
} 