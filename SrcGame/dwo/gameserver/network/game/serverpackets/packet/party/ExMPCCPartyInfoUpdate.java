package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author chris_00
 */

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket
{
	private L2Party _party;
	private int _mode;
	private int _LeaderOID;
	private int _memberCount;
	private String _name;

	/**
	 * @param party
	 * @param mode 0 = Remove, 1 = Add
	 */
	public ExMPCCPartyInfoUpdate(L2Party party, int mode)
	{
		_party = party;
		_name = _party.getLeader().getName();
		_LeaderOID = _party.getLeaderObjectId();
		_memberCount = _party.getMemberCount();
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_name);
		writeD(_LeaderOID);
		writeD(_memberCount);
		writeD(_mode); //mode 0 = Remove Party, 1 = AddParty, maybe more...
	}
}
