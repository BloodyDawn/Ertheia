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
 * Date: 13.08.11
 * Time: 1:45
 */

public class _10280_MutatedKaneusSchuttgart extends Quest
{
	private static final int VISHOTSKY = 31981;
	private static final int ATRAXIA = 31972;
	private static final int VENOMOUS_STORACE = 18571;
	private static final int KEL_BILETTE = 18573;
	private static final int TISSUE_VS = 13838;
	private static final int TISSUE_KB = 13839;

	public _10280_MutatedKaneusSchuttgart()
	{

		addStartNpc(VISHOTSKY);
		addTalkId(VISHOTSKY, ATRAXIA);
		addKillId(VENOMOUS_STORACE, KEL_BILETTE);
		questItemIds = new int[]{TISSUE_VS, TISSUE_KB};
	}

	public static void main(String[] args)
	{
		new _10280_MutatedKaneusSchuttgart();
	}

	@Override
	public int getQuestId()
	{
		return 10280;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("31981-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("31972-03.htm"))
		{
			st.giveAdena(210000, true);
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
		if(npc.getNpcId() == VENOMOUS_STORACE)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_VS))
					{
						qs.giveItems(TISSUE_VS, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_KB))
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_VS))
				{
					qs.giveItems(TISSUE_VS, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_KB))
					{
						qs.setCond(2);
					}
				}
			}

		}
		else if(npc.getNpcId() == KEL_BILETTE)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_KB))
					{
						qs.giveItems(TISSUE_KB, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_VS))
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_KB))
				{
					qs.giveItems(TISSUE_KB, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_VS))
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
				if(npcId == VISHOTSKY)
				{
					return "31981-06.htm";
				}
				if(npcId == ATRAXIA)
				{
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				}
				break;
			case CREATED:
				if(npcId == VISHOTSKY)
				{
					return player.getLevel() >= 58 ? "31981-01.htm" : "31981-00.htm";
				}
				break;
			case STARTED:
				if(npcId == VISHOTSKY)
				{
					return st.hasQuestItems(TISSUE_VS) && st.hasQuestItems(TISSUE_KB) ? "31981-05.htm" : "31981-04.htm";
				}
				if(npcId == ATRAXIA)
				{
					return st.hasQuestItems(TISSUE_VS) && st.hasQuestItems(TISSUE_KB) ? "31972-02.htm" : "31972-01.htm";
				}
				break;
		}
		return null;
	}
}
