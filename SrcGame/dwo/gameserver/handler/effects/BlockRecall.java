package dwo.gameserver.handler.effects;

import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 27.04.13
 * Time: 14:24
 */
public class BlockRecall extends L2Effect
{
	public BlockRecall(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BLOCK_RECALL;
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_BLOCK_RECALL;
	}
}
