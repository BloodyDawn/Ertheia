package dwo.gameserver.network.game.clientpackets.packet.beautyShop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.beautyshop.ExResponseBeautyList;

/**
 * User: Bacek
 * Date: 19.11.12
 * Time: 19:38

 */
public class RequestShowBeautyList extends L2GameClientPacket
{
	// chd
	private int _itemType;

	@Override
	protected void readImpl()
	{
		_itemType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE || player.isInCrystallize())
		{
			return;
		}

		player.sendPacket(new ExResponseBeautyList(player.getAdenaCount(), _itemType));
	}

	@Override
	public String getType()
	{
		return "[C] D0:D7 RequestShowBeautyList";
	}
}
