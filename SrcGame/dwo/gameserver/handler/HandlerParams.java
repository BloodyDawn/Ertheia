package dwo.gameserver.handler;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Parameter container for command handlers.
 * Contains info about active character, command name and additional parameters of command was sent by player.
 *
 * @author Yorie
 */
public class HandlerParams<TCommandType>
{
	protected final L2Character activeChar;
	protected final List<String> args = new FastList<>();
	protected final Map<String, String> queryArgs = new FastMap<>();
	protected TCommandType command;

	/**
	 * @param activeChar Active character.
	 * @param command Command ID.
	 */
	public HandlerParams(L2PcInstance activeChar, TCommandType command)
	{
		this.activeChar = activeChar;
		this.command = command;
	}

	/**
	 * @param activeChar Active character.
	 * @param command Command ID.
	 * @param args Additional parameters.
	 */
	public HandlerParams(L2PcInstance activeChar, TCommandType command, List<String> args, Map<String, String> queryArgs)
	{
		this(activeChar, command);
		this.args.addAll(args);

		if(queryArgs != null)
		{
			this.queryArgs.putAll(queryArgs);
		}
	}

	public HandlerParams(L2PcInstance activeChar, TCommandType command, String[] args, Map<String, String> queryArgs)
	{
		this(activeChar, command);
		Collections.addAll(this.args, args);

		if(queryArgs != null)
		{
			queryArgs.putAll(queryArgs);
		}
	}

	/**
	 * Does string parse separating string into tokens by spaces with trim and adds resulted strings to list.
	 * For example, string "delete foo   bar" will be tokenized into {"delete", "foo", "bar"}
	 *
	 * @param params Parameters string.
	 * @return List of tokenized parameters.
	 */
	public static List<String> parseArgs(String params)
	{
		List<String> list = new FastList<>();

		if(params == null)
		{
			return list;
		}

		for(String param : params.split(" "))
		{
			if(!param.trim().isEmpty())
			{
				list.add(param);
			}
		}
		return list;
	}

	/**
	 * Parses query string. The query string is web-like command.
	 * For example: "dynamic_quest_accept?dquest_id=1&step=2". There is dquest & step is query arguments, 1 & 2 is arguments values.
	 *
	 * @param query Query string.
	 * @return List of mapped parameters.
	 */
	public static Map<String, String> parseQueryArguments(String query)
	{
		Map<String, String> args = new FastMap<>();
		if(!query.contains("?"))
		{
			return args;
		}

		query = query.substring(query.indexOf('?'));

		if(query.length() <= 1)
		{
			return args;
		}

		query = query.substring(1);

		for(String arg : query.split("&"))
		{
			// Non-value argument
			if(arg.contains("="))
			{
				int equationPos = arg.indexOf('=');
				args.put(arg.substring(0, equationPos), arg.substring(equationPos + 1));
			}
			else
			{
				args.put(arg, null);
			}
		}

		return args;
	}

	public static CommandWrapper parseCommand(String command)
	{
		int commandEndIndex;

		commandEndIndex = command.indexOf('?');
		if(commandEndIndex < 0)
		{
			commandEndIndex = command.indexOf(' ');

			if(commandEndIndex < 0)
			{
				commandEndIndex = command.length();
			}
		}

		String opcode = command.substring(0, commandEndIndex);
		List<String> args;
		Map<String, String> queryArgs = new FastMap<>();

		String rest = command.substring(commandEndIndex, command.length());

		if(!rest.isEmpty() && rest.charAt(0) == '?')
		{
			rest = rest.substring(1);
			// Split query ?foo=foo&bar=bar into array {'foo=foo', 'bar=bar', ...}
			String[] tokens = rest.split("&");
			for(String arg : tokens)
			{
				// Non-value argument (example: ?foo=foo&bar - bar is non-value argument)
				if(arg.contains("="))
				{
					// Split each argument of tokens array into array {'foo', 'foo'} and etc.
					String[] parts = arg.split("=");
					String key = parts[0].trim();
					String value = parts.length > 1 ? parts[1] : "";
					queryArgs.put(key, value.trim());
				}
				else
				{
					queryArgs.put(arg, null);
				}
			}
		}
		else
		{
			queryArgs = new FastMap<>();
		}

		args = parseArgs(rest);

		return new CommandWrapper(opcode, args, queryArgs);
	}

	/**
	 * @return Active player for this command.
	 */
	public L2PcInstance getPlayer()
	{
		return activeChar.getActingPlayer();
	}

	/**
	 * @return This command name.
	 */
	public TCommandType getCommand()
	{
		return command;
	}

	/**
	 * Re-sets command opcode.
	 * @param command Command opcode.
	 */
	public void setCommand(TCommandType command)
	{
		this.command = command;
	}

	/**
	 * Arguments is tokens that written right after command name, such arguments separated with space.
	 * Example: "invite Player". There is "Player" is first argument.
	 * @return Additional parameters list.
	 */
	public List<String> getArgs()
	{
		return args;
	}

	/**
	 * Query arguments is web-like arguments.
	 * Example: "dynamic_quest_accept?dquest_id=1&step=2". There is dquest & step is query arguments, 1 & 2 is arguments values.
	 *
	 * @return Additional parameters list.
	 */
	public Map<String, String> getQueryArgs()
	{
		return queryArgs;
	}

	public static class CommandWrapper
	{
		private final List<String> args;
		private final Map<String, String> queryArgs;
		private String command;

		public CommandWrapper(String command, List<String> args, Map<String, String> queryArgs)
		{
			this.command = command;
			this.args = args;
			this.queryArgs = queryArgs;
		}

		/**
		 * @return Command name.
		 */
		public String getCommand()
		{
			return command;
		}

		/**
		 * Allows to replace command name and keep old arguments.
		 * @param command New command name.
		 */
		public void setCommand(String command)
		{
			this.command = command;
		}

		public List<String> getArgs()
		{
			return args;
		}

		public Map<String, String> getQueryArgs()
		{
			return queryArgs;
		}
	}
}
