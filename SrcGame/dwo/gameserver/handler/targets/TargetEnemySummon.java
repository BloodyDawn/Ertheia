package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

/**
 * @author UnAfraid
 *
 */
public class TargetEnemySummon implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		if(target instanceof L2Summon)
		{
			L2Summon targetSummon = (L2Summon) target;
			if(activeChar instanceof L2PcInstance && !activeChar.getPets().contains(targetSummon) && !targetSummon.isDead() && (targetSummon.getOwner().getPvPFlagController().isFlagged() || targetSummon.getOwner().hasBadReputation()) || targetSummon.getOwner().isInsideZone(L2Character.ZONE_PVP) && activeChar.isInsideZone(L2Character.ZONE_PVP) || targetSummon.getOwner().isInDuel() && ((L2PcInstance) activeChar).isInDuel() && targetSummon.getOwner().getDuelId() == ((L2PcInstance) activeChar).getDuelId())
			{
				return new L2Character[]{targetSummon};
			}
		}
		return _emptyTargetList;
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_ENEMY_SUMMON;
	}
}
