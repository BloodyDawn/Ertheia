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
package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.StatsSet;
import javolution.util.FastList;

import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class L2SkillRefreshDebuffTime extends L2Skill
{
	private List<L2EffectType> _refreshableEffectTypes;

	public L2SkillRefreshDebuffTime(StatsSet set)
	{
		super(set);
		String effectTypes = set.getString("refreshableTypes", "");
		_refreshableEffectTypes = new FastList();
		if(!effectTypes.isEmpty())
		{
			String[] types = effectTypes.split(",");
			for(String type : types)
			{
				try
				{
					_refreshableEffectTypes.add(L2EffectType.valueOf(type.trim()));
				}
				catch(IllegalArgumentException e)
				{
					_log.error("Refreshable type " + type + " loading failed.", e);
				}
			}
		}
	}

	@Override
	public void useSkill(L2Character caster, L2Object[] targets)
	{

	}

	/**
	 * Returns list of effect types that skill can refresh.
	 * @return
	 */
	public List<L2EffectType> getRefreshableEffectTypes()
	{
		return _refreshableEffectTypes;
	}
}
