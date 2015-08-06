package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * d: character object id
 * c: 1 if won 0 if failed
 * @author -Wooden-
 */

public class ExFishingEnd extends L2GameServerPacket
{
	L2Character _activeChar;
	private boolean _win;

	public ExFishingEnd(boolean win, L2PcInstance character)
	{
		_win = win;
		_activeChar = character;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeC(_win ? 1 : 0);
	}
}