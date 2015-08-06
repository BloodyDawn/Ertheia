package dwo.gameserver.model.actor.instance;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.handler.ItemHandler;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.instancemanager.ItemsOnGroundManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.stat.PetStat;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.items.itemcontainer.PetInventory;
import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.npc.L2PetData;
import dwo.gameserver.model.world.npc.L2PetLevelData;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ActionFail;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.StopMove;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetInventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetItemList;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class L2PetInstance extends L2Summon
{
	public static final String RESTORE_SKILL_SAVE = "SELECT petObjItemId,skill_id,skill_level,effect_count,effect_cur_time,buff_index FROM character_pet_skills_save WHERE petObjItemId=? ORDER BY buff_index ASC";
	public static final String DELETE_SKILL_SAVE = "DELETE FROM character_pet_skills_save WHERE petObjItemId=?";
	protected static final Logger _logPet = LogManager.getLogger(L2PetInstance.class);
	private static final String ADD_SKILL_SAVE = "INSERT INTO character_pet_skills_save (petObjItemId,skill_id,skill_level,effect_count,effect_cur_time,buff_index) VALUES (?,?,?,?,?,?)";
	private final PetInventory _inventory;
	private final int _controlObjectId;
	private final boolean _mountable;
	private int _curFed;
	private boolean _respawned;
	private boolean _isOnline;
	private Future<?> _feedTask;
	private L2PetData _data;
	private L2PetLevelData _leveldata;

	/**
	 * The Experience before the last Death Penalty
	 */
	private long _expBeforeDeath;
	private int _curWeightPenalty;

	/**
	 * Constructor for new pet
	 *
	 * @param objectId
	 * @param template
	 * @param owner
	 * @param control
	 */
	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		this(objectId, template, owner, control, (byte) (template.getIdTemplate() == 12564 ? owner.getLevel() : template.getLevel()));
	}

	/**
	 * Constructor for restored pet
	 *
	 * @param objectId
	 * @param template
	 * @param owner
	 * @param control
	 * @param level
	 */
	public L2PetInstance(int objectId, L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control, byte level)
	{
		super(objectId, template, owner);

		_controlObjectId = control.getObjectId();

		getStat().setLevel((byte) Math.max(level, PetDataTable.getInstance().getPetMinLevel(template.getNpcId())));

		_inventory = new PetInventory(this);
		_inventory.restore();

		int npcId = template.getNpcId();
		_mountable = PetDataTable.isMountable(npcId);
		getPetData();
		getPetLevelData();

		if(_data.syncLevel())
		{
			getStat().setLevel((byte) getOwner().getLevel());
			getStat().setExp(getStat().getExpForLevel(getOwner().getLevel()));
		}

		restoreEffects();
		_isOnline = true;
	}

	public static L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control)
	{
		synchronized(L2PetInstance.class)
		{
			if(owner.hasPet())
			{
				return null;
			}
			L2PetInstance pet = restore(control, template, owner);
			// add the pet instance to world
			if(pet != null)
			{
				pet.setTitle(owner.getName());
				WorldManager.getInstance().addPet(pet);
			}
			else
			{
				owner.sendMessage("restore(control, template, owner) == null");
			}

			return pet;
		}
	}

	private static L2PetInstance restore(L2ItemInstance control, L2NpcTemplate template, L2PcInstance owner)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			L2PetInstance pet;
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");
			statement.setInt(1, control.getObjectId());
			rset = statement.executeQuery();
			if(!rset.next())
			{
				pet = template.isType("L2BabyPet") ? new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control) : new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control);
				return pet;
			}

			pet = template.isType("L2BabyPet") ? new L2BabyPetInstance(IdFactory.getInstance().getNextId(), template, owner, control, rset.getByte("level")) : new L2PetInstance(IdFactory.getInstance().getNextId(), template, owner, control, rset.getByte("level"));

			pet._respawned = true;
			pet.setName(rset.getString("name"));

			long exp = rset.getLong("exp");
			L2PetLevelData info = PetDataTable.getInstance().getPetLevelData(pet.getNpcId(), pet.getLevel());
			// DS: update experience based by level
			// Avoiding pet delevels due to exp per level values changed.
			if(info != null && exp < info.getPetMaxExp())
			{
				exp = info.getPetMaxExp();
			}

			pet.getStat().setExp(exp);
			pet.getStat().setSp(rset.getInt("sp"));

			pet.getStatus().setCurrentHp(rset.getDouble("curHp"));
			pet.getStatus().setCurrentMp(rset.getDouble("curMp"));
			pet.getStatus().setCurrentCp(pet.getMaxCp());
			if(rset.getDouble("curHp") < 0.5)
			{
				pet.setIsDead(true);
				pet.stopHpMpRegeneration();
			}

			pet.setCurrentFed(rset.getInt("fed"));
			return pet;
		}
		catch(Exception e)
		{
			_logPet.log(Level.ERROR, "Could not restore pet data for owner: " + owner + " - " + e.getMessage(), e);
			return null;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public L2PetLevelData getPetLevelData()
	{
		if(_leveldata == null)
		{
			_leveldata = PetDataTable.getInstance().getPetLevelData(getTemplate().getNpcId(), getStat().getLevel());
		}

		return _leveldata;
	}

	public L2PetData getPetData()
	{
		if(_data == null)
		{
			_data = PetDataTable.getInstance().getPetData(getTemplate().getNpcId());
		}

		return _data;
	}

	public void setPetData(L2PetLevelData value)
	{
		_leveldata = value;
	}

	@Override
	public int getSummonType()
	{
		return 2;
	}

	/**
	 * @return Returns the mount able.
	 */
	@Override
	public boolean isMountable()
	{
		return _mountable;
	}

	@Override
	public long getExpForThisLevel()
	{
		return getStat().getExpForLevel(getLevel());
	}

	@Override
	public long getExpForNextLevel()
	{
		return getStat().getExpForLevel(getLevel() + 1);
	}

	@Override
	public short getSoulShotsPerHit()
	{
		return getPetLevelData().getPetSoulShot();
	}

	@Override
	public short getSpiritShotsPerHit()
	{
		return getPetLevelData().getPetSpiritShot();
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}
		stopFeed();
		sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS);
		// do not decrease exp if is in duel, arena
		L2PcInstance owner = getOwner();
		if(owner != null && !owner.isInDuel() && (!isInsideZone(ZONE_PVP) || isInsideZone(ZONE_SIEGE)))
		{
			deathPenalty();
		}

		if(isPhoenixBlessed() && getOwner() != null)
		{
			getOwner().reviveRequest(getOwner(), null, getObjectId());
		}

		return true;
	}

	@Override
	public PetStat getStat()
	{
		return (PetStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new PetStat(this));
	}

	@Override
	public void unSummon(boolean ignoreDeathAndVis)
	{
		if(unSummon(getOwner(), ignoreDeathAndVis))
		{
			getLocationController().decay();
		}
	}

	@Override
	protected boolean unSummon(L2PcInstance owner, boolean ignoreDeathAndVis)
	{
		synchronized(this)
		{
			stopFeed();
			stopHpMpRegeneration();

			if(!super.unSummon(owner, false))
			{
				return false;
			}

			if(!isDead())
			{
				if(_inventory != null)
				{
					_inventory.deleteMe();
				}
				WorldManager.getInstance().removePet(this);
			}
			return true;
		}
	}

	@Override
	public int getControlObjectId()
	{
		return _controlObjectId;
	}

	@Override
	public PetInventory getInventory()
	{
		return _inventory;
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		startFeed();
		if(!isHungry())
		{
			setRunning();
		}
	}

	/**
	 * Returns the pet's currently equipped weapon instance (if any).
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		for(L2ItemInstance item : _inventory.getItems())
		{
			if(item.getItemLocation() == L2ItemInstance.ItemLocation.PET_EQUIP && item.getItem().getBodyPart() == L2Item.SLOT_R_HAND)
			{
				return item;
			}
		}

		return null;
	}

	/**
	 * Returns the pet's currently equipped weapon (if any).
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
		{
			return null;
		}

		return (L2Weapon) weapon.getItem();
	}

	@Override
	public boolean onDelete()
	{
		_inventory.transferItemsToOwner();
		destroyControlItem(getOwner(), false); // this should also delete the pet from the db
		return super.onDelete();
	}

	@Override
	protected void doPickupItem(L2Object object)
	{
		boolean follow = getFollowStatus();
		if(isDead())
		{
			return;
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		StopMove sm = new StopMove(getObjectId(), getX(), getY(), getZ(), getHeading());

		if(Config.DEBUG)
		{
			_logPet.log(Level.DEBUG, "Pet pickup pos: " + object.getX() + ' ' + object.getY() + ' ' + object.getZ());
		}

		broadcastPacket(sm);

		if(!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_logPet.log(Level.WARN, this + " trying to pickup wrong target." + object);
			getOwner().sendActionFailed();
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		// Cursed weapons
		if(CursedWeaponsManager.getInstance().isCursed(target.getItemId()))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
			return;
		}

		synchronized(target)
		{
			if(!target.isVisible())
			{
				getOwner().sendActionFailed();
				return;
			}
			if(!target.getDropProtection().tryPickUp(this))
			{
				sendPacket(ActionFail.STATIC_PACKET);
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target));
				return;
			}
			if(!_inventory.validateCapacity(target))
			{
				sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			if(!_inventory.validateWeight(target, target.getCount()))
			{
				sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
				return;
			}
			if(target.getOwnerId() != 0 && target.getOwnerId() != getOwner().getObjectId() && !getOwner().isInLooterParty(target.getOwnerId()))
			{
				getOwner().sendActionFailed();

				if(target.getItemId() == PcInventory.ADENA_ID)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addItemNumber(target.getCount()));
				}
				else if(target.getCount() > 1)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(target.getItemId()).addItemNumber(target.getCount()));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
				}
				return;
			}
			if(target.getItemLootShedule() != null && (target.getOwnerId() == getOwner().getObjectId() || getOwner().isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}

			// If owner is in party and it isnt finders keepers, distribute the item instead of stealing it -.-
			if(getOwner().isInParty() && getOwner().getParty().getLootDistribution() != PartyLootType.ITEM_LOOTER)
			{
				getOwner().getParty().distributeItem(getOwner(), target);
			}
			else
			{
				target.pickupMe(this);
			}

			if(Config.SAVE_DROPPED_ITEM) // item must be removed from ItemsOnGroundManager if is active
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}

		// Herbs
		if(target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
			if(handler == null)
			{
				_log.log(Level.ERROR, "No item handler registered for item ID " + target.getItemId() + '.');
			}
			else
			{
				handler.useItem(this, target, false);
			}

			ItemTable.getInstance().destroyItem(ProcessType.CONSUME, target, getOwner(), null);

			broadcastStatusUpdate();
		}
		else
		{
			if(target.getItemId() == PcInventory.ADENA_ID)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA).addItemNumber(target.getCount()));
			}
			else if(target.getEnchantLevel() > 0)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2).addNumber(target.getEnchantLevel()).addString(target.getName()));
			}
			else if(target.getCount() > 1)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S).addItemNumber(target.getCount()).addString(target.getName()));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1).addString(target.getName()));
			}

			_inventory.addItem(ProcessType.PICKUP, target, getOwner(), this);
			// FIXME Just send the updates if possible (old way wasn't working though)
			sendPacket(new PetItemList(this));
		}

		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		if(follow)
		{
			followOwner();
		}
	}

	@Override
	public void setRestoreSummon(boolean val)
	{
		_restoreSummon = val;
	}

	@Override
	public void updateAndBroadcastStatus(int val)
	{
		refreshOverloaded();
		super.updateAndBroadcastStatus(val);
	}

	@Override
	public boolean isHungry()
	{
		return _curFed < getPetData().getHungryLimit() / 100.0f * getPetLevelData().getPetMaxFeed();
	}

	@Override
	public int getWeapon()
	{
		L2ItemInstance weapon = _inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if(weapon != null)
		{
			return weapon.getItemId();
		}
		return 0;
	}

	@Override
	public int getArmor()
	{
		L2ItemInstance weapon = _inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if(weapon != null)
		{
			return weapon.getItemId();
		}
		return 0;
	}

	@Override
	public boolean isUncontrollable()
	{
		return _curFed == 0;
	}

	public boolean isRespawned()
	{
		return _respawned;
	}

	public L2ItemInstance getControlItem()
	{
		return getOwner().getInventory().getItemByObjectId(_controlObjectId);
	}

	public int getCurrentFed()
	{
		return _curFed;
	}

	public void setCurrentFed(int num)
	{
		_curFed = num > getMaxFed() ? getMaxFed() : num;
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a
	 * Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param itemId      : int Item identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(ProcessType process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);

		if(item == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}

		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		sendPacket(petIU);

		if(sendMessage)
		{
			if(count > 1)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addItemNumber(count));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
			}
		}

		return true;
	}

	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate
	 * packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param objectId    : int Item Instance identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(ProcessType process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);

		if(item == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return false;
		}

		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		sendPacket(petIU);

		if(sendMessage)
		{
			if(count > 1)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addItemNumber(count));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
			}
		}
		return true;
	}

	@Override
	public void doRevive(double revivePower)
	{
		// Restore the pet's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}

	@Override
	protected void broadcastModifiedStats(List<Stats> stats)
	{
		super.broadcastModifiedStats(stats);
	}

	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		if(getNpcId() == 12564) // SinEater
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.SINEATER_XP_RATE), addToSp);
		}
		else
		{
			getStat().addExpAndSp(Math.round(addToExp * Config.PET_XP_RATE), addToSp);
		}
	}

	@Override
	public int getSkillLevel(int skillId)
	{
		if(_data != null)
		{
			return _data.getAvailableLevel(skillId, getLevel());
		}

		if(getKnownSkill(skillId) == null)
		{
			return -1;
		}

		int lvl = getLevel();
		return lvl > 70 ? 7 + (lvl - 70) / 5 : lvl / 10;
	}

	@Override
	public int getLevel()
	{
		return getStat().getLevel();
	}

	@Override
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		return getStat().getCriticalHit(target, skill);
	}

	@Override
	public int getMAtk(L2Character target, L2Skill skill)
	{
		return getStat().getMAtk(target, skill);
	}

	@Override
	public int getMDef(L2Character target, L2Skill skill)
	{
		return getStat().getMDef(target, skill);
	}

	/**
	 * Transfers item to another inventory
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param objectId  : int Item Identifier of the item to be transfered
	 * @param count     : int Quantity of items to be transfered
	 * @param actor     : L2PcInstance Player requesting the item transfer
	 * @param reference : L2Object Object referencing current action like NPC selling
	 *                  item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item
	 *         in inventory
	 */
	public L2ItemInstance transferItem(ProcessType process, int objectId, long count, Inventory target, L2PcInstance actor, L2Object reference)
	{
		L2ItemInstance oldItem = _inventory.getItemByObjectId(objectId);

		if(oldItem == null)
		{
			return null;
		}

		L2ItemInstance playerOldItem = target.getItemByItemId(oldItem.getItemId());
		L2ItemInstance newItem = _inventory.transferItem(process, objectId, count, target, actor, reference);

		if(newItem == null)
		{
			return null;
		}

		// Send inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
		{
			petIU.addModifiedItem(oldItem);
		}
		else
		{
			petIU.addRemovedItem(oldItem);
		}
		sendPacket(petIU);

		// Send target update packet
		if(!newItem.isStackable())
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addNewItem(newItem);
			sendPacket(iu);
		}
		else if(playerOldItem != null && newItem.isStackable())
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(newItem);
			sendPacket(iu);
		}

		return newItem;
	}

	/**
	 * Remove the Pet from DB and its associated item from the player inventory
	 *
	 * @param owner The owner from whose invenory we should delete the item
	 */
	public void destroyControlItem(L2PcInstance owner, boolean evolve)
	{
		// remove the pet instance from world
		WorldManager.getInstance().removePet(this);

		// delete from inventory
		try
		{
			L2ItemInstance removedItem;
			if(evolve)
			{
				removedItem = owner.getInventory().destroyItem(ProcessType.EVOLVE, _controlObjectId, 1, getOwner(), this);
			}
			else
			{
				removedItem = owner.getInventory().destroyItem(ProcessType.PETDESTROY, _controlObjectId, 1, getOwner(), this);
				if(removedItem != null)
				{
					owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(removedItem));
				}
			}

			if(removedItem == null)
			{
				_log.log(Level.WARN, "Couldn't destroy pet control item for " + owner + " pet: " + this + " evolve: " + evolve);
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(removedItem);

				owner.sendPacket(iu);

				StatusUpdate su = new StatusUpdate(owner);
				su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
				owner.sendPacket(su);

				owner.broadcastUserInfo();

				WorldManager.getInstance().removeObject(removedItem);
			}
		}
		catch(Exception e)
		{
			_logPet.log(Level.ERROR, "Error while destroying control item: " + e.getMessage(), e);
		}

		// pet control item no longer exists, delete the pet from the db
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");
			statement.setInt(1, _controlObjectId);
			statement.execute();
		}
		catch(Exception e)
		{
			_logPet.log(Level.ERROR, "Failed to delete Pet [ObjectId: " + getObjectId() + ']', e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void dropAllItems()
	{
		try
		{
			for(L2ItemInstance item : _inventory.getItems())
			{
				dropItemHere(item);
			}
		}
		catch(Exception e)
		{
			_logPet.log(Level.ERROR, "Pet Drop Error: " + e.getMessage(), e);
		}
	}

	public void dropItemHere(L2ItemInstance dropit, boolean protect)
	{
		dropit = _inventory.dropItem(ProcessType.DROP, dropit.getObjectId(), dropit.getCount(), getOwner(), this);

		if(dropit != null)
		{
			if(protect)
			{
				dropit.getDropProtection().protect(getOwner());
			}
			_logPet.log(Level.INFO, "Item id to drop: " + dropit.getItemId() + " amount: " + dropit.getCount());
			dropit.dropMe(this, getX(), getY(), getZ() + 100);
		}
	}

	public void dropItemHere(L2ItemInstance dropit)
	{
		dropItemHere(dropit, false);
	}

	public void stopFeed()
	{
		synchronized(this)
		{
			if(_feedTask != null)
			{
				_feedTask.cancel(false);
				_feedTask = null;
			}
		}
	}

	public void startFeed()
	{
		synchronized(this)
		{
			// stop feeding task if its active

			stopFeed();
			if(!isDead() && getOwner().getPets().contains(this))
			{
				_feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
			}
		}
	}

	/**
	 * Restore the specified % of experience this L2PetInstance has lost.<BR>
	 * <BR>
	 */
	public void restoreExp(double restorePercent)
	{
		if(_expBeforeDeath > 0)
		{
			// Restore the specified % of lost experience.
			getStat().addExp(Math.round((_expBeforeDeath - getStat().getExp()) * restorePercent / 100));
			_expBeforeDeath = 0;
		}
	}

	private void deathPenalty()
	{
		// TODO Need Correct Penalty

		int lvl = getStat().getLevel();
		double percentLost = -0.07 * lvl + 6.5;

		// Calculate the Experience loss
		long lostExp = Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100);

		// Get the Experience before applying penalty
		_expBeforeDeath = getStat().getExp();

		// Set the new Experience value of the L2PetInstance
		getStat().addExp(-lostExp);
	}

	public int getMaxFed()
	{
		return getStat().getMaxFeed();
	}

	public void updateRefOwner(L2PcInstance owner)
	{
		setOwner(owner);
		WorldManager.getInstance().removePet(this);
		WorldManager.getInstance().addPet(this);
	}

	public int getInventoryLimit()
	{
		return Config.INVENTORY_MAXIMUM_PET;
	}

	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if(maxLoad > 0)
		{
			long weightproc = (getCurrentLoad() - getBonusWeightPenalty()) * 1000 / maxLoad;
			int newWeightPenalty;
			if(weightproc < 500 || getOwner().getDietMode())
			{
				newWeightPenalty = 0;
			}
			else if(weightproc < 666)
			{
				newWeightPenalty = 1;
			}
			else if(weightproc < 800)
			{
				newWeightPenalty = 2;
			}
			else
			{
				newWeightPenalty = weightproc < 1000 ? 3 : 4;
			}

			if(_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if(newWeightPenalty > 0)
				{
					addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() >= maxLoad);
				}
				else
				{
					removeSkill(getKnownSkill(4270));
					setIsOverloaded(false);
				}
			}
		}
	}

	public int getJewel()
	{
		L2ItemInstance weapon = _inventory.getPaperdollItem(Inventory.PAPERDOLL_NECK);
		if(weapon != null)
		{
			return weapon.getItemId();
		}
		return 0;
	}

	@Override
	public String getName()
	{
		return super.getName();
	}

	@Override
	public void setName(String name)
	{
		L2ItemInstance controlItem = getControlItem();
		if(controlItem != null)
		{
			if(controlItem.getCustomType2() == (name == null ? 1 : 0))
			{
				// name not set yet
				controlItem.setCustomType2(name != null ? 1 : 0);
				controlItem.updateDatabase();
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(controlItem);
				sendPacket(iu);
			}
		}
		else
		{
			_log.log(Level.ERROR, "Pet control item null!");
		}
		super.setName(name);
	}

	@Override
	public boolean isPet()
	{
		return true;
	}

	@Override
	public L2PetInstance getPetInstance()
	{
		return this;
	}

	public boolean canEatFoodId(int itemId)
	{
		return ArrayUtils.contains(_data.getFood(), itemId);
	}

	@Override
	public boolean isOnline()
	{
		return _isOnline;
	}

	@Override
	public void store()
	{
		if(_controlObjectId == 0)
		{
			// this is a summon, not a pet, don't store anything
			return;
		}

		String req;
		req = !_respawned ? "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,ownerId,restore,item_obj_id) " + "VALUES (?,?,?,?,?,?,?,?,?,?)" : "UPDATE pets SET name=?,level=?,curHp=?,curMp=?,exp=?,sp=?,fed=?,ownerId=?,restore=? " + "WHERE item_obj_id = ?";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(req);
			statement.setString(1, getName());
			statement.setInt(2, getStat().getLevel());
			statement.setDouble(3, getStatus().getCurrentHp());
			statement.setDouble(4, getStatus().getCurrentMp());
			statement.setLong(5, getStat().getExp());
			statement.setInt(6, getStat().getSp());
			statement.setInt(7, _curFed);
			statement.setInt(8, getOwner().getObjectId());
			statement.setString(9, String.valueOf(_restoreSummon)); // True restores pet on login
			statement.setInt(10, _controlObjectId);
			statement.executeUpdate();
			_respawned = true;
		}
		catch(Exception e)
		{
			_logPet.log(Level.ERROR, "Failed to store Pet [ObjectId: " + getObjectId() + "] data", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		L2ItemInstance itemInst = getControlItem();
		if(itemInst != null && itemInst.getEnchantLevel() != getStat().getLevel())
		{
			itemInst.setEnchantLevel(getStat().getLevel());
			itemInst.updateDatabase();
		}
	}

	@Override
	public void storeEffect(boolean storeEffects)
	{
		if(!Config.STORE_SKILL_COOLTIME)
		{
			return;
		}

		if(getOwner().getOlympiadController().isParticipating())
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete all current stored effects for summon to avoid dupe
			statement = con.prepareStatement(DELETE_SKILL_SAVE);
			statement.setInt(1, _controlObjectId);
			statement.execute();
			statement.clearParameters();

			int buff_index = 0;

			List<Integer> storedSkills = new ArrayList<>();

			//Store all effect data along with calculated remaining
			statement = con.prepareStatement(ADD_SKILL_SAVE);

			if(storeEffects)
			{
				for(L2Effect effect : getAllEffects())
				{
					if(effect == null)
					{
						continue;
					}

					switch(effect.getEffectType())
					{
						case HEAL_OVER_TIME:
						case CPHEAL_OVER_TIME:
							// TODO: Fix me.
						case HIDE:
							continue;
					}

					L2Skill skill = effect.getSkill();
					if(storedSkills.contains(skill.getReuseHashCode()))
					{
						continue;
					}

					storedSkills.add(skill.getReuseHashCode());

					if(!effect.isHerbEffect() && effect.isInUse() && !skill.isToggle())
					{
						statement.setInt(1, _controlObjectId);
						statement.setInt(2, skill.getId());
						statement.setInt(3, skill.getLevel());
						statement.setInt(4, effect.getCount());
						statement.setInt(5, effect.getTime());
						statement.setInt(6, ++buff_index);
						statement.execute();
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store pet effect data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Manage Feeding Task.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <li>Feed or kill the pet depending on hunger level</li> <li>If pet has
	 * food in inventory and feed level drops below 55% then consume food from
	 * inventory</li> <li>Send a broadcastStatusUpdate packet for this
	 * L2PetInstance</li><BR>
	 * <BR>
	 */

	class FeedTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(!getOwner().getPets().isEmpty())
				{
					boolean stopCurrentFeedTask = true;
					for(L2Summon pet : getOwner().getPets())
					{
						if(pet.getObjectId() == getObjectId())
						{
							stopCurrentFeedTask = false;
						}
					}
					if(stopCurrentFeedTask)
					{
						stopFeed();
						return;
					}
				}
				else if(getOwner().getPets().isEmpty())
				{
					stopFeed();
					return;
				}
				else if(getCurrentFed() > getFeedConsume())
				{
					// кушаем
					setCurrentFed(getCurrentFed() - getFeedConsume());
				}
				else
				{
					setCurrentFed(0);
				}

				broadcastStatusUpdate();

				int[] foodIds = getPetData().getFood();
				if(foodIds.length == 0)
				{
					if(getCurrentFed() == 0)
					{
						// Owl Monk remove PK
						if(getTemplate().getNpcId() == 16050 && getOwner() != null)
						{
							getOwner().setPkKills(Math.max(0, getOwner().getPkKills() - Rnd.get(1, 6)));
						}
						sendPacket(SystemMessageId.THE_HELPER_PET_LEAVING);
						getLocationController().delete();
					}
					else if(isHungry())
					{
						sendPacket(SystemMessageId.THERE_NOT_MUCH_TIME_REMAINING_UNTIL_HELPER_LEAVES);
					}
					return;
				}
				L2ItemInstance food = null;
				for(int id : foodIds)
				{
					food = getInventory().getItemByItemId(id);
					if(food != null)
					{
						break;
					}
				}
				if(isRunning() && isHungry())
				{
					setWalking();
				}
				else if(!isHungry() && !isRunning())
				{
					setRunning();
				}
				if(food != null && isHungry())
				{
					IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
					if(handler != null)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food.getItemId()));
						handler.useItem(L2PetInstance.this, food, false);
					}
				}
				else
				{
					if(getCurrentFed() == 0)
					{
						sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY);
					}
				}
			}
			catch(Exception e)
			{
				_logPet.log(Level.ERROR, "Pet [ObjectId: " + getObjectId() + "] a feed task error has occurred", e);
			}
		}

		/**
		 * @return количество поглащаемого корма в зависимости от боевого режима питомца
		 */
		private int getFeedConsume()
		{
			// если питомец в активном боевом состоянии
			return isAttackingNow() ? getPetLevelData().getPetFeedBattle() : getPetLevelData().getPetFeedNormal();
		}
	}
}