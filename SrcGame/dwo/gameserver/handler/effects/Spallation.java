package dwo.gameserver.handler.effects;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.util.RunnableImpl;

import java.util.concurrent.Future;

/**
 * User: GenCloud
 * Date: 26.03.2015
 * Team: La2Era Team
 * TODO
 */
public class Spallation extends L2Effect
{
    private Future<?> _checkTask;
    
    public Spallation(Env env, EffectTemplate template)
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
        L2PcInstance caster = (L2PcInstance) getEffector();
        getEffector().startAbnormalEffect(AbnormalEffect.SPALLATION);
        _checkTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new CheckDamageTask(caster), 0, 1000);
        return true;
    }
    
    @Override
    public void onExit()
    {
        getEffector().stopAbnormalEffect(AbnormalEffect.SPALLATION);
        if (_checkTask != null)
        {
            _checkTask.cancel(true);
            _checkTask = null;
        }
    }

    private class CheckDamageTask extends RunnableImpl 
    {
        private L2PcInstance caster;
        
        public CheckDamageTask(L2PcInstance caster)
        {
            this.caster = caster;           
        }
        
        @Override
        public void runImpl() throws Exception 
        {
            for(L2Character target : caster.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
            {
                if(target == null)
                {
                    continue;
                }

                if(target.isDead() || target.isInsideZone(L2Character.ZONE_PEACE))
                {
                    return;
                }

                double damage = calc();
                target.reduceCurrentHp(damage, caster, getSkill());
            }
        }
    }
}
