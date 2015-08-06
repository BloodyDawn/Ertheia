package dwo.gameserver.handler.skills;

import dwo.config.Config;
import dwo.gameserver.engine.logengine.formatters.DamageLogFormatter;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.BlowDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Mdam implements ISkillHandler
{
	protected static final Logger _log = LogManager.getLogger(Mdam.class);
	private static final Logger _logDamage = LogManager.getLogger("Mdamage");

	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.MDAM, L2SkillType.DEATHLINK, L2SkillType.MDAM_IMMORTAL
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		L2Character target;
		for(L2Object obj : targets)
		{
			if(obj instanceof L2Character)
			{
				target = (L2Character) obj;
			}
			else
			{
				continue;
			}

			if(activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if(target.isDead())
			{
				continue;
			}

			boolean mcrit = MagicalDamage.calcMCrit(activeChar.getMCriticalHit(target, skill));

			byte shld = Shield.calcShldUse(activeChar, target, skill);
			byte reflect = Reflect.calcSkillReflect(target, skill);

			int damage = skill.isStaticDamage() ? (int) skill.getPower() : (int) MagicalDamage.calcMagicDam(activeChar, target, skill, shld, activeChar.isSpiritshotCharged(skill), activeChar.isBlessedSpiritshotCharged(skill), mcrit);

			if(!skill.isStaticDamage() && skill.getDependOnTargetBuff() != 0)
			{
				damage += (int) (damage * target.getBuffCount() * skill.getDependOnTargetBuff());
			}

			if(!skill.isStaticDamage() && skill.getDependOnTargetEffectId().length != 0)
			{
				int[] increasePowerEffects = skill.getDependOnTargetEffectId();
				int count = 0;
				for(L2Effect eff : target.getAllEffects())
				{
					if(eff == null)
					{
						continue;
					}

					if(ArrayUtils.contains(increasePowerEffects, eff.getSkill().getId()))
					{
						count++;
					}
				}
				if(count != 0)
				{
					damage += damage * count;
				}
			}

			if(!skill.isStaticDamage() && skill.getMaxSoulConsumeCount() > 0 && activeChar instanceof L2PcInstance)
			{
				// Souls Formula (each soul increase +4%)
				damage *= activeChar.getActingPlayer().getSouls() * 0.04 + 1;
			}

			// Possibility of a lethal strike
			BlowDamage.calcLethalHit(activeChar, target, skill);

			if(damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				CancelAttack.calcAtkBreak(target, damage);

				// В годе обычный рефлект так же распростроняется на маг скилы
				int reflectedDamage = Reflect.getReflectDamage(activeChar, target, damage);
				if(reflectedDamage > 0)
				{
					// Шлем сообщение
					target.sendDamageMessage(activeChar, reflectedDamage, false, false, false);
					// Наносим урон
					activeChar.reduceCurrentHp(reflectedDamage, target, true, false, null);
				}

				// Отдельный рефлект от скилов
				reflectedDamage = Reflect.getReflectDamageFromSkill(activeChar, target, damage, skill);
				if(reflectedDamage > 0)
				{
					// Шлем сообщение
					target.sendDamageMessage(activeChar, reflectedDamage, false, false, false);
					// Наносим урон
					activeChar.reduceCurrentHp(reflectedDamage, target, true, false, null);
				}

				// 100% Рефлект от скилов ( контр атака )
				if((reflect & Variables.SKILL_REFLECT_VENGEANCE) != 0)
				{
					//activeChar.reduceCurrentHp(damage, target, skill);
					if(target instanceof L2PcInstance)
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_C1_ATTACK).addCharName(activeChar));
					}
					if(activeChar instanceof L2PcInstance)
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_PERFORMING_COUNTERATTACK).addCharName(target));
					}
					activeChar.reduceCurrentHp(Reflect.getReflectDamageFromSkillVengeance(activeChar, target, damage, skill), target, skill);
				}

				// Если на цели висит баф, ограничивающий урон от умений - режем этот урон
				int targetMaxSkillDamage = (int) target.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null);
				if(targetMaxSkillDamage > 0 && damage > targetMaxSkillDamage)
				{
					damage = targetMaxSkillDamage;
				}

				// Обсорб
				if(Rnd.getChance(30) && !target.isInvul()) // Do not absorb if target invul
				{
					// Поддержка отжора ХП для магических скиллов
					double absorbHpFromSkill = skill.getAbsorbDmgPercent() * damage;

					// Базовый отжор ХП
					double absorbPercent = activeChar.getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

					if(absorbPercent > 0 || absorbHpFromSkill > 0)
					{
						int maxCanAbsorb = (int) (activeChar.getMaxRecoverableHp() - activeChar.getCurrentHp());
						int absorbDamage = (int) (absorbPercent / 100.0 * damage);
						double absorbFull = absorbDamage + absorbHpFromSkill;

						// Нельзя отвампирить больше чем хп у таргета
						absorbFull = Math.min(absorbFull, target.getCurrentHp());
						// Проверяем на перелив хп
						absorbFull = Math.min(absorbFull, maxCanAbsorb);

						// Если абсорб больше 0, то повышаем хп
						if(absorbFull > 0)
						{
							activeChar.setCurrentHp(activeChar.getCurrentHp() + absorbFull);
						}
					}
				}

				if(skill.getSkillType() == L2SkillType.MDAM_IMMORTAL)
				{
					damage = (int) Math.min(target.getCurrentHp() - 1, damage);
				}

				if(skill.hasEffects())
				{
					if((reflect & Variables.SKILL_REFLECT_SUCCEED) == 0)
					{
						if(Skills.calcSkillSuccess(activeChar, target, skill, shld, false, activeChar.isSpiritshotCharged(skill), activeChar.isBlessedSpiritshotCharged(skill)))
						{
							target.stopSkillEffects(skill.getId());
							skill.getEffects(activeChar, target, new Env(shld, false, activeChar.isSpiritshotCharged(skill), activeChar.isBlessedSpiritshotCharged(skill)));
							target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
						}
						else
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
						}
					}
					else
					{
						activeChar.stopSkillEffects(skill.getId());
						skill.getEffects(target, activeChar);
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}
				}

				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				target.reduceCurrentHp(damage, activeChar, skill);

				// Logging damage
				if(Config.LOG_GAME_DAMAGE && activeChar instanceof L2Playable && damage > Config.LOG_GAME_DAMAGE_THRESHOLD)
				{
					_logDamage.log(Level.INFO, DamageLogFormatter.format("DAMAGE MDAM:", new Object[]{
						activeChar, " did damage ", damage, skill, " to ", target
					}));
				}
			}
		}

		// self Effect :]
		if(skill.hasSelfEffects())
		{
			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if(effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			skill.getEffectsSelf(activeChar);
		}

		if(skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
			if(activeChar.isNpc())
			{
				L2Npc mob = L2Npc.class.cast(activeChar);
				L2PcInstance targeta = targets.length > 0 ? targets[0] instanceof L2PcInstance ? L2PcInstance.class.cast(targets[0]) : null : null;
				if(mob.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null && targeta != null)
				{
					for(Quest quest : mob.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
					{
						quest.notifyKill(mob, targeta, false);
					}
				}
			}
		}

		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
