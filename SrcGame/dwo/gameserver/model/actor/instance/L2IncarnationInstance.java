package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.util.Rnd;

import java.util.concurrent.Future;

public class L2IncarnationInstance extends L2DecoyInstance
{
	private Future<?> _attackTask;
	private Future<?> _chargeTask;
    private Future<?> _skillUseTask;
    
    public L2IncarnationInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2Skill skill)
	{
		super(objectId, template, owner, skill);
        setIsClone(true);
    }
    
	@Override
	protected void initCustomAi()
	{
		_attackTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AttackTask(), 0, 1000);
        _skillUseTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new SkillTask(this), 0, 5000);
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
            useSoulShot(true);
        }, 0, 200);
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(_attackTask != null)
		{
			_attackTask.cancel(true);
			_attackTask = null;
		}

		if(_chargeTask != null)
		{
			_chargeTask.cancel(true);
			_chargeTask = null;
		}

        if(_skillUseTask != null)
        {
            _skillUseTask.cancel(true);
            _skillUseTask = null;
        }

		return super.doDie(killer);
	}

	@Override
	public void unSummon(L2PcInstance owner)
	{
		synchronized(this)
		{
			if(_attackTask != null)
			{
				_attackTask.cancel(true);
				_attackTask = null;
			}

			if(_chargeTask != null)
			{
				_chargeTask.cancel(true);
				_chargeTask = null;
			}

            if(_skillUseTask != null)
            {
                _skillUseTask.cancel(true);
                _skillUseTask = null;
            }

			super.unSummon(owner);
		}
	}

	/**
	 * Задача для агра на цели, которые атакует игрок.
	 */
	private class AttackTask implements Runnable
	{
		public AttackTask()
		{
			getOwner().getHookContainer().addHook(HookType.ON_ATTACK, new AttackHook());
		}

		private boolean isAttackable(L2Object target)
		{
			return target != null && target instanceof L2Attackable && !(target instanceof L2IncarnationInstance);
		}

		@Override
		public void run()
		{
			if(getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getOwner());
			}
		}

		public class AttackHook extends AbstractHookImpl
		{
			@Override
			public void onAttack(L2PcInstance player, L2Character attacker, boolean summonAttacked)
			{
				if(isAttackable(attacker))
				{
					setTarget(attacker);
					addDamageHate(attacker, 0, 999);
					getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);                    
				}
			}
		}
	}

    private class SkillTask implements Runnable 
    {
        private final L2IncarnationInstance instance;

        private final SkillHolder[] skills = new SkillHolder[]
                {
                        new SkillHolder(10550, getNpcInstance().getTemplate().getNpcId() - 13301),
                        new SkillHolder(10551, getNpcInstance().getTemplate().getNpcId() - 13301)
                };
        
        public SkillTask(L2IncarnationInstance instance)
        {            
            this.instance = instance;
        }
        
        @Override
        public void run() 
        {
            L2Skill skill = skills[Rnd.get(skills.length)].getSkill();

            if (getTarget() == null)
            {
                _skillUseTask.cancel(true);
                _skillUseTask = null;
            }

            instance.doCast(skill);
        }
    }
}
