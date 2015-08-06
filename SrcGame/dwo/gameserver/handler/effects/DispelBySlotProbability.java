/*
 * Copyright (C) 2004-2013 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.Rnd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Dispel By Slot Probability effect implementation.
 * @author Adry_85
 */
public class DispelBySlotProbability extends L2Effect
{
	private final String _dispel;
	private final Map<String, Byte> _dispelAbnormals;
	private final int _rate;

	public DispelBySlotProbability(Env env, EffectTemplate template)
	{
		super(env, template);
		_dispel = template.getParameters().getString("dispel", null);
		_rate = template.getParameters().getInteger("rate", 0);
		if(_dispel != null && !_dispel.isEmpty())
		{
			_dispelAbnormals = new HashMap<>();

			for(String ngtStack : _dispel.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(ngt[0], Byte.MAX_VALUE);
			}
		}
		else
		{
			_dispelAbnormals = Collections.<String, Byte>emptyMap();
		}
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DISPEL;
	}

	@Override
	public boolean onStart()
	{
		if(_dispelAbnormals.isEmpty())
		{
			return false;
		}

		for(L2Effect effect : getEffected().getAllEffects())
		{
			if(effect == null)
			{
				continue;
			}

			_dispelAbnormals.entrySet().stream().filter(negate -> effect.getEffectTemplate().getAbnormalType().equals(negate.getKey()) && Rnd.get(100) < _rate).forEach(negate -> effect.exit());
		}
		return true;
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}
}
