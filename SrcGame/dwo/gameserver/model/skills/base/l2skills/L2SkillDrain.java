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
package dwo.gameserver.model.skills.base.l2skills;

import dwo.config.Config;
import dwo.gameserver.engine.logengine.formatters.DamageLogFormatter;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class L2SkillDrain extends L2Skill
{
	private static final Logger _logDamage = LogManager.getLogger("Mdamage");

	private final float _absorbPart;
	private final int _absorbAbs;

	public L2SkillDrain(StatsSet set)
	{
		super(set);

		_absorbPart = set.getFloat("absorbPart", 0.0f);
		_absorbAbs = set.getInteger("absorbAbs", 0);
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		for(L2Character target : (L2Character[]) targets)
		{
			if(target.isAlikeDead() && getTargetType() != L2TargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}

			if(!activeChar.equals(target) && target.isInvul())
			{
				continue; // No effect on invulnerable templates unless they cast it themselves.
			}

			boolean mcrit = MagicalDamage.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Shield.calcShldUse(activeChar, target, this);
			int damage = isStaticDamage() ? (int) getPower() : (int) MagicalDamage.calcMagicDam(activeChar, target, this, shld, activeChar.isSpiritshotCharged(this), activeChar.isBlessedSpiritshotCharged(this), mcrit);

			int _drain = 0;
			int _cp = (int) target.getCurrentCp();
			int _hp = (int) target.getCurrentHp();

			if(_cp > 0)
			{
				_drain = damage < _cp ? 0 : damage - _cp;
			}
			else
			{
				_drain = damage > _hp ? _hp : damage;
			}

			double hpAdd = _absorbAbs + _absorbPart * _drain;
			double hp = activeChar.getCurrentHp() + hpAdd > activeChar.getMaxHp() ? activeChar.getMaxHp() : activeChar.getCurrentHp() + hpAdd;

			activeChar.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(activeChar);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);

			// Check to see if we should damage the target
			if(damage > 0 && (!target.isDead() || getTargetType() != L2TargetType.TARGET_CORPSE_MOB))
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				CancelAttack.calcAtkBreak(target, damage);

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);

				if(Config.LOG_GAME_DAMAGE && activeChar.isPlayable() && damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					_logDamage.log(Level.INFO, DamageLogFormatter.format("DAMAGE SKILLDRAIN:", new Object[]{
						activeChar, " did damage ", damage, this, " to ", target
					}));
				}

				if(hasEffects() && getTargetType() != L2TargetType.TARGET_CORPSE_MOB)
				{
					// ignoring vengance-like reflections
					if((Reflect.calcSkillReflect(target, this) & Variables.SKILL_REFLECT_SUCCEED) > 0)
					{
						activeChar.stopSkillEffects(getId());
						getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(getId()));
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(getId());
						if(Skills.calcSkillSuccess(activeChar, target, this, shld, false, activeChar.isSpiritshotCharged(this), activeChar.isBlessedSpiritshotCharged(this)))
						{
							getEffects(activeChar, target);
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(this));
						}
					}
				}

				target.reduceCurrentHp(damage, activeChar, this);
			}

			// Check to see if we should do the decay right after the cast
			if(target.isDead() && getTargetType() == L2TargetType.TARGET_CORPSE_MOB && target.isNpc())
			{
				((L2Npc) target).endDecayTask();
			}
		}
		//effect self :]
		L2Effect effect = activeChar.getFirstEffect(getId());
		if(effect != null && effect.isSelfEffect())
		{
			//Replace old effect with new one.
			effect.exit();
		}
		// cast self effect if any
		getEffectsSelf(activeChar);
		activeChar.spsUncharge(this);
	}

	public void useCubicSkill(L2CubicInstance activeCubic, L2Object[] targets)
	{
		for(L2Character target : (L2Character[]) targets)
		{
			if(target.isAlikeDead() && getTargetType() != L2TargetType.TARGET_CORPSE_MOB)
			{
				continue;
			}

			boolean mcrit = MagicalDamage.calcMCrit(activeCubic.getMCriticalHit());
			byte shld = Shield.calcShldUse(activeCubic.getOwner(), target, this);

			int damage = (int) MagicalDamage.calcMagicDam(activeCubic, target, this, mcrit, shld);

			double hpAdd = _absorbAbs + _absorbPart * damage;
			L2PcInstance owner = activeCubic.getOwner();
			double hp = owner.getCurrentHp() + hpAdd > owner.getMaxHp() ? owner.getMaxHp() : owner.getCurrentHp() + hpAdd;

			owner.setCurrentHp(hp);

			StatusUpdate suhp = new StatusUpdate(owner);
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);

			// Check to see if we should damage the target
			if(damage > 0 && (!target.isDead() || getTargetType() != L2TargetType.TARGET_CORPSE_MOB))
			{
				target.reduceCurrentHp(damage, activeCubic.getOwner(), this);

				// Manage attack or cast break of the target (calculating rate, sending message...)
				CancelAttack.calcAtkBreak(target, damage);

				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}
}
