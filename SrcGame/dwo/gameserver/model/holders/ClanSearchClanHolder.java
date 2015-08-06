package dwo.gameserver.model.holders;

import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch;

/**
 * @author Yorie
 */
public class ClanSearchClanHolder
{
	private int _clanId;
	private ExPledgeDraftListSearch.ClanSearchListType _searchType;
	private String _title;
	private String _desc;

	public ClanSearchClanHolder(int clanId, ExPledgeDraftListSearch.ClanSearchListType searchType, String title, String desc)
	{
		_clanId = clanId;
		_searchType = searchType;
		_title = title;
		_desc = desc;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public ExPledgeDraftListSearch.ClanSearchListType getSearchType()
	{
		return _searchType;
	}

	public void setSearchType(ExPledgeDraftListSearch.ClanSearchListType searchType)
	{
		_searchType = searchType;
	}

	public String getTitle()
	{
		return _title;
	}

	public void setTitle(String title)
	{
		_title = title;
	}

	public String getDesc()
	{
		return _desc;
	}

	public void setDesc(String desc)
	{
		_desc = desc;
	}
}
