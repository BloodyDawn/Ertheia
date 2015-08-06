package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.jump.L2Jump;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 21.06.11
 * Time: 13:39
 */

public class RequestFlyMove extends L2GameClientPacket
{
	private int _nextPoint;

	@Override
	protected void readImpl()
	{
		_nextPoint = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
		{
			return;
		}

		if(player.isJumping())
		{
			L2Jump jump = new L2Jump(player);
			jump.processJump(player, _nextPoint);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:94 RequestFlyMove";
	}
}