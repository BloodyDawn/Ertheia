package dwo.gameserver.model.world.npc;

import dwo.config.Config;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.13
 * Time: 20:11
 */

public class ManorSeedData
{
	private int _id;
	private int _level; // seed level
	private int _crop; // crop type
	private int _mature; // mature crop type
	private int _type1;
	private int _type2;
	private int _manorId; // id of manor (castle id) where seed can be farmed
	private boolean _isAlternative;
	private int _limitSeeds;
	private int _limitCrops;

	public ManorSeedData(int level, int crop, int mature)
	{
		_level = level;
		_crop = crop;
		_mature = mature;
	}

	public void setData(int id, int t1, int t2, int manorId, boolean isAlt, int lim1, int lim2)
	{
		_id = id;
		_type1 = t1;
		_type2 = t2;
		_manorId = manorId;
		_isAlternative = isAlt;
		_limitSeeds = lim1;
		_limitCrops = lim2;
	}

	public int getManorId()
	{
		return _manorId;
	}

	public int getId()
	{
		return _id;
	}

	public int getCrop()
	{
		return _crop;
	}

	public int getMature()
	{
		return _mature;
	}

	public int getReward(int type)
	{
		return type == 1 ? _type1 : _type2;
	}

	public int getLevel()
	{
		return _level;
	}

	public boolean isAlternative()
	{
		return _isAlternative;
	}

	public int getSeedLimit()
	{
		return _limitSeeds * Config.RATE_DROP_MANOR;
	}

	public int getCropLimit()
	{
		return _limitCrops * Config.RATE_DROP_MANOR;
	}

	@Override
	public String toString()
	{
		return "ManorSeedData [_id=" + _id + ", _level=" + _level + ", _crop=" + _crop + ", _mature=" + _mature + ", _type1=" + _type1 + ", _type2=" + _type2 + ", _manorId=" + _manorId + ", _isAlternative=" + _isAlternative + ", _limitSeeds=" + _limitSeeds + ", _limitCrops=" + _limitCrops + ']';
	}
}
