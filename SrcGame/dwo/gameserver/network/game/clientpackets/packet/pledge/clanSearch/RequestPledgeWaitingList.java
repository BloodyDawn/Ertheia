package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeWaitingList;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 *
 * Список заявок в клан.
 */
public class RequestPledgeWaitingList extends L2GameClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeWaitingList unk: " + _clanId);

		player.sendPacket(new ExPledgeWaitingList(_clanId));
	}

	@Override
	public String getType()
	{
		return "[C] D0:E6 RequestPledgeWaitingList";
	}
}
