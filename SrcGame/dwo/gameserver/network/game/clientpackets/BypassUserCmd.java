package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.UserCommandManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

public class BypassUserCmd extends L2GameClientPacket
{
	private int _command;

	@Override
	protected void readImpl()
	{
		_command = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		UserCommandManager.getInstance().execute(new HandlerParams<>(player, _command));
	}

	@Override
	public String getType()
	{
		return "[C] B3 BypassUserCmd";
	}
}