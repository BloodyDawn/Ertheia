package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

public class GameGuardReply extends L2GameClientPacket
{
	private int[] _reply;

	public GameGuardReply()
	{
		_reply = new int[4];
	}

	@Override
	protected void readImpl()
	{
		_reply[0] = readD();
		_reply[1] = readD();
		_reply[2] = readD();
		_reply[3] = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
		}
	}

	@Override
	public String getType()
	{
		return "[C] CA GameGuardReply";
	}
}
