package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.instancemanager.GraciaSeedsManager;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExShowSeedMapInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeD(2); // seed count
		// Seed of Destruction
		writeD(-246857); // x coord
		writeD(251960); // y coord
		writeD(4331); // z coord
		writeD(2770 + GraciaSeedsManager.getInstance().getSoDState()); // sys msg id

		// Seed of Infinity
		writeD(-213770); // x coord
		writeD(210760); // y coord
		writeD(4400); // z coord
		// Manager not implemented yet
		writeD(2766); // sys msg id
	}
}
