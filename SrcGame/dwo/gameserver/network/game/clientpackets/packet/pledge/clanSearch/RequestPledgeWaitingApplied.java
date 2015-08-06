package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ClanSearchPlayerHolder;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeWaitingListApplied;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 */
public class RequestPledgeWaitingApplied extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		//	if (player.isGM())
		//		player.sendMessage("RequestPledgeWaitingApplied");

		ClanSearchPlayerHolder playerHolder = ClanSearchManager.getInstance().findAnyApplicant(player.getObjectId());
		if(playerHolder != null)
		{
			player.sendPacket(new ExPledgeWaitingListApplied(playerHolder));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:E5 RequestPledgeWaitingApplied";
	}
}
