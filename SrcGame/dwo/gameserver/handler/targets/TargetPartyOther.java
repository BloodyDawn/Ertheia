package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 *
 */
public class TargetPartyOther implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if(target != null && !target.equals(activeChar) && activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId())
		{
			if(target.isDead())
			{
				return _emptyTargetList;
			}
			else
			{
				if(target instanceof L2PcInstance)
				{
					switch(skill.getId())
					{
						// FORCE BUFFS may cancel here but there should be a proper condition
						case 426:
							return !((L2PcInstance) target).isMageClass() ? new L2Character[]{
								target
							} : _emptyTargetList;
						case 427:
							return ((L2PcInstance) target).isMageClass() ? new L2Character[]{target} : _emptyTargetList;
					}
				}
				return new L2Character[]{target};
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
			return _emptyTargetList;
		}
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_PARTY_OTHER;
	}
}
