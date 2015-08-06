package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.AccountShareDataTable;
import dwo.gameserver.model.player.AccountShareData;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * Предназначен для общей виталити системы
 * для всех персонажей на аккаунте.
 */

public class ExLoginVitalityEffectInfo extends L2GameServerPacket
{
	private final int _points;

	public ExLoginVitalityEffectInfo(String account)
	{
		AccountShareData data = AccountShareDataTable.getInstance().getAccountData(account, "player_vitality_items_left", "5");
		_points = data.getIntValue();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_points > 0 ? Math.round(Config.RATE_VITALITY * 100) : 0);
		writeD(_points);
	}
}