package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: Yorie, ANZO
 * Date: 19.02.12
 * Time: 1:27
 */

public class DualCast extends Buff
{
	public static final int FIRE_SKILL_ID = 11007;
	// Static for performance, don't do these computations for each effect
	private static final L2Skill SKILL_FIRE = SkillTable.getInstance().getInfo(FIRE_SKILL_ID, 1);
	public static final int WATER_SKILL_ID = 11008;
	private static final L2Skill SKILL_WATER = SkillTable.getInstance().getInfo(WATER_SKILL_ID, 1);
	public static final int WIND_SKILL_ID = 11009;
	private static final L2Skill SKILL_WIND = SkillTable.getInstance().getInfo(WIND_SKILL_ID, 1);
	public static final int EARTH_SKILL_ID = 11010;
	private static final L2Skill SKILL_EARTH = SkillTable.getInstance().getInfo(EARTH_SKILL_ID, 1);

	/**
	 * Element that is was on character before casting double cast
	 */
	private int _prevElementalSkill;

	public DualCast(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public DualCast(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public boolean onStart()
	{
		if(!getEffected().isPlayer())
		{
			return false;
		}

		boolean alreadyDualCasting = ((L2PcInstance) getEffected()).isCanDualCast();
		if(!alreadyDualCasting)
		{
			L2Effect[] effects = getEffected().getEffects(L2EffectType.MANA_DMG_OVER_TIME);
			// Collecting existed Feoh toggle effects
			for(L2Effect effect : effects)
			{
				int skillId = effect.getSkill().getId();
				if(!effect.isAbnormatTypeIgnored() && (skillId == FIRE_SKILL_ID || skillId == WATER_SKILL_ID || skillId == WIND_SKILL_ID || skillId == EARTH_SKILL_ID))
				{
					_prevElementalSkill = skillId;
					break;
				}
			}

			if(_prevElementalSkill != FIRE_SKILL_ID)
			{
				addElementEffect(SKILL_FIRE, "UniqueFire");
			}
			if(_prevElementalSkill != WATER_SKILL_ID)
			{
				addElementEffect(SKILL_WATER, "UniqueWater");
			}
			if(_prevElementalSkill != WIND_SKILL_ID)
			{
				addElementEffect(SKILL_WIND, "UniqueWind");
			}
			if(_prevElementalSkill != EARTH_SKILL_ID)
			{
				addElementEffect(SKILL_EARTH, "UniqueEarth");
			}

			((L2PcInstance) getEffected()).setCanDualCast(true);
		}
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		if(!getEffected().isPlayer())
		{
			return;
		}

		((L2PcInstance) getEffected()).setCanDualCast(false);

		L2Effect[] effects = getEffected().getEffects(L2EffectType.MANA_DMG_OVER_TIME);
		for(L2Effect effect : effects)
		{
			int skillId = effect.getSkill().getId();
			if(skillId != _prevElementalSkill && (skillId == FIRE_SKILL_ID || skillId == WATER_SKILL_ID || skillId == WIND_SKILL_ID || skillId == EARTH_SKILL_ID))
			{
				effect.stopEffectTask();
			}
		}
		super.onExit();
	}

	private void addElementEffect(L2Skill skill, String stackAddition)
	{
		L2Effect effect = skill.getEffects(getEffector(), getEffected())[0];
		effect.setIgnoreAbnormalType(true);
		effect.setAbnormalType(effect.getAbnormalType() + stackAddition);
	}

}
