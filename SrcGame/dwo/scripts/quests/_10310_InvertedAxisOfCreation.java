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
 * User: ANZO
 * Date: 26.02.12
 * Time: 2:27
 */

public class _10310_InvertedAxisOfCreation extends Quest
{
	// Квестовые персонажи
	private static final int Селина = 33032;
	private static final int Горфина = 33031;

	// Квестовые монстры
	private static final int ХранительСада = 22947;
	private static final int РазведчикСада = 22948;
	private static final int КомандирСада = 22949;
	private static final int СадовникВнешнегоСада = 22950;
	private static final int РазрушительСада = 22951;

	public _10310_InvertedAxisOfCreation()
	{
		addStartNpc(Селина);
		addTalkId(Селина, Горфина);
		addKillId(ХранительСада, РазведчикСада, КомандирСада, СадовникВнешнегоСада, РазрушительСада);
	}

	public static void main(String[] args)
	{
		new _10310_InvertedAxisOfCreation();
	}

	@Override
	public int getQuestId()
	{
		return 10310;
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
			case "33032-06.htm":
				st.startQuest();
				break;
			case "33031-03.htm":
				st.setCond(2);
				break;
			case "33031-05.htm":
				st.addExpAndSp(50178765, 21980595);
				st.giveAdena(3424540, true);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
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

		if(st.getCond() == 2)
		{
			int _1 = st.getInt("_1");
			int _2 = st.getInt("_2");
			int _3 = st.getInt("_3");
			int _4 = st.getInt("_4");
			int _5 = st.getInt("_5");

			TIntIntHashMap moblist = new TIntIntHashMap();

			if(npc.getNpcId() == ХранительСада && _1 < 10)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			else if(npc.getNpcId() == РазведчикСада && _2 < 10)
			{
				_2++;
				st.set("_2", String.valueOf(_2));
			}
			else if(npc.getNpcId() == КомандирСада && _3 < 10)
			{
				_3++;
				st.set("_3", String.valueOf(_3));

			}
			else if(npc.getNpcId() == СадовникВнешнегоСада && _4 < 10)
			{
				_4++;
				st.set("_4", String.valueOf(_4));
			}
			else if(npc.getNpcId() == РазрушительСада && _5 < 10)
			{
				_5++;
				st.set("_5", String.valueOf(_5));
			}
			if(_1 + _2 + _3 + _4 + _5 >= 50)
			{
				st.setCond(3);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			moblist.put(1022947, _1);
			moblist.put(1022948, _2);
			moblist.put(1022949, _3);
			moblist.put(1022950, _4);
			moblist.put(1022951, _5);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		QuestState previous = player.getQuestState(_10302_TheShadowOfAnxiety.class);

		if(npc.getNpcId() == Селина)
		{
			if(previous == null || !previous.isCompleted() || player.getLevel() < 90)
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "33032-03.htm";
			}
			switch(st.getState())
			{
				case COMPLETED:
					return "33032-02.htm";
				case CREATED:
					return "33032-01.htm";
				case STARTED:
					return "33032-07.htm";
			}
		}
		else if(npc.getNpcId() == Горфина)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33031-01.htm";
					}
					if(st.getCond() == 2)
					{
						return "33031-03.htm";
					}
					if(st.getCond() == 3)
					{
						return "33031-04.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState previous = player.getQuestState(_10302_TheShadowOfAnxiety.class);
		return previous != null && previous.isCompleted() && player.getLevel() >= 90;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1022947, st.getInt("_1"));
			moblist.put(1022948, st.getInt("_2"));
			moblist.put(1022949, st.getInt("_3"));
			moblist.put(1022950, st.getInt("_4"));
			moblist.put(1022951, st.getInt("_5"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}