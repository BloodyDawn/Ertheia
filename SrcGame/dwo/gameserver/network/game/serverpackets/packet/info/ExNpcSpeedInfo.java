package dwo.gameserver.network.game.serverpackets.packet.info;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 16.01.13
 * Time: 6:27
 *
 * Packet: FE 3C 01 B3 61 10 48 00 00 00 00 00 00 00 F0 3F B4 00 00 00 3C 00 00 00 B4 00 00 00 3C 00 00 00 B4 00 00 00 3C 00 00 00 B4 00 00 00 3C 00 00 00
 */
public class ExNpcSpeedInfo extends L2GameServerPacket
{
	private int _objectId;
	private int _run;
	private int _walk;
	private float _moveMul;

	public ExNpcSpeedInfo(L2Character cha)
	{
		_objectId = cha.getObjectId();
		_run = cha.getRunSpeed();
		_walk = cha.getWalkSpeed();
		_moveMul = cha.getMovementSpeedMultiplier();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeC(0x00); //?
		writeF(0x01); // Я так думаю это speed multiplier   "00 00 00 00 00 00 F0 3F"
		writeD(_run);
		writeD(_walk);
		writeD(_run);
		writeD(_walk);
		writeD(_run);
		writeD(_walk);
		writeD(_run);
		writeD(_walk);
	}
}
