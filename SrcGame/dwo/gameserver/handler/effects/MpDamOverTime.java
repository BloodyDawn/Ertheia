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

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillCont;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class MpDamOverTime extends L2Effect
{
	private L2Skill _castSkill;

	public MpDamOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANA_DMG_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead() && !getSkill().isStayAfterDeath())
		{
			return false;
		}

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				return false;
			}
		}

		if(!checkActiveToggle())
		{
			return false;
		}

		getEffected().reduceCurrentMp(manaDam);
		return getSkill().isToggle();
	}

	public boolean checkActiveToggle()
	{
		L2Skill skill = getSkill();
		if(skill.isActiveToggle() && skill.getSkillType() == L2SkillType.CONT)
		{
			boolean cancel = true;
			for(L2Object character : skill.getTargetList(getEffector()))
			{
				if(!(character instanceof L2Character))
				{
					continue;
				}

				L2Character target = (L2Character) character;

				/*
				 * Проверяем, возможно нужно наложить эффект от тоггла на новую цель
				 * К примеру, тоггл уже включен, и в пати взяли нового персонажа
				 * Накладываем бафф только от эффектора.
				 */
				if(getEffected().getObjectId() == getEffector().getObjectId())
				{
					if(target.getFirstEffect(skill) == null && skill.checkCondition(getEffector(), target, false))
					{
						skill.getEffects(getEffector(), target);
					}
				}

				// Проверяем наличие в списке таргета текущего эффектед объекта. Если нет - то выключает ему бафф.
				if(cancel && target.getObjectId() == getEffected().getObjectId())
				{
					cancel = false;
				}
			}

			// Возможно на эффекторе уже нет этого баффа, тогда нужно снять его с эффектед цели.
			if(getEffector().getFirstEffect(skill) == null)
			{
				cancel = true;
			}

			if(cancel)
			{
				exit();
				return false;
			}

			if(_castSkill == null && ((L2SkillCont) skill).getCastSkillId() > 0 && ((L2SkillCont) skill).getCastSkillLevel() > 0)
			{
				_castSkill = SkillTable.getInstance().getInfo(((L2SkillCont) skill).getCastSkillId(), ((L2SkillCont) skill).getCastSkillLevel());
			}

			if(_castSkill != null)
			{
				for(L2Object player : _castSkill.getTargetList(getEffector()))
				{
					if(!(player instanceof L2Character))
					{
						continue;
					}

					if(_castSkill.isDebuff() && !L2Skill.checkForAreaOffensiveSkills(getEffector(), (L2Character) player, getSkill(), getEffector().isInsideZone(L2Character.ZONE_PVP) && !getEffector().isInsideZone(L2Character.ZONE_SIEGE)))
					{
						continue;
					}

					if(_castSkill.checkCondition(getEffector(), player, false))
					{
						_castSkill.getEffects(getEffector(), (L2Character) player);
					}
				}
			}
		}
		return true;
	}
}
