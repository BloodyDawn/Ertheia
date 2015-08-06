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

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * @author demonia
 */

public class ImmobilePetBuff extends L2Effect
{
	private L2Summon _pet;

	public ImmobilePetBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		_pet = null;

		if(getEffected().isSummon() && getEffector() instanceof L2PcInstance && ((L2Summon) getEffected()).getOwner().equals(getEffector()))
		{
			_pet = (L2Summon) getEffected();
			_pet.setIsImmobilized(true);
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		if(_pet != null)
		{
			_pet.setIsImmobilized(false);
		}
	}
}