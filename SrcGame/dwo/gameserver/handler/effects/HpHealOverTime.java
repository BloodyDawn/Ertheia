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

import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExRegenMax;

public class HpHealOverTime extends L2Effect
{
	public HpHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public HpHealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.HEAL_OVER_TIME;
	}

	@Override
	public boolean onStart()
	{
		getEffected().sendPacket(new ExRegenMax(calc(), getEffectTemplate().getTotalTickCount() * getAbnormalTime(), getAbnormalTime()));
		return true;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		if(getEffected() instanceof L2DoorInstance)
		{
			return false;
		}

		double hp = getEffected().getCurrentHp();
		double maxhp = getEffected().getMaxHp();
		hp += calc();
		if(hp > maxhp)
		{
			hp = maxhp;
		}

		getEffected().setCurrentHp(hp);
		StatusUpdate suhp = new StatusUpdate(getEffected());
		suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
		getEffected().sendPacket(suhp);
		return getSkill().isToggle();
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}
}
