package dwo.gameserver.model.actor;

import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Attackable.AggroInfo;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.ai.L2SummonAI;
import dwo.gameserver.model.actor.controller.player.PvPFlagController;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.actor.knownlist.SummonKnownList;
import dwo.gameserver.model.actor.stat.SummonStat;
import dwo.gameserver.model.actor.status.SummonStatus;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.PetInventory;
import dwo.gameserver.model.player.PlayerSiegeSide;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.RelationChanged;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.TeleportToLocation;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExMagicAttackInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.ExPetInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.SummonInfo;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyPetWindowAdd;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyPetWindowDelete;
import dwo.gameserver.network.game.serverpackets.packet.party.ExPartyPetWindowUpdate;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySpelled;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetDelete;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetInfo;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetItemList;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetStatusUpdate;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

public abstract class L2Summon extends L2Playable
{
	//  /!\ BLACK MAGIC /!\
	// we dont have walk speed in pet data so for now use runspd / 3
	public static final int WALK_SPEED_MULTIPLIER = 3;
	public boolean _restoreSummon = true;
	private L2PcInstance _owner;
	private int _attackRange = 36; //Melee range
	private boolean _follow = true;
	private boolean _defendingMode;
	private boolean _previousFollowStatus = true;
	private int _state = 2; // 0=teleported  1=default   2=summoned
	private int _chargedSoulShot;
	private int _chargedSpiritShot;
	/**
	 * Start time when summon was spawned.
	 */
	private long _summonTime;
	private int _restoredObjectId = -1;
	private boolean _isUnsummoningNow;
    private boolean _isIncarnation;

    protected L2Summon(int objectId, L2NpcTemplate template, L2PcInstance owner)
	{
		super(objectId, template);

		getInstanceController().setInstanceId(owner.getInstanceId()); // set instance to same as owner

		_showSummonAnimation = true;
		_owner = owner;
		_ai = new L2SummonAI(new AIAccessor());

		setXYZ(owner.getX() + 20, owner.getY() + 20, owner.getZ() + 100, false);
	}

    public abstract int getSummonType();

	/**
	 * @return {@code true} if mountable.
	 */
	public boolean isMountable()
	{
		return false;
	}

	public long getExpForThisLevel()
	{
		if(getLevel() >= ExperienceTable.getInstance().getMaxPetLevel())
		{
			return 0;
		}
		return ExperienceTable.getInstance().getExpForLevel(getLevel());
	}

	public long getExpForNextLevel()
	{
		if(getLevel() >= ExperienceTable.getInstance().getMaxPetLevel() - 1)
		{
			return 0;
		}
		return ExperienceTable.getInstance().getExpForLevel(getLevel() + 1);
	}

	public int getTeam()
	{
		return _owner != null ? _owner.getTeam() : 0;
	}

	public L2PcInstance getOwner()
	{
		return _owner;
	}

	public void setOwner(L2PcInstance newOwner)
	{
		_owner = newOwner;
	}

	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	public short getSoulShotsPerHit()
	{
		if(getTemplate().getSoulShot() > 0)
		{
			return (short) getTemplate().getSoulShot();
		}

		return 1;
	}

	public short getSpiritShotsPerHit()
	{
		if(getTemplate().getSpiritShot() > 0)
		{
			return (short) getTemplate().getSpiritShot();
		}

		return 1;
	}

	public void followOwner()
	{
		setFollowStatus(true);
	}

	// this defines the action buttons, 1 for Summon, 2 for Pets

	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}

		L2PcInstance owner = _owner;

		if(owner != null)
		{
			Collection<L2Character> KnownTarget = getKnownList().getKnownCharacters();
			for(L2Character TgMob : KnownTarget)
			{
				// get the mobs which have aggro on the this instance
				if(TgMob instanceof L2Attackable)
				{
					if(L2Attackable.class.cast(TgMob).isDead())
					{
						continue;
					}

					AggroInfo info = L2Attackable.class.cast(TgMob).getAggroList().get(this);
					if(info != null)
					{
						L2Attackable.class.cast(TgMob).addDamageHate(owner, info.getDamage(), info.getHate());
					}
				}
			}
		}

		HookManager.getInstance().notifyEvent(HookType.ON_SUMMON_DIE, getHookContainer(), this, killer);

		DecayTaskManager.getInstance().addDecayTask(this);
		return true;
	}

	@Override
	public SummonStat getStat()
	{
		return (SummonStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new SummonStat(this));
	}

	@Override
	public SummonStatus getStatus()
	{
		return (SummonStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new SummonStatus(this));
	}

	@Override
	public SummonKnownList getKnownList()
	{
		return (SummonKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new SummonKnownList(this));
	}

	@Override
	public int getReputation()
	{
		return _owner != null ? _owner.getReputation() : 0;
	}

	/**
	 * Check if the active L2Skill can be casted.
	 *
	 * Actions:
	 * Check if the target is correct
	 * Check if the target is in the skill cast range
	 * Check if the summon owns enough HP and MP to cast the skill
	 * Check if all skills are enabled and this skill is enabled
	 * Check if the skill is active
	 * Notify the AI with AI_INTENTION_CAST and target
	 *
	 * @param skill    The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		if(_owner == null)
		{
			return false;
		}
		if(skill == null || isDead())
		{
			return false;
		}
		if(skill.isPassive()) // Check if the skill is active
		{
			return false;
		}

		//************************************* Check Casting in Progress *******************************************

		// If a skill is currently being used
		if(isCastingNow())
		{
			return false;
		}

		// Set current pet skill
		_owner.setCurrentPetSkill(skill, forceUse, dontMove);

		//************************************* Check Target *******************************************

		// Get the target for the skill
		L2Object target;

		switch(skill.getTargetType())
		{
			// OWNER_PET should be cast even if no target has been found
			case TARGET_OWNER_PET:
				target = _owner;
				break;
			// PARTY, AURA, SELF should be cast even if no target has been found
			case TARGET_PARTY:
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_SELF:
			case TARGET_AURA_CORPSE_MOB:
				target = this;
				break;
			default:
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				if(target == null)
				{
					return false;
				}
		}

		// Check the validity of the target
		if(target == null)
		{
			sendPacket(SystemMessageId.TARGET_CANT_FOUND);
			return false;
		}

		//************************************* Check skill availability *******************************************

		// Check if this skill is enabled (e.g. reuse time)
		if(isSkillDisabled(skill))
		{
			sendPacket(SystemMessageId.PET_SKILL_CANNOT_BE_USED_RECHARCHING);
			return false;
		}

		//************************************* Check Consumables *******************************************

		// Check if the summon has enough MP
		if(getCurrentMp() < getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill))
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_MP);
			return false;
		}

		// Check if the summon has enough HP
		if(getCurrentHp() <= skill.getHpConsume())
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.NOT_ENOUGH_HP);
			return false;
		}

		//************************************* Check Summon State *******************************************

		// Check if this is offensive magic skill
		if(skill.isOffensive())
		{
			if(isInsidePeaceZone(this, target) && _owner != null && !_owner.getAccessLevel().allowPeaceAttack())
			{
				// If summon or target is in a peace zone, send a system message TARGET_IN_PEACEZONE
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				return false;
			}

			if(_owner != null && _owner.getOlympiadController().isParticipating() && !_owner.getOlympiadController().isPlayingNow())
			{
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFail
				sendActionFailed();
				return false;
			}

			if(target.getActingPlayer() != null && _owner.getSiegeSide() != PlayerSiegeSide.NONE && _owner.isInsideZone(L2Character.ZONE_SIEGE) && target.getActingPlayer().getSiegeSide() == _owner.getSiegeSide() && !target.getActingPlayer().equals(_owner) && target.getActingPlayer().getActiveSiegeId() == _owner.getActiveSiegeId())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS));
				sendActionFailed();
				return false;
			}

			// Check if the target is attackable
			if(target instanceof L2DoorInstance)
			{
				if(!((L2DoorInstance) target).isAttackable(_owner))
				{
					return false;
				}
			}
			else
			{
				if(!target.isAttackable() && _owner != null && !_owner.getAccessLevel().allowPeaceAttack())
				{
					return false;
				}

				// Check if a Forced ATTACK is in progress on non-attackable target
				if(!target.isAutoAttackable(this) && !forceUse &&
					skill.getTargetType() != L2TargetType.TARGET_AURA &&
					skill.getTargetType() != L2TargetType.TARGET_FRONT_AURA &&
					skill.getTargetType() != L2TargetType.TARGET_BEHIND_AURA &&
					skill.getTargetType() != L2TargetType.TARGET_CLAN &&
					skill.getTargetType() != L2TargetType.TARGET_ALLY &&
					skill.getTargetType() != L2TargetType.TARGET_PARTY &&
					skill.getTargetType() != L2TargetType.TARGET_SELF)
				{
					return false;
				}
			}
		}

		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		return true;
	}

	@Override
	@Nullable
	public PvPFlagController getPvPFlagController()
	{
		return _owner != null ? _owner.getPvPFlagController() : null;
	}

	@Override
	public void restoreEffects()
	{
		boolean isPet = this instanceof L2PetInstance;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(isPet ? L2PetInstance.RESTORE_SKILL_SAVE : L2SummonInstance.RESTORE_SKILL_SAVE);
			if(isPet)
			{
				statement.setInt(1, getControlObjectId());
			}
			else if(_restoredObjectId > 0)
			{
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, _owner.getClassIndex());
				statement.setInt(3, _restoredObjectId);
			}
			else
			{
				return;
			}

			rset = statement.executeQuery();

			while(rset.next())
			{
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");

				L2Skill skill = SkillTable.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
				if(skill == null)
				{
					continue;
				}

				if(skill.hasEffects())
				{
					Env env = new Env();
					env.setPlayer(this);
					env.setTarget(this);
					env.setSkill(skill);
					L2Effect ef;
					for(EffectTemplate et : skill.getEffectTemplates())
					{
						ef = et.getEffect(env);
						if(ef != null)
						{
							ef.setCount(effectCount);
							ef.setFirstTime(effectCurTime);
							ef.scheduleEffect();
						}
					}
				}
			}
			statement.clearParameters();

			statement = con.prepareStatement(isPet ? L2PetInstance.DELETE_SKILL_SAVE : L2SummonInstance.DELETE_SKILL_SAVE);
			if(isPet)
			{
				statement.setInt(1, getControlObjectId());
			}
			else if(_restoredObjectId > 0)
			{
				statement.setInt(1, _owner.getObjectId());
				statement.setInt(2, _owner.getClassIndex());
				statement.setInt(3, _restoredObjectId);
			}
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore " + this + " active effect data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void unSummon(boolean ignoreDeathAndVis)
	{
		if(unSummon(_owner, ignoreDeathAndVis))
		{
			getLocationController().decay();
		}
	}

	protected boolean unSummon(L2PcInstance owner, boolean ignoreDeathAndVis)
	{
		if(ignoreDeathAndVis || isVisible() && !isDead())
		{
			_isUnsummoningNow = true;
			getAI().stopFollow();
			owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
			L2Party party;
			if((party = owner.getParty()) != null)
			{
				party.broadcastPacket(owner, new ExPartyPetWindowDelete(this));
			}

			if(getInventory() != null && getInventory().getSize() > 0)
			{
				_owner.setPetInvItems(true);
				sendPacket(SystemMessageId.ITEMS_IN_PET_INVENTORY);
			}
			else
			{
				_owner.setPetInvItems(false);
			}

			store();
			storeEffect(true);

			// Stop AI tasks
			if(hasAI())
			{
				getAI().stopAITask();
			}

			for(L2Effect effect : getAllEffects())
			{
				for(L2Effect ownerEffect : _owner.getAllEffects())
				{
					if(ownerEffect.equals(effect))
					{
						removeEffect(effect);
					}
				}
			}
			stopAllEffects();
			L2WorldRegion oldRegion = getLocationController().getWorldRegion();

			if(oldRegion != null)
			{
				oldRegion.removeFromZones(this);
			}
			getKnownList().removeAllKnownObjects();
			setTarget(null);
			for(int itemId : owner.getAutoSoulShot())
			{
				String handler = ((L2EtcItem) ItemTable.getInstance().getTemplate(itemId)).getHandlerName();
				if(handler != null && handler.contains("Beast") && owner.getPets().size() == 1)
				{
					owner.disableAutoShot(itemId);
				}

			}
			owner.deletePet(this);
			return true;
		}
		return false;
	}

	public int getAttackRange()
	{
		return _attackRange;
	}

	public void setAttackRange(int range)
	{
		if(range < 36)
		{
			range = 36;
		}
		_attackRange = range;
	}

	public boolean getFollowStatus()
	{
		return _follow;
	}

	public void setFollowStatus(boolean state)
	{
		_follow = state;
		if(_follow)
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _owner);
		}
		else
		{
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null);
		}
	}

	public boolean isDefendingMode()
	{
		return _defendingMode;
	}

	public void setDefendingMode(boolean state)
	{
		_defendingMode = state;
	}

	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return _owner;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if(activeChar == _owner)
		{
			activeChar.sendPacket(new PetInfo(this, 0));
			updateEffectIcons(true);
			if(this instanceof L2PetInstance)
			{
				activeChar.sendPacket(new PetItemList((L2PetInstance) this));
			}
		}
		else
		{
            if (isPet())
            {
                activeChar.sendPacket(new ExPetInfo(this, activeChar, 0));
            }
            else
            {
                activeChar.sendPacket(new SummonInfo(this, activeChar, 0));
            }
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + '(' + getNpcId() + ") Owner: " + _owner;
	}

	@Override
	public boolean isSummon()
	{
		return true;
	}

	@Override
	public L2Summon getSummonInstance()
	{
		return this;
	}

	public int getChargedSoulShot()
	{
		return _chargedSoulShot;
	}

	public void setChargedSoulShot(int shotType)
	{
		_chargedSoulShot = shotType;
	}

	public int getChargedSpiritShot()
	{
		return _chargedSpiritShot;
	}

	public void setChargedSpiritShot(int shotType)
	{
		_chargedSpiritShot = shotType;
	}

	public int getControlObjectId()
	{
		return 0;
	}

	public L2Weapon getActiveWeapon()
	{
		return null;
	}

	@Override
	public PetInventory getInventory()
	{
		return null;
	}

	@Override
	public void onTeleported()
	{
		_state = 0; // teleported
		super.onTeleported();
		sendPacket(new TeleportToLocation(this, getLocationController().getX(), getLocationController().getY(), getLocationController().getZ(), getLocationController().getHeading()));
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		if(_owner != null)
		{
			mov.setInvisible(_owner.getAppearance().getInvisible());
		}

		super.broadcastPacket(mov);
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		if(_owner != null)
		{
			mov.setInvisible(_owner.getAppearance().getInvisible());
		}

		super.broadcastPacket(mov, radiusInKnownlist);
	}

	@Override
	public void broadcastStatusUpdate()
	{
		super.broadcastStatusUpdate();
		updateAndBroadcastStatus(1);
	}

	@Override
	public void doCast(L2Skill skill)
	{
		L2PcInstance actingPlayer = _owner;

		if(!actingPlayer.checkPvpSkill(getTarget(), skill, true) && !actingPlayer.getAccessLevel().allowPeaceAttack())
		{
			// Send a System Message to the L2PcInstance
			actingPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			actingPlayer.sendActionFailed();
			return;
		}

		super.doCast(skill);
	}

	@Override
	public void doRevive()
	{
		_owner.removeReviving();
		super.doRevive();

		DecayTaskManager.getInstance().cancelDecayTask(this);
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
	}

	@Override
	public L2CharacterAI getAI()
	{
		L2CharacterAI ai = _ai; // copy handle
		if(ai == null)
		{
			synchronized(this)
			{
				if(_ai == null)
				{
					_ai = new L2SummonAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public void setIsImmobilized(boolean value)
	{
		super.setIsImmobilized(value);

		if(value)
		{
			_previousFollowStatus = _follow;
			// if immobilized temporarly disable follow mode
			if(_previousFollowStatus)
			{
				setFollowStatus(false);
			}
		}
		else
		{
			// if not more immobilized restore previous follow mode
			setFollowStatus(_previousFollowStatus);
		}
	}

	/**
	 * @return {@code true} if the L2Summon is invulnerable or if the summoner is in spawn protection.
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || _owner.isSpawnProtected();
	}

	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	@Override
	public void stopAllEffects()
	{
		super.stopAllEffects();
		updateAndBroadcastStatus(1);
	}

	@Override
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(1);
	}

	@Override
	public void updateAbnormalEffect()
	{
        for (L2PcInstance player : getKnownList().getKnownPlayers().values())
        {
            if (isPet())
            {
                player.sendPacket(new ExPetInfo(this, player, 1));
            }
            else
            {
                player.sendPacket(new SummonInfo(this, player, 1));
            }
        }
	}

	@Override
	public boolean isInCombat()
	{
		return _owner != null && _owner.isInCombat();
	}

    public boolean isTargetable()
    {
        return true;
    }

	@Override
	public boolean isAttackingNow()
	{
		return isInCombat();
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
	public void onSpawn()
	{
		super.onSpawn();
		_summonTime = System.currentTimeMillis();
		setFollowStatus(true);
		updateAndBroadcastStatus(0);
		sendPacket(new RelationChanged(this, _owner.getRelation(_owner), false));

		for(L2PcInstance player : _owner.getKnownList().getKnownPlayersInRadius(800))
		{
			player.sendPacket(new RelationChanged(this, _owner.getRelation(player), isAutoAttackable(player)));
		}

		L2Party party = _owner.getParty();
		if(party != null)
		{
			party.broadcastPacket(_owner, new ExPartyPetWindowAdd(this));
		}
		HookManager.getInstance().notifyEvent(HookType.ON_SUMMON_SPAWN, getHookContainer(), this);
		// if someone comes into range now, the animation shouldnt show any more
		setShowSummonAnimation(false); // addVisibleObject created the info packets with summon animation
		_restoreSummon = false;
		_owner.sendPacket(new PartySpelled(this));

		List<Integer> copySkills = new FastList<>();

		// Charater to summon effects gift (only that can be stolen)
		for(L2Effect effect : _owner.getAllEffects())
		{
			if(effect == null)
			{
				continue;
			}

			boolean addEffect = true;
			for(L2Effect existed : getAllEffects())
			{
				if(effect.equals(existed))
				{
					addEffect = false;
				}
			}

			if(addEffect && effect.canBeStolen())
			{
				// Копируем эффекты
				L2Skill skill = effect.getSkill();
				if(copySkills.contains(skill.getReuseHashCode()))
				{
					continue;
				}

				copySkills.add(skill.getReuseHashCode());

				Env env = new Env();
				env.setPlayer(_owner);
				env.setTarget(this);
				env.setSkill(skill);
				L2Effect ef;
				if(skill.getEffectTemplates() != null)
				{
					for(EffectTemplate et : skill.getEffectTemplates())
					{
						ef = et.getEffect(env);
						if(ef != null)
						{
							ef.setCount(effect.getCount());
							ef.setFirstTime(effect.getTime());
							ef.scheduleEffect();
						}
					}
				}
			}
		}
		updateEffectIcons();
	}

	@Override
	public boolean onDecay()
	{
		return super.onDecay();
	}

	@Override
	public boolean onDelete()
	{
		_owner.sendPacket(new PetDelete(getSummonType(), getObjectId()));
		return unSummon(_owner, true) && super.onDelete();
	}

	/**
	 * @return {@code true} if the L2Character has a Party in progress.
	 */
	@Override
	public boolean isInParty()
	{
		if(_owner == null)
		{
			return false;
		}
		return _owner.getParty() != null;
	}

	/**
	 * @return {@code true} the L2Party object of its L2PcInstance owner or null.
	 */
	@Override
	public L2Party getParty()
	{
		if(_owner == null)
		{
			return null;
		}

		return _owner.getParty();
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, L2Skill skill)
	{
		super.reduceCurrentHp(damage, attacker, skill);
		if(_owner != null && attacker != null)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RECEIVED_DAMAGE_OF_S3_FROM_C2).addNpcName(this).addCharName(attacker).addNumber((int) damage));
			if(!attacker.equals(this))
			{
				HookManager.getInstance().notifyEvent(HookType.ON_ATTACK, _owner.getHookContainer(), _owner, attacker, true);
				HookManager.getInstance().notifyEvent(HookType.ON_SUMMON_ATTACKED, getHookContainer(), this, attacker);
			}
		}
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if(miss || _owner == null)
		{
			return;
		}

		// Prevents the double spam of system messages, if the target is the owning player.
		if(target.getObjectId() != _owner.getObjectId())
		{
			if(pcrit || mcrit)
			{
				if(this instanceof L2SummonInstance)
				{
					sendPacket(SystemMessageId.CRITICAL_HIT_BY_SUMMONED_MOB);
				}
				else
				{
					sendPacket(SystemMessageId.CRITICAL_HIT_BY_PET);
				}
			}

			if(_owner.getOlympiadController().isOpponent(target) && !target.isInvul())
			{
				OlympiadGameManager.getInstance().notifyCompetitorDamage(_owner, damage);
			}

			if(target.isInvul()) //&& !(target instanceof L2NpcInstance))
			{
				sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
				broadcastPacket(new ExMagicAttackInfo(this, target, ExMagicAttackInfo.ATTACK_WAS_BLOCKED));
			}
			else
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_GAVE_C2_DAMAGE_OF_S3).addNpcName(this).addCharName(target).addNumber(damage));
			}
		}
	}

	@Override
	public void sendPacket(L2GameServerPacket mov)
	{
		if(_owner != null)
		{
			_owner.sendPacket(mov);
		}
	}

	@Override
	public void sendPacket(SystemMessageId id)
	{
		if(_owner != null)
		{
			_owner.sendPacket(id);
		}
	}

	protected void doPickupItem(L2Object object)
	{
	}

	public void setRestoreSummon(boolean val)
	{
	}

	public void updateAndBroadcastStatus(int val)
	{
		if(_owner == null || _isUnsummoningNow)
		{
			return;
		}

		sendPacket(new PetInfo(this, val));
		sendPacket(new PetStatusUpdate(this));
		if(isVisible())
		{
			broadcastNpcInfo(val);
		}
		L2Party party = _owner.getParty();
		if(party != null)
		{
			party.broadcastPacket(_owner, new ExPartyPetWindowUpdate(this));
		}
		updateEffectIcons(true);
	}

	private boolean isUnsummoningNow()
	{
		return _isUnsummoningNow;
	}

	public void broadcastNpcInfo(int val)
	{
        for (L2PcInstance player : getKnownList().getKnownPlayers().values())
        {
            if ((player == null) || (player == getOwner()))
            {
                continue;
            }
            if (isPet())
            {
                player.sendPacket(new ExPetInfo(this, player, val));
            }
            else
            {
                player.sendPacket(new SummonInfo(this, player, val));
            }

        }
	}

	public boolean isHungry()
	{
		return false;
	}

	public int getWeapon()
	{
		return getTemplate().getRightHand();
	}

	public int getArmor()
	{
		return 0;
	}

	public int getPointsToSummon()
	{
		return 0;
	}

	public boolean isUncontrollable()
	{
		return false;
	}

	/**
	 * Происходит, когда хозяина саммона атакуют.
	 * @param attacker инстанс атакующего обьекта
	 */
	public void onOwnerGotAttacked(L2Character attacker)
	{
		if(attacker == null || _owner == null)
		{
			return;
		}

		if(_defendingMode)
		{
			setTarget(attacker);
			getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
		}
	}

	public long getSummonTime()
	{
		return _summonTime;
	}

	/**
	 * @return {@code true} если а данный момент саммон в процессе вризыва. Полезно для блокировки действий существа, когда он все еще призывается.
	 */
	public boolean isSpawningNow()
	{
		return _summonTime == 0 || System.currentTimeMillis() - _summonTime < 4000;
	}

	public int getRestoredObjectId()
	{
		return _restoredObjectId;
	}

	public void setRestoredObjectId(int objectId)
	{
		_restoredObjectId = objectId;
	}

	public int getSummonState()
	{
		return _state;
	}

	public void setSummonState(int var)
	{
		_state = var;
	}

    public void setIsIncarnation(boolean val) {
        _isIncarnation = val;
    }

    public boolean isIncarnation() {
        return _isIncarnation;
    }

    public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{
		}

		public L2Summon getSummon()
		{
			return L2Summon.this;
		}

		public boolean isAutoFollow()
		{
			return getFollowStatus();
		}

		public void doPickupItem(L2Object object)
		{
			L2Summon.this.doPickupItem(object);
		}
	}
}