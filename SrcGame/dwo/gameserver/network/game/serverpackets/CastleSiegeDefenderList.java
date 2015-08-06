package dwo.gameserver.network.game.serverpackets;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2SiegeClan;
import dwo.gameserver.model.world.residence.castle.Castle;

/**
 * @author KenM
 */

public class CastleSiegeDefenderList extends L2GameServerPacket
{
	private Castle _castle;

	public CastleSiegeDefenderList(Castle castle)
	{
		_castle = castle;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_castle.getCastleId());
		writeD(0x00);  //0
		writeD(0x01);  //1
		writeD(0x00);  //0
		int size = _castle.getSiege().getDefenderClans().size() + _castle.getSiege().getDefenderWaitingClans().size();
		if(size > 0)
		{
			L2Clan clan;

			writeD(size);
			writeD(size);
			// Listing the Lord and the approved clans
			for(L2SiegeClan siegeclan : _castle.getSiege().getDefenderClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if(clan == null)
				{
					continue;
				}

				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); //signed time (seconds) (not storated by L2J)
				switch(siegeclan.getType())
				{
					case OWNER:
						writeD(0x01); //owner
						break;
					case DEFENDER_PENDING:
						writeD(0x02); //approved
						break;
					case DEFENDER:
						writeD(0x03); // waiting approved
						break;
					default:
						writeD(0x00);
						break;
				}
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); //AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
			for(L2SiegeClan siegeclan : _castle.getSiege().getDefenderWaitingClans())
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				writeD(clan.getClanId());
				writeS(clan.getName());
				writeS(clan.getLeaderName());
				writeD(clan.getCrestId());
				writeD(0x00); //signed time (seconds) (not storated by L2J)
				writeD(0x02); //waiting approval
				writeD(clan.getAllyId());
				writeS(clan.getAllyName());
				writeS(""); //AllyLeaderName
				writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			writeD(0x00);
			writeD(0x00);
		}
	}
}
