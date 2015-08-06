package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 06.06.12
 * Time: 23:45
 */

public class TargetImmunity extends L2Effect
{
	public TargetImmunity(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			// Делаем игрока "неуязвимым" для выделения в цель
			((L2PcInstance) getEffected()).setTargetable(false);

			// Перебираем всех игроков в KnownList и сбрасываем им таргет, если целью являемся мы
			getEffected().getKnownList().getKnownCharacters().stream().filter(cha -> cha.getTarget() == getEffected()).forEach(cha -> {
				cha.setTarget(null);
				cha.abortAttack();
				cha.abortCast();
				cha.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
			});
		}
		return true;
	}

	@Override
	public void onExit()
	{
		if(getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setTargetable(true);
		}
	}
}