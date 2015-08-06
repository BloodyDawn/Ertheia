package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private L2ClanMember _member;

	public PledgeReceivePowerInfo(L2ClanMember member)
	{
		_member = member;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_member.getPowerGrade());
		writeS(_member.getName());
		writeD(_member.getClan().getRankPrivs(_member.getPowerGrade()));
	}
}
