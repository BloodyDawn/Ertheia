package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * Format (ch)dd
 * d: window type
 * d: ban user (1)
 *
 * @author mrTJO
 */
public class Ex2NDPasswordCheck extends L2GameServerPacket
{
	public static final int PASSWORD_NEW = 0x00;
	public static final int PASSWORD_PROMPT = 0x01;
	public static final int PASSWORD_OK = 0x02;

	int _windowType;

	public Ex2NDPasswordCheck(int windowType)
	{
		_windowType = windowType;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_windowType);
		writeD(0x00);
	}
}
