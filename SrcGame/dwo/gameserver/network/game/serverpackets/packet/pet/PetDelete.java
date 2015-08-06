package dwo.gameserver.network.game.serverpackets.packet.pet;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PetDelete extends L2GameServerPacket
{
	private int _petType;
	private int _petObjId;

	public PetDelete(int petType, int petObjId)
	{
		_petType = petType;     // Summon Type
		_petObjId = petObjId;    // objectId
	}

	@Override
	protected void writeImpl()
	{
		writeD(_petType);
		writeD(_petObjId);
	}
}
