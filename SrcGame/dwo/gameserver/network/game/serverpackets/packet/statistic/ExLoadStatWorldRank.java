package dwo.gameserver.network.game.serverpackets.packet.statistic;

import dwo.gameserver.model.holders.WorldStatisticHolder;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 22.02.12
 * Time: 4:29
 */

public class ExLoadStatWorldRank extends L2GameServerPacket
{
	private final int _section;
	private final int _subSection;
	private final Collection<WorldStatisticHolder> _monthlyData;
	private final Collection<WorldStatisticHolder> _generalData;

	public ExLoadStatWorldRank(int section, int subSection, Collection<WorldStatisticHolder> monthlyData, Collection<WorldStatisticHolder> generalData)
	{
		_section = section;
		_subSection = subSection;
		_monthlyData = monthlyData;
		_generalData = generalData;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_section);
		writeD(_subSection);

		// Статистика за месяц
		if(_monthlyData != null && !_monthlyData.isEmpty())
		{
			int place = 1;
			writeD(_monthlyData.size());
			for(WorldStatisticHolder holder : _monthlyData)
			{
				writeH(place);
				writeD(holder.getObjId());
				writeS(holder.getName());
				writeQ(holder.getValue());
				writeH(0x00);//  -1 стрелка вниз  0 прочерк  1 стрелка вверх
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
				place++;
			}
		}
		else
		{
			writeD(0x00);
		}

		// Общая статистика за все время существования сервера
		if(_generalData != null && !_generalData.isEmpty())
		{
			int place = 1;
			writeD(_generalData.size());
			for(WorldStatisticHolder holder : _generalData)
			{
				writeH(place);
				writeD(holder.getObjId());
				writeS(holder.getName());
				writeQ(holder.getValue());
				writeH(0x00);//-1 стрелка вниз  0 прочерк  1 стрелка вверх
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
				place++;
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}
