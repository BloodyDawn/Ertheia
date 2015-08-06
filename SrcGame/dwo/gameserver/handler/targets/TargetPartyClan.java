package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TargetPartyClan implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();

		if(onlyFirst)
		{
			return new L2Character[]{activeChar};
		}

		L2PcInstance player = activeChar.getActingPlayer();

		if(player == null)
		{
			return _emptyTargetList;
		}

		targetList.add(player);

		int radius = skill.getSkillRadius();
		boolean hasClan = player.getClan() != null;
		boolean hasParty = player.isInParty();

		if(!player.getPets().isEmpty())
		{
			targetList.addAll(L2Skill.getSummons(activeChar, player, radius, false));
		}

		// if player in clan and not in party
		if(!(hasClan || hasParty))
		{
			return targetList.toArray(new L2Character[targetList.size()]);
		}

		// Get all visible objects in a spherical area near the L2Character
		Collection<L2PcInstance> objs = activeChar.getKnownList().getKnownPlayersInRadius(radius);
		for(L2PcInstance obj : objs)
		{
			if(obj == null)
			{
				continue;
			}

			// olympiad mode - adding only own side
			if(player.getOlympiadController().isParticipating())
			{
				if(!obj.getOlympiadController().isParticipating())
				{
					continue;
				}
				if(player.getOlympiadController().isParticipating() != obj.getOlympiadController().isParticipating())
				{
					continue;
				}
				if(player.getOlympiadController().getSide() != obj.getOlympiadController().getSide())
				{
					continue;
				}
			}

			if(player.isInDuel())
			{
				if(player.getDuelId() != obj.getDuelId())
				{
					continue;
				}

				if(hasParty && obj.isInParty() && player.getParty().getLeaderObjectId() != obj.getParty().getLeaderObjectId())
				{
					continue;
				}
			}

			if(!(hasClan && obj.getClanId() == player.getClanId() || hasParty && obj.isInParty() && player.getParty().getLeaderObjectId() == obj.getParty().getLeaderObjectId()))
			{
				continue;
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

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_PARTY_CLAN;
	}
}
