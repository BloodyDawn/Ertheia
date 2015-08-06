package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00907_DragonTrophyValakas extends Quest
{
	// NPC's
	private static final int Klein = 31540;

	// Monster
	private static final int Valakas = 29028;

	// Items
	private static final int FloatingStone = 7267;
	private static final int _Medal_of_Glory = 21874;

	public _00907_DragonTrophyValakas()
	{
		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Valakas);
	}

	public static void main(String[] args)
	{
		new _00907_DragonTrophyValakas();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && player.isInsideRadius(npc, 2000, false, false))
		{
			if(st.getCond() == 1 && npc.getNpcId() == Valakas)
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}

	}

	@Override
	public int getQuestId()
	{
		return 907;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == Klein)
		{
			if(event.equalsIgnoreCase("31540-ok.htm"))
			{
				st.startQuest();
			}
		}
		return event;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(killer.isInParty())
		{
			for(L2PcInstance player : killer.getParty().getMembers())
			{
				rewardPlayer(npc, player);
			}
		}
		else
		{
			rewardPlayer(npc, killer);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(st.isNowAvailable() && st.isCompleted())
		{
			st.setState(CREATED);
		}

		if(npc.getNpcId() == Klein)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 84 && st.hasQuestItems(FloatingStone))
					{
						return "31540-01.htm";
					}
					else if(player.getLevel() < 83)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "31540-level.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "31540-noitem.htm";
					}
				case COMPLETED:
					return "31540-complete.htm";
				case STARTED:
					if(st.getCond() == 2)
					{
						st.giveItems(_Medal_of_Glory, 30);
						st.unset("cond");
						st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
						st.exitQuest(QuestType.DAILY);
						return "31540-finish.html";
					}
					else
					{
						return "31540-default.htm";
					}
			}
		}
		return null;
	}
}