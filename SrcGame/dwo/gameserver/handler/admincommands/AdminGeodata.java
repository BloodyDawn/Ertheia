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
package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Nemesiss-
 */
public class AdminGeodata implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_geo_z", "admin_geo_type", "admin_geo_nswe", "admin_geo_los", "admin_geo_position", "admin_geo_bug",
		"admin_geo_load", "admin_geo_unload"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(!Config.GEODATA_ENABLED)
		{
			activeChar.sendMessage("Geo Engine is Turned Off!");
			return true;
		}

		if(command.equals("admin_geo_z"))
		{
			activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ()) + " Loc_Z = " + activeChar.getZ());
		}
		else if(command.equals("admin_geo_type"))
		{
			short type = GeoEngine.getInstance().getType(activeChar.getX(), activeChar.getY());
			activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
			short height = GeoEngine.getInstance().getHeight(activeChar.getX(), activeChar.getY(), activeChar.getZ());
			activeChar.sendMessage("GeoEngine: height = " + height);
		}
		else if(command.equals("admin_geo_nswe"))
		{
			activeChar.sendMessage("GeoEngine: Not implemented");
		}
		else if(command.equals("admin_geo_los"))
		{
			if(activeChar.getTarget() != null)
			{
				if(GeoEngine.getInstance().canSeeTarget(activeChar, activeChar.getTarget()))
				{
					activeChar.sendMessage("GeoEngine: Can See Target");
				}
				else
				{
					activeChar.sendMessage("GeoEngine: Can't See Target");
				}
			}
			else
			{
				activeChar.sendMessage("None Target!");
			}
		}
		else if(command.equals("admin_geo_position"))
		{
			int worldX = activeChar.getX();
			int worldY = activeChar.getY();
			int worldZ = activeChar.getZ();

			int geoX = GeoEngine.getGeoX(worldX);
			int geoY = GeoEngine.getGeoY(worldY);
			int regionX = GeoEngine.getRegionXY(geoX);
			int regionY = GeoEngine.getRegionXY(geoY);
			int blockX = GeoEngine.getBlockXY(geoX);
			int blockY = GeoEngine.getBlockXY(geoY);
			int cellX = GeoEngine.getCellXY(geoX);
			int cellY = GeoEngine.getCellXY(geoY);
			int height = GeoEngine.getInstance().getHeight(worldX, worldY, worldZ);
			int type = GeoEngine.getInstance().getType(worldX, worldY);

			activeChar.sendMessage("GeoEngine: Your current position: ");
			activeChar.sendMessage(".... worldX: " + worldX + " worldY: " + worldY + " worldZ: " + worldZ);
			activeChar.sendMessage(".... geoX: " + geoX + ", geoY: " + geoY);
			activeChar.sendMessage(".... regionX: " + regionX + ", regionY: " + regionY);
			activeChar.sendMessage(".... blockX: " + blockX + ", blockY: " + blockY);
			activeChar.sendMessage(".... cellX: " + cellX + ", cellY: " + cellY);
			activeChar.sendMessage(".... height: " + height + ", type: " + type);
		}
		else if(command.startsWith("admin_geo_load") || command.startsWith("admin_geo_unload") || command.startsWith("admin_geo_bug"))
		{
			activeChar.sendMessage("Sry not implemented");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
