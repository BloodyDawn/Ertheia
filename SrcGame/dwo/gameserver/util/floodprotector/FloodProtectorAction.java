/*
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.util.floodprotector;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPunishLevel;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Flood protector implementation.
 *
 * @author fordfrog
 */
public class FloodProtectorAction
{
	/**
	 * Logger
	 */
	private static final Logger _log = LogManager.getLogger(FloodProtectorAction.class);
	/**
	 * Client for this instance of flood protector.
	 */
	private final L2GameClient _client;
	/**
	 * Configuration of this instance of flood protector.
	 */
	private final FloodProtectorConfig _config;
	/**
	 * Next game tick when new request is allowed.
	 */
	private volatile int _nextGameTick = GameTimeController.getInstance().getGameTicks();
	/**
	 * Request counter.
	 */
	private AtomicInteger _count = new AtomicInteger(0);
	/**
	 * Flag determining whether exceeding request has been logged.
	 */
	private boolean _logged;
	/**
	 * Flag determining whether punishment application is in progress so that we do not apply
	 * punisment multiple times (flooding).
	 */
	private volatile boolean _punishmentInProgress;

	/**
	 * Creates new instance of FloodProtectorAction.
	 *
	 * @param client
	 *            player for which flood protection is being created
	 * @param config
	 *            flood protector configuration
	 */
	public FloodProtectorAction(L2GameClient client, FloodProtectorConfig config)
	{
		_client = client;
		_config = config;
	}

	/**
	 * Checks whether the request is flood protected or not.
	 *
	 * @param command
	 *            command issued or short command description
	 *
	 * @return true if action is allowed, otherwise false
	 */
	public boolean tryPerformAction(FloodAction command)
	{
		if(_client.getActiveChar() != null && _client.getActiveChar().isGM())
		{
			return true;
		}

		int curTick = GameTimeController.getInstance().getGameTicks();

		if(curTick < _nextGameTick || _punishmentInProgress)
		{
			if(_config.LOG_FLOODING && !_logged)
			{
				_log.log(Level.WARN, " called command " + command + " ~" + (_config.FLOOD_PROTECTION_INTERVAL - (_nextGameTick - curTick)) * GameTimeController.MILLIS_IN_TICK + " ms after previous command");
				_logged = true;
			}

			_count.incrementAndGet();

			if(!_punishmentInProgress && _config.PUNISHMENT_LIMIT > 0 && _count.get() >= _config.PUNISHMENT_LIMIT && _config.PUNISHMENT_TYPE != null)
			{
				_punishmentInProgress = true;

				switch(_config.PUNISHMENT_TYPE)
				{
					case "kick":
						kickPlayer();
						break;
					case "ban":
						banAccount();
						break;
					case "jail":
						jailChar();
						break;
				}

				_punishmentInProgress = false;
			}

			return false;
		}

		if(_count.get() > 0)
		{
			if(_config.LOG_FLOODING)
			{
				_log.log(Level.WARN, " issued " + _count + " extra requests within ~" + _config.FLOOD_PROTECTION_INTERVAL * GameTimeController.MILLIS_IN_TICK + " ms");
			}
		}

		_nextGameTick = curTick + _config.FLOOD_PROTECTION_INTERVAL;
		_logged = false;
		_count.set(0);

		return true;
	}

	/**
	 * Kick player from game (close network ThreadConnection).
	 */
	private void kickPlayer()
	{
		if(_client.getActiveChar() != null)
		{
			_client.getActiveChar().logout(false);
		}
		else
		{
			_client.closeNow();
			_log.log(Level.WARN, "kicked for flooding");
		}
	}

	/**
	 * Bans char account and logs out the char.
	 */
	private void banAccount()
	{
		if(_client.getActiveChar() != null)
		{
			_client.getActiveChar().setPunishLevel(PlayerPunishLevel.ACC, _config.PUNISHMENT_TIME);

			_log.log(Level.WARN, " banned for flooding " + (_config.PUNISHMENT_TIME <= 0 ? "forever" : "for ") + _config.PUNISHMENT_TIME + " mins");

			_client.getActiveChar().logout();
		}
		else
		{
			log(" unable to ban account: no active player");
		}
	}

	/**
	 * Jails char.
	 */
	private void jailChar()
	{
		if(_client.getActiveChar() != null)
		{
			_client.getActiveChar().setPunishLevel(PlayerPunishLevel.JAIL, _config.PUNISHMENT_TIME);
			_log.log(Level.WARN, " jailed for flooding " + (_config.PUNISHMENT_TIME <= 0 ? "forever" : "for ") + _config.PUNISHMENT_TIME + " mins");
		}
		else
		{
			log(" unable to jail: no active player");
		}
	}

	private void log(String... lines)
	{
		StringBuilder output = StringUtil.startAppend(100, _config.FLOOD_PROTECTOR_TYPE, ": ");
		String address = null;
		try
		{
			if(!_client.isDetached())
			{
				address = _client.getConnection().getInetAddress().getHostAddress();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while getting client host address.", e);
		}

		switch(_client.getState())
		{
			case IN_GAME:
				if(_client.getActiveChar() != null)
				{
					StringUtil.append(output, _client.getActiveChar().getName());
					StringUtil.append(output, "(", String.valueOf(_client.getActiveChar().getObjectId()), ") ");
				}
			case AUTHED:
				if(_client.getAccountName() != null)
				{
					StringUtil.append(output, _client.getAccountName(), " ");
				}
			case CONNECTED:
				if(address != null)
				{
					StringUtil.append(output, address);
				}
				break;
			default:
				throw new IllegalStateException("Missing state on switch");
		}

		StringUtil.append(output, lines);
		_log.log(Level.WARN, output.toString());
	}
}