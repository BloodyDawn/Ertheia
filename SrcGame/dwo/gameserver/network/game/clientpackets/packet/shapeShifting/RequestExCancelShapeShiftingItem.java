package dwo.gameserver.network.game.clientpackets.packet.shapeShifting;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:24
 */
public class RequestExCancelShapeShiftingItem extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// пусто
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		activeChar.setIsEnchanting(false);
		activeChar.setActiveShapeShiftingSupportItem(null);
		activeChar.setActiveShapeShiftingTargetItem(null);
		activeChar.setActiveShapeShiftingItem(null);
	}

	@Override
	public String getType()
	{
		return "[C] 0xd0:0xc2 RequestExCancelShapeShiftingItem";
	}
}
