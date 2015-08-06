package dwo.gameserver.model.world.olympiad;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.02.12
 * Time: 19:54
 */

public class OlyResultInfo
{
	private String _name;
	private String _clan;
	private int _clanId;
	private int _classId;
	private int _dmg;
	private int _curPoints;
	private int _diffPoints;

	public OlyResultInfo(Participant _player, int _damage, int pointDiff)
	{
		_name = _player.getName();
		_clan = _player.getPlayer().getClan() != null ? _player.getPlayer().getClan().getName() : "";
		_clanId = _player.getPlayer().getClanId();
		_classId = _player.getPlayer().getClassId().getId();
		_dmg = _damage;
		_curPoints = _player.getStats().getInteger(Olympiad.POINTS);
		_diffPoints = pointDiff;
	}

	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	public String getClan()
	{
		return _clan;
	}

	public void setClan(String clan)
	{
		_clan = clan;
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

	public int getDmg()
	{
		return _dmg;
	}

	public void setDmg(int dmg)
	{
		_dmg = dmg;
	}

	public int getCurPoints()
	{
		return _curPoints;
	}

	public void setCurPoints(int curPoints)
	{
		_curPoints = curPoints;
	}

	public int getDiffPoints()
	{
		return _diffPoints;
	}

	public void setDiffPoints(int diffPoints)
	{
		_diffPoints = diffPoints;
	}
}