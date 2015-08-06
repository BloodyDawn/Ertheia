package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager.CropProcure;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private long _money;
	private Map<L2ItemInstance, Long> _sellList = new FastMap<>();
	private List<CropProcure> _procureList = new FastList<>();
	private int _castle;

	public SellListProcure(L2PcInstance player, int castleId)
	{
		_money = player.getAdenaCount();
		_activeChar = player;
		_castle = castleId;
		_procureList = CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
		for(CropProcure c : _procureList)
		{
			L2ItemInstance item = _activeChar.getInventory().getItemByItemId(c.getId());
			if(item != null && c.getAmount() > 0)
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_money);         // money
		writeD(0x00);           // lease ?
		writeH(_sellList.size());         // list size

		for(Map.Entry<L2ItemInstance, Long> l2ItemInstanceLongEntry : _sellList.entrySet())
		{
			writeH(l2ItemInstanceLongEntry.getKey().getItem().getType1());
			writeD(l2ItemInstanceLongEntry.getKey().getObjectId());
			writeD(l2ItemInstanceLongEntry.getKey().getItemId());
			writeQ(l2ItemInstanceLongEntry.getValue());  // count
			writeH(l2ItemInstanceLongEntry.getKey().getItem().getType2());
			writeH(0);  // unknown
			writeQ(0);  // price, u shouldnt get any adena for crops, only raw materials
		}
	}
}
