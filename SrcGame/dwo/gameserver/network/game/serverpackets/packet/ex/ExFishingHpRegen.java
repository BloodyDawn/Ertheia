package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author -Wooden-
 */

public class ExFishingHpRegen extends L2GameServerPacket
{
	private L2Character _activeChar;
	private int _time;
	private int _fishHP;
	private int _hpMode;
	private int _anim;
	private int _goodUse;
	private int _penalty;
	private int _hpBarColor;

	public ExFishingHpRegen(L2Character character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor)
	{
		_activeChar = character;
		_time = time;
		_fishHP = fishHP;
		_hpMode = HPmode;
		_goodUse = GoodUse;
		_anim = anim;
		_penalty = penalty;
		_hpBarColor = hpBarColor;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_activeChar.getObjectId());
		writeD(_time);
		writeD(_fishHP);
		writeC(_hpMode); // 0 = HP stop, 1 = HP raise
		writeC(_goodUse); // 0 = none, 1 = success, 2 = failed
		writeC(_anim); // Anim: 0 = none, 1 = reeling, 2 = pumping
		writeD(_penalty); // Penalty
		writeC(_hpBarColor); // 0 = normal hp bar, 1 = purple hp bar
	}
}