package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 07.10.12
 * Time: 18:05
 */
public class ExMpccRoomInfo extends L2GameServerPacket
{
	private int _index;
	private int _memberSize;
	private int _minLevel;
	private int _maxLevel;
	private int _lootType;
	private int _locationId;
	private String _topic;

	public ExMpccRoomInfo(PartyMatchRoom matching)
	{
		_index = matching.getId();
		_locationId = matching.getLocation();
		_topic = matching.getTitle();
		_minLevel = matching.getMinLvl();
		_maxLevel = matching.getMaxLvl();
		_memberSize = matching.getMaxMembers();
		_lootType = matching.getLootType();
	}

	@Override
	public void writeImpl()
	{
		writeD(_index); //index
		writeD(_memberSize); // member size 1-50
		writeD(_minLevel); //min level
		writeD(_maxLevel); //max level
		writeD(_lootType); //loot type
		writeD(_locationId); //location id as party room
		writeS(_topic); //topic
	}
}
