package dwo.gameserver.network.game.serverpackets.packet.enchant.skill;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author JIV
 */

public class ExEnchantSkillResult extends L2GameServerPacket
{
	private static final ExEnchantSkillResult STATIC_PACKET_TRUE = new ExEnchantSkillResult(true);
	private static final ExEnchantSkillResult STATIC_PACKET_FALSE = new ExEnchantSkillResult(false);
	private boolean _enchanted;

	public ExEnchantSkillResult(boolean enchanted)
	{
		_enchanted = enchanted;
	}

	public static ExEnchantSkillResult valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_enchanted ? 1 : 0);
	}
}
