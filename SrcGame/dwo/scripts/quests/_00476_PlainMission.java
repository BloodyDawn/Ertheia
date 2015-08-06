package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 24.04.12
 * Time: 14:13
 */

public class _00476_PlainMission extends Quest
{
	// Квестовые персонажи
	private static final int Гид = 33463;
	private static final int Андрей = 31292;

	// Квестовые монстры
	private static final int Антилопа = 21278;
	private static final int Драколов = 21282;
	private static final int Буйвол = 21286;
	private static final int Грендель = 21290;

	public _00476_PlainMission()
	{
		setMinMaxLevel(65, 69);
		addStartNpc(Гид);
		addTalkId(Гид, Андрей);
		addKillId(Антилопа, Драколов, Буйвол, Грендель);
	}

	public static void main(String[] args)
	{
		new _00476_PlainMission();
	}

	@Override
	public int getQuestId()
	{
		return 476;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		switch(event)
		{
			case "33463-04.htm":
				st.startQuest();
				break;
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 1)
		{
			int _1 = st.getInt("_1");
			int _2 = st.getInt("_2");
			int _3 = st.getInt("_3");
			int _4 = st.getInt("_4");

			TIntIntHashMap moblist = new TIntIntHashMap();

			if(npc.getNpcId() == Антилопа && _1 < 12)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			else if(npc.getNpcId() == Драколов && _2 < 12)
			{
				_2++;
				st.set("_2", String.valueOf(_2));
			}
			else if(npc.getNpcId() == Буйвол && _3 < 12)
			{
				_3++;
				st.set("_3", String.valueOf(_3));
			}
			else if(npc.getNpcId() == Грендель && _4 < 12)
			{
				_4++;
				st.set("_4", String.valueOf(_4));
			}
			if(_1 + _2 + _3 + _4 >= 48)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			moblist.put(1021278, _1);
			moblist.put(1021282, _2);
			moblist.put(1021286, _3);
			moblist.put(1021290, _4);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Гид)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.DAILY);
				case CREATED:
					return "33463-01.htm";
				case STARTED:
					return st.getCond() == 1 ? "33463-05.htm" : "33463-06.htm";
			}
		}
		else if(npc.getNpcId() == Андрей)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "31292-02.htm";
				}
				else if(st.getCond() == 2)
				{
					st.addExpAndSp(4685175, 3376245);
					st.giveAdena(142200, true);
					st.exitQuest(QuestType.DAILY);
					return "31292-01.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "31292-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 65 && player.getLevel() <= 69;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1021278, st.getInt("_1"));
			moblist.put(1021282, st.getInt("_2"));
			moblist.put(1021286, st.getInt("_3"));
			moblist.put(1021290, st.getInt("_4"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}