package dwo.gameserver.handler;

import dwo.gameserver.handler.voiced.AutoLoot;
import dwo.gameserver.handler.voiced.Banking;
import dwo.gameserver.handler.voiced.Cfg;
import dwo.gameserver.handler.voiced.ChatAdmin;
import dwo.gameserver.handler.voiced.Debug;
import dwo.gameserver.handler.voiced.Hellbound;
import dwo.gameserver.handler.voiced.Mammon;
import dwo.gameserver.handler.voiced.NoExp;
import dwo.gameserver.handler.voiced.Ping;
import dwo.gameserver.handler.voiced.Repair;
import dwo.gameserver.handler.voiced.Stats;
import dwo.gameserver.handler.voiced.Wedding;
import dwo.gameserver.handler.voiced.WhoAmI;

/**
 * Voiced command handlers manager.
 *
 * @author Yorie
 */
@HandlerList({
	AutoLoot.class, Banking.class, Cfg.class, ChatAdmin.class, Debug.class, Hellbound.class, Mammon.class, NoExp.class,
	Ping.class, Repair.class, Stats.class, Wedding.class, WhoAmI.class
})
public class VoicedHandlerManager extends TextHandlerManager
{
	protected VoicedHandlerManager()
	{
		log.info("Loaded " + size() + " voiced handlers.");
	}

	public static VoicedHandlerManager getInstance()
	{
		return Instance.instance;
	}

	private static class Instance
	{
		protected static final VoicedHandlerManager instance = new VoicedHandlerManager();
	}
}
