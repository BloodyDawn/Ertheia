package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.holders.JumpHolder;
import dwo.gameserver.model.player.jump.L2JumpType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Keiichi, Bacek
 * Date: 27.05.2011
 * Time: 12:06:19
 */

public class ExFlyMove extends L2GameServerPacket
{
	private int _objectId;
	private L2JumpType _type;
	private int _idJump;
	private int _waysCount;
	private List<JumpHolder> _jump;

	public ExFlyMove(int ObjectId, L2JumpType jumpType, int jumpId, List<JumpHolder> jump)
	{
		_objectId = ObjectId;
		_type = jumpType;
		_idJump = jumpId;
		_jump = jump;
		_waysCount = jump.size();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeD(_type.ordinal());
		writeD(0x00); // ??
		writeD(_idJump);
		writeD(_waysCount);
		for(JumpHolder jump : _jump)
		{
			writeD(jump.getNum());
			writeD(0x00); // ??
			writeD(jump.getLoc().getX());
			writeD(jump.getLoc().getY());
			writeD(jump.getLoc().getZ());
		}
	}
}
