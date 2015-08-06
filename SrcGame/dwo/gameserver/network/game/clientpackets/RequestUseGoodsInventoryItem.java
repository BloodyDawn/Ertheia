package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2PremiumItem;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGoodsInventoryInfo;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGoodsInventoryResult;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 18.10.11
 * Time: 8:30
 */

public class RequestUseGoodsInventoryItem extends L2GameClientPacket
{
	private long _itemNum;
	private int _unk1;
	private long _itemcount;

	@Override
	protected void readImpl()
	{
		_unk1 = readC();
		_itemNum = readQ();
		if(_unk1 != 1)
		{
			_itemcount = readQ();  // unk ????
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(new ExGoodsInventoryResult(ExGoodsInventoryResult.SYS_NOT_TRADE));
			return;
		}

		if(activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(new ExGoodsInventoryResult(ExGoodsInventoryResult.SYS_NO_ITEMS));
			return;
		}

		if(activeChar.getInventory().getSize(false) >= activeChar.getInventoryLimit() * 0.8)
		{
			activeChar.sendPacket(new ExGoodsInventoryResult(ExGoodsInventoryResult.SYS_SHORTAGE));
			return;
		}

		L2PremiumItem _item = activeChar.getPremiumItemList().get((int) _itemNum);

		if(_item == null)
		{
			return;
		}
		if(_itemcount != 0 && _item.getCount() < _itemcount)
		{
			return;
		}

		activeChar.addItem(ProcessType.PREMIUM_ITEM, _item.getItemId(), _itemcount, activeChar.getTarget(), true);

		long itemsLeft = _item.getCount() - _itemcount;

		if(_itemcount < _item.getCount())
		{
			_item.updateCount(itemsLeft);
			activeChar.updatePremiumItem((int) _itemNum, _item.getCount() - _itemcount);
		}
		else
		{
			activeChar.deletePremiumItem((int) _itemNum);
		}

		activeChar.sendPacket(new ExGoodsInventoryInfo(activeChar.getPremiumItemList()));
	}

	@Override
	public String getType()
	{
		return "[C] D0:B2 RequestUseGoodsInventoryItem";
	}
}