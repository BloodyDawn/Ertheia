package dwo.scripts.instances;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/*
 * TODO
 * Скилл какого-то РБ:
 * <spawn npcId="23080" x="41879" y="-175117" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.50.18222 -->
 * <spawn npcId="19115" x="41879" y="-175117" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.50.24799 -->
 * <spawn npcId="19115" x="42292" y="-175029" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.55.36448 -->
 * <spawn npcId="23080" x="42432" y="-176296" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.55.38655 -->
 * <spawn npcId="23080" x="41603" y="-175070" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.55.42669 -->
 * <spawn npcId="19115" x="41603" y="-175070" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.55.50043 -->
 * <spawn npcId="23080" x="41766" y="-176361" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.55.53657 -->
 * <spawn npcId="19115" x="42369" y="-176320" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.56.10855 -->
 * <spawn npcId="19115" x="42494" y="-176455" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.56.11073 -->
 * <spawn npcId="19115" x="42510" y="-176294" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.56.12726 -->
 * <spawn npcId="19115" x="41904" y="-176227" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.56.14937 -->
 * <spawn npcId="19115" x="41898" y="-176322" z="-7952" heading="0" respawn="0" /><!-- Ядовитое Облако --> <!-- 08.07.12_17.56.17121 -->
 *
 * Бешенство без названия:
 * <spawn npcId="23083" x="41782" y="-175306" z="-7952" heading="0" respawn="0" /><!-- Бешенство без названия --> <!-- 08.07.12_17.50.41570 -->
 *
 * Миньоны Сэр Тьмы Ресинда:
 * <spawn npcId="25842" x="43199" y="-175492" z="-4992" heading="0" respawn="0" /><!-- Черный Зверь (Рейдовый Боец) --> <!-- 08.07.12_17.52.31810 -->
 * <spawn npcId="25842" x="42699" y="-175492" z="-7952" heading="0" respawn="0" /><!-- Черный Зверь (Рейдовый Боец) --> <!-- 08.07.12_17.52.31810 -->
 *
 * <spawn npcId="25842" x="43199" y="-175492" z="-4992" heading="0" respawn="0" /><!-- Черный Зверь (Рейдовый Боец) --> <!-- 08.07.12_17.53.05639 -->
 * <spawn npcId="25842" x="42699" y="-175492" z="-7952" heading="0" respawn="0" /><!-- Черный Зверь (Рейдовый Боец) --> <!-- 08.07.12_17.53.05639 -->
 */

/**
 * Fortuna instance.
 *
 * @author Yorie
 */
public class Fortuna extends Quest
{
	private static final String qn = "Fortuna";
	private static final int SELFINA = 33588;
	private static final Location ENTRANCE = new Location(42108, -172709, -7955);
	private static final Location LAIR_ENTRANCE = new Location(42096, -174439, -7950);
	private static final Location SELFINA_SPAWN = new Location(42101, -175276, -7944, 16200);
	private static final int FOR_ANCIENT_HEROES = 8888011;
	private static final int DROP_SPHERE_AND_SHE_WILL_NOT_SHINE = 8888012;
	private static final int THOSE_WHO_COMES_FOR_CURSED_BODIES = 8888003;
	/**
	 * Базовая задержка перед началом спауна мобов.
	 */
	private static final int BASE_CHALLENGE_DELAY = 120000;
	private static final Map<String, Integer> _scheduledSpawns = new FastMap<>();

	static
	{
		_scheduledSpawns.put("monsters_1", 2000);
		_scheduledSpawns.put("monsters_2", 7000);
		_scheduledSpawns.put("monsters_3", 12000);
		_scheduledSpawns.put("monsters_4", 15000);
		_scheduledSpawns.put("monsters_5", 20000);
		_scheduledSpawns.put("monsters_6", 25000);
		_scheduledSpawns.put("monsters_7", 30000);
		_scheduledSpawns.put("monsters_8", 37000);
		_scheduledSpawns.put("monsters_9", 40000);
		_scheduledSpawns.put("monsters_10", 45000);
		_scheduledSpawns.put("monsters_11", 48000);
		_scheduledSpawns.put("yui", 108000);
		_scheduledSpawns.put("monsters_13", 118000);
		_scheduledSpawns.put("monsters_14", 128000);
		_scheduledSpawns.put("monsters_15", 137000);
		_scheduledSpawns.put("monsters_16", 119200);
		_scheduledSpawns.put("monsters_17", 148300);
		_scheduledSpawns.put("kinen", 200000);
		_scheduledSpawns.put("kinen_transformed", 230000);
		_scheduledSpawns.put("monsters_18", 340000);
		_scheduledSpawns.put("monsters_19", 343000);
		_scheduledSpawns.put("monsters_20", 359000);
		_scheduledSpawns.put("monsters_21", 343000);
		_scheduledSpawns.put("monsters_22", 346000);
		_scheduledSpawns.put("monsters_23", 348000);
		_scheduledSpawns.put("monsters_24", 350000);
		_scheduledSpawns.put("monsters_25", 356000);
		_scheduledSpawns.put("monsters_26", 378000);
		_scheduledSpawns.put("monsters_27", 408000);
		_scheduledSpawns.put("konyar", 409000);
		_scheduledSpawns.put("konyar_tranformed", 449000);
		_scheduledSpawns.put("konyar_tranformed_2", 487000);
		_scheduledSpawns.put("monsters_28", 547000);
		_scheduledSpawns.put("monsters_29", 553000);
		_scheduledSpawns.put("monsters_30", 568000);
		_scheduledSpawns.put("monsters_31", 578000);
		_scheduledSpawns.put("monsters_32", 588000);
		_scheduledSpawns.put("rakiello", 628000);
		_scheduledSpawns.put("monsters_33", 638000);
		_scheduledSpawns.put("monsters_34", 643000);
		_scheduledSpawns.put("monsters_35", 665000);
		_scheduledSpawns.put("monsters_36", 668000);
		_scheduledSpawns.put("monsters_37", 672000);
		_scheduledSpawns.put("monsters_38", 674000);
		_scheduledSpawns.put("monsters_39", 690000);
		_scheduledSpawns.put("monsters_40", 740000);
		_scheduledSpawns.put("monsters_41", 750000);
		_scheduledSpawns.put("monsters_42", 765000);
		_scheduledSpawns.put("monsters_43", 767000);
		_scheduledSpawns.put("monsters_44", 770000);
		_scheduledSpawns.put("monsters_45", 777000);
		_scheduledSpawns.put("monsters_46", 781000);
		_scheduledSpawns.put("monsters_47", 791000);
		_scheduledSpawns.put("monsters_48", 796000);
		_scheduledSpawns.put("monsters_49", 803000);
		_scheduledSpawns.put("monsters_50", 828000);
		_scheduledSpawns.put("monsters_51", 858000);
		_scheduledSpawns.put("monsters_52", 883000);
		_scheduledSpawns.put("monsters_53", 983000);
		_scheduledSpawns.put("monsters_54", 993000);
		_scheduledSpawns.put("monsters_55", 998000);
		_scheduledSpawns.put("monsters_56", 1023000);
		_scheduledSpawns.put("monsters_57", 1034000);
		_scheduledSpawns.put("monsters_58", 1023000);
		_scheduledSpawns.put("monsters_59", 1026000);
		_scheduledSpawns.put("monsters_60", 1031000);
		_scheduledSpawns.put("monsters_61", 1034000);
		_scheduledSpawns.put("monsters_62", 1038000);
		_scheduledSpawns.put("monsters_63", 1037000);
		_scheduledSpawns.put("monsters_64", 1077000);
		_scheduledSpawns.put("monsters_65", 1082000);
		_scheduledSpawns.put("thron", 1097000);
		_scheduledSpawns.put("monsters_66", 1100000);
		_scheduledSpawns.put("monsters_67", 1107000);
		_scheduledSpawns.put("monsters_68", 1112000);
	}

	private static final int ENTRANCE_DOOR = 21120001;
	private static Fortuna _fortunaInstance;

	public static void main(String[] args)
	{
		_fortunaInstance = new Fortuna();
	}

	public static Fortuna getInstance()
	{
		return _fortunaInstance;
	}

	private long getReuseTime()
	{
		// Откат ежедневно
		Calendar instanceTime = Calendar.getInstance();

		instanceTime.add(Calendar.DAY_OF_MONTH, 1);
		instanceTime.set(Calendar.HOUR_OF_DAY, 6);
		instanceTime.set(Calendar.MINUTE, 30);
		instanceTime.set(Calendar.SECOND, 0);

		return instanceTime.getTimeInMillis();
	}

	public EnterInstanceResult reEnterInstance(L2PcInstance player)
	{
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof FortunaWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return EnterInstanceResult.ANOTHER_INSTANCE;
			}

			if(!((FortunaWorld) world).playersInside.contains(player))
			{
				((FortunaWorld) world).playersInside.add(player);
				world.allowed.add(player.getObjectId());
			}

			player.teleToInstance(LAIR_ENTRANCE, world.instanceId);
			return EnterInstanceResult.OK;
		}
		return EnterInstanceResult.CANNOT_REENTER;
	}

	protected EnterInstanceResult enterInstance(L2PcInstance player, String template)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		if(world != null)
		{
			if(!(world instanceof FortunaWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return EnterInstanceResult.ANOTHER_INSTANCE;
			}
		}

		world = new FortunaWorld();

		int instanceTemplateId = InstanceZoneId.FORTUNA.getId();
		EnterInstanceResult reason = checkConditions(player, instanceTemplateId);
		if(reason != EnterInstanceResult.OK)
		{
			return reason;
		}

		instanceId = InstanceManager.getInstance().createDynamicInstance(template);

		world.instanceId = instanceId;
		world.templateId = instanceTemplateId;
		world.status = 0;

		InstanceManager.getInstance().addWorld(world);
		init((FortunaWorld) world);

		if(player.isGM() && player.getParty() == null)
		{
			player.teleToInstance(ENTRANCE, instanceId);
			world.allowed.add(player.getObjectId());
			((FortunaWorld) world).playersInside.add(player);
			return EnterInstanceResult.OK;
		}

		if(player.getParty() != null)
		{
			for(L2PcInstance member : player.getParty().getMembers())
			{
				member.teleToInstance(ENTRANCE, instanceId);
				world.allowed.add(member.getObjectId());
				((FortunaWorld) world).playersInside.add(member);
			}
			return EnterInstanceResult.OK;
		}
		return EnterInstanceResult.NO_PARTY;
	}

	public EnterInstanceResult enterInstance(L2PcInstance player)
	{
		return enterInstance(player, "Fortuna.xml");
	}

	private void init(FortunaWorld world)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
			if(instance != null)
			{
				L2DoorInstance door = instance.getDoor(ENTRANCE_DOOR);
				door.openMe();

				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					door.closeMe();

					L2Npc selfina = addSpawn(SELFINA, SELFINA_SPAWN.getX(), SELFINA_SPAWN.getY(), SELFINA_SPAWN.getZ(), SELFINA_SPAWN.getHeading(), false, 10000, false, world.instanceId);
					selfina.broadcastPacket(new NS(selfina.getObjectId(), ChatType.NPC_ALL, selfina.getNpcId(), FOR_ANCIENT_HEROES));

					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						selfina.broadcastPacket(new NS(selfina.getObjectId(), ChatType.NPC_ALL, selfina.getNpcId(), DROP_SPHERE_AND_SHE_WILL_NOT_SHINE));
						spawnLightnings(world);

						ThreadPoolManager.getInstance().scheduleGeneral(() -> startChallenge(world), 2000);
					}, 5000);
				}, 30000);
			}
		}, 10000);
	}

	private void spawnLightnings(FortunaWorld world)
	{
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
		if(instance != null)
		{
			for(L2Spawn spawn : instance.getGroupSpawn("lightning_sphere_1"))
			{
				L2Npc sphere = spawn.spawnOne(false);
				sphere.setIsNoRndWalk(true);
			}
		}
	}

	private void startChallenge(FortunaWorld world)
	{
		int delay = !world.playersInside.isEmpty() && world.playersInside.get(0).isGM() ? 10000 : 110000;
		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

			if(instance == null)
			{
				return;
			}

			for(Map.Entry<String, Integer> stringIntegerEntry : _scheduledSpawns.entrySet())
			{
				world.spawnTasks.add(ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					List<L2Spawn> spawns = instance.getGroupSpawn(stringIntegerEntry.getKey());

					if(spawns == null)
					{
						_log.warn("Fortuna instance: cannot spawn monsters from group spawn [" + stringIntegerEntry.getKey() + ']');
						return;
					}

					int counter = 0;
					for(L2Spawn spawn : spawns)
					{
						ThreadPoolManager.getInstance().scheduleGeneral(() -> {
							L2Npc npc = spawn.spawnOne(false);
							npc.setIsNoRndWalk(true);
							npc.setIsRunning(true);
						}, counter);

						counter += Rnd.get(650, 1500);
					}
				}, stringIntegerEntry.getValue()));
			}
		}, delay);
	}

	private EnterInstanceResult checkConditions(L2PcInstance player, int instanceTemplateId)
	{
		L2Party party = player.getParty();

		/* Для дебага */
		if(player.isGM())
		{
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
				return EnterInstanceResult.LOCKED_DUE_TO_ENTER_TIME;
			}
			return EnterInstanceResult.OK;
		}

		if(player.getParty() == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return EnterInstanceResult.NO_PARTY;
		}

		int minPlayers = Config.MIN_FORTUNA_PLAYERS;
		int maxPlayers = Config.MAX_FORTUNA_PLAYERS;
		int minLevel = Config.MIN_LEVEL_FORTUNA_PLAYERS;

		if(!party.getLeader().equals(player))
		{
			party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
			return EnterInstanceResult.NOT_LEADER;
		}
		if(party.getMemberCount() > maxPlayers)
		{
			party.broadcastMessage(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
			return EnterInstanceResult.TOO_MANY_MEMBERS;
		}
		if(party.getMemberCount() < minPlayers)
		{
			party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(minPlayers));
			return EnterInstanceResult.TOO_LESS_MEMBERS;
		}

		for(L2PcInstance member : party.getMembers())
		{
			if(member == null || member.getLevel() < minLevel || !member.isAwakened())
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
				return EnterInstanceResult.TOO_LOW_LEVEL;
			}
			if(!Util.checkIfInRange(1000, player, member, true))
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
				return EnterInstanceResult.MEMBER_OUT_OF_RANGE;
			}
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), instanceTemplateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
				return EnterInstanceResult.LOCKED_DUE_TO_ENTER_TIME;
			}
		}
		return EnterInstanceResult.OK;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		synchronized(this)
		{
			if(character instanceof L2PcInstance)
			{
				FortunaWorld world = InstanceManager.getInstance().getInstanceWorld(character, FortunaWorld.class);

				if(world != null)
				{
					if(!world.playersInside.contains(character))
					{
						world.playersInside.add((L2PcInstance) character);
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
			FortunaWorld world = InstanceManager.getInstance().getInstanceWorld(character, FortunaWorld.class);

			if(world != null)
			{
				if(world.playersInside.contains(character))
				{
					world.playersInside.remove(character);
				}
			}
		}
		return super.onExitZone(character, zone);
	}

	public enum EnterInstanceResult
	{
		OK,
		ANOTHER_INSTANCE,
		NO_PARTY,
		NOT_LEADER,
		TOO_MANY_MEMBERS,
		TOO_LESS_MEMBERS,
		TOO_LOW_LEVEL,
		MEMBER_OUT_OF_RANGE,
		LOCKED_DUE_TO_ENTER_TIME,
		CANNOT_REENTER
	}

	public class FortunaWorld extends InstanceWorld
	{
		public List<L2PcInstance> playersInside = new FastList<>();
		public List<Future<?>> spawnTasks = new FastList<>();
	}
}
