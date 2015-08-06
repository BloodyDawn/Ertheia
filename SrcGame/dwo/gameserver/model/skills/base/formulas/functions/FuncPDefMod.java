package dwo.gameserver.model.skills.base.formulas.functions;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 29.09.11
 * Time: 21:29
 */
public class FuncPDefMod extends Func
{
	static final FuncPDefMod _fmm_instance = new FuncPDefMod();

	private FuncPDefMod()
	{
		super(Stats.POWER_DEFENCE, 0x20, null);
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

			L2ItemInstance chest = p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
			if(chest == null)
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getChest());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) == null && !(chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR))
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getLegs());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) == null)
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getHelmet());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) == null)
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getBoots());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) == null)
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getGloves());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_UNDER) == null)
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getUnderwear());
			}

			if(p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CLOAK) == null)
			{
				env.addValue(baseTemplate.getDefaultAttributes().defense().getCloak());
			}

			env.mulValue(p.getLevelMod() * BaseStats.CHA.calcBonus(p));
		}
		else
		{
			env.mulValue(env.getCharacter().getLevelMod() * BaseStats.CHA.calcBonus(env.getCharacter()));
		}
	}
}
