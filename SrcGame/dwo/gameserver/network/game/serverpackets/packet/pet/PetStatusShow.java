package dwo.gameserver.network.game.serverpackets.packet.pet;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Yme, Keiichi
 */

public class PetStatusShow extends L2GameServerPacket
{
	private int _summonType;
	private int _summonObjId;

	public PetStatusShow(L2Summon summon)
	{
		_summonType = summon.getSummonType();
		_summonObjId = summon.getObjectId();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_summonType);
		writeD(_summonObjId);
	}
}
