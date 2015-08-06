package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class ManaHeal implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.MANAHEAL, L2SkillType.MANARECHARGE, L2SkillType.MANA_BY_LEVEL
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for(L2Character target : (L2Character[]) targets)
		{
			if(target.isInvul())
			{
				continue;
			}

			double mp = skill.getPower();

			switch(skill.getSkillType())
			{
				case MANARECHARGE:
					mp = target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null);
					break;
				case MANA_BY_LEVEL:
					//recharged mp influenced by difference between target level and skill level
					//if target is within 5 levels or lower then skill level there's no penalty.
					mp = target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null);
					if(target.getLevel() > skill.getMagicLevel())
					{
						int lvlDiff = target.getLevel() - skill.getMagicLevel();
						//if target is too high compared to skill level, the amount of recharged mp gradually decreases.
						if(lvlDiff == 6)        //6 levels difference:
						{
							mp *= 0.9;            //only 90% effective
						}
						else if(lvlDiff == 7)
						{
							mp *= 0.8;            //80%
						}
						else if(lvlDiff == 8)
						{
							mp *= 0.7;            //70%
						}
						else if(lvlDiff == 9)
						{
							mp *= 0.6;            //60%
						}
						else if(lvlDiff == 10)
						{
							mp *= 0.5;            //50%
						}
						else if(lvlDiff == 11)
						{
							mp *= 0.4;            //40%
						}
						else if(lvlDiff == 12)
						{
							mp *= 0.3;            //30%
						}
						else if(lvlDiff == 13)
						{
							mp *= 0.2;            //20%
						}
						else if(lvlDiff == 14)
						{
							mp *= 0.1;            //10%
						}
						else if(lvlDiff >= 15)    //15 levels or more:
						{
							mp = 0;                //0mp recharged
						}
					}
			}

			//from CT2 u will receive exact MP, u can't go over it, if u have full MP and u get MP buff, u will receive 0MP restored message
			mp = Math.min(mp, target.getMaxRecoverableMp() - target.getCurrentMp());

			// Prevent negative amounts
			if(mp < 0)
			{
				mp = 0;
			}

			target.setCurrentMp(mp + target.getCurrentMp());
			StatusUpdate sump = new StatusUpdate(target);
			sump.addAttribute(StatusUpdate.CUR_MP, (int) target.getCurrentMp());
			target.sendPacket(sump);

			// if skill power is "0 or less" don't show heal system message.
			SystemMessage sm;
			if(skill.getPower() > 0)
			{
				if(activeChar.isPlayer() && !activeChar.equals(target))
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_C1);
					sm.addString(activeChar.getName());
					sm.addNumber((int) mp);
					target.sendPacket(sm);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
					sm.addNumber((int) mp);
					target.sendPacket(sm);
				}
			}

			if(skill.hasEffects())
			{
				target.stopSkillEffects(skill.getId());
				skill.getEffects(activeChar, target);
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
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

		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
