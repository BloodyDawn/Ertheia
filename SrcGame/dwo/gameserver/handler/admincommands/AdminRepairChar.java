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
package dwo.gameserver.handler.admincommands;

import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;

public class AdminRepairChar implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_restore", "admin_repair"
	};
	private static Logger _log = LogManager.getLogger(AdminRepairChar.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		handleRepair(command);
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleRepair(String command)
	{
		String[] parts = command.split(" ");
		if(parts.length != 2)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_REPAIR);
			statement.setString(1, parts[1]);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_CHARID);
			statement.setString(1, parts[1]);
			ResultSet rset = statement.executeQuery();
			int objId = 0;
			if(rset.next())
			{
				objId = rset.getInt(1);
			}

			DatabaseUtils.closeResultSet(rset);
			DatabaseUtils.closeStatement(statement);

			if(objId == 0)
			{
				DatabaseUtils.closeConnection(con);
				return;
			}

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=?");
			statement.setInt(1, objId);
			statement.execute();
			DatabaseUtils.closeStatement(statement);
			DatabaseUtils.closeStatement(statement);
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "could not repair char:", e);
		}
		finally
		{
			try
			{
				DatabaseUtils.closeConnection(con);
			}
			catch(Exception e)
			{
			}
		}
	}
}
