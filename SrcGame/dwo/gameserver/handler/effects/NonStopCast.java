package dwo.gameserver.handler.effects;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.RunnableImpl;

import java.util.concurrent.ScheduledFuture;

/**
 * User: GenCloud
 * Date: 24.03.2015
 * Team: La2Era Team
 */
public class NonStopCast extends L2Effect 
{
    private SkillHolder _skill = new SkillHolder(getSkill().getId(), getSkill().getLevel());
    
    protected ScheduledFuture<?> _skillCastTask;

    public NonStopCast(Env env, EffectTemplate template) 
    {
        super(env, template);
    }

    @Override
    public L2EffectType getEffectType() 
    {
        return L2EffectType.NON_STOP_CAST;
    }

    @Override
    public boolean onStart()
    {
        L2PcInstance caster = (L2PcInstance) getEffector();
        if (getEffected().isDead())
        {
            return false;
        }
        
        _skillCastTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RunnableImpl()
        {
            @Override
            public void runImpl() throws Exception
            {
                if (!getEffected().isDead() || caster.getTarget() != null || caster.getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO || caster.getAI() == null || caster.getAI().getNextIntention() == null)
                {
                     caster.doCast(_skill.getSkill());
                }
                else
                {
                    _skillCastTask.cancel(true);
                    _skillCastTask = null;
                }
            }
        }, 100, 800);

        return true;
    }

    @Override
    public void onExit() 
    {
        super.onExit();

        if (_skillCastTask != null)
        {
            _skillCastTask.cancel(true);
            _skillCastTask = null;
        }
    }
}
