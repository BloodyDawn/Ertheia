package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
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
		if(!getClient().getFloodProtectors().getCharacterSelect().tryPerformAction(FloodAction.CHARACTER_RESTORE))
		{
			return;
		}

		getClient().markRestoredChar(_charSlot);

		CharacterSelectionInfo cl = new CharacterSelectionInfo(getClient().getAccountName(), getClient().getSessionId().playOkID1, 0);
		sendPacket(cl);
		getClient().setCharSelection(cl.getCharInfo());
	}

	@Override
	public String getType()
	{
		return "[C] 62 CharacterRestore";
	}
}