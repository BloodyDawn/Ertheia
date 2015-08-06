package dwo.gameserver.network.game.serverpackets.packet.gmview;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class GMViewPledgeInfo extends L2GameServerPacket
{
	private L2Clan _clan;
	private L2PcInstance _activeChar;

	public GMViewPledgeInfo(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
	}

	@Override
	protected void writeImpl()
	{
		writeS(_activeChar.getName());
		writeD(_clan.getClanId());
		writeD(0x00);
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		writeD(_clan.getCrestId()); // -> no, it's no longer used (nuocnam) fix by game
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getClanhallId());
		writeD(_clan.getFortId());
		writeD(0x00); // GOD
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00); // GOD
		writeD(0x00); // GOD
		writeD(_clan.getAllyId()); //c2
		writeS(_clan.getAllyName()); //c2
		writeD(_clan.getAllyCrestId()); //c2
		writeD(_clan.isAtWar() ? 1 : 0); //c3
		writeD(0); // T3 Unknown
		writeD(_clan.getMembers().length);

		for(L2ClanMember member : _clan.getMembers())
		{
			if(member != null)
			{
				writeS(member.getName());
				writeD(member.getLevel());
				writeD(member.getClassIdNew());
				writeD(member.getSex() ? 1 : 0);
				writeD(member.getRaceOrdinal());
				writeD(member.isOnline() ? member.getObjectId() : 0);
				writeD(member.getSponsor() != 0 ? 1 : 0);
			}
		}
	}
}
