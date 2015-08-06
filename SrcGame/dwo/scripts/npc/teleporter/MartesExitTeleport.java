package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.Location;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.12
 * Time: 1:01
 */

public class MartesExitTeleport extends Quest
{
	public MartesExitTeleport()
	{

		addStartNpc(33292);
		addTalkId(33292);
	}

	public static void main(String[] args)
	{
		new MartesExitTeleport();
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getClass());
		if(st == null)
		{
			return getNoQuestMsg(player);
		}

		player.teleToInstance(new Location(17119, 114729, -3440), 0);
		return null;
	}
}