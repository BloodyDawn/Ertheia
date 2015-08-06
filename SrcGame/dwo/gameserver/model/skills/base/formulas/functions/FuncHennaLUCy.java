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
 * TODO : данный показатель не задействован!
 */
public class FuncHennaLUCy extends Func
{
    static final FuncHennaLUCy _instance = new FuncHennaLUCy();
    private static final Logger _log = LogManager.getLogger(Func.class);

    private FuncHennaLUCy()
    {
        super(Stats.STAT_LUC, 0x10, null);
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
                int minLuc = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getLuc();
                int maxLuc = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getLuc();
                env.setValue(Math.min(maxLuc, Math.max(minLuc, env.getValue() + pc.getHennaStatLUC())));
            }
            catch(Exception e)
            {
                _log.error("Failed to calc LUC Henna! Player: " + pc.getName(), e);
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
