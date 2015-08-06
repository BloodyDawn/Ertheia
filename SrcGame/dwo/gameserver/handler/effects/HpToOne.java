package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.instance.*;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;

/**
 * User: GenCloud
 * Date: 17.03.2015
 * Team: La2Era Team
 */
public class HpToOne extends L2Effect
{
    public HpToOne(Env env, EffectTemplate template)
    {
        super(env, template);
    }

    @Override
    public L2EffectType getEffectType()
    {
        return L2EffectType.HP_TO_ONE;
    }

    @Override
    public boolean onStart()
    {
        if (getEffected().isDead())
        {
            return false;
        }

        if(getEffected() instanceof L2NpcInstance || getEffected() instanceof L2DefenderInstance || getEffected() instanceof L2SiegeFlagInstance || getEffected() instanceof L2SiegeSummonInstance || getEffected() instanceof L2GrandBossInstance || getEffected() instanceof L2RaidBossInstance)
        {
            return false;
        }

        if(!getEffected().isPlayer())
        {
            getEffected().setCurrentHp(1.0);
        }
        return true;
    }
}
