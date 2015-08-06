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
 * Date: 10.06.13
 * Time: 13:39
 */

public class _10446_HitandRun extends Quest
{
	// Квестовые персонажи
	private static final int BURINU = 33840;

	// Квестовые монстры
	private static final int NERVA_ORC = 23322;

	// Квестовые награды
	private static final int ETERNAL_ENHANCEMENT_STONE = 35569;
	private static final int ELMORE_SUPPORT_BOX = 37020;

	public _10446_HitandRun()
	{
		addStartNpc(BURINU);
		addTalkId(BURINU);
		addKillId(NERVA_ORC);
	}

	public static void main(String[] args)
	{
		new _10446_HitandRun();
	}

	@Override
	public int getQuestId()
	{
		return 10446;
	}

	@Override
	public String onEvent(String event, QuestState st)
	{
		if(event.equals("quest_accept") && !st.isCompleted())
		{
			st.startQuest();
			return "h_burinu_q10446_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == BURINU)
		{
			if(reply == 1)
			{
				return "h_burinu_q10446_03.htm";
			}
			else if(reply == 2)
			{
				return "h_burinu_q10446_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());
		if(st != null && st.isStarted())
		{
			if(npc.getNpcId() == NERVA_ORC)
			{
				if(st.getCond() == 1)
				{
					TIntIntHashMap mobList = new TIntIntHashMap();
					int _1 = st.getInt("1023322");
					_1++;
					st.set("_1", String.valueOf(_1));
					if(_1 == 10)
					{
						st.setCond(2);
						st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					}
					mobList.put(1023322, _1);
					killer.sendPacket(new ExQuestNpcLogList(getQuestId(), mobList));
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(npc.getNpcId() == BURINU)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return getAlreadyCompletedMsg(st.getPlayer(), QuestType.ONE_TIME); // TODO: Нет диалога?
				case CREATED:
					QuestState prevSt = st.getPlayer().getQuestState(_10445_AnImpendingThreat.class);
					return st.getPlayer().getLevel() >= 99 && prevSt != null && prevSt.isCompleted() ? "h_burinu_q10446_01.htm" : "h_burinu_q10446_02.htm";
				case STARTED:
					if(st.getCond() == 1)
					{
						return "h_burinu_q10446_06.htm";
					}
					else if(st.getCond() == 2)
					{
						st.giveItem(ETERNAL_ENHANCEMENT_STONE);
						st.giveItem(ELMORE_SUPPORT_BOX);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "h_burinu_q10446_07.htm";
					}
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState st = player.getQuestState(_10445_AnImpendingThreat.class);
		return player.getLevel() >= 99 && st != null && st.isCompleted();
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			if(st.getCond() == 1)
			{
				moblist.put(1023322, st.getInt("1023322"));
			}
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}