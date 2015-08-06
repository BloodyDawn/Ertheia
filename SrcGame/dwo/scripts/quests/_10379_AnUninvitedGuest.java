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
 * Date: 31.08.12
 * Time: 14:27
 */

public class _10379_AnUninvitedGuest extends Quest
{
	// Квестовые персонажи
	private static final int Эндриго = 30632;

	// Квестовые монстры
	private static final int Скальдисект = 23212;

	// Квестовые предметы
	private static final int СвитокТелепорта = 35292;

	public _10379_AnUninvitedGuest()
	{
		addStartNpc(Эндриго);
		addTalkId(Эндриго);
		addKillId(Скальдисект);
	}

	public static void main(String[] args)
	{
		new _10379_AnUninvitedGuest();
	}

	private void giveItem(QuestState pst)
	{
		if(pst != null && pst.getCond() == 1)
		{
			sendNpcLogList(pst.getPlayer());
			pst.setCond(2);
			pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}

	@Override
	public int getQuestId()
	{
		return 10379;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "warden_endrigo_q10379_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Эндриго)
		{
			if(reply == 1)
			{
				return "warden_endrigo_q10379_04.htm";
			}
			else if(reply == 2)
			{
				return "warden_endrigo_q10379_05.htm";
			}
			else if(reply == 10 && cond == 2)
			{
				st.addExpAndSp(934013430, 418281570);
				st.giveAdena(3441680, true);
				st.giveItems(СвитокТелепорта, 2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "warden_endrigo_q10379_09.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();
		if(npc.getNpcId() == Скальдисект)
		{
			if(player.getParty() == null)
			{
				giveItem(player.getQuestState(getClass()));
			}
			else
			{
				for(L2PcInstance member : player.getParty().getMembersInRadius(player, 900))
				{
					giveItem(member.getQuestState(getClass()));
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Эндриго)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "warden_endrigo_q10379_03.htm";
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10377_TheInvadedExecutionGrounds.class);
					return pst == null || !pst.isCompleted() || st.getPlayer().getLevel() < 95 ? "warden_endrigo_q10379_02.htm" : "warden_endrigo_q10379_01.htm";
				case STARTED:
					if(cond == 1)
					{
						return "warden_endrigo_q10379_07.htm";
					}
					else if(cond == 2)
					{
						return "warden_endrigo_q10379_08.htm";
					}
			}
		}

		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10377_TheInvadedExecutionGrounds.class);
		return player.getLevel() >= 95 && pst != null && pst.isCompleted();
	}

	@Override
	public void sendNpcLogList(L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null)
		{
			TIntIntHashMap moblist = new TIntIntHashMap();
			moblist.put(1023212, st.getCond() - 1);
			player.sendPacket(new ExQuestNpcLogList(getQuestId(), moblist));
		}
	}
}