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
 * Time: 1:29
 */
public class _10279_MutatedKaneusOren extends Quest
{
	private static final int MOUEN = 30196;
	private static final int ROVIA = 30189;
	private static final int KAIM_ABIGORE = 18566;
	private static final int KNIGHT_MONTAGNAR = 18568;

	private static final int TISSUE_KA = 13836;
	private static final int TISSUE_KM = 13837;

	public _10279_MutatedKaneusOren()
	{

		addStartNpc(MOUEN);
		addTalkId(MOUEN, ROVIA);
		addKillId(KAIM_ABIGORE, KNIGHT_MONTAGNAR);
		questItemIds = new int[]{TISSUE_KA, TISSUE_KM};
	}

	public static void main(String[] args)
	{
		new _10279_MutatedKaneusOren();
	}

	@Override
	public int getQuestId()
	{
		return 10279;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("30196-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30189-03.htm"))
		{
			st.giveAdena(100000, true);
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
		if(npc.getNpcId() == KAIM_ABIGORE)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_KA))
					{
						qs.giveItems(TISSUE_KA, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_KM))
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_KA))
				{
					qs.giveItems(TISSUE_KA, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_KM))
					{
						qs.setCond(2);
					}
				}
			}

		}
		else if(npc.getNpcId() == KNIGHT_MONTAGNAR)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_KM))
					{
						qs.giveItems(TISSUE_KM, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_KA))
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_KM))
				{
					qs.giveItems(TISSUE_KM, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_KA))
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
				if(npcId == MOUEN)
				{
					return "30196-06.htm";
				}
				if(npcId == ROVIA)
				{
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				}
				break;
			case CREATED:
				if(npcId == MOUEN)
				{
					return player.getLevel() >= 45 ? "30196-01.htm" : "30196-00.htm";
				}
				break;
			case STARTED:
				if(npcId == MOUEN)
				{
					return st.hasQuestItems(TISSUE_KA) && st.hasQuestItems(TISSUE_KM) ? "30196-05.htm" : "30196-04.htm";
				}
				if(npcId == ROVIA)
				{
					return st.hasQuestItems(TISSUE_KA) && st.hasQuestItems(TISSUE_KM) ? "30189-02.htm" : "30189-01.htm";
				}
				break;
		}
		return null;
	}
}
