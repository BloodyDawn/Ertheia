package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.gameserver.datatables.xml.CommunityTeleportData;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.restriction.RestrictionChain;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import javolution.text.TextBuilder;

import java.util.StringTokenizer;

import static dwo.gameserver.datatables.xml.CommunityTeleportData.TeleportList;
import static dwo.gameserver.datatables.xml.CommunityTeleportData.TeleportPoint;

public class TeleportBBSManager
{
	private static TeleportBBSManager _instance = new TeleportBBSManager();

	public static TeleportBBSManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new TeleportBBSManager();
		}
		return _instance;
	}

	public String parsecmd(String var, L2PcInstance activeChar, String content)
	{
		if(var.startsWith("show_"))
		{
			StringTokenizer val = new StringTokenizer(var, "_");
			val.nextToken();
			int page = Integer.parseInt(val.nextToken());
			return showTeleportList(activeChar, page, content);
		}
		if(var.startsWith("go_"))
		{
			StringTokenizer val = new StringTokenizer(var, "_");
			val.nextToken();
			int page = Integer.parseInt(val.nextToken());
			int point = Integer.parseInt(val.nextToken());
			goTeleport(activeChar, page, point);
			return showTeleportList(activeChar, page, content);
		}
		return null;
	}

	private void goTeleport(L2PcInstance activeChar, int page, int point)
	{
		if(!activeChar.getRestrictionController().check(RestrictionChain.CUSTOM_SERVICE).passed())
		{
			activeChar.sendMessage("Телепортация невозможна");
			return;
		}
		TeleportPoint teleport = CommunityTeleportData.getInstance().getTeleportPoint(page, point);
		if(teleport == null)
		{
			return;
		}

		if(teleport.getItemCount() > 0)
		{
			if(teleport.getItemId() == 57)
			{
				if(activeChar.getAdenaCount() < teleport.getItemCount())
				{
					activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
					return;
				}
				else
				{
					activeChar.reduceAdena(ProcessType.NPC, teleport.getItemCount(), activeChar, true);
				}
			}

		}
		activeChar.teleToLocation(teleport.getLocation());
	}

	private String showTeleportList(L2PcInstance activeChar, int page, String content)
	{
		TeleportList teleport = CommunityTeleportData.getInstance().getTeleportLocationList(page);
		if(teleport == null)
		{
			return null;
		}

		int blocks = (int) Math.ceil(teleport.size() / 11);

		TextBuilder html = new TextBuilder();
		html.append("<table width=220>");
		int i = 0;
		int count = 1;
		for(int key : teleport.getPointsKeys())
		{
			TeleportPoint tp = teleport.getPoint(key);
			TextBuilder link = new TextBuilder();
			link.append("_bbstop;teleport:");
			if(tp.isLink())
			{
				link.append("show_").append(tp.getItemId());
			}
			else
			{
				link.append("go_").append(teleport.getId()).append("_").append(key);
			}

			if(i == 0)
			{
				html.append("<tr>");
			}

			html.append("<td><button value=\"").append(tp.getName(activeChar)).append("\" action=\"bypass -h ").append(link.toString()).append("\" width=200 height=30 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");

			if(i == blocks || count == teleport.size())
			{
				html.append("</tr>");
			}

			if(blocks > 0)
			{
				if(i == blocks)
				{
					i = -1;
				}
				i++;
			}
			count++;
		}
		html.append("</table>");
		content = content.replace("%teleport%", html.toString());
		return content;
	}
}