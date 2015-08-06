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
 * Date: 01.03.12
 * Time: 21:46
 */

public class _00469_AStrangeGardener extends Quest
{
	// Квестовые персонажи
	private static final int Горфина = 33031;

	// Квестовые монстры
	private static final int ЗащитникАфроса = 22964;

	// Квестовые предметы
	private static final int ДоказательствоЖизни = 30385;

	public _00469_AStrangeGardener()
	{
		addStartNpc(Горфина);
		addTalkId(Горфина);
		addKillId(ЗащитникАфроса);
	}

	public static void main(String[] args)
	{
		new _00469_AStrangeGardener();
	}

	@Override
	public int getQuestId()
	{
		return 469;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equals("33031-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equals("33031-06.htm"))
		{
			st.giveItems(ДоказательствоЖизни, 2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.DAILY);
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

			TIntIntHashMap moblist = new TIntIntHashMap();

			if(npc.getNpcId() == ЗащитникАфроса && _1 < 30)
			{
				_1++;
				st.set("_1", String.valueOf(_1));
			}
			if(_1 >= 30)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
			moblist.put(1022964, _1);
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

		if(npc.getNpcId() == Горфина)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "33031-04.htm";
				case CREATED:
					if(player.getLevel() >= 90)
					{
						return "33031-01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return getLowLevelMsg(90);
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "33031-03.htm";
					}
					if(st.getCond() == 2)
					{
						return "33031-05.htm";
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
			moblist.put(1022964, st.getInt("_1"));
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}