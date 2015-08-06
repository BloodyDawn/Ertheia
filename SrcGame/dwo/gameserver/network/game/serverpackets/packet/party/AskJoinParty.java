package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class AskJoinParty extends L2GameServerPacket
{
	private String _requestorName;
	private PartyLootType _itemDistribution;

	public AskJoinParty(String requestorName, PartyLootType itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_requestorName);
		writeD(_itemDistribution.ordinal());
	}
}
