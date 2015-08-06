package dwo.gameserver.model.items.itemcontainer;

import dwo.gameserver.datatables.xml.ArmorSetsTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.EnchantEffectTable;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.L2ArmorSet;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.serverpackets.SkillCoolTime;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoEquipSlot;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages inventory
 *
 * @version $Revision: 1.13.2.9.2.12 $ $Date: 2005/03/29 23:15:15 $
 *          rewritten 23.2.2006 by Advi
 */
public abstract class Inventory extends ItemContainer
{
    @PaperdollSlot
    public static final int PAPERDOLL_UNDER = 0;
    @PaperdollSlot
    public static final int PAPERDOLL_HEAD = 1;
    @PaperdollSlot
    public static final int PAPERDOLL_HAIR = 2;
    @PaperdollSlot
    public static final int PAPERDOLL_HAIR2 = 3;
    @PaperdollSlot
    public static final int PAPERDOLL_NECK = 4;
    @PaperdollSlot
    public static final int PAPERDOLL_RHAND = 5;
    @PaperdollSlot
    public static final int PAPERDOLL_CHEST = 6;
    @PaperdollSlot
    public static final int PAPERDOLL_LHAND = 7;
    @PaperdollSlot
    public static final int PAPERDOLL_REAR = 8;
    @PaperdollSlot
    public static final int PAPERDOLL_LEAR = 9;
    @PaperdollSlot
    public static final int PAPERDOLL_GLOVES = 10;
    @PaperdollSlot
    public static final int PAPERDOLL_LEGS = 11;
    @PaperdollSlot
    public static final int PAPERDOLL_FEET = 12;
    @PaperdollSlot
    public static final int PAPERDOLL_RFINGER = 13;
    @PaperdollSlot
    public static final int PAPERDOLL_LFINGER = 14;
    @PaperdollSlot
    public static final int PAPERDOLL_LBRACELET = 15;
    @PaperdollSlot
    public static final int PAPERDOLL_RBRACELET = 16;
    @PaperdollSlot
    public static final int PAPERDOLL_DECO1 = 17;
    @PaperdollSlot
    public static final int PAPERDOLL_DECO2 = 18;
    @PaperdollSlot
    public static final int PAPERDOLL_DECO3 = 19;
    @PaperdollSlot
    public static final int PAPERDOLL_DECO4 = 20;
    @PaperdollSlot
    public static final int PAPERDOLL_DECO5 = 21;
    @PaperdollSlot
    public static final int PAPERDOLL_DECO6 = 22;
    @PaperdollSlot
    public static final int PAPERDOLL_CLOAK = 23;
    @PaperdollSlot
    public static final int PAPERDOLL_BELT = 24;
    @PaperdollSlot
    public static final int PAPERDOLL_BROACH = 25;
    @PaperdollSlot
    public static final int PAPERDOLL_STONE1 = 26;
    @PaperdollSlot
    public static final int PAPERDOLL_STONE2 = 27;
    @PaperdollSlot
    public static final int PAPERDOLL_STONE3 = 28;
    @PaperdollSlot
    public static final int PAPERDOLL_STONE4 = 29;
    @PaperdollSlot
    public static final int PAPERDOLL_STONE5 = 30;
    @PaperdollSlot
    public static final int PAPERDOLL_STONE6 = 31;

    public static final int PAPERDOLL_TOTALSLOTS = 32;
    //Speed percentage mods
    public static final double MAX_ARMOR_WEIGHT = 12000;
    private static final int[][] arrows = {{17, 32249}, // NG
            {1341, 22067, 32250}, // D
            {1342, 22068, 32251}, // C
            {1343, 22069, 32252}, // B
            {1344, 22070, 32253}, // A
            {1345, 22071, 32254}, // S
            {18550, 32255}, // R
    };
    private static final int[][] bolts = {{9632, 32256}, // NG
            {9633, 22144, 32257}, // D
            {9634, 22145, 32258}, // C
            {9635, 22146, 32259}, // B
            {9636, 22147, 32260}, // A
            {9637, 22148, 32261}, // S
            {19443, 32262}, // R
    };
    private final L2ItemInstance[] _paperdoll;
    private final List<PaperdollListener> _paperdollListeners;

    // protected to be accessed from child classes only
    protected int _totalWeight;

    // used to quickly check for using of items of special type
    private int _wearedMask;

    // Recorder of alterations in inventory

    /**
     * Constructor of the inventory
     */
    protected Inventory()
    {
        _paperdoll = new L2ItemInstance[PAPERDOLL_TOTALSLOTS];
        _paperdollListeners = new ArrayList<>();

        if(this instanceof PcInventory)
        {
            addPaperdollListener(ArmorSetListener.getInstance());
            addPaperdollListener(BowCrossRodListener.getInstance());
            addPaperdollListener(ItemSkillsListener.getInstance());
            addPaperdollListener(BraceletListener.getInstance());
            addPaperdollListener(BroachListener.getInstance());
        }

        addPaperdollListener(StatsListener.getInstance());
    }

    public static int getPaperdollIndex(long slot)
    {
        if(slot == L2Item.SLOT_UNDERWEAR)
        {
            return PAPERDOLL_UNDER;
        }
        else if(slot == L2Item.SLOT_R_EAR)
        {
            return PAPERDOLL_REAR;
        }
        else if(slot == L2Item.SLOT_LR_EAR || slot == L2Item.SLOT_L_EAR)
        {
            return PAPERDOLL_LEAR;
        }
        else if(slot == L2Item.SLOT_NECK)
        {
            return PAPERDOLL_NECK;
        }
        else if(slot == L2Item.SLOT_R_FINGER || slot == L2Item.SLOT_LR_FINGER)
        {
            return PAPERDOLL_RFINGER;
        }
        else if(slot == L2Item.SLOT_L_FINGER)
        {
            return PAPERDOLL_LFINGER;
        }
        else if(slot == L2Item.SLOT_HEAD)
        {
            return PAPERDOLL_HEAD;
        }
        else if(slot == L2Item.SLOT_R_HAND || slot == L2Item.SLOT_LR_HAND)
        {
            return PAPERDOLL_RHAND;
        }
        else if(slot == L2Item.SLOT_L_HAND)
        {
            return PAPERDOLL_LHAND;
        }
        else if(slot == L2Item.SLOT_GLOVES)
        {
            return PAPERDOLL_GLOVES;
        }
        else if(slot == L2Item.SLOT_CHEST || slot == L2Item.SLOT_FULL_ARMOR || slot == L2Item.SLOT_ALLDRESS)
        {
            return PAPERDOLL_CHEST;
        }
        else if(slot == L2Item.SLOT_LEGS)
        {
            return PAPERDOLL_LEGS;
        }
        else if(slot == L2Item.SLOT_FEET)
        {
            return PAPERDOLL_FEET;
        }
        else if(slot == L2Item.SLOT_BACK)
        {
            return PAPERDOLL_CLOAK;
        }
        else if(slot == L2Item.SLOT_HAIR || slot == L2Item.SLOT_HAIRALL)
        {
            return PAPERDOLL_HAIR;
        }
        else if(slot == L2Item.SLOT_HAIR2)
        {
            return PAPERDOLL_HAIR2;
        }
        else if(slot == L2Item.SLOT_R_BRACELET)
        {
            return PAPERDOLL_RBRACELET;
        }
        else if(slot == L2Item.SLOT_L_BRACELET)
        {
            return PAPERDOLL_LBRACELET;
        }
        else if(slot == L2Item.SLOT_DECO)
        {
            return PAPERDOLL_DECO1; //return first we deal with it later
        }
        else if(slot == L2Item.SLOT_BELT)
        {
            return PAPERDOLL_BELT;
        }
        else if(slot == L2Item.SLOT_BROACH)
        {
            return PAPERDOLL_BROACH;
        }
        return -1;
    }

    protected abstract ItemLocation getEquipLocation();

    /**
     * Returns the instance of new ChangeRecorder
     *
     * @return ChangeRecorder
     */
    public ChangeRecorder newRecorder()
    {
        return new ChangeRecorder(this);
    }

    /**
     * Drop item from inventory and updates database
     *
     * @param process   : String Identifier of process triggering this action
     * @param item      : L2ItemInstance to be dropped
     * @param actor     : L2PcInstance Player requesting the item drop
     * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
     * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    public L2ItemInstance dropItem(ProcessType process, L2ItemInstance item, L2PcInstance actor, Object reference)
    {
        if(item == null)
        {
            return null;
        }

        synchronized(item)
        {
            if(!_items.contains(item))
            {
                return null;
            }

            removeItem(item);
            item.setOwnerId(process, 0, actor, reference);
            item.setLocation(ItemLocation.VOID);
            item.setLastChange(L2ItemInstance.REMOVED);

            HookManager.getInstance().notifyEvent(HookType.ON_INVENTORY_DELETE, null, getBaseLocation(), item, getOwner());

            item.updateDatabase();
            refreshWeight();
        }
        return item;
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and updates database
     *
     * @param process   : String Identifier of process triggering this action
     * @param objectId  : int Item Instance identifier of the item to be dropped
     * @param count     : int Quantity of items to be dropped
     * @param actor     : L2PcInstance Player requesting the item drop
     * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
     * @return L2ItemInstance corresponding to the destroyed item or the updated item in inventory
     */
    public L2ItemInstance dropItem(ProcessType process, int objectId, long count, L2PcInstance actor, Object reference)
    {
        L2ItemInstance item = getItemByObjectId(objectId);
        if(item == null)
        {
            return null;
        }

        synchronized(item)
        {
            if(!_items.contains(item))
            {
                return null;
            }

            // Adjust item quantity and create new instance to drop
            // Directly drop entire item
            if(item.getCount() > count)
            {
                item.changeCount(process, -count, actor, reference);
                item.setLastChange(L2ItemInstance.MODIFIED);
                item.updateDatabase();

                HookManager.getInstance().notifyEvent(HookType.ON_INVENTORY_CHANGE, null, getBaseLocation(), item, -count, getOwner());

                item = ItemTable.getInstance().createItem(process, item.getItemId(), count, actor, reference);
                item.updateDatabase();
                refreshWeight();
                return item;
            }
        }
        return dropItem(process, item, actor, reference);
    }

    /**
     * Adds item to inventory for further adjustments and Equip it if necessary (itemlocation defined)<BR><BR>
     *
     * @param item : L2ItemInstance to be added from inventory
     */
    @Override
    protected void addItem(L2ItemInstance item)
    {
        super.addItem(item);
        if(item.isEquipped())
        {
            equipItem(item);
        }
    }

    /**
     * Removes item from inventory for further adjustments.
     *
     * @param item : L2ItemInstance to be removed from inventory
     */
    @Override
    protected boolean removeItem(L2ItemInstance item)
    {
        // Unequip item if equiped
        for(int i = 0; i < _paperdoll.length; i++)
        {
            if(_paperdoll[i] == item)
            {
                unEquipItemInSlot(i);
            }
        }
        return super.removeItem(item);
    }

    /**
     * Refresh the weight of equipment loaded
     */
    @Override
    protected void refreshWeight()
    {
        long weight = 0;

        for(L2ItemInstance item : _items)
        {
            if(item != null && item.getItem() != null)
            {
                weight += item.getItem().getWeight() * item.getCount();
            }
        }
        _totalWeight = (int) Math.min(weight, Integer.MAX_VALUE);
    }

    /**
     * Get back items in inventory from database
     */
    @Override
    public void restore()
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        ResultSet inv = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time, skin_id  FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data");
            statement.setInt(1, getOwnerId());
            statement.setString(2, getBaseLocation().name());
            statement.setString(3, getEquipLocation().name());
            inv = statement.executeQuery();

            L2ItemInstance item;
            while(inv.next())
            {
                item = L2ItemInstance.restoreFromDb(getOwnerId(), inv);

                if(item == null)
                {
                    continue;
                }

                if(getOwner() instanceof L2PcInstance)
                {
                    L2PcInstance player = (L2PcInstance) getOwner();

                    if(!player.isGM() && !player.getOlympiadController().isHero() && item.isHeroItem())
                    {
                        item.setLocation(ItemLocation.INVENTORY);
                    }
                }

                WorldManager.getInstance().storeObject(item);

                // If stackable item is found in inventory just add to current quantity
                if(item.isStackable() && getItemByItemId(item.getItemId()) != null)
                {
                    addItem(ProcessType.RESTORE, item, getOwner().getActingPlayer(), null);
                }
                else
                {
                    addItem(item);
                    //if (getOwner() instanceof L2PcInstance)
                    //	HookManager.getInstance().notifyEvent(HookType.ON_INVENTORY_ADD, null, getBaseLocation(), item, getOwner());
                }
            }
            refreshWeight();
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not restore inventory: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCSR(con, statement, inv);
        }
    }

    /**
     * Returns the item in the paperdoll slot
     *
     * @return L2ItemInstance
     */
    public L2ItemInstance getPaperdollItem(int slot)
    {
        return _paperdoll[slot];
    }

    /**
     * Returns the item in the paperdoll L2Item slot
     *
     * @param slot
     * @return L2ItemInstance
     */
    public L2ItemInstance getPaperdollItemByL2ItemId(int slot)
    {
        int index = getPaperdollIndex(slot);
        if(index == -1)
        {
            return null;
        }
        return _paperdoll[index];
    }

    public int getFullArmorEnchant()
    {
        int enchant = 0;
        enchant += getPaperdollItemEnchant(PAPERDOLL_GLOVES);
        enchant += getPaperdollItemEnchant(PAPERDOLL_CHEST);
        enchant += getPaperdollItemEnchant(PAPERDOLL_LEGS);
        enchant += getPaperdollItemEnchant(PAPERDOLL_FEET);
        enchant += getPaperdollItemEnchant(PAPERDOLL_HEAD);
        if(enchant >= 40)
        {
            return 8;
        }
        if(enchant >= 30)
        {
            return 6;
        }

        return 0;
    }

    public int getPaperdollItemSkinByItemId(int slot)
    {
        L2ItemInstance item = _paperdoll[slot];
        if(item != null)
        {
            return item.getSkin() != 0 ? item.getSkin() : item.getItemId();
        }
        return 0;
    }

    public int getPaperdollItemEnchant(int slot)
    {
        L2ItemInstance item = _paperdoll[slot];
        if(item != null)
        {
            return item.getEnchantLevel();
        }
        return 0;
    }

    /**
     * Returns the ID of the item in the paperdol slot
     *
     * @param slot : int designating the slot
     * @return int designating the ID of the item
     */
    public int getPaperdollItemId(int slot)
    {
        L2ItemInstance item = _paperdoll[slot];
        if(item != null)
        {
            return item.getItemId();
        }
        return 0;
    }

    public int getPaperdollAugmentationId(int slot)
    {
        L2ItemInstance item = _paperdoll[slot];
        if(item != null && item.getAugmentation() != null)
        {
            return item.getAugmentation().getAugmentationId();
        }
        return 0;
    }

    /**
     * Returns the objectID associated to the item in the paperdoll slot
     *
     * @param slot : int pointing out the slot
     * @return int designating the objectID
     */
    public int getPaperdollObjectId(int slot)
    {
        L2ItemInstance item = _paperdoll[slot];
        if(item != null)
        {
            return item.getObjectId();
        }
        return 0;
    }

    /**
     * Adds new inventory's paperdoll listener
     * @param listener PaperdollListener pointing out the listener
     */
    public void addPaperdollListener(PaperdollListener listener)
    {
        synchronized(this)
        {
            assert !_paperdollListeners.contains(listener);
            _paperdollListeners.add(listener);
        }
    }

    /**
     * Removes a paperdoll listener
     * @param listener PaperdollListener pointing out the listener to be deleted
     */
    public void removePaperdollListener(PaperdollListener listener)
    {
        synchronized(this)
        {
            _paperdollListeners.remove(listener);
        }
    }

    /**
     * Equips an item in the given slot of the paperdoll.
     * <U><I>Remark :</I></U> The item <B>HAS TO BE</B> already in the inventory
     *
     * @param slot : int pointing out the slot of the paperdoll
     * @param item : L2ItemInstance pointing out the item to add in slot
     * @return L2ItemInstance designating the item placed in the slot before
     */
    public synchronized L2ItemInstance setPaperdollItem(long slot, L2ItemInstance item)
    {
        L2ItemInstance old = _paperdoll[((int) slot)];
        if(old != item)
        {
            if(old != null)
            {
                _paperdoll[((int) slot)] = null;
                // Put old item from paperdoll slot to base location
                old.setLocation(getBaseLocation());
                old.setLastChange(L2ItemInstance.MODIFIED);
                // Get the mask for paperdoll
                int mask = 0;
                for(int i = 0; i < PAPERDOLL_TOTALSLOTS; i++)
                {
                    L2ItemInstance pi = _paperdoll[i];
                    if(pi != null)
                    {
                        mask |= pi.getItem().getItemMask();
                    }
                }
                _wearedMask = mask;
                // Notify all paperdoll listener in order to unequip old item in slot
                for(PaperdollListener listener : _paperdollListeners)
                {
                    if(listener == null)
                    {
                        continue;
                    }

                    listener.notifyUnequiped(slot, old, this);
                }
                old.updateDatabase();
                if (getOwner().isPlayer())
                {
                    getOwner().sendPacket(new ExUserInfoEquipSlot(getOwner().getActingPlayer()));
                }
            }
            // Add new item in slot of paperdoll
            if(item != null)
            {
                _paperdoll[((int) slot)] = item;
                item.setLocation(getEquipLocation(), slot);
                item.setLastChange(L2ItemInstance.MODIFIED);
                _wearedMask |= item.getItem().getItemMask();
                for(PaperdollListener listener : _paperdollListeners)
                {
                    if(listener == null)
                    {
                        continue;
                    }

                    listener.notifyEquiped(slot, item, this);
                }
                item.updateDatabase();
                if (getOwner().isPlayer())
                {
                    getOwner().sendPacket(new ExUserInfoEquipSlot(getOwner().getActingPlayer()));
                }
            }
        }
        return old;
    }

    /**
     * Return the mask of weared item
     *
     * @return int
     */
    public int getWearedMask()
    {
        return _wearedMask;
    }

    public long getSlotFromItem(L2ItemInstance item)
    {
        long slot = -1;
        long location = item.getLocationSlot();

        if(location == PAPERDOLL_UNDER)
        {
            slot = L2Item.SLOT_UNDERWEAR;

        }
        else if(location == PAPERDOLL_LEAR)
        {
            slot = L2Item.SLOT_L_EAR;

        }
        else if(location == PAPERDOLL_REAR)
        {
            slot = L2Item.SLOT_R_EAR;

        }
        else if(location == PAPERDOLL_NECK)
        {
            slot = L2Item.SLOT_NECK;

        }
        else if(location == PAPERDOLL_RFINGER)
        {
            slot = L2Item.SLOT_R_FINGER;

        }
        else if(location == PAPERDOLL_LFINGER)
        {
            slot = L2Item.SLOT_L_FINGER;

        }
        else if(location == PAPERDOLL_HAIR)
        {
            slot = L2Item.SLOT_HAIR;

        }
        else if(location == PAPERDOLL_HAIR2)
        {
            slot = L2Item.SLOT_HAIR2;

        }
        else if(location == PAPERDOLL_HEAD)
        {
            slot = L2Item.SLOT_HEAD;

        }
        else if(location == PAPERDOLL_RHAND)
        {
            slot = L2Item.SLOT_R_HAND;

        }
        else if(location == PAPERDOLL_LHAND)
        {
            slot = L2Item.SLOT_L_HAND;

        }
        else if(location == PAPERDOLL_GLOVES)
        {
            slot = L2Item.SLOT_GLOVES;

        }
        else if(location == PAPERDOLL_CHEST)
        {
            slot = item.getItem().getBodyPart();

        }
        else if(location == PAPERDOLL_LEGS)
        {
            slot = L2Item.SLOT_LEGS;

        }
        else if(location == PAPERDOLL_CLOAK)
        {
            slot = L2Item.SLOT_BACK;

        }
        else if(location == PAPERDOLL_FEET)
        {
            slot = L2Item.SLOT_FEET;

        }
        else if(location == PAPERDOLL_LBRACELET)
        {
            slot = L2Item.SLOT_L_BRACELET;

        }
        else if(location == PAPERDOLL_RBRACELET)
        {
            slot = L2Item.SLOT_R_BRACELET;

        }
        else if(location == PAPERDOLL_DECO1 || location == PAPERDOLL_DECO2 || location == PAPERDOLL_DECO3 || location == PAPERDOLL_DECO4 || location == PAPERDOLL_DECO5 || location == PAPERDOLL_DECO6)
        {
            slot = L2Item.SLOT_DECO;

        }
        else if(location == PAPERDOLL_BELT)
        {
            slot = L2Item.SLOT_BELT;

        }
        else if(location == PAPERDOLL_BROACH)
        {
            slot = L2Item.SLOT_BROACH;

        }
        else if(location == PAPERDOLL_STONE1 || location == PAPERDOLL_STONE2 || location == PAPERDOLL_STONE3 || location == PAPERDOLL_STONE4 || location == PAPERDOLL_STONE5 || location == PAPERDOLL_STONE6)
        {
            slot = L2Item.SLOT_STONE;

        }
        return slot;
    }

    /**
     * Unequips item in body slot and returns alterations.<BR>
     * <B>If you dont need return value use {@link Inventory#unEquipItemInBodySlot(long)} instead</B>
     *
     * @param slot : int designating the slot of the paperdoll
     * @return L2ItemInstance[] : list of changes
     */
    public L2ItemInstance[] unEquipItemInBodySlotAndRecord(long slot)
    {
        ChangeRecorder recorder = newRecorder();

        try
        {
            unEquipItemInBodySlot(slot);
        }
        finally
        {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    /**
     * Sets item in slot of the paperdoll to null value
     *
     * @param pdollSlot : int designating the slot
     * @return L2ItemInstance designating the item in slot before change
     */
    public L2ItemInstance unEquipItemInSlot(long pdollSlot)
    {
        return setPaperdollItem(pdollSlot, null);
    }

    /**
     * Unepquips item in slot and returns alterations<BR>
     * <B>If you dont need return value use {@link Inventory#unEquipItemInSlot(long)} instead</B>
     *
     * @param slot : int designating the slot
     * @return L2ItemInstance[] : list of items altered
     */
    public L2ItemInstance[] unEquipItemInSlotAndRecord(long slot)
    {
        ChangeRecorder recorder = newRecorder();

        try
        {
            unEquipItemInSlot(slot);
            if(getOwner() instanceof L2PcInstance)
            {
                ((L2PcInstance) getOwner()).refreshExpertisePenalty();
            }
        }
        finally
        {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    /**
     * Unequips item in slot (i.e. equips with default value)
     *
     * @param slot : int designating the slot
     * @return {@link L2ItemInstance} designating the item placed in the slot
     */
    public L2ItemInstance unEquipItemInBodySlot(long slot)
    {
        long pdollSlot = -1;

        if(slot == L2Item.SLOT_L_EAR)
        {
            pdollSlot = PAPERDOLL_LEAR;

        }
        else if(slot == L2Item.SLOT_R_EAR)
        {
            pdollSlot = PAPERDOLL_REAR;

        }
        else if(slot == L2Item.SLOT_NECK)
        {
            pdollSlot = PAPERDOLL_NECK;

        }
        else if(slot == L2Item.SLOT_R_FINGER)
        {
            pdollSlot = PAPERDOLL_RFINGER;

        }
        else if(slot == L2Item.SLOT_L_FINGER)
        {
            pdollSlot = PAPERDOLL_LFINGER;

        }
        else if(slot == L2Item.SLOT_HAIR)
        {
            pdollSlot = PAPERDOLL_HAIR;

        }
        else if(slot == L2Item.SLOT_HAIR2)
        {
            pdollSlot = PAPERDOLL_HAIR2;

        }
        else if(slot == L2Item.SLOT_HAIRALL)
        {
            setPaperdollItem(PAPERDOLL_HAIR, null);
            pdollSlot = PAPERDOLL_HAIR;

        }
        else if(slot == L2Item.SLOT_HEAD)
        {
            pdollSlot = PAPERDOLL_HEAD;

        }
        else if(slot == L2Item.SLOT_R_HAND || slot == L2Item.SLOT_LR_HAND)
        {
            pdollSlot = PAPERDOLL_RHAND;

        }
        else if(slot == L2Item.SLOT_L_HAND)
        {
            pdollSlot = PAPERDOLL_LHAND;

        }
        else if(slot == L2Item.SLOT_GLOVES)
        {
            pdollSlot = PAPERDOLL_GLOVES;

        }
        else if(slot == L2Item.SLOT_CHEST || slot == L2Item.SLOT_ALLDRESS || slot == L2Item.SLOT_FULL_ARMOR)
        {
            pdollSlot = PAPERDOLL_CHEST;

        }
        else if(slot == L2Item.SLOT_LEGS)
        {
            pdollSlot = PAPERDOLL_LEGS;

        }
        else if(slot == L2Item.SLOT_BACK)
        {
            pdollSlot = PAPERDOLL_CLOAK;

        }
        else if(slot == L2Item.SLOT_FEET)
        {
            pdollSlot = PAPERDOLL_FEET;

        }
        else if(slot == L2Item.SLOT_UNDERWEAR)
        {
            pdollSlot = PAPERDOLL_UNDER;

        }
        else if(slot == L2Item.SLOT_L_BRACELET)
        {
            pdollSlot = PAPERDOLL_LBRACELET;

        }
        else if(slot == L2Item.SLOT_R_BRACELET)
        {
            pdollSlot = PAPERDOLL_RBRACELET;

        }
        else if(slot == L2Item.SLOT_DECO)
        {
            pdollSlot = PAPERDOLL_DECO1;

        }
        else if(slot == L2Item.SLOT_BELT)
        {
            pdollSlot = PAPERDOLL_BELT;

        }
        else if(slot == L2Item.SLOT_BROACH)
        {
            pdollSlot = PAPERDOLL_BROACH;

        }
        else if(slot == L2Item.SLOT_STONE)
        {
            pdollSlot = PAPERDOLL_STONE1;

        }
        else
        {
            _log.log(Level.INFO, "Unhandled slot type: " + slot);
            _log.log(Level.INFO, StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
        }
        if(pdollSlot >= 0)
        {
            L2ItemInstance old = setPaperdollItem(pdollSlot, null);
            if(old != null)
            {
                if(getOwner() instanceof L2PcInstance)
                {
                    ((L2PcInstance) getOwner()).refreshExpertisePenalty();
                }
            }
            return old;
        }
        return null;
    }

    /**
     * Equips item and returns list of alterations<BR>
     * <B>If you dont need return value use {@link Inventory#equipItem(L2ItemInstance)} instead</B>
     *
     * @param item : L2ItemInstance corresponding to the item
     * @return L2ItemInstance[] : list of alterations
     */
    public L2ItemInstance[] equipItemAndRecord(L2ItemInstance item)
    {
        ChangeRecorder recorder = newRecorder();

        try
        {
            equipItem(item);
        }
        finally
        {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    /**
     * Equips item in slot of paperdoll.
     *
     * @param item : L2ItemInstance designating the item and slot used.
     */
    public void equipItem(L2ItemInstance item)
    {
        if((getOwner() instanceof L2PcInstance) && ((L2PcInstance) getOwner()).getPrivateStoreType() != PlayerPrivateStoreType.NONE)
        {
            return;
        }

        if(getOwner() instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) getOwner();

            if(!player.isGM() && !player.getOlympiadController().isHero() && item.isHeroItem() && !item.isRankItem())
            {
                return;
            }
        }

        long targetSlot = item.getItem().getBodyPart();

        // Check if player is using Formal Wear and item isn't Wedding Bouquet.
        L2ItemInstance formal = getPaperdollItem(PAPERDOLL_CHEST);
        if((item.getItemId() != 21163) && (formal != null) && (formal.getItem().getBodyPart() == L2Item.SLOT_ALLDRESS))
        {
            if(targetSlot == L2Item.SLOT_LR_HAND || targetSlot == L2Item.SLOT_L_HAND || targetSlot == L2Item.SLOT_R_HAND || targetSlot == L2Item.SLOT_LEGS || targetSlot == L2Item.SLOT_FEET || targetSlot == L2Item.SLOT_GLOVES || targetSlot == L2Item.SLOT_HEAD)
            {
                return;
            }
        }

        if(targetSlot == L2Item.SLOT_LR_HAND)
        {
            setPaperdollItem(PAPERDOLL_LHAND, null);
            setPaperdollItem(PAPERDOLL_RHAND, item);
        }
        else if(targetSlot == L2Item.SLOT_L_HAND)
        {
            L2ItemInstance rh = getPaperdollItem(PAPERDOLL_RHAND);
            if(rh != null && rh.getItem().getBodyPart() == L2Item.SLOT_LR_HAND && !((rh.getItemType() == L2WeaponType.BOW && item.getItemType() == L2EtcItemType.ARROW) || (rh.getItemType() == L2WeaponType.CROSSBOW && item.getItemType() == L2EtcItemType.BOLT) || (rh.getItemType() == L2WeaponType.TWOHANDCROSSBOW && item.getItemType() == L2EtcItemType.BOLT) || (rh.getItemType() == L2WeaponType.FISHINGROD && item.getItemType() == L2EtcItemType.LURE)))
            {
                setPaperdollItem(PAPERDOLL_RHAND, null);
            }
            setPaperdollItem(PAPERDOLL_LHAND, item);
        }
        else if(targetSlot == L2Item.SLOT_R_HAND)
        {
            setPaperdollItem(PAPERDOLL_RHAND, item);
        }
        else if(targetSlot == L2Item.SLOT_L_EAR || targetSlot == L2Item.SLOT_R_EAR || targetSlot == L2Item.SLOT_LR_EAR)
        {
            if(_paperdoll[PAPERDOLL_LEAR] == null)
            {
                setPaperdollItem(PAPERDOLL_LEAR, item);
            }
            else if(_paperdoll[PAPERDOLL_REAR] == null)
            {
                setPaperdollItem(PAPERDOLL_REAR, item);
            }
            else
            {
                setPaperdollItem(PAPERDOLL_LEAR, item);
            }
        }
        else if(targetSlot == L2Item.SLOT_L_FINGER || targetSlot == L2Item.SLOT_R_FINGER || targetSlot == L2Item.SLOT_LR_FINGER)
        {
            if(_paperdoll[PAPERDOLL_LFINGER] == null)
            {
                setPaperdollItem(PAPERDOLL_LFINGER, item);
            }
            else if(_paperdoll[PAPERDOLL_RFINGER] == null)
            {
                setPaperdollItem(PAPERDOLL_RFINGER, item);
            }
            else
            {
                setPaperdollItem(PAPERDOLL_LFINGER, item);
            }
        }
        else if(targetSlot == L2Item.SLOT_NECK)
        {
            setPaperdollItem(PAPERDOLL_NECK, item);

        }
        else if(targetSlot == L2Item.SLOT_FULL_ARMOR)
        {
            setPaperdollItem(PAPERDOLL_LEGS, null);
            setPaperdollItem(PAPERDOLL_CHEST, item);

        }
        else if(targetSlot == L2Item.SLOT_CHEST)
        {
            setPaperdollItem(PAPERDOLL_CHEST, item);

        }
        else if(targetSlot == L2Item.SLOT_LEGS)
        {
            L2ItemInstance chest = getPaperdollItem(PAPERDOLL_CHEST);
            if(chest != null && chest.getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR)
            {
                setPaperdollItem(PAPERDOLL_CHEST, null);
            }
            setPaperdollItem(PAPERDOLL_LEGS, item);
        }
        else if(targetSlot == L2Item.SLOT_FEET)
        {
            setPaperdollItem(PAPERDOLL_FEET, item);

        }
        else if(targetSlot == L2Item.SLOT_GLOVES)
        {
            setPaperdollItem(PAPERDOLL_GLOVES, item);

        }
        else if(targetSlot == L2Item.SLOT_HEAD)
        {
            setPaperdollItem(PAPERDOLL_HEAD, item);

        }
        else if(targetSlot == L2Item.SLOT_HAIR)
        {
            L2ItemInstance hair = getPaperdollItem(PAPERDOLL_HAIR);
            if(hair != null && hair.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
            {
                setPaperdollItem(PAPERDOLL_HAIR2, null);
            }
            else
            {
                setPaperdollItem(PAPERDOLL_HAIR, null);
            }

            setPaperdollItem(PAPERDOLL_HAIR, item);

        }
        else if(targetSlot == L2Item.SLOT_HAIR2)
        {
            L2ItemInstance hair2 = getPaperdollItem(PAPERDOLL_HAIR);
            if(hair2 != null && hair2.getItem().getBodyPart() == L2Item.SLOT_HAIRALL)
            {
                setPaperdollItem(PAPERDOLL_HAIR, null);
            }
            else
            {
                setPaperdollItem(PAPERDOLL_HAIR2, null);
            }

            setPaperdollItem(PAPERDOLL_HAIR2, item);

        }
        else if(targetSlot == L2Item.SLOT_HAIRALL)
        {
            setPaperdollItem(PAPERDOLL_HAIR2, null);
            setPaperdollItem(PAPERDOLL_HAIR, item);

        }
        else if(targetSlot == L2Item.SLOT_UNDERWEAR)
        {
            setPaperdollItem(PAPERDOLL_UNDER, item);

        }
        else if(targetSlot == L2Item.SLOT_BACK)
        {
            setPaperdollItem(PAPERDOLL_CLOAK, item);

        }
        else if(targetSlot == L2Item.SLOT_L_BRACELET)
        {
            setPaperdollItem(PAPERDOLL_LBRACELET, item);

        }
        else if(targetSlot == L2Item.SLOT_R_BRACELET)
        {
            setPaperdollItem(PAPERDOLL_RBRACELET, item);

        }
        else if(targetSlot == L2Item.SLOT_DECO)
        {
            equipTalisman(item);

        }
        else if(targetSlot == L2Item.SLOT_BELT)
        {
            setPaperdollItem(PAPERDOLL_BELT, item);

        }
        else if(targetSlot == L2Item.SLOT_ALLDRESS)
        {// formal dress
            setPaperdollItem(PAPERDOLL_LEGS, null);
            setPaperdollItem(PAPERDOLL_LHAND, null);
            setPaperdollItem(PAPERDOLL_RHAND, null);
            setPaperdollItem(PAPERDOLL_RHAND, null);
            setPaperdollItem(PAPERDOLL_LHAND, null);
            setPaperdollItem(PAPERDOLL_HEAD, null);
            setPaperdollItem(PAPERDOLL_FEET, null);
            setPaperdollItem(PAPERDOLL_GLOVES, null);
            setPaperdollItem(PAPERDOLL_CHEST, item);

        }
        else if(targetSlot == L2Item.SLOT_BROACH)
        {
            setPaperdollItem(PAPERDOLL_BROACH, item);

        }
        else if(targetSlot == L2Item.SLOT_STONE)
        {
            equipStone(item);
        }
        else
        {
            _log.log(Level.WARN, "Unknown body slot " + targetSlot + " for Item ID:" + item.getItemId());
        }
    }

    /**
     * Returns the totalWeight.
     *
     * @return int
     */
    public int getTotalWeight()
    {
        return _totalWeight;
    }

    public L2ItemInstance findArrowForBow(L2Item bow)
    {
        int[] arrowsId = arrows[bow.getSoulshotGradeForItem().ordinal()];
        L2ItemInstance ret;
        for(int id : arrowsId)
        {
            if((ret = getItemByItemId(id)) != null)
            {
                return ret;
            }
        }
        return null;
    }

    public L2ItemInstance findBoltForCrossBow(L2Item crossbow)
    {
        int[] boltsId = bolts[crossbow.getSoulshotGradeForItem().ordinal()];
        L2ItemInstance ret;
        for(int id : boltsId)
        {
            if((ret = getItemByItemId(id)) != null)
            {
                return ret;
            }
        }
        return null;
    }

    public int getMaxTalismanCount()
    {
        return (int) getOwner().getStat().calcStat(Stats.TALISMAN_SLOTS, 0, null, null);
    }

    private void equipTalisman(L2ItemInstance item)
    {
        if(getMaxTalismanCount() == 0)
        {
            return;
        }

        // find same (or incompatible) talisman type
        for(int i = PAPERDOLL_DECO1; i < PAPERDOLL_DECO1 + getMaxTalismanCount(); i++)
        {
            if(_paperdoll[i] != null)
            {
                if(getPaperdollItemId(i) == item.getItemId())
                {
                    // overwtite
                    setPaperdollItem(i, item);
                    return;
                }
            }
        }

        // no free slot found - put on first free
        for(int i = PAPERDOLL_DECO1; i < PAPERDOLL_DECO1 + getMaxTalismanCount(); i++)
        {
            if(_paperdoll[i] == null)
            {
                setPaperdollItem(i, item);
                return;
            }
        }

        // no free slots - put on first
        setPaperdollItem(PAPERDOLL_DECO1, item);
    }

    public int getMaxStoneCount()
    {
        return (int) getOwner().getStat().calcStat(Stats.STONE_SLOTS, 0, null, null);
    }

    private void equipStone(L2ItemInstance item)
    {
        if(getMaxStoneCount() == 0)
        {
            return;
        }

        for(int i = PAPERDOLL_STONE1; i < PAPERDOLL_STONE1 + getMaxStoneCount(); i++)
        {
            if(_paperdoll[i] != null)
            {
                if(getPaperdollItemId(i) == item.getItemId())
                {
                    setPaperdollItem(i, item);
                    return;
                }
            }
        }

        for(int i = PAPERDOLL_STONE1; i < PAPERDOLL_STONE1 + getMaxStoneCount(); i++)
        {
            if(_paperdoll[i] == null)
            {
                setPaperdollItem(i, item);
                return;
            }
        }

        setPaperdollItem(PAPERDOLL_STONE1, item);
    }

    public int getCloakStatus()
    {
        return (int) getOwner().getStat().calcStat(Stats.CLOAK_SLOT, 0, null, null);
    }

    /* Проверка на стрелы болты */

    /**
     * Re-notify to paperdoll listeners every equipped item
     */
    public void reloadEquippedItems()
    {
        long slot;

        for(L2ItemInstance item : _paperdoll)
        {
            if(item == null)
            {
                continue;
            }

            slot = item.getLocationSlot();

            for(PaperdollListener listener : _paperdollListeners)
            {
                if(listener == null)
                {
                    continue;
                }

                listener.notifyUnequiped(slot, item, this);
                listener.notifyEquiped(slot, item, this);
            }
        }
        if (getOwner().isPlayer())
        {
            getOwner().sendPacket(new ExUserInfoEquipSlot(getOwner().getActingPlayer()));
        }
    }

    /**
     * Проверяем есть ли итем у игрока.
     * @param itemId ид итема
     * @return есть ли итем у игрока
     */
    public boolean hasItems(int itemId)
    {
        return getItemByItemId(itemId) != null;
    }

    /**
     * Проверяем есть ли итем у игрока.
     * @param itemIds спиок итемов
     * @return есть ли итем у игрока
     */
    public boolean hasItems(int... itemIds)
    {
        for(int itemId : itemIds)
        {
            if(getItemByItemId(itemId) != null)
            {
                return true;
            }
        }
        return false;
    }

    public interface PaperdollListener
    {
        void notifyEquiped(long slot, L2ItemInstance inst, Inventory inventory);

        void notifyUnequiped(long slot, L2ItemInstance inst, Inventory inventory);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface PaperdollSlot
    {
    }

    private static class ChangeRecorder implements PaperdollListener
    {
        private final Inventory _inventory;
        private final List<L2ItemInstance> _changed;

        /**
         * Constructor of the ChangeRecorder
         *
         * @param inventory
         */
        ChangeRecorder(Inventory inventory)
        {
            _inventory = inventory;
            _changed = new FastList<>();
            _inventory.addPaperdollListener(this);
        }

        /**
         * Add alteration in inventory when item equiped
         */
        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(!_changed.contains(item))
            {
                _changed.add(item);
            }
        }

        /**
         * @return L2ItemInstance[] : array of alterated items
         */
        public L2ItemInstance[] getChangedItems()
        {
            return _changed.toArray(new L2ItemInstance[_changed.size()]);
        }

        /**
         * Add alteration in inventory when item unequiped
         */
        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(!_changed.contains(item))
            {
                _changed.add(item);
            }
        }

    }

    private static class BowCrossRodListener implements PaperdollListener
    {
        private static BowCrossRodListener instance = new BowCrossRodListener();

        public static BowCrossRodListener getInstance()
        {
            return instance;
        }

        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(slot != PAPERDOLL_RHAND)
            {
                return;
            }

            if(item.getItemType() == L2WeaponType.BOW || item.getItemType() == L2WeaponType.CROSSBOW || item.getItemType() == L2WeaponType.TWOHANDCROSSBOW || item.getItemType() == L2WeaponType.FISHINGROD)
            {
                L2ItemInstance arrow = inventory.getPaperdollItem(PAPERDOLL_LHAND);

                if(arrow != null)
                {
                    inventory.setPaperdollItem(PAPERDOLL_LHAND, null);
                }
            }
        }

        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(slot != PAPERDOLL_RHAND)
            {
                return;
            }

            if(item.getItemType() == L2WeaponType.BOW)
            {
                L2ItemInstance arrow = inventory.findArrowForBow(item.getItem());

                if(arrow != null)
                {
                    inventory.setPaperdollItem(PAPERDOLL_LHAND, arrow);
                }
            }
            else if(item.getItemType() == L2WeaponType.CROSSBOW || item.getItemType() == L2WeaponType.TWOHANDCROSSBOW)
            {
                L2ItemInstance bolts = inventory.findBoltForCrossBow(item.getItem());

                if(bolts != null)
                {
                    inventory.setPaperdollItem(PAPERDOLL_LHAND, bolts);
                }
            }
        }
    }

    private static class StatsListener implements PaperdollListener
    {
        private static StatsListener instance = new StatsListener();

        public static StatsListener getInstance()
        {
            return instance;
        }

        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            inventory.getOwner().removeStatsOwner(item);
        }

        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            inventory.getOwner().addStatFuncs(item.getStatFuncs(inventory.getOwner()));
        }
    }

    private static class ItemSkillsListener implements PaperdollListener
    {
        private static ItemSkillsListener instance = new ItemSkillsListener();

        public static ItemSkillsListener getInstance()
        {
            return instance;
        }

        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            L2PcInstance player;

            if(inventory.getOwner() instanceof L2PcInstance)
            {
                player = (L2PcInstance) inventory.getOwner();
            }
            else
            {
                return;
            }

            L2Skill itemSkill;
            L2Item it = item.getItem();
            boolean update = false;

            if(it instanceof L2Weapon || it instanceof L2Armor)
            {
                // Apply augmentation bonuses on equip
                if(item.isAugmented())
                {
                    item.getAugmentation().removeBonus(player);
                }
                item.removeElementAttrBonus(player);
                // Remove skills bestowed from +4 Rapiers/Duals
                FastMap<Integer, SkillHolder> enchantSkill = it instanceof L2Weapon ? ((L2Weapon) it).getEnchantSkills() : ((L2Armor) it).getEnchantSkills();
                if(enchantSkill != null)
                {
                    for(Integer key : enchantSkill.keySet())
                    {
                        if(item.getEnchantLevel() >= key)
                        {
                            player.removeSkill(enchantSkill.get(key).getSkill(), false, enchantSkill.get(key).getSkill().isPassive());
                            update = true;
                        }
                    }
                }
            }

            // Поскольку для R+ сетов хватает для открытия слота одного-лишь плаща,
            // проверяем и снимаем возможно занятый слот плаща
            if(it.getBodyPart() == L2Item.SLOT_CHEST)
            {
                player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_BACK);
            }

            SkillHolder[] skills = it.getSkills();

            // Suppress check for stackable item skills, that should not be deleted when 2 similar items equipped
            int bodyPart = getPaperdollIndex(it.getBodyPart());
            boolean keepSkills = false;
            List<L2ItemInstance> parallelItems = new ArrayList<>();
            switch(bodyPart)
            {
                // Rings
                // We need to keep both items, because L2Server determines left/right parts incorrectly. This is strange but does not matter... :(
                case PAPERDOLL_LFINGER:
                case PAPERDOLL_RFINGER:
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_RFINGER));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_LFINGER));
                    break;
                // Earrings
                case PAPERDOLL_LEAR:
                case PAPERDOLL_REAR:
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_REAR));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_LEAR));
                    break;
                // Talismans
                case PAPERDOLL_DECO1:
                case PAPERDOLL_DECO2:
                case PAPERDOLL_DECO3:
                case PAPERDOLL_DECO4:
                case PAPERDOLL_DECO5:
                case PAPERDOLL_DECO6:
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_DECO1));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_DECO2));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_DECO3));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_DECO4));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_DECO5));
                    parallelItems.add(inventory.getPaperdollItem(PAPERDOLL_DECO6));
                    break;
            }

            for(L2ItemInstance parallelItem : parallelItems)
            {
                if(parallelItem != null && parallelItem.getItemId() == item.getItemId())
                {
                    keepSkills = true;
                }
            }

            if(skills != null && !keepSkills)
            {
                for(SkillHolder skillInfo : skills)
                {
                    if(skillInfo == null)
                    {
                        continue;
                    }

                    itemSkill = skillInfo.getSkill();

                    if(itemSkill != null)
                    {
                        player.removeSkill(itemSkill, false, itemSkill.isPassive());
                        // Удаляем эффект у агатионов
                        if(itemSkill.getNpcId() > 0)
                        {
                            player.stopEffects(L2EffectType.SUMMON_AGATHION);
                        }

                        update = true;
                    }
                    else
                    {
                        _log.log(Level.WARN, "Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + '.');
                    }
                }
            }

            if(update)
            {
                player.sendSkillList();
            }
        }

        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            L2PcInstance player;

            if(inventory.getOwner() instanceof L2PcInstance)
            {
                player = (L2PcInstance) inventory.getOwner();
            }
            else
            {
                return;
            }

            L2Skill itemSkill;
            L2Item it = item.getItem();
            boolean update = false;
            boolean updateTimeStamp = false;

            if(it instanceof L2Weapon || it instanceof L2Armor)
            {
                // Apply augmentation bonuses on equip
                if(item.isAugmented())
                {
                    item.getAugmentation().applyBonus(player);
                }

                item.updateElementAttrBonus(player);

                // Add skills bestowed from +4 Rapiers/Duals
                FastMap<Integer, SkillHolder> enchantSkill = it instanceof L2Weapon ? ((L2Weapon) it).getEnchantSkills() : ((L2Armor) it).getEnchantSkills();
                if(enchantSkill != null)
                {
                    for(Integer key : enchantSkill.keySet())
                    {
                        if(item.getEnchantLevel() >= key)
                        {
                            player.addSkill(enchantSkill.get(key).getSkill(), false);
                            update = true;
                        }
                    }
                }
            }

            SkillHolder[] skills = it.getSkills();

            if(skills != null)
            {
                for(SkillHolder skillInfo : skills)
                {
                    if(skillInfo == null)
                    {
                        continue;
                    }

                    itemSkill = skillInfo.getSkill();

                    if(itemSkill != null)
                    {
                        player.addSkill(itemSkill, false);

                        if(itemSkill.isActive())
                        {
                            if(!player.hasSkillReuse(itemSkill.getReuseHashCode()))
                            {
                                int equipDelay = item.getEquipReuseDelay();

                                if(equipDelay > 0)
                                {
                                    player.addTimeStamp(itemSkill, equipDelay);
                                    player.disableSkill(itemSkill, equipDelay);
                                }
                            }
                            updateTimeStamp = true;
                        }
                        update = true;
                    }
                    else
                    {
                        _log.log(Level.ERROR, "Inventory.ItemSkillsListener.Weapon: Incorrect skill: " + skillInfo + '.');
                    }
                }
            }

            if(item.isArmor())
            {
                for(L2ItemInstance itm : inventory.getItems())
                {
                    if(!itm.isEquipped() || itm.getItem().getSkills() == null)
                    {
                        continue;
                    }
                    for(SkillHolder sk : itm.getItem().getSkills())
                    {
                        if(player.getSkillLevel(sk.getSkillId()) != -1)
                        {
                            continue;
                        }

                        itemSkill = sk.getSkill();

                        if(itemSkill != null)
                        {
                            player.addSkill(itemSkill, false);

                            if(itemSkill.isActive())
                            {
                                if(!player.hasSkillReuse(itemSkill.getReuseHashCode()))
                                {
                                    int equipDelay = item.getEquipReuseDelay();
                                    if(equipDelay > 0)
                                    {
                                        player.addTimeStamp(itemSkill, equipDelay);
                                        player.disableSkill(itemSkill, equipDelay);
                                    }
                                }
                                updateTimeStamp = true;
                            }
                            update = true;
                        }
                    }
                }
            }

            if(update)
            {
                player.sendSkillList();

                if(updateTimeStamp)
                {
                    player.sendPacket(new SkillCoolTime(player));
                }
                item.setEnchantEffect(EnchantEffectTable.getInstance().getEnchantEffect(item));
            }
        }
    }

    private static class ArmorSetListener implements PaperdollListener
    {
        private static ArmorSetListener instance = new ArmorSetListener();

        // Используется для графического отображения иконки полного сета в скилах
        final L2Skill _equipSetGraphic = SkillTable.getInstance().getInfo(3006, 1);

        public static ArmorSetListener getInstance()
        {
            return instance;
        }

        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(!(inventory.getOwner() instanceof L2PcInstance))
            {
                return;
            }

            L2PcInstance player = (L2PcInstance) inventory.getOwner();
            L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());

            if(armorSet == null)
            {
                return;
            }

            boolean update = false;
            boolean updateTimeStamp = false;

            // Проверяем, является ли вещь частью сета
            if(armorSet.containItem(item.getItemId()))
            {
                if(armorSet.containAll(player))
                {
                    player.addSkill(_equipSetGraphic, false);

                    List<L2Skill> skills = armorSet.getSkill();
                    if(!skills.isEmpty())
                    {
                        for(L2Skill sk : skills)
                        {
                            if(sk != null)
                            {
                                player.addSkill(sk, false);

                                if(sk.isActive())
                                {
                                    if(!player.hasSkillReuse(sk.getReuseHashCode()))
                                    {
                                        int equipDelay = item.getEquipReuseDelay();

                                        if(equipDelay > 0)
                                        {
                                            player.addTimeStamp(sk, equipDelay);
                                            player.disableSkill(sk, equipDelay);
                                        }
                                    }
                                    updateTimeStamp = true;
                                }
                                update = true;
                            }
                            else
                            {
                                _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + sk + '.');
                            }
                        }
                        player.sendSkillList();
                    }

                    if(armorSet.containShield(player)) // если есть щит в сете
                    {
                        L2Skill shieldSkill = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);

                        if(shieldSkill != null)
                        {
                            player.addSkill(shieldSkill, false);
                            update = true;
                        }
                        else
                        {
                            _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + '.');
                        }
                    }

                    FastMap<Integer, SkillHolder> enchantSkill = armorSet.getEnchantskills();
                    if(enchantSkill != null)
                    {
                        for(Integer key : enchantSkill.keySet())
                        {
                            if(armorSet.isEnchanted6(player, key))
                            {
                                L2Skill skille = enchantSkill.get(key).getSkill();
                                if(skille != null)
                                {
                                    player.addSkill(skille, false);
                                    update = true;
                                }
                                else
                                {
                                    _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getEnchantskills());
                                }
                            }
                        }
                    }
                }
                // Для R-сетов эффект от сета применяем в зависимости от кол-ва надетых его частей (не обязательно иметь фул)
                // (2 части - 1 лвл скила,3 части - 2 лвл и т.д.)
                else if(armorSet.getPartsSkillId() != 0 && armorSet.countOfPieces(player) >= 2)
                {
                    L2Skill skill = SkillTable.getInstance().getInfo(armorSet.getPartsSkillId(), armorSet.countOfPieces(player) - 1);
                    if(skill != null)
                    {
                        player.addSkill(skill, false);
                        update = true;
                    }
                    else
                    {
                        _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect R-Grade skill: " + armorSet.getPartsSkillId() + '.');
                    }
                }
            }
            else if(armorSet.containShield(item.getItemId()))
            {
                if(armorSet.containAll(player))
                {
                    L2Skill shieldSkill = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);

                    if(shieldSkill != null)
                    {
                        player.addSkill(shieldSkill, false);
                        update = true;
                    }
                    else
                    {
                        _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + armorSet.getShieldSkillId() + '.');
                    }
                }
            }

            if(update)
            {
                item.setEnchantEffect(EnchantEffectTable.getInstance().getEnchantEffect(item));
                player.sendSkillList();

                if(updateTimeStamp)
                {
                    player.sendPacket(new SkillCoolTime(player));
                }
            }
        }

        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(!(inventory.getOwner() instanceof L2PcInstance))
            {
                return;
            }

            L2ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());
            L2PcInstance player = (L2PcInstance) inventory.getOwner();

            boolean remove = false;
            List<L2Skill> removeSkillId1 = null; // основные скилы сета
            int removeSkillId2 = 0; // скилл щита
            FastMap<Integer, SkillHolder> removeSkillId3 = null; // скил заточки на +6
            int removeSkillId4 = 0; // Piece-Скил R-Grade сетов

            if(slot == PAPERDOLL_CHEST && item.getItem().getItemGradeRPlus() != CrystalGrade.R)
            {
                if(armorSet == null)
                {
                    return;
                }

                remove = true;
                removeSkillId1 = armorSet.getSkill();
                removeSkillId2 = armorSet.getShieldSkillId();
                removeSkillId3 = armorSet.getEnchantskills();
            }
            else
            {
                if(armorSet == null)
                {
                    return;
                }

                if(armorSet.containItem(slot, item.getItemId())) // снята часть сета
                {
                    remove = true;
                    removeSkillId1 = armorSet.getSkill();
                    removeSkillId2 = armorSet.getShieldSkillId();
                    removeSkillId3 = armorSet.getEnchantskills();
                    removeSkillId4 = armorSet.getPartsSkillId();
                    player.removeSkill(_equipSetGraphic);
                }
                else if(armorSet.containShield(item.getItemId())) // снят щит
                {
                    remove = true;
                    removeSkillId2 = armorSet.getShieldSkillId();
                }
            }

            if(remove)
            {
                if(removeSkillId1 != null)
                {
                    if(!removeSkillId1.isEmpty())
                    {
                        for(L2Skill sk : removeSkillId1)
                        {
                            if(sk != null)
                            {
                                player.removeSkill(sk);
                            }
                            else
                            {
                                _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId1 + '.');
                            }
                        }
                    }
                }
                if(removeSkillId2 != 0)
                {
                    L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
                    if(skill != null)
                    {
                        player.removeSkill(skill);
                    }
                    else
                    {
                        _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId2 + '.');
                    }
                }
                if(removeSkillId3 != null)
                {
                    for(Integer key : removeSkillId3.keySet())
                    {
                        L2Skill skill = removeSkillId3.get(key).getSkill();
                        if(skill != null)
                        {
                            player.removeSkill(skill);
                        }
                        else
                        {
                            _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId3 + '.');
                        }
                    }
                }
                if(removeSkillId4 != 0)
                {
                    // Если при снятии части, еще есть >= 2 частей, то добавляем скил на уровень ниже
                    if(armorSet.countOfPieces(player) >= 2)
                    {
                        L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId4, armorSet.countOfPieces(player) - 1);
                        if(skill != null)
                        {
                            player.addSkill(skill, false);
                        }
                    }
                    // Если меньше, то удаляем скил сета полностью
                    else
                    {
                        L2Skill skill = SkillTable.getInstance().getInfo(removeSkillId4, 1);
                        if(skill != null)
                        {
                            player.removeSkill(skill);
                        }
                        else
                        {
                            _log.log(Level.WARN, "Inventory.ArmorSetListener: Incorrect skill: " + removeSkillId4 + '.');
                        }
                    }
                }
                player.checkItemRestriction();
                player.sendSkillList();
            }
        }
    }

    private static class BraceletListener implements PaperdollListener
    {
        private static BraceletListener instance = new BraceletListener();

        public static BraceletListener getInstance()
        {
            return instance;
        }

        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(item.getItem().getBodyPart() == L2Item.SLOT_R_BRACELET)
            {
                inventory.unEquipItemInSlot(PAPERDOLL_DECO1);
                inventory.unEquipItemInSlot(PAPERDOLL_DECO2);
                inventory.unEquipItemInSlot(PAPERDOLL_DECO3);
                inventory.unEquipItemInSlot(PAPERDOLL_DECO4);
                inventory.unEquipItemInSlot(PAPERDOLL_DECO5);
                inventory.unEquipItemInSlot(PAPERDOLL_DECO6);
            }
        }

        // Note (April 3, 2009): Currently on equip, talismans do not display properly, do we need checks here to fix this?

        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            item.setEnchantEffect(EnchantEffectTable.getInstance().getEnchantEffect(item));
        }
    }

    private static class BroachListener implements PaperdollListener
    {
        private static BroachListener instance = new BroachListener();

        public static BroachListener getInstance()
        {
            return instance;
        }

        @Override
        public void notifyUnequiped(long slot, L2ItemInstance item, Inventory inventory)
        {
            if(item.getItem().getBodyPart() == L2Item.SLOT_BROACH)
            {
                inventory.unEquipItemInSlot(PAPERDOLL_STONE1);
                inventory.unEquipItemInSlot(PAPERDOLL_STONE2);
                inventory.unEquipItemInSlot(PAPERDOLL_STONE3);
                inventory.unEquipItemInSlot(PAPERDOLL_STONE4);
                inventory.unEquipItemInSlot(PAPERDOLL_STONE5);
                inventory.unEquipItemInSlot(PAPERDOLL_STONE6);
            }
        }

        @Override
        public void notifyEquiped(long slot, L2ItemInstance item, Inventory inventory)
        {
        }
    }
}