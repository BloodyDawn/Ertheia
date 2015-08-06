package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStopScenePlayer;

/**
 * L2GOD Team
 * User: ANZO,Bacek
 * Date: 18.10.11
 * Time: 8:50
 */

public class RequestExEscapeScene extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		activeChar.setMovieId(0);
		activeChar.sendPacket(new ExStopScenePlayer());
	}

	@Override
	public String getType()
	{
		return "[C] D0:93 RequestExEscapeScene";
	}
}
