package dwo.gameserver.model.holders;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 25.04.12
 * Time: 6:01
 */

public class ItemChanceHolder
{
	private int _id;
	private int _count;
	private double _chance;

	public ItemChanceHolder(int id, int count, double chance)
	{
		_id = id;
		_count = count;
		_chance = chance;
	}

	public int getId()
	{
		return _id;
	}

	public void setId(int id)
	{
		_id = id;
	}

	public int getCount()
	{
		return _count;
	}

	public void setCount(int count)
	{
		_count = count;
	}

	public double getChance()
	{
		return _chance;
	}

	public void setChance(double chance)
	{
		_chance = chance;
	}
}