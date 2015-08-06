package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author  -Wooden-
 */

public class PledgeSkillListAdd extends L2GameServerPacket
{
	private int _id;
	private int _lvl;

	public PledgeSkillListAdd(int id, int lvl)
	{
		_id = id;
		_lvl = lvl;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_id);
		writeD(_lvl);
	}
}