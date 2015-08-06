package dwo.gameserver.network.game.serverpackets.packet.lobby;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class CharacterCreateFail extends L2GameServerPacket
{
	private int _error;

	public CharacterCreateFail(CharacterCreateFailReason errorCode)
	{
		_error = errorCode.ordinal();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_error);
	}

	public enum CharacterCreateFailReason
	{
		REASON_CREATION_FAILED, // "Your character creation has failed."
		REASON_TOO_MANY_CHARACTERS, // "You cannot create another character. Please delete the existing character and try again." Removes all settings that were selected (race, class, etc).
		REASON_NAME_ALREADY_EXISTS, // "This name already exists."
		REASON_16_ENG_CHARS, // "Your title cannot exceed 16 characters in length. Please try again."
		REASON_INCORRECT_NAME, // "Incorrect name. Please try again."
		REASON_CREATE_NOT_ALLOWED, // "Characters cannot be created from this server."
		REASON_CHOOSE_ANOTHER_SVR // "Unable to create character. You are unable to create a new character on the selected server. A restriction is in place which restricts users from creating characters on different servers where no previous character exists. Please choose another server."
	}
}
