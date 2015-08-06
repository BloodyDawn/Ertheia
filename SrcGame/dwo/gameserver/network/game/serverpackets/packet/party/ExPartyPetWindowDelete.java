package dwo.gameserver.network.game.serverpackets.packet.party;

import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */

public class ExPartyPetWindowDelete extends L2GameServerPacket
{
	private final L2Summon _summon;

	public ExPartyPetWindowDelete(L2Summon summon)
	{
		_summon = summon;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_summon.getObjectId());

		writeD(_summon.getSummonType()); // GoD может быть для удаления конкретного пета, т.к. их теперь можно вызывать несколько. (судя по всему указывается тип суммона)

		writeD(_summon.getOwner().getObjectId());
		writeS(_summon.getName());
	}
}
