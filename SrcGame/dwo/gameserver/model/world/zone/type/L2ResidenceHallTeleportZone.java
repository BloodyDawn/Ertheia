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
package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;

import java.util.concurrent.ScheduledFuture;

public class L2ResidenceHallTeleportZone extends L2ResidenceTeleportZone
{
	private int _id;
	private ScheduledFuture<?> _teleTask;

	/**
	 * @param id
	 */
	public L2ResidenceHallTeleportZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("residenceZoneId"))
		{
			_id = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	public int getResidenceZoneId()
	{
		return _id;
	}

	public void checkTeleporTask()
	{
		synchronized(this)
		{
			if(_teleTask == null || _teleTask.isDone())
			{
				_teleTask = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(), 30000);
			}
		}
	}

	class TeleportTask implements Runnable
	{
		@Override
		public void run()
		{
			int index = 0;
			if(getSpawns().size() > 1)
			{
				index = Rnd.get(getSpawns().size());
			}
			Location loc = getSpawns().get(index);
			if(loc == null)
			{
				throw new NullPointerException();
			}

			getPlayersInside().stream().filter(pc -> pc != null).forEach(pc -> pc.teleToLocation(loc, false));
		}
	}
}
