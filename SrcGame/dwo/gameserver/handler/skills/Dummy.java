package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager.ArenaParticipantsHolder;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2BlockInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;

public class Dummy implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.DUMMY
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		switch(skill.getId())
		{
			case 5852:
			case 5853:
				L2Object obj = targets[0];
				if(obj != null)
				{
					useBlockCheckerSkill((L2PcInstance) activeChar, skill, obj);
				}
				break;
			default:
				if(skill.hasEffects())
				{
					for(L2Character cha : (L2Character[]) targets)
					{
						skill.getEffects(activeChar, cha);
					}
				}
				break;
		}
		if(skill.useSpiritShot())
		{
			activeChar.spsUncharge(skill);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}

	private void useBlockCheckerSkill(L2PcInstance activeChar, L2Skill skill, L2Object target)
	{
		if(!(target instanceof L2BlockInstance))
		{
			return;
		}

		L2BlockInstance block = (L2BlockInstance) target;

		int arena = activeChar.getEventController().getHandysBlockCheckerEventArena();
		if(arena != -1)
		{
			ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
			if(holder == null)
			{
				return;
			}

			int team = holder.getPlayerTeam(activeChar);
			int color = block.getColorEffect();
			if(team == 0 && color == 0x00 || team == 1 && color == 0x53)
			{
				block.changeColor(activeChar, holder, team);
			}
		}
	}
}