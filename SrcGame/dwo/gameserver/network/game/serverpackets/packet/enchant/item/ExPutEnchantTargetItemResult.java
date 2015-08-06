package dwo.gameserver.network.game.serverpackets.packet.enchant.item;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author nBd
 */

public class ExPutEnchantTargetItemResult extends L2GameServerPacket
{
	private int _result;

	public ExPutEnchantTargetItemResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}
}
