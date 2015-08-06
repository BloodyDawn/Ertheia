package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author -Wooden-
 */

public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private int _pledgeType;
	private int _hasSponsor;
	private String _name;
	private int _level;
	private int _classId;
	private int _objectId;
	private boolean _isOnline;
	private int _race;
	private int _sex;

	public PledgeShowMemberListUpdate(L2PcInstance player)
	{
		_pledgeType = player.getPledgeType();
		if(_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = player.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getActiveClassId();
		_race = player.getRace().ordinal();
		_sex = player.getAppearance().getSex() ? 1 : 0;
		_objectId = player.getObjectId();
		_isOnline = player.isOnline();
	}

	public PledgeShowMemberListUpdate(L2ClanMember member)
	{
		_name = member.getName();
		_level = member.getLevel();
		_classId = member.getClassIdNew();
		_objectId = member.getObjectId();
		_isOnline = member.isOnline();
		_pledgeType = member.getPledgeType();
		_race = member.getRaceOrdinal();
		_sex = member.getSex() ? 1 : 0;
		if(_pledgeType == L2Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = member.getSponsor() != 0 ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
	}

	@Override
	protected void writeImpl()
	{
		writeS(_name);
		writeD(_level);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		if(_isOnline)
		{
			writeD(_objectId);
			writeD(_pledgeType);
		}
		else
		{
			// when going offline send as 0
			writeD(0x00);
			writeD(0x00);
		}
		writeD(_hasSponsor);
	}
}
