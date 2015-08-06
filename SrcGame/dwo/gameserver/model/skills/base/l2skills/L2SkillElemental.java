package dwo.gameserver.model.skills.base.l2skills;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2SkillElemental extends L2Skill
{

	private final int[] _seeds;
	private final boolean _seedAny;

	public L2SkillElemental(StatsSet set)
	{
		super(set);

		_seeds = new int[3];
		_seeds[0] = set.getInteger("seed1", 0);
		_seeds[1] = set.getInteger("seed2", 0);
		_seeds[2] = set.getInteger("seed3", 0);

		_seedAny = set.getInteger("seed_any", 0) == 1;
	}

	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();

		if(activeChar.isPlayer())
		{
			if(weaponInst == null)
			{
				activeChar.sendMessage("You must equip your weapon before casting a spell.");
				return;
			}
		}

		for(L2Character target : (L2Character[]) targets)
		{
			if(target.isAlikeDead())
			{
				continue;
			}

			boolean charged = true;
			if(_seedAny)
			{
				charged = false;
				for(int _seed : _seeds)
				{
					if(_seed != 0)
					{
						L2Effect e = target.getFirstEffect(_seed);
						if(e != null && e.isInUse())
						{
							charged = true;
							break;
						}
					}
				}
			}
			else
			{
				for(int _seed : _seeds)
				{
					if(_seed != 0)
					{
						L2Effect e = target.getFirstEffect(_seed);
						if(e == null || !e.isInUse())
						{
							charged = false;
							break;
						}
					}
				}
			}
			if(!charged)
			{
				activeChar.sendMessage("Target is not charged by elements.");
				continue;
			}

			boolean mcrit = MagicalDamage.calcMCrit(activeChar.getMCriticalHit(target, this));
			byte shld = Shield.calcShldUse(activeChar, target, this);

			int damage = (int) MagicalDamage.calcMagicDam(activeChar, target, this, shld, activeChar.isSpiritshotCharged(this), activeChar.isBlessedSpiritshotCharged(this), mcrit);

			if(damage > 0)
			{
				target.reduceCurrentHp(damage, activeChar, this);
				CancelAttack.calcAtkBreak(target, damage);
				activeChar.sendDamageMessage(target, damage, false, false, false);
			}

			// activate attacked effects, if any
			target.stopSkillEffects(getId());
			getEffects(activeChar, target, new Env(shld, activeChar.isSpiritshotCharged(this), false, activeChar.isBlessedSpiritshotCharged(this)));
			activeChar.spsUncharge(this);
		}
	}
}
