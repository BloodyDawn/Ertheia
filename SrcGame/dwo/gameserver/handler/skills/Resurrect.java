package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.Ressurection;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import javolution.util.FastList;

import java.util.List;

public class Resurrect implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.RESURRECT
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		L2PcInstance player = null;
		if(activeChar.isPlayer())
		{
			player = activeChar.getActingPlayer();
		}

		L2Playable targetPlayer;
		List<L2Character> targetToRes = new FastList<>();

		if(EventManager.isStarted() && EventManager.isPlayerParticipant(player))
		{
			activeChar.sendMessage("You cannot use that skill in Event");
			activeChar.abortCast();
			return;
		}

		for(L2Object obj : targets)
		{
			if(!(obj instanceof L2Character))
			{
				continue;
			}

			targetPlayer = (L2Playable) obj;

			if(targetPlayer.isVisible())
			{
				targetToRes.add(targetPlayer);
			}
		}

		if(targetToRes.isEmpty())
		{
			activeChar.abortCast();
			return;
		}

		for(L2Character cha : targetToRes)
		{
			if(activeChar instanceof L2PcInstance)
			{
				if(cha instanceof L2PcInstance)
				{
					((L2PcInstance) cha).reviveRequest((L2PcInstance) activeChar, skill, -1);
				}
				else if(cha instanceof L2PetInstance)
				{
					((L2PetInstance) cha).getOwner().reviveRequest((L2PcInstance) activeChar, skill, cha.getObjectId());
				}
			}
			else
			{
				DecayTaskManager.getInstance().cancelDecayTask(cha);
				cha.doRevive(Ressurection.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
			}
		}
		activeChar.spsUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
