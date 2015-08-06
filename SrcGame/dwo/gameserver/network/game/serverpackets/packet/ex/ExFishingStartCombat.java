package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author -Wooden-
 */

public class ExFishingStartCombat extends L2GameServerPacket
{
	private L2Character _activeChar;
	private int _time;
	private int _hp;
	private int _lureType;
	private int _deceptiveMode;
	private int _mode;

	public ExFishingStartCombat(L2Character character, int time, int hp, int mode, int lureType, int deceptiveMode)
	{
		_activeChar = character;
		_time = time;
		_hp = hp;
		_mode = mode;
		_lureType = lureType;
		_deceptiveMode = deceptiveMode;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(_time);
		writeD(_hp);
		writeC(_mode); // mode: 0 = resting, 1 = fighting
		writeC(_lureType); // 0 = newbie lure, 1 = normal lure, 2 = night lure
		writeC(_deceptiveMode); // Fish Deceptive Mode: 0 = no, 1 = yes
	}
}