package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.05.12
 * Time: 0:38
 */

public class _00150_ExtremeChallengePrimalMotherResurrected extends Quest
{
	// Квестовые персонажи
	private static final int LUMIERE = 33293;

	// Квестовые монстры
	private static final int EXTREME_ISTINA = 29196;

	// Квестовые предметы
	private static final int SHILEN_HIGH_LEVEL_MARK = 17590;
	private static final int ISTINA_SOUL_BOTTLE = 34883;

	public _00150_ExtremeChallengePrimalMotherResurrected()
	{
		addStartNpc(LUMIERE);
		addTalkId(LUMIERE);
		addKillId(EXTREME_ISTINA);
		questItemIds = new int[]{SHILEN_HIGH_LEVEL_MARK};
	}

	public static void main(String[] args)
	{
		new _00150_ExtremeChallengePrimalMotherResurrected();
	}

	@Override
	public int getQuestId()
	{
		return 150;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			return "officer_lumiere_inzone_q0150_06.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == LUMIERE)
		{
			if(reply == 1)
			{
				return "officer_lumiere_inzone_q0150_04.htm";
			}
			else if(reply == 2)
			{
				return "officer_lumiere_inzone_q0150_05.htm";
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(player == null)
		{
			return null;
		}

		if(npc.getNpcId() == EXTREME_ISTINA)
		{
			if(player.getParty() == null)
			{
				giveItem(player.getQuestState(getClass()));
			}
			else
			{
				if(player.getParty().isInCommandChannel())
				{
					for(L2PcInstance member : player.getParty().getCommandChannel().getMembers())
					{
						giveItem(member.getQuestState(getClass()));
					}

				}
				else
				{
					for(L2PcInstance member : player.getParty().getMembers())
					{
						giveItem(member.getQuestState(getClass()));
					}
				}
			}

		}
		return super.onKill(npc, player, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == LUMIERE)
		{
			switch(st.getState())
			{
				case CREATED:
					QuestState prevst = player.getQuestState(_00149_PrimalMotherIstina.class);
					if(player.getLevel() < 97)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "officer_lumiere_inzone_q0150_02.htm";
					}
					else
					{
						return prevst == null || !prevst.isCompleted() ? "officer_lumiere_inzone_q0150_20.htm" : "officer_lumiere_inzone_q0150_01.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "officer_lumiere_inzone_q0150_07.htm";
					}
					if(st.getCond() == 2 && st.hasQuestItems(SHILEN_HIGH_LEVEL_MARK))
					{
						st.giveItem(ISTINA_SOUL_BOTTLE);
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.ONE_TIME);
						return "officer_lumiere_inzone_q0150_08.htm";
					}
					break;
				case COMPLETED:
					return "officer_lumiere_inzone_q0150_03.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState prevst = player.getQuestState(_00149_PrimalMotherIstina.class);
		return player.getLevel() >= 90 && prevst != null && prevst.isCompleted();

	}

	private void giveItem(QuestState pst)
	{
		if(pst != null && pst.getCond() == 1)
		{
			pst.setCond(2);
			pst.giveItem(SHILEN_HIGH_LEVEL_MARK);
			pst.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
		}
	}
}