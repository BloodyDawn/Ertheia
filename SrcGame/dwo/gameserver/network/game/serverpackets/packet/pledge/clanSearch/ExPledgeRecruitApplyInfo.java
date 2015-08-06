package dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 18:26
 */
public class ExPledgeRecruitApplyInfo extends L2GameServerPacket
{
	private ClanSearchWindowStatusType _state;

	public ExPledgeRecruitApplyInfo(ClanSearchWindowStatusType state)
	{
		_state = state;
	}

	/*
			От пакета зависит появление кнопок в окне

			0 - дефолт   ( для кла горит кнопка регистрация )
			1 = список заявлений
			2 - регистрация клана
			3 - хз
			4 - вроде ожидающий добовления
	 */

	@Override
	protected void writeImpl()
	{
		writeD(_state.ordinal()); // d  - 0 1 2 3 4
	}

	public static enum ClanSearchWindowStatusType
	{
		STATUS_TYPE_DEFAULT,
		STATUS_TYPE_ORDER_LIST,
		STATUS_TYPE_CLAN_REG, // Зарегистрировался
		STATUS_TYPE_UNKNOWN,
		STATUS_TYPE_WAITING
	}

}
