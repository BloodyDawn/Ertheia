package dwo.gameserver.handler;

import dwo.gameserver.handler.usercommands.Birthday;
import dwo.gameserver.handler.usercommands.Channel;
import dwo.gameserver.handler.usercommands.Clan;
import dwo.gameserver.handler.usercommands.Dismount;
import dwo.gameserver.handler.usercommands.Escape;
import dwo.gameserver.handler.usercommands.InstanceZone;
import dwo.gameserver.handler.usercommands.Loc;
import dwo.gameserver.handler.usercommands.Mount;
import dwo.gameserver.handler.usercommands.OlympiadStat;
import dwo.gameserver.handler.usercommands.PartyInfo;
import dwo.gameserver.handler.usercommands.Time;
import org.apache.log4j.Level;

@HandlerList({
	Birthday.class, Channel.class, Dismount.class, Escape.class, InstanceZone.class, Loc.class, Mount.class,
	OlympiadStat.class, PartyInfo.class, Time.class, Clan.class
})
public class UserCommandManager extends NumHandlerManager
{
	private UserCommandManager()
	{
		log.log(Level.INFO, "Loaded " + size() + " user command handlers");
	}

	public static UserCommandManager getInstance()
	{
		return SingletonHolder.instance;
	}

	private static class SingletonHolder
	{
		protected static final UserCommandManager instance = new UserCommandManager();
	}
}
