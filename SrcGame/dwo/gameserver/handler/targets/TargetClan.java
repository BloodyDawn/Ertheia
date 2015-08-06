package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author UnAfraid
 *
 */
public class TargetClan implements ITargetTypeHandler
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
			L2Clan clan = player.getClan();

			if(!player.getPets().isEmpty())
			{
				targetList.addAll(L2Skill.getSummons(activeChar, player, radius, false));
			}

			if(clan != null)
			{
				L2PcInstance obj;
				for(L2ClanMember member : clan.getMembers())
				{
					obj = member.getPlayerInstance();

					if(obj == null || obj.equals(player))
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

					// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
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
		else if(activeChar instanceof L2Npc)
		{
			// for buff purposes, returns friendly mobs nearby and mob itself
			L2Npc npc = (L2Npc) activeChar;
			if(npc.getFactionId() == null || npc.getFactionId().isEmpty())
			{
				return new L2Character[]{activeChar};
			}

			targetList.add(activeChar);

			for(L2Object newTarget : activeChar.getKnownList().getKnownCharactersInRadius(skill.getCastRange()))
			{
				if(newTarget instanceof L2Npc && npc.getFactionId().equals(((L2Npc) newTarget).getFactionId()))
				{
					if(skill.getMaxTargets() > -1 && targetList.size() >= skill.getMaxTargets())
					{
						break;
					}

					targetList.add((L2Npc) newTarget);
				}
			}
		}

		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_CLAN;
	}
}