package dwo.gameserver.datatables.sql;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PetNameTable
{
	private static Logger _log = LogManager.getLogger(PetNameTable.class);

	private PetNameTable()
	{
	}

	public static PetNameTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public boolean doesPetNameExist(String name, int petNpcId)
	{
		boolean result = true;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT name FROM pets p, items i WHERE p.item_obj_id = i.object_id AND name=? AND i.item_id IN (?)");
			statement.setString(1, name);

			StringBuilder cond = new StringBuilder();
			for(int it : PetDataTable.getPetItemsByNpc(petNpcId))
			{
				if(cond.length() != 0)
				{
					cond.append(", ");
				}
				cond.append(it);
			}
			statement.setString(2, cond.toString());
			rset = statement.executeQuery();
			result = rset.next();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not check existing petname:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
		return result;
	}

	public boolean isValidPetName(String name)
	{
		boolean result = true;

		if(!isAlphaNumeric(name))
		{
			return result;
		}

		Pattern pattern;
		try
		{
			pattern = Pattern.compile(Config.PET_NAME_TEMPLATE);
		}
		catch(PatternSyntaxException e) // case of illegal pattern
		{
			_log.log(Level.ERROR, "ERROR : Pet name pattern of config is wrong!");
			pattern = Pattern.compile(".*");
		}
		Matcher regexp = pattern.matcher(name);
		if(!regexp.matches())
		{
			result = false;
		}
		return result;
	}

	private boolean isAlphaNumeric(String text)
	{
		boolean result = true;
		char[] chars = text.toCharArray();
		for(char aChar : chars)
		{
			if(!Character.isLetterOrDigit(aChar))
			{
				result = false;
				break;
			}
		}
		return result;
	}

	private static class SingletonHolder
	{
		protected static final PetNameTable _instance = new PetNameTable();
	}
}