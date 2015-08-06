package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;

public class ExShowAgitInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		Collection<AuctionableHall> clannhalls = ClanHallManager.getInstance().getAllAuctionableClanHalls().values();
		writeD(clannhalls.size());
		for(AuctionableHall ch : clannhalls)
		{
			writeD(ch.getId());
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getName()); // owner clan name
			writeS(ch.getOwnerId() <= 0 ? "" : ClanTable.getInstance().getClan(ch.getOwnerId()).getLeaderName()); // leader name
			writeD(ch.getGrade() > 0 ? 0x00 : 0x01); // 0 - auction  1 - war clanhall  2 - ETC (rainbow spring clanhall)
		}
	}
}
