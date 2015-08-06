package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.config.scripts.ConfigChaosFestival;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 28.09.12
 * Time: 19:20
 */
public class ExCuriousHouseMemberList extends L2GameServerPacket
{
	private final List<L2PcInstance> _players;

	public ExCuriousHouseMemberList(List<L2PcInstance> players)
	{
		_players = players;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x01);   // начало отсчета ( сразу идет +1 / т.е если 0 первый игрок будет Игрок1 )
		writeD(ConfigChaosFestival.CHAOS_FESTIVAL_MAX_PLAYERS_PER_MATCH);   // Макс число плееров

		writeD(_players.size()); // Размер массива
		int number = 0;
		for(L2PcInstance player : _players)
		{
			writeD(player.getObjectId()); // objID
			writeD(++number);
			writeD(player.getMaxHp());    // MaxHP
			writeD(player.getMaxCp());    // MaxCP
			writeD((int) player.getCurrentHp());    // CurrentHP
			writeD((int) player.getCurrentCp());    // CurrentCP
		}
	}
}
