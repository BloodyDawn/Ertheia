package dwo.gameserver.model.world;

import dwo.config.Config;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExSendUIEvent;
import dwo.gameserver.util.arrays.IL2Procedure;
import dwo.gameserver.util.arrays.L2FastList;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @author evill33t, GodKratos
 */

public class Instance
{
	private static final Logger _log = LogManager.getLogger(Instance.class);
	private final L2FastList<Integer> _players = new L2FastList<>(true);
	protected ScheduledFuture<?> _checkTimeUpTask;
	private int _id;
	private String _name;
	private FastList<L2Npc> _npcs = new FastList<L2Npc>().shared();
	private Map<String, List<L2Spawn>> _groupSpawns = new FastMap<>();
	private Map<Integer, L2DoorInstance> _doors = new HashMap<>();
	private Map<String, Future<?>> _tasks = new FastMap<String, Future<?>>().shared();
	private Location _spawnLoc;
	private boolean _allowSummon = true;
	private long _emptyDestroyTime = -1;
	private long _lastLeft = -1;
	private long _instanceStartTime = -1;
	private long _instanceEndTime = -1;
	private boolean _isPvPInstance;
	private boolean _showTimer;
	private boolean _isTimerIncrease = true;
	private String _timerText = "";

	public Instance(int id)
	{
		_id = id;
		_instanceStartTime = System.currentTimeMillis();
	}

	/**
	 * Returns the ID of this instance.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * Returns the name of this instance
	 */
	public String getName()
	{
		return _name;
	}

	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Returns whether summon friend type skills are allowed for this instance
	 */
	public boolean isSummonAllowed()
	{
		return _allowSummon;
	}

	/**
	 * Sets the status for the instance for summon friend type skills
	 */
	public void setAllowSummon(boolean b)
	{
		_allowSummon = b;
	}

	/***
	 * @return {@code true} if entire instance is PvP zone
	 */
	public boolean isPvPInstance()
	{
		return _isPvPInstance;
	}

    /*
      * Sets PvP zone status of the instance
      */

	public void setPvPInstance(boolean b)
	{
		_isPvPInstance = b;
	}

	/**
	 * Set the instance duration task
	 *
	 * @param duration in milliseconds
	 */
	public void setDuration(int duration)
	{
		if(_checkTimeUpTask != null)
		{
			_checkTimeUpTask.cancel(true);
		}

		_checkTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(duration), 500);
		_instanceEndTime = System.currentTimeMillis() + duration + 500;
	}

	/**
	 * Set time before empty instance will be removed
	 *
	 * @param time in milliseconds
	 */
	public void setEmptyDestroyTime(long time)
	{
		_emptyDestroyTime = time;
	}

	/**
	 * Checks if the player exists within this instance
	 *
	 * @param objectId
	 * @return true if player exists in instance
	 */
	public boolean containsPlayer(int objectId)
	{
		return _players.contains(objectId);
	}

	/**
	 * Adds the specified player to the instance
	 *
	 * @param objectId Players object ID
	 */
	public void addPlayer(int objectId)
	{
		synchronized(_players)
		{
			_players.add(objectId);
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
		if(player != null && _showTimer)
		{
			int startTime = (int) ((System.currentTimeMillis() - _instanceStartTime) / 1000);
			int endTime = (int) ((_instanceEndTime - _instanceStartTime) / 1000);
			if(_isTimerIncrease)
			{
				player.sendPacket(new ExSendUIEvent(player, 0, 1, startTime, endTime, _timerText));
			}
			else
			{
				player.sendPacket(new ExSendUIEvent(player, 0, 0, endTime - startTime, 0, _timerText));
			}
		}
	}

	public int getCountPlayers()
	{
		return _players.size();
	}

	/**
	 * Removes the specified player from the instance list
	 *
	 * @param objectId Players object ID
	 */
	public void removePlayer(Integer objectId)
	{
		_players.remove(objectId);

		if(_players.isEmpty() && _emptyDestroyTime >= 0)
		{
			_lastLeft = System.currentTimeMillis();
			setDuration((int) (_instanceEndTime - System.currentTimeMillis() - 500));
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
		if(player != null && _showTimer)
		{
			int startTime = (int) ((System.currentTimeMillis() - _instanceStartTime) / 1000);
			int endTime = (int) ((_instanceEndTime - _instanceStartTime) / 1000);
			if(_isTimerIncrease)
			{
				player.sendPacket(new ExSendUIEvent(player, 1, 1, startTime, endTime, _timerText));
			}
			else
			{
				player.sendPacket(new ExSendUIEvent(player, 1, 0, endTime - startTime, 0, _timerText));
			}
		}
	}

	/**
	 * Removes the player from the instance by setting InstanceId to 0 and teleporting to nearest town.
	 *
	 * @param objectId Player object ID.
	 */
	public void ejectPlayer(int objectId)
	{
		L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
		if(player != null && player.getInstanceId() == _id)
		{
			player.getInstanceController().setInstanceId(0);
			player.sendMessage("Вы удалены из временной зоны.");
			if(_spawnLoc != null && _spawnLoc.getX() != 0 && _spawnLoc.getY() != 0 && _spawnLoc.getZ() != 0)
			{
				player.teleToLocation(_spawnLoc);
			}
			else
			{
				player.teleToLocation(TeleportWhereType.TOWN);
			}
		}
	}

	public void addNpc(L2Npc npc)
	{
		_npcs.add(npc);
	}

	public void addGroupSpawn(String groupName, L2Spawn spawn)
	{
		if(!_groupSpawns.containsKey(groupName))
		{
			_groupSpawns.put(groupName, new FastList<>());
		}

		_groupSpawns.get(groupName).add(spawn);
	}

	public List<L2Spawn> getGroupSpawn(String groupName)
	{
		return _groupSpawns.get(groupName);
	}

	public void removeNpc(L2Npc npc)
	{
		if(npc.getSpawn() != null)
		{
			npc.getSpawn().stopRespawn();
		}
		//npc.deleteMe();
		_npcs.remove(npc);
	}

	/**
	 * Adds a door into the instance
	 *
	 * @param doorId - from doors.csv
	 * @param open   - initial state of the door
	 */
	public void addDoor(int doorId, boolean open)
	{
		if(_doors.containsKey(doorId))
		{
			return;
		}

		L2DoorInstance newdoor = DoorGeoEngine.getInstance().newInstance(doorId, _id, true);
		_doors.put(newdoor.getDoorId(), newdoor);

		newdoor.getStatus().setCurrentHpMp(newdoor.getMaxHp(), newdoor.getMaxMp());
		newdoor.setOpen(open);
		newdoor.getLocationController().spawn(newdoor.getX(), newdoor.getY(), newdoor.getZ());
	}

	public L2FastList<Integer> getPlayers()
	{
		return _players;
	}

	public FastList<L2Npc> getNpcs()
	{
		return _npcs;
	}

	public List<L2Npc> getAllByNpcId(int npcId, boolean onlyAlive)
	{
		List<L2Npc> result = new ArrayList<>();
		_npcs.stream().filter(L2Npc::isNpc).forEach(o -> {
			L2Npc npc = o;
			if(npcId == npc.getNpcId() && (!onlyAlive || !npc.isDead()))
			{
				result.add(npc);
			}
		});
		return result;
	}

	public Collection<L2DoorInstance> getDoors()
	{
		return _doors.values();
	}

	public L2DoorInstance getDoor(int id)
	{
		for(L2DoorInstance temp : getDoors())
		{
			if(temp.getDoorId() == id)
			{
				return temp;
			}
		}
		return null;
	}

	public long getInstanceEndTime()
	{
		return _instanceEndTime;
	}

	public long getInstanceStartTime()
	{
		return _instanceStartTime;
	}

	public boolean isShowTimer()
	{
		return _showTimer;
	}

	public boolean isTimerIncrease()
	{
		return _isTimerIncrease;
	}

	public String getTimerText()
	{
		return _timerText;
	}

	/**
	 * @return the spawn location for this instance to be used when leaving the instance
	 */
	public Location getReturnLoc()
	{
		return _spawnLoc;
	}

	/**
	 * @return the spawn location for this instance to be used when leaving the instance
	 */
	public Location getSpawnLoc()
	{
		return _spawnLoc;
	}

	/**
	 * Sets the spawn location for this instance to be used when leaving the instance
	 */
	public void setSpawnLoc(Location loc)
	{
		if(loc == null)
		{
			return;
		}
		_spawnLoc = loc;
	}

	public void removePlayers()
	{
		_players.executeForEach(new EjectProcedure());
		_players.clear();
	}

	public void removeNpcs()
	{
		_npcs.stream().filter(mob -> mob != null).forEach(mob -> {
			if(mob.getSpawn() != null)
			{
				mob.getSpawn().stopRespawn();
			}
			mob.getLocationController().delete();
		});
		_npcs.clear();
	}

	public void removeDoors()
	{
		_doors.values().stream().filter(door -> door != null).forEach(door -> {
			L2WorldRegion region = door.getLocationController().getWorldRegion();
			door.getLocationController().decay();

			if(region != null)
			{
				region.removeVisibleObject(door);
			}

			door.getKnownList().removeAllKnownObjects();
			WorldManager.getInstance().removeObject(door);
			DoorGeoEngine.getInstance().updateDoor(door);
		});
		_doors.clear();
	}

	public void loadInstanceTemplate(String filename)
	{
		Document doc = null;
		File xml = new File(Config.DATAPACK_ROOT, "data/maps/instances/" + filename);

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(CryptUtil.decryptOnDemand(xml), xml.getAbsolutePath());

			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if("instance".equalsIgnoreCase(n.getNodeName()))
				{
					parseInstance(n);
				}
			}
		}
		catch(IOException e)
		{
			_log.log(Level.ERROR, "Instance: can not find " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Instance: error while loading " + xml.getAbsolutePath() + " ! " + e.getMessage(), e);
		}
	}

	private void parseInstance(Node n) throws Exception
	{
		String name;
		name = n.getAttributes().getNamedItem("name").getNodeValue();
		_name = name;

		Node a;
		Node first = n.getFirstChild();
		for(n = first; n != null; n = n.getNextSibling())
		{
			if("activityTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if(a != null)
				{
					_checkTimeUpTask = ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(Integer.parseInt(a.getNodeValue()) * 60000), 15000);
					_instanceEndTime = System.currentTimeMillis() + Long.parseLong(a.getNodeValue()) * 60000 + 15000;
				}
			}
			else if("allowSummon".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if(a != null)
				{
					_allowSummon = Boolean.parseBoolean(a.getNodeValue());
				}
			}
			else if("emptyDestroyTime".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if(a != null)
				{
					_emptyDestroyTime = Long.parseLong(a.getNodeValue()) * 1000;
				}
			}
			else if("showTimer".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if(a != null)
				{
					_showTimer = Boolean.parseBoolean(a.getNodeValue());
				}
				a = n.getAttributes().getNamedItem("increase");
				if(a != null)
				{
					_isTimerIncrease = Boolean.parseBoolean(a.getNodeValue());
				}
				a = n.getAttributes().getNamedItem("text");
				if(a != null)
				{
					_timerText = a.getNodeValue();
				}
			}
			else if("PvPInstance".equalsIgnoreCase(n.getNodeName()))
			{
				a = n.getAttributes().getNamedItem("val");
				if(a != null)
				{
					_isPvPInstance = Boolean.parseBoolean(a.getNodeValue());
				}
			}
			else if("doorlist".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					int doorId = 0;
					boolean doorState = false;
					if("door".equalsIgnoreCase(d.getNodeName()))
					{
						doorId = Integer.parseInt(d.getAttributes().getNamedItem("doorId").getNodeValue());
						if(d.getAttributes().getNamedItem("open") != null)
						{
							doorState = Boolean.parseBoolean(d.getAttributes().getNamedItem("open").getNodeValue());
						}
						addDoor(doorId, doorState);
					}
				}
			}
			else if("spawnlist".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("spawn".equalsIgnoreCase(d.getNodeName()))
					{
						parseSpawn(d, false);
					}
				}
			}
			else if("spawngroups".equalsIgnoreCase(n.getNodeName()))
			{
				for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if("group".equalsIgnoreCase(d.getNodeName()))
					{
						String groupName = d.getAttributes().getNamedItem("name").getNodeValue();

						if(groupName.isEmpty())
						{
							_log.log(Level.WARN, "Error parsing spawn group: group name cannot be empty!");
						}
						else
						{
							for(Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
							{
								if("spawn".equalsIgnoreCase(e.getNodeName()))
								{
									L2Spawn spawn = parseSpawn(e, true);
									if(spawn != null)
									{
										addGroupSpawn(groupName, spawn);
									}
								}
							}
						}
					}
				}
			}
			else if("spawnpoint".equalsIgnoreCase(n.getNodeName()))
			{
				try
				{
					_spawnLoc = new Location(Integer.parseInt(n.getAttributes().getNamedItem("spawnX").getNodeValue()), Integer.parseInt(n.getAttributes().getNamedItem("spawnY").getNodeValue()), Integer.parseInt(n.getAttributes().getNamedItem("spawnZ").getNodeValue()));
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Error parsing instance xml: " + e.getMessage(), e);
					_spawnLoc = null;
				}
			}
		}
	}

	private L2Spawn parseSpawn(Node node, boolean isSpawnGroup) throws ClassNotFoundException, NoSuchMethodException
	{
		int npcId = 0;
		int x = 0;
		int y = 0;
		int z = 0;
		int respawn = 0;
		int heading = 0;
		int delay = -1;
		L2Spawn spawnDat;
		L2NpcTemplate npcTemplate;

		npcId = Integer.parseInt(node.getAttributes().getNamedItem("npcId").getNodeValue());
		x = Integer.parseInt(node.getAttributes().getNamedItem("x").getNodeValue());
		y = Integer.parseInt(node.getAttributes().getNamedItem("y").getNodeValue());
		z = Integer.parseInt(node.getAttributes().getNamedItem("z").getNodeValue());
		heading = Integer.parseInt(node.getAttributes().getNamedItem("heading").getNodeValue());
		respawn = Integer.parseInt(node.getAttributes().getNamedItem("respawn").getNodeValue());
		if(node.getAttributes().getNamedItem("onKillDelay") != null)
		{
			delay = Integer.parseInt(node.getAttributes().getNamedItem("onKillDelay").getNodeValue());
		}

		npcTemplate = NpcTable.getInstance().getTemplate(npcId);
		if(npcTemplate != null)
		{
			spawnDat = new L2Spawn(npcTemplate);
			spawnDat.setLocx(x);
			spawnDat.setLocy(y);
			spawnDat.setLocz(z);
			spawnDat.setAmount(1);
			spawnDat.setHeading(heading);
			spawnDat.setRespawnDelay(respawn);
			if(respawn == 0)
			{
				spawnDat.stopRespawn();
			}
			else
			{
				spawnDat.startRespawn();
			}
			spawnDat.setInstanceId(_id);

			if(isSpawnGroup)
			{
				if(delay > 0)
				{
					spawnDat.setOnKillDelay(delay);
				}
			}
			else
			{
				L2Npc spawned = spawnDat.doSpawn();
				if(delay >= 0 && spawned instanceof L2Attackable)
				{
					((L2Attackable) spawned).setOnKillDelay(delay);
				}
			}

			return spawnDat;
		}
		_log.log(Level.WARN, "Instance: Data missing in NPC table for ID: " + npcId + " in Instance " + _id);
		return null;
	}

	protected void doCheckTimeUp(int remaining)
	{
		Say2 cs = null;
		int timeLeft;
		int interval;

		if(_players.isEmpty() && _emptyDestroyTime == 0)
		{
			remaining = 0;
			interval = 500;
		}
		else if(_players.isEmpty() && _emptyDestroyTime > 0)
		{

			Long emptyTimeLeft = _lastLeft + _emptyDestroyTime - System.currentTimeMillis();
			if(emptyTimeLeft <= 0)
			{
				interval = 0;
				remaining = 0;
			}
			else if(remaining > 300000 && emptyTimeLeft > 300000)
			{
				interval = 300000;
				remaining -= 300000;
			}
			else if(remaining > 60000 && emptyTimeLeft > 60000)
			{
				interval = 60000;
				remaining -= 60000;
			}
			else if(remaining > 30000 && emptyTimeLeft > 30000)
			{
				interval = 30000;
				remaining -= 30000;
			}
			else
			{
				interval = 10000;
				remaining -= 10000;
			}
		}
		else if(remaining > 300000)
		{
			timeLeft = remaining / 60000;
			interval = 300000;
			remaining -= 300000;

			if(timeLeft == 10)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
				sm.addString(Integer.toString(timeLeft));
				Announcements.getInstance().announceToInstance(sm, _id);
			}
		}
		else if(remaining > 60000)
		{
			timeLeft = remaining / 60000;
			interval = 60000;
			remaining -= 60000;

			if(timeLeft == 5)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DUNGEON_EXPIRES_IN_S1_MINUTES);
				sm.addString(Integer.toString(timeLeft));
				Announcements.getInstance().announceToInstance(sm, _id);
			}
		}
		else if(remaining > 30000)
		{
			timeLeft = remaining / 1000;
			interval = 30000;
			cs = new Say2(0, ChatType.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining -= 30000;
		}
		else
		{
			timeLeft = remaining / 1000;
			interval = 10000;
			cs = new Say2(0, ChatType.ALLIANCE, "Notice", timeLeft + " seconds left.");
			remaining -= 10000;
		}

		if(cs != null)
		{
			_players.executeForEach(new BroadcastPacket(cs));
		}

		cancelTimer();
		_checkTimeUpTask = remaining >= 10000 ? ThreadPoolManager.getInstance().scheduleGeneral(new CheckTimeUp(remaining), interval) : ThreadPoolManager.getInstance().scheduleGeneral(new TimeUp(), interval);
	}

	public void cancelTimer()
	{
		if(_checkTimeUpTask != null)
		{
			_checkTimeUpTask.cancel(true);
		}
	}

	public boolean taskExists(String name)
	{
		return _tasks.containsKey(name);
	}

	public void cancelTask(String name)
	{
		cancelTask(name, true);
	}

	public void cancelTask(String name, boolean interrupt)
	{
		if(_tasks.containsKey(name))
		{
			_tasks.get(name).cancel(interrupt);
			_tasks.remove(name);
		}
	}

	public Future<?> getTask(String name)
	{
		return _tasks.get(name);
	}

	public void addTask(String name, Future<?> task)
	{
		if(_tasks.containsKey(name))
		{
			_tasks.get(name).cancel(true);
		}

		_tasks.put(name, task);
	}

	public void cancelTasks()
	{
		if(!_tasks.isEmpty())
		{
			for(Future<?> task : _tasks.values())
			{
				task.cancel(true);
			}
		}
		_tasks.clear();
	}

	@Override
	protected void finalize() throws Throwable
	{
		cancelTasks();
		super.finalize();
	}

	public class CheckTimeUp implements Runnable
	{
		private int _remaining;

		public CheckTimeUp(int remaining)
		{
			_remaining = remaining;
		}

		@Override
		public void run()
		{
			doCheckTimeUp(_remaining);
		}
	}

	public class TimeUp implements Runnable
	{
		@Override
		public void run()
		{
			InstanceManager.getInstance().destroyInstance(getId());
		}
	}

	public class EjectProcedure implements IL2Procedure<Integer>
	{
		@Override
		public boolean execute(Integer objectId)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
			if(player != null && player.getInstanceId() == getId())
			{
				player.getInstanceController().setInstanceId(0);
				player.sendMessage("Вы были выдворены из временной зоны.");
				if(getSpawnLoc() != null)
				{
					player.teleToLocation(getSpawnLoc(), true);
				}
				else
				{
					player.teleToLocation(TeleportWhereType.TOWN);
				}
			}
			return true;
		}
	}

	public class BroadcastPacket implements IL2Procedure<Integer>
	{
		private final L2GameServerPacket _packet;

		public BroadcastPacket(L2GameServerPacket packet)
		{
			_packet = packet;
		}

		@Override
		public boolean execute(Integer objectId)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
			if(player != null && player.getInstanceId() == getId())
			{
				player.sendPacket(_packet);
			}
			return true;
		}
	}
}