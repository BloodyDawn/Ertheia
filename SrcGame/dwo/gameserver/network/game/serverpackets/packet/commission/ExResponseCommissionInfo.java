package dwo.gameserver.network.game.serverpackets.packet.commission;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *  Проверил: Bacek
 *  Дата: 06.05.12
 *  Протокол: 463 (  Glory Days )
 */

public class ExResponseCommissionInfo extends L2GameServerPacket
{

	@Override
	protected void writeImpl()
	{
		writeD(0x01);  //1
		writeD(0x00);
		writeQ(0x00);
		writeQ(0x00);
		writeD(-1);  //-1
	}
}
