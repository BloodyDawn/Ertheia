package dwo.gameserver.model.actor;

import dwo.config.Config;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.PathFinding;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHookContainer;
import dwo.gameserver.engine.hookengine.container.CharacterHookContainer;
import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.handler.SkillHandler;
import dwo.gameserver.handler.effects.ChanceSkillTrigger;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.TownManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2AttackableAI;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.controller.character.LocationController;
import dwo.gameserver.model.actor.controller.player.PvPFlagController;
import dwo.gameserver.model.actor.instance.L2AirShipInstance;
import dwo.gameserver.model.actor.instance.L2BoatInstance;
import dwo.gameserver.model.actor.instance.L2DecoyInstance;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2IncarnationInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2NpcWalkerInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance.SkillDat;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2RiftInvaderInstance;
import dwo.gameserver.model.actor.instance.L2ShuttleInstance;
import dwo.gameserver.model.actor.knownlist.CharKnownList;
import dwo.gameserver.model.actor.stat.CharStat;
import dwo.gameserver.model.actor.status.CharStatus;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.proptypes.SoulshotGrade;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.L2AccessLevel;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.skills.base.formulas.functions.*;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.skills.CastTimeSkill;
import dwo.gameserver.model.skills.ChanceSkillList;
import dwo.gameserver.model.skills.FusionSkill;
import dwo.gameserver.model.skills.IChanceSkillTrigger;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.Variables;
import dwo.gameserver.model.skills.base.formulas.calculations.AttackSpeedAndMiss;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Reflect;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.formulas.calculations.SkillMastery;
import dwo.gameserver.model.skills.base.formulas.calculations.Skills;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.base.l2skills.L2SkillMount;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSummon;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Calculator;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.TargetPosition;
import dwo.gameserver.model.world.zone.type.L2WaterZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Attack;
import dwo.gameserver.network.game.serverpackets.ChangeMoveType;
import dwo.gameserver.network.game.serverpackets.ChangeWaitType;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.FlyToLocation.FlyType;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.MTL;
import dwo.gameserver.network.game.serverpackets.MagicSkillCanceled;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.Revive;
import dwo.gameserver.network.game.serverpackets.ServerObjectInfo;
import dwo.gameserver.network.game.serverpackets.SetupGauge;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.StopMove;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.TeleportToLocation;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAbnormalStatusUpdateFromTarget;
import dwo.gameserver.network.game.serverpackets.packet.info.ExNpcSpeedInfo;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExTeleportToLocationActivate;
import dwo.gameserver.network.game.serverpackets.packet.info.CI;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import dwo.gameserver.util.geometry.Point3D;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import javolution.util.WeakFastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ACTIVE;
import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_FOLLOW;

public abstract class L2Character extends L2Object
{
	public static final Logger _log = LogManager.getLogger(L2Character.class);
	/* Zone system */
	public static final byte ZONE_PVP = 0;
	public static final byte ZONE_PEACE = 1;
	public static final byte ZONE_SIEGE = 2;
	public static final byte ZONE_MOTHERTREE = 3;
	public static final byte ZONE_CLANHALL = 4;
	public static final byte ZONE_LANDING = 5;
	public static final byte ZONE_NOLANDING = 6;
	public static final byte ZONE_WATER = 7;
	public static final byte ZONE_JAIL = 8;
	public static final byte ZONE_MONSTERTRACK = 9;
	public static final byte ZONE_CASTLE = 10;
	public static final byte ZONE_SWAMP = 11;
	public static final byte ZONE_NOSUMMONFRIEND = 12;
	public static final byte ZONE_FORT = 13;
	public static final byte ZONE_NOSTORE = 14;
	public static final byte ZONE_TOWN = 15;
	public static final byte ZONE_SCRIPT = 16;
	public static final byte ZONE_HQ = 17;
	public static final byte ZONE_DANGERAREA = 18;
	public static final byte ZONE_ALTERED = 19;
	public static final byte ZONE_NOBOOKMARK = 20;
	public static final byte ZONE_NOITEMDROP = 21;
	public static final byte ZONE_JUMP = 22;
	public static final byte ZONE_NORESTART = 23;
	private static final FastList<L2Summon> _emptyPetData = new FastList<>(0);
	/**
	 * Table of calculators containing all standard NPC calculator (ex : ACCURACY_COMBAT, EVASION_RATE)
	 */
	private static final Calculator[] NPC_STD_CALCULATOR = getStdNPCCalculators();
	/* FastMap(L2Skill) containing all skills of the L2Character*/
	private final FastMap<Integer, L2Skill> _skills = new FastMap<>();
	/* Map containing all custom skills of this character. */
	private final FastMap<Integer, SkillHolder> _customSkills = new FastMap<>();
	private final byte[] _zones = new byte[24];
	private final ReentrantLock _teleportLock;
	protected boolean _showSummonAnimation = false;
	protected boolean _isTeleporting;
	/* Current force buff this caster is casting to a target */
	protected FusionSkill _fusionSkill;
	//Для скилла Массовые Оковы, вешает дебаф пока кастует на цель.
	protected CastTimeSkill _castTimeSkill;
	/* Table containing all skillId that are disabled */
	protected L2TIntObjectHashMap<Long> _disabledSkills;
	protected byte _zoneValidateCounter = 4;
	protected CharEffectList _effects = new CharEffectList(this);
	/**
	 * Movement data of this L2Character
	 */
	protected MoveData _move;
	protected L2CharacterAI _ai;
	/**
	 * Future Skill Cast
	 */
	protected Future<?> _skillCast;
	protected Future<?> _skillCast2;
	/**
	 * Double casting future skill cast
	 */
	protected Future<?> _skillDoubleCast;
	private volatile Set<L2Character> _attackByList;
	private volatile boolean _isCastingNow;
	private volatile boolean _isDoubleCastingNow;
	private volatile boolean _isCastingSimultaneouslyNow;
	private L2Skill _lastSkillCast;
	private L2Skill _lastSimultaneousSkillCast;
	private List<L2TargetType> _disabledTargetSkills;
	private boolean _canMove = true;
	private boolean _isDead;
	private boolean _isImmobilized;
	private boolean _isOverloaded; // the char is carrying too much
	private boolean _isParalyzed;
	private boolean _isPendingRevive;
	private boolean _isRunning;
	private boolean _isNoRndWalk; // Is no random walk
	private boolean _isNoAttackingBack; // Mob will not attack player
	private boolean _isInvul;
	private boolean _isMortal = true;    // Char will die when HP decreased to 0
	private boolean _lethalable = true; // Можно-ли нанести персонажу летальный удар
	private boolean _isFlying;
    private boolean _isClone;
	private CharStat _stat;
	private CharStatus _status;
	private L2CharTemplate _template;    // The link on the L2CharTemplate object containing generic and static properties of this L2Character type (ex : Max HP, Speed...)
	private String _title;
	private double _hpUpdateIncCheck;
	private double _hpUpdateDecCheck;
	private double _hpUpdateInterval;
	/* Table of Calculators containing all used calculator */
	private Calculator[] _calculators;
	/* FastMap containing the active chance skills on this character */
	private volatile ChanceSkillList _chanceSkills;
	private boolean _allSkillsDisabled;
	private L2Character _debugger;
	/**
	 * Damage modifier. Computed as damage * _reduceDamageRate.
	 */
	private float _reduceDamageRate;
	private IHookContainer _hookContainer = new CharacterHookContainer();
	private FastSet<Integer> _abnormalEffects = new FastSet<Integer>().shared();
	/**
	 * Orientation of the L2Character
	 */
	private int _heading;
	/**
	 * L2Charcater targeted by the L2Character
	 */
	private L2Object _target;
	// set by the start of attack, in game ticks
	private int _attackEndTime;
	private int _attacking;
	private int _disableBowAttackEndTime;

	// =========================================================
	// Constructor
	private int _disableCrossBowAttackEndTime;
	private int _castInterruptTime;

	// =========================================================
	// Event - Public
	private boolean _AIdisabled;
    private boolean _isOctavisRaid;
    private PvPFlagController _pvpFlagController;

    /**
	 * Constructor of L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _template of the L2Character </li>
	 * <li>Set _overloaded to false (the charcater can take more items)</li><BR><BR>
	 * <p/>
	 * <li>If L2Character is a L2NPCInstance, copy skills from template to object</li>
	 * <li>If L2Character is a L2NPCInstance, link _calculators to NPC_STD_CALCULATOR</li><BR><BR>
	 * <p/>
	 * <li>If L2Character is NOT a L2NPCInstance, create an empty _skills slot</li>
	 * <li>If L2Character is a L2PcInstance or L2Summon, copy basic Calculator set to object</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2CharTemplate to apply to the object
	 */
	protected L2Character(int objectId, L2CharTemplate template)
	{
		super(objectId);
		initCharStat();
		initCharStatus();

		_skills.shared();
		_customSkills.shared();

		// Set its template to the new L2Character
		_template = template;

		if(isDoor())
		{
			_calculators = getStdDoorCalculators();
		}
		else if(template != null && isNpc())
		{
			// Copy the Standard Calcultors of the L2NPCInstance in _calculators
			_calculators = NPC_STD_CALCULATOR;

			// Copy the skills of the L2NPCInstance from its template to the L2Character Instance
			// The skills list can be affected by spell effects so it's necessary to make a copy
			// to avoid that a spell affecting a L2NPCInstance, affects others L2NPCInstance of the same type too.
			if(template.getSkills() != null)
			{
				_skills.putAll(template.getSkills());
			}
			if(!_skills.isEmpty())
			{
				for(L2Skill skill : getAllSkills())
				{
					if(skill.getDisplayId() != skill.getId())
					{
						_customSkills.put(skill.getDisplayId(), new SkillHolder(skill.getId(), skill.getLevel()));
					}
					addStatFuncs(skill.getStatFuncs(null, this));
				}
			}
		}
		else
		{
			// If L2Character is a L2PcInstance or a L2Summon, create the basic calculator set
			_calculators = new Calculator[Stats.NUM_STATS];

			if(isSummon())
			{
				// Copy the skills of the L2Summon from its template to the L2Character Instance
				// The skills list can be affected by spell effects so it's necessary to make a copy
				// to avoid that a spell affecting a L2Summon, affects others L2Summon of the same type too.
				if(template != null)
				{
					_skills.putAll(template.getSkills());
				}
				if(!_skills.isEmpty())
				{
					for(L2Skill skill : getAllSkills())
					{
						if(skill.getDisplayId() != skill.getId())
						{
							_customSkills.put(skill.getDisplayId(), new SkillHolder(skill.getId(), skill.getLevel()));
						}
						addStatFuncs(skill.getStatFuncs(null, this));
					}
				}
			}
			addFuncsToNewCharacter(this);
		}

		_isInvul = true;
		_teleportLock = new ReentrantLock();
	}

	/**
	 * Add basics Func objects to L2PcInstance and L2Summon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of
	 * a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func
	 * represents a mathematic function : <BR>
	 * <BR>
	 * FuncPAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 *
	 * @param cha L2PcInstance or L2Summon that must obtain basic Func objects
	 */
	public static void addFuncsToNewCharacter(L2Character cha)
	{
		if(cha instanceof L2PcInstance)
		{
			cha.addStatFunc(FuncMaxHpAdd.getInstance());
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxCpAdd.getInstance());
			cha.addStatFunc(FuncMaxCpMul.getInstance());
			cha.addStatFunc(FuncMaxMpAdd.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			cha.addStatFunc(FuncBowAtkRange.getInstance());
			cha.addStatFunc(FuncCrossBowAtkRange.getInstance());
			cha.addStatFunc(FuncTwoHandCrossBowAtkRange.getInstance());
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncPAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncPAtkAccuracy.getInstance());
			cha.addStatFunc(FuncPAtkEvasion.getInstance());
			cha.addStatFunc(FuncMAtkAccuracy.getInstance());
			cha.addStatFunc(FuncMAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());

			cha.addStatFunc(FuncHennaSTR.getInstance());
			cha.addStatFunc(FuncHennaDEX.getInstance());
			cha.addStatFunc(FuncHennaINT.getInstance());
			cha.addStatFunc(FuncHennaMEN.getInstance());
			cha.addStatFunc(FuncHennaCON.getInstance());
			cha.addStatFunc(FuncHennaWIT.getInstance());
            cha.addStatFunc(FuncHennaLUCy.getInstance());
            cha.addStatFunc(FuncHennaCHA.getInstance());
			cha.addStatFunc(FuncHennaFireRes.getInstance());
			cha.addStatFunc(FuncHennaWindRes.getInstance());
			cha.addStatFunc(FuncHennaWaterRes.getInstance());
			cha.addStatFunc(FuncHennaEarthRes.getInstance());
			cha.addStatFunc(FuncHennaHolyRes.getInstance());
			cha.addStatFunc(FuncHennaDarkRes.getInstance());
		}
		else if(cha.isSummon())
		{
			cha.addStatFunc(FuncMaxHpMul.getInstance());
			cha.addStatFunc(FuncMaxMpMul.getInstance());
			cha.addStatFunc(FuncPAtkMod.getInstance());
			cha.addStatFunc(FuncMAtkMod.getInstance());
			cha.addStatFunc(FuncPDefMod.getInstance());
			cha.addStatFunc(FuncMDefMod.getInstance());
			cha.addStatFunc(FuncPAtkCritical.getInstance());
			cha.addStatFunc(FuncMAtkCritical.getInstance());
			cha.addStatFunc(FuncPAtkAccuracy.getInstance());
			cha.addStatFunc(FuncPAtkEvasion.getInstance());
			cha.addStatFunc(FuncMAtkAccuracy.getInstance());
			cha.addStatFunc(FuncMAtkEvasion.getInstance());
			cha.addStatFunc(FuncPAtkSpeed.getInstance());
			cha.addStatFunc(FuncMAtkSpeed.getInstance());
			cha.addStatFunc(FuncMoveSpeed.getInstance());
		}
	}

	// =========================================================
	// Method - Public

	/**
	 * Return the standard NPC Calculator set containing ACCURACY_PHYSICAL and
	 * EVASION_PHYSICAL_RATE.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of
	 * a character property (ex : MAX_HP, REGENERATE_HP_RATE...).
	 * In fact, each calculator is a table of Func object in which each Func
	 * represents a mathematic function : <BR>
	 * <BR>
	 * FuncPAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * To reduce cache memory use, L2NPCInstances who don't have skills share
	 * the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 */
	public static Calculator[] getStdNPCCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		std[Stats.MAX_HP.ordinal()] = new Calculator();
		std[Stats.MAX_HP.ordinal()].addFunc(FuncMaxHpMul.getInstance());

		std[Stats.MAX_MP.ordinal()] = new Calculator();
		std[Stats.MAX_MP.ordinal()].addFunc(FuncMaxMpMul.getInstance());

		std[Stats.POWER_ATTACK.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());

		std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());

		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncPDefMod.getInstance());

		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncMDefMod.getInstance());

		std[Stats.PCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.PCRITICAL_RATE.ordinal()].addFunc(FuncPAtkCritical.getInstance());

		std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());

		std[Stats.ACCURACY_PHYSICAL.ordinal()] = new Calculator();
		std[Stats.ACCURACY_PHYSICAL.ordinal()].addFunc(FuncPAtkAccuracy.getInstance());

		std[Stats.EVASION_PHYSICAL_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_PHYSICAL_RATE.ordinal()].addFunc(FuncPAtkEvasion.getInstance());

		std[Stats.RUN_SPEED.ordinal()] = new Calculator();
		std[Stats.RUN_SPEED.ordinal()].addFunc(FuncMoveSpeed.getInstance());

		std[Stats.ACCURACY_MAGICAL.ordinal()] = new Calculator();
		std[Stats.ACCURACY_MAGICAL.ordinal()].addFunc(FuncMAtkAccuracy.getInstance());

		std[Stats.EVASION_MAGICAL_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_MAGICAL_RATE.ordinal()].addFunc(FuncMAtkEvasion.getInstance());

		std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());

		std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());

		return std;
	}

	public static Calculator[] getStdDoorCalculators()
	{
		Calculator[] std = new Calculator[Stats.NUM_STATS];

		// Add the FuncPAtkAccuracy to the Standard Calculator of ACCURACY_PHYSICAL
		std[Stats.ACCURACY_PHYSICAL.ordinal()] = new Calculator();
		std[Stats.ACCURACY_PHYSICAL.ordinal()].addFunc(FuncPAtkAccuracy.getInstance());

		// Add the FuncPAtkEvasion to the Standard Calculator of EVASION_PHYSICAL_RATE
		std[Stats.EVASION_PHYSICAL_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_PHYSICAL_RATE.ordinal()].addFunc(FuncPAtkEvasion.getInstance());

		// Add the FuncMAtkAccuracy to the Standard Calculator of ACCURACY_MAGICAL
		std[Stats.ACCURACY_MAGICAL.ordinal()] = new Calculator();
		std[Stats.ACCURACY_MAGICAL.ordinal()].addFunc(FuncMAtkAccuracy.getInstance());

		// Add the FuncMAtkEvasion to the Standard Calculator of EVASION_MAGICAL_RATE
		std[Stats.EVASION_MAGICAL_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_MAGICAL_RATE.ordinal()].addFunc(FuncMAtkEvasion.getInstance());
		return std;
	}

	/**
	 * @return True if debugging is enabled for this L2Character
	 */
	public boolean isDebug()
	{
		return _debugger != null;
	}

	/**
	 * Sets L2Character instance, to which debug packets will be send
	 *
	 * @param d
	 */
	public void setDebug(L2Character d)
	{
		_debugger = d;
	}

	/**
	 * Send debug packet.
	 *
	 * @param pkt
	 */
	public void sendDebugPacket(L2GameServerPacket pkt)
	{
		if(_debugger != null)
		{
			_debugger.sendPacket(pkt);
		}
	}

	/**
	 * Send debug text string
	 *
	 *
	 * @param msg
	 */
	public void sendDebugMessage(String msg)
	{
		if(_debugger != null)
		{
			_debugger.sendChatMessage(0, ChatType.ALL, "SYS", msg);
		}
	}

	/**
	 * @return character inventory, default null, overridden in L2Playable types and in L2NPcInstance
	 */
	public Inventory getInventory()
	{
		return null;
	}

	public boolean destroyItemByItemId(ProcessType process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}

	public boolean destroyItem(ProcessType process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		// Default: NPCs consume virtual items for their skills
		// TODO: should be logged if even happens.. should be false
		return true;
	}

	/**
	 * @param zone
	 * @return
	 */
	public boolean isInsideZone(byte zone)
	{
		Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
		switch(zone)
		{
			case ZONE_PVP:
				if(instance != null && instance.isPvPInstance())
				{
					return true;
				}
				return _zones[ZONE_PVP] > 0 && _zones[ZONE_PEACE] == 0;
			case ZONE_PEACE:
				if(instance != null && instance.isPvPInstance())
				{
					return false;
				}
		}
		return _zones[zone] > 0;
	}

	/**
	 * @param zone
	 * @param state
	 */
	public void setInsideZone(byte zone, boolean state)
	{
		if(state)
		{
			_zones[zone]++;
		}
		else
		{
			_zones[zone]--;
			if(_zones[zone] < 0)
			{
				_zones[zone] = 0;
			}
		}
	}

	/**
	 * This will return true if the player is transformed,<br>
	 * but if the player is not transformed it will return false.
	 *
	 * @return transformation status
	 */
	public boolean isTransformed()
	{
		return false;
	}

	/**
	 * This will untransform a player if they are an instance of L2Pcinstance
	 * and if they are transformed.
	 */
	public void untransform(boolean removeEffects)
	{
		// Just a place holder
	}

	/**
	 * This will return true if the player is GM,<br>
	 * but if the player is not GM it will return false.
	 *
	 * @return GM status
	 */
	public boolean isGM()
	{
		return false;
	}

	/**
	 * Overrided in L2PcInstance
	 */
	public L2AccessLevel getAccessLevel()
	{
		return null;
	}

	protected void initCharStatusUpdateValues()
	{
		_hpUpdateIncCheck = getMaxVisibleHp();
		_hpUpdateInterval = _hpUpdateIncCheck / 352.0; // MAX_HP div MAX_HP_BAR_PX
		_hpUpdateDecCheck = _hpUpdateIncCheck - _hpUpdateInterval;
	}

	public void onTeleported()
	{
		if(!_teleportLock.tryLock())
		{
			return;
		}
		try
		{
			if(!_isTeleporting)
			{
				return;
			}
			getLocationController().spawn(getX(), getY(), getZ());
			setIsTeleporting(false);
		}
		finally
		{
			_teleportLock.unlock();
		}
		if(_isPendingRevive)
		{
			doRevive();
		}
	}

	/**
	 * Add L2Character instance that is attacking to the attacker list.<BR><BR>
	 *
	 * @param player The L2Character that attacks this one
	 *               <p/>
	 *               <B><U> Overridden in </U> :</B><BR><BR>
	 *               <li> L2Attackable : Add to list only for attackables, not all other NPCs</li><BR><BR>
	 */
	public void addAttackerToAttackByList(L2Character player)
	{
		// DS: moved to L2Attackable
	}

	// =========================================================
	// Method - Private

	/**
	 * Send a packet to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * L2PcInstance in the detection area of the L2Character are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of state modification on the L2Character, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 *
	 * @param mov
	 */
	public void broadcastPacket(L2GameServerPacket mov)
	{
		getKnownList().getKnownPlayers().values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(mov));
	}

	/**
	 * Шлет пакет персонажам, находящимся в одном инстансе с действующим актором
	 * @param mov пакет
	 */
	public void broadcastPacketInInstance(L2GameServerPacket mov)
	{
		getKnownList().getKnownPlayers().values().stream().filter(player -> player != null && player.getInstanceId() == getInstanceId()).forEach(player -> player.sendPacket(mov));
	}

	/**
	 * Отправляет пакет всем персонажам в указанном радиусе
	 * @param mov пакет
	 * @param radiusInKnownlist радиус броадкаста
	 */
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		getKnownList().getKnownPlayersInRadius(radiusInKnownlist).stream().filter(player -> player != null).forEach(player -> player.sendPacket(mov));
	}

	/**
	 * @param barPixels
	 * @return true if hp update should be done, false if not
	 */
	protected boolean needHpUpdate(int barPixels)
	{
		double currentHp = getCurrentHp();
		double maxHp = getMaxVisibleHp();

		if(currentHp <= 1.0 || maxHp < barPixels)
		{
			return true;
		}

		if(currentHp < _hpUpdateDecCheck || Math.abs(currentHp - _hpUpdateDecCheck) <= 1.0e-6 ||
			currentHp > _hpUpdateIncCheck || Math.abs(currentHp - _hpUpdateIncCheck) <= 1.0e-6)
		{
			if(Math.abs(currentHp - maxHp) <= 1.0e-6)
			{
				_hpUpdateIncCheck = currentHp + 1;
				_hpUpdateDecCheck = currentHp - _hpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentHp / _hpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_hpUpdateDecCheck = _hpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_hpUpdateIncCheck = _hpUpdateDecCheck + _hpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create the Server->Client packet StatusUpdate with current HP and MP </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all
	 * L2Character called _statusListener that must be informed of HP/MP updates of this L2Character </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND CP information</B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Send current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party</li><BR><BR>
	 */
	public void broadcastStatusUpdate()
	{
		if(getStatus().getStatusListener().isEmpty() || !needHpUpdate(352))
		{
			return;
		}

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "Broadcast Status Update for " + getObjectId() + '(' + getName() + "). HP: " + getCurrentHp());
		}

		// Create the Server->Client packet StatusUpdate with current HP
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());

		// Go through the StatusListener
		// Send the Server->Client packet StatusUpdate with current HP and MP

		getStatus().getStatusListener().stream().filter(temp -> temp != null).forEach(temp -> temp.sendPacket(su));
	}

	/**
	 * Not Implemented.<BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @param text
	 */
	public void sendMessage(String text)
	{
		// default implementation
	}

	/**
	 * Teleport a L2Character and its pet if necessary.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Stop the movement of the L2Character</li>
	 * <li>Set the x,y,z position of the L2Object and if necessary modify its _worldRegion</li>
	 * <li>Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in its _KnownPlayers</li>
	 * <li>Modify the position of the pet if necessary</li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @param x
	 * @param y
	 * @param z
	 * @param heading
	 * @param randomOffset
	 */
	public void teleToLocation(int x, int y, int z, int heading, int randomOffset)
	{
		// Stop movement
		stopMove(null, false);
		abortAttack();
		abortCast();

		setIsTeleporting(true);
		setTarget(null);

		getAI().setIntention(AI_INTENTION_ACTIVE);

		if(Config.OFFSET_ON_TELEPORT_ENABLED && randomOffset > 0)
		{
			x += Rnd.get(-randomOffset, randomOffset);
			y += Rnd.get(-randomOffset, randomOffset);
		}

		z += 5;

		// Send a Server->Client packet TeleportToLocationt to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		broadcastPacket(new TeleportToLocation(this, x, y, z, heading));

		// remove the object from its old location
		getLocationController().decay();

		//Сразу после удаления всех объектов после тп шлется ExTeleportToLocationActivate
		broadcastPacket(new ExTeleportToLocationActivate(this, x, y, z, heading));

		// Set the x,y,z position of the L2Object and if necessary modify its _worldRegion
		getLocationController().setXYZ(x, y, z);

		// temporary fix for heading on teleports
		if(heading != 0)
		{
			getLocationController().setHeading(heading);
		}

		// allow recall of the detached characters
		if(!isPlayer() || getActingPlayer().getClient() != null && getActingPlayer().getClient().isDetached())
		{
			onTeleported();
		}

		revalidateZone(true);
	}

	public void teleToLocation(int x, int y, int z)
	{
		teleToLocation(x, y, z, _heading, 0);
	}

	public void teleToLocation(int x, int y, int z, int randomOffset)
	{
		teleToLocation(x, y, z, _heading, randomOffset);
	}

	public void teleToLocation(Location loc, int randomOffset)
	{
		int x = loc.getX();
		int y = loc.getY();
		int z = loc.getZ();
		teleToLocation(x, y, z, _heading, randomOffset);
	}

	public void teleToLocation(TeleportWhereType teleportWhere)
	{
		teleToLocation(MapRegionManager.getInstance().getTeleToLocation(this, teleportWhere), true);
	}

	public void teleToLocation(Location loc)
	{
		teleToLocation(loc, 0);
	}

	public void teleToLocation(Location loc, boolean allowRandomOffset)
	{
		if(allowRandomOffset)
		{
			teleToLocation(loc, Config.MAX_OFFSET_ON_TELEPORT);
		}
		else
		{
			teleToLocation(loc, 0);
		}
	}

	public void teleToLocation(int x, int y, int z, boolean allowRandomOffset)
	{
		if(allowRandomOffset)
		{
			teleToLocation(x, y, z, Config.MAX_OFFSET_ON_TELEPORT);
		}
		else
		{
			teleToLocation(x, y, z, 0);
		}
	}

	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if(allowRandomOffset)
		{
			teleToLocation(x, y, z, heading, Config.MAX_OFFSET_ON_TELEPORT);
		}
		else
		{
			teleToLocation(x, y, z, heading, 0);
		}
	}

	/**
	 * Телепортация персонажа в указанное место и инстанс
	 * @param loc место, куда телепортировать
	 * @param instanceId ID инстанса, в который телепортировать
	 */
	public void teleToInstance(Location loc, int instanceId)
	{
		teleToInstance(loc, instanceId, true);
	}

	public void teleToInstance(Location loc, int instanceId, boolean allowRandomOffset)
	{
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		getInstanceController().setInstanceId(instanceId);
		teleToLocation(loc, allowRandomOffset);
	}

	/**
	 * Launch a physical attack against a target (Simple, Bow, Pole or Dual).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the active weapon (always equiped in the right hand) </li><BR><BR>
	 * <li>If weapon is a bow, check for arrows, MP and bow re-use delay (if necessary, equip the L2PcInstance with arrows in left hand)</li>
	 * <li>If weapon is a bow, consume MP and set the new period of bow non re-use </li><BR><BR>
	 * <li>Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack) </li>
	 * <li>Select the type of attack to start (Simple, Bow, Pole or Dual) and verify if SoulShot are charged then start calculation</li>
	 * <li>If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character</li>
	 * <li>Notify AI with EVT_READY_TO_ACT</li><BR><BR>
	 *
	 * @param target The L2Character targeted
	 */
	protected void doAttack(L2Character target)
	{
		if(isAttackingDisabled())
		{
			return;
		}

		if(!isAlikeDead() && target != null)
		{
			if(this instanceof L2Npc && target.isAlikeDead() || !getKnownList().knowsObject(target))
			{
				getAI().setIntention(AI_INTENTION_ACTIVE);
				sendActionFailed();
				return;
			}
			else if(isPlayer())
			{
				if(target._isDead)
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendActionFailed();
					return;
				}

				L2PcInstance actor = (L2PcInstance) this;
				/*
				 * Players riding wyvern or with special (flying) transformations can do melee attacks, only with skills
				 */
				if(actor.isMounted() && actor.getMountNpcId() == 12621 || actor.isTransformed() && !actor.getTransformation().canDoMeleeAttack())
				{
					sendActionFailed();
					return;
				}
			}
			// skills can be used on Walls and Doors only during siege
			else if(target instanceof L2DoorInstance)
			{
				boolean isCastle = ((L2DoorInstance) target).getCastle() != null && ((L2DoorInstance) target).getCastle().getCastleId() > 0 && ((L2DoorInstance) target).getCastle().getSiege().isInProgress();
				boolean isFort = ((L2DoorInstance) target).getFort() != null && ((L2DoorInstance) target).getFort().getFortId() > 0 && ((L2DoorInstance) target).getFort().getSiege().isInProgress() && !((L2DoorInstance) target).isCommanderDoor();
				if(!isCastle && !isFort && ((L2DoorInstance) target).isUnlockable())
				{
					sendActionFailed();
					return;
				}
			}
		}

		// Check if attacker's weapon can attack
		if(getActiveWeaponItem() != null)
		{
			L2Weapon wpn = getActiveWeaponItem();
			if(!wpn.isAttackWeapon() && !isGM() && wpn.hasSkills())
			{
				if(wpn.getItemType() == L2WeaponType.FISHINGROD)
				{
					sendPacket(SystemMessageId.CANNOT_ATTACK_WITH_FISHING_POLE);
				}
				else
				{
					sendPacket(SystemMessageId.THAT_WEAPON_CANT_ATTACK);
				}
				sendActionFailed();
				return;
			}
		}

		if(getActingPlayer() != null)
		{
			if(getActingPlayer().getObserverController().isObserving())
			{
				sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
				sendActionFailed();
				return;
			}
			else if(target.getActingPlayer() != null && getActingPlayer().getSiegeSide() != PlayerSiegeSide.NONE && isInsideZone(ZONE_SIEGE) && target.getActingPlayer().getSiegeSide() == getActingPlayer().getSiegeSide() && !target.getActingPlayer().equals(this) && target.getActingPlayer().getActiveSiegeId() == getActingPlayer().getActiveSiegeId())
			{
				sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				sendActionFailed();
				return;
			}
			// Checking if target has moved to peace zone
			else if(target.isInsidePeaceZone(getActingPlayer()))
			{
				getAI().setIntention(AI_INTENTION_ACTIVE);
				sendActionFailed();
				return;
			}
		}
		else if(isInsidePeaceZone(this, target))
		{
			getAI().setIntention(AI_INTENTION_ACTIVE);
			sendActionFailed();
			return;
		}

		if(hasBuffsRemovedOnAction())
		{
			stopEffectsOnAction();
		}

		// Останавливаем эффекты, которые должны сниматься при атакующих действиях персонажа
		if(hasBuffsRemovedOnAttack())
		{
			L2PcInstance actingPlayer = getActingPlayer();
			if(actingPlayer != null)
			{
				// Используется в частности для умения "Движение зла" (при атакующих действиях одного из сопартийцев - снимаем баф у всех в группе)
				if(actingPlayer.getParty() != null)
				{
					actingPlayer.getParty().getMembers().stream().filter(pm -> Util.checkIfInRange(900, actingPlayer, pm, true)).forEach(L2Character::stopEffectsOnAttack);
				}
				else
				{
					stopEffectsOnAttack();
				}
			}
		}

		// Get the active weapon instance (always equiped in the right hand)
		L2ItemInstance weaponInst = getActiveWeaponInstance();

		// Get the active weapon item corresponding to the active weapon instance (always equiped in the right hand)
		L2Weapon weaponItem = getActiveWeaponItem();

		// GeoData Los Check here (or dz > 1000)
		if(!GeoEngine.getInstance().canSeeTarget(this, target))
		{
			sendPacket(SystemMessageId.CANT_SEE_TARGET);
			getAI().setIntention(AI_INTENTION_ACTIVE);
			sendActionFailed();
			return;
		}

		// BOW and CROSSBOW checks
		if(weaponItem != null && !isTransformed())
		{
			if(weaponItem.getItemType() == L2WeaponType.BOW)
			{
				//Check for arrows and MP
				if(isPlayer())
				{
					// Checking if target has moved to peace zone - only for player-bow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if(target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(AI_INTENTION_ACTIVE);
						sendActionFailed();
						return;
					}

					// Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if(!checkAndEquipArrows())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendActionFailed();
						sendPacket(SystemMessageId.NOT_ENOUGH_ARROWS);
						return;
					}

					// Verify if the bow can be use
					if(_disableBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int mpConsume = weaponItem.getMpConsume();
						if(weaponItem.getReducedMpConsume() > 0 && weaponItem.getReducedMpConsumeChance())
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}

						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);

						if(getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendActionFailed();
							return;
						}
						// If L2PcInstance have enough MP, the bow consumes it
						if(mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}

						// Set the period of bow no re-use
						_disableBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getInstance().getGameTicks();
					}
					else
					{
						// Cancel the action because the bow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendActionFailed();
						return;
					}
				}
			}
			if(weaponItem.getItemType() == L2WeaponType.CROSSBOW || weaponItem.getItemType() == L2WeaponType.TWOHANDCROSSBOW)
			{
				//Check for bolts
				if(isPlayer())
				{
					// Checking if target has moved to peace zone - only for player-crossbow attacks at the moment
					// Other melee is checked in movement code and for offensive spells a check is done every time
					if(target.isInsidePeaceZone(getActingPlayer()))
					{
						getAI().setIntention(AI_INTENTION_ACTIVE);
						sendActionFailed();
						return;
					}

					// Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True
					if(!checkAndEquipBolts())
					{
						// Cancel the action because the L2PcInstance have no arrow
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						sendActionFailed();
						sendPacket(SystemMessageId.NOT_ENOUGH_BOLTS);
						return;
					}

					// Verify if the crossbow can be use
					if(_disableCrossBowAttackEndTime <= GameTimeController.getInstance().getGameTicks())
					{
						// Verify if L2PcInstance owns enough MP
						int mpConsume = weaponItem.getMpConsume();
						if(weaponItem.getReducedMpConsume() > 0 && weaponItem.getReducedMpConsumeChance())
						{
							mpConsume = weaponItem.getReducedMpConsume();
						}
						mpConsume = (int) calcStat(Stats.BOW_MP_CONSUME_RATE, mpConsume, null, null);

						if(getCurrentMp() < mpConsume)
						{
							// If L2PcInstance doesn't have enough MP, stop the attack
							ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
							sendPacket(SystemMessageId.NOT_ENOUGH_MP);
							sendActionFailed();
							return;
						}

						// If L2PcInstance have enough MP, the bow consumes it
						if(mpConsume > 0)
						{
							getStatus().reduceMp(mpConsume);
						}
						// Set the period of crossbow no re-use
						_disableCrossBowAttackEndTime = 5 * GameTimeController.TICKS_PER_SECOND + GameTimeController.getInstance().getGameTicks();
					}
					else
					{
						// Cancel the action because the crossbow can't be re-use at this moment
						ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), 1000);
						sendActionFailed();
						return;
					}
				}
				else if(this instanceof L2Npc)
				{
					if(_disableCrossBowAttackEndTime > GameTimeController.getInstance().getGameTicks())
					{
						return;
					}
				}
			}
		}

		// Add the L2PcInstance to _knownObjects and _knownPlayer of the target
		target.getKnownList().addKnownObject(this);

		// Reduce the current CP if TIREDNESS configuration is activated
		if(Config.ALT_GAME_TIREDNESS)
		{
			setCurrentCp(getCurrentCp() - 10);
		}

		// Recharge any active auto soulshot tasks for player (or player's summon if one exists).
		if(isPlayer() || isSummon())
		{
			getActingPlayer().rechargeAutoSoulShot(true, false, isSummon());
		}

		// Verify if soulshots are charged.
		boolean wasSSCharged;

		wasSSCharged = isSummon() && !(this instanceof L2PetInstance && weaponInst != null) ? ((L2Summon) this).getChargedSoulShot() != L2ItemInstance.CHARGED_NONE : weaponInst != null && weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE;

		if(isL2Attackable())
		{
			if(((L2Npc) this).useSoulShot(false))
			{
				wasSSCharged = true;
			}
		}

		// Get the Attack Speed of the L2Character (delay (in milliseconds) before next attack)
		int timeAtk = calculateTimeBetweenAttacks(target, weaponItem);
		// the hit is calculated to happen halfway to the animation - might need further tuning e.g. in bow case
		int timeToHit = timeAtk / 2;
		_attackEndTime = GameTimeController.getInstance().getGameTicks();
		_attackEndTime += timeAtk / GameTimeController.MILLIS_IN_TICK;
		_attackEndTime -= 1;

		SoulshotGrade soulshotGrade = SoulshotGrade.SS_NG;

		if(weaponItem != null)
		{
			soulshotGrade = weaponItem.getSoulshotGradeForItem();
		}

		// Create a Server->Client packet Attack
		Attack attack = new Attack(this, target, wasSSCharged, soulshotGrade);

		// Set the Attacking Body part to CHEST
		setAttackingBodypart();
		// Make sure that char is facing selected target
		// also works: setHeading(Util.convertDegreeToClientHeading(Util.calculateAngleFrom(this, target)));
		_heading = Util.calculateHeadingFrom(this, target);

		// Get the Attack Reuse Delay of the L2Weapon
		int reuse = calculateReuseTime(target, weaponItem);
		boolean hitted;
		// Select the type of attack to start
		if(weaponItem == null || isTransformed())
		{
			hitted = doAttackHitSimple(attack, target, timeToHit);
		}
		else if(weaponItem.getItemType() == L2WeaponType.BOW)
		{
			hitted = doAttackHitByBow(attack, target, timeAtk, reuse);
		}
		else if(weaponItem.getItemType() == L2WeaponType.CROSSBOW || weaponItem.getItemType() == L2WeaponType.TWOHANDCROSSBOW)
		{
			hitted = doAttackHitByCrossBow(attack, target, timeAtk, reuse);
		}
		else if(weaponItem.getItemType() == L2WeaponType.POLE)
		{
			hitted = doAttackHitByPole(attack, target, timeToHit);
		}
		else
		{
			hitted = isUsingDualWeapon() ? doAttackHitByDual(attack, target, timeToHit) : doAttackHitSimple(attack, target, timeToHit);
		}

		// Flag the attacker if it's a L2PcInstance outside a PvP area
		L2PcInstance player = getActingPlayer();

		if(player != null)
		{
			AttackStanceTaskManager.getInstance().addAttackStanceTask(player);
			if(!(target instanceof L2Summon && player.getPets().contains(target)))
			{
				player.getPvPFlagController().updateStatus(target);
			}
		}
		// Check if hit isn't missed
		if(hitted)
		{
			/* ADDED BY nexus - 2006-08-17
			 *
			 * As soon as we know that our hit landed, we must discharge any active soulshots.
			 * This must be done so to avoid unwanted soulshot consumption.
			 */

			// If we didn't miss the hit, discharge the shoulshots, if any
			if(isSummon() && !(isPlayer() && weaponInst != null))
			{
				((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
			}
			else if(weaponInst != null)
			{
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}

			if(player != null)
			{
				if(player.isCursedWeaponEquipped())
				{
					// If hitted by a cursed weapon, Cp is reduced to 0
					if(!target.isInvul())
					{
						target.setCurrentCp(0);
					}
				}
				else if(player.getOlympiadController().isHero())
				{
					// If a cursed weapon is hitted by a Hero, Cp is reduced to 0
					if(target instanceof L2PcInstance && ((L2PcInstance) target).isCursedWeaponEquipped())
					{
						target.setCurrentCp(0);
					}
				}
			}
		}
		else
		{
			abortAttack(); // Abort the attack of the L2Character and send Server->Client ActionFail packet
		}

		// If the Server->Client packet Attack contains at least 1 hit, send the Server->Client packet Attack
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if(attack.hasHits())
		{
			broadcastPacket(attack);

			// Ассистим декоями
			if(isPlayer() && ((L2PcInstance) this).getDecoy() != null)
			{
				((L2PcInstance) this).getDecoy().stream().filter(decoy -> decoy instanceof L2IncarnationInstance && !(target instanceof L2IncarnationInstance && ((L2IncarnationInstance) target).getOwner().equals(this))).forEach(decoy -> {
					decoy.setTarget(target);
					decoy.addDamageHate(target, 0, 999);
					decoy.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
				});
			}
		}

		// Notify AI with EVT_READY_TO_ACT
		ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_READY_TO_ACT), timeAtk + reuse);
	}

	/**
	 * Launch a Bow attack.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume arrows </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the bow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk   The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = AttackSpeedAndMiss.calcHitMiss(this, target);

		// Consume arrows
		reduceArrowCount(false);

		_move = null;

		// Check if hit isn't missed
		if(!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Shield.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = PhysicalDamage.calcCrit(getStat().getCriticalHit(target, null), false, target);

			// Calculate physical damages
			damage1 = (int) PhysicalDamage.calcPhysDam(this, target, null, shld1, crit1, false, attack._useSoulshots);
		}

		// Check if the L2Character is a L2PcInstance
		if(isPlayer())
		{
			sendPacket(new SetupGauge(SetupGauge.RED_MINI, sAtk + reuse));
		}

		// Create a new hit task with Medium priority
		ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._useSoulshots, shld1), sAtk);

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getInstance().getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Launch a CrossBow attack.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>Consume bolts </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>If the L2Character is a L2PcInstance, Send a Server->Client packet SetupGauge </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Calculate and set the disable delay of the crossbow in function of the Attack Speed</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk   The Attack Speed of the attacker
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitByCrossBow(Attack attack, L2Character target, int sAtk, int reuse)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = AttackSpeedAndMiss.calcHitMiss(this, target);

		// Consume bolts
		reduceArrowCount(true);

		_move = null;

		// Check if hit isn't missed
		if(!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Shield.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = PhysicalDamage.calcCrit(getStat().getCriticalHit(target, null), false, target);

			// Calculate physical damages
			damage1 = (int) PhysicalDamage.calcPhysDam(this, target, null, shld1, crit1, false, attack._useSoulshots);
		}

		// Check if the L2Character is a L2PcInstance
		if(isPlayer())
		{
			// Send a Server->Client packet SetupGauge
			sendPacket(new SetupGauge(SetupGauge.RED_MINI, sAtk + reuse));
		}

		// Create a new hit task with Medium priority
		if(isL2Attackable())
		{
			if(((L2Attackable) this)._soulshotcharged)
			{
				// Create a new hit task with Medium priority
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, true, shld1), sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, false, shld1), sAtk);
			}
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._useSoulshots, shld1), sAtk);
		}

		// Calculate and set the disable delay of the bow in function of the Attack Speed
		_disableCrossBowAttackEndTime = (sAtk + reuse) / GameTimeController.MILLIS_IN_TICK + GameTimeController.getInstance().getGameTicks();

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Launch a Dual attack.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hits are missed or not </li>
	 * <li>If hits aren't missed, calculate if shield defense is efficient </li>
	 * <li>If hits aren't missed, calculate if hit is critical </li>
	 * <li>If hits aren't missed, calculate physical damages </li>
	 * <li>Create 2 new hit tasks with Medium priority</li>
	 * <li>Add those hits to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk
	 * @return True if hit 1 or hit 2 isn't missed
	 */
	private boolean doAttackHitByDual(Attack attack, L2Character target, int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		byte shld1 = 0;
		byte shld2 = 0;
		boolean crit1 = false;
		boolean crit2 = false;

		// Calculate if hits are missed or not
		boolean miss1 = AttackSpeedAndMiss.calcHitMiss(this, target);
		boolean miss2 = AttackSpeedAndMiss.calcHitMiss(this, target);

		// Check if hit 1 isn't missed
		if(!miss1)
		{
			// Calculate if shield defense is efficient against hit 1
			shld1 = Shield.calcShldUse(this, target);

			// Calculate if hit 1 is critical
			crit1 = PhysicalDamage.calcCrit(getStat().getCriticalHit(target, null), false, target);

			// Calculate physical damages of hit 1
			damage1 = (int) PhysicalDamage.calcPhysDam(this, target, null, shld1, crit1, true, attack._useSoulshots);
			damage1 /= 2;
		}

		// Check if hit 2 isn't missed
		if(!miss2)
		{
			// Calculate if shield defense is efficient against hit 2
			shld2 = Shield.calcShldUse(this, target);

			// Calculate if hit 2 is critical
			crit2 = PhysicalDamage.calcCrit(getStat().getCriticalHit(target, null), false, target);

			// Calculate physical damages of hit 2
			damage2 = (int) PhysicalDamage.calcPhysDam(this, target, null, shld2, crit2, true, attack._useSoulshots);
			damage2 /= 2;
		}

		if(isL2Attackable())
		{
			if(((L2Attackable) this)._soulshotcharged)
			{

				// Create a new hit task with Medium priority for hit 1
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, true, shld1), sAtk / 2);

				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, true, shld2), sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, false, shld1), sAtk / 2);

				// Create a new hit task with Medium priority for hit 2 with a higher delay
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, false, shld2), sAtk);
			}
		}
		else
		{
			// Create a new hit task with Medium priority for hit 1
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._useSoulshots, shld1), sAtk / 2);

			// Create a new hit task with Medium priority for hit 2 with a higher delay
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage2, crit2, miss2, attack._useSoulshots, shld2), sAtk);
		}

		// Add those hits to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1), attack.createHit(target, damage2, miss2, crit2, shld2));

		// Return true if hit 1 or hit 2 isn't missed
		return !miss1 || !miss2;
	}

	/**
	 * Launch a Pole attack.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get all visible objects in a spherical area near the L2Character to obtain possible targets </li>
	 * <li>If possible target is the L2Character targeted, launch a simple attack against it </li>
	 * <li>If possible target isn't the L2Character targeted but is attackable, launch a simple attack against it </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target
	 * @param sAtk
	 * @return True if one hit isn't missed
	 */
	private boolean doAttackHitByPole(Attack attack, L2Character target, int sAtk)
	{
		//double angleChar;
		int maxRadius = getPhysicalAttackRange();
		int maxAngleDiff = (int) getStat().calcStat(Stats.POWER_ATTACK_ANGLE, 120, null, null);

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "doAttackHitByPole: Max radius = " + maxRadius);
			_log.log(Level.DEBUG, "doAttackHitByPole: Max angle = " + maxAngleDiff);
		}

		// o1 x: 83420 y: 148158 (Giran)
		// o2 x: 83379 y: 148081 (Giran)
		// dx = -41
		// dy = -77
		// distance between o1 and o2 = 87.24
		// arctan2 = -120 (240) degree (excel arctan2(dx, dy); java arctan2(dy, dx))
		//
		// o2
		//
		//          o1 ----- (heading)
		// In the diagram above:
		// o1 has a heading of 0/360 degree from horizontal (facing East)
		// Degree of o2 in respect to o1 = -120 (240) degree
		//
		// o2          / (heading)
		//            /
		//          o1
		// In the diagram above
		// o1 has a heading of -80 (280) degree from horizontal (facing north east)
		// Degree of o2 in respect to 01 = -40 (320) degree

		// Get char's heading degree
		// angleChar = Util.convertHeadingToDegree(getHeading());
		// In H5 ATTACK_COUNT_MAX 1 is by default and 2 was in skill 3599, total 3.
		int attackRandomCountMax = (int) getStat().calcStat(Stats.ATTACK_COUNT_MAX, 1, null, null) - 1;
		int attackcount = 0;

		/*if (angleChar <= 0)
		    angleChar += 360;*/
		// ===========================================================

		boolean hitted = doAttackHitSimple(attack, target, 100, sAtk);
		double attackpercent = 85;
		L2Character temp;

		for(L2Object obj : getKnownList().getKnownObjects().values())
		{
			if(obj.equals(target))
			{
				continue; // do not hit twice
			}
			// Check if the L2Object is a L2Character
			if(obj instanceof L2Character)
			{
				if(obj instanceof L2PetInstance && isPlayer() && ((L2PetInstance) obj).getOwner().equals(this))
				{
					continue;
				}

				if(!Util.checkIfInRange(maxRadius, this, obj, false))
				{
					continue;
				}

				// otherwise hit too high/low. 650 because mob z coord
				// sometimes wrong on hills
				if(Math.abs(obj.getZ() - getZ()) > 650)
				{
					continue;
				}
				if(!isFacing(obj, maxAngleDiff))
				{
					continue;
				}

				if(isL2Attackable() && obj instanceof L2PcInstance && _target instanceof L2Attackable)
				{
					continue;
				}

				if(isL2Attackable() && obj instanceof L2Attackable && ((L2Attackable) this).getEnemyClan() == null && ((L2Attackable) this).getIsChaos() == 0)
				{
					continue;
				}

				if(isL2Attackable() && obj instanceof L2Attackable && !((L2Attackable) this).getEnemyClan().equals(((L2Attackable) obj).getClan()) && ((L2Attackable) this).getIsChaos() == 0)
				{
					continue;
				}

				temp = (L2Character) obj;

				// Launch a simple attack against the L2Character targeted
				if(!temp.isAlikeDead())
				{
					if(temp.equals(getAI().getAttackTarget()) || temp.isAutoAttackable(this))
					{
						hitted |= doAttackHitSimple(attack, temp, attackpercent, sAtk);
						attackpercent /= 1.15;

						attackcount++;
						if(attackcount > attackRandomCountMax)
						{
							break;
						}
					}
				}
			}
		}

		// Return true if one hit isn't missed
		return hitted;
	}

	/**
	 * Launch a simple attack.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate if hit is missed or not </li>
	 * <li>If hit isn't missed, calculate if shield defense is efficient </li>
	 * <li>If hit isn't missed, calculate if hit is critical </li>
	 * <li>If hit isn't missed, calculate physical damages </li>
	 * <li>Create a new hit task with Medium priority</li>
	 * <li>Add this hit to the Server-Client packet Attack </li><BR><BR>
	 *
	 * @param attack Server->Client packet Attack in which the hit will be added
	 * @param target The L2Character targeted
	 * @param sAtk
	 * @return True if the hit isn't missed
	 */
	private boolean doAttackHitSimple(Attack attack, L2Character target, int sAtk)
	{
		return doAttackHitSimple(attack, target, 100, sAtk);
	}

	private boolean doAttackHitSimple(Attack attack, L2Character target, double attackpercent, int sAtk)
	{
		int damage1 = 0;
		byte shld1 = 0;
		boolean crit1 = false;

		// Calculate if hit is missed or not
		boolean miss1 = AttackSpeedAndMiss.calcHitMiss(this, target);

		// Check if hit isn't missed
		if(!miss1)
		{
			// Calculate if shield defense is efficient
			shld1 = Shield.calcShldUse(this, target);

			// Calculate if hit is critical
			crit1 = PhysicalDamage.calcCrit(getStat().getCriticalHit(target, null), false, target);

			// Calculate physical damages
			damage1 = (int) PhysicalDamage.calcPhysDam(this, target, null, shld1, crit1, false, attack._useSoulshots);

			if(attackpercent != 100)
			{
				damage1 = (int) (damage1 * attackpercent / 100);
			}
		}

		// Create a new hit task with Medium priority
		if(isL2Attackable())
		{
			if(((L2Attackable) this)._soulshotcharged)
			{
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, true, shld1), sAtk);
			}
			else
			{
				ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, false, shld1), sAtk);
			}

		}
		else
		{
			ThreadPoolManager.getInstance().scheduleAi(new HitTask(target, damage1, crit1, miss1, attack._useSoulshots, shld1), sAtk);
		}

		// Add this hit to the Server-Client packet Attack
		attack.hit(attack.createHit(target, damage1, miss1, crit1, shld1));

		// Return true if hit isn't missed
		return !miss1;
	}

	/**
	 * Manage the casting task (casting and interrupt time, re-use delay...) and display the casting bar and animation on client.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Verify the possibilty of the the cast : skill is a spell, caster isn't muted... </li>
	 * <li>Get the list of all targets (ex : area effects) and define the L2Charcater targeted (its stats will be used in calculation)</li>
	 * <li>Calculate the casting time (base + modifier of MAtkSpd), interrupt time and re-use delay</li>
	 * <li>Send a Server->Client packet MagicSkillUser (to diplay casting animation), a packet SetupGauge (to display casting bar) and a system message </li>
	 * <li>Disable all skills during the casting time (create a task EnableAllSkills)</li>
	 * <li>Disable the skill during the re-use delay (create a task EnableSkill)</li>
	 * <li>Create a task MagicUseTask (that will call method onMagicUseTimer) to launch the Magic Skill at the end of the casting time</li><BR><BR>
	 *
	 * @param skill The L2Skill to use
	 */
	public void doCast(L2Skill skill)
	{
		beginCast(skill, false);
	}

	public void doSimultaneousCast(L2Skill skill)
	{
		beginCast(skill, true);
	}

	public void doCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if(!checkDoCastConditions(skill))
		{
			if(_isDoubleCastingNow)
			{
				_isDoubleCastingNow = false;
			}
			else
			{
				_isCastingNow = false;
			}
			return;
		}
		// Override casting type
		if(skill.isSimultaneousCast())
		{
			doSimultaneousCast(skill, target, targets);
			return;
		}

		if(hasBuffsRemovedOnAction())
		{
			stopEffectsOnAction();
		}

		// Останавливаем эффекты, которые должны сниматься при атакующих действиях персонажа
		if(skill.isOffensive())
		{
			if(hasBuffsRemovedOnAttack())
			{
				L2PcInstance actingPlayer = getActingPlayer();
				if(actingPlayer != null)
				{
					if(actingPlayer.getParty() != null)
					{
						actingPlayer.getParty().getMembers().stream().filter(pm -> Util.checkIfInRange(900, actingPlayer, pm, true)).forEach(L2Character::stopEffectsOnAttack);
					}
					else
					{
						stopEffectsOnAttack();
					}
				}
			}
		}

		// Recharge AutoSoulShot
		// this method should not used with L2Playable

		beginCast(skill, false, target, targets);
	}

	public void doSimultaneousCast(L2Skill skill, L2Character target, L2Object[] targets)
	{
		if(!checkDoCastConditions(skill))
		{
			_isCastingSimultaneouslyNow = false;
			return;
		}

		if(hasBuffsRemovedOnAction())
		{
			stopEffectsOnAction();
		}

		// Recharge AutoSoulShot
		// this method should not used with L2Playable

		beginCast(skill, true, target, targets);
	}

	private void beginCast(L2Skill skill, boolean simultaneously)
	{
		if(!checkDoCastConditions(skill))
		{
			if(simultaneously)
			{
				_isCastingSimultaneouslyNow = false;
			}
			else
			{
				if(_isDoubleCastingNow)
				{
					_isDoubleCastingNow = false;
				}
				else
				{
					_isCastingNow = false;
				}
			}
			if(isPlayer())
			{
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}
		// Override casting type
		if(skill.isSimultaneousCast() && !simultaneously)
		{
			simultaneously = true;
		}

		if(hasBuffsRemovedOnAction())
		{
			stopEffectsOnAction();
		}

		// Останавливаем эффекты, которые должны сниматься при атакующих действиях персонажа
		if(skill.isOffensive())
		{
			if(hasBuffsRemovedOnAttack())
			{
				L2PcInstance actingPlayer = getActingPlayer();
				if(actingPlayer != null)
				{
					if(actingPlayer.getParty() != null)
					{
						actingPlayer.getParty().getMembers().stream().filter(pm -> Util.checkIfInRange(900, actingPlayer, pm, true)).forEach(L2Character::stopEffectsOnAttack);
					}
					else
					{
						stopEffectsOnAttack();
					}
				}
			}
		}

		//Recharge AutoSoulShot
		if(isPlayer() || isSummon())
		{
			getActingPlayer().rechargeAutoSoulShot(skill.useSoulShot(), skill.useSpiritShot(), isSummon());
		}

		// Set the target of the skill in function of Skill Type and Target Type
		L2Character target = null;
		// Get all possible targets of the skill in a table in function of the skill target type
		L2Object[] targets = skill.getTargetList(this);

		boolean doit = false;

		// AURA skills should always be using caster as target
		switch(skill.getTargetType())
		{
			case TARGET_AREA_SUMMON:    // We need it to correct facing
				target = getPets().getFirst(); // TODO: target = getPets();
				break;
			case TARGET_AURA:
			case TARGET_AURA_CORPSE_MOB:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SUBLIME:
			case TARGET_CORPSE_CLAN:
			case TARGET_CORPSE_COMMAND_CHANNEL:
			case TARGET_CORPSE_PARTY:
				target = this;
				break;
			case TARGET_SELF:
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_SUMMON_AND_ME:
			case TARGET_OWNER_PET:
			case TARGET_PARTY:
			case TARGET_CLAN:
			case TARGET_PARTY_CLAN:
			case TARGET_ALLY:
			case TARGET_MENTOR:
				doit = true;
			default:
				if(targets.length == 0)
				{
					if(simultaneously)
					{
						_isCastingSimultaneouslyNow = false;
					}
					else
					{
						if(_isDoubleCastingNow)
						{
							_isDoubleCastingNow = false;
						}
						else
						{
							_isCastingNow = false;
						}
					}
					// Send a Server->Client packet ActionFail to the L2PcInstance
					if(isPlayer())
					{
						sendActionFailed();
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET));
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}

				switch(skill.getSkillType())
				{
					case BUFF:
					case HEAL:
					case COMBATPOINTHEAL:
					case MANAHEAL:
						doit = true;
						break;
				}

				target = doit ? (L2Character) targets[0] : (L2Character) _target;
		}
		beginCast(skill, simultaneously, target, targets);
	}

	private void beginCast(L2Skill skill, boolean simultaneously, L2Character target, L2Object[] targets)
	{
		if(target == null)
		{
			if(simultaneously)
			{
				_isCastingSimultaneouslyNow = false;
			}
			else
			{
				if(_isDoubleCastingNow)
				{
					_isDoubleCastingNow = false;
				}
				else
				{
					_isCastingNow = false;
				}
			}
			if(isPlayer())
			{
				sendActionFailed();
				getAI().setIntention(AI_INTENTION_ACTIVE);
			}
			return;
		}

		if(skill.getSkillType() == L2SkillType.RESURRECT)
		{
			if(isResurrectionBlocked() || target.isResurrectionBlocked())
			{
				sendPacket(SystemMessageId.REJECT_RESURRECTION); // Reject resurrection
				target.sendPacket(SystemMessageId.REJECT_RESURRECTION); // Reject resurrection

				if(simultaneously)
				{
					_isCastingSimultaneouslyNow = false;
				}
				else
				{
					if(_isDoubleCastingNow)
					{
						_isDoubleCastingNow = false;
					}
					else
					{
						_isCastingNow = false;
					}
				}

				if(isPlayer())
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendActionFailed();
				}
				return;
			}
		}

		if(skill.getSkillType() == L2SkillType.RECALL)
		{
			if(isRecallBlocked() || target.isRecallBlocked())
			{
				// Вероятно должны быть систем мессаги что призыв отменен, но не нашел.

				if(simultaneously)
				{
					_isCastingSimultaneouslyNow = false;
				}
				else
				{
					if(_isDoubleCastingNow)
					{
						_isDoubleCastingNow = false;
					}
					else
					{
						_isCastingNow = false;
					}
				}

				if(isPlayer())
				{
					getAI().setIntention(AI_INTENTION_ACTIVE);
					sendActionFailed();
				}
				return;
			}
		}

		// Get the Identifier of the skill
		int magicId = skill.getId();

		// Get the Display Identifier for a skill that client can't display
		int displayId = skill.getDisplayId();

		// Get the level of the skill
		int level = skill.getLevel();

		if(level < 1)
		{
			level = 1;
		}

		// Get the casting time of the skill (base)
		int hitTime = skill.getHitTime();
		int coolTime = skill.getCoolTime();

		boolean effectWhileCasting = skill.getSkillType() == L2SkillType.FUSION || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME || skill.getSkillType() == L2SkillType.CASTTIME;

		// Calculate the casting time of the skill (base + modifier of MAtkSpd)
		// Don't modify the skill time for FORCE_BUFF skills. The skill time for those skills represent the buff time.
		if(!effectWhileCasting)
		{
			hitTime = AttackSpeedAndMiss.calcAtkSpd(this, skill, hitTime);
			if(coolTime > 0)
			{
				coolTime = AttackSpeedAndMiss.calcAtkSpd(this, skill, coolTime);
			}
		}

		int shotSave = L2ItemInstance.CHARGED_NONE;

		// Calculate altered Cast Speed due to BSpS/SpS
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if(weaponInst != null)
		{
			if(skill.isMagic() && !effectWhileCasting)
			{
				if(weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
				{
					//Only takes 60% of the time to cast a BSpS/SpS cast
					hitTime = (int) (0.60 * hitTime);
					coolTime = (int) (0.60 * coolTime);
				}
			}

			// Save shots value for repeats
			if(skill.useSoulShot())
			{
				shotSave = weaponInst.getChargedSoulshot();
			}
			else if(skill.useSpiritShot())
			{
				shotSave = weaponInst.getChargedSpiritshot();
			}
		}

		if(isNpc())
		{
			// Using SPS/BSPS Casting Time of Magic Skills is reduced in 40%
			if(((L2Npc) this).useSpiritShot())
			{
				hitTime = (int) (0.60 * hitTime);
				coolTime = (int) (0.60 * coolTime);
			}
		}

		// if skill is static
		if(skill.isStatic())
		{
			hitTime = skill.getHitTime();
			coolTime = skill.getCoolTime();
		}
		// if basic hitTime is higher than 500 than the min hitTime is 500
		else if(skill.getHitTime() >= 500 && hitTime < 500)
		{
			hitTime = 550;
		}

		// queue herbs and potions
		if(_isCastingSimultaneouslyNow && simultaneously)
		{
			ThreadPoolManager.getInstance().scheduleAi(new UsePotionTask(this, skill), 100);
			return;
		}

		// Set the _castInterruptTime and casting status (L2PcInstance already has this true)
		if(simultaneously)
		{
			_isCastingSimultaneouslyNow = true;
		}
		else
		{
			_isCastingNow = true;
		}
		// Note: _castEndTime = GameTimeController.getInstance().getGameTicks() + (coolTime + hitTime) / GameTimeController.MILLIS_IN_TICK;
		if(simultaneously)
		{
			_lastSimultaneousSkillCast = skill;
		}
		else
		{
			_castInterruptTime = -2 + GameTimeController.getInstance().getGameTicks() + hitTime / GameTimeController.MILLIS_IN_TICK;
			_lastSkillCast = skill;
		}

		// Init the reuse time of the skill
		int reuseDelay;

		if(skill.isStaticReuse() || skill.isStatic())
		{
			reuseDelay = skill.getReuseDelay();
		}
		else
		{
			reuseDelay = skill.isMagic() ? (int) (skill.getReuseDelay() * calcStat(Stats.MAGIC_REUSE_RATE, 1, null, null)) : (int) (skill.getReuseDelay() * calcStat(Stats.P_REUSE, 1, null, null));
		}

		boolean skillMastery = SkillMastery.calcSkillMastery(this, skill);

		// Skill reuse check
		if(reuseDelay > 30000 && !skillMastery)
		{
			addTimeStamp(skill, reuseDelay);
		}

		// Check if this skill consume mp on start casting
		int initmpcons = getStat().getMpInitialConsume(skill);
		if(initmpcons > 0)
		{
			getStatus().reduceMp(initmpcons);
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
			sendPacket(su);
		}

		// Disable the skill during the re-use delay and create a task EnableSkill with Medium priority to enable it at the end of the re-use delay
		if(reuseDelay > 10)
		{
			if(skillMastery)
			{
				reuseDelay = 100;

				if(getActingPlayer() != null)
				{
					getActingPlayer().sendPacket(SystemMessageId.SKILL_READY_TO_USE_AGAIN);
				}
			}

			disableSkill(skill, reuseDelay);
		}

		// Make sure that char is facing selected target
		if(!target.equals(this))
		{
			_heading = Util.calculateHeadingFrom(this, target);
		}

		boolean acted = false;

		// For force buff skills, start the effect as long as the player is casting.
		if(effectWhileCasting)
		{
			// Consume Items if necessary and Send the Server->Client packet InventoryUpdate with Item modification to all the L2Character
			if(skill.getItemConsumeId() > 0)
			{
				if(!destroyItemByItemId(ProcessType.CONSUMEWITHOUTTRACE, skill.getItemConsumeId(), skill.getItemConsumeCount(), null, true))
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					if(simultaneously)
					{
						_isCastingSimultaneouslyNow = false;
					}
					else
					{
						if(_isDoubleCastingNow)
						{
							_isDoubleCastingNow = false;
						}
						else
						{
							_isCastingNow = false;
						}
					}

					if(isPlayer())
					{
						getAI().setIntention(AI_INTENTION_ACTIVE);
					}
					return;
				}
			}

			// Consume Souls if necessary
			if(skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
			{
				if(isPlayer())
				{
					if(!((L2PcInstance) this).decreaseSouls(skill.getSoulConsumeCount(), skill))
					{
						if(simultaneously)
						{
							_isCastingSimultaneouslyNow = false;
						}
						else
						{
							if(_isDoubleCastingNow)
							{
								_isDoubleCastingNow = false;
							}
							else
							{
								_isCastingNow = false;
							}
						}
						return;
					}
				}
			}

			if(skill.getSkillType() == L2SkillType.FUSION)
			{
				startFusionSkill(target, skill);
			}
			if(skill.getSkillType() == L2SkillType.CASTTIME)
			{
				byte shld = 0;
				boolean ss = isSoulshotCharged(skill);
				boolean sps = isSpiritshotCharged(skill);
				boolean bss = isBlessedSpiritshotCharged(skill);

				if(skill.isDebuff())
				{
					acted = Skills.calcSkillSuccess(this, target, skill, shld, ss, sps, bss);

					if(acted)
					{
						startCastTimeSkill(target, skill);
					}
					else
					{
						// некрасивый костыль для скилла типа каст тайм, который усаммонера при фейле скилла каст должен обрываться
						// (заставить обрываться скилл совсем нельзя иначе не будет изображения отката скилла!!! Поэтому пришлось слать MSU с hitTime = 1000 чтобы скил скастовывался мгновенно.)
						abortCast();
						broadcastPacket(new MagicSkillUse(this, target, displayId, level, 1000, reuseDelay));
						return;
					}
				}
				else
				{
					startCastTimeSkill(target, skill);
				}
			}
			else
			{
				callSkill(skill, targets);
			}
		}

		/* Проверка для клан скиллов которые требуют фейм чара для использования. */
		if(skill.getFameConsumeSelf() > 0)
		{
			if(isPlayer())
			{
				if(!getActingPlayer().decreaseReputationsSelf(skill.getFameConsumeSelf()))
				{
					abortCast();
					return;
				}
			}
		}
		/* Проверка для клан скиллов которые требуют репутацию клана для использования. */
		if(skill.getFameConsumeClan() > 0)
		{
			if(isPlayer())
			{
				if(!getActingPlayer().decreaseReputationsClan(skill.getFameConsumeClan()))
				{
					abortCast();
					return;
				}
			}
		}

		// Send a Server->Client packet MagicSkillUser with target, displayId, level, skillTime, reuseDelay
		// to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		// Не шлем пакет если это тогл
		if(!skill.isToggle())
		{
			broadcastPacket(new MagicSkillUse(this, target, displayId, level, hitTime, reuseDelay));
		}

		// Send a system message USE_S1 to the L2Character
		if(isPlayer() && magicId != 1312)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_S1).addSkillName(skill));
		}

		if(isPlayable())
		{
			if(!effectWhileCasting && skill.getItemConsumeId() > 0)
			{
				if(!destroyItemByItemId(ProcessType.CONSUME, skill.getItemConsumeId(), skill.getItemConsumeCount(), null, true))
				{
					getActingPlayer().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
					abortCast();
					return;
				}
			}

			//reduce talisman mana on skill use
			if(skill.getReferenceItemId() > 0 && ItemTable.getInstance().getTemplate(skill.getReferenceItemId()).getBodyPart() == L2Item.SLOT_DECO)
			{
				for(L2ItemInstance item : getInventory().getItemsByItemId(skill.getReferenceItemId()))
				{
					if(item.isEquipped())
					{
						item.decreaseMana(false, item.useSkillDisTime());
						break;
					}
				}
			}
		}

		// Before start AI Cast Broadcast Fly Effect is Need
		if(skill.getFlyType() != null)
		{
			ThreadPoolManager.getInstance().scheduleEffect(new FlyToLocationTask(this, target, skill), 50);
		}

		MagicUseTask mut = new MagicUseTask(targets, skill, hitTime, coolTime, simultaneously, shotSave, _isDoubleCastingNow);

		// launch the magic in hitTime milliseconds
		if(hitTime > 410)
		{
			// Send a Server->Client packet SetupGauge with the color of the gauge and the casting time
			if(isPlayer() && !effectWhileCasting)
			{
				sendPacket(new SetupGauge(SetupGauge.BLUE, hitTime));
			}

			if(skill.getHitCounts() > 0)
			{
				hitTime = hitTime * skill.getHitTimings()[0] / 100;

				if(hitTime < 410)
				{
					hitTime = 410;
				}
			}

			if(effectWhileCasting)
			{
				mut.phase = 2;
			}

			if(simultaneously)
			{
				Future<?> future = _skillCast2;
				if(future != null)
				{
					future.cancel(true);
					_skillCast2 = null;
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
			else if(!mut.doublecasting)
			{
				Future<?> future = _skillCast;
				if(future != null)
				{
					future.cancel(true);
					_skillCast = null;
				}

				// Create a task MagicUseTask to launch the MagicSkill at the end of the casting time (hitTime)
				// For client animation reasons (party buffs especially) 400 ms before!
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
			else
			{
				Future<?> future = _skillDoubleCast;
				if(future != null)
				{
					future.cancel(true);
					_skillDoubleCast = null;
				}
				_skillDoubleCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime - 400);
			}
		}
		else
		{
			// mut.hitTime = 0;
			onMagicLaunchedTimer(mut);
		}
	}

	/**
	 * Check if casting of skill is possible
	 *
	 * @param skill
	 * @return True if casting is possible
	 */
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if(skill == null || isSkillDisabled(skill))
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendActionFailed();
			return false;
		}

		if(_disabledTargetSkills != null && _disabledTargetSkills.contains(skill.getTargetType()))
		{
			sendActionFailed();
			return false;
		}

		// Check if the caster has enough MP
		if(getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);

			// Send a Server->Client packet ActionFail to the L2PcInstance
			sendActionFailed();
			return false;
		}

		// Check if the caster has enough HP
		if(getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);

			// Send a Server->Client packet ActionFail to the L2PcInstance
			sendActionFailed();
			return false;
		}

		if(!skill.isStatic()) // Skill mute checks.
		{
			// Check if the skill is a magic spell and if the L2Character is not muted
			if(skill.isMagic())
			{
				if(isMuted())
				{
					// Send a Server->Client packet ActionFail to the L2PcInstance
					sendActionFailed();
					return false;
				}
			}
			else
			{
				// Check if the skill is physical and if the L2Character is not physical_muted
				if(isPhysicalMuted())
				{
					// Send a Server->Client packet ActionFail to the L2PcInstance
					sendActionFailed();
					return false;
				}
			}
		}

		// prevent casting signets to peace zone
		if(skill.getSkillType() == L2SkillType.SIGNET || skill.getSkillType() == L2SkillType.SIGNET_CASTTIME || skill.getSkillType() == L2SkillType.CASTTIME)
		{
			L2WorldRegion region = getLocationController().getWorldRegion();
			if(region == null)
			{
				return false;
			}
			boolean canCast = true;
			if(skill.getTargetType() == L2TargetType.TARGET_GROUND && isPlayer())
			{
				Point3D wp = ((L2PcInstance) this).getCurrentSkillWorldPosition();
				if(region.checkIfInPeaceZone(wp.getX(), wp.getY(), wp.getZ()))
				{
					canCast = false;
				}
			}
			else if(isInsideZone(ZONE_PEACE))
			{
				canCast = false;
			}
			if(!canCast)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
				return false;
			}
		}

		// Check if the caster owns the weapon needed
		if(!skill.getWeaponDependancy(this))
		{
			// Send a Server->Client packet ActionFail to the L2PcInstance
			sendActionFailed();
			return false;
		}

		// Check if the caster's weapon is limited to use only its own skills
		if(getActiveWeaponItem() != null)
		{
			L2Weapon wep = getActiveWeaponItem();
			if(wep.useWeaponSkillsOnly() && !isGM())
			{
				boolean found = false;
				for(SkillHolder sh : wep.getSkills())
				{
					if(sh.getSkillId() == skill.getId())
					{
						found = true;
					}
				}

				if(!found)
				{
					if(getActingPlayer() != null)
					{
						sendPacket(SystemMessageId.WEAPON_CAN_USE_ONLY_WEAPON_SKILL);
					}
					return false;
				}
			}
		}

		// Check if the spell consumes an Item
		// TODO: combine check and consume
		if(skill.getItemConsumeId() > 0 && getInventory() != null)
		{
			// Get the L2ItemInstance consumed by the spell
			L2ItemInstance requiredItems = getInventory().getItemByItemId(skill.getItemConsumeId());

			// Check if the caster owns enough consumed Item to cast
			if(requiredItems == null || requiredItems.getCount() < skill.getItemConsumeCount())
			{
				// Checked: when a summon skill failed, server show required consume item count
				if(skill.getSkillType() == L2SkillType.SUMMON)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SUMMONING_SERVITOR_COSTS_S2_S1).addItemName(skill.getItemConsumeId()).addNumber(skill.getItemConsumeCount()));
				}
				else
				{
					// Send a System Message to the caster
					sendPacket(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
				}
				return false;
			}
		}

		return true;
	}

	/**
	 * Index according to skill id the current timestamp of use.<br><br>
	 *
	 * @param skill id
	 * @param reuse delay
	 * <BR><B>Overridden in :</B>  (L2PcInstance)
	 */
	public void addTimeStamp(L2Skill skill, long reuse)
	{
	}

	/**
	 * Отключает возможность использования умений с указанным типом таргета.
	 *
	 * @param type Тип таргета.
	 */
	public void disableSkillsOfTargetType(L2TargetType type)
	{
		if(_disabledTargetSkills == null)
		{
			_disabledTargetSkills = new FastList<L2TargetType>().shared();
		}

		if(!_disabledTargetSkills.contains(type))
		{
			_disabledTargetSkills.add(type);
		}
	}

	/**
	 * Включает возможность использования умений определенного типа таргета, если они были отключены.
	 *
	 * @param type Тип таргета.
	 */
	public void enableSkillsOfTargetType(L2TargetType type)
	{
		if(_disabledTargetSkills == null || !_disabledTargetSkills.contains(type))
		{
			return;
		}

		_disabledTargetSkills.remove(type);
	}

	public void startFusionSkill(L2Character target, L2Skill skill)
	{
		if(skill.getSkillType() != L2SkillType.FUSION)
		{
			return;
		}

		if(_fusionSkill == null)
		{
			_fusionSkill = new FusionSkill(this, target, skill);
		}
	}

	public void startCastTimeSkill(L2Character target, L2Skill skill)
	{
		if(skill.getSkillType() != L2SkillType.CASTTIME)
		{
			return;
		}

		if(_castTimeSkill == null)
		{
			_castTimeSkill = new CastTimeSkill(this, target, skill);
		}
	}

	/**
	 * Kill the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2NpcInstance : Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li> L2Attackable : Distribute rewards (EXP, SP, Drops...) and notify Quest Engine </li>
	 * <li> L2PcInstance : Apply Death Penalty, Manage gain/loss Karma and Item Drop </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 * @return false if the player is already dead.
	 */
	public boolean doDie(L2Character killer)
	{
		// killing is only possible one time
		synchronized(this)
		{
			if(_isDead)
			{
				return false;
			}
			// now reset currentHp to zero
			setCurrentHp(0);
			_isDead = true;
		}

		// Set target to null and cancel Attack or Cast
		setTarget(null);

		// Stop movement
		stopMove(null);

		// Stop HP/MP/CP Regeneration task
		getStatus().stopHpMpRegeneration();

		L2Playable playable = null;
		if(isPlayable())
		{
			playable = L2Playable.class.cast(this);
		}
		// Stop all active skills effects in progress on the L2Character,
		// if the Character isn't affected by Soul of The Phoenix or Salvation
		if(playable != null && playable.isPhoenixBlessed())
		{
			if(playable.getCharmOfLuck()) //remove Lucky Charm if player has SoulOfThePhoenix/Salvation buff
			{
				playable.stopCharmOfLuck(null);
			}
			if(playable.isNoblesseBlessed())
			{
				playable.stopNoblesseBlessing(null);
			}
		}
		// Same thing if the Character isn't a Noblesse Blessed L2PlayableInstance
		else if(playable != null && playable.isNoblesseBlessed())
		{
			playable.stopNoblesseBlessing(null);

			if(playable.getCharmOfLuck()) //remove Lucky Charm if player have Nobless blessing buff
			{
				playable.stopCharmOfLuck(null);
			}
		}
		else
		{
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}

		if(isPlayer() && ((L2PcInstance) this).getAgathionId() != 0)
		{
			((L2PcInstance) this).setAgathionId(0);
		}
		calculateRewards(killer);

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		broadcastStatusUpdate();

		// Notify L2Character AI
		if(hasAI())
		{
			getAI().notifyEvent(CtrlEvent.EVT_DEAD);
		}

		if(getLocationController().getWorldRegion() != null)
		{
			getLocationController().getWorldRegion().onDeath(this);
		}

		getAttackByList().clear();
		// If character is PhoenixBlessed
		// or has charm of courage inside siege battlefield (exact operation to be confirmed)
		// a resurrection popup will show up
		if(isSummon())
		{
			if(((L2Summon) this).isPhoenixBlessed() && ((L2Summon) this).getOwner() != null)
			{
				((L2Summon) this).getOwner().reviveRequest(((L2Summon) this).getOwner(), null, getObjectId());
			}
		}
		if(isPlayer())
		{
			if(playable != null && playable.isPhoenixBlessed() || isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE) && ((L2PcInstance) this).isInSiege())
			{
				((L2PcInstance) this).reviveRequest((L2PcInstance) this, null, -1);
			}
		}
		try
		{
			if(_fusionSkill != null || _castTimeSkill != null)
			{
				abortCast();
			}

			getKnownList().getKnownCharacters().stream().filter(character -> character._castTimeSkill != null && character._castTimeSkill.getTarget().equals(this)).forEach(character -> {
				if(character._fusionSkill != null || character._castTimeSkill != null)
				{
					character.abortCast();
				}
			});
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception while die()", e);
		}
		return true;
	}

	protected void calculateRewards(L2Character killer)
	{
	}

	/**
	 * Sets HP, MP and CP and revives the L2Character.
	 */
	public void doRevive()
	{
		if(!_isDead)
		{
			return;
		}
		if(_isTeleporting)
		{
			_isPendingRevive = true;
		}
		else
		{
			_isPendingRevive = false;
			_isDead = false;
			boolean restorefull = false;

			if(isPlayable() && ((L2Playable) this).isPhoenixBlessed())
			{
				restorefull = true;
				((L2Playable) this).stopPhoenixBlessing(null);
			}
			if(restorefull)
			{
				_status.setCurrentCp(getCurrentCp()); // this is not confirmed, so just trigger regeneration
				_status.setCurrentHp(getMaxHp()); // confirmed
				_status.setCurrentMp(getMaxMp()); // and also confirmed
			}
			else
			{
				if(Config.RESPAWN_RESTORE_CP > 0 && getCurrentCp() < getMaxCp() * Config.RESPAWN_RESTORE_CP)
				{
					_status.setCurrentCp(getMaxCp() * Config.RESPAWN_RESTORE_CP);
				}
				if(Config.RESPAWN_RESTORE_HP > 0 && getCurrentHp() < getMaxHp() * Config.RESPAWN_RESTORE_HP)
				{
					_status.setCurrentHp(getMaxHp() * Config.RESPAWN_RESTORE_HP);
				}
				if(Config.RESPAWN_RESTORE_MP > 0 && getCurrentMp() < getMaxMp() * Config.RESPAWN_RESTORE_MP)
				{
					_status.setCurrentMp(getMaxMp() * Config.RESPAWN_RESTORE_MP);
				}
			}
			// Start broadcast status
			broadcastPacket(new Revive(this));
			if(getLocationController().getWorldRegion() != null)
			{
				getLocationController().getWorldRegion().onRevive(this);
			}
		}
	}

	/**
	 * Revives the L2Character using skill.
	 *
	 * @param revivePower
	 */
	public void doRevive(double revivePower)
	{
		doRevive();
	}

	/**
	 * @return the L2CharacterAI of the L2Character and if its null create a new one.
	 */
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if(ai == null)
		{
			synchronized(this)
			{
				if(_ai == null)
				{
					_ai = new L2CharacterAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	public void setAI(L2CharacterAI newAI)
	{
		L2CharacterAI oldAI = getAI();
		if(oldAI != null && !oldAI.equals(newAI) && oldAI instanceof L2AttackableAI)
		{
			oldAI.stopAITask();
		}
		_ai = newAI;
	}

	/**
	 * @return {@code true} if the L2Character has a L2CharacterAI.
	 */
	public boolean hasAI()
	{
		return _ai != null;
	}

	/**
	 * @return {@code true} if the L2Character is RaidBoss or his minion.
	 */
	public boolean isRaid()
	{
		return false;
	}

    public boolean isGrandRaid() {
        return false;
    }

	/**
	 * @return {@code true} if the L2Character is minion.
	 */
	public boolean isMinion()
	{
		return false;
	}

	/**
	 * @return {@code true} if the L2Character is minion of RaidBoss.
	 */
	public boolean isRaidMinion()
	{
		return false;
	}

	/**
	 * @return a list of L2Character that attacked.
	 */
	public Set<L2Character> getAttackByList()
	{
		if(_attackByList == null)
		{
			synchronized(this)
			{
				if(_attackByList == null)
				{
					_attackByList = new WeakFastSet<>(true);
				}
			}
		}
		return _attackByList;
	}

	public L2Skill getLastSimultaneousSkillCast()
	{
		return _lastSimultaneousSkillCast;
	}

	public void setLastSimultaneousSkillCast(L2Skill skill)
	{
		_lastSimultaneousSkillCast = skill;
	}

	public L2Skill getLastSkillCast()
	{
		return _lastSkillCast;
	}

	public void setLastSkillCast(L2Skill skill)
	{
		_lastSkillCast = skill;
	}

	public boolean isNoRndWalk()
	{
		return _isNoRndWalk;
	}

	public void setIsNoRndWalk(boolean value)
	{
		_isNoRndWalk = value;
	}

	public boolean isNoAttackingBack()
	{
		return _isNoAttackingBack;
	}

	public void setIsNoAttackingBack(boolean value)
	{
		_isNoAttackingBack = value;
	}

	public boolean isAfraid()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_FEAR);
	}

	/**
	 * @return {@code true} если игрок не может использовать скиллы из-за состояния (стан,слип и т.п.)
	 */
	public boolean isAllSkillsDisabled()
	{
		return _allSkillsDisabled || isStunned() || isSleeping() || isParalyzed() || isFlyUp() || isKnockBacked();
	}

	/**
	 * @param skill скилл, вызвавший эту проверку
	 * @return {@code true} если игрок не может использовать скиллы из-за состояния (стан,слип и т.п.)
	 * Так-же учитываются параметры используемого в данный момент скила (ignoreSkillStun, ignoreSkillParalyze и т.п.)
	 */
	public boolean isAllSkillsDisabled(L2Skill skill)
	{
		return _allSkillsDisabled || isStunned() && !skill.ignoreSkillStun() || isSleeping() || isParalyzed() && !skill.ignoreSkillParalyze() || isFlyUp() || isKnockBacked();
	}

	/**
	 * @return True if the L2Character can't attack (stun, sleep, attackEndTime, fakeDeath, paralyze, attackMute).
	 */
	public boolean isAttackingDisabled()
	{
		return _isFlying || isStunned() || isSleeping() || _attackEndTime > GameTimeController.getInstance().getGameTicks() || isAlikeDead() || isParalyzed() || isPhysicalAttackMuted() || _isNoAttackingBack || _AIdisabled || isFlyUp() || isKnockBacked();
	}

	public Calculator[] getCalculators()
	{
		return _calculators;
	}

	public boolean isConfused()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_CONFUSED);
	}

	/**
	 * @return True if the L2Character is dead or use fake death.
	 */
	public boolean isAlikeDead()
	{
		return _isDead;
	}

	/**
	 * @return True if the L2Character is dead.
	 */
	public boolean isOctavisRaid()
	{
		return _isOctavisRaid;
	}

	public void setIsOctavisRaid(boolean value)
	{
		_isOctavisRaid = value;
	}

    /**
     * @return True if the L2Character is dead.
     */
    public boolean isDead()
    {
        return _isDead;
    }

    public void setIsDead(boolean value)
    {
        _isDead = value;
    }

	public boolean isImmobilized()
	{
		return _isImmobilized;
	}

	public void setIsImmobilized(boolean value)
	{
		_isImmobilized = value;
	}

	public boolean isMuted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_MUTED);
	}

	public boolean isPhysicalMuted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_PSYCHICAL_MUTED);
	}

	public boolean isPhysicalAttackMuted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_PSYCHICAL_ATTACK_MUTED);
	}

	public void setIsCanMove(boolean value)
	{
		_canMove = value;
	}

	public boolean isCanMove()
	{
		return _canMove;
	}

	/**
	 * @return True if the L2Character can't move (stun, root, sleep, overload, paralyzed).
	 */
	public boolean isMovementDisabled()
	{
		// check for isTeleporting to prevent teleport cheating (if appear packet not received)
		return !_canMove || isStunned() || isRooted() || isSleeping() || _isOverloaded || isParalyzed() || _isImmobilized || isAlikeDead() || _isTeleporting || isFlyUp() || isKnockBacked();
	}

	/**
	 * @return True if the L2Character can not be controlled by the player (confused, afraid).
	 */
	public boolean isOutOfControl()
	{
		return isConfused() || isAfraid();
	}

	public boolean isOverloaded()
	{
		return _isOverloaded;
	}

	/**
	 * Set the overloaded status of the L2Character is overloaded (if True, the L2PcInstance can't take more item).
	 *
	 * @param value
	 */
	public void setIsOverloaded(boolean value)
	{
		_isOverloaded = value;
	}

	public boolean isParalyzed()
	{
		return _isParalyzed || isAffected(CharEffectList.EFFECT_FLAG_PARALYZED);
	}

	public void setIsParalyzed(boolean value)
	{
		_isParalyzed = value;
	}

	/* GOD Fly Up*/
	public boolean isFlyUp()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_FLY_UP);
	}

	/* GOD Test Knock Back */
	public boolean isKnockBacked()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_CONFUSED);
	}

	public boolean isPendingRevive()
	{
		return _isDead && _isPendingRevive;
	}

	public void setIsPendingRevive(boolean value)
	{
		_isPendingRevive = value;
	}

	public boolean isDisarmed()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_DISARMED);
	}

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return the L2Summon of the L2Character.
	 */
	public FastList<L2Summon> getPets()
	{
		return _emptyPetData;
	}

	public boolean hasPet()
	{
		return false;
	}

	public L2Summon getItemPet()
	{
		return null;
	}

	public boolean isRooted()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_ROOTED);
	}

	/**
	 * @return True if the L2Character is running.
	 */
	public boolean isRunning()
	{
		return _isRunning;
	}

	public void setIsRunning(boolean value)
	{
		_isRunning = value;
		if(getRunSpeed() != 0)
		{
			broadcastPacket(new ChangeMoveType(this));
			// Шлется после ChangeMoveType скорее всего для клиентов чтобы выравнивать передвижение мобов\нпц
			broadcastPacket(new ExNpcSpeedInfo(this));
		}
		if(isPlayer())
		{
			((L2PcInstance) this).broadcastUserInfo();
		}
		else if(isSummon())
		{
			broadcastStatusUpdate();
		}
		else if(this instanceof L2DecoyInstance)
		{
			broadcastPacket(new CI((L2Decoy) this));
			broadcastStatusUpdate();
		}
		else if(this instanceof L2Npc)
		{
			for(L2PcInstance player : getKnownList().getKnownPlayers().values())
			{
				if(player == null)
				{
					continue;
				}

				if(getRunSpeed() == 0)
				{
					player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
				}
				else
				{
					player.sendPacket(new NpcInfo((L2Npc) this));
				}
			}
		}
	}

	/**
	 * Set the L2Character movement type to run and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public void setRunning()
	{
		if(!_isRunning)
		{
			setIsRunning(true);
		}
	}

	public boolean isSleeping()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_SLEEP);
	}

	public boolean isStunned()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_STUNNED);
	}

	public boolean isBetrayed()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_BETRAYED);
	}

	public boolean isTeleporting()
	{
		return _isTeleporting;
	}

	public void setIsTeleporting(boolean value)
	{
		_isTeleporting = value;
	}

	public void setIsInvul(boolean b)
	{
		_isInvul = b;
	}

	public boolean isInvul()
	{
		return _isInvul || _isTeleporting || isAffected(CharEffectList.EFFECT_FLAG_INVUL);
	}

	public void setIsMortal(boolean b)
	{
		_isMortal = b;
	}

	public boolean isMortal()
	{
		return _isMortal;
	}

	public boolean isLethalable()
	{
		return _lethalable;
	}

	public void setLethalable(boolean val)
	{
		_lethalable = val;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isResurrectionBlocked()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_BLOCK_RESURRECTION);
	}

	public boolean isRecallBlocked()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_BLOCK_RECALL);
	}

	public boolean isSkillsBlocked()
	{
		return isAffected(CharEffectList.EFFECT_FLAG_BLOCK_SKILLS);
	}

	public boolean isFlying()
	{
		return _isFlying;
	}

	public void setIsFlying(boolean mode)
	{
		_isFlying = mode;
	}

    public boolean isClone()
    {
        return _isClone;
    }

    public void setIsClone(boolean mode)
    {
        _isClone = mode;
    }

	public CharStat getStat()
	{
		return _stat;
	}

	// =========================================================

	public void setStat(CharStat value)
	{
		_stat = value;
	}

	// =========================================================
	// Abnormal Effect - NEED TO REMOVE ONCE L2CHARABNORMALEFFECT IS COMPLETE
	// Data Field

	/**
	 * Initializes the CharStat class of the L2Object,
	 * is overwritten in classes that require a different CharStat Type.
	 * <p/>
	 * Removes the need for instanceof checks.
	 */
	public void initCharStat()
	{
		_stat = new CharStat(this);
	}

	public CharStatus getStatus()
	{
		return _status;
	}
	// Method - Public

	public void setStatus(CharStatus value)
	{
		_status = value;
	}

	/**
	 * Initializes the CharStatus class of the L2Object,
	 * is overwritten in classes that require a different CharStatus Type.
	 * <p/>
	 * Removes the need for instanceof checks.
	 */
	public void initCharStatus()
	{
		_status = new CharStatus(this);
	}

	public L2CharTemplate getTemplate()
	{
		return _template;
	}

	/**
	 * Set the template of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns generic and static properties (ex : all Keltir have the same number of HP...).
	 * All of those properties are stored in a different template for each type of L2Character.
	 * Each template is loaded once in the server cache memory (reduce memory use).
	 * When a new instance of L2Character is spawned, server just create a link between the instance and the template
	 * This link is stored in <B>_template</B><BR><BR>
	 * <p/>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2Character</li><BR><BR
	 *
	 * @param template
	 */
	protected void setTemplate(L2CharTemplate template)
	{
		_template = template;
	}

	/**
	 * @return the Title of the L2Character.
	 */
	public String getTitle()
	{
		return _title != null ? _title : "";
	}

	/**
	 * Set the Title of the L2Character.
	 *
	 * @param value
	 */
	public void setTitle(String value)
	{
		if(value == null)
		{
			_title = "";
		}
		else
		{
			_title = value.length() > 21 ? value.substring(0, 20) : value;
		}
	}

	/**
	 * /** Set the L2Character movement type to walk and send Server->Client packet ChangeMoveType to all others L2PcInstance.
	 */
	public void setWalking()
	{
		if(_isRunning)
		{
			setIsRunning(false);
		}
	}

	/**
	 * Launch and add L2Effect (including Stack Group management) to L2Character and update client magic icon.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * <p/>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Add the L2Effect to the L2Character _effects</li>
	 * <li>If this effect doesn't belong to a Stack Group, add its Funcs to the Calculator set of the L2Character (remove the old one if necessary)</li>
	 * <li>If this effect has higher priority in its Stack Group, add its Funcs to the Calculator set of the L2Character (remove previous stacked effect Funcs if necessary)</li>
	 * <li>If this effect has NOT higher priority in its Stack Group, set the effect to Not In Use</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 *
	 * @param newEffect
	 */
	public void addEffect(L2Effect newEffect)
	{
		_effects.queueEffect(newEffect, false);
		updateTargetEffects();
	}

	/**
	 * Stop and remove L2Effect (including Stack Group management) from L2Character and update client magic icon.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * <p/>
	 * Several same effect can't be used on a L2Character at the same time.
	 * Indeed, effects are not stackable and the last cast will replace the previous in progress.
	 * More, some effects belong to the same Stack Group (ex WindWald and Haste Potion).
	 * If 2 effects of a same group are used at the same time on a L2Character, only the more efficient (identified by its priority order) will be preserve.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>If the L2Effect belongs to a not empty Stack Group, replace theses Funcs by next stacked effect Funcs</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR>
	 *
	 * @param effect
	 */
	public void removeEffect(L2Effect effect)
	{
		if(effect != null)
		{
			_effects.queueEffect(effect, true);
			updateTargetEffects();
		}
	}

	/**
	 * Active abnormal effects flags in the binary mask and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 *
	 * @param mask
	 */
	public void startAbnormalEffect(AbnormalEffect mask)
	{
		_abnormalEffects.add(mask.getMask());
		updateAbnormalEffect();
	}

	public void startAbnormalEffect(AbnormalEffect[] masks)
	{
		for(AbnormalEffect abn : masks)
		{
			_abnormalEffects.add(abn.getMask());
		}
		updateAbnormalEffect();
	}

	public void startAbnormalEffect(int mask)
	{
		_abnormalEffects.add(mask);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Confused flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startConfused()
	{
		getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Fake Death flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startFakeDeath()
	{
		if(!isPlayer())
		{
			return;
		}

		((L2PcInstance) this).setIsFakeDeath(true);
		/* Aborts any attacks/casts if fake dead */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH);
		broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_START_FAKEDEATH));
	}

	/**
	 * Active the abnormal effect Fear flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startFear()
	{
		getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startMuted()
	{
		/* Aborts any casts if muted */
		abortCast();
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Psychical_Muted flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startPhysicalMuted()
	{
		getAI().notifyEvent(CtrlEvent.EVT_MUTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Root flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startRooted()
	{
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_ROOTED);
		updateAbnormalEffect();
	}

	/**
	 * Active the abnormal effect Sleep flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet.<BR><BR>
	 */
	public void startSleeping()
	{
		/* Aborts any attacks/casts if sleeped */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_SLEEPING);
		updateAbnormalEffect();
	}

	/**
	 * Launch a Stun Abnormal Effect on the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the success rate of the Stun Abnormal Effect on this L2Character</li>
	 * <li>If Stun succeed, active the abnormal effect Stun flag, notify the L2Character AI and send Server->Client UserInfo/CharInfo packet</li>
	 * <li>If Stun NOT succeed, send a system message Failed to the L2PcInstance attacker</li><BR><BR>
	 */
	public void startStunning()
	{
		/* Aborts any attacks/casts if stunned */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_STUNNED);
		if(!isSummon())
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		updateAbnormalEffect();
	}

	public void startParalyze()
	{
		/* Aborts any attacks/casts if paralyzed */
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_PARALYZED);
	}

	public void startFlyUp()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_FLYUP);
		updateAbnormalEffect();
	}

	public void startKnockDown()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_KNOCK_DOWN);
		updateAbnormalEffect();
	}

	public void startKnockBack()
	{
		abortAttack();
		abortCast();
		stopMove(null);
		getAI().notifyEvent(CtrlEvent.EVT_KNOCK_BACK);
		updateAbnormalEffect();
	}

	/**
	 * Modify the abnormal effect map according to the mask.<BR><BR>
	 *
	 * @param mask
	 */
	public void stopAbnormalEffect(AbnormalEffect mask)
	{
		_abnormalEffects.remove(mask.getMask());
		updateAbnormalEffect();
	}

	public void stopAbnormalEffect(AbnormalEffect[] masks)
	{
		for(AbnormalEffect ab : masks)
		{
			_abnormalEffects.remove(ab.getMask());
		}
		updateAbnormalEffect();
	}

	public void stopAbnormalEffect(int mask)
	{
		_abnormalEffects.remove(mask);
		updateAbnormalEffect();
	}

	/**
	 * Stop all active skills effects in progress on the L2Character.<BR><BR>
	 */
	public void stopAllEffects()
	{
		_effects.stopAllEffects();
	}

	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		_effects.stopAllEffectsExceptThoseThatLastThroughDeath();
	}

	/**
	 * Stop a specified/all Confused abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Confused abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _confused to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param effect
	 */
	public void stopConfused(L2Effect effect)
	{
		if(effect == null)
		{
			stopEffects(L2EffectType.CONFUSION);
		}
		else
		{
			removeEffect(effect);
		}

		if(!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop and remove the L2Effects corresponding to the L2Skill Identifier and update client magic icon.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param skillId the L2Skill Identifier of the L2Effect to remove from _effects
	 */
	public void stopSkillEffects(int skillId)
	{
		_effects.stopSkillEffects(skillId);
	}

	/**
	 * Stop and remove the L2Effects corresponding to the L2SkillType and update client magic icon.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 *
	 * @param skillType The L2SkillType of the L2Effect to remove from _effects
	 * @param negateLvl
	 */
	public void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		_effects.stopSkillEffects(skillType, negateLvl);
	}

	public void stopSkillEffects(L2SkillType skillType)
	{
		_effects.stopSkillEffects(skillType, -1);
	}

	/**
	 * Stop and remove all L2Effect of the selected type (ex : BUFF, DMG_OVER_TIME...) from the L2Character and update client magic icon.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in ConcurrentHashMap(Integer,L2Effect) <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Func added by this effect from the L2Character Calculator (Stop L2Effect)</li>
	 * <li>Remove the L2Effect from _effects of the L2Character</li>
	 * <li>Update active skills in progress icons on player client</li><BR><BR>
	 *
	 * @param type The type of effect to stop ((ex : BUFF, DMG_OVER_TIME...)
	 */
	public void stopEffects(L2EffectType type)
	{
		_effects.stopEffects(type);
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.
	 * Called on any action except movement (attack, cast).
	 */
	public void stopEffectsOnAction()
	{
		_effects.stopEffectsOnAction();
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnDamage" set.
	 * Called on decreasing HP and mana burn.
	 *
	 * @param awake
	 */
	public void stopEffectsOnDamage(boolean awake)
	{
		_effects.stopEffectsOnDamage(awake);
	}

	/**
	 * Останавливает эффекты, которые должны сниматься при атакующих действиях персонажа
	 */
	public void stopEffectsOnAttack()
	{
		_effects.stopEffectsOnAttack();
	}

	/**
	 * @return {@code true} если лист эффектов персонажа содержит эффекты, удаляющиеся при каком либо action
	 * кроме как движения
	 */
	public boolean hasBuffsRemovedOnAction()
	{
		return _effects.hasBuffsRemovedOnAction();
	}

	/**
	 * @return {@code true} если лист эффектов персонажа содержит эффекты, удаляющиеся при атакующих действиях
	 */
	public boolean hasBuffsRemovedOnAttack()
	{
		return _effects.hasBuffsRemovedOnAttack();
	}

	/**
	 * @return {@code true} если лист эффектов персонажа содержит эффекты, удаляющиеся при нанесении ему урона
	 */
	public boolean hasBuffsRemovedOnDamage()
	{
		return _effects.hasBuffsRemovedOnDamage();
	}

	/**
	 * Stop a specified/all Fake Death abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fake Death abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _fake_death to False </li>
	 * <li>Notify the L2Character AI</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopFakeDeath(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.FAKE_DEATH);
		}

		// if this is a player instance, start the grace period for this character (grace from mobs only)!
		if(isPlayer())
		{
			((L2PcInstance) this).setIsFakeDeath(false);
			((L2PcInstance) this).setRecentFakeDeath(true);
		}

		ChangeWaitType revive = new ChangeWaitType(this, ChangeWaitType.WT_STOP_FAKEDEATH);
		broadcastPacket(revive);
		//TODO: Temp hack: players see FD on ppl that are moving: Teleport to someone who uses FD - if he gets up he will fall down again for that client -
		// even tho he is actually standing... Probably bad info in CharInfo packet?
		broadcastPacket(new Revive(this));
	}

	/**
	 * Stop a specified/all Fear abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Fear abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _affraid to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopFear(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.FEAR);
		}

		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Muted abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Muted abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _muted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopMuted(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.MUTE);
		}

		updateAbnormalEffect();
	}

	public void stopPhysicalMuted(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.PHYSICAL_MUTE);
		}

		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Root abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Root abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _rooted to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopRooting(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.ROOT);
		}

		if(!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Sleep abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Sleep abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _sleeping to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopSleeping(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.SLEEP);
		}

		if(!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop a specified/all Stun abnormal L2Effect.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete a specified/all (if effect=null) Stun abnormal L2Effect from L2Character and update client magic icon </li>
	 * <li>Set the abnormal effect flag _stuned to False </li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopStunning(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.STUN);
		}

		if(!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	public void stopParalyze(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.PARALYZE);
		}

		if(!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}

	// Property - Public

	public void stopFlyUp(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.FLY_UP);
		}
		updateAbnormalEffect();
	}

	public void stopKnockDown(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.KNOCK_DOWN);
		}
		updateAbnormalEffect();
	}

	public void stopKnockBack(boolean removeEffects)
	{
		if(removeEffects)
		{
			stopEffects(L2EffectType.KNOCK_BACK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Stop L2Effect: Transformation<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove Transformation Effect</li>
	 * <li>Notify the L2Character AI</li>
	 * <li>Send Server->Client UserInfo/CharInfo packet</li><BR><BR>
	 *
	 * @param removeEffects
	 */
	public void stopTransformation(boolean removeEffects)
	{
		// if this is a player instance, then untransform, also set the transform_id column equal to 0 if not cursed.
		if(isPlayer())
		{
			if(((L2PcInstance) this).getTransformation() != null)
			{
				untransform(removeEffects);
			}
		}
		else if(this instanceof L2MonsterInstance)
		{
			if(((L2MonsterInstance) this).getTransformation() != null)
			{
				untransform(removeEffects);
			}
		}

		if(!isPlayer())
		{
			getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
		updateAbnormalEffect();
	}

	/**
	 * Not Implemented.<BR><BR>
	 * <p/>
	 * <B><U> Overridden in</U> :</B><BR><BR>
	 * <li>L2NPCInstance</li>
	 * <li>L2PcInstance</li>
	 * <li>L2Summon</li>
	 * <li>L2DoorInstance</li><BR><BR>
	 */
	public abstract void updateAbnormalEffect();

	/**
	 * Update active skills in progress (In Use and Not In Use because stacked) icons on client.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress (In Use and Not In Use because stacked) are represented by an icon on the client.<BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method ONLY UPDATE the client of the player and not clients of all players in the party.</B></FONT><BR><BR>
	 */
	public void updateEffectIcons()
	{
		updateEffectIcons(false);
	}

	// =========================================================
	// =========================================================
	// NEED TO ORGANIZE AND MOVE TO PROPER PLACE

	/**
	 * Updates Effect Icons for this character(palyer/summon) and his party if any<BR>
	 * <p/>
	 * Overridden in:<BR>
	 * L2PcInstance<BR>
	 * L2Summon<BR>
	 *
	 * @param partyOnly
	 */
	public void updateEffectIcons(boolean partyOnly)
	{
		// overridden
	}

	//	private int _flyingRunSpeed;
	//	private int _floatingWalkSpeed;
	//	private int _flyingWalkSpeed;
	//	private int _floatingRunSpeed;

	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * In Server->Client packet, each effect is represented by 1 bit of the map (ex : BLEEDING = 0x0001 (bit 1), SLEEP = 0x0080 (bit 8)...).
	 * The map is calculated by applying a BINARY OR operation on each effect.<BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Server Packet : CharInfo, NpcInfo, NpcInfoPoly, UserInfo...</li><BR><BR>
	 *
	 * @return a map of 16 bits (0x0000) containing all abnormal effect in progress for this L2Character.
	 */
	public FastSet<Integer> getAbnormalEffects()
	{
		return _abnormalEffects;
	}

	/**
	 * Return all active skills effects in progress on the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 * The Integer key of _effects is the L2Skill Identifier that has created the effect.<BR><BR>
	 *
	 * @return A table containing all active skills effect in progress on the L2Character
	 */
	public L2Effect[] getAllEffects()
	{
		return _effects.getAllEffects();
	}

	/**
	 * Return L2Effect in progress on the L2Character corresponding to the L2Skill Identifier.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skillId The L2Skill Identifier of the L2Effect to return from the _effects
	 * @return The L2Effect corresponding to the L2Skill Identifier
	 */
	public L2Effect getFirstEffect(int skillId)
	{
		return _effects.getFirstEffect(skillId);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character created by the L2Skill.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param skill The L2Skill whose effect must be returned
	 * @return The first L2Effect created by the L2Skill
	 */
	public L2Effect getFirstEffect(L2Skill skill)
	{
		return _effects.getFirstEffect(skill);
	}

	/**
	 * Return the first L2Effect in progress on the L2Character corresponding to the Effect Type (ex : BUFF, STUN, ROOT...).<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All active skills effects in progress on the L2Character are identified in <B>_effects</B>.
	 *
	 * @param tp The Effect Type of skills whose effect must be returned
	 * @return The first L2Effect corresponding to the Effect Type
	 */
	public L2Effect getFirstEffect(L2EffectType tp)
	{
		return _effects.getFirstEffect(tp);
	}

	/**
	 * Return all L2Effects in progress on the L2Character corresponding to the Effect Type
	 *
	 * @param tp
	 * @return
	 */
	public L2Effect[] getEffects(L2EffectType tp)
	{
		return _effects.getEffects(tp);
	}

	/**
	 * Add a Func to the Calculator set of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 * <p/>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If _calculators is linked to NPC_STD_CALCULATOR, create a copy of NPC_STD_CALCULATOR in _calculators</li>
	 * <li>Add the Func object to _calculators</li><BR><BR>
	 *
	 * @param f The Func object to add to the Calculator corresponding to the state affected
	 */
	public void addStatFunc(Func f)
	{
		if(f == null)
		{
			return;
		}

		synchronized(_calculators)
		{
			// Check if Calculator set is linked to the standard Calculator set of NPC
			if(_calculators == NPC_STD_CALCULATOR)
			{
				// Create a copy of the standard NPC Calculator set
				_calculators = new Calculator[Stats.NUM_STATS];

				for(int i = 0; i < Stats.NUM_STATS; i++)
				{
					if(NPC_STD_CALCULATOR[i] != null)
					{
						_calculators[i] = new Calculator(NPC_STD_CALCULATOR[i]);
					}
				}
			}

			// Select the Calculator of the affected state in the Calculator set
			int stat = f.stat.ordinal();

			if(_calculators[stat] == null)
			{
				_calculators[stat] = new Calculator();
			}

			// Add the Func to the calculator corresponding to the state
			_calculators[stat].addFunc(f);
		}
	}

	/**
	 * Add a list of Funcs to the Calculator set of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Equip an item from inventory</li>
	 * <li> Learn a new passive skill</li>
	 * <li> Use an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public void addStatFuncs(Func[] funcs)
	{
        if (!isPlayer() && getKnownList().getKnownPlayers().isEmpty())
        {
            for (final Func f : funcs)
            {
                addStatFunc(f);
            }
        }
        else
        {
            final List<Stats> modifiedStats = new ArrayList<>();

            for (final Func f : funcs) {
                modifiedStats.add(f.stat);
                addStatFunc(f);
            }
            broadcastModifiedStats(modifiedStats);
        }
	}

	/**
	 * Remove a Func from the Calculator set of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 * <p/>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the Func object from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 *
	 * @param f The Func object to remove from the Calculator corresponding to the state affected
	 */
	public void removeStatFunc(Func f)
	{
		if(f == null)
		{
			return;
		}

		// Select the Calculator of the affected state in the Calculator set
		int stat = f.stat.ordinal();

		synchronized(_calculators)
		{
			if(_calculators[stat] == null)
			{
				return;
			}

			// Remove the Func object from the Calculator
			_calculators[stat].removeFunc(f);

			if(_calculators[stat].size() == 0)
			{
				_calculators[stat] = null;
			}

			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if(this instanceof L2Npc)
			{
				int i = 0;
				for(; i < Stats.NUM_STATS; i++)
				{
					if(!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}

				if(i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
		}
	}

	/**
	 * Remove a list of Funcs from the Calculator set of the L2PcInstance.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...). <BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is ONLY for L2PcInstance</B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param funcs The list of Func objects to add to the Calculator corresponding to the state affected
	 */
	public void removeStatFuncs(Func[] funcs)
	{
        if (!isPlayer() && getKnownList().getKnownPlayers().isEmpty())
        {
            for (Func f : funcs)
            {
                removeStatFunc(f);
            }
        }
        else
        {
            final List<Stats> modifiedStats = new ArrayList<>();
            for (Func f : funcs)
            {
                modifiedStats.add(f.stat);
                removeStatFunc(f);
            }

            broadcastModifiedStats(modifiedStats);
        }
	}

	/**
	 * Remove all Func objects with the selected owner from the Calculator set of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>.
	 * Each Calculator (a calculator per state) own a table of Func object.
	 * A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...).
	 * To reduce cache memory use, L2NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR><BR>
	 * <p/>
	 * That's why, if a L2NPCInstance is under a skill/spell effect that modify one of its state, a copy of the NPC_STD_CALCULATOR
	 * must be create in its _calculators before addind new Func object.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove all Func objects of the selected owner from _calculators</li><BR><BR>
	 * <li>If L2Character is a L2NPCInstance and _calculators is equal to NPC_STD_CALCULATOR,
	 * free cache memory and just create a link on NPC_STD_CALCULATOR in _calculators</li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Unequip an item from inventory</li>
	 * <li> Stop an active skill</li><BR><BR>
	 *
	 * @param owner The Object(Skill, Item...) that has created the effect
	 */
	public void removeStatsOwner(Object owner)
	{

		List<Stats> modifiedStats = null;

		int i = 0;
		synchronized(this)
		{
			for(Calculator calc : _calculators)
			{
				if(calc != null)
				{
					// Delete all Func objects of the selected owner
					if(modifiedStats != null)
					{
						modifiedStats.addAll(calc.removeOwner(owner));
					}
					else
					{
						modifiedStats = calc.removeOwner(owner);
					}

					if(calc.size() == 0)
					{
						_calculators[i] = null;
					}
				}
				i++;
			}

			// If possible, free the memory and just create a link on NPC_STD_CALCULATOR
			if(isNpc())
			{
				i = 0;
				for(; i < Stats.NUM_STATS; i++)
				{
					if(!Calculator.equalsCals(_calculators[i], NPC_STD_CALCULATOR[i]))
					{
						break;
					}
				}

				if(i >= Stats.NUM_STATS)
				{
					_calculators = NPC_STD_CALCULATOR;
				}
			}
            
			broadcastModifiedStats(modifiedStats);
		}
	}

    protected void broadcastModifiedStats(List<Stats> stats)
    {
        if ((stats == null) || stats.isEmpty())
        {
            return;
        }

        if (isPlayer() && !getActingPlayer().isOnline())
        {
            return;
        }

        if (isSummon())
        {
            L2Summon summon = (L2Summon) this;
            if (summon.getOwner() != null)
            {
                summon.updateAndBroadcastStatus(1);
            }
        }
        else
        {
            boolean broadcastFull = true;
            StatusUpdate su = new StatusUpdate(this);
            UI info = null;
            if (isPlayer())
            {
                info = new UI(getActingPlayer(), false);
                info.addComponentType(UserInfoType.SLOTS, UserInfoType.ENCHANTLEVEL);
            }
            for (Stats stat : stats)
            {
                if (info != null)
                {
                    switch (stat)
                    {
                        case RUN_SPEED:
                        {
                            info.addComponentType(UserInfoType.MULTIPLIER);
                            break;
                        }
                        case POWER_ATTACK_SPEED:
                        {
                            info.addComponentType(UserInfoType.MULTIPLIER, UserInfoType.STATS);
                            break;
                        }
                        case POWER_ATTACK:
                        case POWER_DEFENCE:
                        case EVASION_PHYSICAL_RATE:
                        case ACCURACY_PHYSICAL:
                        case PCRITICAL_RATE:
                        case MCRITICAL_RATE:
                        case EVASION_MAGICAL_RATE:
                        case ACCURACY_MAGICAL:
                        case MAGIC_ATTACK:
                        case MAGIC_ATTACK_SPEED:
                        case MAGIC_DEFENCE:
                        {
                            info.addComponentType(UserInfoType.STATS);
                            break;
                        }
                        case MAX_CP:
                        {
                            su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
                            break;
                        }
                        case MAX_HP:
                        {
                            su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
                            break;
                        }
                        case MAX_MP:
                        {
                            su.addAttribute(StatusUpdate.MAX_CP, getMaxMp());
                            break;
                        }
                        case STAT_STR:
                        case STAT_CON:
                        case STAT_DEX:
                        case STAT_INT:
                        case STAT_WIT:
                        case STAT_MEN:
                        {
                            info.addComponentType(UserInfoType.BASE_STATS);
                            break;
                        }
                        case FIRE_RES:
                        case WATER_RES:
                        case WIND_RES:
                        case EARTH_RES:
                        case HOLY_RES:
                        case DARK_RES:
                        {
                            info.addComponentType(UserInfoType.ELEMENTALS);
                            break;
                        }
                        case FIRE_POWER:
                        case WATER_POWER:
                        case WIND_POWER:
                        case EARTH_POWER:
                        case HOLY_POWER:
                        case DARK_POWER:
                        {
                            info.addComponentType(UserInfoType.ATK_ELEMENTAL);
                            break;
                        }
                    }
                }
            }

            if (isPlayer())
            {
                final L2PcInstance player = getActingPlayer();
                player.refreshOverloaded();
                player.refreshExpertisePenalty();
                sendPacket(info);

                if (broadcastFull)
                {
                    player.broadcastPacket(new CI(player));
                }
                else
                {
                    broadcastPacket(su);
                }
                if ((getSummonInstance() != null) && isAffected(CharEffectList.EFFECT_FLAG_BLOCK_SKILLS))
                {
                    getSummonInstance().broadcastStatusUpdate();
                }
            }
            else if (isNpc())
            {
                if (broadcastFull)
                {
                    Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
                    for (L2PcInstance player : plrs)
                    {
                        if ((player == null) || !isVisible())
                        {
                            continue;
                        }
                        if (getRunSpeed() == 0)
                        {
                            player.sendPacket(new ServerObjectInfo((L2Npc) this, player));
                        }
                        else
                        {
                            player.sendPacket(new NpcInfo((L2Npc) this));
                        }
                    }
                }
                broadcastPacket(su);
            }
            broadcastPacket(su);
        }
    }

	/**
	 * @return the orientation of the L2Character.
	 */
	public int getHeading()
	{
		return _heading;
	}

	/**
	 * Set the orientation of the L2Character.<BR><BR>
	 *
	 * @param heading
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}

	public int getXdestination()
	{
		MoveData m = _move;

		if(m != null)
		{
			return m._xDestination;
		}

		return getX();
	}

	/**
	 * @return the Y destination of the L2Character or the Y position if not in movement.
	 */
	public int getYdestination()
	{
		MoveData m = _move;

		if(m != null)
		{
			return m._yDestination;
		}

		return getY();
	}

	/**
	 * @return the Z destination of the L2Character or the Z position if not in movement.
	 */
	public int getZdestination()
	{
		MoveData m = _move;

		if(m != null)
		{
			return m._zDestination;
		}

		return getZ();
	}

	/**
	 * @return True if the L2Character is in combat.
	 */
	public boolean isInCombat()
	{
		return hasAI() && (getAI().getAttackTarget() != null || getAI().isAutoAttacking());
	}

	/**
	 * @return True if the L2Character is moving.
	 */
	public boolean isMoving()
	{
		return _move != null;
	}

	/**
	 * @return True if the L2Character is travelling a calculated path.
	 */
	public boolean isOnGeodataPath()
	{
		MoveData m = _move;
		if(m == null)
		{
			return false;
		}
		if(m.onGeodataPathIndex == -1)
		{
			return false;
		}
		return m.onGeodataPathIndex != m.geoPath.length - 1;
	}

	/**
	 * @return True if the L2Character is casting.
	 */
	public boolean isCastingNow()
	{
		return _isCastingNow || _isDoubleCastingNow;
	}

	public void setIsCastingNow(boolean value)
	{
		_isCastingNow = value;
	}

	public boolean isCastingSimultaneouslyNow()
	{
		return _isCastingSimultaneouslyNow;
	}

	public void setIsCastingSimultaneouslyNow(boolean value)
	{
		_isCastingSimultaneouslyNow = value;
	}

	/**
	 * @return True if the cast of the L2Character can be aborted.
	 */
	public boolean canAbortCast()
	{
		return _castInterruptTime > GameTimeController.getInstance().getGameTicks();
	}

	public int getCastInterruptTime()
	{
		return _castInterruptTime;
	}

	/**
	 * @return True if the L2Character is attacking.
	 */
	public boolean isAttackingNow()
	{
		return _attackEndTime > GameTimeController.getInstance().getGameTicks();
	}

	/**
	 * @return True if the L2Character has aborted its attack.
	 */
	public boolean isAttackAborted()
	{
		return _attacking <= 0;
	}

	/**
	 * Abort the attack of the L2Character and send Server->Client ActionFail packet.<BR><BR>
	 */
	public void abortAttack()
	{
		if(isAttackingNow())
		{
			_attacking = 0;
			sendActionFailed();
		}
	}

	/**
	 * @return body part (paperdoll slot) we are targeting right now
	 */
	public int getAttackingBodyPart()
	{
		return _attacking;
	}

	/**
	 * Abort the cast of the L2Character and send Server->Client MagicSkillCanceled/ActionFail packet.<BR><BR>
	 */
	public void abortCast()
	{
		if(isCastingNow() || _isCastingSimultaneouslyNow)
		{
			Future<?> future = _skillCast;
			// cancels the skill hit scheduled task
			if(future != null)
			{
				future.cancel(true);
				_skillCast = null;
			}
			future = _skillCast2;
			if(future != null)
			{
				future.cancel(true);
				_skillCast2 = null;
			}
			future = _skillDoubleCast;
			if(future != null)
			{
				future.cancel(true);
				_skillDoubleCast = null;
			}

			if(_fusionSkill != null)
			{
				_fusionSkill.onCastAbort();
			}

			if(_castTimeSkill != null)
			{
				_castTimeSkill.onCastAbort();
			}

			L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
			if(mog != null)
			{
				mog.exit();
			}

			if(_allSkillsDisabled)
			{
				enableAllSkills(); // this remains for forced skill use, e.g. scroll of escape
			}
			_isCastingNow = false;
			_isDoubleCastingNow = false;
			_isCastingSimultaneouslyNow = false;
			// safeguard for cannot be interrupt any more
			_castInterruptTime = 0;
			if(isPlayer())
			{
				getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING); // setting back previous intention
			}
			broadcastPacket(new MagicSkillCanceled(getObjectId())); // broadcast packet to stop animations client-side
			sendActionFailed(); // send an "action failed" packet to the caster
			sendPacket(new SetupGauge(SetupGauge.BLUE, 0));
		}
	}

	/**
	 * Update the position of the L2Character during a movement and return True if the movement is finished.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * <p/>
	 * When the movement is started (ex : by MovetoLocation), this method will be called each 0.1 sec to estimate and update the L2Character position on the server.
	 * Note, that the current server position can differe from the current client position even if each movement is straight foward.
	 * That's why, client send regularly a Client->Server ValidatePosition packet to eventually correct the gap on the server.
	 * But, it's always the server position that is used in range calculation.<BR><BR>
	 * <p/>
	 * At the end of the estimated movement time, the L2Character position is automatically set to the destination position even if the movement is not finished.<BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current Z position is obtained FROM THE CLIENT by the Client->Server ValidatePosition Packet.
	 * But x and y positions must be calculated to avoid that players try to modify their movement speed.</B></FONT><BR><BR>
	 *
	 * @param gameTicks Nb of ticks since the server start
	 * @return True if the movement is finished
	 */
	public boolean updatePosition(int gameTicks)
	{
		// Get movement data
		MoveData m = _move;

		if(m == null)
		{
			return true;
		}

		if(!isVisible())
		{
			_move = null;
			return true;
		}

		// Check if this is the first update
		if(m._moveTimestamp == 0)
		{
			m._moveTimestamp = m._moveStartTime;
			m._xAccurate = getX();
			m._yAccurate = getY();
		}

		// Check if the position has already been calculated
		if(m._moveTimestamp == gameTicks)
		{
			return false;
		}

		int xPrev = getX();
		int yPrev = getY();
		int zPrev = getZ(); // the z coordinate may be modified by coordinate synchronizations

		double dx;
		double dy;
		double dz;
		if(Config.COORD_SYNCHRONIZE == 1)
		// the only method that can modify x,y while moving (otherwise _move would/should be set null)
		{
			dx = m._xDestination - xPrev;
			dy = m._yDestination - yPrev;
		}
		else
		// otherwise we need saved temporary values to avoid rounding errors
		{
			dx = m._xDestination - m._xAccurate;
			dy = m._yDestination - m._yAccurate;
		}

		boolean isFloating = _isFlying || isInsideZone(ZONE_WATER);

		// Z coordinate will follow geodata or client values
		if(Config.GEODATA_ENABLED && Config.COORD_SYNCHRONIZE == 2 && !_isFlying && !isInsideZone(ZONE_WATER) && !m.disregardingGeodata && GameTimeController.getInstance().getGameTicks() % 10 == 0 // once a second to reduce possible cpu load
			&& GeoEngine.getInstance().hasGeo(xPrev, yPrev) && !(this instanceof L2BoatInstance) && !(this instanceof L2AirShipInstance) && !(this instanceof L2ShuttleInstance))
		{
			short geoHeight = GeoEngine.getInstance().getSpawnHeight(xPrev, yPrev, zPrev - 30, zPrev + 30);
			dz = m._zDestination - geoHeight;
			// quite a big difference, compare to validatePosition packet
			if(isPlayer() && Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) > 200 && Math.abs(((L2PcInstance) this).getClientZ() - geoHeight) < 1500)
			{
				dz = m._zDestination - zPrev; // allow diff
			}
			else if(isInCombat() && Math.abs(dz) > 200 && dx * dx + dy * dy < 40000) // allow mob to climb up to pcinstance
			{
				dz = m._zDestination - zPrev; // climbing
			}
			else
			{
				zPrev = geoHeight;
			}
		}
		else
		{
			dz = m._zDestination - zPrev;
		}

		double delta = dx * dx + dy * dy;
		delta = delta < 10000 && dz * dz > 2500 // close enough, allows error between client and server geodata if it cannot be avoided
			&& !isFloating ? Math.sqrt(delta) : Math.sqrt(delta + dz * dz);

		double distFraction = Double.MAX_VALUE;
		if(delta > 1)
		{
			double distPassed = getStat().getMoveSpeed() * (gameTicks - m._moveTimestamp) / GameTimeController.TICKS_PER_SECOND;
			distFraction = distPassed / delta;
		}

		if(distFraction > 1) // already there
		// Set the position of the L2Character to the destination
		{
			super.getLocationController().setXYZ(m._xDestination, m._yDestination, m._zDestination);
		}
		else
		{
			m._xAccurate += dx * distFraction;
			m._yAccurate += dy * distFraction;

			// Set the position of the L2Character to estimated after parcial move
			super.getLocationController().setXYZ((int) m._xAccurate, (int) m._yAccurate, zPrev + (int) (dz * distFraction + 0.5));
		}
		revalidateZone(false);

		// Set the timer of last position update to now
		m._moveTimestamp = gameTicks;

		return distFraction > 1;
	}

	public void revalidateZone(boolean force)
	{
		if(getLocationController().getWorldRegion() != null)
		{
			// This function is called too often from movement code
			if(force)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				_zoneValidateCounter--;
				if(_zoneValidateCounter < 0)
				{
					_zoneValidateCounter = 4;
				}
				else
				{
					return;
				}
			}

			getLocationController().getWorldRegion().revalidateZones(this);
		}
	}

	/**
	 * Stop movement of the L2Character (Called by AI Accessor only).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Delete movement data of the L2Character </li>
	 * <li>Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading </li>
	 * <li>Remove the L2Object object from _gmList** of GmListManager </li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding L2WorldRegion L2Characters </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet StopMove/StopRotation </B></FONT><BR><BR>
	 *
	 * @param pos
	 */
	public void stopMove(Location pos)
	{
		stopMove(pos, false);
	}

	public void stopMove(Location pos, boolean updateKnownObjects)
	{
		// Delete movement data of the L2Character
		_move = null;

		//if (getAI() != null)
		//  getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Set the current position (x,y,z), its current L2WorldRegion if necessary and its heading
		// All data are contained in a Location object
		if(pos != null)
		{
			getLocationController().setXYZ(pos);
			_heading = pos.getHeading();
			revalidateZone(true);
		}
		broadcastPacket(new StopMove(this));
		if(Config.MOVE_BASED_KNOWNLIST && updateKnownObjects)
		{
			getKnownList().findObjects();
		}
	}

	/**
	 * @return Returns the showSummonAnimation.
	 */
	public boolean isShowSummonAnimation()
	{
		return _showSummonAnimation;
	}

	/**
	 * @param showSummonAnimation The showSummonAnimation to set.
	 */
	public void setShowSummonAnimation(boolean showSummonAnimation)
	{
		_showSummonAnimation = showSummonAnimation;
	}

	/**
	 * @return the identifier of the L2Object targeted or -1.
	 */
	public int getTargetId()
	{
		if(_target != null)
		{
			return _target.getObjectId();
		}

		return -1;
	}

	/**
	 * @return the L2Object targeted or null.
	 */
	public L2Object getTarget()
	{
		return _target;
	}

	/**
	 * Target a L2Object (add the target to the L2Character _target, _knownObject and L2Character to _KnownObject of the L2Object).<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * The L2Object (including L2Character) targeted is identified in <B>_target</B> of the L2Character<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the _target of L2Character to L2Object </li>
	 * <li>If necessary, add L2Object to _knownObject of the L2Character </li>
	 * <li>If necessary, add L2Character to _KnownObject of the L2Object </li>
	 * <li>If object==null, cancel Attak or Cast </li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Remove the L2PcInstance from the old target _statusListener and add it to the new target if it was a L2Character</li><BR><BR>
	 *
	 * @param object L2object to target
	 */
	public void setTarget(L2Object object)
	{
		if(object != null && !object.isVisible())
		{
			object = null;
		}

		if(object != null && !object.equals(_target))
		{
			getKnownList().addKnownObject(object);
			object.getKnownList().addKnownObject(this);
		}

		_target = object;
	}

	/**
	 * Calculate movement data for a move to location action and add the L2Character to movingObjects of GameTimeController (only called by AI Accessor).<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * At the beginning of the move action, all properties of the movement are stored in the MoveData object called <B>_move</B> of the L2Character.
	 * The position of the start point and of the destination permit to estimated in function of the movement speed the time to achieve the destination.<BR><BR>
	 * All L2Character in movement are identified in <B>movingObjects</B> of GameTimeController that will call the updatePosition method of those L2Character each 0.1s.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get current position of the L2Character </li>
	 * <li>Calculate distance (dx,dy) between current position and destination including offset </li>
	 * <li>Create and Init a MoveData object </li>
	 * <li>Set the L2Character _move object to MoveData object </li>
	 * <li>Add the L2Character to movingObjects of the GameTimeController </li>
	 * <li>Create a task to notify the AI that L2Character arrives at a check point of the movement </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T send Server->Client packet MoveToPawn/CharMoveToLocation </B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> AI : onIntentionMoveTo(Location), onIntentionPickUp(L2Object), onIntentionInteract(L2Object) </li>
	 * <li> FollowTask </li><BR><BR>
	 *
	 * @param x      The X position of the destination
	 * @param y      The Y position of the destination
	 * @param z      The Y position of the destination
	 * @param offset The size of the interaction area of the L2Character targeted
	 */
	protected void moveToLocation(int x, int y, int z, int offset)
	{
		if(isPlayer())
		{
			L2PcInstance activeChar = (L2PcInstance) this;
		}

		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if(speed <= 0 || isMovementDisabled())
		{
			return;
		}

		// Get current position of the L2Character
		int curX = getX();
		int curY = getY();
		int curZ = getZ();

		// Calculate distance (dx,dy) between current position and destination
		// TODO: improve Z axis move/follow support when dx,dy are small compared to dz
		double dx = x - curX;
		double dy = y - curY;
		double dz = z - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);

		// make water move short and use no geodata checks for swimming templates
		// distance in a click can easily be over 3000
		if(Config.GEODATA_ENABLED && isInsideZone(ZONE_WATER) && distance > 700)
		{
			double divider = 700 / distance;
			x = curX + (int) (divider * dx);
			y = curY + (int) (divider * dy);
			z = curZ + (int) (divider * dz);
			dx = x - curX;
			dy = y - curY;
			dz = z - curZ;
			distance = Math.sqrt(dx * dx + dy * dy);
		}

		// Define movement angles needed
		// ^
		// |     X (x,y)
		// |   /
		// |  /distance
		// | /
		// |/ angle
		// X ---------->
		// (curx,cury)

		double cos;
		double sin;

		// Check if a movement offset is defined or no distance to go through
		if(offset > 0 || distance < 1)
		{
			// approximation for moving closer when z coordinates are different
			// TODO: handle Z axis movement better
			offset -= Math.abs(dz / 2);
			if(offset < 5)
			{
				offset = 5;
			}

			// If no distance to go through, the movement is canceled
			if(!(this instanceof L2ShuttleInstance) && (distance < 1 || distance - offset <= 0))
			{
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "already in range, no movement needed.");
				}

				// Notify the AI that the L2Character is arrived at destination
				getAI().notifyEvent(CtrlEvent.EVT_ARRIVED);
				return;
			}
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;

			distance -= offset - 5; // due to rounding error, we have to move a bit closer to be in range

			// Calculate the new destination with offset included
			x = curX + (int) (distance * cos);
			y = curY + (int) (distance * sin);

		}
		else
		{
			// Calculate movement angles needed
			sin = dy / distance;
			cos = dx / distance;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// GEODATA MOVEMENT CHECKS AND PATHFINDING
		m.onGeodataPathIndex = -1; // Initialize not on geodata path
		m.disregardingGeodata = false;

		if(Config.GEODATA_ENABLED && !_isFlying && (!isPlayer() || isPlayer() && !getActingPlayer().getObserverController().isObserving()) // flying templates not checked - even canSeeTarget doesn't work yet
			&& (!isInsideZone(ZONE_WATER) || isInsideZone(ZONE_SIEGE)) // swimming also not checked unless in siege zone - but distance is limited (TODO:|| !geoCheckInUnCheckableWaterZone())
			&& !(this instanceof L2NpcWalkerInstance)) // npc walkers not checked
		{
			double originalDistance = distance;
			int originalX = x;
			int originalY = y;
			int originalZ = z;
			int gtx = originalX - WorldManager.MAP_MIN_X >> 4;
			int gty = originalY - WorldManager.MAP_MIN_Y >> 4;

			// Movement checks:
			// when geodata == 2, for all characters except mobs returning home (could be changed later to teleport if pathfinding fails)
			// when geodata == 1, for l2playableinstance and l2riftinstance only
			if(PathFinding.pathFindingEnabledFor(this) && !(isL2Attackable() && ((L2Attackable) this).isReturningToSpawnPoint()) || isPlayer() || isSummon() && getAI().getIntention() != AI_INTENTION_FOLLOW // assuming intention_follow only when following owner
				|| isAfraid())
			{
				if(isOnGeodataPath())
				{
					try
					{
						if(gtx == _move.geoPathGtx && gty == _move.geoPathGty)
						{
							return;
						}
						else
						{
							_move.onGeodataPathIndex = -1; // Set not on geodata path
						}
					}
					catch(NullPointerException ignored)
					{
					}
				}

				if(curX < WorldManager.MAP_MIN_X || curX > WorldManager.MAP_MAX_X || curY < WorldManager.MAP_MIN_Y || curY > WorldManager.MAP_MAX_Y)
				{
					// Temporary fix for character outside world region errors
					_log.log(Level.WARN, "Character " + getName() + " outside world area, in coordinates x:" + curX + " y:" + curY);
					getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					if(isPlayer())
					{
						getLocationController().delete();
					}
					else if(isSummon())
					{
						return; // preventation when summon get out of world coords, player will not loose him, unsummon handled from pcinstance
					}
					else
					{
						getLocationController().decay();
					}
					return;
				}
				Location destiny = GeoEngine.getInstance().moveCheck(curX, curY, curZ, x, y, z, getInstanceId());
				// location different if destination wasn't reached (or just z coord is different)
				x = destiny.getX();
				y = destiny.getY();
				z = destiny.getZ();
				distance = Math.sqrt((x - curX) * (x - curX) + (y - curY) * (y - curY));
			}

			// Pathfinding checks. Only when geodata setting is 2, the LoS check gives shorter result
			// than the original movement was and the LoS gives a shorter distance than 2000
			// This way of detecting need for pathfinding could be changed.
			if(PathFinding.pathFindingEnabledFor(this) && originalDistance - distance > 100 && distance < 2000 && !isAfraid())
			{
				// Path calculation
				// Overrides previous movement check
				if(isPlayable() || isInCombat() || isMinion() || isAttackable())
				{
					m.geoPath = PathFinding.getInstance().findPath(curX, curY, curZ, originalX, originalY, originalZ, isPlayer(), getInstanceId());

					if(m.geoPath == null || m.geoPath.length < 2) // No path found
					{
						// * Even though there's no path found (remember geonodes aren't perfect),
						// the mob is attacking and right now we set it so that the mob will go
						// after target anyway, is dz is small enough.
						// * With cellpathfinding this approach could be changed but would require taking
						// off the geonodes and some more checks.
						// * Summons will follow their masters no matter what.
						// * Currently minions also must move freely since L2AttackableAI commands
						// them to move along with their leader
						if(isPlayer() || !isPlayable() && !isMinion() && Math.abs(z - curZ) > 140 || isSummon() && !((L2Summon) this).getFollowStatus())
						{
							getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							return;
						}
						else
						{
							m.disregardingGeodata = true;
							x = originalX;
							y = originalY;
							z = originalZ;
							distance = originalDistance;
						}
					}
					else
					{
						m.onGeodataPathIndex = 1; // on second segment
						m.geoPathGtx = gtx;
						m.geoPathGty = gty;

						x = m.geoPath[m.onGeodataPathIndex].getX();
						y = m.geoPath[m.onGeodataPathIndex].getY();
						z = m.geoPath[m.onGeodataPathIndex].getZ();

						dx = x - curX;
						dy = y - curY;
						distance = Math.sqrt(dx * dx + dy * dy);
						sin = dy / distance;
						cos = dx / distance;
					}
				}
			}

			// If no distance to go through, the movement is canceled
			if(distance < 1 && (PathFinding.pathFindingEnabledFor(this) || isPlayable() || isAfraid() || this instanceof L2RiftInvaderInstance))
			{
				if(isSummon())
				{
					((L2Summon) this).setFollowStatus(false);
				}
				getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				return;
			}
		}

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);
		m._xDestination = x;
		m._yDestination = y;
		m._zDestination = z; // this is what was requested from client

		// Calculate and set the heading of the L2Character
		m._heading = 0; // initial value for coordinate sync
		_heading = Util.calculateHeadingFrom(cos, sin);

		m._moveStartTime = GameTimeController.getInstance().getGameTicks();

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if(ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController
	}

	public boolean moveToNextRoutePoint()
	{
		if(!isOnGeodataPath())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		// Get the Move Speed of the L2Charcater
		float speed = getStat().getMoveSpeed();
		if(speed <= 0 || isMovementDisabled())
		{
			// Cancel the move action
			_move = null;
			return false;
		}

		MoveData md = _move;
		if(md == null)
		{
			return false;
		}

		// Create and Init a MoveData object
		MoveData m = new MoveData();

		// Update MoveData object
		m.onGeodataPathIndex = md.onGeodataPathIndex + 1; // next segment
		m.geoPath = md.geoPath;
		m.geoPathGtx = md.geoPathGtx;
		m.geoPathGty = md.geoPathGty;

		m._xDestination = md.geoPath[m.onGeodataPathIndex].getX();
		m._yDestination = md.geoPath[m.onGeodataPathIndex].getY();
		m._zDestination = md.geoPath[m.onGeodataPathIndex].getZ();

		double dx = m._xDestination - getX();
		double dy = m._yDestination - getY();
		double distance = Math.sqrt(dx * dx + dy * dy);
		double sin = dy / distance;
		double cos = dx / distance;

		// Caclulate the Nb of ticks between the current position and the destination
		// One tick added for rounding reasons
		int ticksToMove = 1 + (int) (GameTimeController.TICKS_PER_SECOND * distance / speed);

		// Calculate and set the heading of the L2Character
		int heading = (int) (Math.atan2(-sin, -cos) * 10430.378);
		heading += 32768;
		_heading = heading;
		m._heading = 0; // initial value for coordinate sync

		m._moveStartTime = GameTimeController.getInstance().getGameTicks();

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "time to target:" + ticksToMove);
		}

		// Set the L2Character _move object to MoveData object
		_move = m;

		// Add the L2Character to movingObjects of the GameTimeController
		// The GameTimeController manage objects movement
		GameTimeController.getInstance().registerMovingObject(this);

		// Create a task to notify the AI that L2Character arrives at a check point of the movement
		if(ticksToMove * GameTimeController.MILLIS_IN_TICK > 3000)
		{
			ThreadPoolManager.getInstance().scheduleAi(new NotifyAITask(CtrlEvent.EVT_ARRIVED_REVALIDATE), 2000);
		}

		// the CtrlEvent.EVT_ARRIVED will be sent when the character will actually arrive
		// to destination by GameTimeController

		if(isPlayer() && m.onGeodataPathIndex < m.geoPath.length - 1)
		{
			L2PcInstance player = (L2PcInstance) this;
			int diffX = Math.abs(player.getClientX() - player.getX());
			int diffY = Math.abs(player.getClientY() - player.getY());
			int diffZ = Math.abs(player.getClientZ() - player.getZ());
			if(diffX > 300 || diffY > 300 || diffX + diffY > 400 || diffZ > 60)
			{
				broadcastPacket(new ValidateLocation(this));
			}
		}

		// Send a Server->Client packet CharMoveToLocation to the actor and all L2PcInstance in its _knownPlayers
		broadcastPacket(new MTL(this));
		return true;
	}

	public boolean validateMovementHeading(int heading)
	{
		MoveData m = _move;

		if(m == null)
		{
			return true;
		}

		boolean result = true;
		if(m._heading != heading)
		{
			result = m._heading == 0; // initial value or false
			m._heading = heading;
		}

		return result;
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public double getDistance(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return Math.sqrt(dx * dx + dy * dy);
	}

	/**
	 * Return the distance between the current position of the L2Character and the target (x,y).<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z
	 * @return the plan distance
	 * @deprecated use getPlanDistanceSq(int x, int y, int z)
	 */
	@Deprecated
	public double getDistance(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	/**
	 * @param object L2Object
	 * @return the squared distance between the current position of the L2Character and the given object.
	 */
	public double getDistanceSq(L2Object object)
	{
		return getDistanceSq(object.getX(), object.getY(), object.getZ());
	}

	/**
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @param z Z position of the target
	 * @return the squared distance between the current position of the L2Character and the given x, y, z.
	 */
	public double getDistanceSq(int x, int y, int z)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given object.<BR>
	 * (check only x and y, not z)<BR><BR>
	 *
	 * @param object L2Object
	 * @return the squared plan distance
	 */
	public double getPlanDistanceSq(L2Object object)
	{
		return getPlanDistanceSq(object.getX(), object.getY());
	}

	// called from AIAccessor only

	/**
	 * Return the squared plan distance between the current position of the L2Character and the given x, y, z.<BR>
	 * (check only x and y, not z)<BR><BR>
	 *
	 * @param x X position of the target
	 * @param y Y position of the target
	 * @return the squared plan distance
	 */
	public double getPlanDistanceSq(int x, int y)
	{
		double dx = x - getX();
		double dy = y - getY();

		return dx * dx + dy * dy;
	}

	/**
	 * Check if this object is inside the given radius around the given object. Warning: doesn't cover collision radius!<BR><BR>
	 *
	 * @param object      the target
	 * @param radius      the radius around the target
	 * @param checkZ      should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public boolean isInsideRadius(L2Object object, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(object.getX(), object.getY(), object.getZ(), radius, checkZ, strictCheck);
	}

	/**
	 * Check if this object is inside the given plan radius around the given point. Warning: doesn't cover collision radius!<BR><BR>
	 *
	 * @param x           X position of the target
	 * @param y           Y position of the target
	 * @param radius      the radius around the target
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public boolean isInsideRadius(int x, int y, int radius, boolean strictCheck)
	{
		return isInsideRadius(x, y, 0, radius, false, strictCheck);
	}

	public boolean isInsideRadius(Location loc, int radius, boolean checkZ, boolean strictCheck)
	{
		return isInsideRadius(loc.getX(), loc.getY(), loc.getZ(), radius, checkZ, strictCheck);
	}

	/**
	 * Check if this object is inside the given radius around the given point.<BR><BR>
	 *
	 * @param x           X position of the target
	 * @param y           Y position of the target
	 * @param z           Z position of the target
	 * @param radius      the radius around the target
	 * @param checkZ      should we check Z axis also
	 * @param strictCheck true if (distance < radius), false if (distance <= radius)
	 * @return true is the L2Character is inside the radius.
	 */
	public boolean isInsideRadius(int x, int y, int z, int radius, boolean checkZ, boolean strictCheck)
	{
		double dx = x - getX();
		double dy = y - getY();
		double dz = z - getZ();

		if(strictCheck)
		{
			return checkZ ? dx * dx + dy * dy + dz * dz < radius * radius : dx * dx + dy * dy < radius * radius;
		}
		else
		{
			return checkZ ? dx * dx + dy * dy + dz * dz <= radius * radius : dx * dx + dy * dy <= radius * radius;
		}
	}

	/**
	 * Set _attacking corresponding to Attacking Body part to CHEST.<BR><BR>
	 */
	public void setAttackingBodypart()
	{
		_attacking = Inventory.PAPERDOLL_CHEST;
	}

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return True if arrows are available.
	 */
	protected boolean checkAndEquipArrows()
	{
		return true;
	}

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return True if bolts are available.
	 */
	protected boolean checkAndEquipBolts()
	{
		return true;
	}

	/**
	 * Add Exp and Sp to the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li>
	 * <li> L2PetInstance</li><BR><BR>
	 *
	 * @param addToExp
	 * @param addToSp
	 */
	public void addExpAndSp(long addToExp, int addToSp)
	{
		// Dummy method (overridden by players and pets)
	}

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return the active weapon instance (always equiped in the right hand).
	 */
	public abstract L2ItemInstance getActiveWeaponInstance();

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return the active weapon item (always equiped in the right hand).
	 */
	public abstract L2Weapon getActiveWeaponItem();

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return the secondary weapon instance (always equiped in the left hand).
	 */
	public abstract L2ItemInstance getSecondaryWeaponInstance();

	/**
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance</li><BR><BR>
	 *
	 * @return the secondary {@link L2Item} item (always equiped in the left hand).
	 */
	public abstract L2Item getSecondaryWeaponItem();

	/**
	 * Manage hit process (called by Hit Task).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target   The L2Character targeted
	 * @param damage   Nb of HP to reduce
	 * @param crit     True if hit is critical
	 * @param miss     True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld     True if shield is efficient
	 */
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		// If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL
		// and send a Server->Client packet ActionFail (if attacker is a L2PcInstance)
		if(target == null || isAlikeDead() || this instanceof L2Npc && ((L2Npc) this).isEventMob)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		if(this instanceof L2Npc && target.isAlikeDead() || target._isDead || !getKnownList().knowsObject(target) && !(this instanceof L2DoorInstance))
		{
			//getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);

			sendActionFailed();
			return;
		}

		if(miss)
		{
			// Notify target AI
			if(target.hasAI())
			{
				target.getAI().notifyEvent(CtrlEvent.EVT_EVADED, this);
			}

			// ON_EVADED_HIT
			if(target._chanceSkills != null)
			{
				target._chanceSkills.onEvadedHit(this);
			}
		}

		// Send message about damage/crit or miss
		sendDamageMessage(target, damage, false, crit, miss);

		// If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance
		if(!isAttackAborted())
		{
			// Check Raidboss attack
			// Character will be petrified if attacking a raid that's more
			// than 8 levels lower
			if(target.isRaid() && target.giveRaidCurse() && !Config.RAID_DISABLE_CURSE)
			{
				if(getLevel() > target.getLevel() + 8)
				{
					L2Skill skill = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();

					if(skill != null)
					{
						abortAttack();
						abortCast();
						getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
						skill.getEffects(target, this);
					}
					damage = 0; // prevents messing up drop calculation
				}
			}

			// If L2Character target is a L2PcInstance, send a system message
			if(target instanceof L2PcInstance)
			{
				L2PcInstance enemy = (L2PcInstance) target;
				enemy.getAI().clientStartAutoAttack();
			}

			if(!miss && damage > 0)
			{
				int reflectedDamage = Reflect.getReflectDamage(getActingPlayer(), target, damage);

				// reduce targets HP
				target.reduceCurrentHp(damage, this, null);

				if(reflectedDamage > 0)
				{
					target.sendDamageMessage(this, reflectedDamage, false, false, false);
					reduceCurrentHp(reflectedDamage, target, true, false, null);
				}

				if(!target.isInvul()) // Do not absorb if target invul
				{
					// Absorb HP from the damage inflicted
					double absorbPercent = getStat().calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0, null, null);

					if(absorbPercent > 0 && Rnd.getChance(80))
					{
						double maxCanAbsorb = getMaxRecoverableHp() - getCurrentHp();
						double absorbDamage = absorbPercent / 100.0 * damage / 4; // Делим на 4 чтобы сравнять с оффом

						// Нельзя отвампирить больше чем хп у таргета
						absorbDamage = Math.min(absorbDamage, target.getCurrentHp());
						// Проверяем на перелив хп
						absorbDamage = Math.min(absorbDamage, maxCanAbsorb);

						// Если абсорб больше 0 повышаем хп
						if(absorbDamage > 0)
						{
							setCurrentHp(getCurrentHp() + absorbDamage);
						}
					}

					// Absorb MP from the damage inflicted
					absorbPercent = getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0, null, null);

					if(absorbPercent > 0)
					{
						int maxCanAbsorb = (int) (getMaxRecoverableMp() - getCurrentMp());
						int absorbDamage = (int) (absorbPercent / 100.0 * damage);

						if(absorbDamage > maxCanAbsorb)
						{
							absorbDamage = maxCanAbsorb; // Can't absord more than max hp
						}

						if(absorbDamage > 0)
						{
							setCurrentMp(getCurrentMp() + absorbDamage);
						}
					}
				}

				// Notify AI with EVT_ATTACKED
				if(target.hasAI())
				{
					target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
				}
				getAI().clientStartAutoAttack();
				if(isSummon())
				{
					L2PcInstance owner = ((L2Summon) this).getOwner();
					if(owner != null)
					{
						owner.getAI().clientStartAutoAttack();
					}
				}

				// Если у врага есть петы и включен режим защиты - сагриваем их на себя
				if(target instanceof L2PcInstance)
				{
					if(!target.getPets().isEmpty())
					{
						for(L2Summon summon : target.getPets())
						{
							summon.onOwnerGotAttacked(this);
						}
					}
				}

				// Если цель питомец и у него включен режим защиты то агрим его и других петов
				if(target instanceof L2Summon)
				{
					L2PcInstance owner = target.getActingPlayer();
					if(!owner.getPets().isEmpty())
					{
						for(L2Summon summon : owner.getPets())
						{
							summon.onOwnerGotAttacked(this);
						}
					}
				}

				// Manage attack or cast break of the target (calculating rate, sending message...)
				CancelAttack.calcAtkBreak(target, damage);

				// Maybe launch chance skills on us
				if(_chanceSkills != null)
				{
					_chanceSkills.onHit(target, damage, false, crit);
					// Reflect triggers onHit
					if(reflectedDamage > 0)
					{
						_chanceSkills.onHit(target, reflectedDamage, true, false);
					}
				}

				// Maybe launch chance skills on target
				if(target._chanceSkills != null)
				{
					target._chanceSkills.onHit(this, damage, true, crit);
				}
			}

			// Launch weapon Special ability effect if available
			L2Weapon activeWeapon = getActiveWeaponItem();

			if(activeWeapon != null)
			{
				activeWeapon.getSkillEffects(this, target, crit);
			}
			return;
		}

		if(!isCastingNow() && !_isCastingSimultaneouslyNow)
		{
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
		}
	}

	/**
	 * Break an attack and send Server->Client ActionFail packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakAttack()
	{
		if(isAttackingNow())
		{
			// Abort the attack of the L2Character and send Server->Client ActionFail packet
			abortAttack();

			if(isPlayer())
			{
				sendPacket(SystemMessageId.ATTACK_FAILED);
			}
		}
	}

	/**
	 * Break a cast and send Server->Client ActionFail packet and a System Message to the L2Character.<BR><BR>
	 */
	public void breakCast()
	{
		// damage can only cancel magical & static skills
		if(isCastingNow() && canAbortCast() && _lastSkillCast != null && (_lastSkillCast.isMagic() || _lastSkillCast.isStatic()))
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceled/ActionFail packet.
			abortCast();

			if(isPlayer())
			{
				sendPacket(SystemMessageId.CASTING_INTERRUPTED);
			}
		}
	}

	/*
	 * Прерывает все виды скиллов, маг/физ (статик).
	 */
	public void breakCastAll()
	{
		// damage can only cancel magical & static skills
		if(isCastingNow() && canAbortCast() && _lastSkillCast != null)
		{
			// Abort the cast of the L2Character and send Server->Client MagicSkillCanceled/ActionFail packet.
			abortCast();

			if(isPlayer())
			{
				sendPacket(SystemMessageId.CASTING_INTERRUPTED);
			}
		}
	}

	/**
	 * Reduce the arrow number of the L2Character.
	 * Overridden in L2PcInstance
	 *
	 * @param bolts
	 */
	protected void reduceArrowCount(boolean bolts)
	{
		// default is to do nothing
	}

	/**
	 * Manage Forced attack (shift + select target).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If L2Character or target is in a town area, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFail </li>
	 * <li>If target is confused, send a Server->Client packet ActionFail </li>
	 * <li>If L2Character is a L2ArtefactInstance, send a Server->Client packet ActionFail </li>
	 * <li>Send a Server->Client packet MyTargetSelected to start attack and Notify AI with AI_INTENTION_ATTACK </li><BR><BR>
	 *
	 * @param player The L2PcInstance to attack
	 */
	@Override
	public void onForcedAttack(L2PcInstance player)
	{
		if(isInsidePeaceZone(player))
		{
			// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFail
			player.sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
			player.sendActionFailed();
			return;
		}
		if(player.getOlympiadController().isParticipating() && player.getTarget() != null && player.getTarget() instanceof L2Playable)
		{
			L2PcInstance target;
			target = player.getTarget() instanceof L2Summon ? ((L2Summon) player.getTarget()).getOwner() : (L2PcInstance) player.getTarget();

			if(!player.getOlympiadController().isOpponent(target) || !player.getOlympiadController().isPlayingNow())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFail
				player.sendActionFailed();
				return;
			}
		}
		if(player.getTarget() != null && !player.getTarget().isAttackable() && !player.getAccessLevel().allowPeaceAttack())
		{
			// If target is not attackable, send a Server->Client packet ActionFail
			player.sendActionFailed();
			return;
		}
		if(player.isConfused())
		{
			// If target is confused, send a Server->Client packet ActionFail
			player.sendActionFailed();
			return;
		}
		// GeoData Los Check or dz > 1000
		if(!GeoEngine.getInstance().canSeeTarget(player, this))
		{
			player.sendPacket(SystemMessageId.CANT_SEE_TARGET);
			player.sendActionFailed();
			return;
		}

		if(player.getEventController().isInHandysBlockCheckerEventArena())
		{
			player.sendActionFailed();
			return;
		}

		// Notify AI with AI_INTENTION_ATTACK
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		revalidateZone(true);
	}

	/**
	 * Remove the L2Character from the world when the decay task is launched.<BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 */
	@Override
	public boolean onDecay()
	{
		L2WorldRegion reg = getLocationController().getWorldRegion();

		if(reg != null)
		{
			reg.removeFromZones(this);
		}

		return super.onDecay();
	}

	@Override
	public boolean onDelete()
	{
		_debugger = null;

		if(hasAI())
		{
			getAI().stopAITask();
		}
		return true;
	}

	@Override
	public CharKnownList getKnownList()
	{
		return (CharKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new CharKnownList(this));
	}

	@Override
	public boolean isCharacter()
	{
		return true;
	}

	@Override
	public L2Character getCharacter()
	{
		return this;
	}

	@Override
	public LocationController getLocationController()
	{
		if(_locationController == null)
		{
			_locationController = new LocationController(this);
		}

		return (LocationController) _locationController;
	}

	/**
	 * @param attacker проверяемый персонаж
	 * @return True если персонаж находится в мирной зоне
	 */
	public boolean isInsidePeaceZone(L2PcInstance attacker)
	{
		return isInsidePeaceZone(attacker, this);
	}

	public boolean isInsidePeaceZone(L2PcInstance attacker, L2Object target)
	{
		return !attacker.getAccessLevel().allowPeaceAttack() && isInsidePeaceZone((L2Object) attacker, target);
	}

	public boolean isInsidePeaceZone(L2Object attacker, L2Object target)
	{
		if(target == null)
		{
			return false;
		}
		if(!(target instanceof L2Playable && attacker instanceof L2Playable))
		{
			return false;
		}
		if(InstanceManager.getInstance().getInstance(getInstanceId()).isPvPInstance())
		{
			return false;
		}
		if(attacker instanceof L2Character && target instanceof L2Character)
		{
			return ((L2Character) target).isInsideZone(ZONE_PEACE) || ((L2Character) attacker).isInsideZone(ZONE_PEACE);
		}
		if(attacker instanceof L2Character)
		{
			return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || ((L2Character) attacker).isInsideZone(ZONE_PEACE);
		}
		return TownManager.getTown(target.getX(), target.getY(), target.getZ()) != null || TownManager.getTown(attacker.getX(), attacker.getY(), attacker.getZ()) != null;
	}

	/**
	 * @return {@code true} if this character is inside an active grid.
	 */
	public boolean isInActiveRegion()
	{
		L2WorldRegion region = getLocationController().getWorldRegion();
		return region != null && region.isActive();
	}

	/**
	 * @return {@code true} if the L2Character has a Party in progress.
	 */
	public boolean isInParty()
	{
		return false;
	}

	/**
	 * @return the L2Party object of the L2Character.
	 */
	public L2Party getParty()
	{
		return null;
	}

	/**
	 * @param target
	 * @param weapon
	 * @return the Attack Speed of the L2Character (delay (in milliseconds) before next attack).
	 */
	public int calculateTimeBetweenAttacks(L2Character target, L2Weapon weapon)
	{
		double atkSpd = 0;
		if(weapon != null && !isTransformed())
		{
			switch(weapon.getItemType())
			{
				case BOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1500 * 345 / atkSpd);
				case CROSSBOW:
				case TWOHANDCROSSBOW:
					atkSpd = getStat().getPAtkSpd();
					return (int) (1200 * 345 / atkSpd);
				case DAGGER:
					atkSpd = getStat().getPAtkSpd();
					//atkSpd /= 1.15;
					break;
				default:
					atkSpd = getStat().getPAtkSpd();
			}
		}
		else
		{
			atkSpd = getPAtkSpd();
		}

		return AttackSpeedAndMiss.calcPAtkSpd(this, target, atkSpd);
	}

	public int calculateReuseTime(L2Character target, L2Weapon weapon)
	{
		if(weapon == null || isTransformed())
		{
			return 0;
		}

		int reuse = weapon.getReuseDelay();
		// only bows should continue for now
		if(reuse == 0)
		{
			return 0;
		}

		reuse *= getStat().getWeaponReuseModifier(target);
		double atkSpd = getStat().getPAtkSpd();
		switch(weapon.getItemType())
		{
			case BOW:
                return (int) (reuse * 665 / atkSpd);
            case CROSSBOW:
			case TWOHANDCROSSBOW:
				return (int) (reuse * 646 / atkSpd);
			default:
				return (int) (reuse * 632 / atkSpd);
		}
	}

	/**
	 * @return True if the L2Character use a dual weapon.
	 */
	public boolean isUsingDualWeapon()
	{
		return false;
	}

	/**
	 * Add a skill to the L2Character _skills and its Func objects to the calculator set of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(L2Skill newSkill)
	{
		L2Skill oldSkill = null;

		if(newSkill != null)
		{
			if(this instanceof L2PcInstance)
			{
				HookManager.getInstance().notifyEvent(HookType.ON_SKILL_ADD, _hookContainer, this, newSkill);
			}

			// Replace oldSkill by newSkill or Add the newSkill
			oldSkill = _skills.put(newSkill.getId(), newSkill);

			if(newSkill.getDisplayId() != newSkill.getId())
			{
				_customSkills.put(newSkill.getDisplayId(), new SkillHolder(newSkill.getId(), newSkill.getLevel()));
			}

			// If an old skill has been replaced, remove all its Func objects
			if(oldSkill != null)
			{
				// if skill came with another one, we should delete the other one too.
				if(oldSkill.triggerAnotherSkill())
				{
					removeSkill(oldSkill.getTriggeredId(), true);
				}
				removeStatsOwner(oldSkill);
			}
			// Add Func objects of newSkill to the calculator set of the L2Character
			addStatFuncs(newSkill.getStatFuncs(null, this));

			if(oldSkill != null && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
			if(newSkill.isChance())
			{
				addChanceTrigger(newSkill);
			}

			// Add passive effects if there are any.
			newSkill.getEffectsPassive(this);
		}

		return oldSkill;
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	public L2Skill removeSkill(L2Skill skill)
	{
		if(skill == null)
		{
			return null;
		}

		return removeSkill(skill.getId(), true);
	}

	public L2Skill removeSkill(L2Skill skill, boolean cancelEffect)
	{
		if(skill == null)
		{
			return null;
		}

		// Remove the skill from the L2Character _skills
		return removeSkill(skill.getId(), cancelEffect);
	}

	public L2Skill removeSkill(int skillId)
	{
		return removeSkill(skillId, true);
	}

	public L2Skill removeSkill(int skillId, boolean cancelEffect)
	{
		// Remove the skill from the L2Character _skills
		L2Skill oldSkill = _skills.remove(skillId);
		// Remove all its Func objects from the L2Character calculator set
		if(oldSkill != null)
		{
			if(oldSkill.getDisplayId() != oldSkill.getId())
			{
				_customSkills.remove(Integer.valueOf(oldSkill.getDisplayId()));
			}

			//this is just a fail-safe against buggers and gm dummies...
			if(oldSkill.triggerAnotherSkill() && oldSkill.getTriggeredId() > 0)
			{
				removeSkill(oldSkill.getTriggeredId(), true);
			}

			// does not abort casting of the transformation dispell
			if(oldSkill.getSkillType() != L2SkillType.TRANSFORMDISPEL)
			{
				// Stop casting if this skill is used right now
				if(_lastSkillCast != null && isCastingNow())
				{
					if(oldSkill.getId() == _lastSkillCast.getId())
					{
						abortCast();
					}
				}
				if(_lastSimultaneousSkillCast != null && _isCastingSimultaneouslyNow)
				{
					if(oldSkill.getId() == _lastSimultaneousSkillCast.getId())
					{
						abortCast();
					}
				}
			}

			// Remove passive effects.
			_effects.removePassiveEffects(skillId);

			if(cancelEffect || oldSkill.isToggle())
			{
				// for now, to support transformations, we have to let their
				// effects stay when skill is removed
				L2Effect e = getFirstEffect(oldSkill);
				if(e == null || e.getEffectType() != L2EffectType.TRANSFORMATION)
				{
					removeStatsOwner(oldSkill);
					stopSkillEffects(oldSkill.getId());
				}
			}

			if(oldSkill instanceof L2SkillMount && isPlayer() && ((L2PcInstance) this).isMounted())
			{
				((L2PcInstance) this).dismount();
			}

			if(isPlayer())
			{
				if(oldSkill instanceof L2SkillMount && getActingPlayer().isMounted())
				{
					getActingPlayer().dismount();
				}

				// TODO: Unhardcode it!
				if(oldSkill instanceof L2SkillSummon && oldSkill.getId() == 710 && getActingPlayer().hasSummon())
				{
					getPets().stream().filter(pet -> pet.getNpcId() == 14870).forEach(pet -> pet.unSummon((L2PcInstance) this, false));
				}
				HookManager.getInstance().notifyEvent(HookType.ON_SKILL_REMOVE, _hookContainer, this, oldSkill);
			}

			if(oldSkill.isChance() && _chanceSkills != null)
			{
				removeChanceSkill(oldSkill.getId());
			}
		}

		return oldSkill;
	}

	public void removeChanceSkill(int id)
	{
		if(_chanceSkills == null)
		{
			return;
		}

		Iterator<IChanceSkillTrigger> it = _chanceSkills.keySet().iterator();
		while(it.hasNext())
		{
			IChanceSkillTrigger trigger = it.next();
			if(!(trigger instanceof L2Skill))
			{
			}
			else if(((L2Skill) trigger).getId() == id)
			{
				it.remove();
				//break; // XXX: Is it possible to have two triggers with same skill id?
			}
		}
	}

	public void addChanceTrigger(IChanceSkillTrigger trigger)
	{
		if(_chanceSkills == null)
		{
			synchronized(this)
			{
				if(_chanceSkills == null)
				{
					_chanceSkills = new ChanceSkillList(this);
				}
			}
		}
		_chanceSkills.put(trigger, trigger.getTriggeredChanceCondition());
	}

	public void removeChanceEffect(ChanceSkillTrigger effect)
	{
		if(_chanceSkills == null)
		{
			return;
		}
		_chanceSkills.remove(effect);
	}

	public void onStartChanceEffect(byte element)
	{
		if(_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.onStart(element);
	}

	public void onActionTimeChanceEffect(byte element)
	{
		if(_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.onActionTime(element);
	}

	public void onExitChanceEffect(byte element)
	{
		if(_chanceSkills == null)
		{
			return;
		}

		_chanceSkills.onExit(element);
	}

	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B> the L2Character <BR><BR>
	 * @return all skills own by the L2Character in a table of L2Skill.
	 */
	public Collection<L2Skill> getAllSkills()
	{
		return new ArrayList<>(_skills.values());
	}

	/**
	 * @return the map containing this character skills.
	 */
	public Map<Integer, L2Skill> getSkills()
	{
		return _skills;
	}

	/**
	 * @return all the custom skills (skills with different display Id than skill Id).
	 */
	public Map<Integer, SkillHolder> getCustomSkills()
	{
		return _customSkills;
	}

	public ChanceSkillList getChanceSkills()
	{
		return _chanceSkills;
	}

	/**
	 * Return the level of a skill owned by the L2Character.<BR><BR>
	 *
	 * @param skillId The identifier of the L2Skill whose level must be returned
	 * @return The level of the L2Skill identified by skillId
	 */
	public int getSkillLevel(int skillId)
	{
		L2Skill skill = getKnownSkill(skillId);
		if(skill == null)
		{
			return -1;
		}

		return skill.getLevel();
	}

	/**
	 * @param skillId The identifier of the L2Skill to check the knowledge
	 * @return the skill from the known skill.
	 */
	public L2Skill getKnownSkill(int skillId)
	{
		return _skills.get(skillId);
	}

	/**
	 * Return the number of buffs affecting this L2Character.<BR><BR>
	 *
	 * @return The number of Buffs affecting this L2Character
	 */
	public int getBuffCount()
	{
		return _effects.getBuffCount();
	}

	public int getDanceCount()
	{
		return _effects.getDanceCount();
	}

	/**
	 * Manage the magic skill launching task (MP, HP, Item consummation...) and display the magic skill animation on client.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet MagicSkillLaunched (to display magic skill animation) to all L2PcInstance of L2Charcater _knownPlayers</li>
	 * <li>Consumme MP, HP and Item if necessary</li>
	 * <li>Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance</li>
	 * <li>Launch the magic skill in order to calculate its effects</li>
	 * <li>If the skill type is PDAM, notify the AI of the target with AI_INTENTION_ATTACK</li>
	 * <li>Notify the AI of the L2Character with EVT_FINISH_CASTING</li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : A magic skill casting MUST BE in progress</B></FONT><BR><BR>
	 * @param mut
	 */
	public void onMagicLaunchedTimer(MagicUseTask mut)
	{
		L2Skill skill = mut.skill;
		L2Object[] targets = mut.targets;

		if(skill == null || targets == null)
		{
			abortCast();
			return;
		}

		if(targets.length == 0)
		{
			switch(skill.getTargetType())
			{
				// only AURA-type skills can be cast without target
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_AURA_CORPSE_MOB:
					break;
				default:
					abortCast();
					return;
			}
		}

		// Escaping from under skill's radius and peace zone check. First version, not perfect in AoE skills.
		int escapeRange = 0;
		if(skill.getEffectRange() > escapeRange)
		{
			escapeRange = skill.getEffectRange();
		}
		else if(skill.getCastRange() < 0 && skill.getSkillRadius() > 80)
		{
			escapeRange = skill.getSkillRadius();
		}

		if(targets.length > 0 && escapeRange > 0)
		{
			int _skiprange = 0;
			int _skipgeo = 0;
			int _skippeace = 0;
			List<L2Character> targetList = new FastList<>(targets.length);
			for(L2Object target : targets)
			{
				if(target instanceof L2Character)
				{
					if(!Util.checkIfInRange(escapeRange, this, target, true))
					{
						_skiprange++;
						continue;
					}
					if(skill.getSkillRadius() > 0 && skill.isOffensive() && Config.GEODATA_ENABLED && !GeoEngine.getInstance().canSeeTarget(this, target))
					{
						_skipgeo++;
						continue;
					}
					if(skill.isOffensive() && !skill.isNeutral())
					{
						if(isPlayer())
						{
							if(((L2Character) target).isInsidePeaceZone((L2PcInstance) this))
							{
								_skippeace++;
								continue;
							}
						}
						else
						{
							if(((L2Character) target).isInsidePeaceZone(this, target))
							{
								_skippeace++;
								continue;
							}
						}
					}
					targetList.add((L2Character) target);
				}
			}
			if(targetList.isEmpty())
			{
				if(isPlayer())
				{
					if(_skiprange > 0)
					{
						sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
					}
					else if(_skipgeo > 0)
					{
						sendPacket(SystemMessageId.CANT_SEE_TARGET);
					}
					else if(_skippeace > 0)
					{
						sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
					}
				}
				abortCast();
				return;
			}
			mut.targets = targetList.toArray(new L2Character[targetList.size()]);
		}

		// Ensure that a cast is in progress
		// Check if player is using fake death.
		// Static skills can be used while faking death.
		if(mut.simultaneously && !_isCastingSimultaneouslyNow || !mut.simultaneously && !isCastingNow() || isAlikeDead() && !skill.isStatic())
		{
			// now cancels both, simultaneous and normal
			getAI().notifyEvent(CtrlEvent.EVT_CANCEL);
			return;
		}

		// Get the display identifier of the skill
		int magicId = skill.getDisplayId();

		// Get the level of the skill
		int level = getSkillLevel(skill.getReplaceableSkillId() == -1 ? skill.getId() : skill.getReplaceableSkillId());
		if(level < 1)
		{
			level = 1;
		}

		// Send a Server->Client packet MagicSkillLaunched to the L2Character AND to all L2PcInstance in the _KnownPlayers of the L2Character
		if(!skill.isStatic())
		{
			broadcastPacket(new MagicSkillLaunched(this, magicId, level, targets));
		}

		mut.phase = 2;
		if(mut.hitTime < 400)
		{
			onMagicHitTimer(mut);
		}
		else
		{
			_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, 400);
		}
	}

	/*
      * Runs in the end of skill casting
      */

	public void onMagicHitTimer(MagicUseTask mut)
	{
		L2Skill skill = mut.skill;
		L2Object[] targets = mut.targets;

		if(skill == null || targets == null)
		{
			abortCast();
			return;
		}

		if(_fusionSkill != null || _castTimeSkill != null)
		{
			if(mut.simultaneously)
			{
				_skillCast2 = null;
				_isCastingSimultaneouslyNow = false;
			}
			else if(!mut.doublecasting)
			{
				_skillCast = null;
				_isCastingNow = false;
			}
			else
			{
				_skillDoubleCast = null;
				_isDoubleCastingNow = false;
			}

			if(skill.getSkillType() == L2SkillType.FUSION)
			{
				_fusionSkill.onCastAbort();
			}
			if(skill.getSkillType() == L2SkillType.CASTTIME)
			{
				_castTimeSkill.onCastAbort();
			}
			notifyQuestEventSkillFinished(skill, targets[-1]);
			return;
		}
		L2Effect mog = getFirstEffect(L2EffectType.SIGNET_GROUND);
		if(mog != null)
		{
			if(mut.simultaneously)
			{
				_skillCast2 = null;
				_isCastingSimultaneouslyNow = false;
			}
			else if(!mut.doublecasting)
			{
				_skillCast = null;
				_isCastingNow = false;
			}
			else
			{
				_skillDoubleCast = null;
				_isDoubleCastingNow = false;
			}
			mog.exit();
			notifyQuestEventSkillFinished(skill, targets[0]);
			return;
		}

		try
		{
			// Go through targets table
			for(L2Object tgt : targets)
			{
				if(tgt instanceof L2Playable)
				{
					L2Character target = (L2Character) tgt;

					if(skill.getSkillType() == L2SkillType.BUFF)
					{
						target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
					}

					if(isPlayer() && target instanceof L2Summon)
					{
						((L2Summon) target).updateAndBroadcastStatus(1);
					}
				}
			}

			StatusUpdate su = new StatusUpdate(this);
			boolean isSendStatus = false;

			// Consume MP of the L2Character and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			double mpConsume = getStat().getMpConsume(skill);

			if(mpConsume > 0)
			{
				if(mpConsume > getCurrentMp())
				{
					sendPacket(SystemMessageId.NOT_ENOUGH_MP);
					abortCast();
					return;
				}

				getStatus().reduceMp(mpConsume);
				su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
				isSendStatus = true;
			}

			// Consume HP if necessary and Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
			if(skill.getHpConsume() > 0)
			{
				double consumeHp;

				consumeHp = calcStat(Stats.HP_CONSUME_RATE, skill.getHpConsume(), null, null);
				if(consumeHp + 1 >= getCurrentHp())
				{
					consumeHp = getCurrentHp() - 1.0;
				}

				getStatus().reduceHp(consumeHp, this, true);

				su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
				su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
				isSendStatus = true;
			}

			// Consume CP if necessary and Send the Server->Client packet StatusUpdate with current CP/HP and MP to all other L2PcInstance to inform
			if(skill.getCpConsume() > 0)
			{
				double consumeCp;

				consumeCp = skill.getCpConsume();
				if(consumeCp + 1 >= getCurrentHp())
				{
					consumeCp = getCurrentHp() - 1.0;
				}

				getStatus().reduceCp((int) consumeCp);
				su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
				isSendStatus = true;
			}

			// Send a Server->Client packet StatusUpdate with MP modification to the L2PcInstance
			if(isSendStatus)
			{
				sendPacket(su);
			}

			if(isPlayer())
			{
				int charges = ((L2PcInstance) this).getCharges();
				// check for charges
				if(skill.getMaxCharges() == 0 && charges < skill.getNumCharges())
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
					abortCast();
					return;
				}
				// generate charges if any
				if(skill.getNumCharges() > 0)
				{
					if(skill.getMaxCharges() > 0)
					{
						((L2PcInstance) this).increaseCharges(skill.getNumCharges(), skill.getMaxCharges());
					}
					else if(skill.getNumCharges() > 0)
					{
						((L2PcInstance) this).decreaseCharges(skill.getNumCharges(), false);
					}
				}

				// Consume Souls if necessary
				if(skill.getSoulConsumeCount() > 0 || skill.getMaxSoulConsumeCount() > 0)
				{
					if(!((L2PcInstance) this).decreaseSouls(skill.getSoulConsumeCount() > 0 ? skill.getSoulConsumeCount() : skill.getMaxSoulConsumeCount(), skill))
					{
						abortCast();
						return;
					}
				}
			}

			// On each repeat restore shots before cast
			if(mut.count > 0)
			{
				L2ItemInstance weaponInst = getActiveWeaponInstance();
				if(weaponInst != null)
				{
					if(mut.skill.useSoulShot())
					{
						weaponInst.setChargedSoulshot(mut.shots);
					}
					else if(mut.skill.useSpiritShot())
					{
						weaponInst.setChargedSpiritshot(mut.shots);
						System.out.println("2 setChargedSpiritshot: " + weaponInst.getChargedSpiritshot());
					}
				}
			}

			// Launch the magic skill in order to calculate its effects
			callSkill(mut.skill, mut.targets);
		}
		catch(NullPointerException e)
		{
			_log.log(Level.ERROR, "", e);
		}

		if(mut.hitTime > 0)
		{
			mut.count++;
			if(mut.count < skill.getHitCounts())
			{
				int hitTime = mut.hitTime * skill.getHitTimings()[mut.count] / 100;
				if(mut.simultaneously)
				{
					_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				}
				else if(!mut.doublecasting)
				{
					_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				}
				else
				{
					_skillDoubleCast = ThreadPoolManager.getInstance().scheduleEffect(mut, hitTime);
				}
				return;
			}
		}

		mut.phase = 3;
		if(mut.hitTime == 0 || mut.coolTime == 0)
		{
			onMagicFinalizer(mut);
		}
		else
		{
			if(mut.simultaneously)
			{
				_skillCast2 = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			}
			else if(!mut.doublecasting)
			{
				_skillCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			}
			else
			{
				_skillDoubleCast = ThreadPoolManager.getInstance().scheduleEffect(mut, mut.coolTime);
			}
		}
	}

	/*
	 * Runs after skill hitTime+coolTime
	 */
	public void onMagicFinalizer(MagicUseTask mut)
	{
		if(mut.simultaneously)
		{
			_skillCast2 = null;
			_isCastingSimultaneouslyNow = false;
			return;
		}

		if(mut.doublecasting)
		{
			_skillDoubleCast = null;
			_isDoubleCastingNow = false;
		}
		else
		{
			_skillCast = null;
			_isCastingNow = false;
		}
		_castInterruptTime = 0;

		L2Skill skill = mut.skill;
		L2Object target = mut.targets.length > 0 ? mut.targets[0] : null;

		// Notify the AI of the L2Character with EVT_FINISH_CASTING
		getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING);

		notifyQuestEventSkillFinished(skill, target);
		/*
           * If character is a player, then wipe their current cast state and
           * check if a skill is queued.
           *
           * If there is a queued skill, launch it and wipe the queue.
           */
		if(isPlayer())
		{
			L2PcInstance currPlayer = (L2PcInstance) this;
			SkillDat queuedSkill = currPlayer.getQueuedSkill();

			currPlayer.setCurrentSkill(null, false, false);

			if(queuedSkill != null)
			{
				currPlayer.setQueuedSkill(null, false, false);

				// DON'T USE : Recursive call to useMagic() method
				// currPlayer.useMagic(queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed());
				ThreadPoolManager.getInstance().executeTask(new QueuedMagicUseTask(currPlayer, queuedSkill.getSkill(), queuedSkill.isCtrlPressed(), queuedSkill.isShiftPressed()));
			}
		}

		// Attack target after skill use
		if(skill.nextActionIsAttack() && _target instanceof L2Character && _target != this && target != null && _target == target && target.isAttackable())
		{
			if(getAI() == null || getAI().getNextIntention() == null || getAI().getNextIntention().getCtrlIntention() != CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
			}
		}
		if(skill.isOffensive() && !skill.isNeutral() && skill.getSkillType() != L2SkillType.UNLOCK && skill.getSkillType() != L2SkillType.DELUXE_KEY_UNLOCK)
		{
			getAI().clientStartAutoAttack();
		}
	}

	// Quest event ON_SPELL_FNISHED

	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{

	}

	public L2TIntObjectHashMap<Long> getDisabledSkills()
	{
		return _disabledSkills;
	}

	/**
	 * Enable a skill (remove it from _disabledSkills of the L2Character).<BR><BR>
	 *
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their skillId in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 * @param skill the skill to enable.
	 */
	public void enableSkill(L2Skill skill)
	{
		if(skill == null || _disabledSkills == null)
		{
			return;
		}

		_disabledSkills.remove(Integer.valueOf(skill.getReuseHashCode()));
	}

	/**
	 * Disable this skill id for the duration of the delay in milliseconds.
	 * @param skill
	 * @param delay (seconds * 1000)
	 */
	public void disableSkill(L2Skill skill, long delay)
	{
		if(skill == null)
		{
			return;
		}

		if(_disabledSkills == null)
		{
			_disabledSkills = new L2TIntObjectHashMap<>();
		}

		_disabledSkills.put(skill.getReuseHashCode(), delay > 10 ? System.currentTimeMillis() + delay : Long.MAX_VALUE);
	}

	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills disabled are identified by their reuse hashcodes in <B>_disabledSkills</B> of the L2Character <BR><BR>
	 * @return true if a skill is disabled.
	 */
	public boolean isSkillDisabled(L2Skill skill)
	{
		if(skill == null)
		{
			return true;
		}

		if(isAllSkillsDisabled(skill))
		{
			return true;
		}

		if(_disabledSkills == null)
		{
			return false;
		}

		Long timeStamp = _disabledSkills.get(Integer.valueOf(skill.getReuseHashCode()));
		if(timeStamp == null)
		{
			return false;
		}

		if(timeStamp < System.currentTimeMillis())
		{
			_disabledSkills.remove(Integer.valueOf(skill.getReuseHashCode()));
			return false;
		}

		return true;
	}

	/**
	 * Disable all skills (set _allSkillsDisabled to True).<BR><BR>
	 */
	public void disableAllSkills()
	{
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "all skills disabled");
		}
		_allSkillsDisabled = true;
	}

	/**
	 * Enable all skills (set _allSkillsDisabled to False).<BR><BR>
	 */
	public void enableAllSkills()
	{
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "all skills enabled");
		}
		_allSkillsDisabled = false;
	}

	/**
	 * Launch the magic skill and calculate its effects on each target contained in the targets table.<BR><BR>
	 *
	 * @param skill   The L2Skill to use
	 * @param targets The table of L2Object targets
	 */
	public void callSkill(L2Skill skill, L2Object[] targets)
	{
		try
		{
			// Get the skill handler corresponding to the skill type (PDAM, MDAM, SWEEP...) started in gameserver
			ISkillHandler handler = SkillHandler.getInstance().getHandler(skill.getSkillType());
			L2Weapon activeWeapon = getActiveWeaponItem();

			// Check if the toggle skill effects are already in progress on the L2Character
			if(skill.isToggle() && getFirstEffect(skill.getId()) != null)
			{
				return;
			}

			// Initial checks
			for(L2Object trg : targets)
			{
				if(trg instanceof L2Character)
				{
					// Set some values inside target's instance for later use
					L2Character target = (L2Character) trg;

					// Check Raidboss attack and
					// check buffing chars who attack raidboss. Results in mute.
					L2Character targetsAttackTarget = null;
					L2Character targetsCastTarget = null;
					if(target.hasAI())
					{
						targetsAttackTarget = target.getAI().getAttackTarget();
						targetsCastTarget = target.getAI().getCastTarget();
					}
					if(!Config.RAID_DISABLE_CURSE && (target.isRaid() && target.giveRaidCurse() && getLevel() > target.getLevel() + 8 ||
						!skill.isOffensive() && targetsAttackTarget != null && targetsAttackTarget.isRaid() && targetsAttackTarget.giveRaidCurse() && targetsAttackTarget.getAttackByList().contains(target) // has attacked raid
							&& getLevel() > targetsAttackTarget.getLevel() + 8 ||
						!skill.isOffensive() && targetsCastTarget != null && targetsCastTarget.isRaid() && targetsCastTarget.giveRaidCurse() && targetsCastTarget.getAttackByList().contains(target) // has attacked raid
							&& getLevel() > targetsCastTarget.getLevel() + 8))
					{
						if(skill.isMagic())
						{
							L2Skill tempSkill = SkillTable.FrequentSkill.RAID_CURSE.getSkill();
							if(tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
						}
						else
						{
							L2Skill tempSkill = SkillTable.FrequentSkill.RAID_CURSE2.getSkill();
							if(tempSkill != null)
							{
								abortAttack();
								abortCast();
								getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
								tempSkill.getEffects(target, this);
							}
						}
						return;
					}

					// Check if over-hit is possible
					if(skill.isOverhit())
					{
						if(target.isL2Attackable())
						{
							((L2Attackable) target).overhitEnabled(true);
						}
					}

					// crafting does not trigger any chance skills
					// possibly should be unhardcoded
					switch(skill.getSkillType())
					{
						case COMMON_CRAFT:
						case DWARVEN_CRAFT:
							break;
						default:
							// Launch weapon Special ability skill effect if available
							if(activeWeapon != null && !target._isDead)
							{
								if(activeWeapon.getSkillEffects(this, target, skill).length > 0 && isPlayer())
								{
									sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ACTIVATED).addSkillName(skill));
								}
							}

							// Maybe launch chance skills on us
							if(_chanceSkills != null)
							{
								_chanceSkills.onSkillHit(target, skill, false);
							}
							// Maybe launch chance skills on target
							if(target._chanceSkills != null)
							{
								target._chanceSkills.onSkillHit(this, skill, true);
							}
					}
				}
			}

			// Launch the magic skill and calculate its effects
			if(handler != null)
			{
				handler.useSkill(this, skill, targets);
			}
			else
			{
				skill.useSkill(this, targets);
			}

			L2PcInstance player = getActingPlayer();
			if(player != null)
			{
				for(L2Object target : targets)
				{
					// EVT_ATTACKED and PvPStatus
					if(target instanceof L2Character)
					{
						if(skill.isNeutral())
						{
							// no flags
						}
						else if(skill.isOffensive())
						{
							if(target instanceof L2PcInstance || target instanceof L2Summon || target instanceof L2Trap)
							{
								// Signets are a special case, casted on target_self but don't harm self
								if(skill.getSkillType() != L2SkillType.SIGNET && skill.getSkillType() != L2SkillType.SIGNET_CASTTIME)
								{
									if(target instanceof L2PcInstance)
									{
										((L2PcInstance) target).getAI().clientStartAutoAttack();
									}
									else if(target instanceof L2Summon && ((L2Character) target).hasAI())
									{
										L2PcInstance owner = ((L2Summon) target).getOwner();
										if(owner != null)
										{
											owner.getAI().clientStartAutoAttack();
										}
									}
									// Если цель является петом самого игрока или ловушкой, не ставим игроку пвп-флаг
									if(!(!player.getPets().isEmpty() && target instanceof L2Summon && player.getPets().contains(target) && !(this instanceof L2Trap)))
									{
										player.getPvPFlagController().updateStatus((L2Character) target);
									}
								}
							}
							else if(target.isL2Attackable())
							{
								switch(skill.getId())
								{
									case 51: // Lure
									case 511: // Temptation
										break;
									default:
										// add attacker into list
										((L2Character) target).addAttackerToAttackByList(this);
								}
							}
							// notify target AI about the attack
							if(((L2Character) target).hasAI())
							{
								switch(skill.getSkillType())
								{
									case AGGREDUCE:
									case AGGREDUCE_CHAR:
									case AGGREMOVE:
										break;
									default:
										((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
								}
							}
						}
						else
						{
							if(target instanceof L2PcInstance)
							{
								// Casting non offensive skill on player with pvp flag set or with bad reputation
								if(!(target.equals(this) || target.equals(player)) && (((L2PcInstance) target).getPvPFlagController().isFlagged() || ((L2PcInstance) target).hasBadReputation()))
								{
									player.getPvPFlagController().updateStatus();
								}
							}
							else if(target.isL2Attackable())
							{
								switch(skill.getSkillType())
								{
									case SUMMON:
									case BEAST_FEED:
									case UNLOCK:
									case DELUXE_KEY_UNLOCK:
									case UNLOCK_SPECIAL:
										break;
									default:
										player.getPvPFlagController().updateStatus();
								}
							}
						}
					}
				}

				// Mobs in range 1000 see spell
				getKnownList().getKnownCharactersInRadius(1000).stream().filter(spMob -> spMob instanceof L2Npc).forEach(spMob -> {
					L2Npc npcMob = (L2Npc) spMob;

					if(npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE) != null)
					{
						for(Quest quest : npcMob.getTemplate().getEventQuests(Quest.QuestEventType.ON_SKILL_SEE))
						{
							quest.notifySkillSee(npcMob, player, skill, targets, isSummon());
						}
					}
				});
				HookManager.getInstance().notifyEvent(HookType.ON_SKILL_USE, _hookContainer, player, skill);
			}
			// Notify AI
			if(skill.isOffensive())
			{
				switch(skill.getSkillType())
				{
					case AGGREDUCE:
					case AGGREDUCE_CHAR:
					case AGGREMOVE:
						break;
					default:
						for(L2Object target : targets)
						{
							if(target instanceof L2Character && ((L2Character) target).hasAI())
							{
								// notify target AI about the attack
								((L2Character) target).getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, this);
							}
						}
						break;
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": callSkill() failed.", e);
		}
	}

	/**
	 * @param target
	 * @return True if the L2Character is behind the target and can't be seen.
	 */
	public boolean isInBehindOf(L2Character target)
	{
		if(target == null)
		{
			return false;
		}
		return getTargetPosition(target) == TargetPosition.BACK;
	}

	/**
	 * @return {@code true} если цель находится спиной к персонажу
	 */
	public boolean isBehindTarget()
	{
		if(_target == null)
		{
			return false;
		}
		return getTargetPosition((L2Character) _target) == TargetPosition.BACK;
	}

	/**
	 * @param target инстанс цели
	 * @return {@code true} если цель находится лицом к игроку
	 */
	public boolean isInFrontOf(L2Character target)
	{
		if(target == null)
		{
			return false;
		}
		return getTargetPosition(target) == TargetPosition.FRONT;
	}

	/**
	 * @return {@code true} если цель находится лицом к персонажу
	 */
	public boolean isInFrontOfTarget()
	{
		return isInFrontOf((L2Character) _target);
	}

	/**
	 * @param target
	 * @param maxAngle
	 * @return true if target is in front of L2Character (shield def etc)
	 */
	public boolean isFacing(L2Object target, int maxAngle)
	{
		double angleChar;
		double angleTarget;
		double angleDiff;
		double maxAngleDiff;
		if(target == null)
		{
			return false;
		}
		maxAngleDiff = maxAngle / 2;
		angleTarget = Util.calculateAngleFrom(this, target);
		angleChar = Util.convertHeadingToDegree(_heading);
		angleDiff = angleChar - angleTarget;
		if(angleDiff <= -360 + maxAngleDiff)
		{
			angleDiff += 360;
		}
		if(angleDiff >= 360 - maxAngleDiff)
		{
			angleDiff -= 360;
		}
		return Math.abs(angleDiff) <= maxAngleDiff;
	}

	/**
	 * @return Модификатор уровня персонажа.
	 */
	public double getLevelMod()
	{
		return (getLevel() + 89) / 100.0f;
	}

	public void setSkillCast(Future<?> newSkillCast)
	{
		_skillCast = newSkillCast;
	}

	/**
	 * Sets _isCastingNow to true and _castInterruptTime is calculated from end time (ticks)
	 *
	 * @param newSkillCastEndTick
	 */
	public void forceIsCasting(int newSkillCastEndTick)
	{
		_isCastingNow = true;
		// for interrupt -400 ms
		_castInterruptTime = newSkillCastEndTick - 4;
	}

	public void updatePvPFlag(int value)
	{
		// Overridden in L2PcInstance
	}

	/**
	 * @return a multiplier based on weapon random damage
	 */
	public double getRandomDamageMultiplier()
	{
		L2Weapon activeWeapon = getActiveWeaponItem();
		int random;

		if(activeWeapon != null)
		{
			random = activeWeapon.getRandomDamage();
		}
		else
		{
			random = this instanceof L2PcInstance ? (int) ((L2PcInstance) this).getBaseTemplate().getBaseRandomDamage() : 5 + (int) Math.sqrt(getLevel());
		}

		return 1 + (double) Rnd.get(0 - random, random) / 100;
	}

	public int getAttackEndTime()
	{
		return _attackEndTime;
	}

	/**
	 * Not Implemented.<BR><BR>
	 *
	 * @return
	 */
	public abstract int getLevel();

	// =========================================================
	// Stat - NEED TO REMOVE ONCE L2CHARSTAT IS COMPLETE
	// Property - Public
	public double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		return getStat().calcStat(stat, init, target, skill);
	}
	// =========================================================

	public int getMagicalAccuracy()
	{
		return getStat().getMagicalAccuracy();
	}

	// Property - Public

	public int getPhysicalAccuracy()
	{
		return getStat().getPhysicalAccuracy();
	}

	public float getAttackSpeedMultiplier()
	{
		return getStat().getAttackSpeedMultiplier();
	}

	public int getCON()
	{
		return getStat().getCON();
	}

	public int getDEX()
	{
		return getStat().getDEX();
	}

	public double getCriticalDmg(L2Character target, double init)
	{
		return getStat().getCriticalDmg(target, init);
	}

	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	public int getMagicalEvasionRate(L2Character target)
	{
		return getStat().getMagicalEvasionRate(target);
	}

	public int getPhysicalEvasionRate(L2Character target)
	{
		return getStat().getEvasionRate(target);
	}

	public int getINT()
	{
		return getStat().getINT();
	}

	public int getMagicalAttackRange(L2Skill skill)
	{
		return getStat().getMagicalAttackRange(skill);
	}

	public int getMaxCp()
	{
		return getStat().getMaxCp();
	}

	public int getMaxRecoverableCp()
	{
		return getStat().getMaxRecoverableCp();
	}

	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	public double getMAtkAnimals(L2Character target)
	{
		return getStat().getMAtkAnimals(target);
	}

	public double getMAtkDragons(L2Character target)
	{
		return getStat().getMAtkDragons(target);
	}

	public double getMAtkInsects(L2Character target)
	{
		return getStat().getMAtkInsects(target);
	}

	public double getMAtkMonsters(L2Character target)
	{
		return getStat().getMAtkMonsters(target);
	}

	public double getMAtkPlants(L2Character target)
	{
		return getStat().getMAtkPlants(target);
	}

	public double getMAtkGiants(L2Character target)
	{
		return getStat().getMAtkGiants(target);
	}

	public double getMAtkMagicCreatures(L2Character target)
	{
		return getStat().getMAtkMagicCreatures(target);
	}

	public double getMAtkNpcs(L2Character target)
	{
		return getStat().getMAtkNpcs(target);
	}

	public int getMAtkSpd()
	{
		return getStat().getMAtkSpd();
	}

	public int getMaxMp()
	{
		return getStat().getMaxMp();
	}

	public int getMaxRecoverableMp()
	{
		return getStat().getMaxRecoverableMp();
	}

	public int getMaxHp()
	{
		return getStat().getMaxHp();
	}

	public int getMaxRecoverableHp()
	{
		return getStat().getMaxRecoverableHp();
	}

	public int getMCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getMCriticalHit(target, skill);
	}

	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	public int getMEN()
	{
		return getStat().getMEN();
	}

	public double getMReuseRate(L2Skill skill)
	{
		return getStat().getMReuseRate(skill);
	}

	public float getMovementSpeedMultiplier()
	{
		return getStat().getMovementSpeedMultiplier();
	}

	public int getPAtk(L2Character target)
	{
		return getStat().getPAtk(target);
	}

	public double getPAtkAnimals(L2Character target)
	{
		return getStat().getPAtkAnimals(target);
	}

	public double getPAtkDragons(L2Character target)
	{
		return getStat().getPAtkDragons(target);
	}

	public double getPAtkInsects(L2Character target)
	{
		return getStat().getPAtkInsects(target);
	}

	public double getPAtkMonsters(L2Character target)
	{
		return getStat().getPAtkMonsters(target);
	}

	public double getPAtkPlants(L2Character target)
	{
		return getStat().getPAtkPlants(target);
	}

	public double getPAtkGiants(L2Character target)
	{
		return getStat().getPAtkGiants(target);
	}

	public double getPAtkMagicCreatures(L2Character target)
	{
		return getStat().getPAtkMagicCreatures(target);
	}

	public double getPAtkNpcs(L2Character target)
	{
		return getStat().getPAtkNpcs(target);
	}

	public double getPDefAnimals(L2Character target)
	{
		return getStat().getPDefAnimals(target);
	}

	public double getPDefDragons(L2Character target)
	{
		return getStat().getPDefDragons(target);
	}

	public double getPDefInsects(L2Character target)
	{
		return getStat().getPDefInsects(target);
	}

	public double getPDefMonsters(L2Character target)
	{
		return getStat().getPDefMonsters(target);
	}

	public double getPDefPlants(L2Character target)
	{
		return getStat().getPDefPlants(target);
	}

	public double getPDefGiants(L2Character target)
	{
		return getStat().getPDefGiants(target);
	}

	public double getPDefMagicCreatures(L2Character target)
	{
		return getStat().getPDefMagicCreatures(target);
	}

	public double getPDefNpcs(L2Character target)
	{
		return getStat().getPDefNpcs(target);
	}

	/**
	 * Calculated by applying non-visible HP limit
	 * getMaxHp() = getMaxVisibleHp() * limitHp
	 *
	 * @return max visible HP for display purpose.
	 */
	public int getMaxVisibleHp()
	{
		return getStat().getMaxVisibleHp();
	}

	public int getPAtkSpd()
	{
		return getStat().getPAtkSpd();
	}

	public int getPDef(L2Character target)
	{
		return getStat().getPDef(target);
	}

	public int getPhysicalAttackRange()
	{
		return getStat().getPhysicalAttackRange();
	}

	public int getRunSpeed()
	{
		return getStat().getRunSpeed();
	}

	public int getShldDef()
	{
		return getStat().getShldDef();
	}

	public int getSTR()
	{
		return getStat().getSTR();
	}

	public int getWalkSpeed()
	{
		return getStat().getWalkSpeed();
	}

	public int getWIT()
	{
		return getStat().getWIT();
	}

    public int getLUC()
    {
        return getStat().getLUC();
    }

    public int getCHA()
    {
        return getStat().getCHA();
    }

	public void addStatusListener(L2Character object)
	{
		getStatus().addStatusListener(object);
	}
	// =========================================================

	// =========================================================
	// Status - NEED TO REMOVE ONCE L2CHARTATUS IS COMPLETE
	// Method - Public

	public void reduceCurrentHp(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, true, false, skill);
	}

	public void reduceCurrentHpByDOT(double i, L2Character attacker, L2Skill skill)
	{
		reduceCurrentHp(i, attacker, !skill.isToggle(), true, skill);
	}

	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if(_reduceDamageRate > 0.0f)
		{
			damage -= damage * _reduceDamageRate;
		}

		// Абсорб маны при уроне скиллом
		if(skill != null && !isDOT)
		{
			// Absorb MP from the damage inflicted
			double absorbPercent = attacker.getStat().calcStat(Stats.ABSORB_MANA_DAMAGE_PERCENT, 0, null, null);

			if(absorbPercent > 0)
			{
				int maxCanAbsorb = (int) (attacker.getMaxRecoverableMp() - attacker.getCurrentMp());
				int absorbDamage = (int) (absorbPercent / 100.0 * damage);

				if(absorbDamage > maxCanAbsorb)
				{
					absorbDamage = maxCanAbsorb; // Can't absord more than max hp
				}

				if(absorbDamage > 0)
				{
					attacker.setCurrentMp(attacker.getCurrentMp() + absorbDamage);
				}
			}
		}

		if(Config.CHAMPION_ENABLE && isChampion() && Config.CHAMPION_HP != 0)
		{
			getStatus().reduceHp(damage / Config.CHAMPION_HP, attacker, awake, isDOT, false);
		}
		else
		{
			getStatus().reduceHp(damage, attacker, awake, isDOT, false);
		}
	}

	public void reduceCurrentMp(double i)
	{
		getStatus().reduceMp(i);
	}

	public void removeStatusListener(L2Character object)
	{
		getStatus().removeStatusListener(object);
	}

	protected void stopHpMpRegeneration()
	{
		getStatus().stopHpMpRegeneration();
	}

	public double getCurrentCp()
	{
		return getStatus().getCurrentCp();
	}

	// Property - Public

	public void setCurrentCp(Double newCp)
	{
		setCurrentCp((double) newCp);
	}

	public void setCurrentCp(double newCp)
	{
		getStatus().setCurrentCp(newCp);
	}

	public double getCurrentHp()
	{
		return getStatus().getCurrentHp();
	}

	public void setCurrentHp(double newHp)
	{
		getStatus().setCurrentHp(newHp);
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		getStatus().setCurrentHpMp(newHp, newMp);
	}

	public double getCurrentMp()
	{
		return getStatus().getCurrentMp();
	}

	public void setCurrentMp(Double newMp)
	{
		setCurrentMp((double) newMp);
	}

	public void setCurrentMp(double newMp)
	{
		getStatus().setCurrentMp(newMp);
	}

	/**
	 * @return the max weight that the L2Character can load.
	 */
	public int getMaxLoad()
	{
		if(isPlayer() || isPet())
		{
			// Weight Limit = (CON Modifier*69000) * Skills
			// Source http://l2p.bravehost.com/weightlimit.html (May 2007)
			double baseLoad = Math.floor(BaseStats.CON.calcBonus(this) * 69000 * Config.ALT_WEIGHT_LIMIT);
			return (int) calcStat(Stats.WEIGHT_LIMIT, baseLoad, this, null);
		}
		return 0;
	}

	public int getBonusWeightPenalty()
	{
		if(isPlayer() || isPet())
		{
			return (int) calcStat(Stats.WEIGHT_PENALTY, 1, this, null);
		}
		return 0;
	}

	/**
	 * @return the current weight of the L2Character.
	 */
	public int getCurrentLoad()
	{
		if(isPlayer() || isPet())
		{
			return getInventory().getTotalWeight();
		}
		return 0;
	}

	public boolean isChampion()
	{
		return false;
	}

	/**
	 * Check player max buff count
	 *
	 * @return max buff count
	 */
	public int getMaxBuffCount()
	{
		return Config.BUFFS_MAX_AMOUNT + Math.max(0, getSkillLevel(L2Skill.SKILL_DIVINE_INSPIRATION));
	}

	/**
	 * Send system message about damage.<BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance
	 * <li> L2SummonInstance
	 * <li> L2PetInstance</li><BR><BR>
	 *
	 * @param target
	 * @param damage
	 * @param mcrit
	 * @param pcrit
	 * @param miss
	 */
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
	}

	public FusionSkill getFusionSkill()
	{
		return _fusionSkill;
	}

	public void setFusionSkill(FusionSkill fb)
	{
		_fusionSkill = fb;
	}

	public CastTimeSkill getCastTimeSkill()
	{
		return _castTimeSkill;
	}

	public void setCastTimeSkill(CastTimeSkill ct)
	{
		_castTimeSkill = ct;
	}

	public byte getAttackElement()
	{
		return getStat().getAttackElement();
	}

	public int getAttackElementValue(byte attackAttribute)
	{
		return getStat().getAttackElementValue(attackAttribute);
	}

	public int getDefenseElementValue(byte defenseAttribute)
	{
		return getStat().getDefenseElementValue(defenseAttribute);
	}

	public void startPhysicalAttackMuted()
	{
		abortAttack();
	}

	public void stopPhysicalAttackMuted(L2Effect effect)
	{
		if(effect == null)
		{
			stopEffects(L2EffectType.PHYSICAL_ATTACK_MUTE);
		}
		else
		{
			removeEffect(effect);
		}
	}

	public void disableCoreAI(boolean val)
	{
		_AIdisabled = val;
	}

	public boolean isCoreAIDisabled()
	{
		return _AIdisabled;
	}

	/**
	 * @return true
	 */
	public boolean giveRaidCurse()
	{
		return true;
	}

	/**
	 * Check if target is affected with special buff
	 *
	 * @param flag int
	 * @return boolean
	 * @see CharEffectList#isAffected(int)
	 */
	public boolean isAffected(int flag)
	{
		return _effects.isAffected(flag);
	}

	public void broadcastSocialAction(int id)
	{
		broadcastPacket(new SocialAction(getObjectId(), id));
	}

	public void sendChatMessage(int objectId, ChatType messageType, String charName, String text)
	{
		// default implementation
	}

	/**
	 * Return the period between 2 regenerations task (3s for L2Character, 5 min
	 * for L2DoorInstance).<BR>
	 * <BR>
	 */
	public int getRegeneratePeriod()
	{
		if(this instanceof L2DoorInstance)
		{
			return Variables.HP_REGENERATE_PERIOD * 100; // 5 mins
		}

		return Variables.HP_REGENERATE_PERIOD; // 3s
	}

	// L2jS ADD Custom

	/**
	 * @return {@code true} если персонаж находится в воде, глубина\высота которой больше 200
	 */
	public boolean geoCheckInUnCheckableWaterZone()
	{
		L2WaterZone zone = ZoneManager.getInstance().getZone(this, L2WaterZone.class);
		return ZoneManager.getInstance().getZone(this, L2WaterZone.class) != null && zone.getZone().getHighZ() - zone.getZone().getLowZ() > 200;
	}

	public boolean isDoubleCastingNow()
	{
		return _isDoubleCastingNow;
	}

	public void setIsDoubleCastingNow(boolean value)
	{
		_isDoubleCastingNow = value;
	}

	/**
	 * @return HookContainer текущего игрока
	 */
	public IHookContainer getHookContainer()
	{
		return _hookContainer;
	}

	public void setReduceDamageRate(float value)
	{
		_reduceDamageRate = value;
	}

	/**
	 * @param target проверяемая цель
	 * @return положение цели по отношению к обьекту (спина,бок,лицо)
	 */
	public TargetPosition getTargetPosition(L2Character target)
	{
		int a_dif = Math.abs(_heading - target._heading);
		if(a_dif > 8050 && a_dif < 24150 || a_dif > 40250 && a_dif < 56350)
		{
			return TargetPosition.SIDE;
		}
		else
		{
			return a_dif <= 8050 || a_dif >= 56350 ? TargetPosition.BACK : TargetPosition.FRONT;
		}
	}

	public void updateTargetEffects()
	{
		getKnownList().getKnownCharacters().stream().filter(pl -> pl != null && pl instanceof L2PcInstance && pl._target == this).forEach(pl -> pl.sendPacket(new ExAbnormalStatusUpdateFromTarget(this, pl.isAwakened())));
	}

	public boolean canSeeThroughSilentMove()
	{
		return false;
	}

	/**
	 * Sets the character's spiritshot charge to none, if the skill allows it.
	 * @param skill
	 */
	public void spsUncharge(L2Skill skill)
	{
		if(!skill.isStatic())
		{
			spsUncharge();
		}
	}

	/**
	 * Sets the character's spiritshot charge to none.
	 */
	public void spsUncharge()
	{
		if(isPlayer())
		{
			L2ItemInstance weapon = getActiveWeaponInstance();
			if(weapon != null)
			{
				weapon.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if(isSummon()) // If is not player, check for summon.
		{
			((L2Summon) this).setChargedSpiritShot(L2ItemInstance.CHARGED_NONE);
		}
		else if(isNpc())
		{
			((L2Npc) this)._spiritshotcharged = false;
		}
	}

	/**
	 * Sets the character's soulshot charge to none, if the skill allows it.
	 * @param skill
	 */
	public void ssUncharge(L2Skill skill)
	{
		if(!skill.isStatic())
		{
			ssUncharge();
		}
	}

	/**
	 * Sets the character's soulshot charge to none.
	 */
	public void ssUncharge()
	{
		if(isPlayer())
		{
			L2ItemInstance weapon = getActiveWeaponInstance();
			if(weapon != null)
			{
				weapon.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
			}
		}
		else if(isSummon()) // If is not player, check for summon.
		{
			((L2Summon) this).setChargedSoulShot(L2ItemInstance.CHARGED_NONE);
		}
		else if(isNpc())
		{
			((L2Npc) this)._soulshotcharged = false;
		}
	}

	public boolean isSoulshotCharged(L2Skill skill)
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if(isPlayer() && !skill.isMagic() && weapon != null && weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT)
		{
			return true;
		}
		if(isNpc() && ((L2Npc) this)._soulshotcharged)
		{
			return true;
		}
		return isSummon() && ((L2Summon) this).getChargedSoulShot() == L2ItemInstance.CHARGED_SOULSHOT;
	}

	public boolean isSpiritshotCharged(L2Skill skill)
	{
		L2ItemInstance weapon = getActiveWeaponInstance();
		if(isPlayer() && skill.isMagic() && weapon != null && weapon.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
		{
			return true;
		}
		if(isNpc() && ((L2Npc) this)._spiritshotcharged)
		{
			return true;
		}
		return isSummon() && ((L2Summon) this).getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT;
	}

	public boolean isBlessedSpiritshotCharged(L2Skill skill)
	{
		L2ItemInstance weaponInst = getActiveWeaponInstance();
		if(isPlayer() && skill.isMagic() && weaponInst != null && weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
		{
			return true;
		}
		return isSummon() && ((L2Summon) this).getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT;
	}

	public boolean isAwakened()
	{
		return false;
	}

	/**
	 * Send network packet method.
	 */
	public void sendPacket(L2GameServerPacket mov)
	{
	}

    public void sendPacket(L2GameServerPacket[] mov)
    {
    }

	/**
	 * Sends system message with given @id to object.
	 * @param id Message ID.
	 */
	public void sendPacket(SystemMessageId id)
	{
	}

	/**
	 * Called when action should be failed and client must know it.
	 */
	public void sendActionFailed()
	{
	}

	/**
	 * @return {@code true} if object Npc Walker or Vehicle
	 */
	public boolean isWalker()
	{
		return false;
	}

    public PvPFlagController getPvPFlagController()
    {
        return _pvpFlagController;
    }

    /**
	 * Task launching the function useMagic()
	 */
	private static class QueuedMagicUseTask implements Runnable
	{
		L2PcInstance _currPlayer;
		L2Skill _queuedSkill;
		boolean _isCtrlPressed;
		boolean _isShiftPressed;

		public QueuedMagicUseTask(L2PcInstance currPlayer, L2Skill queuedSkill, boolean isCtrlPressed, boolean isShiftPressed)
		{
			_currPlayer = currPlayer;
			_queuedSkill = queuedSkill;
			_isCtrlPressed = isCtrlPressed;
			_isShiftPressed = isShiftPressed;
		}

		@Override
		public void run()
		{
			try
			{
				_currPlayer.useMagic(_queuedSkill, _isCtrlPressed, _isShiftPressed);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Failed executing QueuedMagicUseTask.", e);
			}
		}
	}

	public static class MoveData
	{
		// when we retrieve x/y/z we use GameTimeControl.getGameTicks()
		// if we are moving, but move timestamp==gameticks, we don't need
		// to recalculate position
		public int _moveStartTime;
		public int _moveTimestamp; // last update
		public int _xDestination;
		public int _yDestination;
		public int _zDestination;
		public double _xAccurate; // otherwise there would be rounding errors
		public double _yAccurate;
		public int _heading;

		public boolean disregardingGeodata;
		public int onGeodataPathIndex;
		public Location[] geoPath;
		public int geoPathGtx;
		public int geoPathGty;
	}

	/**
	 * Task for potion and herb queue
	 */
	private static class UsePotionTask implements Runnable
	{
		private final L2Character _activeChar;
		private final L2Skill _skill;

		UsePotionTask(L2Character activeChar, L2Skill skill)
		{
			_activeChar = activeChar;
			_skill = skill;
		}

		@Override
		public void run()
		{
			try
			{
				_activeChar.doSimultaneousCast(_skill);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Eror in UsePotionTask: ", e);
			}
		}
	}

	/**
	 * Task lauching the function onHitTimer().<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 */
	class HitTask implements Runnable
	{
		L2Character _hitTarget;
		int _damage;
		boolean _crit;
		boolean _miss;
		byte _shld;
		boolean _soulshot;

		public HitTask(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
		{
			_hitTarget = target;
			_damage = damage;
			_crit = crit;
			_shld = shld;
			_miss = miss;
			_soulshot = soulshot;
		}

		@Override
		public void run()
		{
			try
			{
				onHitTimer(_hitTarget, _damage, _crit, _miss, _soulshot, _shld);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Failed executing HitTask.", e);
			}
		}
	}

	/**
	 * Task lauching the magic skill phases
	 */
	class MagicUseTask implements Runnable
	{
		L2Object[] targets;
		L2Skill skill;
		int count;
		int hitTime;
		int coolTime;
		int phase;
		boolean simultaneously;
		int shots;
		boolean doublecasting;

		public MagicUseTask(L2Object[] tgts, L2Skill s, int hit, int coolT, boolean simultaneous, int shot, boolean doublecast)
		{
			targets = tgts;
			skill = s;
			count = 0;
			phase = 1;
			hitTime = hit;
			coolTime = coolT;
			simultaneously = simultaneous;
			shots = shot;
			doublecasting = doublecast;
		}

		@Override
		public void run()
		{
			try
			{
				switch(phase)
				{
					case 1:
						onMagicLaunchedTimer(this);
						break;
					case 2:
						onMagicHitTimer(this);
						break;
					case 3:
						onMagicFinalizer(this);
						break;
					default:
						break;
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Failed executing MagicUseTask.", e);
				if(simultaneously)
				{
					setIsCastingSimultaneouslyNow(false);
				}
				else
				{
					setIsCastingNow(false);
					setIsDoubleCastingNow(false);
				}
			}
		}
	}

	/**
	 * Task of AI notification
	 */
	public class NotifyAITask implements Runnable
	{
		private final CtrlEvent _evt;

		NotifyAITask(CtrlEvent evt)
		{
			_evt = evt;
		}

		@Override
		public void run()
		{
			try
			{
				getAI().notifyEvent(_evt, null);
			}
			catch(Exception e)
			{
				_log.log(Level.WARN, "NotifyAITask failed. " + e.getMessage() + " Actor " + L2Character.this, e);
			}
		}
	}

	/**
	 * Task lauching the magic skill phases
	 */
	class FlyToLocationTask implements Runnable
	{
		private final L2Object _tgt;
		private final L2Character _actor;
		private final L2Skill _skill;

		public FlyToLocationTask(L2Character actor, L2Object target, L2Skill skill)
		{
			_actor = actor;
			_tgt = target;
			_skill = skill;
		}

		@Override
		public void run()
		{
			try
			{
				FlyType _flyType;

				_flyType = FlyType.valueOf(_skill.getFlyType());

				broadcastPacket(new FlyToLocation(_actor, _tgt, _flyType, _skill.getFlySpeed(), _skill.getFlyDelay(), _skill.getFlyAnimationSpeed()));
				setXYZ(_tgt.getX(), _tgt.getY(), _tgt.getZ());
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Failed executing FlyToLocationTask.", e);
			}
		}
	}

	/**
	 * This class permit to the L2Character AI to obtain informations and uses L2Character method
	 */
	public class AIAccessor
	{

		/**
		 * @return the L2Character managed by this Accessor AI.
		 */
		public L2Character getActor()
		{
			return L2Character.this;
		}

		/**
		 * Accessor to L2Character moveToLocation() method with an interaction area.<BR><BR>
		 *
		 * @param x
		 * @param y
		 * @param z
		 * @param offset
		 */
		public void moveTo(int x, int y, int z, int offset)
		{
			moveToLocation(x, y, z, offset);
		}

		/**
		 * Accessor to L2Character moveToLocation() method without interaction area.<BR><BR>
		 *
		 * @param x
		 * @param y
		 * @param z
		 */
		public void moveTo(int x, int y, int z)
		{
			moveToLocation(x, y, z, 0);
		}

		/**
		 * Accessor to L2Character stopMove() method.<BR><BR>
		 *
		 * @param pos
		 */
		public void stopMove(Location pos)
		{
			L2Character.this.stopMove(pos);
		}

		/**
		 * Accessor to L2Character doAttack() method.<BR><BR>
		 *
		 * @param target
		 */
		public void doAttack(L2Character target)
		{
			L2Character.this.doAttack(target);
		}

		/**
		 * Accessor to L2Character doCast() method.<BR><BR>
		 *
		 * @param skill
		 */
		public void doCast(L2Skill skill)
		{
			L2Character.this.doCast(skill);
		}

		/**
		 * Create a NotifyAITask.<BR><BR>
		 *
		 * @param evt
		 * @return
		 */
		public NotifyAITask newNotifyTask(CtrlEvent evt)
		{
			return new NotifyAITask(evt);
		}

		/**
		 * Cancel the AI.<BR><BR>
		 */
		public void detachAI()
		{
			_ai = null;
		}
	}
}