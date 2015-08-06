package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00650_ABrokenDream extends Quest
{
	// NPC
	private static final int RailroadEngineer = 32054;
	// mobs
	private static final int ForgottenCrewman = 22027;
	private static final int VagabondOfTheRuins = 22028;
	// QuestItem
	private static final int RemnantsOfOldDwarvesDreams = 8514;

	public _00650_ABrokenDream()
	{
		addStartNpc(RailroadEngineer);
		addKillId(ForgottenCrewman);
		addKillId(VagabondOfTheRuins);
		questItemIds = new int[]{RemnantsOfOldDwarvesDreams};
	}

	public static void main(String[] args)
	{
		new _00650_ABrokenDream();
	}

	@Override
	public int getQuestId()
	{
		return 650;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(event.equalsIgnoreCase("2a.htm"))
		{
			st.startQuest();
		}
		else if(event.equalsIgnoreCase("500.htm"))
		{
			st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
			st.exitQuest(QuestType.REPEATABLE);
			st.unset("cond");
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		QuestState st = killer.getQuestState(getClass());

		if(npc == null || st == null)
		{
			return null;
		}

		st.rollAndGive(RemnantsOfOldDwarvesDreams, 1, 1, 68);

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		if(st.getCond() == 0)
		{
			QuestState OceanOfDistantStar = st.getPlayer().getQuestState(_00117_OceanOfDistantStar.class);
			if(OceanOfDistantStar != null)
			{
				if(OceanOfDistantStar.isCompleted())
				{
					if(st.getPlayer().getLevel() < 39)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "100.htm";
					}
					else
					{
						return "200.htm";
					}
				}
				else
				{
					st.exitQuest(QuestType.REPEATABLE);
					return "600.htm";
				}
			}
			else
			{
				st.exitQuest(QuestType.REPEATABLE);
				return "600.htm";
			}
		}
		if(st.getCond() == 1)
		{
			return "400.htm";
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		QuestState qs = player.getQuestState(_00117_OceanOfDistantStar.class);
		return qs != null && qs.isCompleted() && player.getLevel() >= 39;
	}
}