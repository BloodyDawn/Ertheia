package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.items.CursedWeapon;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExCursedWeaponLocation;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExCursedWeaponLocation.CursedWeaponInfo;
import dwo.gameserver.util.geometry.Point3D;
import javolution.util.FastList;

import java.util.List;

/**
 * Format: (ch)
 * @author  -Wooden-
 */
public class RequestCursedWeaponLocation extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		//nothing to read it's just a trigger
	}

	@Override
	protected void runImpl()
	{
		L2Character activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		List<CursedWeaponInfo> list = new FastList<>();
		for(CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			if(!cw.isActive())
			{
				continue;
			}

			Point3D pos = cw.getWorldPosition();
			if(pos != null)
			{
				list.add(new CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
			}
		}

		//send the ExCursedWeaponLocation
		if(!list.isEmpty())
		{
			activeChar.sendPacket(new ExCursedWeaponLocation(list));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:23 RequestCursedWeaponLocation";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
