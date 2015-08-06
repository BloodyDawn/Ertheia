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
package dwo.gameserver.model.world.residence.castle;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Thorgrim - 2005
 * Class managing periodical events with castle
 */
public class CastleUpdater implements Runnable
{
	protected static final Logger _log = LogManager.getLogger(CastleUpdater.class);
	private L2Clan _clan;
	private int _runCount;

	public CastleUpdater(L2Clan clan, int runCount)
	{
		_clan = clan;
		_runCount = runCount;
	}

	@Override
	public void run()
	{
		try
		{
			// Move current castle treasury to clan warehouse every 2 hour
			ItemContainer warehouse = _clan.getWarehouse();
			if(warehouse != null && _clan.getCastleId() > 0)
			{
				Castle castle = CastleManager.getInstance().getCastleById(_clan.getCastleId());
				if(!Config.ALT_MANOR_SAVE_ALL_ACTIONS)
				{
					if(_runCount % Config.ALT_MANOR_SAVE_PERIOD_RATE == 0)
					{
						castle.saveSeedData();
						castle.saveCropData();
						if(Config.DEBUG)
						{
							_log.log(Level.DEBUG, "Manor System: all data for " + castle.getName() + " saved");
						}
					}
				}
				CastleUpdater cu = new CastleUpdater(_clan, ++_runCount);
				ThreadPoolManager.getInstance().scheduleGeneral(cu, 3600000);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "", e);
		}
	}
}
