package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Yorie
 */
public class LocationController extends dwo.gameserver.model.actor.controller.character.LocationController
{
	private final L2PcInstance player;

	public LocationController(L2PcInstance object)
	{
		super(object);
		player = object;
	}

	@Override
	protected void onBadCoords()
	{
		player.teleToLocation(0, 0, 0, false);
		player.sendMessage("Error with your coords, please ask a GM for help!");
	}
}
