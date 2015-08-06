package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterDeleteFail;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterDeleteSuccess;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelectionInfo;
import org.apache.log4j.Level;

public class CharacterDelete extends L2GameClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		if(!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction(FloodAction.CHARACTER_DELETE))
		{
			sendPacket(new CharacterDeleteFail(CharacterDeleteFail.ECharacterDeleteFailType.ECDFT_UNKNOWN));
			return;
		}

		try
		{
			byte answer = getClient().markToDeleteChar(_charSlot);

			switch(answer)
			{
				default:
				case -1: // Error
					break;
				case 0: // Success!
					sendPacket(new CharacterDeleteSuccess());
					break;
				case 1:
					sendPacket(new CharacterDeleteFail(CharacterDeleteFail.ECharacterDeleteFailType.ECDFT_PLEDGE_MEMBER));
					break;
				case 2:
					sendPacket(new CharacterDeleteFail(CharacterDeleteFail.ECharacterDeleteFailType.ECDFT_PLEDGE_MASTER));
					break;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error:", e);
		}

		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	@Override
	public String getType()
	{
		return "[C] 0D CharacterDelete";
	}
}
