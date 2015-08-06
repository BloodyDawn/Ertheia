package dwo.gameserver.model.items.base;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.conditions.Condition;
import dwo.gameserver.model.skills.base.conditions.ConditionGameChance;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class L2Weapon extends L2Item
{
	private final L2WeaponType _type;
	private final boolean _isMagicWeapon;
	private final int _rndDam;
	private final int _soulShotCount;
	private final int _spiritShotCount;
	private final int _mpConsume;
	private final int _changeWeaponId;
	private final int _reducedSoulshot;
	private final int _reducedSoulshotChance;
	private final int _reducedMpConsume;
	private final int _reducedMpConsumeChance;
	private final boolean _isForceEquip;
	private final boolean _isAttackWeapon;
	private final boolean _useWeaponSkillsOnly;
	private CommissionItemHolder.CommissionCategoryType _commission_type;
	private FastMap<Integer, SkillHolder> _enchantSkill; // skill that activates when item is enchanted +4 (for duals)
	// Attached skills for Special Abilities
	private SkillHolder _skillsOnCast;
	private Condition _skillsOnCastCondition;
	private SkillHolder _skillsOnCrit;
	private Condition _skillsOnCritCondition;

	/**
	 * Constructor for Weapon.<BR><BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>_soulShotCount & _spiritShotCount</LI>
	 * <LI>_pDam & _mDam & _rndDam</LI>
	 * <LI>_critical</LI>
	 * <LI>_hitModifier</LI>
	 * <LI>_avoidModifier</LI>
	 * <LI>_shieldDes & _shieldDefRate</LI>
	 * <LI>_atkSpeed & _AtkReuse</LI>
	 * <LI>_mpConsume</LI>
	 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see L2Item constructor
	 */
	public L2Weapon(StatsSet set)
	{
		super(set);
		_type = L2WeaponType.valueOf(set.getString("weapon_type", "none").toUpperCase());
		_commission_type = CommissionItemHolder.CommissionCategoryType.valueOf(set.getString("commission_type", "none").toUpperCase());
		_type1 = TYPE1_WEAPON_RING_EARRING_NECKLACE;
		_type2 = TYPE2_WEAPON;
		_isMagicWeapon = set.getBool("is_magic_weapon", false);
		_soulShotCount = set.getInteger("soulshots", 0);
		_spiritShotCount = set.getInteger("spiritshots", 0);
		_rndDam = set.getInteger("random_damage", 0);
		_mpConsume = set.getInteger("mp_consume", 0);

		String[] reduced_soulshots = set.getString("reduced_soulshot", "").split(",");
		_reducedSoulshotChance = reduced_soulshots.length == 2 ? Integer.parseInt(reduced_soulshots[0]) : 0;
		_reducedSoulshot = reduced_soulshots.length == 2 ? Integer.parseInt(reduced_soulshots[1]) : 0;

		String[] reduced_mpconsume = set.getString("reduced_mp_consume", "").split(",");
		_reducedMpConsumeChance = reduced_mpconsume.length == 2 ? Integer.parseInt(reduced_mpconsume[0]) : 0;
		_reducedMpConsume = reduced_mpconsume.length == 2 ? Integer.parseInt(reduced_mpconsume[1]) : 0;

		String skill = set.getString("enchant_skill", null);
		if(skill != null)
		{
			_enchantSkill = new FastMap<>();
			String[] split = skill.split(";");
			for(String part : split)
			{
				try
				{
					String[] enc = part.split(",");
					int enchant = Integer.parseInt(enc[0]);
					String[] info = enc[1].split("-");
					if(info != null && info.length == 2)
					{
						int id = Integer.parseInt(info[0]);
						int level = Integer.parseInt(info[1]);
						if(id > 0 && level > 0)
						{
							_enchantSkill.put(enchant, new SkillHolder(id, level));
						}
					}
				}
				catch(Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.log(Level.ERROR, StringUtil.concat("> Couldnt parse ", skill, " in weapon enchant skills! item ", toString()));
				}
			}
		}

		skill = set.getString("oncast_skill", null);
		if(skill != null)
		{
			String[] info = skill.split("-");
			int chance = set.getInteger("oncast_chance", 100);
			if(info != null && info.length == 2)
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch(Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.log(Level.ERROR, StringUtil.concat("> Couldnt parse ", skill, " in weapon oncast skills! item ", toString()));
				}
				if(id > 0 && level > 0 && chance > 0)
				{
					_skillsOnCast = new SkillHolder(id, level);
					_skillsOnCastCondition = new ConditionGameChance(chance);
				}
			}
		}

		skill = set.getString("oncrit_skill", null);
		if(skill != null)
		{
			String[] info = skill.split("-");
			int chance = set.getInteger("oncrit_chance", 100);
			if(info != null && info.length == 2)
			{
				int id = 0;
				int level = 0;
				try
				{
					id = Integer.parseInt(info[0]);
					level = Integer.parseInt(info[1]);
				}
				catch(Exception nfe)
				{
					// Incorrect syntax, dont add new skill
					_log.log(Level.ERROR, StringUtil.concat("> Couldnt parse ", skill, " in weapon oncrit skills! item ", toString()));
				}
				if(id > 0 && level > 0 && chance > 0)
				{
					_skillsOnCrit = new SkillHolder(id, level);
					_skillsOnCritCondition = new ConditionGameChance(chance);
				}
			}
		}

		_changeWeaponId = set.getInteger("change_weaponId", 0);
		_isForceEquip = set.getBool("isForceEquip", false);
		_isAttackWeapon = set.getBool("isAttackWeapon", true);
		_useWeaponSkillsOnly = set.getBool("useWeaponSkillsOnly", false);
	}

	/**
	 * @return the type of Weapon
	 */
	@Override
	public L2WeaponType getItemType()
	{
		return _type;
	}

	/**
	 * @return the type of Etc Item.
	 */
	@Override
	public CommissionItemHolder.CommissionCategoryType getItemCommissionType()
	{
		return _commission_type;
	}

	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return _type.mask();
	}

	/**
	 * @param instance : L2ItemInstance pointing out the weapon
	 * @param player : L2Character pointing out the player
	 * @return array of Func objects containing the list of functions used by the weapon
	 */
	@Override
	public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
	{
		if(_funcTemplates == null || _funcTemplates.length == 0)
		{
			return _emptyFunctionSet;
		}

		ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);

		Env env = new Env();
		env.setPlayer(player);
		env.setItem(instance);
		Func f;

		for(FuncTemplate t : _funcTemplates)
		{
			f = t.getFunc(env, instance);
			if(f != null)
			{
				funcs.add(f);
			}
		}

		return funcs.toArray(new Func[funcs.size()]);
	}

	/**
	 * @return {@code true} if L2Weapon is magic type
	 */
	public boolean isMagicWeapon()
	{
		return _isMagicWeapon;
	}

	/**
	 * @return the quantity of SoulShot used.
	 */
	public int getSoulShotCount()
	{
		return _soulShotCount;
	}

	/**
	 * @return the quatity of SpiritShot used.
	 */
	public int getSpiritShotCount()
	{
		return _spiritShotCount;
	}

	/**
	 * @return the quantity of SoultShot used.
	 */
	public int getReducedSoulShot()
	{
		return _reducedSoulshot;
	}

	/**
	 * @return the chance to use Reduced SoultShot.
	 */
	public boolean getReducedSoulShotChance()
	{
		return Rnd.getChance(_reducedSoulshotChance);
	}

	/**
	 * @return the random damage inflicted by the weapon
	 */
	public int getRandomDamage()
	{
		return _rndDam;
	}

	/**
	 * @return the MP consumption with the weapon
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}

	/**
	 * @return the reduced MP consumption with the weapon.
	 */
	public int getReducedMpConsume()
	{
		return _reducedMpConsume;
	}

	/**
	 * @return the chance to use getReducedMpConsume()
	 */
	public boolean getReducedMpConsumeChance()
	{
		return Rnd.getChance(_reducedMpConsumeChance);
	}

	/**
	 * @return skill that player get when has equiped weapon +4  or more  (for duals SA)
	 */
	public FastMap<Integer, SkillHolder> getEnchantSkills()
	{
		return _enchantSkill;
	}

	/**
	 * @return Id in wich weapon this weapon can be changed
	 */
	public int getChangeWeaponId()
	{
		return _changeWeaponId;
	}

	public boolean isForceEquip()
	{
		return _isForceEquip;
	}

	public boolean isAttackWeapon()
	{
		return _isAttackWeapon;
	}

	public boolean useWeaponSkillsOnly()
	{
		return _useWeaponSkillsOnly;
	}

	/**
	 * @param caster : L2Character pointing out the caster
	 * @param target : L2Character pointing out the target
	 * @param crit : boolean tells whether the hit was critical
	 * @return effects of skills associated with the item to be triggered onHit.
	 */
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, boolean crit)
	{
		if(_skillsOnCrit == null || !crit)
		{
			return _emptyEffectSet;
		}
		List<L2Effect> effects = new FastList<>();
		L2Skill onCritSkill = _skillsOnCrit.getSkill();
		if(_skillsOnCritCondition != null)
		{
			Env env = new Env();
			env.setPlayer(caster);
			env.setTarget(target);
			env.setSkill(onCritSkill);
			if(!_skillsOnCritCondition.test(env))
			{
				// Chance not met
				return _emptyEffectSet;
			}
		}

		if(!onCritSkill.checkCondition(caster, target, false))
		{
			// Skill condition not met
			return _emptyEffectSet;
		}

		byte shld = Shield.calcShldUse(caster, target, onCritSkill);
		if(!Skills.calcSkillSuccess(caster, target, onCritSkill, shld, false, false, false))
		{
			// These skills should not work on RaidBoss
			return _emptyEffectSet;
		}
		if(target.getFirstEffect(onCritSkill.getId()) != null)
		{
			target.getFirstEffect(onCritSkill.getId()).exit();
		}
		Collections.addAll(effects, onCritSkill.getEffects(caster, target, new Env(shld, false, false, false)));
		if(effects.isEmpty())
		{
			return _emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}

	/**
	 * @param caster : L2Character pointing out the caster
	 * @param target : L2Character pointing out the target
	 * @param trigger : L2Skill pointing out the skill triggering this action
	 * @return effects of skills associated with the item to be triggered onCast.
	 */
	public L2Effect[] getSkillEffects(L2Character caster, L2Character target, L2Skill trigger)
	{
		if(_skillsOnCast == null)
		{
			return _emptyEffectSet;
		}
		L2Skill onCastSkill = _skillsOnCast.getSkill();
		// No Trigger if Offensive Skill
		if(trigger.isOffensive() && onCastSkill.isOffensive())
		{
			return _emptyEffectSet;
		}
		// No Trigger if not Magic skill
		if(!trigger.isMagic() && !onCastSkill.isMagic())
		{
			return _emptyEffectSet;
		}

		if(_skillsOnCastCondition != null)
		{
			Env env = new Env();
			env.setPlayer(caster);
			env.setTarget(target);
			env.setSkill(onCastSkill);
			if(!_skillsOnCastCondition.test(env))
			{
				return _emptyEffectSet;
			}
		}

		if(!onCastSkill.checkCondition(caster, target, false))
		{
			// Skill condition not met
			return _emptyEffectSet;
		}

		byte shld = Shield.calcShldUse(caster, target, onCastSkill);
		if(onCastSkill.isOffensive() && !Skills.calcSkillSuccess(caster, target, onCastSkill, shld, false, false, false))
		{
			return _emptyEffectSet;
		}

		L2Character[] targets = {target};

		// Launch the magic skill and calculate its effects
		// Get the skill handler corresponding to the skill type
		ISkillHandler handler = SkillHandler.getInstance().getHandler(onCastSkill.getSkillType());
		if(handler != null)
		{
			handler.useSkill(caster, onCastSkill, targets);
		}
		else
		{
			onCastSkill.useSkill(caster, targets);
		}

		// notify quests of a skill use
		if(caster instanceof L2PcInstance)
		{
			// Mobs in range 1000 see spell
			caster.getKnownList().getKnownCharactersInRadius(1000).stream().filter(spMob -> spMob instanceof L2Npc).forEach(spMob -> {
				L2Npc npcMob = (L2Npc) spMob;

				if(npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
				{
					for(Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
					{
						quest.notifySkillSee(npcMob, caster.getActingPlayer(), _skillsOnCast.getSkill(), targets, false);
					}
				}
			});
			HookManager.getInstance().notifyEvent(HookType.ON_SKILL_USE, caster.getHookContainer(), caster, _skillsOnCast.getSkill());
		}
		return _emptyEffectSet;
	}

	public boolean isBow()
	{
		return _type == L2WeaponType.BOW || _type == L2WeaponType.CROSSBOW || _type == L2WeaponType.TWOHANDCROSSBOW;
	}
}