package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowStatPage;

/**
 * World statistic handler.
 *
 * @author ANZO
 * @author Yorie
 */
public class WorldStatistic extends CommandHandler<String>
{
	@TextCommand
	public boolean worldStatistic(BypassHandlerParams params)
	{
		params.getTarget().sendPacket(new ExShowStatPage(-2));
		return true;
	}
}
