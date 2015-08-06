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
package dwo.gameserver.model.world.npc.drop;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;

import java.util.concurrent.ScheduledFuture;

/**
 * @author DrHouse
 */
public class DropProtection implements Runnable
{
	private static final long PROTECTED_MILLIS_TIME = 15000;
	private volatile boolean _isProtected;
	private L2PcInstance _owner;
	private ScheduledFuture<?> _task;

	@Override
	public void run()
	{
		synchronized(this)
		{
			_isProtected = false;
			_owner = null;
			_task = null;
		}
	}

	public boolean isProtected()
	{
		return _isProtected;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public boolean tryPickUp(L2PcInstance actor)
	{
		synchronized(this)
		{
			if(!_isProtected)
			{
				return true;
			}

			if(_owner.equals(actor))
			{
				return true;
			}

			if(_owner.getParty() != null && _owner.getParty().equals(actor.getParty()))
			{
				return true;
			}

        /*
           * if (_owner.getClan() != null && _owner.getClan() == actor.getClan())
           * return true;
           */

			return false;
		}
	}

	public boolean tryPickUp(L2PetInstance pet)
	{
		return tryPickUp(pet.getOwner());
	}

	public void unprotect()
	{
		synchronized(this)
		{
			if(_task != null)
			{
				_task.cancel(false);
			}
			_isProtected = false;
			_owner = null;
			_task = null;
		}
	}

	public void protect(L2PcInstance player)
	{
		synchronized(this)
		{
			unprotect();

			_isProtected = true;

			if((_owner = player) == null)
			{
				throw new NullPointerException("Trying to protect dropped item to null owner");
			}

			_task = ThreadPoolManager.getInstance().scheduleGeneral(this, PROTECTED_MILLIS_TIME);
		}
	}
}
