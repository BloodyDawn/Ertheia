package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.PartySearchingManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 19.10.11
 * Time: 19:32
 */

// UC Data: native final function RequestRegistWaitingSubstitute (int admission);
public class RequestRegistWaitingSubstitute extends L2GameClientPacket
{
	private int _admission;

	@Override
	protected void readImpl()
	{
		_admission = readD(); // 0 - удаление из списка 1 - добовление в скисок
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(_admission == 1)
		{
			PartySearchingManager.getInstance().addToWaitingList(player);
		}
		else
		{
			PartySearchingManager.getInstance().deleteFromWaitingList(player, false);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:AA RequestRegistWaitingSubstitute";
	}
}

