package dwo.gameserver.network.game.serverpackets.packet.statistic;

import dwo.gameserver.model.world.worldstat.StatisticContainer;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO, Bacek
 * Date: 12.05.12
 * Time: 16:06
 */

public class ExLoadStatUser extends L2GameServerPacket
{
	private List<StatisticContainer> _list;

	public ExLoadStatUser(List<StatisticContainer> list)
	{
		_list = list;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_list.size());
		for(StatisticContainer stat : _list)
		{
			writeD(stat.getId().getClientId());
			writeD(stat.getSubId());
			writeQ(stat.getMonthlyStatisticCount());
			writeQ(stat.getGeneralStatisticCount());
		}
	}
}
