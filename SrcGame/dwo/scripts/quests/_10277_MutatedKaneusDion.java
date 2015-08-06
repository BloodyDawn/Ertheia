package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 11.08.11
 * Time: 22:42
 */

public class _10277_MutatedKaneusDion extends Quest
{
	// Квестовые персонажи
	private static final int LUKAS = 30071;
	private static final int MIRIEN = 30461;
	private static final int CRIMSON_HATU = 18558;
	private static final int SEER_FLOUROS = 18559;

	// Квестовые предметы
	private static final int TISSUE_CH = 13832;
	private static final int TISSUE_SF = 13833;

	public _10277_MutatedKaneusDion()
	{

		addStartNpc(LUKAS);
		addTalkId(LUKAS, MIRIEN);
		addKillId(CRIMSON_HATU, SEER_FLOUROS);
		questItemIds = new int[]{TISSUE_CH, TISSUE_SF};
	}

	public static void main(String[] args)
	{
		new _10277_MutatedKaneusDion();
	}

	@Override
	public int getQuestId()
	{
		return 10277;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("30071-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30461-03.htm"))
		{
			st.giveAdena(20000, true);
			st.unset("cond");
			st.exitQuest(QuestType.ONE_TIME);
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		L2Party party = player.getParty();
		if(npc.getNpcId() == CRIMSON_HATU)
		{
			if(party != null)
			{
				for(L2PcInstance partyMember : party.getMembersInRadius(player, 900))
				{
					QuestState qs = partyMember.getQuestState(getClass());
					if(qs == null)
					{
						return null;
					}
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_CH))
					{
						qs.giveItems(TISSUE_CH, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_SF))
						{
							qs.setCond(2);
						}
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(getClass());
				if(qs == null)
				{
					return null;
				}
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_CH))
				{
					qs.giveItems(TISSUE_CH, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_SF))
					{
						qs.setCond(2);
					}
				}
			}

		}
		else if(npc.getNpcId() == SEER_FLOUROS)
		{
			if(party != null)
			{
				for(L2PcInstance partyMember : party.getMembersInRadius(player, 900))
				{
					QuestState qs = partyMember.getQuestState(getClass());
					if(qs == null)
					{
						return null;
					}
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_SF))
					{
						qs.giveItems(TISSUE_SF, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_CH))
						{
							qs.setCond(2);
						}
					}
				}
			}
			else
			{
				QuestState qs = player.getQuestState(getClass());
				if(qs == null)
				{
					return null;
				}
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_SF))
				{
					qs.giveItems(TISSUE_SF, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_CH))
					{
						qs.setCond(2);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		int npcId = npc.getNpcId();

		switch(st.getState())
		{
			case COMPLETED:
				if(npcId == LUKAS)
				{
					return "30071-06.htm";
				}
				if(npcId == MIRIEN)
				{
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				}
				break;
			case CREATED:
				if(npcId == LUKAS)
				{
					return player.getLevel() >= 28 ? "30071-01.htm" : "30071-00.htm";
				}
				break;
			case STARTED:
				if(npcId == LUKAS)
				{
					return st.hasQuestItems(TISSUE_CH) && st.hasQuestItems(TISSUE_SF) ? "30071-05.htm" : "30071-04.htm";
				}
				if(npcId == MIRIEN)
				{
					return st.hasQuestItems(TISSUE_CH) && st.hasQuestItems(TISSUE_SF) ? "30461-02.htm" : "30461-01.htm";
				}
				break;
		}
		return null;
	}
}
