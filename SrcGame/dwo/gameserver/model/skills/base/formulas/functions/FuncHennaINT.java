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
 * Time: 21:48
 */

public class FuncHennaINT extends Func
{
	static final FuncHennaINT _fh_instance = new FuncHennaINT();
	private static final Logger _log = LogManager.getLogger(Func.class);

	private FuncHennaINT()
	{
		super(Stats.STAT_INT, 0x10, null);
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
				int minInt = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getInt();
				int maxInt = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getInt();
				env.setValue(Math.min(maxInt, Math.max(minInt, env.getValue() + pc.getHennaStatINT())));
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
