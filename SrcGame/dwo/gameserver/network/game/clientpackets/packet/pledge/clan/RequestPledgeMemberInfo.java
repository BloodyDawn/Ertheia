package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeReceiveMemberInfo;

public class RequestPledgeMemberInfo extends L2GameClientPacket
{
	private int _pledgeId;
	private String _player;

	@Override
	protected void readImpl()
	{
		_pledgeId = readD();
		_player = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		//do we need powers to do that??
		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}
		L2ClanMember member = clan.getClanMember(_player);
		if(member == null)
		{
			return;
		}
		activeChar.sendPacket(new PledgeReceiveMemberInfo(member));
	}

	@Override
	public String getType()
	{
		return "[C] D0:1D RequestPledgeMemberInfo";
	}
}