package dwo.gameserver.model.items.base.instance;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.queries.Items;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.logengine.formatters.ItemLogFormatter;
import dwo.gameserver.instancemanager.ItemsOnGroundManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleMercTicketManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.knownlist.NullKnownList;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.Elementals;
import dwo.gameserver.model.items.EnchantEffectTable;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.L2Augmentation;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.items.base.type.L2ItemType;
import dwo.gameserver.model.skills.base.funcs.Func;
import dwo.gameserver.model.world.npc.drop.DropProtection;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.*;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAdenaInvenCount;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.GMAudit;
import dwo.gameserver.util.database.DatabaseUtils;
import dwo.scripts.services.Tutorial;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.ADENA_ID;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class L2ItemInstance extends L2Object
{
	public static final int CHARGED_NONE = 0;
	public static final int CHARGED_SOULSHOT = 1;
	public static final int CHARGED_SPIRITSHOT = 1;
	public static final int CHARGED_BLESSED_SPIRITSHOT = 2;
	public static final int UNCHANGED = 0;
	public static final int ADDED = 1;
	public static final int REMOVED = 3;
	public static final int MODIFIED = 2;
	protected static final Logger _log = LogManager.getLogger(L2ItemInstance.class);
	private static final Logger _logItems = LogManager.getLogger("item");
	private static final int MANA_CONSUMPTION_RATE = 60000;
	/**
	 * ID of the item
	 */
	private final int _itemId;
	/**
	 * Object L2Item associated to the item
	 */
	private final L2Item _item;
	private final ReentrantLock _dbLock = new ReentrantLock();
	private final DropProtection _dropProtection = new DropProtection();
	public ScheduledFuture<?> _lifeTimeTask;
	/**
	 * ID of the owner
	 */
	private int _ownerId;
	/**
	 * ID of who dropped the item last, used for knownlist
	 */
	private int _dropperObjectId;
	/**
	 * Quantity of the item
	 */
	private long _count;
	/**
	 * Initial Quantity of the item
	 */
	private long _initCount;
	/**
	 * Remaining time (in miliseconds)
	 */
	private long _time;
	/**
	 * Quantity of the item can decrease
	 */
	private boolean _decrease;
	/**
	 * Location of the item : Inventory, PaperDoll, WareHouse
	 */
	private ItemLocation _loc;
	/**
	 * Slot where item is stored : Paperdoll slot, inventory order ...
	 */
	private long _locData;
	/**
	 * Level of enchantment of the item
	 */
	private int _enchantLevel;
	/**
	 * Wear Item
	 */
	private boolean _wear;
	/**
	 * Augmented Item
	 */
	private L2Augmentation _augmentation;
	/** enchant Effect*/
	public static int[] _enchantEffect = {0, 0, 0};
	/*
	 * Внешний вид оружия / брони
	 */
	private int _skin;
	/**
	 * Shadow item
	 */
	private int _mana = -1;
	private boolean _consumingMana;
	/**
	 * Custom item types (used loto, race tickets)
	 */
	private int _type1;
	private int _type2;
	private long _dropTime;
	private boolean _published;
	/**
	 * Item charged with SoulShot (type of SoulShot)
	 */
	private int _chargedSoulshot;
	/**
	 * Item charged with SpiritShot (type of SpiritShot)
	 */
	private int _chargedSpiritshot;
	private boolean _chargedFishtshot;
	private boolean _protected;
	private int _lastChange = 2;    //1 ??, 2 modified, 3 removed
	private boolean _existsInDb; // if a record exists in DB.
	private boolean _storedInDb; // if DB data is up-to-date.
	private Elementals[] _elementals;
	private ScheduledFuture<?> itemLootShedule;

	/**
	 * Constructor of the L2ItemInstance from the objectId and the itemId.
	 *
	 * @param objectId : int designating the ID of the object in the world
	 * @param itemId   : int designating the ID of the item
	 */
	public L2ItemInstance(int objectId, int itemId)
	{
		super(objectId);
		_itemId = itemId;
		_item = ItemTable.getInstance().getTemplate(itemId);
		if(_itemId == 0 || _item == null)
		{
			throw new IllegalArgumentException();
		}
		setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_type1 = 0;
		_type2 = 0;
		_dropTime = 0;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) _item.getTime() * 60 * 1000;
		scheduleLifeTimeTask();
	}

	/**
	 * Constructor of the L2ItemInstance from the objetId and the description of the item given by the L2Item.
	 *
	 * @param objectId : int designating the ID of the object in the world
	 * @param item     : L2Item containing informations of the item
	 */
	public L2ItemInstance(int objectId, L2Item item)
	{
		super(objectId);
		_itemId = item.getItemId();
		_item = item;
		if(_itemId == 0)
		{
			throw new IllegalArgumentException();
		}
		setName(_item.getName());
		setCount(1);
		_loc = ItemLocation.VOID;
		_mana = _item.getDuration();
		_time = _item.getTime() == -1 ? -1 : System.currentTimeMillis() + (long) _item.getTime() * 60 * 1000;
		scheduleLifeTimeTask();
	}

	/**
	 * Returns a L2ItemInstance stored in database from its objectID
	 *
	 * @param ownerId : int designating the objectID of the item
	 * @return L2ItemInstance
	 */
	public static L2ItemInstance restoreFromDb(int ownerId, ResultSet rs)
	{
		L2ItemInstance inst = null;
		int objectId;
		int item_id;
		int loc_data;
		int enchant_level;
		int custom_type1;
		int custom_type2;
		int manaLeft;
		int skin;
		long time;
		long count;
		ItemLocation loc;
		try
		{
			objectId = rs.getInt("object_id");
			item_id = rs.getInt("item_id");
			count = rs.getLong("count");
			loc = ItemLocation.valueOf(rs.getString("loc"));
			loc_data = rs.getInt("loc_data");
			enchant_level = rs.getInt("enchant_level");
			custom_type1 = rs.getInt("custom_type1");
			custom_type2 = rs.getInt("custom_type2");
			manaLeft = rs.getInt("mana_left");
			time = rs.getLong("time");
			skin = rs.getInt("skin_id");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore an item owned by " + ownerId + " from DB:", e);
			return null;
		}
		L2Item item = ItemTable.getInstance().getTemplate(item_id);
		if(item == null)
		{
			_log.log(Level.ERROR, "Item item_id=" + item_id + " not known, object_id=" + objectId);
			return null;
		}
		inst = new L2ItemInstance(objectId, item);
		inst._ownerId = ownerId;
		inst.setCount(count);
		inst._enchantLevel = enchant_level;
		inst._type1 = custom_type1;
		inst._type2 = custom_type2;
		inst._loc = loc;
		inst._locData = loc_data;
		inst._existsInDb = true;
		inst._storedInDb = true;

		// Setup life time for shadow weapons
		inst._mana = manaLeft;
		inst._time = time;

		// Внешний вид
		inst._skin = skin;

		// load augmentation and elemental enchant
		if(inst.isEquipable())
		{
			inst.restoreAttributes();
		}

		return inst;
	}

	/**
	 * Remove a L2ItemInstance from the world and send server->client GetItem packets.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a ServerMode->Client Packet GetItem to player that pick up and its _knowPlayers member </li>
	 * <li>Remove the L2Object from the world</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> this instanceof L2ItemInstance</li>
	 * <li> _worldRegion != null <I>(L2Object is visible at the beginning)</I></li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Do Pickup Item : PCInstance and Pet</li><BR><BR>
	 *
	 * @param player Player that pick up the item
	 */
	public void pickupMe(L2Character player)
	{
		assert getLocationController().getWorldRegion() != null;

		L2WorldRegion oldregion = getLocationController().getWorldRegion();

		// Create a server->client GetItem packet to pick up the L2ItemInstance
		GetItem gi = new GetItem(this, player.getObjectId());
		player.broadcastPacket(gi);

		synchronized(this)
		{
			getLocationController().setVisible(false);
		}

		// if this item is a mercenary ticket, remove the spawns!
		int itemId = _itemId;

		if(CastleMercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
		{
			CastleMercTicketManager.getInstance().removeTicket(this);
			ItemsOnGroundManager.getInstance().removeObject(this);
		}

		if(!Config.DISABLE_TUTORIAL && (itemId == ADENA_ID || itemId == 6353))
		{
			L2PcInstance actor = player.getActingPlayer();
			if(actor != null)
			{
				QuestState qs = actor.getQuestState(Tutorial.class);
				if(qs != null && qs.getQuest() != null)
				{
					qs.getQuest().notifyEvent("CE" + itemId, null, actor);
				}
			}
		}
		// outside of synchronized to avoid deadlocks
		// Remove the L2ItemInstance from the world
		WorldManager.getInstance().removeVisibleObject(this, oldregion);
	}

	/**
	 * Sets the ownerID of the item
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param owner_id  : int designating the ID of the owner
	 * @param creator   : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void setOwnerId(ProcessType process, int owner_id, L2PcInstance creator, Object reference)
	{
		setOwnerId(owner_id);

		if(Config.LOG_ITEMS)
		{
			_logItems.log(Level.INFO, ItemLogFormatter.format("CHANGE:" + process, new Object[]{
				this, "[ALL]", creator, reference
			}));
		}
		if(creator != null)
		{
			if(creator.isGM())
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
				String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
				if(Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + ']', process + "(id: " + _itemId + " name: " + getName() + ')', targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}
	}

	/**
	 * Returns the ownerID of the item
	 *
	 * @return int : ownerID of the item
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}

	/**
	 * Sets the ownerID of the item
	 *
	 * @param owner_id : int designating the ID of the owner
	 */
	public void setOwnerId(int owner_id)
	{
		if(owner_id == _ownerId)
		{
			return;
		}

		// Remove any inventory skills from the old owner.
		removeSkillsFromOwner();

		_ownerId = owner_id;
		_storedInDb = false;

		// Give any inventory skills to the new owner only if the item is in inventory
		// else the skills will be given when location is set to inventory.
		giveSkillsToOwner();
	}

	/**
	 * Sets the location of the item
	 *
	 * @param loc : ItemLocation (enumeration)
	 */
	public void setLocation(ItemLocation loc)
	{
		setLocation(loc, 0);
	}

	/**
	 * Sets the location of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 *
	 * @param loc      : ItemLocation (enumeration)
	 * @param loc_data : int designating the slot where the item is stored or the village for freights
	 */
	public void setLocation(ItemLocation loc, long loc_data)
	{
		if(loc == _loc && loc_data == _locData)
		{
			return;
		}

		// Remove any inventory skills from the old owner.
		removeSkillsFromOwner();

		_loc = loc;
		_locData = loc_data;
		_storedInDb = false;

		// Give any inventory skills to the new owner only if the item is in inventory
		// else the skills will be given when location is set to inventory.
		giveSkillsToOwner();
	}

	public ItemLocation getItemLocation()
	{
		return _loc;
	}

	/**
	 * @return Returns the count.
	 */
	public long getCount()
	{
		return _count;
	}

	/**
	 * Sets the quantity of the item.<BR><BR>
	 *
	 * @param count the new count to set
	 */
	public void setCount(long count)
	{
		if(_count == count)
		{
			return;
		}

		_count = count >= -1 ? count : 0;
		_storedInDb = false;
	}

	/**
	 * Sets the quantity of the item.<BR><BR>
	 * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param count     : int
	 * @param creator   : L2PcInstance Player requesting the item creation
	 * @param reference : Object Object referencing current action like NPC selling item or previous item in transformation
	 */
	public void changeCount(ProcessType process, long count, L2PcInstance creator, Object reference)
	{
		if(count == 0)
		{
			return;
		}
		long old = _count;
		long max = _itemId == ADENA_ID ? MAX_ADENA : Integer.MAX_VALUE;

		if(count > 0 && _count > max - count)
		{
			setCount(max);
		}
		else
		{
			setCount(_count + count);
		}

		if(_count < 0)
		{
			setCount(0);
		}

		_storedInDb = false;

		if(Config.LOG_ITEMS && process != null)
		{
			switch(process)
			{
				case NPC:
				case CONSUMEWITHOUTTRACE:
					break;
				default:
					_logItems.log(Level.INFO, ItemLogFormatter.format("CHANGE:" + process, new Object[]{
						this, "add=" + count, creator, reference
					}));
					break;
			}
		}

		if(creator != null)
		{
			if(creator.isGM())
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
				String targetName = creator.getTarget() != null ? creator.getTarget().getName() : "no-target";
				if(Config.GMAUDIT)
				{
					GMAudit.auditGMAction(creator.getName() + " [" + creator.getObjectId() + ']', process + "(id: " + _itemId + " objId: " + getObjectId() +
						" name: " + getName() + " count: " + count + ')', targetName, "L2Object referencing this action is: " + referenceName);
				}
			}
		}

		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			if(count > 0 && creator != null && _itemId == ADENA_ID && !(WorldManager.getInstance().findObject(_dropperObjectId) instanceof L2PcInstance))
			{
				creator.updateWorldStatistic(CategoryType.ADENA_ADDED, null, (long) (count / Config.RATE_DROP_ITEMS_ID.get(57)));
			}
		}
	}

	public void changeCountWithoutTrace(int count, L2PcInstance creator, Object reference)
	{
		changeCount(null, count, creator, reference);
	}

	// No logging (function designed for shots only)

	/**
	 * Return true if item can be enchanted
	 * @return boolean
	 */
	public boolean isEnchantable()
	{
		if(_loc == ItemLocation.INVENTORY || _loc == ItemLocation.PAPERDOLL)
		{
			return _item.isEnchantable();
		}
		return false;
	}

	/**
	 * Returns if item is equipable
	 *
	 * @return boolean
	 */
	public boolean isEquipable()
	{
		return !(_item.getBodyPart() == 0 || _item.getItemType() == L2EtcItemType.ARROW || _item.getItemType() == L2EtcItemType.BOLT || _item.getItemType() == L2EtcItemType.LURE);
	}

	/**
	 * Returns if item is equipped
	 *
	 * @return boolean
	 */
	public boolean isEquipped()
	{
		return _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP;
	}

	/**
	 * Returns the slot where the item is stored
	 *
	 * @return int
	 */
	public long getLocationSlot()
	{
		assert _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.INVENTORY || _loc == ItemLocation.MAIL || _loc == ItemLocation.FREIGHT;
		return _locData;
	}

	/**
	 * @return L2Item the characteristics of the item
	 */
	public L2Item getItem()
	{
		return _item;
	}

	public int getCustomType1()
	{
		return _type1;
	}

	public void setCustomType1(int newtype)
	{
		_type1 = newtype;
	}

	public int getCustomType2()
	{
		return _type2;
	}

	public void setCustomType2(int newtype)
	{
		_type2 = newtype;
	}

	public long getDropTime()
	{
		return _dropTime;
	}

	public void setDropTime(long time)
	{
		_dropTime = time;
	}

	/**
	 * Returns the type of item
	 *
	 * @return Enum
	 */
	public L2ItemType getItemType()
	{
		return _item.getItemType();
	}

	/**
	 * @return the ID of the item
	 */
	public int getItemId()
	{
		return _itemId;
	}

	/**
	 * @return {@code true} if item is an EtcItem
	 */
	public boolean isEtcItem()
	{
		return _item instanceof L2EtcItem;
	}

	/**
	 * @return {@code true} if item is a Weapon/Shield
	 */
	public boolean isWeapon()
	{
		return _item instanceof L2Weapon;
	}

	/**
	 * @return {@code true} if item is an Armor
	 */
	public boolean isArmor()
	{
		return _item instanceof L2Armor;
	}

	/**
	 * @return {@code true} if item is an Accessory
	 */
	public boolean isAccessory()
	{
		return _item.getType2() == L2Item.TYPE2_ACCESSORY;
	}

	/**
	 * @return the characteristics of the L2EtcItem
	 */
	public L2EtcItem getEtcItem()
	{
		if(_item instanceof L2EtcItem)
		{
			return (L2EtcItem) _item;
		}
		return null;
	}

	/**
	 * @return the characteristics of the L2Weapon
	 */
	public L2Weapon getWeaponItem()
	{
		if(_item instanceof L2Weapon)
		{
			return (L2Weapon) _item;
		}
		return null;
	}

	/**
	 * @return the characteristics of the L2Armor
	 */
	public L2Armor getArmorItem()
	{
		if(_item instanceof L2Armor)
		{
			return (L2Armor) _item;
		}
		return null;
	}

	/**
	 * @return the quantity of crystals for crystallization
	 */
	public int getCrystalCount()
	{
		return _item.getCrystalCount(_enchantLevel);
	}

	/**
	 * @return the reference price of the item
	 */
	public int getReferencePrice()
	{
		return _item.getReferencePrice();
	}

	/**
	 * @return имя предмета
	 */
	public String getItemName()
	{
		return _item.getName();
	}

	/**
	 * @return серверное имя предмета
	 */
	public String getItemServerName()
	{
		return _item.getServerName();
	}

	/**
	 * @return id квеста, связанного с этим предметом
	 */
	public int getQuestId()
	{
		return _item.getQuestId();
	}

	/**
	 * @return откат текущего предмета
	 */
	public int getReuseDelay()
	{
		return _item.getReuseDelay();
	}

	/**
	 * @return общая группа отката для предмета
	 */
	public int getSharedReuseGroup()
	{
		return _item.getSharedReuseGroup();
	}

	/**
	 * @return время последнего изменения предмета
	 */
	public int getLastChange()
	{
		return _lastChange;
	}

	/**
	 * Устанавливает время последнего действия с предметом
	 *
	 * @param lastChange время
	 */
	public void setLastChange(int lastChange)
	{
		_lastChange = lastChange;
	}

	/**
	 * @return {@code true} если предмет стакается
	 */
	public boolean isStackable()
	{
		return _item.isStackable();
	}

	/**
	 * @return {@code true} если предмет можно выбросить
	 */
	public boolean isDropable()
	{
		return _item.isDropable();
	}

	/**
	 * @return {@code true} если предмет можно уничтожить
	 */
	public boolean isDestroyable()
	{
		return _item.isDestroyable();
	}

	/**
	 * Returns if item is tradeable
	 *
	 * @return {@code true} если предмет можно передать
	 */
	public boolean isTradeable()
	{
		return !isAugmented() && _item.isTradeable();
	}

	public boolean isEnchantableTimeLimited()
	{
		return _item.isEnchantableTimeLimited();
	}

	/**
	 * @return {@code true} если предмет можно продать
	 */
	public boolean isSellable()
	{
		return !isAugmented() && _item.isSellable();
	}

	/**
	 * @return {@code true} если предмет можно положить на склад
	 */
	public boolean isDepositable(boolean isPrivateWareHouse)
	{
		// equipped, hero and quest items
		if(isEquipped() || !_item.isDepositable())
		{
			return false;
		}
		if(!isPrivateWareHouse)
		{
			// augmented not tradeable
			if(!isTradeable() || isShadowItem())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Returns if item is consumable
	 *
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return _item.isConsumable();
	}

	public boolean isPotion()
	{
		return _item.isPotion();
	}

	public boolean isElixir()
	{
		return _item.isElixir();
	}

	public boolean isCrystal()
	{
		return _item.isCrystal();
	}

	public boolean isHeroItem()
	{
		return _item.isHeroItem();
	}

	public boolean isRankItem()
	{
		return _item.isRankItem();
	}

	public boolean isCommonItem()
	{
		return _item.isCommon();
	}

	/**
	 * Returns whether this item is pvp or not
	 *
	 * @return boolean
	 */
	public boolean isPvp()
	{
		return _item.isPvpItem();
	}

	public boolean isOlyRestrictedItem()
	{
		return _item.isOlyRestrictedItem();
	}

	/**
	 * Returns if item is available for manipulation
	 *
	 * @return boolean
	 */
	public boolean isAvailable(L2PcInstance player, boolean allowAdena, boolean allowNonTradeable)
	{
		if(!player.getPets().isEmpty())
		{
			boolean isPetControlItem = false;
			for(L2Summon pet : player.getPets())
			{
				if(pet.getControlObjectId() == getObjectId())
				{
					isPetControlItem = true;
				}
			}
			if(isPetControlItem)
			{
				return false;
			}
		}
		return !isEquipped() // Not equipped
			&& _item.getType2() != L2Item.TYPE2_QUEST // Not Quest Item
			&& (_item.getType2() != L2Item.TYPE2_MONEY || _item.getType1() != L2Item.TYPE1_SHIELD_ARMOR) // not money, not shield
			&& player.getActiveEnchantItem() != this // Not momentarily used enchant scroll
			&& (allowAdena || _itemId != ADENA_ID) // Not adena
			&& (player.getCurrentSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != _itemId) && (!player.isCastingSimultaneouslyNow() || player.getLastSimultaneousSkillCast() == null || player.getLastSimultaneousSkillCast().getItemConsumeId() != _itemId) && (allowNonTradeable || isTradeable() && !(_item.getItemType() == L2EtcItemType.PET_COLLAR && player.havePetInvItems()));
	}

	/**
	 * Returns the level of enchantment of the item
	 *
	 * @return int
	 */
	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	/**
	 * Sets the level of enchantment of the item
	 *
	 * @param enchantLevel
	 */
	public void setEnchantLevel(int enchantLevel)
	{
		if(_enchantLevel == enchantLevel)
		{
			return;
		}
		_enchantLevel = enchantLevel;
		_storedInDb = false;
		// Refresh enchant effects
		_enchantEffect = EnchantEffectTable.getInstance().getEnchantEffect(this);
	}

	/**
	 * Returns enchant effect object for this item
	 * @return enchanteffect
	 */
	public int[] getEnchantEffect()
	{
		return _enchantEffect;
	}

	/**
	 * Set enchant effect for this item
	 * @param effect
	 */
	public void setEnchantEffect(int[] effect)
	{
		_enchantEffect = effect;
	}

	/**
	 * Returns whether this item is augmented or not
	 *
	 * @return true if augmented
	 */
	public boolean isAugmented()
	{
		return _augmentation != null;
	}

	/**
	 * Returns the augmentation object for this item
	 *
	 * @return augmentation
	 */
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}

	public int getAugmentationId()
	{
		return _augmentation != null ? _augmentation.getAugmentationId() : 0;
	}

	/**
	 * Sets a new augmentation
	 *
	 * @param augmentation
	 * @return return true if sucessfull
	 */
	public boolean setAugmentation(L2Augmentation augmentation)
	{
		// there shall be no previous augmentation..
		if(_augmentation != null)
		{
			return false;
		}
		_augmentation = augmentation;
		updateItemAttributes(null);
		return true;
	}

	/**
	 * Remove the augmentation
	 */
	public void removeAugmentation()
	{
		if(_augmentation == null)
		{
			return;
		}
		_augmentation = null;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Items.ITEM_ATTRIBUTES_REMOVE);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not remove augmentation for item: " + this + " from DB:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void restoreAttributes()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Items.ITEM_ATTRIBUTES_LOAD);
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			if(rs.next())
			{
				int aug_attributes = rs.getInt(1);
				int aug_skillId = rs.getInt(2);
				int aug_skillLevel = rs.getInt(3);
				if(aug_attributes != -1 && aug_skillId != -1 && aug_skillLevel != -1)
				{
					_augmentation = new L2Augmentation(rs.getInt("augAttributes"), rs.getInt("augSkillId"), rs.getInt("augSkillLevel"));
				}
			}
			statement.clearParameters();

			statement = con.prepareStatement(Items.ITEM_ELEMENTALS_LOAD);
			statement.setInt(1, getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				byte elem_type = rs.getByte(1);
				int elem_value = rs.getInt(2);
				if(elem_type != -1 && elem_value != -1)
				{
					applyAttribute(elem_type, elem_value);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore augmentation and elemental data for item " + this + " from DB: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	private void updateItemAttributes(ThreadConnection pooledCon)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			statement = con.prepareStatement(Items.ITEM_ATTRIBUTES_UPDATE);
			statement.setInt(1, getObjectId());
			if(_augmentation == null)
			{
				statement.setInt(2, -1);
				statement.setInt(3, -1);
				statement.setInt(4, -1);
			}
			else
			{
				statement.setInt(2, _augmentation.getAttributes());
				if(_augmentation.getSkill() == null)
				{
					statement.setInt(3, 0);
					statement.setInt(4, 0);
				}
				else
				{
					statement.setInt(3, _augmentation.getSkill().getId());
					statement.setInt(4, _augmentation.getSkill().getLevel());
				}
			}
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not update atributes for item: " + this + " from DB:", e);

		}
		finally
		{
			if(pooledCon == null)
			{
				DatabaseUtils.closeConnection(con);
			}
			DatabaseUtils.closeStatement(statement);
		}
	}

	private void updateItemElements(ThreadConnection pooledCon)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = pooledCon == null ? L2DatabaseFactory.getInstance().getConnection() : pooledCon;
			statement = con.prepareStatement(Items.ITEM_ELEMENTALS_REMOVE);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			statement.clearParameters();

			if(_elementals == null)
			{
				return;
			}

			statement = con.prepareStatement(Items.ITEM_ELEMENTALS_ADD);

			for(Elementals elm : _elementals)
			{
				statement.setInt(1, getObjectId());
				statement.setByte(2, elm.getElement());
				statement.setInt(3, elm.getValue());
				statement.executeUpdate();
				statement.clearParameters();
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not update elementals for item: " + this + " from DB:", e);
		}
		finally
		{
			if(pooledCon == null)
			{
				DatabaseUtils.closeConnection(con);
			}
			DatabaseUtils.closeStatement(statement);
		}
	}

	public Elementals[] getElementals()
	{
		return _elementals;
	}

	public Elementals getElemental(byte attribute)
	{
		if(_elementals == null)
		{
			return null;
		}
		for(Elementals elm : _elementals)
		{
			if(elm.getElement() == attribute)
			{
				return elm;
			}
		}
		return null;
	}

	public byte getAttackElementType()
	{
		if(!isWeapon())
		{
			return -2;
		}
		if(_item.getElementals() != null)
		{
			return _item.getElementals()[0].getElement();
		}
		if(_elementals != null)
		{
			return _elementals[0].getElement();
		}
		return -2;
	}

	public int getAttackElementPower()
	{
		if(!isWeapon())
		{
			return 0;
		}
		if(_item.getElementals() != null)
		{
			return _item.getElementals()[0].getValue();
		}
		if(_elementals != null)
		{
			return _elementals[0].getValue();
		}
		return 0;
	}

	public int getElementDefAttr(byte element)
	{
		if(!isArmor())
		{
			return 0;
		}
		if(_item.getElementals() != null)
		{
			Elementals elm = _item.getElemental(element);
			if(elm != null)
			{
				return elm.getValue();
			}
		}
		else if(_elementals != null)
		{
			Elementals elm = getElemental(element);
			if(elm != null)
			{
				return elm.getValue();
			}
		}
		return 0;
	}

	private void applyAttribute(byte element, int value)
	{
		if(_elementals == null)
		{
			_elementals = new Elementals[1];
			_elementals[0] = new Elementals(element, value);
		}
		else
		{
			Elementals elm = getElemental(element);
			if(elm != null)
			{
				elm.setValue(value);
			}
			else
			{
				elm = new Elementals(element, value);
				Elementals[] array = new Elementals[_elementals.length + 1];
				System.arraycopy(_elementals, 0, array, 0, _elementals.length);
				array[_elementals.length] = elm;
				_elementals = array;
			}
		}
	}

	/**
	 * Add elemental attribute to item and save to db
	 *
	 * @param element
	 * @param value
	 */
	public void setElementAttr(byte element, int value)
	{
		applyAttribute(element, value);
		updateItemElements(null);
	}

	/**
	 * Remove elemental from item
	 *
	 * @param element byte element to remove, -1 for all elementals remove
	 */
	public void clearElementAttr(byte element)
	{
		if(getElemental(element) == null && element != -1)
		{
			return;
		}

		Elementals[] array = null;
		if(element != -1 && _elementals != null && _elementals.length > 1)
		{
			array = new Elementals[_elementals.length - 1];
			int i = 0;
			for(Elementals elm : _elementals)
			{
				if(elm.getElement() != element)
				{
					array[i++] = elm;
				}
			}
		}
		_elementals = array;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(element == -1)
			{
				// Remove the entries
				statement = con.prepareStatement(Items.ITEM_ELEMENTALS_REMOVE);
			}
			else
			{
				//Item can have still others
				statement = con.prepareStatement(Items.ITEM_ELEMENTALS_REMOVE_BY_TYPE);
				statement.setInt(2, element);
			}

			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not remove elemental enchant for item: " + this + " from DB:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Returns true if this item is a shadow item
	 * Shadow items have a limited life-time
	 *
	 * @return
	 */
	public boolean isShadowItem()
	{
		return _mana >= 0;
	}

	/**
	 * Returns the remaining mana of this shadow item
	 *
	 * @return lifeTime
	 */
	public int getMana()
	{
		return _mana;
	}

	/**
	 * Decreases the mana of this shadow item,
	 * sends a inventory update
	 * schedules a new consumption task if non is running
	 * optionally one could force a new task
	 *
	 * @param resetConsumingMana a new consumption task if item is equipped
	 */
	public void decreaseMana(boolean resetConsumingMana)
	{
		decreaseMana(resetConsumingMana, 1);
	}

	/**
	 * Decreases the mana of this shadow item,
	 * sends a inventory update
	 * schedules a new consumption task if non is running
	 * optionally one could force a new task
	 *
	 * @param resetConsumingMana a new consumption task if item is equipped
	 * @param count  how much mana decrease
	 */
	public void decreaseMana(boolean resetConsumingMana, int count)
	{
		if(!isShadowItem())
		{
			return;
		}

		if(_mana - count >= 0)
		{
			_mana -= count;
		}
		else
		{
			_mana = 0;
		}

		if(_storedInDb)
		{
			_storedInDb = false;
		}
		if(resetConsumingMana)
		{
			_consumingMana = false;
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(_ownerId);
		if(player != null)
		{
			switch(_mana)
			{
				case 10:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10).addItemName(_item));
					break;
				case 5:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5).addItemName(_item));
					break;
				case 1:
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1).addItemName(_item));
					break;
			}

			if(_mana == 0) // The life time has expired
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0).addItemName(_item));

				// unequip
				if(isEquipped())
				{
					L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
					InventoryUpdate iu = new InventoryUpdate();
					for(L2ItemInstance item : unequiped)
					{
						player.checkSShotsMatch(null, item);
						iu.addModifiedItem(item);
					}
					player.sendPacket(iu);
					player.broadcastUserInfo();
				}

				if(_loc == ItemLocation.WAREHOUSE)
				{
					player.getWarehouse().destroyItem(ProcessType.EXPIRE, this, player, null);
				}
				else
				{
					// destroy
					player.getInventory().destroyItem(ProcessType.EXPIRE, this, player, null);

					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);

                    player.sendPacket(new ExUserInfoInvenWeight(player));
                    player.sendPacket(new ExAdenaInvenCount(player));
				}

				// delete from world
				WorldManager.getInstance().removeObject(this);
			}
			else
			{
				// Reschedule if still equipped
				if(!_consumingMana && isEquipped())
				{
					scheduleConsumeManaTask();
				}
				if(_loc != ItemLocation.WAREHOUSE)
				{
					InventoryUpdate iu = new InventoryUpdate();
					iu.addModifiedItem(this);
					player.sendPacket(iu);
				}
			}
		}
	}

	public void scheduleConsumeManaTask()
	{
		if(_consumingMana)
		{
			return;
		}
		_consumingMana = true;
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleConsumeManaTask(this), MANA_CONSUMPTION_RATE);
	}

	/**
	 * Returns the type of charge with SoulShot of the item.
	 *
	 * @return int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public int getChargedSoulshot()
	{
		return _chargedSoulshot;
	}

	/**
	 * Sets the type of charge with SoulShot of the item
	 *
	 * @param type : int (CHARGED_NONE, CHARGED_SOULSHOT)
	 */
	public void setChargedSoulshot(int type)
	{
		_chargedSoulshot = type;
	}

	/**
	 * Returns the type of charge with SpiritShot of the item
	 *
	 * @return int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public int getChargedSpiritshot()
	{
		return _chargedSpiritshot;
	}

	/**
	 * Sets the type of charge with SpiritShot of the item
	 *
	 * @param type : int (CHARGED_NONE, CHARGED_SPIRITSHOT, CHARGED_BLESSED_SPIRITSHOT)
	 */
	public void setChargedSpiritshot(int type)
	{
		_chargedSpiritshot = type;
	}

	public boolean getChargedFishshot()
	{
		return _chargedFishtshot;
	}

	public void setChargedFishshot(boolean type)
	{
		_chargedFishtshot = type;
	}

	/**
	 * This function basically returns a set of functions from
	 * L2Item/L2Armor/L2Weapon, but may add additional
	 * functions, if this particular item instance is enhanched
	 * for a particular player.
	 *
	 * @param player : L2Character designating the player
	 * @return Func[]
	 */
	public Func[] getStatFuncs(L2Character player)
	{
		return _item.getStatFuncs(this, player);
	}

	/**
	 * Updates the database.<BR>
	 */
	public void updateDatabase()
	{
		updateDatabase(false);
	}

	/**
	 * Updates the database.<BR>
	 *
	 * @param force if the update should necessarilly be done.
	 */
	public void updateDatabase(boolean force)
	{
		_dbLock.lock();
		try
		{
			if(_existsInDb)
			{
				if(_ownerId == 0 || _loc == ItemLocation.VOID || _loc == ItemLocation.REFUND || _count == 0L && _loc != ItemLocation.LEASE)
				{
					removeFromDb();
				}
				else if(!Config.LAZY_ITEMS_UPDATE || force)
				{
					updateInDb();
				}
			}
			else
			{
				if(_ownerId == 0 || _loc == ItemLocation.VOID || _loc == ItemLocation.REFUND || _count == 0L && _loc != ItemLocation.LEASE)
				{
					return;
				}
				insertIntoDb();
			}
		}
		finally
		{
			_dbLock.unlock();
		}
	}

	public void dropMe(L2Character dropper, int x, int y, int z)
	{
		ThreadPoolManager.getInstance().executeTask(new ItemDropTask(this, dropper, x, y, z));
	}

	/**
	 * Update the database with values of the item
	 */
	private void updateInDb()
	{
		assert _existsInDb;

		if(_wear)
		{
			return;
		}

		if(_storedInDb)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Items.UPDATE);
			statement.setInt(1, _ownerId);
			statement.setLong(2, _count);
			statement.setString(3, _loc.name());
			statement.setLong(4, _locData);
			statement.setInt(5, _enchantLevel);
			statement.setInt(6, _type1);
			statement.setInt(7, _type2);
			statement.setInt(8, _mana);
			statement.setLong(9, _time);
			statement.setInt(10, _skin);
			statement.setInt(11, getObjectId());
			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not update item " + this + " in DB: Reason: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Insert the item in database
	 */
	private void insertIntoDb()
	{
		assert !_existsInDb && getObjectId() != 0;

		if(_wear)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Items.INSERT);
			statement.setInt(1, _ownerId);
			statement.setInt(2, _itemId);
			statement.setLong(3, _count);
			statement.setString(4, _loc.name());
			statement.setLong(5, _locData);
			statement.setInt(6, _enchantLevel);
			statement.setInt(7, getObjectId());
			statement.setInt(8, _type1);
			statement.setInt(9, _type2);
			statement.setInt(10, _mana);
			statement.setLong(11, _time);
			statement.setInt(12, _skin);

			statement.executeUpdate();
			_existsInDb = true;
			_storedInDb = true;

			if(_augmentation != null)
			{
				updateItemAttributes(con);
			}
			if(_elementals != null)
			{
				updateItemElements(con);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not insert item " + this + " into DB: Reason: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Delete item from database
	 */
	private void removeFromDb()
	{
		assert _existsInDb;

		if(_wear)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Items.DELETE);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
			_existsInDb = false;
			_storedInDb = false;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not delete item " + this + " in DB: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Items.ITEM_ATTRIBUTES_REMOVE);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not delete item " + this + " in DB: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void resetOwnerTimer()
	{
		if(itemLootShedule != null)
		{
			itemLootShedule.cancel(true);
		}
		itemLootShedule = null;
	}

	public ScheduledFuture<?> getItemLootShedule()
	{
		return itemLootShedule;
	}

	public void setItemLootShedule(ScheduledFuture<?> sf)
	{
		itemLootShedule = sf;
	}

	public boolean isProtected()
	{
		return _protected;
	}

	public void setProtected(boolean is_protected)
	{
		_protected = is_protected;
	}

	public boolean isNightLure()
	{
		return _itemId >= 8505 && _itemId <= 8513 || _itemId == 8485;
	}

	public boolean getCountDecrease()
	{
		return _decrease;
	}

	public void setCountDecrease(boolean decrease)
	{
		_decrease = decrease;
	}

	public long getInitCount()
	{
		return _initCount;
	}

	public void setInitCount(int InitCount)
	{
		_initCount = InitCount;
	}

	public void restoreInitCount()
	{
		if(_decrease)
		{
			setCount(_initCount);
		}
	}

	public boolean isTimeLimitedItem()
	{
		return _time > 0;
	}

	/**
	 * Returns (current system time + time) of this time limited item
	 *
	 * @return Time
	 */
	public long getTime()
	{
		return _time;
	}

	public long getRemainingTime()
	{
		return _time - System.currentTimeMillis();
	}

	public void setRemainingTime(int time)
	{
		_time = System.currentTimeMillis() + time * 1000;
		scheduleLifeTimeTask();
	}

	public void endOfLife()
	{
		L2PcInstance player = WorldManager.getInstance().getPlayer(_ownerId);
		if(player != null)
		{
			if(isEquipped())
			{
				L2ItemInstance[] unequiped = player.getInventory().unEquipItemInSlotAndRecord(getLocationSlot());
				InventoryUpdate iu = new InventoryUpdate();
				for(L2ItemInstance item : unequiped)
				{
					player.checkSShotsMatch(null, item);
					iu.addModifiedItem(item);
				}
				player.sendPacket(iu);
				player.broadcastUserInfo();
			}

			if(_loc == ItemLocation.WAREHOUSE)
			{
				if(_skin == 0)
				{
					player.getWarehouse().destroyItem(ProcessType.EXPIRE, this, player, null);
				}
			}
			else
			{
				if(_skin == 0)
				{
					// destroy
					player.getInventory().destroyItem(ProcessType.EXPIRE, this, player, null);
					// send update
					InventoryUpdate iu = new InventoryUpdate();
					iu.addRemovedItem(this);
					player.sendPacket(iu);

                    player.sendPacket(new ExUserInfoInvenWeight(player));
                    player.sendPacket(new ExAdenaInvenCount(player));
				}
			}
			// delete from world
			if(_skin == 0)
			{
				player.sendPacket(SystemMessageId.TIME_LIMITED_ITEM_DELETED);
				WorldManager.getInstance().removeObject(this);
			}
			else
			{
				// Снимаем скин
				setSkin(0);
				_time = 0;
			}
		}
	}

	public void scheduleLifeTimeTask()
	{
		if(!isTimeLimitedItem())
		{
			return;
		}
		if(getRemainingTime() <= 0)
		{
			endOfLife();
		}
		else
		{
			if(_lifeTimeTask != null)
			{
				_lifeTimeTask.cancel(false);
			}
			_lifeTimeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleLifeTimeTask(this), getRemainingTime());
		}
	}

	public void updateElementAttrBonus(L2PcInstance player)
	{
		if(_elementals == null)
		{
			return;
		}
		for(Elementals elm : _elementals)
		{
			elm.updateBonus(player, isArmor());
		}
	}

	public void removeElementAttrBonus(L2PcInstance player)
	{
		if(_elementals == null)
		{
			return;
		}
		for(Elementals elm : _elementals)
		{
			elm.removeBonus(player);
		}
	}

	public int getDropperObjectId()
	{
		return _dropperObjectId;
	}

	public void setDropperObjectId(int id)
	{
		_dropperObjectId = id;
	}

	public DropProtection getDropProtection()
	{
		return _dropProtection;
	}

	public boolean isPublished()
	{
		return _published;
	}

	public void publish()
	{
		_published = true;
	}

	@Override
	public boolean onDecay()
	{
		if(Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}

		return super.onDecay();
	}

	/**
	 * Returns false cause item can't be attacked
	 *
	 * @return boolean false
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return false;
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new NullKnownList(this));
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if(_dropperObjectId == 0)
		{
			activeChar.sendPacket(new SpawnItem(this));
		}
		else
		{
			activeChar.sendPacket(new DropItem(this, _dropperObjectId));
		}
	}

	/**
	 * @return the item in String format
	 */
	@Override
	public String toString()
	{
		return _item + "[" + getObjectId() + ']';
	}

	public boolean isQuestItem()
	{
		return _item.isQuestItem();
	}

	public boolean isElementable()
	{
		if(_loc == ItemLocation.INVENTORY || _loc == ItemLocation.PAPERDOLL)
		{
			return _item.isElementable();
		}
		return false;
	}

	public boolean isFreightable()
	{
		return _item.isFreightable();
	}

	public int useSkillDisTime()
	{
		return _item.useSkillDisTime();
	}

	public int getDefaultEnchantLevel()
	{
		return _item.getDefaultEnchantLevel();
	}

	public boolean hasPassiveSkills()
	{
		return getItemType() == L2EtcItemType.RUNE && _loc == ItemLocation.INVENTORY && _ownerId > 0 && _item.hasSkills();
	}

	public void giveSkillsToOwner()
	{
		if(!hasPassiveSkills())
		{
			return;
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(_ownerId);

		if(player != null)
		{
			for(SkillHolder sh : _item.getSkills())
			{
				if(sh.getSkill().isPassive()) // Добавляем в скиллист только если скилл предмета пассивный
				{
					player.addSkill(sh.getSkill(), false);
				}
			}
			player.refreshExpertisePenalty();
		}
	}

	public void removeSkillsFromOwner()
	{
		if(!hasPassiveSkills())
		{
			return;
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(_ownerId);

		if(player != null)
		{
			for(SkillHolder sh : _item.getSkills())
			{
				if(sh.getSkill().isPassive())
				{
					player.removeSkill(sh.getSkill(), false, true);
				}
			}
			player.refreshExpertisePenalty();
		}
	}

	public boolean isFeatherOfBlessing()
	{
		return _item.isFeatherOfBlessing();
	}

	public boolean isBlessedItem()
	{
		return _item.isBlessedItem();
	}

	public boolean isInfinityItem()
	{
		return _item.isInfinityItem();
	}

	public int getEquipReuseDelay()
	{
		return _item.getEquipReuseDelay();
	}

	/**
	 * @return Внешний вид оружия / брани
	 */
	public int getSkin()
	{
		return _skin;
	}

	public void setSkin(int id)
	{
		_skin = id;
		_storedInDb = false;
	}

	// TODO: Implement me!
	public int isBlocked()
	{
		return 1;
	}

    public boolean isAdena() 
    {
        return getItemId() == 57;
    }

    /**
	 * Enumeration of locations for item
	 */
	public static enum ItemLocation
	{
		VOID,
		INVENTORY,
		PAPERDOLL,
		WAREHOUSE,
		CLANWH,
		PET,
		PET_EQUIP,
		LEASE,
		REFUND,
		MAIL,
		FREIGHT,
		COMMISSION,
		GOODS
	}

	/**
	 * Used to decrease mana
	 * (mana means life time for shadow items)
	 */
	public static class ScheduleConsumeManaTask implements Runnable
	{
		private final L2ItemInstance _shadowItem;

		public ScheduleConsumeManaTask(L2ItemInstance item)
		{
			_shadowItem = item;
		}

		@Override
		public void run()
		{
			try
			{
				// decrease mana
				if(_shadowItem != null)
				{
					_shadowItem.decreaseMana(true);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while running ScheduleConsumeManaTask()", e);
			}
		}
	}

	public static class ScheduleLifeTimeTask implements Runnable
	{
		private final L2ItemInstance _limitedItem;

		public ScheduleLifeTimeTask(L2ItemInstance item)
		{
			_limitedItem = item;
		}

		@Override
		public void run()
		{
			try
			{
				if(_limitedItem != null)
				{
					_limitedItem.endOfLife();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	/**
	 * Init a dropped L2ItemInstance and add it in the world as a visible object.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion </li>
	 * <li>Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion</li>
	 * <li>Add the L2ItemInstance dropped in the world as a <B>visible</B> object</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects of L2World </B></FONT><BR><BR>
	 * <p/>
	 * <B><U> Assert </U> :</B><BR><BR>
	 * <li> _worldRegion == null <I>(L2Object is invisible at the beginning)</I></li><BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Drop item</li>
	 * <li> Call Pet</li><BR>
	 */
	public class ItemDropTask implements Runnable
	{
		private final L2Character _dropper;
		private final L2ItemInstance _itm;
		private int _x;
		private int _y;
		private int _z;

		public ItemDropTask(L2ItemInstance item, L2Character dropper, int x, int y, int z)
		{
			_x = x;
			_y = y;
			_z = z;
			_dropper = dropper;
			_itm = item;
		}

		@Override
		public void run()
		{
			if(_itm.getLocationController().getWorldPosition() == null)
			{
				_log.error("Item with ObjectId [" + _itm.getObjectId() + "] has no world region where to drop! Item ID [" + _itm.getItemId() + "].");
				return;
			}

			if(Config.GEODATA_ENABLED && _dropper != null)
			{
				Location dropDest = GeoEngine.getInstance().moveCheck(_dropper.getX(), _dropper.getY(), _dropper.getZ(), _x, _y, _z, _dropper.getInstanceId());
				_x = dropDest.getX();
				_y = dropDest.getY();
				_z = dropDest.getZ();
			}

			if(_dropper != null)
			{
				getInstanceController().setInstanceId(_dropper.getInstanceId()); // Inherit instancezone when dropped in visible world
			}
			else
			{
				getInstanceController().setInstanceId(0); // No dropper? Make it a global item...
			}

			synchronized(_itm)
			{
				// Set the x,y,z position of the L2ItemInstance dropped and update its _worldregion
				_itm.getLocationController().setVisible(true);
				_itm.getLocationController().setXYZ(_x, _y, _z);

				// Add the L2ItemInstance dropped to _visibleObjects of its L2WorldRegion
				_itm.getLocationController().getWorldRegion().addVisibleObject(_itm);
				_itm.setDropTime(System.currentTimeMillis());
				_itm.setDropperObjectId(_dropper != null ? _dropper.getObjectId() : 0); //Set the dropper Id for the knownlist packets in sendInfo

				// this can synchronize on others instancies, so it's out of
				// synchronized, to avoid deadlocks
				// Add the L2ItemInstance dropped in the world as a visible object
				WorldManager.getInstance().addVisibleObject(_itm, _itm.getLocationController().getWorldRegion());
				if(Config.SAVE_DROPPED_ITEM)
				{
					ItemsOnGroundManager.getInstance().save(_itm);
				}
				_itm.setDropperObjectId(0); //Set the dropper Id back to 0 so it no longer shows the drop packet
			}
		}
	}
}