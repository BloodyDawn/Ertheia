package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 28.09.12
 * Time: 19:20
 * Пакет для обновления окна с хп игроков ( на скрине слева http://godworld.ru/scrupload/i/b71a86.png )
 *  для каждого игрока шлется отдельный пакет!!!
 */
public class ExCuriousHouseMemberUpdate extends L2GameServerPacket
{
	private L2PcInstance _player;

	public ExCuriousHouseMemberUpdate(L2PcInstance player)
	{
		_player = player;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_player.getObjectId()); // обджекИд игрока
		writeD(_player.getMaxHp());    // MaxHP
		writeD(_player.getMaxCp());    // MaxCP
		writeD((int) _player.getCurrentHp());    // CurrentHP
		writeD((int) _player.getCurrentCp());    // CurrentCP
	}
}
