package dwo.gameserver.handler.voiced;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Exping command handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class NoExp extends CommandHandler<String>
{
	@TextCommand
	public boolean noexp(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(activeChar.isAbleToGainExp())
		{
			activeChar.getVariablesController().set("ableToGainExp@", false);
			activeChar.sendMessage("Потребление опыта отключено.");
		}
		else
		{
			activeChar.getVariablesController().set("ableToGainExp@", true);
			activeChar.sendMessage("Потребление опыта включено.");
		}

		return true;
	}
}