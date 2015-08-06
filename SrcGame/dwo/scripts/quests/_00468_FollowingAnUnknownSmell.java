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
 * Date: 29.02.12
 * Time: 5:26
 */

public class _00468_FollowingAnUnknownSmell extends Quest
{
	// Квестовые персонажи
	private static final int Селина = 33032;

	// Квестовые монстры
	private static final int ВоеночальникСада = 22962;
	private static final int УправляющийСадомЛуны = 22958;
	private static final int СадовникВнутреннегоСадаЛуны = 22960;
	private static final int ОхранникСада = 22959;

	// Квестовые предметы
	private static final int ДоказательствоЖизни = 30385;

	public _00468_FollowingAnUnknownSmell()
	{
		addStartNpc(Селина);
		addTalkId(Селина);
		addKillId(ВоеночальникСада, УправляющийСадомЛуны, СадовникВнутреннегоСадаЛуны, ОхранникСада);
	}

	public static void main(String[] args)
	{
		new _00468_FollowingAnUnknownSmell();
	}

	@Override
	public int getQuestId()
	{
		return 468;
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
			case "33032-04.htm":
				st.startQuest();
				break;
			case "33032-07.htm":
				st.giveItems(ДоказательствоЖизни, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
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

			if(npc.getNpcId() == ВоеночальникСада && _1 < 10)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			else if(npc.getNpcId() == УправляющийСадомЛуны && _2 < 10)
			{
				_2++;
				st.set("_2", String.valueOf(_2));
			}
			else if(npc.getNpcId() == СадовникВнутреннегоСадаЛуны && _3 < 10)
			{
				_3++;
				st.set("_3", String.valueOf(_3));
			}
			else if(npc.getNpcId() == ОхранникСада && _4 < 10)
			{
				_4++;
				st.set("_4", String.valueOf(_4));
			}
			if(_1 + _2 + _3 + _4 >= 40)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			moblist.put(1022962, _1);
			moblist.put(1022958, _2);
			moblist.put(1022960, _3);
			moblist.put(1022959, _4);
			killer.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == Селина)
		{
			if(st.isNowAvailable() && st.isCompleted())
			{
				st.setState(CREATED);
			}

			switch(st.getState())
			{
				case COMPLETED:
					return "33032-08.htm";
				case CREATED:
					if(player.getLevel() < 90)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "33032-02.htm";
					}
					else
					{
						return "33032-01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33032-05.htm";
					}
					if(st.getCond() == 2)
					{
						return "33032-06.htm";
					}
					break;
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 90;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1022962, st.getInt("_1"));
			moblist.put(1022958, st.getInt("_2"));
			moblist.put(1022960, st.getInt("_3"));
			moblist.put(1022959, st.getInt("_4"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}