package dwo.gameserver.model.items;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.EnchantBonusData;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.documentengine.XmlDocumentEngine;
import dwo.gameserver.engine.documentengine.items.XmlDocumentItemClient;
import dwo.gameserver.engine.logengine.formatters.EnchantLogFormatter;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2EventMonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.MaterialType;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.util.GMAudit;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class ItemTable
{
    public static final Map<String, MaterialType> _materials = new HashMap<>();
    public static final Map<String, CrystalGrade> _crystalGrades = new HashMap<>();
    public static final Map<String, Long> _slots = new HashMap<>();
    public static final Map<String, L2WeaponType> _weaponTypes = new HashMap<>();
    public static final Map<String, L2ArmorType> _armorTypes = new HashMap<>();
    private static final Map<Integer, L2EtcItem> _etcItems = new HashMap<>();
    private static final Map<Integer, L2Armor> _armors = new HashMap<>();
    private static final Map<Integer, L2Weapon> _weapons = new HashMap<>();

    static
    {
        _materials.put("adamantaite", MaterialType.MATERIAL_ADAMANTAITE);
        _materials.put("blood_steel", MaterialType.MATERIAL_BLOOD_STEEL);
        _materials.put("bone", MaterialType.MATERIAL_BONE);
        _materials.put("bronze", MaterialType.MATERIAL_BRONZE);
        _materials.put("cloth", MaterialType.MATERIAL_CLOTH);
        _materials.put("chrysolite", MaterialType.MATERIAL_CHRYSOLITE);
        _materials.put("cobweb", MaterialType.MATERIAL_COBWEB);
        _materials.put("cotton", MaterialType.MATERIAL_FINE_STEEL);
        _materials.put("crystal", MaterialType.MATERIAL_CRYSTAL);
        _materials.put("damascus", MaterialType.MATERIAL_DAMASCUS);
        _materials.put("dyestuff", MaterialType.MATERIAL_DYESTUFF);
        _materials.put("fine_steel", MaterialType.MATERIAL_FINE_STEEL);
        _materials.put("fish", MaterialType.MATERIAL_FISH);
        _materials.put("gold", MaterialType.MATERIAL_GOLD);
        _materials.put("horn", MaterialType.MATERIAL_HORN);
        _materials.put("leather", MaterialType.MATERIAL_LEATHER);
        _materials.put("liquid", MaterialType.MATERIAL_LIQUID);
        _materials.put("mithril", MaterialType.MATERIAL_MITHRIL);
        _materials.put("oriharukon", MaterialType.MATERIAL_ORIHARUKON);
        _materials.put("paper", MaterialType.MATERIAL_PAPER);
        _materials.put("rune_xp", MaterialType.MATERIAL_RUNE_XP);
        _materials.put("rune_sp", MaterialType.MATERIAL_RUNE_SP);
        _materials.put("rune_remove_penalty", MaterialType.MATERIAL_RUNE_PENALTY);
        _materials.put("scale_of_dragon", MaterialType.MATERIAL_SCALE_OF_DRAGON);
        _materials.put("seed", MaterialType.MATERIAL_SEED);
        _materials.put("silver", MaterialType.MATERIAL_SILVER);
        _materials.put("steel", MaterialType.MATERIAL_STEEL);
        _materials.put("wood", MaterialType.MATERIAL_WOOD);

        /* Events's crystal type */
        _crystalGrades.put("olf", CrystalGrade.SPECIAL_OLF);
        _crystalGrades.put("vega", CrystalGrade.SPECIAL_VEGA);

        _crystalGrades.put("r99", CrystalGrade.R99);
        _crystalGrades.put("r95", CrystalGrade.R95);
        _crystalGrades.put("r", CrystalGrade.R);
        _crystalGrades.put("s84", CrystalGrade.S84);
        _crystalGrades.put("s80", CrystalGrade.S80);
        _crystalGrades.put("s", CrystalGrade.S);
        _crystalGrades.put("a", CrystalGrade.A);
        _crystalGrades.put("b", CrystalGrade.B);
        _crystalGrades.put("c", CrystalGrade.C);
        _crystalGrades.put("d", CrystalGrade.D);
        _crystalGrades.put("none", CrystalGrade.NONE);

        // weapon types
        for(L2WeaponType type : L2WeaponType.values())
        {
            _weaponTypes.put(type.toString(), type);
        }

        // armor types
        for(L2ArmorType type : L2ArmorType.values())
        {
            _armorTypes.put(type.toString(), type);
        }

        _slots.put("shirt", L2Item.SLOT_UNDERWEAR);
        _slots.put("lbracelet", L2Item.SLOT_L_BRACELET);
        _slots.put("rbracelet", L2Item.SLOT_R_BRACELET);
        _slots.put("talisman", L2Item.SLOT_DECO);
        _slots.put("chest", L2Item.SLOT_CHEST);
        _slots.put("fullarmor", L2Item.SLOT_FULL_ARMOR);
        _slots.put("head", L2Item.SLOT_HEAD);
        _slots.put("hair", L2Item.SLOT_HAIR);
        _slots.put("hairall", L2Item.SLOT_HAIRALL);
        _slots.put("underwear", L2Item.SLOT_UNDERWEAR);
        _slots.put("back", L2Item.SLOT_BACK);
        _slots.put("neck", L2Item.SLOT_NECK);
        _slots.put("legs", L2Item.SLOT_LEGS);
        _slots.put("feet", L2Item.SLOT_FEET);
        _slots.put("gloves", L2Item.SLOT_GLOVES);
        _slots.put("chest,legs", L2Item.SLOT_CHEST | L2Item.SLOT_LEGS);
        _slots.put("belt", L2Item.SLOT_BELT);
        _slots.put("rhand", L2Item.SLOT_R_HAND);
        _slots.put("lhand", L2Item.SLOT_L_HAND);
        _slots.put("lrhand", L2Item.SLOT_LR_HAND);
        _slots.put("rear;lear", L2Item.SLOT_R_EAR | L2Item.SLOT_L_EAR);
        _slots.put("rfinger;lfinger", L2Item.SLOT_R_FINGER | L2Item.SLOT_L_FINGER);
        _slots.put("wolf", L2Item.SLOT_WOLF);
        _slots.put("greatwolf", L2Item.SLOT_GREATWOLF);
        _slots.put("hatchling", L2Item.SLOT_HATCHLING);
        _slots.put("strider", L2Item.SLOT_STRIDER);
        _slots.put("babypet", L2Item.SLOT_BABYPET);
        _slots.put("broach", L2Item.SLOT_BROACH);
        _slots.put("stone", L2Item.SLOT_STONE);
        _slots.put("none", L2Item.SLOT_NONE);

        // retail compatibility
        _slots.put("onepiece", L2Item.SLOT_FULL_ARMOR);
        _slots.put("hair2", L2Item.SLOT_HAIR2);
        _slots.put("dhair", L2Item.SLOT_HAIRALL);
        _slots.put("alldress", L2Item.SLOT_ALLDRESS);
        _slots.put("deco1", L2Item.SLOT_DECO);
        _slots.put("waist", L2Item.SLOT_BELT);
    }

    private static Logger _log = LogManager.getLogger(ItemTable.class);
    private static Logger _logItems = LogManager.getLogger("item");
    private L2Item[] _allTemplates;

    /**
     * Constructor.
     */
    private ItemTable()
    {
    }

    public static ItemTable getInstance()
    {
        return SingletonHolder._instance;
    }

    public void loadClient()
    {
        _armors.clear();
        _etcItems.clear();
        _weapons.clear();

        List<L2Item> items = new ArrayList<>();

        XmlDocumentItemClient doc = new XmlDocumentItemClient(new File(Config.DATAPACK_ROOT, "data/stats/client/ClientWeapons.xml"));
        doc.parse();
        items.addAll(doc.getItems());

        doc = new XmlDocumentItemClient(new File(Config.DATAPACK_ROOT, "data/stats/client/ClientArmors.xml"));
        doc.parse();
        items.addAll(doc.getItems());

        doc = new XmlDocumentItemClient(new File(Config.DATAPACK_ROOT, "data/stats/client/ClientEtcItems.xml"));
        doc.parse();
        items.addAll(doc.getItems());

        int lastItemId = 0;

        for(L2Item item : items)
        {
            if(item instanceof L2EtcItem)
            {
                _etcItems.put(item.getItemId(), (L2EtcItem) item);
            }
            else if(item instanceof L2Armor)
            {
                _armors.put(item.getItemId(), (L2Armor) item);
            }
            else
            {
                _weapons.put(item.getItemId(), (L2Weapon) item);
            }
            lastItemId = Math.max(lastItemId, item.getItemId());
        }

        buildFastLookupTable(lastItemId);
    }

    public void load(boolean reload)
    {
        int highest = 0;
        _armors.clear();
        _etcItems.clear();
        _weapons.clear();
        for(L2Item item : XmlDocumentEngine.getInstance().loadItems(reload))
        {
            if(highest < item.getItemId())
            {
                highest = item.getItemId();
            }
            if(item instanceof L2EtcItem)
            {
                _etcItems.put(item.getItemId(), (L2EtcItem) item);
            }
            else if(item instanceof L2Armor)
            {
                _armors.put(item.getItemId(), (L2Armor) item);
            }
            else
            {
                _weapons.put(item.getItemId(), (L2Weapon) item);
            }
        }
        buildFastLookupTable(highest);
    }

    public void reload()
    {
        load(true);
        EnchantBonusData.getInstance();
    }

    /**
     * Builds a variable in which all items are putting in in function of their
     * ID.
     * @param size ??????
     */
    private void buildFastLookupTable(int size)
    {
        // Create a FastLookUp Table called _allTemplates of size : value of the
        // highest item ID
        _log.log(Level.INFO, "Highest item id used:" + size);
        _allTemplates = new L2Item[size + 1];

        // Insert armor item in Fast Look Up Table
        for(L2Armor item : _armors.values())
        {
            _allTemplates[item.getItemId()] = item;
        }

        // Insert weapon item in Fast Look Up Table
        for(L2Weapon item : _weapons.values())
        {
            _allTemplates[item.getItemId()] = item;
        }

        // Insert etcItem item in Fast Look Up Table
        for(L2EtcItem item : _etcItems.values())
        {
            _allTemplates[item.getItemId()] = item;
        }
    }

    /**
     * Returns the item corresponding to the item ID
     *
     * @param id : int designating the item
     * @return L2Item
     */
    public L2Item getTemplate(int id)
    {
        return id >= _allTemplates.length ? null : _allTemplates[id];
    }

    /**
     * Create the L2ItemInstance corresponding to the Item Identifier and
     * quantitiy add logs the activity.<BR>
     * <BR>
     * <B><U> Actions</U> :</B><BR>
     * <BR>
     * <li>Create and Init the L2ItemInstance corresponding to the Item
     * Identifier and quantity</li> <li>Add the L2ItemInstance object to
     * _allObjects of L2world</li> <li>Logs Item creation according to log
     * settings</li><BR>
     * <BR>
     *
     * @param process   : String Identifier of process triggering this action
     * @param itemId    : int Item Identifier of the item to be created
     * @param count     : int Quantity of items to be created for stackable items
     * @param actor     : L2PcInstance Player requesting the item creation
     * @param reference : Object Object referencing current action like NPC selling
     *                  item or previous item in transformation
     * @return L2ItemInstance corresponding to the new item
     */
    public L2ItemInstance createItem(ProcessType process, int itemId, long count, L2PcInstance actor, Object reference)
    {
        // Create and Init the L2ItemInstance corresponding to the Item
        // Identifier
        L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);

        if(process == ProcessType.LOOT)
        {
            ScheduledFuture<?> itemLootShedule;
            if(reference instanceof L2Attackable && ((L2Attackable) reference).isRaid()) // loot privilege for raids
            {
                L2Attackable raid = (L2Attackable) reference;
                // if in CommandChannel and was killing a World/RaidBoss
                if(raid.getFirstCommandChannelAttacked() != null && !Config.AUTO_LOOT_RAIDS)
                {
                    item.setOwnerId(raid.getFirstCommandChannelAttacked().getLeader().getObjectId());
                    itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), Config.LOOT_RAIDS_PRIVILEGE_INTERVAL);
                    item.setItemLootShedule(itemLootShedule);
                }
            }
            else if(!Config.AUTO_LOOT || reference instanceof L2EventMonsterInstance && ((L2EventMonsterInstance) reference).eventDropOnGround())
            {
                item.setOwnerId(actor.getObjectId());
                itemLootShedule = ThreadPoolManager.getInstance().scheduleGeneral(new ResetOwner(item), 15000);
                item.setItemLootShedule(itemLootShedule);
            }
        }

        // Add the L2ItemInstance object to _allObjects of L2world
        WorldManager.getInstance().storeObject(item);

        // Set Item parameters
        if(item.isStackable() && count > 1)
        {
            item.setCount(count);
        }

        if(Config.LOG_ITEMS && process != ProcessType.NPC)
        {
            _logItems.log(Level.INFO, EnchantLogFormatter.format("CREATE:" + process, new Object[]{
                    item, "add=" + count, actor, reference
            }));
        }

        if(actor != null)
        {
            if(actor.isGM())
            {
                String referenceName = "no-reference";
                if(reference instanceof L2Object)
                {
                    referenceName = ((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name";
                }
                else if(reference instanceof String)
                {
                    referenceName = (String) reference;
                }
                String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
                if(Config.GMAUDIT)
                {
                    GMAudit.auditGMAction(actor.getName() + " [" + actor.getObjectId() + ']', process + "(id: " + itemId + " count: " + count + " name: " + item.getItemName() + " objId: " + item.getObjectId() + ')', targetName, "L2Object referencing this action is: " + referenceName);
                }
            }
        }

        return item;
    }

    public L2ItemInstance createItem(ProcessType process, int itemId, int count, L2PcInstance actor)
    {
        return createItem(process, itemId, count, actor, null);
    }

    /**
     * Returns a dummy (fr = factice) item.<BR>
     * <BR>
     * <U><I>Concept :</I></U><BR>
     * Dummy item is created by setting the ID of the object in the world at
     * null value
     *
     * @param itemId : int designating the item
     * @return L2ItemInstance designating the dummy item created
     */
    public L2ItemInstance createDummyItem(int itemId)
    {
        L2Item item = getTemplate(itemId);
        if(item == null)
        {
            return null;
        }
        return new L2ItemInstance(0, item);
    }

    /**
     * Destroys the L2ItemInstance.<BR>
     * <BR>
     * <B><U> Actions</U> :</B><BR>
     * <BR>
     * <li>Sets L2ItemInstance parameters to be unusable</li> <li>Removes the
     * L2ItemInstance object to _allObjects of L2world</li> <li>Logs Item
     * delettion according to log settings</li><BR>
     * <BR>
     *
     * @param process   : String Identifier of process triggering this action
     * @param item      : int Item Identifier of the item to be created
     * @param actor     : L2PcInstance Player requesting the item destroy
     * @param reference : Object Object referencing current action like NPC selling
     *                  item or previous item in transformation
     */
    public void destroyItem(ProcessType process, L2ItemInstance item, L2PcInstance actor, Object reference)
    {
        synchronized(item)
        {
            item.setCount(0);
            item.setOwnerId(0);
            item.setLocation(ItemLocation.VOID);
            item.setLastChange(L2ItemInstance.REMOVED);

            WorldManager.getInstance().removeObject(item);
            IdFactory.getInstance().releaseId(item.getObjectId());

            if(Config.LOG_ITEMS)
            {
                _logItems.log(Level.INFO, EnchantLogFormatter.format("DELETE:" + process, new Object[]{
                        item, actor, reference
                }));
            }

            if(actor != null)
            {
                if(actor.isGM())
                {
                    String referenceName = "no-reference";
                    if(reference instanceof L2Object)
                    {
                        referenceName = ((L2Object) reference).getName() != null ? ((L2Object) reference).getName() : "no-name";
                    }
                    else if(reference instanceof String)
                    {
                        referenceName = (String) reference;
                    }
                    String targetName = actor.getTarget() != null ? actor.getTarget().getName() : "no-target";
                    if(Config.GMAUDIT)
                    {
                        GMAudit.auditGMAction(actor.getName() + " [" + actor.getObjectId() + ']', process + "(id: " + item.getItemId() + " count: " + item.getCount() + " itemObjId: " + item.getObjectId() + ')', targetName, "L2Object referencing this action is: " + referenceName);
                    }
                }
            }

            // if it's a pet control item, delete the pet as well
            if(PetDataTable.isPetItem(item.getItemId()))
            {
                ThreadConnection con = null;
                FiltredPreparedStatement statement = null;
                try
                {
                    // Delete the pet in db
                    con = L2DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
                    statement.setInt(1, item.getObjectId());
                    statement.execute();
                }
                catch(Exception e)
                {
                    _log.log(Level.ERROR, "could not delete pet objectid:", e);
                }
                finally
                {
                    DatabaseUtils.closeDatabaseCS(con, statement);
                }
            }
        }
    }

    public Set<Integer> getAllArmorsId()
    {
        return _armors.keySet();
    }

    public Set<Integer> getAllWeaponsId()
    {
        return _weapons.keySet();
    }

    public int getArraySize()
    {
        return _allTemplates.length;
    }

    protected static class ResetOwner implements Runnable
    {
        L2ItemInstance _item;

        public ResetOwner(L2ItemInstance item)
        {
            _item = item;
        }

        @Override
        public void run()
        {
            _item.setOwnerId(0);
            _item.setItemLootShedule(null);
        }
    }

    private static class SingletonHolder
    {
        protected static final ItemTable _instance = new ItemTable();
    }
}
