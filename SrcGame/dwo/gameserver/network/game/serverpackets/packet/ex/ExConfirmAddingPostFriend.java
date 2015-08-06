package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author mrTJO & UnAfraid
 */

public class ExConfirmAddingPostFriend extends L2GameServerPacket
{
	public static final byte SUCCESS = 1;
	public static final byte NOT_EXISTS = -2;
	public static final byte MAX_REACHED = -3;
	public static final byte ALREADY_EXISTS = -4;
	private final String _charName;
	private byte _type;

	public ExConfirmAddingPostFriend(String requestedName, byte type)
	{
		_charName = requestedName;
		_type = type;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_charName);
		writeD(_type);
	}
}
