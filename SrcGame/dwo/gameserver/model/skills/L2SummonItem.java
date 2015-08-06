package dwo.gameserver.model.skills;

import dwo.gameserver.model.skills.stats.StatsSet;

public class L2SummonItem
{
	private final int _itemId;
	private final int _npcId;
	private final byte _type;
	private final int _despawnDelay;

	public L2SummonItem(StatsSet set)
	{
		_itemId = set.getInteger("id");
		_npcId = set.getInteger("npcId");
		_type = set.getByte("type");
		_despawnDelay = set.getInteger("despawnDelay", 0);
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public byte getType()
	{
		return _type;
	}

	public boolean isPetSummon()
	{
		return _type == 1 || _type == 2;
	}

	public int getDespawnDelay()
	{
		return _despawnDelay;
	}
}
