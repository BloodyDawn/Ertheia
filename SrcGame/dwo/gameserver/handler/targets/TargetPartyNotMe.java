package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author UnAfraid
 *
 */
public class TargetPartyNotMe implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if(target != null && !target.isDead() && !activeChar.equals(target) && (activeChar.isInParty() && target.isInParty() && activeChar.getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId() ||
			activeChar instanceof L2PcInstance && target instanceof L2Summon && activeChar.getPets().contains(target) ||
			activeChar instanceof L2Summon && target instanceof L2PcInstance && target.getPets().contains(activeChar)))
		{
			return new L2Character[]{target};
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return _emptyTargetList;
		}
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_PARTY_NOTME;
	}
}
