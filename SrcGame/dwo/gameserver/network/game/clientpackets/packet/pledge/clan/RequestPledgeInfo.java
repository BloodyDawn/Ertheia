package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeInfo;

public class RequestPledgeInfo extends L2GameClientPacket
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
		L2PcInstance activeChar = getClient().getActiveChar();

		if(activeChar == null)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClan(_clanId);
		if(clan == null)
		{
			return; // we have no clan data ?!? should not happen
		}

		activeChar.sendPacket(new PledgeInfo(clan));
	}

	@Override
	public String getType()
	{
		return "[C] 66 RequestPledgeInfo";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
