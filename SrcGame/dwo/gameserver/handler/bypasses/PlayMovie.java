package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.L2Npc;

/**
 * Movie player command handler.
 *
 * @author ANZO
 * @author Yorie
 */
public class PlayMovie extends CommandHandler<String>
{
	@TextCommand
	public boolean playMovie(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		int movieId = Integer.parseInt(params.getArgs().get(0));
		params.getPlayer().showQuestMovie(movieId);
		return true;
	}
}