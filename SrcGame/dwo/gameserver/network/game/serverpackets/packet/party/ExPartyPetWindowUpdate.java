package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */

public class ExPartyPetWindowUpdate extends L2GameServerPacket
{
	private final L2Summon _summon;

	public ExPartyPetWindowUpdate(L2Summon summon)
	{
		_summon = summon;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_summon.getObjectId());
		writeD(_summon.getTemplate().getIdTemplate() + 1000000);
		writeD(_summon.getSummonType());
		writeD(_summon.getOwner().getObjectId());
		writeS(_summon.getName());
		writeD((int) _summon.getCurrentHp());
		writeD(_summon.getMaxVisibleHp());
		writeD((int) _summon.getCurrentMp());
		writeD(_summon.getMaxMp());
		writeD(_summon.getLevel());
	}
}
