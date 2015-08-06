package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:28
 */
public class FuncMDefMod extends Func
{
	static final FuncMDefMod _fmm_instance = new FuncMDefMod();

	private FuncMDefMod()
	{
		super(Stats.MAGIC_DEFENCE, 0x20, null);
	}

	public static Func getInstance()
	{
		return _fmm_instance;
	}

	@Override
	public void calc(Env env)
	{
		if(env.getCharacter().isPlayer())
		{
			L2PcInstance p = (L2PcInstance) env.getCharacter();
			L2CharBaseTemplate baseTemplate = p.getTemplate().getBaseCharTemplate();
			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
			{
				env.setValue(env.getValue() - baseTemplate.getDefaultAttributes().defense().getLeftRing());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
			{
				env.setValue(env.getValue() - baseTemplate.getDefaultAttributes().defense().getRightRing());
			}
			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
			{
				env.setValue(env.getValue() - baseTemplate.getDefaultAttributes().defense().getLeftEarring());
			}
			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
			{
				env.setValue(env.getValue() - baseTemplate.getDefaultAttributes().defense().getRightEarring());
			}
			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
			{
				env.setValue(env.getValue() - baseTemplate.getDefaultAttributes().defense().getNecklace());
			}

			env.setValue(env.getValue() * BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod() * BaseStats.CHA.calcBonus(env.getCharacter()));
		}
		else if(env.getCharacter().isPet())
		{
			if(env.getCharacter().getInventory().getPaperdollObjectId(Inventory.PAPERDOLL_NECK) == 0)
			{
				env.setValue(env.getValue() * BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod() * BaseStats.CHA.calcBonus(env.getCharacter()));
			}
			else
			{
				env.setValue(env.getValue() - 13);
				env.setValue(env.getValue() * BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod() * BaseStats.CHA.calcBonus(env.getCharacter()));
			}
		}
		else
		{
			env.setValue(env.getValue() * BaseStats.MEN.calcBonus(env.getCharacter()) * env.getCharacter().getLevelMod() * BaseStats.CHA.calcBonus(env.getCharacter()));
		}
	}
}