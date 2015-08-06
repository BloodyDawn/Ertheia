package dwo.gameserver.model.actor;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.BuyListData;
import dwo.gameserver.datatables.xml.TeleportListTable;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.handler.BypassCommandManager;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.HandlerParams.CommandWrapper;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.TownManager;
import dwo.gameserver.instancemanager.WalkingManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.controller.object.InstanceController;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.knownlist.NpcKnownList;
import dwo.gameserver.model.actor.stat.NpcStat;
import dwo.gameserver.model.actor.status.NpcStatus;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.actor.templates.L2NpcTemplate.AIType;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.L2TradeList;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.teleport.TeleportLocation;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.type.L2TownZone;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.*;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExChangeNPCState;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.NpcInfoAbnormalVisualEffect;
import dwo.gameserver.network.game.serverpackets.packet.tradelist.ExBuySellList;
import dwo.gameserver.taskmanager.manager.DecayTaskManager;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static dwo.gameserver.model.actor.ai.CtrlIntention.AI_INTENTION_ACTIVE;

public class L2Npc extends L2Character
{
	/**
	 * The interaction distance of the L2NpcInstance(is used as offset in MovetoLocation method)
	 */
	public static final int INTERACTION_DISTANCE = 205;
	/**
	 * Minimum interval between social packets
	 */
	private static final int _minimalSocialInterval = 6000;
	public boolean isEventMob;
	public boolean _soulshotcharged;
	public boolean _spiritshotcharged;
	public boolean _ssrecharged = true;
	public boolean _spsrecharged = true;
	protected RandomAnimationTask _rAniTask;
	protected boolean _isShowName;
	protected boolean _isTargetable;
	/**
	 * True if endDecayTask has already been called
	 */
	volatile boolean _isDecayed;
	/**
	 * The L2Spawn object that manage this L2NpcInstance
	 */
	private L2Spawn _spawn;
	/**
	 * The flag to specify if this L2NpcInstance is busy
	 */
	private boolean _isBusy;
	/**
	 * The busy message for this L2NpcInstance
	 */
	private String _busyMessage = "";
	/**
	 * The castle index in the array of L2Castle this L2NpcInstance belongs to
	 */
	private int _castleIndex = -2;
	/**
	 * The fortress index in the array of L2Fort this L2NpcInstance belongs to
	 */
	private int _fortIndex = -2;
	private boolean _isInTown;
	/**
	 * True if this L2Npc is autoattackable *
	 */
	private boolean _isAutoAttackable;
	/**
	 * Time of last social packet broadcast
	 */
	private long _lastSocialBroadcast;
	private Future<?> _watcherTask;
	private FastList<L2PcInstance> _watcherList = new FastList<L2PcInstance>().shared();
	private Future<?> _defenceOwnerTask;
	private boolean _isInSocialAction;
	private int _currentLHandId; // normally this shouldn't change from the template, but there exist exceptions
	private int _currentRHandId; // normally this shouldn't change from the template, but there exist exceptions
    private int _armorId;
	private int _currentEnchant; // normally this shouldn't change from the template, but there exist exceptions
	private double _currentCollisionHeight; // used for npc grow effect skills
	private double _currentCollisionRadius; // used for npc grow effect skills
	private int _soulshotamount;
	private int _spiritshotamount;
	private int _displayEffect;
	private int _customInt;
	private boolean _isNoAnimation;

	private L2Character owner;

	// Переменны AI
	private Map<String, Object> _aiVars = new HashMap<>();

    /**
	 * Constructor of L2NpcInstance (use L2Character constructor).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to set the _template of the L2Character (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)  </li>
	 * <li>Set the name of the L2Character</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it </li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @param template The L2NpcTemplate to apply to the NPC
	 *
	 */
	public L2Npc(int objectId, L2NpcTemplate template)
	{
		// Call the L2Character constructor to set the _template of the L2Character, copy skills from template to object
		// and link _calculators to NPC_STD_CALCULATOR
		super(objectId, template);
		initCharStatusUpdateValues();

		// initialize the "current" equipment
		_currentLHandId = getTemplate().getLeftHand();
		_currentRHandId = getTemplate().getRightHand();
        _armorId = getTemplate().getArmor();
		_currentEnchant = Config.ENABLE_RANDOM_ENCHANT_EFFECT ? Rnd.get(4, 21) : getTemplate().getEnchantEffect();
		// initialize the "current" collisions
		_currentCollisionHeight = getTemplate().getFCollisionHeight(this);
		_currentCollisionRadius = getTemplate().getFCollisionRadius(this);

		if(template == null)
		{
			_log.log(Level.ERROR, "No template for Npc. Please check your datapack is setup correctly.");
			return;
		}

		setName(template.getName());
	}

	public L2Character getOwner()
	{
		return owner;
	}

	//AI Recall

	/**
	 * Назначает владельца инстансу НПЦ
	 * @param player персонаж-владелец
	 */
	public void setOwner(L2Character player)
	{
		owner = player;
	}

	public int getSoulShot()
	{
		return getTemplate().getSoulShot();
	}

	public int getSpiritShot()
	{
		return getTemplate().getSpiritShot();
	}

	public int getSoulShotChance()
	{
		return getTemplate().getSoulShotChance();
	}

	public int getSpiritShotChance()
	{
		return getTemplate().getSpiritShotChance();
	}

	/**
	 * Зарядка соулшотом.
	 *
	 * @param ignoreItem Проигнорировать наличие соулшота.
	 * @return True, если соулшот успешно использован.
	 */
	public boolean useSoulShot(boolean ignoreItem)
	{
		if(_soulshotcharged)
		{
			return true;
		}
		if(_ssrecharged)
		{
			_soulshotamount = getSoulShot();
			_ssrecharged = false;
		}
		else if(_soulshotamount > 0 || ignoreItem)
		{
			if(ignoreItem || Rnd.getChance(getSoulShotChance()))
			{
				if(!ignoreItem)
				{
					_soulshotamount -= 1;
				}

				Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2154, 1, 0, 0), 600);
				_soulshotcharged = true;
			}
		}
		else
		{
			return false;
		}

		return _soulshotcharged;
	}

	public boolean useSpiritShot()
	{
		if(_spiritshotcharged)
		{
			return true;
		}
		if(_spsrecharged)
		{
			_spiritshotamount = getSpiritShot();
			_spsrecharged = false;
		}
		else if(_spiritshotamount > 0)
		{
			if(Rnd.getChance(getSpiritShotChance()))
			{
				_spiritshotamount -= 1;
				Broadcast.toSelfAndKnownPlayersInRadius(this, new MagicSkillUse(this, this, 2061, 1, 0, 0), 600);
				_spiritshotcharged = true;
			}
		}
		else
		{
			return false;
		}

		return _spiritshotcharged;
	}

	public int getEnemyRange()
	{
		return getTemplate().getEnemyRange();
	}

	public String getEnemyClan()
	{
		return getTemplate().getEnemyClan();
	}

	public int getClanRange()
	{
		return getTemplate().getClanRange();
	}

	public String getClan()
	{
		return getTemplate().getClan();
	}

	/**
	 * @return the primary attack.
	 */
	public int getPrimarySkillId()
	{
		return getTemplate().getPrimarySkillId();
	}

	public int getMinSkillChance()
	{
		return getTemplate().getMinSkillChance();
	}

	public int getMaxSkillChance()
	{
		return getTemplate().getMaxSkillChance();
	}

	public boolean getCanMove()
	{
		return getTemplate().getCanMove();
	}

	public int getIsChaos()
	{
		return getTemplate().getIsChaos();
	}

	public int getDodgeChance()
	{
		return getTemplate().getDodge();
	}

	public int getSSkillChance()
	{
		return getTemplate().getShortRangeChance();
	}

	public int getLSkillChance()
	{
		return getTemplate().getLongRangeChance();
	}

	public boolean hasLSkill()
	{
		return getTemplate().getLongRangeSkill() != 0;
	}

	public boolean hasSSkill()
	{
		return getTemplate().getShortRangeSkill() != 0;
	}

	public List<L2Skill> getLongRangeSkill()
	{
		List<L2Skill> skilldata = new ArrayList<>();
		if(getTemplate() == null || getTemplate().getLongRangeSkill() == 0)
		{
			return skilldata;
		}

		switch(getTemplate().getLongRangeSkill())
		{
			case -1:
				Collection<L2Skill> skills = getAllSkills();
				if(skills != null)
				{
					for(L2Skill sk : skills)
					{
						if(sk == null || sk.isPassive() || sk.getTargetType() == L2TargetType.TARGET_SELF)
						{
							continue;
						}

						if(sk.getCastRange() >= 200)
						{
							skilldata.add(sk);
						}
					}
				}
				break;
			case 1:
				if(getTemplate().getUniversalSkills() != null)
				{
					skilldata.addAll(getTemplate().getUniversalSkills().stream().filter(sk -> sk.getCastRange() >= 200).collect(Collectors.toList()));
				}
				break;
			default:
				skilldata.addAll(getAllSkills().stream().filter(sk -> sk.getId() == getTemplate().getLongRangeSkill()).collect(Collectors.toList()));
		}
		return skilldata;
	}

	public List<L2Skill> getShortRangeSkill()
	{
		List<L2Skill> skilldata = new ArrayList<>();
		if(getTemplate() == null || getTemplate().getShortRangeSkill() == 0)
		{
			return skilldata;
		}

		switch(getTemplate().getShortRangeSkill())
		{
			case -1:
				Collection<L2Skill> skills = getAllSkills();
				if(skills != null)
				{
					for(L2Skill sk : skills)
					{
						if(sk == null || sk.isPassive() || sk.getTargetType() == L2TargetType.TARGET_SELF)
						{
							continue;
						}
						if(sk.getCastRange() <= 200)
						{
							skilldata.add(sk);
						}
					}
				}
				break;
			case 1:
				if(getTemplate().getUniversalSkills() != null)
				{
					skilldata.addAll(getTemplate().getUniversalSkills().stream().filter(sk -> sk.getCastRange() <= 200).collect(Collectors.toList()));
				}
				break;
			default:
				skilldata.addAll(getAllSkills().stream().filter(sk -> sk.getId() == getTemplate().getShortRangeSkill()).collect(Collectors.toList()));
		}
		return skilldata;
	}

	/**
	 * Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance and create a new RandomAnimation Task.<BR><BR>
	 * @param animationId
	 */
	public void onRandomAnimation(int animationId)
	{
		// Send a packet SocialAction to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
		long now = System.currentTimeMillis();
		if(now - _lastSocialBroadcast > _minimalSocialInterval)
		{
			_lastSocialBroadcast = now;
			broadcastPacket(new SocialAction(getObjectId(), animationId));
			if(getTemplate().getIdleRandomSkillId() != null)
			{
				broadcastPacket(new MagicSkillUse(this, getTemplate().getIdleRandomSkillId(), 500, 1500));
			}
		}
	}

	/**
	 * Create a RandomAnimation Task that will be launched after the calculated delay.<BR><BR>
	 */
	public void startRandomAnimationTimer()
	{
		if(!hasRandomAnimation())
		{
			return;
		}

		int minWait = isMob() ? Config.MIN_MONSTER_ANIMATION : Config.MIN_NPC_ANIMATION;
		int maxWait = isMob() ? Config.MAX_MONSTER_ANIMATION : Config.MAX_NPC_ANIMATION;

		// Calculate the delay before the next animation
		int interval = Rnd.get(minWait, maxWait) * 1000;

		// Create a RandomAnimation Task that will be launched after the calculated delay
		_rAniTask = new RandomAnimationTask();
		ThreadPoolManager.getInstance().scheduleGeneral(_rAniTask, interval);
	}

	/**
	 * @return true if the server allows Random Animation.
	 */
	public boolean hasRandomAnimation()
	{
		return Config.MAX_NPC_ANIMATION > 0 && getAiType() != AIType.CORPSE && !_isNoAnimation;
	}

	/**
	 * @return the generic Identifier of this L2NpcInstance contained in the L2NpcTemplate.
	 */
	public int getNpcId()
	{
		return getTemplate().getNpcId();
	}

	/**
	 * <B><U> Concept</U> :</B><BR><BR>
	 * If a NPC belongs to a Faction, other NPC of the faction inside the Faction range will help it if it's attacked<BR><BR>
	 * @return the faction Identifier of this L2NpcInstance contained in the L2NpcTemplate.
	 */
	//@Deprecated
	public String getFactionId()
	{
		return getClan();
	}

	/**
	 * @return True if the L2NpcInstance is aggressive (ex : L2MonsterInstance in function of aggroRange).
	 */
	public boolean isAggressive()
	{
		return false;
	}

	/**
	 * @return the Aggro Range of this L2NpcInstance contained in the L2NpcTemplate.
	 */
	public int getAggroRange()
	{
		return getTemplate().getAggroRange();
	}

	/**
	 * @return the Faction Range of this L2NpcInstance contained in the L2NpcTemplate.
	 */
	//@Deprecated
	public int getFactionRange()
	{
		return getClanRange();
	}

	/**
	 * <B><U> Values </U> :</B><BR>
	 * <BR>
	 * <li> object is a L2FolkInstance : 0 (don't remember it) </li>
	 * <li> object is a L2Character : 0 (don't remember it) </li>
	 * <li> object is a L2PlayableInstance : 1500 </li>
	 * <li> others : 500 </li>
	 * <BR>
	 * <BR>
	 *
	 * <B><U> Override in </U> :</B><BR>
	 * <BR>
	 * <li> L2Attackable</li>
	 * <BR>
	 * <BR>
	 *
	 * @param object The Object to add to _knownObject
	 * @return the distance under which the object must be add to _knownObject in function of the object type.
	 */
	public int getDistanceToWatchObject(L2Object object)
	{
		if(object instanceof L2NpcInstance || !(object instanceof L2Character))
		{
			return 0;
		}

		if(object instanceof L2Playable)
		{
			return 1500;
		}

		return 500;
	}

	/**
	 * <B><U> Values </U> :</B><BR><BR>
	 * <li> object is not a L2Character : 0 (don't remember it) </li>
	 * <li> object is a L2FolkInstance : 0 (don't remember it)</li>
	 * <li> object is a L2PlayableInstance : 3000 </li>
	 * <li> others : 1000 </li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable</li><BR><BR>
	 *
	 * @param object The Object to remove from _knownObject
	 * @return the distance after which the object must be remove from _knownObject in function of the object type.
	 */
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}

	/**
	 * Return False.<BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2MonsterInstance : Check if the attacker is not another L2MonsterInstance</li>
	 * <li> L2PcInstance</li><BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		return _isAutoAttackable;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if (isVisible())
        {
            if(activeChar == null)
            {
                return;
            }

            if (getRunSpeed() == 0)
            {
                activeChar.sendPacket(new ServerObjectInfo(this, activeChar));
            }
            else
            {
                activeChar.sendPacket(new NpcInfo(this));
            }
        }
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": " + getTemplate().getName() + '(' + getNpcId() + ')' + '[' + getObjectId() + ']';
	}

	@Override
	public boolean isNpc()
	{
		return true;
	}

	@Override
	public L2Npc getNpcInstance()
	{
		return this;
	}

	@Override
	public InstanceController getInstanceController()
	{
		if(_instanceController == null)
		{
			_instanceController = new dwo.gameserver.model.actor.controller.npc.InstanceController(this);
		}

		return _instanceController;
	}

	public void setAutoAttackable(boolean flag)
	{
		_isAutoAttackable = flag;
	}

	/**
	 * @return the Identifier of the item in the left hand of this L2NpcInstance contained in the L2NpcTemplate.
	 */
	public int getLeftHandItem()
	{
		return _currentLHandId;
	}

	/**
	 * @return the Identifier of the item in the right hand of this L2NpcInstance contained in the L2NpcTemplate.
	 */
	public int getRightHandItem()
	{
		return _currentRHandId;
	}

    public int getArmorId()
    {
        return _armorId;
    }

	public int getEnchantEffect()
	{
		return _currentEnchant;
	}

	/**
	 * @return the busy status of this L2NpcInstance.
	 */
	public boolean isBusy()
	{
		return _isBusy;
	}

	/**
	 * @param isBusy the busy status of this L2Npc
	 */
	public void setBusy(boolean isBusy)
	{
		_isBusy = isBusy;
	}

	/**
	 * @return the busy message of this L2NpcInstance.
	 */
	public String getBusyMessage()
	{
		return _busyMessage;
	}

	/**
	 * @param message the busy message of this L2Npc.
	 */
	public void setBusyMessage(String message)
	{
		_busyMessage = message;
	}

	/**
	 * @return {@code true} if this L2Npc instance can be warehouse manager.
	 */
	public boolean isWarehouse()
	{
		return false;
	}

	public boolean canTarget(L2PcInstance player)
	{
		if(player.isOutOfControl())
		{
			player.sendActionFailed();
			return false;
		}
		if(player.isLockedTarget() && !player.getLockedTarget().equals(this))
		{
			player.sendPacket(SystemMessageId.FAILED_CHANGE_TARGET);
			player.sendActionFailed();
			return false;
		}
		if(!isTargetable())
		{
			player.sendActionFailed();
			return false;
		}
		// TODO: More checks...

		return true;
	}

	public boolean canInteract(L2PcInstance player)
	{
		// TODO: NPC busy check etc...

		if(player.isCastingNow() || player.isCastingSimultaneouslyNow())
		{
			return false;
		}
		if(player.isDead() || player.isFakeDeath())
		{
			return false;
		}
		if(player.isSitting())
		{
			return false;
		}
		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			return false;
		}
		if(!isInsideRadius(player, INTERACTION_DISTANCE, true, false))
		{
			return false;
		}
		if(player.getInstanceId() != getInstanceId() && player.getInstanceId() != -1)
		{
			return false;
		}
		return !_isBusy;
	}

	/**
	 * @return the L2Castle this L2NpcInstance belongs to.
	 */
	public Castle getCastle()
	{
		// Get castle this NPC belongs to (excluding L2Attackable)
		if(_castleIndex < 0)
		{
			L2TownZone town = TownManager.getTown(getX(), getY(), getZ());

			if(town != null)
			{
				_castleIndex = CastleManager.getInstance().getCastleIndex(town.getTaxById());
			}

			if(_castleIndex < 0)
			{
				_castleIndex = CastleManager.getInstance().findNearestCastleIndex(this);
			}
			else
			{
				_isInTown = true; // Npc was spawned in town
			}
		}

		if(_castleIndex < 0)
		{
			return null;
		}

		return CastleManager.getInstance().getCastles().get(_castleIndex);
	}

	/**
	 * Return closest castle in defined distance
	 *
	 * @param maxDistance long
	 * @return Castle
	 */
	public Castle getCastle(long maxDistance)
	{
		int index = CastleManager.getInstance().findNearestCastleIndex(this, maxDistance);

		if(index < 0)
		{
			return null;
		}

		return CastleManager.getInstance().getCastles().get(index);
	}

	/**
	 * @return ClanHallSiegable closest siegable clanhall
	 */
	public ClanHallSiegable getConquerableHall()
	{
		return ClanHallSiegeManager.getInstance().getNearbyClanHall(getX(), getY(), 10000);
	}

	/***
	 * @return ClanHall ближайший к NPC
	 */
	public ClanHall getClanHall()
	{
		return ClanHallManager.getInstance().getNearbyClanHall(getX(), getY(), 500);
	}

	/**
	 * @return the L2Fort this L2NpcInstance belongs to.
	 */
	public Fort getFort()
	{
		// Get Fort this NPC belongs to (excluding L2Attackable)
		if(_fortIndex < 0)
		{
			Fort fort = FortManager.getInstance().getFort(getX(), getY(), getZ());
			if(fort != null)
			{
				_fortIndex = FortManager.getInstance().getFortIndex(fort.getFortId());
			}

			if(_fortIndex < 0)
			{
				_fortIndex = FortManager.getInstance().findNearestFortIndex(this);
			}
		}

		if(_fortIndex < 0)
		{
			return null;
		}

		return FortManager.getInstance().getForts().get(_fortIndex);
	}

	/**
	 * Return closest Fort in defined distance
	 * @param maxDistance long
	 * @return Fort
	 */
	public Fort getFort(long maxDistance)
	{
		int index = FortManager.getInstance().findNearestFortIndex(this, maxDistance);

		if(index < 0)
		{
			return null;
		}

		return FortManager.getInstance().getForts().get(index);
	}

	public boolean getIsInTown()
	{
		if(_castleIndex < 0)
		{
			getCastle();
		}

		return _isInTown;
	}

	/**
	 * Open a quest or chat window on client with the text of the L2NpcInstance in function of the command.<BR><BR>
	 *
	 * <B><U> Example of use </U> :</B><BR><BR>
	 * <li> Client packet : RequestBypassToServer</li><BR><BR>
	 * @param player
	 * @param command The command string received from client
	 */
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		//if (canInteract(player))
		if(_isBusy && !_busyMessage.isEmpty())
		{
			player.sendActionFailed();

			NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(player.getLang(), "npcbusy.htm");
			html.replace("%busymessage%", _busyMessage);
			html.replace("%npcname%", getName());
			html.replace("%playername%", player.getName());
			player.sendPacket(html);
		}
		else
		{
			CommandWrapper wrapper = HandlerParams.parseCommand(command);
			BypassCommandManager.getInstance().execute(new BypassHandlerParams(player, command, wrapper.getCommand(), this, wrapper.getArgs(), wrapper.getQueryArgs()));
		}
	}

	/**
	 * Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance.<BR><BR>
	 *
	 * @param player  The L2PcInstance who talks with the L2NpcInstance
	 * @param content The text of the L2NpcMessage
	 */
	public void insertObjectIdAndShowChatWindow(L2PcInstance player, String content)
	{
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		content = content.replaceAll("%objectId%", String.valueOf(getObjectId()));
		NpcHtmlMessage npcReply = new NpcHtmlMessage(getObjectId());
		npcReply.setHtml(content);
		player.sendPacket(npcReply);
	}

	/**
	 * Return the pathfile of the selected HTML file in function of the npcId and of the page number.<BR><BR>
	 * <p/>
	 * <B><U> Format of the pathfile </U> :</B><BR><BR>
	 * <li> if the file exists on the server (page number = 0) : <B>default/12006.htm</B> (npcId-page number)</li>
	 * <li> if the file exists on the server (page number > 0) : <B>default/12006-1.htm</B> (npcId-page number)</li>
	 * <li> if the file doesn't exist on the server : <B>npcdefault.htm</B> (message : "I have nothing to say to you")</li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2GuardInstance : Set the pathfile to guard/12006-1.htm (npcId-page number)</li><BR><BR>
	 *
	 * @param npcId The Identifier of the L2NpcInstance whose text must be display
	 * @param val   The number of the page to display
	 */
	public String getHtmlPath(int npcId, int val)
	{
		String pom;

		pom = val == 0 ? String.valueOf(npcId) : npcId + "-" + val;

		String temp = "default/" + pom + ".htm";

		if(HtmCache.getInstance().containsHtml(temp))
		{
			return temp;
		}
		temp = "default/" + getServerName() + "001.htm";
		if(HtmCache.getInstance().containsHtml(temp))
		{
			return temp;
		}

		// If the file is not found, the standard message "I have nothing to say to you" is returned
		return "default/noquest.htm";
	}

	public void showChatWindow(L2PcInstance player)
	{
		showChatWindow(player, 0);
	}

	/**
	 * @param player
	 * @return boolean true if html exists
	 */
	private boolean showPkDenyChatWindow(L2PcInstance player)
	{
		String html = HtmCache.getInstance().getHtm(player.getLang(), "default/" + getServerName() + "003.htm");

		if(html != null)
		{
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendActionFailed();
			return true;
		}
		html = HtmCache.getInstance().getHtm(player.getLang(), "default/" + getServerName() + "006.htm");
		if(html != null)
		{
			NpcHtmlMessage pkDenyMsg = new NpcHtmlMessage(getObjectId());
			pkDenyMsg.setHtml(html);
			player.sendPacket(pkDenyMsg);
			player.sendActionFailed();
			return true;
		}

		return false;
	}

	/**
	 * Open a chat window on client with the text of the L2NpcInstance.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Get the text of the selected HTML file in function of the npcId and of the page number </li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance </li>
	 * <li>Send a Server->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet </li><BR>
	 *
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param val    The number of the page of the L2NpcInstance to display
	 */
	public void showChatWindow(L2PcInstance player, int val)
	{
		// Не показываем диалог для NPC из соответствующего конфига
		if(Config.NON_TALKING_NPCS.contains(getNpcId()))
		{
			player.sendActionFailed();
			return;
		}
		if(player.isCursedWeaponEquipped() && !(player.getTarget().isNpc() && ((L2Npc) player.getTarget()).getTemplate().getDoorList().isEmpty()))
		{
			player.setTarget(player);
			return;
		}
		if(player.hasBadReputation())
		{
			if(showPkDenyChatWindow(player))
			{
				return;
			}
		}

		if(getTemplate().isType("L2Auctioneer") && val == 0)
		{
			return;
		}

		int npcId = getTemplate().getNpcId();

		String filename = getHtmlPath(npcId, val);

		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%name%", getName());
		html.replace("%playername%", player.getName());

		player.sendPacket(html);

		// Send a Server->Client ActionFail to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	/**
	 * Open a chat window on client with the text specified by the given file name and path,<BR>
	 * relative to the datapack root.
	 * <BR><BR>
	 * Added by Tempy
	 *
	 * @param player   The L2PcInstance that talk with the L2NpcInstance
	 * @param filename The filename that contains the text to send
	 */
	public void showChatWindow(L2PcInstance player, String filename)
	{
		// Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(player.getLang(), filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));

		// L2jS - Addons Custom
		html.replace("%name%", getName());
		html.replace("%player_name%", player.getName());

		player.sendPacket(html);

		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendActionFailed();
	}

	/**
	 * @return the Exp Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_XP).
	 */
	public float getExpReward()
	{
		return getTemplate().getRewardExp() * Config.RATE_XP;
	}

	/**
	 * @return the SP Reward of this L2NpcInstance contained in the L2NpcTemplate (modified by RATE_SP).
	 */
	public float getSpReward()
	{
		return getTemplate().getRewardSp() * Config.RATE_SP;
	}

	/**
	 * Kill the L2NpcInstance (the corpse disappeared after 7 seconds).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Create a DecayTask to remove the corpse of the L2NpcInstance after 7 seconds </li>
	 * <li>Set target to null and cancel Attack or Cast </li>
	 * <li>Stop movement </li>
	 * <li>Stop HP/MP/CP Regeneration task </li>
	 * <li>Stop all active skills effects in progress on the L2Character </li>
	 * <li>Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform </li>
	 * <li>Notify L2Character AI </li><BR><BR>
	 *
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2Attackable </li><BR><BR>
	 *
	 * @param killer The L2Character who killed it
	 *
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		if(!super.doDie(killer))
		{
			return false;
		}

		// normally this wouldn't really be needed, but for those few exceptions,
		// we do need to reset the weapons back to the initial templated weapon.
		_currentLHandId = getTemplate().getLeftHand();
		_currentRHandId = getTemplate().getRightHand();
        _armorId = getTemplate().getArmor();
		_currentCollisionHeight = getTemplate().getFCollisionHeight(this);
		_currentCollisionRadius = getTemplate().getFCollisionRadius(this);
		DecayTaskManager.getInstance().addDecayTask(this);

		// Notify the Quest Engine of the L2Attackable death if necessary
		try
		{
			L2PcInstance player = null;

			if(killer != null)
			{
				player = killer.getActingPlayer();
			}

			// Если соблюдены условия, то даем Энергию разрушения
			if(player != null)
			{
				if(Rnd.getChance(0.4) && player.isAwakened() && getLevel() >= 85)
				{
					String var = player.getVariablesController().get("destructionEnergy");
					if(var == null)
					{
						player.sendPacket(SystemMessageId.getSystemMessageId(3791));
						player.addItem(ProcessType.PICKUP, 35562, 1, this, true);
						player.getVariablesController().set("destructionEnergy", "1");
					}
					else
					{
						if(Integer.valueOf(var) == 1)
						{
							player.sendPacket(SystemMessageId.getSystemMessageId(3792));
							player.addItem(ProcessType.PICKUP, 35562, 1, this, true);
							player.getVariablesController().set("destructionEnergy", "2");
						}
					}
				}
			}

			if(getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
			{
				for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
				{
					quest.notifyKill(this, killer);
					if(player != null)
					{
						ThreadPoolManager.getInstance().scheduleEffect(new OnKillNotifyTask(this, quest, player, killer instanceof L2Summon), 5000);
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
		return true;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || !getCanMove() || getAiType() == AIType.CORPSE;
	}

	/**
	 * Return True if this L2NpcInstance is undead in function of the L2NpcTemplate.<BR><BR>
	 */
	@Override
	public boolean isUndead()
	{
		return getTemplate().isUndead();
	}

	@Override
	public NpcStat getStat()
	{
		return (NpcStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new NpcStat(this));
	}

	@Override
	public NpcStatus getStatus()
	{
		return (NpcStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new NpcStatus(this));
	}

	/** Return the L2NpcTemplate of the L2NpcInstance. */
	@Override
	public L2NpcTemplate getTemplate()
	{
		return (L2NpcTemplate) super.getTemplate();
	}

	/**
	 * Send a packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance.<BR><BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
		// Send a Server->Client packet NpcInfo with state of abnormal effect to all L2PcInstance in the _KnownPlayers of the L2NpcInstance
		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			if(getRunSpeed() == 0)
			{
				player.sendPacket(new ServerObjectInfo(this, player));
			}
			else
			{
				player.sendPacket(new NpcInfoAbnormalVisualEffect(this));
			}
		}
	}

	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}

	/**
	 * Return the weapon item equiped in the right hand of the L2NpcInstance or null.<BR><BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().getRightHand();

		if(weaponId < 1)
		{
			weaponId = _currentRHandId;

			if(weaponId < 1)
			{
				return null;
			}
		}

		// Get the weapon item equiped in the right hand of the L2NpcInstance
		L2Item item = ItemTable.getInstance().getTemplate(weaponId);

		if(!(item instanceof L2Weapon))
		{
			return null;
		}

		return (L2Weapon) item;
	}

	/**
	 * Return null (regular NPCs don't have weapons instancies).<BR><BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		// regular NPCs dont have weapons instancies
		return null;
	}

	/**
	 * Return the weapon item equiped in the left hand of the L2NpcInstance or null.<BR><BR>
	 */
	@Override
	public L2Weapon getSecondaryWeaponItem()
	{
		// Get the weapon identifier equiped in the right hand of the L2NpcInstance
		int weaponId = getTemplate().getLeftHand();

		if(weaponId < 1)
		{
			return null;
		}

		// Get the weapon item equiped in the right hand of the L2NpcInstance
		L2Item item = ItemTable.getInstance().getTemplate(getTemplate().getLeftHand());

		if(!(item instanceof L2Weapon))
		{
			return null;
		}

		return (L2Weapon) item;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();

		// initialize the "current" shots
		_soulshotamount = getTemplate().getSoulShot();
		_spiritshotamount = getTemplate().getSpiritShot();

		if(getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN) != null)
		{
			for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPAWN))
			{
				quest.notifySpawn(this);
			}
		}
	}

	/**
	 * Remove the L2NpcInstance from the world and update its spawn object (for a complete removal use the deleteMe method).<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2NpcInstance from the world when the decay task is launched </li>
	 * <li>Decrease its spawn counter </li>
	 * <li>Manage Siege task (killFlag, killCT) </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of L2World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 */
	@Override
	public boolean onDecay()
	{
		// If NPC is already decayed, guess its alright
		if(_isDecayed)
		{
			return true;
		}
		_isDecayed = true;

		// Remove the L2NpcInstance from the world when the decay task is launched
		super.onDecay();

		// Decrease its spawn counter
		if(_spawn != null)
		{
			_spawn.decreaseCount(this);
		}

		//Notify Walking Manager
		WalkingManager.getInstance().onDeath(this);
		return true;
	}

	/**
	 * Remove PROPERLY the L2NpcInstance from the world.<BR><BR>
	 *
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2NpcInstance from the world and update its spawn object </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2NpcInstance then cancel Attack or Cast and notify AI </li>
	 * <li>Remove L2Object object from _allObjects of L2World </li><BR><BR>
	 *
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR><BR>
	 *
	 */
	@Override
	public boolean onDelete()
	{
		L2WorldRegion oldRegion = getLocationController().getWorldRegion();

		try
		{
			getLocationController().decay();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed decayMe().", e);
		}
		try
		{
			if(_fusionSkill != null)
			{
				abortCast();
			}

			getKnownList().getKnownCharacters().stream().filter(character -> character.getFusionSkill() != null && character.getFusionSkill().getTarget().equals(this)).forEach(L2Character::abortCast);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}
		if(oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed removing cleaning knownlist.", e);
		}

		// Remove L2Object object from _allObjects of L2World
		WorldManager.getInstance().removeObject(this);

		return super.onDelete();
	}

	@Override
	public NpcKnownList getKnownList()
	{
		return (NpcKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new NpcKnownList(this));
	}

	@Override
	protected void notifyQuestEventSkillFinished(L2Skill skill, L2Object target)
	{
		try
		{
			if(getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED) != null)
			{
				L2PcInstance player = null;
				if(target != null)
				{
					player = target.getActingPlayer();
				}
				for(Quest quest : getTemplate().getEventQuests(Quest.QuestEventType.ON_SPELL_FINISHED))
				{
					quest.notifySpellFinished(this, player, skill);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	/**
	 * Return the Level of this L2NpcInstance contained in the L2NpcTemplate.<BR><BR>
	 */
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}

	@Override
	public boolean isWalker()
	{
		return WalkingManager.getInstance().isRegistered(this);
	}

	// Two functions to change the appearance of the equipped weapons on the NPC
	// This is only useful for a few NPCs and is most likely going to be called from AI

	/**
	 * @return the L2Spawn object that manage this L2NpcInstance.
	 */
	public L2Spawn getSpawn()
	{
		return _spawn;
	}

	/**
	 * Set the spawn of the L2NpcInstance.<BR><BR>
	 *
	 * @param spawn The L2Spawn that manage the L2NpcInstance
	 */
	public void setSpawn(L2Spawn spawn)
	{
		_spawn = spawn;
	}

	public boolean isDecayed()
	{
		return _isDecayed;
	}

	public void setDecayed(boolean decayed)
	{
		_isDecayed = decayed;
	}

	public void endDecayTask()
	{
		if(!_isDecayed)
		{
			DecayTaskManager.getInstance().cancelDecayTask(this);
			getLocationController().decay();
		}
	}

	public boolean isMob() // rather delete this check
	{
		return false; // This means we use MAX_NPC_ANIMATION instead of MAX_MONSTER_ANIMATION
	}

	public void setLHandId(int newWeaponId)
	{
		_currentLHandId = newWeaponId;
		updateAbnormalEffect();
	}

	public void setRHandId(int newWeaponId)
	{
		_currentRHandId = newWeaponId;
		updateAbnormalEffect();
	}

	public void setLRHandId(int newLWeaponId, int newRWeaponId)
	{
		_currentRHandId = newRWeaponId;
		_currentLHandId = newLWeaponId;
		updateAbnormalEffect();
	}

    public void setChest(int newChest)
    {
        _armorId = newChest;
        updateAbnormalEffect();
    }

	public void setEnchant(int newEnchantValue)
	{
		_currentEnchant = newEnchantValue;
		updateAbnormalEffect();
	}

	public boolean isTargetable()
	{
		return getTemplate().isTargetable() || _isTargetable;
	}

	public void setTargetable(boolean val)
	{
		_isTargetable = val;
	}

	public boolean isTargetableBase()
	{
		return getTemplate().isTargetable();
	}

	public boolean isShowName()
	{
		return getTemplate().showName() || _isShowName;
	}

	public void setShowName(boolean val)
	{
		_isShowName = val;
	}

	public boolean isShowNameBase()
	{
		return getTemplate().showName();
	}

	public double getCollisionHeight()
	{
		return _currentCollisionHeight;
	}

	public void setCollisionHeight(double height)
	{
		_currentCollisionHeight = height;
	}

	public double getCollisionRadius()
	{
		return _currentCollisionRadius;
	}

	public void setCollisionRadius(double radius)
	{
		_currentCollisionRadius = radius;
	}

	public L2Npc scheduleDespawn(long delay)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(this.new DespawnTask(), delay);
		return this;
	}

	public AIType getAiType()
	{
		return getTemplate().getAiType();
	}

	public int getDisplayEffect()
	{
		return _displayEffect;
	}

	public void setDisplayEffect(int val)
	{
		if(val != _displayEffect)
		{
			_displayEffect = val;
			broadcastPacket(new ExChangeNPCState(getObjectId(), val));
		}
	}

	public int getColorEffect()
	{
		return 0;
	}

	public int getCustomInt()
	{
		return _customInt;
	}

	public void setCustomInt(int val)
	{
		_customInt = val;
	}

	public void increaseCustomInt()
	{
		_customInt++;
	}

	public boolean isInSocialAction()
	{
		return _isInSocialAction;
	}

	public void setIsInSocialAction(boolean value)
	{
		_isInSocialAction = value;
	}

	public boolean isNoAnimation()
	{
		return _isNoAnimation;
	}

	public void setIsNoAnimation(boolean value)
	{
		_isNoAnimation = value;
	}

	public void startWatcherTask(int range)
	{
		if(_watcherTask == null)
		{
			_watcherTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WatcherTask(this, range), 5000, 5000);
		}
	}

	public void stopWatcherTask()
	{
		if(_watcherTask != null)
		{
			_watcherTask.cancel(true);
			_watcherTask = null;

			if(!_watcherList.isEmpty())
			{
				_watcherList.clear();
			}
		}
	}

	public FastList<L2PcInstance> getWatcherList()
	{
		return _watcherList;
	}

	public void startDefenceOwnerTask()
	{
		if(getOwner() == null)
		{
			return;
		}

		if(_defenceOwnerTask == null)
		{
			if(this instanceof L2Attackable)
			{
				_defenceOwnerTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new DefenceOwnerTask(getAttackable(), getOwner()), 5000, 5000);
			}
		}
	}

	public void stopDefenceOwnerTask()
	{
		if(_defenceOwnerTask != null)
		{
			_defenceOwnerTask.cancel(true);
			_defenceOwnerTask = null;
			owner = null;
		}
	}

	/**
	 * @param highestLevel int
	 * @return the level modifier for drop
	 */
	public int calculateLevelModifierForDrop(int highestLevel)
	{
		if(!isRaid() && Config.DEEPBLUE_DROP_RULES || isRaid() && Config.DEEPBLUE_DROP_RULES_RAID)
		{
			// Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
			if(!getAttackByList().isEmpty())
			{
				for(L2Character atkChar : getAttackByList())
				{
					if(atkChar != null && atkChar.getLevel() > highestLevel)
					{
						highestLevel = atkChar.getLevel();
					}
				}
			}

			// According to official data (Prima), deep blue mobs are 9 or more levels below players
			if(highestLevel - 9 >= getLevel())
			{
				return (highestLevel - (getLevel() + 8)) * 9;
			}
		}
		return 0;
	}

	/**
	 * Выводит html страничку с телепортами
	 * @param player игрок
	 * @param subListId номер подраздела в TeleportsData.xml у нпц
	 */
	public void showTeleportList(L2PcInstance player, int subListId)
	{
		// Проверка на трансформации
		if(player.getTransformationId() == 111 || player.getTransformationId() == 112 || player.getTransformationId() == 124)
		{
			String content = HtmCache.getInstance().getHtm(player.getLang(), "default/q194_noteleport.htm");
			if(content != null)
			{
				player.sendPacket(new NpcHtmlMessage(getObjectId(), content));
			}
			return;
		}

		// Если игрок ПК, то показываем ему стандартный диалог НПЦ "вы убийца бла бла бла"
		if(player.hasBadReputation())
		{
			String content = HtmCache.getInstance().getHtm(player.getLang(), "default/" + getServerName() + "003.htm");
			if(content != null)
			{
				player.sendPacket(new NpcHtmlMessage(getObjectId(), content));
			}
			return;
		}

		TeleportLocation[] list = TeleportListTable.getInstance().getTeleportLocationList(getNpcId(), subListId);

		StringBuilder sb = new StringBuilder();

		sb.append("<html><body>&$556;<br><br>");
		if(list != null)
		{
			int tpPointIndex = 0;
			int day;
			int hour;
			int price;
			for(TeleportLocation tl : list)
			{
				if(tl.getItemId() == PcInventory.ADENA_ID)
				{
					price = player.getLevel() <= 41 || Config.ALT_GAME_FREE_TELEPORT ? 0 : tl.getPrice();
					if(tl.getPrice() > 0 && price > 0)
					{
						day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
						hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
						if(day != 1 && day != 7 && (hour <= 8 || hour >= 24))
						{
							price /= 2;
						}
					}
					if(price == 0)
					{
						sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_tp ").append(subListId).append(' ').append(tpPointIndex).append("\" msg=\"811;F;").append(tl.getFstring()).append("\"><fstring>").append(tl.getFstring()).append("</fstring> </a><br1>");
					}
					else
					{
						sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_tp ").append(subListId).append(' ').append(tpPointIndex).append("\" msg=\"811;F;").append(tl.getFstring()).append("\"><fstring>").append(tl.getFstring()).append("</fstring> - ").append(price).append(" <fstring p1=\"\" p2=\"\" p3=\"\" p4=\"\" p5=\"\">1000308</fstring> </a><br1>");
					}
				}
				else
				{
					sb.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_tp ").append(subListId).append(' ').append(tpPointIndex).append("\" msg=\"811;F;").append(tl.getFstring()).append("\"><fstring>").append(tl.getFstring()).append("</fstring> - ").append(tl.getPrice()).append(" <fstring p1=\"\" p2=\"\" p3=\"\" p4=\"\" p5=\"\">1000454</fstring> </a><br1>");
				}
				tpPointIndex++;
			}
		}
		else
		{
			sb.append("Ссылка неисправна, сообщите администратору.");
		}

		sb.append("</body></html>");
		player.sendPacket(new NpcHtmlMessage(getObjectId(), sb.toString()));
		player.setLastTeleporterObjectId(getObjectId() + subListId);
	}

	public String getServerName()
	{
		return getTemplate().getServerName();
	}

	/**
	 * @param player L2PcInstance игрока
	 * @param andClanLeader {@code true} если игрок так же должен являться и кланлидером
	 * @return {@code true} если персонаж является владельцем резиденции (замок\форт\захватываемый кх)
	 */
	public boolean isMyLord(L2PcInstance player, boolean andClanLeader)
	{
		if(player.getClan() != null)
		{
			if(getClanHall() != null)
			{
				if(player.getClan() != null && getClanHall() != null)
				{
					if(player.getClanId() == getClanHall().getOwnerId())
					{
						return !(andClanLeader && !player.isClanLeader());
					}
				}
				return false;
			}
			else if(getConquerableHall() != null)
			{
				if(getConquerableHall().getOwnerId() == player.getClan().getClanId())
				{
					return !(andClanLeader && !player.isClanLeader());
				}
			}
			else if(getFort(10000) != null)
			{
				if(getFort().getOwnerClan() != null)
				{
					if(player.getClanId() == getFort().getOwnerClan().getClanId())
					{
						return !(andClanLeader && !player.isClanLeader());
					}
				}
			}
			else if(getCastle(10000) != null)
			{
				if(player.getClanId() == getCastle().getOwnerId())
				{
					return !(andClanLeader && !player.isClanLeader());
				}
			}
		}
		return false;
	}

	/***
	 * Открывает двери по указанным именам из списка в Template
	 * @param names список дверей, которые нужно открыть
	 */
	public void openMyDoors(String... names)
	{
		for(String doorName : names)
		{
			DoorGeoEngine.getInstance().getDoor(getTemplate().getDoorId(doorName));
		}
	}

	/***
	 * Закрывает двери по указанным именам из списка в Template
	 * @param names список дверей, которые нужно закрыть
	 */
	public void closeMyDoors(String... names)
	{
		for(String doorName : names)
		{
			DoorGeoEngine.getInstance().getDoor(getTemplate().getDoorId(doorName));
		}
	}

	/***
	 * Открытие всех дверей, связанных с NPC
	 */
	public void openMyDoors()
	{
		for(Integer doorId : getTemplate().getDoorList())
		{
			DoorGeoEngine.getInstance().getDoor(doorId).openMe();
		}
	}

	/***
	 * Открытие всех дверей, связанных с NPC
	 * @param secsToClose через сколько милисекунд дверь закроется
	 */
	public void openMyDoors(long secsToClose)
	{
		for(Integer doorId : getTemplate().getDoorList())
		{
			DoorGeoEngine.getInstance().getDoor(doorId).openMe(secsToClose);
		}
	}

	/***
	 * Закрытие всех дверей, связанных с NPC
	 */
	public void closeMyDoors()
	{
		for(Integer doorId : getTemplate().getDoorList())
		{
			DoorGeoEngine.getInstance().getDoor(doorId).closeMe();
		}
	}

	/***
	 * @param processType тип операции
	 * @return налог в процентах, который отойдет городу при совершении указанной операции
	 */
	public double getAdenaTownTaxRate(ProcessType processType)
	{
		if(getCastle() != null)
		{
			switch(getCastle().getCastleSide())
			{
				case LIGHT:
					if(processType == ProcessType.BUY)
					{
						return getCastle().getOwnerId() > 0 ? 0.2 : 0.3;
					}
					else
					{
						return getCastle().getOwnerId() > 0 ? 0.5 : 0.6;
					}
				case DARK:
					return processType == ProcessType.BUY ? 0.2 : 0.5;
			}
		}
		return 0;
	}

	/***
	 * @return общий налог города и замка на покупку предметов
	 */
	public double getAdenaTotalTaxRate(ProcessType processType)
	{
		return getAdenaTownTaxRate(processType) + getAdenaCastleTaxRate(processType);
	}

	public double getAdenaCastleTaxRate(ProcessType processType)
	{
		return getCastle() != null ? getCastle().getTaxRate(processType) : 0;
	}

	/**
	 * Send an "event" to all NPC's within given radius
	 * @param eventName - name of event
	 * @param radius - radius to send event
	 * @param reference - L2Object to pass, if needed
	 */
	public void broadcastEvent(String[] eventName, int radius, L2Object reference)
	{
		WorldManager.getInstance().getVisibleObjects(this, radius).stream().filter(obj -> obj.isNpc() && ((L2Npc) obj).getTemplate().getEventQuests(Quest.QuestEventType.ON_EVENT_RECEIVED) != null).forEach(obj -> {
			for(Quest quest : ((L2Npc) obj).getTemplate().getEventQuests(Quest.QuestEventType.ON_EVENT_RECEIVED))
			{
				quest.notifyEventReceived(eventName, this, (L2Npc) obj, reference);
			}
		});
	}

	/***
	 * Показать байлист с указанным номером указнному игроку
	 * @param player игрок, которому показываем байлист
	 * @param buylistIndex номер байлиста
	 */
	public void showBuyList(L2PcInstance player, int buylistIndex)
	{
		double taxRate = 0.5;

		player.tempInventoryDisable();

		L2TradeList list = BuyListData.getInstance().getBuyList(getNpcId(), buylistIndex);

		if(list != null)
		{
			player.sendPacket(new ExBuySellList(player, list, ProcessType.BUY, taxRate, false, player.getAdenaCount()));
            player.sendPacket(new ExBuySellList(player, list, ProcessType.SELL, taxRate, false, player.getAdenaCount()));
		}
		else
		{
			_log.log(Level.WARN, "possible client hacker: " + player.getName() + " attempting to buy from GM shop! < Ban him!");
			_log.log(Level.WARN, "buylist id:" + buylistIndex);
		}

		player.sendActionFailed();
	}

	public void setAiVar(String name, Object value)
	{
		_aiVars.put(name, value);
	}

	public void unsetAiVar(String name)
	{
		if(name == null)
		{
			return;
		}
		_aiVars.remove(name);
	}

	public Object getAiVar(String name)
	{
		if(_aiVars != null && _aiVars.containsKey(name))
		{
			return _aiVars.get(name);
		}
		return null;
	}

	public int getAiVarInt(String name)
	{
		if(_aiVars != null && _aiVars.containsKey(name))
		{
			if(_aiVars.get(name) instanceof Integer)
			{
				return (Integer) _aiVars.get(name);
			}
		}
		return 0;
	}

    public int getAiVarInt(String name, int def)
    {
        if(_aiVars != null && _aiVars.containsKey(name))
        {
            if(_aiVars.get(name) instanceof Integer)
            {
                return (Integer) _aiVars.get(name);
            }
        }
        return def;
    }

	public Map<String, Object> getVars()
	{
		return _aiVars;
	}

    protected static class OnKillNotifyTask implements Runnable
	{
		private final L2Npc _npc;
		private final Quest _quest;
		private final L2PcInstance _killer;
		private final boolean _isPet;

		public OnKillNotifyTask(L2Npc npc, Quest quest, L2PcInstance killer, boolean isPet)
		{
			_npc = npc;
			_quest = quest;
			_killer = killer;
			_isPet = isPet;
		}

		@Override
		public void run()
		{
			_quest.notifyKill(_npc, _killer, _isPet);
		}
	}

	/** Task launching the function onRandomAnimation() */
	protected class RandomAnimationTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				if(!equals(_rAniTask))
				{
					return; // Shouldn't happen, but who knows... just to make sure every active npc has only one timer.
				}
				if(isMob())
				{
					// Cancel further animation timers until intention is changed to ACTIVE again.
					if(getAI().getIntention() != AI_INTENTION_ACTIVE)
					{
						return;
					}
				}
				else
				{
					if(!isInActiveRegion()) // NPCs in inactive region don't run this task
					{
						return;
					}
				}

				if(!(isDead() || isStunned() || isSleeping() || isParalyzed() || isFlyUp() || isKnockBacked()))
				{
					int[] randomAnimations = getTemplate().getIdleRandomActionIds();
					onRandomAnimation(randomAnimations[Rnd.get(randomAnimations.length)]);
				}

				startRandomAnimationTimer();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	private class DespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isDecayed())
			{
				getLocationController().delete();
			}
		}
	}

	private class WatcherTask implements Runnable
	{
		L2Npc _npc;
		int _range;

		protected WatcherTask(L2Npc npc, int range)
		{
			_npc = npc;
			_range = range;
		}

		@Override
		public void run()
		{
			if(_npc == null)
			{
				return;
			}
			if(_npc.isDead())
			{
				_npc.stopWatcherTask();
			}

			// Get all characters in selected range
			Collection<L2PcInstance> knowList = _npc.getKnownList().getKnownPlayersInRadius(_range);
			// Get list of known and triggered already players
			List<L2PcInstance> watcherList = _npc.getWatcherList();

			// Check if all is ok with our knowlist
			if(knowList == null)
			{
				return;
			}

			// If knowlist is empty we can clear watchlist also if its not empty
			if(knowList.isEmpty())
			{
				if(!watcherList.isEmpty())
				{
					watcherList.clear();
				}
				return;
			}

			// First verify if player on our list is still in range
			for(L2PcInstance player : watcherList)
			{
				if(player == null)
				{
					continue;
				}
				if(!knowList.contains(player))
				{
					watcherList.remove(player);
				}
			}

			// Now check know list for new objects
			for(L2Character character : knowList)
			{
				if(character == null || character.isDead() || character.getInstanceId() != _npc.getInstanceId())
				{
					continue;
				}

				L2PcInstance player = character.getActingPlayer();
				if(player != null)
				{
					// Player can be invisible
					if(player.getObserverController().isObserving() || player.getAppearance().getInvisible() && !_npc.canSeeThroughSilentMove())
					{
						continue;
					}
					// Check if player is on our list (was triggered already)
					if(!watcherList.contains(player))
					{
						// Check if we really 'see' our target
						if(GeoEngine.getInstance().canSeeTarget(_npc, player))
						{
							// Add player to list - we need trigger quests only once
							watcherList.add(player);
							HookManager.getInstance().notifyEvent(HookType.ON_SEE_PLAYER, null, _npc, player);
						}
					}
				}
			}
		}
	}

	private class DefenceOwnerTask implements Runnable
	{
		L2Attackable _npc;
		L2Character _owner;

		protected DefenceOwnerTask(L2Attackable npc, L2Character owner)
		{
			_npc = npc;
			_owner = owner;
		}

		@Override
		public void run()
		{
			if(_owner == null || !Util.checkIfInRange(2000, _npc, _owner, true) || _owner.isDead())
			{
				_npc.stopDefenceOwnerTask();
			}
			else
			{
				if(_owner.getAI().getIntention() == CtrlIntention.AI_INTENTION_ATTACK || _owner.getAI().getIntention() == CtrlIntention.AI_INTENTION_CAST)
				{
					L2Object target = _owner.getTarget();
					if(target != null && target instanceof L2MonsterInstance && !((L2MonsterInstance) target).isDead())
					{
						L2MonsterInstance monster = (L2MonsterInstance) target;
						_npc.attackCharacter(monster);
					}
				}
				else
				{
					_npc.getAI().startFollow(_owner);
				}
			}
		}
	}

    public void showChatText(ChatType type, NpcStringId id)
    {
        this.broadcastPacket(new NS(this, type, id));
    }
}