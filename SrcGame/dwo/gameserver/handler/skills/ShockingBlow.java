package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;

/**
 * User: Deazer
 * Date: 20.06.13
 * Time: 10:50
 */
public class ShockingBlow implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.CANCEL_CELESTIAL,
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isDead())
		{
			return;
		}
		for(L2Object obj : targets)
		{
			L2PcInstance player = obj.getActingPlayer();
			if(player == null)
			{
				continue;
			}
			for(L2Effect e : player.getAllEffects())
			{
				if(e != null && e.getEffectType() == L2EffectType.INVINCIBLE && e.getSkill().canBeDispeled())
				{
					e.exit();
				}
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}