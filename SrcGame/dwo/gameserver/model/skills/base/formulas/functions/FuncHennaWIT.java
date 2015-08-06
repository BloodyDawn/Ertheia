package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:51
 */
public class FuncHennaWIT extends Func
{
	static final FuncHennaWIT _fh_instance = new FuncHennaWIT();
	private static final Logger _log = LogManager.getLogger(Func.class);

	private FuncHennaWIT()
	{
		super(Stats.STAT_WIT, 0x10, null);
	}

	public static Func getInstance()
	{
		return _fh_instance;
	}

	@Override
	public void calc(Env env)
	{
		L2PcInstance pc = (L2PcInstance) env.getCharacter();
		if(pc != null)
		{
			try
			{
				int minWit = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getWit();
				int maxWit = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getWit();
				env.setValue(Math.min(maxWit, Math.max(minWit, env.getValue() + pc.getHennaStatWIT())));
			}
			catch(Exception e)
			{
				_log.error("Failed to calc MEN Henna! Player: " + pc.getName(), e);
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
