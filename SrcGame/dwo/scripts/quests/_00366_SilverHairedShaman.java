package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.util.Rnd;

public class _00366_SilverHairedShaman extends Quest
{
	// Квестовые персонажи
	public static final int DIETER = 30111;

	// Квесстовые предметяы
	public static final int HAIR = 5874;

	// Квестовые монстры
	private final int[] MOBS = {20986, 20987, 20988, 20989};

	public _00366_SilverHairedShaman()
	{
		addStartNpc(DIETER);
		addTalkId(DIETER);
		addKillId(MOBS);
		questItemIds = new int[]{HAIR};
	}

	public static void main(String[] args)
	{
		new _00366_SilverHairedShaman();
	}

	@Override
	public int getQuestId()
	{
		return 366;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && qs.getPlayer().getLevel() >= 48)
		{
			qs.startQuest();
			return "dieter_q0366_03.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		int npcId = npc.getNpcId();

		if(npcId == DIETER)
		{
			switch(reply)
			{
				case 1:
					st.exitQuest(QuestType.REPEATABLE);
					st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
					return "dieter_q0366_06.htm";
				case 2:
					return "dieter_q0366_07.htm";

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
				if(pst != null && pst.isStarted() && Rnd.getChance(66))
				{
					pst.giveItems(HAIR, 1);
					pst.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
			}
		}
		else
		{
			if(st.isStarted())
			{
				st.giveItems(HAIR, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		int npcId = npc.getNpcId();
		if(npcId == DIETER)
		{
			switch(st.getState())
			{
				case CREATED:
					if(st.getPlayer().getLevel() >= 48)
					{
						return "dieter_q0366_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "dieter_q0366_02.htm";
					}
				case STARTED:
					if(st.getQuestItemsCount(HAIR) < 1)
					{
						return "dieter_q0366_04.htm";
					}
					else
					{
						st.giveAdena(12070 + 500 * st.getQuestItemsCount(HAIR), true);
						st.takeItems(HAIR, -1);
						return "dieter_q0366_05.htm";
					}
			}
		}
		return getNoQuestMsg(st.getPlayer());
	}
}