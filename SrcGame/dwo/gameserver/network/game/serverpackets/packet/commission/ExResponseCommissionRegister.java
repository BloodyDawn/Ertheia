package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 22.07.11
 *
 * Проверил: Bacek
 * Дата: 06.05.12
 * Протокол: 463 (  Glory Days )
 */

public class ExResponseCommissionRegister extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0x01);
	}
}
