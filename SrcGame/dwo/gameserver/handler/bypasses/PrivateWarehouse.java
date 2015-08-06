package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.WareHouseDepositList;
import dwo.gameserver.network.game.serverpackets.WareHouseWithdrawList;

/**
 * Private player warehouse commands handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class PrivateWarehouse extends CommandHandler<String>
{
	private static void showWithdrawWindow(L2PcInstance player)
	{
		player.sendActionFailed();
		player.setActiveWarehouse(player.getWarehouse());

		if(player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}

		player.sendPacket(new WareHouseWithdrawList(player, WareHouseWithdrawList.PRIVATE));
	}

	@TextCommand
	public boolean withdraw(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		L2Character target = params.getTarget();

		if(!(target instanceof L2Npc))
		{
			return false;
		}

		if(activeChar.isEnchanting())
		{
			return false;
		}

		showWithdrawWindow(activeChar);
		return true;
	}

	@TextCommand
	public boolean deposit(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		L2Character target = params.getTarget();

		if(!(target instanceof L2Npc))
		{
			return false;
		}

		if(activeChar.isEnchanting())
		{
			return false;
		}

		activeChar.sendActionFailed();
		activeChar.setActiveWarehouse(activeChar.getWarehouse());
		activeChar.tempInventoryDisable();
		activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.PRIVATE));
		return true;
	}
}