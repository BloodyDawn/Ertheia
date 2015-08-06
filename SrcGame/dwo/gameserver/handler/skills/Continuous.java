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

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.player.duel.DuelManager;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.BlowDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class Continuous implements ISkillHandler
{
	private static final L2SkillType[] SKILL_TYPES = {
		L2SkillType.BUFF, L2SkillType.DEBUFF, L2SkillType.DOT, L2SkillType.MDOT, L2SkillType.POISON, L2SkillType.BLEED,
		L2SkillType.HOT, L2SkillType.CPHOT, L2SkillType.MPHOT, L2SkillType.FEAR, L2SkillType.CONT,
		L2SkillType.UNDEAD_DEFENSE, L2SkillType.AGGDEBUFF, L2SkillType.FUSION, L2SkillType.CASTTIME
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		boolean acted = true;

		L2PcInstance player = null;
		if(activeChar instanceof L2PcInstance)
		{
			player = (L2PcInstance) activeChar;
		}

		if(skill.getEffectId() != 0)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(skill.getEffectId(), skill.getEffectLvl() == 0 ? 1 : skill.getEffectLvl());

			if(sk != null)
			{
				skill = sk;
			}
		}

		boolean ss = activeChar.isSoulshotCharged(skill);
		boolean sps = activeChar.isSpiritshotCharged(skill);
		boolean bss = activeChar.isBlessedSpiritshotCharged(skill);

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

			byte shld = 0;

			if(Reflect.calcSkillReflect(target, skill) == Variables.SKILL_REFLECT_SUCCEED)
			{
				target = activeChar;
			}

			// Персонаж, владеющий ПО, не может быть бафнутым и не сам не может никого бафать
			if(skill.getSkillType() == L2SkillType.BUFF && activeChar.isNpc() && ((L2Npc) activeChar).getClanHall() != null)
			{
				if(!target.equals(activeChar))
				{
					if(target instanceof L2PcInstance)
					{
						L2PcInstance trg = (L2PcInstance) target;
						if(trg.isCursedWeaponEquipped())
						{
							continue;
						}
						// Avoiding block checker players get buffed from outside
						else if(trg.getEventController().isInHandysBlockCheckerEventArena())
						{
							continue;
						}
					}
					else if(player != null && player.isCursedWeaponEquipped())
					{
						continue;
					}
				}
			}

			switch(skill.getSkillType())
			{
				case HOT:
				case CPHOT:
				case MPHOT:
					if(activeChar.isInvul())
					{
						continue;
					}
					break;
			}

			if(skill.isOffensive() || skill.isDebuff())
			{
				shld = Shield.calcShldUse(activeChar, target, skill);
				acted = Skills.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss);
			}

			if(acted)
			{
				if(skill.isToggle())
				{
					for(L2Effect e : target.getAllEffects())
					{
						if(e != null && e.getSkill().getId() == skill.getId())
						{
							e.exit();
							return;
						}
					}
				}

				// if this is a debuff let the duel manager know about it
				// so the debuff can be removed after the duel
				// (player & target must be in the same duel)
				if(target instanceof L2PcInstance && ((L2PcInstance) target).isInDuel() && (skill.getSkillType() == L2SkillType.DEBUFF || skill.getSkillType() == L2SkillType.BUFF) && player != null && player.getDuelId() == ((L2PcInstance) target).getDuelId())
				{
					DuelManager dm = DuelManager.getInstance();
					for(L2Effect buff : skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss)))
					{
						if(buff != null)
						{
							dm.onBuff((L2PcInstance) target, buff);
						}
					}
				}
				else
				{
					L2Effect[] effects = skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss));

					if(target instanceof L2PcInstance && !target.getPets().isEmpty())
					{
						for(L2Summon pet : target.getPets())
						{
							if(!pet.equals(activeChar) && pet instanceof L2SummonInstance && effects.length > 0)
							{
								if(effects[0].canBeStolen() || skill.isHeroSkill() || skill.isStatic())
								{
									skill.getEffects(activeChar, pet, new Env(shld, ss, sps, bss));
								}
							}
						}
					}
				}

				if(skill.getSkillType() == L2SkillType.AGGDEBUFF)
				{
					if(target instanceof L2Attackable)
					{
						target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, (int) skill.getPower());
					}
					else if(target instanceof L2Playable)
					{
						if(target.getTarget().equals(activeChar))
						{
							target.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, activeChar);
						}
						else
						{
							target.setTarget(activeChar);
						}
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.ATTACK_FAILED);
			}

			// Possibility of a lethal strike
			BlowDamage.calcLethalHit(activeChar, target, skill);
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

		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_TYPES;
	}
}
