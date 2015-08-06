package dwo.gameserver.model.skills.base.proptypes;

import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillChangeWeapon;
import dwo.gameserver.model.skills.base.l2skills.L2SkillChargeDmg;
import dwo.gameserver.model.skills.base.l2skills.L2SkillCont;
import dwo.gameserver.model.skills.base.l2skills.L2SkillCreateItem;
import dwo.gameserver.model.skills.base.l2skills.L2SkillDecoy;
import dwo.gameserver.model.skills.base.l2skills.L2SkillDefault;
import dwo.gameserver.model.skills.base.l2skills.L2SkillDrain;
import dwo.gameserver.model.skills.base.l2skills.L2SkillIncarnation;
import dwo.gameserver.model.skills.base.l2skills.L2SkillLearnSkill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillMount;
import dwo.gameserver.model.skills.base.l2skills.L2SkillRefreshDebuffTime;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSiegeFlag;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignet;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignetCasttime;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSpawn;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSummon;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSweeper;
import dwo.gameserver.model.skills.base.l2skills.L2SkillTeleport;
import dwo.gameserver.model.skills.base.l2skills.L2SkillTrap;
import dwo.gameserver.model.skills.stats.StatsSet;

import java.lang.reflect.Constructor;

/**
 * @author nBd
 */
public enum L2SkillType
{
	// Damage
	PDAM,
	MDAM,
	MDAM_IMMORTAL,
	CPDAM,
	MANADAM,
	CPDAMPERCENT,
	DOT,
	MDOT,
	DRAIN_SOUL,
	DRAIN(L2SkillDrain.class),
	DEATHLINK,
	FATAL,
	BLOW,
	SIGNET(L2SkillSignet.class),
	SIGNET_CASTTIME(L2SkillSignetCasttime.class),
	CASTTIME,

	// Disablers
	BLEED,
	POISON,
	STUN,
	ROOT,
	CONFUSION,
	FEAR,
	SLEEP,
	CONFUSE_MOB_ONLY,
	MUTE,
	PARALYZE,
	DISARM,

	// hp, mp, cp
	HEAL,
	HEAL_COHERENTLY,
	HOT,
	BALANCE_LIFE,
	HEAL_PERCENT,
	HEAL_STATIC,
	COMBATPOINTHEAL,
	CPHEAL_PERCENT,
	CPHOT,
	MANAHEAL,
	MANA_BY_LEVEL,
	MANAHEAL_PERCENT,
	MANARECHARGE,
	MPHOT,
	HPMPCPHEAL_PERCENT,
	HPMPHEAL_PERCENT,
	HPCPHEAL_PERCENT,

	// sp
	GIVE_SP,
	// reco
	GIVE_RECO,
	// vitality
	GIVE_VITALITY,

	// Aggro
	AGGDAMAGE,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	AGGDEBUFF,

	// Fishing
	FISHING,
	PUMPING,
	REELING,

	// MISC
	UNLOCK,
	UNLOCK_SPECIAL,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	ENCHANT_ATTRIBUTE,
	SOULSHOT,
	SPIRITSHOT,
	SIEGEFLAG(L2SkillSiegeFlag.class),
	TAKECASTLE,
	TAKEFORT,
	WEAPON_SA,
	DELUXE_KEY_UNLOCK,
	SOW,
	HARVEST,
	GET_PLAYER,
	MOUNT(L2SkillMount.class),
	INSTANT_JUMP,
	REPLACE_WITH_PET,
	DETECTION,
	DUMMY,

	// Creation
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	CREATE_ITEM(L2SkillCreateItem.class),
	LEARN_SKILL(L2SkillLearnSkill.class),

	// Summons
	SUMMON(L2SkillSummon.class),
	FEED_PET,
	DEATHLINK_PET,
	STRSIEGEASSAULT,
	ERASE,
	BETRAY,
	DECOY(L2SkillDecoy.class),
	INCARNATION(L2SkillIncarnation.class),
	SPAWN(L2SkillSpawn.class),

	// Cancel
	CANCEL,
	CANCEL_ALL,
	CANCEL_STATS,
	CANCEL_DEBUFF,
	CANCEL_CELESTIAL,
	NEGATE,

	BUFF,
	DEBUFF,
	PASSIVE,
	CONT(L2SkillCont.class),
	FUSION,

	// Char up (fly)
	FLY_UP,
	// Knock Down
	KNOCK_DOWN,
	// Knock Back
	KNOCK_BACK,

	RESURRECT,
	CHARGEDAM(L2SkillChargeDmg.class),
	MHOT,
	DETECT_WEAKNESS,
	LUCK,
	RECALL(L2SkillTeleport.class),
	TELEPORT(L2SkillTeleport.class),
	SUMMON_FRIEND,
	SPOIL,
	SWEEP(L2SkillSweeper.class),
	FAKE_DEATH,
	UNDEAD_DEFENSE,
	BEAST_FEED,
	BEAST_RELEASE,
	BEAST_RELEASE_ALL,
	BEAST_SKILL,
	BEAST_ACCOMPANY,
	CHARGESOUL,
	TRANSFORMDISPEL,
	SUMMON_TRAP(L2SkillTrap.class),
	DETECT_TRAP,
	REMOVE_TRAP,

	// Kamael WeaponChange
	CHANGEWEAPON(L2SkillChangeWeapon.class),

	STEAL_BUFF,

	// Skill is done within the core.
	COREDONE,

	// Refuel airship
	REFUEL,
	// Nornil's Power (Nornil's Garden instance)
	NORNILS_POWER,
	// Pailaka Spear
	PAILAKA_PDAM,

	// unimplemented
	NOTDONE, BALLISTA,

	// L2jS ADD Custom
	TRANSFER_SOUL,
	// GOD
	CHANGE_SUBCLASS,
	REFRESH_DEFUFF_TIME(L2SkillRefreshDebuffTime.class);

	private final Class<? extends L2Skill> _class;

	private L2SkillType()
	{
		_class = L2SkillDefault.class;
	}

	private L2SkillType(Class<? extends L2Skill> classType)
	{
		_class = classType;
	}

	public L2Skill makeSkill(StatsSet set)
	{
		try
		{
			Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);
			return c.newInstance(set);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
