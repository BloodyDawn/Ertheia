package dwo.gameserver.network.game.clientpackets.packet.enchant.item;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.enchant.item.EnchantResult;

/**
 *
 * @author KenM
 */
public class RequestExCancelEnchantItem extends L2GameClientPacket
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
		if(activeChar != null)
		{
			activeChar.sendPacket(new EnchantResult(EnchantResult.ERROR, 0, 0));
			activeChar.setActiveEnchantItem(null);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:4E RequestExCancelEnchantItem";
	}
}
