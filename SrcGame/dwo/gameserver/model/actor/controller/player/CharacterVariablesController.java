package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing player custom variables.
 * @author Yorie
 */
public class CharacterVariablesController extends PlayerController
{
	private static final Map<Type, Method> valueOfCache = new HashMap<>();
	private final Map<String, String> vars = new HashMap<>();

	public CharacterVariablesController(L2PcInstance player)
	{
		super(player);
	}

	/**
	 * [Re]loads player custom variables from database.
	 * Also before loading this method cleans up database and removes expired variables.
	 */
	public void reload()
	{
		if(!vars.isEmpty())
		{
			log.warn("Performed player variables reloading more than one times. This not an error, just a performance issue.");
			vars.clear(); // This is safe call, because all variables stored in database each time they've changed. Because of publicity of reloading.
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM `character_variables` WHERE type = \"user-var\" and `obj_id` = " + player.getObjectId() + " and `expire_time` < " + System.currentTimeMillis() / 1000 + " and `expire_time` != -1");
			statement.execute();
			statement.clearParameters();

			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM character_variables WHERE obj_id = ?");
			statement.setInt(1, player.getObjectId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				String name = rs.getString("name");
				String value = rs.getString("value");
				vars.put(name, value);
			}
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Error while loading character_variables.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Gets named variable with given type.
	 * This method implements an auto-cast values on passed types.
	 * Passed type <b>should</b> implement method valueOf(Type value),
	 * in another circumstances invocation will be invalid.
	 * Unfortunately, Java has no interfaces for conversation methods.
	 *
	 * This method will return null in cases: if stored value is null, if variable does not exists, if type casting was failed.
	 *
	 * @param name Variable name.
	 * @param type Type of variable
	 * @param <T> Type that will be used as primary type for casting variable value.
	 * @return Value of custom player variable. Value is automatically casted to given type.
	 */
	@Nullable
	public <T> T get(String name, Class<T> type)
	{
		return get(name, type, null);
	}

	/**
	 * Gets named variable with given type.
	 * This method implements an auto-cast values on passed types.
	 * Passed type <b>should</b> implement method valueOf(Type value),
	 * in another circumstances invocation will be invalid.
	 * Unfortunately, Java has no interfaces for conversation methods.
	 *
	 * @param name Variable name.
	 * @param type Type of variable
	 * @param defaultValue Default value that will be returned if: variable does not exists, or if type casting was failed.
	 * @param <T> Type that will be used as primary type for casting variable value.
	 * @return Value of custom player variable. Value is automatically casted to given type.
	 */
	public <T> T get(String name, Class<T> type, T defaultValue)
	{
		if(!vars.containsKey(name))
		{
			return defaultValue;
		}

		Method valueOf;
		if(valueOfCache.containsKey(type))
		{
			valueOf = valueOfCache.get(type);
		}
		else
		{
			try
			{
				valueOf = type.getMethod("valueOf", String.class);
				valueOfCache.put(type, valueOf);
			}
			catch(NoSuchMethodException e)
			{
				valueOfCache.put(type, null);
				log.error("VariablesController: Trying to get variable with using non-trait class " + type);
				return defaultValue;
			}
		}

		// Cached method may be null to prevent method searching for next get-method call
		if(valueOf == null)
		{
			return defaultValue;
		}

		T value;
		try
		{
			value = (T) valueOf.invoke(vars.get(name), vars.get(name));
		}
		catch(IllegalAccessException | InvocationTargetException e)
		{
			log.error("VariablesController: Failed to cast variable " + name + " (variables value is " + vars.get(name) + ") to " + type + '.');
			return defaultValue;
		}
		return value;
	}

	/**
	 * Gets named variable with given type.
	 * @param name Variable name.
	 * @return String value of variable or null if variable does not exists.
	 */
	@Nullable
	public String get(String name)
	{
		return get(name, (String) null);
	}

	/**
	 * Gets named variable with given type.
	 * @param name Variable name.
	 * @param defaultValue Default value that will be returned only if variable does not exists.
	 * @return String value of variable or null if variable does not exists.
	 */
	public String get(String name, String defaultValue)
	{
		return vars.containsKey(name) ? vars.get(name) : defaultValue;
	}

	/**
	 * Sets up new variable value if variable exists, or creates new entry with given name and value.
	 * This method accepts Object as value and works through String.valueOf() method.
	 * Be careful when passing abstract objects to this method. Object should override toString() method to present it's value.
	 *
	 * @param name Variable name.
	 * @param value Variable value.
	 */
	public void set(String name, Object value)
	{
		set(name, String.valueOf(value));
	}

	/**
	 * Sets up new variable value if variable exists, or creates new entry with given name and value.
	 * This method works directly with strings.
	 *
	 * @param name Variable name.
	 * @param value Variable value.
	 */
	public void set(String name, String value)
	{
		if(vars.containsKey(name) && vars.get(name).equals(value))
		{
			return;
		}

		vars.put(name, value);

		DatabaseUtils.executeStatementQuick("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (" + player.getObjectId() + ",'user-var','" + StringUtil.addSlashes(name) + "','" + StringUtil.addSlashes(value) + "',-1)");
	}

	/**
	 * Removes variable from memory and database if it exists.
	 * @param name Variable name.
	 */
	public void unset(String name)
	{
		if(name == null)
		{
			return;
		}

		if(vars.containsKey(name))
		{
			vars.remove(name);
			DatabaseUtils.executeStatementQuick("DELETE FROM `character_variables` WHERE `obj_id`='" + player.getObjectId() + "' AND `type`='user-var' AND `name`='" + name + "' LIMIT 1");
		}
	}
}
