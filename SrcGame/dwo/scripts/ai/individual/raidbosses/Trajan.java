package dwo.scripts.ai.individual.raidbosses;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.ai.individual.TrajanEgg;
import dwo.scripts.instances.RB_Trajan;
import javolution.util.FastList;

import java.util.List;
import java.util.concurrent.Future;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class Trajan extends Quest
{
	private static final int TRAJAN = 25785;

	private static final int[][] WALKING_ROUTES = {
		{175528, -186334, -3801}, {175259, -185337, -3785}, {175748, -184471, -3801}, {176522, -184277, -3785},
		{177048, -184824, -3801}, {176728, -186056, -3801},
	};

	private static final int[] TRAJAN_BEETLES = {18993, 18995};

	public Trajan()
	{
		addSpawnId(TRAJAN);
		addKillId(TRAJAN);
		addSkillSeeId(TRAJAN);
	}

	public static void main(String[] args)
	{
		new Trajan();
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		if(npc.getNpcId() == TRAJAN)
		{
			Instance instance = InstanceManager.getInstance().getInstance(npc.getInstanceId());

			if(instance != null)
			{
				instance.cancelTasks();
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if(npc.getNpcId() == TRAJAN && (skill.getAggroPoints() > 0 || skill.getSkillType() == L2SkillType.AGGDAMAGE) && Rnd.get() <= 0.1)
		{
			Instance instance = InstanceManager.getInstance().getInstance(npc.getInstanceId());
			if(instance != null)
			{
				for(int playerId : instance.getPlayers())
				{
					L2PcInstance player = WorldManager.getInstance().getPlayer(playerId);

					if(Util.calculateDistance(player, npc, false) < 900)
					{
						((L2RaidBossInstance) npc).addDamageHate(player, 999, 999);
						instance.cancelTask("trajanMovement");
						instance.addTask("trajanMovement", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TrajanWalkTask(npc), 120000, 120000));
					}
				}
			}
		}
		return null;
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		RB_Trajan.TrajanWorld world = InstanceManager.getInstance().getInstanceWorld(npc, RB_Trajan.TrajanWorld.class);

		if(world != null)
		{
			Instance instance = InstanceManager.getInstance().getInstance(world.instanceId);

			if(instance != null)
			{
				npc.setRunning();
				((L2RaidBossInstance) npc).setCanReturnToSpawnPoint(false);
				instance.addTask("trajanMovement", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TrajanWalkTask(npc), 0, 1000));

				Future<?> aggroTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
					if(instance != null)
					{
						for(int playerId : instance.getPlayers())
						{
							L2PcInstance player = WorldManager.getInstance().getPlayer(playerId);

							if(Util.calculateDistance(player, npc, false) < 2500)
							{
								instance.cancelTask("trajanMovement");
								instance.cancelTask("eggyWalkTask");
								instance.addTask("trajanMovement", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new TrajanWalkTask(npc), 60000, 1000));
								((L2RaidBossInstance) npc).addDamageHate(player, 999, 999);
								((L2RaidBossInstance) npc).attackCharacter(player);

								break;
							}
						}
					}
				}, 60000, 120000);

				Future<?> doomTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(() -> {
					for(int playerId : instance.getPlayers())
					{
						L2PcInstance player = WorldManager.getInstance().getPlayer(playerId);

						if(Util.calculateDistance(player, npc, false) < 2500)
						{
							// Кастуем ультимейт с шансом 1/20 при условии, что Траджан движется под землей
							if(Rnd.get() <= 0.1 && !npc.isAttackingNow() && !npc.isCastingNow())
							{
								instance.cancelTask("trajanMovement");
								instance.cancelTask("eggyWalkTask");
								npc.setTarget(player);
								npc.doCast(SkillTable.getInstance().getInfo(6268, 1));
								instance.addTask("trajanMovement", ThreadPoolManager.getInstance().scheduleGeneral(new TrajanWalkTask(npc), 10000));
							}
							// Кастуем "лужу" на произвольного игрока с шансом 1/5
							else if(Rnd.get() <= 0.2)
							{
								L2Npc doom = addSpawn(18998, player.getX(), player.getY(), player.getZ(), 0, false, 30, false, world.instanceId);
								doom.setTarget(player);
								doom.doCast(SkillTable.getInstance().getInfo(14113, 1));
								ThreadPoolManager.getInstance().scheduleGeneral(() -> {
									doom.setDisplayEffect(0x02);
									doom.setDisplayEffect(0x00);
									doom.getLocationController().delete();
								}, 15000);
							}
						}
					}
				}, 30000, 10000);
				instance.addTask("aggroTask", aggroTask);
				instance.addTask("doomTask", doomTask);
			}
		}

		return "";
	}

	class TrajanWalkTask implements Runnable
	{
		private final L2Npc _trajan;
		private int _currentRoute;
		private List<L2Npc> _minions = new FastList<>();
		private Future<?> _eggyTask;

		public TrajanWalkTask(L2Npc trajan)
		{
			_trajan = trajan;
			eggy();
		}

		public void eggy()
		{
			Instance instance = InstanceManager.getInstance().getInstance(_trajan.getInstanceId());

			if(instance == null)
			{
				return;
			}

			byte spawned = 0;
			for(L2Npc npc : instance.getNpcs())
			{
				if(Util.calculateDistance(_trajan, npc, false) < 2000 && npc.getNpcId() == TrajanEgg.TRAJAN_RAID_EGG && spawned < 2 && _minions.size() < 16)
				{
					L2Npc eggy = addSpawn(TRAJAN_BEETLES[Rnd.get(TRAJAN_BEETLES.length)], npc.getX(), npc.getY(), npc.getZ(), 0, false, 0, false, npc.getInstanceId());
					eggy.setRunning();
					_minions.add(eggy);
					++spawned;
				}
			}

			if(!_minions.isEmpty())
			{
				instance.addTask("eggyWalkTask", ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new EggyWalkTask(), 0, 1000));
			}
		}

		class EggyWalkTask implements Runnable
		{
			@Override
			public void run()
			{
				synchronized(this)
				{
					if(_minions.isEmpty())
					{
						_eggyTask.cancel(true);
						_eggyTask = null;
					}

					_minions.stream().filter(minion -> minion != null && !minion.isAttackingNow() && !minion.isCastingNow()).forEach(minion -> minion.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_trajan.getX() + Rnd.get(-50, 50), _trajan.getY() + Rnd.get(-50, 50), _trajan.getZ())));
				}
			}
		}

		@Override
		public void run()
		{
			if(_trajan == null || _trajan.getAI().getIntention() == CtrlIntention.AI_INTENTION_MOVE_TO)
			{
				return;
			}

			_trajan.setRunning();

			int[] route = WALKING_ROUTES[_currentRoute];

			_trajan.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(route[0], route[1], route[2]));

			++_currentRoute;
			if(_currentRoute > WALKING_ROUTES.length - 1)
			{
				_currentRoute = 0;
			}
		}
	}
}