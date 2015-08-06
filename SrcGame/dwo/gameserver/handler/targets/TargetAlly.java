package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author UnAfraid
 */

public class TargetAlly implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if(activeChar instanceof L2Playable)
		{
			L2PcInstance player = activeChar.getActingPlayer();

			if(player == null)
			{
				return _emptyTargetList;
			}

			if(player.getOlympiadController().isParticipating())
			{
				return new L2Character[]{player};
			}

			if(onlyFirst)
			{
				return new L2Character[]{player};
			}

			targetList.add(player);

			int radius = skill.getSkillRadius();

			if(!activeChar.getPets().isEmpty())
			{
				targetList.addAll(L2Skill.getSummons(activeChar, player, radius, false));
			}

			if(player.getClan() != null)
			{
				// Get all visible objects in a spherical area near the L2Character
				Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
				for(L2PcInstance obj : objs)
				{
					if(obj == null)
					{
						continue;
					}
					if((obj.getAllyId() == 0 || obj.getAllyId() != player.getAllyId()) && (obj.getClan() == null || obj.getClanId() != player.getClanId()))
					{
						continue;
					}

					if(player.isInDuel())
					{
						if(player.getDuelId() != obj.getDuelId())
						{
							continue;
						}
						if(player.isInParty() && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
						{
							continue;
						}
					}

					// Don't add this target if this is a Pc->Pc pvp
					// casting and pvp condition not met
					if(!player.checkPvpSkill(obj, skill))
					{
						continue;
					}

					if(!EventManager.checkForEventSkill(player, obj, skill))
					{
						continue;
					}

					if(!onlyFirst && !obj.getPets().isEmpty())
					{
						targetList.addAll(L2Skill.getSummons(activeChar, obj, radius, false));
					}

					if(!L2Skill.addCharacter(activeChar, obj, radius, false))
					{
						continue;
					}

					if(onlyFirst)
					{
						return new L2Character[]{obj};
					}

					if(skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
					{
						break;
					}

					targetList.add(obj);
				}
			}
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_ALLY;
	}
}
