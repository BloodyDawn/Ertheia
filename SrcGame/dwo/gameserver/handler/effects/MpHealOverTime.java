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

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;

public class MpHealOverTime extends L2Effect
{
	public MpHealOverTime(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public MpHealOverTime(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MANA_HEAL_OVER_TIME;
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		double mp = getEffected().getCurrentMp();
		double maxmp = getEffected().getMaxMp();
		mp += calc();
		if(mp > maxmp)
		{
			mp = maxmp;
		}

		getEffected().setCurrentMp(mp);
		StatusUpdate sump = new StatusUpdate(getEffected());
		sump.addAttribute(StatusUpdate.CUR_MP, (int) mp);
		getEffected().sendPacket(sump);
		return getSkill().isToggle();
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}
}
