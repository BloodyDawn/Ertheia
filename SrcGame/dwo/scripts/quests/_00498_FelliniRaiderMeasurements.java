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
 * Date: 19.02.12
 * Time: 0:11
 */

public class _00498_FelliniRaiderMeasurements extends Quest
{
	// Квестовые персонажи
	private static final int ИсследовательКартии = 33647;

	// Квестовые монстры
	private static final int Феллини = 19254;

	// Квестовые предметы
	private static final int СундукХранителя = 34931;

	public _00498_FelliniRaiderMeasurements()
	{
		addStartNpc(ИсследовательКартии);
		addTalkId(ИсследовательКартии);
		addKillId(Феллини);
	}

	public static void main(String[] args)
	{
		new _00498_FelliniRaiderMeasurements();
	}

	@Override
	public int getQuestId()
	{
		return 498;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "agent_cartia_aden_q0498_07.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();
		if(npcId == ИсследовательКартии)
		{
			if(reply == 1)
			{
				return "agent_cartia_aden_q0498_04.htm";
			}
			else if(reply == 2)
			{
				return "agent_cartia_aden_q0498_05.htm";
			}
			else if(reply == 3)
			{
				return "agent_cartia_aden_q0498_06.htm";
			}
			else if(reply == 4 && cond == 2)
			{
				st.unset("cond");
				st.giveItem(СундукХранителя);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
				return "agent_cartia_aden_q0498_10.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		QuestState st = player.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		if(st.isStarted() && st.getCond() == 1 && npc.getNpcId() == Феллини)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025883, 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
			st.setCond(2);
			st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npcId == ИсследовательКартии)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() < 90 || st.getPlayer().getLevel() > 95)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "agent_cartia_aden_q0498_02.htm";
					}
					else
					{
						return "agent_cartia_aden_q0498_01.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "agent_cartia_aden_q0498_08.htm";
					}
					if(cond == 2)
					{
						return "agent_cartia_aden_q0498_09.htm";
					}
					break;
				case COMPLETED:
					return "agent_cartia_aden_q0498_03.htm";
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() < 95 && player.getLevel() >= 90;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025883, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}