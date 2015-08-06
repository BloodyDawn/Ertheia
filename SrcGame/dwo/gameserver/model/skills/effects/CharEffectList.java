package dwo.gameserver.model.skills.effects;

import dwo.config.Config;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.handler.effects.DispelBySlot;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2IncarnationInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2EffectStopCond;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.olympiad.OlympiadGameTask;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.AbnormalStatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExAbnormalStatusUpdateFromTarget;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExOlympiadSpelledInfo;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySpelled;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class CharEffectList
{
	public static final int EFFECT_FLAG_CHARM_OF_COURAGE = 0x1;
	public static final int EFFECT_FLAG_CHARM_OF_LUCK = 0x2;
	public static final int EFFECT_FLAG_PHOENIX_BLESSING = 0x4;
	public static final int EFFECT_FLAG_NOBLESS_BLESSING = 0x8;
	public static final int EFFECT_FLAG_SILENT_MOVE = 0x10;
	public static final int EFFECT_FLAG_PROTECTION_BLESSING = 0x20;
	public static final int EFFECT_FLAG_RELAXING = 0x40;
	public static final int EFFECT_FLAG_FEAR = 0x80;
	public static final int EFFECT_FLAG_CONFUSED = 0x100;
	public static final int EFFECT_FLAG_MUTED = 0x200;
	public static final int EFFECT_FLAG_PSYCHICAL_MUTED = 0x400;
	//public static final int EFFECT_FLAG_PARALYZE = 2048;  //too much abuse in code
	public static final int EFFECT_FLAG_PSYCHICAL_ATTACK_MUTED = 0x800;
	public static final int EFFECT_FLAG_DISARMED = 0x1000;
	public static final int EFFECT_FLAG_ROOTED = 0x2000;
	public static final int EFFECT_FLAG_SLEEP = 0x4000;
	public static final int EFFECT_FLAG_STUNNED = 0x8000;
	public static final int EFFECT_FLAG_BETRAYED = 0x10000;
	public static final int EFFECT_FLAG_FLY_UP = 0x20000;
	public static final int EFFECT_FLAG_INVUL = 0x40000;
	public static final int EFFECT_FLAG_PARALYZED = 0x80000;
	public static final int EFFECT_FLAG_BLOCK_RESURRECTION = 0x100000;
	public static final int EFFECT_FLAG_BLOCK_SKILLS = 0x200000;
	public static final int EFFECT_FLAG_BLOCK_RECALL = 0x400000;
	public static final int EFFECT_FLAG_RESIST_SKILLID = 0x800000;
	public static final int EFFECT_FLAG_FEATHER_OF_BLESSING = 0x1000000;
	protected static final Logger _log = LogManager.getLogger(CharEffectList.class);
	private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
	private final AtomicBoolean queueLock = new AtomicBoolean();
	// Owner of this list
	private final L2Character _owner;
	private final Object _buildEffectLock = new Object();
	private FastList<L2Effect> _buffs;
	private FastList<L2Effect> _debuffs;
	private FastList<L2Effect> _passives; // They bypass most of the actions, keep in mind that those arent included in getAllEffects()
	// The table containing the List of all stacked effect in progress for each Stack group Identifier
	private Map<String, List<L2Effect>> _stackedEffects;
	private FastSet<String> _lockedStackTypes;
	private FastSet<L2EffectStopCond> _removedEffectList = new FastSet<>();
	private boolean _queuesInitialized;
	private LinkedBlockingQueue<L2Effect> _addQueue;
	private LinkedBlockingQueue<L2Effect> _removeQueue;
	private int _effectFlags;
	// only party icons need to be updated
	private boolean _partyOnly;
	private L2Effect[] _effectCache;
	private volatile boolean _rebuildCache = true;

	public CharEffectList(L2Character owner)
	{
		_owner = owner;
	}

	/**
	 * @return все эффекты на персонаже
	 */
	public L2Effect[] getAllEffects()
	{
		// If no effect is active, return EMPTY_EFFECTS
		if((_buffs == null || _buffs.isEmpty()) && (_debuffs == null || _debuffs.isEmpty()))
		{
			return EMPTY_EFFECTS;
		}

		synchronized(_buildEffectLock)
		{
			// If we dont need to rebuild the cache, just return the current one.
			if(!_rebuildCache)
			{
				return _effectCache;
			}

			_rebuildCache = false;

			// Create a copy of the effects
			FastList<L2Effect> temp = FastList.newInstance();

			// Add all buffs and all debuffs
			if(_buffs != null && !_buffs.isEmpty())
			{
				temp.addAll(_buffs);
			}
			if(_debuffs != null && !_debuffs.isEmpty())
			{
				temp.addAll(_debuffs);
			}

			// Return all effects in an array
			L2Effect[] tempArray = new L2Effect[temp.size()];
			temp.toArray(tempArray);
			return _effectCache = tempArray;
		}
	}

	/**
	 * @param tp
	 * @return the first effect matching the given EffectType
	 */
	public L2Effect getFirstEffect(L2EffectType tp)
	{
		L2Effect effectNotInUse = null;

		if(_buffs != null && !_buffs.isEmpty())
		{
			for(L2Effect e : _buffs)
			{
				if(e == null)
				{
					continue;
				}
				if(e.getEffectType() == tp)
				{
					if(e.isInUse())
					{
						return e;
					}
					else
					{
						effectNotInUse = e;
					}
				}
			}
		}
		if(effectNotInUse == null && _debuffs != null && !_debuffs.isEmpty())
		{
			for(L2Effect e : _debuffs)
			{
				if(e == null)
				{
					continue;
				}

				if(e.getEffectType() == tp)
				{
					if(e.isInUse())
					{
						return e;
					}
					else
					{
						effectNotInUse = e;
					}
				}
			}
		}
		return effectNotInUse;
	}

	/**
	 * @param skill
	 * @return the first effect matching the given L2Skill
	 */
	public L2Effect getFirstEffect(L2Skill skill)
	{
		L2Effect effectNotInUse = null;

		if(skill.isDebuff())
		{
			if(_debuffs != null && !_debuffs.isEmpty())
			{
				for(L2Effect e : _debuffs)
				{
					if(e == null)
					{
						continue;
					}

					if(e.getSkill().equals(skill))
					{
						if(e.isInUse())
						{
							return e;
						}
						else
						{
							effectNotInUse = e;
						}
					}
				}
			}
		}
		else
		{
			if(_buffs != null && !_buffs.isEmpty())
			{
				for(L2Effect e : _buffs)
				{
					if(e == null)
					{
						continue;
					}

					if(e.getSkill().equals(skill))
					{
						if(e.isInUse())
						{
							return e;
						}
						else
						{
							effectNotInUse = e;
						}
					}
				}
			}
		}

		return effectNotInUse;
	}

	/**
	 * @param skillId
	 * @return the first effect matching the given skillId
	 */
	public L2Effect getFirstEffect(int skillId)
	{
		L2Effect effectNotInUse = null;

		if(_buffs != null && !_buffs.isEmpty())
		{
			for(L2Effect e : _buffs)
			{
				if(e == null)
				{
					continue;
				}

				if(e.getSkill().getId() == skillId)
				{
					if(e.isInUse())
					{
						return e;
					}
					else
					{
						effectNotInUse = e;
					}
				}
			}
		}

		if(effectNotInUse == null && _debuffs != null && !_debuffs.isEmpty())
		{
			for(L2Effect e : _debuffs)
			{
				if(e == null)
				{
					continue;
				}
				if(e.getSkill().getId() == skillId)
				{
					if(e.isInUse())
					{
						return e;
					}
					else
					{
						effectNotInUse = e;
					}
				}
			}
		}

		return effectNotInUse;
	}

	/**
	 * @param tp
	 * @return all effects matching the given EffectType
	 */
	public L2Effect[] getEffects(L2EffectType tp)
	{
		FastList<L2Effect> temp = FastList.newInstance();

		if(_buffs != null)
		{
			if(!_buffs.isEmpty())
			{
				for(L2Effect e : _buffs)
				{
					if(e == null)
					{
						continue;
					}
					if(e.getEffectType() == tp)
					{
						if(e.isInUse())
						{
							temp.add(e);
						}
					}
				}
			}
		}
		if(_debuffs != null)
		{
			if(!_debuffs.isEmpty())
			{
				for(L2Effect e : _debuffs)
				{
					if(e == null)
					{
						continue;
					}
					if(e.getEffectType() == tp)
					{
						if(e.isInUse())
						{
							temp.add(e);
						}
					}
				}
			}

		}

		L2Effect[] tempArray = new L2Effect[temp.size()];
		temp.toArray(tempArray);
		FastList.recycle(temp);
		return tempArray;
	}

	/**
	 * Checks if the given skill stacks with an existing one.
	 *
	 * @param checkSkill the skill to be checked
	 * @return Returns whether or not this skill will stack
	 */
	private boolean doesStack(L2Skill checkSkill)
	{
		if(_buffs == null || _buffs.isEmpty() ||
			checkSkill._effectTemplates == null ||
			checkSkill._effectTemplates.length < 1 ||
			checkSkill._effectTemplates[0].getAbnormalType() == null ||
			"none".equals(checkSkill._effectTemplates[0].getAbnormalType()))
		{
			return false;
		}

		String stackType = checkSkill._effectTemplates[0].getAbnormalType();

		for(L2Effect e : _buffs)
		{
			if(e.getAbnormalType() != null && e.getAbnormalType().equals(stackType))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the number of buffs in this CharEffectList not counting Songs/Dances
	 */
	public int getBuffCount()
	{
		if(_buffs == null || _buffs.isEmpty())
		{
			return 0;
		}

		int buffCount = 0;
		for(L2Effect e : _buffs)
		{
			if(e != null && e.isIconDisplay() && !e.getSkill().isDance() && !e.getSkill().isTriggeredSkill())
			{
				switch(e.getSkill().getSkillType())
				{
					case BUFF:
					case HEAL_PERCENT:
					case MANAHEAL_PERCENT:
					case HPMPCPHEAL_PERCENT:
					case HPCPHEAL_PERCENT:
					case COMBATPOINTHEAL:
						++buffCount;
				}
			}
		}

		return buffCount;
	}

	/**
	 * @return the number of Songs/Dances in this CharEffectList
	 */
	public int getDanceCount()
	{
		if(_buffs == null || _buffs.isEmpty())
		{
			return 0;
		}

		int danceCount = 0;
		for(L2Effect e : _buffs)
		{
			if(e != null && e.isIconDisplay() && e.getSkill().isDance() && e.isInUse())
			{
				danceCount++;
			}
		}

		return danceCount;
	}

	/**
	 * @return number of Activation Buffs in this CharEffectList
	 */
	public int getTriggeredBuffCount()
	{
		if(_buffs == null)
		{
			return 0;
		}
		int activationBuffCount = 0;

		if(_buffs.isEmpty())
		{
			return 0;
		}

		for(L2Effect e : _buffs)
		{
			if(e != null && e.getSkill().isTriggeredSkill() && e.isInUse())
			{
				activationBuffCount++;
			}
		}
		return activationBuffCount;
	}

	/**
	 * Exits all effects in this CharEffectList
	 */
	public void stopAllEffects()
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();

		// Exit them
		for(L2Effect e : effects)
		{
			if(e != null)
			{
				e.exit(true);
			}
		}
	}

	/**
	 * Exits all effects in this CharEffectList
	 */
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		// Get all active skills effects from this list
		L2Effect[] effects = getAllEffects();

		// Exit them
		for(L2Effect e : effects)
		{
			if(e != null && !e.getSkill().isStayAfterDeath())
			{
				if(_owner instanceof L2Summon || _owner instanceof L2IncarnationInstance)
				{
					_owner.removeEffect(e);
				}
				else
				{
					e.exit(true);
				}
			}
		}
	}

	/**
	 * Exit all toggle-type effects
	 */
	public void stopAllToggles()
	{
		if(_buffs != null && !_buffs.isEmpty())
		{
			_buffs.stream().filter(e -> e != null && e.getSkill().isToggle()).forEach(L2Effect::exit);
		}
	}

	/**
	 * Exit all effects having a specified type
	 *
	 * @param type
	 */
	public void stopEffects(L2EffectType type)
	{
		if(_buffs != null && !_buffs.isEmpty())
		{
			// Get active skills effects of the selected type
			_buffs.stream().filter(e -> e != null && e.getEffectType() == type).forEach(L2Effect::exit);
		}

		if(_debuffs != null && !_debuffs.isEmpty())
		{
			// Get active skills effects of the selected type
			_debuffs.stream().filter(e -> e != null && e.getEffectType() == type).forEach(L2Effect::exit);
		}
	}

	/**
	 * Exit all effects having a specified stacktype
	 * @param type
	 */
	public void stopEffects(String... type)
	{
		// Go through all active skills effects
		FastList<L2Effect> temp = FastList.newInstance();
		if(_buffs != null)
		{
			if(!_buffs.isEmpty())
			{
				String stackType;
				for(L2Effect e : _buffs)
				{
					if(e != null)
					{
						stackType = e.getAbnormalType();
						// Get active skills effects of the selected type
						for(String stack : type)
						{
							if(stackType.equalsIgnoreCase(stack))
							{
								temp.add(e);
								break;
							}
						}
					}
				}
			}
		}
		if(_debuffs != null)
		{
			if(!_debuffs.isEmpty())
			{
				String stackType;
				for(L2Effect e : _debuffs)
				{
					if(e != null)
					{
						stackType = e.getAbnormalType();
						// Get active skills effects of the selected type
						for(String stack : type)
						{
							if(stackType.equalsIgnoreCase(stack))
							{
								temp.add(e);
								break;
							}
						}
					}
				}
			}
		}
		if(!temp.isEmpty())
		{
			temp.stream().filter(e -> e != null).forEach(L2Effect::exit);
		}
		FastList.recycle(temp);
	}

	/**
	 * Exits all effects created by a specific skillId
	 * @param skillId
	 */
	public void stopSkillEffects(int skillId)
	{
		// Go through all active skills effects
		FastList<L2Effect> temp = FastList.newInstance();
		if(_buffs != null && !_buffs.isEmpty())
		{
			temp.addAll(_buffs.stream().filter(e -> e != null && e.getSkill().getId() == skillId).collect(Collectors.toList()));
		}
		if(_debuffs != null && !_debuffs.isEmpty())
		{
			temp.addAll(_debuffs.stream().filter(e -> e != null && e.getSkill().getId() == skillId).collect(Collectors.toList()));
		}
		if(!temp.isEmpty())
		{
			// Just remove effect from summon, if this is copied buff.
			temp.stream().filter(e -> e != null).forEach(e -> {
				if(_owner instanceof L2Summon)
				{
					// Just remove effect from summon, if this is copied buff.
					for(L2Effect effect : _owner.getAllEffects())
					{
						if(effect == e)
						{
							_owner.removeEffect(e);
						}
					}
				}
				else
				{
					e.exit();
				}
			});
		}
		FastList.recycle(temp);
	}

	/**
	 * Exits all effects created by a specific skill type
	 * @param skillType skill type
	 */
	public void stopSkillEffects(L2SkillType skillType, int negateLvl)
	{
		// Go through all active skills effects
		FastList<L2Effect> temp = FastList.newInstance();
		if(_buffs != null && !_buffs.isEmpty())
		{
			temp.addAll(_buffs.stream().filter(e -> e != null && (e.getSkill().getSkillType() == skillType || e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == skillType) && (negateLvl == -1 || e.getSkill().getEffectType() != null && e.getSkill().getEffectAbnormalLvl() >= 0 && e.getSkill().getEffectAbnormalLvl() <= negateLvl || e.getSkill().getAbnormalLvl() >= 0 && e.getSkill().getAbnormalLvl() <= negateLvl)).collect(Collectors.toList()));
		}
		if(_debuffs != null && !_debuffs.isEmpty())
		{
			temp.addAll(_debuffs.stream().filter(e -> e != null && (e.getSkill().getSkillType() == skillType || e.getSkill().getEffectType() != null && e.getSkill().getEffectType() == skillType) && (negateLvl == -1 || e.getSkill().getEffectType() != null && e.getSkill().getEffectAbnormalLvl() >= 0 && e.getSkill().getEffectAbnormalLvl() <= negateLvl || e.getSkill().getAbnormalLvl() >= 0 && e.getSkill().getAbnormalLvl() <= negateLvl)).collect(Collectors.toList()));
		}
		if(!temp.isEmpty())
		{
			temp.stream().filter(e -> e != null).forEach(L2Effect::exit);
		}
		FastList.recycle(temp);
	}

	/**
	 * Exits all buffs effects of the skills with "removedOnAnyAction" set.
	 * Called on any action except movement (attack, cast).
	 */
	public void stopEffectsOnAction()
	{
		if(_removedEffectList.contains(L2EffectStopCond.ON_ACTION_EXCEPT_MOVE))
		{
			if(_buffs != null && !_buffs.isEmpty())
			{
				_buffs.stream().filter(e -> e != null && e.getRemovedEffectType().contains(L2EffectStopCond.ON_ACTION_EXCEPT_MOVE)).forEach(e -> e.exit(true));
			}
		}
	}

	/**
	 * Если на персонаже есть эффекты, которые снимаются при атакующих действиях - снимаем их.
	 */
	public void stopEffectsOnAttack()
	{
		if(_removedEffectList.contains(L2EffectStopCond.ON_ATTACK_BUFF))
		{
			if(_buffs != null && !_buffs.isEmpty())
			{
				for(L2Effect e : _buffs)
				{
					if(e != null && e.getRemovedEffectType().contains(L2EffectStopCond.ON_ATTACK_BUFF))
					{
						if(e.isHitCountRemove())
						{
							continue;
						}

						e.exit(true);
					}
				}
			}
		}

		if(_removedEffectList.contains(L2EffectStopCond.ON_ATTACK_DEBUFF))
		{
			if(_debuffs != null && !_debuffs.isEmpty())
			{
				for(L2Effect e : _debuffs)
				{
					if(e != null && e.getRemovedEffectType().contains(L2EffectStopCond.ON_ATTACK_DEBUFF))
					{
						if(e.isHitCountRemove())
						{
							continue;
						}

						e.exit(true);
					}
				}
			}
		}
	}

	public void stopEffectsOnDamage(boolean awake)
	{
		if(_removedEffectList.contains(L2EffectStopCond.ON_DAMAGE_BUFF))
		{
			if(_buffs != null && !_buffs.isEmpty())
			{
				for(L2Effect e : _buffs)
				{
					if(e != null && e.getRemovedEffectType().contains(L2EffectStopCond.ON_DAMAGE_BUFF) && (awake || e.getSkill().getSkillType() != L2SkillType.SLEEP))
					{
						if(e.isHitCountRemove())
						{
							continue;
						}

						e.exit(true);
					}
				}
			}
		}
		if(_removedEffectList.contains(L2EffectStopCond.ON_DAMAGE_DEBUFF))
		{
			if(_debuffs != null && !_debuffs.isEmpty())
			{
				for(L2Effect e : _debuffs)
				{
					if(e != null && e.getRemovedEffectType().contains(L2EffectStopCond.ON_DAMAGE_DEBUFF) && (awake || e.getSkill().getSkillType() != L2SkillType.SLEEP))
					{
						if(e.isHitCountRemove())
						{
							continue;
						}

						e.exit(true);
					}
				}
			}
		}
	}

	/**
	 * @return {@code true} если лист эффектов персонажа содержит эффекты, удаляющиеся при каком либо action
	 * кроме как движения
	 */
	public boolean hasBuffsRemovedOnAction()
	{
		return _removedEffectList.contains(L2EffectStopCond.ON_ACTION_EXCEPT_MOVE);
	}

	/**
	 * @return {@code true} если лист эффектов персонажа содержит эффекты, удаляющиеся при атакующих действиях
	 */
	public boolean hasBuffsRemovedOnAttack()
	{
		return _removedEffectList.contains(L2EffectStopCond.ON_ATTACK_DEBUFF) || _removedEffectList.contains(L2EffectStopCond.ON_ATTACK_BUFF);
	}

	/**
	 * @return {@code true} если лист эффектов персонажа содержит эффекты, удаляющиеся при нанесении ему урона
	 */
	public boolean hasBuffsRemovedOnDamage()
	{
		return _removedEffectList.contains(L2EffectStopCond.ON_DAMAGE_DEBUFF) || _removedEffectList.contains(L2EffectStopCond.ON_DAMAGE_BUFF);
	}

	public void updateEffectIcons(boolean partyOnly)
	{
		if(_buffs == null && _debuffs == null)
		{
			return;
		}

		if(partyOnly)
		{
			_partyOnly = true;
		}

		queueRunner();
	}

	public void queueEffect(L2Effect effect, boolean remove)
	{
		if(effect == null)
		{
			return;
		}

		if(!_queuesInitialized)
		{
			init();
		}

		if(remove)
		{
			_removeQueue.offer(effect);
		}
		else
		{
			_addQueue.offer(effect);
		}

		queueRunner();
	}

	private void init()
	{
		synchronized(this)
		{
			if(_queuesInitialized)
			{
				return;
			}
			_addQueue = new LinkedBlockingQueue<>();
			_removeQueue = new LinkedBlockingQueue<>();
			_queuesInitialized = true;
		}
	}

	private void queueRunner()
	{
		if(!queueLock.compareAndSet(false, true))
		{
			return;
		}
		try
		{
			L2Effect effect;
			do
			{
				// remove has more priority than add
				// so removing all effects from queue first
				while((effect = _removeQueue.poll()) != null)
				{
					removeEffectFromQueue(effect);

					if(_owner instanceof L2PcInstance)
					{
						HookManager.getInstance().notifyEvent(HookType.ON_EFFECT_STOP, _owner.getHookContainer(), effect);
					}

					_partyOnly = false;
				}

				if((effect = _addQueue.poll()) != null)
				{
					addEffectFromQueue(effect);

					if(_owner instanceof L2PcInstance)
					{
						HookManager.getInstance().notifyEvent(HookType.ON_EFFECT_START, _owner.getHookContainer(), effect);
					}

					_partyOnly = false;

					if(effect.getEffectTemplate().getAbnormalLockedStackTypes() != null)
					{
						stopEffects(effect.getEffectTemplate().getAbnormalLockedStackTypes());
					}
				}
			}
			while(!_addQueue.isEmpty() || !_removeQueue.isEmpty());

			computeEffectFlags();
			updateEffectIcons();
		}
		finally
		{
			queueLock.set(false);
		}
	}

	protected void removeEffectFromQueue(L2Effect effect)
	{
		if(effect == null)
		{
			return;
		}

		if(effect.isPassiveEffect())
		{
			if(effect.setInUse(false))
			{
				// Remove Func added by this effect from the L2Character Calculator
				_owner.removeStatsOwner(effect.getStatFuncs());
				if(_passives != null)
				{
					_passives.remove(effect);
				}
				else
				{
					_log.log(Level.ERROR, " _passives==null skill: " + effect.getSkill());
				}
			}
		}

		FastList<L2Effect> effectList;
		// array modified, then rebuild on next request
		_rebuildCache = true;

		if(effect.getSkill().isDebuff())
		{
			if(_debuffs == null)
			{
				return;
			}
			effectList = _debuffs;
		}
		else
		{
			if(_buffs == null)
			{
				return;
			}
			effectList = _buffs;
		}

		if("none".equals(effect.getAbnormalType()))
		{
			// Remove Func added by this effect from the L2Character Calculator
			_owner.removeStatsOwner(effect);
		}
		else
		{
			if(_stackedEffects == null)
			{
				return;
			}

			// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
			List<L2Effect> stackQueue = _stackedEffects.get(effect.getAbnormalType());

			if(stackQueue == null || stackQueue.isEmpty())
			{
				return;
			}

			int index = stackQueue.indexOf(effect);

			// Remove the effect from the stack group
			if(index >= 0)
			{
				stackQueue.remove(effect);
				// Check if the first stacked effect was the effect to remove
				if(index == 0)
				{
					// Remove all its Func objects from the L2Character calculator set
					_owner.removeStatsOwner(effect);

					// Check if there's another effect in the Stack Group
					if(!stackQueue.isEmpty())
					{
						L2Effect newStackedEffect = listsContains(stackQueue.get(0));
						if(newStackedEffect != null)
						{
							// Set the effect to In Use
							if(newStackedEffect.setInUse(true))
							{
								// Add its list of Funcs to the Calculator set of the L2Character
								_owner.addStatFuncs(newStackedEffect.getStatFuncs());
							}
						}
					}
				}
				if(stackQueue.isEmpty())
				{
					_stackedEffects.remove(effect.getAbnormalType());
				}
				else
				{
					// Update the Stack Group table _stackedEffects of the L2Character
					_stackedEffects.put(effect.getAbnormalType(), stackQueue);
				}
			}
		}
		// Remove the active skill L2effect from _effects of the L2Character
		if(effectList.remove(effect) && _owner instanceof L2PcInstance && effect.isIconDisplay() && !effect.getSkill().isMsgStatusHidden())
		{
			if(effect.getSkill().isToggle())
			{
				_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED).addSkillName(effect));
			}
			else
			{
				_owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED).addSkillName(effect));
			}
		}
	}

	protected void addEffectFromQueue(L2Effect newEffect)
	{
		if(newEffect == null)
		{
			return;
		}

		L2Skill newSkill = newEffect.getSkill();

		// check if we block somewhere :x
		if(!(newEffect instanceof DispelBySlot) && _lockedStackTypes != null && _lockedStackTypes.contains(newEffect.getAbnormalType()))
		{
			newEffect.stopEffectTask();
			return;
		}

		// Passive effects are treated specially
		if(newEffect.isPassiveEffect())
		{
			if(_passives == null)
			{
				_passives = new FastList<L2Effect>().shared();
			}

			// Passive effects dont need stack type
			if("none".equals(newEffect.getAbnormalType()))
			{
				// Set this L2Effect to In Use
				if(newEffect.setInUse(true))
				{
					for(L2Effect eff : _passives)
					{
						if(eff == null)
						{
							continue;
						}

						// Check and remove if there is already such effect in order to prevent passive effects overstack.
						if(eff.getEffectTemplate().equals(newEffect.getEffectTemplate()))
						{
							eff.exit();
						}

					}

					// Add Funcs of this effect to the Calculator set of the L2Character
					_owner.addStatFuncs(newEffect.getStatFuncs());
					_passives.add(newEffect);
				}
			}
			return;
		}

		// array modified, then rebuild on next request
		_rebuildCache = true;

		if(newSkill.isDebuff())
		{
			if(_debuffs == null)
			{
				_debuffs = new FastList<L2Effect>().shared();
			}

			for(L2Effect e : _debuffs)
			{
				if(e != null && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getAbnormalLvl() == newEffect.getAbnormalLvl() && e.getAbnormalType().equals(newEffect.getAbnormalType()))
				{

					if(e.getSkill().isRestartableDebuff())
					{
						e.restart();
					}

					// STARTED scheduled timer needs to be canceled.
					newEffect.stopEffectTask();
					return;
				}
			}
			_debuffs.addLast(newEffect);
		}
		else
		{
			if(_buffs == null)
			{
				_buffs = new FastList<L2Effect>().shared();
			}

			for(L2Effect e : _buffs)
			{
				if(newEffect.isAbnormatTypeIgnored())
				{
					break;
				}

				if(e != null && e.getSkill().getId() == newEffect.getSkill().getId() && e.getEffectType() == newEffect.getEffectType() && e.getAbnormalLvl() == newEffect.getAbnormalLvl())
				{
					if(e.getAbnormalType().equals(newEffect.getAbnormalType()))
					{
						e.exit(); // exit this
					}
					else if(e.isAbnormatTypeIgnored())
					{
						newEffect.exit();
					}
				}
			}

			// if max buffs, no herb effects are used, even if they would replace one old
			if(newEffect.isHerbEffect() && getBuffCount() >= _owner.getMaxBuffCount())
			{
				newEffect.stopEffectTask();
				return;
			}

			// Remove first buff when buff list is full
			if(!doesStack(newSkill))
			{
				int effectsToRemove;
				if(newSkill.isDance())
				{
					effectsToRemove = getDanceCount() - Config.DANCES_MAX_AMOUNT;
					if(effectsToRemove >= 0)
					{
						for(L2Effect e : _buffs)
						{
							if(e == null || !e.getSkill().isDance())
							{
								continue;
							}

							// get first dance
							e.exit();
							effectsToRemove--;
							if(effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else if(newSkill.isTriggeredSkill())
				{
					effectsToRemove = getTriggeredBuffCount() - Config.TRIGGERED_BUFFS_MAX_AMOUNT;
					if(effectsToRemove >= 0)
					{
						for(L2Effect e : _buffs)
						{
							if(e == null || !e.getSkill().isTriggeredSkill())
							{
								continue;
							}

							// get first dance
							e.exit();
							effectsToRemove--;
							if(effectsToRemove < 0)
							{
								break;
							}
						}
					}
				}
				else
				{
					effectsToRemove = getBuffCount() - _owner.getMaxBuffCount();
					if(effectsToRemove >= 0)
					{
						switch(newSkill.getSkillType())
						{
							case BUFF:
							case HEAL_PERCENT:
							case MANAHEAL_PERCENT:
								for(L2Effect e : _buffs)
								{
									if(e == null || e.getSkill().isDance() || e.getSkill().isTriggeredSkill())
									{
										continue;
									}

									switch(e.getSkill().getSkillType())
									{
										case BUFF:
										case HEAL_PERCENT:
										case MANAHEAL_PERCENT:
											e.exit();
											effectsToRemove--;
											break; // break switch()
										default:
											continue; // continue for()
									}
									if(effectsToRemove < 0)
									{
										break; // break for()
									}
								}
						}
					}
				}
			}

			// Icons order: buffs, 7s, toggles, dances, activation buffs
			if(newSkill.isTriggeredSkill())
			{
				_buffs.addLast(newEffect);
			}
			else
			{
				int pos = 0;
				if(newSkill.isToggle())
				{
					// toggle skill - before all dances
					for(L2Effect e : _buffs)
					{
						if(e == null)
						{
							continue;
						}
						if(e.getSkill().isDance())
						{
							break;
						}
						pos++;
					}
				}
				else if(newSkill.isDance())
				{
					// dance skill - before all activation buffs
					for(L2Effect e : _buffs)
					{
						if(e == null)
						{
							continue;
						}
						if(e.getSkill().isTriggeredSkill())
						{
							break;
						}
						pos++;
					}
				}
				else
				{
					// normal buff - before toggles and 7s and dances
					for(L2Effect e : _buffs)
					{
						if(e == null)
						{
							continue;
						}
						if(e.getSkill().isToggle() || e.getSkill().isDance())
						{
							break;
						}
						pos++;
					}
				}
				_buffs.add(pos, newEffect);
			}
		}

		// Check if a stack group is defined for this effect
		if("none".equals(newEffect.getAbnormalType()))
		{
			// Set this L2Effect to In Use
			if(newEffect.setInUse(true))
			{
				// Add Funcs of this effect to the Calculator set of the L2Character
				_owner.addStatFuncs(newEffect.getStatFuncs());
			}
			return;
		}

		List<L2Effect> stackQueue;
		L2Effect effectToAdd = null;
		L2Effect effectToRemove = null;
		if(_stackedEffects == null)
		{
			_stackedEffects = new FastMap<>();
		}

		// Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
		stackQueue = _stackedEffects.get(newEffect.getAbnormalType());

		if(stackQueue != null)
		{
			int pos = 0;
			if(stackQueue.isEmpty())
			{
				stackQueue.add(0, newEffect);
			}
			else
			{
				// Get the first stacked effect of the Stack group selected
				effectToRemove = listsContains(stackQueue.get(0));

				// Create an Iterator to go through the list of stacked effects in progress on the L2Character
				for(L2Effect aStackQueue : stackQueue)
				{
					if(newEffect.getAbnormalLvl() < aStackQueue.getAbnormalLvl())
					{
						pos++;
					}
					else
					{
						break;
					}
				}
				// Add the new effect to the Stack list in function of its position in the Stack group
				stackQueue.add(pos, newEffect);

				// skill.exit() could be used, if the users don't wish to see "effect
				// removed" always when a timer goes off, even if the buff isn't active
				// any more (has been replaced). but then check e.g. npc hold and raid petrification.
				if(Config.EFFECT_CANCELING && !newEffect.isHerbEffect() && stackQueue.size() > 1)
				{
					if(newSkill.isDebuff())
					{
						_debuffs.remove(stackQueue.remove(1));
					}
					else
					{
						_buffs.remove(stackQueue.remove(1));
					}
				}
			}
		}
		else
		{
			stackQueue = new FastList<>();
			stackQueue.add(0, newEffect);
		}

		// Update the Stack Group table _stackedEffects of the L2Character
		_stackedEffects.put(newEffect.getAbnormalType(), stackQueue);

		// Get the first stacked effect of the Stack group selected
		if(!stackQueue.isEmpty())
		{
			effectToAdd = listsContains(stackQueue.get(0));
		}

		if(effectToRemove != effectToAdd)
		{
			//TODO: https://godworld.ru/forum/showthread.php?t=2051
			if(effectToRemove != null)
			{
				// Remove all Func objects corresponding to this stacked effect from the Calculator set of the L2Character
				_owner.removeStatsOwner(effectToRemove);

				// Set the L2Effect to Not In Use
				effectToRemove.setInUse(false);
			}
			if(effectToAdd != null)
			{
				// Set this L2Effect to In Use
				if(effectToAdd.setInUse(true))
				{
					// Add all Func objects corresponding to this stacked effect to the Calculator set of the L2Character
					_owner.addStatFuncs(effectToAdd.getStatFuncs());
				}
			}
		}
	}

	/**
	 * Remove all passive effects held by this <b>skillId</b>.
	 * @param skillId
	 */
	public void removePassiveEffects(int skillId)
	{
		if(_passives == null)
		{
			return;
		}

		for(L2Effect eff : _passives)
		{
			if(eff == null)
			{
				continue;
			}

			if(eff.getSkill().getId() == skillId)
			{
				eff.exit();
			}
		}
	}

	protected void updateEffectIcons()
	{
		if(_owner == null)
		{
			return;
		}

		if(!(_owner instanceof L2Playable))
		{
			updateEffectFlags();
			return;
		}

		AbnormalStatusUpdate mi = null;
		PartySpelled ps = null;
		ExOlympiadSpelledInfo os = null;

		if(_owner instanceof L2PcInstance)
		{
			if(_partyOnly)
			{
				_partyOnly = false;
			}
			else
			{
				mi = new AbnormalStatusUpdate();
			}

			if(_owner.isInParty())
			{
				ps = new PartySpelled(_owner);
			}

			if(((L2PcInstance) _owner).getOlympiadController().isParticipating() && ((L2PcInstance) _owner).getOlympiadController().isPlayingNow())
			{
				os = new ExOlympiadSpelledInfo((L2PcInstance) _owner);
			}
		}
		else
		{
			if(_owner instanceof L2Summon)
			{
				ps = new PartySpelled(_owner);
			}
		}

		FastSet<L2EffectStopCond> removedEffectList = new FastSet<>();
		int flags = 0;

		if(_lockedStackTypes != null)
		{
			_lockedStackTypes.clear();
		}

		if(_buffs != null && !_buffs.isEmpty())
		{
			for(L2Effect e : _buffs)
			{
				if(e == null)
				{
					continue;
				}

				if(e.isInUse())
				{
					flags |= e.getEffectFlags();

					if(e.getEffectTemplate().getAbnormalLockedStackTypes() != null)
					{
						if(_lockedStackTypes == null)
						{
							_lockedStackTypes = FastSet.newInstance();
						}
						Collections.addAll(_lockedStackTypes, e.getEffectTemplate().getAbnormalLockedStackTypes());
					}
				}

				removedEffectList.addAll(e.getRemovedEffectType());

				if(!e.isIconDisplay())
				{
					continue;
				}

				switch(e.getEffectType())
				{
					case CHARGE: // handled by EtcStatusUpdate
					case SIGNET_GROUND:
						continue;
				}

				if(e.isInUse())
				{
					if(mi != null)
					{
						e.addIcon(mi);
					}

					if(ps != null)
					{
						e.addPartySpelledIcon(ps);
					}

					if(os != null)
					{
						e.addOlympiadSpelledIcon(os);
					}
				}
			}
		}

		if(_debuffs != null && !_debuffs.isEmpty())
		{
			for(L2Effect e : _debuffs)
			{
				if(e == null)
				{
					continue;
				}

				if(e.isInUse())
				{
					flags |= e.getEffectFlags();

					if(e.getEffectTemplate().getAbnormalLockedStackTypes() != null)
					{
						if(_lockedStackTypes == null)
						{
							_lockedStackTypes = FastSet.newInstance();
						}
						Collections.addAll(_lockedStackTypes, e.getEffectTemplate().getAbnormalLockedStackTypes());
					}
				}

				removedEffectList.addAll(e.getRemovedEffectType());

				if(!e.isIconDisplay())
				{
					continue;
				}

				switch(e.getEffectType())
				{
					case SIGNET_GROUND:
						continue;
				}

				if(e.isInUse())
				{
					if(mi != null)
					{
						e.addIcon(mi);
					}

					if(ps != null)
					{
						e.addPartySpelledIcon(ps);
					}

					if(os != null)
					{
						e.addOlympiadSpelledIcon(os);
					}
				}
			}
		}

		_effectFlags = flags;

		if(!_removedEffectList.isEmpty())
		{
			_removedEffectList.clear();
		}
		_removedEffectList.addAll(removedEffectList);

		if(_lockedStackTypes != null && _lockedStackTypes.isEmpty())
		{
			FastSet.recycle(_lockedStackTypes);
			_lockedStackTypes = null;
		}

		if(mi != null)
		{
			_owner.sendPacket(mi);
		}

		if(ps != null)
		{
			if(_owner instanceof L2Summon)
			{
				L2PcInstance summonOwner = ((L2Summon) _owner).getOwner();

				if(summonOwner != null)
				{
					if(summonOwner.isInParty())
					{
						summonOwner.getParty().broadcastPacket(ps);
					}
					else
					{
						summonOwner.sendPacket(ps);
					}
				}
			}
			else
			{
				if(_owner instanceof L2PcInstance && _owner.isInParty())
				{
					_owner.getParty().broadcastPacket(ps);
				}
			}
		}

		if(os != null)
		{
			OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(((L2PcInstance) _owner).getOlympiadController().getGameId());
			if(game != null && game.isBattleStarted())
			{
				game.getZone().broadcastPacketToObservers(os);
			}
		}

		// При обновлении бафов, шлем пакет всем, у кого игрок в таргете
		_owner.getKnownList().getKnownPlayers().values().stream().filter(pl -> pl != null && pl.getTarget() == _owner).forEach(pl -> pl.sendPacket(new ExAbnormalStatusUpdateFromTarget(_owner, pl.isAwakened())));
		if(_owner.getTarget() == _owner)
		{
			_owner.sendPacket(new ExAbnormalStatusUpdateFromTarget(_owner, _owner.isAwakened()));
		}
	}

	protected void updateEffectFlags()
	{

		FastSet<L2EffectStopCond> removedEffectList = new FastSet<>();
		int flags = 0;

		if(_lockedStackTypes != null)
		{
			_lockedStackTypes.clear();
		}

		if(_buffs != null && !_buffs.isEmpty())
		{
			for(L2Effect e : _buffs)
			{
				if(e == null)
				{
					continue;
				}

				if(e.isInUse())
				{
					flags |= e.getEffectFlags();

					if(e.getEffectTemplate().getAbnormalLockedStackTypes() != null)
					{
						if(_lockedStackTypes == null)
						{
							_lockedStackTypes = FastSet.newInstance();
						}
						Collections.addAll(_lockedStackTypes, e.getEffectTemplate().getAbnormalLockedStackTypes());
					}
				}

				removedEffectList.addAll(e.getRemovedEffectType());
			}
		}

		if(_debuffs != null && !_debuffs.isEmpty())
		{
			for(L2Effect e : _debuffs)
			{
				if(e == null)
				{
					continue;
				}

				if(e.isInUse())
				{
					flags |= e.getEffectFlags();

					if(e.getEffectTemplate().getAbnormalLockedStackTypes() != null)
					{
						if(_lockedStackTypes == null)
						{
							_lockedStackTypes = FastSet.newInstance();
						}
						Collections.addAll(_lockedStackTypes, e.getEffectTemplate().getAbnormalLockedStackTypes());
					}
				}

				removedEffectList.addAll(e.getRemovedEffectType());
			}
		}

		if(!_removedEffectList.isEmpty())
		{
			_removedEffectList.clear();
		}
		_removedEffectList.addAll(removedEffectList);

		if(_lockedStackTypes != null && _lockedStackTypes.isEmpty())
		{
			FastSet.recycle(_lockedStackTypes);
			_lockedStackTypes = null;
		}

		_effectFlags = flags;
	}

	/**
	 * @param effect
	 * @return effect if contains in _buffs or _debuffs and null if not found
	 */
	private L2Effect listsContains(L2Effect effect)
	{
		if(_buffs != null && !_buffs.isEmpty() && _buffs.contains(effect))
		{
			return effect;
		}
		if(_debuffs != null && !_debuffs.isEmpty() && _debuffs.contains(effect))
		{
			return effect;
		}
		return null;
	}

	/**
	 * Recalculate effect bits flag.
	 * Please no concurrency access
	 */
	private void computeEffectFlags()
	{
		int flags = 0;

		if(_buffs != null)
		{
			for(L2Effect e : _buffs)
			{
				if(e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}

		if(_debuffs != null)
		{
			for(L2Effect e : _debuffs)
			{
				if(e == null)
				{
					continue;
				}
				flags |= e.getEffectFlags();
			}
		}

		_effectFlags = flags;
	}

	/**
	 * Check if target is affected with special buff
	 *
	 * @param bitFlag flag of special buff
	 * @return boolean true if affected
	 */
	public boolean isAffected(int bitFlag)
	{
		return (_effectFlags & bitFlag) != 0;
	}

	/**
	 * Clear and null all queues and lists
	 * Use only during delete character from the world.
	 */
	public void clear()
	{
		/*
		 * Removed .clear() since nodes/entries and its references (Effects) should be
		 * terminated by GC when Queue/Map/List object has no more reference.
		 * This way we will save a little more CPU
		 * [DrHouse]
		 */
		try
		{
			_addQueue = null;
			_removeQueue = null;
			_buffs = null;
			_debuffs = null;
			_stackedEffects = null;
			_queuesInitialized = false;
			_effectCache = null;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}
}
