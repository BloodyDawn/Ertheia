package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 09.05.12
 * Time: 0:19
 */

public class _10353_CertificationOfValue extends Quest
{
	// Квестовые персонажи
	private static final int Лией1 = 33155;
	private static final int Лией2 = 33406;
	private static final int Омар = 33358;

	// Квестовые предметы
	private static final int СимволДерзости = 17624;

	public _10353_CertificationOfValue()
	{
		setMinMaxLevel(48, 100);
		addStartNpc(Лией1, Лией2);
		addTalkId(Лией1, Лией2, Омар);
		for(int i = 23044; i <= 23068; i++)
		{
			addKillId(i);
		}
		for(int i = 23101; i <= 23112; i++)
		{
			addKillId(i);
		}
	}

	public static void main(String[] args)
	{
		new _10353_CertificationOfValue();
	}

	@Override
	public int getQuestId()
	{
		return 10353;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33155-08.htm") || event.equalsIgnoreCase("33406-08.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("33358-04.htm"))
		{
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.getCond() == 2)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();

			int _1 = st.getInt("_1");

			if((npc.getNpcId() >= 23044 && npc.getNpcId() <= 23068 || npc.getNpcId() >= 23101 && npc.getNpcId() <= 23112) && _1 < 10 && Rnd.getChance(10))
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			moblist.put(1033349, _1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			if(_1 >= 10)
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

		if(npc.getNpcId() == Лией1)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 48)
					{
						return "33155-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33155-02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33155-08.htm";
					}
					break;
				case COMPLETED:
					return "33155-03.htm";
			}
		}
		else if(npc.getNpcId() == Лией2)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 48)
					{
						return "33406-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33406-02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33406-08.htm";
					}
					break;
				case COMPLETED:
					return "33406-03.htm";
			}
		}
		else if(npc.getNpcId() == Омар)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "33358-01.htm";
				}
				else if(st.getCond() == 2)
				{
					return "33358-05.htm";
				}
				else if(st.getCond() == 3)
				{
					st.addExpAndSp(3000000, 2500000);
					st.giveItem(СимволДерзости);
					st.exitQuest(QuestType.ONE_TIME);
					return "33358-07.htm";
				}
			}
			else if(st.isCompleted())
			{
				return "33358-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 48;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1033349, st.getInt("_1"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}