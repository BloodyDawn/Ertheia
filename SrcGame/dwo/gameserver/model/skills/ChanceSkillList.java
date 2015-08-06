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
package dwo.gameserver.model.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.handler.effects.ChanceSkillTrigger;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * CT2.3: Added support for allowing effect as a chance skill trigger (DrHouse)
 *
 * @author kombat
 */

public class ChanceSkillList extends FastMap<IChanceSkillTrigger, ChanceCondition>
{
	protected static final Logger _log = LogManager.getLogger(ChanceSkillList.class);
	private static final long serialVersionUID = 1L;

	private final L2Character _owner;

	public ChanceSkillList(L2Character owner)
	{
		shared();
		_owner = owner;
	}

	public L2Character getOwner()
	{
		return _owner;
	}

	public void onHit(L2Character target, int damage, boolean ownerWasHit, boolean wasCrit)
	{
		int event;
		if(ownerWasHit)
		{
			event = ChanceCondition.EVT_ATTACKED | ChanceCondition.EVT_ATTACKED_HIT;
			if(wasCrit)
			{
				event |= ChanceCondition.EVT_ATTACKED_CRIT;
			}
		}
		else
		{
			event = ChanceCondition.EVT_HIT;
			if(wasCrit)
			{
				event |= ChanceCondition.EVT_CRIT;
			}
		}

		onEvent(event, damage, target, null, Elementals.NONE);
	}

	public void onEvadedHit(L2Character attacker)
	{
		onEvent(ChanceCondition.EVT_EVADED_HIT, 0, attacker, null, Elementals.NONE);
	}

	public void onSkillHit(L2Character target, L2Skill skill, boolean ownerWasHit)
	{
		int event;
		if(ownerWasHit)
		{
			event = ChanceCondition.EVT_HIT_BY_SKILL;
			if(skill.isOffensive())
			{
				event |= ChanceCondition.EVT_HIT_BY_OFFENSIVE_SKILL;
				event |= ChanceCondition.EVT_ATTACKED;
				event |= ChanceCondition.EVT_ATTACKED_HIT;
			}
			else
			{
				event |= ChanceCondition.EVT_HIT_BY_GOOD_MAGIC;
			}
		}
		else
		{
			event = ChanceCondition.EVT_CAST;
			event |= skill.isMagic() ? ChanceCondition.EVT_MAGIC : ChanceCondition.EVT_PHYSICAL;
			event |= skill.isOffensive() ? ChanceCondition.EVT_MAGIC_OFFENSIVE : ChanceCondition.EVT_MAGIC_GOOD;
		}

		onEvent(event, 0, target, skill, skill.getElement());
	}

	public void onStart(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_START, 0, _owner, null, element);
	}

	public void onActionTime(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_ACTION_TIME, 0, _owner, null, element);
	}

	public void onExit(byte element)
	{
		onEvent(ChanceCondition.EVT_ON_EXIT, 0, _owner, null, element);
	}

	public void onEvent(int event, int damage, L2Character target, L2Skill skill, byte element)
	{
		if(_owner.isDead())
		{
			return;
		}

		// Prevent triggering skill on char load
		if(_owner instanceof L2Playable && !((L2Playable) _owner).isOnline())
		{
			return;
		}

		boolean playable = target instanceof L2Playable;
		for(Entry<IChanceSkillTrigger, ChanceCondition> e = head(), end = tail(); !(e = e.getNext()).equals(end); )
		{
			if(e.getValue() != null && e.getValue().trigger(event, damage, element, playable, skill))
			{
				if(e.getKey() instanceof L2Skill)
				{
					makeCast((L2Skill) e.getKey(), target);
				}
				else if(e.getKey() instanceof ChanceSkillTrigger)
				{
					makeCast((ChanceSkillTrigger) e.getKey(), target);
				}
			}
		}
	}

	/* Для тригеров активируемых скллом */
	private void makeCast(L2Skill skill, L2Character target)
	{
		try
		{
			if(skill.getWeaponDependancy(_owner, true) && skill.checkCondition(_owner, target, false))
			{
				if(skill.triggersChanceSkill()) //skill will trigger another skill, but only if its not chance skill
				{
					skill = SkillTable.getInstance().getInfo(skill.getTriggeredChanceId(), skill.getTriggeredChanceLevel());

					if(skill == null || skill.getSkillType() == L2SkillType.NOTDONE)
					{
						return;
					}

					/* Необходимая проверка для тригерных скиллов.
					 * Для того чтобы не выбивало тригер если он уже висит.
					 * Добавил проверку эффект тригер или нет, необходим чтобы управлять "заменяемостью" тригеров.
					 * Если верить игрокам, то есть есть тригеры которые как не обновляются так и обновляются.
					 */
					for(L2Effect effect : _owner.getAllEffects())
					{
						if(effect != null && skill.getId() == effect.getTriggeredChanceId() && effect.getSkill().isTriggeredSkill())
						{
							return;
						}
					}
				}

				if(_owner.isSkillDisabled(skill))
				{
					return;
				}

				if(skill.getReuseDelay() > 0)
				{
					_owner.disableSkill(skill, skill.getReuseDelay());
				}

				L2Object[] targets = skill.getTargetList(_owner, false, target);

				if(targets.length == 0)
				{
					return;
				}

				L2Character firstTarget = (L2Character) targets[0];

				// Повышение уровня тригера
				int max = skill.getTriggeredLevelUpMax();
				if(max > 0 && max > skill.getLevel())
				{
					L2Effect effect = _owner.getFirstEffect(skill.getId());
					if(effect != null)
					{
						skill = SkillTable.getInstance().getInfo(effect.getSkill().getId(), effect.getSkill().getLevel() + 1);
					}
				}

				ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());

				_owner.broadcastPacket(new MagicSkillLaunched(_owner, skill.getDisplayId(), skill.getLevel(), targets));
				_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, skill.getDisplayId(), skill.getLevel(), 0, 0));

				// Launch the magic skill and calculate its effects
				// TODO: once core will support all possible effects, use effects (not handler)
				if(handler != null)
				{
					handler.useSkill(_owner, skill, targets);
				}
				else
				{
					skill.useSkill(_owner, targets);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	/* Для тригеров активируемых эффектом */
	private void makeCast(ChanceSkillTrigger effect, L2Character target)
	{
		try
		{
			if(effect == null || !effect.triggersChanceSkill())
			{
				return;
			}

			L2Skill triggered = SkillTable.getInstance().getInfo(effect.getTriggeredChanceId(), effect.getTriggeredChanceLevel());
			if(triggered == null)
			{
				return;
			}
			L2Character caster = triggered.getTargetType() == L2TargetType.TARGET_SELF ? _owner : effect.getEffector();

			if(caster == null || triggered.getSkillType() == L2SkillType.NOTDONE || caster.isSkillDisabled(triggered))
			{
				return;
			}

			if(triggered.getReuseDelay() > 0)
			{
				caster.disableSkill(triggered, triggered.getReuseDelay());
			}

			L2Object[] targets = triggered.getTargetList(caster, false, target);

			if(targets.length == 0)
			{
				return;
			}

			L2Character firstTarget = (L2Character) targets[0];

			ISkillHandler handler = SkillHandler.getInstance().getHandler(triggered.getSkillType());

			_owner.broadcastPacket(new MagicSkillLaunched(_owner, triggered.getDisplayId(), triggered.getLevel(), targets));
			_owner.broadcastPacket(new MagicSkillUse(_owner, firstTarget, triggered.getDisplayId(), triggered.getLevel(), 0, 0));

			// Launch the magic skill and calculate its effects
			// TODO: once core will support all possible effects, use effects (not handler)
			if(handler != null)
			{
				handler.useSkill(caster, triggered, targets);
			}
			else
			{
				triggered.useSkill(caster, targets);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}
}