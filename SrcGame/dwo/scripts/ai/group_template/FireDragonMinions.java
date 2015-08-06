package dwo.scripts.ai.group_template;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 10.10.11
 * Time: 10:42
 */

public class FireDragonMinions extends Quest
{
	public FireDragonMinions()
	{
		registerMobs(new int[]{29029});
	}

	public static void main(String[] args)
	{
		new FireDragonMinions();
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		npc.setIsInvul(true);
		return super.onSpawn(npc);
	}
}