package dwo.gameserver.model.items;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.instancemanager.TransformationManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.formation.group.PartyExitReason;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExRedSky;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.database.DatabaseUtils;
import dwo.gameserver.util.geometry.Point3D;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

public class CursedWeapon
{
	private static final Logger _log = LogManager.getLogger(CursedWeaponsManager.class);
	private static final int[] TRANSFORM_IDS = {3630, 3631};
	// _name is the name of the cursed weapon associated with its ID.
	private final String _name;
	// _itemId is the Item ID of the cursed weapon.
	private final int _itemId;
	// _skillId is the skills ID.
	private final int _skillId;
	private final int _skillMaxLevel;
	protected L2PcInstance _player;
	protected int transformationId;
	private double _dropRate;
	private int _duration;
	private int _durationLost;
	private int _disapearChance;
	private int _stageKills;
	// this should be false unless if the cursed weapon is dropped, in that case it would be true.
	private boolean _isDropped;
	// this sets the cursed weapon status to true only if a player has the cursed weapon, otherwise this should be false.
	private boolean _isActivated;
	private ScheduledFuture<?> _removeTask;
	private int _nbKills;
	private long _endTime;
	private int _playerId;
	private L2ItemInstance _item;
	private int _playerReputation;
	private int _playerPkKills;

	public CursedWeapon(int itemId, int skillId, String name)
	{
		_name = name;
		_itemId = itemId;
		_skillId = skillId;
		_skillMaxLevel = SkillTable.getInstance().getMaxLevel(_skillId);
	}

	public void endOfLife()
	{
		if(_isActivated)
		{
			if(_player != null && _player.isOnline())
			{
				// Remove from player
				_log.log(Level.INFO, _name + " being removed online.");

				_player.abortAttack();

				_player.setReputation(_playerReputation);
				_player.setPkKills(_playerPkKills);
				_player.setCursedWeaponEquippedId(0);
                _player.untransform(true);
				removeSkill();

				// Remove
				_player.getInventory().unEquipItemInBodySlot(L2Item.SLOT_LR_HAND);
				_player.store();

				// Destroy
				L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId(ProcessType.EXPIRE, _itemId, 1, _player, null);
				if(removedItem != null)
				{
					if(Config.FORCE_INVENTORY_UPDATE)
					{
						_player.sendPacket(new ItemList(_player, true));
					}
					else
					{
						InventoryUpdate iu = new InventoryUpdate();
						if(removedItem.getCount() == 0)
						{
							iu.addRemovedItem(removedItem);
						}
						else
						{
							iu.addModifiedItem(removedItem);
						}

						_player.sendPacket(iu);
					}
				}
				_player.broadcastUserInfo();

			}
			else
			{
				// Remove from Db
				_log.log(Level.INFO, _name + " being removed offline.");

				ThreadConnection con = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();

					// Delete the item
					FiltredPreparedStatement statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
					statement.setInt(1, _playerId);
					statement.setInt(2, _itemId);
					if(statement.executeUpdate() != 1)
					{
						_log.log(Level.ERROR, "Error while deleting itemId " + _itemId + " from userId " + _playerId);
					}
					DatabaseUtils.closeStatement(statement);

					// Restore the karma
					statement = con.prepareStatement(Characters.UPDATE_CHAR_REPUTATION_PKKILLS);
					statement.setInt(1, _playerReputation);
					statement.setInt(2, _playerPkKills);
					statement.setInt(3, _playerId);
					if(statement.executeUpdate() != 1)
					{
						_log.log(Level.ERROR, "Error while updating reputation & pkkills for userId " + _playerId);
					}

					DatabaseUtils.closeStatement(statement);
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Could not delete : " + e.getMessage(), e);
				}
				finally
				{
					DatabaseUtils.closeConnection(con);
				}
			}
		}
		else
		{
			// either this cursed weapon is in the inventory of someone who has another cursed weapon equipped,
			// OR this cursed weapon is on the ground.
			if(_player != null && _player.getInventory().getItemByItemId(_itemId) != null)
			{
				// Destroy
				L2ItemInstance removedItem = _player.getInventory().destroyItemByItemId(ProcessType.EXPIRE, _itemId, 1, _player, null);
				if(Config.FORCE_INVENTORY_UPDATE)
				{
					_player.sendPacket(new ItemList(_player, true));
				}
				else
				{
					InventoryUpdate iu = new InventoryUpdate();
					if(removedItem.getCount() == 0)
					{
						iu.addRemovedItem(removedItem);
					}
					else
					{
						iu.addModifiedItem(removedItem);
					}

					_player.sendPacket(iu);
				}

				_player.broadcastUserInfo();
			}
			//  is dropped on the ground
			else if(_item != null)
			{
				_item.getLocationController().decay();
				WorldManager.getInstance().removeObject(_item);
				_log.log(Level.INFO, _name + " item has been removed from World.");
			}
		}

		// Delete infos from table if any
		CursedWeaponsManager.getInstance().removeFromDb(_itemId);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
		sm.addItemName(_itemId);
		CursedWeaponsManager.announce(sm);

		// Reset  state
		cancelTask();
		_isActivated = false;
		_isDropped = false;
		_endTime = 0;
		_player = null;
		_playerId = 0;
		_playerReputation = 0;
		_playerPkKills = 0;
		_item = null;
		_nbKills = 0;
	}

	private void cancelTask()
	{
		if(_removeTask != null)
		{
			_removeTask.cancel(true);
			_removeTask = null;
		}
	}

	private void dropIt(L2Attackable attackable, L2PcInstance player)
	{
		dropIt(attackable, player, null, true);
	}

	private void dropIt(L2Attackable attackable, L2PcInstance player, L2Character killer, boolean fromMonster)
	{
		_isActivated = false;

		if(fromMonster)
		{
			_item = attackable.dropItem(player, _itemId, 1);
			_item.setDropTime(0); // Prevent item from being removed by ItemsAutoDestroy

			// RedSky and EarthQuake
			ExRedSky packet = new ExRedSky(10);
			EarthQuake eq = new EarthQuake(player.getX(), player.getY(), player.getZ(), 14, 3);
			Broadcast.toAllOnlinePlayers(packet);
			Broadcast.toAllOnlinePlayers(eq);
		}
		else
		{
			_item = _player.getInventory().getItemByItemId(_itemId);
			_player.dropItem(ProcessType.DROP, _item, killer, true);
			_player.setReputation(_playerReputation);
			_player.setPkKills(_playerPkKills);
			_player.setCursedWeaponEquippedId(0);
			removeSkill();
			_player.abortAttack();
			//L2ItemInstance item = _player.getInventory().getItemByItemId(_itemId);
			//_player.getInventory().dropItem("DieDrop", item, _player, null);
			//_player.getInventory().getItemByItemId(_itemId).dropMe(_player, _player.getX(), _player.getY(), _player.getZ());
		}
		_isDropped = true;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
		if(player != null)
		{
			sm.addZoneName(player.getX(), player.getY(), player.getZ()); // Region Name
		}
		else if(_player != null)
		{
			sm.addZoneName(_player.getX(), _player.getY(), _player.getZ()); // Region Name
		}
		else
		{
			sm.addZoneName(killer.getX(), killer.getY(), killer.getZ()); // Region Name
		}
		sm.addItemName(_itemId);
		CursedWeaponsManager.announce(sm); // in the Hot Spring region
	}

	public void cursedOnLogin()
	{
		doTransform();
		giveSkill();

		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION);
		msg.addZoneName(_player.getX(), _player.getY(), _player.getZ());
		msg.addItemName(_player.getCursedWeaponEquippedId());
		CursedWeaponsManager.announce(msg);

		CursedWeapon cw = CursedWeaponsManager.getInstance().getCursedWeapon(_player.getCursedWeaponEquippedId());
		SystemMessage msg2 = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
		int timeLeft = (int) (cw.getTimeLeft() / 60000);
		msg2.addItemName(_player.getCursedWeaponEquippedId());
		msg2.addNumber(timeLeft);
		_player.sendPacket(msg2);
	}

	/**
	 * Yesod:<br>
	 * Rebind the passive skill belonging to the CursedWeapon. Invoke this
	 * method if the weapon owner switches to a subclass.
	 */
	public void giveSkill()
	{
		int level = 1 + _nbKills / _stageKills;
		if(level > _skillMaxLevel)
		{
			level = _skillMaxLevel;
		}

		L2Skill skill = SkillTable.getInstance().getInfo(_skillId, level);
		// Yesod:
		// To properly support subclasses this skill can not be stored.
		_player.addSkill(skill, false);

		// Void Burst, Void Flow
		skill = SkillTable.FrequentSkill.VOID_BURST.getSkill();
		_player.addSkill(skill, false);
		skill = SkillTable.FrequentSkill.VOID_FLOW.getSkill();
		_player.addSkill(skill, false);
		_player.setTransformAllowedSkills(TRANSFORM_IDS);
		_player.sendSkillList();
	}

	public void doTransform()
	{
		if(_itemId == 8689)
		{
			transformationId = 302;
		}
		else if(_itemId == 8190)
		{
			transformationId = 301;
		}

		if(_player.isTransformed() || _player.isInStance())
		{
			_player.stopTransformation(true);

			ThreadPoolManager.getInstance().scheduleGeneral(() -> TransformationManager.getInstance().transformPlayer(transformationId, _player), 500);
		}
		else
		{
			TransformationManager.getInstance().transformPlayer(transformationId, _player);
		}
	}

	public void removeSkill()
	{
		_player.removeSkill(_skillId);
		_player.removeSkill(SkillTable.FrequentSkill.VOID_BURST.getSkill().getId());
		_player.removeSkill(SkillTable.FrequentSkill.VOID_FLOW.getSkill().getId());
		_player.untransform(true);
		_player.sendSkillList();
    }

	public void reActivate()
	{
		_isActivated = true;
		if(_endTime - System.currentTimeMillis() <= 0)
		{
			endOfLife();
		}
		else
		{
			_removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);
		}

	}

	public boolean checkDrop(L2Attackable attackable, L2PcInstance player)
	{
		if(Rnd.getChance(_dropRate))
		{
			// Drop the item
			dropIt(attackable, player);

			// Start the Life Task
			_endTime = System.currentTimeMillis() + _duration * 60000L;
			_removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), _durationLost * 12000L, _durationLost * 12000L);

			return true;
		}

		return false;
	}

	public void activate(L2PcInstance player, L2ItemInstance item)
	{
		// If the player is mounted, attempt to unmount first.
		// Only allow picking up the cursed weapon if unmounting is successful.
		if(player.isMounted() && !player.dismount())
		{
			// TODO: Verify the following system message, may still be custom.
			player.sendPacket(SystemMessageId.FAILED_TO_PICKUP_S1);
			player.dropItem(ProcessType.DROP, item, null, true);
			return;
		}

		_isActivated = true;

		// Player holding it data
		_player = player;
		_playerId = _player.getObjectId();
		_playerReputation = _player.getReputation();
		_playerPkKills = _player.getPkKills();
		saveData();

		// Change player stats
		_player.setCursedWeaponEquippedId(_itemId);
		_player.setReputation(-9999999);
		_player.setPkKills(0);
		if(_player.isInParty())
		{
			_player.getParty().removePartyMember(_player, PartyExitReason.EXPELLED);
		}

		// Disable All Skills
		// Do Transform
		doTransform();
		// Add skill
		giveSkill();

		// Equip with the weapon
		_item = item;
		//L2ItemInstance[] items =
		_player.getInventory().equipItem(_item);
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(_item);
		_player.sendPacket(sm);

		// Fully heal player
		_player.setCurrentHpMp(_player.getMaxHp(), _player.getMaxMp());
		_player.setCurrentCp(_player.getMaxCp());

		// Refresh inventory
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		else
		{
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			//iu.addItems(Arrays.asList(items));
			_player.sendPacket(iu);
		}

		// Refresh player stats
		_player.broadcastUserInfo();

		SocialAction atk = new SocialAction(_player.getObjectId(), 17);

		_player.broadcastPacket(atk);

		sm = SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
		sm.addZoneName(_player.getX(), _player.getY(), _player.getZ()); // Region Name
		sm.addItemName(_item);
		CursedWeaponsManager.announce(sm);
	}

	public void saveData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete previous datas
			statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
			statement.setInt(1, _itemId);
			statement.executeUpdate();

			if(_isActivated)
			{
				statement = con.prepareStatement("INSERT INTO cursed_weapons (itemId, charId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)");
				statement.setInt(1, _itemId);
				statement.setInt(2, _playerId);
				statement.setInt(3, _playerReputation);
				statement.setInt(4, _playerPkKills);
				statement.setInt(5, _nbKills);
				statement.setLong(6, _endTime);
				statement.executeUpdate();
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "CursedWeapon: Failed to save data.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void dropIt(L2Character killer)
	{
		if(Rnd.getChance(_disapearChance))
		{
			// Remove it
			endOfLife();
		}
		else
		{
			// Unequip & Drop
			dropIt(null, null, killer, false);
			// Reset player stats
			_player.setReputation(_playerReputation);
			_player.setPkKills(_playerPkKills);
			_player.setCursedWeaponEquippedId(0);
			removeSkill();

            _player.broadcastUserInfo();

            _player.abortAttack();
		}
	}

	public void increaseKills()
	{
		_nbKills++;

		if(_player != null && _player.isOnline())
		{
			_player.setPkKills(_nbKills);
			_player.sendUserInfo();

			if(_nbKills % _stageKills == 0 && _nbKills <= _stageKills * (_skillMaxLevel - 1))
			{
				giveSkill();
			}
		}
		// Reduce time-to-live
		_endTime -= _durationLost * 60000L;
		saveData();
	}

	public void setDisapearChance(int disapearChance)
	{
		_disapearChance = disapearChance;
	}

	public void setDropRate(double dropRate)
	{
		_dropRate = dropRate;
	}

	public void setDurationLost(int durationLost)
	{
		_durationLost = durationLost;
	}

	public void setItem(L2ItemInstance item)
	{
		_item = item;
	}

	public boolean isActivated()
	{
		return _isActivated;
	}

	public void setActivated(boolean isActivated)
	{
		_isActivated = isActivated;
	}

	public boolean isDropped()
	{
		return _isDropped;
	}

	public void setDropped(boolean isDropped)
	{
		_isDropped = isDropped;
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public void setEndTime(long endTime)
	{
		_endTime = endTime;
	}

	public String getName()
	{
		return _name;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getSkillId()
	{
		return _skillId;
	}

	public int getPlayerId()
	{
		return _playerId;
	}

	public void setPlayerId(int playerId)
	{
		_playerId = playerId;
	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	public void setPlayer(L2PcInstance player)
	{
		_player = player;
	}

	public int getPlayerReputation()
	{
		return _playerReputation;
	}

	public void setPlayerReputation(int playerReputation)
	{
		_playerReputation = playerReputation;
	}

	public int getPlayerPkKills()
	{
		return _playerPkKills;
	}

	public void setPlayerPkKills(int playerPkKills)
	{
		_playerPkKills = playerPkKills;
	}

	public int getNbKills()
	{
		return _nbKills;
	}

	public void setNbKills(int nbKills)
	{
		_nbKills = nbKills;
	}

	public int getStageKills()
	{
		return _stageKills;
	}

	public void setStageKills(int stageKills)
	{
		_stageKills = stageKills;
	}

	public boolean isActive()
	{
		return _isActivated || _isDropped;
	}

	public int getLevel()
	{
		return _nbKills > _stageKills * _skillMaxLevel ? _skillMaxLevel : _nbKills / _stageKills;
	}

	public long getTimeLeft()
	{
		return _endTime - System.currentTimeMillis();
	}

	public void goTo(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}

		if(_isActivated && _player != null)
		{
			// Go to player holding the weapon
			player.teleToLocation(_player.getX(), _player.getY(), _player.getZ() + 20, true);
		}
		else if(_isDropped && _item != null)
		{
			// Go to item on the ground
			player.teleToLocation(_item.getX(), _item.getY(), _item.getZ() + 20, true);
		}
		else
		{
			player.sendMessage(_name + " isn't in the World.");
		}
	}

	public Point3D getWorldPosition()
	{
		if(_isActivated && _player != null)
		{
			return _player.getLocationController().getWorldPosition();
		}

		if(_isDropped && _item != null)
		{
			return _item.getLocationController().getWorldPosition();
		}

		return null;
	}

	public long getDuration()
	{
		return _duration;
	}

	public void setDuration(int duration)
	{
		_duration = duration;
	}

	private class RemoveTask implements Runnable
	{
		protected RemoveTask()
		{
		}

		@Override
		public void run()
		{
			if(System.currentTimeMillis() >= getEndTime())
			{
				endOfLife();
			}
		}
	}
}