package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 04.10.12
 * Time: 11:56
 */
public class MagicAndSkillList extends L2GameServerPacket
{
	private int _obj;

	public MagicAndSkillList(L2PcInstance player)
	{
		_obj = player.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_obj);
        writeD(0x00);
        writeC(0x86);
        writeC(0x25);
        writeC(0x0B);
        writeC(0x00);
	}
}
