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

import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

public class Paralyze extends L2Effect
{
	public Paralyze(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public Paralyze(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PARALYZE;
	}

	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.HOLD_1);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
		getEffected().startParalyze();
		return super.onStart();
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_1);
		getEffected().stopParalyze(false);
		super.onExit();
	}

	@Override
	protected boolean effectCanBeStolen()
	{
		return true;
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_PARALYZED;
	}
}
