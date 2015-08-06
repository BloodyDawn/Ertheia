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
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.TaskZoneSettings;

/**
 * A damage zone
 *
 * @author durgus
 */

public class L2DamageZone extends L2ZoneType
{
	private int _damageHPPerSec;
	private int _damageMPPerSec;

	private int _castleId;
	private Castle _castle;

	private int _startTask;
	private int _reuseTask;

	public L2DamageZone(int id)
	{
		super(id);

		// Setup default damage
		_damageHPPerSec = 200;
		_damageMPPerSec = 0;

		// Setup default start / reuse time
		_startTask = 10;
		_reuseTask = 5000;

		// no castle by default
		_castleId = 0;
		_castle = null;

		setTargetType(L2Playable.class); // default only playabale
		AbstractZoneSettings settings = ZoneManager.getSettings(getName());
		if(settings == null)
		{
			settings = new TaskZoneSettings();
		}
		setSettings(settings);
	}

	@Override
	public void setParameter(String name, String value)
	{
		switch(name)
		{
			case "dmgHPSec":
				_damageHPPerSec = Integer.parseInt(value);
				break;
			case "dmgMPSec":
				_damageMPPerSec = Integer.parseInt(value);
				break;
			case "castleId":
				_castleId = Integer.parseInt(value);
				break;
			case "initialDelay":
				_startTask = Integer.parseInt(value);
				break;
			case "reuse":
				_reuseTask = Integer.parseInt(value);
				break;
			default:
				super.setParameter(name, value);
				break;
		}
	}

	@Override
	public TaskZoneSettings getSettings()
	{
		return (TaskZoneSettings) super.getSettings();
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(getSettings().getTask() == null && (_damageHPPerSec != 0 || _damageMPPerSec != 0))
		{
			L2PcInstance player = character.getActingPlayer();
			if(getCastle() != null)
			{
				if(!(getCastle().getSiege().isInProgress() && player != null && player.getSiegeSide() != PlayerSiegeSide.DEFENDER)) // CastleSiegeEngine and no defender
				{
					return;
				}
			}
			synchronized(this)
			{
				if(getSettings().getTask() == null)
				{
					getSettings().setTask(ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ApplyDamage(this), _startTask, _reuseTask));
				}
			}
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(_characterList.isEmpty() && getSettings().getTask() != null)
		{
			getSettings().clear();
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	protected int getHPDamagePerSecond()
	{
		return _damageHPPerSec;
	}

	protected int getMPDamagePerSecond()
	{
		return _damageMPPerSec;
	}

	private Castle getCastle()
	{
		if(_castleId > 0 && _castle == null)
		{
			_castle = CastleManager.getInstance().getCastleById(_castleId);
		}

		return _castle;
	}

	class ApplyDamage implements Runnable
	{
		private final L2DamageZone _dmgZone;
		private final Castle _castle;

		ApplyDamage(L2DamageZone zone)
		{
			_dmgZone = zone;
			_castle = zone.getCastle();
		}

		@Override
		public void run()
		{
			boolean siege = false;

			if(_castle != null)
			{
				siege = _castle.getSiege().isInProgress();

				// Зоны замков активны только во время осад
				if(!siege)
				{
					_dmgZone.getSettings().clear();
					return;
				}
			}

			if(!isEnabled())
			{
				return;
			}

			for(L2Character temp : _dmgZone.getCharactersInside())
			{
				if(temp != null && !temp.isDead())
				{
					L2PcInstance player = temp.getActingPlayer();

					// Не демажим игрокам, которые находяться под спаунпротектом или которые телепортируются
					if(temp instanceof L2Playable && player != null && (player.isSpawnProtected() || player.isTeleportProtected()))
					{
						continue;
					}

					// Во время осад не берем в учет защитников резиденции
					if(player != null && siege && player.isInSiege() && player.getSiegeSide() == PlayerSiegeSide.DEFENDER)
					{
						continue;
					}

					double multiplier = 1 + temp.calcStat(Stats.DAMAGE_ZONE_VULN, 0, null, null) / 100;
					if(getHPDamagePerSecond() != 0)
					{
						temp.reduceCurrentHp(_dmgZone.getHPDamagePerSecond() * multiplier, null, null);
					}
					if(getMPDamagePerSecond() != 0)
					{
						temp.reduceCurrentMp(_dmgZone.getMPDamagePerSecond() * multiplier);
					}
				}
			}
		}
	}
}