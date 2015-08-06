package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _10276_MutatedKaneusGludio extends Quest
{
	// Квестовые пресонажи
	private static final int Bathis = 30332;
	private static final int Rohmer = 30344;
	private static final int TomlanKamos = 18554;
	private static final int OlAriosh = 18555;

	// Квестовые предметы
	private static final int DNK1 = 13830;
	private static final int DNK2 = 13831;

	public _10276_MutatedKaneusGludio()
	{

		addStartNpc(Bathis);
		addTalkId(Bathis, Rohmer);
		addKillId(TomlanKamos, OlAriosh);
	}

	public static void main(String[] args)
	{
		new _10276_MutatedKaneusGludio();
	}

	@Override
	public int getQuestId()
	{
		return 10276;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(event.equalsIgnoreCase("30332-03.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("30344-02.htm"))
		{
			st.giveAdena(8500, true);
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
		if(npc.getNpcId() == TomlanKamos)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && qs.getQuestItemsCount(DNK1) == 0)
					{
						qs.giveItems(DNK1, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.getQuestItemsCount(DNK2) >= 1)
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && qs.getQuestItemsCount(DNK1) == 0)
				{
					qs.giveItems(DNK1, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.getQuestItemsCount(DNK2) >= 1)
					{
						qs.setCond(2);
					}
				}
			}

		}
		else if(npc.getNpcId() == OlAriosh)
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
					if(qs.getState() == STARTED && qs.getCond() == 1 && qs.getQuestItemsCount(DNK2) == 0)
					{
						qs.giveItems(DNK2, 1);
						qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
						if(qs.getQuestItemsCount(DNK1) >= 1)
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
				if(qs.getState() == STARTED && qs.getCond() == 1 && qs.getQuestItemsCount(DNK2) == 0)
				{
					qs.giveItems(DNK2, 1);
					qs.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					if(qs.getQuestItemsCount(DNK1) >= 1)
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
		int cond = st.getCond();
		switch(st.getState())
		{
			case COMPLETED:
				if(npcId == Bathis)
				{
					return "30332-0a.htm";
				}
				break;
			case CREATED:
				if(npcId == Bathis)
				{
					return player.getLevel() >= 18 ? "30332-01.htm" : "30332-00.htm";
				}
				break;
			case STARTED:
				if(npcId == Bathis)
				{
					if(cond == 1)
					{
						return "30332-04.htm";
					}
					else if(cond == 2)
					{
						return "30332-05.htm";
					}
				}
				else if(npcId == Rohmer)
				{
					return st.getQuestItemsCount(DNK1) > 0 && st.getQuestItemsCount(DNK2) > 0 ? "30344-01.htm" : "30344-03.htm";
				}
				break;
		}
		return null;
	}
}