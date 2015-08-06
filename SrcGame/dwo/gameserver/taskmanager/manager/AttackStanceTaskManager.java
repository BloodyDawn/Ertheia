package dwo.gameserver.taskmanager.manager;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2CubicInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.serverpackets.AutoAttackStop;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.Map;

public class AttackStanceTaskManager
{
	protected static final Logger _log = LogManager.getLogger(AttackStanceTaskManager.class);

	protected static final FastMap<L2Character, Long> _attackStanceTasks = new FastMap<>();

	private AttackStanceTaskManager()
	{
		_attackStanceTasks.shared();
		ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new FightModeScheduler(), 0, 1000);
	}

	/**
	 * Gets the single instance of AttackStanceTaskManager.
	 * @return single instance of AttackStanceTaskManager
	 */
	public static AttackStanceTaskManager getInstance()
	{
		return SingletonHolder._instance;
	}

	/**
	 * Adds the attack stance task.
	 * @param actor the actor
	 */
	public void addAttackStanceTask(L2Character actor)
	{
		if(actor != null && actor.isPlayable())
		{
			L2PcInstance player = actor.getActingPlayer();
			// TODO: Че это вообще за хуйня?! о_О
			player.getCubics().stream().filter(cubic -> cubic.getId() != L2CubicInstance.CUBIC_HEAL && cubic.getId() != L2CubicInstance.SKILL_CUBIC_HEALER).forEach(L2CubicInstance::doAction);
		}
		_attackStanceTasks.put(actor, System.currentTimeMillis());
	}

	/**
	 * Removes the attack stance task.
	 * @param actor the actor
	 */
	public void removeAttackStanceTask(L2Character actor)
	{
		if(actor != null && actor.isSummon())
		{
			actor = actor.getActingPlayer();
		}
		_attackStanceTasks.remove(actor);
	}

	/**
	 * Checks for attack stance task.
	 * @param actor the actor
	 * @return {@code true} if the character has an attack stance task, {@code false} otherwise
	 */
	public boolean hasAttackStanceTask(L2Character actor)
	{
		if(actor instanceof L2Summon)
		{
			L2Summon summon = (L2Summon) actor;
			actor = summon.getOwner();
		}
		return _attackStanceTasks.containsKey(actor);
	}

	private static class SingletonHolder
	{
		protected static final AttackStanceTaskManager _instance = new AttackStanceTaskManager();
	}

	protected class FightModeScheduler implements Runnable
	{
		@Override
		public void run()
		{
			long current = System.currentTimeMillis();
			try
			{
				Iterator<Map.Entry<L2Character, Long>> iter = _attackStanceTasks.entrySet().iterator();
				Map.Entry<L2Character, Long> e;
				L2Character actor;
				while(iter.hasNext())
				{
					e = iter.next();
					if(current - e.getValue() > 15000)
					{
						actor = e.getKey();
						if(actor != null)
						{
							actor.broadcastPacket(new AutoAttackStop(actor.getObjectId()));
							actor.getAI().setAutoAttacking(false);
							if(actor.isPlayer() && !actor.getActingPlayer().getPets().isEmpty())
							{
								for(L2Summon summon : actor.getActingPlayer().getPets())
								{
									summon.broadcastPacket(new AutoAttackStop(summon.getObjectId()));
								}
							}
						}
						if(actor instanceof L2PcInstance)
						{
							((L2PcInstance) actor).updateWorldStatistic(CategoryType.TIME_IN_BATTLE, null, (current - e.getValue()) / 1000);
						}
						iter.remove();
					}
				}
			}
			catch(Exception e)
			{
				// Unless caught here, players remain in attack positions.
				_log.log(Level.WARN, "Error in FightModeScheduler: " + e.getMessage(), e);
			}
		}
	}
}
