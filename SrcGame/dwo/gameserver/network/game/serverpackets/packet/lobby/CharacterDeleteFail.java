package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class CharacterDeleteFail extends L2GameServerPacket
{
	private int _error;

	public CharacterDeleteFail(ECharacterDeleteFailType errorCode)
	{
		_error = errorCode.ordinal();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_error);
	}

	public enum ECharacterDeleteFailType
	{
		ECDFT_NONE,
		ECDFT_UNKNOWN,
		ECDFT_PLEDGE_MEMBER,
		ECDFT_PLEDGE_MASTER,
		ECDFT_PROHIBIT_CHAR_DELETION,
		ECDFT_COMMISSION,
		ECDFT_MENTOR,
		ECDFT_MENTEE,
		ECDFT_MAIL
	}
}
