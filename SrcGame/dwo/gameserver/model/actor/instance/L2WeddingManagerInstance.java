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
import dwo.gameserver.Announcements;
import dwo.gameserver.instancemanager.WeddingManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.WeddingData;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

public class L2WeddingManagerInstance extends L2Npc
{
	/**
	 * @author evill33t & squeezed
	 */
	public L2WeddingManagerInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}

	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// Standard message
		String filename = "mods/Wedding_start.htm";
		String replace = "";

		// If player has no partner
		if(player.getPartnerId() == 0)
		{
			filename = "mods/Wedding_nopartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		L2PcInstance ptarget = WorldManager.getInstance().getPlayer(player.getPartnerId());

		// Is partner online?
		if(ptarget == null || !ptarget.isOnline())
		{
			filename = "mods/Wedding_notfound.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		// Is already married?
		if(player.isMarried())
		{
			filename = "mods/Wedding_already.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		if(player.isMarryAccepted())
		{
			filename = "mods/Wedding_waitforpartner.htm";
			sendHtmlMessage(player, filename, replace);
			return;
		}
		if(command.startsWith("AcceptWedding"))
		{
			// Check for Formal Wear
			if(!wearsFormalWear(player, ptarget))
			{
				filename = "mods/Wedding_noformal.htm";
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}

			// Check and reduce wedding price
			if(player.getAdenaCount() < Config.WEDDING_PRICE || ptarget.getAdenaCount() < Config.WEDDING_PRICE)
			{
				filename = "mods/Wedding_adena.htm";
				replace = String.valueOf(Config.WEDDING_PRICE);
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			player.reduceAdena(ProcessType.NPC, Config.WEDDING_PRICE, player.getLastFolkNPC(), true);
			ptarget.reduceAdena(ProcessType.NPC, Config.WEDDING_PRICE, player.getLastFolkNPC(), true);

			// Accept the wedding request
			player.setMarryAccepted(true);
			WeddingData couple = WeddingManager.getInstance().getCouple(player.getCoupleId());
			couple.marry();

			// Messages to the couple
			player.sendMessage("Congratulations you are married!");
			player.setMarried(true);
			player.setMarryRequest(false);
			ptarget.sendMessage("Congratulations you are married!");
			ptarget.setMarried(true);
			ptarget.setMarryRequest(false);

			// Wedding march
			MagicSkillUse MSU = new MagicSkillUse(player, player, 2230, 1, 1, 0);
			player.broadcastPacket(MSU);
			MSU = new MagicSkillUse(ptarget, ptarget, 2230, 1, 1, 0);
			ptarget.broadcastPacket(MSU);

			// Fireworks
			L2Skill skill = SkillTable.FrequentSkill.LARGE_FIREWORK.getSkill();
			if(skill != null)
			{
				MSU = new MagicSkillUse(player, player, 2025, 1, 1, 0);
				player.sendPacket(MSU);
				player.broadcastPacket(MSU);
				player.useMagic(skill, false, false);

				MSU = new MagicSkillUse(ptarget, ptarget, 2025, 1, 1, 0);
				ptarget.sendPacket(MSU);
				ptarget.broadcastPacket(MSU);
				ptarget.useMagic(skill, false, false);

			}

			Announcements.getInstance().announceToAll("Congratulations to " + player.getName() + " and " + ptarget.getName() + "! They have been married.");

			filename = "mods/Wedding_accepted.htm";
			sendHtmlMessage(ptarget, filename, replace);
			sendHtmlMessage(player, filename, replace);

			return;
		}
		if(command.startsWith("DeclineWedding"))
		{
			player.setMarryRequest(false);
			ptarget.setMarryRequest(false);
			player.setMarryAccepted(false);
			ptarget.setMarryAccepted(false);

			player.sendMessage("You declined your partner's marriage request.");
			ptarget.sendMessage("Your partner declined your marriage request.");

			filename = "mods/Wedding_declined.htm";
			sendHtmlMessage(ptarget, filename, replace);
			sendHtmlMessage(player, filename, replace);

			return;
		}
		if(player.isMarryRequest())
		{
			// Check for Formal Wear
			if(!wearsFormalWear(player, ptarget))
			{
				filename = "mods/Wedding_noformal.htm";
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}
			filename = "mods/Wedding_ask.htm";
			player.setMarryRequest(false);
			ptarget.setMarryRequest(false);
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);
			return;
		}
		if(command.startsWith("AskWedding"))
		{
			// Check for Formal Wear
			if(!wearsFormalWear(player, ptarget))
			{
				filename = "mods/Wedding_noformal.htm";
				sendHtmlMessage(ptarget, filename, replace);
				sendHtmlMessage(player, filename, replace);
				return;
			}

			player.setMarryAccepted(true);
			ptarget.setMarryRequest(true);

			filename = "mods/Wedding_ask.htm";
			replace = player.getName();
			sendHtmlMessage(ptarget, filename, replace);

			filename = "mods/Wedding_requested.htm";
			replace = ptarget.getName();
			sendHtmlMessage(player, filename, replace);

			return;
		}
		sendHtmlMessage(player, filename, replace);
	}

	@Override
	public void showChatWindow(L2PcInstance player)
	{
		String filename = "mods/Wedding_start.htm";
		String replace = String.valueOf(Config.WEDDING_PRICE);

		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}

	/**
	 * Are both partners wearing formal wear? If Formal Wear check is disabled,
	 * returns True in any case.<BR>
	 *
	 * @param p1 L2PcInstance
	 * @param p2 L2PcInstance
	 * @return boolean
	 */
	private boolean wearsFormalWear(L2PcInstance p1, L2PcInstance p2)
	{
		if(Config.WEDDING_FORMALWEAR)
		{
			L2ItemInstance fw1 = p1.getChestArmorInstance();
			L2ItemInstance fw2 = p2.getChestArmorInstance();

			return !(fw1 == null || fw2 == null || fw1.getItemId() != 6408 || fw2.getItemId() != 6408);
		}

		return true;
	}

	private void sendHtmlMessage(L2PcInstance player, String filename, String replace)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(1);
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%replace%", replace);
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
}
