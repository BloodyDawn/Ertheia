package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.xml.AdminTable;

public class RequestGmList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		if(getClient().getActiveChar() == null)
		{
			return;
		}
		AdminTable.getInstance().sendListToPlayer(getClient().getActiveChar());
	}

	@Override
	public String getType()
	{
		return "[C] 81 RequestGmList";
	}
}
