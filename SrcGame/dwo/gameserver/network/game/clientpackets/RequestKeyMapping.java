package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExUISetting;

/**
 * @author Keiichi
 */

public class RequestKeyMapping extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger (no data)
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null || player.getBindConfigData() == null)
		{
			return;
		}

		player.sendPacket(new ExUISetting(player));
	}

	@Override
	public String getType()
	{
		return "[C] D0:21 RequestKeyMapping";
	}
}
