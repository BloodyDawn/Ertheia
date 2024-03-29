package dwo.gameserver.network.game.components;

import dwo.gameserver.model.actor.controller.player.CharacterVariablesController;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.items.itemcontainer.PcInventory;

/**
 * Used to Store data sent to Client for Character
 * Selection screen.
 */
public class CharSelectInfoPackage
{
	private String _name;
	private int _objectId;
	private int _charId = 0x00030b7a;
	private long _exp;
	private int _sp;
	private int _clanId;
	private int _race;
	private int _classId;
	private int _baseClassId;
	private long _deleteTimer;
	private long _lastAccess;
	private int _face;
	private int _hairStyle;
	private int _hairColor;
	private int _sex;
	private int _level = 1;
	private int _maxHp;
	private double _currentHp;
	private int _maxMp;
	private double _currentMp;
	private int[][] _paperdoll;
	private int _karma;
	private int _pkKills;
	private int _pvpKills;
	private int _augmentationId;
	private int _transformId;
	private int _x;
	private int _y;
	private int _z;
    private CharacterVariablesController _vars;
    private int _accessLevel;

    /**
	 * @param objectId
	 * @param name
	 */
	public CharSelectInfoPackage(int objectId, String name)
	{
		_objectId = objectId;
		_name = name;
		_paperdoll = PcInventory.restoreVisibleInventory(objectId);
	}

    public int getAccessLevel()
    {
        return _accessLevel;
    }

    public void setAccessLevel(int level)
    {
        _accessLevel = level;
    }

	public int getObjectId()
	{
		return _objectId;
	}

	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}

	public int getCharId()
	{
		return _charId;
	}

	public void setCharId(int charId)
	{
		_charId = charId;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}

	public int getClassId()
	{
		return _classId;
	}

	public void setClassId(int classId)
	{
		_classId = classId;
	}

	public int getBaseClassId()
	{
		return _baseClassId;
	}

	public void setBaseClassId(int baseClassId)
	{
		_baseClassId = baseClassId;
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public void setCurrentHp(double currentHp)
	{
		_currentHp = currentHp;
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public void setCurrentMp(double currentMp)
	{
		_currentMp = currentMp;
	}

	public long getDeleteTimer()
	{
		return _deleteTimer;
	}

	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	public void setLastAccess(long lastAccess)
	{
		_lastAccess = lastAccess;
	}

	public long getExp()
	{
		return _exp;
	}

	public void setExp(long exp)
	{
		_exp = exp;
	}

	public int getFace()
	{
		return _face;
	}

	public void setFace(int face)
	{
		_face = face;
	}

	public int getHairColor()
	{
		return _hairColor;
	}

	public void setHairColor(int hairColor)
	{
		_hairColor = hairColor;
	}

	public int getHairStyle()
	{
		return _hairStyle;
	}

	public void setHairStyle(int hairStyle)
	{
		_hairStyle = hairStyle;
	}

	public int getPaperdollObjectId(int slot)
	{
		return _paperdoll[slot][0];
	}

	public int getPaperdollItemId(int slot)
	{
		return _paperdoll[slot][1];
	}

	public int getPaperdollItemSkinByItemId(int slot)
	{
		return _paperdoll[slot][3];
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLevel(int level)
	{
		_level = level;
	}

	public int getMaxHp()
	{
		return _maxHp;
	}

	public void setMaxHp(int maxHp)
	{
		_maxHp = maxHp;
	}

	public int getMaxMp()
	{
		return _maxMp;
	}

	public void setMaxMp(int maxMp)
	{
		_maxMp = maxMp;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public int getRace()
	{
		return _race;
	}

	public void setRace(int race)
	{
		_race = race;
	}

	public int getSex()
	{
		return _sex;
	}

	public void setSex(int sex)
	{
		_sex = sex;
	}

	public int getSp()
	{
		return _sp;
	}

	public void setSp(int sp)
	{
		_sp = sp;
	}

	public int getEnchantEffect()
	{
		return _paperdoll[Inventory.PAPERDOLL_RHAND][2];
	}

	public int getReputation()
	{
		return _karma;
	}

	public void setReputation(int k)
	{
		_karma = k;
	}

	public int getAugmentationId()
	{
		return _augmentationId;
	}

	public void setAugmentationId(int augmentationId)
	{
		_augmentationId = augmentationId;
	}

	public int getPkKills()
	{
		return _pkKills;
	}

	public void setPkKills(int PkKills)
	{
		_pkKills = PkKills;
	}

	public int getPvPKills()
	{
		return _pvpKills;
	}

	public void setPvPKills(int PvPKills)
	{
		_pvpKills = PvPKills;
	}

	public int getTransformId()
	{
		return _transformId;
	}

	public void setTransformId(int id)
	{
		_transformId = id;
	}

	public int getX()
	{
		return _x;
	}

	public void setX(int x)
	{
		_x = x;
	}

	public int getY()
	{
		return _y;
	}

	public void setY(int y)
	{
		_y = y;
	}

	public int getZ()
	{
		return _z;
	}

	public void setZ(int z)
	{
		_z = z;
	}

    public boolean isHairAccessoryEnabled()
    {
        return _vars.get("hairAccessoryEnabled", Boolean.class, false);
    }
}