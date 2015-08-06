package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.L2GameClient;

/**
 * @author Keiichi
 */

public class RequestSaveKeyMapping extends L2GameClientPacket
{
	private byte[] _config;

	@Override
	protected void readImpl()
	{
		int length = readD();

		if(length > _buf.remaining() || length > Short.MAX_VALUE || length < 0)
		{
			_config = null;
			return;
		}
		_config = new byte[length];
		readB(_config);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null || _config == null)
		{
			return;
		}
		if(getClient().getState() != L2GameClient.GameClientState.IN_GAME)
		{
			return;
		}

		player.setBindConfigData(_config);
	}

	@Override
	public String getType()
	{
		return "[C] D0:22 RequestSaveKeyMapping";
	}
}
