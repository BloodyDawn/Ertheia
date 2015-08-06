package dwo.scripts.instances;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.InstanceManager.InstanceWorld;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2GuardInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.InstanceZoneId;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowScreenMessage;
import dwo.gameserver.util.Util;
import dwo.scripts.ai.zone.KartiaSupporters;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

/** TODO
 15182	1	u,Велика Аура Ненавести Адольфа\0	u,Провоцирует противника, находящихся возле персонажа, заставляя его сменить цель и атаковать самого себя.\0	a,none\0	a,none\0
 15183	1	u,Велика Аура Ненавести Адольфа\0	u,Провоцирует противника, находящихся возле персонажа, заставляя его сменить цель и атаковать самого себя.\0	a,none\0	a,none\0
 15184	1	u,Велика Аура Ненавести Адольфа\0	u,Провоцирует противника, находящихся возле персонажа, заставляя его сменить цель и атаковать самого себя.\0	a,none\0	a,none\0
 15185	1	u,Волна Щита Адольфа\0	u,Атакует персонажей, стоящих спереди. Вызывает шок в течение 9 сек. \0	a,none\0	a,none\0
 15186	1	u,Волна Щита Адольфа\0	u,Атакует персонажей, стоящих спереди. Вызывает шок в течение 9 сек. \0	a,none\0	a,none\0
 15187	1	u,Волна Щита Адольфа\0	u,Атакует персонажей, стоящих спереди. Вызывает шок в течение 9 сек. \0	a,none\0	a,none\0
 15188	1	u,Тучас Стрел Хаюка\0	u,Атакует цель и всех врагов вокруг нее. Наносит дополнительный урон. Мощность 23391.\0	a,none\0	a,none\0
 15189	1	u,Тучас Стрел Хаюка\0	u,Атакует цель и всех врагов вокруг нее. Наносит дополнительный урон. Мощность 27459.\0	a,none\0	a,none\0
 15190	1	u,Тучас Стрел Хаюка\0	u,Атакует цель и всех врагов вокруг нее. Наносит дополнительный урон. Мощность 31527.\0	a,none\0	a,none\0
 15191	1	u,Дождь Тяжелых Стрел Хаюка\0	u,Обрушивает дождь стрел на цель и врагов вокруг нее, наносит дополнительный урон. Мощность 22526.\0	a,none\0	a,none\0
 15192	1	u,Дождь Тяжелых Стрел Хаюка\0	u,Обрушивает дождь стрел на цель и врагов вокруг нее, наносит дополнительный урон. Мощность 26448.\0	a,none\0	a,none\0
 15193	1	u,Дождь Тяжелых Стрел Хаюка\0	u,Обрушивает дождь стрел на цель и врагов вокруг нее, наносит дополнительный урон. Мощность 30371.\0	a,none\0	a,none\0
 15194	1	u,Массовое Изгнание Эллии\0	u,Отправляет врагов вокруг цели в другой мир. Изгнанный враг не может двигаться, атаковать и использовать умения. Противник становится неуязвимим для отрицательных эффектов и каждую секунду теряет 282 HP.\0	a,none\0	a,none\0
 15195	1	u,Массовое Изгнание Эллии\0	u,Отправляет врагов вокруг цели в другой мир. Изгнанный враг не может двигаться, атаковать и использовать умения. Противник становится неуязвимим для отрицательных эффектов и каждую секунду теряет 296 HP.\0	a,none\0	a,none\0
 15196	1	u,Массовое Изгнание Эллии\0	u,Отправляет врагов вокруг цели в другой мир. Изгнанный враг не может двигаться, атаковать и использовать умения. Противник становится неуязвимим для отрицательных эффектов и каждую секунду теряет 313 HP.\0	a,none\0	a,none\0
 15197	1	u,Гнев Духа-Хранителя\0	u,Атака противников, находящихся вокрук персонажа.\0	a,none\0	a,none\0
 15198	1	u,Гнев Духа-Хранителя\0	u,Атака противников, находящихся вокрук персонажа.\0	a,none\0	a,none\0
 15199	1	u,Гнев Духа-Хранителя\0	u,Атака противников, находящихся вокрук персонажа.\0	a,none\0	a,none\0
 у Send Eliyah." (Summoner)
 */

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Kartia extends Quest
{
	public static final int KARTIA_SOLO85 = InstanceZoneId.KARTIAS_LABYRINTH_TAINTED_DIMENSION_ATTACKER_85.getId();
	public static final int KARTIA_SOLO90 = InstanceZoneId.KARTIAS_LABYRINTH_TAINTED_DIMENSION_ATTACKER_90.getId();
	public static final int KARTIA_SOLO95 = InstanceZoneId.KARTIAS_LABYRINTH_TAINTED_DIMENSION_ATTACKER_95.getId();
	public static final int KARTIA_PARTY85 = InstanceZoneId.KARTIAS_LABYRINTH_85.getId();
	public static final int KARTIA_PARTY90 = InstanceZoneId.KARTIAS_LABYRINTH_90.getId();
	public static final int KARTIA_PARTY95 = InstanceZoneId.KARTIAS_LABYRINTH_95.getId();
	public static final int SSQ_CAMERA = 18830;
	public static final int SOLO_POISON_ZONE = 12000052;
	public static final int PARTY_POISON_ZONE = 12000051;
	// Т.к. корейцы курят очень много, получилось две разных зоны для Картии. По гео, та, что правее считается соло-зоной.
	public static final int PARTY_LAIR_ZONE = 4600071;
	public static final int SOLO_LAIR_ZONE = 4600072;

	private static int _firstRoomSubwavesSize;

	public static final Map<String, Integer> SOLO85_MONSTERS = new FastMap<>();
	static
	{
		SOLO85_MONSTERS.put("npc_adolph", 33608);
		SOLO85_MONSTERS.put("support_adolph", 33609);
		SOLO85_MONSTERS.put("npc_barton", 33610);
		SOLO85_MONSTERS.put("support_barton", 33611);
		SOLO85_MONSTERS.put("npc_hayuk", 33612);
		SOLO85_MONSTERS.put("support_hayuk", 33613);
		SOLO85_MONSTERS.put("npc_eliyah", 33614);
		SOLO85_MONSTERS.put("support_eliyah", 33615);
		SOLO85_MONSTERS.put("npc_elise", 33616);
		SOLO85_MONSTERS.put("support_elise", 33617);
		SOLO85_MONSTERS.put("support_eliyah_spirit", 33618);
		SOLO85_MONSTERS.put("support_troop", 33642);
		SOLO85_MONSTERS.put("captivated", 33641);
		SOLO85_MONSTERS.put("altar", 19247);
		// мобы
		SOLO85_MONSTERS.put("keeper", 19220);
		SOLO85_MONSTERS.put("watcher", 19221);
		SOLO85_MONSTERS.put("overseer", 19222);
		SOLO85_MONSTERS.put("ruler", 19253);
	}
	public static final Map<String, Integer> PARTY85_MONSTERS = new FastMap<>();
	static
	{
		PARTY85_MONSTERS.put("support_troop", 33642);
		PARTY85_MONSTERS.put("altar", 19248);
		PARTY85_MONSTERS.put("captivated", 33641);
		PARTY85_MONSTERS.put("altar", 19248);
		// мобы
		PARTY85_MONSTERS.put("keeper", 19223);
		PARTY85_MONSTERS.put("watcher", 19230);
		PARTY85_MONSTERS.put("overseer", 19225);
		PARTY85_MONSTERS.put("ruler", 25882);
	}
	public static final Map<String, Integer> SOLO90_MONSTERS = new FastMap<>();
	static
	{
		SOLO90_MONSTERS.put("npc_adolph", 33619);
		SOLO90_MONSTERS.put("support_adolph", 33620);
		SOLO90_MONSTERS.put("npc_barton", 33621);
		SOLO90_MONSTERS.put("support_barton", 33622);
		SOLO90_MONSTERS.put("npc_hayuk", 33623);
		SOLO90_MONSTERS.put("support_hayuk", 33624);
		SOLO90_MONSTERS.put("npc_eliyah", 33625);
		SOLO90_MONSTERS.put("support_eliyah", 33626);
		SOLO90_MONSTERS.put("npc_elise", 33627);
		SOLO90_MONSTERS.put("support_elise", 33628);
		SOLO90_MONSTERS.put("support_eliyah_spirit", 33629);
		SOLO90_MONSTERS.put("support_troop", 33644);
		SOLO90_MONSTERS.put("altar", 19249);
		SOLO90_MONSTERS.put("captivated", 33643);
		SOLO90_MONSTERS.put("altar", 19249);
		// мобы
		SOLO90_MONSTERS.put("keeper", 19226);
		SOLO90_MONSTERS.put("watcher", 19224);
		SOLO90_MONSTERS.put("overseer", 19228);
		SOLO90_MONSTERS.put("ruler", 19254);
	}
	public static final Map<String, Integer> PARTY90_MONSTERS = new FastMap<>();
	static
	{
		PARTY90_MONSTERS.put("support_troop", 33644);
		PARTY85_MONSTERS.put("altar", 19250);
		PARTY90_MONSTERS.put("captivated", 33643);
		PARTY90_MONSTERS.put("altar", 19250);
		// мобы
		PARTY90_MONSTERS.put("keeper", 19229);
		PARTY90_MONSTERS.put("watcher", 19233);
		PARTY90_MONSTERS.put("overseer", 19231);
		PARTY90_MONSTERS.put("ruler", 25883);
	}
	public static final Map<String, Integer> SOLO95_MONSTERS = new FastMap<>();
	static
	{
		SOLO95_MONSTERS.put("npc_adolph", 33630);
		SOLO95_MONSTERS.put("support_adolph", 33631);
		SOLO95_MONSTERS.put("npc_barton", 33632);
		SOLO95_MONSTERS.put("support_barton", 33633);
		SOLO95_MONSTERS.put("npc_hayuk", 33634);
		SOLO95_MONSTERS.put("support_hayuk", 33635);
		SOLO95_MONSTERS.put("npc_eliyah", 33636);
		SOLO95_MONSTERS.put("support_eliyah", 33637);
		SOLO95_MONSTERS.put("npc_elise", 33638);
		SOLO95_MONSTERS.put("support_elise", 33639);
		SOLO95_MONSTERS.put("support_eliyah_spirit", 33640);
		SOLO95_MONSTERS.put("support_troop", 33646);
		SOLO95_MONSTERS.put("altar", 19251);
		SOLO95_MONSTERS.put("captivated", 33645);
		SOLO95_MONSTERS.put("altar", 19251);
		// мобы
		SOLO95_MONSTERS.put("keeper", 19228);
		SOLO95_MONSTERS.put("watcher", 19227);
		SOLO95_MONSTERS.put("overseer", 19234);
		SOLO95_MONSTERS.put("ruler", 19255);
	}
	public static final Map<String, Integer> PARTY95_MONSTERS = new FastMap<>();
	static
	{
		PARTY95_MONSTERS.put("support_troop", 33646);
		PARTY95_MONSTERS.put("altar", 19252);
		PARTY95_MONSTERS.put("captivated", 33645);
		PARTY95_MONSTERS.put("altar", 19252);
		// мобы
		PARTY95_MONSTERS.put("keeper", 19235);
		PARTY95_MONSTERS.put("watcher", 19236);
		PARTY95_MONSTERS.put("overseer", 19237);
		PARTY95_MONSTERS.put("ruler", 25884);
	}
	private static final String qn = "Kartia";
	private static final Location SOLO_ENTRANCE = new Location(-108983, -10446, -11920);
	private static final Location PARTY_ENTRANCE = new Location(-118510, -10449, -11920);
	private static final Location SUPPORT_ENTRANCE = new Location(-107155, -10576, -12076);
	private static final Location SOLO_UPSTAIR_TELEPORT = new Location(-110262, -10547, -11925);
	private static final Location PARTY_UPSTAIR_TELEPORT = new Location(-119830, -10547, -11925);
	private static final Location RAID_ENTRANCE = new Location(-111281, -14239, -11428);
	private static final int SOLO_UPSTAIR_ZONE = 400061;
	private static final int PARTY_UPSTAIR_ZONE = 400062;
	private static final int SOLO_ROOM_DOOR = 16170002;
	private static final int SOLO_RAID_DOOR = 16170003;
	private static final int PARTY_ROOM_DOOR = 16170012;
	private static final int PARTY_RAID_DOOR = 16170003;
	private static final Map<String, List<String>> _firstRoomWaveNames = new FastMap<>();
	static
	{
		List<String> waves = new FastList<>();
		waves.add("wave1");
		_firstRoomWaveNames.put("wave1", waves);
		waves = new FastList<>();
		waves.add("wave2");
		_firstRoomWaveNames.put("wave2", waves);
		waves = new FastList<>();
		waves.add("wave3_part1");
		waves.add("wave3_part2");
		_firstRoomWaveNames.put("wave3", waves);
		waves = new FastList<>();
		waves.add("wave4_part1");
		waves.add("wave4_part2");
		_firstRoomWaveNames.put("wave4", waves);
		waves = new FastList<>();
		waves.add("wave5_part1");
		waves.add("wave5_part2");
		waves.add("wave5_part3");
		_firstRoomWaveNames.put("wave5", waves);
		waves = new FastList<>();
		waves.add("wave6_part1");
		waves.add("wave6_part2");
		waves.add("wave6_part3");
		_firstRoomWaveNames.put("wave6", waves);
		waves = new FastList<>();
		waves.add("wave7_part1");
		waves.add("wave7_part2");
		waves.add("wave7_part3");
		_firstRoomWaveNames.put("wave7", waves);

		for(List<String> spawns : _firstRoomWaveNames.values())
		{
			_firstRoomSubwavesSize += spawns.size();
		}
	}
	private static final Map<String, List<String>> _secondRoomWaveNames = new FastMap<>();
	static
	{
		List<String> waves = new FastList<>();
		waves.add("wave_room");
		_secondRoomWaveNames.put("wave_room", waves);
		waves = new FastList<>();
		waves.add("wave_room_ending");
		_secondRoomWaveNames.put("wave_room_ending", waves);
	}
	private static final Map<String, List<String>> _raidRoomWaveNames = new FastMap<>();
	static
	{
		List<String> waves = new FastList<>();
		waves.add("wave_rb1");
		_raidRoomWaveNames.put("wave1", waves);
		waves = new FastList<>();
		waves.add("wave_rb2");
		_raidRoomWaveNames.put("wave2", waves);
		waves = new FastList<>();
		waves.add("wave_rb3");
		_raidRoomWaveNames.put("wave3", waves);
		waves = new FastList<>();
		waves.add("wave_rb4");
		_raidRoomWaveNames.put("wave4", waves);
		waves = new FastList<>();
		waves.add("wave_rb5");
		_raidRoomWaveNames.put("wave5", waves);
		waves = new FastList<>();
		waves.add("wave_rb6");
		_raidRoomWaveNames.put("wave6", waves);
		waves = new FastList<>();
		waves.add("wave_rb7");
		_raidRoomWaveNames.put("wave7", waves);
	}

	private static Kartia _kartiaInstance;

	public Kartia()
	{

		addEnterZoneId(SOLO_UPSTAIR_ZONE, PARTY_UPSTAIR_ZONE);
		addEnterZoneId(SOLO_LAIR_ZONE, PARTY_LAIR_ZONE);
		addExitZoneId(SOLO_LAIR_ZONE, PARTY_LAIR_ZONE);

		SOLO85_MONSTERS.values().forEach(this::addKillId);

		SOLO90_MONSTERS.values().forEach(this::addKillId);

		SOLO95_MONSTERS.values().forEach(this::addKillId);

		PARTY85_MONSTERS.values().forEach(this::addKillId);

		PARTY90_MONSTERS.values().forEach(this::addKillId);

		PARTY95_MONSTERS.values().forEach(this::addKillId);
	}

	public static void main(String[] args)
	{
		_kartiaInstance = new Kartia();
	}

	public static Kartia getInstance()
	{
		return _kartiaInstance;
	}

	public long getReuseTime()
	{
		Calendar _instanceTime = Calendar.getInstance();

		Calendar currentTime = Calendar.getInstance();
		_instanceTime.set(Calendar.HOUR_OF_DAY, 6);
		_instanceTime.set(Calendar.MINUTE, 30);
		_instanceTime.set(Calendar.SECOND, 0);

		if(_instanceTime.compareTo(currentTime) < 0)
		{
			_instanceTime.add(Calendar.DAY_OF_MONTH, 1);
		}

		return _instanceTime.getTimeInMillis();
	}

	private void startChallenge(KartiaWorld world)
	{
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(world.isPartyInstance)
		{
			world.kartiaAlthar = addSpawn(world.monsterSet.get("altar"), -119684, -10453, -11307, 0, false, 0, false, world.instanceId);
			world.ssqCameraLight = addSpawn(SSQ_CAMERA, -119684, -10453, -11307, 0, false, 0, false, world.instanceId);
			world.ssqCameraZone = addSpawn(SSQ_CAMERA, -119907, -10443, -11924, 0, false, 0, false, world.instanceId);
		}
		else
		{
			world.kartiaAlthar = addSpawn(world.monsterSet.get("altar"), -110116, -10453, -11307, 0, false, 0, false, world.instanceId);
			world.ssqCameraLight = addSpawn(SSQ_CAMERA, -110116, -10453, -11307, 0, false, 0, false, world.instanceId);
			world.ssqCameraZone = addSpawn(SSQ_CAMERA, -110339, -10443, -11924, 0, false, 0, false, world.instanceId);
		}

		world.ssqCameraZone.setDisplayEffect(0x03);
		world.ssqCameraZone.setDisplayEffect(0x00);

		world.ssqCameraLight.setDisplayEffect(0x03);
		world.ssqCameraLight.setDisplayEffect(0x00);

		world.kartiaAlthar.setIsNoRndWalk(true);
		world.kartiaAlthar.setIsInvul(true);
		world.ssqCameraLight.setIsNoRndWalk(true);
		world.ssqCameraZone.setIsNoRndWalk(true);

		// Спауним саппорт
		if(!world.isPartyInstance)
		{
			world.knight = addSpawn(world.monsterSet.get("support_adolph"), SOLO_ENTRANCE.getX(), SOLO_ENTRANCE.getY(), SOLO_ENTRANCE.getZ(), 0, true, 0, false, world.instanceId);
			world.followers.add(world.knight);

			if(!world.excludedSupport.equals("WARRIOR"))
			{
				world.warrior = addSpawn(world.monsterSet.get("support_barton"), SOLO_ENTRANCE.getX(), SOLO_ENTRANCE.getY(), SOLO_ENTRANCE.getZ(), 0, true, 0, false, world.instanceId);
				world.followers.add(world.warrior);
			}

			if(!world.excludedSupport.equals("ARCHER"))
			{
				world.archer = addSpawn(world.monsterSet.get("support_hayuk"), SOLO_ENTRANCE.getX(), SOLO_ENTRANCE.getY(), SOLO_ENTRANCE.getZ(), 0, true, 0, false, world.instanceId);
				world.followers.add(world.archer);
			}

			if(!world.excludedSupport.equals("SUMMONER"))
			{
				world.summoner = addSpawn(world.monsterSet.get("support_eliyah"), SOLO_ENTRANCE.getX(), SOLO_ENTRANCE.getY(), SOLO_ENTRANCE.getZ(), 0, true, 0, false, world.instanceId);
				world.followers.add(world.summoner);

				for(byte i = 0; i < 3; ++i)
				{
					L2Npc light = addSpawn(world.monsterSet.get("support_eliyah_spirit"), world.summoner.getX(), world.summoner.getY(), world.summoner.getZ(), 0, true, 0, false, world.instanceId);
					world.followers.add(light);
				}
			}

			if(!world.excludedSupport.equals("HEALER"))
			{
				world.healer = addSpawn(world.monsterSet.get("support_elise"), SOLO_ENTRANCE.getX(), SOLO_ENTRANCE.getY(), SOLO_ENTRANCE.getZ(), 0, true, 0, false, world.instanceId);
				world.followers.add(world.healer);

				instance.addTask("healTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new KartiaSupporters.HealTask(world), 2000, 7000));
			}

			for(L2Npc guard : world.followers)
			{
				((L2GuardInstance) guard).setCanAttackPlayer(false);
				((L2GuardInstance) guard).setCanAttackGuard(false);
			}

			instance.addTask("supportTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new KartiaSupporters.KartiaSupportTask(world), 2000, 2000));
		}

		// Закрываем двери
		for(L2DoorInstance door : instance.getDoors())
		{
			door.closeMe();
		}

		// Спауним пленных
		for(L2Spawn spawn : instance.getGroupSpawn("captivated"))
		{
			L2Npc captivated = spawn.spawnOne(false);
			captivated.setIsRunning(false);
			((L2GuardInstance) captivated).setCanAttackPlayer(false);
			world.captivateds.add(captivated);
		}

		ThreadPoolManager.getInstance().scheduleGeneral(() -> nextWave(world), 10000);

		instance.addTask("aggroCheckTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new KartiaSupporters.MonsterAggroTask(world), 5000, 3000));
		instance.addTask("waveMovementTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new KartiaSupporters.MonsterMovementTask(world), 5000, 3000));
		instance.addTask("altharCheckTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new KartiaSupporters.AltharTask(world), 5000, 3000));
	}

	/**
	 * Спауним лечащее дерево для игрока. Т.к. НПЦ не умеют сами юзать саммонов, эмулируем сию ситуацию.
	 * @param world
	 */
	public void spawnHealingTree(KartiaWorld world)
	{
		if(world.playersInLairZone.size() <= 0 || world.isPartyInstance)
		{
			return;
		}

		L2Skill buff = SkillTable.getInstance().getInfo(15003, 1);
		L2Skill heal = SkillTable.getInstance().getInfo(15002, 1);

		final L2PcInstance player = world.playersInLairZone.get(0);
		Location loc = player.getLoc();

		final L2Character tree = addSpawn(19256, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), false, 60, false, world.instanceId);
		tree.setHeading(world.playersInLairZone.get(0).getHeading());
		tree.setTarget(player);
		tree.doCast(buff);
		tree.doCast(heal);

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if(tree != null && !player.isDead())
				{
					tree.setTarget(player);
					// Кастуем цепное возрождение
					tree.doCast(SkillTable.getInstance().getInfo(15002, 1));

					ThreadPoolManager.getInstance().scheduleGeneral(this, 10000);
				}
			}
		}, 10000);

		ThreadPoolManager.getInstance().scheduleGeneral(new Runnable()
		{
			@Override
			public void run()
			{
				if(tree != null && !player.isDead())
				{
					tree.setTarget(player);
					// Кастуем бафф, восстанавливающий 666 ХП каждую секунду
					tree.doCast(SkillTable.getInstance().getInfo(15003, 1));

					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
				}
			}
		}, 20000);
	}

	/**
	 * Получение спауна для следующей волны мобов.
	 *
	 * @param world
	 * @return
	 */
	private boolean nextWave(KartiaWorld world)
	{
		if(world.status > 2)
		{
			return false;
		}

		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(instance == null)
		{
			return false;
		}

		// Выбираем волну мобов на основании статуса инстанса
		Map<String, List<String>> waves = null;
		if(world.status == 0)
		{
			waves = _firstRoomWaveNames;
		}
		else if(world.status == 1)
		{
			waves = _secondRoomWaveNames;
		}
		else if(world.status == 2)
		{
			waves = _raidRoomWaveNames;
		}

		if(waves == null || world.currentWave >= waves.size())
		{
			return false;
		}

		List<String> subwaves = null;
		int i = 0;
		for(List<String> wave : waves.values())
		{
			if(i == world.currentWave)
			{
				if(world.currentSubwave >= wave.size())
				{
					world.currentSubwave = 0;
					++world.currentWave;
					++i;
					continue;
				}
				subwaves = wave;
				world.currentWave = i;
			}
			++i;
		}

		if(subwaves == null || world.currentSubwave >= subwaves.size())
		{
			return false;
		}

		String waveName = subwaves.get(world.currentSubwave++);

		List<L2Spawn> spawnList = instance.getGroupSpawn(waveName);

		if(world.currentSubwave - 1 == 0 && (world.status == 0 || world.status == 2))
		{
			for(L2PcInstance player : world.playersInLairZone)
			{
				ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.STAGE_S1, ExShowScreenMessage.TOP_CENTER, 3000, (world.currentWave + 1) + "-й ");
				player.sendPacket(msg);
			}
		}

		world.waveSpawnTime = 0;
		world.wave.clear();
		world.monstersToKill.clear();
		for(L2Spawn spawn : spawnList)
		{
			int npcId = spawn.getNpcId();
			if(!world.monstersToKill.containsKey(npcId))
			{
				world.monstersToKill.put(npcId, 0);
			}

			world.monstersToKill.put(spawn.getNpcId(), world.monstersToKill.get(npcId) + 1);
			L2Npc npc = spawn.spawnOne(false);
			npc.setRunning();

			world.wave.add(npc);
		}
		world.waveSpawnTime = System.currentTimeMillis();

		return true;
	}

	public void cleanup(KartiaWorld world)
	{
		if(world.isPartyInstance)
		{
			ZoneManager.getInstance().getZoneById(PARTY_POISON_ZONE).setEnabled(false);
		}
		else
		{
			ZoneManager.getInstance().getZoneById(SOLO_POISON_ZONE).setEnabled(false);
		}

		world.ssqCameraZone.setDisplayEffect(0x03);
		world.ssqCameraZone.setDisplayEffect(0x00);
		world.ssqCameraZone.getLocationController().delete();

		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

		if(instance != null)
		{
			instance.cancelTasks();
		}
	}

	@Override
	public String onNpcDie(L2Npc npc, L2Character killer)
	{
		KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(killer, KartiaWorld.class);

		if(world != null)
		{
			if(world.status <= 2 && world.monstersToKill != null && !world.monstersToKill.isEmpty())
			{
				boolean neededToProgress = false;
				for(int npcId : world.monstersToKill.keySet())
				{
					//_log.info("Kartia: you need to kill additional " + world.monstersToKill.get(npcId) + " NPCs with ID " + npcId + " to pass next wave #" + (world.currentWave + 1) + ".");
					if(npc.getNpcId() == npcId)
					{
						int newValue = world.monstersToKill.get(npcId) - 1;
						if(newValue <= 0)
						{
							world.monstersToKill.remove(npcId);
						}
						else
						{
							world.monstersToKill.put(npcId, newValue);
						}

						neededToProgress = true;
						break;
					}
				}

				if(neededToProgress)
				{
					boolean needNextWave = true;
					for(int killCount : world.monstersToKill.values())
					{
						if(killCount > 0)
						{
							needNextWave = false;
							break;
						}
					}

					if(needNextWave)
					{
						if(world.currentSubwave - 1 == 0)
						{
							++world.killedWaves;
						}

						++world.killedSubwaves;

						if(world.status == 0 && world.killedWaves == _firstRoomWaveNames.size() && world.killedSubwaves == _firstRoomSubwavesSize - 1)
						{
							world.poisonZoneEnabled = false;
							world.ssqCameraLight.setDisplayEffect(0x01);
							world.ssqCameraZone.setDisplayEffect(0x02);

							if(world.isPartyInstance)
							{
								ZoneManager.getInstance().getZoneById(PARTY_POISON_ZONE).setEnabled(true);
							}
							else
							{
								ZoneManager.getInstance().getZoneById(SOLO_POISON_ZONE).setEnabled(true);
							}
						}
						// Закончились первые 7 этапов
						else if(world.status == 0 && world.killedSubwaves >= _firstRoomSubwavesSize)
						{
							world.status = 1;
							world.killedWaves = 0;
							world.currentWave = 0;
							world.currentSubwave = 0;
							Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
							if(instance != null)
							{
								if(world.isPartyInstance)
								{
									if(instance.getDoor(PARTY_ROOM_DOOR) == null)
									{
										_log.error("Invalid door for instance of Kartia. Door ID: [" + PARTY_ROOM_DOOR + "], instance template ID: [" + world.templateId + "], instance type: [" + world.type + "].");
									}
									else
									{
										instance.getDoor(PARTY_ROOM_DOOR).openMe();
									}
								}
								else
								{
									if(instance.getDoor(SOLO_ROOM_DOOR) == null)
									{
										_log.error("Invalid door for instance of Kartia. Door ID: [" + SOLO_ROOM_DOOR + "], instance template ID: [" + world.templateId + "], instance type: [" + world.type + "].");
									}
									else
									{
										instance.getDoor(SOLO_ROOM_DOOR).openMe();
									}
								}
							}

							world.poisonZoneEnabled = false;
							world.ssqCameraZone.setDisplayEffect(0x03);
							world.ssqCameraZone.setDisplayEffect(0x00);

							if(world.isPartyInstance)
							{
								ZoneManager.getInstance().getZoneById(PARTY_POISON_ZONE).setEnabled(false);
							}
							else
							{
								ZoneManager.getInstance().getZoneById(SOLO_POISON_ZONE).setEnabled(false);
							}

							KartiaSupporters.saveCaptivateds(world);
						}
						// Игроки вынесли комнату перед РБ
						else if(world.status == 1 && world.killedWaves >= _secondRoomWaveNames.size() - 1)
						{
							openRaidDoor(world);
						}
						else if(world.status == 2 && world.killedWaves >= _raidRoomWaveNames.size() - 1)
						{
							world.status = 3;
							KartiaSupporters.freeRuler(world);
						}

						if(world.status < 3)
						{
							ThreadPoolManager.getInstance().scheduleGeneral(() -> nextWave(world), 10000);
						}
					}
				}
			}
			else if(world.status == 3 && npc.getNpcId() == world.monsterSet.get("ruler"))
			{
				cleanup(world);

				Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
				instance.setDuration(300000);

				if(!world.isPartyInstance)
				{
					for(L2PcInstance player : world.playersInLairZone)
					{
						switch(world.type)
						{
							case SOLO85:
								player.addExpAndSp(486407696, 3800614);
								break;
							case SOLO90:
								player.addExpAndSp(672353854, 5644776);
								break;
							case SOLO95:
								player.addExpAndSp(972042801, 8502614);
								break;
						}
					}
				}

				long instanceTime = getReuseTime();
				for(Integer playerId : instance.getPlayers())
				{
					InstanceManager.getInstance().setInstanceTime(playerId, world.templateId, instanceTime);
				}
			}
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(character, KartiaWorld.class);

		if(world != null)
		{
			// Не даем игрокам подняться по лестнице :)
			if(world.status == 0)
			{
				if((world.isPartyInstance && zone.getId() == PARTY_UPSTAIR_ZONE || !world.isPartyInstance && zone.getId() == SOLO_UPSTAIR_ZONE) && (character instanceof L2PcInstance || character instanceof L2Summon))
				{
					if(world.isPartyInstance)
					{
						character.teleToInstance(PARTY_UPSTAIR_TELEPORT, world.instanceId);
					}
					else
					{
						character.teleToInstance(SOLO_UPSTAIR_TELEPORT, world.instanceId);
					}
				}
			}

			if(character.isPlayer() && (world.isPartyInstance && zone.getId() == PARTY_LAIR_ZONE || !world.isPartyInstance && zone.getId() == SOLO_LAIR_ZONE))
			{
				// Вешаем хук при смерти, чтобы закрыть инстанс через 15 секунд
				if(!world.playersInLairZone.isEmpty())
				{
					AfterDeathHook hook = world.deathHooks.containsKey(character) ? world.deathHooks.get(character) : new AfterDeathHook();
					character.getHookContainer().addHook(HookType.ON_DIE, hook);
				}

				if(!world.playersInLairZone.contains(character))
				{
					world.playersInLairZone.add((L2PcInstance) character);
				}
			}

			if(world.ssqCameraZone != null)
			{
				if(world.poisonZoneEnabled)
				{
					world.ssqCameraZone.setDisplayEffect(0x02);
				}
				else
				{
					world.ssqCameraZone.setDisplayEffect(0x03);
					world.ssqCameraZone.setDisplayEffect(0x00);
				}

				if(world.isPartyInstance)
				{
					ZoneManager.getInstance().getZoneById(PARTY_POISON_ZONE).setEnabled(world.poisonZoneEnabled);
				}
				else
				{
					ZoneManager.getInstance().getZoneById(SOLO_POISON_ZONE).setEnabled(world.poisonZoneEnabled);
				}
			}
		}

		return null;
	}

	@Override
	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(character, KartiaWorld.class);

		if(world != null)
		{
			if(character.isPlayer() && (world.isPartyInstance && zone.getId() == PARTY_LAIR_ZONE || !world.isPartyInstance && zone.getId() == SOLO_LAIR_ZONE))
			{
				if(world.deathHooks.containsKey(character))
				{
					character.getHookContainer().removeHook(HookType.ON_DIE, world.deathHooks.get(character));
				}

				if(world.playersInLairZone.contains(character))
				{
					world.playersInLairZone.remove(character);
				}
			}

			if(world.ssqCameraZone != null)
			{
				if(world.poisonZoneEnabled)
				{
					world.ssqCameraZone.setDisplayEffect(0x02);
				}
				else
				{
					world.ssqCameraZone.setDisplayEffect(0x03);
					world.ssqCameraZone.setDisplayEffect(0x00);
				}

				if(world.isPartyInstance)
				{
					ZoneManager.getInstance().getZoneById(PARTY_POISON_ZONE).setEnabled(world.poisonZoneEnabled);
				}
				else
				{
					ZoneManager.getInstance().getZoneById(SOLO_POISON_ZONE).setEnabled(world.poisonZoneEnabled);
				}
			}
		}

		return null;
	}

	public void openRaidDoor(KartiaWorld world)
	{
		world.status = 2;
		world.killedWaves = 0;
		world.currentWave = 0;
		world.currentSubwave = 0;
		Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);
		if(world.isPartyInstance)
		{
			instance.getDoor(PARTY_RAID_DOOR).openMe();
		}
		else
		{
			instance.getDoor(SOLO_RAID_DOOR).openMe();
		}

		world.ruler = world.isPartyInstance ? addSpawn(world.monsterSet.get("ruler"), -120864, -15872, -11400, 15596, false, 0, false, world.instanceId) : addSpawn(world.monsterSet.get("ruler"), -111296, -15872, -11400, 15596, false, 0, false, world.instanceId);

		if(world.status < 2 && world.savedCaptivateds > 0)
		{
			for(int i = 0; i < world.savedCaptivateds; ++i)
			{
				L2Npc support;
				support = world.isPartyInstance ? addSpawn(world.monsterSet.get("support_troop"), -120901, -14562, -11424, 47595, true, 0, false, world.instanceId) : addSpawn(world.monsterSet.get("support_troop"), -111333, -14562, -11424, 47595, true, 0, false, world.instanceId);
				((L2GuardInstance) support).setCanAttackPlayer(false);
				((L2GuardInstance) support).setCanAttackGuard(false);
				world.supports.add(support);
			}

			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new KartiaSupporters.RaidSupportTask(world), 1000, 5000);
		}

		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(world.isPartyInstance)
			{
				instance.getDoor(PARTY_RAID_DOOR).closeMe();
			}
			else
			{
				instance.getDoor(SOLO_RAID_DOOR).closeMe();
			}
		}, 60000);
	}

	public void deselectSupport(KartiaWorld world, String support)
	{
		if(world.type != KartiaType.SOLO85 && world.type != KartiaType.SOLO90 && world.type != KartiaType.SOLO95)
		{
			return;
		}

		if(world.excludedSupport == null || world.excludedSupport.isEmpty())
		{
			world.excludedSupport = support;
			for(L2PcInstance player : world.playersInside)
			{
				player.teleToInstance(SOLO_ENTRANCE, world.instanceId);
			}

			startChallenge(world);
		}
	}

	public int enterInstance(L2PcInstance player, int instanceTemplateId)
	{
		int instanceId;
		InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);

		Location entrance = SOLO_ENTRANCE;

		if(world != null)
		{
			if(!(world instanceof KartiaWorld))
			{
				player.sendPacket(SystemMessageId.ALREADY_ENTERED_ANOTHER_INSTANCE_CANT_ENTER);
				return 0;
			}

			if(!((KartiaWorld) world).playersInLairZone.contains(player))
			{
				((KartiaWorld) world).playersInLairZone.add(player);
				world.allowed.add(player.getObjectId());
			}

			if(((KartiaWorld) world).status >= 2)
			{
				entrance = RAID_ENTRANCE;
			}

			player.teleToInstance(entrance, world.instanceId);
			return world.instanceId;
		}
		else
		{
			if(instanceTemplateId == KARTIA_SOLO85 || instanceTemplateId == KARTIA_SOLO90 || instanceTemplateId == KARTIA_SOLO95)
			{
				entrance = SUPPORT_ENTRANCE;
			}
			else if(instanceTemplateId == KARTIA_PARTY85 || instanceTemplateId == KARTIA_PARTY90 || instanceTemplateId == KARTIA_PARTY95)
			{
				entrance = PARTY_ENTRANCE;
			}
			else
			{
				_log.log(Level.WARN, "Unknown Kartia template ID [" + instanceTemplateId + "].");
			}

			world = new KartiaWorld();
			KartiaWorld kartiaWorld = (KartiaWorld) world;
			kartiaWorld.type = KartiaType.getTypeByTemplateId(instanceTemplateId);
			kartiaWorld.isPartyInstance = KartiaType.isPartyInstance(kartiaWorld.type);

			if(!checkConditions(player, kartiaWorld))
			{
				return 0;
			}

			String dynamicInstanceName = "";
			switch(KartiaType.getTypeByTemplateId(instanceTemplateId))
			{
				case SOLO85:
					dynamicInstanceName = "Kartia_S85.xml";
					break;
				case SOLO90:
					dynamicInstanceName = "Kartia_S90.xml";
					break;
				case SOLO95:
					dynamicInstanceName = "Kartia_S95.xml";
					break;
				case PARTY85:
					dynamicInstanceName = "Kartia_P85.xml";
					break;
				case PARTY90:
					dynamicInstanceName = "Kartia_P90.xml";
					break;
				case PARTY95:
					dynamicInstanceName = "Kartia_P95.xml";
					break;
			}

			if(dynamicInstanceName.isEmpty())
			{
				return 0;
			}

			instanceId = InstanceManager.getInstance().createDynamicInstance(dynamicInstanceName);

			kartiaWorld.instanceId = instanceId;
			kartiaWorld.templateId = instanceTemplateId;
			kartiaWorld.status = 0;

			switch(kartiaWorld.type)
			{
				case SOLO85:
					kartiaWorld.monsterSet = SOLO85_MONSTERS;
					break;
				case SOLO90:
					kartiaWorld.monsterSet = SOLO90_MONSTERS;
					break;
				case SOLO95:
					kartiaWorld.monsterSet = SOLO95_MONSTERS;
					break;
				case PARTY85:
					kartiaWorld.monsterSet = PARTY85_MONSTERS;
					break;
				case PARTY90:
					kartiaWorld.monsterSet = PARTY90_MONSTERS;
					break;
				case PARTY95:
					kartiaWorld.monsterSet = PARTY95_MONSTERS;
					break;
			}

			InstanceManager.getInstance().addWorld(world);

			if(!((KartiaWorld) world).isPartyInstance)
			{
				Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

				for(L2Spawn spawn : instance.getGroupSpawn("support"))
				{
					spawn.spawnOne(false);
				}
			}

			if(!kartiaWorld.isPartyInstance && player.getParty() == null)
			{
				player.teleToInstance(entrance, instanceId);
				world.allowed.add(player.getObjectId());
				((KartiaWorld) world).playersInLairZone.add(player);
				((KartiaWorld) world).playersInside.add(player);
				if(((KartiaWorld) world).isPartyInstance)
				{
					startChallenge(kartiaWorld);
				}
				return instanceId;
			}
			if(kartiaWorld.isPartyInstance && player.getParty() != null)
			{
				for(L2PcInstance member : player.getParty().getMembers())
				{
					member.teleToInstance(entrance, instanceId);
					world.allowed.add(member.getObjectId());
					((KartiaWorld) world).playersInLairZone.add(member);
					((KartiaWorld) world).playersInside.add(member);
				}
				if(((KartiaWorld) world).isPartyInstance)
				{
					startChallenge(kartiaWorld);
				}
				return instanceId;
			}

			return 0;
		}
	}

	private boolean checkConditions(L2PcInstance player, KartiaWorld world)
	{
		L2Party party = player.getParty();

		/* Для дебага */
		if(player.isGM())
		{
			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), world.templateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
				return false;
			}
			return true;
		}

		if(world.isPartyInstance && party == null)
		{
			player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
			return false;
		}

		int minPlayers = world.type.getMinPlayers();
		int maxPlayers = world.type.getMaxPlayers();
		int minLevel = world.type.getMinLevel();
		int maxLevel = world.type.getMaxLevel();

		if(world.isPartyInstance)
		{
			if(!party.getLeader().equals(player))
			{
				party.broadcastMessage(SystemMessageId.ONLY_PARTY_LEADER_CAN_ENTER);
				return false;
			}
			if(party.getMemberCount() > maxPlayers)
			{
				player.sendPacket(SystemMessageId.PARTY_EXCEEDED_THE_LIMIT_CANT_ENTER);
				return false;
			}
			if(party.getMemberCount() < minPlayers)
			{
				party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(minPlayers));
				return false;
			}

			for(L2PcInstance member : party.getMembers())
			{
				/* В инст пускает только перерожденных чаров и минимальный лвл с которого пускает 85. */
				if(member == null || member.getLevel() < minLevel || member.getLevel() > maxLevel || !member.isAwakened())
				{
					party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
					return false;
				}
				if(!Util.checkIfInRange(1000, player, member, true))
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
					return false;
				}
				Long reEnterTime = InstanceManager.getInstance().getInstanceTime(member.getObjectId(), world.templateId);
				if(System.currentTimeMillis() < reEnterTime)
				{
					party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(member));
					return false;
				}
			}
		}
		else
		{
			if(player.getLevel() < minLevel || player.getLevel() > maxLevel || !player.isAwakened())
			{
				player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(player));
				return false;
			}

			Long reEnterTime = InstanceManager.getInstance().getInstanceTime(player.getObjectId(), world.templateId);
			if(System.currentTimeMillis() < reEnterTime)
			{
				party.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_MAY_NOT_REENTER_YET).addPcName(player));
				return false;
			}
		}
		return true;
	}

	public enum KartiaType
	{
		SOLO85(1, 1, 85, 89),
		SOLO90(1, 1, 90, 94),
		SOLO95(1, 1, 95, 99),
		PARTY85(2, 7, 85, 89),
		PARTY90(2, 7, 90, 94),
		PARTY95(2, 7, 95, 99);

		private final int _minPlayers;
		private final int _maxPlayers;
		private final int _minLevel;
		private final int _maxLevel;

		KartiaType(int minPlayers, int maxPlayers, int minLevel, int maxLevel)
		{
			_minPlayers = minPlayers;
			_maxPlayers = maxPlayers;
			_minLevel = minLevel;
			_maxLevel = maxLevel;
		}

		public static boolean isPartyInstance(KartiaType type)
		{
			return type == PARTY85 || type == PARTY90 || type == PARTY95;
		}

		public static KartiaType getTypeByTemplateId(int templateId)
		{
			if(templateId == KARTIA_SOLO85)
			{
				return SOLO85;
			}
			if(templateId == KARTIA_SOLO90)
			{
				return SOLO90;
			}
			if(templateId == KARTIA_SOLO95)
			{
				return SOLO95;
			}
			if(templateId == KARTIA_PARTY85)
			{
				return PARTY85;
			}
			if(templateId == KARTIA_PARTY90)
			{
				return PARTY90;
			}
			if(templateId == KARTIA_PARTY95)
			{
				return PARTY95;
			}

			return null;
		}

		int getMinPlayers()
		{
			return _minPlayers;
		}

		int getMaxPlayers()
		{
			return _maxPlayers;
		}

		int getMinLevel()
		{
			return _minLevel;
		}

		int getMaxLevel()
		{
			return _maxLevel;
		}
	}

	public class AfterDeathHook extends AbstractHookImpl
	{
		@Override
		public void onDie(L2PcInstance player, L2Character killer)
		{
			KartiaWorld world = InstanceManager.getInstance().getInstanceWorld(player, KartiaWorld.class);

			if(world != null)
			{
				boolean exit = true;
				if(world.isPartyInstance)
				{
					for(L2PcInstance member : world.playersInLairZone)
					{
						if(!member.isDead())
						{
							exit = false;
							break;
						}
					}
				}

				if(exit)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(() -> {
						Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

						if(instance != null)
						{
							instance.setDuration(0);
						}
					}, 15000);
				}
			}
		}
	}

	public class KartiaWorld extends InstanceWorld
	{
		public KartiaType type;
		public List<L2PcInstance> playersInside = new FastList<>();
		public List<L2PcInstance> playersInLairZone = new FastList<>();
		public Map<L2PcInstance, AfterDeathHook> deathHooks = new FastMap<>();
		public boolean isPartyInstance;
		public List<L2Npc> captivateds = new FastList<L2Npc>().shared();
		public L2Npc kartiaAlthar;
		public L2Npc ssqCameraLight;
		public L2Npc ssqCameraZone;
		public int currentWave;
		public int currentSubwave;
		public long waveSpawnTime;
		public int killedSubwaves;
		public int killedWaves;
		public Map<String, Integer> monsterSet;
		public Map<Integer, Integer> monstersToKill = new FastMap<Integer, Integer>().shared();
		public List<L2Npc> wave = new FastList<>();
		public L2Npc ruler;
		public int savedCaptivateds;
		public List<L2Npc> supports = new FastList<>();
		public String excludedSupport;
		public List<L2Npc> followers = new FastList<>();
		public L2Npc warrior;
		public L2Npc archer;
		public L2Npc summoner;
		public L2Npc healer;
		public L2Npc knight;
		public boolean poisonZoneEnabled;
	}
}
