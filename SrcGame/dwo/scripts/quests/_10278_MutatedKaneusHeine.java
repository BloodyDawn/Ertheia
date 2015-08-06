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
 * Date: 12.08.11
 * Time: 18:29
 */

public class _10278_MutatedKaneusHeine extends Quest
{
	private static final int GOSTA = 30916;
	private static final int MINEVIA = 30907;
	private static final int BLADE_OTIS = 18562;
	private static final int WEIRD_BUNEI = 18564;

	private static final int TISSUE_BO = 13834;
	private static final int TISSUE_WB = 13835;

	public _10278_MutatedKaneusHeine()
	{

		addStartNpc(GOSTA);
		addTalkId(GOSTA, MINEVIA);
		addKillId(BLADE_OTIS, WEIRD_BUNEI);
		questItemIds = new int[]{TISSUE_BO, TISSUE_WB};
	}

	public static void main(String[] args)
	{
		new _10278_MutatedKaneusHeine();
	}

	@Override
	public int getQuestId()
	{
		return 10278;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}
		if(event.equalsIgnoreCase("30916-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30907-03.htm"))
		{
			st.giveAdena(50000, true);
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
		if(npc.getNpcId() == BLADE_OTIS)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_BO))
					{
						qs.giveItems(TISSUE_BO, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_WB))
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_BO))
				{
					qs.giveItems(TISSUE_BO, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_WB))
					{
						qs.setCond(2);
					}
				}
			}

		}
		else if(npc.getNpcId() == WEIRD_BUNEI)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_WB))
					{
						qs.giveItems(TISSUE_WB, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.hasQuestItems(TISSUE_BO))
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && !qs.hasQuestItems(TISSUE_WB))
				{
					qs.giveItems(TISSUE_WB, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.hasQuestItems(TISSUE_BO))
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
				if(npcId == GOSTA)
				{
					return "30916-06.htm";
				}
				if(npcId == MINEVIA)
				{
					return getAlreadyCompletedMsg(player, QuestType.ONE_TIME);
				}
				break;
			case CREATED:
				if(npcId == GOSTA)
				{
					return player.getLevel() >= 48 ? "30916-01.htm" : "30916-00.htm";
				}
				break;
			case STARTED:
				if(npcId == GOSTA)
				{
					return st.hasQuestItems(TISSUE_BO) && st.hasQuestItems(TISSUE_WB) ? "30916-05.htm" : "30916-04.htm";
				}
				if(npcId == MINEVIA)
				{
					return st.hasQuestItems(TISSUE_BO) && st.hasQuestItems(TISSUE_WB) ? "30907-02.htm" : "30907-01.htm";
				}
				break;
		}
		return null;
	}
}
