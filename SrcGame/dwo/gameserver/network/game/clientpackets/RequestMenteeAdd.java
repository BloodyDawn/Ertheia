package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExMentorAdd;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:26
 */

public class RequestMenteeAdd extends L2GameClientPacket
{
	private String _target;

	@Override
	protected void readImpl()
	{
		_target = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance mentor = getClient().getActiveChar();
		if(mentor == null)
		{
			return;
		}

		L2PcInstance mentee = WorldManager.getInstance().getPlayer(_target);
		if(mentee == null)
		{
			return;
		}

		if(ConfirmMenteeAdd.validate(mentor, mentee))
		{
			mentor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_OFFERED_TO_BECOME_S1_MENTOR).addCharName(mentee));
			mentee.sendPacket(new ExMentorAdd(mentor));
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:BF RequestMenteeAdd";
	}
}
