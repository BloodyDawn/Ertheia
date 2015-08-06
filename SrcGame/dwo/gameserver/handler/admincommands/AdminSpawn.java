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
import dwo.gameserver.datatables.xml.*;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.*;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.npc.spawn.AutoSpawnHandler;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class AdminSpawn implements IAdminCommandHandler
{

	private static final String[] ADMIN_COMMANDS = {
		"admin_show_spawns", "admin_spawn", "admin_spawn_monster", "admin_spawn_index", "admin_unspawnall",
		"admin_respawnall", "admin_spawn_reload", "admin_npc_index", "admin_spawn_once", "admin_show_npcs",
		"admin_teleport_reload", "admin_spawnnight", "admin_spawnday", "admin_instance_spawns", "admin_list_spawns",
		"admin_list_positions", "admin_spawn_debug_menu", "admin_spawn_debug_print", "admin_spawn_debug_print_menu"
	};
	public static Logger _log = LogManager.getLogger(AdminSpawn.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.equals("admin_show_spawns"))
		{
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		}
		else if(command.equalsIgnoreCase("admin_spawn_debug_menu"))
		{
			AdminHelpPage.showHelpPage(activeChar, "spawns_debug.htm");
		}
		else if(command.startsWith("admin_spawn_debug_print"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			L2Object target = activeChar.getTarget();
			if(target instanceof L2Npc)
			{
				try
				{
					st.nextToken();
					int type = Integer.parseInt(st.nextToken());
					printSpawn((L2Npc) target, type);
					if(command.contains("_menu"))
					{
						AdminHelpPage.showHelpPage(activeChar, "spawns_debug.htm");
					}
				}
				catch(Exception e)
				{
                    e.printStackTrace();
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
			}
		}
		else if(command.startsWith("admin_spawn_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				int level = Integer.parseInt(st.nextToken());
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch(NoSuchElementException nsee)
				{
					_log.log(Level.ERROR, "", nsee);
				}
				showMonsters(activeChar, level, from);
			}
			catch(Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if(command.equals("admin_show_npcs"))
		{
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		}
		else if(command.startsWith("admin_npc_index"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				String letter = st.nextToken();
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch(NoSuchElementException nsee)
				{
					_log.log(Level.ERROR, "", nsee);
				}
				showNpcs(activeChar, letter, from);
			}
			catch(Exception e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
		}
		else if(command.startsWith("admin_instance_spawns"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				int instance = Integer.parseInt(st.nextToken());
				if(instance >= 300000)
				{
					StringBuilder html = StringUtil.startAppend(500 + 1000, "<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=180><center>", "<font color=\"LEVEL\">Spawns for " + instance + "</font>", "</td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table><br>", "<table width=\"100%\"><tr><td width=200>NpcName</td><td width=70>Action</td></tr>");
					int counter = 0;
					int skipped = 0;
					Instance inst = InstanceManager.getInstance().getInstance(instance);
					if(inst != null)
					{
						for(L2Npc npc : inst.getNpcs())
						{
							if(!npc.isDead())
							{
								// Only 50 because of client html limitation
								if(counter < 50)
								{
									StringUtil.append(html, "<tr><td>" + npc.getName() + "</td><td>", "<a action=\"bypass -h admin_move_to " + npc.getX() + ' ' + npc.getY() + ' ' + npc.getZ() + "\">Go</a>", "</td></tr>");
									counter++;
								}
								else
								{
									skipped++;
								}
							}
						}
						StringUtil.append(html, "<tr><td>Skipped:</td><td>" + skipped + "</td></tr></table></body></html>");
						NpcHtmlMessage ms = new NpcHtmlMessage(1);
						ms.setHtml(html.toString());
						activeChar.sendPacket(ms);
					}
					else
					{
						activeChar.sendMessage("Cannot find instance " + instance);
					}
				}
				else
				{
					activeChar.sendMessage("Invalid instance number.");
				}
			}
			catch(Exception e)
			{
				activeChar.sendMessage("Usage //instance_spawns <instance_number>");
			}
		}
		else if(command.startsWith("admin_unspawnall"))
		{
			Broadcast.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING));
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			WorldManager.getInstance().deleteVisibleNpcSpawns();
			AdminTable.getInstance().broadcastMessageToGMs("NPC Unspawn completed!");
		}
		else if(command.startsWith("admin_spawnday"))
		{
			DayNightSpawnManager.getInstance().spawnDayCreatures();
		}
		else if(command.startsWith("admin_spawnnight"))
		{
			DayNightSpawnManager.getInstance().spawnNightCreatures();
		}
		else if(command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
		{
			// make sure all spawns are deleted
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			WorldManager.getInstance().deleteVisibleNpcSpawns();
			// now respawn all
			NpcTable.getInstance();
			SpawnTable.getInstance().reload();
			RaidBossSpawnManager.getInstance().reloadBosses();
			AutoSpawnHandler.getInstance().reload();
            AutoChatDataTable.getInstance().reload();
            QuestManager.getInstance().reloadAllQuests();
			AdminTable.getInstance().broadcastMessageToGMs("NPC Respawn completed!");
		}
		else if(command.startsWith("admin_teleport_reload"))
		{
			TeleportListTable.getInstance();
			AdminTable.getInstance().broadcastMessageToGMs("Teleport List Table reloaded.");
		}
		else if(command.startsWith("admin_spawn_monster") || command.startsWith("admin_spawn"))
		{
			StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				String id = st.nextToken();
				int respawnTime = 0;
				int mobCount = 1;
				if(st.hasMoreTokens())
				{
					mobCount = Integer.parseInt(st.nextToken());
					if(mobCount > 10)
					{
						activeChar.sendMessage("Превышено максимальное количество NPC для спауна, сбрасываем до 10!");
						mobCount = 10;
					}
				}
				if(st.hasMoreTokens())
				{
					respawnTime = Integer.parseInt(st.nextToken());
				}
				spawnMonster(activeChar, id, respawnTime, mobCount);
			}
			catch(Exception e)
			{
				// Case of wrong or missing monster data
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void printSpawn(L2Npc target, int type)
	{
		int i = target.getNpcId();
		int x = target.getSpawn().getLocx();
		int y = target.getSpawn().getLocy();
		int z = target.getSpawn().getLocz();
		int h = target.getSpawn().getHeading();
		switch(type)
		{
			default:
			case 0:
				_log.log(Level.INFO, "('',1," + i + ',' + x + ',' + y + ',' + z + ",0,0," + h + ",60,0,0),");
				break;
			case 1:
				_log.log(Level.INFO, "<spawn npcId=\"" + i + "\" x=\"" + x + "\" y=\"" + y + "\" z=\"" + z + "\" heading=\"" + h + "\" respawn=\"0\" />");
				break;
			case 2:
				_log.log(Level.INFO, "{ " + i + ", " + x + ", " + y + ", " + z + ", " + h + " },");
				break;
		}
	}

	private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, int mobCount)
	{
		L2Object target = activeChar.getTarget();
		if(target == null)
		{
			target = activeChar;
		}

		L2NpcTemplate template1;
		if(monsterId.matches("[0-9]*"))
		{
			//First parameter was an ID number
			int monsterTemplate = Integer.parseInt(monsterId);
			template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			//First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template1 = NpcTable.getInstance().getTemplateByName(monsterId);
		}

		try
		{
			L2Spawn spawn = new L2Spawn(template1);
			if(Config.SAVE_GMSPAWN_ON_CUSTOM)
			{
				spawn.setCustom(true);
			}
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			if(activeChar.getInstanceId() > 0)
			{
				spawn.setInstanceId(activeChar.getInstanceId());
			}
			else
			{
				spawn.setInstanceId(0);
			}
			// TODO add checks for GrandBossSpawnManager
			if(RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcId()))
			{
				activeChar.sendMessage("You cannot spawn another instance of " + template1.getName() + '.');
			}
			else
			{
				if(RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcId()) != null)
				{
					spawn.setRespawnMinDelay(43200);
					spawn.setRespawnMaxDelay(129600);
					RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.getBaseHpMax(), template1.getBaseMpMax());
				}
				else
				{
					SpawnTable.getInstance().addNewSpawn(spawn);
					spawn.init();
				}
				activeChar.sendMessage("Created " + template1.getName() + " on " + target.getObjectId());
			}
		}
		catch(Exception e)
		{
			activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND);
		}
	}

	private void showMonsters(L2PcInstance activeChar, int level, int from)
	{
		List<L2NpcTemplate> mobs = NpcTable.getInstance().getAllMonstersOfLevel(level);
		StringBuilder tb = StringUtil.startAppend(500 + mobs.size() * 80, "<html><title>Spawn Monster:</title><body><p> Level : ", Integer.toString(level), "<br>Total Npc's : ", Integer.toString(mobs.size()), "<br>");

		// Loop
		int i = from;
		for(int j = 0; i < mobs.size() && j < 50; i++, j++)
		{
			StringUtil.append(tb, "<a action=\"bypass -h admin_spawn_monster ", Integer.toString(mobs.get(i).getNpcId()), "\">", mobs.get(i).getName(), "</a><br1>");
		}

		if(i == mobs.size())
		{
			tb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		}
		else
		{
			StringUtil.append(tb, "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index ", Integer.toString(level), " ", Integer.toString(i), "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		}

		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}

	private void showNpcs(L2PcInstance activeChar, String starting, int from)
	{
		List<L2NpcTemplate> mobs = NpcTable.getInstance().getAllNpcStartingWith(starting);
		StringBuilder tb = StringUtil.startAppend(500 + mobs.size() * 80, "<html><title>Spawn Monster:</title><body><p> There are ", Integer.toString(mobs.size()), " Npcs whose name starts with ", starting, ":<br>");

		// Loop
		int i = from;
		for(int j = 0; i < mobs.size() && j < 50; i++, j++)
		{
			StringUtil.append(tb, "<a action=\"bypass -h admin_spawn_monster ", Integer.toString(mobs.get(i).getNpcId()), "\">", mobs.get(i).getName(), "</a><br1>");
		}

		if(i == mobs.size())
		{
			tb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		}
		else
		{
			StringUtil.append(tb, "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index ", starting, " ", Integer.toString(i), "\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
		}

		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
}
