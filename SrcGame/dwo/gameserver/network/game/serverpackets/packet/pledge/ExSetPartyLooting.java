package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExSetPartyLooting extends L2GameServerPacket
{
	private final int _result;
	private final PartyLootType _mode;

	public ExSetPartyLooting(int result, PartyLootType mode)
	{
		_result = result;
		_mode = mode;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeD(_mode.ordinal());
	}
}
