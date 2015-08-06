package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Map;

/**
 * @author -Wooden-
 * Format from KenM
 * Re-written by godson
 */

public class ExHeroList extends L2GameServerPacket
{
	private Map<Integer, StatsSet> _heroList;

	public ExHeroList()
	{
		_heroList = HeroManager.getInstance().getHeroes();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_heroList.size());

		for(Map.Entry<Integer, StatsSet> integerStatsSetEntry : _heroList.entrySet())
		{
			StatsSet hero = integerStatsSetEntry.getValue();
			writeS(hero.getString(Olympiad.CHAR_NAME));
			writeD(hero.getInteger(Olympiad.CLASS_ID));
			writeS(hero.getString(HeroManager.CLAN_NAME, ""));
			writeD(hero.getInteger(HeroManager.CLAN_CREST, 0));
			writeS(hero.getString(HeroManager.ALLY_NAME, ""));
			writeD(hero.getInteger(HeroManager.ALLY_CREST, 0));
			writeD(hero.getInteger(HeroManager.COUNT));
		}
	}
}