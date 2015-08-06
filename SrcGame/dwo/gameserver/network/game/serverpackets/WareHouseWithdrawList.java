package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;

public class WareHouseWithdrawList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3; //not sure
	public static final int WAREHOUSE_NOT_USE = 4; // Используется или нет?
	public static final int FREIGHT = 1;
	private L2PcInstance _activeChar;
	private long _playerAdena;
	private L2ItemInstance[] _items;
	private int _whType;
    private final int _invSize;
    private final List<Integer> _itemsStackable = new ArrayList<>();

    public WareHouseWithdrawList(L2PcInstance player, int type)
	{
		_activeChar = player;
		_whType = type;

		_playerAdena = _activeChar.getAdenaCount();
        _invSize = player.getInventory().getSize();

		if(_activeChar.getActiveWarehouse() == null)
		{
			_log.log(Level.WARN, "error while sending withdraw request to: " + _activeChar.getName());
		}

	    _items = _activeChar.getActiveWarehouse().getItems();

        for (L2ItemInstance item : _items)
        {
            if (item.isStackable())
            {
                _itemsStackable.add(item.getItem().getItemId());
            }
        }
	}

	@Override
	protected void writeImpl()
	{
		writeH(_whType);
		writeQ(_playerAdena);
		writeH(_items.length);
        writeH(_itemsStackable.size());
        for (int itemId : _itemsStackable)
        {
            writeD(itemId);
        }
        writeD(_invSize);
		for(L2ItemInstance item : _items)
		{
            writeItemInfo(item);
            writeD(item.getObjectId());
            writeD(0);//TODO ??? verife me
            writeD(0);
		}
	}
}
