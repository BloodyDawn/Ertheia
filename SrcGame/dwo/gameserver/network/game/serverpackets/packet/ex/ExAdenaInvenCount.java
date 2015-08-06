package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 16.10.12
 * Time: 11:28
 */

public class ExAdenaInvenCount extends L2GameServerPacket
{
	private L2PcInstance _player;

	public ExAdenaInvenCount(L2PcInstance player)
	{
		_player = player;
	}

	@Override
	protected void writeImpl()
	{
		writeQ(_player.getInventory().getAdenaCount()); // Количество адены
		writeD(_player.getInventory().getSize(false));  // Количество предметов в инвентаре
	}
}
