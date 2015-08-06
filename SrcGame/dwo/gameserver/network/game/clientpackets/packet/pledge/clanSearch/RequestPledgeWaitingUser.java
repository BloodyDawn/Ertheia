package dwo.gameserver.network.game.clientpackets.packet.pledge.clanSearch;

import dwo.gameserver.instancemanager.ClanSearchManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeWaitingList;
import dwo.gameserver.network.game.serverpackets.packet.pledge.clanSearch.ExPledgeWaitingUser;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 06.12.12
 * Time: 19:26
 */
public class RequestPledgeWaitingUser extends L2GameClientPacket
{
	private int _clanId;
	private int _charId;

	@Override
	protected void readImpl()
	{
		_clanId = readD();
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

		//	if(player.isGM())
		//		player.sendMessage("RequestPledgeWaitingUser _unk: " + _clanId + " _unk1: " + _charId);

		// If user unregistered, re-send pledge list
		if(ClanSearchManager.getInstance().isApplicantRegistered(_clanId, _charId))
		{
			player.sendPacket(new ExPledgeWaitingUser(_charId, ClanSearchManager.getInstance().getApplicant(_clanId, _charId).getDesc()));
		}
		else
		{
			player.sendPacket(new ExPledgeWaitingList(_clanId));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:E7 RequestPledgeWaitingUser";
	}
}
