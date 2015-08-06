package dwo.gameserver.network.game.serverpackets;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * @author -Wooden-
 * @author UnAfraid
 */

public class PackageToList extends L2GameServerPacket
{
	private final TIntObjectHashMap<String> _players = new TIntObjectHashMap<>();

	public PackageToList(TIntObjectHashMap<String> chars)
	{
		_players.putAll(chars);
	}

	@Override
	protected void writeImpl()
	{
		writeD(_players.size());
		TIntObjectIterator<String> iterator = _players.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			writeD(iterator.key());
			writeS(iterator.value());
		}
	}
}
