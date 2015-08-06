package dwo.gameserver.handler;

import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Parameter container for chat command handlers only.
 *
 * @author Yorie
 */
public class ChatHandlerParams<Integer> extends HandlerParams<Integer>
{
	private final String message;
	private final String target;

	public ChatHandlerParams(L2PcInstance activeChar, Integer command, String message, String target)
	{
		super(activeChar, command);
		this.message = message;
		this.target = target;
	}

	public String getMessage()
	{
		return message;
	}

	public String getTarget()
	{
		return target;
	}
}
