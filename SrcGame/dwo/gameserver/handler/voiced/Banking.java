package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;

/**
 * Banking commands handler.
 *
 * @author GODWORLD
 * @author Yorie
 */
public class Banking extends CommandHandler<String>
{
	@TextCommand
	public boolean bank(HandlerParams<String> params)
	{
		params.getPlayer().sendMessage(".deposit (" + Config.BANKING_SYSTEM_ADENA + " Adena = " + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar) / .withdraw (" + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar = " + Config.BANKING_SYSTEM_ADENA + " Adena)");
		return true;
	}

	@TextCommand
	public boolean deposit(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.getInventory().getInventoryItemCount(PcInventory.ADENA_ID, 0) >= Config.BANKING_SYSTEM_ADENA)
		{
			if(!activeChar.reduceAdena(ProcessType.WAREHOUSE, Config.BANKING_SYSTEM_ADENA, activeChar, false))
			{
				return false;
			}
			activeChar.getInventory().addItem(ProcessType.WAREHOUSE, 3470, Config.BANKING_SYSTEM_GOLDBARS, activeChar, null);
			activeChar.getInventory().updateDatabase();
			activeChar.sendMessage("Thank you, you now have " + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar(s), and " + Config.BANKING_SYSTEM_ADENA + " less adena.");
		}
		else
		{
			activeChar.sendMessage("You do not have enough Adena to convert to Goldbar(s), you need " + Config.BANKING_SYSTEM_ADENA + " Adena.");
		}

		return true;
	}

	@TextCommand
	public boolean withdraw(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.getInventory().getInventoryItemCount(3470, 0) >= Config.BANKING_SYSTEM_GOLDBARS)
		{
			if(!activeChar.destroyItemByItemId(ProcessType.CONSUME, 3470, Config.BANKING_SYSTEM_GOLDBARS, activeChar, false))
			{
				return false;
			}
			activeChar.getInventory().addAdena(ProcessType.PRIVATESTORE, Config.BANKING_SYSTEM_ADENA, activeChar, null);
			activeChar.getInventory().updateDatabase();
			activeChar.sendMessage("Thank you, you now have " + Config.BANKING_SYSTEM_ADENA + " Adena, and " + Config.BANKING_SYSTEM_GOLDBARS + " less Goldbar(s).");
		}
		else
		{
			activeChar.sendMessage("You do not have any Goldbars to turn into " + Config.BANKING_SYSTEM_ADENA + " Adena.");
		}

		return true;
	}

	@Override
	public boolean isActive()
	{
		return Config.BANKING_SYSTEM_ENABLED;
	}
}