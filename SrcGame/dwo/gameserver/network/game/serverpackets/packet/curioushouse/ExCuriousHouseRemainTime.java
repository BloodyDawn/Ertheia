package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 28.09.12
 * Time: 19:20
 * Пакет оставшегося времени ( шлется каждую секунду ) от 360 до 0
 */
public class ExCuriousHouseRemainTime extends L2GameServerPacket
{
	private int _time;

	public ExCuriousHouseRemainTime(int time)
	{
		_time = time;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_time);  // Remaining time in secs
	}
}
