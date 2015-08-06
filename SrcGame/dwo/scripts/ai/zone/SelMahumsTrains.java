package dwo.scripts.ai.zone;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.util.Rnd;
import javolution.util.FastList;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;
import java.util.Set;

public class SelMahumsTrains extends Quest
{
	//Sel Mahum Drill Sergeant, Sel Mahum Training Officer, Sel Mahum Drill Sergeant respectively
	private static final int[] MAHUM_CHIEFS = {22775, 22776, 22778};

	//Sel Mahum Recruit, Sel Mahum Recruit, Sel Mahum Soldier, Sel Mahum Recruit, Sel Mahum Soldier respectively 
	private static final int[] MAHUM_SOLDIERS = {22780, 22782, 22783, 22784, 22785};

	private static final int[] CHIEF_SOCIAL_ACTIONS = {1, 4, 5, 7};
	private static final int[] SOLDIER_SOCIAL_ACTIONS = {1, 5, 6, 7};

	private static final NpcStringId[] CHIEF_FSTRINGS = {
		NpcStringId.HOW_DARE_YOU_ATTACK_MY_RECRUITS, NpcStringId.WHO_IS_DISRUPTING_THE_ORDER
	};
	private static final NpcStringId[] SOLDIER_FSTRINGS = {
		NpcStringId.THE_DRILLMASTER_IS_DEAD, NpcStringId.LINE_UP_THE_RANKS
	};

	private static List<L2Spawn> _spawns = new FastList<>(); //all Mahum's spawns are stored here
	private static FastList<Integer> _scheduledReturnTasks = new FastList<>(); //Used to track scheduled Return Tasks

	public SelMahumsTrains()
	{

		addAttackId(MAHUM_CHIEFS);
		addKillId(MAHUM_CHIEFS);
		addSpawnId(MAHUM_CHIEFS);

		addSpawnId(MAHUM_SOLDIERS);

		//Send event to monsters, that was spawned through SpawnTable at server start (it is impossible to track first spawn)
		for(int npcId : MAHUM_CHIEFS)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				onSpawn(spawn.getLastSpawn());
				_spawns.add(spawn);
			}
		}
		for(int npcId : MAHUM_SOLDIERS)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				onSpawn(spawn.getLastSpawn());
				_spawns.add(spawn);
			}
		}
	}

	public static void main(String[] args)
	{
		new SelMahumsTrains();
	}

	@Override
	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		if(!npc.isDead() && !npc.isBusy())
		{
			if(Rnd.getChance(10))
			{
				npc.broadcastPacket(new NS(npc.getObjectId(), ChatType.NPC_ALL, npc.getNpcId(), CHIEF_FSTRINGS[Rnd.get(2)]));
			}

			npc.setBusy(true);
			startQuestTimer("reset_busy_state", 60000, npc, null);
		}

		return super.onAttack(npc, attacker, damage, isPet);
	}

	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(event.equalsIgnoreCase("do_social_action"))
		{
			if(npc != null && !npc.isDead())
			{
				if(!npc.isBusy() && npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && npc.getX() == npc.getSpawn().getLocx() && npc.getY() == npc.getSpawn().getLocy())
				{
					int idx = Rnd.get(6);
					if(idx <= CHIEF_SOCIAL_ACTIONS.length - 1)
					{
						npc.broadcastPacket(new SocialAction(npc.getObjectId(), CHIEF_SOCIAL_ACTIONS[idx]));

						L2ZoneType zone = getZone(npc);

						if(zone != null)
						{
							zone.getCharactersInside().stream().filter(ch -> ch != null && !ch.isDead() && ch.isMonster() && !((L2MonsterInstance) ch).isBusy() && ArrayUtils.contains(MAHUM_SOLDIERS, ((L2MonsterInstance) ch).getNpcId()) && ch.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE && ch.getX() == ((L2MonsterInstance) ch).getSpawn().getLocx() && ch.getY() == ((L2MonsterInstance) ch).getSpawn().getLocy()).forEach(ch -> ch.broadcastPacket(new SocialAction(ch.getObjectId(), SOLDIER_SOCIAL_ACTIONS[idx])));
						}
					}
				}

				startQuestTimer("do_social_action", 15000, npc, null);
			}
		}
		else if(event.equalsIgnoreCase("reset_busy_state"))
		{
			if(npc != null)
			{
				npc.setBusy(false);
				npc.disableCoreAI(false);
			}
		}

		return null;
	}

	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		L2ZoneType leaderZone = getZone(npc);

		if(leaderZone != null)
		{
			for(L2Spawn sp : _spawns)
			{
				if(sp == null)
				{
					continue;
				}
				L2MonsterInstance soldier = (L2MonsterInstance) sp.getLastSpawn();
				if(soldier != null && !soldier.isDead())
				{
					L2ZoneType soldierZone = getZone(soldier);
					if(soldierZone != null && leaderZone.getId() == soldierZone.getId())
					{
						if(Rnd.get(4) < 1)
						{
							soldier.broadcastPacket(new NS(soldier.getObjectId(), ChatType.ALL, soldier.getNpcId(), SOLDIER_FSTRINGS[Rnd.get(2)]));
						}

						soldier.setBusy(true);
						soldier.setIsRunning(true);
						soldier.clearAggroList();
						soldier.disableCoreAI(true);
						soldier.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(soldier.getX() + Rnd.get(-800, 800), soldier.getY() + Rnd.get(-800, 800), soldier.getZ(), soldier.getHeading()));
						startQuestTimer("reset_busy_state", 5000, soldier, null);
					}
				}
			}
			//Soldiers should return into spawn location, if they have "NO_DESIRE" state. It looks like AI_INTENTION_ACTIVE in L2J terms,
			//but we have no possibility to track AI intention change, so timer is used here. Time can be ajusted, if needed.
			if(!_scheduledReturnTasks.contains(leaderZone.getId())) //Check for shceduled task presence for this zone
			{
				_scheduledReturnTasks.add(leaderZone.getId()); //register scheduled task for zone
				ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTask(leaderZone.getId()), 120000); //schedule task
			}
		}

		return super.onKill(npc, killer, isPet);
	}

	@Override
	public String onSpawn(L2Npc npc)
	{
		if(!npc.isTeleporting())
		{
			if(ArrayUtils.contains(MAHUM_CHIEFS, npc.getNpcId()))
			{
				startQuestTimer("do_social_action", 15000, npc, null);
			}

			npc.disableCoreAI(false);
			npc.setBusy(false);
			npc.setIsNoRndWalk(true);
			npc.setIsNoAnimation(true);
		}

		return super.onSpawn(npc);
	}

	private L2ZoneType getZone(L2Npc npc)
	{
		L2ZoneType zone = null;

		try
		{
			L2Spawn spawn = npc.getSpawn();
			zone = ZoneManager.getInstance().getZones(spawn.getLocx(), spawn.getLocy(), spawn.getLocz()).get(0);
		}

		catch(NullPointerException | IndexOutOfBoundsException ignored)
		{
		}

		return zone;
	}

	/**
	 * Returns monsters in their spawn location
	 */
	private class ReturnTask implements Runnable
	{
		private final int _zoneId;
		private boolean _runAgain;

		public ReturnTask(int zoneId)
		{
			_zoneId = zoneId;
			_runAgain = false;
		}

		@Override
		public void run()
		{
			for(L2Spawn sp : _spawns)
			{
				L2MonsterInstance monster = (L2MonsterInstance) sp.getLastSpawn();

				if(monster != null && !monster.isDead())
				{
					L2ZoneType zone = getZone(monster);
					if(zone != null && zone.getId() == _zoneId)
					{
						if(monster.getX() != sp.getLocx() && monster.getY() != sp.getLocy()) //Check if there is monster not in spawn location
						{
							//Teleport him if not engaged in battle / not flee
							if(monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || monster.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
							{
								monster.setHeading(sp.getHeading());
								monster.teleToLocation(sp.getLocx(), sp.getLocy(), sp.getLocz());
							}
							else
							//There is monster('s) not in spawn location, but engaged in battle / flee. Set flag to repeat Return Task for this zone
							{
								_runAgain = true;
							}
						}
					}
				}
			}
			if(_runAgain) //repeat task
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new ReturnTask(_zoneId), 120000);
			}
			else if(_scheduledReturnTasks.contains(_zoneId))
			{
				// Task is not sheduled ahain for this zone, unregister it
				_scheduledReturnTasks.remove(_scheduledReturnTasks.indexOf(_zoneId));
			}
		}
	}
}
