package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.11.11
 * Time: 9:54
 */

// UC Data: native final function RequestRegistPartySubstitute (int UserID);
public class RequestRegistPartySubstitute extends L2GameClientPacket
{
	private int _charId;

	@Override
	protected void readImpl()
	{
		_charId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		L2Party party = player.getParty();
		if(party == null)
		{
			return;
		}

		L2PcInstance subs = WorldManager.getInstance().getPlayer(_charId);
		if(subs == null)
		{
			return;
		}

		if(!party.getMembers().contains(subs))
		{
			return;
		}

		party.tryReplaceMember(subs);
	}

	@Override
	public String getType()
	{
		return "[C] D0:A8 RequestRegistPartySubstitute";
	}
}
