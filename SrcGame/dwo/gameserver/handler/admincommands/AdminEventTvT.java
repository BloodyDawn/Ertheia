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

import dwo.config.events.ConfigEventTvT;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.events.TvT.TvTEvent;
import dwo.gameserver.instancemanager.events.TvT.TvTEventTeleporter;
import dwo.gameserver.instancemanager.events.TvT.TvTLocationManager;
import dwo.gameserver.instancemanager.events.TvT.TvTManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.NickNameChanged;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.packet.show.ShowBoard;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

public class AdminEventTvT implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_tvt", "admin_tvt_add", "admin_tvt_remove", "admin_tvt_advance", "admin_tvt_loc", "admin_tvt_stop",
		"admin_tvt_start",
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		String cmd = st.nextToken();
		if(cmd.equals("admin_tvt"))
		{
			generateMainHtm(activeChar);
		}
		else if(cmd.startsWith("admin_tvt_loc"))
		{
			String token = st.hasMoreTokens() ? st.nextToken() : "";
			if(token.equals("selectMap"))
			{
				String name = "";
				while(st.hasMoreTokens())
				{
					name += st.nextToken();
				}
				findAndSendMap(activeChar, name);
			}
			else if(token.equals("createMap"))
			{
				String name = "";
				while(st.hasMoreTokens())
				{
					name += st.nextToken();
				}
				TvTLocationManager.getInstance().createNewLocation(name.trim());
				findAndSendMap(activeChar, name);
			}
			else if(token.equals("deleteMap"))
			{
				String name = "";
				while(st.hasMoreTokens())
				{
					name += st.nextToken();
				}
				TvTLocationManager.getInstance().createNewLocation(name.trim());

				Map<Integer, TvTLocationManager.TvTLocation> locs = TvTLocationManager.getInstance().getLocations();
				for(Entry<Integer, TvTLocationManager.TvTLocation> entry : locs.entrySet())
				{
					if(entry.getValue().getName().equalsIgnoreCase(name.trim()))
					{
						TvTLocationManager.getInstance().removeLocation(entry.getKey());
						activeChar.sendMessage("Map removed");
					}
				}
				generateMainHtm(activeChar);
			}
			else if(token.equals("add"))
			{
				int mapId = Integer.parseInt(st.nextToken());
				int teamId = Integer.parseInt(st.nextToken());

				Location loc = new Location(activeChar);
				TvTLocationManager.getInstance().getLocation(mapId).addLocationToTeam(teamId, loc);
				generateMapEditor(activeChar, mapId);
			}
			else if(token.equals("delete"))
			{
				int mapId = Integer.parseInt(st.nextToken());
				int teamId = Integer.parseInt(st.nextToken());
				int locId = Integer.parseInt(st.nextToken());

				TvTLocationManager.getInstance().getLocation(mapId).removeLocation(teamId, locId);
				generateLocationListAndSend(activeChar, mapId);
			}
			else if(token.equals("list"))
			{
				if(st.hasMoreTokens())
				{
					int mapId = Integer.parseInt(st.nextToken());
					generateLocationListAndSend(activeChar, mapId);
					generateMapEditor(activeChar, mapId);
				}
			}
			else if(token.equals("clear"))
			{
				TvTLocationManager.getInstance().clearMap();
				generateMainHtm(activeChar);
			}
			else if(token.equals("load"))
			{
				TvTLocationManager.getInstance().load();
				generateMainHtm(activeChar);
			}
			else if(token.equals("save"))
			{
				TvTLocationManager.getInstance().save();
				generateMainHtm(activeChar);
			}
			else if(token.equals("debug_clear"))
			{
				Map<Integer, L2Npc> debugs = TvTLocationManager.getInstance().getDebugNpcs();
				if(!debugs.isEmpty())
				{
					debugs.values().stream().filter(npc -> npc != null).forEach(npc -> npc.getLocationController().delete());
				}
				TvTLocationManager.getInstance().getDebugNpcs().clear();
				activeChar.sendMessage("All spawned debug npcs has been removed");
			}
			else if(token.equalsIgnoreCase("debug_loc"))
			{
				int mapId = Integer.parseInt(st.nextToken());
				int teamId = Integer.parseInt(st.nextToken());

				List<Location> locs = TvTLocationManager.getInstance().getLocation(mapId).getLocationsForTeam(teamId);

				if(!locs.isEmpty())
				{
					for(Location loc : locs)
					{
						L2NpcTemplate template = NpcTable.getInstance().getTemplate(13297);
						try
						{
							L2Spawn spawn = new L2Spawn(template);
							spawn.setAmount(1);
							spawn.setInstanceId(activeChar.getInstanceId());
							spawn.setLocx(loc.getX());
							spawn.setLocy(loc.getY());
							spawn.setLocz(loc.getZ());
							spawn.doSpawn(true);
							L2Npc npc = spawn.getLastSpawn();
							npc.setTitle("Место: " + loc.getId());
							npc.broadcastPacket(new NickNameChanged(npc));
							TvTLocationManager.getInstance().getDebugNpcs().put(npc.getObjectId(), npc);
						}
						catch(Exception e)
						{
							//
						}
					}
				}
				generateMapEditor(activeChar, mapId);
				activeChar.sendMessage("All debug npcs has been spawned");
			}
		}

		else if(cmd.equals("admin_tvt_add"))
		{
			L2Object target = activeChar.getTarget();

			if(!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			add(activeChar, (L2PcInstance) target);
			activeChar.sendMessage(target.getName() + " is added to TvT Event");
			generateMainHtm(activeChar);
		}
		else if(cmd.equals("admin_tvt_remove"))
		{
			L2Object target = activeChar.getTarget();

			if(!(target instanceof L2PcInstance))
			{
				activeChar.sendMessage("You should select a player!");
				return true;
			}

			remove(activeChar, (L2PcInstance) target);
			activeChar.sendMessage(target.getName() + " is removed from TvT Event");
			generateMainHtm(activeChar);
		}
		else if(cmd.equals("admin_tvt_advance"))
		{
			TvTManager.getInstance().skipDelay();
			generateMainHtm(activeChar);
		}
		else if(cmd.equals("admin_tvt_stop"))
		{
			TvTManager.getInstance().stopEvent();
		}
		else if(cmd.equals("admin_tvt_start"))
		{
			TvTManager.getInstance().startOnce();
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void add(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if(TvTEvent.isPlayerParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}

		if(!TvTEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}

		if(TvTEvent.isStarted())
		{
			new TvTEventTeleporter(playerInstance, TvTEvent.getParticipantTeamCoordinates(playerInstance.getObjectId()), true, false);
		}
	}

	private void remove(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if(!TvTEvent.removeParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}

		new TvTEventTeleporter(playerInstance, ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}

	private void findAndSendMap(L2PcInstance activeChar, String name)
	{
		Map<Integer, TvTLocationManager.TvTLocation> locs = TvTLocationManager.getInstance().getLocations();
		for(Entry<Integer, TvTLocationManager.TvTLocation> entry : locs.entrySet())
		{
			if(entry.getValue().getName().equalsIgnoreCase(name.trim()))
			{
				generateMapEditor(activeChar, entry.getKey());
				return;
			}
		}
	}

	private void generateMainHtm(L2PcInstance activeChar)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(5);
		StringBuilder sb = new StringBuilder();
		String items = "";
		Collection<TvTLocationManager.TvTLocation> locs = TvTLocationManager.getInstance().getLocations().values();
		for(TvTLocationManager.TvTLocation l : locs)
		{
			items += l.getName() + ';';
		}
		sb.append("<html><title>TvT Admin Panel</title><body>");
		sb.append("<table width=270>");
		sb.append("<tr><td>TvT Manager:</td></tr>");
		sb.append("<tr>");
		sb.append("<td><td> <button value=\"Add Player\" width=120 action=\"bypass -h admin_tvt_add\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"> </td>");
		sb.append("<td><td> <button value=\"Remove Player\" width=120 action=\"bypass -h admin_tvt_remove\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"> </td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td> <button value=\"Force TvT Start\" width=120 action=\"bypass -h admin_tvt_advance\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"> </td>");
		sb.append("<td> <button value=\"Force TvT Stop\" width=120 action=\"bypass -h admin_tvt_stop\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"> </td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td> <button value=\"Start TvT Once\" width=120 action=\"bypass -h admin_tvt_start\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"> </td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<br><center>Maps:</center><br>");
		sb.append("<table width=270>");
		sb.append("<tr>");
		sb.append("<td><combobox width=120 var=map list=").append(items).append("></td> <td><button value=\"Edit Map\" action=\"bypass -h admin_tvt_loc selectMap $map\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"> <button value=\"Delete Map\" action=\"bypass -h admin_tvt_loc deleteMap $map\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td><edit var=\"mapName\" width=120></td> <td><button value=\"Create Map\" action=\"bypass -h admin_tvt_loc createMap $mapName\" width=90 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<br><center>Tools:</center><br>");
		sb.append("<table width=270>");
		sb.append("<tr>");
		sb.append("<td><button value=\"Save\" width=80 action=\"bypass -h admin_tvt_loc save\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("<td><button value=\"Clear\" width=80 action=\"bypass -h admin_tvt_loc clear\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("<td><button value=\"Load\" width=80 action=\"bypass -h admin_tvt_loc load\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}

	public void generateMapEditor(L2PcInstance player, int mapId)
	{
		StringBuilder sb = new StringBuilder();
		NpcHtmlMessage html = new NpcHtmlMessage(5, 1);
		sb.append("<html><head><title>TvT Admin</title></head><body>");
		sb.append("<br>");
		sb.append("<center>");
		sb.append("<table width=270>");
		sb.append("<tr>");
		sb.append("<td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		sb.append("<td width=180><center>Editting map: ").append(TvTLocationManager.getInstance().getLocation(mapId).getName()).append(" (").append(mapId).append(")</center></td>");
		sb.append("<td width=45><button value=\"Back\" action=\"bypass -h admin_tvt\" width=45 height=21 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
		sb.append("</tr></table>");
		sb.append("</center>");
		sb.append("<br>");
		sb.append("<table width=270>");
		sb.append("<tr><td>Locations:</td></tr>");
		sb.append("<tr>");
		sb.append("<td><button value=\"Team1 Add Location\" width=120 action=\"bypass -h admin_tvt_loc add ").append(mapId).append(" 0\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("<td><button value=\"Team2 Add Location\" width=120 action=\"bypass -h admin_tvt_loc add ").append(mapId).append(" 1\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<table width=270>");
		sb.append("<tr>");
		sb.append("<td><button value=\"List\" width=80 action=\"bypass -h admin_tvt_loc list ").append(mapId).append("\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("<td><button value=\"Debug 0\" width=80 action=\"bypass -h admin_tvt_loc debug_loc ").append(mapId).append(" 0\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("<td><button value=\"Debug 1\" width=80 action=\"bypass -h admin_tvt_loc debug_loc ").append(mapId).append(" 1\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<table width=270>");
		sb.append("<tr>");
		sb.append("<td></td>");
		sb.append("<td><button value=\"Clear debug\" width=80 action=\"bypass -h admin_tvt_loc debug_clear\" height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		sb.append("<td></td>");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("</body></html>");
		html.setHtml(sb.toString());
		player.sendPacket(html);
	}

	public void generateLocationListAndSend(L2PcInstance player, int mapId)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>TvT Admin</title></head><body>");
		sb.append("<br><br><center>Locations of map: ").append(TvTLocationManager.getInstance().getLocation(mapId).getName()).append(" (").append(mapId).append(")</center>");
		sb.append("<br>-- Team 0<br>");
		sb.append("<table width=\"800\">");
		sb.append("<tr><td>Loc Id</td><td>X</td><td>Y</td><td>Z</td><td>Actions</td></tr>");
		List<Location> locs = TvTLocationManager.getInstance().getLocation(mapId).getLocationsForTeam(TvTLocationManager.TEAM_BLUE);
		for(Location loc : locs)
		{
			sb.append("<tr>");

			sb.append("<td>");
			sb.append(loc.getId());
			sb.append("</td>");

			sb.append("<td>");
			sb.append(loc.getX());
			sb.append("</td>");

			sb.append("<td>");
			sb.append(loc.getY());
			sb.append("</td>");

			sb.append("<td>");
			sb.append(loc.getZ());
			sb.append("</td>");

			sb.append("<td>");
			sb.append("<a action=\"bypass -h admin_move_to ").append(loc.getX()).append(' ').append(loc.getY()).append(' ').append(loc.getZ()).append("\">Go To </a> | ");
			sb.append("<a action=\"bypass -h admin_tvt_loc delete ").append(mapId).append(' ').append(TvTLocationManager.TEAM_BLUE).append(' ').append(loc.getId()).append("\">Delete </a> ");
			sb.append("</td>");

			sb.append("</tr>");
		}
		sb.append("</table><br><br>");
		sb.append("-- Team 1<br>");
		sb.append("<table width=\"800\">");
		sb.append("<tr><td>Loc Id</td><td>X</td><td>Y</td><td>Z</td><td>Actions</td></tr>");
		locs = TvTLocationManager.getInstance().getLocation(mapId).getLocationsForTeam(TvTLocationManager.TEAM_RED);
		for(Location loc : locs)
		{
			sb.append("<tr>");

			sb.append("<td>");
			sb.append(loc.getId());
			sb.append("</td>");

			sb.append("<td>");
			sb.append(loc.getX());
			sb.append("</td>");

			sb.append("<td>");
			sb.append(loc.getY());
			sb.append("</td>");

			sb.append("<td>");
			sb.append(loc.getZ());
			sb.append("</td>");

			sb.append("<td>");
			sb.append("<a action=\"bypass -h admin_move_to ").append(loc.getX()).append(' ').append(loc.getY()).append(' ').append(loc.getZ()).append("\">Go To </a> | ");
			sb.append("<a action=\"bypass -h admin_tvt_loc delete ").append(mapId).append(' ').append(TvTLocationManager.TEAM_RED).append(' ').append(loc.getId()).append("\">Delete </a> ");
			sb.append("</td>");

			sb.append("</tr>");
		}
		sb.append("</table><br><br>");
		sb.append("</body></html>");

		player.sendPacket(new ShowBoard(sb.toString(), ShowBoard.TYPE_101));
		player.sendPacket(new ShowBoard(null, ShowBoard.TYPE_102));
		player.sendPacket(new ShowBoard(null, ShowBoard.TYPE_103));
	}
}
