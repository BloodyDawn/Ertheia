package dwo.gameserver.model.holders;

/**
 * Chaos Festival player info database entry.
 *
 * @author Yorie
 */
public class ChaosFestivalEntry
{
	private final int _playerId;
	private int _mystSigns;
	private int _skipRounds;
	private int _totalBans;

	private boolean _needUpdate;

	public ChaosFestivalEntry(int playerId)
	{
		_playerId = playerId;
		_mystSigns = 0;
		_skipRounds = 0;
		_totalBans = 0;
		_needUpdate = true;
	}

	public ChaosFestivalEntry(int playerId, int mystSigns, int skipRounds, int totalBans)
	{
		_playerId = playerId;
		_mystSigns = mystSigns;
		_skipRounds = skipRounds;
		_totalBans = totalBans;
	}

	public int getPlayerId()
	{
		return _playerId;
	}

	public int getMystSigns()
	{
		return _mystSigns;
	}

	public void setMystSigns(int count)
	{
		_needUpdate = true;
		_mystSigns = count;
	}

	public int getSkipRounds()
	{
		return _skipRounds;
	}

	public void setSkipRounds(int count)
	{
		_needUpdate = true;
		_skipRounds = count;
	}

	public int getTotalBans()
	{
		return _totalBans;
	}

	public void setTotalBans(int count)
	{
		_needUpdate = true;
		_totalBans = count;
	}

	public boolean isNeedUpdate()
	{
		return _needUpdate;
	}
}
