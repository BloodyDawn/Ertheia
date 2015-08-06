package dwo.scripts.instances;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowUsm;
import dwo.gameserver.util.Util;
import javolution.util.FastList;

import java.util.Calendar;
import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class RB_Octavis extends Quest
{
    private static final Location ENTRANCE = new Location(210651, 119052, -9996);
	private static final Location LAIR_ENTRANCE = new Location(208404, 120572, -10014);
	private static final Location OCTAVIS_SPAWN = new Location(207069, 120580, -10008);
	private static final Location LAIR_CENTER = new Location(207190, 120574, -10009);
	private static final int LAIR_ZONE = 400051;
	private static final int INSTANCE_ID_LIGHT = InstanceZoneId.OCTAVIS_WARZONE.getId();
	private static final int INSTANCE_ID_HARD = InstanceZoneId.OCTAVIS_WARZONE_EPIC_BATTLE.getId();
	// NPCs
	private static final int VOLCANO_ZONE = 19161;
	private static final int OCTAVIS_POWER = 18984;
	private static final int OCTAVIS_LIGHT_FIRST = 29191;
	private static final int OCTAVIS_LIGHT_BEAST = 29192;
	private static final int OCTAVIS_LIGHT_SECOND = 29193;
	private static final int OCTAVIS_LIGHT_THIRD = 29194;
	private static final int OCTAVIS_HARD_FIRST = 29209;
	private static final int OCTAVIS_HARD_BEAST = 29210;
	private static final int OCTAVIS_HARD_SECOND = 29211;
	private static final int OCTAVIS_HARD_THIRD = 29212;
	private static final int OCTAVIS_NPC = 32949;
	private static final int OCTAVIS_GLADIATOR = 22928;
	private static final int ARENA_BEAST = 22929;
	private static final int OCTAVIS_SCIENTIST = 22930;
	private static final int[] BEAST_DOORS = {
		26210101, 26210102, 26210103, 26210104, 26210105, 26210106,
	};
	private static final int[][] OCTAVIS_SCIENTIST_SPAWN = {
		{207820, 120312, -10008, 28144}, {207450, 119936, -10008, 19504}, {207817, 120832, -10008, 36776},
		{206542, 120306, -10008, 4408}, {206923, 119936, -10008, 12008}, {207458, 121218, -10008, 44440},
		{206923, 121216, -10008, 53504}, {206620, 120568, -10008, 800}, {207194, 121082, -10008, 49000},
		{207197, 120029, -10008, 17080}, {207776, 120577, -10008, 33016}, {206541, 120848, -10008, 60320},
	};
	private static final int[][] OCTAVIS_GLADIATOR_SPAWN = {
		{206519, 118937, -9976, 12416}, {207865, 118937, -9976, 19232}, {208829, 119896, -9976, 28264},
		{208825, 121260, -9976, 38080}, {207875, 122209, -9976, 44144}, {206507, 122208, -9976, 54680},
	};
	private static final int[][] ARENA_BEAST_SPAWN = {
		{206692, 119375, -10008, 0}, {208418, 120065, -10008, 0}, {207700, 121810, -10008, 0},
	};
	private static final int[][] OUTROOM_LOCATIONS = {
		{206849, 119744, -10014}, {207524, 119765, -10014}, {208002, 120238, -10016}, {207995, 120911, -10016},
		{207524, 121377, -10014}, {206861, 121375, -10014},
	};
	private static RB_Octavis _octavisInstance;

	public RB_Octavis()
	{

		addEnterZoneId(LAIR_ZONE);
		addExitZoneId(LAIR_ZONE);
	}

	public static void main(String[] args)
	{
		_octavisInstance = new RB_Octavis();
	}

	public static RB_Octavis getInstance()
	{
		return _octavisInstance;
	}

	private long getReuseTime(boolean isHardInstance)
	{
		// Откаты по времени в среду и субботу в 6:30
		Calendar _instanceTimeWednesday = Calendar.getInstance();
		Calendar _instanceTimeSaturday = Calendar.getInstance();

		Calendar currentTime = Calendar.getInstance();

		_instanceTimeWednesday.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		_instanceTimeWednesday.set(Calendar.HOUR_OF_DAY, 6);
		_instanceTimeWednesday.set(Calendar.MINUTE, 30);
		_instanceTimeWednesday.set(Calendar.SECOND, 0);

		if(_instanceTimeWednesday.compareTo(currentTime) < 0)
		{
			_instanceTimeWednesday.add(Calendar.DAY_OF_MONTH, 7);
		}

		_instanceTimeSaturday.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		_instanceTimeSaturday.set(Calendar.HOUR_OF_DAY, 6);
		_instanceTimeSaturday.set(Calendar.MINUTE, 30);
		_instanceTimeSaturday.set(Calendar.SECOND, 0);

		if(_instanceTimeSaturday.compareTo(currentTime) < 0)
		{
			_instanceTimeSaturday.add(Calendar.DAY_OF_MONTH, 7);
		}

		if(isHardInstance)
		{
			return _instanceTimeWednesday.getTimeInMillis();
		}
		else
		{
			return _instanceTimeWednesday.compareTo(_instanceTimeSaturday) < 0 ? _instanceTimeWednesday.getTimeInMillis() : _instanceTimeSaturday.getTimeInMillis();
		}
	}

	protected int enterInstance(L2PcInstance player, String template, boolean isHardInstance)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof OctavisWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}

			if(!((OctavisWorld) world).playersInside.contains(player))
			{
				((OctavisWorld) world).playersInside.add(player);
			}

			player.teleToInstance(LAIR_ENTRANCE, world.instanceId);
			return world.instanceId;
		}
		else
		{
			world = new OctavisWorld();
			((OctavisWorld) world).isHardInstance = isHardInstance;
			int instanceTemplateId = ((OctavisWorld) world).isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT;
			if(!checkConditions(player, instanceTemplateId))
			{
				return 0;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(template);

			world.instanceId = instanceId;
			world.templateId = instanceTemplateId;
			world.status = 0;

			InstanceManager.getInstance().addWorld(world);

			if(player.isGM() && player.getParty() == null)
			{
				player.teleToInstance(ENTRANCE, instanceId);
				world.allowed.add(player.getObjectId());
				((OctavisWorld) world).playersInside.add(player);
				return instanceId;
			}

			if(player.getParty() != null)
			{
				if(player.getParty().getCommandChannel() == null)
				{
					for(L2PcInstance partyMember : player.getParty().getMembers())
					{
						partyMember.teleToInstance(ENTRANCE, instanceId);
						world.allowed.add(partyMember.getObjectId());
						((OctavisWorld) world).playersInside.add(partyMember);
					}
					return instanceId;
				}
				else
				{
					for(L2PcInstance channelMember : player.getParty().getCommandChannel().getMembers())
					{
						channelMember.teleToInstance(ENTRANCE, instanceId);
						world.allowed.add(channelMember.getObjectId());
						((OctavisWorld) world).playersInside.add(channelMember);
					}
					return instanceId;
				}
			}
			return 0;
		}
	}

	public boolean enterInstance(L2PcInstance player, boolean isExtreme)
	{
		boolean result = enterInstance(player, "RB_Octavis.xml", isExtreme) != 0;
		if(result)
		{
			OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(player, OctavisWorld.class);

			if(world != null)
			{
               // world.octavis.setIsOctavisRaid(true);

                ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					for(L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
					{
						door.openMe();
					}
				}, 10000);
			}
		}
		return result;
	}

	private boolean checkConditions(L2PcInstance player, int instanceTemplateId)
	{
		L2Party party = player.getParty();

		/* Для дебага */
		if(player.isGM())
		{
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
				return false;
			}
			return true;
		}

		if(player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}

		if(!party.isInCommandChannel())
		{
			party.broadcastPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
			return false;
		}

		int minPlayers = instanceTemplateId != INSTANCE_ID_HARD ? Config.MIN_OCTAVIS_PLAYERS : Config.MIN_OCTAVIS_HARD_PLAYERS;
		int maxPlayers = instanceTemplateId != INSTANCE_ID_HARD ? Config.MAX_OCTAVIS_PLAYERS : Config.MAX_OCTAVIS_HARD_PLAYERS;
		int minLevel = instanceTemplateId != INSTANCE_ID_HARD ? Config.MIN_LEVEL_OCTAVIS_PLAYERS : Config.MIN_LEVEL_OCTAVIS_HARD_PLAYERS;

		L2CommandChannel channel = player.getParty().getCommandChannel();
		if(!channel.getLeader().equals(player))
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return false;
		}
		if(channel.getMemberCount() < minPlayers || channel.getMemberCount() > maxPlayers)
		{
			player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return false;
		}

		for(L2PcInstance member : channel.getMembers())
		{
			/* В инст пускает только перерожденных чаров и минимальный лвл с которого пускает 85. */
			if(member == null || member.getLevel() < minLevel || !member.isAwakened())
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return false;
			}
			if(!Util.checkIfInRange(1000, player, member, true))
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
				return false;
			}
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return false;
			}
		}
		return true;
	}

	public void nextSpawn(final OctavisWorld world)
	{
		switch(world.status)
		{
			case 1:
				if(world.status == 1)
				{
					world.status = 2;

					for(L2PcInstance player : world.playersInLairZone)
					{
						player.showQuestMovie(ExStartScenePlayer.SCENE_OCTABIS_PHASECH_A);
					}

					if(world.octavisBeast != null)
					{
						world.octavisBeast.getLocationController().delete();
					}

					ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
					{
						@Override
						public void run()
						{
							Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
							int npcId = world.isHardInstance ? OCTAVIS_HARD_SECOND : OCTAVIS_LIGHT_SECOND;
							world.octavis = addSpawn(npcId, LAIR_CENTER.getX(), LAIR_CENTER.getY(), LAIR_CENTER.getZ(), 0, false, 0, false, world.instanceId);

							for(int doorId : BEAST_DOORS)
							{
								L2DoorInstance door = instance.getDoor(doorId);
								door.openMe();
							}

							for(int[] loc : OCTAVIS_GLADIATOR_SPAWN)
							{
								L2Npc gladiator = addSpawn(OCTAVIS_GLADIATOR, loc[0], loc[1], loc[2], loc[3], false, 0, false, world.instanceId);
								gladiator.getSpawn().setRespawnDelay(120);
								gladiator.setIsNoRndWalk(true);

								int[] selectedLoc = null;
								double selectedDistance = 0;
								Location currentLoc = gladiator.getLoc();
								for(int[] outloc : OUTROOM_LOCATIONS)
								{
									if(selectedLoc == null || selectedDistance > Util.calculateDistance(currentLoc.getX(), currentLoc.getY(), outloc[0], outloc[1]))
									{
										selectedLoc = outloc;
										selectedDistance = Util.calculateDistance(currentLoc.getX(), currentLoc.getY(), selectedLoc[0], selectedLoc[1]);
									}
								}

								gladiator.setRunning();
                                if (selectedLoc != null) {
                                    gladiator.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(selectedLoc[0], selectedLoc[1], selectedLoc[2]));
                                }
                            }

							ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
							{
								@Override
								public void run()
								{
									if(world.octavis != null && (world.octavis.getNpcId() == OCTAVIS_LIGHT_SECOND || world.octavis.getNpcId() == OCTAVIS_HARD_SECOND))
									{
										// Спауним по 7 зверей за раз. Каждый раз звери появляются у разных дверей (всего три двери)
										int offset = world.arenaBeastSpawnNumber % 3;
										for(int i = offset; i < offset + 7; ++i)
										{
											int[] loc = ARENA_BEAST_SPAWN[offset];
											L2Npc beast = addSpawn(ARENA_BEAST, loc[offset], loc[1], loc[2], loc[3], true, 0, false, world.instanceId);
											beast.setRunning();
											beast.setIsNoRndWalk(true);
											beast.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, world.octavis.getLoc());
										}
										++world.arenaBeastSpawnNumber;

										ThreadPoolManager.getInstance().scheduleGeneral(this, 180000);
									}
								}
							}, 1000);
						}
					}, 10000);
				}
				break;
			case 2:
				if(world.status == 2)
				{
					world.status = 3;

					// Деспауним мобов из предыдущего этапа
					Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
					instance.getNpcs().stream().filter(npc -> npc.getNpcId() == OCTAVIS_GLADIATOR || npc.getNpcId() == ARENA_BEAST).forEach(npc -> {
						npc.getSpawn().stopRespawn();
						npc.getLocationController().delete();
					});

					for(L2PcInstance player : world.playersInLairZone)
					{
						player.showQuestMovie(ExStartScenePlayer.SCENE_OCTABIS_PHASECH_B);
					}

					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						int npcId = world.isHardInstance ? OCTAVIS_HARD_THIRD : OCTAVIS_LIGHT_THIRD;

						world.octavis = addSpawn(npcId, LAIR_CENTER.getX(), LAIR_CENTER.getY(), LAIR_CENTER.getZ(), 0, false, 0, false, world.instanceId);

						for(int[] loc : OCTAVIS_SCIENTIST_SPAWN)
						{
							L2Npc scientist = addSpawn(OCTAVIS_SCIENTIST, loc[0], loc[1], loc[2], loc[3], false, 0, false, world.instanceId);
							scientist.getSpawn().setRespawnDelay(120);
						}
						++world.arenaBeastSpawnNumber;
					}, 15000);
				}
				break;
			case 3:
				if(world.status == 3)
				{
					world.status = 4;

					// Деспауним мобов из предыдущего этапа
					Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
					instance.getNpcs().stream().filter(npc -> npc.getNpcId() == OCTAVIS_SCIENTIST).forEach(npc -> {
						npc.getSpawn().stopRespawn();
						npc.getLocationController().delete();
					});

					for(L2PcInstance player : world.playersInLairZone)
					{
						player.showQuestMovie(ExStartScenePlayer.SCENE_OCTABIS_ENDING);
					}

					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						for(L2PcInstance player : world.playersInLairZone)
						{
							player.sendPacket(new ExShowUsm(ExShowUsm.Q005));
						}
					}, 50000);

					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						int npcId = OCTAVIS_NPC;
						world.octavis = addSpawn(npcId, LAIR_CENTER.getX(), LAIR_CENTER.getY(), LAIR_CENTER.getZ(), 0, false, 0, false, world.instanceId);
						world.octavis.doDie(null);
						long instanceTime = getReuseTime(world.isHardInstance);
						for(Integer player : instance.getPlayers())
						{
							InstanceManager.getInstance().setInstanceTime(player, world.isHardInstance ? INSTANCE_ID_HARD : INSTANCE_ID_LIGHT, instanceTime);
							if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
							{
								WorldStatisticsManager.getInstance().updateStat(player, CategoryType.EPIC_BOSS_KILLS, world.isHardInstance ? OCTAVIS_HARD_THIRD : OCTAVIS_LIGHT_THIRD, 1);
							}
						}

						InstanceManager.getInstance().getInstance(world.instanceId).setDuration(5 * 60 * 1000);
					}, 70000);
				}
				break;
		}
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		synchronized(this)
		{
			if(character instanceof L2PcInstance)
			{
				OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(character, OctavisWorld.class);

				if(world != null)
				{
					if(zone.getId() == LAIR_ZONE && world.status < 4)
					{
						if(!world.playersInLairZone.contains(character))
						{
							world.playersInLairZone.add((L2PcInstance) character);

							if(world.status == 0)
							{
								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									for(L2PcInstance player : world.playersInLairZone)
									{
										player.showQuestMovie(ExStartScenePlayer.SCENE_OCTABIS_OPENING);
									}
								}, 15000);

								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									for(L2DoorInstance door : InstanceManager.getInstance().getInstance(world.instanceId).getDoors())
									{
										door.closeMe();
									}

									int octavisId = world.isHardInstance ? OCTAVIS_HARD_FIRST : OCTAVIS_LIGHT_FIRST;
									int beastId = world.isHardInstance ? OCTAVIS_LIGHT_BEAST : OCTAVIS_HARD_BEAST;

									world.octavisBeast = addSpawn(beastId, 207244, 120579, -10008, 0, false, 0, false, world.instanceId);
									world.octavis = addSpawn(octavisId, OCTAVIS_SPAWN.getX(), OCTAVIS_SPAWN.getY(), OCTAVIS_SPAWN.getZ(), 0, false, 0, false, world.instanceId);

									for(byte i = 0; i < 4; ++i)
									{
										world.volcanos.add(addSpawn(VOLCANO_ZONE, OCTAVIS_SPAWN.getX(), OCTAVIS_SPAWN.getY(), OCTAVIS_SPAWN.getZ(), 0, false, 0, false, world.instanceId));
									}
									world.octavisPower = addSpawn(OCTAVIS_POWER, OCTAVIS_SPAWN.getX(), OCTAVIS_SPAWN.getY(), OCTAVIS_SPAWN.getZ(), 0, false, 0, false, world.instanceId);
								}, 42000);

								world.status = 1;
							}
						}
					}
				}
			}
			return super.onEnterZone(character, zone);
		}
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance)
		{
			OctavisWorld world = InstanceManager.getInstance().getInstanceWorld(character, OctavisWorld.class);

			if(world != null)
			{
				if(zone.getId() == LAIR_ZONE)
				{
					world.playersInLairZone.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}

	public class OctavisWorld extends InstanceWorld
	{
		public boolean isHardInstance;
		public List<L2PcInstance> playersInside = new FastList<>();
		public List<L2PcInstance> playersInLairZone = new FastList<>();
		public L2Npc octavis;
		public L2Npc octavisBeast;
		public List<L2Npc> volcanos = new FastList<>();
		public L2Npc octavisPower;
		public int arenaBeastSpawnNumber;
	}
}
