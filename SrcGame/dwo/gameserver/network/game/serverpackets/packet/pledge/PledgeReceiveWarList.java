package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author  -Wooden-
 */

public class PledgeReceiveWarList extends L2GameServerPacket
{
	private L2Clan _clan;
	private int _page;
	private int _state;

	public PledgeReceiveWarList(L2Clan clan, int page, int state)
	{
		_clan = clan;
		_page = page;
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_page);

		writeD(_clan.getClanWars().size());
		for(ClanWar war : _clan.getClanWars())
		{
			// Detect enemy clan
			L2Clan opposingClan = ClanTable.getInstance().getClan(war.getAttackerClanId());
			if(opposingClan.equals(_clan))
			{
				opposingClan = ClanTable.getInstance().getClan(war.getOpposingClanId());
			}

			if(opposingClan == null)
			{
				continue;
			}

			int pointDiff = war.getPointDiff(_clan);
			int duration = (int) (war.getPeriodDuration() / 1000);
			if(war.getClanWarState(_clan).ordinal() >= 3)
			{
				duration += 60 * 60 * 24 * 2; // Add 2 days for ending states. Why? Ask korean dev.
			}
			else if(war.getClanWarState(_clan).ordinal() <= 1)
			{
				duration += 60 * 60 * 24 * 4; // Add 4 days for starting states. Why? Ask korean dev.
			}

			writeS(opposingClan.getName());            //  ClanName
			writeD(war.getClanWarState(_clan).ordinal());                //  State  0=Объявление, 1=Не объявлене, 2=Война, 3=Победа, 4=Поражене, 5=Ничья
			writeD(duration);                    //  ProgressTimeInSec   время этапа в секундах.  макс значение ( state= 0, 1 = 7д   2 = 21д  3,4,5 = 7д )
			// god
			writeD(pointDiff);                    //  Point    количество убист ( очки )
			writeD(war.calculateWarProgress(pointDiff).ordinal());                //  PointDiff
			writeD(opposingClan.getMembersCount());                //  LeftKillCountOfEnemyToWar   условие войны ( количество варов )
		}
	}
}
