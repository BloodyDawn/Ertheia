package dwo.gameserver.network.game.serverpackets;

public class RestartResponse extends L2GameServerPacket
{
	private static final RestartResponse STATIC_PACKET_TRUE = new RestartResponse(true);
	private static final RestartResponse STATIC_PACKET_FALSE = new RestartResponse(false);
	private boolean _result;

	public RestartResponse(boolean result)
	{
		_result = result;
	}

	public static RestartResponse valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result ? 1 : 0);
	}
}
