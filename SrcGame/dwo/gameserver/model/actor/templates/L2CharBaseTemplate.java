package dwo.gameserver.model.actor.templates;

import dwo.gameserver.model.player.base.ClassType;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.base.Sex;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;
import java.util.Map;

/**
 * Базовые шаблоны для персонажей. Описывает основные характеристики базовых классов (например, Human Fighter, Elven Mystic и т.п.).
 * Каждому игровому классу ставится в соответствие один из базовых шаблонов.
 *
 * @author Yorie
 */
public class L2CharBaseTemplate
{
	CreationData _creationData;
	DefaultAttributes _defaultAttributes;
	BaseStats _stats;
	LevelData _levelData;
	private Race _race;
	private ClassType _type;

	public L2CharBaseTemplate(Race race, ClassType type)
	{
		_race = race;
		_type = type;
		_creationData = new CreationData();
		_levelData = new LevelData();
	}

	public Race getRace()
	{
		return _race;
	}

	public ClassType getClassType()
	{
		return _type;
	}

	public CreationData getCreationData()
	{
		return _creationData;
	}

	public DefaultAttributes getDefaultAttributes()
	{
		return _defaultAttributes;
	}

	public void setDefaultAttributes(DefaultAttributes attributes)
	{
		_defaultAttributes = attributes;
	}

	public void setBaseStats(BaseStats stats)
	{
		_stats = stats;
	}

	public BaseStats getStats()
	{
		return _stats;
	}

	public LevelData getLevelData()
	{
		return _levelData;
	}

	/**
	 * Данные создания персонажа.
	 */
	public static class CreationData
	{
		List<StartPoint> _startPoints = new FastList<>();

		/**
		 * Добавляет стартовую точку для нового персонажа.
		 *
		 * @param point Стартовая точка.
		 */
		public void addStartPoint(StartPoint point)
		{
			_startPoints.add(point);
		}

		/**
		 * Добавляет стартовую точку для нового персонажа.
		 *
		 * @param x X
		 * @param y Y
		 * @param z Z
		 */
		public void addStartPoint(int x, int y, int z)
		{
			addStartPoint(new StartPoint(x, y, z));
		}

		public StartPoint getRandomStartPoint()
		{
			return _startPoints.get(Rnd.get(0, _startPoints.size() - 1));
		}

		/**
		 * Стартовая точка респауна нового персонажа.
		 */
		public static class StartPoint
		{
			private int _x;
			private int _y;
			private int _z;

			public StartPoint(int x, int y, int z)
			{
				_x = x;
				_y = y;
				_z = z;
			}

			public int x()
			{
				return _x;
			}

			public int y()
			{
				return _y;
			}

			public int z()
			{
				return _z;
			}
		}
	}

	/**
	 * Класс, описывающий набор атрибутов персонажа по умолчанию и ограничения по атрибутам.
	 */
	public static class DefaultAttributes
	{
		private AttributeSet _min;
		private AttributeSet _max;
		private AttributeSet _base;
		private DefenseSet _defense;

		public DefaultAttributes(AttributeSet base, AttributeSet min, AttributeSet max, DefenseSet defense)
		{
			_base = base;
			_min = min;
			_max = max;
			_defense = defense;
		}

		public AttributeSet base()
		{
			return _base;
		}

		public AttributeSet min()
		{
			return _min;
		}

		public AttributeSet max()
		{
			return _max;
		}

		public DefenseSet defense()
		{
			return _defense;
		}

		/**
		 * Набор базовых характеристик персонажа.
		 */
		public static class AttributeSet
		{
			private int _int;
			private int _wit;
			private int _men;
			private int _str;
			private int _dex;
			private int _con;
            private int _luc;
            private int _cha;

			public AttributeSet(int _int, int wit, int men, int str, int dex, int con, int luc, int cha)
			{
				this._int = _int;
				_wit = wit;
				_men = men;
				_str = str;
				_dex = dex;
				_con = con;
                _luc = luc;
                _cha = cha;
			}

			public int getInt()
			{
				return _int;
			}

			public int getWit()
			{
				return _wit;
			}

			public int getMen()
			{
				return _men;
			}

			public int getStr()
			{
				return _str;
			}

			public int getDex()
			{
				return _dex;
			}

			public int getCon()
			{
				return _con;
			}

            public int getLuc()
            {
                return _luc;
            }

            public int getCha()
            {
                return _cha;
            }
		}

		/**
		 * Стандартные показатели защиты брони, когда персонаж раздет.
		 */
		public static class DefenseSet
		{
			private int _chest;
			private int _legs;
			private int _helmet;
			private int _boots;
			private int _gloves;
			private int _underwear;
			private int _cloak;

			private int _rRing;
			private int _lRing;
			private int _rEarring;
			private int _lEarring;
			private int _necklace;

			public DefenseSet(int chest, int legs, int helmet, int boots, int gloves, int underwear, int cloak, int rRing, int lRing, int rEarring, int lEarring, int necklace)
			{
				_chest = chest;
				_legs = legs;
				_helmet = helmet;
				_boots = boots;
				_gloves = gloves;
				_underwear = underwear;
				_cloak = cloak;

				_rRing = rRing;
				_lRing = lRing;
				_rEarring = rEarring;
				_lEarring = lEarring;
				_necklace = necklace;
			}

			public int getChest()
			{
				return _chest;
			}

			public int getLegs()
			{
				return _legs;
			}

			public int getHelmet()
			{
				return _helmet;
			}

			public int getBoots()
			{
				return _boots;
			}

			public int getGloves()
			{
				return _gloves;
			}

			public int getUnderwear()
			{
				return _underwear;
			}

			public int getCloak()
			{
				return _cloak;
			}

			public int getRightRing()
			{
				return _rRing;
			}

			public int getLeftRing()
			{
				return _lRing;
			}

			public int getRightEarring()
			{
				return _rEarring;
			}

			public int getLeftEarring()
			{
				return _lEarring;
			}

			public int getNecklace()
			{
				return _necklace;
			}
		}
	}

	/**
	 * Статические данные базовых классов по уровням.
	 */
	public static class LevelData
	{
		private Map<Integer, Double> _hpRegen = new FastMap<>();
		private Map<Integer, Double> _mpRegen = new FastMap<>();
		private Map<Integer, Double> _cpRegen = new FastMap<>();

		public void set(LevelDataType type, int level, Double value)
		{
			switch(type)
			{
				case HP_REGEN:
					_hpRegen.put(level, value);
					break;
				case MP_REGEN:
					_mpRegen.put(level, value);
					break;
				case CP_REGEN:
					_cpRegen.put(level, value);
					break;
			}
		}

		public double get(LevelDataType type, int level)
		{
			switch(type)
			{
				case HP_REGEN:
					return _hpRegen.containsKey(level) ? _hpRegen.get(level) : 0.0;
				case MP_REGEN:
					return _mpRegen.containsKey(level) ? _mpRegen.get(level) : 0.0;
				case CP_REGEN:
					return _cpRegen.containsKey(level) ? _cpRegen.get(level) : 0.0;
			}

			return 0.0;
		}

		public static enum LevelDataType
		{
			HP_REGEN,
			MP_REGEN,
			CP_REGEN
		}
	}

	public static class BaseStats
	{
		// Sex-dependent
		private final Map<Integer, Double> _collisionRadius = new FastMap<>(2);
		private final Map<Integer, Double> _collisionHeight = new FastMap<>(2);
		private final Map<Integer, Integer> _safeFallHeight = new FastMap<>(2);

		private final int _pAtk;
		private final int _mAtk;
		private final int _critRate;
		private final int _mCritRate;
		private final int _atkSpd;
		private final int _castSpd;
		private final int _atkRange;
		private final int _randDamage;
		private final int _breathBonus;
		private final int _walkSpd;
		private final int _runSpd;
		private final int _waterWalkSpd;
		private final int _waterRunSpd;
		private final int _flyWalkSpd;
		private final int _flyRunSpd;
		private final int _rideWalkSpd;
		private final int _rideRunSpd;
		private final String _attackType;

		/**
		 * @param baseSet Набор атрибутов для всех полов.
		 * @param maleSet Набор атрибутов мужчин.
		 * @param femaleSet Набор атрибутов женщин.
		 */
		public BaseStats(StatsSet baseSet, StatsSet maleSet, StatsSet femaleSet)
		{
			_collisionRadius.put(Sex.MALE, baseSet.contains("collisionRadius") ? baseSet.getDouble("collisionRadius") : maleSet.getDouble("collisionRadius"));
			_collisionRadius.put(Sex.FEMALE, baseSet.contains("collisionRadius") ? baseSet.getDouble("collisionRadius") : femaleSet.getDouble("collisionRadius"));
			_collisionHeight.put(Sex.MALE, baseSet.contains("collisionHeight") ? baseSet.getDouble("collisionHeight") : maleSet.getDouble("collisionHeight"));
			_collisionHeight.put(Sex.FEMALE, baseSet.contains("collisionHeight") ? baseSet.getDouble("collisionHeight") : femaleSet.getDouble("collisionHeight"));
			_safeFallHeight.put(Sex.MALE, baseSet.contains("baseSafeFallHeight") ? baseSet.getInteger("baseSafeFallHeight") : maleSet.getInteger("baseSafeFallHeight"));
			_safeFallHeight.put(Sex.FEMALE, baseSet.contains("baseSafeFallHeight") ? baseSet.getInteger("baseSafeFallHeight") : femaleSet.getInteger("baseSafeFallHeight"));

			_pAtk = baseSet.getInteger("basePAtk");
			_mAtk = baseSet.getInteger("baseMAtk");
			_attackType = baseSet.getString("baseAttackType");
			_critRate = baseSet.getInteger("basePCritRate");
			_mCritRate = baseSet.getInteger("baseMCritRate");
			_atkSpd = baseSet.getInteger("basePAtkSpd");
			_castSpd = baseSet.getInteger("baseMAtkSpd");
			_atkRange = baseSet.getInteger("baseAtkRange");
			_randDamage = baseSet.getInteger("baseRandDam");
			_breathBonus = baseSet.getInteger("baseBreathBonus");
			_walkSpd = baseSet.getInteger("baseWalkSpd");
			_runSpd = baseSet.getInteger("baseRunSpd");
			_waterWalkSpd = baseSet.getInteger("baseWaterWalkSpd");
			_waterRunSpd = baseSet.getInteger("baseWaterRunSpd");
			_flyWalkSpd = baseSet.getInteger("baseFlyWalkSpd");
			_flyRunSpd = baseSet.getInteger("baseFlyRunSpd");
			_rideWalkSpd = baseSet.getInteger("baseRideWalkSpd");
			_rideRunSpd = baseSet.getInteger("baseRideRunSpd");
		}

		public double getCollisionRadius(int sex)
		{
			return _collisionRadius.containsKey(sex) ? _collisionRadius.get(sex) : 10;
		}

		public double getCollisionHeight(int sex)
		{
			return _collisionHeight.containsKey(sex) ? _collisionHeight.get(sex) : 10;
		}

		public int getSafeFallHeight(int sex)
		{
			return _safeFallHeight.containsKey(sex) ? _safeFallHeight.get(sex) : 333;
		}

		public int getPAtk()
		{
			return _pAtk;
		}

		public int getMAtk()
		{
			return _mAtk;
		}

		public String getAttackType()
		{
			return _attackType;
		}

		public int getCritRate()
		{
			return _critRate;
		}

		public int getMCritRate()
		{
			return _mCritRate;
		}

		public int getAtkSpd()
		{
			return _atkSpd;
		}

		public int getCastSpd()
		{
			return _castSpd;
		}

		public int getAttackRange()
		{
			return _atkRange;
		}

		public int getRandomDamage()
		{
			return _randDamage;
		}

		public int getBreathBonus()
		{
			return _breathBonus;
		}

		public int getWalkSpd()
		{
			return _walkSpd;
		}

		public int getRunSpd()
		{
			return _runSpd;
		}

		public int getWaterWalkSpd()
		{
			return _waterWalkSpd;
		}

		public int getWaterRunSpd()
		{
			return _waterRunSpd;
		}

		public int getFlyWalkSpd()
		{
			return _flyWalkSpd;
		}

		public int getFlyRunSpd()
		{
			return _flyRunSpd;
		}

		public int getRideWalkSpd()
		{
			return _rideWalkSpd;
		}

		public int getRideRunSpd()
		{
			return _rideRunSpd;
		}
	}
}
