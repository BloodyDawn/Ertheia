/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 *
 * @author Gnat
 */
public class Negate extends L2Effect
{
	public Negate(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.NEGATE;
	}

	@Override
	public boolean onStart()
	{
		L2Skill skill = getSkill();

		for(int negateSkillId : skill.getNegateId())
		{
			if(negateSkillId != 0)
			{
				getEffected().stopSkillEffects(negateSkillId);
			}
		}

		for(L2SkillType negateSkillType : skill.getNegateStats())
		{
			getEffected().stopSkillEffects(negateSkillType, skill.getNegateLvl());
		}

		if(skill.getNegateAbnormals() != null)
		{
			for(L2Effect effect : getEffected().getAllEffects())
			{
				if(effect == null)
				{
					continue;
				}

				skill.getNegateAbnormals().keySet().stream().filter(negateAbnormalType -> negateAbnormalType.equalsIgnoreCase(effect.getAbnormalType()) && skill.getNegateAbnormals().get(negateAbnormalType) >= effect.getAbnormalLvl()).forEach(negateAbnormalType -> effect.exit());
			}
		}

		if(skill.getNegateEffects() != null)
		{
			for(L2EffectType effectType : skill.getNegateEffects())
			{
				for(L2Effect effect : getEffected().getEffects(effectType))
				{
					if(effect != null)
					{
						effect.exit();
					}
				}
			}
		}

		return true;
	}

}
