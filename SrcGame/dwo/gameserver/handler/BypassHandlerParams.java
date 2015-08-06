package dwo.gameserver.handler;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;

import java.util.List;
import java.util.Map;

/**
 * Parameter container for bypass command handlers only.
 *
 * @author Yorie
 */
public class BypassHandlerParams extends HandlerParams<String>
{
	private final L2Character target;
	// TODO: Понапридумывали хуеты. Выпилить и сделать по-нормальному вместе с bypass-валидатором в L2PcInstance.
	private final String bypassSource;

	public BypassHandlerParams(L2PcInstance activeChar, String bypassSource, String command, L2Character target)
	{
		super(activeChar, command);
		this.target = target;
		this.bypassSource = bypassSource;
	}

	public BypassHandlerParams(L2PcInstance activeChar, String bypassSource, String command, L2Character target, List<String> args, Map<String, String> queryArgs)
	{
		super(activeChar, command, args, queryArgs);
		this.target = target;
		this.bypassSource = bypassSource;
	}

	/**
	 * @return Returns bypass source command (source string with non-chunked parameters).
	 */
	public String getSource()
	{
		return bypassSource;
	}

	public L2Character getTarget()
	{
		return target;
	}
}
