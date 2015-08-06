package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2Mentee;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import javolution.util.FastList;

import java.util.Collections;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 13.10.11
 * Time: 9:26
 */

public class ExMentorList extends L2GameServerPacket
{
	private final int _type;
	private final List<L2Mentee> _mentees = new FastList<>();

	public ExMentorList(L2PcInstance activeChar)
	{
		if(activeChar.isMentor())
		{
			_type = 0x01;
			_mentees.addAll(MentorManager.getInstance().getMentees(activeChar.getObjectId()));
		}
		else if(activeChar.isMentee())
		{
			_type = 0x02;
			_mentees.add(MentorManager.getInstance().getMentor(activeChar.getObjectId()));
		}
        else if (activeChar.isAwakened())
        {
            _type = 0x01;
            _mentees.addAll(Collections.emptyList());
        }
		else
		{
			_type = activeChar.getLevel() >= 85 ? 0x01 : 0x00;
            _mentees.addAll(Collections.emptyList());
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_type);
		writeD(0x00); // ??
		writeD(_mentees.size());
		for(L2Mentee mentee : _mentees)
		{
			writeD(mentee.getObjectId());
			writeS(mentee.getName());
			writeD(mentee.getClassId());
			writeD(mentee.getLevel());
			writeD(mentee.isOnlineInt());
		}
	}
}
