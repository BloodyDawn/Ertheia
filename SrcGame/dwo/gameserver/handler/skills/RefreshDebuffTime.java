package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillRefreshDebuffTime;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;

import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.10.11
 * Time: 20:20
 */

public class RefreshDebuffTime implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.REFRESH_DEFUFF_TIME
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance) || targets.length <= 0)
		{
			return;
		}

		L2Character target = (L2Character) targets[0];

		if(target.isDead() || target.isAlikeDead())
		{
			return;
		}

		for(L2Effect effect : target.getAllEffects())
		{
			if(effect == null || effect.getSkill() == null)
			{
				continue;
			}

			if(effect.getSkill().getSkillType() != L2SkillType.DEBUFF && !effect.getSkill().isDebuff())
			{
				continue;
			}

			List<L2EffectType> refreshableEffectTypes = ((L2SkillRefreshDebuffTime) skill).getRefreshableEffectTypes();
			if(refreshableEffectTypes.isEmpty() || refreshableEffectTypes.contains(effect.getEffectType()))
			{
				effect.restart();
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
