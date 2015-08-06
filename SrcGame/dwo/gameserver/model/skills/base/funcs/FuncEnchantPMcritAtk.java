package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.05.12
 * Time: 18:15
 */

public class FuncEnchantPMcritAtk extends Func
{
	public FuncEnchantPMcritAtk(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		super(pStat, pOrder, owner);
	}

	@Override
	public void calc(Env env)
	{
		if(cond != null && !cond.test(env))
		{
			return;
		}

		L2ItemInstance item = (L2ItemInstance) funcOwner;
		int enchantLevel = item.getEnchantLevel();
		if(enchantLevel > 3)
		{
			// Физ\Маг сила крита
			if(enchantLevel == 4)
			{
				env.addValue(0.34 * (item.isBlessedItem() ? 1.5 : 1));
			}
			else
			{
				env.addValue(0.34 * (item.isBlessedItem() ? 1.5 : 1) * ((enchantLevel << 1) - 9));
			}
		}
	}
}