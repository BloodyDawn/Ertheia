package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2PremiumItem;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGetPremiumItemList;
import dwo.gameserver.util.Util;

/**
 ** @author Gnacik
 */

public class RequestWithDrawPremiumItem extends L2GameClientPacket
{
	private int _itemNum;
	private int _charId;
	private long _itemcount;

	@Override
	protected void readImpl()
	{
		_itemNum = readD();
		_charId = readD();
		_itemcount = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}
		if(_itemcount <= 0)
		{
			return;
		}

		if(activeChar.getObjectId() != _charId)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Incorrect owner, Player: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		if(activeChar.getPremiumItemList().isEmpty())
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestWithDrawPremiumItem] Player: " + activeChar.getName() + " try to get item with empty list!", Config.DEFAULT_PUNISH);
			return;
		}
		if(activeChar.getWeightPenalty() >= 3 || !activeChar.isInventoryUnder90(false))
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM);
			return;
		}
		if(activeChar.isProcessingTransaction())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE);
			return;
		}

		L2PremiumItem _item = activeChar.getPremiumItemList().get(_itemNum);

		if(_item == null)
		{
			return;
		}
		if(_item.getCount() < _itemcount)
		{
			return;
		}

		activeChar.addItem(ProcessType.PREMIUM_ITEM, _item.getItemId(), _itemcount, activeChar.getTarget(), true);

		long itemsLeft = _item.getCount() - _itemcount;

		if(_itemcount < _item.getCount())
		{
			_item.updateCount(itemsLeft);
			activeChar.updatePremiumItem(_itemNum, _item.getCount() - _itemcount);
		}
		else
		{
			activeChar.deletePremiumItem(_itemNum);
		}

		if(activeChar.getPremiumItemList().isEmpty())
		{
			activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
		}
		else
		{
			activeChar.sendPacket(new ExGetPremiumItemList(activeChar.getPremiumItemList()));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:52 RequestWithDrawPremiumItem";
	}
}
