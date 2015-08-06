package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.util.geometry.Point3D;

import java.util.List;

/**
 * @author  -Wooden-
 */

public class ExCursedWeaponLocation extends L2GameServerPacket
{
	private List<CursedWeaponInfo> _cursedWeaponInfo;

	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}

	@Override
	protected void writeImpl()
	{
		if(_cursedWeaponInfo.isEmpty())
		{
			writeD(0);
			writeD(0);
		}
		else
		{
			writeD(_cursedWeaponInfo.size());
			for(CursedWeaponInfo w : _cursedWeaponInfo)
			{
				writeD(w.id);
				writeD(w.activated);

				writeD(w.pos.getX());
				writeD(w.pos.getY());
				writeD(w.pos.getZ());
			}
		}
	}

	public static class CursedWeaponInfo
	{
		public Point3D pos;
		public int id;
		public int activated; //0 - not activated ? 1 - activated

		public CursedWeaponInfo(Point3D p, int ID, int status)
		{
			pos = p;
			id = ID;
			activated = status;
		}
	}
}