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
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.Effects;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

import java.util.List;

public class StealBuffs implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.STEAL_BUFF
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar instanceof L2Npc)
		{
			((L2Npc) activeChar)._spiritshotcharged = false;
		}

		L2Character target;
		L2Effect effect;

		for(L2Object obj : targets)
		{
			if(!(obj instanceof L2Character))
			{
				continue;
			}
			target = (L2Character) obj;

			if(target.isDead())
			{
				continue;
			}

			if(!(target instanceof L2PcInstance))
			{
				continue;
			}

			Env env;
			List<L2Effect> toSteal = Effects.calcCancel(activeChar, target, skill, skill.getPower());

			if(toSteal.isEmpty())
			{
				continue;
			}

			// stealing effects
			for(L2Effect eff : toSteal)
			{
				env = new Env();
				env.setPlayer(target);
				env.setTarget(activeChar);
				env.setSkill(eff.getSkill());
				try
				{
					effect = eff.getEffectTemplate().getStolenEffect(env, eff);
					if(effect != null)
					{
						effect.scheduleEffect();
						if(effect.isIconDisplay() && activeChar instanceof L2PcInstance)
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(effect));
						}
					}
					// Finishing stolen effect
					eff.exit();
				}
				catch(RuntimeException e)
				{
					_log.log(Level.ERROR, "Cannot steal effect: " + eff + " Stealer: " + activeChar + " Stolen: " + target, e);
				}
			}
		}

		if(skill.hasSelfEffects())
		{
			// Applying self-effects
			effect = activeChar.getFirstEffect(skill.getId());
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
		return SKILL_IDS;
	}
}