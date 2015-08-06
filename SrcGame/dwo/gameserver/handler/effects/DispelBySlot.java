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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Dispel By Slot effect implementation.
 * @author Gnacik, Zoey76, Adry_85
 */
public class DispelBySlot extends L2Effect
{
	private final String _dispel;
	private final Map<String, Byte> _dispelAbnormals;

	public DispelBySlot(Env env, EffectTemplate template)
	{
		super(env, template);
		_dispel = template.getParameters().getString("dispel", null);
		if(_dispel != null && !_dispel.isEmpty())
		{
			_dispelAbnormals = new HashMap<>();
			for(String ngtStack : _dispel.split(";"))
			{
				String[] ngt = ngtStack.split(",");
				_dispelAbnormals.put(ngt[0], Byte.parseByte(ngt[1]));
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

			for(Entry<String, Byte> dispel : _dispelAbnormals.entrySet())
			{
				// TODO
				if(effect.getEffectTemplate().getAbnormalType().equals("transform") && (dispel.getValue() == getEffected().getActingPlayer().getTransformationId() || dispel.getValue() < 0))
				{
					getEffected().stopTransformation(true);
				}
				else if(effect.getEffectTemplate().getAbnormalType().equals(dispel.getKey()) && dispel.getValue() >= effect.getSkill().getAbnormalLvl())
				{
					effect.exit();
				}
			}
		}
		return true;
	}

	@Override
	public boolean isInstant()
	{
		return true;
	}
}
