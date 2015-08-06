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
package dwo.gameserver.model.actor.instance.PcInstance;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author K4N4BS @ L2jThunder Team.
 */
public class PcExtension
{
	protected static final Logger _log = LogManager.getLogger(PcExtension.class);

	private L2PcInstance _activeChar;

	public PcExtension(L2PcInstance activeChar)
	{
		if(activeChar == null)
		{
			_log.log(Level.WARN, "[PcExtension] _activeChar: There can be a null value!");
			return;
		}
		_activeChar = activeChar;
		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, "[PcExtension] _activeChar: " + _activeChar.getObjectId() + " - " + _activeChar.getName() + '.');
		}
	}

	public L2PcInstance getPlayer()
	{
		return _activeChar;
	}
}
