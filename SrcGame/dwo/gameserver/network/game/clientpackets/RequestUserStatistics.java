package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.worldstat.StatisticContainer;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.game.serverpackets.packet.statistic.ExLoadStatUser;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.02.12
 * Time: 9:07
 */
public class RequestUserStatistics extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Триггер
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}
		// TODO проверка на флуд на 1 мин
		List<StatisticContainer> statUser = WorldStatisticsManager.getInstance().getStatUser(activeChar.getObjectId(), activeChar.getClanId());
		if(statUser != null)
		{
			activeChar.sendPacket(new ExLoadStatUser(statUser));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:A7 RequestUserStatistics";
	}
}
