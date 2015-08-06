package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi,Bacek
 * Date: 21.05.2011
 * Time: 12:29:18
 */

public class ExTacticalSign extends L2GameServerPacket
{
	int _signs;
	private L2Character _target;
	private Sign _sign;

	public ExTacticalSign(L2Character target, Sign sign)
	{
		_target = target;
		_sign = sign;
	}

	public ExTacticalSign(L2Character target, int signs)
	{
		_target = target;
		_signs = signs;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_target.getObjectId());
		writeD(_signs);
	}

	public enum Sign
	{
		SIGN_NONE, //0
		SIGN_STAR, //1
		SIGN_HEART,//2
		SIGN_MOON, //3
		SIGN_CROSS //4
	}
}
