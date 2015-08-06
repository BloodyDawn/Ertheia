package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.instancemanager.CommissionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;

/**
 * L2GOD Team
 * User: Keiichi, Bacek
 * Date: 17.07.2011
 * Time: 23:58:25
 */

public class RequestCommissionRegister extends L2GameClientPacket
{
	private int _objectId;
	private String _itemName;
	private long _pricePerUnit;
	private long _amount;
	private int _period;
	private int _premiumItemID;
	private int _unk;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_itemName = readS();  // название итема
		_pricePerUnit = readQ();
		_amount = readQ();
		_period = readD();
		//TODO: Возможно две последние D нужны только на руоффе. Пока что оставляем!
		//_premiumItemID = readD();
		//_unk = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();

		if(!validate(cha))
		{
			return;
		}

		L2Npc manager = cha.getLastFolkNPC();
		if(manager == null || !manager.canInteract(cha))
		{
			cha.sendPacket(new ExCloseCommission());
			return;
		}

		L2ItemInstance item = cha.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			return;
		}

		if(cha.getActiveTradeList() != null)
		{
			cha.cancelActiveTrade();
		}

		CommissionManager.getInstance().addLot(cha, _objectId, _itemName, _pricePerUnit, _amount, _period);
		cha.sendPacket(SystemMessageId.THE_ITEM_HAS_BEEN_SUCCESSFULLY_REGISTERED);
	}

	@Override
	public String getType()
	{
		return "[C] DO:9D RequestCommissionRegister";
	}

	protected boolean validate(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return false;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.COMMISSIN_ADD))
		{
			activeChar.sendMessage("Вы выставляете на продажу слишком часто.");
			return false;
		}

		if(_amount > 99999)
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_COUNT_LIMIT);
			return false;
		}

		if(_pricePerUnit > 99999999999L)
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_PRICE_LIMIT);
			return false;
		}

		if(activeChar.isInStoreMode() || activeChar.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_CANNOT_REGISTER_BUY_OR_CANCEL_DURING_PRIVATE_STORE_OR_CRAFTING_MODE);
			return false;
		}
		if(activeChar.isInCraftMode())
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_CANNOT_REGISTER_BUY_OR_CANCEL_DURING_TRADE);
			return false;
		}
		if(activeChar.isEnchanting() || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_CANNOT_REGISTER_BUY_OR_CANCEL_DURING_ENCHANTING_ENHANCING_OR_CRYSTALLIZING);
			return false;
		}

		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);

		if(item == null)
		{
			return false;
		}

		if(!item.isSellable() || !item.isDropable() || !item.isTradeable() || item.getItemId() == 57)
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_CANNOT_REGISTER_ITEMS_THAT_ARE_CANNOT_BE_TRADED_DROPPED_OR_SELLED_IN_PRIVATE_STORE);
			return false;
		}

		if(item.isEquipped())
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_CANNOT_REGISTER_WEARING_ITEM);
			return false;
		}

		return true;
	}
}