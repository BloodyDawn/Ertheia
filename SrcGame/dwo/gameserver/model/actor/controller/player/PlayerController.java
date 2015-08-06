package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.model.actor.controller.object.L2ObjectController;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Base of any player controller.
 *
 * @author Yorie
 */
public class PlayerController extends L2ObjectController
{
	/**
	 * The same as L2ObjectController.object, but casted to L2PcInstance because of player controllers guess only L2PcInstance types.
	 */
	protected final L2PcInstance player;

	public PlayerController(L2PcInstance player)
	{
		super(player);
		this.player = player;
	}
}
