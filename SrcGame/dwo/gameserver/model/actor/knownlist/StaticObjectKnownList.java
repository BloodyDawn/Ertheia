package dwo.gameserver.model.actor.knownlist;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2DefenderInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;

public class StaticObjectKnownList extends CharKnownList
{
	public StaticObjectKnownList(L2StaticObjectInstance activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2StaticObjectInstance getActiveChar()
	{
		return (L2StaticObjectInstance) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if(object instanceof L2DefenderInstance)
		{
			return 800;
		}
		if(!object.isPlayer())
		{
			return 0;
		}

		return 4000;
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if(object instanceof L2DefenderInstance)
		{
			return 600;
		}
		if(!object.isPlayer())
		{
			return 0;
		}
		return 2000;
	}
}
