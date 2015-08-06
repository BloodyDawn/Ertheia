package dwo.gameserver.model.player.base;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ExperienceTable;

/**
 * Character Sub-Class Definition
 * <BR>
 * Used to store key information about a character's sub-class.
 *
 * @author Tempy, ANZO , Bacek
 */
public class SubClass
{
	// Применяется только для обычных сабов, для дуалкласса хардкод.
	private static final byte _maxLevel = Config.MAX_SUBCLASS_LEVEL < ExperienceTable.getInstance().getMaxLevel() ? Config.MAX_SUBCLASS_LEVEL : (byte) (ExperienceTable.getInstance().getMaxLevel() - 1);
	private static final byte _maxLevelDual = (byte) (ExperienceTable.getInstance().getMaxLevel() - 1);

	private static final byte _minLevel = 40;
	private static final byte _minLevelDual = 85;

	private PlayerClass _class;
	private long _exp;
	private int _sp;
	private byte _level;
	private int _classIndex = 1;
	private SubClassType _classType;

	public SubClass(int classId, long exp, int sp, byte level, int classIndex, SubClassType classType)
	{
		_class = PlayerClass.values()[classId];
		_exp = exp;
		_sp = sp;
		_level = level;
		_classIndex = classIndex;
		_classType = classType;
	}

	public SubClass(int classId, int classIndex)
	{
		// Used for defining a sub class using default values for XP, SP and player level.
		_class = PlayerClass.values()[classId];
		_classIndex = classIndex;
	}

	public SubClass()
	{
		// Used for specifying ALL attributes of a sub class directly,
		// using the preset default values.
	}

	public PlayerClass getClassDefinition()
	{
		return _class;
	}

	public int getClassId()
	{
		return _class.ordinal();
	}

	public void setClassId(int classId)
	{
		_class = PlayerClass.values()[classId];
	}

	public long getExp()
	{
		// Устанавливаем минимальный опыт
		if(_exp == 0)
		{
			_exp = ExperienceTable.getInstance().getExpForLevel(getMinLevel());
		}

		return _exp;
	}

	public void setExp(long expValue)
	{
		if(expValue > ExperienceTable.getInstance().getExpForLevel(isDualClass() ? _maxLevelDual + 1 : _maxLevel + 1) - 1)
		{
			expValue = ExperienceTable.getInstance().getExpForLevel(isDualClass() ? _maxLevelDual + 1 : _maxLevel + 1) - 1;
		}

		_exp = expValue;
	}

	public int getSp()
	{
		return _sp;
	}

	public void setSp(int spValue)
	{
		_sp = spValue;
	}

	public byte getMinLevel()
	{
		if(isDualClass())
		{
			return _minLevelDual;
		}
		return _minLevel;
	}

	public byte getLevel()
	{
		return _level;
	}

	public void setLevel(byte levelValue)
	{
		// Если ниже мин уровня устанавливаем минимальный уровень
		if(levelValue < getMinLevel())
		{
			levelValue = getMinLevel();
		}

		_level = levelValue;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	public void setClassIndex(int classIndex)
	{
		_classIndex = classIndex;
	}

	public void incLevel()
	{
		if(isDualClass() ? _level == _maxLevelDual : _level == _maxLevel)
		{
			return;
		}

		_level++;
		setExp(ExperienceTable.getInstance().getExpForLevel(_level));
	}

	public void decLevel()
	{
		if(_level == getMinLevel())
		{
			return;
		}

		_level--;
		setExp(ExperienceTable.getInstance().getExpForLevel(_level));
	}

	public SubClassType getClassType()
	{
		return _classType;
	}

	public void setClassType(SubClassType classType)
	{
		_classType = classType;
	}

	public boolean isDualClass()
	{
		return _classType == SubClassType.DUAL_CLASS;
	}
}