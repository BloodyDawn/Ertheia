package dwo.scripts.instances;

import dwo.config.FilePath;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.GraciaSeedsManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Trap;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2Territory;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 Todo:
 - no random mob spawns after mob kill
 - implement Seed of Destruction Defense state and one party instances
 - use proper zone spawn system
 Contributing authors: Gigiikun
 Please maintain consistency between the Seed scripts.
 */
public class GR_SeedOfDestruction extends Quest
{

	private static final String qn = "SoDStage1";
	private static final int INSTANCEID = 110; // this is the client number
	private static final int MIN_PLAYERS = 36;
	private static final int MAX_PLAYERS = 45;
	private static final int MAX_DEVICESPAWNEDMOBCOUNT = 100; // prevent too much mob spawn
	private static final boolean debug = false;
	// Teleports
	private static final Location ENTRY_POINT = new Location(-242759, 219981, -9986);
	private static final Location ENTRY2_POINT = new Location(-245800, 220488, -12112);
	private static final Location CENTER_POINT = new Location(-245800, 220488, -12112);
	//Traps/Skills
	private static final SkillHolder TRAP_HOLD = new SkillHolder(4186, 9); // 18720-18728
	private static final SkillHolder TRAP_STUN = new SkillHolder(4072, 10); // 18729-18736
	private static final SkillHolder TRAP_DAMAGE = new SkillHolder(5340, 4); // 18737-18770
	private static final SkillHolder TRAP_SPAWN = new SkillHolder(10002, 1); // 18771-18774 : handled in this script
	private static final int[] TRAP_18771_NPCS = {22541, 22544, 22541, 22544};
	private static final int[] TRAP_OTHER_NPCS = {22546, 22546, 22538, 22537};
	//NPCs
	private static final int ALENOS = 32526;
	private static final int TELEPORT = 32601;
	//mobs
	private static final int OBELISK = 18776;
	private static final int POWERFUL_DEVICE = 18777;
	private static final int THRONE_POWERFUL_DEVICE = 18778;
	private static final int SPAWN_DEVICE = 18696;
	private static final int TIAT = 29163;
	private static final int TIAT_GUARD = 29162;
	private static final int TIAT_GUARD_NUMBER = 5;
	private static final int TIAT_VIDEO_NPC = 29169;
	private static final Location MOVE_TO_TIAT = new Location(-250403, 207273, -11952, 16384);
	private static final Location MOVE_TO_DOOR = new Location(-251432, 214905, -12088, 16384);
	// TODO: handle this better
	private static final int[] SPAWN_MOB_IDS = {
		22536, 22537, 22538, 22539, 22540, 22541, 22542, 22543, 22544, 22547, 22550, 22551, 22552, 22596
	};
	// Doors/Walls/Zones
	private static final int[] ATTACKABLE_DOORS = {
		12240005, 12240006, 12240007, 12240008, 12240009, 12240010, 12240013, 12240014, 12240015, 12240016, 12240017,
		12240018, 12240021, 12240022, 12240023, 12240024, 12240025, 12240026, 12240028, 12240029, 12240030
	};
	private static final int[] ENTRANCE_ROOM_DOORS = {12240001, 12240002};
	private static final int[] SQUARE_DOORS = {12240003, 12240004, 12240011, 12240012, 12240019, 12240020};
	private static final int SCOUTPASS_DOOR = 12240027;
	private static final int FORTRESS_DOOR = 12240030;
	private static final int THRONE_DOOR = 12240031;
	// Initialization at 6:30 am on Wednesday and Saturday
	private static final int RESET_HOUR = 6;
	private static final int RESET_MIN = 30;
	private static final int RESET_DAY_1 = 4;
	private static final int RESET_DAY_2 = 7;
	private TIntObjectHashMap<L2Territory> _spawnZoneList = new TIntObjectHashMap<>();

	// spawns
	private TIntObjectHashMap<List<SODSpawn>> _spawnList = new TIntObjectHashMap<>();
	private List<Integer> _mustKillMobsId = new FastList<>();

	public GR_SeedOfDestruction()
	{

		load();
		addStartNpc(ALENOS, TELEPORT);
		addTalkId(ALENOS, TELEPORT);
		addAttackId(OBELISK, TIAT);
		addKillId(OBELISK, POWERFUL_DEVICE, THRONE_POWERFUL_DEVICE, TIAT, SPAWN_DEVICE, TIAT_GUARD);
		addSpawnId(OBELISK, POWERFUL_DEVICE, TIAT_GUARD, THRONE_POWERFUL_DEVICE);
		addAggroRangeEnterId(TIAT_VIDEO_NPC);
		// registering spawn traps which handled in this script
		for(int i = 18771; i <= 18774; i++)
		{
			addTrapActionId(i);
		}
		_mustKillMobsId.forEach(this::addKillId);
	}

	public static void main(String[] args)
	{
		// now call the constructor (starts up the)
		new GR_SeedOfDestruction();
	}

	private void load()
	{
		int spawnCount = 0;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			Document doc = factory.newDocumentBuilder().parse(FilePath.GR_SEED_OF_DESTRUCTION);
			Node first = doc.getFirstChild();
			if(first != null && "list".equalsIgnoreCase(first.getNodeName()))
			{
				for(Node n = first.getFirstChild(); n != null; n = n.getNextSibling())
				{
					if("npc".equalsIgnoreCase(n.getNodeName()))
					{
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if("spawn".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								Node att = attrs.getNamedItem("npcId");
								if(att == null)
								{
									_log.log(Level.ERROR, "[Seed of Destruction] Missing npcId in npc List, skipping");
									continue;
								}
								int npcId = Integer.parseInt(attrs.getNamedItem("npcId").getNodeValue());

								att = attrs.getNamedItem("flag");
								if(att == null)
								{
									_log.log(Level.ERROR, "[Seed of Destruction] Missing flag in npc List npcId: " + npcId + ", skipping");
									continue;
								}
								int flag = Integer.parseInt(attrs.getNamedItem("flag").getNodeValue());
								if(!_spawnList.contains(flag))
								{
									_spawnList.put(flag, new FastList<>());
								}

								for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if("loc".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										SODSpawn spw = new SODSpawn();
										spw.npcId = npcId;

										att = attrs.getNamedItem("x");
										if(att != null)
										{
											spw.x = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("y");
										if(att != null)
										{
											spw.y = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("z");
										if(att != null)
										{
											spw.z = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("heading");
										if(att != null)
										{
											spw.h = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("mustKill");
										if(att != null)
										{
											spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
										}
										if(spw.isNeededNextFlag)
										{
											_mustKillMobsId.add(npcId);
										}
										_spawnList.get(flag).add(spw);
										spawnCount++;
									}
									else if("zone".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										SODSpawn spw = new SODSpawn();
										spw.npcId = npcId;
										spw.isZone = true;

										att = attrs.getNamedItem("id");
										if(att != null)
										{
											spw.zone = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("count");
										if(att != null)
										{
											spw.count = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("mustKill");
										if(att != null)
										{
											spw.isNeededNextFlag = Boolean.parseBoolean(att.getNodeValue());
										}
										if(spw.isNeededNextFlag)
										{
											_mustKillMobsId.add(npcId);
										}
										_spawnList.get(flag).add(spw);
										spawnCount++;
									}
								}
							}
						}
					}
					else if("spawnZones".equalsIgnoreCase(n.getNodeName()))
					{
						for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
						{
							if("zone".equalsIgnoreCase(d.getNodeName()))
							{
								NamedNodeMap attrs = d.getAttributes();
								Node att = attrs.getNamedItem("id");
								if(att == null)
								{
									_log.log(Level.ERROR, "[Seed of Destruction] Missing id in spawnZones List, skipping");
									continue;
								}
								int id = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("minZ");
								if(att == null)
								{
									_log.log(Level.ERROR, "[Seed of Destruction] Missing minZ in spawnZones List id: " + id + ", skipping");
									continue;
								}
								int minz = Integer.parseInt(att.getNodeValue());
								att = attrs.getNamedItem("maxZ");
								if(att == null)
								{
									_log.log(Level.ERROR, "[Seed of Destruction] Missing maxZ in spawnZones List id: " + id + ", skipping");
									continue;
								}
								int maxz = Integer.parseInt(att.getNodeValue());
								L2Territory ter = new L2Territory(id);

								for(Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling())
								{
									if("point".equalsIgnoreCase(cd.getNodeName()))
									{
										attrs = cd.getAttributes();
										int x;
										int y;
										att = attrs.getNamedItem("x");
										if(att != null)
										{
											x = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}
										att = attrs.getNamedItem("y");
										if(att != null)
										{
											y = Integer.parseInt(att.getNodeValue());
										}
										else
										{
											continue;
										}

										ter.add(x, y, minz, maxz, 0);
									}
								}

								_spawnZoneList.put(id, ter);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "[Seed of Destruction] Could not parse data.xml file: " + e.getMessage(), e);
		}
		if(debug)
		{
			_log.log(Level.INFO, "[Seed of Destruction] Loaded " + spawnCount + " spawns data.");
			_log.log(Level.INFO, "[Seed of Destruction] Loaded " + _spawnZoneList.size() + " spawn zones data.");
		}
	}

	protected void openDoor(int doorId, int instanceId)
	{
		InstanceManager.getInstance().getInstance(instanceId).getDoors().stream().filter(door -> door.getDoorId() == doorId).forEach(L2DoorInstance::openMe);
	}

	protected void closeDoor(int doorId, int instanceId)
	{
		InstanceManager.getInstance().getInstance(instanceId).getDoors().stream().filter(door -> door.getDoorId() == doorId).forEach(door -> {
			if(door.isOpened())
			{
				door.closeMe();
			}
		});
	}

	private boolean checkConditions(L2PcInstance player)
	{
		if(debug)
		{
			return true;
		}
		L2Party party = player.getParty();
		if(party == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_PARTY_CANT_ENTER));
			return false;
		}
		L2CommandChannel channel = player.getParty().getCommandChannel();
		if(channel == null)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER));
			return false;
		}
		if(!channel.getLeader().equals(player))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER));
			return false;
		}
		if(channel.getMemberCount() < MIN_PLAYERS || channel.getMemberCount() > MAX_PLAYERS)
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER));
			return false;
		}
		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember.getLevel() < 75)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			if(!Util.checkIfInRange(1000, player, partyMember, true))
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
			Long reentertime = InstanceManager.getInstance().getInstanceTime(partyMember.getObjectId(), INSTANCEID);
			if(System.currentTimeMillis() < reentertime)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET);
				sm.addPcName(partyMember);
				party.broadcastPacket(sm);
				return false;
			}
		}
		return true;
	}

	protected int enterInstance(L2PcInstance player, String template, Location loc)
	{
		int instanceId = 0;
		//check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		//existing instance
		if(world != null)
		{
			if(!(world instanceof SOD1World))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}
			player.teleToInstance(loc, world.instanceId);
			return world.instanceId;
		}
		//New instance
		else
		{
			if(!checkConditions(player))
			{
				return 0;
			}
			instanceId = InstanceManager.getInstance().createDynamicInstance(template);
			world = new SOD1World();
			world.instanceId = instanceId;
			world.status = 0;
			InstanceManager.getInstance().addWorld(world);
			spawnState((SOD1World) world);
			InstanceManager.getInstance().getInstance(instanceId).getDoors().stream().filter(door -> ArrayUtils.contains(ATTACKABLE_DOORS, door.getDoorId())).forEach(door -> door.setIsAttackableDoor(true));
			_log.log(Level.INFO, "Seed of Destruction started " + template + " Instance: " + instanceId + " created by player: " + player.getName());
			// teleport players
			if(player.getParty() == null || player.getParty().getCommandChannel() == null)
			{
				player.teleToInstance(loc, instanceId);
				world.allowed.add(player.getObjectId());
			}
			else
			{
				for(L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
				{
					player.teleToInstance(loc, instanceId);
					world.allowed.add(channelMember.getObjectId());
				}
			}
			return instanceId;
		}
	}

	protected boolean checkKillProgress(L2Npc mob, SOD1World world)
	{
		if(world.npcList.containsKey(mob))
		{
			world.npcList.put(mob, true);
		}
		for(boolean isDead : world.npcList.values())
		{
			if(!isDead)
			{
				return false;
			}
		}
		return true;
	}

	private void spawnFlaggedNPCs(SOD1World world, int flag)
	{
		if(world.lock.tryLock())
		{
			try
			{
				for(SODSpawn spw : _spawnList.get(flag))
				{
					if(spw.isZone)
					{
						for(int i = 0; i < spw.count; i++)
						{
							if(_spawnZoneList.contains(spw.zone))
							{
								int[] point = _spawnZoneList.get(spw.zone).getRandomPoint();
								spawn(world, spw.npcId, point[0], point[1], GeoEngine.getInstance().getSpawnHeight(point[0], point[1], point[2], point[3]), Rnd.get(65535), spw.isNeededNextFlag);
							}
							else
							{
								_log.log(Level.INFO, "[Seed of Destruction] Missing zone: " + spw.zone);
							}
						}
					}
					else
					{
						spawn(world, spw.npcId, spw.x, spw.y, spw.z, spw.h, spw.isNeededNextFlag);
					}
				}
			}
			finally
			{
				world.lock.unlock();
			}
		}
	}

	protected boolean spawnState(SOD1World world)
	{
		if(world.lock.tryLock())
		{
			try
			{
				world.npcList.clear();
				switch(world.status)
				{
					case 0:
						spawnFlaggedNPCs(world, 0);
						break;
					case 1:
						ExShowScreenMessage message1 = new ExShowScreenMessage(NpcStringId.getNpcStringId(1800273), ExShowScreenMessage.MIDDLE_CENTER, 10000);
						sendScreenMessage(world, message1);
						for(int i : ENTRANCE_ROOM_DOORS)
						{
							openDoor(i, world.instanceId);
						}
						spawnFlaggedNPCs(world, 1);
						break;
					case 2:
					case 3:
						// handled elsewhere
						return true;
					case 4:
						ExShowScreenMessage message2 = new ExShowScreenMessage(NpcStringId.getNpcStringId(1800295), ExShowScreenMessage.MIDDLE_CENTER, 10000);
						sendScreenMessage(world, message2);
						for(int i : SQUARE_DOORS)
						{
							openDoor(i, world.instanceId);
						}
						spawnFlaggedNPCs(world, 4);
						break;
					case 5:
						openDoor(SCOUTPASS_DOOR, world.instanceId);
						spawnFlaggedNPCs(world, 3);
						spawnFlaggedNPCs(world, 5);
						break;
					case 6:
						openDoor(THRONE_DOOR, world.instanceId);
						break;
					case 7:
						spawnFlaggedNPCs(world, 7);
						break;
					case 8:
						ExShowScreenMessage message4 = new ExShowScreenMessage(NpcStringId.getNpcStringId(1800297), ExShowScreenMessage.MIDDLE_CENTER, 1000);
						sendScreenMessage(world, message4);
						world.deviceSpawnedMobCount = 0;
						spawnFlaggedNPCs(world, 8);
						break;
					case 9:
						// instance end
						break;
				}
				world.status++;
				return true;
			}
			finally
			{
				world.lock.unlock();
			}
		}
		return false;
	}

	protected void spawn(SOD1World world, int npcId, int x, int y, int z, int h, boolean addToKillTable)
	{
		// traps
		if(npcId >= 18720 && npcId <= 18774)
		{
			L2Skill skill = null;
			if(npcId <= 18728)
			{
				skill = TRAP_HOLD.getSkill();
			}
			else if(npcId <= 18736)
			{
				skill = TRAP_STUN.getSkill();
			}
			else
			{
				skill = npcId <= 18770 ? TRAP_DAMAGE.getSkill() : TRAP_SPAWN.getSkill();
			}
			addTrap(npcId, x, y, z, h, skill, world.instanceId);
			return;
		}
		L2Npc npc = addSpawn(npcId, x, y, z, h, false, 0, false, world.instanceId);
		if(addToKillTable)
		{
			world.npcList.put(npc, false);
		}
		npc.setIsNoRndWalk(true);
		if(npc.is(L2Attackable.class))
		{
			((L2Attackable) npc).setSeeThroughSilentMove(true);
		}
		if(npcId == TIAT_VIDEO_NPC)
		{
			startQuestTimer("DoorCheck", 10000, npc, null);
		}
		else if(npcId == SPAWN_DEVICE)
		{
			npc.disableCoreAI(true);
			startQuestTimer("Spawn", 10000, npc, null, true);
		}
		else if(npcId == TIAT)
		{
			for(int i = 0; i < TIAT_GUARD_NUMBER; i++)
			{
				addMinion((L2MonsterInstance) npc, TIAT_GUARD);
			}
		}
	}

	protected void setInstanceTimeRestrictions(SOD1World world)
	{
		Calendar reenter = Calendar.getInstance();
		reenter.set(Calendar.MINUTE, RESET_MIN);
		reenter.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		// if time is >= RESET_HOUR - roll to the next day
		if(reenter.getTimeInMillis() <= System.currentTimeMillis())
		{
			reenter.add(Calendar.DAY_OF_MONTH, 1);
		}
		if(reenter.get(Calendar.DAY_OF_WEEK) <= RESET_DAY_1)
		{
			while(reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_1)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		else
		{
			while(reenter.get(Calendar.DAY_OF_WEEK) != RESET_DAY_2)
			{
				reenter.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.INSTANT_ZONE_S1_RESTRICTED);
		sm.addString(InstanceManager.getInstance().getInstanceIdName(INSTANCEID));

		// set instance reenter time for all allowed players
		for(int objectId : world.allowed)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objectId);
			InstanceManager.getInstance().setInstanceTime(objectId, INSTANCEID, reenter.getTimeInMillis());
			if(player != null && player.isOnline())
			{
				player.sendPacket(sm);
			}
		}
	}

	private void sendScreenMessage(SOD1World world, ExShowScreenMessage message)
	{
		for(int objId : world.allowed)
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(objId);
			if(player != null)
			{
				player.sendPacket(message);
			}
		}
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if(world.status == 2 && npc.getNpcId() == OBELISK)
			{
				world.status = 4;
				spawnFlaggedNPCs(world, 3);
			}
			else if(world.status == 3 && npc.getNpcId() == OBELISK)
			{
				world.status = 4;
				spawnFlaggedNPCs(world, 2);
			}
			else if(world.status <= 8 && npc.getNpcId() == TIAT)
			{
				if(npc.getCurrentHp() < npc.getMaxHp() / 2)
				{
					if(spawnState(world))
					{
						startQuestTimer("TiatFullHp", 3000, npc, null);
						setInstanceTimeRestrictions(world);
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if(event.equalsIgnoreCase("Spawn"))
			{
				L2PcInstance target = WorldManager.getInstance().getPlayer(world.allowed.get(Rnd.get(world.allowed.size())));
				if(world.deviceSpawnedMobCount < MAX_DEVICESPAWNEDMOBCOUNT && target != null && target.getInstanceId() == npc.getInstanceId() && !target.isDead())
				{
					L2Attackable mob = (L2Attackable) addSpawn(SPAWN_MOB_IDS[Rnd.get(SPAWN_MOB_IDS.length)], npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz(), npc.getSpawn().getHeading(), false, 0, false, world.instanceId);
					world.deviceSpawnedMobCount++;
					mob.setSeeThroughSilentMove(true);
					mob.setRunning();
					if(world.status >= 7)
					{
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_TIAT);
					}
					else
					{
						mob.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, MOVE_TO_DOOR);
					}
				}
			}
			else if(event.equalsIgnoreCase("DoorCheck"))
			{
				L2DoorInstance tmp = InstanceManager.getInstance().getInstance(npc.getInstanceId()).getDoor(FORTRESS_DOOR);
				if(tmp.getCurrentHp() < tmp.getMaxHp())
				{
					world.deviceSpawnedMobCount = 0;
					spawnFlaggedNPCs(world, 6);
					ExShowScreenMessage message3 = new ExShowScreenMessage(NpcStringId.getNpcStringId(1800296), ExShowScreenMessage.MIDDLE_CENTER, 10000);
					sendScreenMessage(world, message3);
				}
				else
				{
					startQuestTimer("DoorCheck", 10000, npc, null);
				}
			}
			else if(event.equalsIgnoreCase("TiatFullHp"))
			{
				if(!npc.isStunned() && !npc.isInvul())
				{
					npc.setCurrentHp(npc.getMaxHp());
				}
			}
			else if(event.equalsIgnoreCase("BodyGuardThink"))
			{
				L2Character mostHate = ((L2Attackable) npc).getMostHated();
				if(mostHate != null)
				{
					double dist = Util.calculateDistance(mostHate.getXdestination(), mostHate.getXdestination(), npc.getSpawn().getLocx(), npc.getSpawn().getLocy());
					if(dist > 900)
					{
						((L2Attackable) npc).reduceHate(mostHate, ((L2Attackable) npc).getHating(mostHate));
					}
					mostHate = ((L2Attackable) npc).getMostHated();
					if(mostHate != null || ((L2Attackable) npc).getHating(mostHate) < 5)
					{
						((L2Attackable) npc).returnHome();
					}
				}
			}
		}
		return "";
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == SPAWN_DEVICE)
		{
			cancelQuestTimer("Spawn", npc, null);
			return "";
		}
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			if(world.status == 1)
			{
				if(checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if(world.status == 2)
			{
				if(checkKillProgress(npc, world))
				{
					world.status++;
				}
			}
			else if(world.status == 4 && npc.getNpcId() == OBELISK)
			{
				spawnState(world);
			}
			else if(world.status == 5 && npc.getNpcId() == POWERFUL_DEVICE || world.status == 6 && npc.getNpcId() == THRONE_POWERFUL_DEVICE)
			{
				if(checkKillProgress(npc, world))
				{
					spawnState(world);
				}
			}
			else if(world.status >= 7)
			{
				if(npc.getNpcId() == TIAT)
				{
					world.status++;
					for(int objId : world.allowed)
					{
						L2PcInstance pl = WorldManager.getInstance().getPlayer(objId);
						if(pl != null)
						{
							pl.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_SUCCESS);
						}
					}
					for(L2Npc mob : InstanceManager.getInstance().getInstance(world.instanceId).getNpcs())
					{
						mob.getLocationController().delete();
					}

					GraciaSeedsManager.getInstance().increaseSoDTiatKilled();
				}
				else if(npc.getNpcId() == TIAT_GUARD)
				{
					addMinion(((L2MonsterInstance) npc).getLeader(), TIAT_GUARD);
				}
			}
		}
		return "";
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();
		QuestState st = player.getQuestState(qn);
		if(st == null)
		{
			st = newQuestState(player);
		}
		if(npcId == ALENOS)
		{
			InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			if(GraciaSeedsManager.getInstance().getSoDState() == 1 || world instanceof SOD1World)
			{
				enterInstance(player, "SeedOfDestructionStage1.xml", ENTRY_POINT);
			}
			else if(GraciaSeedsManager.getInstance().getSoDState() == 2)
			{
				player.teleToInstance(ENTRY2_POINT, 0);
			}
		}
		else if(npcId == TELEPORT)
		{
			player.teleToInstance(CENTER_POINT, player.getInstanceId());
		}
		return "";
	}

	@Override
	public String onTrapAction(L2Trap trap, L2Character trigger, TrapAction action)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(trap.getInstanceId());
		if(tmpworld instanceof SOD1World)
		{
			SOD1World world = (SOD1World) tmpworld;
			switch(action)
			{
				case TRAP_TRIGGERED:
					if(trap.getNpcId() == 18771)
					{
						for(int npcId : TRAP_18771_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, false, world.instanceId);
						}
					}
					else
					{
						for(int npcId : TRAP_OTHER_NPCS)
						{
							addSpawn(npcId, trap.getX(), trap.getY(), trap.getZ(), trap.getHeading(), true, 0, false, world.instanceId);
						}
					}
					break;
			}
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == TIAT_GUARD)
		{
			startQuestTimer("GuardThink", 2500 + Rnd.get(-200, 200), npc, null, true);
		}
		else
		{
			npc.disableCoreAI(true);
		}
		return super.onSpawn(npc);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(!isPet && player != null)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());
			if(tmpworld instanceof SOD1World)
			{
				SOD1World world = (SOD1World) tmpworld;
				if(world.status == 7)
				{
					if(spawnState(world))
					{
						for(int objId : world.allowed)
						{
							L2PcInstance pl = WorldManager.getInstance().getPlayer(objId);
							if(pl != null)
							{
								pl.showQuestMovie(ExStartScenePlayer.SCENE_TIAT_OPENING);
							}
						}
						npc.getLocationController().delete();
					}
				}
			}
		}
		return null;
	}

	private static class SODSpawn
	{
		public boolean isZone;
		public boolean isNeededNextFlag;
		public int npcId;
		public int x;
		public int y;
		public int z;
		public int h;
		public int zone;
		public int count;
	}

	private class SOD1World extends InstanceWorld
	{
		public Map<L2Npc, Boolean> npcList = new FastMap<>();
		public int deviceSpawnedMobCount;
		public Lock lock = new ReentrantLock();

		public SOD1World()
		{
		}
	}
}