package dwo.gameserver.model.actor.knownlist;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Trap;

public class TrapKnownList extends CharKnownList
{
	public TrapKnownList(L2Trap activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Trap getActiveChar()
	{
		return (L2Trap) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if(object.equals(getActiveChar().getOwner()) || object.equals(getActiveChar().getTarget()))
		{
			return 6000;
		}

		return 3000;
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 1500;
	}
}
