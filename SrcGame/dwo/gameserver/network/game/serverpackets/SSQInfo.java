package dwo.gameserver.network.game.serverpackets;

public class SSQInfo extends L2GameServerPacket
{
	private int _state;

	public SSQInfo(int state)
	{
		_state = state;
	}

	@Override
	protected void writeImpl()
	{
		if(_state == 2) // Dawn Sky
		{
			writeH(258);
		}
		else if(_state == 1) // Dusk Sky
		{
			writeH(257);
		}
		else
		{
			writeH(256);
		}
	}
}
