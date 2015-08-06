package dwo.scripts.instances;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DecoyInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneForm;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RB_Zaken extends Quest
{
	private static final int INSTANCEID_DAYDREAM = InstanceZoneId.CAVERN_OF_THE_PIRATE_CAPTAIN_DAYDREAM_1.getId();
	private static final int INSTANCEID_NIGHTMARE = InstanceZoneId.CAVERN_OF_THE_PIRATE_CAPTAIN_NIGHTMARE.getId();
	private static final int INSTANCEID83 = InstanceZoneId.CAVERN_OF_THE_PIRATE_CAPTAIN_DAYDREAM_ULTIMATE.getId();
	// NPCs
	//Bosses
	private static final int ZAKEN = 29176;
	private static final int ZAKEN_NIGHTMARE = 29022;
	private static final int ZAKEN83 = 29181;
	//Mobs
	private static final int DOLL_BLADER = 29023;
	private static final int VALE_MASTER = 29024;
	private static final int ZOMBIE_CAPTAIN = 29026;
	private static final int ZOMBIE = 29027;
	private static final int DOLL_BLADER83 = 29182;
	private static final int VALE_MASTER83 = 29183;
	private static final int ZOMBIE_CAPTAIN83 = 29184;
	private static final int ZOMBIE83 = 29185;
	//Barrel
	private static final int BARREL = 32705;
	//Zones for rooms
	//floor 1
	private static final int _room1zone = 120111;
	private static final int _room2zone = 120112;
	private static final int _room3zone = 120113;
	private static final int _room4zone = 120114;
	private static final int _room5zone = 120115;
	//floor 2
	private static final int _room6zone = 120116;
	private static final int _room7zone = 120117;
	private static final int _room8zone = 120118;
	private static final int _room9zone = 120119;
	private static final int _room10zone = 120120;
	//floor 3
	private static final int _room11zone = 120121;
	private static final int _room12zone = 120122;
	private static final int _room13zone = 120123;
	private static final int _room14zone = 120124;
	private static final int _room15zone = 120125;
	private static final int[] zones = {
		_room1zone, _room2zone, _room3zone, _room4zone, _room5zone, _room6zone, _room7zone, _room8zone, _room9zone,
		_room10zone, _room11zone, _room12zone, _room13zone, _room14zone, _room15zone
	};
	private static final int[] barrelSpawnzones = {
		_room4zone, _room4zone, _room3zone, _room1zone, _room3zone, _room3zone, _room5zone, _room5zone, _room3zone,
		_room2zone, _room2zone, _room1zone, _room9zone, _room9zone, _room8zone, _room6zone, _room8zone, _room8zone,
		_room10zone, _room10zone, _room8zone, _room7zone, _room7zone, _room6zone, _room14zone, _room14zone, _room13zone,
		_room11zone, _room13zone, _room13zone, _room15zone, _room15zone, _room13zone, _room12zone, _room12zone,
		_room11zone,
	};
	private static final TIntObjectHashMap<int[]> zoneBarrels = new TIntObjectHashMap<>();

	static
	{
		zoneBarrels.put(_room1zone, new int[]{3, 4, 5, 12});
		zoneBarrels.put(_room2zone, new int[]{5, 9, 10, 11});
		zoneBarrels.put(_room3zone, new int[]{3, 5, 6, 9});
		zoneBarrels.put(_room4zone, new int[]{1, 2, 3, 6});
		zoneBarrels.put(_room5zone, new int[]{6, 7, 8, 9});
		zoneBarrels.put(_room6zone, new int[]{15, 16, 17, 24});
		zoneBarrels.put(_room7zone, new int[]{17, 21, 22, 23});
		zoneBarrels.put(_room8zone, new int[]{15, 17, 18, 21});
		zoneBarrels.put(_room9zone, new int[]{13, 14, 15, 18});
		zoneBarrels.put(_room10zone, new int[]{18, 19, 20, 21});
		zoneBarrels.put(_room11zone, new int[]{27, 28, 29, 36});
		zoneBarrels.put(_room12zone, new int[]{29, 33, 34, 35});
		zoneBarrels.put(_room13zone, new int[]{27, 29, 30, 33});
		zoneBarrels.put(_room14zone, new int[]{25, 26, 27, 30});
		zoneBarrels.put(_room15zone, new int[]{30, 31, 32, 33});
	}

	private static final Location[] RandomTelelocations = {
		new Location(52684, 219989, -3496), new Location(52669, 219120, -3224), new Location(52672, 219439, -3312)
	};
	private static RB_Zaken _zakenInstance;
	// Monday 6:30AM
	private Calendar reuse_date_1;
	// Wednesday 6:30AM
	private Calendar reuse_date_2;
	// Friday 6:30AM
	private Calendar reuse_date_3;
	private long nextCalendarReschedule;

	public RB_Zaken()
	{

		addKillId(ZAKEN);
		addKillId(ZAKEN_NIGHTMARE);
		addKillId(ZAKEN83);
		addTalkId(BARREL);
		addSpellFinishedId(ZAKEN);
		addSpellFinishedId(ZAKEN_NIGHTMARE);
		addSpellFinishedId(ZAKEN83);
		for(int i = 120111; i <= 120125; i++)
		{
			addEnterZoneId(i);
			addExitZoneId(i);
		}
	}

	public static void main(String[] args)
	{
		_zakenInstance = new RB_Zaken();
	}

	public static RB_Zaken getInstance()
	{
		return _zakenInstance;
	}

	private boolean checkConditions(L2PcInstance player, int templateId)
	{
		if(player.isGM())
		{
			return true;
		}

		L2Party party = player.getParty();

		if(player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}

		if(party.isInCommandChannel())
		{
			for(L2PcInstance member : party.getCommandChannel().getMembers())
			{
				if((templateId == INSTANCEID_DAYDREAM || templateId == INSTANCEID83) && (member == null || member.getLevel() < 55 || member.getLevel() > 68))
				{
					party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
				if(templateId == INSTANCEID_NIGHTMARE && (member == null || member.getLevel() < 78))
				{
					party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
				if(!Util.checkIfInRange(1000, player, member, true))
				{
					party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
					return false;
				}

				Long reentertime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_DAYDREAM);
				Long reentertime2 = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID83);
				Long reentertime3 = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_NIGHTMARE);
				if(templateId == INSTANCEID_DAYDREAM && System.currentTimeMillis() < reentertime)
				{
					party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
				else if(templateId == INSTANCEID_NIGHTMARE && System.currentTimeMillis() < reentertime2)
				{
					party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
				else if(templateId == INSTANCEID83 && System.currentTimeMillis() < reentertime3)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
			}
		}
		else
		{
			for(L2PcInstance member : party.getMembers())
			{
				if((templateId == INSTANCEID_DAYDREAM || templateId == INSTANCEID83) && (member == null || member.getLevel() < 55 || member.getLevel() > 68))
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
				if(templateId == INSTANCEID_NIGHTMARE && (member == null || member.getLevel() < 78))
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
				if(!Util.checkIfInRange(1000, player, member, true))
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
					return false;
				}
				Long reentertime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_DAYDREAM);
				Long reentertime2 = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID83);
				Long reentertime3 = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), INSTANCEID_NIGHTMARE);
				if(templateId == INSTANCEID_DAYDREAM && System.currentTimeMillis() < reentertime)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
				else if(templateId == INSTANCEID_NIGHTMARE && System.currentTimeMillis() < reentertime2)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
				else if(templateId == INSTANCEID83 && System.currentTimeMillis() < reentertime3)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
			}
		}
		return true;
	}

	private void teleportPlayer(L2PcInstance player, Location teleto, ZakenWorld world)
	{
		player.teleToInstance(teleto, world.instanceId);
		world.playersInInstance.add(player);
	}

	public void enterInstance(L2PcInstance player, int templateId)
	{
		int instanceId;
		// check for existing instances for this player
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		int inst = checkWorld(player);
		if(inst == 0)
		{
			player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
		}
		else if(inst == 1)
		{
			teleportPlayer(player, RandomTelelocations[0], (ZakenWorld) world);
		}
		else
		{
			if(!checkConditions(player, templateId))
			{
				return;
			}

			L2Party party = player.getParty();
			instanceId = InstanceManager.getInstance().createDynamicInstance("RB_Zaken.xml");
			world = new ZakenWorld(System.currentTimeMillis() + 3600000);
			world.instanceId = instanceId;
			world.templateId = templateId;
			InstanceManager.getInstance().addWorld(world);

			startQuestTimer("ChooseZakenRoom", 1000, null, player);

			if(party.getCommandChannel() != null)
			{
				List<L2PcInstance> channelMembers = party.getCommandChannel().getMembers();

				for(L2PcInstance member : channelMembers)
				{
					teleportPlayer(member, RandomTelelocations[Rnd.get(RandomTelelocations.length)], (ZakenWorld) world);
					world.allowed.add(member.getObjectId());
				}
			}
			else
			{
				for(L2PcInstance member : party.getMembers())
				{
					teleportPlayer(member, RandomTelelocations[Rnd.get(RandomTelelocations.length)], (ZakenWorld) world);
					world.allowed.add(member.getObjectId());
				}
			}
		}
	}

	private int checkWorld(L2PcInstance player)
	{
		InstanceWorld checkworld = InstanceManager.getInstance().getPlayerWorld(player);
		if(checkworld != null)
		{
			if(!(checkworld instanceof ZakenWorld))
			{
				return 0;
			}
			return 1;
		}
		return 2;
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		ZakenWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ZakenWorld.class);

		if(world == null)
		{
			world = InstanceManager.getInstance().getInstanceWorld(player, ZakenWorld.class);
		}

		if(world != null)
		{
			switch(event)
			{
				case "RedBarrel1":
					npc.setRHandId(15280);
					npcUpdate(npc);
					startQuestTimer("RedBarrel", 5000, npc, player);
					break;
				case "BlueBarrel1":
					npc.setRHandId(15280);
					npcUpdate(npc);
					startQuestTimer("BlueBarrel", 5000, npc, player);
					break;
				case "BlueBarrel":
					npc.setRHandId(15302);
					npc.setLHandId(15302);
					npcUpdate(npc);
					world._bluecandles++;
					if(world._bluecandles == 4)
					{
						startQuestTimer("ZakenSpawn", 1000, npc, player);
					}
					break;
				case "RedBarrel":
					npc.setRHandId(15281);
					npc.setLHandId(15281);
					npcUpdate(npc);
					for(L2Npc barrel : world.barrels)
					{
						if(barrel.equals(npc))
						{
							int zone = barrelSpawnzones[world.barrels.indexOf(barrel)];
							spawnRoom(npc.getInstanceId(), zone);
						}
					}
					break;
				case "ZakenSpawn":
					Location position = getSpawnLocationForZaken();
					if(world.templateId == INSTANCEID_DAYDREAM)
					{
						world._zaken = (L2Attackable) createNewSpawn(ZAKEN, position, world.instanceId);
						world._zaken.addSkill(SkillTable.getInstance().getInfo(4218, 1));
						world._zaken.addSkill(SkillTable.getInstance().getInfo(4219, 1));
						world._zaken.addSkill(SkillTable.getInstance().getInfo(4220, 1));
						world._zaken.addSkill(SkillTable.getInstance().getInfo(4221, 1));
						world._zaken.setRunning();
						world._zaken.addDamageHate(player, 0, 999);
						world._zaken.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						world._target = player;
						startQuestTimer("RandomTarget", 3000, world._zaken, (L2PcInstance) getRandomTarget(world._zaken));
						startQuestTimer("Skill", 4500, world._zaken, (L2PcInstance) getRandomTarget(world._zaken));
					}
					else if(world.templateId == INSTANCEID83)
					{
						world._zaken = (L2Attackable) createNewSpawn(ZAKEN83, position, world.instanceId);
						world._zaken.addSkill(SkillTable.getInstance().getInfo(6689, 1));
						world._zaken.addSkill(SkillTable.getInstance().getInfo(6690, 1));
						world._zaken.addSkill(SkillTable.getInstance().getInfo(6691, 1));
						world._zaken.addSkill(SkillTable.getInstance().getInfo(6692, 1));
						world._zaken.setRunning();
						world._zaken.addDamageHate(player, 0, 999);
						world._zaken.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player);
						world._target = player;
						startQuestTimer("RandomTarget", 3000, world._zaken, (L2PcInstance) getRandomTarget(world._zaken));
						startQuestTimer("Skill", 4500, world._zaken, (L2PcInstance) getRandomTarget(world._zaken));
					}
					break;
				case "ChooseZakenRoom":
					world._zakenzone = zones[Rnd.get(zones.length)];
					spawnBarrels(world.instanceId);
					int[] barrels = zoneBarrels.get(world._zakenzone);
					world._zakenbarel1 = world.barrels.get(barrels[0] - 1);
					world._zakenbarel2 = world.barrels.get(barrels[1] - 1);
					world._zakenbarel3 = world.barrels.get(barrels[2] - 1);
					world._zakenbarel4 = world.barrels.get(barrels[3] - 1);
					break;
				case "RandomTarget":
					L2Character target = getRandomTarget(world._zaken);
					if(target != null)
					{
						world._zaken.reduceHate(world._target, 1000000);
						world._target = target;
						world._zaken.setTarget(world._target);
						world._zaken.addDamageHate(world._target, 0, 1000000);
						world._zaken.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._target);
					}
					startQuestTimer("RandomTarget", 6000, world._zaken, (L2PcInstance) world._target);
					break;
				case "Skill":
					L2Skill skill = chooseSkill(npc);
					if(world._target != null && skill != null)
					{
						if(Util.checkIfInRange(skill.getCastRange(), world._zaken, world._target, true))
						{
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
							npc.setTarget(world._target);
							npc.doCast(skill);
						}
						else
						{
							npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, world._target, null);
						}
					}
					startQuestTimer("Skill", 7000, world._zaken, (L2PcInstance) world._target);
					break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		int npcId = npc.getNpcId();
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof ZakenWorld)
		{
			ZakenWorld world = (ZakenWorld) tmpworld;
			if(npcId == ZAKEN || npcId == ZAKEN_NIGHTMARE || npcId == ZAKEN83)
			{
				InstanceManager.getInstance().getInstance(world.instanceId).setDuration(300000);
				world.playersInInstance.stream().filter(p -> p != null).forEach(this::savePlayerReenter);
			}
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		int npcId = npc.getNpcId();

		if(npcId == BARREL)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
			if(tmpworld instanceof ZakenWorld)
			{
				ZakenWorld world = (ZakenWorld) tmpworld;
				if(npc.getLeftHandItem() == 0 && npc.getRightHandItem() == 0)
				{
					world.barrels.stream().filter(barrel -> barrel.equals(npc)).forEach(barrel -> {
						if(barrel.equals(world._zakenbarel1) || barrel.equals(world._zakenbarel2) ||
							barrel.equals(world._zakenbarel3) || barrel.equals(world._zakenbarel4))
						{
							startQuestTimer("BlueBarrel1", 1000, npc, player);
						}
						else
						{
							startQuestTimer("RedBarrel1", 1000, npc, player);
						}
					});
				}
				else
				{
					return "<html><body>Эта бочка уже зажжена.</body></html>";
				}
			}
		}
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof ZakenWorld)
		{
			ZakenWorld world = (ZakenWorld) tmpworld;
			if(npc.getNpcId() == ZAKEN || npc.getNpcId() == ZAKEN83 || npc.getNpcId() == ZAKEN_NIGHTMARE)
			{
				if(world._target != null)
				{
					world._zaken.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, world._target);
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(tmpworld instanceof ZakenWorld)
			{
				ZakenWorld world = (ZakenWorld) tmpworld;
				if(zone.getId() == world._zakenzone)
				{
					world.playersInZakenzone.add((L2PcInstance) character);
				}
			}
		}
		return super.onEnterZone(character, zone);
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(character.getInstanceId());
			if(tmpworld instanceof ZakenWorld)
			{
				ZakenWorld world = (ZakenWorld) tmpworld;
				if(zone.getId() == world._zakenzone)
				{
					world.playersInZakenzone.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}

	private L2Skill chooseSkill(L2Npc npc)
	{
		int npcId = npc.getNpcId();
		if(npcId == ZAKEN || npcId == ZAKEN_NIGHTMARE)
		{
			int j = Rnd.get(100);

			if(j < 20)
			{
				return SkillTable.getInstance().getInfo(4218, 1); //Absorb HP MP
			}
			else if(j < 40)
			{
				return SkillTable.getInstance().getInfo(4219, 1); //Hold
			}
			else
			{
				return j < 70 ? SkillTable.getInstance().getInfo(4220, 1) : SkillTable.getInstance().getInfo(4221, 1);
			}
		}
		if(npcId == ZAKEN83)
		{
			int j = Rnd.get(100);

			if(j < 20)
			{
				return SkillTable.getInstance().getInfo(6689, 1); //Absorb HP MP
			}
			else if(j < 40)
			{
				return SkillTable.getInstance().getInfo(6690, 1); //Hold
			}
			else
			{
				return j < 70 ? SkillTable.getInstance().getInfo(6691, 1) : SkillTable.getInstance().getInfo(6692, 1);
			}
		}
		return null;
	}

	private void spawnRoom(int instanceId, int zoneId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if(tmpworld instanceof ZakenWorld)
		{
			ZakenWorld world = (ZakenWorld) tmpworld;
			int[] position;
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			L2ZoneForm zoneform = zone.getZone();
			for(int i = 1; i <= 5; i++)
			{
				position = zoneform.getRandomPosition();
				if(world.templateId == INSTANCEID_DAYDREAM || world.templateId == INSTANCEID_NIGHTMARE)
				{
					addSpawn(ZOMBIE, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
				else if(world.templateId == INSTANCEID83)
				{
					addSpawn(ZOMBIE83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
			}
			for(int i = 1; i <= 3; i++)
			{
				position = zoneform.getRandomPosition();
				if(world.templateId == INSTANCEID_DAYDREAM || world.templateId == INSTANCEID_NIGHTMARE)
				{
					addSpawn(DOLL_BLADER, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
				else if(world.templateId == INSTANCEID83)
				{
					addSpawn(DOLL_BLADER83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
			}
			for(int i = 1; i <= 2; i++)
			{
				position = zoneform.getRandomPosition();
				if(world.templateId == INSTANCEID_DAYDREAM || world.templateId == INSTANCEID_NIGHTMARE)
				{
					addSpawn(VALE_MASTER, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
				else if(world.templateId == INSTANCEID83)
				{
					addSpawn(VALE_MASTER83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
			}
			for(int i = 1; i <= 2; i++)
			{
				position = zoneform.getRandomPosition();
				if(world.templateId == INSTANCEID_DAYDREAM || world.templateId == INSTANCEID_NIGHTMARE)
				{
					addSpawn(ZOMBIE_CAPTAIN, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
				else if(world.templateId == INSTANCEID83)
				{
					addSpawn(ZOMBIE_CAPTAIN83, position[0], position[1], position[2], Rnd.get(65000), false, 0, false, instanceId);//.setIsRaidMinion(true);
				}
			}
		}
	}

	private L2Character getRandomTarget(L2Npc npc)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(npc.getInstanceId());
		if(tmpworld instanceof ZakenWorld)
		{
			ZakenWorld world = (ZakenWorld) tmpworld;
			List<L2Character> result = new ArrayList<>();
			for(L2Character obj : world.playersInZakenzone)
			{
				if(!GeoEngine.getInstance().canSeeTarget(obj, npc))
				{
					continue;
				}

				if(obj instanceof L2PcInstance || obj instanceof L2Summon || obj instanceof L2DecoyInstance)
				{
					if(Util.checkIfInRange(2000, npc, obj, true) && !obj.isDead())
					{
						if(obj instanceof L2PcInstance && ((L2PcInstance) obj).getAppearance().getInvisible())
						{
							continue;
						}

						result.add(obj);
					}
				}
			}
			if(!result.isEmpty())
			{
				Object[] characters = result.toArray();
				return (L2Character) characters[Rnd.get(characters.length)];
			}
		}
		return null;
	}

	private void setReuseCalendars()
	{
		if(nextCalendarReschedule > System.currentTimeMillis())
		{
			return;
		}

		Calendar currentTime = Calendar.getInstance();
		reuse_date_1 = Calendar.getInstance();

		reuse_date_1.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		reuse_date_1.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_1.set(Calendar.MINUTE, 30);
		reuse_date_1.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_1) > 0)
		{
			reuse_date_1.add(Calendar.DAY_OF_MONTH, 7);
		}

		reuse_date_2 = Calendar.getInstance();

		reuse_date_2.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		reuse_date_2.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_2.set(Calendar.MINUTE, 30);
		reuse_date_2.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_2) > 0)
		{
			reuse_date_2.add(Calendar.DAY_OF_MONTH, 7);
		}

		reuse_date_3 = Calendar.getInstance();

		reuse_date_3.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		reuse_date_3.set(Calendar.HOUR_OF_DAY, 6);
		reuse_date_3.set(Calendar.MINUTE, 30);
		reuse_date_3.set(Calendar.SECOND, 0);

		if(currentTime.compareTo(reuse_date_3) > 0)
		{
			reuse_date_3.add(Calendar.DAY_OF_MONTH, 7);
		}

		if(reuse_date_1.compareTo(reuse_date_2) < 0)
		{
			nextCalendarReschedule = reuse_date_1.getTimeInMillis();
		}
		else
		{
			nextCalendarReschedule = reuse_date_2.compareTo(reuse_date_3) < 0 ? reuse_date_2.getTimeInMillis() : reuse_date_3.getTimeInMillis();
		}
	}

	private void savePlayerReenter(L2PcInstance player)
	{
		setReuseCalendars();

		long nextTime;

		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(player.getInstanceId());

		// Дневной Закен обновляется каждую среду
		if(tmpworld.templateId == INSTANCEID_DAYDREAM)
		{
			nextTime = reuse_date_2.getTimeInMillis();
		}
		else
		{
			if(reuse_date_1.compareTo(reuse_date_2) < 0)
			{
				nextTime = reuse_date_1.getTimeInMillis();
			}
			else
			{
				nextTime = reuse_date_2.compareTo(reuse_date_3) < 0 ? reuse_date_2.getTimeInMillis() : reuse_date_3.getTimeInMillis();
			}
		}

		if(tmpworld.templateId == INSTANCEID_DAYDREAM)
		{
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID_DAYDREAM, nextTime);
		}
		else if(tmpworld.templateId == INSTANCEID_NIGHTMARE)
		{
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID_NIGHTMARE, nextTime);
		}
		else if(tmpworld.templateId == INSTANCEID83)
		{
			InstanceManager.getInstance().setInstanceTime(player.getObjectId(), INSTANCEID83, nextTime);
		}
	}

	protected void npcUpdate(L2Npc npc)
	{
		ZakenWorld world = InstanceManager.getInstance().getInstanceWorld(npc, ZakenWorld.class);
		if(world != null)
		{
			world.playersInInstance.stream().filter(pc -> pc instanceof L2PcInstance).forEach(pc -> pc.sendPacket(new NpcInfo(npc)));
		}
	}

	protected L2Npc createNewSpawn(int templateId, Location location, int instanceId)
	{
		L2Spawn tempSpawn = null;

		L2Npc npc;
		L2NpcTemplate template;

		try
		{
			template = NpcTable.getInstance().getTemplate(templateId);
			tempSpawn = new L2Spawn(template);

			tempSpawn.setLocation(location);
			tempSpawn.setHeading(location.getHeading());
			tempSpawn.setAmount(1);
			tempSpawn.setInstanceId(instanceId);
			tempSpawn.stopRespawn();
			SpawnTable.getInstance().addNewSpawn(tempSpawn);
		}
		catch(Throwable t)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while createNewSpawn() " + t.getMessage());
		}
		npc = tempSpawn.doSpawn();

		return npc;
	}

	private void spawnBarrels(int instanceId)
	{
		InstanceWorld tmpworld = InstanceManager.getInstance().getWorld(instanceId);
		if(tmpworld instanceof ZakenWorld)
		{
			ZakenWorld world = (ZakenWorld) tmpworld;
			world._barel1 = createNewSpawn(BARREL, new Location(53312, 220128, -3504, 0), world.instanceId); //4a
			world.barrels.add(0, world._barel1);
			world._barel2 = createNewSpawn(BARREL, new Location(54241, 221062, -3499, 49151), world.instanceId); //4a
			world.barrels.add(1, world._barel2);
			world._barel3 = createNewSpawn(BARREL, new Location(54333, 219104, -3504, 35048), world.instanceId);  //1,3a,4
			world.barrels.add(2, world._barel3);
			world._barel4 = createNewSpawn(BARREL, new Location(53312, 218079, -3504, 0), world.instanceId); //1a
			world.barrels.add(3, world._barel4);
			world._barel5 = createNewSpawn(BARREL, new Location(55260, 218171, -3504, 16384), world.instanceId); //1,2,3a
			world.barrels.add(4, world._barel5);
			world._barel6 = createNewSpawn(BARREL, new Location(55266, 220042, -3504, 49151), world.instanceId); //3a,4,5
			world.barrels.add(5, world._barel6);
			world._barel7 = createNewSpawn(BARREL, new Location(56288, 221056, -3504, 49152), world.instanceId); //5a
			world.barrels.add(6, world._barel7);
			world._barel8 = createNewSpawn(BARREL, new Location(57200, 220128, -3504, 32768), world.instanceId); //5a
			world.barrels.add(7, world._barel8);
			world._barel9 = createNewSpawn(BARREL, new Location(56192, 219104, -3504, 32768), world.instanceId); //2,3a,5
			world.barrels.add(8, world._barel9);
			world._barel10 = createNewSpawn(BARREL, new Location(57216, 218080, -3504, 32768), world.instanceId); //2a
			world.barrels.add(9, world._barel10);
			world._barel11 = createNewSpawn(BARREL, new Location(56286, 217156, -3504, 16384), world.instanceId); //2a
			world.barrels.add(10, world._barel11);
			world._barel12 = createNewSpawn(BARREL, new Location(54240, 217168, -3504, 16384), world.instanceId); //1a
			world.barrels.add(11, world._barel12);

			world._barel13 = createNewSpawn(BARREL, new Location(53332, 220128, -3227, 0), world.instanceId); //9a
			world.barrels.add(12, world._barel13);
			world._barel14 = createNewSpawn(BARREL, new Location(54240, 221040, -3232, 49152), world.instanceId); //9a
			world.barrels.add(13, world._barel14);
			world._barel15 = createNewSpawn(BARREL, new Location(54336, 219104, -3232, 0), world.instanceId); //6,8a,9
			world.barrels.add(14, world._barel15);
			world._barel16 = createNewSpawn(BARREL, new Location(53312, 218080, -3232, 0), world.instanceId); //6a
			world.barrels.add(15, world._barel16);
			world._barel17 = createNewSpawn(BARREL, new Location(55270, 218176, -3232, 16384), world.instanceId); //6,7,8a
			world.barrels.add(16, world._barel17);
			world._barel18 = createNewSpawn(BARREL, new Location(55264, 220032, -3232, 49152), world.instanceId); //8a,9,10
			world.barrels.add(17, world._barel18);
			world._barel19 = createNewSpawn(BARREL, new Location(56288, 221040, -3232, 49152), world.instanceId); //10a
			world.barrels.add(18, world._barel19);
			world._barel20 = createNewSpawn(BARREL, new Location(57200, 220128, -3232, 32768), world.instanceId); //10a
			world.barrels.add(19, world._barel20);
			world._barel21 = createNewSpawn(BARREL, new Location(56192, 219104, -3232, 32768), world.instanceId); //7,8a,10
			world.barrels.add(20, world._barel21);
			world._barel22 = createNewSpawn(BARREL, new Location(57213, 218080, -3229, 32768), world.instanceId); //7a
			world.barrels.add(21, world._barel22);
			world._barel23 = createNewSpawn(BARREL, new Location(56293, 217149, -3231, 16384), world.instanceId); //7a
			world.barrels.add(22, world._barel23);
			world._barel24 = createNewSpawn(BARREL, new Location(54240, 217152, -3232, 16384), world.instanceId); //6a
			world.barrels.add(23, world._barel24);

			world._barel25 = createNewSpawn(BARREL, new Location(53328, 220128, -2960, 0), world.instanceId); //14a
			world.barrels.add(24, world._barel25);
			world._barel26 = createNewSpawn(BARREL, new Location(54240, 221040, -2960, 49152), world.instanceId); //14a
			world.barrels.add(25, world._barel26);
			world._barel27 = createNewSpawn(BARREL, new Location(54331, 219104, -2960, 0), world.instanceId); //11,13a,14
			world.barrels.add(26, world._barel27);
			world._barel28 = createNewSpawn(BARREL, new Location(53328, 218080, -2956, 0), world.instanceId); //11a
			world.barrels.add(27, world._barel28);
			world._barel29 = createNewSpawn(BARREL, new Location(55264, 218165, -2960, 16384), world.instanceId); //11,12,13a
			world.barrels.add(28, world._barel29);
			world._barel30 = createNewSpawn(BARREL, new Location(55264, 220016, -2960, 49152), world.instanceId); //13a,14,15
			world.barrels.add(29, world._barel30);
			world._barel31 = createNewSpawn(BARREL, new Location(56288, 221024, -2960, 49152), world.instanceId); //15a
			world.barrels.add(30, world._barel31);
			world._barel32 = createNewSpawn(BARREL, new Location(57200, 220128, -2960, 32768), world.instanceId); //15a
			world.barrels.add(31, world._barel32);
			world._barel33 = createNewSpawn(BARREL, new Location(56192, 219104, -2960, 32768), world.instanceId); //12,13a,15
			world.barrels.add(32, world._barel33);
			world._barel34 = createNewSpawn(BARREL, new Location(57200, 218080, -2960, 32768), world.instanceId); //12a
			world.barrels.add(33, world._barel34);
			world._barel35 = createNewSpawn(BARREL, new Location(56288, 217152, -2960, 16384), world.instanceId); //12a
			world.barrels.add(34, world._barel35);
			world._barel36 = createNewSpawn(BARREL, new Location(54240, 217152, -2960, 16384), world.instanceId); //11a
			world.barrels.add(35, world._barel36);
		}
	}

	/***
	 * @return случайную позицию (одну из 16) для Закена в инстансе
	 */
	private Location getSpawnLocationForZaken()
	{
		int i0 = Rnd.get(15);
		int x = 0;
		int y = 0;
		int z = 0;

		if(i0 == 0)
		{
			x = 54237;
			y = 218135;
			z = -3496;
		}
		else if(i0 == 1)
		{
			x = 56288;
			y = 218087;
			z = -3496;
		}
		else if(i0 == 2)
		{
			x = 55273;
			y = 219140;
			z = -3496;
		}
		else if(i0 == 3)
		{
			x = 54232;
			y = 220184;
			z = -3496;
		}
		else if(i0 == 4)
		{
			x = 56259;
			y = 220168;
			z = -3496;
		}
		else if(i0 == 5)
		{
			x = 54250;
			y = 218122;
			z = -3224;
		}
		else if(i0 == 6)
		{
			x = 56308;
			y = 218125;
			z = -3224;
		}
		else if(i0 == 7)
		{
			x = 55243;
			y = 219064;
			z = -3224;
		}
		else if(i0 == 8)
		{
			x = 54255;
			y = 220156;
			z = -3224;
		}
		else if(i0 == 9)
		{
			x = 56255;
			y = 220161;
			z = -3224;
		}
		else if(i0 == 10)
		{
			x = 54261;
			y = 218095;
			z = -2952;
		}
		else if(i0 == 11)
		{
			x = 56258;
			y = 218086;
			z = -2952;
		}
		else if(i0 == 12)
		{
			x = 55258;
			y = 219080;
			z = -2952;
		}
		else if(i0 == 13)
		{
			x = 54292;
			y = 220096;
			z = -2952;
		}
		else if(i0 == 14)
		{
			x = 56258;
			y = 220135;
			z = -2952;
		}
		return new Location(x, y, z);
	}

	private class ZakenWorld extends InstanceWorld
	{
		public List<L2PcInstance> playersInInstance = new FastList<>();
		public List<L2PcInstance> playersInZakenzone = new FastList<>();
		public List<L2Npc> barrels = new FastList<>();
		int _zakenzone;
		int _bluecandles;
		L2Character _target;
		L2Attackable _zaken;
		L2Npc _barel1;
		L2Npc _barel2;
		L2Npc _barel3;
		L2Npc _barel4;
		L2Npc _barel5;
		L2Npc _barel6;
		L2Npc _barel7;
		L2Npc _barel8;
		L2Npc _barel9;
		L2Npc _barel10;
		L2Npc _barel11;
		L2Npc _barel12;
		L2Npc _barel13;
		L2Npc _barel14;
		L2Npc _barel15;
		L2Npc _barel16;
		L2Npc _barel17;
		L2Npc _barel18;
		L2Npc _barel19;
		L2Npc _barel20;
		L2Npc _barel21;
		L2Npc _barel22;
		L2Npc _barel23;
		L2Npc _barel24;
		L2Npc _barel25;
		L2Npc _barel26;
		L2Npc _barel27;
		L2Npc _barel28;
		L2Npc _barel29;
		L2Npc _barel30;
		L2Npc _barel31;
		L2Npc _barel32;
		L2Npc _barel33;
		L2Npc _barel34;
		L2Npc _barel35;
		L2Npc _barel36;
		L2Npc _zakenbarel1;
		L2Npc _zakenbarel2;
		L2Npc _zakenbarel3;
		L2Npc _zakenbarel4;

		public ZakenWorld(Long time)
		{
			InstanceManager.getInstance();
		}
	}
}