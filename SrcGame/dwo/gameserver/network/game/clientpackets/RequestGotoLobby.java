package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExLoginVitalityEffectInfo;
import dwo.gameserver.network.game.serverpackets.packet.lobby.CharacterSelectionInfo;

/**
 * @author KenM, ANZO
 */

public class RequestGotoLobby extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		client.sendPacket(new CharacterSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1));
        client.sendPacket(new ExLoginVitalityEffectInfo(client.getAccountName()));
	}

	@Override
	public String getType()
	{
		return "[C] D0:38 RequestGotoLobby";
	}
}
