package dwo.gameserver.model.skills.base.funcs;

import dwo.gameserver.datatables.xml.EnchantBonusData;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * @author Yamaneko
 */

public class FuncEnchantHp extends Func
{
	public FuncEnchantHp(Stats pStat, int pOrder, Object owner, Lambda lambda)
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
		if(item.getEnchantLevel() > 0)
		{
			env.addValue(EnchantBonusData.getInstance().getHPBonus(item));
		}
	}
}