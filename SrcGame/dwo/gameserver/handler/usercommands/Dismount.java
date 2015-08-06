package dwo.gameserver.handler.usercommands;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;

/**
 * Support for /dismount command.
 *
 * @author Micht
 * @author Yorie
 */
public class Dismount extends CommandHandler<Integer>
{
	@NumericCommand(62)
	public boolean dismount(HandlerParams<Integer> params)
	{
		if(params.getPlayer().isMounted())
		{
			params.getPlayer().dismount();
		}

		return true;
	}
}
