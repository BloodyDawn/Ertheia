package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Keiichi, ANZO
 * Date: 25.12.11
 * Time: 11:32
 */

public class ExCastleState extends L2GameServerPacket
{
	private int _id;
	private CastleSide _side;

	public ExCastleState(Castle castle)
	{
		_id = castle.getCastleId();
		_side = castle.getCastleSide();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_id); //castle id, town id, etc...
		writeD(_side.ordinal()); // Сторона замка
	}
}