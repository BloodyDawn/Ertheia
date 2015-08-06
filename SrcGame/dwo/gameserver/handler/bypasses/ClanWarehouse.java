package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.WareHouseDepositList;
import dwo.gameserver.network.game.serverpackets.WareHouseWithdrawList;

/**
 * Clan warehouse dialog commands handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class ClanWarehouse extends CommandHandler<String>
{
	private static final String[] COMMANDS = {
		"withdraw_pledge", "deposit_pledge"
	};

	private boolean canUseClanWarehouse(L2PcInstance activeChar)
	{
		if(activeChar.isEnchanting())
		{
			return false;
		}

		if(activeChar.getClan() == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return false;
		}

		if(activeChar.getClan().getLevel() == 0)
		{
			activeChar.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
			return false;
		}
		return true;
	}

	@TextCommand("withdraw_pledge")
	public boolean withdraw(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(!canUseClanWarehouse(activeChar))
		{
			return true;
		}

		showWithdrawWindow(activeChar);
		return true;
	}

	@TextCommand("deposit_pledge")
	public boolean deposit(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(!canUseClanWarehouse(activeChar))
		{
			return false;
		}

		activeChar.sendActionFailed();
		activeChar.setActiveWarehouse(activeChar.getClan().getWarehouse());
		activeChar.tempInventoryDisable();
		activeChar.sendPacket(new WareHouseDepositList(activeChar, WareHouseDepositList.CLAN));
		return true;
	}

	private void showWithdrawWindow(L2PcInstance player)
	{
		player.sendActionFailed();

		if((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
			return;
		}

		player.setActiveWarehouse(player.getClan().getWarehouse());

		if(player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH);
			return;
		}

		player.sendPacket(new WareHouseWithdrawList(player, WareHouseWithdrawList.CLAN));
	}
}