/*
 * $Header: AdminTest.java, 25/07/2005 17:15:21 luisantonioa Exp $
 *
 * $Author: luisantonioa $
 * $Date: 25/07/2005 17:15:21 $
 * $Revision: 1 $
 * $Log: AdminTest.java,v $
 * Revision 1  25/07/2005 17:15:21  luisantonioa
 * Added copyright notice
 *
 *
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
package dwo.gameserver.handler.admincommands;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;

import java.util.StringTokenizer;

public class AdminZone implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_zone_check", "admin_zone_reload", "admin_zone_visual", "admin_zone_visual_clear",
		// test commands
		"admin_loc_add"
	};

	private static void showPoint(int x, int y, int z, int itemId)
	{
		L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);

		item.setCount(1);
		item.getLocationController().spawn(x, y, z);
	}

	private static void showHtml(L2PcInstance activeChar)
	{
		String htmContent = HtmCache.getInstance().getHtm(activeChar.getLang(), "mods/admin/zone.htm");
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		adminReply.setHtml(htmContent);
		adminReply.replace("%PEACE%", activeChar.isInsideZone(L2Character.ZONE_PEACE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%PVP%", activeChar.isInsideZone(L2Character.ZONE_PVP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%SIEGE%", activeChar.isInsideZone(L2Character.ZONE_SIEGE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%TOWN%", activeChar.isInsideZone(L2Character.ZONE_TOWN) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%CASTLE%", activeChar.isInsideZone(L2Character.ZONE_CASTLE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%FORT%", activeChar.isInsideZone(L2Character.ZONE_FORT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%HQ%", activeChar.isInsideZone(L2Character.ZONE_HQ) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%CLANHALL%", activeChar.isInsideZone(L2Character.ZONE_CLANHALL) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%LAND%", activeChar.isInsideZone(L2Character.ZONE_LANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOLAND%", activeChar.isInsideZone(L2Character.ZONE_NOLANDING) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOSUMMON%", activeChar.isInsideZone(L2Character.ZONE_NOSUMMONFRIEND) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%WATER%", activeChar.isInsideZone(L2Character.ZONE_WATER) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%SWAMP%", activeChar.isInsideZone(L2Character.ZONE_SWAMP) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%DANGER%", activeChar.isInsideZone(L2Character.ZONE_DANGERAREA) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%NOSTORE%", activeChar.isInsideZone(L2Character.ZONE_NOSTORE) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		adminReply.replace("%SCRIPT%", activeChar.isInsideZone(L2Character.ZONE_SCRIPT) ? "<font color=\"LEVEL\">YES</font>" : "NO");
		StringBuilder zones = new StringBuilder(100);
		L2WorldRegion region = WorldManager.getInstance().getRegion(activeChar.getX(), activeChar.getY());
		// not display id for dynamic zones
		region.getZones().stream().filter(zone -> zone.isCharacterInZone(activeChar)).forEach(zone -> {
			if(zone.getName() != null)
			{
				StringUtil.append(zones, zone.getName());
				StringUtil.append(zones, "<br1>");
				if(zone.getId() < 300000) // not display id for dynamic zones
				{
					StringUtil.append(zones, "(", String.valueOf(zone.getId()), ")");
				}
			}
			else
			{
				StringUtil.append(zones, String.valueOf(zone.getId()));
			}
			StringUtil.append(zones, " ");
		});
		adminReply.replace("%ZLIST%", zones.toString());
		activeChar.sendPacket(adminReply);
	}

	private static void getGeoRegionXY(L2PcInstance activeChar)
	{
		int worldX = activeChar.getX();
		int worldY = activeChar.getY();
		int geoX = (worldX - -327680 >> 4 >> 11) + 10;
		int geoY = (worldY - -262144 >> 4 >> 11) + 10;
		activeChar.sendMessage("GeoRegion: " + geoX + '_' + geoY);
	}

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		StringTokenizer st = new StringTokenizer(command, " ");
		String actualCommand = st.nextToken(); // Get actual command

		//String val = "";
		//if (st.countTokens() >= 1) {val = st.nextToken();}

		if(actualCommand.equalsIgnoreCase("admin_zone_check"))
		{
			showHtml(activeChar);
			activeChar.sendMessage("MapRegion: x:" + MapRegionManager.getInstance().getMapRegionX(activeChar.getX()) + " y:" + MapRegionManager.getInstance().getMapRegionY(activeChar.getY()) + " (" + MapRegionManager.getInstance().getMapRegion(activeChar.getX(), activeChar.getY()) + ')');
			getGeoRegionXY(activeChar);
			activeChar.sendMessage("Closest Town: " + MapRegionManager.getInstance().getClosestTownName(activeChar.getLoc()));

			Location loc;

			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CASTLE);
			activeChar.sendMessage("TeleToLocation (Castle): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.CLANHALL);
			activeChar.sendMessage("TeleToLocation (ClanHall): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.SIEGE_FLAG);
			activeChar.sendMessage("TeleToLocation (SiegeFlag): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());

			loc = MapRegionManager.getInstance().getTeleToLocation(activeChar, TeleportWhereType.TOWN);
			activeChar.sendMessage("TeleToLocation (Town): x:" + loc.getX() + " y:" + loc.getY() + " z:" + loc.getZ());
		}
		else if(actualCommand.equalsIgnoreCase("admin_zone_reload"))
		{
			ZoneManager.getInstance().reload();
			activeChar.sendMessage("All Zones have been reloaded");
		}
		else if(actualCommand.equalsIgnoreCase("admin_zone_visual"))
		{
			String next = st.nextToken();
			if(next.equalsIgnoreCase("all"))
			{
				for(L2ZoneType zone : ZoneManager.getInstance().getZones(activeChar))
				{
					zone.visualizeZone(activeChar.getZ());
				}
				showHtml(activeChar);
			}
			else
			{
				int zoneId = Integer.parseInt(next);
				ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(activeChar.getZ());
			}
		}
		else if(actualCommand.equalsIgnoreCase("admin_zone_visual_clear"))
		{
			ZoneManager.getInstance().clearDebugItems();
			showHtml(activeChar);
		}
		else if(actualCommand.equalsIgnoreCase("admin_loc_add"))
		{
			// А то заколебалось руками прописывать >_<
			//System.out.println("<node X=" + "\"" + activeChar.getX() + "\"" + " Y=" + "\"" + activeChar.getY() + "\"" +" />");
			//<point string="" X="-145489" Y="145154" Z="-11982" delay="30" run="true" animationWhenArrived="false"/>
			System.out.println("<point string=" + '"' + '"' + " X=" + '"' + activeChar.getX() + '"' + " Y=" + '"' + activeChar.getY() + '"' + " Z=" + '"' + activeChar.getZ() + '"' + " delay=" + '"' + '0' + '"' + " run=" + '"' + "true" + '"' + " animationWhenArrived=" + '"' + "false" + '"' + " />");
			showPoint(activeChar.getX(), activeChar.getY(), activeChar.getZ(), PcInventory.ADENA_ID);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
