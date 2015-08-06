package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;

public class _10332_ToughRoad extends Quest
{
	// Квестовые персонажи
	private static final int KAKAI = 30565;
	private static final int BATHIS = 30332;

	// Квестовые предметы
	private static final int LETTER_OF_INTRODUCTION = 17582;

	public _10332_ToughRoad()
	{
		addStartNpc(KAKAI);
		addTalkId(KAKAI, BATHIS);
		questItemIds = new int[]{LETTER_OF_INTRODUCTION};
	}

	public static void main(String[] args)
	{
		new _10332_ToughRoad();
	}

	@Override
	public int getQuestId()
	{
		return 10332;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		QuestState qs = player.getQuestState(getClass());
		if(event.equalsIgnoreCase("movie"))
		{
			player.showQuestMovie(ExStartScenePlayer.SCENE_SI_ILLUSION_04_QUE);
			return null;
		}
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.startQuest();
			qs.giveItem(LETTER_OF_INTRODUCTION);
			startQuestTimer("movie", 3000, null, qs.getPlayer());
			return "kakai_the_lord_of_flame_q10332_04.htm";
		}
		return event;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == BATHIS)
		{
			if(reply == 1 && st.getCond() == 1)
			{
				return "captain_bathia_q10332_03.htm";
			}
			else if(reply == 2 && st.getCond() == 1)
			{
				st.giveAdena(70000, true);
				st.addExpAndSp(90000, 30000);
				st.takeItems(LETTER_OF_INTRODUCTION, -1);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				st.exitQuest(QuestType.ONE_TIME);
				return "captain_bathia_q10332_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == KAKAI)
		{
			switch(st.getState())
			{
				case CREATED:
					if(player.getLevel() >= 20 && player.getLevel() < 40)
					{
						return "kakai_the_lord_of_flame_q10332_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "kakai_the_lord_of_flame_q10332_03.htm";
					}
				case STARTED:
					if(st.getCond() == 1)
					{
						return "kakai_the_lord_of_flame_q10332_05.htm";
					}
					break;
				case COMPLETED:
					return "kakai_the_lord_of_flame_q10332_02.htm";
			}
		}
		else if(npc.getNpcId() == BATHIS)
		{
			if(st.isStarted())
			{
				if(st.getCond() == 1)
				{
					return "captain_bathia_q10332_01.htm";
				}
			}
			else
			{
				return st.isCompleted() ? "captain_bathia_q10332_02a.htm" : "captain_bathia_q10332_02.htm";
			}
		}
		return null;
	}

	@Override
	public boolean canBeStarted(L2PcInstance player)
	{
		return player.getLevel() >= 20 && player.getLevel() <= 40;
	}
} 