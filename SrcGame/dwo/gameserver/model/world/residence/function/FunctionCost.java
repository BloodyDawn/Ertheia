package dwo.gameserver.model.world.residence.function;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.01.13
 * Time: 20:07
 */

public class FunctionCost
{
	private byte _days;
	private int _cost;

	public FunctionCost(int time, int cost)
	{
		_days = (byte) time;
		_cost = cost;
	}

	public byte getDays()
	{
		return _days;
	}

	public int getCost()
	{
		return _cost;
	}
}