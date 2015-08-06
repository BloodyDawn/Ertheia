package dwo.gameserver.network.game.serverpackets;

import java.util.List;

/**
 * List of blocked character in new GoD window.
 *
 * Base format (0xd5 D [SS]):
 * D	Count	Total count for block list.
 * [list] List of blocked characters with associated memos
 * S	CharName	Blocked character name.
 * S	Memo		Associated text note.
 * [/list]
 */

public class BlockList extends L2GameServerPacket
{
	private final List<String> _blockedCharNames;
	private final List<String> _memos;

	public BlockList(List<String> blockedCharNames, List<String> memos)
	{
		_blockedCharNames = blockedCharNames;
		_memos = memos;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_blockedCharNames.size());
		for(int i = 0, j = _blockedCharNames.size(); i < j; ++i)
		{
			writeS(_blockedCharNames.get(i));
			writeS(_memos.get(i));
		}
	}
}
