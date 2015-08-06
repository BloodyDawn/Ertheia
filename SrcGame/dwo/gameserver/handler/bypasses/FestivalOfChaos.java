package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.scripts.instances.ChaosFestival;

/**
 * Festival of Chaos commands handler.
 *
 * @author Bacek
 * @author Yorie
 */
public class FestivalOfChaos extends CommandHandler<String>
{
	@TextCommand
	public boolean pledgeGame(BypassHandlerParams params)
	{
		if(params.getQueryArgs().isEmpty())
		{
			// TODO: Просмотр чата
		}
		else
		{
			if(params.getQueryArgs().containsKey("command") && params.getQueryArgs().get("command").equalsIgnoreCase("apply") && ChaosFestival.getInstance().canParticipate(params.getPlayer()))
			{
				ChaosFestival.getInstance().addMember(params.getPlayer());
			}
		}
		return true;
	}
}