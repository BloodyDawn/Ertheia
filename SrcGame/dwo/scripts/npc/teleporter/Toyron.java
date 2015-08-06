package dwo.scripts.npc.teleporter;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.06.12
 * Time: 19:29
 */

public class Toyron extends Quest
{
	private static final int Toyron = 33004;

	public Toyron()
	{
		addAskId(Toyron, -3526);
		addAskId(Toyron, -3527);
	}

	public static void main(String[] args)
	{
		new Toyron();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		switch(ask)
		{
			case -3526: // Просмотреть Записи Героев
				if(reply == 1)
				{
					// player.sendPacket(new ExShowStatPage(-2)); УБРАНО В LINDVIOR
					return null;
				}
				break;
			case -3527: // Выйти из музея
				if(reply == 1)
				{
					player.teleToLocation(-114371, 260183, -1192);
					player.getInstanceController().setInstanceId(0);
				}
				break;
		}
		return null;
	}
}