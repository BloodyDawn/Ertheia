package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author littlecrow
 */

public class Confusion extends L2Effect
{

	public Confusion(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.CONFUSION;
	}

	@Override
	public boolean onStart()
	{
		getEffected().startAbnormalEffect(AbnormalEffect.FEAR);
		getEffected().startConfused();
		onActionTime();
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.FEAR);
		getEffected().stopConfused(this);
	}

	@Override
	public boolean onActionTime()
	{
		List<L2Character> targetList = new FastList<>();

		// Getting the possible targets
		targetList.addAll(getEffected().getKnownList().getKnownObjects().values().stream().filter(obj -> obj instanceof L2Character && !obj.equals(getEffected())).map(obj -> (L2Character) obj).collect(Collectors.toList()));
		// if there is no target, exit function
		if(targetList.isEmpty())
		{
			return false;
		}

		// Choosing randomly a new target
		int nextTargetIdx = Rnd.get(targetList.size());
		L2Object target = targetList.get(nextTargetIdx);

		// Attacking the target
		getEffected().setTarget(target);
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);

		return getSkill().isToggle();
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_CONFUSED;
	}
}