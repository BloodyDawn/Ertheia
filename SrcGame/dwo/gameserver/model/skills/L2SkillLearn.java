package dwo.gameserver.model.skills;

import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.base.SocialClass;
import dwo.gameserver.model.player.base.SubClassType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.residence.castle.CastleSide;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zoey76
 */

public class L2SkillLearn
{
	private final String _skillName;
	private final int _skillId;
	private final int _skillLvl;
	private final int _getLevel;
	private final int _getDualLevel;
	private final boolean _autoGet;
	private final int _levelUpSp;
	private final List<Integer> _subClassTypes = new ArrayList<>();
	private final List<Race> _races = new ArrayList<>();
	private final List<SkillHolder> _preReqSkills = new ArrayList<>();
	private final boolean _residenceSkill;
	private final List<Integer> _residenceIds = new ArrayList<>();
	private final List<SubClassData> _subClassLvlNumber = new ArrayList<>();
	private final List<CastleSide> _castleSide = new ArrayList<>();
	private final boolean _learnedByNpc;
	private final boolean _learnedByFS;
	private SocialClass _socialClass;
	private List<ItemHolder> _requiredItems = new ArrayList<>();

	/**
	 * Constructor for L2SkillLearn.
	 *
	 * @param set the set with the L2SkillLearn data.
	 */
	public L2SkillLearn(StatsSet set)
	{
		_skillName = set.getString("name");
		_skillId = set.getInteger("skillId");
		_skillLvl = set.getInteger("skillLvl");
		_getLevel = set.getInteger("minLevel", 1);
		_getDualLevel = set.getInteger("minDualLevel", 0);
		_autoGet = set.getBool("autoLearn", false);
		_levelUpSp = set.getInteger("sp", 0);
		_residenceSkill = set.getBool("residenceSkill", false);
		_learnedByNpc = set.getBool("learnedByNpc", false);
		_learnedByFS = set.getBool("learnedByFS", false);
	}

	/**
	 * @return the name of this skill.
	 */
	public String getName()
	{
		return _skillName;
	}

	/**
	 * @return the ID of this skill.
	 */
	public int getSkillId()
	{
		return _skillId;
	}

	/**
	 * @return the level of this skill.
	 */
	public int getSkillLevel()
	{
		return _skillLvl;
	}

	/**
	 * @return the minimum level required to acquire this skill.
	 */
	public int getMinLevel()
	{
		return _getLevel;
	}

	/**
	 * @return the minimum level required to acquire this skill.
	 */
	public int getMinDualLevel()
	{
		return _getDualLevel;
	}

	/**
	 * @return the amount of SP/Clan Reputation to acquire this skill.
	 */
	public int getLevelUpSp()
	{
		return _levelUpSp;
	}

	/**
	 * @return {@code true} if the skill is auto-get, this skill is automatically delivered.
	 */
	public boolean isAutoGet()
	{
		return _autoGet;
	}

	/**
	 * @return a list with the races that can acquire this skill.
	 */
	public List<Race> getRaces()
	{
		return _races;
	}

	/**
	 * Adds a required race to learn this skill.
	 * @param race the required race.
	 */
	public void addRace(Race race)
	{
		_races.add(race);
	}

	public boolean checkSubClassTypes(L2PcInstance player)
	{
		if(!_subClassTypes.isEmpty())
		{
			for(Integer type : _subClassTypes)
			{
				if(player.getClassIndex() == type)
				{
					return true;
				}
			}
			return false;
		}
		return true;
	}

	public void addsubClassType(String classTypes)
	{
		_subClassTypes.add(SubClassType.valueOf(classTypes).ordinal());
	}

	/**
	 * @return the list of skill holders required to acquire this skill.
	 */
	public List<SkillHolder> getPreReqSkills()
	{
		return _preReqSkills;
	}

	/**
	 * Adds a required skill holder to learn this skill.
	 * @param skill the required skill holder.
	 */
	public void addPreReqSkill(SkillHolder skill)
	{
		_preReqSkills.add(skill);
	}

	/**
	 * @return the social class required to get this skill.
	 */
	public SocialClass getSocialClass()
	{
		return _socialClass;
	}

	/**
	 * Sets the social class if hasn't been set before.
	 * @param socialClass the social class to set.
	 */
	public void setSocialClass(SocialClass socialClass)
	{
		if(_socialClass == null)
		{
			_socialClass = socialClass;
		}
	}

	/**
	 * @return {@code true} if this skill is a Residence skill.
	 */
	public boolean isResidencialSkill()
	{
		return _residenceSkill;
	}

	/**
	 * @return a list with the Ids where this skill is available.
	 */
	public List<Integer> getResidenceIds()
	{
		return _residenceIds;
	}

	/**
	 * Adds a required residence Id.
	 * @param id the residence Id to add.
	 */
	public void addResidenceId(Integer id)
	{
		_residenceIds.add(id);
	}

	/**
	 * @return a list with Sub-Class conditions, amount of subclasses and level.
	 */
	public List<SubClassData> getSubClassConditions()
	{
		return _subClassLvlNumber;
	}

	/**
	 * Adds a required residence Id.
	 * @param slot the sub-class slot.
	 * @param lvl the required sub-class level.
	 */
	public void addSubclassConditions(int slot, int lvl)
	{
		_subClassLvlNumber.add(new SubClassData(slot, lvl));
	}

	public void addCastleSide(CastleSide side)
	{
		_castleSide.add(side);
	}

	public boolean isCastleSide(CastleSide side)
	{
		return _castleSide.isEmpty() || side != null && _castleSide.contains(side);
	}

	/**
	 * @return {@code true} if this skill is learned from Npc.
	 */
	public boolean isLearnedByNpc()
	{
		return _learnedByNpc;
	}

	/**
	 * @return {@code true} if this skill is learned by Forgotten Scroll.
	 */
	public boolean isLearnedByFS()
	{
		return _learnedByFS;
	}

	public int getReuse()
	{
		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, _skillLvl);
		return skill.getReuseDelay();
	}

	/**
	 * @return the list with the item holders required to acquire this skill.
	 */
	public List<ItemHolder> getRequiredItems()
	{
		return _requiredItems;
	}

	/**
	 * Adds a required item holder to learn this skill.
	 * @param item the required item holder.
	 */
	public void addRequiredItem(ItemHolder item)
	{
		_requiredItems.add(item);
	}

	public List<SkillHolder> getPrequisiteSkills()
	{
		List<SkillHolder> prequisite = new ArrayList<>();
		List<Integer> replaceable = SkillTreesData.getInstance().getAllReplaceableSkills(this);

		if(replaceable == null)
		{
			return prequisite;
		}

		prequisite.addAll(replaceable.stream().map(skillId -> new SkillHolder(skillId, 1)).collect(Collectors.toList()));

		return prequisite;
	}

	public List<SkillHolder> getPrequisiteSkills(L2PcInstance cha, boolean skipNonExistant)
	{
		List<SkillHolder> list = new ArrayList<>();
		for(SkillHolder sk : getPrequisiteSkills())
		{
			L2Skill oldSkill = cha.getSkills().get(sk.getSkillId());
			if(oldSkill != null && oldSkill.getId() == sk.getSkillId())
			{
				list.add(new SkillHolder(sk.getSkillId(), oldSkill.getLevel()));
			}
			else if(!skipNonExistant)
			{
				list.add(new SkillHolder(sk.getSkillId(), 1));
			}
		}
		return list;
	}

	public class SubClassData
	{
		private final int slot;
		private final int lvl;

		public SubClassData(int pSlot, int pLvl)
		{
			slot = pSlot;
			lvl = pLvl;
		}

		/**
		 * @return the sub-class slot.
		 */
		public int getSlot()
		{
			return slot;
		}

		/**
		 * @return the required sub-class level.
		 */
		public int getLvl()
		{
			return lvl;
		}
	}
}