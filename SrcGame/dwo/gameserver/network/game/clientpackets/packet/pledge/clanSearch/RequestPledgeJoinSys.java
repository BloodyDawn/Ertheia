package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:27
 */
public class RequestPledgeJoinSys extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Пусто
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
		}

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeJoinSys");
	}

	@Override
	public String getType()
	{
		return "[C] D0:EC RequestPledgeJoinSys";
	}
}
