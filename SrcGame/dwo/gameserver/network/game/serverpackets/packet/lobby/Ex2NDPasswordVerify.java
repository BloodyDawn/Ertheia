package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *
 * @author mrTJO
 */
public class Ex2NDPasswordVerify extends L2GameServerPacket
{
	public static final int PASSWORD_OK = 0x00;
	public static final int PASSWORD_WRONG = 0x01;
	public static final int PASSWORD_BAN = 0x02;

	int _wrongTentatives;
	int _mode;

	public Ex2NDPasswordVerify(int mode, int wrongTentatives)
	{
		_mode = mode;
		_wrongTentatives = wrongTentatives;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_mode);
		writeD(_wrongTentatives);
	}
}
