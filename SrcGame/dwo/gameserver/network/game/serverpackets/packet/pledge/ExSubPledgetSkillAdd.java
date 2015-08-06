package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author VISTALL
 */

public class ExSubPledgetSkillAdd extends L2GameServerPacket
{
	private final int _type;
	private final int _skillId;
	private final int _skillLevel;

	public ExSubPledgetSkillAdd(int type, int skillId, int skillLevel)
	{
		_type = type;
		_skillId = skillId;
		_skillLevel = skillLevel;
	}

	@Override
	public void writeImpl()
	{
		writeD(_type);
		writeD(_skillId);
		writeD(_skillLevel);
	}
}
