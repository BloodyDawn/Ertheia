package dwo.scripts.quests;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.02.12
 * Time: 17:50
 */

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExQuestNpcLogList;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntIntHashMap;

public class _00496_KalliosTheEmbodimentOfGreed extends Quest
{
	// Квестовые персонажи
	private static final int ИсследовательКартии = 33647;

	// Квестовые монстры
	private static final int Каллиос = 25884;

	// Квестовые предметы
	private static final int СундукХранителя = 34929;

	public _00496_KalliosTheEmbodimentOfGreed()
	{
		addStartNpc(ИсследовательКартии);
		addTalkId(ИсследовательКартии);
		addKillId(Каллиос);
	}

	public static void main(String[] args)
	{
		new _00496_KalliosTheEmbodimentOfGreed();
	}

	@Override
	public int getQuestId()
	{
		return 496;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "agent_cartia_aden_q0496_04.htm";
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
				return "agent_cartia_aden_q0496_03.htm";
			}
			else if(reply == 2 && cond == 2)
			{
				st.unset("cond");
				st.giveItem(СундукХранителя);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
				return "agent_cartia_aden_q0496_07.htm";
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

		if(st.isStarted() && st.getCond() == 1 && npc.getNpcId() == Каллиос)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025884, 1);
			if(player.isInParty())
			{
				player.getParty().getPartyMembersQuestStates(this, 1).stream().filter(state -> Util.checkIfInRange(900, player, st.getPlayer(), false)).forEach(state -> {
					state.getPlayer().sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
					state.setCond(2);
					state.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				});
			}
			else
			{
				player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
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
					if(st.getPlayer().getLevel() < 95)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "agent_cartia_aden_q0496_01a.htm";
					}
					else
					{
						return "agent_cartia_aden_q0496_01.htm";
					}
				case STARTED:
					if(cond == 1)
					{
						return "agent_cartia_aden_q0496_05.htm";
					}
					if(cond == 2)
					{
						return "agent_cartia_aden_q0496_06.htm";
					}
					break;
				case COMPLETED:
					return "agent_cartia_aden_q0496_08.htm";
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 95;

	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1025884, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}