package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetParty implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if(onlyFirst)
		{
			return new L2Character[]{activeChar};
		}

		targetList.add(activeChar);

		int radius = skill.getSkillRadius();

		L2PcInstance player = activeChar.getActingPlayer();
		if(activeChar instanceof L2Summon)
		{
			if(L2Skill.addCharacter(activeChar, player, radius, false))
			{
				targetList.add(player);
			}
		}
		else if(activeChar instanceof L2PcInstance && !player.getPets().isEmpty())
		{
			targetList.addAll(L2Skill.getSummons(activeChar, player, radius, false));
		}

		if(activeChar.isInParty())
		{
			// Get a list of Party Members
			for(L2PcInstance partyMember : activeChar.getParty().getMembers())
			{
				if(partyMember == null || partyMember.equals(player))
				{
					continue;
				}
				if(skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
				{
					break;
				}
				if(L2Skill.addCharacter(activeChar, partyMember, radius, false))
				{
					targetList.add(partyMember);
				}
				if(!partyMember.getPets().isEmpty())
				{
					targetList.addAll(L2Skill.getSummons(activeChar, partyMember, radius, false));
				}
			}
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_PARTY;
	}
}