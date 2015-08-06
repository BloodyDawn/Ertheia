package dwo.gameserver.model.actor.instance;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 27.11.11
 * Time: 19:13
 */

public class L2TrainingDollInstance extends L2Attackable
{
	public L2TrainingDollInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setIsNoRndWalk(true);
		setIsInvul(false);
		setIsMortal(true);
		stopHpMpRegeneration();
		// TODO: Или ввести для кукл, которые бьют гварды инстанс Моба и заставить их бить или оставить хардкод.
		if(getNpcId() == 33023)
		{
			setTargetable(false);
		}
		setIsNoAttackingBack(true);
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
