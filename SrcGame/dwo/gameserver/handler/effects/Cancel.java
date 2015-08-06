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

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.base.formulas.calculations.Effects;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;

/**
 *
 * @author DS
 *
 */
public class Cancel extends L2Effect
{
	protected static final Logger _log = LogManager.getLogger(Cancel.class);

	public Cancel(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	private static boolean cancel(L2Character activeChar, L2Character target, L2Effect effect)
	{
		if(target.isDead())
		{
			return false;
		}

		List<L2Effect> canceled = Effects.calcCancel(activeChar, target, effect.getSkill(), effect.getEffectPower());
		for(L2Effect eff : canceled)
		{
			eff.exit();
		}
		return true;
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CANCEL;
	}

	@Override
	public boolean onStart()
	{
		return cancel(getEffector(), getEffected(), this);
	}
}