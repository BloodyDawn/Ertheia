package dwo.gameserver.model.skills;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.documentengine.XmlDocumentEngine;
import dwo.gameserver.engine.documentengine.skills.XmlDocumentSkillClient;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SkillTable
{
	private static final Map<Integer, L2Skill> _skills = new HashMap<>();
	private static final TIntIntHashMap _skillMaxLevel = new TIntIntHashMap();
	private static final TIntArrayList _enchantable = new TIntArrayList();
	private static Logger _log = LogManager.getLogger(SkillTable.class);

	private SkillTable()
	{
		_skills.clear();
		_skillMaxLevel.clear();
		_enchantable.clear();
	}

	public static SkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Provides the skill hash
	 *
	 * @param skill The L2Skill to be hashed
	 * @return getSkillHashCode(skill.getId(), skill.getLevel())
	 */
	public static int getSkillHashCode(L2Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}

	/**
	 * Centralized method for easier change of the hashing sys
	 *
	 * @param skillId    The Skill Id
	 * @param skillLevel The Skill Level
	 * @return The Skill hash number
	 */
	public static int getSkillHashCode(int skillId, int skillLevel)
	{
		return (skillId << 16) + skillLevel;
	}

	public void reload()
	{
		// reload some related too
		SkillTreesData.getInstance().reload();
		load(true);
	}

	public void load(boolean reload)
	{
		XmlDocumentEngine.getInstance().loadAllSkills(_skills);

		for(L2Skill skill : _skills.values())
		{
			int skillId = skill.getId();
			int skillLvl = skill.getLevel();

			if(skillLvl > 99)
			{
				if(!_enchantable.contains(skillId))
				{
					_enchantable.add(skillId);
				}
				continue;
			}
			// only non-enchanted skills
			int maxLvl = _skillMaxLevel.get(skillId);
			if(skillLvl > maxLvl)
			{
				_skillMaxLevel.put(skillId, skillLvl);
			}
		}

		// Sorting for binarySearch
		_enchantable.sort();
	}

	public L2Skill getInfo(int hash)
	{
		return _skills.get(hash);
	}

	public L2Skill getInfo(int skillId, int level)
	{
		L2Skill result = _skills.get(getSkillHashCode(skillId, level));
		if(result != null)
		{
			return result;
		}

		// skill/level not found, fix for transformation scripts
		int maxLvl = _skillMaxLevel.get(skillId);
		// requested level too high
		if(maxLvl > 0 && level > maxLvl)
		{
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		}
        //      Уже не имеет смысла, т.к. болванки создаются на все скилы )
		//_log.log(Level.WARN, "No skill info found for skill id " + skillId + " and skill level " + level + '.');
		//for(StackTraceElement elem : Thread.currentThread().getStackTrace())
		//{
		//	if(elem != null)
		//	{
		//		_log.log(Level.WARN, "Class " + elem.getClassName() + ", line " + elem.getLineNumber() + ", method" + elem.getMethodName() + ", file " + elem.getFileName());
		//	}
		//}
		return null;
	}

	public int getMaxLevel(int skillId)
	{
		return _skillMaxLevel.get(skillId);
	}

	public boolean isEnchantable(int skillId)
	{
		return _enchantable.binarySearch(skillId) >= 0;
	}

	/**
	 * @return an array with siege skills. If addNoble == true, will add also Advanced headquarters.
	 */
	public L2Skill[] getSiegeSkills(boolean addNoble, boolean hasCastle)
	{
		L2Skill[] temp = new L2Skill[3 + (addNoble ? 1 : 0) + (hasCastle ? 2 : 0)];
		int i = 0;
		temp[i++] = _skills.get(getSkillHashCode(19034, 1));
		temp[i++] = _skills.get(getSkillHashCode(19035, 1));
		temp[i++] = _skills.get(getSkillHashCode(247, 1));

		if(addNoble)
		{
			temp[i++] = _skills.get(getSkillHashCode(326, 1));
		}
		if(hasCastle)
		{
			temp[i++] = _skills.get(getSkillHashCode(844, 1));
			temp[i++] = _skills.get(getSkillHashCode(845, 1));
		}
		return temp;
	}

	/**
	 * Enum to hold some important references to frequently used (hardcoded)
	 * skills in core
	 *
	 * @author DrHouse
	 */
	public static enum FrequentSkill
	{
		RAID_CURSE(4215, 1),
		RAID_CURSE2(4515, 1),
		SEAL_OF_RULER_LIGHT(19034, 1),
		SEAL_OF_RULER_DARK(19035, 1),
		BUILD_HEADQUARTERS(247, 1),
		LUCKY(194, 1),
		DWARVEN_CRAFT(1321, 1),
		COMMON_CRAFT(1322, 1),
		WYVERN_BREATH(4289, 1),
		STRIDER_SIEGE_ASSAULT(325, 1),
		FAKE_PETRIFICATION(4616, 1),
		FIREWORK(5965, 1),
		LARGE_FIREWORK(2025, 1),
		BLESSING_OF_PROTECTION(5182, 1),
		ARENA_CP_RECOVERY(4380, 1),
		VOID_BURST(3630, 1),
		VOID_FLOW(3631, 1),
		THE_VICTOR_OF_WAR(5074, 1),
		THE_VANQUISHED_OF_WAR(5075, 1),
		SPECIAL_TREE_RECOVERY_BONUS(2139, 1);

		private final SkillHolder _holder;

		private FrequentSkill(int id, int level)
		{
			_holder = new SkillHolder(id, level);
		}

		public L2Skill getSkill()
		{
			return _holder.getSkill();
		}
	}

	private static class SingletonHolder
	{
		protected static final SkillTable _instance = new SkillTable();
	}
}