package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;

public class L2FireChestInstance extends L2MonsterInstance
{
	private boolean _triggered;

	public L2FireChestInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsNoRndWalk(true);
		setIsInvul(false);
		setIsMortal(false);
		setIsNoAttackingBack(true);
	}

	public void setIsTriggered(boolean value)
	{
		_triggered = value;
	}

	public boolean isTriggered()
	{
		return _triggered;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return true;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return true;
	}

	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
}