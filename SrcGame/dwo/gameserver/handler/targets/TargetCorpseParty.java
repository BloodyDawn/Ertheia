package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.10.11
 * Time: 17:10
 */

public class TargetCorpseParty implements ITargetTypeHandler
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

			int radius = skill.getSkillRadius();
			L2Party party = player.getParty();

			if(party == null)
			{
				return _emptyTargetList;
			}

			if(!player.getPets().isEmpty())
			{
				targetList.addAll(L2Skill.getSummons(activeChar, player, radius, true));
			}

			for(L2PcInstance member : party.getMembers())
			{
				if(member == null || member.equals(player))
				{
					continue;
				}

				if(player.isInDuel())
				{
					if(player.getDuelId() != member.getDuelId())
					{
						continue;
					}
					if(player.isInParty() && member.isInParty() && player.getParty().getLeaderObjectId() != member.getParty().getLeaderObjectId())
					{
						continue;
					}
				}

				// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
				if(!player.checkPvpSkill(member, skill))
				{
					continue;
				}

				if(!onlyFirst && !member.getPets().isEmpty())
				{
					targetList.addAll(L2Skill.getSummons(activeChar, member, radius, true));
				}

				if(!L2Skill.addCharacter(activeChar, member, radius, true))
				{
					continue;
				}

				if(skill.getSkillType() == L2SkillType.RESURRECT)
				{
					// check target is not in a active siege zone
					if(member.isInsideZone(L2Character.ZONE_SIEGE) && !member.isInSiege())
					{
						continue;
					}
				}

				if(onlyFirst)
				{
					return new L2Character[]{member};
				}

				if(skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
				{
					break;
				}

				targetList.add(member);
			}
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CORPSE_PARTY;
	}
}