package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

public class _10368_RebellionOfMonsters extends Quest
{
	//npc
	private static final int FRED = 33179;
	private static final int Weary_Jaguar = 23024;
	private static final int Weary_Jaguar_Scout = 23025;
	private static final int Ant_Soldier = 23099;
	private static final int Ant_Warrior_Captain = 23100;

	public _10368_RebellionOfMonsters()
	{
		addStartNpc(FRED);
		addTalkId(FRED);
		addKillId(Weary_Jaguar, Weary_Jaguar_Scout, Ant_Soldier, Ant_Warrior_Captain);
	}

	public static void main(String[] args)
	{
		new _10368_RebellionOfMonsters();
	}

	@Override
	public int getQuestId()
	{
		return 10368;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33179-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("33179-06.html"))
		{
			st.unset("Weary_Jaguar");
			st.unset("Weary_Jaguar_Scout");
			st.unset("Ant_Soldier");
			st.unset("Ant_Warrior_Captain");
			st.giveAdena(99000, true);
			st.addExpAndSp(550000, 150000);
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

		if(st.getCond() == 1)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();

			int WJ = st.getInt("_1");
			int WJS = st.getInt("_2");
			int AS = st.getInt("_3");
			int AWC = st.getInt("_4");

			if(npc.getNpcId() == Weary_Jaguar && WJ < 10)
			{
				WJ++;
				st.set("_1", String.valueOf(WJ));
			}
			else if(npc.getNpcId() == Weary_Jaguar_Scout && WJS < 15)
			{
				WJS++;
				st.set("_2", String.valueOf(WJS));
			}
			else if(npc.getNpcId() == Ant_Soldier && AS < 15)
			{
				AS++;
				st.set("_3", String.valueOf(AS));
			}
			else if(npc.getNpcId() == Ant_Warrior_Captain && AWC < 20)
			{
				AWC++;
				st.set("_4", String.valueOf(AWC));
			}
			moblist.put(1023024, WJ);
			moblist.put(1023025, WJS);
			moblist.put(1023099, AS);
			moblist.put(1023100, AWC);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			if(WJ >= 10 && WJS >= 15 && AS >= 15 && AWC >= 20)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == FRED)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 34 && player.getLevel() < 40)
					{
						return "33179-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33179-08.html";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33179-04.html";
					}
					if(st.getCond() == 2)
					{
						return "33179-05.html";
					}
					break;
				case COMPLETED:
					return "33179-07.html";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 34 && player.getLevel() < 40;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023024, st.getInt("_1"));
			moblist.put(1023025, st.getInt("_2"));
			moblist.put(1023099, st.getInt("_3"));
			moblist.put(1023100, st.getInt("_4"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
} 