package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowCalc;

public class Calculator implements IItemHandler
{
	private static final int CalculatorId = 4393;

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!playable.isPlayer())
		{
			return false;
		}
		playable.broadcastPacket(new ShowCalc(CalculatorId));
		return true;
	}
}
