package dwo.gameserver.handler.voiced;

import dwo.config.Config;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.scripts.npc.town.MammonsMoving;

/**
 * Mammon info command handler.
 *
 * @author ANZO
 * @author Yorie
 */

public class Mammon extends CommandHandler<String>
{
	@TextCommand
	public boolean mammon(HandlerParams<String> params)
	{
		params.getPlayer().sendMessage("Маммоны сейчас в городе: " + MapRegionManager.getInstance().getClosestTownName(MammonsMoving.getMammonCoords()));
		return true;
	}

	@Override
	public boolean isActive()
	{
		return Config.MAMMONS_VOICE_LOC_ENABLE;
	}
}