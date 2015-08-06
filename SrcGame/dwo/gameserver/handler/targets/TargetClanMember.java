package dwo.gameserver.handler.targets;

import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.network.game.components.SystemMessageId;
import javolution.util.FastList;

import java.util.List;

public class TargetClanMember implements ITargetTypeHandler
{
	@Override
	public L2Object[] getTargetList(L2Skill skill, L2Character activeChar, boolean onlyFirst, L2Character target)
	{
		List<L2Character> targetList = new FastList<>();
		if(activeChar instanceof L2Npc)
		{
			// for buff purposes, returns friendly mobs nearby and mob itself
			L2Npc npc = (L2Npc) activeChar;
			if(npc.getFactionId() == null || npc.getFactionId().isEmpty())
			{
				return new L2Character[]{activeChar};
			}
			for(L2Object newTarget : activeChar.getKnownList().getKnownCharactersInRadius(skill.getCastRange()))
			{
				if(newTarget instanceof L2Npc && npc.getFactionId().equals(((L2Npc) newTarget).getFactionId()))
				{
					if(((L2Npc) newTarget).getFirstEffect(skill) != null)
					{
						continue;
					}
					targetList.add((L2Npc) newTarget);
					break;
				}
			}
			if(targetList.isEmpty())
			{
				targetList.add(npc);
			}
		}
		else if(activeChar instanceof L2Playable)
		{
			L2PcInstance player = activeChar.getActingPlayer();

			// Возможность кастовать на себя без клана
			if(target != null && player.equals(target.getActingPlayer()))
			{
				return new L2Character[]{target};
			}

			// Проверка на принадлежность цели к клану текущего игрока
			if(target == null || target.isDead() || !target.isPlayable() || target.getActingPlayer().getClan() == null || player.getClan() == null && !player.equals(target.getActingPlayer()) || !player.getClan().isMember(target.getActingPlayer().getObjectId()))
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return _emptyTargetList;
			}
			else
			{
				return new L2Character[]{target};
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
		return L2TargetType.TARGET_CLAN_MEMBER;
	}
}
