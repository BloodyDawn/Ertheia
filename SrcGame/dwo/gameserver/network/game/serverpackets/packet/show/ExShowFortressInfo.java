package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * format: d (dSdd)
 * cnt:%d (fortressID:%d ownerName:%s, siegeState:%d, lastOwnedTime:%d)
 * @author KenM
 */

public class ExShowFortressInfo extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		List<Fort> forts = FortManager.getInstance().getForts();
		writeD(forts.size());
		for(Fort fort : forts)
		{
			L2Clan clan = fort.getOwnerClan();
			writeD(fort.getFortId());
			if(clan != null)
			{
				writeS(clan.getName());
			}
			else
			{
				writeS("");
			}

			if(fort.getSiege().isInProgress())
			{
				writeD(1);
			}
			else
			{
				writeD(0);
			}
			// Time of possession
			writeD(fort.getOwnedTime());
		}
	}
}
