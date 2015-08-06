package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Auto-loot mode voice commands.
 * Allows enable/disable auto-loot drop & herbs.
 *
 * @author GODWORLD
 * @author Yorie
 */
public class AutoLoot extends CommandHandler<String>
{
	@TextCommand
	public boolean autoLoot(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(activeChar.getUseAutoLoot())
		{

			activeChar.getVariablesController().set("useAutoLoot@", false);
			activeChar.sendMessage("Автоматический сбор вещей выключен.");
		}
		else
		{
			activeChar.getVariablesController().set("useAutoLoot@", true);
			activeChar.sendMessage("Автоматический сбор вещей включен.");
		}
		return true;
	}

	@TextCommand
	public boolean autoLootHerbs(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		if(activeChar.getUseAutoLootHerbs())
		{
			activeChar.getVariablesController().set("useAutoLootHerbs@", false);
			activeChar.sendMessage("Автоматический сбор настоек выключен.");
		}
		else
		{
			activeChar.getVariablesController().set("useAutoLootHerbs@", true);
			activeChar.sendMessage("Автоматический сбор настоек включен.");
		}
		return true;
	}

	@Override
	public boolean isActive()
	{
		return Config.ALLOW_AUTOLOOT_COMMAND;
	}
}
