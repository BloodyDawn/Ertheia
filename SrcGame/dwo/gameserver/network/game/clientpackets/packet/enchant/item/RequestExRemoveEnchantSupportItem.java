package dwo.gameserver.network.game.clientpackets.packet.enchant.item;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.enchant.item.ExRemoveEnchantSupportItemResult;

/**
 * User: Bacek
 * Date: 07.02.13
 * Time: 4:35
 */
public class RequestExRemoveEnchantSupportItem extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// nothing (trigger)
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		activeChar.setActiveEnchantTimestamp(System.currentTimeMillis());
		L2ItemInstance support = activeChar.getActiveEnchantSupportItem();
		if(support == null || support.getCount() < 1)
		{
			activeChar.sendPacket(new ExRemoveEnchantSupportItemResult());
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:F1 RequestExRemoveEnchantSupportItem";
	}
}
