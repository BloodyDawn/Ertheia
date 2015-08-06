package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;

import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 24.11.11
 * Time: 14:04
 */

public class TargetCommandChannel implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new ArrayList<>();
		if(activeChar instanceof L2Playable)
		{
			L2PcInstance player = activeChar.getActingPlayer();
			L2Party party = player.getParty();
			if(party == null)
			{
				return _emptyTargetList;
			}
			L2CommandChannel ch = party.getCommandChannel();
			if(ch == null)
			{
				return _emptyTargetList;
			}

			int radius = skill.getSkillRadius();
			// Добавляем членов КК
			for(L2PcInstance ccMember : ch.getMembers())
			{
				if(L2Skill.addCharacter(activeChar, ccMember, radius, !activeChar.isDead()))
				{
					targetList.add(ccMember);
				}

				if(!ccMember.getPets().isEmpty())
				{
					targetList.addAll(L2Skill.getSummons(activeChar, ccMember, radius, !activeChar.isDead()));
				}
			}

			// Добавляем себя
			targetList.add(activeChar);
			if(!activeChar.getPets().isEmpty())
			{
				targetList.addAll(L2Skill.getSummons(activeChar, player, radius, !activeChar.isDead()));
			}
		}
		else
		{
			return _emptyTargetList;
		}
		return targetList.toArray(new L2Character[targetList.size()]);
	}

	@Override
	public Enum<L2TargetType> getTargetType()
	{
		return L2TargetType.TARGET_COMMAND_CHANNEL;
	}
}