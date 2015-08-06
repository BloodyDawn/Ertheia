package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.player.L2Mentee;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 23.11.12
 * Time: 2:31
 */

public class TargetMentee implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		L2Mentee mentor = MentorManager.getInstance().getMentor(target.getObjectId());

		if(mentor != null)
		{
			for(L2Mentee mentees : MentorManager.getInstance().getMentees(mentor.getObjectId()))
			{
				if(mentees != null && mentees.getPlayerInstance().equals(target))
				{
					return new L2Character[]{mentees.getPlayerInstance()};
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
					return _emptyTargetList;
				}
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return _emptyTargetList;
		}

		activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
		return _emptyTargetList;
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_MENTEE;
	}
}
