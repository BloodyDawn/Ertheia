package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * User: GenCloud
 * Date: 20.01.2015
 * Team: La2Era Team
 */
public class FuncHennaCHA extends Func
{
    static final FuncHennaCHA _instance = new FuncHennaCHA();
    private static final Logger _log = LogManager.getLogger(Func.class);

    private FuncHennaCHA()
    {
        super(Stats.STAT_CHA, 0x10, null);
    }

    public static Func getInstance()
    {
        return _instance;
    }

    @Override
    public void calc(Env env)
    {
        L2PcInstance pc = (L2PcInstance) env.getCharacter();
        if(pc != null)
        {
            try
            {
                int minCha = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getCha();
                int maxCha = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getCha();
                env.setValue(Math.min(maxCha, Math.max(minCha, env.getValue() + pc.getHennaStatCHA())));
            }
            catch(Exception e)
            {
                _log.error("Failed to calc CHA Henna! Player: " + pc.getName(), e);
                if(pc.getBaseTemplate() == null)
                {
                    _log.error("Pc base template is NULL!");
                }
                else if(pc.getBaseTemplate().getBaseCharTemplate() == null)
                {
                    _log.error("PC Base char template is NULL!");
                }
                else if(pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes() == null)
                {
                    _log.error("PC default attributes is NULL!");
                }
                else if(pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min() == null)
                {
                    _log.error("PC default min attributes is NULL!");
                }
            }
        }
    }
}
