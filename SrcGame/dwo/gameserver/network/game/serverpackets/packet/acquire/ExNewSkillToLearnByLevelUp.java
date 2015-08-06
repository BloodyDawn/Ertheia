package dwo.gameserver.network.game.serverpackets.packet.acquire;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.10.11
 * Time: 9:24
 */

public class ExNewSkillToLearnByLevelUp extends L2GameServerPacket
{
	private int skillLearnCount;
	private String skillLearnName;

	//значок при новых скилах
	public ExNewSkillToLearnByLevelUp()
	{
		skillLearnCount = 0; //skill.getSkillId();
		skillLearnName = ""; //skill.getName();
	}

	@Override
	protected void writeImpl()
	{
		writeD(skillLearnCount); // ??
		writeS(skillLearnName); // ??
	}
}
