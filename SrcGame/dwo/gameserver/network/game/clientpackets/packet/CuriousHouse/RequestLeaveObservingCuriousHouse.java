package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseObserveMode;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 29.09.12
 * Time: 22:52
 */
public class RequestLeaveObservingCuriousHouse extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Ничего
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar != null && activeChar.getObserverController().isObserving())
		{
			activeChar.getObserverController().leave();
			activeChar.sendPacket(new ExCuriousHouseObserveMode(false));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:C8 RequestLeaveObservingCuriousHouse";
	}
}
