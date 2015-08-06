package dwo.gameserver.model.skills.base;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.hookengine.impl.skills.SkillHookTemplate;
import dwo.gameserver.handler.ITargetTypeHandler;
import dwo.gameserver.handler.TargetHandler;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.*;
import dwo.gameserver.model.actor.instance.*;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.skills.*;
import dwo.gameserver.model.skills.base.conditions.Condition;
import dwo.gameserver.model.skills.base.formulas.calculations.Effects;
import dwo.gameserver.model.skills.base.formulas.calculations.SkillMastery;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.base.proptypes.*;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.BaseStats;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class L2Skill implements IChanceSkillTrigger {
    public static final int SKILL_LUCKY = 194;
    public static final int SKILL_CREATE_COMMON = 1320;
    public static final int SKILL_CREATE_DWARVEN = 172;
    public static final int SKILL_CRYSTALLIZE = 248;
    public static final int SKILL_DIVINE_INSPIRATION = 1405;
    public static final int SKILL_CLAN_LUCK = 390;
    public static final int SKILL_SOUL_MASTERY = 467;
    //public static final int SKILL_RESIDENCE_LUCK = 610;
    public static final int SKILL_NPC_RACE = 4416;
    public static final int SKILL_ONYX_BEAST_TRANSFORMATION = 617;
    public static final int SKILL_ACADEMY_DIADEMA = 21250;
    //public static final int SKILL_SHILEN_BREATH = 14571;
    //conditional values
    //public static final int COND_RUNNING = 0x0001;
    //public static final int COND_WALKING = 0x0002;
    //public static final int COND_SIT = 0x0004;
    public static final int COND_BEHIND = 0x0008;
    public static final int COND_CRIT = 0x0010;
    //public static final int COND_LOWHP = 0x0020;
    //public static final int COND_ROBES = 0x0040;
    //public static final int COND_CHARGES = 0x0080;
    public static final int COND_SHIELD = 0x0100;
    protected static final Logger _log = LogManager.getLogger(L2Skill.class);
    private static final L2Object[] _emptyTargetList = new L2Object[0];
    private static final Func[] _emptyFunctionSet = new Func[0];
    private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
    private static final SkillHookTemplate[] emptyHooks = new SkillHookTemplate[0];
    // these two build the primary key
    private final int _id;
    private final int _level;
    // not needed, just for easier debug
    private final String _name;
    public EffectTemplate[] _effectTemplates;
    // Hide messages like "Effect $s1 was aborted"?
    public boolean _hideMsgStatus;
    protected List<Condition> _preCondition;
    protected List<Condition> _itemPreCondition;
    protected FuncTemplate[] _funcTemplates;
    protected EffectTemplate[] _effectTemplatesSelf;
    protected EffectTemplate[] _effectTemplatesPassive;
    protected ChanceCondition _chanceCondition;
    private L2SkillOpType _operateType;
    private L2MagicType _magic;
    private L2TraitType _traitType;
    private boolean _staticReuse;
    private boolean _staticDamage; // Скил наносит статический демаг, который прописан у него в power
    private int _mpConsume;
    private int _mpInitialConsume;
    private int _hpConsume;
    private int _cpConsume;
    private int _targetConsume;
    private int _targetConsumeId;
    private int _itemConsume;
    private int _itemConsumeId;
    private int _fameConsumeSelf;
    private int _fameConsumeClan;
    private int _castRange;
    private int _effectRange;
    // Abnormal levels for skills and their canceling, e.g. poison vs negate
    private int _abnormalLvl; // e.g. poison or bleed lvl 2
    // Note: see also _effectAbnormalLvl
    private int _negateLvl;   // abnormalLvl is negated with negateLvl
    private int[] _negateId;             // cancels the effect of skill ID
    private L2SkillType[] _negateStats;     // lists the effect types that are canceled
    private Map<String, Byte> _negateAbnormals; // lists the effect abnormal types with order below the presented that are canceled
    private List<L2EffectType> _negateEffects;
    private int _maxNegatedEffects;     // maximum number of effects to negate
    private boolean _stayAfterDeath; // skill should stay after death
    private boolean _stayOnSubclassChange; // эффект скила остается после смены сабкласса
    // kill by damage over time
    private boolean _killByDOT;
    private int _refId;
    // all times in milliseconds
    private int _hitTime;
    private int[] _hitTimings;
    //private int _skillInterruptTime;
    private int _coolTime;
    private int _reuseHashCode;
    private int _reuseDelay;
    private int _buffDuration;
    /**
     * Target type of the skill : SELF, PARTY, CLAN, PET...
     */
    private L2TargetType _targetType;
    private int _feed;
    // base success chance
    private double _power;
    private double _pvpPower;
    private double _pvePower;
    private int _magicLevel;
    private int _lvlBonusRate;
    private boolean _ignoreResists;
    private int _minChance;
    private int _maxChance;
    private int _blowChance;
    /* Для дебафов\дотов которые могут накладываться на нефлагнутых игроков */
    private boolean _isNoFlag;
    private boolean _isNeutral;
    //private String _icon;
    //private String _desc;
    // Effecting area of the skill, in radius.
    // The radius center varies according to the _targetType:
    // "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
    private int _skillRadius;
    private L2SkillType _skillType;
    private L2SkillType _effectType; // additional effect has a type
    private int _effectAbnormalLvl; // abnormal level for the additional effect type, e.g. poison lvl 1
    private int _effectId;
    private int _effectLvl; // normal effect level
    private boolean _nextActionIsAttack;
    private boolean _nextActionIsUseSkill;
    private byte _element;
    private int _elementPower;
    private Stats _stat;
    private BaseStats _saveVs;
    private L2BasicResistType _basicProperty;
    private int _condition;
    private int _conditionValue;
    private boolean _overhit;
    private int _weaponsAllowed;
    private int _armorsAllowed;
    private int _minPledgeClass;
    private boolean _isOffensive;
    private int _maxCharges;
    private int _numCharges;
    private int _maxChargesConsumeCount;
    private int _triggeredId;
    private int _triggeredLevel;
    private int _triggeredLevelUPMax;
    private int _triggeredById;
    private float _absorbDmgPercent;
    private String _chanceType;
    private int _soulMaxConsume;
    private int _soulConsume;
    private int _numSouls;
    private int _expNeeded;
    private int _critChance;
    private float _dependOnTargetBuff;
    private int[] _dependOnTargetEffectId;
    private FastList<Integer> _transformId = new FastList<>();
    private int _transformDuration;
    private int _afterEffectId;
    private int _afterEffectLvl;
    private boolean _isHeroSkill; // If true the skill is a Hero Skill
    private boolean _isGMSkill;    // True if skill is GM skill
    private int _baseCritRate;  // percent of success for skill critical hit (especially for PDAM & BLOW - they're not affected by rCrit values or buffs). Default loads -1 for all other skills but 0 to PDAM & BLOW
    private int _lethalEffect1;     // percent of success for lethal 1st effect (hit cp to 1 or if mob hp to 50%) (only for PDAM skills)
    private int _lethalEffect2;     // percent of success for lethal 2nd effect (hit cp,hp to 1 or if mob hp to 1) (only for PDAM skills)
    private boolean _directHpDmg;  // If true then dmg is being make directly
    private boolean _isTriggeredSkill;      // If true the skill will take activation buff slot instead of a normal buff slot
    private boolean _isMentorSkill;
    private int _aggroPoints;
    // Flying support
    private String _flyType;
    private int _flyRadius;
    private float _flyCourse;
    private int _flySpeed;
    private int _flyDelay;
    private int _flyAnimationSpeed;
    private boolean _isDebuff;
    private String _attribute;
    private boolean _ignoreShield;
    private float _ignorePdefPercent;
    private boolean _ignoreSkillStun;
    private boolean _ignoreSkillParalyze;
    private boolean _isSuicideAttack;
    private boolean _canBeReflected;
    private boolean _canBeDispeled;
    private boolean _isClanSkill;
    private boolean _isSharedSkill;
    private boolean _excludedFromCheck;
    private boolean _simultaneousCast;
    private int _maxTargets;
    private boolean _isStaticHeal;
    private boolean _isForceStorable;
    private boolean _isHerbEffect;
    private boolean _isVitalityItemSkill;
    private boolean _restartableDebuff;
    private boolean _moveStop;
    // Appearance
    private int _faceId;
    private int _hairColorId;
    private int _hairStyleId;
    /**
     * Identifier for a skill that client can't display
     */
    private int _displayId;
    /**
     * Необходима для сохронения актионна при юзе сумон скилов.
     */
    private int _actionId;
    private int[][] _replaceableSkills;
    private SkillHookTemplate[] skillHooks;
    private L2ExtractableSkill _extractableItems;
    private int _npcId;
    private int _replaceableSk = -1;

    private int _giveItemId;
    private int _giveItemCount;

    private int _hashCode;

    private StatsSet set;

    public L2Skill(StatsSet set) {
        this.set = set;
        _id = set.getInteger("skill_id");
        _level = set.getInteger("level");
        _hashCode = Util.generateHashCode(_id, _level);
        String _icon = set.getString("icon", "");
        _castRange = set.getInteger("castRange", -1);
        _hitTime = set.getInteger("hitTime", 0);
        _coolTime = set.getInteger("coolTime", 0);
        _reuseDelay = Config.ENABLE_MODIFY_SKILL_REUSE && Config.SKILL_REUSE_LIST.containsKey(_id) ? Config.SKILL_REUSE_LIST.get(_id) : set.getInteger("reuseDelay", 0);
        _name = set.getString("name", "");
        String _desc = set.getString("desc", "");
        _mpConsume = set.getInteger("mpConsume", 0);
        updateL2Skill(set);
    }

    public static boolean checkForAreaOffensiveSkills(L2Character caster, L2Character target, L2Skill skill, boolean sourceInArena) {
        if (target == null || target.isDead() || target.equals(caster)) {
            return false;
        }

        L2PcInstance player = caster.getActingPlayer();
        L2PcInstance targetPlayer = target.getActingPlayer();
        if (player != null) {
            if (targetPlayer != null) {
                try {
                    if ((targetPlayer != caster || targetPlayer != player) && !target.isSummon() || (targetPlayer == player && target.isSummon() && player.getCurrentSkill() != null && !player.getCurrentSkill().isCtrlPressed())) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (targetPlayer.getObserverController().isObserving()) {
                    return false;
                }

                if (skill._isOffensive && player.getSiegeSide() != PlayerSiegeSide.NONE && player.isInsideZone(L2Character.ZONE_SIEGE) && player.getSiegeSide() == targetPlayer.getSiegeSide() && player.getActiveSiegeId() == targetPlayer.getActiveSiegeId()) {
                    return false;
                }

                if (skill._isOffensive && target.isInsideZone(L2Character.ZONE_PEACE)) {
                    return false;
                }

                if (player.isInParty() && targetPlayer.isInParty()) {
                    // Same party
                    if (player.getParty().getLeaderObjectId() == targetPlayer.getParty().getLeaderObjectId()) {
                        return false;
                    }

                    // Same commandchannel
                    if (player.getParty().getCommandChannel() != null && player.getParty().getCommandChannel().equals(targetPlayer.getParty().getCommandChannel())) {
                        return false;
                    }
                }

                if (!EventManager.checkForEventSkill(player, targetPlayer, skill)) {
                    return false;
                }

                if (!sourceInArena && !(targetPlayer.isInsideZone(L2Character.ZONE_PVP) && !targetPlayer.isInsideZone(L2Character.ZONE_SIEGE))) {
                    if (player.getAllyId() != 0 && player.getAllyId() == targetPlayer.getAllyId() && !(player.isInDuel() && targetPlayer.isInDuel() && player.getDuelId() == targetPlayer.getDuelId())) {
                        return false;
                    }

                    if (player.getClanId() != 0 && player.getClanId() == targetPlayer.getClanId() && !(player.isInDuel() && targetPlayer.isInDuel() && player.getDuelId() == targetPlayer.getDuelId())) {
                        return false;
                    }

                    if (!player.checkPvpSkill(targetPlayer, skill, caster instanceof L2Summon)) {
                        return false;
                    }
                }
            } else {
                // Если цель - гвард, то его надо цеплять только в пвп-зонах и через Ctrl-скилл
                if (!sourceInArena && !(target.isInsideZone(L2Character.ZONE_PVP) && !target.isInsideZone(L2Character.ZONE_SIEGE))) {
                    if (target instanceof L2GuardInstance && player.getCurrentSkill() != null && !player.getCurrentSkill().isCtrlPressed()) {
                        return false;
                    }
                }
            }
        } else {
            // target is mob
            if (targetPlayer == null && target instanceof L2Attackable && caster instanceof L2Attackable) {
                String casterEnemyClan = ((L2Attackable) caster).getEnemyClan();
                if (casterEnemyClan == null || casterEnemyClan.isEmpty()) {
                    return false;
                }

                String targetClan = ((L2Attackable) target).getClan();
                if (targetClan == null || targetClan.isEmpty()) {
                    return false;
                }

                if (!casterEnemyClan.equals(targetClan)) {
                    return false;
                }
            }
        }

        return !(Config.GEODATA_ENABLED && !GeoEngine.getInstance().canSeeTarget(caster, target));

    }

    /**
     * @param caster персонаж, кастующий скилл
     * @param owner  владелец саммона
     * @param radius радиус действия скилла
     * @param isDead добавлять ли в спиок мертвых саммонов
     * @return список саммонов, удовлетворяющих указанным условиям
     */
    public static List<L2Summon> getSummons(L2Character caster, L2PcInstance owner, int radius, boolean isDead) {
        List<L2Summon> summons = owner.getPets();
        return summons.stream().filter(pet -> !addCharacter(caster, pet, radius, isDead)).map(pet -> pet).collect(Collectors.toList());
    }

    public static boolean addCharacter(L2Character caster, L2Character target, int radius, boolean isDead) {
        if (isDead != target.isDead()) {
            return false;
        }

        return !(radius > 0 && !Util.checkIfInRange(radius, caster, target, true));

    }

    public static boolean checkForAreaFriendlySkills(L2PcInstance activeChar, L2Character target, L2Skill skill) {
        if (target == null || target.isDead() || target.equals(activeChar)) {
            return false;
        }

        if (activeChar.getInstanceId() != target.getInstanceId()) {
            return false;
        }

        if (target instanceof L2Playable) {
            L2PcInstance pTarget = target.getActingPlayer();

            if (pTarget.getObserverController().isObserving()) {
                return false;
            }
            if (pTarget.getAppearance().getInvisible()) {
                return false;
            }
            if (!pTarget.isVisible()) {
                return false;
            }
            if (activeChar.getDuelId() != 0) {
                if (activeChar.getDuelId() != pTarget.getDuelId()) {
                    return false;
                }
            } else if (pTarget.getDuelId() != 0) {
                return false;
            }

            if (activeChar.isInSameParty(pTarget) || activeChar.isInSameChannel(pTarget)) {
                return true;
            }

            if (activeChar.getOlympiadController().isOpponent(pTarget)) {
                return false;
            }

            if (activeChar.getSiegeSide() != PlayerSiegeSide.NONE && activeChar.isInsideZone(L2Character.ZONE_SIEGE) && activeChar.getSiegeSide() != pTarget.getSiegeSide() && pTarget.getActiveSiegeId() != activeChar.getActiveSiegeId()) {
                return false;
            }

            if (!EventManager.checkForEventSkill(activeChar, pTarget, skill)) {
                return false;
            }

            if (activeChar.isInSameClan(pTarget) || activeChar.isInSameAlly(pTarget)) {
                return true;
            }

            if (target.isInsideZone(L2Character.ZONE_PEACE)) {
                return true;
            }

            if (pTarget.isAutoAttackable(activeChar) || pTarget.isInsideZone(L2Character.ZONE_PVP) || activeChar.isInSameClanWar(pTarget) || pTarget.getReputation() < 0 || pTarget.getPvPFlagController().isFlagged()) {
                return false;
            }

        } else if (target instanceof L2Npc) {
            // АОЕ скиллы не могут задевать гвардов и френдли мобов в любом случае
            if (target instanceof L2GuardInstance || target instanceof L2FriendlyMobInstance) {
                return false;
            } else {
                L2Npc npc = (L2Npc) target;
                if (!npc.isInsideZone(L2Character.ZONE_PEACE)) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return !(Config.GEODATA_ENABLED && !GeoEngine.getInstance().canSeeTarget(activeChar, target));

    }

    public StatsSet getSet() {
        return set;
    }

    public void updateL2Skill(StatsSet set) {
        _refId = set.getInteger("referenceId", 0);
        _displayId = set.getInteger("displayId", _id);
        _operateType = set.getEnum("operateType", L2SkillOpType.class, L2SkillOpType.OP_ACTIVE);
        _magic = set.getEnum("castMagic", L2MagicType.class, L2MagicType.MAGIC);
        _traitType = set.getEnum("trait", L2TraitType.class, L2TraitType.NONE);
        _staticReuse = set.getBool("staticReuse", false);
        _staticDamage = set.getBool("staticDamage", false);
        _mpInitialConsume = set.getInteger("mpInitialConsume", 0);
        _hpConsume = set.getInteger("hpConsume", 0);
        _cpConsume = set.getInteger("cpConsume", 0);

        _targetConsume = set.getInteger("targetConsumeCount", 0);
        _targetConsumeId = set.getInteger("targetConsumeId", 0);
        _itemConsume = set.getInteger("itemConsumeCount", 0);
        _itemConsumeId = set.getInteger("itemConsumeId", 0);
        _fameConsumeSelf = set.getInteger("fameConsumeSelfCount", 0);
        _fameConsumeClan = set.getInteger("fameConsumeClanCount", 0);
        _giveItemCount = set.getInteger("giveItemCount", 0);
        _giveItemId = set.getInteger("giveItemId", 0);
        _afterEffectId = set.getInteger("afterEffectId", 0);
        _afterEffectLvl = set.getInteger("afterEffectLvl", 1);
        _hideMsgStatus = set.getBool("msgStatusHidden", false);
        _moveStop = set.getBool("moveStop", true);

        _effectRange = set.getInteger("effectRange", -1);

        _abnormalLvl = set.getInteger("abnormalLvl", -1);
        _effectAbnormalLvl = set.getInteger("effectAbnormalLvl", -1); // support for a separate effect abnormal lvl, e.g. poison inside a different skill
        _negateLvl = set.getInteger("negateLvl", -1);

        _attribute = set.getString("attribute", "");

        String list = set.getString("replaceableSkills", null);
        if (list != null) {
            try {
                String[] entities = list.split(";");
                _replaceableSkills = new int[entities.length][];
                int index = 0;
                for (String replacebleEntity : entities) {
                    String[] parts = replacebleEntity.split(":");
                    String[] effectList = parts[0].split(",");
                    int replaceSkill = Integer.parseInt(parts[1]);
                    _replaceableSkills[index] = new int[effectList.length + 1];
                    _replaceableSkills[index][0] = replaceSkill;
                    int counter = 0;
                    for (String effect : effectList) {
                        _replaceableSkills[index][++counter] = Integer.parseInt(effect);
                    }
                    ++index;
                }
            } catch (Exception e) {
                _log.log(Level.ERROR, "Eror replaceableSkills: SkillId: " + _id + ' ' + e);
            }
        }

        String str = set.getString("negateStats", "");
        if (str.isEmpty()) {
            _negateStats = new L2SkillType[0];
        } else {
            String[] stats = str.split(" ");
            L2SkillType[] array = new L2SkillType[stats.length];

            for (int i = 0; i < stats.length; i++) {
                L2SkillType type = null;
                try {
                    type = Enum.valueOf(L2SkillType.class, stats[i]);
                } catch (Exception e) {
                    throw new IllegalArgumentException("SkillId: " + _id + "Enum value of type " + L2SkillType.class.getName() + "required, but found: " + stats[i]);
                }

                array[i] = type;
            }
            _negateStats = array;
        }

        String negateAbnormals = set.getString("negateAbnormals", null);
        if (negateAbnormals != null && !negateAbnormals.isEmpty()) {
            _negateAbnormals = new FastMap<>();
            for (String ngtStack : negateAbnormals.split(";")) {
                String[] ngt = ngtStack.split(",");
                if (ngt.length == 1) // Only abnormalType is present, without abnormalLvl
                {
                    _negateAbnormals.put(ngt[0], Byte.MAX_VALUE);
                } else if (ngt.length == 2) // Both abnormalType and abnormalLvl are present
                {
                    try {
                        _negateAbnormals.put(ngt[0], Byte.parseByte(ngt[1]));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("SkillId: " + _id + " Byte value required, but found: " + ngt[1]);
                    }
                } else // If not both from above, then smth is messed up... throw an error.
                {
                    throw new IllegalArgumentException("SkillId: " + _id + ": Incorrect negate Abnormals for " + ngtStack + ". Lvl: abnormalType1,abnormalLvl1;abnormalType2,abnormalLvl2;abnormalType3,abnormalLvl3... or abnormalType1;abnormalType2;abnormalType3...");
                }
            }
        } else {
            _negateAbnormals = null;
        }

        String negateEffects = set.getString("negateEffects", null);
        if (negateEffects != null && !negateEffects.isEmpty()) {
            String[] effects = negateEffects.split(";");
            if (effects.length > 0) {
                _negateEffects = new FastList<>();

                for (String effect : effects) {
                    try {
                        _negateEffects.add(L2EffectType.valueOf(effect));
                    } catch (Exception e) {
                        throw new IllegalArgumentException("SkillId: " + _id + ": Incorrect negate effect [" + effect + "] for.");
                    }
                }
            } else {
                _negateEffects = null;
            }
        } else {
            _negateEffects = null;
        }

        String negateId = set.getString("negateId", null);
        if (negateId != null) {
            String[] valuesSplit = negateId.split(",");
            _negateId = new int[valuesSplit.length];
            for (int i = 0; i < valuesSplit.length; i++) {
                _negateId[i] = Integer.parseInt(valuesSplit[i]);
            }
        } else {
            _negateId = new int[0];
        }
        _maxNegatedEffects = set.getInteger("maxNegated", 0);

        _stayAfterDeath = set.getBool("stayAfterDeath", false);
        _stayOnSubclassChange = set.getBool("stayOnSubclassChange", false);

        _killByDOT = set.getBool("killByDOT", false);
        _isNeutral = set.getBool("neutral", false);
        _isNoFlag = set.getBool("isNoFlag", false);
        String hitTimings = set.getString("hitTimings", null);
        if (hitTimings != null) {
            try {
                String[] valuesSplit = hitTimings.split(",");
                _hitTimings = new int[valuesSplit.length];
                for (int i = 0; i < valuesSplit.length; i++) {
                    _hitTimings[i] = Integer.parseInt(valuesSplit[i]);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("SkillId: " + _id + " invalid hitTimings value: " + hitTimings + ", \"percent,percent,...percent\" required");
            }
        } else {
            _hitTimings = new int[0];
        }

        _isDebuff = set.getBool("isDebuff", false);
        _restartableDebuff = set.getBool("isRestartableDebuff", false);

        _feed = set.getInteger("feed", 0);

        _reuseHashCode = SkillTable.getSkillHashCode(_id, _level);

        _buffDuration = set.getInteger("buffDuration", 0);

        _skillRadius = set.getInteger("skillRadius", 0);

        _targetType = set.getEnum("target", L2TargetType.class, L2TargetType.TARGET_ONE);
        _power = set.getFloat("power", 0.0f);
        _pvpPower = set.getFloat("pvpPower", (float) _power);
        _pvePower = set.getFloat("pvePower", (float) _power);
        _magicLevel = set.getInteger("magicLvl", 0);
        _lvlBonusRate = set.getInteger("lvlDepend", 0);
        _ignoreResists = set.getBool("ignoreResists", false);
        _minChance = set.getInteger("minChance", Config.MIN_DEBUFF_CHANCE);
        _maxChance = set.getInteger("maxChance", Config.MAX_DEBUFF_CHANCE);
        _stat = set.getEnum("stat", Stats.class, null);
        _ignoreShield = set.getBool("ignoreShld", false);
        _ignorePdefPercent = set.getFloat("ignorePdefPercent", 0);

        _ignoreSkillStun = set.getBool("ignoreSkillStun", false);
        _ignoreSkillParalyze = set.getBool("ignoreSkillParalyze", false);

        _skillType = set.getEnum("skillType", L2SkillType.class, L2SkillType.DUMMY);
        _effectType = set.getEnum("effectType", L2SkillType.class, null);
        _effectId = set.getInteger("effectId", 0);
        _effectLvl = set.getInteger("effectLevel", 0);

        _nextActionIsAttack = set.getBool("nextActionAttack", false);
        _nextActionIsUseSkill = set.getBool("nextActionUseSkill", false);

        _element = set.getByte("element", (byte) -1);
        _elementPower = set.getInteger("elementPower", 0);

        _saveVs = set.getEnum("saveVs", BaseStats.class, BaseStats.NULL);

        _basicProperty = set.getEnum("basicProperty", L2BasicResistType.class, L2BasicResistType.NONE);
        _condition = set.getInteger("condition", 0);
        _conditionValue = set.getInteger("conditionValue", 0);
        _overhit = set.getBool("overHit", false);
        _isSuicideAttack = set.getBool("isSuicideAttack", false);

        String weaponsAllowedString = set.getString("weaponsAllowed", null);
        if (weaponsAllowedString != null && !weaponsAllowedString.trim().isEmpty()) {
            int mask = 0;
            StringTokenizer st = new StringTokenizer(weaponsAllowedString, ",");
            while (st.hasMoreTokens()) {
                int old = mask;
                String item = st.nextToken().trim();
                if (ItemTable._weaponTypes.containsKey(item)) {
                    mask |= ItemTable._weaponTypes.get(item).mask();
                }

                if (ItemTable._armorTypes.containsKey(item)) // for shield
                {
                    mask |= ItemTable._armorTypes.get(item).mask();
                }

                if (old == mask) {
                    _log.log(Level.INFO, "[weaponsAllowed] Unknown item type name: " + item);
                }
            }
            _weaponsAllowed = mask;
        } else {
            _weaponsAllowed = 0;
        }

        _armorsAllowed = set.getInteger("armorsAllowed", 0);

        _minPledgeClass = set.getInteger("minPledgeClass", 0);
        _isOffensive = set.getBool("offensive", isSkillTypeOffensive());
        _maxCharges = set.getInteger("maxCharges", 0);
        _numCharges = set.getInteger("numCharges", 0);
        _maxChargesConsumeCount = set.getInteger("chargesMaxConsume", 0);
        _triggeredId = set.getInteger("triggeredId", 0);
        _triggeredLevel = set.getInteger("triggeredLevel", 0);
        _triggeredLevelUPMax = set.getInteger("triggeredLevelUpMax", 0);

        _triggeredById = set.getInteger("triggeredById", 0);
        _absorbDmgPercent = set.getFloat("absorbPart", 0.0f);
        _chanceType = set.getString("chanceType", "");
        if (!_chanceType.isEmpty() && !_chanceType.isEmpty()) {
            _chanceCondition = ChanceCondition.parse(set);
        }

        _numSouls = set.getInteger("num_souls", 0);
        _soulMaxConsume = set.getInteger("soulMaxConsumeCount", 0);
        _blowChance = set.getInteger("blowChance", 0);
        _soulConsume = set.getInteger("soulConsumeCount", 0);
        _expNeeded = set.getInteger("expNeeded", 0);
        _critChance = set.getInteger("critChance", 0);

        for (String st : set.getString("transformId", "0").split(";")) {
            _transformId.add(Integer.parseInt(st));
        }

        _transformDuration = set.getInteger("transformDuration", 0);

        _isHeroSkill = SkillTreesData.getInstance().isHeroSkill(_id, _level);
        _isGMSkill = SkillTreesData.getInstance().isGMSkill(_id, _level);

        _baseCritRate = set.getInteger("baseCritRate", _skillType == L2SkillType.PDAM || _skillType == L2SkillType.BLOW ? 0 : -1);
        _lethalEffect1 = set.getInteger("lethal1", 0);
        _lethalEffect2 = set.getInteger("lethal2", 0);

        _directHpDmg = set.getBool("dmgDirectlyToHp", false);
        _isTriggeredSkill = set.getBool("isTriggeredSkill", false);
        _aggroPoints = set.getInteger("aggroPoints", 0);

        _flyType = set.getString("flyType", null);
        _flyRadius = set.getInteger("flyRadius", 0);
        _flyCourse = set.getFloat("flyCourse", 0);
        _flySpeed = set.getInteger("flySpeed", 0);
        _flyDelay = set.getInteger("flyDelay", 0);
        _flyAnimationSpeed = set.getInteger("flyAnimationSpeed", 0);
        _canBeReflected = set.getBool("canBeReflected", true);

        _canBeDispeled = set.getBool("canBeDispeled", true);

        _isClanSkill = set.getBool("isClanSkill", false);
        _isSharedSkill = set.getBool("isSharedSkill", false);
        _excludedFromCheck = set.getBool("excludedFromCheck", false);
        _dependOnTargetBuff = set.getFloat("dependOnTargetBuff", 0);

        String dependOnTargetEffectId = set.getString("dependOnTargetEffectId", null);
        if (dependOnTargetEffectId != null) {
            String[] valuesSplit = dependOnTargetEffectId.split(",");
            _dependOnTargetEffectId = new int[valuesSplit.length];
            for (int i = 0; i < valuesSplit.length; i++) {
                _dependOnTargetEffectId[i] = Integer.parseInt(valuesSplit[i]);
            }
        } else {
            _dependOnTargetEffectId = new int[0];
        }

        _simultaneousCast = set.getBool("simultaneousCast", false);

        _maxTargets = set.getInteger("maxTargets", -1);
        _isStaticHeal = set.getBool("isStaticHeal", false);

        String capsuled_items = set.getString("capsuled_items_skill", null);
        if (capsuled_items != null) {
            if (capsuled_items.isEmpty()) {
                _log.log(Level.WARN, "Empty Extractable Item Skill data in Skill Id: " + _id);
            }

            _extractableItems = parseExtractableSkill(_id, _level, capsuled_items);
        }
        _isForceStorable = set.getBool("forceStore", false);

        _isHerbEffect = set.getBool("isHerbEffect", false);

        _isVitalityItemSkill = set.getBool("isVitalityItemSkill", false);
        _npcId = set.getInteger("npcId", 0);

        _faceId = set.getInteger("faceId", -1);
        _hairColorId = set.getInteger("hairColorId", -1);
        _hairStyleId = set.getInteger("hairStyleId", -1);
        _isMentorSkill = set.getBool("isMentorSkill", false);
        parseHooks(set.getString("skillHooks", null));
    }

    public void useSkill(L2Character caster, L2Object[] targets) {

    }

    public int getArmorsAllowed() {
        return _armorsAllowed;
    }

    public int getConditionValue() {
        return _conditionValue;
    }

    public L2SkillType getSkillType() {
        return _skillType;
    }

    public L2TraitType getTraitType() {
        return _traitType;
    }

    public byte getElement() {
        return _element;
    }

    public int getElementPower() {
        return _elementPower;
    }

    /**
     * @return the target type of the skill : SELF, PARTY, CLAN, PET...
     */
    public L2TargetType getTargetType() {
        return _targetType;
    }

    public int getCondition() {
        return _condition;
    }

    public boolean isOverhit() {
        return _overhit;
    }

    public boolean killByDOT() {
        return _killByDOT;
    }

    public boolean isSuicideAttack() {
        return _isSuicideAttack;
    }

    public boolean allowOnTransform() {
        return isPassive();
    }

    /**
     * @param activeChar
     * @param target
     * @param isPvP
     * @param isPvE
     * @return the power of the skill.
     */
    public double getPower(L2Character activeChar, L2Character target, boolean isPvP, boolean isPvE) {
        if (activeChar == null) {
            return getPower(isPvP, isPvE);
        }

        switch (_skillType) {
            case DEATHLINK:
                return getPower(isPvP, isPvE) * Math.pow(1.7165 - activeChar.getCurrentHp() / activeChar.getMaxHp(), 2) * 0.577;
                /*
					 * DrHouse:
					 * Rolling back to old formula (look below) for DEATHLINK due to this one based on logarithm is not
					 * accurate enough. Commented here because probably is a matter of just adjusting a constant
					if(activeChar.getCurrentHp() / activeChar.getMaxHp() > 0.005)
						return _power*(-0.45*Math.log(activeChar.getCurrentHp()/activeChar.getMaxHp())+1.);
					else
						return _power*(-0.45*Math.log(0.005)+1.);
					 */
            case FATAL:
                return getPower(isPvP, isPvE) * 3.5 * (1 - target.getCurrentHp() / target.getMaxHp());
            default:
                return getPower(isPvP, isPvE);
        }
    }

    public double getPower() {
        return _power;
    }

    public double getPower(boolean isPvP, boolean isPvE) {
        return isPvE ? _pvePower : isPvP ? _pvpPower : _power;
    }

    public L2SkillType[] getNegateStats() {
        return _negateStats;
    }

    public List<L2EffectType> getNegateEffects() {
        return _negateEffects;
    }

    public Map<String, Byte> getNegateAbnormals() {
        return _negateAbnormals;
    }

    public int getAbnormalLvl() {
        return _abnormalLvl;
    }

    public int getNegateLvl() {
        return _negateLvl;
    }

    public int[] getNegateId() {
        return _negateId;
    }

    public int getMagicLevel() {
        return _magicLevel;
    }

    public int getMaxNegatedEffects() {
        return _maxNegatedEffects;
    }

    public int getLvlBonusRate() {
        return _lvlBonusRate;
    }

    /**
     * @return true if skill should ignore all resistances
     */
    public boolean ignoreResists() {
        return _ignoreResists;
    }

    /**
     * @return minimum skill/effect land rate (default is 1).
     */
    public int getMinChance() {
        return _minChance;
    }

    /**
     * @return maximum skill/effect land rate (default is 99).
     */
    public int getMaxChance() {
        return _maxChance;
    }

    /**
     * @return the additional effect Id.
     */
    public int getEffectId() {
        return _effectId;
    }

    /**
     * @return the additional effect level.
     */
    public int getEffectLvl() {
        return _effectLvl;
    }

    public int getEffectAbnormalLvl() {
        return _effectAbnormalLvl;
    }

    /**
     * @return the additional effect skill type (ex : STUN, PARALYZE,...).
     */
    public L2SkillType getEffectType() {
        return _effectType;
    }

    /**
     * @return {@code true} if character should attack target after skill
     */
    public boolean nextActionIsAttack() {
        return _nextActionIsAttack;
    }


    public boolean nextActionIsSkillUse() {
        return _nextActionIsUseSkill;
    }
    /**
     * @return the buffDuration.
     */
    public int getBuffDuration() {
        return _buffDuration;
    }

    /**
     * @return the castRange.
     */
    public int getCastRange() {
        return _castRange;
    }

    /**
     * @return the cpConsume;
     */
    public int getCpConsume() {
        return _cpConsume;
    }

    /**
     * @return Returns the effectRange.
     */
    public int getEffectRange() {
        return _effectRange;
    }

    /**
     * @return количество поглощаемого HP при использовании
     */
    public int getHpConsume() {
        return _hpConsume;
    }

    /**
     * @return ID скила
     */
    public int getId() {
        return _id;
    }

    /**
     * @return {@code true} если скилл имеет отрицательный эффект
     */
    public boolean isDebuff() {
        return _isDebuff;
    }

    public int getDisplayId() {
        return _displayId;
    }

    public void setDisplayId(int id) {
        _displayId = id;
    }

    public int gettActionId() {
        return _actionId;
    }

    public void setActionId(int id) {
        _actionId = id;
    }

    public int getTriggeredId() {
        return _triggeredId;
    }

    public int getTriggeredLevel() {
        return _triggeredLevel;
    }

    public int getTriggeredLevelUpMax() {
        return _triggeredLevelUPMax;
    }

    public int getTriggeredById() {
        return _triggeredById;
    }

    public boolean triggerAnotherSkill() {
        return _triggeredId > 1;
    }

    public float getAbsorbDmgPercent() {
        return _absorbDmgPercent;
    }

    /**
     * @return the skill type (ex : BLEED, SLEEP, WATER...).<BR><BR>
     */
    public Stats getStat() {
        return _stat;
    }

    /**
     * @return skill saveVs base stat (STR, INT ...).<BR><BR>
     */
    public BaseStats getSaveVs() {
        return _saveVs;
    }

    public L2BasicResistType getBasicProperty() {
        return _basicProperty;
    }

    /**
     * @return Returns the _targetConsumeId.
     */
    public int getTargetConsumeId() {
        return _targetConsumeId;
    }

    /**
     * @return the targetConsume.
     */
    public int getTargetConsume() {
        return _targetConsume;
    }

    /**
     * @return the itemConsume.
     */
    public int getItemConsumeCount() {
        return _itemConsume;
    }

    /**
     * @return the itemConsumeId.
     */
    public int getItemConsumeId() {
        return _itemConsumeId;
    }

    public int getGiveItemCount() {
        return _giveItemCount;
    }

    public int getGiveItemId() {
        return _giveItemId;
    }

    /**
     * @return количество нужной для каста личной славы
     */
    public int getFameConsumeSelf() {
        return _fameConsumeSelf;
    }

    /**
     * @return количество нужной для каста славы клана
     */
    public int getFameConsumeClan() {
        return _fameConsumeClan;
    }

    /**
     * @return уровень умения
     */
    public int getLevel() {
        return _level;
    }

    /**
     * @return хеш значение
     */
    @Override
    public int hashCode() 
    {
        return _hashCode;
    }
    
    /**
     * @return {@code true} если умение является магией
     */
    public boolean isMagic() {
        return _magic == L2MagicType.MAGIC;
    }

    /**
     * @return {@code true} если умение статическое
     */
    public boolean isStatic() {
        return _magic == L2MagicType.STATIC;
    }

    /**
     * @return {@code true} если умение имеет статическое время отката
     */
    public boolean isStaticReuse() {
        return _staticReuse;
    }

    /**
     * @return {@code true} если умение имеет фиксированный урон
     */
    public boolean isStaticDamage() {
        return _staticDamage;
    }

    /**
     * @return количество потребляемой маны по окончании произношения заклинания
     */
    public int getMpConsume() {
        return _mpConsume;
    }

    /**
     * @return количество потребляемой маны при попытке использовать умение
     */
    public int getMpInitialConsume() {
        return _mpInitialConsume;
    }

    /**
     * @return имя умения
     */
    public String getName() {
        return _name;
    }

    /**
     * @return откат умения
     */
    public int getReuseDelay() {
        return _reuseDelay;
    }

    public int getReuseHashCode() {
        return _reuseHashCode;
    }

    public int getHitTime() {
        return _hitTime;
    }

    public int getHitCounts() {
        return _hitTimings.length;
    }

    public int[] getHitTimings() {
        return _hitTimings;
    }

    /**
     * @return the coolTime.
     */
    public int getCoolTime() {
        return _coolTime;
    }

    public int getSkillRadius() {
        return _skillRadius;
    }

    public boolean isActive() {
        return _operateType != null && _operateType.isActive();
    }

    public boolean isPassive() {
        return _operateType != null && _operateType.isPassive();
    }

    public boolean isToggle() {
        return _operateType != null && _operateType.isToggle();
    }

    public boolean isActiveToggle() {
        return isActive() && isToggle();
    }

    public boolean isChance() {
        return _chanceCondition != null;
    }

    public boolean isTriggeredSkill() {
        return _isTriggeredSkill;
    }

    public boolean isDance() {
        return _magic == L2MagicType.DANCE;
    }

    public boolean isMentor() {
        return _isMentorSkill;
    }

    public int getAggroPoints() {
        return _aggroPoints;
    }

    public boolean useSoulShot() {
        switch (_skillType) {
            case PDAM:
            case CHARGEDAM:
            case BLOW:
                return true;
            default:
                return false;
        }
    }

    public boolean useSpiritShot() {
        return isMagic();
    }

    public boolean useFishShot() {
        return _skillType == L2SkillType.PUMPING || _skillType == L2SkillType.REELING;
    }

    public int getWeaponsAllowed() {
        return _weaponsAllowed;
    }

    public int getMinPledgeClass() {
        return _minPledgeClass;
    }

    public boolean isOffensive() {
        return _isOffensive;
    }

    public boolean isNeutral() {
        return _isNeutral;
    }

    public boolean isNoFlag() {
        return _isNoFlag;
    }

    public boolean isHeroSkill() {
        return _isHeroSkill;
    }

    public boolean isGMSkill() {
        return _isGMSkill;
    }

    public int getNumCharges() {
        return _numCharges;
    }

    public int getMaxChargesConsumeCount() {
        return _maxChargesConsumeCount;
    }

    public int getNumSouls() {
        return _numSouls;
    }

    public int getMaxSoulConsumeCount() {
        return _soulMaxConsume;
    }

    public int getSoulConsumeCount() {
        return _soulConsume;
    }

    public int getExpNeeded() {
        return _expNeeded;
    }

    public int getCritChance() {
        return _critChance;
    }

    public FastList<Integer> getTransformId() {
        return _transformId;
    }

    public int getTransformDuration() {
        return _transformDuration;
    }

    public int getBaseCritRate() {
        return _baseCritRate;
    }

    public int getLethalChance1() {
        return _lethalEffect1;
    }

    public int getLethalChance2() {
        return _lethalEffect2;
    }

    public boolean getDmgDirectlyToHP() {
        return _directHpDmg;
    }

    /**
     * Для скиллов использующих анимацию FlyToLocation
     * Тип анимации
     *
     * @return _flyType
     */
    public String getFlyType() {
        return _flyType;
    }

    /**
     * Для скиллов использующих анимацию FlyToLocation
     * Расстояние полета(движения)
     *
     * @return _flyRadius
     */
    public int getFlyRadius() {
        return _flyRadius;
    }

    /**
     * Для скиллов использующих анимацию FlyToLocation
     * Скорость полета(движения)
     *
     * @return _flySpeed
     */
    public int getFlySpeed() {
        return _flySpeed;
    }

    /**
     * Для скиллов использующих анимацию FlyToLocation
     * Задержка перед началом полета(движения)
     * Чем меньше значение тем больше задержка, минимальное 0.
     *
     * @return _flyDelay
     */
    public int getFlyDelay() {
        return _flyDelay;
    }

    /**
     * Для скиллов использующих анимацию FlyToLocation
     * Вероятно скорость анимации
     * На оффе с некоторыми скиллами приходит 333
     *
     * @return
     */
    public int getFlyAnimationSpeed() {
        return _flyAnimationSpeed;
    }

    /**
     * Для скиллов использующих анимацию FlyToLocation
     * Направление объекта относительно своей оси.
     *
     * @return _flyCourse
     */
    public float getFlyCourse() {
        return _flyCourse;
    }

    public boolean isPvpSkill() {
        switch (_skillType) {
            case DOT:
            case BLEED:
            case CONFUSION:
            case POISON:
            case DEBUFF:
            case AGGDEBUFF:
            case STUN:
            case ROOT:
            case FEAR:
            case SLEEP:
            case MDOT:
            case MUTE:
            case PARALYZE:
            case FLY_UP:
            case KNOCK_DOWN:
            case KNOCK_BACK:
            case CANCEL:
            case BETRAY:
            case DISARM:
            case AGGDAMAGE:
            case STEAL_BUFF:
            case AGGREDUCE_CHAR:
            case MANADAM:
            case ERASE:
                return true;
            default:
                return false;
        }
    }

    public boolean isSkillTypeOffensive() {
        switch (_skillType) {
            case PDAM:
            case MDAM:
            case CPDAM:
            case DOT:
            case CPDAMPERCENT:
            case BLEED:
            case POISON:
            case AGGDAMAGE:
            case DEBUFF:
            case AGGDEBUFF:
            case STUN:
            case ROOT:
            case CONFUSION:
            case ERASE:
            case BLOW:
            case FATAL:
            case FEAR:
            case DRAIN:
            case SLEEP:
            case CHARGEDAM:
            case CONFUSE_MOB_ONLY:
            case DEATHLINK:
            case DETECT_WEAKNESS:
            case MANADAM:
            case MDOT:
            case MUTE:
            case SOULSHOT:
            case SPIRITSHOT:
            case SPOIL:
            case SWEEP:
            case PARALYZE:
            case DRAIN_SOUL:
            case AGGREDUCE:
            case CANCEL:
                //case AGGREMOVE:
            case AGGREDUCE_CHAR:
            case BETRAY:
            case DELUXE_KEY_UNLOCK:
            case SOW:
            case HARVEST:
            case DISARM:
            case STEAL_BUFF:
            case INSTANT_JUMP:
            case FLY_UP:
            case KNOCK_DOWN:
            case KNOCK_BACK:
                return true;
            default:
                return _isDebuff;
        }
    }

    public boolean isStayAfterDeath() {
        return _stayAfterDeath;
    }

    public boolean isStayOnSubclassChange() {
        return _stayOnSubclassChange;
    }

    public boolean getWeaponDependancy(L2Character activeChar) {
        if (getWeaponDependancy(activeChar, false)) {
            return true;
        } else {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(this));
            return false;
        }
    }

    public boolean getWeaponDependancy(L2Character activeChar, boolean chance) {
        int weaponsAllowed = _weaponsAllowed;
        //check to see if skill has a weapon dependency.
        if (weaponsAllowed == 0) {
            return true;
        }

        int mask = 0;

        if (activeChar.getActiveWeaponItem() != null) {
            mask |= activeChar.getActiveWeaponItem().getItemType().mask();
        }
        if (activeChar.getSecondaryWeaponItem() != null && activeChar.getSecondaryWeaponItem() instanceof L2Armor) {
            mask |= activeChar.getSecondaryWeaponItem().getItemType().mask();
        }

        return (mask & weaponsAllowed) != 0;

    }

    public boolean checkCondition(L2Character activeChar, L2Object target, boolean itemOrWeapon) {
        if (activeChar.isGM() && !Config.GM_SKILL_RESTRICTION) {
            return true;
        }
        if ((_condition & COND_SHIELD) != 0) {
			/*
				L2Armor armorPiece;
				L2ItemInstance dummy;
				dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				armorPiece = (L2Armor) dummy.getItem();
				*/
            //TODO add checks for shield here.
        }

        List<Condition> preCondition = _preCondition;
        if (itemOrWeapon) {
            preCondition = _itemPreCondition;
        }

        if (preCondition == null || preCondition.isEmpty()) {
            return true;
        }

        for (Condition cond : preCondition) {
            Env env = new Env();
            env.setPlayer(activeChar);
            if (target instanceof L2Character) // TODO: object or char?
            {
                env.setTarget((L2Character) target);
            }
            env.setSkill(this);

            if (!cond.test(env)) {
                String msg = cond.getMessage();
                int msgId = cond.getMessageId();
                if (msgId != 0) {
                    SystemMessage sm = SystemMessage.getSystemMessage(msgId);
                    if (cond.isAddName()) {
                        sm.addSkillName(_id);
                    }
                    activeChar.sendPacket(sm);
                } else if (msg != null) {
                    activeChar.sendMessage(msg);
                }
                return false;
            }
        }
        return true;
    }

	/*
	  * Check if should be target added to the target list
	  * false if target is dead, target same as caster,
	  * target inside peace zone, target in the same party with caster,
	  * caster can see target
	  * Additional checks if not in PvP zones (arena, siege):
	  * target in not the same clan and alliance with caster,
	  * and usual skill PvP check.
	  * If TvT event is active - performing additional checks.
	  *
	  * Caution: distance is not checked.
	  */

    public L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst) {
        // Init to null the target of the skill
        L2Character target = null;

        // Get the L2Objcet targeted by the user of the skill at this moment
        L2Object objTarget = activeChar.getTarget();
        // If the L2Object targeted is a L2Character, it becomes the L2Character target
        if (objTarget instanceof L2Character) {
            target = (L2Character) objTarget;
        }

        return getTargetList(activeChar, onlyFirst, target);
    }

    /**
     * <B><U> Values of skill type</U> :</B><BR><BR>
     * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no L2PcInstance targeted</li>
     * <li>SELF</li>
     * <li>HOLY, UNDEAD</li>
     * <li>PET</li>
     * <li>AURA, AURA_CLOSE</li>
     * <li>AREA</li>
     * <li>MULTIFACE</li>
     * <li>PARTY, CLAN</li>
     * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
     * <li>UNLOCKABLE</li>
     * <li>ITEM</li><BR><BR>
     *
     * @param activeChar The L2Character who use the skill
     * @param onlyFirst
     * @param target
     * @return all targets of the skill in a table in function a the skill type.
     */
    public L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst, L2Character target) {
        ITargetTypeHandler handler = TargetHandler.getInstance().getHandler(_targetType);
        if (handler != null) {
            try {
                return handler.getTargetList(this, activeChar, onlyFirst, target);
            } catch (Exception e) {
                _log.log(Level.ERROR, "Exception in L2Skill.getTargetList(): " + e.getMessage(), e);
            }
        }
        activeChar.sendMessage("Target type of skill is not currently handled");
        return _emptyTargetList;
    }

    public L2Object[] getTargetList(L2Character activeChar) {
        return getTargetList(activeChar, false);
    }

    public L2Object getFirstOfTargetList(L2Character activeChar) {
        L2Object[] targets;

        targets = getTargetList(activeChar, true);

        return targets.length == 0 ? null : targets[0];
    }

    public Func[] getStatFuncs(L2Effect effect, L2Character player) {
        if (_funcTemplates == null) {
            return _emptyFunctionSet;
        }

        if (!(player instanceof L2Playable) && !(player instanceof L2Attackable)) {
            return _emptyFunctionSet;
        }

        ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);

        Env env = new Env();
        env.setPlayer(player);
        env.setSkill(this);

        Func f;

        for (FuncTemplate t : _funcTemplates) {
            f = t.getFunc(env, this); // skill is owner
            if (f != null) {
                funcs.add(f);
            }
        }
        if (funcs.isEmpty()) {
            return _emptyFunctionSet;
        }

        return funcs.toArray(new Func[funcs.size()]);
    }

    public boolean hasEffects() {
        return _effectTemplates != null && _effectTemplates.length > 0;
    }

    public EffectTemplate[] getEffectTemplates() {
        return _effectTemplates;
    }

    public EffectTemplate[] getEffectTemplatesPassive() {
        return _effectTemplatesPassive;
    }

    public boolean hasSelfEffects() {
        return _effectTemplatesSelf != null && _effectTemplatesSelf.length > 0;
    }

    public boolean hasPassiveEffects() {
        return _effectTemplatesPassive != null && _effectTemplatesPassive.length > 0;
    }

    /**
     * Env is used to pass parameters for secondary effects (shield and ss/bss/bsss)
     *
     * @return an array with the effects that have been added to effector
     */
    public L2Effect[] getEffects(L2Character effector, L2Character effected, Env env, int effectTime) {
        if (!hasEffects() || isPassive()) {
            return _emptyEffectSet;
        }

        // doors and siege flags cannot receive any effects
        if (effected instanceof L2DoorInstance || effected instanceof L2SiegeFlagInstance) {
            return _emptyEffectSet;
        }

        if (effector.isPlayer() && getSkillType() == L2SkillType.BUFF && (effected.isMonster() || effected.isRaid() || effected.isRaidMinion() || effected.isGrandRaid())) {
            effector.getActingPlayer().getPvPFlagController().updateStatus();
        }

        if (effector != effected && (isOffensive() || isDebuff()) && effected.isInvul()) {
            return _emptyEffectSet;
        }

        ArrayList<L2Effect> effects = new ArrayList<>(_effectTemplates.length);

        if (env == null) {
            env = new Env();
        }

        env.setSkillMastery(SkillMastery.calcSkillMastery(effector, this));
        env.setPlayer(effector);
        env.setTarget(effected);
        env.setSkill(this);

        for (EffectTemplate et : _effectTemplates) {
            boolean success = Effects.calcEffectSuccess(effector, effected, et, this, env.getShld(), env.isSs(), env.isSps(), env.isBss());
            if (success && Rnd.getChance(et.getActivationChanceEffect())) {
                L2Effect e = et.getEffect(env);
                if (e != null) {
                    // Для выставления кастомной длительности эффектов
                    if (effectTime > 1) {
                        e.setAbnormalTime(effectTime);
                    }
                    e.scheduleEffect();
                    effects.add(e);
                }
            }
            // display fail message only for effects with icons
            else if (et.icon && effector instanceof L2PcInstance) {
                effector.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(effected).addSkillName(this));
            }
        }

        if (effects.isEmpty()) {
            return _emptyEffectSet;
        }

        return effects.toArray(new L2Effect[effects.size()]);
    }

    public L2Effect[] getEffects(L2Character effector, L2Character effected, Env env) {
        return getEffects(effector, effected, env, 1);
    }

    public L2Effect[] getEffects(L2Character effector, L2Character effected, int timeMultiplier) {
        return getEffects(effector, effected, null, timeMultiplier);
    }

    public L2Effect[] getEffectsPassive(L2Character effector) {
        if (!hasPassiveEffects()) {
            return _emptyEffectSet;
        }

        List<L2Effect> effects = new ArrayList<>(_effectTemplatesPassive.length);

        for (EffectTemplate et : _effectTemplatesPassive) {
            Env env = new Env();
            env.setPlayer(effector);
            env.setTarget(effector);
            env.setSkill(this);
            L2Effect e = et.getEffect(env);
            if (e != null) {
                e.setPassiveEffect();
                e.scheduleEffect();
                effects.add(e);
            }
        }
        if (effects.isEmpty()) {
            return _emptyEffectSet;
        }

        return effects.toArray(new L2Effect[effects.size()]);
    }

    /**
     * Warning: this method doesn't consider modifier (shield, ss, sps, bss) for secondary effects
     */
    public L2Effect[] getEffects(L2Character effector, L2Character effected) {
        return getEffects(effector, effected, null);
    }

    /**
     * This method has suffered some changes in CT2.2 ->CT2.3<br>
     * Effect engine is now supporting secondary effects with independent
     * success/fail calculus from effect skill. Env parameter has been added to
     * pass parameters like soulshot, spiritshots, blessed spiritshots or shield deffence.
     * Some other optimizations have been done
     * <br><br>
     * This new feature works following next rules:
     * <li> To enable feature, effectPower must be over -1 (check XmlDocumentSkill#attachEffect for further information)</li>
     * <li> If main skill fails, secondary effect always fail</li>
     */
    public L2Effect[] getEffects(L2CubicInstance effector, L2Character effected, Env env) {
        if (!hasEffects() || isPassive()) {
            return _emptyEffectSet;
        }

        if (!effector.getOwner().equals(effected)) {
            if (_isDebuff || _isOffensive) {
                if (effected.isInvul()) {
                    return _emptyEffectSet;
                }

                if (effector.getOwner().isGM() && !effector.getOwner().getAccessLevel().canGiveDamage()) {
                    return _emptyEffectSet;
                }
            }
        }

        ArrayList<L2Effect> effects = new ArrayList<>(_effectTemplates.length);

        if (env == null) {
            env = new Env();
        }

        env.setPlayer(effector.getOwner());
        env.setCubic(effector);
        env.setTarget(effected);
        env.setSkill(this);

        for (EffectTemplate et : _effectTemplates) {
            boolean success = Effects.calcEffectSuccess(effector.getOwner(), effected, et, this, env.getShld(), env.isSs(), env.isSps(), env.isBss());
            if (success) {
                L2Effect e = et.getEffect(env);
                if (e != null) {
                    e.scheduleEffect();
                    effects.add(e);
                }
            }
        }

        if (effects.isEmpty()) {
            return _emptyEffectSet;
        }

        return effects.toArray(new L2Effect[effects.size()]);
    }

    public L2Effect[] getEffectsSelf(L2Character effector) {
        if (!hasSelfEffects() || isPassive()) {
            return _emptyEffectSet;
        }

        List<L2Effect> effects = new ArrayList<>(_effectTemplatesSelf.length);

        for (EffectTemplate et : _effectTemplatesSelf) {
            Env env = new Env();
            env.setPlayer(effector);
            env.setTarget(effector);
            env.setSkill(this);
            L2Effect e = et.getEffect(env);
            if (e != null) {
                e.setSelfEffect();
                e.scheduleEffect();
                effects.add(e);
            }
        }
        if (effects.isEmpty()) {
            return _emptyEffectSet;
        }

        return effects.toArray(new L2Effect[effects.size()]);
    }

    public void attachPassive(EffectTemplate effect) {
        if (_effectTemplatesPassive == null) {
            _effectTemplatesPassive = new EffectTemplate[]{effect};
        } else {
            int len = _effectTemplatesPassive.length;
            EffectTemplate[] tmp = new EffectTemplate[len + 1];
            System.arraycopy(_effectTemplatesPassive, 0, tmp, 0, len);
            tmp[len] = effect;
            _effectTemplatesPassive = tmp;
        }
    }

    public void attach(FuncTemplate f) {
        if (_funcTemplates == null) {
            _funcTemplates = new FuncTemplate[]{f};
        } else {
            int len = _funcTemplates.length;
            FuncTemplate[] tmp = new FuncTemplate[len + 1];
            System.arraycopy(_funcTemplates, 0, tmp, 0, len);
            tmp[len] = f;
            _funcTemplates = tmp;
        }
    }

    public void attach(EffectTemplate effect) {
        if (_effectTemplates == null) {
            _effectTemplates = new EffectTemplate[]{effect};
        } else {
            int len = _effectTemplates.length;
            EffectTemplate[] tmp = new EffectTemplate[len + 1];
            System.arraycopy(_effectTemplates, 0, tmp, 0, len);
            tmp[len] = effect;
            _effectTemplates = tmp;
        }

    }

    public void attachSelf(EffectTemplate effect) {
        if (_effectTemplatesSelf == null) {
            _effectTemplatesSelf = new EffectTemplate[]{effect};
        } else {
            int len = _effectTemplatesSelf.length;
            EffectTemplate[] tmp = new EffectTemplate[len + 1];
            System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
            tmp[len] = effect;
            _effectTemplatesSelf = tmp;
        }
    }

    public void attach(Condition c, boolean itemOrWeapon) {
        if (itemOrWeapon) {
            if (_itemPreCondition == null) {
                _itemPreCondition = new ArrayList<>();
            }
            _itemPreCondition.add(c);
        } else {
            if (_preCondition == null) {
                _preCondition = new ArrayList<>();
            }
            _preCondition.add(c);
        }
    }

    @Override
    public String toString() {
        return _name + " [id=" + _id + ",lvl=" + _level + ']';
    }

    /**
     * @return pet food
     */
    public int getFeed() {
        return _feed;
    }

    /**
     * used for tracking item id in case that item consume cannot be used
     *
     * @return reference item id
     */
    public int getReferenceItemId() {
        return _refId;
    }

    public int getMaxCharges() {
        return _maxCharges;
    }

    public int getAfterEffectId() {
        return _afterEffectId;
    }

    public int getAfterEffectLvl() {
        return _afterEffectLvl;
    }

    @Override
    public boolean triggersChanceSkill() {
        return _triggeredId > 0 && isChance();
    }

    @Override
    public int getTriggeredChanceId() {
        return _triggeredId;
    }

    @Override
    public int getTriggeredChanceLevel() {
        return _triggeredLevel;
    }

    @Override
    public ChanceCondition getTriggeredChanceCondition() {
        return _chanceCondition;
    }

    public String getAttributeName() {
        return _attribute;
    }

    public L2Skill getReplaceableSkills(L2PcInstance activeChar) {
        if (_replaceableSkills != null) {
            int replaceSkill = 0;
            for (int[] replacable : _replaceableSkills) {
                replaceSkill = replacable[0];
                // Проверяем наличие аур
                for (short i = 1; i < replacable.length; ++i) {
                    // Если нет аур то убираем замену.
                    if (activeChar.getFirstEffect(replacable[i]) == null) {
                        replaceSkill = 0;
                        break;
                    }
                }

                if (replaceSkill != 0) {
                    break;
                }
            }
            if (replaceSkill > 0) {
                L2Skill skill = SkillTable.getInstance().getInfo(replaceSkill, _level);
                skill._replaceableSk = _id;
                return skill;
            }
        }
        return this;
    }

    public int getReplaceableSkillId() {
        return _replaceableSk;
    }

    public void setReplaceableSkillId(int newID) {
        _replaceableSk = newID;
    }

    public boolean isReplaceableSkills() {
        return _replaceableSkills != null;
    }

    /**
     * @return the _blowChance
     */
    public int getBlowChance() {
        return _blowChance;
    }

    public boolean ignoreShield() {
        return _ignoreShield;
    }

    public float getIgnorePdefPercent() {
        return _ignorePdefPercent;
    }

    public boolean ignoreSkillStun() {
        return _ignoreSkillStun;
    }

    public boolean ignoreSkillParalyze() {
        return _ignoreSkillParalyze;
    }

    public boolean canBeReflected() {
        return _canBeReflected;
    }

    public boolean canBeDispeled() {
        return _canBeDispeled;
    }

    public boolean isClanSkill() {
        return _isClanSkill;
    }

    public boolean isSharedSkill() {
        return _isSharedSkill;
    }

    public boolean isExcludedFromCheck() {
        return _excludedFromCheck;
    }

    public float getDependOnTargetBuff() {
        return _dependOnTargetBuff;
    }

    public int[] getDependOnTargetEffectId() {
        return _dependOnTargetEffectId;
    }

    public boolean isSimultaneousCast() {
        return _simultaneousCast;
    }

    public int getMaxTargets() {
        return _maxTargets;
    }

    public boolean isStaticHeal() {
        return _isStaticHeal;
    }

    private L2ExtractableSkill parseExtractableSkill(int skillId, int skillLvl, String values) {
        String[] prodLists = values.split(";");
        List<L2ExtractableProductItem> products = new ArrayList<>();
        String[] prodData;
        for (String prodList : prodLists) {
            prodData = prodList.split(",");
            if (prodData.length < 3) {
                _log.log(Level.WARN, "Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> wrong seperator!");
            }
            List<ItemHolder> items = null;
            double chance = 0;
            int prodId = 0;
            int quantity = 0;
            int lenght = prodData.length - 1;
            try {
                items = new ArrayList<>(lenght / 2);
                for (int j = 0; j < lenght; j++) {
                    prodId = Integer.parseInt(prodData[j]);
                    quantity = Integer.parseInt(prodData[j += 1]);
                    if (prodId <= 0 || quantity <= 0) {
                        _log.log(Level.WARN, "Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " wrong production Id: " + prodId + " or wrond quantity: " + quantity + '!');
                    }
                    items.add(new ItemHolder(prodId, quantity));
                }
                chance = Double.parseDouble(prodData[lenght]);
            } catch (Exception e) {
                _log.log(Level.ERROR, "Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> incomplete/invalid production data or wrong seperator!");
            }
            products.add(new L2ExtractableProductItem(items, chance));
        }

        if (products.isEmpty()) {
            _log.log(Level.WARN, "Extractable skills data: Error in Skill Id: " + skillId + " Level: " + skillLvl + " -> There are no production items!");
        }
        return new L2ExtractableSkill(SkillTable.getSkillHashCode(skillId, skillLvl), products);
    }

    public L2ExtractableSkill getExtractableSkill() {
        return _extractableItems;
    }

    private void addHook(SkillHookTemplate temp) {
        if (skillHooks == null) {
            skillHooks = new SkillHookTemplate[1];
            skillHooks[0] = temp;
            return;
        }

        skillHooks = Arrays.copyOf(skillHooks, skillHooks.length + 1);
        skillHooks[skillHooks.length - 1] = temp;
    }

    private void parseHooks(String hooks) {
        //TODO: Handle this better please?
        if (hooks == null) {
            skillHooks = emptyHooks;
            return;
        }

        String[] hooksSplit = hooks.split(";");

        for (String hookInfo : hooksSplit) {
            String[] hookSplit = hookInfo.split(",");
            String[] args = hookSplit.length > 3 ? Arrays.copyOfRange(hookSplit, 3, hookSplit.length) : new String[0];

            addHook(new SkillHookTemplate(Integer.parseInt(hookSplit[1]), Integer.parseInt(hookSplit[2]), _id, !isPassive(), hookSplit[0], args));
        }
    }

    public SkillHookTemplate[] getSkillHookTemplate() {
        return skillHooks;
    }

    /**
     * TODO: JavaDoc's me!
     *
     * @return
     */
    public boolean isForceStorable() {
        return _isForceStorable;
    }

    public boolean isHerbEffect() {
        return _isHerbEffect;
    }

    public boolean isVitalityItemSkill() {
        return _isVitalityItemSkill;
    }

    public boolean isRestartableDebuff() {
        return _restartableDebuff;
    }

    /**
     * @return the _npcId
     */
    public int getNpcId() {
        return _npcId;
    }

    /**
     * @return the _faceId
     */
    public int getFaceId() {
        return _faceId;
    }

    /**
     * @return the _hairColorId
     */
    public int getHairColorId() {
        return _hairColorId;
    }

    /**
     * @return the _hairStyleId
     */
    public int getHairStyleId() {
        return _hairStyleId;
    }

    public boolean isMsgStatusHidden() {
        return _hideMsgStatus;
    }

    public void setHideExitStatus(boolean value) {
        _hideMsgStatus = value;
    }

    public boolean isMoveStop() {
        return _moveStop;
    }

    /**
     * Tries to consume items from target, that was affected by this skill.
     *
     * @param effector Those who launched skill.
     * @param target   Those who was affected by this skill.
     * @return True on successful consumption.
     */
    public boolean consumeTargetItems(L2PcInstance effector, L2PcInstance target) {
        int itemConsumeId = _targetConsumeId;
        int itemConsumeCount = _targetConsume;
        if (itemConsumeId != 0 && itemConsumeCount != 0) {
            if (target.getInventory().getInventoryItemCount(itemConsumeId, 0) < itemConsumeCount) {
                target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(_targetConsumeId));
                return false;
            }
            target.getInventory().destroyItemByItemId(ProcessType.CONSUME, itemConsumeId, itemConsumeCount, effector, target);
            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(_targetConsumeId));
        }
        return true;
    }
}