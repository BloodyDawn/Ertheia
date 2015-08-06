package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

public class ExQuestNpcLogList extends L2GameServerPacket
{
	private final int _questId;
	private final TIntIntHashMap _log;

	public ExQuestNpcLogList(int questId, TIntIntHashMap log)
	{
		_questId = questId;
		_log = log;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_questId);
		writeC(_log.size());
		TIntIntIterator iterator = _log.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			writeD(iterator.key());
			writeC(0x00);
			writeD(iterator.value());
		}
	}
}