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
package dwo.gameserver.model.player;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;

/**
 * @author UnAfraid & mrTJO
 *         TODO: System Messages:
 *         ADD:
 *         3223: The previous name is being registered. Please try again later.
 *         END OF ADD
 *         DEL
 *         3219: $s1 was successfully deleted from your Contact List.
 *         3217: The name is not currently registered.
 *         END OF DEL
 */
public class L2ContactList
{
	private static final String QUERY_ADD = "INSERT INTO character_contacts (charId, contactId) VALUES (?, ?)";
	private static final String QUERY_REMOVE = "DELETE FROM character_contacts WHERE charId = ? and contactId = ?";
	private static final String QUERY_LOAD = "SELECT contactId FROM character_contacts WHERE charId = ?";
	private final Logger _log = LogManager.getLogger(getClass().getName());
	private final L2PcInstance activeChar;
	private final List<String> _contacts;

	public L2ContactList(L2PcInstance player)
	{
		activeChar = player;
		_contacts = new FastList<String>().shared();
		restore();
	}

	public void restore()
	{
		_contacts.clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(QUERY_LOAD);
			statement.setInt(1, activeChar.getObjectId());
			rset = statement.executeQuery();

			int contactId;
			String contactName;
			while(rset.next())
			{
				contactId = rset.getInt(1);
				contactName = CharNameTable.getInstance().getNameById(contactId);
				if(contactName == null || contactName.equals(activeChar.getName()) || contactId == activeChar.getObjectId())
				{
					continue;
				}

				_contacts.add(contactName);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public boolean add(String name)
	{
		int contactId = CharNameTable.getInstance().getIdByName(name);
		if(_contacts.contains(name))
		{
			activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
			return false;
		}
		if(activeChar.getName().equals(name))
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_ADD_YOUR_NAME_ON_CONTACT_LIST);
			return false;
		}
		if(_contacts.size() >= 100)
		{
			activeChar.sendPacket(SystemMessageId.CONTACT_LIST_LIMIT_REACHED);
			return false;
		}
		if(contactId < 1)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NAME_S1_NOT_EXIST_TRY_ANOTHER_NAME).addString(name));
			return false;
		}
		for(String contactName : _contacts)
		{
			if(contactName.equalsIgnoreCase(name))
			{
				activeChar.sendPacket(SystemMessageId.NAME_ALREADY_EXIST_ON_CONTACT_LIST);
				return false;
			}
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(QUERY_ADD);
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, contactId);
			statement.execute();

			_contacts.add(name);

			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESSFULLY_ADDED_TO_CONTACT_LIST).addString(name));
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		return true;
	}

	public void remove(String name)
	{
		int contactId = CharNameTable.getInstance().getIdByName(name);

		if(!_contacts.contains(name))
		{
			activeChar.sendPacket(SystemMessageId.NAME_NOT_REGISTERED_ON_CONTACT_LIST);
			return;
		}
		if(contactId < 1)
		{
			//TODO: Message?
			return;
		}

		_contacts.remove(name);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(QUERY_REMOVE);
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, contactId);
			statement.execute();
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_SUCCESFULLY_DELETED_FROM_CONTACT_LIST).addString(name));
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error found in " + activeChar.getName() + "'s ContactsList: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public List<String> getAllContacts()
	{
		return _contacts;
	}
}
