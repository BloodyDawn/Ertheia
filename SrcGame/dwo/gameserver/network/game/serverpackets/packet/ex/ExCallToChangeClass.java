package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 07.06.2011
 * Time: 0:27:11
 */

public class ExCallToChangeClass extends L2GameServerPacket
{
	private final ClassId _charClass;
	private final int _popup;

	public ExCallToChangeClass(ClassId charClass, boolean popup)
	{
		_charClass = charClass;
		_popup = popup ? 1 : 0;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_charClass.getId());
		writeD(_popup);
	}
}