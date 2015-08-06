package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import javolution.util.FastList;

public class WareHouseDepositList extends L2GameServerPacket
{
	/*
	 * 0x01-Private Warehouse
	 * 0x02-Clan Warehouse
	 * 0x03-Castle Warehouse
	 * 0x04-Warehouse
	 */

	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int WAREHOUSE_NOT_USE = 4; // Используется или нет?
	public static final int FREIGHT = 1;
	private final long _playerAdena;
	private final FastList<L2ItemInstance> _items;
	private final int _whType;

	public WareHouseDepositList(L2PcInstance player, int type)
	{
		_whType = type;
		_playerAdena = player.getAdenaCount();
		_items = new FastList<>();

		boolean isPrivate = _whType == PRIVATE;
		for(L2ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate, false))
		{
			if(temp != null && temp.isDepositable(isPrivate))
			{
				_items.add(temp);
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeH(_whType);
		writeQ(_playerAdena);
		writeD(0x00); // кол-во слотов
		writeH(-1); // не понятно для чего, -1 итемы отображаются, 0+ итемы не отображаются
		writeH(_items.size());

		for(L2ItemInstance item : _items)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
		_items.clear();
	}
}
