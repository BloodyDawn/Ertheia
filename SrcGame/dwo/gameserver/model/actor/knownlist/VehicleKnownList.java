package dwo.gameserver.model.actor.knownlist;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;

public class VehicleKnownList extends CharKnownList
{
	public VehicleKnownList(L2Character activeChar)
	{
		super(activeChar);
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if(!object.isPlayer())
		{
			return 0;
		}

		return object.getKnownList().getDistanceToForgetObject(getActiveObject());
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if(!object.isPlayer())
		{
			return 0;
		}

		return object.getKnownList().getDistanceToWatchObject(getActiveObject());
	}
}