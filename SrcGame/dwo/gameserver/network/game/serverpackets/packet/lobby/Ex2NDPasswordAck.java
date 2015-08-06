package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *
 * @author mrTJO
 */
public class Ex2NDPasswordAck extends L2GameServerPacket
{
	public static int SUCCESS;
	public static int WRONG_PATTERN = 0x01;
	int _response;

	public Ex2NDPasswordAck(int response)
	{
		_response = response;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0x00);
		if(_response == WRONG_PATTERN)
		{
			writeD(0x01);
		}
		else
		{
			writeD(0x00);
		}
		writeD(0x00);
	}
}
