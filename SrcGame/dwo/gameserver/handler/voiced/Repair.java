package dwo.gameserver.handler.voiced;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repair character commands handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Repair extends CommandHandler<String>
{
	@TextCommand
	public boolean closeThisWin(HandlerParams<String> params)
	{
		return true;
	}

	@TextCommand
	public boolean repair(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/repair/repair.htm");
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
		npcHtmlMessage.setHtml(htmContent);
		npcHtmlMessage.replace("%acc_chars%", getCharList(activeChar));
		activeChar.sendPacket(npcHtmlMessage);

		return true;
	}

	@TextCommand
	public boolean startRepair(HandlerParams<String> params)
	{
		if(params.getArgs().size() < 1)
		{
			return false;
		}

		String repairChar = params.getArgs().get(0);

		L2PcInstance activeChar = params.getPlayer();

		if(checkAcc(activeChar, repairChar))
		{
			if(checkChar(activeChar, repairChar))
			{
				String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/repair/repair-self.htm");
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				npcHtmlMessage.setHtml(htmContent);
				activeChar.sendPacket(npcHtmlMessage);
				return true;
			}
			else if(checkJail(repairChar))
			{
				String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/repair/repair-jail.htm");
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				npcHtmlMessage.setHtml(htmContent);
				activeChar.sendPacket(npcHtmlMessage);
				return false;
			}
			else
			{
				repairBadCharacter(repairChar);
				String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/repair/repair-done.htm");
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
				npcHtmlMessage.setHtml(htmContent);
				activeChar.sendPacket(npcHtmlMessage);
				return true;
			}
		}
		else
		{
			String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/repair/repair-error.htm");
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(5);
			npcHtmlMessage.setHtml(htmContent);
			activeChar.sendPacket(npcHtmlMessage);
			return false;
		}
	}

	private String getCharList(L2PcInstance activeChar)
	{
		String result = "";
		String repCharAcc = activeChar.getAccountName();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_CHAR_NAME);
			statement.setString(1, repCharAcc);
			rset = statement.executeQuery();
			while(rset.next())
			{
				if(activeChar.getName().compareTo(rset.getString(1)) != 0)
				{
					result += rset.getString(1) + ';';
				}
			}
		}
		catch(SQLException e)
		{
			return result;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return result;
	}

	private boolean checkAcc(L2PcInstance activeChar, String repairChar)
	{
		boolean result = false;
		String repCharAcc = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_ACCOUNT_NAME);
			statement.setString(1, repairChar);
			rset = statement.executeQuery();
			if(rset.next())
			{
				repCharAcc = rset.getString(1);
			}
		}
		catch(SQLException e)
		{
			return result;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		if(activeChar.getAccountName().compareTo(repCharAcc) == 0)
		{
			result = true;
		}
		return result;
	}

	private boolean checkJail(String repairChar)
	{
		boolean result = false;
		int repCharJail = 0;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_PUNISH_LEVEL);
			statement.setString(1, repairChar);
			rset = statement.executeQuery();
			if(rset.next())
			{
				repCharJail = rset.getInt(1);
			}
		}
		catch(SQLException e)
		{
			return result;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		if(repCharJail > 1) // 0 norm, 1 chat ban, 2 jail, 3....
		{
			result = true;
		}
		return result;
	}

	private boolean checkChar(L2PcInstance activeChar, String repairChar)
	{
		boolean result = false;
		if(activeChar.getName().compareTo(repairChar) == 0)
		{
			result = true;
		}
		return result;
	}

	private void repairBadCharacter(String charName)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_CHARACTERS_CHARID);
			statement.setString(1, charName);
			rset = statement.executeQuery();

			int objId = 0;
			if(rset.next())
			{
				objId = rset.getInt(1);
			}
			statement.close();

			if(objId == 0)
			{
				con.close();
				return;
			}
			statement = con.prepareStatement(Characters.UPDATE_CHAR_REPAIR_PLAYER);
			statement.setInt(1, objId);
			statement.execute();
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "GameServerStartup: could not repair character:" + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}
}