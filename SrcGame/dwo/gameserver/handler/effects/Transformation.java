package dwo.gameserver.handler.effects;

import dwo.gameserver.instancemanager.TransformationManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Rnd;

/**
 * @author nBd, ANZO
 */

public class Transformation extends L2Effect
{
	public Transformation(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	// Special constructor to steal this effect
	public Transformation(Env env, L2Effect effect)
	{
		super(env, effect);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TRANSFORMATION;
	}

	@Override
	public boolean onStart()
	{
		L2Character target = getEffected();

		if(getEffected() == null)
		{
			return false;
		}

		if(!(getEffected() instanceof L2PcInstance || getEffected() instanceof L2MonsterInstance))
		{
			return false;
		}

		//  Если происходит обновление возвращаем true
		if(isRefreshTime())
		{
			return true;
		}

		if(getEffected() instanceof L2PcInstance)
		{
			if(target.isAlikeDead() || ((L2PcInstance) target).isCursedWeaponEquipped())
			{
				return false;
			}
			if(target.isTransformed() || ((L2PcInstance) target).isInStance())
			{
				target.sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
				return false;
			}
			if(((L2PcInstance) target).isSitting())
			{
				target.sendPacket(SystemMessageId.CANNOT_TRANSFORM_WHILE_SITTING);
				return false;
			}
			if(((L2PcInstance) target).isInWater())
			{
				target.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
				return false;
			}
			if(((L2PcInstance) target).isFlyingMounted() || ((L2PcInstance) target).isMounted() || ((L2PcInstance) target).isRidingStrider())
			{
				target.sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
				return false;
			}
			TransformationManager.getInstance().transformPlayer(getSkill().getTransformId().get(Rnd.get(getSkill().getTransformId().size())), (L2PcInstance) target);
		}
		else if(getEffected() instanceof L2MonsterInstance)
		{
			if(target.isAlikeDead() || target.isDead())
			{
				return false;
			}
			if(((L2MonsterInstance) target).getTransformation() != null)
			{
				return false;
			}
			TransformationManager.getInstance().transformMonster(getSkill().getTransformId().get(Rnd.get(getSkill().getTransformId().size())), (L2MonsterInstance) target);
		}
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopTransformation(false);
		// Stop the task of the L2Effect, remove it and update client magic icon
		stopEffectTask();
	}
}
