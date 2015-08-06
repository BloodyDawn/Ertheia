package dwo.gameserver.model.player.formation.clan;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.02.12
 * Time: 22:39
 */

public class RankPrivs
{
	private final int _rankId;
	private final int _party;// TODO find out what this stuff means and implement it
	private int _rankPrivs;

	public RankPrivs(int rank, int party, int privs)
	{
		_rankId = rank;
		_party = party;
		_rankPrivs = privs;
	}

	public int getRank()
	{
		return _rankId;
	}

	public int getParty()
	{
		return _party;
	}

	public int getPrivs()
	{
		return _rankPrivs;
	}

	public void setPrivs(int privs)
	{
		_rankPrivs = privs;
	}
}
