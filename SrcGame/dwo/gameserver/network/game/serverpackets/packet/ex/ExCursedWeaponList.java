package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Set;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private Set<Integer> _cursedWeaponIds;

	public ExCursedWeaponList()
	{
		_cursedWeaponIds = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_cursedWeaponIds.size());
		_cursedWeaponIds.forEach(this::writeD);
	}
}