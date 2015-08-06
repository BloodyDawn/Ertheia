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

import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author Kerberos
 */

public class Fusion extends L2Effect
{
	public int _effect;
	public int _maxEffect;

	public Fusion(Env env, EffectTemplate template)
	{
		super(env, template);
		_effect = getSkill().getLevel();
		_maxEffect = SkillTable.getInstance().getMaxLevel(getSkill().getId());
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FUSION;
	}

	@Override
	public boolean onActionTime()
	{
		return getSkill().isToggle();
	}

	@Override
	public void decreaseForce()
	{
		_effect--;
		if(_effect < 1)
		{
			exit();
		}
		else
		{
			updateBuff();
		}
	}

	@Override
	public void increaseEffect()
	{
		if(_effect < _maxEffect)
		{
			_effect++;
			updateBuff();
		}
	}

	private void updateBuff()
	{
		exit();
		SkillTable.getInstance().getInfo(getSkill().getId(), _effect).getEffects(getEffector(), getEffected());
	}
}
