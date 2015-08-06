package dwo.gameserver.model.player;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 19.01.13
 * Time: 14:22
 */

public class PlayerLvlUpData
{
	private final double _hp;
	private final double _mp;
	private final double _cp;

	public PlayerLvlUpData(double hp, double mp, double cp)
	{
		_hp = hp;
		_mp = mp;
		_cp = cp;
	}

	public double getHP()
	{
		return _hp;
	}

	public double getMP()
	{
		return _mp;
	}

	public double getCP()
	{
		return _cp;
	}
}
