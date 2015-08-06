package dwo.gameserver.handler.chat;

import dwo.config.Config;
import dwo.gameserver.handler.ChatHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.Say2;
import org.apache.log4j.Level;

/**
 * @author durgus
 */

public class ChatTrade extends CommandHandler<Integer>
{
	@NumericCommand(8)
	public boolean trageChat(ChatHandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		Say2 cs = new Say2(activeChar.getObjectId(), ChatType.values()[params.getCommand()], activeChar.getName(), params.getMessage());

		if(Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("on") || Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("gm") && activeChar.isGM())
		{
			int region = 0;
			try
			{
				region = MapRegionManager.getInstance().getMapRegion(activeChar.getX(), activeChar.getY()).getLocId();
			}
			catch(Exception e)
			{
				log.log(Level.ERROR, "!!! ATTENTION: MapRegion not exists on XY: " + activeChar.getX() + ' ' + activeChar.getY());
			}
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				int regionMap = 0;
				try
				{
					regionMap = MapRegionManager.getInstance().getMapRegion(player.getX(), player.getY()).getLocId();
				}
				catch(Exception e)
				{
					log.log(Level.ERROR, "!!! ATTENTION: MapRegion not exists on XY: " + activeChar.getX() + ' ' + activeChar.getY());
				}

				if(player != null && region == regionMap && !RelationListManager.getInstance().isBlocked(player, activeChar) && player.getInstanceId() == activeChar.getInstanceId())
				{
					player.sendPacket(cs);
				}
			}
		}
		else if(Config.DEFAULT_TRADE_CHAT.equalsIgnoreCase("global"))
		{
			if(!activeChar.isGM() && !activeChar.getFloodProtectors().getGlobalChat().tryPerformAction(FloodAction.CHAT_TRADE))
			{
				activeChar.sendMessage("Do not spam trade channel.");
				return false;
			}

			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				if(!RelationListManager.getInstance().isBlocked(player, activeChar))
				{
					player.sendPacket(cs);
				}
			}
		}

		return true;
	}
}