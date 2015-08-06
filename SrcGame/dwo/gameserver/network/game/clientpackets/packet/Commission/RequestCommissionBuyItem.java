package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.instancemanager.CommissionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 18.07.2011
 * Time: 0:11:11
 */

public class RequestCommissionBuyItem extends L2GameClientPacket
{
	private long _commissionDBId;
	private int _itemType;

	@Override
	protected void readImpl()
	{
		_commissionDBId = readQ();   // номер лота
		_itemType = readD();   // номер кнопки (строки в 1 вкладке)
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();

		if(!validate(cha))
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.BUY_ITEM))
		{
			cha.sendMessage("Вы покупаете слишком часто.");
			return;
		}

		L2Npc manager = cha.getLastFolkNPC();
		if(manager == null || !manager.canInteract(cha))
		{
			cha.sendPacket(new ExCloseCommission());
			return;
		}

		CommissionManager.getInstance().buyCommissionItem(cha, _commissionDBId);
	}

	@Override
	public String getType()
	{
		return "[C] DO:A1 RequestCommissionBuyItem";
	}

	protected boolean validate(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
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
		if(!activeChar.isInventoryUnder90(false) || activeChar.isInventoryDisabled() || activeChar.getCurrentLoad() / activeChar.getMaxLoad() > 0.8)
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_FOR_ITEM_BUYING_YOU_NEED_20_PERCENTS_WEIGHT_LIMIT_AND_10_PERCENTS_COUNT_LIMIT);
			return false;
		}

		return true;
	}
}