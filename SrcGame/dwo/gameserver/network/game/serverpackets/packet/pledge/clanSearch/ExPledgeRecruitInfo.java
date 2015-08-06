package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:26
 *
 * Пакет, отправляемый сервером при добавлении клана в список поиска клана лидером.
 * Impl by Yorie.
 */
public class ExPledgeRecruitInfo extends L2GameServerPacket
{
	private final String _clanName;
	private final String _leaderName;
	private final int _clanLevel;
	private final int _clanMemberCount;

	public ExPledgeRecruitInfo(L2Clan clan)
	{
		_clanName = clan.getName();
		_leaderName = clan.getLeader().getName();
		_clanLevel = clan.getLevel();
		_clanMemberCount = clan.getMembersCount();
	}

	@Override
	protected void writeImpl()
	{
		// SSddd
		writeS(_clanName);          // Имя клана
		writeS(_leaderName);        // Имя лидера
		writeD(_clanLevel);         // Уровень клана
		writeD(_clanMemberCount);   // Количество человек

		// Наверно список клана ( Уточнить !! )   или кто имеен доступ
		int size = 2;
		writeD(size);
		for(int i = 0; i < size; i++)
		{
			writeD(2123);
			writeS("ttttttt");
		}
	}
}
