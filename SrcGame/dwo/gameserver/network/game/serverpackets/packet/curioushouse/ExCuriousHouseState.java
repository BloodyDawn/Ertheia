package dwo.gameserver.network.game.serverpackets.packet.curioushouse;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 28.09.12
 * Time: 19:19
 */
public class ExCuriousHouseState extends L2GameServerPacket
{
	private ChaosFestivalInviteState _state;

	public ExCuriousHouseState(ChaosFestivalInviteState state)
	{
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_state.ordinal()); // GamingState видел 0 и 1 ( при 1 в клиенте идет ShowWindow )
	}

	public enum ChaosFestivalInviteState
	{
		IDLE,
		INVITE,
		PREPARE
	}
}
