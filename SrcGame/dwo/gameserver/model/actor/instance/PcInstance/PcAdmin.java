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

/**
 * @author K4N4BS @ L2jThunder Team.
 */
public class PcAdmin extends PcExtension
{
	private boolean _safeadmin;
	private String _adminConfirmCmd;
	private boolean _inCameraMode;

	public PcAdmin(L2PcInstance activeChar)
	{
		super(activeChar);
	}

	public void setIsSafeAdmin(boolean b)
	{
		_safeadmin = b;
	}

	public boolean isSafeAdmin()
	{
		return _safeadmin;
	}

	public boolean canUseAdminCommand()
	{
		if(Config.ENABLE_SAFE_ADMIN_PROTECTION && !getPlayer().getPcAdmin()._safeadmin)
		{
			_log.log(Level.WARN, "Character " + getPlayer().getName() + '(' + getPlayer().getObjectId() + ") tryed to use an admin command.");
			punishUnSafeAdmin();
			return false;
		}
		return true;
	}

	public void punishUnSafeAdmin()
	{
		if(getPlayer() != null)
		{
			getPlayer().setAccessLevel(0);
		}
	}

	public String getAdminConfirmCmd()
	{
		return _adminConfirmCmd;
	}

	public void setAdminConfirmCmd(String adminConfirmCmd)
	{
		_adminConfirmCmd = adminConfirmCmd;
	}

	public void setCameraMode(boolean val)
	{
		_inCameraMode = val;
	}

	public boolean inCameraMode()
	{
		return _inCameraMode;
	}
}
