package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class AwakenForce extends Buff
{
	public static final int[] POWER_TOGGLES = {1939};
	private static final int AEORE_SKILL_ID = 1940;
	private static final int SIGEL_SKILL_ID = 1928;
	private static final int SOLIDARITY_SKILL_ID = 1955;

	public AwakenForce(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.AWAKEN_FORCE;
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return false;
	}

	/**
	 * Checks if Party Solidarity should be given to effected player
	 * @param effectStarted
	 */
	public void checkSolidarity(boolean effectStarted)
	{
		int skillId = getSkill().getId();
		boolean hasSigelForce = skillId == SIGEL_SKILL_ID && effectStarted;
		boolean hasAeoreForce = skillId == AEORE_SKILL_ID && effectStarted;
		// Count of awaken classes powers without sigel and aeore powers.
		short powerCount = 0;

		// Look for awaken powers
		for(L2Effect effect : getEffected().getEffects(L2EffectType.AWAKEN_FORCE))
		{
			int effectSkillId = effect.getSkill().getId();
			if(effectSkillId == SIGEL_SKILL_ID)
			{
				hasSigelForce = true;
			}
			else if(effectSkillId == AEORE_SKILL_ID)
			{
				hasAeoreForce = true;
			}
			else
			{
				++powerCount;
			}
		}

		int solidarityLevel = (powerCount - 1) % 3;
		// Give or take Party Solidarity
		if(hasAeoreForce && hasSigelForce && solidarityLevel > 0)
		{
			SkillTable.getInstance().getInfo(SOLIDARITY_SKILL_ID, solidarityLevel).getEffects(getEffector(), getEffected());
		}
		else
		{
			for(L2Effect effect : getEffected().getEffects(L2EffectType.BUFF))
			{
				if(effect.getSkill().getId() == SOLIDARITY_SKILL_ID)
				{
					effect.exit();
					break;
				}
			}
		}
	}

	@Override
	public boolean onStart()
	{
		checkSolidarity(true);
		return true;
	}

	@Override
	public void onExit()
	{
		checkSolidarity(false);
	}
}
