package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.player.formation.clan.SubPledge;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private L2Clan _clan;
	private L2PcInstance _activeChar;
	private L2ClanMember[] _members;
	private int _pledgeType;

	public PledgeShowMemberListAll(L2Clan clan, L2PcInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
		_members = _clan.getMembers();
	}

	@Override
	protected void writeImpl()
	{
		writePledge(null, _clan.getLeaderName());

		for(SubPledge subPledge : _clan.getAllSubPledges())
		{
			_activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(subPledge, _clan));
		}

		for(L2ClanMember m : _members)
		{
			if(m.getPledgeType() == 0)
			{
				continue;
			}
			_activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		}
		_activeChar.sendUserInfo();
	}

	void writePledge(SubPledge pledge, String name)
	{
		int pledgeId = pledge == null ? 0x00 : pledge.getTypeId();

		writeD(pledge == null ? 0 : 1);
		writeD(_clan.getClanId());
		writeD( Config.SERVER_ID);
		writeD(pledgeId);
		writeS(_clan.getName());
		writeS(name);

		writeD(_clan.getCrestId());
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(0x00); //god
		writeD(_clan.getClanhallId());
		writeD(_clan.getFortId());
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00); // GOD
		writeD(0x00); // GOD
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);// new c3
		writeD(0x00); // Territory castle ID
		writeD(_clan.getSubPledgeMembersCount(pledgeId));
		for(L2ClanMember m : _clan.getSubPledgeMembers(pledgeId))
		{
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassIdNew());

			L2PcInstance player;
			if((player = m.getPlayerInstance()) != null)
			{
				writeD(player.getAppearance().getSex() ? 0x01 : 0x00); // no visible effect
				writeD(player.getRace().ordinal()); //writeD(0x01);
			}
			else
			{
				writeD(0x01); // no visible effect
				writeD(0x01); //writeD(1);
			}
			writeD(m.isOnline() ? m.getObjectId() : 0x00); // objectId=online 0=offline
			writeD(m.getSponsor() != 0 ? 0x01 : 0x00);
		}
	}
}
