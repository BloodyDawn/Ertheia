package dwo.gameserver.handler;

import javolution.util.FastList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Yorie
 */
public class CommandHandler<TCommandType> implements ICommandHandler<TCommandType>
{
	protected final Logger log = LogManager.getLogger(getClass());

	private List<TCommandType> commands;

	@Override
	public boolean isActive()
	{
		return true;
	}

	@Override
	public List<TCommandType> getCommandList()
	{
		if(commands == null)
		{
			commands = new FastList<>();
			for(Method method : getClass().getDeclaredMethods())
			{
				if(method.isAnnotationPresent(TextCommand.class))
				{
					String commandName = method.getAnnotation(TextCommand.class).value();

					if(!commands.contains(commandName))
					{
						commands.add((TCommandType) commandName);
					}
				}
				else if(method.isAnnotationPresent(NumericCommand.class))
				{
					Integer command = method.getAnnotation(NumericCommand.class).value();

					if(!commands.contains(command))
					{
						commands.add((TCommandType) command);
					}
				}
			}
		}

		return commands;
	}
}
