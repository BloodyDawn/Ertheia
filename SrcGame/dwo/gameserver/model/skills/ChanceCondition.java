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

import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * @author kombat
 */
public class ChanceCondition
{
	public static final int EVT_HIT = 1;
	public static final int EVT_CRIT = 2;
	public static final int EVT_CAST = 4;
	public static final int EVT_PHYSICAL = 8;
	public static final int EVT_MAGIC = 16;
	public static final int EVT_MAGIC_GOOD = 32;
	public static final int EVT_MAGIC_OFFENSIVE = 64;
	public static final int EVT_ATTACKED = 128;
	public static final int EVT_ATTACKED_HIT = 256;
	public static final int EVT_ATTACKED_CRIT = 512;
	public static final int EVT_HIT_BY_SKILL = 1024;
	public static final int EVT_HIT_BY_OFFENSIVE_SKILL = 2048;
	public static final int EVT_HIT_BY_GOOD_MAGIC = 4096;
	public static final int EVT_EVADED_HIT = 8192;
	public static final int EVT_ON_START = 16384;
	public static final int EVT_ON_ACTION_TIME = 32768;
	public static final int EVT_ON_EXIT = 65536;
	protected static final Logger _log = LogManager.getLogger(ChanceCondition.class);
	private final TriggerType _triggerType;
	private final int _chance;
	private final int _mindmg;
	private final byte[] _elements;
	private final int[] _activationSkills;
	private final boolean _pvpOnly;
	private final int _bySkillId;

	private ChanceCondition(TriggerType trigger, int chance, int mindmg, byte[] elements, int[] activationSkills, boolean pvpOnly, int bySkillId)
	{
		_triggerType = trigger;
		_chance = chance;
		_mindmg = mindmg;
		_elements = elements;
		_pvpOnly = pvpOnly;
		_activationSkills = activationSkills;
		_bySkillId = bySkillId;
	}

	public static ChanceCondition parse(StatsSet set)
	{
		try
		{
			TriggerType trigger = set.getEnum("chanceType", TriggerType.class, null);
			int chance = set.getInteger("activationChance", -1);
			int mindmg = set.getInteger("activationMinDamage", -1);
			String elements = set.getString("activationElements", null);
			String activationSkills = set.getString("activationSkills", null);
			boolean pvpOnly = set.getBool("pvpChanceOnly", false);
			int bySkillId = set.getInteger("triggeredById", -1);

			if(trigger != null)
			{
				return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), parseActivationSkills(activationSkills), pvpOnly, bySkillId);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		return null;
	}

	public static ChanceCondition parse(String chanceType, int chance, int mindmg, String elements, String activationSkills, boolean pvpOnly, int bySkillId)
	{
		try
		{
			if(chanceType == null)
			{
				return null;
			}

			TriggerType trigger = Enum.valueOf(TriggerType.class, chanceType);

			if(trigger != null)
			{
				return new ChanceCondition(trigger, chance, mindmg, parseElements(elements), parseActivationSkills(activationSkills), pvpOnly, bySkillId);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}

		return null;
	}

	public static byte[] parseElements(String list)
	{
		if(list == null)
		{
			return null;
		}

		String[] valuesSplit = list.split(",");
		byte[] elements = new byte[valuesSplit.length];
		for(int i = 0; i < valuesSplit.length; i++)
		{
			elements[i] = Byte.parseByte(valuesSplit[i]);
		}

		Arrays.sort(elements);
		return elements;
	}

	public static int[] parseActivationSkills(String list)
	{
		if(list == null)
		{
			return null;
		}

		String[] valuesSplit = list.split(",");
		int[] skillIds = new int[valuesSplit.length];
		for(int i = 0; i < valuesSplit.length; i++)
		{
			skillIds[i] = Integer.parseInt(valuesSplit[i]);
		}

		return skillIds;
	}

	public boolean trigger(int event, int damage, byte element, boolean playable, L2Skill skill)
	{
		if(_pvpOnly && !playable)
		{
			return false;
		}

		if(_elements != null && Arrays.binarySearch(_elements, element) < 0)
		{
			return false;
		}

		if(_activationSkills != null && skill != null && Arrays.binarySearch(_activationSkills, skill.getId()) < 0)
		{
			return false;
		}

		// onCast is called on every skill cast, so we must check if trigger
		// have limit (can be triggered only by some specific skill)
		if(_bySkillId > 0 && (skill == null || skill.getId() != _bySkillId))
		{
			return false;
		}

		// if the skill has "activationMinDamage" setted to higher than -1(default)
		// and if "activationMinDamage" is still higher than the recieved damage, the skill wont trigger
		if(_mindmg > -1 && _mindmg > damage)
		{
			return false;
		}

		return _triggerType.check(event) && (_chance < 0 || Rnd.getChance(_chance));
	}

	@Override
	public String toString()
	{
		return "Trigger[" + _chance + ';' + _triggerType.toString() + ']';
	}

	public static enum TriggerType
	{
		// Вы нанесли удар игроку
		ON_HIT(1),
		// Вы нанесли критический удар игроку
		ON_CRIT(2),
		// Вы произнесли заклинание
		ON_CAST(4),
		// Вы произнесли заклинание физического типа
		ON_PHYSICAL(8),
		// Вы произнесли заклинание магиеского типа
		ON_MAGIC(16),
		// Вы произнесли хорошее заклинание магического типа
		ON_MAGIC_GOOD(32),
		// Вы произнесли плохое заклинание магического типа
		ON_MAGIC_OFFENSIVE(64),
		// Вы были атакованы врагом (любой атакой)
		ON_ATTACKED(128),
		// Вы были атакованы враго (физический удар)
		ON_ATTACKED_HIT(256),
		// Вы были атакованы врагом (физический критический удар)
		ON_ATTACKED_CRIT(512),
		// На Вас наложили заклинание
		ON_HIT_BY_SKILL(1024),
		// На Вас наложили плохое заклинание
		ON_HIT_BY_OFFENSIVE_SKILL(2048),
		// На Вас наложили хорошее заклинание
		ON_HIT_BY_GOOD_MAGIC(4096),
		// Вы увернулись от атаки
		ON_EVADED_HIT(8192),
		// Стартовал некоторый эффект
		ON_START(16384),
		// Вызывается на каждый тик действующего эффекта
		ON_ACTION_TIME(32768),
		// Завершился некоторый эффект
		ON_EXIT(65536);

		private final int _mask;

		private TriggerType(int mask)
		{
			_mask = mask;
		}

		public boolean check(int event)
		{
			return (_mask & event) != 0; // Trigger (sub-)type contains event (sub-)type
		}
	}
}