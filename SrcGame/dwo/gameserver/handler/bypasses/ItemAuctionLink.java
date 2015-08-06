package dwo.gameserver.handler.bypasses;

import dwo.config.Config;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.ItemAuctionManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.itemauction.ItemAuction;
import dwo.gameserver.model.items.itemauction.ItemAuctionInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExItemAuctionInfo;
import org.apache.log4j.Level;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Item auction functions handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class ItemAuctionLink extends CommandHandler<String>
{
	private static final SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");

	@TextCommand
	public boolean itemAuction(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		L2Character target = params.getTarget();
		if(!(target instanceof L2Npc))
		{
			return false;
		}

		if(!Config.ALT_ITEM_AUCTION_ENABLED)
		{
			activeChar.sendPacket(SystemMessageId.NO_AUCTION_PERIOD);
			return true;
		}

		ItemAuctionInstance au = ItemAuctionManager.getInstance().getManagerInstance(((L2Npc) target).getNpcId());
		if(au == null)
		{
			return false;
		}

		try
		{
			if(params.getArgs().isEmpty())
			{
				return false;
			}

			String cmd = params.getArgs().get(0);
			return "show".equalsIgnoreCase(cmd) ? showAuction(activeChar, au) : "cancel".equalsIgnoreCase(cmd) && cancelAuction(activeChar, au);
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in: " + getClass().getSimpleName() + ':' + e.getMessage(), e);
		}

		return true;
	}

	// TODO: This all below should be a part of auction manager.
	public boolean showAuction(L2PcInstance activeChar, ItemAuctionInstance au)
	{
		if(!activeChar.getFloodProtectors().getItemAuction().tryPerformAction(FloodAction.ITEM_AUCTION_SHOW))
		{
			return false;
		}

		if(activeChar.isItemAuctionPolling())
		{
			return false;
		}

		ItemAuction currentAuction = au.getCurrentAuction();
		ItemAuction nextAuction = au.getNextAuction();

		if(currentAuction == null)
		{
			activeChar.sendPacket(SystemMessageId.NO_AUCTION_PERIOD);

			if(nextAuction != null) // used only once when database is empty
			{
				activeChar.sendMessage("The next auction will begin on the " + fmt.format(new Date(nextAuction.getStartingTime())) + '.');
			}
			return true;
		}

		activeChar.sendPacket(new ExItemAuctionInfo(false, currentAuction, nextAuction));
		return true;
	}

	public boolean cancelAuction(L2PcInstance activeChar, ItemAuctionInstance au)
	{
		ItemAuction[] auctions = au.getAuctionsByBidder(activeChar.getObjectId());
		boolean returned = false;
		for(ItemAuction auction : auctions)
		{
			if(auction.cancelBid(activeChar))
			{
				returned = true;
			}
		}
		if(!returned)
		{
			activeChar.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
		}
		return true;
	}
}