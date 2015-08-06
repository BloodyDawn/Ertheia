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
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.zone.L2ZoneType;

/**
 * A simple no restart zone
 * @author GKR
 */
public class L2NoRestartZone extends L2ZoneType
{
	private int _restartAllowedTime;
	private boolean _enabled = true;

	public L2NoRestartZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "default_enabled":
				_enabled = Boolean.parseBoolean(value);
				break;
			case "restartAllowedTime":
				_restartAllowedTime = Integer.parseInt(value);
				break;
			case "restartTime":
				// Do nothing.
				break;
			case "instanceId":
				// Do nothing.
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(!_enabled)
		{
			return;
		}

		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_NORESTART, true);
			L2PcInstance player = (L2PcInstance) character;

			if(player.getZoneRestartLimitTime() > 0 && player.getZoneRestartLimitTime() < System.currentTimeMillis())
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new TeleportTask(player), 2000);
			}
			player.setZoneRestartLimitTime(0);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(!_enabled)
		{
			return;
		}

		if(character instanceof L2PcInstance)
		{
			character.setInsideZone(L2Character.ZONE_NORESTART, false);
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
		// Do nothing.
	}

	@Override
	public void onReviveInside(L2Character character)
	{
		// Do nothing.
	}

	public int getRestartAllowedTime()
	{
		return _restartAllowedTime;
	}

	public void setRestartAllowedTime(int time)
	{
		_restartAllowedTime = time;
	}

	private static class TeleportTask implements Runnable
	{
		private final L2PcInstance _player;

		public TeleportTask(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			_player.teleToLocation(TeleportWhereType.TOWN);
		}
	}
}
