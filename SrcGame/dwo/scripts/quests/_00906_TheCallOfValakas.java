package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;

public class _00906_TheCallOfValakas extends Quest
{
	// Квестовые персонажи
	private static final int Klein = 31540;

	// Квестовые монстры
	private static final int Lavasaurus_Pustbon = 29029;

	// Квестовые предметы
	private static final int FloatingStone = 7267;
	private static final int Reward = 21895;
	private static final int Lavasaurus_Alpha_Fragment = 21993;

	public _00906_TheCallOfValakas()
	{
		addStartNpc(Klein);
		addTalkId(Klein);
		addKillId(Lavasaurus_Pustbon);
		questItemIds = new int[]{Lavasaurus_Alpha_Fragment};
	}

	public static void main(String[] args)
	{
		new _00906_TheCallOfValakas();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && player.isInsideRadius(npc, 2000, false, false))
		{
			if(st.getCond() == 1)
			{
				st.setCond(2);
				st.giveItems(Lavasaurus_Alpha_Fragment, 1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 906;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return null;
		}

		if(npc.getNpcId() == Klein)
		{
			if(event.equalsIgnoreCase("31540-ok.html"))
			{
				st.startQuest();
				return "31540-ok.html";
			}
		}
		return null;
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
					if(player.getLevel() >= 83 && st.hasQuestItems(FloatingStone))
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
						st.takeItems(Lavasaurus_Alpha_Fragment, 1);
						st.giveItems(Reward, 1);
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

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return !(player.getLevel() < 83 && player.getInventory().getCountOf(FloatingStone) < 1);
	}
}