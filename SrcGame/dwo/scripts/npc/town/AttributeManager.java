package dwo.scripts.npc.town;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.attribute.ExShowBaseAttributeCancelWindow;

/**
 * User: Bacek
 * Date: 19.06.13
 * Time: 14:26
 */
public class AttributeManager extends Quest
{
	private static final int[] managers = {32325, 32326};

	public AttributeManager()
	{
		addAskId(managers, -550);
	}

	public static void main(String[] args)
	{
		new AttributeManager();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 611)
		{
			player.sendPacket(new ExShowBaseAttributeCancelWindow(player));
		}
		return null;
	}
}
