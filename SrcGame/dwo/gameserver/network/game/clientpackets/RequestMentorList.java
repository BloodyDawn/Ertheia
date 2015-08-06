package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExMentorList;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:23
 */

public class RequestMentorList extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player != null)
		{
			player.sendPacket(new ExMentorList(player));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:BE RequestMentorList";
	}
}
