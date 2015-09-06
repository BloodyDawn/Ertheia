/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.actor.instance;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.knownlist.MonsterKnownList;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.util.MinionList;
import dwo.gameserver.util.Rnd;

import java.util.concurrent.ScheduledFuture;

/**
 * This class manages all Monsters.
 * L2MonsterInstance :<BR>
 * <BR>
 * <li>L2MinionInstance</li> <li>L2RaidBossInstance</li> <li>L2GrandBossInstance
 * </li>
 */

public class L2MonsterInstance extends L2Attackable
{
	private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
	protected ScheduledFuture<?> _maintenanceTask;
	private boolean _enableMinions = true;
	private L2MonsterInstance _master;
	private MinionList _minionList;
	private L2Transformation _transformation;

	/**
	 * Constructor of L2MonsterInstance (use L2Character and L2NpcInstance
	 * constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the _template of the
	 * L2MonsterInstance (copy skills from template to object and link
	 * _calculators to NPC_STD_CALCULATOR)</li> <li>Set the name of the
	 * L2MonsterInstance</li> <li>Create a RandomAnimation Task that will be
	 * launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 *
	 * @param objectId      Identifier of the object to initialized
	 * @param template Template to apply to the NPC
	 */
	public L2MonsterInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
		setAutoAttackable(true);
	}

	@Override
	public MonsterKnownList getKnownList()
	{
		return (MonsterKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new MonsterKnownList(this));
	}

	@Override
	public void onSpawn()
	{
		if(!isTeleporting())
		{
			if(_master != null)
			{
				setIsNoRndWalk(true);
				setIsRaidMinion(_master.isRaid());
				_master.getMinionList().onMinionSpawn(this);
			}

			// delete spawned minions before dynamic minions spawned by script
			if(hasMinions())
			{
				getMinionList().onMasterSpawn();
			}

			startMaintenanceTask();
		}

		// dynamic script-based minions spawned here, after all preparations.
		super.onSpawn();
	}

	@Override
	public L2MonsterInstance getLeader()
	{
		return _master;
	}

	public void setLeader(L2MonsterInstance leader)
	{
		_master = leader;
	}

	/**
	 * Return True if the L2MonsterInstance is Agressive (aggroRange > 0).<BR>
	 * <BR>
	 */
	@Override
	public boolean isAggressive()
	{
		return getAggroRange() > 0 && !isEventMob;
	}

	/**
	 * Return True if the attacker is not another L2MonsterInstance.<BR>
	 * <BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return super.isAutoAttackable(attacker) && !isEventMob;
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}

		if(_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false); // doesn't do it?
			_maintenanceTask = null;
		}

		return true;
	}

	@Override
	public boolean onDelete()
	{
		if(_maintenanceTask != null)
		{
			_maintenanceTask.cancel(false);
			_maintenanceTask = null;
		}

		if(hasMinions())
		{
			getMinionList().onMasterDie(true);
		}

		if(_master != null)
		{
			_master.getMinionList().onMinionDie(this, 0);
		}

		return super.onDelete();
	}

	protected int getMaintenanceInterval()
	{
		return MONSTER_MAINTENANCE_INTERVAL;
	}

	/**
	 * Spawn all minions at a regular interval
	 */
	protected void startMaintenanceTask()
	{
		// maintenance task now used only for minions spawn
		if(getTemplate().getMinionData() == null || getTemplate().getMinionData().isEmpty())
		{
			return;
		}

		if(_maintenanceTask == null)
		{
			_maintenanceTask = ThreadPoolManager.getInstance().scheduleGeneral(() -> {
				if(_enableMinions)
				{
					getMinionList().spawnMinions();
				}
			}, getMaintenanceInterval() + Rnd.get(1000));
		}
	}

	public void enableMinions(boolean b)
	{
		_enableMinions = b;
	}

	public boolean hasMinions()
	{
		return _minionList != null;
	}

	public MinionList getMinionList()
	{
		synchronized(this)
		{
			if(_minionList == null)
			{
				_minionList = new MinionList(this);
			}
		}

		return _minionList;
	}

	@Override
	public boolean isTransformed()
	{
		return _transformation != null && !_transformation.isStance();
	}

	@Override
	public void untransform(boolean removeEffects)
	{
		synchronized(this)
		{
			if(_transformation != null)
			{
				_transformation.onUntransform();
				_transformation = null;
				if(removeEffects)
				{
					stopEffects(L2EffectType.TRANSFORMATION);
				}

				broadcastPacket(new NpcInfo(this));
			}
		}
	}

	@Override
	public void onTeleported()
	{
		super.onTeleported();

		if(hasMinions())
		{
			getMinionList().onMasterTeleported();
		}
	}

	public void transform(L2Transformation transformation)
	{
		if(_transformation != null)
		{
			return;
		}

		_transformation = transformation;
		transformation.onTransform();
		broadcastPacket(new NpcInfo(this));
	}

	public L2Transformation getTransformation()
	{
		return _transformation;
	}

	/**
	 * @return Transformation Id
	 */
	public int getTransformationId()
	{
		return _transformation == null ? 0 : _transformation.getId();
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}
}
