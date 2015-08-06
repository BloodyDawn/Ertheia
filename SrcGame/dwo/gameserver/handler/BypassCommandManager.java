package dwo.gameserver.handler;

import dwo.gameserver.handler.bypasses.*;
import org.apache.log4j.Level;

@HandlerList({
	ChatLink.class, ClanWarehouse.class, ItemAuctionLink.class, Link.class, Loto.class, ManorManager.class,
	Multisell.class, Observation.class, PcCafe.class, PlayerHelp.class, PlayMovie.class, PrivateWarehouse.class, QuestLink.class,
	ReceivePremium.class, ReleaseAttribute.class, TerritoryStatus.class, VoiceCommand.class, WorldStatistic.class,
	Teleport.class, DynamicQuest.class, Clan.class, FestivalOfChaos.class, Ollympiad.class
})
public class BypassCommandManager extends TextHandlerManager
{
	private BypassCommandManager()
	{
		log.log(Level.INFO, "Loaded " + size() + " Bypass Handlers");
	}

	public static BypassCommandManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private static class SingletonHolder
	{
		protected static final BypassCommandManager _instance = new BypassCommandManager();
	}
}