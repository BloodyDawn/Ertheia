package dwo.gameserver.network.game.serverpackets.packet.statistic;

import dwo.gameserver.model.holders.WorldStatisticHolder;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 18.06.12
 * Time: 14:30
 */

public class ExLoadStatHotLink extends L2GameServerPacket
{
	private final Collection<WorldStatisticHolder> _monthlyData;
	private final Collection<WorldStatisticHolder> _generalData;
	private int _statueCategoryId;

	public ExLoadStatHotLink(CategoryType statueCategoryId)
	{
		_statueCategoryId = statueCategoryId.getClientId();
		_monthlyData = WorldStatisticsManager.getInstance().getStatisticTop(statueCategoryId, false);
		_generalData = WorldStatisticsManager.getInstance().getStatisticTop(statueCategoryId, true);
	}

	@Override
	protected void writeImpl()
	{
		// Выводит только топ 5 !!
		writeD(_statueCategoryId);
		writeD(0x00);
		int num = 1;
		if(_monthlyData != null && !_monthlyData.isEmpty())
		{

			if(_monthlyData.size() > 5)
			{
				writeD(0x05);
			}
			else
			{
				writeD(_monthlyData.size());
			}

			for(WorldStatisticHolder holder : _monthlyData)
			{
				writeH(num);
				writeD(holder.getObjId());  // m_arrow
				writeS(holder.getName());
				writeQ(holder.getValue());
				writeH(0x00);  // -1 стрелка вниз  0 прочерк  1 стрелка вверх
				if(holder.isClanStatistic())
				{
					writeD(holder.getObjId());
					writeD(holder.getClanCrestId());
				}
				else
				{
					writeD(0x00);
					writeD(0x00);
				}
				if(num == 5)
				{
					break;
				}

				num++;
			}
		}
		else
		{
			writeD(0x00);
		}

		if(_generalData != null && !_generalData.isEmpty())
		{
			num = 1;
			if(_generalData.size() > 5)
			{
				writeD(0x05);
			}
			else
			{
				writeD(_generalData.size());
			}

			for(WorldStatisticHolder holder : _generalData)
			{
				writeH(num);
				writeD(holder.getObjId());
				writeS(holder.getName());
				writeQ(holder.getValue());
				writeH(0x00);  // -1 стрелка вниз  0 прочерк  1 стрелка вверх
				if(holder.isClanStatistic())
				{
					writeD(holder.getObjId());
					writeD(holder.getClanCrestId());
				}
				else
				{
					writeD(0x00);
					writeD(0x00);
				}
				if(num == 5)
				{
					break;
				}

				num++;
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}
