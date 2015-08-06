package dwo.gameserver.model.actor.controller.object;

import dwo.gameserver.model.actor.L2Object;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

/**
 * Basis of L2Object controllers.
 *
 * @author Yorie
 */
public class L2ObjectController
{
	protected static final Logger log = LogManager.getLogger(L2ObjectController.class);

	protected final L2Object object;

	public L2ObjectController(@NotNull L2Object object)
	{
		this.object = object;
	}
}
