package dwo.gameserver.network.game.clientpackets.packet.CuriousHouse;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.curioushouse.ExCuriousHouseObserveList;

/**
 * Requests for available Chaos Festival battles.
 * @author Bacek
 * @author Yorie
 */
public class RequestObservingListCuriousHouse extends L2GameClientPacket
{

	@Override
	protected void readImpl()
	{
		// Пусто
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
		{
			return;
		}

		player.sendPacket(new ExCuriousHouseObserveList());
	}

	@Override
	public String getType()
	{
		return "[C] D0:C6 RequestObservingListCuriousHouse";
	}
}
