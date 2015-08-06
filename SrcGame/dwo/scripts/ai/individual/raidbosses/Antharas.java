package dwo.scripts.ai.individual.raidbosses;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.network.game.serverpackets.EarthQuake;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;
import dwo.gameserver.util.Rnd;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class Antharas extends Quest
{
	// config
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	//private static final int FWA_APPTIMEOFANTHARAS = 1800000;
	private static final int FWA_INACTIVITYTIME = 900000;
	private static final boolean FWA_OLDANTHARAS = false;
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;

	private static final int FWA_MAXMOBS = 10; // this includes Antharas itself
	private static final int FWA_INTERVALOFMOBSWEAK = 180000;
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	private static final int FWA_SELFDESTRUCTTIME = 15000;
	// Location of teleport cube.
	private static final int _teleportCubeId = 31859;
	// monstersId
	private static final int ANTHARASOLDID = 29019;
	private static final int ANTHARASWEAKID = 29066;
	private static final int ANTHARASNORMALID = 29067;
	private static final int ANTHARASSTRONGID = 29068;
	//Antharas Status Tracking :
	private static final byte DORMANT = 0;        //Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final byte WAITING = 1;        //Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter
	//before he unleashes his attack. Entry is unlocked
	private static final byte FIGHTING = 2;        //Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final byte DEAD = 3;            //Antharas has been killed. Entry is locked
	protected static long _LastAction;
	protected static L2BossZone _Zone;
	private static Antharas _instance;
	private final int[][] _teleportCubeLocation = {{177615, 114941, -7709, 0}};
	protected List<L2Spawn> _teleportCubeSpawn = new FastList<>();
	protected List<L2Npc> _teleportCube = new FastList<>();
	// Spawn data of monsters.
	protected TIntObjectHashMap<L2Spawn> _monsterSpawn = new TIntObjectHashMap<>();
	// Instance of monsters.
	protected List<L2Npc> _monsters = new FastList<>();
	protected L2GrandBossInstance _antharas;
	// Tasks.
	protected ScheduledFuture<?> _cubeSpawnTask;
	protected ScheduledFuture<?> _monsterSpawnTask;
	protected ScheduledFuture<?> _activityCheckTask;
	protected ScheduledFuture<?> _socialTask;
	protected ScheduledFuture<?> _mobiliseTask;
	protected ScheduledFuture<?> _mobsSpawnTask;
	protected ScheduledFuture<?> _selfDestructionTask;
	protected ScheduledFuture<?> _moveAtRandomTask;
	protected ScheduledFuture<?> _movieTask;

	public Antharas()
	{
		int[] mob = {
			ANTHARASOLDID, ANTHARASWEAKID, ANTHARASNORMALID, ANTHARASSTRONGID, 29069, 29070, 29071, 29072, 29073, 29074,
			29075, 29076
		};
		registerMobs(mob);
		init();
	}

	public static void main(String[] args)
	{
		_instance = new Antharas();
	}

	public static Antharas getInstance()
	{
		return _instance;
	}

	// Initialize
	private void init()
	{
		// Setting spawn data of monsters.
		try
		{
			_Zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
			L2NpcTemplate template1;
			L2Spawn tempSpawn;

			// Old Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASOLDID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS << 1);
			SpawnTable.getInstance().addNewSpawn(tempSpawn);
			_monsterSpawn.put(29019, tempSpawn);

			// Weak Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASWEAKID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS << 1);
			SpawnTable.getInstance().addNewSpawn(tempSpawn);
			_monsterSpawn.put(29066, tempSpawn);

			// Normal Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASNORMALID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS << 1);
			SpawnTable.getInstance().addNewSpawn(tempSpawn);
			_monsterSpawn.put(29067, tempSpawn);

			// Strong Antharas
			template1 = NpcTable.getInstance().getTemplate(ANTHARASSTRONGID);
			tempSpawn = new L2Spawn(template1);
			tempSpawn.setLocx(181323);
			tempSpawn.setLocy(114850);
			tempSpawn.setLocz(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS << 1);
			SpawnTable.getInstance().addNewSpawn(tempSpawn);
			_monsterSpawn.put(29068, tempSpawn);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}

		// Setting spawn data of teleport cube.
		try
		{
			L2NpcTemplate Cube = NpcTable.getInstance().getTemplate(_teleportCubeId);
			L2Spawn spawnDat;
			for(int[] element : _teleportCubeLocation)
			{
				spawnDat = new L2Spawn(Cube);
				spawnDat.setAmount(1);
				spawnDat.setLocx(element[0]);
				spawnDat.setLocy(element[1]);
				spawnDat.setLocz(element[2]);
				spawnDat.setHeading(element[3]);
				spawnDat.setRespawnDelay(60);
				spawnDat.setLocation(0);
				SpawnTable.getInstance().addNewSpawn(spawnDat);
				_teleportCubeSpawn.add(spawnDat);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		int status = GrandBossManager.getInstance().getBossStatus(ANTHARASOLDID);
		if(status == WAITING)
		{
			StatsSet info = GrandBossManager.getInstance().getStatsSet(ANTHARASOLDID);
			Long respawnTime = info.getLong("respawn_time");
			if(status == DEAD && respawnTime <= System.currentTimeMillis())
			{
				// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// also, the status needs to be changed to DORMANT
				GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
				status = DORMANT;
			}
			else if(status == FIGHTING)
			{
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(ANTHARASOLDID, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if(status == DEAD)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(ANTHARASOLDID), respawnTime - System.currentTimeMillis());
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			int statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARASWEAKID);
			int statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARASNORMALID);
			int statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARASSTRONGID);
			int antharasId = 0;
			if(statusWeak == FIGHTING || statusWeak == DEAD)
			{
				antharasId = ANTHARASWEAKID;
				status = statusWeak;
			}
			else if(statusNormal == FIGHTING || statusNormal == DEAD)
			{
				antharasId = ANTHARASNORMALID;
				status = statusNormal;
			}
			else if(statusStrong == FIGHTING || statusStrong == DEAD)
			{
				antharasId = ANTHARASSTRONGID;
				status = statusStrong;
			}
			if(antharasId != 0 && status == FIGHTING)
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				int loc_x = info.getInteger("loc_x");
				int loc_y = info.getInteger("loc_y");
				int loc_z = info.getInteger("loc_z");
				int heading = info.getInteger("heading");
				int hp = info.getInteger("currentHP");
				int mp = info.getInteger("currentMP");
				_antharas = (L2GrandBossInstance) addSpawn(antharasId, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_LastAction = System.currentTimeMillis();
				// Start repeating timer to check for inactivity
				_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if(antharasId != 0 && status == DEAD)
			{
				StatsSet info = GrandBossManager.getInstance().getStatsSet(antharasId);
				Long respawnTime = info.getLong("respawn_time");
				if(respawnTime <= System.currentTimeMillis())
				{
					// the time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// also, the status needs to be changed to DORMANT
					GrandBossManager.getInstance().setBossStatus(antharasId, DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(antharasId), respawnTime - System.currentTimeMillis());
				}
			}
		}
	}

	// Do spawn teleport cube.
	public void spawnCube()
	{
		if(_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if(_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if(_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}

		_teleportCube.addAll(_teleportCubeSpawn.stream().map(L2Spawn::doSpawn).collect(Collectors.toList()));
	}

	// Setting Antharas spawn task.
	public void setAntharasSpawnTask()
	{
		if(_monsterSpawnTask == null)
		{
			synchronized(this)
			{
				if(_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, WAITING);
					_monsterSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(1), Config.ANTHARAS_WAIT_TIME);
				}
			}
		}
	}

	private void startMinionSpawns(int antharasId)
	{
		int intervalOfMobs;

		// Interval of minions is decided by the type of Antharas
		// that invaded the lair.
		switch(antharasId)
		{
			case ANTHARASWEAKID:
				intervalOfMobs = FWA_INTERVALOFMOBSWEAK;
				break;
			case ANTHARASNORMALID:
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			default:
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
		}

		// Spawn mobs.
		_mobsSpawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new MobsSpawn(), intervalOfMobs, intervalOfMobs);
	}

	protected void broadcastPacket(L2GameServerPacket mov)
	{
		if(_Zone != null)
		{
			_Zone.getCharactersInside().stream().filter(characters -> characters instanceof L2PcInstance).forEach(characters -> characters.sendPacket(mov));
		}
	}

	// Clean Antharas's lair.
	public void setUnspawn()
	{
		// Eliminate players.
		_Zone.oustAllPlayers();

		// Not executed tasks is canceled.
		if(_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if(_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if(_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if(_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if(_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if(_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if(_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if(_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}

		// Delete monsters.
		for(L2Npc mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.getLocationController().delete();
		}
		_monsters.clear();

		// Delete teleport cube.
		for(L2Npc cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.getLocationController().delete();
		}
		_teleportCube.clear();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			_LastAction = System.currentTimeMillis();
			if(GrandBossManager.getInstance().getBossStatus(_antharas.getNpcId()) != FIGHTING)
			{
				_Zone.oustAllPlayers();
			}
			else if(_mobsSpawnTask == null)
			{
				startMinionSpawns(npc.getNpcId());
			}
		}
		else if(npc.getNpcId() > 29069 && npc.getNpcId() < 29077 && npc.getCurrentHp() <= damage)
		{
			SkillHolder skill = null;
			switch(npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = new SkillHolder(5097, 1);
					break;
				case 29076:
					skill = new SkillHolder(5094, 1);
					break;
			}

			npc.doCast(skill.getSkill());
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("waiting"))
		{
			setAntharasSpawnTask();
		}

		return super.onAdvEvent(event, npc, player);
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == 29019 || npc.getNpcId() == 29066 || npc.getNpcId() == 29067 || npc.getNpcId() == 29068)
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getNpcId(), DEAD);
			long respawnTime = (long) Config.INTERVAL_OF_ANTHARAS_SPAWN + Rnd.get(Config.RANDOM_OF_ANTHARAS_SPAWN);
			ThreadPoolManager.getInstance().scheduleGeneral(new UnlockAntharas(npc.getNpcId()), respawnTime);
			// also save the respawn time so that the info is maintained past reboots
			StatsSet info = GrandBossManager.getInstance().getStatsSet(npc.getNpcId());
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(npc.getNpcId(), info);
		}
		else if(npc.getNpcId() == 29069)
		{
			int countHPHerb = Rnd.get(6, 18);
			int countMPHerb = Rnd.get(6, 18);
			for(int i = 0; i < countHPHerb; i++)
			{
				((L2MonsterInstance) npc).dropItem(killer, 8602, 1);
			}
			for(int i = 0; i < countMPHerb; i++)
			{
				((L2MonsterInstance) npc).dropItem(killer, 8605, 1);
			}
		}
		if(_monsters.contains(npc))
		{
			_monsters.remove(npc);
		}
		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc.isInvul())
		{
			return null;
		}
		if(skill != null && (skill.getId() == 5097 || skill.getId() == 5094))
		{
			switch(npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
					npc.doDie(npc);
					break;
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		switch(npc.getNpcId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
				if(_selfDestructionTask == null && !npc.isDead())
				{
					_selfDestructionTask = ThreadPoolManager.getInstance().scheduleGeneral(new SelfDestructionOfBomber(npc), FWA_SELFDESTRUCTTIME);
				}
				break;
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}

	// UnLock Antharas.
	private static class UnlockAntharas implements Runnable
	{
		private final int _bossId;

		public UnlockAntharas(int bossId)
		{
			_bossId = bossId;
		}

		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId, DORMANT);
			if(FWA_DOSERVEREARTHQUAKE)
			{
				for(L2PcInstance p : WorldManager.getInstance().getAllPlayersArray())
				{
					p.broadcastPacket(new EarthQuake(185708, 114298, -8221, 20, 10));
				}
			}
		}
	}

	// Move at random on after Antharas appears.
	private static class MoveAtRandom implements Runnable
	{
		private final L2Npc _npc;
		private final Location _pos;

		public MoveAtRandom(L2Npc npc, Location pos)
		{
			_npc = npc;
			_pos = pos;
		}

		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}

	// Do spawn Antharas.
	private class AntharasSpawn implements Runnable
	{
		private final Collection<L2Character> _players = _Zone.getCharactersInside();
		private int _taskId;

		public AntharasSpawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			int npcId;
			L2Spawn antharasSpawn = null;

			switch(_taskId)
			{
				case 1: // Spawn.
					// Strength of Antharas is decided by the number of players that
					// invaded the lair.
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if(FWA_OLDANTHARAS)
					{
						npcId = 29019; // old
					}
					else if(_players.size() <= FWA_LIMITOFWEAK)
					{
						npcId = 29066; // weak
					}
					else
					{
						npcId = _players.size() > FWA_LIMITOFNORMAL ? 29068 : 29067;
					}

					// Do spawn.
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (L2GrandBossInstance) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);

					_monsters.add(_antharas);
					_antharas.setIsImmobilized(true);

					GrandBossManager.getInstance().setBossStatus(ANTHARASOLDID, DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId, FIGHTING);
					_LastAction = System.currentTimeMillis();
					// Start repeating timer to check for inactivity
					_activityCheckTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new CheckActivity(), 60000, 60000);

					// Setting 1st time of minions spawn task.
					startMinionSpawns(npcId);

					// Set next task.
					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(2), 16);
					break;
				case 2:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, -19, 0, 20000, 0, 0, 1, 0));

					// Set next task.
					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(3), 3000);
					break;

				case 3:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, 0, 6000, 20000, 0, 0, 1, 0));
					// Set next task.
					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(4), 10000);
					break;
				case 4:
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 3700, 0, -3, 0, 10000, 0, 0, 1, 0));
					// Set next task.
					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(5), 200);
					break;

				case 5:
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 22000, 30000, 0, 0, 1, 0));
					// Set next task.
					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(6), 10800);
					break;

				case 6:
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 300, 7000, 0, 0, 1, 0));
					// Set next task.
					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPoolManager.getInstance().scheduleGeneral(new AntharasSpawn(7), 1900);
					break;

				case 7:
					_antharas.abortCast();

					_mobiliseTask = ThreadPoolManager.getInstance().scheduleGeneral(new SetMobilised(_antharas), 16);

					// Move at random.
					if(FWA_MOVEATRANDOM)
					{
						Location pos = new Location(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPoolManager.getInstance().scheduleGeneral(new MoveAtRandom(_antharas, pos), 500);
					}

					if(_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
			}
		}
	}

	// Do spawn Behemoth or Bomber.
	private class MobsSpawn implements Runnable
	{
		public MobsSpawn()
		{
		}

		@Override
		public void run()
		{
			L2NpcTemplate template1;
			L2Spawn tempSpawn;
			boolean isBehemoth = Rnd.getChance(FWA_PERCENTOFBEHEMOTH);
			try
			{
				int mobNumber = isBehemoth ? 2 : 3;
				// Set spawn.
				for(int i = 0; i < mobNumber; i++)
				{
					if(_monsters.size() >= FWA_MAXMOBS)
					{
						break;
					}
					int npcId;
					npcId = isBehemoth ? 29069 : Rnd.get(29070, 29076);
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new L2Spawn(template1);
					// allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = (_antharas.getX() - x) * (_antharas.getX() - x) + (_antharas.getY() - y) * (_antharas.getY() - y);
					while(tried++ < 25 && notFound)
					{
						int rx = Rnd.get(175000, 179900);
						int ry = Rnd.get(112400, 116000);
						int rdt = (_antharas.getX() - rx) * (_antharas.getX() - rx) + (_antharas.getY() - ry) * (_antharas.getY() - ry);
						if(GeoEngine.getInstance().canSeeTarget(_antharas.getX(), _antharas.getY(), -7704, rx, ry, -7704, _antharas.getInstanceId()))
						{
							if(rdt < dt)
							{
								x = rx;
								y = ry;
								dt = rdt;
								if(rdt <= 900000)
								{
									notFound = false;
								}
							}
						}
					}
					tempSpawn.setLocx(x);
					tempSpawn.setLocy(y);
					tempSpawn.setLocz(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS << 1);
					SpawnTable.getInstance().addNewSpawn(tempSpawn);
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, e.getMessage(), e);
			}
		}
	}

	// Do self destruction.
	private class SelfDestructionOfBomber implements Runnable
	{
		private final L2Npc _bomber;

		public SelfDestructionOfBomber(L2Npc bomber)
		{
			_bomber = bomber;
		}

		@Override
		public void run()
		{
			SkillHolder skill = null;
			switch(_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
					skill = new SkillHolder(5097, 1);
					break;
				case 29076:
					skill = new SkillHolder(5094, 1);
					break;
			}

			_bomber.doCast(skill.getSkill());
			if(_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}
		}
	}

	// At end of activity time.
	protected class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			Long temp = System.currentTimeMillis() - _LastAction;
			if(temp > FWA_INACTIVITYTIME)
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getNpcId(), DORMANT);
				setUnspawn();
			}
		}
	}

	// Do spawn teleport cube.
	private class CubeSpawn implements Runnable
	{
		private final int _type;

		public CubeSpawn(int type)
		{
			_type = type;
		}

		@Override
		public void run()
		{
			if(_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPoolManager.getInstance().scheduleGeneral(new CubeSpawn(1), 1800000);
			}
			else
			{
				setUnspawn();
			}
		}
	}

	// Action is enabled the boss.
	private class SetMobilised implements Runnable
	{
		private final L2GrandBossInstance _boss;

		public SetMobilised(L2GrandBossInstance boss)
		{
			_boss = boss;
		}

		@Override
		public void run()
		{
			_boss.setIsImmobilized(false);

			// When it is possible to act, a social action is canceled.
			if(_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}
}