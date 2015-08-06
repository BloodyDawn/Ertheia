package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Keiichi
 */

public class ExUISetting extends L2GameServerPacket
{

	private final byte[] _config;

	public ExUISetting(L2PcInstance player)
	{
		_config = player.getBindConfigData();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_config.length);
		writeB(_config);
	}
}
