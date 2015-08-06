package dwo.gameserver.model.skills.base.conditions;

import dwo.gameserver.model.skills.stats.Env;
import dwo.scripts.instances.ChaosFestival;

/**
 * Check player Chaos Festival participation.
 * @author Yorie
 */
public class ConditionPlayerChaosFestival extends Condition
{
	private boolean checkParticipation;

	public ConditionPlayerChaosFestival(boolean checkParticipation)
	{
		this.checkParticipation = checkParticipation;
	}

	@Override
	public boolean testImpl(Env env)
	{
		boolean isParticipatinNow = ChaosFestival.getInstance().isFightingNow(env.getCharacter().getActingPlayer());
		return checkParticipation && isParticipatinNow || !checkParticipation && !isParticipatinNow;
	}
}
