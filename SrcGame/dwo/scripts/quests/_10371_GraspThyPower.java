package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 08.05.12
 * Time: 17:04
 */

public class _10371_GraspThyPower extends Quest
{
	// Квестовые персонажи
	private static final int Гергхенштейн = 33648;

	// Квестовые монстры
	private static final int ВоительВедьмы = 23181;
	private static final int ВоинВедьмы = 23182;
	private static final int СтрелокВедьмы = 23183;
	private static final int ШаманВедьмы = 23184;
	private static final int КровавыйКошмар = 23185;

	public _10371_GraspThyPower()
	{
		addStartNpc(Гергхенштейн);
		addTalkId(Гергхенштейн);
		addKillId(ВоительВедьмы, ВоинВедьмы, СтрелокВедьмы, ШаманВедьмы, КровавыйКошмар);
	}

	public static void main(String[] args)
	{
		new _10371_GraspThyPower();
	}

	@Override
	public int getQuestId()
	{
		return 10371;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		if(event.equalsIgnoreCase("33648-06.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("33648-10.htm"))
		{
			st.giveAdena(484990, true);
			st.addExpAndSp(22641900, 25729500);
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
			return null;
		}

		if(st.getCond() == 1)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();

			int _1 = st.getInt("_1");
			int _2 = st.getInt("_2");
			int _3 = st.getInt("_3");
			int _4 = st.getInt("_4");
			int _5 = st.getInt("_5");

			if(npc.getNpcId() == ВоительВедьмы && _1 < 12)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			else if(npc.getNpcId() == ВоинВедьмы && _2 < 12)
			{
				_2++;
				st.set("_2", String.valueOf(_2));
			}
			else if(npc.getNpcId() == СтрелокВедьмы && _3 < 8)
			{
				_3++;
				st.set("_3", String.valueOf(_3));
			}
			else if(npc.getNpcId() == ШаманВедьмы && _4 < 8)
			{
				_4++;
				st.set("_4", String.valueOf(_4));
			}
			else if(npc.getNpcId() == КровавыйКошмар && _5 < 5)
			{
				_5++;
				st.set("_5", String.valueOf(_5));
			}
			moblist.put(1023181, _1);
			moblist.put(1023182, _2);
			moblist.put(1023183, _3);
			moblist.put(1023184, _4);
			moblist.put(1023185, _5);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			if(_1 >= 12 && _2 >= 12 && _3 >= 8 && _4 >= 8 && _4 >= 5)
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

		if(npc.getNpcId() == Гергхенштейн)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState prevst = player.getQuestState(_10370_MenacingTimes.class);
					if(prevst != null && prevst.isCompleted() && player.getLevel() >= 76 && player.getLevel() <= 81)
					{
						return "33648-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33648-02.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33648-07.htm";
					}
					if(st.getCond() == 2)
					{
						return "33648-08.htm";
					}
					break;
				case COMPLETED:
					return "33648-03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(_10370_MenacingTimes.class);
		return st != null && st.isCompleted() && player.getLevel() >= 76 && player.getLevel() <= 81 && player.getClassId().level() == ClassLevel.THIRD.ordinal();

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023181, st.getInt("_1"));
			moblist.put(1023182, st.getInt("_2"));
			moblist.put(1023183, st.getInt("_3"));
			moblist.put(1023184, st.getInt("_4"));
			moblist.put(1023185, st.getInt("_5"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}