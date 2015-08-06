package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.ManaDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * Class handling the Mana damage skill
 *
 * @author slyce
 */
public class Manadam implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.MANADAM
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
			if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
			{
				target = activeChar;
			}

			boolean acted = MagicalDamage.calcMagicAffected(activeChar, target, skill);
			if(target.isInvul() || !acted)
			{
				activeChar.sendPacket(SystemMessageId.MISSED_TARGET);
			}
			else
			{
				if(skill.hasEffects())
				{
					byte shld = Shield.calcShldUse(activeChar, target, skill);
					target.stopSkillEffects(skill.getId());
					if(Skills.calcSkillSuccess(activeChar, target, skill, shld, false, activeChar.isSpiritshotCharged(skill), activeChar.isBlessedSpiritshotCharged(skill)))
					{
						skill.getEffects(activeChar, target, new Env(shld, activeChar.isSpiritshotCharged(skill), false, activeChar.isBlessedSpiritshotCharged(skill)));
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
					}
				}

				double damage = skill.isStaticDamage() ? skill.getPower() : ManaDamage.calcManaDam(activeChar, target, skill, activeChar.isSpiritshotCharged(skill), activeChar.isBlessedSpiritshotCharged(skill));

				if(!skill.isStaticDamage() && MagicalDamage.calcMCrit(activeChar.getMCriticalHit(target, skill)))
				{
					damage *= 3.0;
					activeChar.sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
				}

				double mp = damage > target.getCurrentMp() ? target.getCurrentMp() : damage;
				target.reduceCurrentMp(mp);

				if(target instanceof L2PcInstance)
				{
					StatusUpdate sump = new StatusUpdate(target);
					sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
					target.sendPacket(sump);
					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_C1).addCharName(activeChar).addNumber((int) mp));
				}

				if(activeChar instanceof L2PcInstance)
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1).addNumber((int) mp));
				}
			}
		}

		if(skill.hasSelfEffects())
		{
			L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if(effect != null && effect.isSelfEffect())
			{
				//Replace old effect with new one.
				effect.exit();
			}
			// cast self effect if any
			skill.getEffectsSelf(activeChar);
		}

		if(skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}

		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}