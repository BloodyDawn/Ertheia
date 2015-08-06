package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.variation.ExPutItemResultForVariationCancel;
import dwo.gameserver.util.Util;

public class RequestConfirmCancelItem extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if(item == null)
		{
			return;
		}

		if(item.getOwnerId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(getClient().getActiveChar(), "Warning!! Character " + getClient().getActiveChar().getName() + " of account " + getClient().getActiveChar().getAccountName() + " tryied to destroy augment on item that doesn't own.", Config.DEFAULT_PUNISH);
			return;
		}

		if(!item.isAugmented())
		{
			activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
			return;
		}

		if(item.isPvp() && !Config.AUGMENTATION_WEAPONS_PVP)
		{
			activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
			return;
		}

		// Lindvior: При отмене аугментации плата не взымается
		activeChar.sendPacket(new ExPutItemResultForVariationCancel(item, 0));
	}

	@Override
	public String getType()
	{
		return "[C] D0:2D RequestConfirmCancelItem";
	}
}