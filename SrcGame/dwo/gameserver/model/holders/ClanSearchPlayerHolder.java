package dwo.gameserver.model.holders;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeDraftListSearch;

/**
 * @author Yorie
 */
public class ClanSearchPlayerHolder
{
	private final int _charId;
	private final int _prefferedClanId;
	private final ExPledgeDraftListSearch.ClanSearchListType _searchType;
	private final String _desc;
	private String _charName;
	private int _charLevel;
	private int _charClassId;

	public ClanSearchPlayerHolder(int charId, String charName, int charLevel, int charClassId, ExPledgeDraftListSearch.ClanSearchListType searchType)
	{
		_charId = charId;
		_searchType = searchType;
		_prefferedClanId = -1;
		_desc = null;
		_charName = charName;
		_charLevel = charLevel;
		_charClassId = charClassId;
	}

	public ClanSearchPlayerHolder(int charId, String charName, int charLevel, int charClassId, int prefferedClanId, ExPledgeDraftListSearch.ClanSearchListType searchType, String desc)
	{
		_charId = charId;
		_prefferedClanId = prefferedClanId;
		_searchType = searchType;
		_desc = desc;
		_charName = charName;
		_charLevel = charLevel;
		_charClassId = charClassId;
	}

	public boolean isApplicant()
	{
		return _prefferedClanId > 0;
	}

	public int getCharId()
	{
		return _charId;
	}

	public int getPrefferedClanId()
	{
		return _prefferedClanId;
	}

	public ExPledgeDraftListSearch.ClanSearchListType getSearchType()
	{
		return _searchType;
	}

	public String getDesc()
	{
		return _desc;
	}

	public String getCharName()
	{
		L2PcInstance player = WorldManager.getInstance().getPlayer(_charId);

		if(player != null)
		{
			_charName = player.getName();
		}

		return _charName;
	}

	public int getCharLevel()
	{
		L2PcInstance player = WorldManager.getInstance().getPlayer(_charId);

		if(player != null)
		{
			_charLevel = player.getLevel();
		}

		return _charLevel;
	}

	public int getCharClassId()
	{
		L2PcInstance player = WorldManager.getInstance().getPlayer(_charId);

		if(player != null)
		{
			_charClassId = player.getBaseClassId();
		}
		return _charClassId;
	}
}
