package dwo.gameserver.model.actor.stat;

import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.skills.stats.Stats;

public class DoorStat extends CharStat
{
	private L2DoorInstance _door;

	public DoorStat(L2DoorInstance activeChar)
	{
		super(activeChar);
		_door = activeChar;
	}

	@Override
	public L2DoorInstance getActiveChar()
	{
		return (L2DoorInstance) super.getActiveChar();
	}

	@Override
	public int getMaxHp()
	{
		// Апгрейд двери замка или форта
		double modifier = _door.getHpLevel() / 100;
		return (int) calcStat(Stats.LIMIT_HP, getMaxVisibleHp() * modifier, null, null);
	}
}