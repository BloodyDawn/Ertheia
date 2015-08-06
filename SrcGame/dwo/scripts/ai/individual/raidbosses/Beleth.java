package dwo.scripts.ai.individual.raidbosses;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.DoorStatusUpdate;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.SpecialCamera;
import dwo.gameserver.network.game.serverpackets.StaticObject;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

/**
 * Beleth's AI.
 * @author Treat
 */

public class Beleth extends Quest
{
	protected static L2Npc camera;
	protected static L2Npc camera2;
	protected static L2Npc camera3;
	protected static L2Npc camera4;
	protected static L2Npc beleth;
	protected static L2Npc priest;
	protected static L2ZoneType _zone;
	protected static boolean movie;
	protected static ScheduledFuture<?> spawnTimer;
	protected static List<L2Npc> minions = new ArrayList<>();
	private static L2PcInstance belethKiller;
	private static boolean debug;
	private static boolean attacked;
	private static int allowObjectId;
	private static int killed;
	private static SkillHolder Bleed = new SkillHolder(5495, 1);
	private static SkillHolder Fireball = new SkillHolder(5496, 1);
	private static SkillHolder HornOfRising = new SkillHolder(5497, 1);
	private static SkillHolder Lightening = new SkillHolder(5499, 1);

	public Beleth()
	{
		_zone = ZoneManager.getInstance().getZoneById(12018);
		addEnterZoneId(12018);
		registerMobs(new int[]{
			29118, 29119
		});
		addStartNpc(32470);
		addTalkId(32470);
		addFirstTalkId(29128);
		StatsSet info = GrandBossManager.getInstance().getStatsSet(29118);
		int status = GrandBossManager.getInstance().getBossStatus(29118);
		if(status == 3)
		{
			long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			if(temp > 0)
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new unlock(), temp);
			}
			else
			{
				GrandBossManager.getInstance().setBossStatus(29118, 0);
			}
		}
		else if(status != 0)
		{
			GrandBossManager.getInstance().setBossStatus(29118, 0);
		}
		DoorGeoEngine.getInstance().getDoor(20240001).openMe();
	}

	protected static L2Npc spawn(int npcId, int[] loc, int instanceId)
	{
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if(template != null)
			{
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(loc[3]);
				spawn.setLocx(loc[0]);
				spawn.setLocy(loc[1]);
				spawn.setLocz(loc[2] + 20);
				spawn.setAmount(spawn.getAmount() + 1);
				return spawn.doSpawn();
			}
		}
		catch(Exception ignored)
		{
		}
		return null;
	}

	public static void startSpawnTask()
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(1), debug ? 10000 : 300000);
	}

	private static void deleteAll()
	{
		if(minions != null && !minions.isEmpty())
		{
			for(L2Npc npc : minions)
			{
				if(npc == null || npc.isDead())
				{
					continue;
				}
				npc.abortCast();
				npc.setTarget(null);
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				npc.getLocationController().delete();
			}
		}
		minions.clear();
		allowObjectId = 0;
		attacked = false;
	}

	private static void SpawnBeleths()
	{
		int a = 0;
		L2Npc npc;
		for(int i = 0; i < 16; i++)
		{
			a++;
			int x = (int) (650 * Math.cos(i * 0.39) + 16323);
			int y = (int) (650 * Math.sin(i * 0.39) + 213170);
			npc = spawn(29119, new int[]{
				x, y, -9357, 49152
			}, 0);
			minions.add(npc);
			if(a >= 2)
			{
				npc.setIsOverloaded(true);
				a = 0;
			}
		}
		int[] xm = new int[16];
		int[] ym = new int[16];
		for(int i = 0; i < 4; i++)
		{
			xm[i] = (int) (1700 * Math.cos(i * 1.57 + 0.78) + 16323);
			ym[i] = (int) (1700 * Math.sin(i * 1.57 + 0.78) + 213170);
			npc = spawn(29119, new int[]{
				xm[i], ym[i], -9357, 49152
			}, 0);
			npc.setIsOverloaded(true);
			minions.add(npc);
		}
		xm[4] = (xm[0] + xm[1]) / 2;
		ym[4] = (ym[0] + ym[1]) / 2;
		npc = spawn(29119, new int[]{
			xm[4], ym[4], -9357, 49152
		}, 0);
		npc.setIsOverloaded(true);
		minions.add(npc);
		xm[5] = (xm[1] + xm[2]) / 2;
		ym[5] = (ym[1] + ym[2]) / 2;
		npc = spawn(29119, new int[]{
			xm[5], ym[5], -9357, 49152
		}, 0);
		npc.setIsOverloaded(true);
		minions.add(npc);
		xm[6] = (xm[2] + xm[3]) / 2;
		ym[6] = (ym[2] + ym[3]) / 2;
		npc = spawn(29119, new int[]{
			xm[6], ym[6], -9357, 49152
		}, 0);
		npc.setIsOverloaded(true);
		minions.add(npc);
		xm[7] = (xm[3] + xm[0]) / 2;
		ym[7] = (ym[3] + ym[0]) / 2;
		npc = spawn(29119, new int[]{
			xm[7], ym[7], -9357, 49152
		}, 0);
		npc.setIsOverloaded(true);
		minions.add(npc);
		xm[8] = (xm[0] + xm[4]) / 2;
		ym[8] = (ym[0] + ym[4]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[8], ym[8], -9357, 49152
		}, 0));
		xm[9] = (xm[4] + xm[1]) / 2;
		ym[9] = (ym[4] + ym[1]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[9], ym[9], -9357, 49152
		}, 0));
		xm[10] = (xm[1] + xm[5]) / 2;
		ym[10] = (ym[1] + ym[5]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[10], ym[10], -9357, 49152
		}, 0));
		xm[11] = (xm[5] + xm[2]) / 2;
		ym[11] = (ym[5] + ym[2]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[11], ym[11], -9357, 49152
		}, 0));
		xm[12] = (xm[2] + xm[6]) / 2;
		ym[12] = (ym[2] + ym[6]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[12], ym[12], -9357, 49152
		}, 0));
		xm[13] = (xm[6] + xm[3]) / 2;
		ym[13] = (ym[6] + ym[3]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[13], ym[13], -9357, 49152
		}, 0));
		xm[14] = (xm[3] + xm[7]) / 2;
		ym[14] = (ym[3] + ym[7]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[14], ym[14], -9357, 49152
		}, 0));
		xm[15] = (xm[7] + xm[0]) / 2;
		ym[15] = (ym[7] + ym[0]) / 2;
		minions.add(spawn(29119, new int[]{
			xm[15], ym[15], -9357, 49152
		}, 0));
		allowObjectId = minions.get(Rnd.get(minions.size())).getObjectId();
		attacked = false;
	}

	public static void main(String[] args)
	{
		new Beleth();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance player, int damage, boolean isPet)
	{
		if(npc.getNpcId() == 29118 || npc.getNpcId() == 29119)
		{
			if(npc.getObjectId() == allowObjectId && !attacked)
			{
				attacked = true;
				L2Npc fakeBeleth = minions.get(Rnd.get(minions.size()));
				while(fakeBeleth.getObjectId() == allowObjectId)
				{
					fakeBeleth = minions.get(Rnd.get(minions.size()));
				}
				_zone.broadcastPacket(new Say2(fakeBeleth.getObjectId(), ChatType.ALL, fakeBeleth.getName(), "Miss text."));
			}
			if(Rnd.getChance(40))
			{
				return null;
			}
			double distance = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
			if(distance > 500 || Rnd.getChance(80))
			{
				minions.stream().filter(beleth -> beleth != null && !beleth.isDead() && Util.checkIfInRange(900, beleth, player, false) && !beleth.isCastingNow()).forEach(beleth -> {
					beleth.setTarget(player);
					beleth.doCast(Fireball.getSkill());
				});
				if(beleth != null && !beleth.isDead() && Util.checkIfInRange(900, beleth, player, false) && !beleth.isCastingNow())
				{
					beleth.setTarget(player);
					beleth.doCast(Fireball.getSkill());
				}
			}
			else if(npc != null && !npc.isDead() && !npc.isCastingNow())
			{
				if(!npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
				{
					npc.doCast(Lightening.getSkill());
					return null;
				}
				((L2Attackable) npc).clearAggroList();
			}
		}
		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc.getNpcId() == 29118 && player != null)
		{
			if(player.getParty() != null)
			{
				belethKiller = player.getParty().getCommandChannel() != null ? player.getParty().getCommandChannel().getLeader() : player.getParty().getLeader();
			}
			else
			{
				belethKiller = player;
			}
			GrandBossManager.getInstance().setBossStatus(29118, 3);
			long respawnTime = (long) Config.INTERVAL_OF_BELETH_SPAWN + Rnd.get(Config.RANDOM_OF_BELETH_SPAWN);
			StatsSet info = GrandBossManager.getInstance().getStatsSet(29118);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(29118, info);
			ThreadPoolManager.getInstance().scheduleGeneral(new unlock(), respawnTime);
			deleteAll();
			if(npc != null)
			{
				npc.getLocationController().delete();
			}
			movie = true;
			beleth = spawn(29118, new int[]{
				16323, 213170, -9357, 49152
			}, 0);
			beleth.setIsInvul(true);
			beleth.setIsImmobilized(true);
			beleth.disableAllSkills();
			priest = spawn(29128, new int[]{
				beleth.getX(), beleth.getY(), beleth.getZ(), beleth.getHeading()
			}, 0);
			priest.setShowSummonAnimation(true);
			priest.getLocationController().decay();
			spawn(32470, new int[]{
				12470, 215607, -9381, 49152
			}, 0);
			ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(27), 1000);
		}
		else if(npc.getNpcId() == 29119)
		{
			if(npc.getObjectId() == allowObjectId)
			{
				minions.remove(npc);
				killed++;
				if(killed >= 5)
				{
					deleteAll();
					spawnTimer = ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(333), 60000);
				}
				else
				{
					allowObjectId = minions.get(Rnd.get(minions.size())).getObjectId();
					attacked = false;
				}
			}
			else if(spawnTimer == null)
			{
				deleteAll();
				spawnTimer = ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(26), 60000);
				killed = 0;
			}
			npc.abortCast();
			npc.setTarget(null);
			npc.getLocationController().delete();
		}
		return null;
	}

	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		if(belethKiller != null && player.getObjectId() == belethKiller.getObjectId())
		{
			player.addItem(ProcessType.QUEST, 10314, 1, null, true);// giveItems(10314, 1, 0)
			belethKiller = null;
			return "<html><body>Stone Coffin:<br> You open the lid and are immediately flooded by a dark and gloomy aura. It appears that something is happening...</body></html>";
		}
		else
		{
			return "<html><body>Stone Coffin:<br> The lid doesn't budge - it seems to be held firmly in place by some unknown force. This coffin must be the object that Beleth bequeaths to the leader of the attacking forces.";
		}
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance player, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(npc != null && !npc.isDead() && (npc.getNpcId() == 29118 || npc.getNpcId() == 29119) && !npc.isCastingNow() && skill.getSkillType() == L2SkillType.HEAL && Rnd.getChance(80))
		{
			npc.setTarget(player);
			npc.doCast(HornOfRising.getSkill());
		}
		return null;
	}

	@Override
	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		if(npc != null && !npc.isDead() && (npc.getNpcId() == 29118 || npc.getNpcId() == 29119) && !npc.isCastingNow())
		{
			if(player != null && !player.isDead())
			{
				double distance2 = Math.sqrt(npc.getPlanDistanceSq(player.getX(), player.getY()));
				if(distance2 > 890 && !npc.isMovementDisabled())
				{
					npc.setTarget(player);
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, player);
					int speed = npc.isRunning() ? npc.getRunSpeed() : npc.getWalkSpeed();
					int time = (int) ((distance2 - 890) / speed * 1000);
					ThreadPoolManager.getInstance().scheduleGeneral(new Cast(Fireball.getSkill(), npc), time);

				}
				else if(distance2 < 890)
				{
					npc.setTarget(player);
					npc.doCast(Fireball.getSkill());
				}
				return null;
			}
			if(Rnd.getChance(40))
			{
				if(!npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
				{
					npc.doCast(Lightening.getSkill());
					return null;
				}
			}
			for(L2PcInstance plr : npc.getKnownList().getKnownPlayersInRadius(950))
			{
				npc.setTarget(plr);
				npc.doCast(Fireball.getSkill());
				return null;
			}
			((L2Attackable) npc).clearAggroList();
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(npc.getNpcId() == 29118 || npc.getNpcId() == 29119)
		{
			npc.setRunning();
			if(!movie && !npc.getKnownList().getKnownPlayersInRadius(300).isEmpty() && Rnd.getChance(60))
			{
				npc.doCast(Bleed.getSkill());
			}
			if(npc.getNpcId() == 29118)
			{
				npc.getSpawn().setRespawnDelay(0);// setOnKillDelay
			}
		}
		return null;
	}

	@Override
	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		if(npc != null && !npc.isDead() && (npc.getNpcId() == 29118 || npc.getNpcId() == 29119) && !npc.isCastingNow() && !movie)
		{
			if(Rnd.getChance(40))
			{
				if(!npc.getKnownList().getKnownPlayersInRadius(200).isEmpty())
				{
					npc.doCast(Bleed.getSkill());
					return null;
				}
			}
			npc.setTarget(player);
			npc.doCast(Fireball.getSkill());
		}
		return null;
	}

	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if(character instanceof L2PcInstance && GrandBossManager.getInstance().getBossStatus(29118) == 1 || debug && GrandBossManager.getInstance().getBossStatus(29118) != 2 && character instanceof L2PcInstance)
		{
			startSpawnTask();
			GrandBossManager.getInstance().setBossStatus(29118, 2);
		}
		return null;
	}

	protected static class unlock implements Runnable
	{
		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(29118, 0);
			DoorGeoEngine.getInstance().getDoor(20240001).openMe();
		}
	}

	private static class Cast implements Runnable
	{
		L2Skill _skill;
		L2Npc _npc;

		public Cast(L2Skill skill, L2Npc npc)
		{
			_skill = skill;
			_npc = npc;
		}

		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			if(_npc != null && !_npc.isDead() && !_npc.isCastingNow())
			{
				_npc.doCast(_skill);
			}
		}
	}

	private static class Spawn implements Runnable
	{
		private int _taskId;

		public Spawn(int taskId)
		{
			_taskId = taskId;
		}

		@Override
		public void run()
		{
			try
			{
				int instanceId = 0;
				switch(_taskId)
				{
					case 1:
						movie = true;
						_zone.getCharactersInside().stream().filter(npc -> npc instanceof L2Npc).forEach(npc -> npc.getLocationController().delete());
						camera = spawn(29120, new int[]{
							16323, 213142, -9357, 0
						}, instanceId);
						camera2 = spawn(29121, new int[]{
							16323, 210741, -9357, 0
						}, instanceId);
						camera3 = spawn(29122, new int[]{
							16323, 213170, -9357, 0
						}, instanceId);
						camera4 = spawn(29123, new int[]{
							16323, 214917, -9356, 0
						}, instanceId);
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 75, -25, 0, 2500, 0, 0, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 75, -25, 0, 2500, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(2), 300);
						break;
					case 2:
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 1800, -45, -45, 5000, 5000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(3), 4900);
						break;
					case 3:
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 2500, -120, -45, 5000, 5000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(4), 4900);
						break;
					case 4:
						_zone.broadcastPacket(new SpecialCamera(camera2.getObjectId(), 2200, 130, 0, 0, 1500, -20, 15, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(5), 1400);
						break;
					case 5:
						_zone.broadcastPacket(new SpecialCamera(camera2.getObjectId(), 2300, 100, 0, 2000, 4500, 0, 10, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(6), 2500);
						break;
					case 6:
						L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(20240001);
						door.closeMe();
						_zone.broadcastPacket(new StaticObject(door, false));
						_zone.broadcastPacket(new DoorStatusUpdate(door));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(7), 1700);
						break;
					case 7:
						_zone.broadcastPacket(new SpecialCamera(camera4.getObjectId(), 1500, 210, 0, 0, 1500, 0, 0, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera4.getObjectId(), 900, 255, 0, 5000, 6500, 0, 10, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(8), 6000);
						break;
					case 8:
						spawn(29125, new int[]{
							16323, 214917, -9356, 0
						}, instanceId);
						_zone.broadcastPacket(new SpecialCamera(camera4.getObjectId(), 900, 255, 0, 0, 1500, 0, 10, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(9), 1000);
						break;
					case 9:
						_zone.broadcastPacket(new SpecialCamera(camera4.getObjectId(), 1000, 255, 0, 7000, 17000, 0, 25, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(10), 3000);
						break;
					case 10:
						beleth = spawn(29118, new int[]{
							16321, 214211, -9352, 49369
						}, instanceId);
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(11), 200);
						break;
					case 11:
						_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), 1));
						for(int i = 0; i < 6; i++)
						{
							int x = (int) (150 * Math.cos(i * 1.046666667) + 16323);
							int y = (int) (150 * Math.sin(i * 1.046666667) + 213059);
							L2Npc minion = spawn(29119, new int[]{
								x, y, -9357, 49152
							}, beleth.getInstanceId());
							minion.setShowSummonAnimation(true);
							minion.getLocationController().decay();
							minions.add(minion);
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(12), 6800);
						break;
					case 12:
						_zone.broadcastPacket(new SpecialCamera(beleth.getObjectId(), 0, 270, -5, 0, 4000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(13), 3500);
						break;
					case 13:
						_zone.broadcastPacket(new SpecialCamera(beleth.getObjectId(), 800, 270, 10, 3000, 6000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(14), 5000);
						break;
					case 14:
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 100, 270, 15, 0, 5000, 0, 0, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 100, 270, 15, 0, 5000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(15), 100);
						break;
					case 15:
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 100, 270, 15, 3000, 6000, 0, 5, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(16), 1400);
						break;
					case 16:
						beleth.teleToLocation(16323, 213059, -9357, 49152, false);
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(17), 200);
						break;
					case 17:
						_zone.broadcastPacket(new MagicSkillUse(beleth, beleth, 5532, 1, 2000, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(18), 2000);
						break;
					case 18:
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 700, 270, 20, 1500, 8000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(19), 6900);
						break;
					case 19:
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 40, 260, 0, 0, 4000, 0, 0, 1, 0));
						for(L2Npc blth : minions)
						{
							blth.getLocationController().spawn();
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(20), 3000);
						break;
					case 20:
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 40, 280, 0, 0, 4000, 5, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(21), 3000);
						break;
					case 21:
						_zone.broadcastPacket(new SpecialCamera(camera3.getObjectId(), 5, 250, 5, 0, 13000, 20, 15, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(22), 1000);
						break;
					case 22:
						_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), 3));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(23), 4000);
						break;
					case 23:
						_zone.broadcastPacket(new MagicSkillUse(beleth, beleth, 5533, 1, 2000, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(24), 6800);
						break;
					case 24:
						beleth.getLocationController().delete();
						for(L2Npc bel : minions)
						{
							bel.getLocationController().delete();
						}
						minions.clear();
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(25), 1000);
						break;
					case 25:
						camera.getLocationController().delete();
						camera2.getLocationController().delete();
						camera3.getLocationController().delete();
						camera4.getLocationController().delete();
						movie = false;
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(26), 60000);
						break;
					case 26:
						if(spawnTimer != null)
						{
							spawnTimer.cancel(false);
							spawnTimer = null;
						}
						SpawnBeleths();
						break;
					case 27:
						beleth.doDie(null);
						camera = spawn(29122, new int[]{
							16323, 213170, -9357, 0
						}, instanceId);
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 290, 25, 0, 10000, 0, 0, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 290, 25, 0, 10000, 0, 0, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 110, 25, 4000, 10000, 0, 0, 1, 0));
						_zone.broadcastPacket(new SocialAction(beleth.getObjectId(), 5));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(28), 4000);
						break;
					case 28:
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 295, 25, 4000, 5000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(29), 4500);
						break;
					case 29:
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 400, 295, 10, 4000, 11000, 0, 25, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(30), 9000);
						break;
					case 30:
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 250, 90, 25, 0, 1000, 0, 0, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera.getObjectId(), 250, 90, 25, 0, 10000, 0, 0, 1, 0));
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(31), 2000);
						break;
					case 31:
						priest.getLocationController().spawn();
						beleth.getLocationController().delete();
						camera2 = spawn(29121, new int[]{
							14056, 213170, -9357, 0
						}, instanceId);
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(32), 3500);
						break;
					case 32:
						_zone.broadcastPacket(new SpecialCamera(camera2.getObjectId(), 800, 180, 0, 0, 4000, 0, 10, 1, 0));
						_zone.broadcastPacket(new SpecialCamera(camera2.getObjectId(), 800, 180, 0, 0, 4000, 0, 10, 1, 0));
						L2DoorInstance door2 = DoorGeoEngine.getInstance().getDoor(20240002);
						door2.openMe();
						_zone.broadcastPacket(new StaticObject(door2, false));
						_zone.broadcastPacket(new DoorStatusUpdate(door2));
						DoorGeoEngine.getInstance().getDoor(20240003).openMe();
						ThreadPoolManager.getInstance().scheduleGeneral(new Spawn(33), 4000);
						break;
					case 33:
						camera.getLocationController().delete();
						camera2.getLocationController().delete();
						movie = false;
						break;
					case 333:
						beleth = spawn(29118, new int[]{
							16323, 213170, -9357, 49152
						}, 0);
						break;

				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, getClass().getSimpleName() + ": Error while spawn! ", e);
			}
		}
	}
}
