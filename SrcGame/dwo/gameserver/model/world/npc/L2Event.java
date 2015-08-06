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
package dwo.gameserver.model.world.npc;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.EventData;
import dwo.gameserver.util.StringUtil;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class L2Event
{
	public static final Map<Integer, String> names = new HashMap<>();
	public static final LinkedList<String> participatingPlayers = new LinkedList<>(); //TODO store by objid
	public static final Map<Integer, LinkedList<String>> players = new HashMap<>();
	public static final LinkedList<String> npcs = new LinkedList<>();
	public static final Map<String, EventData> connectionLossData = new HashMap<>();
	protected static final Logger _log = LogManager.getLogger(L2Event.class);
	public static String eventName = "";
	public static int teamsNumber;
	public static int id = 12760;
	public static boolean active;

	public static int getTeamOfPlayer(String name)
	{
		for(int i = 1; i <= players.size(); i++)
		{
			Iterable<String> temp = players.get(i);
			for(String aTemp : temp)
			{
				if(aTemp.equals(name))
				{
					return i;
				}
			}
		}
		return 0;
	}

	public static String[] getTopNKillers(int N)
	{
		//this will return top N players sorted by kills, first element in the array will be the one with more kills
		String[] killers = new String[N];
		String playerTemp = "";
		int kills = 0;
		Deque<String> killersTemp = new LinkedList<>();

		for(int k = 0; k < N; k++)
		{
			kills = 0;
			for(int i = 1; i <= teamsNumber; i++)
			{
				Iterable<String> temp = players.get(i);
				for(String aTemp : temp)
				{
					try
					{
						L2PcInstance player = WorldManager.getInstance().getPlayer(aTemp);
						if(!killersTemp.contains(player.getName()))
						{
							if(player.getEventController().getKillsCount() > kills)
							{
								kills = player.getEventController().getKillsCount();
								playerTemp = player.getName();
							}
						}
					}
					catch(Exception e)
					{
						_log.log(Level.ERROR, "", e);
					}
				}
			}
			killersTemp.add(playerTemp);
		}

		for(int i = 0; i < N; i++)
		{
			kills = 0;
			for(String aKillersTemp : killersTemp)
			{
				try
				{
					L2PcInstance player = WorldManager.getInstance().getPlayer(aKillersTemp);
					if(player.getEventController().getKillsCount() > kills)
					{
						kills = player.getEventController().getKillsCount();
						playerTemp = player.getName();
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "", e);
				}
			}
			killers[i] = playerTemp;
			killersTemp.remove(playerTemp);
		}
		return killers;
	}

	public static void showEventHtml(L2PcInstance player, String objectid)
	{
		try
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);

			DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream("data/events/" + eventName)));
			BufferedReader inbr = new BufferedReader(new InputStreamReader(in));

			StringBuilder replyMSG = new StringBuilder();
			StringUtil.append(replyMSG, "<html><body>" + "<center><font color=\"LEVEL\">", eventName, "</font><font color=\"FF0000\"> bY ", inbr.readLine(), "</font></center><br>" + "<br>", inbr.readLine());

			if(participatingPlayers.contains(player.getName()))
			{
				replyMSG.append("<br><center>You are already in the event players list !!</center></body></html>");
			}
			else
			{
				StringUtil.append(replyMSG, "<br><center><button value=\"Participate !! \" action=\"bypass -h npc_", String.valueOf(objectid), "_event_participate\" width=90 height=15 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></body></html>");
			}

			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Exception on showEventHtml(): " + e.getMessage(), e);
		}
	}

	public static void spawn(L2PcInstance target, int npcid)
	{
		L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(npcid);

		try
		{
			//L2MonsterInstance mob = new L2MonsterInstance(template1);

			L2Spawn spawn = new L2Spawn(template1);

			spawn.setLocx(target.getX() + 50);
			spawn.setLocy(target.getY() + 50);
			spawn.setLocz(target.getZ());
			spawn.setAmount(1);
			spawn.setHeading(target.getHeading());
			spawn.setRespawnDelay(1);

			SpawnTable.getInstance().addNewSpawn(spawn);

			spawn.init();
			spawn.getLastSpawn().setCurrentHp(999999999);
			spawn.getLastSpawn().setName("event inscriptor");
			spawn.getLastSpawn().setTitle(eventName);
			spawn.getLastSpawn().isEventMob = true;
			spawn.getLastSpawn().isAggressive();
			spawn.getLastSpawn().getLocationController().decay();
			spawn.getLastSpawn().getLocationController().spawn(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());

			spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));

			npcs.add(String.valueOf(spawn.getLastSpawn().getObjectId()));

		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Exception on spawn(): " + e.getMessage(), e);
		}

	}

	public static void announceAllPlayers(String text)
	{
		Broadcast.announceToOnlinePlayers(text);
	}

	public static boolean isOnEvent(L2PcInstance player)
	{
		for(int k = 0; k < teamsNumber; k++)
		{
			Iterator<String> it = players.get(k + 1).iterator();
			boolean temp = false;
			while(it.hasNext())
			{
				temp = player.getName().equalsIgnoreCase(it.next());
				if(temp)
				{
					return true;
				}
			}
		}
		return false;

	}

	public static void inscribePlayer(L2PcInstance player)
	{
		try
		{
			participatingPlayers.add(player.getName());
			player.getEventController().prepare();
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Error when signing in the event:" + e.getMessage(), e);
		}
	}
}
