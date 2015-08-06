package dwo.gameserver.network.game.clientpackets.packet.primeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
//TODO
public class RequestBR_ProductInfo extends L2GameClientPacket
{
	private int _brId;

	@Override
	protected void readImpl()
	{
		_brId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player != null)
		{
			//PrimeShopTable.getInstance().showProductInfo(player, _brId);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:8B RequestBRProductInfo";
	}
}