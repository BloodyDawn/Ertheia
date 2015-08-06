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
public class FuncHennaDEX extends Func
{
	static final FuncHennaDEX _fh_instance = new FuncHennaDEX();
	private static final Logger _log = LogManager.getLogger(Func.class);

	private FuncHennaDEX()
	{
		super(Stats.STAT_DEX, 0x10, null);
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
				int minDex = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getDex();
				int maxDex = pc.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getDex();
				env.setValue(Math.min(maxDex, Math.max(minDex, env.getValue() + pc.getHennaStatDEX())));
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
