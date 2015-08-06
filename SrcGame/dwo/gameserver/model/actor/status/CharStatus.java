package dwo.gameserver.model.actor.status;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.stat.CharStat;
import dwo.gameserver.model.skills.base.formulas.calculations.HpMpCpRegen;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.util.Rnd;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.concurrent.Future;

public class CharStatus
{
	protected static final Logger _log = LogManager.getLogger(CharStatus.class);
	protected static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	protected byte _flagsRegenActive;
	private L2Character _activeChar;
	private double _currentHp; //Current HP of the L2Character
	private double _currentMp; //Current MP of the L2Character
	/**
	 * Array containing all clients that need to be notified about hp/mp updates of the L2Character
	 */
	private Set<L2Character> _StatusListener;
	private Future<?> _regTask;

	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}

	/**
	 * Add the object to the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Target a PC or NPC</li><BR><BR>
	 *
	 * @param object L2Character to add to the listener
	 */
	public void addStatusListener(L2Character object)
	{
		if(object.equals(getActiveChar()))
		{
			return;
		}

		getStatusListener().add(object);
	}

	/**
	 * Remove the object from the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 * <p/>
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Untarget a PC or NPC</li><BR><BR>
	 *
	 * @param object L2Character to add to the listener
	 */
	public void removeStatusListener(L2Character object)
	{
		getStatusListener().remove(object);
	}

	/**
	 * Return the list of L2Character that must be informed of HP/MP updates of this L2Character.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.
	 * Players who must be informed are players that target this L2Character.
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<BR><BR>
	 *
	 * @return The list of L2Character to inform or null if empty
	 */
	public Set<L2Character> getStatusListener()
	{
		if(_StatusListener == null)
		{
			_StatusListener = new FastSet<L2Character>().shared();
		}
		return _StatusListener;
	}

	// place holder, only PcStatus has CP

	public void reduceCp(int value)
	{
	}

	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable : Set overhit values</li><BR>
	 * <li> L2Npc : Update the attacker AggroInfo of the L2Attackable _aggroList and clear duel status of the attacking players</li><BR><BR>
	 * @param value
	 * @param attacker
	 */
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}

	public void reduceHp(double value, L2Character attacker, boolean isHpConsumption)
	{
		reduceHp(value, attacker, true, false, isHpConsumption);
	}

	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if(getActiveChar().isDead())
		{
			return;
		}

		// invul handling
		if(getActiveChar().isInvul() && !(isDOT || isHPConsumption))
		{
			return;
		}

		if(attacker != null)
		{
			L2PcInstance attackerPlayer = attacker.getActingPlayer();
			if(attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
			{
				return;
			}
		}

		if(!isDOT && !isHPConsumption)
		{
			if(getActiveChar().hasBuffsRemovedOnDamage())
			{
				getActiveChar().stopEffectsOnDamage(awake);
			}
			if(getActiveChar().isStunned() && Rnd.get(10) == 0)
			{
				getActiveChar().stopStunning(true);
			}
		}

		if(value > 0) // Reduce Hp if any, and Hp can't be negative
		{
			double realDamage = _currentHp >= value ? value : _currentHp;
			setCurrentHp(Math.max(_currentHp - value, 0));

			// Обновляем значения статистики для возможных действующих игроков
			if(attacker instanceof L2PcInstance && attacker.isAwakened())
			{
				if(getActiveChar() instanceof L2PcInstance)
				{
					if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
					{
						attacker.getActingPlayer().updateWorldStatistic(CategoryType.DAMAGE_TO_PC, null, (long) value);
					}
				}
			}
			if(getActiveChar() instanceof L2PcInstance && getActiveChar().isAwakened())
			{
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					if(attacker instanceof L2PcInstance)
					{
						getActiveChar().getActingPlayer().updateWorldStatistic(CategoryType.DAMAGE_FROM_PC, null, (long) value);
					}
					else if(attacker instanceof L2Attackable)
					{
						getActiveChar().getActingPlayer().updateWorldStatistic(CategoryType.DAMAGE_FROM_MONSTERS, null, (long) value);
					}
				}
			}
			HookManager.getInstance().notifyEvent(HookType.ON_HP_CHANGED, _activeChar.getHookContainer(), _activeChar, realDamage, value);
		}

		if(getActiveChar().getCurrentHp() < 0.5) // Die
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			getActiveChar().doDie(attacker);
		}
	}

	public void reduceMp(double value)
	{
		setCurrentMp(Math.max(_currentMp - value, 0));
	}

	/**
	 * Start the HP/MP/CP Regeneration task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the regen task period </li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority </li><BR><BR>
	 */
	public void startHpMpRegeneration()
	{
		synchronized(this)
		{
			if(_regTask == null && !getActiveChar().isDead())
			{
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "HP/MP regen started");
				}

				// Get the Regeneration period
				int period = getActiveChar().getRegeneratePeriod();

				// Create the HP/MP/CP Regeneration task
				_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
			}
		}
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 */
	public void stopHpMpRegeneration()
	{
		synchronized(this)
		{
			if(_regTask != null)
			{
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "HP/MP regen stop");
				}

				// Stop the HP/MP/CP Regeneration task
				_regTask.cancel(false);
				_regTask = null;

				// Set the RegenActive flag to false
				_flagsRegenActive = 0;
			}
		}
	}

	// place holder, only PcStatus has CP

	public double getCurrentCp()
	{
		return 0;
	}

	// place holder, only PcStatus has CP

	public void setCurrentCp(double newCp)
	{
	}

	public double getCurrentHp()
	{
		return _currentHp;
	}

	public void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}

	public void setCurrentHp(double newHp, boolean broadcastPacket)
	{
		double realDamage = 0.0;

		// HP не падает ниже 1
		if(newHp < 1 && !getActiveChar().isMortal())
		{
			newHp = 1;
		}

		if(newHp < _currentHp)
		{
			realDamage = _currentHp - newHp;
		}

		// Get the Max HP of the L2Character
		double maxHp = getActiveChar().getStat().getMaxHp();

		synchronized(this)
		{
			if(getActiveChar().isDead())
			{
				return;
			}

			if(newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;

				// Stop the HP/MP/CP Regeneration task
				if(_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if(broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}

		if(_activeChar instanceof L2Playable)
		{
			HookManager.getInstance().notifyEvent(HookType.ON_HP_CHANGED, _activeChar.getHookContainer(), _activeChar, realDamage, realDamage);
		}
	}

	public void setCurrentHpMp(double newHp, double newMp)
	{
		setCurrentHp(newHp, false);
		setCurrentMp(newMp, true); //send the StatusUpdate only once
	}

	public double getCurrentMp()
	{
		return _currentMp;
	}

	public void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}

	public void setCurrentMp(double newMp, boolean broadcastPacket)
	{
		// Get the Max MP of the L2Character
		int maxMp = getActiveChar().getStat().getMaxMp();

		synchronized(this)
		{
			if(getActiveChar().isDead())
			{
				return;
			}

			if(newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;

				// Stop the HP/MP/CP Regeneration task
				if(_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;

				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}

		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if(broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}

	protected void doRegeneration()
	{
		CharStat charstat = getActiveChar().getStat();

		// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
		if(_currentHp < charstat.getMaxHp())
		{
			setCurrentHp(_currentHp + HpMpCpRegen.calcHpRegen(getActiveChar()), false);
		}

		// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
		if(_currentMp < charstat.getMaxMp())
		{
			setCurrentMp(_currentMp + HpMpCpRegen.calcMpRegen(getActiveChar()), false);
		}

		if(getActiveChar().isInActiveRegion())
		{
			getActiveChar().broadcastStatusUpdate(); //send the StatusUpdate packet
		}
		else
		{
			// no broadcast necessary for characters that are in inactive regions.
			// stop regeneration for characters who are filled up and in an inactive region.
			if(_currentHp == charstat.getMaxHp() && _currentMp == charstat.getMaxMp())
			{
				stopHpMpRegeneration();
			}
		}
	}

	public L2Character getActiveChar()
	{
		return _activeChar;
	}

	/**
	 * Task of HP/MP regeneration
	 */
	class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doRegeneration();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}
}
