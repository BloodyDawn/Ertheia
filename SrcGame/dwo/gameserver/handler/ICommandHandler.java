package dwo.gameserver.handler;

import java.util.List;

/**
 * Interface for all command handlers.
 * @author Yorie
 */
public interface ICommandHandler<TCommandType>
{
	/**
	 * @return True if current handler is active.
	 */
	boolean isActive();

	/**
	 * @return List of commands supported by this handler.
	 */
	List<TCommandType> getCommandList();
}
