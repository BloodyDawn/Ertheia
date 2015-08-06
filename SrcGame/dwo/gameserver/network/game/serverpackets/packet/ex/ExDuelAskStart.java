package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */

public class ExDuelAskStart extends L2GameServerPacket
{
	private String _requestorName;
	private int _partyDuel;

	public ExDuelAskStart(String requestor, int partyDuel)
	{
		_requestorName = requestor;
		_partyDuel = partyDuel;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_requestorName);
		writeD(_partyDuel);
	}
}
