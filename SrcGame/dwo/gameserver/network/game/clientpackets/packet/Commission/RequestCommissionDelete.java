package dwo.gameserver.network.game.clientpackets.packet.Commission;

import dwo.gameserver.instancemanager.CommissionManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExCloseCommission;

/**
 * L2GOD Team
 * User: Keiichi, Bacek
 * Date: 22.07.2011
 * Time: 18:03:10
 */

public class RequestCommissionDelete extends L2GameClientPacket
{
	private long _CommissionDBId;
	private int _ItemType; // TODO
	private int _PeriodType; // TODO

	@Override
	protected void readImpl()
	{
		_CommissionDBId = readQ();
		_ItemType = readD();
		_PeriodType = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance cha = getClient().getActiveChar();

		if(!validate(cha))
		{
			return;
		}

		CommissionManager.getInstance().cancelLot(cha, _CommissionDBId);
	}

	@Override
	public String getType()
	{
		return "[C] DO:9F RequestCommissionDelete";
	}

	protected boolean validate(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			return false;
		}

		L2Npc manager = activeChar.getLastFolkNPC();
		if(manager == null || !manager.canInteract(activeChar))
		{
			activeChar.sendPacket(new ExCloseCommission());
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

		return true;
	}
}
