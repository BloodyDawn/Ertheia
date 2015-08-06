package dwo.gameserver.model.actor;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.controller.player.PvPFlagController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.knownlist.TrapKnownList;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.Quest.TrapAction;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import org.apache.log4j.Level;
import org.jetbrains.annotations.Nullable;

/**
 * @author nBd
 */
public class L2Trap extends L2Character
{
	protected static final int TICK = 1000; // 1s
	private final L2Skill _skill;
	private final int _lifeTime;
	private boolean _isTriggered;
	private int _timeRemaining;
	private boolean _hasLifeTime;

	public L2Trap(int objectId, L2NpcTemplate template, int lifeTime, L2Skill skill)
	{
		super(objectId, template);
		setName(template.getName());
		setIsInvul(false);

		_isTriggered = false;
		_skill = skill;
		_hasLifeTime = true;
		_lifeTime = lifeTime != 0 ? lifeTime : 30000;
		_timeRemaining = _lifeTime;
		if(lifeTime < 0)
		{
			_hasLifeTime = false;
		}

		if(skill != null)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), TICK);
		}
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return !canSee(attacker);
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if(_isTriggered || canSee(activeChar))
		{
			activeChar.sendPacket(new NpcInfo(this));
		}
	}

	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	public void unSummon()
	{
		synchronized(this)
		{
			if(isVisible() && !isDead())
			{
				if(getLocationController().getWorldRegion() != null)
				{
					getLocationController().getWorldRegion().removeFromZones(this);
				}

				onDelete();
			}
		}
	}

	public L2Skill getSkill()
	{
		return _skill;
	}

	public L2PcInstance getOwner()
	{
		return null;
	}

	public int getReputation()
	{
		return 0;
	}

	@Nullable
	public PvPFlagController getPvPFlagController()
	{
		return null;
	}

	/**
	 * Checks is triggered
	 *
	 * @return True if trap is triggered.
	 */
	public boolean isTriggered()
	{
		return _isTriggered;
	}

	/**
	 * Checks trap visibility
	 *
	 * @param cha - checked character
	 * @return True if character can see trap
	 */
	public boolean canSee(L2Character cha)
	{
		return false;
	}

	/**
	 * Reveal trap to the detector (if possible)
	 */
	public void setDetected(L2Character detector)
	{
		detector.sendPacket(new NpcInfo(this));
	}

	/**
	 * Check if target can trigger trap
	 */
	protected boolean checkTarget(L2Character target)
	{
		return L2Skill.checkForAreaOffensiveSkills(this, target, _skill, false);
	}

	/**
	 * Trigger trap
	 */
	public void trigger(L2Character target)
	{
		_isTriggered = true;
		broadcastPacket(new NpcInfo(this));
		setTarget(target);

		if(getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION) != null)
		{
			for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_TRAP_ACTION))
			{
				quest.notifyTrapAction(this, target, TrapAction.TRAP_TRIGGERED);
			}
		}

		ThreadPoolManager.getInstance().scheduleGeneral(new TriggerTask(), 300);
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		getKnownList().getKnownPlayers().values().stream().filter(player -> player != null && (_isTriggered || canSee(player))).forEach(player -> player.sendPacket(mov));
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			if(isInsideRadius(player, radiusInKnownlist, false, false))
			{
				if(_isTriggered || canSee(player))
				{
					player.sendPacket(mov);
				}
			}
		}
	}

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}

		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	@Override
	public void updateAbnormalEffect()
	{

	}

	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public boolean onDecay()
	{
		//getLocationController().delete();
		return true;
	}

	@Override
	public boolean onDelete()
	{
		getLocationController().decay();
		getKnownList().removeAllKnownObjects();
		return super.onDelete();
	}

	@Override
	public TrapKnownList getKnownList()
	{
		return (TrapKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new TrapKnownList(this));
	}

	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}

	private class TrapTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(!_isTriggered)
				{
					if(_hasLifeTime)
					{
						_timeRemaining -= TICK;
						if(_timeRemaining < _lifeTime - 15000)
						{
							SocialAction sa = new SocialAction(getObjectId(), 2);
							broadcastPacket(sa);
						}
						if(_timeRemaining < 0)
						{
							switch(getSkill().getTargetType())
							{
								case TARGET_AURA:
								case TARGET_FRONT_AURA:
								case TARGET_BEHIND_AURA:
									trigger(L2Trap.this);
									break;
								default:
									unSummon();
							}
							return;
						}
					}

					for(L2Character target : getKnownList().getKnownCharactersInRadius(_skill.getSkillRadius()))
					{
						if(!checkTarget(target))
						{
							continue;
						}

						trigger(target);
						return;
					}

					ThreadPoolManager.getInstance().scheduleGeneral(new TrapTask(), TICK);
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
				unSummon();
			}
		}
	}

	private class TriggerTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doCast(_skill);
				ThreadPoolManager.getInstance().scheduleGeneral(new UnsummonTask(), _skill.getHitTime() + 300);
			}
			catch(Exception e)
			{
				unSummon();
			}
		}
	}

	private class UnsummonTask implements Runnable
	{
		@Override
		public void run()
		{
			unSummon();
		}
	}
}
