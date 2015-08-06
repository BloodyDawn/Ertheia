package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.scripts.quests._10388_ConspiracyBehindDoors;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.01.13
 * Time: 11:21
 */

public class Kargos extends Quest
{
	private static final int NPC = 33821;

	public Kargos()
	{
		addAskId(NPC, -8399);
	}

	public static void main(String[] args)
	{
		new Kargos();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 1)
		{
			return "kargos003.htm";
		}
		player.teleToLocation(0, 0, 0); // TODO: Координаты куда телепортировать
		return null;
	}

	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(_10388_ConspiracyBehindDoors.class);
		if(st != null && st.isCompleted())
		{
			return "kargos002.htm";
		}
		return "kargos001.htm";
	}
}