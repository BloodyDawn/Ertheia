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
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.RaidBossSpawnManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class AdminTeleport implements IAdminCommandHandler
{
	private static final Logger _log = LogManager.getLogger(AdminTeleport.class);

	private static final String[] ADMIN_COMMANDS = {
		"admin_show_moves", "admin_show_moves_other", "admin_show_teleport", "admin_teleport_to_character",
		"admin_teleportto", "admin_move_to", "admin_teleport_character", "admin_recall", "admin_walk", "teleportto",
		"recall", "admin_recall_npc", "admin_gonorth", "admin_gosouth", "admin_goeast", "admin_gowest", "admin_goup",
		"admin_godown", "admin_tele", "admin_teleto", "admin_instant_move"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_teleto"))
		{
			activeChar.setTeleMode(1);
		}
		if(command.equals("admin_instant_move"))
		{
			activeChar.setTeleMode(1);
		}
		if(command.equals("admin_teleto r"))
		{
			activeChar.setTeleMode(2);
		}
		if(command.equals("admin_teleto end"))
		{
			activeChar.setTeleMode(0);
		}
		if(command.equals("admin_show_moves"))
		{
			AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
		}
		if(command.equals("admin_show_moves_other"))
		{
			AdminHelpPage.showHelpPage(activeChar, "tele/other.html");
		}
		else if(command.equals("admin_show_teleport"))
		{
			showTeleportCharWindow(activeChar);
		}
		else if(command.equals("admin_recall_npc"))
		{
			recallNPC(activeChar);
		}
		else if(command.equals("admin_teleport_to_character"))
		{
			teleportToCharacter(activeChar, activeChar.getTarget());
		}
		else if(command.startsWith("admin_walk"))
		{
			try
			{
				String val = command.substring(11);
				StringTokenizer st = new StringTokenizer(val);
				String x1 = st.nextToken();
				int x = Integer.parseInt(x1);
				String y1 = st.nextToken();
				int y = Integer.parseInt(y1);
				String z1 = st.nextToken();
				int z = Integer.parseInt(z1);
				activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(x, y, z, 0));
			}
			catch(Exception e)
			{
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "admin_walk: " + e);
				}
			}
		}
		else if(command.startsWith("admin_move_to"))
		{
			try
			{
				String val = command.substring(14);
				teleportTo(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				//Case of empty or missing coordinates
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
			}
			catch(NumberFormatException nfe)
			{
				activeChar.sendMessage("Usage: //move_to <x> <y> <z>");
				AdminHelpPage.showHelpPage(activeChar, "teleports.htm");
			}
		}
		else if(command.startsWith("admin_teleport_character"))
		{
			try
			{
				String val = command.substring(25);

				teleportCharacter(activeChar, val);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				//Case of empty coordinates
				activeChar.sendMessage("Wrong or no Coordinates given.");
				showTeleportCharWindow(activeChar); //back to character teleport
			}
		}
		else if(command.startsWith("admin_teleportto "))
		{
			try
			{
				String targetName = command.substring(17);
				L2PcInstance player = WorldManager.getInstance().getPlayer(targetName);
				teleportToCharacter(activeChar, player);
			}
			catch(StringIndexOutOfBoundsException e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
		else if(command.startsWith("admin_recall "))
		{
			try
			{
				String[] param = command.split(" ");
				if(param.length != 2)
				{
					activeChar.sendMessage("Usage: //recall <playername>");
					return false;
				}
				String targetName = param[1];
				L2PcInstance player = WorldManager.getInstance().getPlayer(targetName);
				if(player != null)
				{
					teleportCharacter(player, activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar);
				}
				else
				{
					changeCharacterPosition(activeChar, targetName);
				}
			}
			catch(StringIndexOutOfBoundsException e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
		else if(command.equals("admin_tele"))
		{
			showTeleportWindow(activeChar);
		}
		else if(command.startsWith("admin_go"))
		{
			int intVal = 150;
			int x = activeChar.getX();
			int y = activeChar.getY();
			int z = activeChar.getZ();
			try
			{
				String val = command.substring(8);
				StringTokenizer st = new StringTokenizer(val);
				String dir = st.nextToken();
				if(st.hasMoreTokens())
				{
					intVal = Integer.parseInt(st.nextToken());
				}
				switch(dir)
				{
					case "east":
						x += intVal;
						break;
					case "west":
						x -= intVal;
						break;
					case "north":
						y -= intVal;
						break;
					case "south":
						y += intVal;
						break;
					case "up":
						z += intVal;
						break;
					case "down":
						z -= intVal;
						break;
				}
				activeChar.teleToLocation(x, y, z, false);
				showTeleportWindow(activeChar);
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage: //go<north|south|east|west|up|down> [offset] (default 150)");
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void teleportTo(L2PcInstance activeChar, String Coords)
	{
		try
		{
			StringTokenizer st = new StringTokenizer(Coords);
			String x1 = st.nextToken();
			int x = Integer.parseInt(x1);
			String y1 = st.nextToken();
			int y = Integer.parseInt(y1);
			String z1 = st.nextToken();
			int z = Integer.parseInt(z1);

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, false);

			activeChar.sendMessage("You have been teleported to " + Coords);
		}
		catch(NoSuchElementException nsee)
		{
			activeChar.sendMessage("Wrong or no Coordinates given.");
		}
	}

	private void showTeleportWindow(L2PcInstance activeChar)
	{
		AdminHelpPage.showHelpPage(activeChar, "move.htm");
	}

	private void showTeleportCharWindow(L2PcInstance activeChar)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

		String replyMSG = StringUtil.concat("<html><title>Teleport Character</title>" +
			"<body>" +
			"The character you will teleport is ", player.getName(), '.' +
			"<br>" +
			"Co-ordinate x" +
			"<edit var=\"char_cord_x\" width=110>" +
			"Co-ordinate y" +
			"<edit var=\"char_cord_y\" width=110>" +
			"Co-ordinate z" +
			"<edit var=\"char_cord_z\" width=110>" +
			"<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" +
			"<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character ", String.valueOf(activeChar.getX()), " ", String.valueOf(activeChar.getY()), " ", String.valueOf(activeChar.getZ()), "\" width=115 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">" +
			"<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>" +
			"</body></html>");
		adminReply.setHtml(replyMSG);
		activeChar.sendPacket(adminReply);
	}

	private void teleportCharacter(L2PcInstance activeChar, String Cords)
	{
		L2Object target = activeChar.getTarget();
		L2PcInstance player = null;
		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		if(player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			try
			{
				StringTokenizer st = new StringTokenizer(Cords);
				String x1 = st.nextToken();
				int x = Integer.parseInt(x1);
				String y1 = st.nextToken();
				int y = Integer.parseInt(y1);
				String z1 = st.nextToken();
				int z = Integer.parseInt(z1);
				teleportCharacter(player, x, y, z, null);
			}
			catch(NoSuchElementException nsee)
			{
				_log.log(Level.ERROR, "", nsee);
			}
		}
	}

	/**
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 */
	private void teleportCharacter(L2PcInstance player, int x, int y, int z, L2PcInstance activeChar)
	{
		if(player != null)
		{
			// Check for jail
			if(player.isInJail())
			{
				activeChar.sendMessage("Sorry, player " + player.getName() + " is in Jail.");
			}
			else
			{
				// Set player to same instance as GM teleporting.
				if(activeChar != null && activeChar.getInstanceId() >= 0)
				{
					player.getInstanceController().setInstanceId(activeChar.getInstanceId());
				}
				else
				{
					player.getInstanceController().setInstanceId(0);
				}

				// Information
				activeChar.sendMessage("You have recalled " + player.getName());
				player.sendMessage("Admin is teleporting you.");

				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				player.teleToLocation(x, y, z, true);
			}
		}
	}

	private void teleportToCharacter(L2PcInstance activeChar, L2Object target)
	{
		if(target == null)
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		L2PcInstance player = null;
		if(target instanceof L2PcInstance)
		{
			player = (L2PcInstance) target;
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		if(player.getObjectId() == activeChar.getObjectId())
		{
			player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
		}
		else
		{
			// move to targets instance
			activeChar.getInstanceController().setInstanceId(target.getInstanceId());

			int x = player.getX();
			int y = player.getY();
			int z = player.getZ();

			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			activeChar.teleToLocation(x, y, z, true);

			activeChar.sendMessage("You have teleported to character " + player.getName() + '.');
		}
	}

	private void changeCharacterPosition(L2PcInstance activeChar, String name)
	{
		int x = activeChar.getX();
		int y = activeChar.getY();
		int z = activeChar.getZ();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_X_Y_Z);
			statement.setInt(1, x);
			statement.setInt(2, y);
			statement.setInt(3, z);
			statement.setString(4, name);
			statement.execute();
			int count = statement.getUpdateCount();
			if(count == 0)
			{
				activeChar.sendMessage("Character not found or position unaltered.");
			}
			else
			{
				activeChar.sendMessage("Player's [" + name + "] position is now set to (" + x + ',' + y + ',' + z + ").");
			}
		}
		catch(SQLException se)
		{
			activeChar.sendMessage("SQLException while changing offline character's position");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void recallNPC(L2PcInstance activeChar)
	{
		L2Object obj = activeChar.getTarget();
		if(obj instanceof L2Npc && !((L2Npc) obj).isMinion() && !(obj instanceof L2RaidBossInstance) && !(obj instanceof L2GrandBossInstance))
		{
			L2Npc target = (L2Npc) obj;

			int monsterTemplate = target.getTemplate().getNpcId();
			L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
			if(template1 == null)
			{
				activeChar.sendMessage("Incorrect monster template.");
				_log.log(Level.WARN, "ERROR: NPC " + target.getObjectId() + " has a 'null' template.");
				return;
			}

			L2Spawn spawn = target.getSpawn();
			if(spawn == null)
			{
				activeChar.sendMessage("Incorrect monster spawn.");
				_log.log(Level.WARN, "ERROR: NPC " + target.getObjectId() + " has a 'null' spawn.");
				return;
			}
			int respawnTime = spawn.getRespawnDelay() / 1000;

			target.getLocationController().delete();
			spawn.stopRespawn();
			SpawnTable.getInstance().deleteSpawn(spawn);

			try
			{
				spawn = new L2Spawn(template1);
				if(Config.SAVE_GMSPAWN_ON_CUSTOM)
				{
					spawn.setCustom(true);
				}
				spawn.setLocx(activeChar.getX());
				spawn.setLocy(activeChar.getY());
				spawn.setLocz(activeChar.getZ());
				spawn.setAmount(1);
				spawn.setHeading(activeChar.getHeading());
				spawn.setRespawnDelay(respawnTime);
				if(activeChar.getInstanceId() >= 0)
				{
					spawn.setInstanceId(activeChar.getInstanceId());
				}
				else
				{
					spawn.setInstanceId(0);
				}
				SpawnTable.getInstance().addNewSpawn(spawn);
				spawn.init();

				activeChar.sendMessage("Created " + template1.getName() + " on " + target.getObjectId() + '.');
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Target is not in game.");
			}
		}
		else if(obj instanceof L2RaidBossInstance)
		{
			L2RaidBossInstance target = (L2RaidBossInstance) obj;
			L2Spawn spawn = target.getSpawn();
			double curHP = target.getCurrentHp();
			double curMP = target.getCurrentMp();
			if(spawn == null)
			{
				activeChar.sendMessage("Incorrect raid spawn.");
				_log.log(Level.WARN, "ERROR: NPC Id" + target.getNpcId() + " has a 'null' spawn.");
				return;
			}
			RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true);
			try
			{
				L2NpcTemplate template = NpcTable.getInstance().getTemplate(target.getNpcId());
				L2Spawn spawnDat = new L2Spawn(template);
				if(Config.SAVE_GMSPAWN_ON_CUSTOM)
				{
					spawn.setCustom(true);
				}
				spawnDat.setLocx(activeChar.getX());
				spawnDat.setLocy(activeChar.getY());
				spawnDat.setLocz(activeChar.getZ());
				spawnDat.setAmount(1);
				spawnDat.setHeading(activeChar.getHeading());
				spawnDat.setRespawnMinDelay(43200);
				spawnDat.setRespawnMaxDelay(129600);

				RaidBossSpawnManager.getInstance().addNewSpawn(spawnDat, 0, curHP, curMP);
			}
			catch(Exception e)
			{
				activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			}
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
	}
}