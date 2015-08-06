package dwo.gameserver.network.game.serverpackets.packet.enchant.item;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * User: Bacek
 * Date: 07.02.13
 * Time: 4:22
 */
public class ExPutEnchantScrollItemResult extends L2GameServerPacket
{
	private int _result;

	public ExPutEnchantScrollItemResult(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}
}
