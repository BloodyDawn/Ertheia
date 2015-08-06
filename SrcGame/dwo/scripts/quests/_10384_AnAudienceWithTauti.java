package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 16.08.12
 * Time: 1:14
 */

public class _10384_AnAudienceWithTauti extends Quest
{
	// Квестовые персонажи
	private static final int Фергюсон = 33681;
	private static final int Аку = 33671;

	// Квестовые монстры
	private static final int Таути = 29237;

	// Квестовые предметы
	private static final int Осколок = 34960;
	private static final int Бутыль = 35295;

	public _10384_AnAudienceWithTauti()
	{
		addStartNpc(Фергюсон);
		addTalkId(Фергюсон, Аку);
		addKillId(Таути);
		questItemIds = new int[]{Осколок};
	}

	public static void main(String[] args)
	{
		new _10384_AnAudienceWithTauti();
	}

	@Override
	public int getQuestId()
	{
		return 10384;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "maestro_ferguson_q10384_04.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Фергюсон)
		{
			if(reply == 1)
			{
				return "maestro_ferguson_q10384_02.htm";
			}
			else if(reply == 2)
			{
				return "maestro_ferguson_q10384_03.htm";
			}
			else if(reply == 11 && cond == 3)
			{
				return "maestro_ferguson_q10384_10.htm";
			}
			else if(reply == 12 && cond == 3)
			{
				st.addExpAndSp(951127800, 435041400);
				st.giveAdena(3256740, true);
				st.giveItem(Бутыль);
				st.setState(COMPLETED);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "maestro_ferguson_q10384_11.htm";
			}
		}
		else if(npcId == Аку)
		{
			if(reply == 1 && cond == 1)
			{
				st.setCond(2);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				return "sofa_aku_q10384_02.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, QuestState st)
	{
		L2Party party = st.getPlayer().getParty();
		if(party != null)
		{
			for(L2PcInstance partyMember : party.getMembersInRadius(st.getPlayer(), 900))
			{
				QuestState pst = partyMember.getQuestState(getClass());
				if(pst != null && pst.isStarted() && pst.getCond() == 2)
				{
					pst.giveItem(Осколок);
					pst.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
					pst.setCond(3);
				}
			}
		}
		else
		{
			if(st.getCond() == 2)
			{
				st.giveItem(Осколок);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				st.setCond(3);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		int cond = st.getCond();

		if(npcId == Фергюсон)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState pst = st.getPlayer().getQuestState(_10383_FergasonsOffer.class);
					if(st.getPlayer().getLevel() < 97)
					{
						return "maestro_ferguson_q10384_05.htm";
					}
					else
					{
						return pst == null || !pst.isCompleted() ? "maestro_ferguson_q10384_06.htm" : "maestro_ferguson_q10384_01.htm";
					}
				case STARTED:
					if(cond == 1 || cond == 2)
					{
						return "maestro_ferguson_q10384_08.htm";
					}
					if(cond == 3 && st.hasQuestItems(Осколок))
					{
						return "maestro_ferguson_q10384_09.htm";
					}
				case COMPLETED:
					return "maestro_ferguson_q10384_07.htm";
			}
		}
		else if(npcId == Аку)
		{
			if(st.isStarted())
			{
				return "sofa_aku_q10384_01.htm";
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState pst = player.getQuestState(_10383_FergasonsOffer.class);
		return player.getLevel() >= 97 && pst != null && pst.isCompleted();
	}
}