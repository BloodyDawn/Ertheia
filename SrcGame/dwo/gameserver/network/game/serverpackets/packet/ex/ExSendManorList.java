package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.List;

/**
 * @author l3x
 */

public class ExSendManorList extends L2GameServerPacket
{
	private List<String> _manors;

	public ExSendManorList(FastList<String> manors)
	{
		_manors = manors;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_manors.size());
		int i = 1;
		for(String manor : _manors)
		{
			writeD(i++);
			writeS(manor);
		}
	}
}