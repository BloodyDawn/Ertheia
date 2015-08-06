package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

public class FakeDeath extends L2Effect
{
	// Чтобы не вводить новый тип скила для FakeDeath с невозможностью взятия в таргет, просто используем
	// маркер additional в эффекте скила (если равен единице, то также добавляем к эффекту невозмоность взятия в таргет)
	private boolean isNonTargetable;

	public FakeDeath(Env env, EffectTemplate template)
	{
		super(env, template);

		if(template.additional != null)
		{
			isNonTargetable = Integer.parseInt(template.additional) == 1;
		}
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FAKE_DEATH;
	}

	@Override
	public boolean onStart()
	{
		if(isNonTargetable)
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
		getEffected().startFakeDeath();
		return true;
	}

	@Override
	public void onExit()
	{
		if(isNonTargetable)
		{
			((L2PcInstance) getEffected()).setTargetable(true);
		}
		getEffected().stopFakeDeath(false);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
		{
			return false;
		}

		double manaDam = calc();

		if(manaDam > getEffected().getCurrentMp())
		{
			if(getSkill().isToggle())
			{
				getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
				return false;
			}
		}
		getEffected().reduceCurrentMp(manaDam);
		return getSkill().isToggle();
	}
}