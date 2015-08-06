package dwo.gameserver.handler.items;

import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2ControllableAirShipInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class EnergyStarStone extends ItemSkills
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2AirShipInstance ship = ((L2PcInstance) playable).getAirShip();
		if(ship == null || !(ship instanceof L2ControllableAirShipInstance) || ship.getFuel() >= ship.getMaxFuel())
		{
			playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
			playable.sendActionFailed();
			return false;
		}
		return super.useItem(playable, item, forceUse);
	}
}