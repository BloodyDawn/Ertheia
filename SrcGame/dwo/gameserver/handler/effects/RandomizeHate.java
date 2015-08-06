package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;

import java.util.Collection;
import java.util.List;

public class RandomizeHate extends L2Effect
{
	public RandomizeHate(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.RANDOMIZE_HATE;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() == null || getEffected().equals(getEffector()))
		{
			return false;
		}

		// Эффект действует только на мобов
		if(!(getEffected() instanceof L2Attackable))
		{
			return false;
		}

		L2Attackable effectedMob = (L2Attackable) getEffected();

		List<L2Character> targetList = new FastList<>();

		// Ищем возможные цели, ограничиваем радиус 500-ми, чтобы моб не бежал куда попало
		Collection<L2Character> chars = getEffected().getKnownList().getKnownCharactersInRadius(500);
		for(L2Character cha : chars)
		{
			if(cha != null && !cha.equals(effectedMob) && !cha.equals(getEffector()))
			{
				// Агр не может действовать на мобов с такой же социалкой
				if(cha instanceof L2Attackable && ((L2Attackable) cha).getFactionId() != null && ((L2Attackable) cha).getFactionId().equals(effectedMob.getFactionId()))
				{
					continue;
				}

				targetList.add(cha);
			}
		}
		// Если цели не найдены, прекращаем выполнение
		if(targetList.isEmpty())
		{
			return true;
		}

		// Выбираем рандомную цель из списка
		L2Character target = targetList.get(Rnd.get(targetList.size()));

		int hate = effectedMob.getHating(getEffector());
		effectedMob.stopHating(getEffector());
		effectedMob.addDamageHate(target, 0, hate);

		return true;
	}

}