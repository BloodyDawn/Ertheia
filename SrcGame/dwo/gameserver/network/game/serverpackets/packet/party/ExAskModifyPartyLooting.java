package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExAskModifyPartyLooting extends L2GameServerPacket
{
	private final String _requestor;
	private final PartyLootType _mode;

	public ExAskModifyPartyLooting(String name, PartyLootType mode)
	{
		_requestor = name;
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_requestor);
		writeD(_mode.ordinal());
	}
}
