package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 21.09.11
 * Time: 12:57
 */

public class BlockSkills extends L2Effect
{
	public BlockSkills(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_SKILLS;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			getEffected().disableAllSkills();
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			getEffected().enableAllSkills();
		}
	}

	@Override
	public int getEffectFlags()
	{
		// TODO: Возможно понадобится в будующем, сейчас просто абы был
		return CharEffectList.EFFECT_FLAG_BLOCK_SKILLS;
	}
}
