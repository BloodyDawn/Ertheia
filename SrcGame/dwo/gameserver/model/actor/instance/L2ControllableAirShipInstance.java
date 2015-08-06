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
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.stat.ControllableAirShipStat;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.DeleteObject;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

import java.util.concurrent.Future;

public class L2ControllableAirShipInstance extends L2AirShipInstance
{
	private static final int HELM = 13556;
	private static final int LOW_FUEL = 40;

	private int _fuel;
	private int _maxFuel;

	private int _ownerId;
	private int _helmId;
	private L2PcInstance _captain;

	private Future<?> _consumeFuelTask;
	private Future<?> _checkTask;

	public L2ControllableAirShipInstance(int objectId, L2CharTemplate template, int ownerId)
	{
		super(objectId, template);
		_ownerId = ownerId;
		_helmId = IdFactory.getInstance().getNextId(); // not forget to release
		// !
	}

	@Override
	public boolean canBeControlled()
	{
		return super.canBeControlled() && !isInDock();
	}

	@Override
	public ControllableAirShipStat getStat()
	{
		return (ControllableAirShipStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new ControllableAirShipStat(this));
	}

	@Override
	public void oustPlayer(L2PcInstance player)
	{
		if(player.equals(_captain))
		{
			setCaptain(null); // no need to broadcast userinfo here
		}

		super.oustPlayer(player);
	}

	@Override
	public boolean onDelete()
	{
		getLocationController().delete();

		if(_checkTask != null)
		{
			_checkTask.cancel(false);
			_checkTask = null;
		}
		if(_consumeFuelTask != null)
		{
			_consumeFuelTask.cancel(false);
			_consumeFuelTask = null;
		}

		try
		{
			broadcastPacket(new DeleteObject(_helmId));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed decayMe():" + e.getMessage());
		}

		return true;
	}

	@Override
	public boolean isOwner(L2PcInstance player)
	{
		if(_ownerId == 0)
		{
			return false;
		}

		return player.getClanId() == _ownerId || player.getObjectId() == _ownerId;
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}

	@Override
	public boolean isCaptain(L2PcInstance player)
	{
		return _captain != null && player.equals(_captain);
	}

	@Override
	public int getCaptainId()
	{
		return _captain != null ? _captain.getObjectId() : 0;
	}

	@Override
	public int getHelmObjectId()
	{
		return _helmId;
	}

	@Override
	public int getHelmItemId()
	{
		return HELM;
	}

	@Override
	public boolean setCaptain(L2PcInstance player)
	{
		if(player == null)
		{
			_captain = null;
		}
		else
		{
			if(_captain == null && player.getAirShip().equals(this))
			{
				int x = player.getInVehiclePosition().getX() - 0x16e;
				int y = player.getInVehiclePosition().getY();
				int z = player.getInVehiclePosition().getZ() - 0x6b;
				if(x * x + y * y + z * z > 2500)
				{
					player.sendPacket(SystemMessageId.CANT_CONTROL_TOO_FAR);
					return false;
				}
				// TODO: Missing message ID: 2739 Message: You cannot control
				// the helm because you do not meet the requirements.
				if(player.isInCombat())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_BATTLE);
					return false;
				}
				if(player.isSitting())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_SITTING_POSITION);
					return false;
				}
				if(player.isParalyzed())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_YOU_ARE_PETRIFIED);
					return false;
				}
				if(player.isCursedWeaponEquipped())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_A_CURSED_WEAPON_IS_EQUIPPED);
					return false;
				}
				if(player.isFishing())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_FISHING);
					return false;
				}
				if(player.isDead() || player.isFakeDeath())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHEN_YOU_ARE_DEAD);
					return false;
				}
				if(player.isCastingNow())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_USING_A_SKILL);
					return false;
				}
				if(player.isTransformed())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_TRANSFORMED);
					return false;
				}
				if(player.isCombatFlagEquipped())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_HOLDING_A_FLAG);
					return false;
				}
				if(player.isInDuel())
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_CONTROL_THE_HELM_WHILE_IN_A_DUEL);
					return false;
				}
				_captain = player;
				player.broadcastUserInfo();
			}
			else
			{
				return false;
			}
		}
		updateAbnormalEffect();
		return true;
	}

	@Override
	public int getFuel()
	{
		return _fuel;
	}

	@Override
	public void setFuel(int f)
	{

		int old = _fuel;
		if(f < 0)
		{
			_fuel = 0;
		}
		else
		{
			_fuel = f > _maxFuel ? _maxFuel : f;
		}

		if(_fuel == 0 && old > 0)
		{
			broadcastToPassengers(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_FUEL_RUN_OUT));
		}
		else if(_fuel < LOW_FUEL)
		{
			broadcastToPassengers(SystemMessage.getSystemMessage(SystemMessageId.THE_AIRSHIP_FUEL_SOON_RUN_OUT));
		}
	}

	@Override
	public int getMaxFuel()
	{
		return _maxFuel;
	}

	@Override
	public void setMaxFuel(int mf)
	{
		_maxFuel = mf;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		super.sendInfo(activeChar);
		if(_captain != null)
		{
			_captain.sendInfo(activeChar);
		}
	}

	@Override
	public void onAction(L2PcInstance player, boolean interact)
	{
		player.sendPacket(new MyTargetSelected(_helmId, 0));
		super.onAction(player, interact);
	}

	@Override
	public void refreshID()
	{
		super.refreshID();
		IdFactory.getInstance().releaseId(_helmId);
		_helmId = IdFactory.getInstance().getNextId();
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		_checkTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckTask(), 60000, 10000);
		_consumeFuelTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ConsumeFuelTask(), 60000, 60000);
	}

	private class ConsumeFuelTask implements Runnable
	{
		@Override
		public void run()
		{
			int fuel = getFuel();
			if(fuel > 0)
			{
				fuel -= 10;
				if(fuel < 0)
				{
					fuel = 0;
				}

				setFuel(fuel);
				updateAbnormalEffect();
			}
		}
	}

	private class CheckTask implements Runnable
	{
		@Override
		public void run()
		{
			if(isVisible() && isEmpty() && !isInDock())
			// deleteMe() can't be called from CheckTask because task should
			// not cancel itself
			{
				ThreadPoolManager.getInstance().executeTask(new DecayTask());
			}
		}
	}

	private class DecayTask implements Runnable
	{
		@Override
		public void run()
		{
			getLocationController().delete();
		}
	}
}