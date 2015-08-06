package dwo.gameserver.network.game.clientpackets.packet.primeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.primeshop.ExBR_GamePoint;

public class RequestBR_GamePoint extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		player.sendPacket(new ExBR_GamePoint(player));
	}

	@Override
	public String getType()
	{
		return "[C] D0:89 RequestBrGamePoint";
	}
}
