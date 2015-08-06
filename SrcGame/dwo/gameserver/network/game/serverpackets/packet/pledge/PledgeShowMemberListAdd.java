package dwo.gameserver.network.game.serverpackets.packet.pledge;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private String _name;
	private int _lvl;
	private int _classId;
	private int _isOnline;
	private int _pledgeType;

	public PledgeShowMemberListAdd(L2PcInstance player)
	{
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getActiveClassId();
		_isOnline = player.isOnline() ? player.getObjectId() : 0;
		_pledgeType = player.getPledgeType();
	}

	public PledgeShowMemberListAdd(L2ClanMember cm)
	{
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassIdNew();
		_isOnline = cm.isOnline() ? cm.getObjectId() : 0;
		_pledgeType = cm.getPledgeType();
	}

	@Override
	protected void writeImpl()
	{
		writeS(_name);
		writeD(_lvl);
		writeD(_classId);
		writeD(0);
		writeD(1);
		writeD(_isOnline);
		writeD(_pledgeType);
	}
}
