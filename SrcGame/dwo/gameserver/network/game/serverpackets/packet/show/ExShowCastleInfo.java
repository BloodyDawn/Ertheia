package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import org.apache.log4j.Level;

import java.util.List;

/**
 * @author KenM
 */

public class ExShowCastleInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		List<Castle> castles = CastleManager.getInstance().getCastles();
		writeD(castles.size());
		for(Castle castle : castles)
		{
			writeD(castle.getCastleId());
			if(castle.getOwnerId() > 0)
			{
				if(ClanTable.getInstance().getClan(castle.getOwnerId()) != null)
				{
					writeS(ClanTable.getInstance().getClan(castle.getOwnerId()).getName());
				}
				else
				{
					_log.log(Level.WARN, "Castle owner with no name! Castle: " + castle.getName() + " has an OwnerId = " + castle.getOwnerId() + " who does not have a  name!");
					writeS("");
				}
			}
			else
			{
				writeS("");
			}
			writeD(castle.getTaxPercent());
			writeD((int) (castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		}
	}
}
