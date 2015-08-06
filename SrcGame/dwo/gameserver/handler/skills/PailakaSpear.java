package dwo.gameserver.handler.skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class PailakaSpear extends Pdam
{
	private static final int CRITICAL_HIT_BUFF = 5760;

	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.PAILAKA_PDAM
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		// Pailaka Spear needs correct 'charge' level of 'Critical Hit' buff
		// Check is done by condition, but here also to prevent cheating
		int needStack = 2 + (skill.getLevel() << 1);
		L2Effect effect = activeChar.getFirstEffect(CRITICAL_HIT_BUFF);
		if(effect != null && effect.getAbnormalLvl() >= needStack)
		{
			// All charges are removed when player use skill
			effect.exit();

			super.useSkill(activeChar, skill, targets);
		}
		else
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}