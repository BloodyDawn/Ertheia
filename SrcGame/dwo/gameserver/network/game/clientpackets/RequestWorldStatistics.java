package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.game.serverpackets.packet.statistic.ExLoadStatWorldRank;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.02.12
 * Time: 9:07
 */

public class RequestWorldStatistics extends L2GameClientPacket
{
	private int _section;
	private int _subSection;

	@Override
	protected void readImpl()
	{
		_section = readD();
		_subSection = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		CategoryType cat = CategoryType.getCategoryById(_section, _subSection);
		if(cat == null)
		{
			return;
		}

		activeChar.sendPacket(new ExLoadStatWorldRank(_section, _subSection, WorldStatisticsManager.getInstance().getStatisticTop(cat, false), WorldStatisticsManager.getInstance().getStatisticTop(cat, true)));

		if(activeChar.isGM())
		{
			activeChar.sendMessage("Секция: " + _section + " Подсекция: " + _subSection);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:A6 RequestWorldStatistics";
	}
}
