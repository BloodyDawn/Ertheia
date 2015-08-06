package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * Packet will sent when user adds new character to block list.
 *
 * Base format (0xfe:0xec D[S]):
 * D	Count	Count of blocked characters in a row
 * [list]
 * S	Names	List of blocked character names
 * [/list]
 */
public class ExBlockAddResult extends L2GameServerPacket
{
	private final List<String> _blockCharNames;

	public ExBlockAddResult(List<String> blockCharNames)
	{
		_blockCharNames = blockCharNames;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_blockCharNames.size());
		_blockCharNames.forEach(this::writeS);
	}
}
