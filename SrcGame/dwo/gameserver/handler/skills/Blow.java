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
package dwo.gameserver.handler.skills;

import dwo.config.Config;
import dwo.gameserver.engine.logengine.formatters.DamageLogFormatter;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.BlowDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Blow implements ISkillHandler
{
	private static final Logger _logDamage = LogManager.getLogger("Pdamage");

	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.BLOW
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		for(L2Character target : (L2Character[]) targets)
		{
			if(target.isAlikeDead())
			{
				continue;
			}

			// Check firstly if target dodges skill
			boolean skillIsEvaded = PhysicalDamage.calcPhysicalSkillEvasion(target, skill);

			if(!skillIsEvaded && BlowDamage.calcBlowSuccess(activeChar, target, skill))
			{
				byte reflect = Reflect.calcSkillReflect(target, skill);

				if(skill.hasEffects())
				{
					if(reflect == Variables.SKILL_REFLECT_SUCCEED)
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
					else
					{
						byte shld = Shield.calcShldUse(activeChar, target, skill);
						target.stopSkillEffects(skill.getId());
						if(Skills.calcSkillSuccess(activeChar, target, skill, shld, false, false, true))
						{
							skill.getEffects(activeChar, target, new Env(shld, false, false, false));
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
				}

				boolean soul = activeChar.isSoulshotCharged(skill);
				byte shld = Shield.calcShldUse(activeChar, target, skill);

				double damage = skill.isStaticDamage() ? skill.getPower() : (int) BlowDamage.calcBlowDamage(activeChar, target, skill, shld, soul);
				if(!skill.isStaticDamage() && skill.getMaxSoulConsumeCount() > 0 && activeChar.isPlayer())
				{
					// Souls Formula (each soul increase +4%)
					damage *= activeChar.getActingPlayer().getSouls() * 0.04 + 1;
				}

				// Crit rate base crit rate for skill, modified with STR bonus
				if(!skill.isStaticDamage() && PhysicalDamage.calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar), true, target))
				{
					damage *= 2;
				}

				// Ограничение урона
				int targetMaxSkillDamage = (int) target.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null);
				if(targetMaxSkillDamage > 0 && damage > targetMaxSkillDamage)
				{
					damage = targetMaxSkillDamage;
				}

				if(Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					_logDamage.log(Level.INFO, DamageLogFormatter.format("DAMAGE BLOW:", new Object[]{
						activeChar, " did damage ", damage, skill, " to ", target
					}));
				}

				target.reduceCurrentHp(damage, activeChar, skill);

				// vengeance reflected damage
				if((reflect & Variables.SKILL_REFLECT_VENGEANCE) != 0)
				{
					if(target.isPlayer())
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK).addCharName(activeChar));
					}
					if(activeChar.isPlayer())
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK).addCharName(target));
					}
					activeChar.reduceCurrentHp(Reflect.getReflectDamageFromSkillVengeance(activeChar, target, damage, skill), target, skill);
				}

				// Manage attack or cast break of the target (calculating rate, sending message...)
				CancelAttack.calcAtkBreak(target, damage);

				if(activeChar.isPlayer())
				{
					L2PcInstance player = activeChar.getActingPlayer();
					player.sendDamageMessage(target, (int) damage, false, true, false);
				}
			}

			// Sending system messages
			if(skillIsEvaded)
			{
				if(activeChar.isPlayer())
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_DODGES_ATTACK).addString(target.getName()));
				}
				if(target.isPlayer())
				{
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_C1_ATTACK).addString(activeChar.getName()));
				}
			}

			//Possibility of a lethal strike
			BlowDamage.calcLethalHit(activeChar, target, skill);

			//Self Effect
			if(skill.hasSelfEffects())
			{
				L2Effect effect = activeChar.getFirstEffect(skill.getId());
				if(effect != null && effect.isSelfEffect())
				{
					effect.exit();
				}
				skill.getEffectsSelf(activeChar);
			}
			activeChar.ssUncharge(skill);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}