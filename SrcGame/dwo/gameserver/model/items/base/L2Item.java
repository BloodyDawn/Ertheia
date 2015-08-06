package dwo.gameserver.model.items.base;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.ItemPriceData;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.MaterialType;
import dwo.gameserver.model.items.base.proptypes.SoulshotGrade;
import dwo.gameserver.model.items.base.type.L2ActionType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.items.base.type.L2ItemType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.conditions.Condition;
import dwo.gameserver.model.skills.base.conditions.ConditionLogicOr;
import dwo.gameserver.model.skills.base.conditions.ConditionPetType;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.skills.base.funcs.FuncTemplate;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.StringUtil;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains all informations concerning the item (weapon, armor, etc).<BR>
 * Mother class of :
 * <LI>L2Armor</LI>
 * <LI>L2EtcItem</LI>
 * <LI>L2Weapon</LI>
 */

public abstract class L2Item
{
    public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
    public static final int TYPE1_SHIELD_ARMOR = 1;
    public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
    public static final int TYPE2_WEAPON = 0;
    public static final int TYPE2_SHIELD_ARMOR = 1;
    public static final int TYPE2_ACCESSORY = 2;
    public static final int TYPE2_QUEST = 3;
    public static final int TYPE2_MONEY = 4;
    public static final int TYPE2_OTHER = 5;
    public static final int WOLF = 0x1;
    public static final int HATCHLING = 0x2;
    public static final int STRIDER = 0x4;
    public static final int BABY = 0x8;
    public static final int IMPROVED_BABY = 0x10;
    public static final int GROWN_WOLF = 0x20;
    public static final int ALL_WOLF = 0x21;
    public static final int ALL_PET = 0x3F;
    public static final long SLOT_NONE = 0x0000;
    public static final long SLOT_UNDERWEAR = 0x0001;
    public static final long SLOT_R_EAR = 0x0002;
    public static final long SLOT_L_EAR = 0x0004;
    public static final long SLOT_LR_EAR = 0x00006;
    public static final long SLOT_NECK = 0x0008;
    public static final long SLOT_R_FINGER = 0x0010;
    public static final long SLOT_L_FINGER = 0x0020;
    public static final long SLOT_LR_FINGER = 0x0030;
    public static final long SLOT_HEAD = 0x0040;
    public static final long SLOT_R_HAND = 0x0080;
    public static final long SLOT_L_HAND = 0x0100;
    public static final long SLOT_GLOVES = 0x0200;
    public static final long SLOT_CHEST = 0x0400;
    public static final long SLOT_LEGS = 0x0800;
    public static final long SLOT_FEET = 0x1000;
    public static final long SLOT_BACK = 0x2000;
    public static final long SLOT_LR_HAND = 0x4000;
    public static final long SLOT_MULTI_ALLWEAPON = SLOT_LR_HAND | SLOT_R_HAND;
    public static final long SLOT_FULL_ARMOR = 0x8000;
    public static final long SLOT_HAIR = 0x010000;
    public static final long SLOT_ALLDRESS = 0x020000;
    public static final long SLOT_HAIR2 = 0x040000;
    public static final long SLOT_HAIRALL = 0x080000;
    public static final long SLOT_R_BRACELET = 0x100000;
    public static final long SLOT_L_BRACELET = 0x200000;
    public static final long SLOT_DECO = 0x400000;
    public static final long SLOT_BELT = 0x10000000;
    public static final long SLOT_BROACH = 0x20000000;
    public static final long SLOT_STONE = 0x40000000;
    public static final long SLOT_WOLF = -100;
    public static final long SLOT_HATCHLING = -101;
    public static final long SLOT_STRIDER = -102;
    public static final long SLOT_BABYPET = -103;
    public static final long SLOT_GREATWOLF = -104;
    protected static final Logger _log = LogManager.getLogger(L2Item.class);
    protected static final Func[] _emptyFunctionSet = new Func[0];
    protected static final L2Effect[] _emptyEffectSet = new L2Effect[0];
    private static final int[] crystalItemId = {
            0, 1458, 1459, 1460, 1461, 1462, 1462, 1462, 17371, 17371, 17371
    };
    private static final int[] crystalEnchantBonusArmor = {
            0, 11, 6, 11, 19, 25, 25, 25, 25, 25, 25  //тест нужно заменить
    };
    private static final int[] crystalEnchantBonusWeapon = {
            0, 90, 45, 67, 144, 250, 250, 250, 250, 250, 250 //тест нужно заменить
    };
    private final int _itemId;
    private final String _serverName;
    private final int _questId;
    private final String _name;
    private final String _icon;
    private final int _weight;
    private final boolean _stackable;
    private final MaterialType _materialType;
    private final CrystalGrade _crystalType; // default to none-grade
    private final int _equipReuseDelay;
    private final int _duration;
    private final int _time;
    private final int _autoDestroyTime;
    private final long _bodyPart;
    private final int _referencePrice;
    private final int _crystalCount;
    private final boolean _enchantable_time_limited;
    private final boolean _sellable;
    private final boolean _dropable;
    private final boolean _destroyable;
    private final boolean _tradeable;
    private final boolean _depositable;
    private final boolean _enchantable;
    private final boolean _elementable;
    private final boolean _questItem;
    private final boolean _freightable;
    private final boolean _is_oly_restricted;
    private final boolean _common;
    private final boolean _heroItem;
    private final boolean _rankItem;
    private final boolean _pvpItem;
    private final boolean _ex_immediate_effect;
    private final int _defaultEnchantLevel;
    private final L2ActionType _defaultAction;
    private final boolean _featherOfBlessing;
    private final boolean _isBlessedItem;
    private final boolean _isInfinityItem;
    private final int _useSkillDisTime;
    private final int _reuseDelay;
    protected int _type1; // needed for item list (inventory)
    protected int _type2; // different lists for armor, weapon, etc
    protected Elementals[] _elementals;
    protected FuncTemplate[] _funcTemplates;
    protected EffectTemplate[] _effectTemplates;
    protected List<Condition> _preConditions;
    private SkillHolder[] _skillHolder;
    private List<Quest> _questEvents = new FastList<>();
    private int _sharedReuseGroup;
    
    private int _compoundItem;
    private float _compoundChance;

    /**
     * Constructor of the L2Item that fill class variables.<BR><BR>
     * @param set : StatsSet corresponding to a set of couples (key,value) for description of the item
     */
    protected L2Item(StatsSet set)
    {
        _itemId = set.getInteger("item_id");
        _serverName = set.getString("server_name", "");
        _questId = set.getInteger("quest_id", 0);
        _name = set.getString("name");
        _icon = set.getString("icon", null);
        _weight = set.getInteger("weight", 0);
        _materialType = ItemTable._materials.get(set.getString("material", "steel"));
        _equipReuseDelay = set.getInteger("equip_reuse_delay", 0) * 1000;
        _duration = set.getInteger("duration", -1);
        _time = set.getInteger("time", -1);
        _autoDestroyTime = set.getInteger("auto_destroy_time", -1) * 1000;
        _bodyPart = ItemTable._slots.get(set.getString("bodypart", "none"));
        _referencePrice = (int) ItemPriceData.getInstance().getPrice(_itemId);
        _crystalType = ItemTable._crystalGrades.get(set.getString("crystal_type", "none"));
        _crystalCount = set.getInteger("crystal_count", 0);

        _stackable = set.getBool("is_stackable", false);
        _enchantable_time_limited = set.getBool("is_enchantable_tl", false);
        _sellable = set.getBool("is_sellable", true);
        _dropable = set.getBool("is_dropable", true);
        _destroyable = set.getBool("is_destroyable", true);
        _tradeable = set.getBool("is_tradable", true);
        _depositable = set.getBool("is_depositable", true);
        _elementable = set.getBool("element_enabled", false);
        _enchantable = set.getBool("enchant_enabled", false);
        _questItem = set.getBool("is_questitem", false);
        _freightable = set.getBool("is_freightable", false);
        _is_oly_restricted = set.getBool("is_oly_restricted", false);
        //_immediate_effect - herb
        _ex_immediate_effect = set.getBool("ex_immediate_effect", false);
        //used for custom type select
        _defaultAction = set.getEnum("default_action", L2ActionType.class, L2ActionType.none);
        _featherOfBlessing = set.getBool("isFeatherOfBlessing", false);
        _isBlessedItem = set.getBool("isBlessedItem", false);
        _isInfinityItem = set.getBool("is_infinity", false);
        _useSkillDisTime = set.getInteger("useSkillDisTime", 0);
        _defaultEnchantLevel = set.getInteger("enchanted", 0);
        _reuseDelay = set.getInteger("reuse_delay", 0);
        _sharedReuseGroup = set.getInteger("shared_reuse_group", 0);
        _heroItem = set.getBool("is_heroEquip", false); // для итемов являющихся героическими
        _rankItem = set.getBool("is_rankItem", false);  // для героических итемов имеющих исключение (могут одевать обычные игроки).

        _compoundItem = set.getInteger("compoundItem", 0);
        _compoundChance = set.getFloat("compoundChance", 0);
        
        //TODO cleanup + finish
        String equip_condition = set.getString("equip_condition", null);
        if(equip_condition != null)
        {
            //pet conditions
            ConditionLogicOr cond = new ConditionLogicOr();
            if(equip_condition.contains("all_wolf_group"))
            {
                cond.add(new ConditionPetType(ALL_WOLF));
            }
            if(equip_condition.contains("hatchling_group"))
            {
                cond.add(new ConditionPetType(HATCHLING));
            }
            if(equip_condition.contains("strider"))
            {
                cond.add(new ConditionPetType(STRIDER));
            }
            if(equip_condition.contains("baby_pet_group"))
            {
                cond.add(new ConditionPetType(BABY));
            }
            if(equip_condition.contains("upgrade_baby_pet_group"))
            {
                cond.add(new ConditionPetType(IMPROVED_BABY));
            }
            if(equip_condition.contains("grown_up_wolf_group"))
            {
                cond.add(new ConditionPetType(GROWN_WOLF));
            }
            if(equip_condition.contains("item_equip_pet_group"))
            {
                cond.add(new ConditionPetType(ALL_PET));
            }

            if(cond.conditions.length > 0)
            {
                attach(cond);
            }
        }

        String skills = set.getString("item_skill", null);
        if(skills != null)
        {
            String[] skillsSplit = skills.split(";");
            _skillHolder = new SkillHolder[skillsSplit.length];
            int used = 0;

            for(String aSkillsSplit : skillsSplit)
            {
                try
                {
                    String[] skillSplit = aSkillsSplit.split("-");
                    int id = Integer.parseInt(skillSplit[0]);
                    int level = Integer.parseInt(skillSplit[1]);

                    if(id == 0)
                    {
                        _log.log(Level.INFO, StringUtil.concat("Ignoring item_skill(", aSkillsSplit, ") for item ", toString(), ". Skill id is 0!"));
                        continue;
                    }

                    if(level == 0)
                    {
                        _log.log(Level.INFO, StringUtil.concat("Ignoring item_skill(", aSkillsSplit, ") for item ", toString(), ". Skill level is 0!"));
                        continue;
                    }

                    if(SkillTable.getInstance().getInfo(id, level) == null)
                    {
                        _log.log(Level.INFO, StringUtil.concat("Ignoring item_skill(", aSkillsSplit, ") for item ", toString(), ". Skill == mull!"));
                        continue;
                    }

                    if(SkillTable.getInstance().getMaxLevel(id) < level)
                    {
                        _log.log(Level.INFO, StringUtil.concat("Ignoring item_skill(", aSkillsSplit, ") for item ", toString(), ". Level higher than the limit!"));
                        continue;
                    }

                    _skillHolder[used] = new SkillHolder(id, level);
                    ++used;
                }
                catch(Exception e)
                {
                    _log.log(Level.ERROR, StringUtil.concat("Failed to parse item_skill(", aSkillsSplit, ") for item ", toString(), "! Format: SkillId0-SkillLevel0[;SkillIdN-SkillLevelN]"));
                }
            }

            // this is only loading? just don't leave a null or use a collection?
            if(used != _skillHolder.length)
            {
                SkillHolder[] skillHolder = new SkillHolder[used];
                System.arraycopy(_skillHolder, 0, skillHolder, 0, used);
                _skillHolder = skillHolder;
            }
        }

        _common = _itemId >= 11605 && _itemId <= 12361;
        _pvpItem = _itemId >= 10667 && _itemId <= 10835 || _itemId >= 12852 && _itemId <= 12977 || _itemId >= 14363 && _itemId <= 14525 || _itemId == 14528 || _itemId == 14529 || _itemId == 14558 || _itemId >= 15913 && _itemId <= 16024 || _itemId >= 16134 && _itemId <= 16147 || _itemId == 16149 || _itemId == 16151 || _itemId == 16153 || _itemId == 16155 || _itemId == 16157 || _itemId == 16159 || _itemId >= 16168 && _itemId <= 16176 || _itemId >= 16179 && _itemId <= 16220;
    }

    /**
     * @return Enum the itemType.
     */
    public abstract L2ItemType getItemType();

    /**
     * @return Enum the CommissionCategoryType.
     */
    public abstract CommissionItemHolder.CommissionCategoryType getItemCommissionType();

    /**
     * @return the duration of the item
     */
    public int getDuration()
    {
        return _duration;
    }

    /**
     * @return the time of the item
     */
    public int getTime()
    {
        return _time;
    }

    /**
     * @return the auto destroy time of the item in seconds: 0 or less - default
     */
    public int getAutoDestroyTime()
    {
        return _autoDestroyTime;
    }

    /**
     * @return the ID of the iden
     */
    public int getItemId()
    {
        return _itemId;
    }

    public abstract int getItemMask();

    /**
     * @return the type of material of the item
     */
    public MaterialType getMaterialType()
    {
        return _materialType;
    }

    /**
     * @return the _equipReuseDelay
     */
    public int getEquipReuseDelay()
    {
        return _equipReuseDelay;
    }

    /**
     * @return the type 2 of the item
     */
    public int getType2()
    {
        return _type2;
    }

    /**
     * @return вес предмета
     */
    public int getWeight()
    {
        return _weight;
    }

    /**
     * @return {@code true} if the item is crystallizable
     */
    public boolean isCrystallizable()
    {
        return _crystalType != CrystalGrade.NONE && _crystalCount > 0;
    }

    /**
     * @return the type of crystal if item is crystallizable
     */
    public CrystalGrade getCrystalType()
    {
        return _crystalType;
    }

    /**
     * @return the type of crystal if item is crystallizable
     */
    public int getCrystalItemId()
    {
        return crystalItemId[_crystalType.ordinal()];
    }

    /**
     * <U><I>Concept :</I></U><BR>
     * In fact, this fucntion returns the type of crystal of the item.
     * @return the grade of the item.
     */
    public CrystalGrade getItemGrade()
    {
        return _crystalType;
    }

    /**
     * @return группу грейда проедмета
     */
    public CrystalGrade getItemGradeSPlus()
    {
        switch(_crystalType)
        {
            case S80:
            case S84:
                return CrystalGrade.S;
            case R95:
            case R99:
                return CrystalGrade.R;
            default:
                return _crystalType;
        }
    }

    public CrystalGrade getItemGradeRPlus()
    {
        switch(_crystalType)
        {
            case NONE:
                return CrystalGrade.NONE;
            case D:
                return CrystalGrade.D;
            case C:
                return CrystalGrade.C;
            case B:
                return CrystalGrade.B;
            case A:
                return CrystalGrade.A;
            case S:
            case S80:
            case S84:
                return CrystalGrade.S;
            case R:
            case R95:
            case R99:
                return CrystalGrade.R;
            default:
                return _crystalType;
        }
    }

    /**
     * @return Soulshot грейд для текущего предмета
     */
    public SoulshotGrade getSoulshotGradeForItem()
    {
        switch(_crystalType)
        {
            case NONE:
                return SoulshotGrade.SS_NG;
            case D:
                return SoulshotGrade.SS_D;
            case C:
                return SoulshotGrade.SS_C;
            case B:
                return SoulshotGrade.SS_B;
            case A:
                return SoulshotGrade.SS_A;
            case S:
            case S80:
            case S84:
                return SoulshotGrade.SS_S;
            case R:
            case R95:
            case R99:
                return SoulshotGrade.SS_R;
        }
        return SoulshotGrade.SS_NG;
    }

    /**
     * @return количество кристаллов при кристаллизации предмета
     */
    public int getCrystalCount()
    {
        return _crystalCount;
    }

    /**
     * @return количество кристаллов при кристаллизации предмета в зависимости от уровня заточки
     */
    public int getCrystalCount(int enchantLevel)
    {
        if(enchantLevel > 3)
        {
            switch(_type2)
            {
                case TYPE2_SHIELD_ARMOR:
                case TYPE2_ACCESSORY:
                    return _crystalCount + crystalEnchantBonusArmor[_crystalType.ordinal()] * (3 * enchantLevel - 6);
                case TYPE2_WEAPON:
                    return _crystalCount + crystalEnchantBonusWeapon[_crystalType.ordinal()] * (2 * enchantLevel - 3);
                default:
                    return _crystalCount;
            }
        }
        else if(enchantLevel > 0)
        {
            switch(_type2)
            {
                case TYPE2_SHIELD_ARMOR:
                case TYPE2_ACCESSORY:
                    return _crystalCount + crystalEnchantBonusArmor[_crystalType.ordinal()] * enchantLevel;
                case TYPE2_WEAPON:
                    return _crystalCount + crystalEnchantBonusWeapon[_crystalType.ordinal()] * enchantLevel;
                default:
                    return _crystalCount;
            }
        }
        else
        {
            return _crystalCount;
        }
    }

    /**
     * @return имя предмета
     */
    public String getName()
    {
        return _name;
    }

    /**
     * @return серверное имя предмета
     */
    public String getServerName()
    {
        return _serverName;
    }

    /**
     * @return id квеста, связанного с этим предметом
     */
    public int getQuestId()
    {
        return _questId;
    }

    /**
     * @return the base elemental of the item
     */
    public Elementals[] getElementals()
    {
        return _elementals;
    }

    /**
     * Устанавливает элементал-атрибут предмету
     * @param element атрибут
     */
    public void setElementals(Elementals element)
    {
        if(_elementals == null)
        {
            _elementals = new Elementals[1];
            _elementals[0] = element;
        }
        else
        {
            Elementals elm = getElemental(element.getElement());
            if(elm != null)
            {
                elm.setValue(element.getValue());
            }
            else
            {
                elm = element;
                Elementals[] array = new Elementals[_elementals.length + 1];
                System.arraycopy(_elementals, 0, array, 0, _elementals.length);
                array[_elementals.length] = elm;
                _elementals = array;
            }
        }
    }

    public Elementals getElemental(byte attribute)
    {
        for(Elementals elm : _elementals)
        {
            if(elm.getElement() == attribute)
            {
                return elm;
            }
        }
        return null;
    }

    /**
     * @return часть тела, на которую надевается предмет
     */
    public long getBodyPart()
    {
        return _bodyPart;
    }

    /**
     * @return type1 предмета
     */
    public int getType1()
    {
        return _type1;
    }

    /**
     * @return {@code true} если предмет стопковый
     */
    public boolean isStackable()
    {
        return _stackable;
    }

    /**
     * @return {@code true} если предмет является потребительским
     */
    public boolean isConsumable()
    {
        return false;
    }

    public boolean isEquipable()
    {
        return _bodyPart != 0 && !(getItemType() instanceof L2EtcItemType);
    }

    /**
     * @return стоимость предмета
     */
    public int getReferencePrice()
    {
        return _referencePrice;
    }

    public boolean isEnchantableTimeLimited()
    {
        return _enchantable_time_limited;
    }

    /**
     * @return {@code true} если предмет можно продать в магазин
     */
    public boolean isSellable()
    {
        return _sellable;
    }

    /**
     * @return {@code true} если предмет можно выбросить
     */
    public boolean isDropable()
    {
        return _dropable;
    }

    /**
     * @return {@code true} если предмет можно уничтожить
     */
    public boolean isDestroyable()
    {
        return _destroyable;
    }

    /**
     * @return {@code true} если предмет можно поставить на продажу
     */
    public boolean isTradeable()
    {
        return _tradeable;
    }

    /**
     * @return {@code true} если предмет можно положить в личное хранилище
     */
    public boolean isDepositable()
    {
        return _depositable;
    }

    /**
     * @return {@code true} если предмет можно точить
     */
    public boolean isEnchantable()
    {
        return _enchantable || Arrays.binarySearch(Config.ENCHANT_BLACKLIST, _itemId) == 0;
    }

    /**
     * @return {@code true} если предмет можно атрибутить
     */
    public boolean isElementable()
    {
        return _elementable;
    }

    /**
     * @return {@code true} если предмет является "Обычным"
     */
    public boolean isCommon()
    {
        return _common;
    }

    /**
     * @return {@code} если предмет является геройским
     */
    public boolean isHeroItem()
    {
        return _heroItem;
    }

    /**
     * @return {@code} если геройский предмет имеет некоторые исключения
     */
    public boolean isRankItem()
    {
        return _rankItem;
    }

    /**
     * @return {@code true} если предмет является PvP вещью
     */
    public boolean isPvpItem()
    {
        return _pvpItem;
    }

    /**
     * @return {@code true} если предмет является пошеном
     */
    public boolean isPotion()
    {
        return getItemType() == L2EtcItemType.POTION;
    }

    /**
     * @return {@code true} если предмет является элексиром
     */
    public boolean isElixir()
    {
        return getItemType() == L2EtcItemType.ELIXIR;
    }

    public boolean isCrystal()
    {
        for(int id : crystalItemId)
        {
            if(id == _itemId)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param instance : L2ItemInstance pointing out the item
     * @param player : L2Character pointing out the player
     * @return Func[] : array of Func objects containing the list of functions used by the item
     */
    public Func[] getStatFuncs(L2ItemInstance instance, L2Character player)
    {
        if(_funcTemplates == null || _funcTemplates.length == 0)
        {
            return _emptyFunctionSet;
        }

        ArrayList<Func> funcs = new ArrayList<>(_funcTemplates.length);

        Env env = new Env();
        env.setPlayer(player);
        env.setTarget(player);
        env.setItem(instance);

        Func f;

        for(FuncTemplate t : _funcTemplates)
        {
            f = t.getFunc(env, this); // skill is owner
            if(f != null)
            {
                funcs.add(f);
            }
        }

        if(funcs.isEmpty())
        {
            return _emptyFunctionSet;
        }

        return funcs.toArray(new Func[funcs.size()]);
    }

    /**
     * Returns the effects associated with the item.
     * @param instance : L2ItemInstance pointing out the item
     * @param player : L2Character pointing out the player
     * @return L2Effect[] : array of effects generated by the item
     */
    public L2Effect[] getEffects(L2ItemInstance instance, L2Character player)
    {
        if(_effectTemplates == null || _effectTemplates.length == 0)
        {
            return _emptyEffectSet;
        }

        FastList<L2Effect> effects = FastList.newInstance();

        Env env = new Env();
        env.setPlayer(player);
        env.setTarget(player);
        env.setItem(instance);

        L2Effect e;

        for(EffectTemplate et : _effectTemplates)
        {

            e = et.getEffect(env);
            if(e != null)
            {
                e.scheduleEffect();
                effects.add(e);
            }
        }

        if(effects.isEmpty())
        {
            return _emptyEffectSet;
        }

        L2Effect[] result = effects.toArray(new L2Effect[effects.size()]);
        FastList.recycle(effects);
        return result;
    }

    /**
     * Add the FuncTemplate f to the list of functions used with the item
     * @param f : FuncTemplate to add
     */
    public void attach(FuncTemplate f)
    {
        switch(f.stat)
        {
            case FIRE_RES:
            case FIRE_POWER:
                setElementals(new Elementals(Elementals.FIRE, (int) f.lambda.calc(null)));
                break;
            case WATER_RES:
            case WATER_POWER:
                setElementals(new Elementals(Elementals.WATER, (int) f.lambda.calc(null)));
                break;
            case WIND_RES:
            case WIND_POWER:
                setElementals(new Elementals(Elementals.WIND, (int) f.lambda.calc(null)));
                break;
            case EARTH_RES:
            case EARTH_POWER:
                setElementals(new Elementals(Elementals.EARTH, (int) f.lambda.calc(null)));
                break;
            case HOLY_RES:
            case HOLY_POWER:
                setElementals(new Elementals(Elementals.HOLY, (int) f.lambda.calc(null)));
                break;
            case DARK_RES:
            case DARK_POWER:
                setElementals(new Elementals(Elementals.DARK, (int) f.lambda.calc(null)));
                break;
        }
        // If _functTemplates is empty, create it and add the FuncTemplate f in it
        if(_funcTemplates == null)
        {
            _funcTemplates = new FuncTemplate[]{
                    f
            };
        }
        else
        {
            int len = _funcTemplates.length;
            FuncTemplate[] tmp = new FuncTemplate[len + 1];
            // Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
            //						  number of components to be copied)
            System.arraycopy(_funcTemplates, 0, tmp, 0, len);
            tmp[len] = f;
            _funcTemplates = tmp;
        }
    }

    /**
     * Add the EffectTemplate effect to the list of effects generated by the item
     * @param effect : EffectTemplate
     */
    public void attach(EffectTemplate effect)
    {
        if(_effectTemplates == null)
        {
            _effectTemplates = new EffectTemplate[]{
                    effect
            };
        }
        else
        {
            int len = _effectTemplates.length;
            EffectTemplate[] tmp = new EffectTemplate[len + 1];
            // Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
            //						  number of components to be copied)
            System.arraycopy(_effectTemplates, 0, tmp, 0, len);
            tmp[len] = effect;
            _effectTemplates = tmp;
        }
    }

    public void attach(Condition c)
    {
        if(_preConditions == null)
        {
            _preConditions = new FastList<>();
        }
        if(!_preConditions.contains(c))
        {
            _preConditions.add(c);
        }
    }

    public boolean hasSkills()
    {
        return _skillHolder != null;
    }

    /**
     * Method to retrive skills linked to this item
     *
     * armor and weapon: passive skills
     * etcitem: skills used on item use <-- ???
     *
     * @return Skills linked to this item as SkillHolder[]
     */
    public SkillHolder[] getSkills()
    {
        return _skillHolder;
    }

    public boolean checkCondition(L2ItemInstance equippedItem, L2Character activeChar, L2Object target, boolean sendMessage)
    {
        if(activeChar.isGM() && !Config.GM_ITEM_RESTRICTION)
        {
            return true;
        }

        // Don't allow hero equipment and restricted items during Olympiad
        if((isOlyRestrictedItem() || _heroItem) && activeChar instanceof L2PcInstance && activeChar.getActingPlayer().getOlympiadController().isParticipating())
        {
            if(isEquipable())
            {
                activeChar.sendPacket(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT);
            }
            else
            {
                activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            }
            return false;
        }
        if(!isConditionAttached())
        {
            return true;
        }

        Env env = new Env();
        env.setPlayer(activeChar);
        env.setItem(equippedItem);
        if(target instanceof L2Character)
        {
            env.setTarget((L2Character) target);
        }

        for(Condition preCondition : _preConditions)
        {
            if(preCondition == null)
            {
                continue;
            }

            if(!preCondition.test(env))
            {
                if(activeChar instanceof L2Summon)
                {
                    activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
                    return false;
                }

                if(sendMessage)
                {
                    String msg = preCondition.getMessage();
                    int msgId = preCondition.getMessageId();
                    if(msg != null)
                    {
                        activeChar.sendMessage(msg);
                    }
                    else if(msgId != 0)
                    {
                        SystemMessage sm = SystemMessage.getSystemMessage(msgId);
                        if(preCondition.isAddName())
                        {
                            sm.addItemName(_itemId);
                        }
                        activeChar.sendPacket(sm);
                    }
                }
                return false;
            }
        }
        return true;
    }

    /**
     * @return {@code true} если предмет имеет условия использования
     */
    public boolean isConditionAttached()
    {
        return _preConditions != null && !_preConditions.isEmpty();
    }

    /**
     * @return {@code true} если предмет является квестовым
     */
    public boolean isQuestItem()
    {
        return _questItem;
    }

    /**
     * @return {@code true} если можно передавать предмет между персонажами на аккаунте
     */
    public boolean isFreightable()
    {
        return _freightable;
    }

    /**
     * @return {@code true} если предмет запрещен на Олимпиаде
     */
    public boolean isOlyRestrictedItem()
    {
        return _is_oly_restricted || Config.LIST_OLY_RESTRICTED_ITEMS.contains(_itemId);
    }

    /**
     * @return имя предмета
     */
    @Override
    public String toString()
    {
        return _name + '(' + _itemId + ')';
    }

    /**
     * @return the _ex_immediate_effect
     */
    public boolean is_ex_immediate_effect()
    {
        return _ex_immediate_effect;
    }

    /**
     * @return the _default_action
     */
    public L2ActionType getDefaultAction()
    {
        return _defaultAction;
    }

    public int useSkillDisTime()
    {
        return _useSkillDisTime;
    }

    /**
     * @return the Reuse Delay of item.
     */
    public int getReuseDelay()
    {
        return _reuseDelay;
    }

    /**
     * @return the shared reuse group.
     */
    public int getSharedReuseGroup()
    {
        return _sharedReuseGroup;
    }

    /**
     * Get the icon link in client files.<BR> Usable in HTML windows.
     * @return the _icon
     */
    public String getIcon()
    {
        return _icon;
    }

    public void addQuestEvent(Quest q)
    {
        _questEvents.add(q);
    }

    public List<Quest> getQuestEvents()
    {
        return _questEvents;
    }

    public int getDefaultEnchantLevel()
    {
        return _defaultEnchantLevel;
    }

    /**
     * @return {@code true} if the item is Feather of Blessing
     */
    public boolean isFeatherOfBlessing()
    {
        return _featherOfBlessing;
    }

    /**
     * @return {@code true} если предмет является благословенным
     */
    public boolean isBlessedItem()
    {
        return _isBlessedItem;
    }

    /**
     * @return {@code true} если предмет является бесконечным (например новые колчаны стрел)
     */
    public boolean isInfinityItem()
    {
        return _isInfinityItem;
    }

    public int getCompoundItem()
    {
        return _compoundItem;
    }

    public float getCompoundChance()
    {
        return _compoundChance;
    }

    public int getId()
    {
        return _itemId;
    }
}
