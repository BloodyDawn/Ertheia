/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.ai.L2DoorAI;
import dwo.gameserver.model.actor.knownlist.DoorKnownList;
import dwo.gameserver.model.actor.stat.DoorStat;
import dwo.gameserver.model.actor.status.DoorStatus;
import dwo.gameserver.model.actor.templates.L2DoorTemplate;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ActionFail;
import dwo.gameserver.network.game.serverpackets.ConfirmDlg;
import dwo.gameserver.network.game.serverpackets.DoorStatusUpdate;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.StaticObject;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class L2DoorInstance extends L2Character
{
	protected static final Logger log = LogManager.getLogger(L2DoorInstance.class);
	protected static ScheduledFuture<?> closeTask;
	private final L2DoorTemplate _doorTemplate;
	protected int _autoActionDelay = -1;
	private int _castleIndex = -2;
	private int _fortIndex = -2;
	private int _meshIndex = 1;
	private boolean _painted;
	private boolean _open;
	private boolean _targetable;
	private ClanHall _clanHall;
	private ScheduledFuture<?> _autoActionTask;
	private boolean _overrideAttackable;
	// Для DoorUpgrade замков
	private int _hpLevel = 100;

	public L2DoorInstance(int objectId, L2DoorTemplate template)
	{
		super(objectId, template.getCharTemplate());
		_doorTemplate = template;
		_open = template.isDefaultOpen();

		int[] position = template.getPosition();
		getLocationController().setXYZ(position[0], position[1], position[2], false);

		getStat(); // init stats
		getStatus(); // init status

		setCurrentHpMp(getMaxHp(), getMaxMp());

		_targetable = getName() == null || !getName().toLowerCase().contains("bridge"); // TODO: WTF?!
		setLethalable(false);
	}

	public void setIsAttackableDoor(boolean attackable)
	{
		_overrideAttackable = attackable;
	}

	public void setIsShowHp(boolean attackable)
	{
		// i am supportive
	}

	public boolean isUnlockable()
	{
		return _doorTemplate.isUnlockAble();
	}

	/**
	 * @return the doorId.
	 */
	public int getDoorId()
	{
		return _doorTemplate.getId();
	}

	public boolean isPainted()
	{
		return _painted;
	}

	public void setPainted(boolean painted)
	{
		_painted = painted;
	}

	/**
	 * @return {@code true} если дверь открыта
	 */
	public boolean isOpened()
	{
		return _open;
	}

	public void setOpen(boolean open)
	{
		setOpen(open, true);
	}

	/**
	 * @param open The open to set.
	 */
	public void setOpen(boolean open, boolean update)
	{
		synchronized(this)
		{
			if(open == _open)
			{
				return;
			}

			_open = open;

			if(update)
			{
				DoorGeoEngine.getInstance().updateDoor(this);
			}
		}
	}

	/**
	 * @return Doors that cannot be attacked during siege
	 * these doors will be auto opened if u take control of all commanders buildings
	 */
	public boolean isCommanderDoor()
	{
		return _doorTemplate.isCommanderDoor();
	}

	public boolean getDestroyAbleBySiegeSummon()
	{
		return _doorTemplate.isDestroyAbleBySiegeSummon();
	}

	/**
	 * Sets the delay in milliseconds for automatic opening/closing
	 * of this door instance.
	 * <BR>
	 * <B>Note:</B> A value of -1 cancels the auto open/close task.
	 *
	 * @param actionDelay actionDelay
	 */
	public void setAutoActionDelay(int actionDelay)
	{
		if(_autoActionDelay == actionDelay)
		{
			return;
		}

		if(_autoActionTask != null)
		{
			_autoActionTask.cancel(true);
		}

		_autoActionDelay = actionDelay;

		if(actionDelay > -1 && getInstanceId() == 0)
		{
			_autoActionTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AutoOpenClose(), actionDelay, actionDelay);
		}
	}

	public int getDamage()
	{
		int dmg = 6 - (int) Math.ceil(getCurrentHp() / getMaxHp() * 6);
		if(dmg > 6)
		{
			return 6;
		}
		if(dmg < 0)
		{
			return 0;
		}
		return dmg;
	}

	public Castle getCastle()
	{
		if(_castleIndex == -2)
		{
			_castleIndex = CastleManager.getInstance().getCastleIndex(_doorTemplate.getCastleId());
			if(_castleIndex < 0)
			{
				_castleIndex = -1;
			}
		}

		return _castleIndex == -1 ? null : CastleManager.getInstance().getCastles().get(_castleIndex);
	}

	public Fort getFort()
	{
		if(_fortIndex == -2)
		{
			_fortIndex = FortManager.getInstance().getFortIndex(_doorTemplate.getFortId());
			if(_fortIndex < 0)
			{
				_fortIndex = -1;
			}
		}

		return _fortIndex == -1 ? null : FortManager.getInstance().getForts().get(_fortIndex);
	}

	public ClanHall getClanHall()
	{
		return _clanHall;
	}

	public void setClanHall(ClanHall clanhall)
	{
		_clanHall = clanhall;
	}

	public boolean isEnemy()
	{
		if(getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getZone().isSiegeActive())
		{
			return true;
		}
		if(getFort() != null && getFort().getFortId() > 0 && getFort().getZone().isSiegeActive() && !isCommanderDoor())
		{
			return true;
		}
		return _clanHall != null && _clanHall.isSiegableHall() && ((ClanHallSiegable) _clanHall).getSiegeZone().isSiegeActive();
	}

	public boolean isAttackable(L2Character attacker)
	{
		return isAutoAttackable(attacker);
	}

	public int getDistanceToWatchObject(L2Object object)
	{
		if(!(object instanceof L2PcInstance))
		{
			return 0;
		}
		return 3000;
	}

	/**
	 * Если объект игрок, то 4000. Если не игрок, то 0
	 * @return distance after which the object must be remove from _knownObject according to the type of the object
	 */
	public int getDistanceToForgetObject(L2Object object)
	{
		if(!(object instanceof L2PcInstance))
		{
			return 0;
		}

		return 4000;
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		if(player == null)
		{
			return;
		}

		// Check if the L2PcInstance already target the L2NpcInstance
		if(equals(player.getTarget()))
		{
			if(isAutoAttackable(player))
			{
				if(Math.abs(player.getZ() - getZ()) < 400) // this max heigth difference might need some tweaking
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, this);
				}
			}
			else if(player.getClan() != null && _clanHall != null && player.getClanId() == _clanHall.getOwnerId() || player.getClan() != null && getFort() != null && player.getClan().equals(getFort().getOwnerClan()) && isUnlockable() && !getFort().getSiege().isInProgress())
			{
				if(isInsideRadius(player, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					player.gatesRequest(this);
					if(_open)
					{
						player.sendPacket(new ConfirmDlg(1141));
					}
					else
					{
						player.sendPacket(new ConfirmDlg(1140));
					}
				}
				else
				{
					player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
				}
			}
		}
		else
		{
			// Set the target of the L2PcInstance player
			player.setTarget(this);

			// Send a Server->Client packet MyTargetSelected to the L2PcInstance player
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);

			StaticObject su = new StaticObject(this, false);

			// send HP amount if doors are inside castle/fortress zone
			// TODO: needed to be added here doors from conquerable clanhalls
			if(getCastle() != null && getCastle().getCastleId() > 0 || getFort() != null && getFort().getFortId() > 0 && !isCommanderDoor())
			{
				su = new StaticObject(this, true);
			}
			player.sendPacket(su);

			// Send a Server->Client packet ValidateLocation to correct the L2NpcInstance position and heading on the client
			player.sendPacket(new ValidateLocation(this));
		}
		// Send a Server->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFail.STATIC_PACKET);
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if(isUnlockable() && getFort() == null)
		{
			return true;
		}

		// Doors can`t be attacked by NPCs
		if(!(attacker instanceof L2Playable))
		{
			return false;
		}

		L2PcInstance actingPlayer = attacker.getActingPlayer();

		if(_clanHall != null)
		{
			if(!_clanHall.isSiegableHall())
			{
				return false;
			}
			return ((ClanHallSiegable) _clanHall).isInSiege() && ((ClanHallSiegable) _clanHall).getSiege().doorIsAutoAttackable() && ((ClanHallSiegable) _clanHall).getSiege().checkIsAttacker(actingPlayer.getClan());
		}
		// Attackable  only during siege by everyone (not owner)
		boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getZone().isSiegeActive();
		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getZone().isSiegeActive() && !isCommanderDoor();
		int activeSiegeId = getFort() != null ? getFort().getFortId() : getCastle() != null ? getCastle().getCastleId() : 0;

		if(isFort)
		{
			L2Clan clan = actingPlayer.getClan();
			if(clan != null && clan.equals(getFort().getOwnerClan()))
			{
				return false;
			}
		}
		else if(isCastle)
		{
			L2Clan clan = actingPlayer.getClan();
			if(clan != null && clan.getClanId() == getCastle().getOwnerId())
			{
				return false;
			}
		}
		return isCastle || isFort;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		activeChar.sendPacket(new StaticObject(this, false));
	}

	@Override
	public String toString()
	{
		return "door " + _doorTemplate.getId();
	}

	@Override
	public boolean isDoor()
	{
		return true;
	}

	/***
	 * Закрытие двери
	 */
	public void closeMe()
	{
		setOpen(false);
		broadcastStatusUpdate();
	}

	/***
	 * Открытие двери
	 */
	public void openMe()
	{
		setOpen(true);
		broadcastStatusUpdate();
	}

	/***
	 * Открытие двери
	 * @param secsToClose через сколько милисекунд дверь закроется
	 */
	public void openMe(long secsToClose)
	{
		openMe();
		if(closeTask == null)
		{
			closeTask = ThreadPoolManager.getInstance().scheduleAi(new CloseTask(), secsToClose);
		}
		else
		{
			_log.log(Level.WARN, getClass().getSimpleName() + ": Trying run second close task for door ID = " + getDoorId());
		}
	}

	public String getDoorName()
	{
		return _doorTemplate.getDoorName();
	}

	public Collection<L2DefenderInstance> getKnownSiegeGuards()
	{
		FastList<L2DefenderInstance> result = getKnownList().getKnownObjects().values().stream().filter(obj -> obj instanceof L2DefenderInstance).map(obj -> (L2DefenderInstance) obj).collect(Collectors.toCollection(FastList::new));

		return result;
	}

	@Override
	public void broadcastStatusUpdate()
	{
		if(getKnownList().getKnownPlayers().isEmpty())
		{
			return;
		}

		StaticObject su = new StaticObject(this, false);
		DoorStatusUpdate dsu = new DoorStatusUpdate(this);

		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if(getCastle() != null && getCastle().getCastleId() > 0 || getFort() != null && getFort().getFortId() > 0 && !isCommanderDoor())
			{
				su = new StaticObject(this, true);
			}

			player.sendPacket(su);
			player.sendPacket(dsu);
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}

		boolean isFort = getFort() != null && getFort().getFortId() > 0 && getFort().getSiege().isInProgress() && !isCommanderDoor();
		boolean isCastle = getCastle() != null && getCastle().getCastleId() > 0 && getCastle().getSiege().isInProgress();

		if(isFort || isCastle)
		{
			broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_GATE_BROKEN_DOWN));
		}

		if(!_open)
		{
			DoorGeoEngine.getInstance().updateDoor(this);
		}

		setAutoActionDelay(-1);
		return true;
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if(ai == null)
		{
			synchronized(this)
			{
				if(_ai == null)
				{
					_ai = new L2DoorAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	/**
	 * Checks is door invulnerable for attacks.
	 * Door can be attacked only if its castle/fort door and siege is started.
	 *
	 * @return Invulnerable flag.
	 */
	@Override
	public boolean isInvul()
	{
		if(getCastle() != null && getCastle().getSiege() != null)
		{
			return false;
		}
		if(getFort() != null && getFort().getSiege() != null)
		{
			return false;
		}
		return !(_clanHall != null && _clanHall.isSiegableHall() && ((ClanHallSiegable) _clanHall).isInSiege());
	}

	@Override
	public DoorStat getStat()
	{
		if(!(super.getStat() instanceof DoorStat))
		{
			setStat(new DoorStat(this));
		}
		return (DoorStat) super.getStat();
	}

	@Override
	public DoorStatus getStatus()
	{
		if(!(super.getStatus() instanceof DoorStatus))
		{
			setStatus(new DoorStatus(this));
		}
		return (DoorStatus) super.getStatus();
	}

	@Override
	public void updateAbnormalEffect()
	{
		// Ничего
	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		setAutoActionDelay(_doorTemplate.getAutoActionDelay());

		if(!_open)
		{
			DoorGeoEngine.getInstance().updateDoor(this);
		}
	}

	@Override
	public DoorKnownList getKnownList()
	{
		return (DoorKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new DoorKnownList(this));
	}

	@Override
	public int getLevel()
	{
		return 1;
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if(getDestroyAbleBySiegeSummon() && !(attacker instanceof L2SiegeSummonInstance))
		{
			return;
		}

		super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
	}

	public int getMeshIndex()
	{
		return _meshIndex;
	}

	public void setMeshIndex(int id)
	{
		_meshIndex = id;
	}

	public boolean isTargetable()
	{
		return _targetable;
	}

	public void setTargetable(boolean value)
	{
		_targetable = value;
	}

	public L2DoorTemplate getDoorTemplate()
	{
		return _doorTemplate;
	}

	/***
	 * @return уровень апгрейда HP двери
	 */
	public int getHpLevel()
	{
		return _hpLevel;
	}

	/***
	 * Установить уровень апгрейда HP двери
	 * @param lv новый уровень
	 */
	public void setHpLevel(int lv)
	{
		if(_hpLevel == lv)
		{
			return;
		}

		getStatus().setCurrentHp(getStat().getMaxHp());
		_hpLevel = lv;
	}

	class CloseTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				closeMe();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error while CloseTask(): ", e);
			}
		}
	}

	/**
	 * Manages the auto open and closing of a door.
	 */
	class AutoOpenClose implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(isOpened())
				{
					closeMe();
				}
				else
				{
					openMe();
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not auto open/close door ID " + getDoorId() + " (" + getName() + ')');
			}
		}
	}

	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		@Override
		public L2DoorInstance getActor()
		{
			return L2DoorInstance.this;
		}

		@Override
		public void moveTo(int x, int y, int z, int offset)
		{
		}

		@Override
		public void moveTo(int x, int y, int z)
		{
		}

		@Override
		public void stopMove(Location pos)
		{
		}

		@Override
		public void doAttack(L2Character target)
		{
		}

		@Override
		public void doCast(L2Skill skill)
		{
		}
	}
}