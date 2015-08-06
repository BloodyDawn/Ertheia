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
package dwo.gameserver.handler.bypasses;

import dwo.config.Config;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.games.LotteryManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;

import java.text.DateFormat;

/**
 * Loto game handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Loto extends CommandHandler<String>
{
	/**
	 * Open a Loto window on client with the text of the L2NpcInstance.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
	 * <li>Send a ServerMode->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li>
	 * <li>Send a ServerMode->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet </li><BR>
	 *
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param npc    L2Npc loto instance
	 * @param val    The number of the page of the L2NpcInstance to display
	 */
	// 0 - first buy lottery ticket window
	// 1-20 - buttons
	// 21 - second buy lottery ticket window
	// 22 - selected ticket with 5 numbers
	// 23 - current lottery jackpot
	// 24 - Previous winning numbers/Prize claim
	// >24 - check lottery ticket by item object id
	public static void showLotoWindow(L2PcInstance player, L2Npc npc, int val)
	{
		int npcId = npc.getTemplate().getNpcId();
		String filename;
		NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());

		if(val == 0) // 0 - first buy lottery ticket window
		{
			filename = npc.getHtmlPath(npcId, 1);
			html.setFile(player.getLang(), filename);
		}
		else if(val >= 1 && val <= 21) // 1-20 - buttons, 21 - second buy lottery ticket window
		{
			if(!LotteryManager.getInstance().isStarted())
			{
				//tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if(!LotteryManager.getInstance().isSellableTickets())
			{
				//tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}

			filename = npc.getHtmlPath(npcId, 5);
			html.setFile(player.getLang(), filename);

			int count = 0;
			int found = 0;
			// counting buttons and unsetting button if found
			for(int i = 0; i < 5; i++)
			{
				if(player.getLoto(i) == val)
				{
					//unsetting button
					player.setLoto(i, 0);
					found = 1;
				}
				else if(player.getLoto(i) > 0)
				{
					count++;
				}
			}

			//if not rearched limit 5 and not unseted value
			if(count < 5 && found == 0 && val <= 20)
			{
				for(int i = 0; i < 5; i++)
				{
					if(player.getLoto(i) == 0)
					{
						player.setLoto(i, val);
						break;
					}
				}
			}

			//setting pusshed buttons
			count = 0;
			for(int i = 0; i < 5; i++)
			{
				if(player.getLoto(i) > 0)
				{
					count++;
					String button = String.valueOf(player.getLoto(i));
					if(player.getLoto(i) < 10)
					{
						button = '0' + button;
					}
					String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
					String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + '"';
					html.replace(search, replace);
				}
			}

			if(count == 5)
			{
				String search = "0\">Return";
				String replace = "22\">Your lucky numbers have been selected above.";
				html.replace(search, replace);
			}
		}
		else if(val == 22) //22 - selected ticket with 5 numbers
		{
			if(!LotteryManager.getInstance().isStarted())
			{
				//tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD);
				return;
			}
			if(!LotteryManager.getInstance().isSellableTickets())
			{
				//tickets can't be sold
				player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE);
				return;
			}

			long price = Config.ALT_LOTTERY_TICKET_PRICE;
			int lotonumber = LotteryManager.getInstance().getId();
			int enchant = 0;
			int type2 = 0;

			for(int i = 0; i < 5; i++)
			{
				if(player.getLoto(i) == 0)
				{
					return;
				}

				if(player.getLoto(i) < 17)
				{
					enchant += Math.pow(2, player.getLoto(i) - 1);
				}
				else
				{
					type2 += Math.pow(2, player.getLoto(i) - 17);
				}
			}
			if(player.getAdenaCount() < price)
			{
				player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
				return;
			}
			if(!player.reduceAdena(ProcessType.LOTTO, price, npc, true))
			{
				return;
			}
			LotteryManager.getInstance().increasePrize(price);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(lotonumber).addItemName(4442));

			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4442);
			item.setCount(1);
			item.setCustomType1(lotonumber);
			item.setEnchantLevel(enchant);
			item.setCustomType2(type2);
			player.getInventory().addItem(ProcessType.LOTTO, item, player, npc);

			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(PcInventory.ADENA_ID);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);

			filename = npc.getHtmlPath(npcId, 6);
			html.setFile(player.getLang(), filename);
		}
		else if(val == 23) //23 - current lottery jackpot
		{
			filename = npc.getHtmlPath(npcId, 3);
			html.setFile(player.getLang(), filename);
		}
		else if(val == 24) // 24 - Previous winning numbers/Prize claim
		{
			filename = npc.getHtmlPath(npcId, 4);
			html.setFile(player.getLang(), filename);

			int lotonumber = LotteryManager.getInstance().getId();
			String message = "";
			for(L2ItemInstance item : player.getInventory().getItems())
			{
				if(item == null)
				{
					continue;
				}
				if(item.getItemId() == 4442 && item.getCustomType1() < lotonumber)
				{
					message = message + "<a action=\"bypass -h npc_%objectId%_Loto " + item.getObjectId() + "\">" + item.getCustomType1() + " Event Number ";
					int[] numbers = LotteryManager.getInstance().decodeNumbers(item.getEnchantLevel(), item.getCustomType2());
					for(int i = 0; i < 5; i++)
					{
						message += numbers[i] + " ";
					}
					long[] check = LotteryManager.getInstance().checkTicket(item);
					if(check[0] > 0)
					{
						switch((int) check[0])
						{
							case 1:
								message += "- 1st Prize";
								break;
							case 2:
								message += "- 2nd Prize";
								break;
							case 3:
								message += "- 3th Prize";
								break;
							case 4:
								message += "- 4th Prize";
								break;
						}
						message += " " + check[1] + "a.";
					}
					message += "</a><br>";
				}
			}
			if(message.isEmpty())
			{
				message += "There has been no winning lottery ticket.<br>";
			}
			html.replace("%result%", message);
		}
		else if(val == 25) //25 - lottery instructions
		{
			filename = npc.getHtmlPath(npcId, 2);
			html.setFile(player.getLang(), filename);
		}
		else if(val > 25) // >25 - check lottery ticket by item object id
		{
			int lotonumber = LotteryManager.getInstance().getId();
			L2ItemInstance item = player.getInventory().getItemByObjectId(val);
			if(item == null || item.getItemId() != 4442 || item.getCustomType1() >= lotonumber)
			{
				return;
			}
			long[] check = LotteryManager.getInstance().checkTicket(item);

			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(4442));

			long adena = check[1];
			if(adena > 0)
			{
				player.addAdena(ProcessType.LOTTO, adena, npc, true);
			}
			player.destroyItem(ProcessType.LOTTO, item, npc, false);
			return;
		}
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		html.replace("%race%", String.valueOf(LotteryManager.getInstance().getId()));
		html.replace("%adena%", String.valueOf(LotteryManager.getInstance().getPrize()));
		html.replace("%ticket_price%", String.valueOf(Config.ALT_LOTTERY_TICKET_PRICE));
		html.replace("%prize5%", String.valueOf(Config.ALT_LOTTERY_5_NUMBER_RATE * 100));
		html.replace("%prize4%", String.valueOf(Config.ALT_LOTTERY_4_NUMBER_RATE * 100));
		html.replace("%prize3%", String.valueOf(Config.ALT_LOTTERY_3_NUMBER_RATE * 100));
		html.replace("%prize2%", String.valueOf(Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE));
		html.replace("%enddate%", DateFormat.getDateInstance().format(LotteryManager.getInstance().getEndDate()));
		player.sendPacket(html);

		// Send a ServerMode->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	@TextCommand
	public boolean loto(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		int val;
		try
		{
			val = Integer.parseInt(params.getArgs().get(0));
		}
		catch(IndexOutOfBoundsException | NumberFormatException e)
		{
			log.log(Level.ERROR, "", e);
			return false;
		}

		if(val == 0)
		{
			// new loto ticket
			for(int i = 0; i < 5; i++)
			{
				params.getPlayer().setLoto(i, 0);
			}
		}
		showLotoWindow(params.getPlayer(), (L2Npc) params.getTarget(), val);

		return false;
	}
}