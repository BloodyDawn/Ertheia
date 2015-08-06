package dwo.gameserver.network.game.serverpackets.packet.recipeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2ManufactureItem;
import dwo.gameserver.model.player.L2ManufactureList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class RecipeShopSellList extends L2GameServerPacket
{
	private L2PcInstance _buyer;
	private L2PcInstance _manufacturer;

	public RecipeShopSellList(L2PcInstance buyer, L2PcInstance manufacturer)
	{
		_buyer = buyer;
		_manufacturer = manufacturer;
	}

	@Override
	protected void writeImpl()
	{
		L2ManufactureList createList = _manufacturer.getCreateList();

		if(createList != null)
		{
			//dddd d(ddd)
			writeD(_manufacturer.getObjectId());
			writeD((int) _manufacturer.getCurrentMp());//Creator's MP
			writeD(_manufacturer.getMaxMp());//Creator's MP
			writeQ(_buyer.getAdenaCount());//Buyer Adena

			int count = createList.size();
			writeD(count);
			L2ManufactureItem temp;

			for(int i = 0; i < count; i++)
			{
				temp = createList.getList().get(i);
				writeD(temp.getRecipeId());
				writeD(0x00); //unknown
				writeQ(temp.getCost());
			}
		}
	}
}
