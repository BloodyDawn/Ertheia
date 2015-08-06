package dwo.scripts.quests;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestType;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.08.11
 * Time: 12:49
 */

public class _10320_ToTheCentralSquare extends Quest
{
	// Квестовые персонажи
	private static final int PANTHEON = 32972;
	private static final int THEODORE = 32975;

	// Списки с персонажами, которые заходили в зону проигрывания роликов
	private static final List<Integer> first = new ArrayList<>();
	private static final List<Integer> second = new ArrayList<>();

	public _10320_ToTheCentralSquare()
	{
		addStartNpc(PANTHEON);
		addTalkId(PANTHEON, THEODORE);
		addEnterZoneId(33010);
	}

	public static void main(String[] args)
	{
		new _10320_ToTheCentralSquare();
	}

	@Override
	public int getQuestId()
	{
		return 10320;
	}

	@Override
	public String onEvent(String event, QuestState qs)
	{
		if(event.equals("quest_accept") && !qs.isCompleted())
		{
			qs.getPlayer().sendPacket(new TutorialShowHtml(TutorialShowHtml.CLIENT_SIDE, "..\\L2text\\QT_001_Radar_01.htm"));
			qs.startQuest();
			return "si_illusion_pantheon_q10320_05.htm";
		}
		return null;
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, QuestState st, int reply)
	{
		if(npc.getNpcId() == PANTHEON)
		{
			if(reply == 1)
			{
				return "si_illusion_pantheon_q10320_02.htm";
			}
		}
		else if(npc.getNpcId() == THEODORE)
		{
			if(reply == 2 && st.isStarted())
			{
				st.addExpAndSp(30, 5);
				st.giveAdena(3000, true);
				st.exitQuest(QuestType.ONE_TIME);
				st.playSound(QuestSound.ITEMSOUND_QUEST_FINISH);
				return "si_illusion_theodore_q10320_04.htm";
			}
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, QuestState st)
	{
		L2PcInstance player = st.getPlayer();

		if(npc.getNpcId() == PANTHEON)
		{
			switch(st.getState())
			{
				case COMPLETED:
					return "si_illusion_pantheon_q10320_04.htm";
				case CREATED:
					if(player.getLevel() < 20)
					{
						return "si_illusion_pantheon_q10320_01.htm";
					}
					else
					{
						st.exitQuest(QuestType.REPEATABLE);
						return "si_illusion_pantheon_q10320_03.htm";
					}
				case STARTED:
					return "si_illusion_pantheon_q10320_06.htm";
			}
		}
		else if(npc.getNpcId() == THEODORE)
		{
			if(player.getLevel() >= 20)
			{
				return "si_illusion_theodore_q10320_02.htm";
			}
			else if(st.isCompleted())
			{
				return "si_illusion_theodore_q10320_03.htm";
			}
			else if(st.isStarted())
			{
				return "si_illusion_theodore_q10320_01.htm";
			}
		}
		return getNoQuestMsg(player);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		if(player == null)
		{
			return null;
		}

		if(player.getLevel() < 20)
		{
			QuestState st = player.getQuestState(getClass());
			if((st == null || st.isCreated()) && !first.contains(player.getObjectId()))
			{
				player.showQuestMovie(ExStartScenePlayer.SCENE_MUSEUM_EXIT_1);
				first.add(player.getObjectId());
			}
			else if(st != null && st.getCond() == 1 && !second.contains(player.getObjectId()))
			{
				player.showQuestMovie(ExStartScenePlayer.SCENE_MUSEUM_EXIT_2);
				second.add(player.getObjectId());
			}
		}
		return null;
	}
}
