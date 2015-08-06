package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import org.apache.commons.lang3.ArrayUtils;

public class _00904_DragonTrophyAntharas extends Quest
{
	// NPC's
	private static final int Theodoric = 30755;

	// Monster
	private static final int[] Antharas = {29019, 29066, 29067, 29068};

	// Items
	private static final int PortalStone = 3865;
	private static final int _Medal_of_Glory = 21874;

	public _00904_DragonTrophyAntharas()
	{

		addStartNpc(Theodoric);
		addTalkId(Theodoric);
		addKillId(Antharas);
	}

	public static void main(String[] args)
	{
		new _00904_DragonTrophyAntharas();
	}

	private void rewardPlayer(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st != null && st.isStarted() && player.isInsideRadius(npc, 2000, false, false))
		{
			if(st.getCond() == 1 && ArrayUtils.contains(Antharas, npc.getNpcId()))
			{
				st.playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
				st.setCond(2);
			}
		}
	}

	@Override
	public int getQuestId()
	{
		return 904;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());

		if(st == null)
		{
			return event;
		}

		if(npc.getNpcId() == Theodoric)
		{
			if(event.equalsIgnoreCase("30755-ok.htm"))
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

		if(npc.getNpcId() == Theodoric)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 84 && st.hasQuestItems(PortalStone))
					{
						return "30755-01.htm";
					}
					else if(player.getLevel() < 83)
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30755-level.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "30755-noitem.htm";
					}
				case COMPLETED:
					return "30755-complete.htm";
				case STARTED:
					return st.getCond() == 2 ? getNoQuestMsg(player) : "30755-default.htm";
			}
		}
		else if(npc.getNpcId() == Theodoric)
		{
			if(st.isStarted() && st.getCond() == 2)
			{
				st.giveItems(_Medal_of_Glory, 30);
				st.unset("cond");
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.DAILY);
				return "30755-finish.html";
			}
		}
		return null;
	}
}