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
package dwo.gameserver.model.actor.templates;

import dwo.gameserver.datatables.xml.HerbDropTable;
import dwo.gameserver.model.actor.instance.L2XmassTreeInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.L2MinionData;
import dwo.gameserver.model.world.npc.drop.L2DropCategory;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.Quest.QuestEventType;
import dwo.gameserver.model.world.zone.Location;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class L2NpcTemplate extends L2CharTemplate
{
	protected static final Logger _log = LogManager.getLogger(L2NpcTemplate.class);

	// Базовые характеристики темплейта
	private int _npcId;
	private int _idTemplate;
	private String _type;
	private boolean _serverSideName;
	private String _title;
	private boolean _serverSideTitle;
	private byte _level;
	private float _rewardExp;
	private float _rewardSp;
	private int _rHand;
	private int _lHand;
    private int _armor;
	private int _enchantEffect;
	private int _dropHerbGroup;
	private boolean _isCustom;
	private boolean _isQuestMonster;
	private float _baseVitalityDivider;
	// АИ скилов темплейта
	private final List<L2Skill> _buffSkills = new ArrayList<>();
	private final List<L2Skill> _negativeSkills = new ArrayList<>();
	private final List<L2Skill> _debuffSkills = new ArrayList<>();
	private final List<L2Skill> _atkSkills = new ArrayList<>();
	private final List<L2Skill> _rootSkills = new ArrayList<>();
	private final List<L2Skill> _stunskills = new ArrayList<>();
	private final List<L2Skill> _sleepSkills = new ArrayList<>();
	private final List<L2Skill> _paralyzeSkills = new ArrayList<>();
	private final List<L2Skill> _fossilSkills = new ArrayList<>();
	private final List<L2Skill> _floatSkills = new ArrayList<>();
	private final List<L2Skill> _immobilizeSkills = new ArrayList<>();
	private final List<L2Skill> _healSkills = new ArrayList<>();
	private final List<L2Skill> _resSkills = new ArrayList<>();
	private final List<L2Skill> _dotSkills = new ArrayList<>();
	private final List<L2Skill> _cotSkills = new ArrayList<>();
	private final List<L2Skill> _universalSkills = new ArrayList<>();
	private final List<L2Skill> _manaSkills = new ArrayList<>();
	private final List<L2Skill> _longRangeSkills = new ArrayList<>();
	private final List<L2Skill> _shortRangeSkills = new ArrayList<>();
	private final List<L2Skill> _generalSkills = new ArrayList<>();
	private final List<L2Skill> _suicideSkills = new ArrayList<>();
	/**
	 * The table containing all Item that can be dropped by L2NpcInstance using this L2NpcTemplate
	 */
	private final List<L2DropCategory> _categories = new ArrayList<>();
	/**
	 * The table containing all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate
	 */
	private final List<L2MinionData> _minions = new ArrayList<>();
	private final List<ClassId> _teachInfo = new ArrayList<>();
	private final Map<Integer, L2Skill> _skills = new FastMap<Integer, L2Skill>().shared();
	/**
	 * Таблица, в которой содержится информация о связанных с НПЦ дверях
	 */
	private final Map<String, Integer> _doorList = new HashMap<>();
	/**
	 * Таблица, в которой содержится информация о teleport positions (pos_x1, pos_z1 и т.д.)
	 */
	private final Map<Integer, Location> _telePositionList = new HashMap<>();
	/**
	 * Contains a list of quests for each event type (questStart, questAttack, questKill, etc).
	 */
	private final Map<QuestEventType, List<Quest>> _questEvents = new FastMap<QuestEventType, List<Quest>>().shared();
	protected String _name;
	protected String _serverName;
	protected String _sex;
	private Race _race;
	// Basic AI
	private int _primarySkillId;
	private int _minskillChance;
	private int _maxskillChance;
	private boolean _canMove;
	private int _soulshot;
	private int _spiritshot;
	private int _soulshotChance;
	private int _spiritshotChance;
	private int _isChaos;
	private String _clan;
	private int _clanRange;
	private String _enemyClan;
	private int _enemyRange;
	private int _dodge;
	private int _longRangeSkill;
	private int _shortRangeSkill;
	private int _longRangeChance;
	private int _shortRangeChance;
	private AIType _aiType = AIType.FIGHTER;
	private boolean _showName;
	private boolean _targetable;
	private int _aggroRange;
	// Анимация при idle режиме
	private int[] _idleActionIds;
	private SkillHolder _idleSkillId;

	/**
	 * Constructor of L2Character.<BR><BR>
	 *
	 * @param set The StatsSet object to transfer data to the method
	 *
	 */
    public L2NpcTemplate(StatsSet set)
    {
        super(set);
        updateL2NpcTemplate(set);
    }

    public void updateL2NpcTemplate(StatsSet set)
    {
        _npcId = set.getInteger("npcId");
        _idTemplate = set.getInteger("idTemplate");
        _type = set.getString("type", "L2Npc");
        _name = set.getString("name");
        _serverName = set.getString("server_name");
        _serverSideName = set.getBool("server_side_name", false);
        _title = set.getString("title");
        _isQuestMonster = _title.equalsIgnoreCase("Квестовый Монстр");
        _serverSideTitle = set.getBool("server_side_title", false);
        _sex = set.getString("sex", "etc");
        _level = set.getByte("level", (byte) 99);
        _rewardExp = set.getLong("exp", 0);
        _rewardSp = set.getLong("sp", 0);
        _rHand = set.getInteger("slot_rhand", 0);
        _lHand = set.getInteger("slot_lhand", 0);
        _armor = set.getInteger("slot_armor", 0);
        _enchantEffect = set.getInteger("enchant", 0);
        _race = Race.values()[set.getInteger("raceId", 0)];

        // npcAi
        _primarySkillId = set.getInteger("primary_skill_id", 0);
        _minskillChance = set.getInteger("min_skill_chance", 7);
        _maxskillChance = set.getInteger("max_skill_chance", 15);
        _canMove = set.getBool("can_move", true);
        _soulshot = set.getInteger("soulshot_count", 0);
        _spiritshot = set.getInteger("spiritshot_count", 0);
        _soulshotChance = set.getInteger("use_soulshot_chance", 0);
        _spiritshotChance = set.getInteger("use_spiritshot_chance", 0);
        _isChaos = set.getInteger("is_chaos", 0);
        _clanRange = set.getInteger("clan_range", 0);
        _enemyRange = set.getInteger("enemy_range", 0);
        _dodge = set.getInteger("dodge", 0);
        _longRangeSkill = set.getInteger("max_range_skill", 0);
        _shortRangeSkill = set.getInteger("min_range_skill", 0);
        _longRangeChance = set.getInteger("max_range_chance", 0);
        _shortRangeChance = set.getInteger("min_range_chance", 0);
        _showName = set.getBool("show_name_tag", true);
        _targetable = set.getBool("targetable", true);
        _aggroRange = set.getInteger("agro_range", 0);
        setClan(set.getString("clan", null));
        setEnemyClan(set.getString("enemy_clan", null));
        setAi(set.getString("ai_type", "fighter"));

        _idleActionIds = set.getIntegerArray("idle_action_ids", new int[]{1, 2});

        _idleSkillId = set.getInteger("idle_skill_id", 0) != 0 ? new SkillHolder(set.getInteger("idle_skill_id"), set.getInteger("idle_skill_level")) : null;

        int herbGroup = set.getInteger("drop_herb_group", 0);
        if(herbGroup > 0 && HerbDropTable.getInstance().getHerbDroplist(herbGroup) == null)
        {
            _log.log(Level.WARN, "Missing Herb Drop Group for npcId: " + _npcId);
            _dropHerbGroup = 0;
        }
        else
        {
            _dropHerbGroup = herbGroup;
        }

        // TODO: Could be loaded from db.
        _baseVitalityDivider = _level > 0 && _rewardExp > 0 ? getBaseHpMax() * 9 * _level * _level / (100 * _rewardExp) : 0;

        _isCustom = _npcId != _idTemplate;
    }

	public static boolean isAssignableTo(Class<?> sub, Class<?> clazz)
	{
		// If clazz represents an interface
		if(clazz.isInterface())
		{
			// check if obj implements the clazz interface
			Class<?>[] interfaces = sub.getInterfaces();
			for(Class<?> interface1 : interfaces)
			{
				if(clazz.getName().equals(interface1.getName()))
				{
					return true;
				}
			}
		}
		else
		{
			do
			{
				if(sub.getName().equals(clazz.getName()))
				{
					return true;
				}

				sub = sub.getSuperclass();
			}
			while(sub != null);
		}
		return false;
	}

	/**
	 * Checks if obj can be assigned to the Class represented by clazz.<br>
	 * This is true if, and only if, obj is the same class represented by clazz, or a subclass of it or obj implements the interface represented by clazz.
	 * @param obj
	 * @param clazz
	 * @return
	 */
	public static boolean isAssignableTo(Object obj, Class<?> clazz)
	{
		return isAssignableTo(obj.getClass(), clazz);
	}

	private void setAi(String ai)
	{
		if(ai.equalsIgnoreCase("archer"))
		{
			_aiType = AIType.ARCHER;
		}
		else if(ai.equalsIgnoreCase("balanced"))
		{
			_aiType = AIType.BALANCED;
		}
		else if(ai.equalsIgnoreCase("mage"))
		{
			_aiType = AIType.MAGE;
		}
		else if(ai.equalsIgnoreCase("healer"))
		{
			_aiType = AIType.HEALER;
		}
		else
		{
			_aiType = ai.equalsIgnoreCase("corpse") ? AIType.CORPSE : AIType.FIGHTER;
		}
	}

	public void addAtkSkill(L2Skill skill)
	{
		_atkSkills.add(skill);
	}

	public void addBuffSkill(L2Skill skill)
	{
		_buffSkills.add(skill);
	}

	public void addCOTSkill(L2Skill skill)
	{
		_cotSkills.add(skill);
	}

	public void addDebuffSkill(L2Skill skill)
	{
		_debuffSkills.add(skill);
	}

	public void addDOTSkill(L2Skill skill)
	{
		_dotSkills.add(skill);
	}

	public void setDropCategoryChance(int catType, int chance)
	{
		L2DropCategory category = null;
		for(L2DropCategory cat : _categories)
		{
			if(cat.getCategoryType() == catType)
			{
				category = cat;
				break;
			}
		}

		if(category == null)
		{
			category = new L2DropCategory(catType);
			_categories.add(category);
		}

		category.setCategoryChance(chance, isType("L2RaidBoss") || isType("L2GrandBoss"));
	}

	/**
	 * Add a drop to a given category.<br>
	 * If the category does not exist, create it.
	 * @param drop
	 * @param categoryType
	 */
	public void addDropData(L2DropData drop, int categoryType)
	{
		if(!drop.isQuestDrop())
		{
			// If the category doesn't already exist, create it first
			synchronized(_categories)
			{
				boolean catExists = false;
				for(L2DropCategory cat : _categories)
				{
					// If the category exists, add the drop to this category.
					if(cat.getCategoryType() == categoryType)
					{
						cat.addDropData(drop, isType("L2RaidBoss") || isType("L2GrandBoss"));
						catExists = true;
						break;
					}
				}
				// If the category doesn't exit, create it and add the drop
				if(!catExists)
				{
					L2DropCategory cat = new L2DropCategory(categoryType);
					cat.addDropData(drop, isType("L2RaidBoss") || isType("L2GrandBoss"));
					if(!cat.validate())
					{
						_log.log(Level.WARN, "Problems with rewardlist for npc: " + _npcId);
					}
					_categories.add(cat);
				}
			}
		}
	}

	public void addFloatSkill(L2Skill skill)
	{
		_floatSkills.add(skill);
	}

	public void addFossilSkill(L2Skill skill)
	{
		_fossilSkills.add(skill);
	}

	public void addGeneralSkill(L2Skill skill)
	{
		_generalSkills.add(skill);
	}

	public void addHealSkill(L2Skill skill)
	{
		_healSkills.add(skill);
	}

	public void addImmobiliseSkill(L2Skill skill)
	{
		_immobilizeSkills.add(skill);
	}

	public void addManaHealSkill(L2Skill skill)
	{
		_manaSkills.add(skill);
	}

	public void addNegativeSkill(L2Skill skill)
	{
		_negativeSkills.add(skill);
	}

	public void addParalyzeSkill(L2Skill skill)
	{
		_paralyzeSkills.add(skill);
	}

	public void addQuestEvent(QuestEventType EventType, Quest q)
	{
		if(_questEvents.containsKey(EventType))
		{
			List<Quest> quests = _questEvents.get(EventType);
			if(!EventType.isMultipleRegistrationAllowed() && !quests.isEmpty())
			{
				_log.log(Level.WARN, "Quest event not allowed in multiple quests.  Skipped addition of Event Type \"" + EventType + "\" for NPC \"" + _name + " (ID: " + _npcId + " ) " + "\" and quest \"" + q.getName() + "\".");
			}
			else
			{
				quests.add(q);
			}
		}
		else
		{
			List<Quest> quests = new ArrayList<>();
			quests.add(q);
			_questEvents.put(EventType, quests);
		}
	}

	public void removeQuest(Quest q)
	{
		_questEvents.entrySet().stream().filter(entry -> entry.getValue().contains(q)).forEach(entry -> {
			Iterator<Quest> it = entry.getValue().iterator();
			while(it.hasNext())
			{
				Quest q1 = it.next();
				if(q1.equals(q))
				{
					it.remove();
				}
			}

			if(entry.getValue().isEmpty())
			{
				_questEvents.remove(entry.getKey());
			}
		});
	}

	public void addRaidData(L2MinionData minion)
	{
		_minions.add(minion);
	}

	public void addRangeSkill(L2Skill skill)
	{
		if(skill.getCastRange() <= 150 && skill.getCastRange() > 0)
		{
			_shortRangeSkills.add(skill);
		}
		else if(skill.getCastRange() > 150)
		{
			_longRangeSkills.add(skill);
		}
	}

	public void addResSkill(L2Skill skill)
	{
		_resSkills.add(skill);
	}

	public void addRootSkill(L2Skill skill)
	{
		_rootSkills.add(skill);
	}

	public void addSkill(L2Skill skill)
	{
		if(!skill.isPassive())
		{
			if(skill.isSuicideAttack())
			{
				addSuicideSkill(skill);
			}
			else
			{
				addGeneralSkill(skill);
				switch(skill.getSkillType())
				{
					case BUFF:
						addBuffSkill(skill);
						break;
					case HEAL:
					case HOT:
					case HEAL_PERCENT:
					case HEAL_STATIC:
					case BALANCE_LIFE:
					case HEAL_COHERENTLY:
					case MANA_BY_LEVEL:
						addHealSkill(skill);
						break;
					case RESURRECT:
						addResSkill(skill);
						break;
					case DEBUFF:
						addDebuffSkill(skill);
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					case ROOT:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case SLEEP:
						addSleepSkill(skill);
						addImmobiliseSkill(skill);
						break;
					case STUN:
						addRootSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PARALYZE:
						addParalyzeSkill(skill);
						addImmobiliseSkill(skill);
						addRangeSkill(skill);
						break;
					case PDAM:
					case MDAM:
					case BLOW:
					case DRAIN:
					case CHARGEDAM:
					case FATAL:
					case DEATHLINK:
					case CPDAM:
					case MANADAM:
					case CPDAMPERCENT:
						addAtkSkill(skill);
						addUniversalSkill(skill);
						addRangeSkill(skill);
						break;
					case POISON:
					case DOT:
					case MDOT:
					case BLEED:
						addDOTSkill(skill);
						addRangeSkill(skill);
						break;
					case MUTE:
					case FEAR:
						addCOTSkill(skill);
						addRangeSkill(skill);
						break;
					case CANCEL:
					case NEGATE:
						addNegativeSkill(skill);
						addRangeSkill(skill);
						break;
					default:
						addUniversalSkill(skill);
						break;
				}
			}
		}

		_skills.put(skill.getId(), skill);
	}

	public void addSleepSkill(L2Skill skill)
	{
		_sleepSkills.add(skill);
	}

	public void addStunSkill(L2Skill skill)
	{
		_stunskills.add(skill);
	}

	public void addSuicideSkill(L2Skill skill)
	{
		_suicideSkills.add(skill);
	}

	public void addTeachInfo(ClassId classId)
	{
		_teachInfo.add(classId);
	}

	public void addUniversalSkill(L2Skill skill)
	{
		_universalSkills.add(skill);
	}

	public boolean canTeach(ClassId classId)
	{
		// If the player is on a third class, fetch the class teacher
		// information for its parent class.
		if(classId.level() == 3)
		{
			return _teachInfo.contains(classId.getParent());
		}

		return _teachInfo.contains(classId);
	}

	/***
	 * Добавляет в список связанных с NPC дверей указанный ObjectID двери по указанному индексу
	 * @param doorName имя двери
	 * @param doorId ObjectID двери
	 */
	public void addDoor(String doorName, int doorId)
	{
		_doorList.put(doorName, doorId);
	}

	/***
	 * @return список ObjectID дверей, связанных с текущим темплейтом
	 */
	public Collection<Integer> getDoorList()
	{
		return _doorList.values();
	}

	/***
	 * @param doorName имя двери
	 * @return ObjectID двери по указанному индексу в списке
	 */
	public Integer getDoorId(String doorName)
	{
		return _doorList.get(doorName);
	}

	/***
	 * Добавляет точку телепортации, в которую может попасть игрок от NPC
	 * @param teleLoc координаты точки для телепортации
	 */
	public void addTelePosition(Location teleLoc)
	{
		_telePositionList.put(teleLoc.getId(), teleLoc);
	}

	/***
	 * @param index индекс точки телепортации
	 * @return Location точки телепортации
	 */
	public Location getTelePosition(int index)
	{
		return _telePositionList.get(index);
	}

	/**
	 * Empty all possible drops of this L2NpcTemplate.<BR><BR>
	 */
	public void clearAllDropData()
	{
		synchronized(this)
		{
			for(L2DropCategory cat : _categories)
			{
				cat.clearAllDrops();
			}
			_categories.clear();
		}
	}

	/**
	 * @return the list of all possible item drops of this L2NpcTemplate.<br>
	 *         (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)
	 */
	public List<L2DropData> getAllDropData()
	{
		List<L2DropData> list = new ArrayList<>();
		for(L2DropCategory tmp : _categories)
		{
			list.addAll(tmp.getItems());
		}
		return list;
	}

	/**
	 * @return the attack skills.
	 */
	public List<L2Skill> getAtkSkills()
	{
		return _atkSkills;
	}

	/**
	 * @return the base vitality divider value.
	 */
	public float getBaseVitalityDivider()
	{
		return _baseVitalityDivider;
	}

	/**
	 * @return the buff skills.
	 */
	public List<L2Skill> getBuffSkills()
	{
		return _buffSkills;
	}

	/**
	 * @return the cost over time skills.
	 */
	public List<L2Skill> getCostOverTimeSkills()
	{
		return _cotSkills;
	}

	/**
	 * @return the debuff skills.
	 */
	public List<L2Skill> getDebuffSkills()
	{
		return _debuffSkills;
	}

	/**
	 * @return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.
	 */
	public List<L2DropCategory> getDropData()
	{
		return _categories;
	}

	/**
	 * @return the drop herb group.
	 */
	public int getDropHerbGroup()
	{
		return _dropHerbGroup;
	}

	/**
	 * @return the enchant effect.
	 */
	public int getEnchantEffect()
	{
		return _enchantEffect;
	}

	public Map<QuestEventType, List<Quest>> getEventQuests()
	{
		return _questEvents;
	}

	public List<Quest> getEventQuests(QuestEventType EventType)
	{
		return _questEvents.get(EventType);
	}

	/**
	 * @return the general skills.
	 */
	public List<L2Skill> getGeneralskills()
	{
		return _generalSkills;
	}

	/**
	 * @return the heal skills.
	 */
	public List<L2Skill> getHealSkills()
	{
		return _healSkills;
	}

	/**
	 * @return the Id template.
	 */
	public int getIdTemplate()
	{
		return _idTemplate;
	}

	/**
	 * @return the immobilize skills.
	 */
	public List<L2Skill> getImmobiliseSkills()
	{
		return _immobilizeSkills;
	}

	/**
	 * @return the left hand item.
	 */
	public int getLeftHand()
	{
		return _lHand;
	}

	/**
	 * @return the NPC level.
	 */
	public byte getLevel()
	{
		return _level;
	}

	/**
	 * @return the long range skills.
	 */
	public List<L2Skill> getLongRangeSkills()
	{
		return _longRangeSkills;
	}

	/**
	 * @return the list of all Minions that must be spawn with the L2NpcInstance using this L2NpcTemplate.
	 */
	public List<L2MinionData> getMinionData()
	{
		return _minions;
	}

	/**
	 * @return the NPC name.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * TODO: Перенести все имена в свойства НПЦ в датапаке
	 * @return серверное имя НПЦ
	 */
	public String getServerName()
	{
		return _serverName;
	}

	/**
	 * @return the negative skills.
	 */
	public List<L2Skill> getNegativeSkills()
	{
		return _negativeSkills;
	}

	/**
	 * @return the npc Id.
	 */
	public int getNpcId()
	{
		return _npcId;
	}

	/**
	 * @return the NPC race.
	 */
	public Race getRace()
	{
		return _race;
	}

	/**
	 * @return the resurrection skills.
	 */
	public List<L2Skill> getResSkills()
	{
		return _resSkills;
	}

	/**
	 * @return the reward Exp.
	 */
	public float getRewardExp()
	{
		return _rewardExp;
	}

	/**
	 * @return the reward SP.
	 */
	public float getRewardSp()
	{
		return _rewardSp;
	}

	/**
	 * @return the right hand weapon.
	 */
	public int getRightHand()
{
    return _rHand;
}

    public int getArmor()
    {
        return _armor;
    }

	/**
	 * @return the NPC sex.
	 */
	public String getSex()
	{
		return _sex;
	}

	/**
	 * @return the short range skills.
	 */
	public List<L2Skill> getShortRangeSkills()
	{
		return _shortRangeSkills;
	}

	public List<L2Skill> getSuicideSkills()
	{
		return _suicideSkills;
	}

	public List<ClassId> getTeachInfo()
	{
		return _teachInfo;
	}

	/**
	 * @return the NPC title.
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * @return the NPC type.
	 */
	public String getType()
	{
		return _type;
	}

	/**
	 * @return the universal skills.
	 */
	public List<L2Skill> getUniversalSkills()
	{
		return _universalSkills;
	}

	/**
	 * @return {@code true} if the NPC is custom, {@code false} otherwise.
	 */
	public boolean isCustom()
	{
		return _isCustom;
	}

	/**
	 * @return {@code true} if the NPC is a quest monster, {@code false} otherwise.
	 */
	public boolean isQuestMonster()
	{
		return _isQuestMonster;
	}

	/**
	 * @return {@code true} if the NPC uses server side name, {@code false} otherwise.
	 */
	public boolean isServerSideName()
	{
		return _serverSideName;
	}

	/**
	 * @return {@code true} if the NPC uses server side title, {@code false} otherwise.
	 */
	public boolean isServerSideTitle()
	{
		return _serverSideTitle;
	}

	/**
	 * @return {@code true} if the NPC is Christmas Special Tree, {@code false} otherwise.
	 */
	public boolean isSpecialTree()
	{
		return _npcId == L2XmassTreeInstance.SPECIAL_TREE_ID;
	}

	/**
	 * Checks types, ignore case.
	 * @param t the type to check.
	 * @return {@code true} if the type are the same, {@code false} otherwise.
	 */
	public boolean isType(String t)
	{
		return _type.equalsIgnoreCase(t);
	}

	/**
	 * @return {@code true} if the NPC is an undead, {@code false} otherwise.
	 */
	public boolean isUndead()
	{
		return _race == Race.UNDEAD;
	}

	@Override
	public void setBasicElementals(StatsSet set)
	{
		setBaseFireRes(set.getInteger("baseFireRes", 20));
		setBaseWindRes(set.getInteger("baseWindRes", 20));
		setBaseWaterRes(set.getInteger("baseWaterRes", 20));
		setBaseEarthRes(set.getInteger("baseEarthRes", 20));
		setBaseHolyRes(set.getInteger("baseHolyRes", 20));
		setBaseDarkRes(set.getInteger("baseDarkRes", 20));
		setBaseFire(set.getInteger("baseFire", 0));
		setBaseWind(set.getInteger("baseWind", 0));
		setBaseWater(set.getInteger("baseWater", 0));
		setBaseEarth(set.getInteger("baseEarth", 0));
		setBaseHoly(set.getInteger("baseHoly", 0));
		setBaseDark(set.getInteger("baseDark", 0));
	}

	@Override
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}

	public int getPrimarySkillId()
	{
		return _primarySkillId;
	}

	public int getMinSkillChance()
	{
		return _minskillChance;
	}

	public int getMaxSkillChance()
	{
		return _maxskillChance;
	}

	public boolean getCanMove()
	{
		return _canMove;
	}

	public int getSoulShot()
	{
		return _soulshot;
	}

	public int getSpiritShot()
	{
		return _spiritshot;
	}

	public int getSoulShotChance()
	{
		return _soulshotChance;
	}

	public int getSpiritShotChance()
	{
		return _spiritshotChance;
	}

	public int getShortRangeSkill()
	{
		return _shortRangeSkill;
	}

	public int getShortRangeChance()
	{
		return _shortRangeChance;
	}

	public int getLongRangeSkill()
	{
		return _longRangeSkill;
	}

	public int getLongRangeChance()
	{
		return _longRangeChance;
	}

	public int getIsChaos()
	{
		return _isChaos;
	}

	public String getClan()
	{
		return _clan;
	}

	private void setClan(String clan)
	{
		if(clan != null && !clan.isEmpty() && !clan.equalsIgnoreCase("null"))
		{
			_clan = clan.intern();
		}
	}

	public int getClanRange()
	{
		return _clanRange;
	}

	public String getEnemyClan()
	{
		return _enemyClan;
	}

	private void setEnemyClan(String enemyClan)
	{
		if(enemyClan != null && !enemyClan.isEmpty() && !enemyClan.equalsIgnoreCase("null"))
		{
			_enemyClan = enemyClan.intern();
		}
	}

	public int getEnemyRange()
	{
		return _enemyRange;
	}

	public int getDodge()
	{
		return _dodge;
	}

	public AIType getAiType()
	{
		return _aiType;
	}

	/**
	 * @return {@code true} if the NPC name should shows above NPC, {@code false} otherwise.
	 */
	public boolean showName()
	{
		return _showName;
	}

	/**
	 * @return {@code true} if the NPC can be targeted, {@code false} otherwise.
	 */
	public boolean isTargetable()
	{
		return _targetable;
	}

	public int getAggroRange()
	{
		return _aggroRange;
	}

	public int[] getIdleRandomActionIds()
	{
		return _idleActionIds;
	}

	public SkillHolder getIdleRandomSkillId()
	{
		return _idleSkillId;
	}

	public static enum AIType
	{
		FIGHTER,
		ARCHER,
		BALANCED,
		MAGE,
		HEALER,
		CORPSE
	}

	public static enum Race
	{
		NONE,
		UNDEAD,
		MAGICCREATURE,
		BEAST,
		ANIMAL,
		PLANT,
		HUMANOID,
		SPIRIT,
		ANGEL,
		DEMON,
		DRAGON,
		GIANT,
		BUG,
		FAIRIE,
		HUMAN,
		ELVE,
		DARKELVE,
		ORC,
		DWARVE,
		OTHER,
		NONLIVING,
		SIEGEWEAPON,
		DEFENDINGARMY,
		MERCENARIE,
		UNKNOWN,
		KAMAEL,
        NEW
	}
}