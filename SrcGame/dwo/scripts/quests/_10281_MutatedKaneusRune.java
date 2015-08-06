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
 * Time: 1:57
 */
public class _10281_MutatedKaneusRune extends Quest
{
	private static final int MATHIAS = 31340;
	private static final int KAYAN = 31335;
	private static final int WHITE_ALLOSCE = 18577;

	private static final int TISSUE_WA = 13840;

	public _10281_MutatedKaneusRune()
	{

		addStartNpc(MATHIAS);
		addTalkId(MATHIAS, KAYAN);
		addKillId(WHITE_ALLOSCE);
		questItemIds = new int[]{TISSUE_WA};
	}

	public static void main(String[] args)
	{
		new _10281_MutatedKaneusRune();
	}

	@Override
	public int getQuestId()
	{
		return 10281;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("31340-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("31335-03.htm"))
		{
			st.giveAdena(360000, true);
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
		if(npc.getNpcId() == WHITE_ALLOSCE)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_WA))
					{
						qs.giveItems(TISSUE_WA, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_WA))
				{
					qs.giveItems(TISSUE_WA, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
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
				if(npcId == MATHIAS)
				{
					return "31340-06.htm";
				}
				if(npcId == KAYAN)
				{
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				}
				break;
			case CREATED:
				if(npcId == MATHIAS)
				{
					return player.getLevel() >= 68 ? "31340-01.htm" : "31340-00.htm";
				}
				break;
			case STARTED:
				if(npcId == MATHIAS)
				{
					return st.hasQuestItems(TISSUE_WA) ? "31340-05.htm" : "31340-04.htm";
				}
				if(npcId == KAYAN)
				{
					return st.hasQuestItems(TISSUE_WA) ? "31335-02.htm" : "31335-01.htm";
				}
				break;
		}
		return null;
	}
}