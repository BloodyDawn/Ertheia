package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 17.05.12
 * Time: 23:37
 */

public class Mozzarella extends Quest
{
	public Mozzarella()
	{
		addTeleportRequestId(30483);
	}

	public static void main(String[] args)
	{
		new Mozzarella();
	}

	@Override
	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		player.teleToLocation(17728, 115139, -11752);
		return null;
	}
}