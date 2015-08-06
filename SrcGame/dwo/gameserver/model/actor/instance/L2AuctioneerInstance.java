/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor.instance;

import dwo.config.Config;
import dwo.gameserver.instancemanager.AuctionManager;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.clanhall.ClanHallAuctionEngine;
import dwo.gameserver.model.world.residence.clanhall.ClanHallAuctionEngine.Bidder;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class L2AuctioneerInstance extends L2Npc
{
	private static final int COND_ALL_FALSE = 0;
	private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
	private static final int COND_REGULAR = 3;

	private TIntObjectHashMap<ClanHallAuctionEngine> _pendingAuctions = new TIntObjectHashMap<>();

	public L2AuctioneerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		int condition = validateCondition(player);
		if(condition <= COND_ALL_FALSE)
		{
			//TODO: html
			player.sendMessage("Wrong conditions.");
			return;
		}
		if(condition == COND_BUSY_BECAUSE_OF_SIEGE)
		{
			String filename = "auction/auction-busy.htm";
			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getLang(), filename);
			html.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(html);
			return;
		}
		if(condition == COND_REGULAR)
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			String actualCommand = st.nextToken(); // Get actual command

			String val = "";
			if(st.countTokens() >= 1)
			{
				val = st.nextToken();
			}

			if(actualCommand.equalsIgnoreCase("auction"))
			{
				if(val.isEmpty())
				{
					return;
				}

				try
				{
					int days = Integer.parseInt(val);
					try
					{
						SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
						long bid = 0;
						if(st.countTokens() >= 1)
						{
							bid = Math.min(Long.parseLong(st.nextToken()), MAX_ADENA);
						}

						ClanHallAuctionEngine a = new ClanHallAuctionEngine(player.getClan().getClanhallId(), player.getClan(), days * 86400000L, bid, ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getName());
						if(_pendingAuctions.get(a.getId()) != null)
						{
							_pendingAuctions.remove(a.getId());
						}

						_pendingAuctions.put(a.getId(), a);

						String filename = "auction/AgitSale3.htm";
						NpcHtmlMessage html = new NpcHtmlMessage(1);
						html.setFile(player.getLang(), filename);
						html.replace("%x%", val);
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MIN%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
						html.replace("%objectId%", String.valueOf(getObjectId()));
						player.sendPacket(html);
					}
					catch(Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid auction duration!");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("confirmAuction"))
			{
				try
				{
					ClanHallAuctionEngine a = _pendingAuctions.get(player.getClan().getClanhallId());
					a.confirmAuction();
					_pendingAuctions.remove(player.getClan().getClanhallId());
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid auction");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("bidding"))
			{
				if(val.isEmpty())
				{
					return;
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "bidding show successful");
				}

				try
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					int auctionId = Integer.parseInt(val);

					if(Config.DEBUG)
					{
						_log.log(Level.DEBUG, "auction test started");
					}

					String filename = "auction/AgitAuctionInfo.htm";
					ClanHallAuctionEngine a = AuctionManager.getInstance().getAuction(auctionId);

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					if(a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%", (a.getEndDate() - System.currentTimeMillis()) / 3600000 + " hours " + (a.getEndDate() - System.currentTimeMillis()) / 60000 % 60 + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_COUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
						html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + a.getId());
						html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						_log.log(Level.WARN, "Auctioneer ClanHallAuctionEngine null for AuctionId : " + auctionId);
					}

					player.sendPacket(html);
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("bid"))
			{
				if(val.isEmpty())
				{
					return;
				}

				try
				{
					int auctionId = Integer.parseInt(val);
					try
					{
						long bid = 0;
						if(st.countTokens() >= 1)
						{
							bid = Math.min(Long.parseLong(st.nextToken()), MAX_ADENA);
						}

						AuctionManager.getInstance().getAuction(auctionId).setBid(player, bid);
					}
					catch(Exception e)
					{
						player.sendMessage("Invalid bid!");
					}
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("bid1"))
			{
				if(player.getClan() == null || player.getClan().getLevel() < 2)
				{
					player.sendPacket(SystemMessageId.AUCTION_ONLY_CLAN_LEVEL_2_HIGHER);
					return;
				}

				if(val.isEmpty())
				{
					return;
				}

				if(player.getClan().getAuctionBiddedAt() > 0 && player.getClan().getAuctionBiddedAt() != Integer.parseInt(val) || player.getClan().getClanhallId() > 0)
				{
					player.sendPacket(SystemMessageId.ALREADY_SUBMITTED_BID);
					return;
				}

				try
				{
					String filename = "auction/AgitBid1.htm";

					long minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getHighestBidderMaxBid();
					if(minimumBid == 0)
					{
						minimumBid = AuctionManager.getInstance().getAuction(Integer.parseInt(val)).getStartingBid();
					}

					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
					html.replace("%PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdenaCount()));
					html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(minimumBid));
					html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
					player.sendPacket(html);
					return;
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("list"))
			{
				List<ClanHallAuctionEngine> auctions = AuctionManager.getInstance().getAuctions();
				SimpleDateFormat format = new SimpleDateFormat("yy/MM/dd");
				/** Limit for make new page, prevent client crash **/
				int limit = 15;
				int start;
				int i = 1;
				double npage = Math.ceil((float) auctions.size() / limit);

				if(val.isEmpty())
				{
					start = 1;
				}
				else
				{
					start = limit * (Integer.parseInt(val) - 1) + 1;
					limit *= Integer.parseInt(val);
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "cmd list: auction test started");
				}

				StringBuilder items = new StringBuilder();

				items.append("<table width=280 border=0><tr>");
				for(int j = 1; j <= npage; j++)
				{
					items.append("<td><center><a action=\"bypass -h npc_");
					items.append(getObjectId());
					items.append("_list ");
					items.append(j);
					items.append("\"> Page ");
					items.append(j);
					items.append(" </a></center></td>");
				}
				items.append("</tr></table>");
				items.append("<table width=280 border=0>");

				for(ClanHallAuctionEngine a : auctions)
				{
					if(a == null)
					{
						continue;
					}

					if(i > limit)
					{
						break;
					}
					if(i < start)
					{
						i++;
						continue;
					}
					i++;

					items.append("<tr>");
					items.append("<td>");
					items.append(ClanHallManager.getInstance().getClanHallById(a.getItemId()).getLocation());
					items.append("</td>");
					items.append("<td><a action=\"bypass -h npc_");
					items.append(getObjectId());
					items.append("_bidding ");
					items.append(a.getId());
					items.append("\">");
					items.append(a.getItemName());
					items.append("</a></td>");
					items.append("<td>").append(format.format(a.getEndDate()));
					items.append("</td>");
					items.append("<td>");
					items.append(a.getStartingBid());
					items.append("</td>");
					items.append("</tr>");
				}
				items.append("</table>");
				String filename = "auction/AgitAuctionList.htm";

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), filename);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				html.replace("%itemsField%", items.toString());
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("bidlist"))
			{
				int auctionId = 0;
				if(val.isEmpty())
				{
					if(player.getClan().getAuctionBiddedAt() <= 0)
					{
						return;
					}
					else
					{
						auctionId = player.getClan().getAuctionBiddedAt();
					}
				}
				else
				{
					auctionId = Integer.parseInt(val);
				}

				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "cmd bidlist: auction test started");
				}

				String biders = "";
				TIntObjectHashMap<Bidder> bidders = AuctionManager.getInstance().getAuction(auctionId).getBidders();
				for(Bidder b : bidders.values(new Bidder[0]))
				{
					biders += "<tr>" +
						"<td>" + b.getClanName() + "</td><td>" + b.getName() + "</td><td>" + b.getTimeBid().get(Calendar.YEAR) + '/' + (b.getTimeBid().get(Calendar.MONTH) + 1) + '/' + b.getTimeBid().get(Calendar.DATE) + "</td><td>" + b.getBid() + "</td>" +
						"</tr>";
				}
				String filename = "auction/AgitBidderList.htm";

				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), filename);
				html.replace("%AGIT_LIST%", biders);
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%x%", val);
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("selectedItems"))
			{
				if(player.getClan() != null && player.getClan().getClanhallId() == 0 && player.getClan().getAuctionBiddedAt() > 0)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					String filename = "auction/AgitBidInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					ClanHallAuctionEngine a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if(a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%", (a.getEndDate() - System.currentTimeMillis()) / 3600000 + " hours " + (a.getEndDate() - System.currentTimeMillis()) / 60000 % 60 + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_MYBID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getDesc());
						html.replace("%objectId%", String.valueOf(getObjectId()));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
					}
					else
					{
						_log.log(Level.WARN, "Auctioneer ClanHallAuctionEngine null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
					}

					player.sendPacket(html);
					return;
				}
				else if(player.getClan() != null && AuctionManager.getInstance().getAuction(player.getClan().getClanhallId()) != null)
				{
					SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
					String filename = "auction/AgitSaleInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					ClanHallAuctionEngine a = AuctionManager.getInstance().getAuction(player.getClan().getClanhallId());
					if(a != null)
					{
						html.replace("%AGIT_NAME%", a.getItemName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", a.getSellerClanName());
						html.replace("%OWNER_PLEDGE_MASTER%", a.getSellerName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getLocation());
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_AUCTION_REMAIN%", (a.getEndDate() - System.currentTimeMillis()) / 3600000 + " hours " + (a.getEndDate() - System.currentTimeMillis()) / 60000 % 60 + " minutes");
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_BIDCOUNT%", String.valueOf(a.getBidders().size()));
						html.replace("%AGIT_AUCTION_DESC%", ClanHallManager.getInstance().getAuctionableHallById(a.getItemId()).getDesc());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
						html.replace("%id%", String.valueOf(a.getId()));
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						_log.log(Level.WARN, "Auctioneer ClanHallAuctionEngine null for getClanhallId : " + player.getClan().getClanhallId());
					}

					player.sendPacket(html);
					return;
				}
				else if(player.getClan() != null && player.getClan().getClanhallId() != 0)
				{
					int ItemId = player.getClan().getClanhallId();
					String filename = "auction/AgitInfo.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					if(ClanHallManager.getInstance().getAuctionableHallById(ItemId) != null)
					{
						html.replace("%AGIT_NAME%", ClanHallManager.getInstance().getAuctionableHallById(ItemId).getName());
						html.replace("%AGIT_OWNER_PLEDGE_NAME%", player.getClan().getName());
						html.replace("%OWNER_PLEDGE_MASTER%", player.getClan().getLeaderName());
						html.replace("%AGIT_SIZE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(ItemId).getGrade() * 10));
						html.replace("%AGIT_LEASE%", String.valueOf(ClanHallManager.getInstance().getAuctionableHallById(ItemId).getLease()));
						html.replace("%AGIT_LOCATION%", ClanHallManager.getInstance().getAuctionableHallById(ItemId).getLocation());
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
						html.replace("%objectId%", String.valueOf(getObjectId()));
					}
					else
					{
						_log.log(Level.WARN, "Clan Hall ID NULL : " + ItemId + " Can be caused by concurent write in ClanHallManager");
					}

					player.sendPacket(html);
					return;
				}
				else if(player.getClan() != null && player.getClan().getClanhallId() == 0)
				{
					player.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
					return;
				}
				else if(player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AN_AUCTION);
					return;
				}
			}
			else if(actualCommand.equalsIgnoreCase("cancelBid"))
			{
				long bid = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).getBidders().get(player.getClanId()).getBid();
				String filename = "auction/AgitBidCancel.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), filename);
				html.replace("%AGIT_BID%", String.valueOf(bid));
				html.replace("%AGIT_BID_REMAIN%", String.valueOf((long) (bid * 0.9)));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("doCancelBid"))
			{
				if(AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()) != null)
				{
					AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt()).cancelBid(player.getClanId());
					player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANCELED_BID));
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("cancelAuction"))
			{
				if((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) != L2Clan.CP_CH_AUCTION)
				{
					String filename = "auction/not_authorized.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				String filename = "auction/AgitSaleCancel.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("doCancelAuction"))
			{
				if(AuctionManager.getInstance().getAuction(player.getClan().getClanhallId()) != null)
				{
					AuctionManager.getInstance().getAuction(player.getClan().getClanhallId()).cancelAuction();
					player.sendMessage("Your auction has been canceled");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("sale2"))
			{
				String filename = "auction/AgitSale2.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), filename);
				html.replace("%AGIT_LAST_PRICE%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("sale"))
			{
				if((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) != L2Clan.CP_CH_AUCTION)
				{
					String filename = "auction/not_authorized.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				String filename = "auction/AgitSale1.htm";
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), filename);
				html.replace("%AGIT_DEPOSIT%", String.valueOf(ClanHallManager.getInstance().getClanHallByOwner(player.getClan()).getLease()));
				html.replace("%AGIT_PLEDGE_ADENA%", String.valueOf(player.getClan().getWarehouse().getAdenaCount()));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
				html.replace("%objectId%", String.valueOf(getObjectId()));
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("rebid"))
			{
				SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
				if((player.getClanPrivileges() & L2Clan.CP_CH_AUCTION) != L2Clan.CP_CH_AUCTION)
				{
					String filename = "auction/not_authorized.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					html.replace("%objectId%", String.valueOf(getObjectId()));
					player.sendPacket(html);
					return;
				}
				try
				{
					String filename = "auction/AgitBid2.htm";
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), filename);
					ClanHallAuctionEngine a = AuctionManager.getInstance().getAuction(player.getClan().getAuctionBiddedAt());
					if(a != null)
					{
						html.replace("%AGIT_AUCTION_BID%", String.valueOf(a.getBidders().get(player.getClanId()).getBid()));
						html.replace("%AGIT_AUCTION_MINBID%", String.valueOf(a.getStartingBid()));
						html.replace("%AGIT_AUCTION_END%", String.valueOf(format.format(a.getEndDate())));
						html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
						html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + a.getId());
					}
					else
					{
						_log.log(Level.WARN, "Auctioneer ClanHallAuctionEngine null for AuctionBiddedAt : " + player.getClan().getAuctionBiddedAt());
					}

					player.sendPacket(html);
				}
				catch(Exception e)
				{
					player.sendMessage("Invalid auction!");
				}
				return;
			}
			else if(actualCommand.equalsIgnoreCase("location"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), "auction/location.htm");
				html.replace("%location%", MapRegionManager.getInstance().getClosestTownName(player.getLoc()));
				html.replace("%LOCATION%", getPictureName(player));
				html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
				player.sendPacket(html);
				return;
			}
			else if(actualCommand.equalsIgnoreCase("instancehalls"))
			{
				NpcHtmlMessage html = new NpcHtmlMessage(1);
				html.setFile(player.getLang(), "auction/instanceAuction_list.htm");
				player.sendPacket(html);

				// TODO Если сейчас не период аукциона
					/*NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), "auction/instanceAuction_notime.htm");
					player.sendPacket(html);*/
				return;
			}
			else if(actualCommand.startsWith("instancebid"))
			{
				int id = Integer.parseInt(actualCommand.split(" ")[1]); // ид инстанс аукциона
				if(player.getClan() != null && player.getClan().getLeader().getPlayerInstance().equals(player))
				{
					if(player.getClan().getLevel() >= 5) // TODO: требования в зависимости от типа инст КХ
					{
						//TODO: Дальше хз, не клана на офе 5+ лвла
					}
					else
					{
						player.sendPacket(SystemMessageId.CLAN_LEVEL_REQUIREMENTS_FOR_BIDDING_ARE_NOT_MET);
						return;
					}
				}
				else
				{
					NpcHtmlMessage html = new NpcHtmlMessage(1);
					html.setFile(player.getLang(), "auction/instanceAuction_nolvl.htm");
					player.sendPacket(html);
					return;
				}
			}
			else if(actualCommand.equalsIgnoreCase("start"))
			{
				showChatWindow(player);
				return;
			}
		}
		super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename;

		int condition = validateCondition(player);
		filename = condition == COND_BUSY_BECAUSE_OF_SIEGE ? "auction/auction-busy.htm" : "auction/auction.htm";

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcId%", String.valueOf(getNpcId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	private int validateCondition(L2PcInstance player)
	{
		if(getCastle() != null && getCastle().getCastleId() > 0)
		{
			if(getCastle().getSiege().isInProgress())
			{
				return COND_BUSY_BECAUSE_OF_SIEGE; // Busy because of siege
			}
			return COND_REGULAR;
		}

		return COND_ALL_FALSE;
	}

	private String getPictureName(L2PcInstance player)
	{
		int nearestTownId = MapRegionManager.getInstance().getMapRegionLocId(player);
		String nearestTown;

		switch(nearestTownId)
		{
			case 911:
				nearestTown = "GLUDIN";
				break;
			case 912:
				nearestTown = "GLUDIO";
				break;
			case 916:
				nearestTown = "DION";
				break;
			case 918:
				nearestTown = "GIRAN";
				break;
			case 1537:
				nearestTown = "RUNE";
				break;
			case 1538:
				nearestTown = "GODARD";
				break;
			case 1714:
				nearestTown = "SCHUTTGART";
				break;
			default:
				nearestTown = "ADEN";
				break;
		}

		return nearestTown;
	}
}