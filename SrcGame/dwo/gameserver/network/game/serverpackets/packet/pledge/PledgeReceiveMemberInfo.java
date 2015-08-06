package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 *
 * @author  -Wooden-
 */
public class PledgeReceiveMemberInfo extends L2GameServerPacket
{
	private L2ClanMember _member;

	/**
	 * @param member
	 */
	public PledgeReceiveMemberInfo(L2ClanMember member)
	{
		_member = member;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_member.getPledgeType());
		writeS(_member.getName());
		writeS(_member.getTitle()); // title
		writeD(_member.getPowerGrade()); // power

		//clan or subpledge name
		if(_member.getPledgeType() == 0)
		{
			writeS(_member.getClan().getName());
		}
		else
		{
			writeS(_member.getClan().getSubPledge(_member.getPledgeType()).getName());
		}

		writeS(_member.getApprenticeOrSponsorName()); // name of this member's apprentice/sponsor
	}
}
