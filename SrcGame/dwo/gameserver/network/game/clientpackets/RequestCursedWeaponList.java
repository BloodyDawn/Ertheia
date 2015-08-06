package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Триггер
	}

	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		activeChar.sendPacket(new ExCursedWeaponList());
	}

	@Override
	public String getType()
	{
		return "[C] D0:22 RequestCursedWeaponList";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
