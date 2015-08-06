package dwo.gameserver.network.game.serverpackets.packet.gmview;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private L2ItemInstance[] _items;
	private String _playerName;
	private L2PcInstance _activeChar;
	private long _money;

	public GMViewWarehouseWithdrawList(L2PcInstance cha)
	{
		_activeChar = cha;
		_items = _activeChar.getWarehouse().getItems();
		_playerName = _activeChar.getName();
		_money = _activeChar.getWarehouse().getAdenaCount();
	}

	public GMViewWarehouseWithdrawList(L2Clan clan)
	{
		_playerName = clan.getLeaderName();
		_items = clan.getWarehouse().getItems();
		_money = clan.getWarehouse().getAdenaCount();
	}

	@Override
	protected void writeImpl()
	{
		writeS(_playerName);
		writeQ(_money);
		writeH(_items.length);
		for(L2ItemInstance item : _items)
		{
			writeItemInfo(item);
			writeD(item.getObjectId());
		}
	}
}
