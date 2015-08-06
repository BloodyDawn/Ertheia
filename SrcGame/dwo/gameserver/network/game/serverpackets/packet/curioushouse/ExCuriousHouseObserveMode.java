package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 28.09.12
 * Time: 19:20
 */
public class ExCuriousHouseObserveMode extends L2GameServerPacket
{
	private final boolean _isInObserveMode;

	public ExCuriousHouseObserveMode(boolean isInObserveMode)
	{
		_isInObserveMode = isInObserveMode;
	}

	@Override
	protected void writeImpl()
	{
		writeC(_isInObserveMode ? 0x00 : 0x01); // видел 0 и 1   ( CuriousHouseObserverState в клиенте )
	}
}
