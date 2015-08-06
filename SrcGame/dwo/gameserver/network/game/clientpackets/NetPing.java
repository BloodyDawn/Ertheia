package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

public class NetPing extends L2GameClientPacket
{
	private int _clientID;
	private int _ping;

	@Override
	protected void readImpl()
	{
		_clientID = readD();
		_ping = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		// Записываем пришедший пинг игроку
		player.setPing(_ping);
	}

	@Override
	public String getType()
	{
		return "[C] B1 NetPing";
	}
}