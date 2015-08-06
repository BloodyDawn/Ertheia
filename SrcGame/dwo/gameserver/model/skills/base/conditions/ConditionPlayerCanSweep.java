package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * Checks Sweeper conditions:
 * <ul>
 * 	<li>Minimum checks, player not null, skill not null.</li>
 * 	<li>Checks if the target isn't null, is dead and spoiled.</li>
 * 	<li>Checks if the sweeper player is the target spoiler, or is in the spoiler party.</li>
 * 	<li>Checks if the corpse is too old.</li>
 * 	<li>Checks inventory limit and weight max load won't be exceed after sweep.</li>
 * </ul>
 * If two or more conditions aren't meet at the same time, one message per condition will be shown.
 * @author Zoey76
 */
public class ConditionPlayerCanSweep extends Condition
{
	private static final int maxSweepTime = 15000;
	private final boolean _val;

	public ConditionPlayerCanSweep(boolean val)
	{
		_val = val;
	}

	@Override
	public boolean testImpl(Env env)
	{
		boolean canSweep = env.getCharacter() != null && env.getCharacter() instanceof L2PcInstance;
		if(canSweep)
		{
			L2PcInstance sweeper = env.getCharacter().getActingPlayer();
			L2Skill sweep = env.getSkill();
			canSweep &= sweep != null;
			if(canSweep)
			{
				L2Object[] targets = sweep.getTargetList(sweeper);
				canSweep &= targets != null;
				if(canSweep)
				{
					L2Attackable target;
					for(L2Object objTarget : targets)
					{
						canSweep &= objTarget instanceof L2Attackable;
						if(canSweep)
						{
							target = (L2Attackable) objTarget;
							canSweep &= target.isDead();
							if(canSweep)
							{
								canSweep &= target.isSpoil();
								if(canSweep)
								{
									canSweep &= target.checkSpoilOwner(sweeper, true);
									canSweep &= target.checkCorpseTime(sweeper, maxSweepTime, true);
									canSweep &= sweeper.getInventory().checkInventorySlotsAndWeight(target.getSpoilLootItems(), true, true);
								}
								else
								{
									sweeper.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED));
								}
							}
						}
					}
				}
			}
		}
		return _val == canSweep;
	}
}
