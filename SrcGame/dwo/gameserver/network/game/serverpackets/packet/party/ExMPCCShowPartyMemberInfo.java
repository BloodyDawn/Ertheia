package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author chris_00
 */

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private L2Party _party;

	public ExMPCCShowPartyMemberInfo(L2Party party)
	{
		_party = party;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_party.getMemberCount()); // Number of Members
		for(L2PcInstance pc : _party.getMembers())
		{
			writeS(pc.getName()); // Membername
			writeD(pc.getObjectId()); // ObjId
			writeD(pc.getClassId().getId()); // Classid
		}
	}
}
