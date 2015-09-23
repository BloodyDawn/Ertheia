package dwo.gameserver.model.world.npc;

import dwo.gameserver.model.skills.SkillTable;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Class hold information about basic pet stats which are same on each level
 * @author JIV
 */
public class L2PetData
{
	private final TIntObjectHashMap<L2PetLevelData> _levelStats = new TIntObjectHashMap<>();
	private final List<L2PetSkillLearn> _skills = new ArrayList<>();

	private int _load = 20000;
	private int _hungry_limit = 1;
	private int _minlvl = Byte.MAX_VALUE;
	private int[] _food = ArrayUtils.EMPTY_INT_ARRAY;
	private boolean _sync_level;

	public void addNewStat(L2PetLevelData data, int level)
	{
		_minlvl = Math.min( _minlvl, level );
		_levelStats.put(level, data);
	}

	public L2PetLevelData getPetLevelData(int petLevel)
	{
		return _levelStats.get(petLevel);
	}

	public int getLoad()
	{
		return _load;
	}

	public void setLoad(int load)
	{
		_load = load;
	}

	public int getHungryLimit()
	{
		return _hungry_limit;
	}

	public void setHungryLimit(int hungry_limit)
	{
		_hungry_limit = hungry_limit;
	}

	public int getMinLevel()
	{
		return _minlvl;
	}

	public int[] getFood()
	{
		return _food;
	}

	public void setFood(int[] food)
	{
		_food = food;
	}

	public boolean syncLevel()
	{
		return _sync_level;
	}

	public void setSyncLevel(int sync_level)
	{
		_sync_level = sync_level == 1;
	}

	//SKILS

	public void addNewSkill(int id, int lvl, int petLvl)
	{
		_skills.add(new L2PetSkillLearn(id, lvl, petLvl));
	}

	public int getAvailableLevel(int skillId, int petLvl)
	{
		int lvl = 0;
		for(L2PetSkillLearn temp : _skills)
		{
			if(temp.getId() != skillId)
			{
				continue;
			}
			if(temp.getLevel() == 0)
			{
				if(petLvl < 70)
				{
					lvl = petLvl / 10;
					if(lvl <= 0)
					{
						lvl = 1;
					}
				}
				else
				{
					lvl = 7 + (petLvl - 70) / 5;
				}

				// formula usable for skill that have 10 or more skill levels
				int maxLvl = SkillTable.getInstance().getMaxLevel(temp.getId());
				if(lvl > maxLvl)
				{
					lvl = maxLvl;
				}
				break;
			}
			else if(temp.getMinLevel() <= petLvl)
			{
				if(temp.getLevel() > lvl)
				{
					lvl = temp.getLevel();
				}
			}

		}
		return lvl;
	}

	public List<L2PetSkillLearn> getAvailableSkills()
	{
		return _skills;
	}

	public static class L2PetSkillLearn
	{
		private final int _id;
		private final int _level;
		private final int _minLevel;

		public L2PetSkillLearn(int id, int lvl, int minLvl)
		{
			_id = id;
			_level = lvl;
			_minLevel = minLvl;
		}

		public int getId()
		{
			return _id;
		}

		public int getLevel()
		{
			return _level;
		}

		public int getMinLevel()
		{
			return _minLevel;
		}
	}
}
