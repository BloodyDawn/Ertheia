package dwo.gameserver.handler.effects;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.base.funcs.FuncAdd;
import dwo.gameserver.model.skills.base.funcs.FuncMul;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import org.apache.log4j.Level;

import java.util.concurrent.Future;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class SummonShare extends L2Effect
{
	private Future<?> _statsRenewTask;
	private Func[] _funcs;

	public SummonShare(Env env, EffectTemplate template)
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
		// Если нету сумов снимаем бафф
		if(!getEffected().isSummon())
		{
			exit();
			return false;
		}

		// Включаем таск на обновление стат
		_statsRenewTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this::recalcStats, 5000, 5000);

		return super.onStart();
	}

	@Override
	public void onExit()
	{
		// Чистим статы саммонов при снятии эффекта
		if(_funcs != null)
		{
			getEffected().removeStatsOwner(getEffector());
			_funcs = null;
		}

		// Выключаем пересчет
		if(_statsRenewTask != null)
		{
			_statsRenewTask.cancel(true);
		}
		super.onExit();
	}

	@Override
	public Func[] getStatFuncs()
	{
		if(_funcs != null)
		{
			return _funcs;
		}

		Func[] funcTemplates = super.getStatFuncs();
		_funcs = new Func[funcTemplates.length];

		for(int i = 0; i < _funcs.length; i++)
		{
			_funcs[i] = new FuncShare(funcTemplates[i]);
		}

		return _funcs;
	}

	public void recalcStats()
	{
		synchronized(this)
		{
			if(_funcs == null)
			{
				return;
			}

			if(getEffected().isSummon())
			{
				// Если чара нету разделения то завершаем его у пета
				if(getEffector().getFirstEffect(getSkill()) == null)
				{
					getEffected().removeStatsOwner(getEffector());
					_funcs = null;
					exit();
					return;
				}

				// Проверяем эффект на сумах и если его нет накладываем
				// Пропускаем пета из которого вызван эффект
				// Если его нету накладываем
				getEffector().getPets().stream().filter(summon -> !summon.equals(getEffected())).forEach(summon -> {
					// Если его нету накладываем
					if(summon.getFirstEffect(getSkill()) == null)
					{
						SkillTable.getInstance().getInfo(getSkill().getId(), getSkill().getLevel()).getEffects(getEffector(), summon);
					}
				});

				// Удаляем бонус
				getEffected().removeStatsOwner(getEffector());
				// Пересчитываем бонус
				for(Func func : getStatFuncs())
				{
					if(func != null)
					{
						getEffected().addStatFunc(func);
					}
				}
			}
		}
	}

	public class FuncShare extends Func
	{
		private final Func _ownerFunc;

		public FuncShare(Func ownerFunc)
		{
			super(ownerFunc.stat, ownerFunc.order, getEffector(), ownerFunc.value);
			_ownerFunc = ownerFunc;
		}

		@Override
		public void calc(Env env)
		{
			L2Character character = env.getCharacter();

			if(character.isSummon() && ((L2Summon) character).getOwner() != null)
			{
				L2PcInstance owner = ((L2Summon) character).getOwner();
				double value;

				if(stat == Stats.POWER_ATTACK)
				{
					value = owner.getStat().getPAtk(null);
				}
				else if(stat == Stats.POWER_ATTACK_SPEED)
				{
					value = owner.getStat().getPAtkSpd();
				}
				else if(stat == Stats.POWER_DEFENCE)
				{
					value = owner.getStat().getPDef(null);
				}
				else if(stat == Stats.MAGIC_ATTACK)
				{
					value = owner.getStat().getMAtk(null, null);
				}
				else if(stat == Stats.MAGIC_DEFENCE)
				{
					value = owner.getStat().getMDef(null, null);
				}
				else if(stat == Stats.MAX_HP)
				{
					value = owner.getStat().getMaxHp();
				}
				else if(stat == Stats.MAX_MP)
				{
					value = owner.getStat().getMaxMp();
				}
				else if(stat == Stats.PCRITICAL_RATE)
				{
					value = owner.getStat().getCriticalHit(null, null);
				}
				else if(stat == Stats.RUN_SPEED)
				{
					value = owner.getStat().getRunSpeed();
				}
				else if(stat == Stats.MAGIC_ATTACK_SPEED)
				{
					value = owner.getStat().getMAtkSpd();
				}
				else
				{
					_log.log(Level.WARN, "[SummonShare] Unsupported stat " + stat);
					return;
				}

				if(_ownerFunc instanceof FuncMul)
				{
					env.setValue(env.getValue() + value * ((FuncMul) _ownerFunc).getLambda().calc(env));
				}
				else if(_ownerFunc instanceof FuncAdd)
				{
					env.setValue(env.getValue() + ((FuncAdd) _ownerFunc).getLambda().calc(env));
				}
				else
				{
					_log.log(Level.WARN, "[SummonShare] unsupported function instance: " + _ownerFunc);
				}
			}
		}
	}

}
