package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.knownlist.FriendlyMobKnownList;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * This class represents Friendly Mobs lying over the world.
 * These friendly mobs should only attack players with karma > 0
 * and it is always aggro, since it just attacks players with karma
 */

public class L2FriendlyMobInstance extends L2Attackable
{
	public L2FriendlyMobInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public FriendlyMobKnownList getKnownList()
	{
		return (FriendlyMobKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new FriendlyMobKnownList(this));
	}

	@Override
	public boolean isAggressive()
	{
		return true;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return attacker instanceof L2PcInstance && ((L2PcInstance) attacker).hasBadReputation();
	}
}