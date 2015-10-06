package dwo.gameserver.model.actor.instance;

import dwo.config.Config;
import dwo.config.events.ConfigEvents;
import dwo.config.main.ConfigLocalization;
import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.LoginServerThread;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.cache.WarehouseCacheManager;
import dwo.gameserver.datatables.sql.AccountShareDataTable;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.CharSummonTable;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.datatables.xml.ClassTemplateTable;
import dwo.gameserver.datatables.xml.CommunityBuffTable;
import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import dwo.gameserver.datatables.xml.ExperienceTable;
import dwo.gameserver.datatables.xml.FishData;
import dwo.gameserver.datatables.xml.HennaTable;
import dwo.gameserver.datatables.xml.HennaTreeTable;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.PetDataTable;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.handler.ItemHandler;
import dwo.gameserver.instancemanager.CursedWeaponsManager;
import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.HeroManager;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.ItemsOnGroundAutoDestroyManager;
import dwo.gameserver.instancemanager.ItemsOnGroundManager;
import dwo.gameserver.instancemanager.MentorManager;
import dwo.gameserver.instancemanager.PartySearchingManager;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WeddingManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.instancemanager.games.HandysBlockCheckerManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Decoy;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.L2Trap;
import dwo.gameserver.model.actor.L2Vehicle;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.ai.L2CharacterAI;
import dwo.gameserver.model.actor.ai.L2PlayerAI;
import dwo.gameserver.model.actor.ai.L2SummonAI;
import dwo.gameserver.model.actor.appearance.PcAppearance;
import dwo.gameserver.model.actor.controller.object.InstanceController;
import dwo.gameserver.model.actor.controller.object.RestrictionController;
import dwo.gameserver.model.actor.controller.player.*;
import dwo.gameserver.model.actor.instance.PcInstance.PcAdmin;
import dwo.gameserver.model.actor.knownlist.PcKnownList;
import dwo.gameserver.model.actor.requests.AbstractRequest;
import dwo.gameserver.model.actor.stat.PcStat;
import dwo.gameserver.model.actor.status.PcStatus;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.holders.VitalityHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.TimeStamp;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Henna;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2PremiumItem;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2HennaInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.items.itemcontainer.ItemContainer;
import dwo.gameserver.model.items.itemcontainer.PcFreight;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.items.itemcontainer.PcRefund;
import dwo.gameserver.model.items.itemcontainer.PcWarehouse;
import dwo.gameserver.model.items.itemcontainer.PetInventory;
import dwo.gameserver.model.items.multisell.PreparedListContainer;
import dwo.gameserver.model.player.*;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.player.base.PlayerClass;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.player.base.SubClassType;
import dwo.gameserver.model.player.duel.DuelManager;
import dwo.gameserver.model.player.duel.DuelState;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.model.player.formation.group.L2CommandChannel;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyExitReason;
import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.model.player.formation.group.PartyMatchRoom;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.model.player.formation.group.PartyMatchWaitingList;
import dwo.gameserver.model.player.macro.L2Macro;
import dwo.gameserver.model.player.macro.MacroList;
import dwo.gameserver.model.player.teleport.TeleportBookmark;
import dwo.gameserver.model.skills.L2EnchantSkillLearn;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.Falling;
import dwo.gameserver.model.skills.base.formulas.calculations.Ressurection;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSiegeFlag;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSummon;
import dwo.gameserver.model.skills.base.l2skills.L2SkillTrap;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.communitybbs.BB.Forum;
import dwo.gameserver.model.world.communitybbs.Manager.ForumsBBSManager;
import dwo.gameserver.model.world.communitybbs.Manager.RegionBBSManager;
import dwo.gameserver.model.world.fishing.L2Fish;
import dwo.gameserver.model.world.fishing.L2Fishing;
import dwo.gameserver.model.world.npc.L2Event;
import dwo.gameserver.model.world.npc.L2PetData;
import dwo.gameserver.model.world.npc.L2PetLevelData;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.olympiad.OlympiadGameTask;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestSound;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.quest.QuestStateType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import dwo.gameserver.model.world.residence.castle.CastleSiegeEngine;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.model.world.zone.type.L2NoRestartZone;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.masktypes.UserInfoType;
import dwo.gameserver.network.game.serverpackets.ActionFail;
import dwo.gameserver.network.game.serverpackets.ChangeWaitType;
import dwo.gameserver.network.game.serverpackets.ConfirmDlg;
import dwo.gameserver.network.game.serverpackets.EtcStatusUpdate;
import dwo.gameserver.network.game.serverpackets.GameGuardQuery;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.LogOutOk;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NickNameChanged;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.RelationChanged;
import dwo.gameserver.network.game.serverpackets.Ride;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.SetupGauge;
import dwo.gameserver.network.game.serverpackets.SeverClose;
import dwo.gameserver.network.game.serverpackets.ShortBuffStatusUpdate;
import dwo.gameserver.network.game.serverpackets.ShortCutInit;
import dwo.gameserver.network.game.serverpackets.SkillCoolTime;
import dwo.gameserver.network.game.serverpackets.SkillList;
import dwo.gameserver.network.game.serverpackets.Snoop;
import dwo.gameserver.network.game.serverpackets.SocialAction;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.StopMove;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.TargetSelected;
import dwo.gameserver.network.game.serverpackets.TargetUnselected;
import dwo.gameserver.network.game.serverpackets.packet.ability.ExAcquireAPSkillList;
import dwo.gameserver.network.game.serverpackets.packet.acquire.AcquireSkillList;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExNewSkillToLearnByLevelUp;
import dwo.gameserver.network.game.serverpackets.packet.ex.*;
import dwo.gameserver.network.game.serverpackets.packet.friend.FriendStatus;
import dwo.gameserver.network.game.serverpackets.packet.henna.HennaInfo;
import dwo.gameserver.network.game.serverpackets.packet.info.CI;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoAbnormalVisualEffect;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoCubic;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.network.game.serverpackets.packet.info.UI;
import dwo.gameserver.network.game.serverpackets.packet.party.PartyMemberPosition;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pet.PetInventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListDelete;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListUpdate;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreBuyList;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreBuyManageList;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreBuyMsg;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreList;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreManageList;
import dwo.gameserver.network.game.serverpackets.packet.privatestore.PrivateStoreMsg;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopMsg;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopSellList;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowUsm;
import dwo.gameserver.network.game.serverpackets.packet.trade.TradeDone;
import dwo.gameserver.network.game.serverpackets.packet.trade.TradePressOtherOk;
import dwo.gameserver.network.game.serverpackets.packet.trade.TradeStart;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.airship.ExGetOnAirShip;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.GetOnVehicle;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.shuttle.ExSuttleGetOn;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.L2FastList;
import dwo.gameserver.util.database.DatabaseUtils;
import dwo.gameserver.util.floodprotector.FloodProtectors;
import dwo.gameserver.util.geometry.Point3D;
import dwo.scripts.instances.ChaosFestival;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class L2PcInstance extends L2Playable
{
	public static final Logger _log = LogManager.getLogger(L2PcInstance.class);

	public static final int REQUEST_TIMEOUT = 15;
	// during fall validations will be disabled for 10 ms.
	private static final int FALLING_VALIDATION_DELAY = 10000;
	public final ReentrantLock soulShotLock = new ReentrantLock();
	private final ReentrantLock _subclassLock = new ReentrantLock();
	private final RestrictionController _restrictionController = new RestrictionController(this);
	private final DeathPenaltyController _deathPenaltyController = new DeathPenaltyController(this);
	private final PvPFlagController _pvpFlagController = new PvPFlagController(this);
	private final RecipeBookController _recipeBookController = new RecipeBookController(this);
	private final ShortcutController _shortcutController = new ShortcutController(this);
	private final SummonFriendController _summonFriendController = new SummonFriendController(this);
	private final OlympiadController _olympiadController = new OlympiadController(this);
	private final ObserverController _observerController = new ObserverController(this);
	private final StateController _stateController = new StateController(this);
	private final StoreController _storeController = new StoreController(this);
	private final EventController _eventController = new EventController(this);
	private final CharacterVariablesController variablesController = new CharacterVariablesController(this);
	private final L2ContactList _contactList = new L2ContactList(this);
	/**
	 * Premium Items
	 */
	private final Map<Integer, L2PremiumItem> _premiumItems = new HashMap<>();
	/**
	 * Stored from last ValidatePosition *
	 */
	private final Point3D _lastServerPosition = new Point3D(0, 0, 0);
	private final PcInventory _inventory = new PcInventory(this);
	private final PcFreight _freight = new PcFreight(this);
	/**
	 * The table containing all Quests began by the L2PcInstance
	 */
	private final Map<String, QuestState> _quests = new FastMap<>();
	/* The list containing all macroses of this L2PcInstance */
	private final MacroList _macroses = new MacroList(this);
	private final List<L2PcInstance> _snoopListener = new FastList<>();
	private final List<L2PcInstance> _snoopedPlayer = new FastList<>();
	/* Татуировки */
	private final L2HennaInstance[] _henna = new L2HennaInstance[3];
	// charges
	private final AtomicInteger _charges = new AtomicInteger();
	private final TIntArrayList _silenceModeExcluded = new TIntArrayList();     // silence mode
	private final L2Request _request = new L2Request(this);
	private final TIntObjectHashMap<String> _chars = new TIntObjectHashMap<>();
	/**
	 * new loto ticket *
	 */
	private final int[] _loto = new int[5];
	/**
	 * new race ticket *
	 */
	private final int[] _race = new int[2];
	/**
	 * Bypass validations
	 */
	private final List<String> _validBypass = new L2FastList<>(true);
	private final List<String> _validBypassCopy = new L2FastList<>(true);
	private final List<String> _validBypass2 = new L2FastList<>(true);
	// open/close gates
	private final GatesRequest _gatesRequest = new GatesRequest();
	private final FastMap<Integer, TimeStamp> _reuseTimeStampsSkills = new FastMap<>();
	public ScheduledFuture<?> _taskforfish;
	public int _telemode;
	public int _shortBuffTaskSkillId;
	protected int _baseClassId;
	protected int _activeClassId;
	protected int _classIndex;
	protected Future<?> _mountFeedTask;
	protected boolean _inventoryDisable;
	/**
	 * Active shots.
	 */
	protected FastSet<Integer> _activeSoulShots = new FastSet<Integer>().shared();
	/**
	 * ShortBuff clearing Task
	 */
	ScheduledFuture<?> _shortBuffTask;
	private L2GameClient _client;
	private String _accountName;
	private long _deleteTimer;
	private Calendar _createDate = Calendar.getInstance();
	private volatile boolean _isOnline;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	private long _zoneRestartLimitTime;
	/**
	 * data for mounted pets
	 */
	private int _controlItemId;
	private L2PetData _petData;
	private L2PetLevelData _leveldata;
	private int _curFeed;
	private ScheduledFuture<?> _dismountTask;
	private boolean _petItems;
	/**
	 * The list of sub-classes this character has.
	 */
	private Map<Integer, SubClass> _subClasses; //TODO: переделать хранилище в _classList (чтобы мейн так же хранился)
	private PcAppearance _appearance;
	/**
	 * The Identifier of the L2PcInstance
	 */
	private int _charId = 0x00030b7a;
	/**
	 * The Experience of the L2PcInstance before the last Death Penalty
	 */
	private long _expBeforeDeath;
	/**
	 * The reputation of the L2PcInstance (if higher than 0, the name of the
	 * L2PcInstance appears in red)
	 */
	private int _reputation;
	/**
	 * The number of player killed during a PvP (the player killed was PvP Flagged)
	 */
	private int _pvpKills;
	/**
	 * The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed)
	 */
	private int _pkKills;
	/**
	 * The Fame of this L2PcInstance
	 */
	private int _fame;
	private ScheduledFuture<?> _fameTask;
	// Бинды.
	private byte[] _bindConfig;
	private boolean _bindConfigSavedInDB = true;
	/**
	 * Vitality recovery task
	 */
	private Map<Integer, VitalityHolder> _vitalityData = new HashMap();
	/**
	 * Очки PCCafe
	 */
	private int _pcBangPoints;
	/**
	 * Премиум аккаунт
	 */
	private AccountShareData _premiumTime;
	private ScheduledFuture<?> _premiumTask;
	/**
	 * Находится ли игрок в системе поиска партии
	 */
	private boolean _isInPartyWaitingList;
	private volatile ScheduledFuture<?> _teleportWatchdog;
	/**
	 * Сторона во время осады, за которую сражается текущий игрок
	 */
	private PlayerSiegeSide _siegeState = PlayerSiegeSide.NONE;
	/**
	 * ID замка или форта, на осаду которого зарегистрирован текущий игрок
	 */
	private int _activeSiegeId;
	private int _curWeightPenalty;
	private int _lastCompassZone; // the last compass zone update send to the client
	// Save responder name for log it
	private String _lastPetitionGmName;
	private int _bookmarkslot; // The Teleport Bookmark Slot
	private List<TeleportBookmark> tpbookmark = new ArrayList<>();
	private PlayerPunishLevel _punishLevel = PlayerPunishLevel.NONE;
	private long _punishTimer;
	private ScheduledFuture<?> _punishTask;
	private int _ping = -1;
	/**
	 * Система прыжков
	 */
	private int _currentJumpId;
	private boolean _isJumping;
	/**
	 * Duel
	 */
	private boolean _isInDuel;
	private DuelState _duelState = DuelState.NODUEL;
	private int _duelId;
	private SystemMessageId _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
	/**
	 * Boat, Shuttle and AirShip
	 */
	private L2Vehicle _vehicle;
	private Point3D _inVehiclePosition;
	private int _mountType;
	private int _mountNpcId;
	private int _mountLevel;
	/**
	 * Store object used to summon the strider you are mounting *
	 */
	private int _mountObjectID;
	private boolean _inCrystallize;
	private long _offlineShopStart;
	private L2Transformation _transformation;
	/**
	 * True if the L2PcInstance is sitting
	 */
	private boolean _isSitting;
	/**
	 * Location before entering Observer Mode
	 */
	private int _lastObserverPositionX;
	private int _lastObserverPositionY;
	private int _lastObserverPositionZ;
	private boolean _isInObserverMode;
	/**
	 * Previous coordinate sent to party in ValidatePosition *
	 */
	private int _lastPartyPositionX;
	private int _lastPartyPositionY;
	private int _lastPartyPositionZ;
	private int _lastPartyPositionTick;
	/**
	 * Количество рекоммендаций, которые имеет текущий игрок
	 */
	private int _recomendationHave;
	/**
	 * Количество рекоммендаций, которые может дать текущий игрок другим
	 */
	private int _recomendationLeft;
	/**
	 * Таск на получение рекомментаций
	 */
	private ScheduledFuture<?> _recommendationGiveTask;
	/**
	 * Счетчик получения бонуса в 10 реков в первые два часа игры
	 */
	private int _recomendationTwoHoursGiven;
	private PcWarehouse _warehouse;
	private PcRefund _refund;
	/**
	 * Тип лавки у игрока в текущий момент
	 */
	private PlayerPrivateStoreType _privateStoreType = PlayerPrivateStoreType.NONE;
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private L2ManufactureList _createList;
	private TradeList _sellList;
	private TradeList _buyList;
	// Multisell
	private PreparedListContainer _currentMultiSell;
	private boolean _isNoble;
	private boolean _isHero;
	private boolean _canDualCast;
	/**
	 * The L2FolkInstance corresponding to the last Folk which one the player
	 * talked.
	 */
	private L2Npc _lastFolkNpc;
	/**
	 * Last NPC Id talked on a quest
	 */
	private int _questNpcObject;

	// Clan related attributes
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
    private int _hennaLUC;
    private int _hennaCHA;
	private List<L2Skill> _hennaAttrSkill;
	/**
	 *Списки для схем бафов из комьюнити борда
	 */
	private Map<Integer, Integer> _bbsBuff = new FastMap<>();
	/**
	 * The L2Summon of the L2PcInstance
	 */
	private FastList<L2Summon> _summons = new FastList<L2Summon>().shared();
	/**
	 * The L2Decoy of the L2PcInstance
	 */
	private List<L2Decoy> _decoy;
	/**
	 * The L2Trap of the L2PcInstance
	 */
	private L2Trap _trap;
	/**
	 * The L2Agathion of the L2PcInstance
	 */
	private int _agathionId;
	// apparently, a L2PcInstance CAN have both a summon AND a tamed beast at
	// the same time!!
	// after Freya players can control more than one tamed beast
	private List<L2TamedBeastInstance> _tamedBeast;
	private boolean _minimapAllowed;
	// client radar
	// TODO: This needs to be better intergrated and saved/loaded
	private L2Radar _radar;
	// Party matching
	private int _partyRoomId;
	/**
	 * The Clan Identifier of the L2PcInstance
	 */
	private int _clanId;
	/**
	 * The Clan object of the L2PcInstance
	 */
	private L2Clan _clan;
	/**
	 * Apprentice and Sponsor IDs
	 */
	private int _apprentice;
	private int _sponsor;
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private int _powerGrade;
	private int _clanPrivileges;
	/**
	 * L2PcInstance's pledge class (knight, Baron, etc.)
	 */
	private int _pledgeClass;
	private int _pledgeType;
	/**
	 * Level at which the player joined the clan as an academy member
	 */
	private int _lvlJoinedAcademy;
	private ScheduledFuture<?> _chargeTask;
	// Absorbed Souls
	private int _souls;
	private ScheduledFuture<?> _soulTask;
	// WorldPosition used by TARGET_SIGNET_GROUND
	private Point3D _currentSkillWorldPosition;
	private L2AccessLevel _accessLevel;
	private boolean _messageRefusal; // message refusal mode
	private boolean _silenceMode; // silence mode
	private boolean _dietMode; // ignore weight penalty
	private boolean _tradeRefusal; // Trade refusal
	private L2Party _party;
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _activeRequester;
	private long _requestExpireTime;
	private L2ItemInstance _arrowItem;
	private L2ItemInstance _boltItem;
	private long _startingTimeInFullParty;
	private long _startingTimeInParty;
	private long _idleFromTime = System.currentTimeMillis();
	private long _spawnProtectEndTime;
	private long _teleportProtectEndTime;
	// protects a char from agro mobs when getting up from fake death
	private long _recentFakeDeathEndTime;
	private boolean _isFakeDeath;
	/**
	 * The fists L2Weapon of the L2PcInstance (used when no weapon is equiped)
	 */
	private L2Weapon _fistsWeaponItem;
	/**
	 * Текущий максимальный уровень владения экипировкой
	 * (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7, R=8, R95=9, R99=10)
	 */
	private int _expertiseArmorPenalty;
	private int _expertiseWeaponPenalty;
	private int _expertisePenaltyBonus;
    private boolean _isEnchanting = false;
    private L2ItemInstance _activeEnchantItem = null;
    private L2ItemInstance _activeEnchantSupportItem = null;
    private L2ItemInstance _activeEnchantAttrItem = null;
    private long _activeEnchantTimestamp = 0;
	private L2ItemInstance _activeShapeShiftingItem = null;
	private L2ItemInstance _activeShapeShiftingTargetItem = null;
	private L2ItemInstance _activeShapeShiftingSupportItem = null;
	private List<L2CubicInstance> _cubics = new FastList<L2CubicInstance>().shared();
	private int _team;
	/**
	 * lvl of alliance with ketra orcs or varka silenos, used in quests and
	 * aggro checks
	 * [-5,-1] varka, 0 neutral, [1,5] ketra
	 */
	private int _alliedVarkaKetra;
	private L2Fishing _fishCombat;
	private boolean _fishing;
	private int _fishx;
	private int _fishy;
	private int _fishz;
	private int[] _transformAllowedSkills = {};
	private ScheduledFuture<?> _taskWater;
	private Forum _forumMail;
	private Forum _forumMemo;
	/**
	 * Current skill in use. Note that L2Character has _lastSkillCast, but
	 * this has the button presses
	 */
	private SkillDat _currentSkill;
	private SkillDat _currentPetSkill;
	/**
	 * Skills queued because a skill is already in progress
	 */
	private SkillDat _queuedSkill;
	private int _cursedWeaponEquippedId;
	private boolean _combatFlagEquippedId;
	private int _reviveRequested;
	private double _revivePower;
	private boolean _revivePet;
	private long _petToRevive;
	private double _cpUpdateIncCheck;
	private double _cpUpdateDecCheck;
	private double _cpUpdateInterval;
	private double _mpUpdateIncCheck;
	private double _mpUpdateDecCheck;
	private double _mpUpdateInterval;
	private boolean _isRidingStrider;
	private boolean _isFlyingMounted;
	/**
	 * Char Coords from Client
	 */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	private volatile long _fallingTimestamp;
	private int _multiSocialTarget;
	private int _multiSociaAction;
	private int _movieId;
	private volatile long _lastItemAuctionInfoRequest;
	private long _notMoveUntil;
	private boolean _isTargetable = true;
	private int _lastTeleporterObjectId;
	/**
	 * Herbs Task Time *
	 */
	private int _herbstask;
	// L2JMOD Wedding
	private boolean _married;
	private int _partnerId;
	private int _coupleId;
	private boolean _engagerequest;
	private int _engageid;
	private boolean _marryrequest;
	private boolean _marryaccepted;
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
    private ScheduledFuture<?> _taskWarnChatTask;
	private int _hoursInGame;
	private L2Fish _fish;
	private L2ItemInstance _lure;
	private FastMap<Integer, TimeStamp> _reuseTimeStampsItems = new FastMap<>();
	private boolean _canFeed;
	private boolean _isInSiege;
	private boolean _isInHideoutSiege;
	private boolean _hideInfo;
	private PcAdmin _PcAdmin;
	/*
	 * Премиум очки, используются в итеммолле
     */
	private AccountShareData _gamePoints;
	private int _tvtPvpKills;
	private int _tvtPvpKillSteak;
	private boolean _ctfFlagEquipped;

    public static final String WORLD_CHAT_VARIABLE_NAME = "WORLD_CHAT_POINTS";

    /**
	 * Constructor of L2PcInstance (use L2Character constructor).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Call the L2Character constructor to create an empty _skills slot and copy basic Calculator set to this L2PcInstance </li>
	 * <li>Set the name of the L2PcInstance</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the L2PcInstance to 1</B></FONT><BR><BR>
	 *
	 * @param objectId    Identifier of the object to initialized
	 * @param template    The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the account including this L2PcInstance
	 * @param app
	 */
	private L2PcInstance(int objectId, L2PcTemplate template, String accountName, PcAppearance app)
	{
		super(objectId, template);
		initCharStatusUpdateValues();
		initPcStatusUpdateValues();

		_accountName = accountName;
		_premiumTime = AccountShareDataTable.getInstance().getAccountData(_accountName, "player_premium_time", "0");
		app.setOwner(this);
		_appearance = app;

		// Create an AI
		_ai = new L2PlayerAI(new AIAccessor());

		// Create a L2Radar object
		_radar = new L2Radar(this);
	}

	/**
	 * Create a new L2PcInstance and add it in the characters table of the
	 * database.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Create a new L2PcInstance with an account name</li> <li>Set the name,
	 * the Hair Style, the Hair Color and the Face type of the L2PcInstance</li>
	 * <li>Add the player in the characters table of the database</li><BR>
	 * <BR>
	 *
	 * @param objectId    Identifier of the object to initialized
	 * @param template    The L2PcTemplate to apply to the L2PcInstance
	 * @param accountName The name of the L2PcInstance
	 * @param name        The name of the L2PcInstance
	 * @param hairStyle   The hair style Identifier of the L2PcInstance
	 * @param hairColor   The hair color Identifier of the L2PcInstance
	 * @param face        The face type Identifier of the L2PcInstance
	 * @return The L2PcInstance added to the database or null
	 */
	public static L2PcInstance create(int objectId, L2PcTemplate template, String accountName, String name, byte hairStyle, byte hairColor, byte face, boolean sex)
	{
		// Create a new L2PcInstance with an account name
		PcAppearance app = new PcAppearance(face, hairColor, hairStyle, sex);
		L2PcInstance player = new L2PcInstance(objectId, template, accountName, app);

		// Set the name of the L2PcInstance
		player.setName(name);

		// Set Character's create time
		player._createDate = Calendar.getInstance();

		// Set the base class ID to that of the actual class ID.
		player.setBaseClassId(player.getClassId());

		// Add the player in the characters table of the database
		boolean ok = player.createDb();

		if(!ok)
		{
			return null;
		}

		return player;
	}

	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world (call restore method).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @return The L2PcInstance loaded from the database
	 */
	public static L2PcInstance load(int objectId)
	{
		return restore(objectId);
	}

	/**
	 * Retrieve a L2PcInstance from the characters table of the database and add it in _allObjects of the L2world.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Retrieve the L2PcInstance from the characters table of the database </li>
	 * <li>Add the L2PcInstance object in _allObjects </li>
	 * <li>Set the x,y,z position of the L2PcInstance and make it invisible</li>
	 * <li>Update the overloaded status of the L2PcInstance</li><BR><BR>
	 *
	 * @param objectId Identifier of the object to initialized
	 * @return The L2PcInstance loaded from the database
	 */
	private static L2PcInstance restore(int objectId)
	{
		L2PcInstance player = null;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			// Retrieve the L2PcInstance from the characters table of the database
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.SELECT_RESTORE_CHARACTER);
			statement.setInt(1, objectId);
			rset = statement.executeQuery();

			double currentCp = 0;
			double currentHp = 0;
			double currentMp = 0;

			if(rset.next())
			{
				int activeClassId = rset.getInt("classid");
				boolean female = rset.getInt("sex") != 0;
				L2PcTemplate template = ClassTemplateTable.getInstance().getTemplate(activeClassId);
				PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
				app.setCustomFace(rset.getInt("custom_face"));
				app.setCustomHairColor(rset.getInt("custom_hair_color"));
				app.setCustomHairStyle(rset.getInt("custom_hair_style"));

				player = new L2PcInstance(objectId, template, rset.getString("account_name"), app);
				player.setName(rset.getString("char_name"));
				player._lastAccess = rset.getLong("lastAccess");

				player.getStat().setExp(rset.getLong("exp"));
				player._expBeforeDeath = rset.getLong("expBeforeDeath");
				player.getStat().setLevel(rset.getByte("level"));
				player.getStat().setSp(rset.getInt("sp"));

				player.setHeading(rset.getInt("heading"));

				player.setReputation(rset.getInt("reputation"));
				player.setFame(rset.getInt("fame"));
				player._pvpKills = rset.getInt("pvpkills");
				player._pkKills = rset.getInt("pkkills");
				player.setOnlineTime(rset.getLong("onlinetime"));
				player.setNoble(rset.getInt("nobless") == 1);

				player._clanJoinExpiryTime = rset.getLong("clan_join_expiry_time");
				if(player._clanJoinExpiryTime < System.currentTimeMillis())
				{
					player._clanJoinExpiryTime = 0;
				}
				player._clanCreateExpiryTime = rset.getLong("clan_create_expiry_time");
				if(player._clanCreateExpiryTime < System.currentTimeMillis())
				{
					player._clanCreateExpiryTime = 0;
				}

				int clanId = rset.getInt("clanid");
				player._powerGrade = (int) rset.getLong("power_grade");
				player._pledgeType = rset.getInt("subpledge");

				if(clanId > 0)
				{
					player.setClan(ClanTable.getInstance().getClan(clanId));
				}

				if(player._clan != null)
				{
					if(player._clan.getLeaderId() == player.getObjectId())
					{
						player._clanPrivileges = L2Clan.CP_ALL;
						player._powerGrade = 1;
					}
					else
					{
						if(player._powerGrade == 0)
						{
							player._powerGrade = 5;
						}
						player._clanPrivileges = player._clan.getRankPrivs(player._powerGrade);
					}
					int pledgeClass = 0;

					pledgeClass = player._clan.getClanMember(objectId).calculatePledgeClass(player);

					if(player._isNoble && pledgeClass < 5)
					{
						pledgeClass = 5;
					}

					if(player._olympiadController.isHero() && pledgeClass < 8)
					{
						pledgeClass = 8;
					}

					player.setPledgeClass(pledgeClass);
				}
				else
				{
					player._clanPrivileges = L2Clan.CP_NOTHING;
				}

				player._deleteTimer = rset.getLong("deletetime");

				player.setTitle(rset.getString("title"));
				player.setAccessLevel(rset.getInt("accesslevel"));
				player._appearance.setTitleColor(rset.getInt("title_color"));
				player._appearance.setNameColor(rset.getInt("name_color"));
				player._fistsWeaponItem = player.findFistsWeaponItem(activeClassId);
				player._uptime = System.currentTimeMillis();

				currentHp = rset.getDouble("curHp");
				currentCp = rset.getDouble("curCp");
				currentMp = rset.getDouble("curMp");

				player._classIndex = 0;
				try
				{
					player._baseClassId = rset.getInt("base_class");
				}
				catch(Exception e)
				{
					player._baseClassId = activeClassId;
				}

				// Restore Subclass Data (cannot be done earlier in function)
				if(restoreSubClassData(player))
				{
					if(activeClassId != player._baseClassId)
					{
						for(SubClass subClass : player.getSubClasses().values())
						{
							if(subClass.getClassId() == activeClassId)
							{
								player._classIndex = subClass.getClassIndex();
							}
						}
					}
				}
				if(player._classIndex == 0 && activeClassId != player._baseClassId)
				{
					// Subclass in use but doesn't exist in DB -
					// a possible restart-while-modifysubclass cheat has been attempted.
					// Switching to use base class
					player.setClassId(player._baseClassId);
					_log.log(Level.WARN, "Player " + player.getName() + " reverted to base class. Possibly has tried a relogin exploit while subclassing.");
				}
				else
				{
					player._activeClassId = activeClassId;
				}

				player._apprentice = rset.getInt("apprentice");
				player._sponsor = rset.getInt("sponsor");
				player._lvlJoinedAcademy = rset.getInt("lvl_joined_academy");
				player.setPunishLevel(rset.getInt("punish_level"));
				player._punishTimer = player._punishLevel == PlayerPunishLevel.NONE ? 0 : rset.getLong("punish_timer");

				CursedWeaponsManager.getInstance().checkPlayer(player);

				player._alliedVarkaKetra = rset.getInt("varka_ketra_ally");

				// Set the x,y,z position of the L2PcInstance and make it invisible
				player.setXYZ(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"), false);

				// Set Teleport Bookmark Slot
				player.setBookMarkSlot(rset.getInt("BookmarkSlot"));

				// character creation Time
				player._createDate.setTime(rset.getDate("createDate"));

				// Очки PC-Bang
				player._pcBangPoints = rset.getInt("pcbang_points");

				// Retrieve the name and ID of the other characters assigned to this account.
				FiltredPreparedStatement stmt = con.prepareStatement(Characters.SELECT_CHAR_ACCOUNT_NAME_CHARID);
				stmt.setString(1, player._accountName);
				stmt.setInt(2, objectId);
				ResultSet chars = stmt.executeQuery();

				while(chars.next())
				{
					Integer charId = chars.getInt("charId");
					String charName = chars.getString("char_name");
					player._chars.put(charId, charName);
				}

				chars.close();
				stmt.close();
			}

			if(player == null)
			{
				return null;
			}

			// Set Hero status if it applies
			if(HeroManager.getInstance().getHeroes() != null && HeroManager.getInstance().getHeroes().containsKey(objectId))
			{
				player._olympiadController.giveHero();
			}

			// Retrieve from the database all skills of this L2PcInstance and add them to _skills
			// Retrieve from the database all items of this L2PcInstance and add them to _inventory
			player._inventory.restore();
			player._freight.restore();
			if(!Config.WAREHOUSE_CACHE)
			{
				player.getWarehouse();
			}

			// Retrieve from the database all secondary data of this L2PcInstance
			// Note that Clan, Noblesse and Hero skills are given separately and not here.
			// Retrieve from the database all skills of this L2PcInstance and add them to _skills
			player.restoreCharData();

			// Reward auto-get skills and all available skills if auto-learn skills is true.
			player.rewardSkills();

			// buff and status icons
			if(Config.STORE_SKILL_COOLTIME)
			{
				player.restoreEffects();
			}

			player.restoreItemReuse();

			// Restore current Cp, HP and MP values
			player.setCurrentCp(currentCp);
			player.setCurrentHp(currentHp);
			player.setCurrentMp(currentMp);

			if(currentHp < 0.5)
			{
				player.stopHpMpRegeneration();
				player.setIsDead(true);
			}

			// Restore pet if exists in the world
			if(WorldManager.getInstance().getPets(player.getObjectId()) != null)
			{
				for(L2PetInstance pet : WorldManager.getInstance().getPets(player.getObjectId()))
				{
					player.addPet(pet);
				}
			}
			if(!player._summons.isEmpty())
			{
				for(L2Summon pet : player._summons)
				{
					pet.setOwner(player);
				}
			}
			// If no pets - trying to restore summons
			else if(Config.PLAYER_SPAWN_PROTECTION <= 0)
			{
				CharSummonTable.getInstance().restoreSummon(player, 5000);
			}

			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			// Update the expertise status of the L2PcInstance
			player.refreshExpertisePenalty();

			player.restoreZoneRestartLimitTime();

			player.loadVitalityData();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed loading character.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		player.variablesController.reload();
		return player;
	}

	/**
	 * Restores sub-class data for the L2PcInstance, used to check the current
	 * class index for the character.
	 *
	 * @param player
	 * @return
	 */
	private static boolean restoreSubClassData(L2PcInstance player)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SUBCLASSES_RESTORE);
			statement.setInt(1, player.getObjectId());

			rset = statement.executeQuery();

			while(rset.next())
			{
				SubClass subClass = new SubClass();
				subClass.setClassId(rset.getInt("class_id"));
				subClass.setLevel(rset.getByte("level"));
				subClass.setClassType(SubClassType.values()[rset.getInt("class_type")]);
				subClass.setExp(rset.getLong("exp"));
				subClass.setSp(rset.getInt("sp"));
				subClass.setClassIndex(rset.getInt("class_index"));

				// Enforce the correct indexing of _subClasses against their
				// class indexes.
				player.getSubClasses().put(subClass.getClassIndex(), subClass);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore classes for " + player.getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		return true;
	}

	public String getAccountName()
	{
		if(_client == null)
		{
			return _accountName;
		}
		return _client.getAccountName();
	}

	public void setAccountName(String newAccountName)
	{
		_accountName = newAccountName;
	}

	public String getAccountNamePlayer()
	{
		return _accountName;
	}

	public TIntObjectHashMap<String> getAccountChars()
	{
		return _chars;
	}

	public int getRelation(L2PcInstance target)
	{
		int result = 0;

		if(_clan != null)
		{
			result |= RelationChanged.RELATION_CLAN_MEMBER;
			if(_clan.equals(target._clan))
			{
				result |= RelationChanged.RELATION_CLAN_MATE;
			}
			if(getAllyId() != 0)
			{
				result |= RelationChanged.RELATION_ALLY_MEMBER;
			}
		}
		if(isClanLeader())
		{
			result |= RelationChanged.RELATION_LEADER;
		}
		if(_party != null && _party == target._party)
		{
			result |= RelationChanged.RELATION_HAS_PARTY;
			for(int i = 0; i < _party.getMembers().size(); i++)
			{
				if(!_party.getMembers().get(i).equals(this))
				{
					continue;
				}
				switch(i)
				{
					case 0:
						result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
						break;
					case 1:
						result |= RelationChanged.RELATION_PARTY4; // 0x8
						break;
					case 2:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x7
						break;
					case 3:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
						break;
					case 4:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
						break;
					case 5:
						result |= RelationChanged.RELATION_PARTY3; // 0x4
						break;
					case 6:
						result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
						break;
					case 7:
						result |= RelationChanged.RELATION_PARTY2; // 0x2
						break;
					case 8:
						result |= RelationChanged.RELATION_PARTY1; // 0x1
						break;
				}
			}
		}
		if(_siegeState != PlayerSiegeSide.NONE)
		{
			result |= RelationChanged.RELATION_INSIEGE;
			result |= _siegeState != target._siegeState ? RelationChanged.RELATION_ENEMY : RelationChanged.RELATION_ALLY;
			if(_siegeState == PlayerSiegeSide.ATTACKER)
			{
				result |= RelationChanged.RELATION_ATTACKER;
			}
		}
		if(_clan != null && target._clan != null)
		{
			if(target._pledgeType != L2Clan.SUBUNIT_ACADEMY && _pledgeType != L2Clan.SUBUNIT_ACADEMY && target._clan.isAtWarWith(_clan.getClanId()))
			{
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if(_clan.isAtWarWith(target._clan.getClanId()))
				{
					result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
		}
		if(_eventController.isInHandysBlockCheckerEventArena())
		{
			result |= RelationChanged.RELATION_INSIEGE;
			HandysBlockCheckerManager.ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(_eventController.getHandysBlockCheckerEventArena());
			result |= holder.getPlayerTeam(this) == 0 ? RelationChanged.RELATION_ENEMY : RelationChanged.RELATION_ALLY;
			result |= RelationChanged.RELATION_ATTACKER;
		}
		return result;
	}

	/**
	 * Forces Relation change for target player
	 * @param target
	 */
	public void changeRelation(L2PcInstance target)
	{
		target.sendPacket(new RelationChanged(this, getRelation(target), isAutoAttackable(target)));
		if(!_summons.isEmpty())
		{
			for(L2Summon summon : _summons)
			{
				target.sendPacket(new RelationChanged(summon, getRelation(target), isAutoAttackable(target)));
			}
		}
	}

	private void initPcStatusUpdateValues()
	{
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}

	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty </li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance </li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed L2PcInstance </li>
	 * <li>If the killed L2PcInstance has Karma, manage Drop Item</li>
	 * <li>Kill the L2PcInstance </li><BR><BR>
	 *
	 * @param killer
	 */
	@Override
	public boolean doDie(L2Character killer)
	{
		// Kill the L2PcInstance
		if(!super.doDie(killer))
		{
			return false;
		}

		HookManager.getInstance().notifyEvent(HookType.ON_DIE, getHookContainer(), this, killer);

		if(isMounted())
		{
			stopFeed();
		}

		synchronized(this)
		{
			if(_isFakeDeath)
			{
				stopFakeDeath(true);
			}
		}

		if(killer != null)
		{
			L2PcInstance pk = killer.getActingPlayer();

			if(_eventController.isParticipant() && pk != null)
			{
				pk._eventController.addKill(getName());
			}

			EventManager.onKill(killer, this);

			broadcastStatusUpdate();

			// Очищаем кеш с кол-вом экспы до смерти
			_expBeforeDeath = 0;

			// Если персонаж с ПО, дропаем его
			if(isCursedWeaponEquipped())
			{
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquippedId, killer);
			}
			else if(_combatFlagEquippedId)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if(fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(this, fort.getFortId());
				}
				else
				{
					long slot = _inventory.getSlotFromItem(_inventory.getItemByItemId(9819));
					_inventory.unEquipItemInBodySlot(slot);
					destroyItem(ProcessType.COMBATFLAG, _inventory.getItemByItemId(9819), null, true);
				}
			}
			else
			{
				if(pk == null || !pk.isCursedWeaponEquipped())
				{
					onDieDropItem(killer); // Check if any item should be dropped

					if(!(isInsideZone(ZONE_PVP) && !isInsideZone(ZONE_SIEGE)))
					{
						if(pk != null && pk._clan != null && _clan != null && !isAcademyMember() && !pk.isAcademyMember())
						{
							if(_clan.isAtWarWith(pk._clanId) && pk._clan.isAtWarWith(_clan.getClanId()) || _isInSiege && pk._isInSiege)
							{
								ClanWar war = _clan.getClanWar(pk._clan);
								if(war != null)
								{
									war.onKill(pk, this);
								}

								if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
								{
									WorldStatisticsManager.getInstance().updateClanStat(pk._clanId, CategoryType.ALL_CLAN_PVP_COUNT, 0, 1);
								}
							}
						}
					}
					if(Config.ALT_GAME_DELEVEL)
					{
						// If player is Lucky shouldn't get penalized.
						if(!isLucky())
						{
							// Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty
							// NOTE: deathPenalty +- Exp will update karma
							// Penalty is lower if the player is at war with the pk (war has to be declared)
							boolean siegeNpc = killer instanceof L2DefenderInstance || killer instanceof L2FortCommanderInstance;
							boolean atWar = pk != null && _clan != null && _clan.isAtWarWith(pk._clanId);
							deathPenalty(atWar, pk != null, siegeNpc);
						}
					}
				}
			}
			if(killer instanceof L2Attackable)
			{
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					updateWorldStatistic(CategoryType.KILLED_BY_MONSTER_COUNT, null, 1);
				}
			}
		}

		// Выключаем кубики
		if(!_cubics.isEmpty())
		{
			for(L2CubicInstance cubic : _cubics)
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}

		// Если вызваны саммоны - останавливаем им атаку
		if(!_summons.isEmpty())
		{
			for(L2Summon summon : _summons)
			{
				summon.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
			}
		}

		if(_fusionSkill != null)
		{
			abortCast();
		}

		getKnownList().getKnownCharacters().stream().filter(character -> character.getFusionSkill() != null && character.getFusionSkill().getTarget().equals(this)).forEach(L2Character::abortCast);

		if(_agathionId != 0)
		{
			_agathionId = 0;
		}

		stopWaterTask();

		// Обновляем статистику по смертям
		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			updateWorldStatistic(CategoryType.DIE_COUNT, null, 1);
		}

		if(isPhoenixBlessed() || isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE) && _isInSiege)
		{
			reviveRequest(this, null, -1);
		}
		return true;
	}

	@Override
	public PcStat getStat()
	{
		return (PcStat) super.getStat();
	}

	@Override
	public void initCharStat()
	{
		setStat(new PcStat(this));
	}

	@Override
	public PcStatus getStatus()
	{
		return (PcStatus) super.getStatus();
	}

	@Override
	public void initCharStatus()
	{
		setStatus(new PcStatus(this));
	}

	@Override
	public PcKnownList getKnownList()
	{
		return (PcKnownList) super.getKnownList();
	}

	@Override
	protected void initKnownList()
	{
		setKnownList(new PcKnownList(this));
	}

	/**
	 * @return the Reputation of the L2PcInstance.
	 */
	@Override
	public int getReputation()
	{
		return _reputation;
	}

	/**
	 * Set the reputation of the L2PcInstance
	 * StatusUpdate (broadcast)
	 */
	public void setReputation(int reputation)
	{
		if(hasBadReputation() && reputation < 0)
		{
			// Если получаем репу в минус, уведомляем об этом находящихся рядом гвардов
			for(L2Object object : getKnownList().getKnownObjects().values())
			{
				if(!(object instanceof L2GuardInstance))
				{
					continue;
				}

				if(((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		}
		else if(_reputation < 0 && reputation == 0)
		{
			// Send a Server->Client StatusUpdate packet with reputation and PvP Flag
			// to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setBadReputationFlag(0);
		}

		_reputation = reputation;
		broadcastReputationStatus();
	}

	/**
	 * Check if the active L2Skill can be casted.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the skill isn't toggle and is offensive </li>
	 * <li>Check if the target is in the skill cast range </li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled </li>
	 * <li>Check if the caster owns enought consummed Item, enough HP and MP to cast the skill </li>
	 * <li>Check if the caster isn't sitting </li>
	 * <li>Check if all skills are enabled and this skill is enabled </li><BR><BR>
	 * <li>Check if the caster own the weapon needed </li><BR><BR>
	 * <li>Check if the skill is active </li><BR><BR>
	 * <li>Check if all casting conditions are completed</li><BR><BR>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li><BR><BR>
	 *
	 * @param skill    The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		// Check if the skill is active
		if(skill.isPassive())
		{
			// just ignore the passive skill request. why does the client send it anyway ??
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendActionFailed();
			return false;
		}

		//************************************* Check Casting in Progress *******************************************

		// If a skill is currently being used, queue this one if this is not the same
		if(isCastingNow() && (!_canDualCast || isDoubleCastingNow()))
		{
			SkillDat currentSkill = _currentSkill;
			// Check if new skill different from current skill in progress
			if(currentSkill != null && skill.getId() == currentSkill.getSkillId())
			{
				sendActionFailed();
				return false;
			}

			// Create a new SkillDat object and queue it in the player _queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendActionFailed();
			return false;
		}

		if(isCastingNow())
		{
			setIsDoubleCastingNow(true);
		}
		else
		{
			setIsCastingNow(true);
		}

		// Create a new SkillDat object and set the player _currentSkill
		// This is used mainly to save & queue the button presses, since L2Character has
		// _lastSkillCast which could otherwise replace it
		setCurrentSkill(skill, forceUse, dontMove);

		if(_queuedSkill != null) // wiping out previous values, after casting has been aborted
		{
			setQueuedSkill(null, false, false);
		}

		if(!checkUseMagicConditions(skill, forceUse, dontMove))
		{
			if(isDoubleCastingNow())
			{
				setIsDoubleCastingNow(false);
			}
			else
			{
				setIsCastingNow(false);
			}
			return false;
		}

		// Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
		L2Object target;
		switch(skill.getTargetType())
		{
			case TARGET_AURA:    // AURA, SELF should be cast even if no target has been found
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_AURA_CORPSE_MOB:
			case TARGET_SUBLIME:
			case TARGET_CORPSE_CLAN:
			case TARGET_CORPSE_COMMAND_CHANNEL:
			case TARGET_CORPSE_PARTY:
				target = this;
				break;
			default:

				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}

		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		return true;
	}

	@Override
	public PvPFlagController getPvPFlagController()
	{
		return _pvpFlagController;
	}

	/**
	 * @return {@code true} если игрок находится в игре
	 */
	@Override
	public boolean isOnline()
	{
		return _isOnline;
	}

	@Override
	public void store()
	{
		store(true);
	}

	@Override
	public void storeEffect(boolean storeEffects)
	{
		if(!Config.STORE_SKILL_COOLTIME)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			// Delete all current stored effects for char to avoid dupe
			statement = con.prepareStatement(Characters.EFFECTS_CLEAR);
			statement.setInt(1, getObjectId());
			statement.setInt(2, _classIndex);
			statement.execute();
			statement.clearParameters();

			int buff_index = 0;

			List<Integer> storedSkills = new FastList<>();

			// Store all effect data along with calulated remaining
			// reuse delays for matching skills. 'restore_type'= 0.
			statement = con.prepareStatement(Characters.EFFECTS_STORE);

			if(storeEffects)
			{
				for(L2Effect effect : getAllEffects())
				{
					if(effect == null)
					{
						continue;
					}

					switch(effect.getEffectType())
					{
						case HEAL_OVER_TIME:
						case CPHEAL_OVER_TIME:
							// TODO: Fix me.
						case HIDE:
							continue;
					}

					L2Skill skill = effect.getSkill();

					// Эффекты танцев и песен не сохраняются при релоге
					if(skill.isDance())
					{
						continue;
					}

					// Эффекты бафа наставничества не сохраняем при релоге
					if(skill.isMentor())
					{
						continue;
					}

					if(storedSkills.contains(skill.getReuseHashCode()))
					{
						continue;
					}

					storedSkills.add(skill.getReuseHashCode());

					if(!effect.isHerbEffect() && effect.isInUse() && !skill.isToggle() || skill.isForceStorable())
					{
						statement.setInt(1, getObjectId());
						statement.setInt(2, skill.getId());
						statement.setInt(3, skill.getLevel());
						statement.setInt(4, effect.getCount());
						statement.setInt(5, effect.getTime());

						if(_reuseTimeStampsSkills.containsKey(skill.getReuseHashCode()))
						{
							TimeStamp t = _reuseTimeStampsSkills.get(skill.getReuseHashCode());
							statement.setLong(6, t.hasNotPassed() ? t.getReuse() : 0);
							statement.setDouble(7, t.hasNotPassed() ? t.getStamp() : 0);
						}
						else
						{
							statement.setLong(6, 0);
							statement.setDouble(7, 0);
						}

						statement.setInt(8, 0);
						statement.setInt(9, _classIndex);
						statement.setInt(10, ++buff_index);
						statement.execute();
					}
				}
			}

			// Store the reuse delays of remaining skills which
			// lost effect but still under reuse delay. 'restore_type' 1.
			int hash;
			TimeStamp t;
			for(Map.Entry<Integer, TimeStamp> ts : _reuseTimeStampsSkills.entrySet())
			{
				hash = ts.getKey();
				if(storedSkills.contains(hash))
				{
					continue;
				}
				t = ts.getValue();
				if(t != null && t.hasNotPassed())
				{
					storedSkills.add(hash);

					statement.setInt(1, getObjectId());
					statement.setInt(2, t.getSkillId());
					statement.setInt(3, t.getSkillLvl());
					statement.setInt(4, -1);
					statement.setInt(5, -1);
					statement.setLong(6, t.getReuse());
					statement.setDouble(7, t.getStamp());
					statement.setInt(8, 1);
					statement.setInt(9, _classIndex);
					statement.setInt(10, ++buff_index);
					statement.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store char effect data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Retrieve from the database all skill effects of this L2PcInstance and add them to the player.
	 */
	@Override
	public void restoreEffects()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.EFFECTS_RESTORE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, _classIndex);
			rset = statement.executeQuery();

			while(rset.next())
			{
				int effectCount = rset.getInt("effect_count");
				int effectCurTime = rset.getInt("effect_cur_time");
				long reuseDelay = rset.getLong("reuse_delay");
				long systime = rset.getLong("systime");
				int restoreType = rset.getInt("restore_type");

				L2Skill skill = SkillTable.getInstance().getInfo(rset.getInt("skill_id"), rset.getInt("skill_level"));
				if(skill == null)
				{
					continue;
				}

				long remainingTime = systime - System.currentTimeMillis();
				if(remainingTime > 10)
				{
					disableSkill(skill, remainingTime);
					addTimeStamp(skill, reuseDelay, systime);
				}

				/**
				 * Restore Type 1
				 * The remaning skills lost effect upon logout but
				 * were still under a high reuse delay.
				 */
				if(restoreType > 0)
				{
					continue;
				}

				/**
				 * Restore Type 0
				 * These skill were still in effect on the character
				 * upon logout. Some of which were self casted and
				 * might still have had a long reuse delay which also
				 * is restored.
				 */
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

			DatabaseUtils.closeStatement(statement);

			statement = con.prepareStatement(Characters.EFFECTS_CLEAR);
			statement.setInt(1, getObjectId());
			statement.setInt(2, _classIndex);
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

	public PcAppearance getAppearance()
	{
		return _appearance;
	}

	/**
	 * @return the base L2PcTemplate link to the L2PcInstance.
	 */
	public L2PcTemplate getBaseTemplate()
	{
		return ClassTemplateTable.getInstance().getTemplate(_baseClassId);
	}

	public void setBaseClassId(int baseClass)
	{
		_baseClassId = baseClass;
	}

	public boolean isInStoreMode()
	{
		return _privateStoreType != PlayerPrivateStoreType.NONE;
	}

	public boolean isInCraftMode()
	{
		return _privateStoreType == PlayerPrivateStoreType.MANUFACTURE;
	}

	/**
	 * Manage Logout Task: <li>Remove player from world <BR>
	 * {@link L2PcInstance#onDelete()}</li> <li>Save player data into DB
	 * <BR>
	 * {@link L2GameClient#saveCharToDisk()}</li> <BR>
	 * <BR>
	 */
	public void logout()
	{
		logout(true);
	}

	/**
	 * Manage Logout Task: <li>Remove player from world <BR>
	 * {@link L2PcInstance#onDelete()} ()} </li> <li>Save player data into DB
	 * <BR>
	 * {@link L2GameClient#saveCharToDisk()}</li> <BR>
	 * <BR>
	 *
	 * @param closeClient
	 */
	public void logout(boolean closeClient)
	{
		try
		{
			closeNetConnection(closeClient);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on logout(): " + e.getMessage(), e);
		}
	}

	/**
	 * @return the Id for the last talked quest NPC.
	 */
	public int getLastQuestNpcObject()
	{
		return _questNpcObject;
	}

	public void setLastQuestNpcObject(int npcId)
	{
		_questNpcObject = npcId;
	}

	/**
	 * @param quest The name of the quest
	 * @return the QuestState object corresponding to the quest name.
	 */
	public QuestState getQuestState(String quest)
	{
		return _quests.get(quest);
	}

	/**
	 * @param questClass класс квеста
	 * @return the QuestState object corresponding to the quest name.
	 */
	public QuestState getQuestState(Class<? extends Quest> questClass)
	{
		return _quests.get(questClass.getSimpleName());
	}

	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param qs The QuestState to add to _quest
	 */
	public void setQuestState(QuestState qs)
	{
		_quests.put(qs.getQuestName(), qs);
	}

	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.<BR><BR>
	 *
	 * @param quest The name of the quest
	 */
	public void delQuestState(String quest)
	{
		_quests.remove(quest);
	}

	private QuestState[] addToQuestStateArray(QuestState[] questStateArray, QuestState state)
	{
		int len = questStateArray.length;
		QuestState[] tmp = new QuestState[len + 1];
		System.arraycopy(questStateArray, 0, tmp, 0, len);
		tmp[len] = state;
		return tmp;
	}

	/**
	 * @return a table containing all Quest in progress from the table _quests.
	 */
	public Quest[] getAllActiveQuests()
	{
		List<Quest> quests = new ArrayList<>();

		for(QuestState qs : _quests.values())
		{
			if(qs == null)
			{
				continue;
			}

			if(qs.getQuest() == null)
			{
				continue;
			}

			int questId = qs.getQuest().getQuestId();
			if(questId > 19999 || questId < 1)
			{
				continue;
			}

			if(!qs.isStarted() && !Config.DEVELOPER)
			{
				continue;
			}

			quests.add(qs.getQuest());
		}

		return quests.toArray(new Quest[quests.size()]);
	}

	/**
	 * @param npcId The Identifier of the NPC
	 * @return table containing all QuestState from the table _quests in which the L2PcInstance must talk to the NPC.
	 */
	public QuestState[] getQuestsForTalk(int npcId)
	{
		// Create a QuestState table that will contain all QuestState to modify
		QuestState[] states = null;

		// Go through the QuestState of the L2PcInstance quests

		// L2NpcTemplate НПЦ, с которым разговаривает игрок
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(npcId);

		// Берем все квесты с ON_TALK событиями для НПЦ
		List<Quest> onTalkQuests = npcTemplate.getEventQuests(Quest.QuestEventType.ON_TALK);
		/*
		// Берем все квесты с ON_FIRST_TALK событиями для НПЦ
		List<Quest> onFirstTalkQuests = npcTemplate.getEventQuests(Quest.QuestEventType.ON_FIRST_TALK);

		// Добавляем в список onTalkQuests те скрипты, в которых есть только ON_FIRST_TALK событие
		for (Quest onFirstTalkQuest : onFirstTalkQuests)
		{
			if (!onTalkQuests.contains(onFirstTalkQuest))
			{
				onTalkQuests.add(onFirstTalkQuest);
			}
		}
        */
		if(onTalkQuests != null)
		{
			for(Quest quest : onTalkQuests)
			{
				if(quest != null)
				{
					// Copy the current L2PcInstance QuestState in the QuestState table
					if(getQuestState(quest.getName()) != null)
					{
						states = states == null ? new QuestState[]{
							getQuestState(quest.getName())
						} : addToQuestStateArray(states, getQuestState(quest.getName()));
					}
				}
			}
		}

		// Return a table containing all QuestState to modify
		return states;
	}

	/**
	 * Заточка по офф-лайк диалоги. "reply" это часть ссылки "&reply=X".
	 *
	 * @param quest Имя квеста.
	 * @param reply ID запроса.
	 * @return
	 */
	public QuestState processQuestEvent(Quest quest, int reply)
	{
		QuestState retval = null;

		if(quest == null)
		{
			return retval;
		}

		QuestState qs = getQuestState(quest.getName());
		if(qs == null && reply <= Integer.MIN_VALUE)
		{
			return retval;
		}

		if(qs == null)
		{
			qs = quest.newQuestState(this);
		}
		if(qs != null)
		{
			if(_questNpcObject > 0)
			{
				L2Object object = WorldManager.getInstance().findObject(_questNpcObject);
				if(object instanceof L2Npc && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());

					if(states != null)
					{
						for(QuestState state : states)
						{
							if(state.getQuest().getName().equals(qs.getQuest().getName()))
							{
								if(qs.getQuest().notifyAskReplyEvent(npc, this, reply))
								{
									showQuestWindow(quest, qs.getState());
								}

								retval = qs;
							}
						}
					}
				}
			}
		}

		return retval;
	}

	public QuestState processQuestEvent(Quest quest, String event)
	{
		QuestState retval = null;
		if(quest == null)
		{
			return retval;
		}

		if(event == null)
		{
			event = "";
		}

		QuestState qs = getQuestState(quest.getName());
		if(qs == null && event.isEmpty())
		{
			return retval;
		}
		if(qs == null)
		{
			qs = quest.newQuestState(this);
		}
		if(qs != null)
		{
			if(_questNpcObject > 0)
			{
				L2Object object = WorldManager.getInstance().findObject(_questNpcObject);
				if(object instanceof L2Npc && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false))
				{
					L2Npc npc = (L2Npc) object;
					QuestState[] states = getQuestsForTalk(npc.getNpcId());

					if(states != null)
					{
						for(QuestState state : states)
						{
							if(state.getQuest().getName().equals(qs.getQuest().getName()))
							{
								if(qs.getQuest().notifyEvent(event, npc, this))
								{
									showQuestWindow(quest, qs.getState());
								}

								retval = qs;
							}
						}
					}
				}
			}
		}

		return retval;
	}

	/***
	 * TODO: Что это за бред и для чего нужен? о_О
	 * @param questId
	 * @param stateId
	 */
	private void showQuestWindow(Quest questId, QuestStateType stateId)
	{
		String path = "quests/" + questId.getQuestId() + '/' + stateId + ".htm";
		String content = HtmCache.getInstance().getHtmQuest(getLang(), path);

		if(content != null)
		{
			NpcHtmlMessage npcReply = new NpcHtmlMessage(5);
			npcReply.setHtml(content);
			sendPacket(npcReply);
		}

		sendActionFailed();
	}

	/**
	 * @param macro the macro to add to this L2PcInstance.
	 */
	public void registerMacro(L2Macro macro)
	{
		_macroses.registerMacro(macro);
	}

	/**
	 * @param id the macro Id to delete.
	 */
	public void deleteMacro(int id)
	{
		_macroses.deleteMacro(id);
	}

	/**
	 * @return all L2Macro of the L2PcInstance.
	 */
	public MacroList getMacroses()
	{
		return _macroses;
	}

	/**
	 * @return cторона, за которую сражается на осаде игрок
	 */
	public PlayerSiegeSide getSiegeSide()
	{
		return _siegeState;
	}

	/**
	 * Установить сторону, за которую будет сражаться на осаде игрок
	 * @param siegeSide cторона, за которую сражается на осаде игрок
	 */
	public void setSiegeSide(PlayerSiegeSide siegeSide)
	{
		_siegeState = siegeSide;
	}

	/***
	 * @return ID резиденции (форта или замка) в осаде которой принимает участие игрок
	 */
	public int getActiveSiegeId()
	{
		return _activeSiegeId;
	}

	/***
	 * установить ID резиденции, в осаде которой игрок принимает участие
	 * @param residenceId ID замка или форта
	 */
	public void setActiveSiegeId(int residenceId)
	{
		_activeSiegeId = residenceId;
	}

	public boolean isRegisteredOnThisSiegeField(int val)
	{
		return !(_activeSiegeId != val && (_activeSiegeId < 81 || _activeSiegeId > 89));
	}

	/**
	 * @return True if the L2PcInstance can Craft Dwarven Recipes.
	 */
	public boolean hasDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN) >= 1;
	}

	public int getDwarvenCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_DWARVEN);
	}

	/* У перерожденных чаров добавлен скил ID = 248, Lvl = 6 отвечающий за кристализацию */
	public boolean hasCrystalization()
	{
		return getSkillLevel(L2Skill.SKILL_CRYSTALLIZE) >= 1;
	}

	/**
	 * @return True if the L2PcInstance can Craft Dwarven Recipes.
	 */
	public boolean hasCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON) >= 1;
	}

	public int getCommonCraft()
	{
		return getSkillLevel(L2Skill.SKILL_CREATE_COMMON);
	}

	/**
	 * @return the PK counter of the L2PcInstance.
	 */
	public int getPkKills()
	{
		return _pkKills;
	}

	/**
	 * Set the PK counter of the L2PcInstance.<BR><BR>
	 *
	 * @param pkKills
	 */
	public void setPkKills(int pkKills)
	{
		_pkKills = pkKills;
	}

	/**
	 * @return the _deleteTimer of the L2PcInstance.
	 */
	public long getDeleteTimer()
	{
		return _deleteTimer;
	}

	/**
	 * Set the _deleteTimer of the L2PcInstance.<BR><BR>
	 *
	 * @param deleteTimer
	 */
	public void setDeleteTimer(long deleteTimer)
	{
		_deleteTimer = deleteTimer;
	}

	/**
	 * @return количество рекоммендаций у игрока
	 */
	public int getRecommendations()
	{
		return _recomendationHave;
	}

	/**
	 * Установить количество рекоммендаций текущему игроку (Максимум : 255)
	 * @param value количество рекоммендаций
	 */
	public void setRecommendations(int value)
	{
		if(value > 255)
		{
			_recomendationHave = 255;
		}
		else
		{
			_recomendationHave = value < 0 ? 0 : value;
		}
	}

	/**
	 * Увеличить количество рекоммендаций текущему игроку на +1 (Максимум : 255)
	 */
	protected void increaseRecommendations()
	{
		if(_recomendationHave < 255)
		{
			_recomendationHave++;
		}
	}

	/**
	 * @return количествао рекоммендаций на раздачу у текущего игрока
	 */
	public int getRecommendationsLeft()
	{
		return _recomendationLeft;
	}

	/**
	 * Установить количество доступных на раздачу рекоммендаций текущему игроку (Максимум : 255)
	 * @param value количество рекоммендаций
	 */
	public void setRecommendationsLeft(int value)
	{
		if(value > 255)
		{
			_recomendationLeft = 255;
		}
		else
		{
			_recomendationLeft = value < 0 ? 0 : value;
		}
	}

	/**
	 * Уменьшить количество доступных на раздачу рекоммендаций текущему игроку
	 */
	protected void decreaseRecommendationsLeft()
	{
		if(_recomendationLeft > 0)
		{
			_recomendationLeft--;
		}
	}

	/***
	 * Дать рекоммендацию игроку в таргете
	 * @param target игрок, которому даем рекоммендацию
	 */
	public void giveRecommendation(L2PcInstance target)
	{
		target.increaseRecommendations();
		decreaseRecommendationsLeft();
	}

	public long getExpBeforeDeath()
	{
		return _expBeforeDeath;
	}

	/**
	 * Set the exp of the L2PcInstance before a death
	 *
	 * @param exp
	 */
	public void setExpBeforeDeath(long exp)
	{
		_expBeforeDeath = exp;
	}

	/**
	 * Проверка на отрицательную репутацию
	 *
	 * @return является-ли репутация отрицательной
	 */
	public boolean hasBadReputation()
	{
		return _reputation < 0;
	}

	public int getExpertiseArmorPenalty()
	{
		return _expertiseArmorPenalty;
	}

	public int getExpertiseWeaponPenalty()
	{
		return _expertiseWeaponPenalty;
	}

	public int getExpertisePenaltyBonus()
	{
		return _expertisePenaltyBonus;
	}

	public void setExpertisePenaltyBonus(int bonus)
	{
		_expertisePenaltyBonus = bonus;
	}

	public int getWeightPenalty()
	{
		if(_dietMode)
		{
			return 0;
		}
		return _curWeightPenalty;
	}

	/**
	 * Update the overloaded status of the L2PcInstance.<BR><BR>
	 */
	public void refreshOverloaded()
	{
		int maxLoad = getMaxLoad();
		if(maxLoad > 0)
		{
			long weightproc = (long) ((getCurrentLoad() - calcStat(Stats.WEIGHT_PENALTY, 1, this, null)) * 1000 / maxLoad);
			int newWeightPenalty;
			if(weightproc < 500L || _dietMode)
			{
				newWeightPenalty = 0;
			}
			else if(weightproc < 666L)
			{
				newWeightPenalty = 1;
			}
			else if(weightproc < 800L)
			{
				newWeightPenalty = 2;
			}
			else
			{
				newWeightPenalty = weightproc < 1000L ? 3 : 4;
			}

			if(_curWeightPenalty != newWeightPenalty)
			{
				_curWeightPenalty = newWeightPenalty;
				if(newWeightPenalty > 0 && !_dietMode)
				{
					addSkill(SkillTable.getInstance().getInfo(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() > maxLoad);
				}
				else
				{
					super.removeSkill(getKnownSkill(4270));
					setIsOverloaded(false);
				}
				sendUserInfo();
				sendPacket(new EtcStatusUpdate(this));
				broadcastPacket(new CI(this));
			}
		}
		sendPacket(new ExAdenaInvenCount(this));
	}

	public void refreshExpertisePenalty()
	{
		if(!Config.EXPERTISE_PENALTY)
		{
			return;
		}

		int expertiseLevel = getExpertiseLevel();
		int armorPenalty = 0;
		int weaponPenalty = 0;
		int crystaltype;

		for(L2ItemInstance item : _inventory.getItems())
		{
			if(item != null && item.isEquipped() && item.getItemType() != L2EtcItemType.ARROW && item.getItemType() != L2EtcItemType.BOLT)
			{
				crystaltype = item.getItem().getCrystalType().ordinal();

				if(crystaltype > getExpertiseLevel())
				{
					if(item.isWeapon() && crystaltype > weaponPenalty)
					{
						weaponPenalty = crystaltype;
					}
					else if(crystaltype > armorPenalty)
					{
						armorPenalty = crystaltype;
					}
				}
			}
		}

		boolean changed = false;
		int bonus = _expertisePenaltyBonus;

		// calc weapon penalty
		weaponPenalty = weaponPenalty - expertiseLevel - bonus;
		if(weaponPenalty < 0)
		{
			weaponPenalty = 0;
		}
		else if(weaponPenalty > 4)
		{
			weaponPenalty = 4;
		}

		if(_expertiseWeaponPenalty != weaponPenalty || getSkillLevel(6209) != weaponPenalty)
		{
			_expertiseWeaponPenalty = weaponPenalty;

			if(_expertiseWeaponPenalty > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(6209, _expertiseWeaponPenalty)); // level used to be newPenalty
			}
			else
			{
				super.removeSkill(getKnownSkill(6209));
			}

			changed = true;
		}

		// calc armor penalty
		armorPenalty = armorPenalty - expertiseLevel - bonus;

		if(armorPenalty < 0)
		{
			armorPenalty = 0;
		}
		else if(armorPenalty > 4)
		{
			armorPenalty = 4;
		}

		if(_expertiseArmorPenalty != armorPenalty || getSkillLevel(6213) != armorPenalty)
		{
			_expertiseArmorPenalty = armorPenalty;

			if(_expertiseArmorPenalty > 0)
			{
				addSkill(SkillTable.getInstance().getInfo(6213, _expertiseArmorPenalty)); // level used to be newPenalty
			}
			else
			{
				super.removeSkill(getKnownSkill(6213));
			}

			changed = true;
		}

		if(changed)
		{
			sendPacket(new EtcStatusUpdate(this));
		}
	}

	public void checkIfWeaponIsAllowed()
	{
		// Override for Gamemasters
		if(isGM())
		{
			return;
		}

		// Iterate through all effects currently on the character.
		for(L2Effect currenteffect : getAllEffects())
		{
			L2Skill effectSkill = currenteffect.getSkill();

			// Ignore all buff skills that are party related (ie. songs, dances) while still remaining weapon dependant on cast though.
			if(!effectSkill.isOffensive() && !(effectSkill.getTargetType() == L2TargetType.TARGET_PARTY && effectSkill.getSkillType() == L2SkillType.BUFF))
			{
				// Check to rest to assure current effect meets weapon
				// requirements.
				if(!effectSkill.getWeaponDependancy(this))
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
					sm.addSkillName(effectSkill);
					sendPacket(sm);
					currenteffect.exit();
				}
			}
		}
	}

	public void checkSShotsMatch(L2ItemInstance equipped, L2ItemInstance unequipped)
	{
		if(unequipped == null)
		{
			return;
		}

		unequipped.setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
		unequipped.setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
	}

	public void useEquippableItem(L2ItemInstance item, boolean abortAttack)
	{
		// Equip or unEquip
		L2ItemInstance[] items;
		boolean isEquiped = item.isEquipped();
		int oldInvLimit = getInventoryLimit();
		SystemMessage sm;
		if((item.getItem().getBodyPart() & L2Item.SLOT_MULTI_ALLWEAPON) != 0)
		{
			checkSShotsMatch(item, _inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND));
		}

		if(isEquiped)
		{
			if(item.getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(item.getEnchantLevel());
				sm.addItemName(item);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(item);
			}
			sendPacket(sm);

            long slot = getInventory().getSlotFromItem(item);
            if ((slot == L2Item.SLOT_DECO) || (slot == L2Item.SLOT_BROACH))
            {
                items = getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
            }
            else
            {
                items = getInventory().unEquipItemInBodySlotAndRecord(slot);
            }
		}
		else
		{
			items = _inventory.equipItemAndRecord(item);

			if(item.isEquipped())
			{
				if(item.getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED);
					sm.addItemName(item);
				}
				sendPacket(sm);

				// Consume mana - will start a task if required; returns if item is not a shadow item
				item.decreaseMana(false);

				if((item.getItem().getBodyPart() & L2Item.SLOT_MULTI_ALLWEAPON) != 0)
				{
					rechargeAutoSoulShot(true, true, false);
				}
			}
			else
			{
				sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			}
		}
		refreshExpertisePenalty();

		broadcastUserInfo();

		InventoryUpdate iu = new InventoryUpdate();
		iu.addItems(Arrays.asList(items));
		sendPacket(iu);

		if(abortAttack)
		{
			abortAttack();
		}

		if(getInventoryLimit() != oldInvLimit)
		{
			sendPacket(new ExStorageMaxCount(this));
		}
	}

	/**
	 * @return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).
	 */
	public int getPvpKills()
	{
		return _pvpKills;
	}

	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).<BR><BR>
	 *
	 * @param pvpKills
	 */
	public void setPvpKills(int pvpKills)
	{
		_pvpKills = pvpKills;
	}

	/**
	 * @return the Fame of this L2PcInstance
	 */
	public int getFame()
	{
		return _fame;
	}

	/**
	 * Set the Fame of this L2PcInstane <BR><BR>
	 *
	 * @param fame
	 */
	public void setFame(int fame)
	{
		_fame = fame > Config.MAX_PERSONAL_FAME_POINTS ? Config.MAX_PERSONAL_FAME_POINTS : fame;
	}

	public int getJumpId()
	{
		return _currentJumpId;
	}

	public void setJumpId(int jumpid)
	{
		_currentJumpId = jumpid;
	}

	public boolean isJumping()
	{
		return _isJumping;
	}

	public void setJumping(boolean val)
	{
		_isJumping = val;
	}

	/**
	 * @return the ClassId object of the L2PcInstance contained in L2PcTemplate.
	 */
	public ClassId getClassId()
	{
		return getTemplate().getClassId();
	}

	/**
	 * Set the template of the L2PcInstance.<BR>
	 * <BR>
	 *
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 */
	public void setClassId(int Id)
	{
		if(!_subclassLock.tryLock())
		{
			return;
		}

		try
		{
			// Если игрок закончил академию и получил 4-ю профессию - выкидываем его из клана и дает очки клану
			if(_lvlJoinedAcademy != 0 && _clan != null && PlayerClass.values()[Id].getLevel() == ClassLevel.AWAKEN)
			{
				// Очки репутации клана = min {(85 - ур. на момент вступления), 40} х 45 + 200, но максимум - 2000 очков репутации клана.
				// Old formula: int points = Math.min(Math.min(85 - getLvlJoinedAcademy(), 40) * 45 + 200, 2000);
				int points = 1000 - Math.max(0, _lvlJoinedAcademy - 44) * 20;
				_clan.addReputationScore(points, true);
				_lvlJoinedAcademy = 0;
				variablesController.set("academyCompleted", true);

				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_GRADUATED_FROM_ACADEMY);
				msg.addPcName(this);
				msg.addNumber(points);

				_clan.broadcastToOnlineMembers(msg);
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
				_clan.removeClanMember(getObjectId(), 0);
				sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
			}
			if(isSubClassActive())
			{
				getSubClasses().get(_classIndex).setClassId(Id);
			}
			setTarget(this);
			broadcastPacket(new MagicSkillUse(this, 5103, 1, 1000, 0));
			setClassIdTemplate(Id);
			if(getClassId().level() == ClassLevel.THIRD.ordinal())
			{
				sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
			}
			else
			{
				sendPacket(SystemMessageId.CLASS_TRANSFER);
			}

			// Update class icon in party and clan
			if(isInParty())
			{
				_party.broadcastPacket(new PartySmallWindowUpdate(this));
			}

			if(_clan != null)
			{
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
			}

			if(Config.AUTO_LEARN_SKILLS)
			{
				rewardSkills();
			}
			if(!isGM() && Config.DECREASE_SKILL_LEVEL)
			{
				checkPlayerSkills();
			}
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	/**
	 * Return the Experience of the L2PcInstance.
	 */
	public long getExp()
	{
		return getStat().getExp();
	}

	/**
	 * Set the Experience value of the L2PcInstance.
	 *
	 * @param exp
	 */
	public void setExp(long exp)
	{
		if(exp < 0)
		{
			exp = 0;
		}

		getStat().setExp(exp);
	}

	public L2ItemInstance getActiveEnchantAttrItem()
	{
		return _activeEnchantAttrItem;
	}

	public void setActiveEnchantAttrItem(L2ItemInstance stone)
	{
		_activeEnchantAttrItem = stone;
	}

    public L2ItemInstance getActiveEnchantItem()
    {
        return _activeEnchantItem;
    }

    public void setActiveEnchantItem(L2ItemInstance scroll)
    {
        if (scroll == null)
        {
            setActiveEnchantSupportItem(null);
            setActiveEnchantTimestamp(0);
            setIsEnchanting(false);
        }
        _activeEnchantItem = scroll;
    }

    public L2ItemInstance getActiveEnchantSupportItem()
    {
        return _activeEnchantSupportItem;
    }

    public void setActiveEnchantSupportItem(L2ItemInstance item)
    {
        _activeEnchantSupportItem = item;
    }

    public long getActiveEnchantTimestamp()
    {
        return _activeEnchantTimestamp;
    }

    public void setActiveEnchantTimestamp(long val)
    {
        _activeEnchantTimestamp = val;
    }

    public void setIsEnchanting(boolean val)
    {
        _isEnchanting = val;
    }

    public boolean isEnchanting()
    {
        return _isEnchanting;
    }

	public L2ItemInstance getActiveShapeShiftingItem()
	{
		return _activeShapeShiftingItem;
	}

	public void setActiveShapeShiftingItem(L2ItemInstance scroll)
	{
		_activeShapeShiftingItem = scroll;
	}

	/* Return true if Hellbound minimap allowed */

	public L2ItemInstance getActiveShapeShiftingTargetItem()
	{
		return _activeShapeShiftingTargetItem;
	}

	/* Enable or disable minimap on Hellbound */

	public void setActiveShapeShiftingTargetItem(L2ItemInstance item)
	{
		_activeShapeShiftingTargetItem = item;
	}

	public L2ItemInstance getActiveShapeShiftingSupportItem()
	{
		return _activeShapeShiftingSupportItem;
	}

	public void setActiveShapeShiftingSupportItem(L2ItemInstance item)
	{
		_activeShapeShiftingSupportItem = item;
	}

	/**
	 * @return the fists weapon of the L2PcInstance (used when no weapon is equipped).
	 */
	public L2Weapon getFistsWeaponItem()
	{
		return _fistsWeaponItem;
	}

	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).<BR><BR>
	 *
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 */
	public void setFistsWeaponItem(L2Weapon weaponItem)
	{
		_fistsWeaponItem = weaponItem;
	}

	/**
	 * @param classId
	 * @return the fists weapon of the L2PcInstance Class (used when no weapon is equipped).
	 */
	public L2Weapon findFistsWeaponItem(int classId)
	{
		L2Weapon weaponItem = null;
		if(classId >= 0x00 && classId <= 0x09)
		{
			// human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x0a && classId <= 0x11)
		{
			// human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x12 && classId <= 0x18)
		{
			// elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x19 && classId <= 0x1e)
		{
			// elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x1f && classId <= 0x25)
		{
			// dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x26 && classId <= 0x2b)
		{
			// dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x2c && classId <= 0x30)
		{
			// orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x31 && classId <= 0x34)
		{
			// orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		}
		else if(classId >= 0x35 && classId <= 0x39)
		{
			// dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}

		return weaponItem;
	}

	/**
	 * Give Expertise skill of this level and remove beginner Lucky skill.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the Expertise skill
	 * corresponding to its Expertise level</li> <li>Update the overloaded
	 * status of the L2PcInstance</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T give other
	 * free skills (SP needed = 0)</B></FONT><BR>
	 * <BR>
	 */
	public void rewardSkills()
	{
		HookManager.getInstance().notifyEvent(HookType.ON_REWARD_SKILLS, getHookContainer(), this);
		//Give all normal skills if activated Auto-Learn is activated, included AutoGet skills.
		if(Config.AUTO_LEARN_SKILLS)
		{
			giveAvailableAutoGetSkills();
			if(getClassId().level() < ClassLevel.AWAKEN.ordinal())
			{
				giveAvailableSkills(true, true);
			}
		}
		else
		{
			giveAvailableAutoGetSkills();
		}

		// Шлем иконку о доступных к изучению скилов
		if(SkillTreesData.getInstance().hasNewSkillsByLevel(this))
		{
			sendPacket(new ExNewSkillToLearnByLevelUp());
		}

		checkItemRestriction();
		sendSkillList();
	}

	/**
	 * Regive all skills which aren't saved to database, like Noble, Hero, Clan
	 * Skills<BR>
	 * <BR>
	 */
	public void regiveTemporarySkills()
	{
		// Do not call this on enterworld or char load

		// Add noble skills if noble
		if(_isNoble)
		{
			setNoble(true);
		}

		// Add Hero skills if hero
		if(_olympiadController.isHero())
		{
			_olympiadController.giveHeroSkills();
		}

		// Add clan skills
		if(_clan != null)
		{
			L2Clan clan = _clan;
			clan.addSkillEffects(this);

			if(clan.getLevel() >= CastleSiegeManager.getInstance().getSiegeClanMinLevel() && isClanLeader())
			{
				CastleSiegeManager.getInstance().addSiegeSkills(this);
			}
			if(_clan.getCastleId() > 0)
			{
				CastleManager.getInstance().getCastleByOwner(_clan).giveResidentialSkills(this);
			}
			if(_clan.getFortId() > 0)
			{
				FortManager.getInstance().getFortByOwner(_clan).giveResidentialSkills(this);
			}
		}

		// Reload passive skills from armors / jewels / weapons
		_inventory.reloadEquippedItems();
	}

	/**
	 * Give all available skills to the player.<br>
	 *
	 * @param includedByFs
	 * @param includeAutoGet
	 * @return skillCounter, the amount of new skills added.
	 */
	public int giveAvailableSkills(boolean includedByFs, boolean includeAutoGet)
	{
		int unLearnable = 0;
		int skillCounter = 0;

		// Get available skills
		List<L2SkillLearn> skills = SkillTreesData.getInstance().getAvailableSkills(this, getClassId(), includedByFs, includeAutoGet, false);

		while(skills.size() > unLearnable)
		{
			for(L2SkillLearn s : skills)
			{
				L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
				// Prevent infinite loop
				if(sk.getLevel() < s.getSkillLevel())
				{
					_log.warn("Player trying to learn lower-level skill (ID " + s.getSkillId() + "). Seems skill level X pasted into skill tree, but skill only have (X - 1) levels!");
					break;
				}

				if(sk == null || sk.getId() == L2Skill.SKILL_DIVINE_INSPIRATION && !Config.AUTO_LEARN_DIVINE_INSPIRATION && !isGM())
				{
					unLearnable++;
					continue;
				}

				if(getSkillLevel(sk.getId()) == -1)
				{
					skillCounter++;
				}

				// fix when learning toggle skills
				if(sk.isToggle())
				{
					L2Effect toggleEffect = getFirstEffect(sk.getId());
					if(toggleEffect != null)
					{
						// stop old toggle skill effect, and give new toggle skill effect back
						toggleEffect.exit();
						sk.getEffects(this, this);
					}
				}

				addSkill(sk, true);
			}

			//Get new available skills, some skills depend of previous skills to be available.
			skills = SkillTreesData.getInstance().getAvailableSkills(this, getClassId(), includedByFs, includeAutoGet, false);
		}

		sendMessage("You have learned " + skillCounter + " new skills.");
		return skillCounter;
	}

	/**
	 * Give all available AutoGet skills to the player.<br>
	 */
	public void giveAvailableAutoGetSkills()
	{
		// Get available skills
		List<L2SkillLearn> autoGetSkills = SkillTreesData.getInstance().getAvailableAutoGetSkills(this);

		SkillTable st = SkillTable.getInstance();
		L2Skill skill;
		for(L2SkillLearn s : autoGetSkills)
		{
			skill = st.getInfo(s.getSkillId(), s.getSkillLevel());
			if(skill != null)
			{
				addSkill(skill, true);
			}
			else
			{
				_log.warn("Skipping null autoGet Skill for player: " + this);
			}
		}
	}

	/**
	 * @return the Race object of the L2PcInstance.
	 */
	public Race getRace()
	{
		if(!isSubClassActive())
		{
			return getTemplate().getRace();
		}

		return ClassTemplateTable.getInstance().getTemplate(_baseClassId).getRace();
	}

	public L2Radar getRadar()
	{
		return _radar;
	}

	public boolean isMinimapAllowed()
	{
		return _minimapAllowed;
	}

	public void setMinimapAllowed(boolean b)
	{
		_minimapAllowed = b;
	}

	/**
	 * @return the SP amount of the L2PcInstance.
	 */
	public int getSp()
	{
		return getStat().getSp();
	}

	/**
	 * Set the SP amount of the L2PcInstance.
	 *
	 * @param sp
	 */
	public void setSp(int sp)
	{
		if(sp < 0)
		{
			sp = 0;
		}

		super.getStat().setSp(sp);
	}

	/**
	 * @param castleId
	 * @return true if this L2PcInstance is a clan leader in ownership of the passed castle
	 */
	public boolean isCastleLord(int castleId)
	{
		L2Clan clan = _clan;

		// player has clan and is the clan leader, check the castle info
		if(clan != null && clan.getLeader().getPlayerInstance().equals(this))
		{
			// if the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if(castle != null && castle.equals(CastleManager.getInstance().getCastleById(castleId)))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the Clan Identifier of the L2PcInstance.
	 */
	public int getClanId()
	{
		return _clanId;
	}

	/**
	 * @return the Clan Crest Identifier of the L2PcInstance or 0.
	 */
	public int getClanCrestId()
	{
		if(_clan != null)
		{
			return _clan.getCrestId();
		}

		return 0;
	}

	/**
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId()
	{
		if(_clan != null)
		{
			return _clan.getCrestLargeId();
		}

		return 0;
	}

	public long getClanJoinExpiryTime()
	{
		return _clanJoinExpiryTime;
	}

	public void setClanJoinExpiryTime(long time)
	{
		_clanJoinExpiryTime = time;
	}

	public long getClanCreateExpiryTime()
	{
		return _clanCreateExpiryTime;
	}

	public void setClanCreateExpiryTime(long time)
	{
		_clanCreateExpiryTime = time;
	}

	public void setOnlineTime(long time)
	{
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}

    public long getOnlineTime()
    {
        return _onlineTime;
    }

	public long getZoneRestartLimitTime()
	{
		return _zoneRestartLimitTime;
	}

	public void setZoneRestartLimitTime(long time)
	{
		_zoneRestartLimitTime = time;
	}

	public void storeZoneRestartLimitTime()
	{
		if(isInsideZone(L2Character.ZONE_NORESTART))
		{
			L2NoRestartZone zone = null;
			for(L2ZoneType tmpzone : ZoneManager.getInstance().getZones(this))
			{
				if(tmpzone instanceof L2NoRestartZone)
				{
					zone = (L2NoRestartZone) tmpzone;
					break;
				}
			}
			if(zone != null)
			{
				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(Characters.ZONE_RESTART_LIMIT_UPDATE);
					statement.setInt(1, getObjectId());
					statement.setLong(2, System.currentTimeMillis() + zone.getRestartAllowedTime() * 1000);
					statement.execute();
				}
				catch(SQLException e)
				{
					_log.log(Level.ERROR, "Cannot store zone norestart limit for character " + getObjectId(), e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}
			}
		}
	}

	private void restoreZoneRestartLimitTime()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.ZONE_RESTART_LIMIT_RESTORE);
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			if(rset.next())
			{
				_zoneRestartLimitTime = rset.getLong("time_limit");
				statement.close();
				statement = con.prepareStatement(Characters.ZONE_RESTART_LIMIT_CLEAR);
				statement.setInt(1, getObjectId());
				statement.executeUpdate();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore " + this + " zone restart time: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * @return PcInventory Inventory of the L2PcInstance contained in _inventory.
	 */
	@Override
	public PcInventory getInventory()
	{
		return _inventory;
	}

	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param itemId      : int Item identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(ProcessType process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if(itemId == PcInventory.ADENA_ID)
		{
			return reduceAdena(process, count, reference, sendMessage);
		}

		L2ItemInstance item = _inventory.getItemByItemId(itemId);

		if(item == null || item.getCount() < count || _inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return false;
		}

		// Send inventory update packet
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			sendPacket(new ItemList(this, false));
		}
		else
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}

		// Update current load as well
		sendPacket(new ExUserInfoInvenWeight(this));

		// Sends message to client if requested
		if(sendMessage)
		{
			if(count > 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(itemId);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(itemId);
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate
	 * packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param objectId    : int Item Instance identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(ProcessType process, int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);

		if(item == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		return destroyItem(process, item, count, reference, sendMessage);
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
				setQueuedSkill(null, false, false);
				_transformAllowedSkills = new int[]{};
				_transformation.onUntransform();
				_transformation = null;
				if(removeEffects)
				{
					stopEffects(L2EffectType.TRANSFORMATION);
				}

				sendSkillList();
				sendPacket(new SkillCoolTime(this));
				sendPacket(ExBasicActionList.getStaticPacket(this));
				broadcastUserInfo();
			}
		}
	}

	/**
	 * @return {@code true} if the L2PcInstance is a GM.
	 */
	@Override
	public boolean isGM()
	{
		return getAccessLevel().isGm();
	}

	/**
	 * @return the _accessLevel of the L2PcInstance.
	 */
	@Override
	public L2AccessLevel getAccessLevel()
	{
		if(Config.EVERYBODY_HAS_ADMIN_RIGHTS)
		{
			return AdminTable.getInstance().getMasterAccessLevel();
		}
		if(_accessLevel == null) /* This is here because inventory etc. is loaded before access level on login, so it is not null */
		{
			setAccessLevel(0);
		}

		return _accessLevel;
	}

	/**
	 * Set the _accessLevel of the L2PcInstance.
	 *
	 * @param level
	 */
	public void setAccessLevel(int level)
	{
		_accessLevel = AdminTable.getInstance().getAccessLevel(level);

		// Устанавливаем цвет ника и титула из таблицы прав только для ГМов
		if(isGM())
		{
			_appearance.setNameColor(_accessLevel.getNameColor());
			_appearance.setTitleColor(_accessLevel.getTitleColor());
			broadcastUserInfo();
		}

		CharNameTable.getInstance().addName(this);

		if(!AdminTable.getInstance().hasAccessLevel(level))
		{
			_log.log(Level.WARN, "Trying to set unregistered access level " + level + " for " + this + ". Setting access level without privileges!");
		}
		else if(level > 0)
		{
			_log.log(Level.WARN, _accessLevel.getName() + " access level set for character " + getName() + "! Just a warning to be careful ;)");
		}
	}

	@Override
	public void onTeleported()
	{
		super.onTeleported();

		if(isInAirShip())
		{
			getAirShip().sendInfo(this);
		}

		if(isInShuttle())
		{
			getShuttle().sendInfo(this);
		}

		// Force a revalidation
		revalidateZone(true);

		checkItemRestriction();

		if(Config.PLAYER_TELEPORT_PROTECTION > 0 && !_olympiadController.isParticipating())
		{
			setTeleportProtection(true);
		}

		// Trained beast is after teleport lost
		if(_tamedBeast != null)
		{
			for(L2TamedBeastInstance tamedBeast : _tamedBeast)
			{
				tamedBeast.getLocationController().delete();
			}
			_tamedBeast.clear();
		}

		// Modify the position of the pet if necessary
		if(!_summons.isEmpty())
		{
			for(L2Summon pet : _summons)
			{
				pet.setFollowStatus(false);
				pet.setShowSummonAnimation(true);
				pet.teleToLocation(getLocationController().getX() + Rnd.get(-120, 120), getLocationController().getY() + Rnd.get(-120, 120), getLocationController().getZ(), false);
				((L2SummonAI) pet.getAI()).setStartFollowController(true);
				pet.setFollowStatus(true);
				ThreadPoolManager.getInstance().scheduleGeneral(() -> {
					pet.updateAndBroadcastStatus(0);
					pet.onSpawn();
				}, 5000);
			}
		}
		EventManager.onTeleported(this);
		// HookManager.getInstance().notifyEvent(HookType.ON_TELEPORTED, getHookContainer(), this);
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov)
	{
		if(!(mov instanceof CI))
		{
			sendPacket(mov);
		}

		mov.setInvisible(_appearance.getInvisible());

		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			player.sendPacket(mov);
			if(mov instanceof CI)
			{
				int relation = getRelation(player);
				Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
				if(oldrelation != null && oldrelation != relation)
				{
					player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
					if(!_summons.isEmpty())
					{
						for(L2Summon pet : _summons)
						{
							player.sendPacket(new RelationChanged(pet, relation, isAutoAttackable(player)));
						}
					}
				}
			}
		}
	}

	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist)
	{
		if(!(mov instanceof CI))
		{
			sendPacket(mov);
		}

		mov.setInvisible(_appearance.getInvisible());

		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			if(player == null)
			{
				continue;
			}
			if(isInsideRadius(player, radiusInKnownlist, false, false))
			{
				player.sendPacket(mov);
				if(mov instanceof CI)
				{
					int relation = getRelation(player);
					Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
					if(oldrelation != null && oldrelation != relation)
					{
						player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
						if(!_summons.isEmpty())
						{
							for(L2Summon pet : _summons)
							{
								player.sendPacket(new RelationChanged(pet, relation, isAutoAttackable(player)));
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance </li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party </li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT><BR><BR>
	 */
	@Override
	public void broadcastStatusUpdate()
	{
		//TODO We mustn't send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
		//super.broadcastStatusUpdate();

		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		sendPacket(su);

		boolean needCpUpdate = needCpUpdate(352);
		boolean needHpUpdate = needHpUpdate(352);
		// Check if a party is in progress and party window update is usefull
		L2Party party = _party;
		if(party != null && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
		{
			// Send the Server->Client packet PartySmallWindowUpdate with
			// current HP, MP and Level to all other L2PcInstance of the Party
			PartySmallWindowUpdate update = new PartySmallWindowUpdate(this);
			party.broadcastPacket(this, update);
		}

		if(_olympiadController.isParticipating() && _olympiadController.isPlayingNow() && (needCpUpdate || needHpUpdate))
		{
			OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(_olympiadController.getGameId());
			if(game != null && game.isBattleStarted())
			{
				game.getZone().broadcastStatusUpdate(this);
			}
		}
		// In duel MP updated only with CP or HP
		if(_isInDuel && (needCpUpdate || needHpUpdate))
		{
			ExDuelUpdateUserInfo update = new ExDuelUpdateUserInfo(this);
			DuelManager.getInstance().broadcastToOppositTeam(this, update);
		}
	}

	@Override
	public void sendMessage(String message)
	{
		sendPacket(SystemMessage.sendString(message));
	}

	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean allowRandomOffset)
	{
		if(_vehicle != null && !_vehicle.isTeleporting())
		{
			setVehicle(null);
		}

		if(_isFlyingMounted && z < -1005)
		{
			z = -1005;
		}

		super.teleToLocation(x, y, z, heading, allowRandomOffset);
	}

	@Override
	protected boolean checkDoCastConditions(L2Skill skill)
	{
		if(!super.checkDoCastConditions(skill))
		{
			return false;
		}

		switch(skill.getSkillType())
		{
			case SUMMON_TRAP:
				if(isInsideZone(ZONE_PEACE))
				{
					sendPacket(SystemMessageId.A_MALICIOUS_SKILL_CANNOT_BE_USED_IN_PEACE_ZONE);
					return false;
				}
				if(_trap != null && _trap.getSkill().getId() == ((L2SkillTrap) skill).getTriggerSkillId())
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
					sm.addSkillName(skill);
					sendPacket(sm);
					return false;
				}
				break;
			case SUMMON:
				if(!((L2SkillSummon) skill).isCubic() && (getSizePets() > 3 || isMounted()))
				{
					sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
					return false;
				}
		}

		// TODO: Should possibly be checked only in L2PcInstance's useMagic
		// Can't use Hero and resurrect skills during Olympiad
		if(_olympiadController.isParticipating() && (skill.isHeroSkill() || skill.getSkillType() == L2SkillType.RESURRECT))
		{
			sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		int charges = getCharges();
		// Check if the spell using charges or not in AirShip
		if(skill.getMaxCharges() == 0 && charges < skill.getNumCharges() || isInAirShip() && skill.getSkillType() != L2SkillType.REFUEL)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			sendPacket(sm);
			return false;
		}

		return true;
	}

	/**
	 * Index according to skill id the current timestamp of use.
	 * @param skill
	 * @param reuse delay
	 */
	@Override
	public void addTimeStamp(L2Skill skill, long reuse)
	{
		_reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse));
	}

	@Override
	public void doRevive()
	{
		super.doRevive();
		stopEffects(L2EffectType.CHARMOFCOURAGE);
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		_reviveRequested = 0;
		_revivePower = 0;

		if(isMounted())
		{
			startFeed(_mountNpcId);
		}
	}

	@Override
	public void doRevive(double revivePower)
	{
		// Restore the player's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}

	/**
	 * @return the AI of the L2PcInstance (create it if necessary)
	 */
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
					_ai = new L2PlayerAI(new AIAccessor());
				}
				return _ai;
			}
		}
		return ai;
	}

	@Override
	public boolean isAlikeDead()
	{
		if(super.isAlikeDead())
		{
			return true;
		}

		return _isFakeDeath;
	}

	@Override
	public boolean isMovementDisabled()
	{
		return super.isMovementDisabled() || _movieId > 0;
	}

	/**
	 * @return the L2Summon of the L2PcInstance or null.
	 */
	@Override
	public FastList<L2Summon> getPets()
	{
		return _summons;
	}

	@Override
	public boolean hasPet()
	{
		if(hasSummon())
		{
			for(L2Summon activeSummon : _summons)
			{
				if(activeSummon instanceof L2PetInstance || activeSummon.getPointsToSummon() == -1)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public L2Summon getItemPet()
	{
		if(hasSummon())
		{
			for(L2Summon activeSummon : _summons)
			{
				if(activeSummon instanceof L2PetInstance)
				{
					return activeSummon;
				}
			}
		}
		return null;
	}

	@Override
	public void setIsTeleporting(boolean teleport)
	{
		setIsTeleporting(teleport, true);
	}

	/**
	 * @return {@code true} if the L2PcInstance is invulnerable.
	 */
	@Override
	public boolean isInvul()
	{
		return super.isInvul() || _teleportProtectEndTime > GameTimeController.getInstance().getGameTicks() || HookManager.getInstance().checkEvent(HookType.ON_IS_INVUL_CHECK, null, true, this);
	}

	/**
	 * @return the L2PcTemplate link to the L2PcInstance.
	 */
	@Override
	public L2PcTemplate getTemplate()
	{
		return (L2PcTemplate) super.getTemplate();
	}

	public void setTemplate(ClassId newclass)
	{
		setTemplate(ClassTemplateTable.getInstance().getTemplate(newclass));
	}

	@Override
	public void stopAllEffects()
	{
		super.stopAllEffects();
		updateAndBroadcastStatus(2);
	}

	@Override
	public void stopAllEffectsExceptThoseThatLastThroughDeath()
	{
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(2);
	}

	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 */
	@Override
	public void updateAbnormalEffect()
	{
        sendPacket(new ExUserInfoAbnormalVisualEffect(this));
        broadcastPacket(new CI(this));
	}

	@Override
	public void revalidateZone(boolean force)
	{
		// Cannot validate if not in  a world region (happens during teleport)
		if(getLocationController().getWorldRegion() == null)
		{
			return;
		}

		// This function is called too often from movement code
		if(force)
		{
			_zoneValidateCounter = 4;
		}
		else
		{
			_zoneValidateCounter--;
			if(_zoneValidateCounter < 0)
			{
				_zoneValidateCounter = 4;
			}
			else
			{
				return;
			}
		}

		getLocationController().getWorldRegion().revalidateZones(this);

		if(Config.ALLOW_WATER)
		{
			checkWaterState();
		}

		if(isInsideZone(ZONE_ALTERED))
		{
			if(_lastCompassZone == ExSetCompassZoneCode.ALTEREDZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.ALTEREDZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE);
			sendPacket(cz);
		}
		else if(isInsideZone(ZONE_SIEGE))
		{
			if(_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2);
			sendPacket(cz);
		}
		else if(isInsideZone(ZONE_PVP))
		{
			if(_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE);
			sendPacket(cz);
		}
		else if(isInsideZone(ZONE_PEACE))
		{
			if(_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
			{
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE);
			sendPacket(cz);
		}
		else
		{
			if(_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
			{
				return;
			}
			if(_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
			{
				_pvpFlagController.updateStatus();
			}
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE);
			sendPacket(cz);
		}
	}

	/**
	 * Set a target.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character </li>
	 * <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character </li>
	 * <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)</li><BR><BR>
	 *
	 * @param newTarget The L2Object to target
	 */
	@Override
	public void setTarget(L2Object newTarget)
	{
		if(newTarget != null)
		{
			boolean isParty = newTarget instanceof L2PcInstance && isInParty() && _party.getMembers().contains(newTarget);

			// Check if the new target is visible
			if(!isParty && !newTarget.isVisible())
			{
				newTarget = null;
			}

			// Prevents /target exploiting
			if(newTarget != null && !isParty && Math.abs(newTarget.getZ() - getZ()) > 1000)
			{
				newTarget = null;
			}
		}

		// Get the current target
		L2Object oldTarget = getTarget();

		if(oldTarget != null)
		{
			if(oldTarget.equals(newTarget))
			{
				return; // no target change
			}

			// Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character
			if(oldTarget instanceof L2Character)
			{
				((L2Character) oldTarget).removeStatusListener(this);
			}
		}

		// Add the L2PcInstance to the _statusListener of the new target if it's a L2Character
		if(newTarget instanceof L2Character)
		{
			((L2Character) newTarget).addStatusListener(this);
			TargetSelected my = new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ());
			broadcastPacket(my);
		}
		if(newTarget == null && getTarget() != null)
		{
			broadcastPacket(new TargetUnselected(this));
		}

		// Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)
		super.setTarget(newTarget);
	}

	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.
	 */
	@Override
	protected boolean checkAndEquipArrows()
	{
		// Проверям, чтобы на игроке был надет лук
		if(getActiveWeaponItem().getItemType() == L2WeaponType.BOW)
		{
			// Если стрелы не заряжены
			if(_inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
			{
				// Get the L2ItemInstance of the arrows needed for this bow
				_arrowItem = _inventory.findArrowForBow(getActiveWeaponItem());

				if(_arrowItem != null)
				{
					// Equip arrows needed in left hand
					_inventory.setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);

					// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
					sendPacket(new ItemList(this, false));
				}
			}
			else
			{
				// Get the L2ItemInstance of arrows equiped in left hand
				_arrowItem = _inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			}
		}

		return _arrowItem != null;
	}

	/**
	 * Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.
	 */
	@Override
	protected boolean checkAndEquipBolts()
	{
		// Проверям, чтобы на игроке был надет арбалет
		if(getActiveWeaponItem().getItemType() == L2WeaponType.CROSSBOW || getActiveWeaponItem().getItemType() == L2WeaponType.TWOHANDCROSSBOW)
		{
			// Check if nothing is equiped in left hand
			if(_inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null)
			{
				// Get the L2ItemInstance of the arrows needed for this bow
				_boltItem = _inventory.findBoltForCrossBow(getActiveWeaponItem());

				if(_boltItem != null)
				{
					// Equip arrows needed in left hand
					_inventory.setPaperdollItem(Inventory.PAPERDOLL_LHAND, _boltItem);

					// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
					// update left hand equipement
					ItemList il = new ItemList(this, false);
					sendPacket(il);
				}
			}
			else
			{
				// Get the L2ItemInstance of arrows equiped in left hand
				_boltItem = _inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			}
		}

		return _boltItem != null;
	}

	@Override
	public void addExpAndSp(long addToExp, int addToSp)
	{
		getStat().addExpAndSp(addToExp, addToSp, false);
	}

	/**
	 * @return the active weapon instance (always equiped in the right hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance()
	{
		return _inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}

	/**
	 * @return the active weapon item (always equiped in the right hand).<BR><BR>
	 */
	@Override
	public L2Weapon getActiveWeaponItem()
	{
		L2ItemInstance weapon = getActiveWeaponInstance();

		if(weapon == null)
		{
			return _fistsWeaponItem;
		}

		return (L2Weapon) weapon.getItem();
	}

	/**
	 * @return the secondary weapon instance (always equiped in the left hand).<BR><BR>
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance()
	{
		return _inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}

	/**
	 * @return the secondary L2Item item (always equiped in the left hand). Arrows, Shield..
	 */
	@Override
	public L2Item getSecondaryWeaponItem()
	{
		L2ItemInstance item = _inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(item != null)
		{
			return item.getItem();
		}
		return null;
	}

	/**
	 * Manage hit process (called by Hit Task of L2Character).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the attacker/target is dead or use fake death, notify the AI with EVT_CANCEL and send a Server->Client packet ActionFailed (if attacker is a L2PcInstance)</li>
	 * <li>If attack isn't aborted, send a message system (critical hit, missed...) to attacker/target if they are L2PcInstance </li>
	 * <li>If attack isn't aborted and hit isn't missed, reduce HP of the target and calculate reflection damage to reduce HP of attacker if necessary </li>
	 * <li>if attack isn't aborted and hit isn't missed, manage attack or cast break of the target (calculating rate, sending message...) </li><BR><BR>
	 *
	 * @param target   The L2Character targeted
	 * @param damage   Nb of HP to reduce
	 * @param crit     True if hit is critical
	 * @param miss     True if hit is missed
	 * @param soulshot True if SoulShot are charged
	 * @param shld     True if shield is efficient
	 */
	@Override
	protected void onHitTimer(L2Character target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld)
	{
		super.onHitTimer(target, damage, crit, miss, soulshot, shld);
	}

	/**
	 * Reduce the number of arrows/bolts owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).<BR><BR>
	 */
	@Override
	protected void reduceArrowCount(boolean bolts)
	{
		L2ItemInstance arrows = _inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);

		if(arrows == null)
		{
			_inventory.unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if(bolts)
			{
				_boltItem = null;
			}
			else
			{
				_arrowItem = null;
			}
			sendPacket(new ItemList(this, false));
			return;
		}

		// Отнимаем стрелу, только если она не бесконечная
		if(!arrows.isInfinityItem())
		{
			// Adjust item quantity
			if(arrows.getCount() > 1)
			{
				synchronized(arrows)
				{
					arrows.changeCountWithoutTrace(-1, this, null);
					arrows.setLastChange(L2ItemInstance.MODIFIED);

					// could do also without saving, but let's save approx 1 of 10
					if(GameTimeController.getInstance().getGameTicks() % 10 == 0)
					{
						arrows.updateDatabase();
					}
					_inventory.refreshWeight();
				}
			}
			else
			{
				// Destroy entire item and save to database
				_inventory.destroyItem(ProcessType.CONSUME, arrows, this, null);

				_inventory.unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
				if(bolts)
				{
					_boltItem = null;
				}
				else
				{
					_arrowItem = null;
				}

				sendPacket(new ItemList(this, false));
				return;
			}

			if(Config.FORCE_INVENTORY_UPDATE)
			{
				sendPacket(new ItemList(this, false));
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(arrows);
				sendPacket(iu);
			}
		}
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		HookManager.getInstance().notifyEvent(HookType.ON_SPAWN, getHookContainer(), this);
	}

	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode </li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li>
	 * <li>Cancel Crafting, Attak or Cast </li>
	 * <li>Remove the L2PcInstance from the world </li>
	 * <li>Stop Party and Unsummon Pet </li>
	 * <li>Update database with items in its inventory and remove them from the world </li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI </li>
	 * <li>Close the connection with the client </li><BR><BR>
	 */
	@Override
	public boolean onDelete()
	{
		try
		{
			HookManager.getInstance().notifyEvent(HookType.ON_DELETEME, getHookContainer(), this);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		store();
		cleanup();
		return super.onDelete();
	}

	@Override
	public LocationController getLocationController()
	{
		if(_locationController == null)
		{
			_locationController = new LocationController(this);
		}

		return (LocationController) _locationController;
	}

	/**
	 * @return {@code true} if the L2PcInstance has a Party in progress.
	 */
	@Override
	public boolean isInParty()
	{
		return _party != null;
	}

	/**
	 * @return the _party object of the L2PcInstance.
	 */
	@Override
	public L2Party getParty()
	{
		return _party;
	}

	/**
	 * @return {@code true} if the L2PcInstance use a dual weapon.
	 */
	@Override
	public boolean isUsingDualWeapon()
	{
		L2Weapon weaponItem = getActiveWeaponItem();
		if(weaponItem == null)
		{
			return false;
		}

		return weaponItem.getItemType() == L2WeaponType.DUAL || weaponItem.getItemType() == L2WeaponType.DUALFIST || weaponItem.getItemType() == L2WeaponType.DUALDAGGER;
	}

	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2Character are identified in <B>_skills</B><BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Remove the skill from the L2Character _skills </li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li><BR><BR>
	 * <p/>
	 * <B><U> Overridden in </U> :</B><BR><BR>
	 * <li> L2PcInstance : Save update in the character_skills table of the database</li><BR><BR>
	 *
	 * @param skill The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	@Override
	public L2Skill removeSkill(L2Skill skill)
	{
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		boolean isSharedSkill = skill.isSharedSkill();
		L2Skill oldSkill = super.removeSkill(skill);
		if(oldSkill != null)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				// Remove or update a L2PcInstance skill from the character_skills table of the database
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Characters.SKILLS_DELETE);

				statement.setInt(1, oldSkill.getId());
				statement.setInt(2, getObjectId());
				statement.setInt(3, isSharedSkill ? -1 : _classIndex);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Error could not delete skill: " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}

		if(getTransformationId() > 0 || isCursedWeaponEquipped())
		{
			return oldSkill;
		}

		// TODO: Rough fix for shortcuts of augments getting removed. Find a better way
		_shortcutController.list().stream().filter(sc -> sc != null && sc.getId() == skill.getId() && sc.getType() == L2ShortCut.ShortcutType.SKILL && !(skill.getId() >= 3080 && skill.getId() <= 3259)).forEach(_shortcutController::removeShortcut);

		return oldSkill;
	}

	@Override
	public L2Skill removeSkill(L2Skill skill, boolean store)
	{
		if(store)
		{
			return removeSkill(skill);
		}
		return super.removeSkill(skill, true);
	}

	@Override
	public void enableSkill(L2Skill skill)
	{
		super.enableSkill(skill);
		_reuseTimeStampsSkills.remove(skill.getReuseHashCode());
	}

	/**
	 * Return the Level of the L2PcInstance.
	 */
	@Override
	public int getLevel()
	{
		return getStat().getLevel();
	}

	@Override
	public void reduceCurrentHp(double damage, L2Character attacker, boolean awake, boolean isDOT, L2Skill skill)
	{
		if(skill != null)
		{
			getStatus().reduceHp(damage, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
		}
		else
		{
			getStatus().reduceHp(damage, attacker, awake, isDOT, false, false);
		}

		if(attacker != null && attacker != this)
		{
			HookManager.getInstance().notifyEvent(HookType.ON_ATTACK, getHookContainer(), this, attacker, false);
		}

		if(_olympiadController.isOpponent(attacker))
		{
			OlympiadGameManager.getInstance().notifyCompetitorDamage((L2PcInstance) attacker, (int) damage);
		}

		// notify the tamed beast of attacks
		if(_tamedBeast != null)
		{
			for(L2TamedBeastInstance tamedBeast : _tamedBeast)
			{
				tamedBeast.onOwnerGotAttacked(attacker);
			}
		}
	}

	@Override
	public void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss)
	{
		if(target == null)
		{
			return;
		}

		if(target.isInvul())
		{
			sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED);
			broadcastPacket(new ExMagicAttackInfo(this, target, ExMagicAttackInfo.ATTACK_WAS_BLOCKED));
			return;
		}

		// Check if hit is missed
		if(miss)
		{
			if(target instanceof L2PcInstance)
			{
				target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_EVADED_C2_ATTACK).addPcName((L2PcInstance) target).addCharName(this));
			}
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_ATTACK_WENT_ASTRAY).addPcName(this));
			return;
		}

		// Check if hit is critical
		if(pcrit)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_HAD_CRITICAL_HIT).addPcName(this));
			if(target.isNpc() && getSkillLevel(467) > 0)
			{
				L2Skill skill = SkillTable.getInstance().getInfo(467, getSkillLevel(467));
				if(Rnd.getChance(skill.getCritChance()))
				{
					absorbSoul(skill, (L2Npc) target);
				}
			}
		}
		else if(mcrit)
		{
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
		}

		if(target instanceof L2DoorInstance || target instanceof L2ControlTowerInstance)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage));
		}
		else
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_GAVE_C2_DAMAGE_OF_S3).addPcName(this).addCharName(target).addNumber(damage).addShowWindowDamage(this, target.getObjectId(), damage));
		}
	}

	@Override
	public void sendChatMessage(int objectId, ChatType messageType, String charName, String text)
	{
		sendPacket(new Say2(objectId, messageType, charName, text));
	}

	/**
	 * @return {@code true} если текущий класс является пробудившимся
	 */
	@Override
	public boolean isAwakened()
	{
		return ClassId.values()[_activeClassId].getClassLevel() == ClassLevel.AWAKEN;
	}

	/**
	 * Send a Server->Client packet StatusUpdate to the L2PcInstance.<BR><BR>
	 */
	@Override
	public void sendPacket(L2GameServerPacket packet)
	{
		if(_client != null)
		{
			_client.sendPacket(packet);
		}
	}

	/**
	 * Send SystemMessage packet.<BR><BR>
	 *
	 * @param id SystemMessageId
	 */
	@Override
	public void sendPacket(SystemMessageId id)
	{
		sendPacket(SystemMessage.getSystemMessage(id));
	}

	/**
	 * Шлет ActionFail пакет клиенту
	 */
	@Override
	public void sendActionFailed()
	{
		sendPacket(ActionFail.STATIC_PACKET);
	}

	/**
	 * Set the _party object of the L2PcInstance (without joining it).
	 *
	 * @param party
	 */
	public void setParty(L2Party party)
	{
		_party = party;
	}

	/**
	 * @return True if the L2PcInstance is sitting.
	 */
	public boolean isSitting()
	{
		return _isSitting;
	}

	/**
	 * Set _isSitting to given value
	 *
	 * @param state
	 */
	public void setIsSitting(boolean state)
	{
		_isSitting = state;
	}

	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void sitDown()
	{
		sitDown(true);
	}

	public void sitDown(boolean checkCast)
	{
		if(checkCast && isCastingNow())
		{
			sendMessage("Cannot sit while casting");
			return;
		}

		if(!_isSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized())
		{
			breakAttack();
			_isSitting = true;
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			// Schedule a sit down task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(), 2500);
			setIsParalyzed(true);
		}
	}

	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)<BR><BR>
	 */
	public void standUp()
	{
		if(L2Event.active && _eventController.isForceSit())
		{
			sendMessage("A dark force beyond your mortal understanding makes your knees to shake when you try to stand up ...");
		}
		else if(_isSitting && !isInStoreMode() && !isAlikeDead())
		{
			if(_effects.isAffected(CharEffectList.EFFECT_FLAG_RELAXING))
			{
				stopEffects(L2EffectType.RELAXING);
			}

			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(), 2500);
		}
	}

	/**
	 * @return the PcWarehouse object of the L2PcInstance.
	 */
	public PcWarehouse getWarehouse()
	{
		if(_warehouse == null)
		{
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		if(Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().addCacheTask(this);
		}
		return _warehouse;
	}

	/**
	 * Free memory used by Warehouse
	 */
	public void clearWarehouse()
	{
		if(_warehouse != null)
		{
			_warehouse.deleteMe();
		}
		_warehouse = null;
	}

	/**
	 * @return the PcFreight object of the L2PcInstance.
	 */
	public PcFreight getFreight()
	{
		return _freight;
	}

	/**
	 * @return true if refund list is not empty
	 */
	public boolean hasRefund()
	{
		return _refund != null && _refund.getSize() > 0;
	}

	/**
	 * @return refund object or create new if not exist
	 */
	public PcRefund getRefund()
	{
		if(_refund == null)
		{
			_refund = new PcRefund(this);
		}
		return _refund;
	}

	/**
	 * Clear refund
	 */
	public void clearRefund()
	{
		if(_refund != null)
		{
			_refund.deleteMe();
		}
		_refund = null;
	}

	/**
	 * @return the Identifier of the L2PcInstance.
	 */
	@Deprecated
	public int getCharId()
	{
		return _charId;
	}

	/**
	 * Set the Identifier of the L2PcInstance.<BR><BR>
	 *
	 * @param charId
	 */
	public void setCharId(int charId)
	{
		_charId = charId;
	}

	/**
	 * @return the Adena amount of the L2PcInstance.
	 */
	public long getAdenaCount()
	{
		return _inventory.getAdenaCount();
	}

	/**
	 * @return the Ancient Adena amount of the L2PcInstance.
	 */
	public long getAncientAdena()
	{
		return _inventory.getAncientAdena();
	}

	/**
	 * Add adena to Inventory of the L2PcInstance and send a Server->Client
	 * InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param count       : int Quantity of adena to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 */
	public void addAdena(ProcessType process, long count, L2Object reference, boolean sendMessage)
	{
		if(sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addItemNumber(count));
		}

		if(count > 0)
		{
			_inventory.addAdena(process, count, this, reference);

			// Send update packet
			if(Config.FORCE_INVENTORY_UPDATE)
			{
				sendPacket(new ItemList(this, false));
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
			}
		}
	}

	/**
	 * Reduce adena in Inventory of the L2PcInstance and send a Server->Client
	 * InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param count       : long Quantity of adena to be reduced
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAdena(ProcessType process, long count, L2Object reference, boolean sendMessage)
	{
		if(count > getAdenaCount())
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			return false;
		}

		if(count > 0)
		{
			L2ItemInstance adenaItem = _inventory.getAdenaInstance();
			if(!_inventory.reduceAdena(process, count, this, reference))
			{
				return false;
			}

			// Send update packet
			if(Config.FORCE_INVENTORY_UPDATE)
			{
				sendPacket(new ItemList(this, false));
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
			}

			if(sendMessage)
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addItemNumber(count));
			}
		}
		return true;
	}

	/**
	 * Add ancient adena to Inventory of the L2PcInstance and send a
	 * Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param count       : int Quantity of ancient adena to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 */
	public void addAncientAdena(ProcessType process, long count, L2Object reference, boolean sendMessage)
	{
		if(sendMessage)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(PcInventory.ANCIENT_ADENA_ID);
			sm.addItemNumber(count);
			sendPacket(sm);
		}

		if(count > 0)
		{
			_inventory.addAncientAdena(process, count, this, reference);

			if(Config.FORCE_INVENTORY_UPDATE)
			{
				sendPacket(new ItemList(this, false));
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAncientAdenaInstance());
				sendPacket(iu);
			}
		}
	}

	/**
	 * Reduce ancient adena in Inventory of the L2PcInstance and send a
	 * Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param count       : long Quantity of ancient adena to be reduced
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean reduceAncientAdena(ProcessType process, long count, L2Object reference, boolean sendMessage)
	{
		if(count > getAncientAdena())
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}

			return false;
		}

		if(count > 0)
		{
			L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
			if(!_inventory.reduceAncientAdena(process, count, this, reference))
			{
				return false;
			}

			if(Config.FORCE_INVENTORY_UPDATE)
			{
				sendPacket(new ItemList(this, false));
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
			}

			if(sendMessage)
			{
				if(count > 1)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID).addItemNumber(count));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID));
				}
			}
		}

		return true;
	}

	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet
	 * to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param item        : L2ItemInstance to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 */
	public void addItem(ProcessType process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		if(item.getCount() > 0)
		{
			// Sends message to client if requested
			if(sendMessage)
			{
				if(item.getCount() > 1)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item);
					sm.addItemNumber(item.getCount());
					sendPacket(sm);
				}
				else if(item.getEnchantLevel() > 0)
				{
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addNumber(item.getEnchantLevel());
					sm.addItemName(item);
					sendPacket(sm);
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item));
				}
			}

			// Add the item to inventory
			L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);

			// Send inventory update packet
			if(Config.FORCE_INVENTORY_UPDATE)
			{
				sendPacket(new ItemList(this, false));
			}
			else
			{
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem );
				sendPacket(playerIU);
			}

			// Update current load as well
			sendPacket(new ExUserInfoInvenWeight(this));

			// If over capacity, drop the item
			if(!isGM() && !_inventory.validateCapacity(0, item.isQuestItem()) && newitem.isDropable() && (!newitem.isStackable() || newitem.getLastChange() != L2ItemInstance.MODIFIED))
			{
				dropItem(ProcessType.DROP, newitem, null, true, true);
			}

			// Cursed Weapon
			else if(CursedWeaponsManager.getInstance().isCursed(newitem.getItemId()))
			{
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}

			// Combat Flag
			else if(FortSiegeManager.getInstance().isCombatFlag(item.getItemId()))
			{
				if(FortSiegeManager.getInstance().activateCombatFlag(this, item))
				{
					Fort fort = FortManager.getInstance().getFort(this);
					fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
				}
			}
		}
	}

	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet
	 * to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param itemId      : int Item Identifier of the item to be added
	 * @param count       : long Quantity of items to be added
	 * @param reference   : L2Object Object referencing current action like NPC selling
	 *                    item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about
	 *                    this action
	 */
	public L2ItemInstance addItem(ProcessType process, int itemId, long count, L2Object reference, boolean sendMessage)
	{
		if(count > 0)
		{
			L2ItemInstance item = null;
			if(ItemTable.getInstance().getTemplate(itemId) != null)
			{
				item = ItemTable.getInstance().createDummyItem(itemId);
			}
			else
			{
				_log.log(Level.ERROR, "Item doesn't exist so cannot be added. Item ID: " + itemId);
				return null;
			}
			// Sends message to client if requested
			if(sendMessage && (!isCastingNow() && item.getItemType() == L2EtcItemType.HERB || item.getItemType() != L2EtcItemType.HERB))
			{
				if(count > 1)
				{
					if(process == ProcessType.SWEEP || process == ProcessType.PLUNDER || process == ProcessType.QUEST)
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addItemNumber(count);
						sendPacket(sm);
					}
					else
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addItemNumber(count);
						sendPacket(sm);
					}
				}
				else
				{
					if(process == ProcessType.SWEEP || process == ProcessType.PLUNDER || process == ProcessType.QUEST)
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
					}
					else
					{
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId));
					}
				}
			}
			// Auto use herbs - autoloot
			if(item.getItemType() == L2EtcItemType.HERB) //If item is herb dont add it to iv :]
			{
				if(isCastingNow())
				{
					_herbstask += 100;
					ThreadPoolManager.getInstance().scheduleAi(new HerbTask(process, itemId, count, reference, sendMessage), _herbstask);
				}
				else
				{
					L2ItemInstance herb = new L2ItemInstance(_charId, itemId);
					IItemHandler handler = ItemHandler.getInstance().getHandler(herb.getEtcItem());
					if(handler == null)
					{
						_log.log(Level.WARN, "No item handler registered for Herb - item ID " + herb.getItemId() + '.');
					}
					else
					{
						handler.useItem(this, herb, false);
						if(_herbstask >= 100)
						{
							_herbstask -= 100;
						}
					}
				}
			}
			else
			{
				// Add the item to inventory
				L2ItemInstance createdItem = _inventory.addItem(process, itemId, count, this, reference);
				// If over capacity, drop the item
				if(!isGM() && !_inventory.validateCapacity(0, item.isQuestItem()) && createdItem.isDropable() && (!createdItem.isStackable() || createdItem.getLastChange() != L2ItemInstance.MODIFIED))
				{
					dropItem(ProcessType.DROP, createdItem, null, true, false);
				}

				// Cursed Weapon
				else if(CursedWeaponsManager.getInstance().isCursed(createdItem.getItemId()))
				{
					CursedWeaponsManager.getInstance().activate(this, createdItem);
				}

				// Combat Flag
				else if(FortSiegeManager.getInstance().isCombatFlag(createdItem.getItemId()))
				{
					if(FortSiegeManager.getInstance().activateCombatFlag(this, item))
					{
						Fort fort = FortManager.getInstance().getFort(this);
						fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
					}
				}
				return createdItem;
			}
		}
		return null;
	}

	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param item        : L2ItemInstance to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(ProcessType process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return destroyItem(process, item, item.getCount(), reference, sendMessage);
	}

	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param item        : L2ItemInstance to be destroyed
	 * @param count
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItem(ProcessType process, L2ItemInstance item, long count, L2Object reference, boolean sendMessage)
	{
		item = _inventory.destroyItem(process, item, count, this, reference);

		if(item == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}

		// Send inventory update packet
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			sendPacket(new ItemList(this, false));
		}
		else
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}

		// Update current load as well
		sendPacket(new ExUserInfoInvenWeight(this));

		// Sends message to client if requested
		if(sendMessage)
		{
			if(count > 1)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item);
				sm.addItemNumber(count);
				sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item);
				sendPacket(sm);
			}
		}

		return true;
	}

	/**
	 * Destroys shots from inventory without logging and only occasional saving to database.
	 * Sends a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param objectId    : int Item Instance identifier of the item to be destroyed
	 * @param count       : int Quantity of items to be destroyed
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	public boolean destroyItemWithoutTrace(int objectId, long count, L2Object reference, boolean sendMessage)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);

		if(item == null || item.getCount() < count)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return false;
		}

		return destroyItem(null, item, count, reference, sendMessage);
	}

	public void destroyOneItemByItemIds(ProcessType process, long count, L2Object reference, boolean sendMessage, int... itemIds)
	{
		for(int itemId : itemIds)
		{
			L2ItemInstance item = _inventory.getItemByItemId(itemId);
			if(item != null)
			{
				if(destroyItemByItemId(process, itemId, count, reference, sendMessage))
				{
					return;
				}
			}
		}
	}

	/**
	 * This method validates slots and weight limit, for stackable and non-stackable items.
	 * @param process a generic string representing the process that is exchanging this items
	 * @param reference the (probably NPC) reference, could be null
	 * @param coinId the item Id of the item given on the exchange
	 * @param cost the amount of items given on the exchange
	 * @param rewardId the item received on the exchange
	 * @param count the amount of items received on the exchange
	 * @param sendMessage if {@code true} it will send messages to the acting player
	 * @return {@code true} if the player successfully exchanged the items, {@code false} otherwise
	 */
	public boolean exchangeItemsById(ProcessType process, L2Object reference, int coinId, long cost, int rewardId, long count, boolean sendMessage)
	{
		PcInventory inv = _inventory;
		if(!inv.validateCapacityByItemId(rewardId, count))
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.SLOTS_FULL);
			}
			return false;
		}

		if(!inv.validateWeightByItemId(rewardId, count))
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			}
			return false;
		}

		if(destroyItemByItemId(process, coinId, cost, reference, sendMessage))
		{
			addItem(process, rewardId, count, reference, sendMessage);
			return true;
		}
		return false;
	}

	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process   : String Identifier of process triggering this action
	 * @param objectId  : int Item Identifier of the item to be transfered
	 * @param count     : long Quantity of items to be transfered
	 * @param target
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(ProcessType process, int objectId, long count, Inventory target, L2Object reference)
	{
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, ProcessType.EXCHANGE);
		if(oldItem == null)
		{
			return null;
		}
		L2ItemInstance newItem = _inventory.transferItem(process, objectId, count, target, this, reference);
		if(newItem == null)
		{
			return null;
		}

		// Send inventory update packet
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			sendPacket(new ItemList(this, false));
		}
		else
		{
			InventoryUpdate playerIU = new InventoryUpdate();

			if(oldItem.getCount() > 0 && !oldItem.equals(newItem))
			{
				playerIU.addModifiedItem(oldItem);
			}
			else
			{
				playerIU.addRemovedItem(oldItem);
			}

			sendPacket(playerIU);
		}

		// Update current load as well
		sendPacket(new ExUserInfoInvenWeight(this));

		// Send target update packet
		if(target instanceof PcInventory)
		{
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();

			if(Config.FORCE_INVENTORY_UPDATE)
			{
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			else
			{
				InventoryUpdate playerIU = new InventoryUpdate();

				if(newItem.getCount() > count)
				{
					playerIU.addModifiedItem(newItem);
				}
				else
				{
					playerIU.addNewItem(newItem);
				}
				targetPlayer.sendPacket(playerIU);
			}

			// Update current load as well
			targetPlayer.sendPacket(new ExUserInfoInvenWeight(targetPlayer));
		}
		else if(target instanceof PetInventory)
		{
			PetInventoryUpdate petIU = new PetInventoryUpdate();

			if(newItem.getCount() > count)
			{
				petIU.addModifiedItem(newItem);
			}
			else
			{
				petIU.addNewItem(newItem);
			}

			((PetInventory) target).getOwner().sendPacket(petIU);
		}
		return newItem;
	}

	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     String Identifier of process triggering this action
	 * @param item        L2ItemInstance to be dropped
	 * @param reference   L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @param protectItem whether or not dropped item must be protected temporary against other players
	 * @return boolean informing if the action was successfull
	 */
	public boolean dropItem(ProcessType process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean protectItem)
	{
		item = _inventory.dropItem(process, item, this, reference);

		if(item == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return false;
		}

		item.dropMe(this, getX() + Rnd.get(50) - 25, getY() + Rnd.get(50) - 25, getZ() + 20);

		if(Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if(item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
			{
				ItemsOnGroundAutoDestroyManager.getInstance().addItem(item);
			}
		}

		// protection against auto destroy dropped item
		if(Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if(!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}

		// retail drop protection
		if(protectItem)
		{
			item.getDropProtection().protect(this);
		}

		// Send inventory update packet
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			sendPacket(new ItemList(this, false));
		}
		else
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		}

		// Update current load as well
		sendPacket(new ExUserInfoInvenWeight(this));

		// Sends message to client if requested
		if(sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
		}

		return true;
	}

	public boolean dropItem(ProcessType process, L2ItemInstance item, L2Object reference, boolean sendMessage)
	{
		return dropItem(process, item, reference, sendMessage, false);
	}

	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 *
	 * @param process     : String Identifier of process triggering this action
	 * @param objectId    : int Item Instance identifier of the item to be dropped
	 * @param count       : long Quantity of items to be dropped
	 * @param x           : int coordinate for drop X
	 * @param y           : int coordinate for drop Y
	 * @param z           : int coordinate for drop Z
	 * @param reference   : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param protectItem
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(ProcessType process, int objectId, long count, int x, int y, int z, L2Object reference, boolean sendMessage, boolean protectItem)
	{
		L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
		L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);

		if(item == null)
		{
			if(sendMessage)
			{
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}

			return null;
		}

		item.dropMe(this, x, y, z);

		if(Config.AUTODESTROY_ITEM_AFTER > 0 && Config.DESTROY_DROPPED_PLAYER_ITEM && !Config.LIST_PROTECTED_ITEMS.contains(item.getItemId()))
		{
			if(item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM || !item.isEquipable())
			{
				ItemsOnGroundAutoDestroyManager.getInstance().addItem(item);
			}
		}
		if(Config.DESTROY_DROPPED_PLAYER_ITEM)
		{
			if(!item.isEquipable() || item.isEquipable() && Config.DESTROY_EQUIPABLE_PLAYER_ITEM)
			{
				item.setProtected(false);
			}
			else
			{
				item.setProtected(true);
			}
		}
		else
		{
			item.setProtected(true);
		}

		// retail drop protection
		if(protectItem)
		{
			item.getDropProtection().protect(this);
		}

		// Send inventory update packet
		if(Config.FORCE_INVENTORY_UPDATE)
		{
			sendPacket(new ItemList(this, false));
		}
		else
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
		}

		// Update current load as well
		sendPacket(new ExUserInfoInvenWeight(this));

		// Sends message to client if requested
		if(sendMessage)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item));
		}

		return item;
	}

	public L2ItemInstance checkItemManipulation(int objectId, long count, ProcessType action)
	{
		// TODO: if we remove objects that are not visisble from the L2World,
		// we'll have to remove this check
		if(WorldManager.getInstance().findObject(objectId) == null)
		{
			_log.log(Level.INFO, getObjectId() + ": player tried to " + action + " item not available in L2World");
			return null;
		}

		L2ItemInstance item = _inventory.getItemByObjectId(objectId);

		if(item == null || item.getOwnerId() != getObjectId())
		{
			_log.log(Level.INFO, getObjectId() + ": player tried to " + action + " item he is not owner of");
			return null;
		}

		if(count < 0 || count > 1 && !item.isStackable())
		{
			_log.log(Level.INFO, getObjectId() + ": player tried to " + action + " item with invalid count: " + count);
			return null;
		}

		if(count > item.getCount())
		{
			_log.log(Level.INFO, getObjectId() + ": player tried to " + action + " more items than he owns");
			return null;
		}

		// Если игрок на страйдере, рожок с которого он вызывается выпасть не может
		if(_mountObjectID == objectId)
		{
			return null;
		}

		// Вещь, с которой в текущее время был присумонен питомец выпасть не может
		if(!_summons.isEmpty())
		{
			boolean isPetControlItem = false;
			for(L2Summon pet : _summons)
			{
				if(pet.getControlObjectId() == objectId)
				{
					isPetControlItem = true;
				}
			}
			if(isPetControlItem)
			{
				return null;
			}
		}

		if(_activeEnchantItem != null && _activeEnchantItem.getObjectId() == objectId)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}

			return null;
		}

		// We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
		if(item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow()))
		{
			return null;
		}

		return item;
	}

	/**
	 * Set _spawnProtectEndTime according settings.
	 *
	 * @param protect
	 */
	public void setSpawnProtection(boolean protect)
	{
		_spawnProtectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + Config.PLAYER_SPAWN_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public void setTeleportProtection(boolean protect)
	{
		_teleportProtectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + Config.PLAYER_TELEPORT_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public boolean isSpawnProtected()
	{
		return _spawnProtectEndTime > GameTimeController.getInstance().getGameTicks();
	}

	public boolean isTeleportProtected()
	{
		return _teleportProtectEndTime > GameTimeController.getInstance().getGameTicks();
	}

	public boolean isRecentFakeDeath()
	{
		return _recentFakeDeathEndTime > GameTimeController.getInstance().getGameTicks();
	}

	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 *
	 * @param protect
	 */
	public void setRecentFakeDeath(boolean protect)
	{
		_recentFakeDeathEndTime = protect ? GameTimeController.getInstance().getGameTicks() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * GameTimeController.TICKS_PER_SECOND : 0;
	}

	public boolean isFakeDeath()
	{
		return _isFakeDeath;
	}

	public void setIsFakeDeath(boolean value)
	{
		_isFakeDeath = value;
	}

	/**
	 * @return клиент текущего персонажа
	 */
	public L2GameClient getClient()
	{
		return _client;
	}

	public void setClient(L2GameClient client)
	{
		_client = client;
	}

	/**
	 * Закрытие активного соединения с клиентом
	 *
	 * @param closeClient закрывать клиент?
	 */
	public void closeNetConnection(boolean closeClient)
	{
		L2GameClient client = _client;
		if(client != null)
		{
			if(client.isDetached())
			{
				client.cleanMe(true);
			}
			else
			{
				if(!client.getConnection().isClosed())
				{
					if(closeClient)
					{
						client.close(LogOutOk.STATIC_PACKET);
					}
					else
					{
						client.close(SeverClose.STATIC_PACKET);
					}
				}
			}
		}
	}

	public Point3D getCurrentSkillWorldPosition()
	{
		return _currentSkillWorldPosition;
	}

	public void setCurrentSkillWorldPosition(Point3D worldPosition)
	{
		_currentSkillWorldPosition = worldPosition;
	}

	/**
	 * @param barPixels кол-во пикселей в панельке CP
	 * @return {@code true} если обновление кол-ва CP уже было выполнено
	 */
	private boolean needCpUpdate(int barPixels)
	{
		double currentCp = getCurrentCp();

		if(currentCp <= 1.0 || getMaxCp() < barPixels)
		{
			return true;
		}

		if(currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck)
		{
			if(currentCp == getMaxCp())
			{
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * @param barPixels кол-во пикселей в панельке MP
	 * @return {@code true} если обновление кол-ва MP уже было выполнено
	 */
	private boolean needMpUpdate(int barPixels)
	{
		double currentMp = getCurrentMp();

		if(currentMp <= 1.0 || getMaxMp() < barPixels)
		{
			return true;
		}

		if(currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck)
		{
			if(currentMp == getMaxMp())
			{
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			}
			else
			{
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;

				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}

			return true;
		}

		return false;
	}

	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet.
	 * Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR><BR>
	 */
	public void broadcastUserInfo()
	{
        sendPacket(new UI(this));
        broadcastPacket(new CI(this));
	}

    public final void broadcastUserInfo(UserInfoType... types)
    {
        UI ui = new UI(this, false);
        ui.addComponentType(types);
        sendPacket(ui);

        broadcastPacket(new CI(this));
    }

	public void broadcastClanMemberInfo()
	{
		_clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
	}

	public void broadcastTitleInfo()
	{
        UI ui = new UI(this, false);
        ui.addComponentType(UserInfoType.CLAN);
        sendPacket(ui);

        broadcastPacket(new NickNameChanged(this));
	}

	/**
	 * @return the Alliance Identifier of the L2PcInstance.
	 */
	public int getAllyId()
	{
		if(_clan == null)
		{
			return 0;
		}
		return _clan.getAllyId();
	}

	public int getAllyCrestId()
	{
		if(_clanId == 0)
		{
			return 0;
		}
		if(_clan.getAllyId() == 0)
		{
			return 0;
		}
		return _clan.getAllyCrestId();
	}

	public void queryGameGuard()
	{
		if(_client != null)
		{
			_client.setGameGuardOk(false);
			sendPacket(new GameGuardQuery());
		}
		if(Config.GAMEGUARD_ENFORCE)
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheck(), 30 * 1000);
		}
	}

	public void sendUserInfo()
	{
		sendPacket(new UI(this));
	}

	/**
	 * Manage Interact Task with another L2PcInstance.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>If the private store is a PlayerPrivateStoreType.SELL, send a Server->Client
	 * PrivateBuyListSell packet to the L2PcInstance</li> <li>If the private
	 * store is a PlayerPrivateStoreType.BUY, send a Server->Client PrivateBuyListBuy
	 * packet to the L2PcInstance</li> <li>If the private store is a
	 * PlayerPrivateStoreType.MANUFACTURE, send a Server->Client RecipeShopSellList
	 * packet to the L2PcInstance</li><BR>
	 * <BR>
	 *
	 * @param target The L2Character targeted
	 */
	public void doInteract(L2Character target)
	{
		if(target instanceof L2PcInstance)
		{
			L2PcInstance temp = (L2PcInstance) target;
			sendActionFailed();

			if(temp._privateStoreType == PlayerPrivateStoreType.SELL || temp._privateStoreType == PlayerPrivateStoreType.SELL_PACKAGE)
			{
				sendPacket(new PrivateStoreList(this, temp));
			}
			else if(temp._privateStoreType == PlayerPrivateStoreType.BUY)
			{
				sendPacket(new PrivateStoreBuyList(this, temp));
			}
			else if(temp._privateStoreType == PlayerPrivateStoreType.MANUFACTURE)
			{
				sendPacket(new RecipeShopSellList(this, temp));
			}
		}
		else
		{
			// _interactTarget=null should never happen but one never knows ^^;
			if(target != null)
			{
				target.onAction(this);
			}
		}
	}

	/**
	 * Manage AutoLoot Task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param target The L2ItemInstance dropped
	 * @param item
	 */
	public void doAutoLoot(L2Attackable target, ItemHolder item)
	{
		if(isInParty() && ItemTable.getInstance().getTemplate(item.getId()).getItemType() != L2EtcItemType.HERB)
		{
			_party.distributeItem(this, item, false, target);
		}
		else if(item.getId() == PcInventory.ADENA_ID)
		{
			addAdena(ProcessType.LOOT, item.getCount(), target, true);
		}
		else
		{
			addItem(ProcessType.LOOT, item.getId(), item.getCount(), target, true);
		}
	}

	/**
	 * Manage Pickup Task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance </li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets </li>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li><BR><BR>
	 * <p/>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT><BR><BR>
	 *
	 * @param object The L2ItemInstance to pick up
	 */
	protected void doPickupItem(L2Object object)
	{
		if(isAlikeDead() || _isFakeDeath)
		{
			return;
		}

		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		// Check if the L2Object to pick up is a L2ItemInstance
		if(!(object instanceof L2ItemInstance))
		{
			// dont try to pickup anything that is not an item :)
			_log.log(Level.WARN, this + " trying to pickup wrong target." + getTarget());
			return;
		}

		L2ItemInstance target = (L2ItemInstance) object;

		// Send a Server->Client packet ActionFail to this L2PcInstance
		sendActionFailed();

		// Send a Server->Client packet StopMove to this L2PcInstance
		sendPacket(new StopMove(this));
		SystemMessage smsg = null;
		synchronized(target)
		{
			// Check if the target to pick up is visible
			if(!target.isVisible())
			{
				// Send a Server->Client packet ActionFail to this
				// L2PcInstance
				sendActionFailed();
				return;
			}

			if(!HookManager.getInstance().checkEvent(HookType.ON_ITEM_PICKUP, null, false, this, target))
			{
				sendActionFailed();
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}

			if(!target.getDropProtection().tryPickUp(this))
			{
				sendActionFailed();
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}

			if((isInParty() && _party.getLootDistribution() == PartyLootType.ITEM_LOOTER || !isInParty()) && !_inventory.validateCapacity(target))
			{
				sendActionFailed();
				sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}

			if(isInvul() && !isGM())
			{
				sendActionFailed();
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}

			if(target.getOwnerId() != 0 && target.getOwnerId() != getObjectId() && !isInLooterParty(target.getOwnerId()))
			{
				sendActionFailed();

				if(target.getItemId() == PcInventory.ADENA_ID)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addItemNumber(target.getCount());
					sendPacket(smsg);
				}
				else if(target.getCount() > 1)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target);
					smsg.addItemNumber(target.getCount());
					sendPacket(smsg);
				}
				else
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target);
					sendPacket(smsg);
				}
				sendActionFailed();
				sendPacket(smsg);
				return;
			}

			// You can pickup only 1 combat flag
			if(FortSiegeManager.getInstance().isCombatFlag(target.getItemId()))
			{
				if(!FortSiegeManager.getInstance().checkIfCanPickup(this))
				{
					return;
				}
			}

			if(target.getItemLootShedule() != null && (target.getOwnerId() == getObjectId() || isInLooterParty(target.getOwnerId())))
			{
				target.resetOwnerTimer();
			}

			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
			if(Config.SAVE_DROPPED_ITEM) // item must be removed from ItemsOnGroundManager if is active
			{
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}

		// Auto use herbs - pick up
		if(target.getItemType() == L2EtcItemType.HERB)
		{
			IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
			if(handler == null)
			{
				_log.log(Level.INFO, "No item handler registered for item ID " + target.getItemId() + '.');
			}
			else
			{
				handler.useItem(this, target, false);
			}
			ItemTable.getInstance().destroyItem(ProcessType.CONSUME, target, this, null);
		}
		// Cursed Weapons are not distributed
		else if(CursedWeaponsManager.getInstance().isCursed(target.getItemId()) || FortSiegeManager.getInstance().isCombatFlag(target.getItemId()))
		{
			addItem(ProcessType.PICKUP, target, null, true);
		}
		else
		{
			// if item is instance of L2ArmorType or L2WeaponType broadcast an "Attention" system message
			if(target.getItemType() instanceof L2ArmorType || target.getItemType() instanceof L2WeaponType)
			{
				if(target.getEnchantLevel() > 0)
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
					smsg.addPcName(this);
					smsg.addNumber(target.getEnchantLevel());
					smsg.addItemName(target.getItemId());
					broadcastPacket(smsg, 1400);
				}
				else
				{
					smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
					smsg.addPcName(this);
					smsg.addItemName(target.getItemId());
					broadcastPacket(smsg, 1400);
				}
			}

			// Check if a Party is in progress
			if(isInParty())
			{
				_party.distributeItem(this, target);
			}
			// Target is adena
			else if(target.getItemId() == PcInventory.ADENA_ID && _inventory.getAdenaInstance() != null)
			{
				addAdena(ProcessType.PICKUP, target.getCount(), null, true);
				ItemTable.getInstance().destroyItem(ProcessType.PICKUP, target, this, null);
			}
			else
			{
				addItem(ProcessType.PICKUP, target, null, true);
				//Auto-Equip arrows/bolts if player has a bow/crossbow and player picks up arrows/bolts.
				L2EtcItem etcItem = target.getEtcItem();
				if(etcItem != null)
				{
					L2EtcItemType itemType = etcItem.getItemType();
					if(itemType == L2EtcItemType.ARROW)
					{
						checkAndEquipArrows();
					}
					else if(itemType == L2EtcItemType.BOLT)
					{
						checkAndEquipBolts();
					}
				}
			}
		}
	}

	public boolean canOpenPrivateStore()
	{
		if(ChaosFestival.getInstance().isRegistered(this) || ChaosFestival.getInstance().isFightingNow(this))
		{
			sendPacket(SystemMessage.getSystemMessage(3742));
			return false;
		}
		return !isAlikeDead() && !_olympiadController.isParticipating() && !isMounted() && !isInsideZone(ZONE_NOSTORE) && !isCastingNow();
	}

	public void tryOpenPrivateBuyStore()
	{
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if(canOpenPrivateStore())
		{
			if(_privateStoreType == PlayerPrivateStoreType.BUY || _privateStoreType == PlayerPrivateStoreType.BUY_MANAGE)
			{
				setPrivateStoreType(PlayerPrivateStoreType.NONE);
			}
			if(_privateStoreType == PlayerPrivateStoreType.NONE)
			{
				if(_isSitting)
				{
					standUp();
				}
				setPrivateStoreType(PlayerPrivateStoreType.BUY_MANAGE);
				sendPacket(new PrivateStoreBuyManageList(this));
			}
		}
		else
		{
			if(isInsideZone(ZONE_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendActionFailed();
		}
	}

	public void tryOpenPrivateSellStore(boolean isPackageSale)
	{
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if(canOpenPrivateStore())
		{
			if(_privateStoreType == PlayerPrivateStoreType.SELL || _privateStoreType == PlayerPrivateStoreType.SELL_MANAGE || _privateStoreType == PlayerPrivateStoreType.SELL_PACKAGE)
			{
				setPrivateStoreType(PlayerPrivateStoreType.NONE);
			}

			if(_privateStoreType == PlayerPrivateStoreType.NONE)
			{
				if(_isSitting)
				{
					standUp();
				}
				setPrivateStoreType(PlayerPrivateStoreType.SELL_MANAGE);
				sendPacket(new PrivateStoreManageList(this, isPackageSale));
			}
		}
		else
		{
			if(isInsideZone(ZONE_NOSTORE))
			{
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendActionFailed();
		}
	}

	public PreparedListContainer getMultiSell()
	{
		return _currentMultiSell;
	}

	public void setMultiSell(PreparedListContainer list)
	{
		_currentMultiSell = list;
	}

	public boolean isInStance()
	{
		return _transformation != null && _transformation.isStance();
	}

	public void transform(L2Transformation transformation)
	{
		if(_transformation != null)
		{
			// You already polymorphed and cannot polymorph again.
			sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return;
		}
		setQueuedSkill(null, false, false);
		if(isMounted())
		{
			// Get off the strider or something else if character is mounted
			dismount();
		}

		_transformation = transformation;
		stopAllToggles();
		transformation.onTransform();
		sendSkillList();
		sendPacket(new SkillCoolTime(this));
        sendPacket(new ExUserInfoAbnormalVisualEffect(this));
		broadcastUserInfo();
	}

	public L2Transformation getTransformation()
	{
		return _transformation;
	}

	/**
	 * This returns the transformation Id of the current transformation.
	 * For example, if a player is transformed as a Buffalo, and then picks up the Zariche,
	 * the transform Id returned will be that of the Zariche, and NOT the Buffalo.
	 *
	 * @return Transformation Id
	 */
	public int getTransformationId()
	{
		return _transformation == null ? 0 : _transformation.getId();
	}

	/**
	 * This is a simple query that inserts the transform Id into the character table for future reference.
	 */
	public void transformInsertInfo()
	{
		int transformationId = getTransformationId();

		if(transformationId == L2Transformation.TRANSFORM_AKAMANAH || transformationId == L2Transformation.TRANSFORM_ZARICHE)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHAR_TRANSFORM);
			statement.setInt(1, transformationId);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Transformation insert info: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public L2ItemInstance getChestArmorInstance()
	{
		return _inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}

	public L2ItemInstance getLegsArmorInstance()
	{
		return _inventory.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}

	public L2Armor getActiveChestArmorItem()
	{
		L2ItemInstance armor = getChestArmorInstance();

		if(armor == null)
		{
			return null;
		}

		return (L2Armor) armor.getItem();
	}

	public L2Armor getActiveLegsArmorItem()
	{
		L2ItemInstance legs = getLegsArmorInstance();

		if(legs == null)
		{
			return null;
		}

		return (L2Armor) legs.getItem();
	}

	public boolean isWearingHeavyArmor()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();

		if(armor != null && legs != null)
		{
			if(legs.getItemType() == L2ArmorType.HEAVY && armor.getItemType() == L2ArmorType.HEAVY)
			{
				return true;
			}
		}
		if(armor != null)
		{
			if(_inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && armor.getItemType() == L2ArmorType.HEAVY)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isWearingLightArmor()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();

		if(armor != null && legs != null)
		{
			if(legs.getItemType() == L2ArmorType.LIGHT && armor.getItemType() == L2ArmorType.LIGHT)
			{
				return true;
			}
		}
		if(armor != null)
		{
			if(_inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && armor.getItemType() == L2ArmorType.LIGHT)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isWearingMagicArmor()
	{
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();

		if(armor != null && legs != null)
		{
			if(legs.getItemType() == L2ArmorType.MAGIC && armor.getItemType() == L2ArmorType.MAGIC)
			{
				return true;
			}
		}
		if(armor != null)
		{
			if(_inventory.getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR && armor.getItemType() == L2ArmorType.MAGIC)
			{
				return true;
			}
		}
		return false;
	}

	public boolean isMarried()
	{
		return _married;
	}

	public void setMarried(boolean state)
	{
		_married = state;
	}

	public boolean isEngageRequest()
	{
		return _engagerequest;
	}

	public void setEngageRequest(boolean state, int playerid)
	{
		_engagerequest = state;
		_engageid = playerid;
	}

	public boolean isMarryRequest()
	{
		return _marryrequest;
	}

	public void setMarryRequest(boolean state)
	{
		_marryrequest = state;
	}

	public boolean isMarryAccepted()
	{
		return _marryaccepted;
	}

	public void setMarryAccepted(boolean state)
	{
		_marryaccepted = state;
	}

	public int getEngageId()
	{
		return _engageid;
	}

	public int getPartnerId()
	{
		return _partnerId;
	}

	public void setPartnerId(int partnerid)
	{
		_partnerId = partnerid;
	}

	public int getCoupleId()
	{
		return _coupleId;
	}

	public void setCoupleId(int coupleId)
	{
		_coupleId = coupleId;
	}

	public void engageAnswer(int answer)
	{
		if(_engagerequest && _engageid != 0)
		{
			L2PcInstance playerToEngage = WorldManager.getInstance().getPlayer(_engageid);
			setEngageRequest(false, 0);
			if(playerToEngage != null)
			{
				if(answer == 1)
				{
					WeddingManager.getInstance().createCouple(playerToEngage, this);
					playerToEngage.sendMessage("Запрос на свадьбу >ПОДТВЕРЖДЕН<");
				}
				else
				{
					playerToEngage.sendMessage("Запрос на свадьбу >ОТМЕНЕН<");
				}
			}
		}
	}

	private void onDieDropItem(L2Character killer)
	{
		if(_eventController.isParticipant() || killer == null)
		{
			return;
		}

		L2PcInstance pk = killer.getActingPlayer();
		if(!hasBadReputation() && pk != null && pk._clan != null && _clan != null && pk._clan.isAtWarWith(_clanId))
		{
			return;
		}

		if((!isInsideZone(ZONE_PVP) || pk == null) && (!isGM() || Config.REPUTATION_DROP_GM))
		{
			boolean isBadReputationDrop = false;
			boolean isKillerNpc = killer instanceof L2Npc;
			int pkLimit = Config.REPUTATION_DROPITEM_VALUE;

			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;

			if(hasBadReputation() && _pkKills >= pkLimit)
			{
				isBadReputationDrop = true;
				dropPercent = Config.BADREPUTATION_RATE_DROP;
				dropEquip = Config.BADREPUTATION_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.BADREPUTATION_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.BADREPUTATION_RATE_DROP_ITEM;
				dropLimit = Config.BADREPUTATION_DROP_LIMIT;
			}
			else if(isKillerNpc && getLevel() > 4)
			{
				dropPercent = Config.PLAYER_RATE_DROP;
				dropEquip = Config.PLAYER_RATE_DROP_EQUIP;
				dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON;
				dropItem = Config.PLAYER_RATE_DROP_ITEM;
				dropLimit = Config.PLAYER_DROP_LIMIT;
			}

			if(dropPercent > 0 && Rnd.getChance(dropPercent))
			{
				int dropCount = 0;

				int itemDropPercent = 0;

				for(L2ItemInstance itemDrop : _inventory.getItems())
				{
					// Не выпадающие вещи
					if(itemDrop.isShadowItem() ||                                    // Не дропаем шедоу вещи
						itemDrop.isTimeLimitedItem() ||                            // Не дропаем временные итемы
						!itemDrop.isDropable() ||
						itemDrop.getItemId() == PcInventory.ADENA_ID ||            // Не дропаем адену
						itemDrop.getItem().getType2() == L2Item.TYPE2_QUEST ||  // Не дропаем квстовые предметы
						Arrays.binarySearch(Config.REPUTATION_LIST_NONDROPPABLE_ITEMS, itemDrop.getItemId()) >= 0 ||  // Итемы описанные в онфиге для персонажей
						Arrays.binarySearch(Config.REPUTATION_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.getItemId()) >= 0 // Итемы описанные в онфиге для питомцев
						)
					{
						continue;
					}

					// Если вещь предназначена для вызова питомце и питомец в данный момент призван
					if(!_summons.isEmpty())
					{
						boolean isPetControlItem = false;
						for(L2Summon pet : _summons)
						{
							if(pet.getControlObjectId() == itemDrop.getObjectId())
							{
								isPetControlItem = true;
							}
						}
						if(isPetControlItem)
						{
							continue;
						}
					}

					// Хаотические персонажи со счетчиком PK 31 и более получают штраф,
					// при смерти могут потерять предметы из инвентаря, при этом улучшенное
					// оружие будет выпадать без улучшения.
					if(itemDrop.isAugmented())
					{
						itemDrop.removeAugmentation();
					}

					if(itemDrop.isEquipped())
					{
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == L2Item.TYPE2_WEAPON ? dropEquipWeapon : dropEquip;
						_inventory.unEquipItemInSlot(itemDrop.getLocationSlot());
					}
					else
					{
						itemDropPercent = dropItem; // Item in inventory
					}

					// NOTE: Each time an item is dropped, the chance of another
					// item being dropped gets lesser (dropCount * 2)
					if(Rnd.getChance(itemDropPercent))
					{
						dropItem(ProcessType.DROP, itemDrop, killer, true);
						if(++dropCount >= dropLimit)
						{
							break;
						}
					}
				}
			}
		}
	}

	public void onKillUpdatePvPReputation(L2Character target)
	{
		if(target == null)
		{
			return;
		}
		if(!(target instanceof L2Playable))
		{
			return;
		}

		L2PcInstance targetPlayer = target.getActingPlayer();

		if(targetPlayer == null)
		{
			return;
		}
		if(targetPlayer.equals(this))
		{
			return;
		}

		if(isCursedWeaponEquipped())
		{
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			return;
		}

		// If in duel and you kill (only can kill l2summon), do nothing
		if(_isInDuel && targetPlayer._isInDuel)
		{
			return;
		}

		// If in Arena, do nothing
		if(isInsideZone(ZONE_PVP) || targetPlayer.isInsideZone(ZONE_PVP))
		{
			return;
		}

		// Check if it's pvp
		if(checkIfPvP(target) && targetPlayer._pvpFlagController.isFlagged() // Can pvp and Target player has pvp flag set
			|| // or
			isInsideZone(ZONE_PVP) && targetPlayer.isInsideZone(ZONE_PVP)) // Player is inside pvp zone and Target player is inside pvp zone

		{
			increasePvpKills(target);
		}
		else
		// Target player doesn't have pvp flag set
		{
			// check about wars
			if(targetPlayer._clan != null && _clan != null && _clan.isAtWarWith(targetPlayer._clanId) && targetPlayer._clan.isAtWarWith(_clanId) && targetPlayer._pledgeType != L2Clan.SUBUNIT_ACADEMY && _pledgeType != L2Clan.SUBUNIT_ACADEMY)
			{
				// 'Both way war' -> 'PvP Kill'
				increasePvpKills(target);
				return;
			}

			// Если 'Нет войны' или 'Односторонняя война'

			// Если у цели плохая репутация и его еще никто не убивал
			if(targetPlayer.hasBadReputation())
			{
				if(!targetPlayer.variablesController.get("pkKilled", Boolean.class, false) && _pkKills < 31)
				{
					// Чтобы получить репутации выше 0, необходимо убить
					// хаотического персонажа – начисляется 360 очков репутации
					// TODO: Уточнить то ли сообщение ($s1, вы пополнили собственную репутацию)
					int newCount = _reputation + 360;
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_FAME).addNumber(newCount));
					setReputation(newCount);
					targetPlayer.variablesController.set("pkKilled", true);
				}
			}
			else if(targetPlayer._pvpFlagController.getState() == PvPFlagController.FlagState.NO_FLAG && Rnd.getChance(calcStat(Stats.PK_PENALTY_CHANCE, 100, null, null))) // Если цель была нефлагнутая
			{
				increasePkKillsAndReputation(targetPlayer);
				// Снимаем вещи путешественника
				checkItemRestriction();
			}
		}
	}

	/**
	 * Увеличивает счетчик PVP и броадкастит иноформацию о игроке
	 *
	 * @param target убитый игрок
	 */
	public void increasePvpKills(L2Character target)
	{
		if(target instanceof L2PcInstance)
		{
			_pvpKills += 1;

            UI ui = new UI(this, false);
            ui.addComponentType(UserInfoType.SOCIAL);
            sendPacket(ui);

			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				((L2PcInstance) target).updateWorldStatistic(CategoryType.KILLED_IN_PVP_COUNT, null, 1);
				updateWorldStatistic(CategoryType.PVP_COUNT, null, 1);
			}
		}
	}

	/**
	 * Увеличивает счетчик ПК и уменьшает
	 * количество репутации у игрока.
	 *
	 * @param target персонаж, который был убит в ПК
	 */
	public void increasePkKillsAndReputation(L2PcInstance target)
	{
		// При убийстве члена клана тьмы пк и карма не начисляется, начисляется ПВП
		if(target._clan != null && target._clan.getCastleId() > 0)
		{
			if(CastleManager.getInstance().getCastleById(target._clan.getCastleId()).getCastleSide() == CastleSide.DARK)
			{
				increasePvpKills(target);
				return;
			}
		}

		// Дефоултное значение теряемой репутации
		int baseReputation = Config.REPUTATION_LOST_DEFAULT_VALUE;

		// Удаляемая репутация в конце расчетов
		int deleteReputation;

		// Уровни персонажей
		int pkLevel = getLevel();
		int targetLevel = target.getLevel();

		int pkPKCount = _pkKills;

		// Модификаторы
		int lvlDiffMulti;
		int pkCountMulti;

		// Проверяем, есть-ли уже ПК у убийцы и выставляем множитель
		pkCountMulti = pkPKCount > 0 ? pkPKCount / 2 : 1;

		if(pkCountMulti < 1)
		{
			pkCountMulti = 1;
		}

		// Считаем множитель, основанный на разнице в уровнях жертвы и убийцы
		lvlDiffMulti = pkLevel > targetLevel ? pkLevel / targetLevel : 1;

		if(lvlDiffMulti < 1)
		{
			lvlDiffMulti = 1;
		}

		// Вычисляем конечную репутацию и ее лимиты для персонажа по формуле:
		deleteReputation = baseReputation * pkCountMulti * lvlDiffMulti;

		// Снимаем репутацию персонажу и увеличиваем счетчик ПК
		// При PK 30 и ниже репутация становится 0 и не может подняться выше.
		if(_pkKills > 0 && _pkKills <= 30 && _reputation > 0)
		{
			setReputation(0);
		}

		setReputation(_reputation - deleteReputation);

		_pkKills += 1;

		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			updateWorldStatistic(CategoryType.PK_COUNT, null, 1);
			target.updateWorldStatistic(CategoryType.KILLED_IN_PVP_COUNT, null, 1);
		}

		// Шлем обновленную инфу игроку-убийце
		sendUserInfo();
	}

	/**
	 * Метод восстановления репутации за опыт.
	 * Применимо только к хаотическим персонажам,
	 * не может подняться выше 0.
	 *
	 * @param exp количество полученного опыта
	 * @return количество восстанавливамой кармы
	 */
	public int calculateReputationRestore(long exp)
	{
		long expGained = Math.abs(exp);
		expGained /= Config.REPUTATION_XP_DIVIDER;

		int reputationRestore = 0;

		reputationRestore = expGained > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) expGained;

		if(_reputation + reputationRestore > 0)
		{
			reputationRestore = -_reputation;
		}

		return reputationRestore;
	}

	/**
	 * TODO: Unhardcode by implementing Lucky effect (Support for effects on passive skills required).
	 *
	 * @return {@code true} if player has Lucky skill and is level 9 or less.
	 */
	public boolean isLucky()
	{
		return getLevel() <= 9 && getKnownSkill(L2Skill.SKILL_LUCKY) != null;
	}

	/**
	 * Восстанавливает заданный процент опыта после смерти персонажа
	 *
	 * @param restorePercent процент восстановления опытта
	 */
	public void restoreExp(double restorePercent)
	{
		if(_expBeforeDeath > 0)
		{
			getStat().addExp(Math.round((_expBeforeDeath - getExp()) * restorePercent / 100));
			_expBeforeDeath = 0;
		}
	}

	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Calculate the Experience loss </li>
	 * <li>Set the value of _expBeforeDeath </li>
	 * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary </li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience </li><BR><BR>
	 *
	 * @param atwar
	 * @param killed_by_pc
	 * @param killed_by_siege_npc
	 */
	public void deathPenalty(boolean atwar, boolean killed_by_pc, boolean killed_by_siege_npc)
	{
		// TODO Need Correct Penalty
		// Get the level of the L2PcInstance
		int lvl = getLevel();

		int clan_luck = getSkillLevel(L2Skill.SKILL_CLAN_LUCK);

		double clan_luck_modificator = 1.0;
		double academy_diadema_modificator = 1.0;

		if(killed_by_pc)
		{
			switch(clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.5;
					break;
				case 2:
					clan_luck_modificator = 0.5;
					break;
				case 1:
					clan_luck_modificator = 0.5;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}
		else
		{
			switch(clan_luck)
			{
				case 3:
					clan_luck_modificator = 0.8;
					break;
				case 2:
					clan_luck_modificator = 0.8;
					break;
				case 1:
					clan_luck_modificator = 0.88;
					break;
				default:
					clan_luck_modificator = 1.0;
					break;
			}
		}

		// Поддержка понижения штрафа от Диадемы Академии
		// TODO: Вынести хардкод кланового скилла и диадемы в отдельный calcStat()
		int academy_diadema_luck = getSkillLevel(L2Skill.SKILL_ACADEMY_DIADEMA);
		if(academy_diadema_luck > 0)
		{
			academy_diadema_modificator = 0.5;
		}

		// The death steal you some Exp
		double percentLost = Config.PLAYER_XP_PERCENT_LOST[getLevel()] * clan_luck_modificator * academy_diadema_modificator;

		if(hasBadReputation())
		{
			percentLost *= Config.RATE_BADREPUTATION_EXP_LOST;
		}

		// Lindvior+: player loses 100% experience when killed by war clan.
		if(atwar)
		{
			percentLost /= 1.0;
		}

		// Calculate the Experience loss
		long lostExp = 0L;

		if(!_eventController.isParticipant())
		{
			lostExp = lvl < ExperienceTable.getInstance().getMaxLevel() ? Math.round((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost / 100.0) : Math.round((getStat().getExpForLevel(ExperienceTable.getInstance().getMaxLevel()) - getStat().getExpForLevel(ExperienceTable.getInstance().getMaxLevel() - 1)) * percentLost / 100.0);
		}

		// Get the Experience before applying penalty
		_expBeforeDeath = getExp();

		// No xp loss inside pvp zone unless
		// - it's a siege zone and you're NOT participating
		// - you're killed by a non-pc whose not belong to the siege
		if(isInsideZone(ZONE_PVP))
		{
			// No xp loss for siege participants inside siege zone
			if(isInsideZone(ZONE_SIEGE))
			{
				if(_isInSiege && (killed_by_pc || killed_by_siege_npc))
				{
					lostExp = 0L;
				}
			}
			else if(killed_by_pc)
			{
				lostExp = 0L;
			}
		}

		// Делевел только до 85 уровня
		if(lostExp != 0L && getLevel() <= 85)
		{
			if(getExp() - lostExp < getStat().getExpForLevel(getLevel()))
			{
				lostExp = getExp() - getStat().getExpForLevel(getLevel());
			}
			else if(getExp() - lostExp == getStat().getExpForLevel(getLevel()))
			{
				lostExp = 0L;
			}
		}

		// Если пк умирает он должен терять в 5 раз больше экспы.
		if(hasBadReputation())
		{
			lostExp *= 5;
		}

		// Set the new Experience value of the L2PcInstance
		getStat().addExp(-lostExp);
	}

	public boolean isPartyWaiting()
	{
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}

	public int getPartyRoom()
	{
		return _partyRoomId;
	}

	public void setPartyRoom(int id)
	{
		_partyRoomId = id;
	}

	public boolean isInPartyMatchRoom()
	{
		return _partyRoomId > 0;
	}

	/**
	 * Stop the HP/MP/CP Regeneration task.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Set the RegenActive flag to False </li>
	 * <li>Stop the HP/MP/CP Regeneration task </li><BR><BR>
	 */
	public void stopAllTimers()
	{
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopFeed();
		clearPetData();
		storePetFood(_mountNpcId);
		_pvpFlagController.stopFlag();
		stopPunishTask(true);
		stopSoulTask();
		stopChargeTask();
		stopFameTask();
		stopRecommendationGiveTask();
	}

	public int getSizePets()
	{
		return hasPet() ? _summons.size() - 1 : _summons.size();
	}

	/**
	 * @return {@code true} if the player has a pet, {@code false} otherwise
	 */
	public boolean hasSummon()
	{
		return !_summons.isEmpty();
	}

	/**
	 * @return the L2Decoy of the L2PcInstance or null.
	 */
	public List<L2Decoy> getDecoy()
	{
		return _decoy;
	}

	/**
	 * @return the L2Trap of the L2PcInstance or null.
	 */
	public L2Trap getTrap()
	{
		return _trap;
	}

	/**
	 * Set the L2Trap of this L2PcInstance<BR><BR>
	 *
	 * @param trap
	 */
	public void setTrap(L2Trap trap)
	{
		_trap = trap;
	}

	/**
	 * Добавляет пета персонажу
	 * @param pet инстанс суммона
	 */
	public void addPet(L2Summon pet)
	{
		_summons.add(pet);
		if(pet.isPet())
		{
			startFeed(pet.getNpcId());
		}
	}

	public void deletePet(L2Summon pet)
	{
		if(pet != null && pet.isPet())
		{
			stopFeed();
		}
		_summons.remove(pet);
		if(!_summons.isEmpty())
		{
			for(L2Summon summon : _summons)
			{
				summon.sendInfo(this);
			}
		}
	}

	/**
	 * Set the L2Decoy of the L2PcInstance.<BR><BR>
	 *
	 * @param decoy
	 */
	public void addDecoy(L2Decoy decoy)
	{
		synchronized(this)
		{
			if(_decoy == null)
			{
				_decoy = new FastList(1);
			}

			_decoy.add(decoy);
		}
	}

	public void removeDecoy(L2Decoy decoy)
	{
		synchronized(this)
		{
			if(_decoy != null && decoy != null)
			{
				_decoy.remove(decoy);
			}
		}
	}

	/**
	 * @return the L2Summon of the L2PcInstance or null.
	 */
	public List<L2TamedBeastInstance> getTrainedBeasts()
	{
		return _tamedBeast;
	}

	/**
	 * Set the L2Summon of the L2PcInstance.<BR><BR>
	 *
	 * @param tamedBeast
	 */
	public void addTrainedBeast(L2TamedBeastInstance tamedBeast)
	{
		if(_tamedBeast == null)
		{
			_tamedBeast = new FastList<>();
		}
		_tamedBeast.add(tamedBeast);
	}

	/**
	 * @return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public L2Request getRequest()
	{
		return _request;
	}

	/**
	 * @return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public L2PcInstance getActiveRequester()
	{
		L2PcInstance requester = _activeRequester;
		if(requester != null)
		{
			if(requester.isRequestExpired() && _activeTradeList == null)
			{
				_activeRequester = null;
			}
		}
		return _activeRequester;
	}

	/**
	 * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 *
	 * @param requester
	 */
	public void setActiveRequester(L2PcInstance requester)
	{
		_activeRequester = requester;
	}

	/**
	 * @return {@code true} if a transaction is in progress.
	 */
	public boolean isProcessingRequest()
	{
		return getActiveRequester() != null || _requestExpireTime > GameTimeController.getInstance().getGameTicks();
	}

	/**
	 * @return {@code true} if a transaction is in progress.
	 */
	public boolean isProcessingTransaction()
	{
		return getActiveRequester() != null || _activeTradeList != null || _requestExpireTime > GameTimeController.getInstance().getGameTicks();
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 *
	 * @param partner
	 */
	public void onTransactionRequest(L2PcInstance partner)
	{
		_requestExpireTime = GameTimeController.getInstance().getGameTicks() + REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND;
		partner._activeRequester = this;
	}

	/**
	 * @return {@code true} if last request is expired.
	 */
	public boolean isRequestExpired()
	{
		return !(_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 */
	public void onTransactionResponse()
	{
		_requestExpireTime = 0;
	}

	/**
	 * @return active Warehouse.
	 */
	public ItemContainer getActiveWarehouse()
	{
		return _activeWarehouse;
	}

	/**
	 * Select the Warehouse to be used in next activity.<BR><BR>
	 *
	 * @param warehouse
	 */
	public void setActiveWarehouse(ItemContainer warehouse)
	{
		_activeWarehouse = warehouse;
	}

	/**
	 * @return active TradeList.
	 */
	public TradeList getActiveTradeList()
	{
		return _activeTradeList;
	}

	/**
	 * Select the TradeList to be used in next activity.<BR><BR>
	 *
	 * @param tradeList
	 */
	public void setActiveTradeList(TradeList tradeList)
	{
		_activeTradeList = tradeList;
	}

	public void onTradeStart(L2PcInstance partner)
	{
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);

		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1).addPcName(partner));
		sendPacket(new TradeStart(this));
	}

	public void onTradeConfirm(L2PcInstance partner)
	{
		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CONFIRMED_TRADE).addPcName(partner));
		sendPacket(new TradePressOtherOk());
	}

	public void onTradeCancel(L2PcInstance partner)
	{
		if(_activeTradeList == null)
		{
			return;
		}

		_activeTradeList.lock();
		_activeTradeList = null;

		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CANCELED_TRADE).addPcName(partner));
		sendPacket(new TradeDone(0));
	}

	public void onTradeFinish(boolean successfull)
	{
		_activeTradeList = null;
		sendPacket(new TradeDone(1));
		if(successfull)
		{
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
		}
	}

	public void startTrade(L2PcInstance partner)
	{
		onTradeStart(partner);
		partner.onTradeStart(this);
	}

	public void cancelActiveTrade()
	{
		if(_activeTradeList == null)
		{
			return;
		}

		L2PcInstance partner = _activeTradeList.getPartner();
		if(partner != null)
		{
			partner.onTradeCancel(this);
		}
		onTradeCancel(this);
	}

	/**
	 * @return the _createList object of the L2PcInstance.
	 */
	public L2ManufactureList getCreateList()
	{
		return _createList;
	}

	/**
	 * Set the _createList object of the L2PcInstance.<BR><BR>
	 *
	 * @param x
	 */
	public void setCreateList(L2ManufactureList x)
	{
		_createList = x;
	}

	/**
	 * @return the _buyList object of the L2PcInstance.
	 */
	public TradeList getSellList()
	{
		if(_sellList == null)
		{
			_sellList = new TradeList(this);
		}
		return _sellList;
	}

	/**
	 * @return the _buyList object of the L2PcInstance.
	 */
	public TradeList getBuyList()
	{
		if(_buyList == null)
		{
			_buyList = new TradeList(this);
		}
		return _buyList;
	}

	/**
	 * @return тип лавки у текущего игрока
	 */
	public PlayerPrivateStoreType getPrivateStoreType()
	{
		return _privateStoreType;
	}

	/**
	 * Установить указанный тип лавки текущему игроку
	 * @param type тип лавки
	 */
	public void setPrivateStoreType(PlayerPrivateStoreType type)
	{
		_privateStoreType = type;

		if(Config.OFFLINE_DISCONNECT_FINISHED && _privateStoreType == PlayerPrivateStoreType.NONE && (_client == null || _client.isDetached()))
		{
			getLocationController().delete();
		}
	}

	/**
	 * @return the _clan object of the L2PcInstance.
	 */
	public L2Clan getClan()
	{
		return _clan;
	}

	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the
	 * L2PcInstance.
	 *
	 * @param clan
	 */
	public void setClan(L2Clan clan)
	{
		_clan = clan;
		setTitle("");

		if(clan == null)
		{
			_clanId = 0;
			_clanPrivileges = 0;
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			_activeWarehouse = null;
			return;
		}

		if(!clan.isMember(getObjectId()))
		{
			// char has been kicked from clan
			setClan(null);
			return;
		}

		_clanId = clan.getClanId();
	}

	/**
	 * @return {@code true} if the L2PcInstance is the leader of its clan.
	 */
	public boolean isClanLeader()
	{
		if(_clan == null)
		{
			return false;
		}
		return getObjectId() == _clan.getLeaderId();
	}

	/***
	 * @return {@code true} если персонаж является лидером группы, если он в ней состоит
	 */
	public boolean isPartyLeader()
	{
		if(_party == null)
		{
			return false;
		}
		return getObjectId() == _party.getLeaderObjectId();
	}

	/***
	 * @return {@code true} если персонаж является лидером группы, если он в ней состоит
	 */
	public boolean isCommandChannelLeader()
	{
		if(_party == null || _party.getCommandChannel() == null)
		{
			return false;
		}
		return getObjectId() == _party.getCommandChannel().getLeaderObjectId();
	}

	/**
	 * Disarm the player's weapon.
	 * @return {@code true} if the player was disarmed or doesn't have a weapon to disarm, {@code false} otherwise.
	 */
	public boolean disarmWeapons()
	{
		// If there is no weapon to disarm then return true.
		L2ItemInstance wpn = _inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if(wpn == null)
		{
			return true;
		}

		// Don't allow disarming if the weapon is force equip.
		if(wpn.getWeaponItem().isForceEquip())
		{
			return false;
		}

		// Don't allow disarming a cursed weapon.
		if(isCursedWeaponEquipped())
		{
			return false;
		}

		// Don't allow disarming a Combat Flag or Territory Ward.
		if(_combatFlagEquippedId)
		{
			return false;
		}

		L2ItemInstance[] unequiped = _inventory.unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
		InventoryUpdate iu = new InventoryUpdate();
		for(L2ItemInstance itm : unequiped)
		{
			iu.addModifiedItem(itm);
		}

		sendPacket(iu);
		abortAttack();
		broadcastUserInfo();

		// This can be 0 if the user pressed the right mousebutton twice very fast.
		if(unequiped.length > 0)
		{
			SystemMessage sm;
			if(unequiped[0].getEnchantLevel() > 0)
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addNumber(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0]);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequiped[0]);
			}
			sendPacket(sm);
		}
		return true;
	}

	/**
	 * Disarm the player's shield.
	 * @return {@code true}.
	 */
	public boolean disarmShield()
	{
		L2ItemInstance shieldItem = _inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if(shieldItem != null)
		{
			L2ItemInstance[] unEquippedItems = _inventory.unEquipItemInBodySlotAndRecord(shieldItem.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance itm : unEquippedItems)
			{
				iu.addModifiedItem(itm);
			}
			sendPacket(iu);

			abortAttack();
			broadcastUserInfo();

			// this can be 0 if the user pressed the right mousebutton twice
			// very fast
			if(unEquippedItems.length > 0)
			{
				SystemMessage sm = null;
				if(unEquippedItems[0].getEnchantLevel() > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addNumber(unEquippedItems[0].getEnchantLevel());
					sm.addItemName(unEquippedItems[0]);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unEquippedItems[0]);
				}
				sendPacket(sm);
			}
		}
		return true;
	}

	public boolean mount(L2Summon pet)
	{
		if(!disarmWeapons())
		{
			return false;
		}
		if(!disarmShield())
		{
			return false;
		}
		if(isTransformed())
		{
			return false;
		}

		stopAllToggles();
		Ride mount = new Ride(this, true, pet.getTemplate().getNpcId());
		setMount(pet.getNpcId(), pet.getLevel(), mount.getMountType());
		_mountObjectID = pet.getControlObjectId();
		clearPetData();
		startFeed(pet.getNpcId());
		broadcastPacket(mount);

		// Notify self and others about speed change
		broadcastUserInfo();

		pet.unSummon(false);

		return true;
	}

	public boolean mount(int npcId, int controlItemObjId, boolean useFood)
	{
		if(!disarmWeapons())
		{
			return false;
		}
		if(!disarmShield())
		{
			return false;
		}
		if(isTransformed())
		{
			return false;
		}

		stopAllToggles();
		Ride mount = new Ride(this, true, npcId);
		if(setMount(npcId, getLevel(), mount.getMountType()))
		{
			clearPetData();
			_mountObjectID = controlItemObjId;
			broadcastPacket(mount);

			// Notify self and others about speed change
			broadcastUserInfo();
			if(useFood)
			{
				startFeed(npcId);
			}
			return true;
		}
		return false;
	}

	public boolean mountPlayer(L2Summon pet)
	{
		if(pet != null && pet.isMountable() && !isMounted() && !isBetrayed())
		{
			if(isDead())
			{
				// A strider cannot be ridden when dead
				sendActionFailed();
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			}
			else if(pet.isDead())
			{
				// A dead strider cannot be ridden.
				sendActionFailed();
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			}
			else if(pet.isInCombat() || pet.isRooted())
			{
				// A strider in battle cannot be ridden
				sendActionFailed();
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;

			}
			else if(isInCombat())
			{
				// A strider cannot be ridden while in battle
				sendActionFailed();
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			}
			else if(_isSitting)
			{
				// A strider can be ridden only when standing
				sendActionFailed();
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			}
			else if(_fishing)
			{
				// You can't mount, dismount, break and drop items while fishing
				sendActionFailed();
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			}
			else if(isTransformed() || isCursedWeaponEquipped())
			{
				// no message needed, player while transformed doesn't have mount action
				sendActionFailed();
				return false;
			}
			else if(_inventory.getItemByItemId(9819) != null)
			{
				sendActionFailed();
				// FIXME: Wrong Message
				sendMessage("You cannot mount a steed while holding a flag.");
				return false;
			}
			else if(pet.isHungry())
			{
				sendActionFailed();
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else if(!Util.checkIfInRange(200, this, pet, true))
			{
				sendActionFailed();
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
				return false;
			}
			else if(!pet.isDead() && !isMounted())
			{
				mount(pet);
			}
		}
		else if(isMounted())
		{
			if(_mountType == 2 && isInsideZone(L2Character.ZONE_NOLANDING))
			{
				sendActionFailed();
				sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				return false;
			}
			else if(isHungry())
			{
				sendActionFailed();
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			}
			else
			{
				dismount();
			}
		}
		return true;
	}

	public boolean dismount()
	{
		boolean wasFlying = isFlying();

		sendPacket(new SetupGauge(SetupGauge.GREEN_MINI, 0, 0));
		int petId = _mountNpcId;
		if(setMount(0, 0, 0))
		{
			stopFeed();
			clearPetData();
			if(wasFlying)
			{
				removeSkill(SkillTable.FrequentSkill.WYVERN_BREATH.getSkill());
			}
			Ride dismount = new Ride(this, false, 0);
			broadcastPacket(dismount);
			_mountObjectID = 0;
			storePetFood(petId);
			// Notify self and others about speed change
			broadcastUserInfo();
			return true;
		}
		return false;
	}

	public long getUptime()
	{
		return System.currentTimeMillis() - _uptime;
	}

	public void setUptime(long time)
	{
		_uptime = time;
	}

	/**
	 * Set the _party object of the L2PcInstance AND join it.
	 *
	 * @param party
	 */
	public void joinParty(L2Party party)
	{
		if(party != null)
		{
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			_party = party;
			party.addPartyMember(this);
		}
	}

	/**
	 * Manage the Leave Party task of the L2PcInstance.<BR><BR>
	 */
	public void leaveParty()
	{
		if(isInParty())
		{
			_party.removePartyMember(this, PartyExitReason.DISCONNECTED);
			_party = null;
		}
	}

	/**
	 * @return время, начиная с которого пати считается фуллпартией (кол-во участников == 7)
	 */
	public long getStartingTimeInFullParty()
	{
		return _startingTimeInFullParty;
	}

	/**
	 * Устанавливает время, с которого партия считается фуллпартией
	 *
	 * @param time время, когда количество персонажей в партии стало равно 7-ми
	 */
	public void setStartingTimeInFullParty(long time)
	{
		_startingTimeInFullParty = time;
	}

	/**
	 * @return время, начиная с которого пати считается фуллпартией (кол-во участников == 7)
	 */
	public long getStartingTimeInParty()
	{
		return _startingTimeInParty;
	}

	/**
	 * Устанавливает время, с которого партия считается фуллпартией
	 *
	 * @param time время, когда количество персонажей в партии стало равно 7-ми
	 */
	public void setStartingTimeInParty(long time)
	{
		_startingTimeInParty = time;
	}

	public void setAccountAccesslevel(int level)
	{
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}

	/**
	 * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its _KnownPlayers (broadcast).
	 *
	 * @param broadcastType
	 */
	public void updateAndBroadcastStatus(int broadcastType)
	{
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and
		// CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		if(broadcastType == 1)
		{
			sendUserInfo();
		}
		else if(broadcastType == 2)
		{
			broadcastUserInfo();
		}
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).
	 *
	 * @param flag
	 */
	public void setBadReputationFlag(int flag)
	{
		sendUserInfo();
		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if(!_summons.isEmpty())
			{
				for(L2Summon pet : _summons)
				{
					player.sendPacket(new RelationChanged(pet, getRelation(player), isAutoAttackable(player)));
				}
			}
		}
	}

	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).
	 */
	public void broadcastReputationStatus()
	{
        UI ui = new UI(this, false);
        ui.addComponentType(UserInfoType.SOCIAL);
        sendPacket(ui);

		for(L2PcInstance player : getKnownList().getKnownPlayers().values())
		{
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if(!_summons.isEmpty())
			{
				for(L2Summon pet : _summons)
				{
					player.sendPacket(new RelationChanged(pet, getRelation(player), isAutoAttackable(player)));
				}
			}
		}
	}

	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
	 *
	 * @param isOnline
	 * @param updateInDb
	 */
	public void setOnlineStatus(boolean isOnline, boolean updateInDb)
	{
		if(_isOnline != isOnline)
		{
			_isOnline = isOnline;
		}

		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		if(updateInDb)
		{
			updateOnlineStatus();
		}
	}

	/**
	 * Update the characters table of the database with online status and lastAccess of this L2PcInstance (called when login and logout).
	 */
	public void updateOnlineStatus()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE charId=?");
			statement.setInt(1, isOnlineInt());
			statement.setLong(2, System.currentTimeMillis());
			statement.setInt(3, getObjectId());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed updating character online status.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Запись созданного персонажа в базу данных
	 */
	private boolean createDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.INSERT_CHARACTER);
			statement.setString(1, _accountName);
			statement.setInt(2, getObjectId());
			statement.setString(3, getName());
			statement.setInt(4, getLevel());
			statement.setInt(5, getMaxHp());
			statement.setDouble(6, getCurrentHp());
			statement.setInt(7, getMaxCp());
			statement.setDouble(8, getCurrentCp());
			statement.setInt(9, getMaxMp());
			statement.setDouble(10, getCurrentMp());
			statement.setInt(11, _appearance.getOldFace());
			statement.setInt(12, _appearance.getOldHairStyle());
			statement.setInt(13, _appearance.getOldHairColor());
			statement.setInt(14, _appearance.getCustomFace());
			statement.setInt(15, _appearance.getCustomHairStyle());
			statement.setInt(16, _appearance.getCustomHairColor());
			statement.setInt(17, _appearance.getSex() ? 1 : 0);
			statement.setLong(18, getExp());
			statement.setInt(19, getSp());
			statement.setInt(20, _reputation);
			statement.setInt(21, _fame);
			statement.setInt(22, _pvpKills);
			statement.setInt(23, _pkKills);
			statement.setInt(24, _clanId);
			statement.setInt(25, getRace().ordinal());
			statement.setInt(26, getClassId().getId());
			statement.setInt(27, _baseClassId);
			statement.setLong(28, _deleteTimer);
			statement.setInt(29, hasDwarvenCraft() ? 1 : 0);
			statement.setString(30, getTitle());
			statement.setInt(31, _appearance.getTitleColor());
			statement.setInt(32, getAccessLevel().getLevel());
			statement.setInt(33, isOnlineInt());
			statement.setInt(34, _clanPrivileges);
			statement.setInt(35, 0);
			statement.setLong(36, 0);
			statement.setDate(37, new Date(_createDate.getTimeInMillis()));
			statement.setInt(38, 0);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not insert char data: " + e.getMessage(), e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		return true;
	}

	/**
	 * @return
	 */
	public Forum getMail()
	{
		if(_forumMail == null)
		{
			_forumMail = ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName());

			if(_forumMail == null)
			{
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				_forumMail = ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName());
			}
		}

		return _forumMail;
	}

	/**
	 * @param forum
	 */
	public void setMail(Forum forum)
	{
		_forumMail = forum;
	}

	/**
	 * @return
	 */
	public Forum getMemo()
	{
		if(_forumMemo == null)
		{
			_forumMemo = ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName);

			if(_forumMemo == null)
			{
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				_forumMemo = ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName);
			}
		}

		return _forumMemo;
	}

	/**
	 * @param forum
	 */
	public void setMemo(Forum forum)
	{
		_forumMemo = forum;
	}

	/**
	 * Restores:
	 * <ul>
	 * <li>Skills</li>
	 * <li>Macros</li>
	 * <li>Short-cuts</li>
	 * <li>Henna</li>
	 * <li>Teleport Bookmark</li>
	 * <li>Recipe Book</li>
	 * <li>Recipe Shop List (If configuration enabled)</li>
	 * <li>Premium Item List</li>
	 * <li>Pet Inventory Items</li>
	 * </ul>
	 */
	private void restoreCharData()
	{
		// Retrieve from the database all skills of this L2PcInstance and add them to _skills.
		restoreSkills();

		// Retrieve from the database all macroses of this L2PcInstance and add them to _macros.
		_macroses.restore();

		// Retrieve from the database all shortCuts of this L2PcInstance and add them to _shortCuts.
		_shortcutController.restore();

		// Retrieve from the database all henna of this L2PcInstance and add them to _henna.
		restoreHenna();

		// Retrieve from the database all teleport bookmark of this L2PcInstance and add them to _tpbookmark.
		restoreTeleportBookmark();

		// Retrieve from the database the recipe book of this L2PcInstance.
		_recipeBookController.restoreBook(true);

		// Восстанавливаем бинды
		restoreBindConfigData();

		// Restore Recipe Shop list
		if(Config.STORE_RECIPE_SHOPLIST)
		{
			restoreRecipeShopList();
		}

		// Load Premium Item List
		loadPremiumItemList();

		// Restore items in pet inventory.
		checkPetInvItems();
	}

	public Map<Integer, L2PremiumItem> getPremiumItemList()
	{
		return _premiumItems;
	}

	private void loadPremiumItemList()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT itemNum, itemId, itemCount, itemSender, itemSenderMessage FROM character_premium_items WHERE charId=?");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				_premiumItems.put(rset.getInt("itemNum"), new L2PremiumItem(rset.getInt("itemId"), rset.getLong("itemCount"), rset.getString("itemSender"), rset.getString("itemSenderMessage")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore premium items: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void updatePremiumItem(int itemNum, long newcount)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_premium_items SET itemCount=? WHERE charId=? AND itemNum=? ");
			statement.setLong(1, newcount);
			statement.setInt(2, getObjectId());
			statement.setInt(3, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not update premium items: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void addPremiumItem(L2PremiumItem item)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_premium_items (charId, itemNum, itemId, itemCount, itemSender, itemSenderMessage, time) VALUES (?, ?, ?, ?, ?, ?, ?)");
			statement.setInt(1, getObjectId());
			statement.setInt(2, _premiumItems.size() + 1);
			statement.setInt(3, item.getItemId());
			statement.setLong(4, item.getCount());
			statement.setString(5, item.getSender());
			statement.setString(6, item.getSenderMessage());
			statement.setLong(7, Calendar.getInstance().getTimeInMillis());
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not add premium item: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_premiumItems.put(_premiumItems.size() + 1, item);
		}
	}

	public void deletePremiumItem(int itemNum)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_premium_items WHERE charId=? AND itemNum=? ");
			statement.setInt(1, getObjectId());
			statement.setInt(2, itemNum);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not delete premium item: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_premiumItems.remove(itemNum);
		}
	}

	/**
	 * Update L2PcInstance stats in the characters table of the database.
	 *
	 * @param storeActiveEffects
	 */
	public void store(boolean storeActiveEffects)
	{
		// update client coords, if these look like true
		if(isInsideRadius(_clientX, _clientY, 1000, true))
		{
			setXYZ(_clientX, _clientY, _clientZ);
		}

		storeSummons();
		storeCharBase();
		storeCharSubClasses();
		storeEffect(storeActiveEffects);
		// Записываем бинды
		storeBindConfigData();
		storeItemReuseDelay();
		transformInsertInfo();

		if(Config.STORE_RECIPE_SHOPLIST)
		{
			storeRecipeShopList();
		}
		storeRecommendations();
		saveVitalityData();
	}

	/**
	 * Store all summons in summon save table.
	 */
	private void storeSummons()
	{
		_summons.stream().filter(summon -> summon.isSummon() && summon instanceof L2SummonInstance).forEach(summon -> CharSummonTable.getInstance().saveSummon((L2SummonInstance) summon));
	}

	public void storeCharBase()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.UPDATE_CHARACTER);

			statement.setInt(1, getStat().getLevelBaseClass());
			statement.setInt(2, getMaxHp());
			statement.setDouble(3, getCurrentHp());
			statement.setInt(4, getMaxCp());
			statement.setDouble(5, getCurrentCp());
			statement.setInt(6, getMaxMp());
			statement.setDouble(7, getCurrentMp());
			statement.setInt(8, _appearance.getOldFace());
			statement.setInt(9, _appearance.getOldHairStyle());
			statement.setInt(10, _appearance.getOldHairColor());
			statement.setInt(11, _appearance.getCustomFace());
			statement.setInt(12, _appearance.getCustomHairStyle());
			statement.setInt(13, _appearance.getCustomHairColor());
			statement.setInt(14, _appearance.getSex() ? 1 : 0);
			statement.setInt(15, getHeading());
			statement.setInt(16, _isInObserverMode ? _lastObserverPositionX : getX());
			statement.setInt(17, _isInObserverMode ? _lastObserverPositionY : getY());
			statement.setInt(18, _isInObserverMode ? _lastObserverPositionZ : getZ());
			statement.setLong(19, getStat().getExpBaseClass());
			statement.setLong(20, _expBeforeDeath);
			statement.setInt(21, getStat().getSpBaseClass());
			statement.setInt(22, _reputation);
			statement.setInt(23, _fame);
			statement.setInt(24, _pvpKills);
			statement.setInt(25, _pkKills);
			statement.setInt(26, _clanId);
			statement.setInt(27, getRace().ordinal());
			statement.setInt(28, getClassId().getId());
			statement.setLong(29, _deleteTimer);
			statement.setString(30, getTitle());
			statement.setInt(31, _appearance.getTitleColor());
			statement.setInt(32, getAccessLevel().getLevel());
			statement.setInt(33, isOnlineInt());
			statement.setInt(34, _clanPrivileges);
			statement.setInt(35, _baseClassId);

			long totalOnlineTime = _onlineTime;

			if(_onlineBeginTime > 0)
			{
				long diff = (System.currentTimeMillis() - _onlineBeginTime) / 1000;
				totalOnlineTime += diff;

				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					updateWorldStatistic(CategoryType.TIME_PLAYED, null, diff);
				}
			}

			statement.setLong(36, totalOnlineTime);
			statement.setInt(37, _punishLevel.ordinal());
			statement.setLong(38, _punishTimer);
			statement.setInt(39, _isNoble ? 1 : 0);
			statement.setLong(40, _powerGrade);
			statement.setInt(41, _pledgeType);
			statement.setInt(42, _lvlJoinedAcademy);
			statement.setLong(43, _apprentice);
			statement.setLong(44, _sponsor);
			statement.setInt(45, _alliedVarkaKetra);
			statement.setLong(46, _clanJoinExpiryTime);
			statement.setLong(47, _clanCreateExpiryTime);
			statement.setString(48, getName());
			statement.setInt(49, _bookmarkslot);
			statement.setLong(50, _pcBangPoints);
			statement.setInt(51, _appearance.getNameColor());
			statement.setString(52, getAccountName());
			statement.setInt(53, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store char base data: " + this + " - " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void storeCharSubClasses()
	{
		if(getTotalSubClasses() <= 0)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SUBCLASSES_UPDATE);
			for(SubClass subClass : getSubClasses().values())
			{
				statement.setLong(1, subClass.getExp());
				statement.setInt(2, subClass.getSp());
				statement.setInt(3, subClass.getLevel());
				statement.setInt(4, subClass.getClassId());
				statement.setInt(5, subClass.getClassType().ordinal());
				statement.setInt(6, getObjectId());
				statement.setInt(7, subClass.getClassIndex());

				statement.execute();
				statement.clearParameters();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store sub class data for " + getName() + ": " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Сохранения отката предметов в базу
	 */
	private void storeItemReuseDelay()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.ITEM_REUSE_CLEAR);

			statement.setInt(1, getObjectId());
			statement.execute();
			statement.close();

			statement = con.prepareStatement(Characters.ITEM_REUSE_STORE);
			for(TimeStamp ts : _reuseTimeStampsItems.values())
			{
				if(ts != null && ts.hasNotPassed())
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, ts.getItemId());
					statement.setInt(3, ts.getItemObjectId());
					statement.setLong(4, ts.getReuse());
					statement.setDouble(5, ts.getStamp());
					statement.execute();
					statement.clearParameters();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store char item reuse data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int isOnlineInt()
	{
		if(_isOnline && _client != null)
		{
			return _client.isDetached() ? 2 : 1;
		}
		return 0;
	}

	/**
	 * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database.<BR><BR>
	 * <p/>
	 * <B><U> Concept</U> :</B><BR><BR>
	 * All skills own by a L2PcInstance are identified in <B>_skills</B><BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Replace oldSkill by newSkill or Add the newSkill </li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character </li><BR><BR>
	 *
	 * @param newSkill The L2Skill to add to the L2Character
	 * @param store
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public L2Skill addSkill(L2Skill newSkill, boolean store)
	{
		if(newSkill == null)
		{
			return null;
		}

		// Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
		L2Skill oldSkill = addSkill(newSkill);

		List<Integer> replaceables = SkillTreesData.getInstance().getAllReplaceableSkills(newSkill.getId());
		if(replaceables != null)
		{
			replaceables.stream().filter(skillId -> getSkills().containsKey(skillId)).forEach(skillId -> removeSkill(getSkills().get(skillId), true, store));
		}

		// Add or update a L2PcInstance skill in the character_skills table of the database
		if(store)
		{
			storeSkill(newSkill, oldSkill, -1);
		}
		return oldSkill;
	}

	public L2Skill removeSkill(L2Skill skill, boolean store, boolean cancelEffect)
	{
		if(store)
		{
			return removeSkill(skill);
		}
		return super.removeSkill(skill, cancelEffect);
	}

	/**
	 * Добавляет или обновляет в базе (character_skills) заданный скилл игроку
	 * @param newSkill добавляемый персонажу скилл
	 * @param oldSkill {@code null}, если скилл до этого момента отсутствовал у персонажа
	 * @param newClassIndex ClassIndex для которого будет сохранен скилл, если == -1, то будет сохранен по умолчанию для текущего класса
	 */
	private void storeSkill(L2Skill newSkill, L2Skill oldSkill, int newClassIndex)
	{
		int classIndex = _classIndex;

		if(newClassIndex > -1)
		{
			classIndex = newClassIndex;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			if(oldSkill != null && newSkill != null)
			{
				statement = con.prepareStatement(Characters.SKILLS_LEVEL_UPDATE);
				statement.setInt(1, newSkill.getLevel());
				statement.setInt(2, oldSkill.getId());
				statement.setInt(3, getObjectId());
				statement.setInt(4, newSkill.isSharedSkill() ? -1 : classIndex);
				statement.execute();
			}
			else if(newSkill != null)
			{
				statement = con.prepareStatement(Characters.SKILLS_ADD);
				statement.setInt(1, getObjectId());
				statement.setInt(2, newSkill.getId());
				statement.setInt(3, newSkill.getLevel());
				statement.setInt(4, newSkill.isSharedSkill() ? -1 : classIndex);
				statement.execute();
			}
			else
			{
				_log.log(Level.WARN, "Could not store new skill. It's NULL");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error could not store char skills: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Retrieve from the database all skills of this L2PcInstance and add them to _skills.
	 */
	private void restoreSkills()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		FastList<Integer> toRemove = new FastList<>();
		try
		{
			// Retrieve all skills of this L2PcInstance from the database
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SKILLS_RESTORE);

			statement.setInt(1, getObjectId());
			statement.setInt(2, _classIndex); // Основные скиллы персонажа
			statement.setInt(3, -1); // Общие для всех подклассов скиллы

			rset = statement.executeQuery();

			// Go though the recordset of this SQL query
			while(rset.next())
			{
				int id = rset.getInt("skill_id");
				int level = rset.getInt("skill_level");

				// Create a L2Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);

				if(skill == null)
				{
					_log.log(Level.WARN, "Skipped null skill Id: " + id + " Level: " + level + " while restoring player skills for playerObjId: " + getObjectId());
					continue;
				}

				// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
				addSkill(skill);

				List<Integer> replaceable = SkillTreesData.getInstance().getAllReplaceableSkills(skill.getId());
				if(replaceable != null)
				{
					toRemove.addAll(replaceable.stream().collect(Collectors.toList()));
				}

				if(Config.SKILL_CHECK_ENABLE && (!isGM() || Config.SKILL_CHECK_GM))
				{
					if(!SkillTreesData.getInstance().isSkillAllowed(this, skill))
					{
						Util.handleIllegalPlayerAction(this, "Player " + getName() + " has invalid skill " + skill.getName() + " (" + skill.getId() + '/' + skill.getLevel() + "), class:" + ClassTemplateTable.getInstance().getClass(getClassId()).getClassName(), 1);
						if(Config.SKILL_CHECK_REMOVE)
						{
							removeSkill(skill);
						}
					}
				}
			}

			toRemove.stream().filter(skillId -> getSkills().containsKey(skillId)).forEach(skillId -> removeSkill(getSkills().get(skillId), true, true));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore character " + this + " skills: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Восстанавливаем предметы с откатом из базы и даем их игроку
	 */
	private void restoreItemReuse()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.ITEM_REUSE_RESTORE);
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			int itemId;
			int itemObjId;
			long reuseDelay;
			long systime;
			boolean isInInventory;
			long remainingTime;
			while(rset.next())
			{
				itemId = rset.getInt("itemId");
				itemObjId = rset.getInt("itemObjId");
				reuseDelay = rset.getLong("reuseDelay");
				systime = rset.getLong("systime");
				isInInventory = true;

				// Используем ID предмета
				L2ItemInstance item = _inventory.getItemByItemId(itemId);
				if(item == null)
				{
					item = getWarehouse().getItemByItemId(itemId);
					isInInventory = false;
				}

				if(item != null && item.getItemId() == itemId && item.getReuseDelay() > 0)
				{
					remainingTime = systime - System.currentTimeMillis();
					// Хардкод с 10 секунд
					if(remainingTime > 10)
					{
						addTimeStampItem(item, reuseDelay, systime);

						if(isInInventory && item.isEtcItem())
						{
							int group = item.getSharedReuseGroup();
							if(group > 0)
							{
								sendPacket(new ExUseSharedGroupItem(itemId, group, (int) remainingTime, (int) reuseDelay));
							}
						}
					}
				}
			}
			statement.close();

			statement = con.prepareStatement(Characters.ITEM_REUSE_CLEAR);
			statement.setInt(1, getObjectId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore " + this + " Item Reuse data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Retrieve from the database all Henna of this L2PcInstance, add them to _henna and calculate stats of the L2PcInstance.<BR><BR>
	 */
	private void restoreHenna()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.HENNAS_RESTORE);
			statement.setInt(1, getObjectId());
			statement.setInt(2, _classIndex);
			rset = statement.executeQuery();

			for(int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}

			while(rset.next())
			{
				int slot = rset.getInt("slot");

				if(slot < 1 || slot > 3)
				{
					continue;
				}

				int symbol_id = rset.getInt("symbol_id");

				L2HennaInstance sym = null;

				if(symbol_id != 0)
				{
					L2Henna tpl = HennaTable.getInstance().getTemplate(symbol_id);

					if(tpl != null)
					{
						sym = new L2HennaInstance(tpl);
						_henna[slot - 1] = sym;
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed restoing character " + this + " hennas.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
	}

	/**
	 * @return the number of Henna empty slot of the L2PcInstance.
	 */
	public int getHennaEmptySlots()
	{
		int totalSlots = 0;
		totalSlots = getClassId().level() == 1 ? 2 : 3;

		for(int i = 0; i < 3; i++)
		{
			if(_henna[i] != null)
			{
				totalSlots--;
			}
		}

		if(totalSlots <= 0)
		{
			return 0;
		}

		return totalSlots;
	}

	/**
	 * Remove a Henna of the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 *
	 * @param slot
	 * @return
	 */
	public boolean removeHenna(int slot)
	{
		if(slot < 1 || slot > 3)
		{
			return false;
		}

		slot--;

		if(_henna[slot] == null)
		{
			return false;
		}

		L2HennaInstance henna = _henna[slot];
		_henna[slot] = null;

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.HENNAS_DELETE);

			statement.setInt(1, getObjectId());
			statement.setInt(2, slot + 1);
			statement.setInt(3, _classIndex);

			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed remocing character henna.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();

		// Send Server->Client HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));

        UI ui = new UI(this, false);
        ui.addComponentType(UserInfoType.BASE_STATS, UserInfoType.MAX_HPCPMP, UserInfoType.STATS, UserInfoType.SPEED);
        sendPacket(ui);
		// Add the recovered dyes to the player's inventory and notify them.
		_inventory.addItem(ProcessType.HENNA, henna.getItemIdDye(), henna.getAmountDyeRequire() / 2, this, null);

		reduceAdena(ProcessType.HENNA, henna.getPrice() / 5, this, false);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getItemIdDye());
		sm.addItemNumber(henna.getAmountDyeRequire() / 2);
		sendPacket(sm);

		sendPacket(SystemMessageId.SYMBOL_DELETED);

		return true;
	}

	/**
	 * Add a Henna to the L2PcInstance, save update in the character_hennas table of the database and send Server->Client HennaInfo/UserInfo packet to this L2PcInstance.<BR><BR>
	 *
	 * @param henna
	 * @return
	 */
	public boolean addHenna(L2HennaInstance henna)
	{
		for(int i = 0; i < 3; i++)
		{
			if(_henna[i] == null)
			{
				_henna[i] = henna;

				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();

				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{
					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(Characters.HENNAS_ADD);

					statement.setInt(1, getObjectId());
					statement.setInt(2, henna.getSymbolId());
					statement.setInt(3, i + 1);
					statement.setInt(4, _classIndex);

					statement.execute();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Failed saving character henna.", e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}

				// Send Server->Client HennaInfo packet to this L2PcInstance
				sendPacket(new HennaInfo(this));

				// Send Server->Client UserInfo packet to this L2PcInstance
                UI ui = new UI(this, false);
                ui.addComponentType(UserInfoType.BASE_STATS, UserInfoType.MAX_HPCPMP, UserInfoType.STATS, UserInfoType.SPEED);
                sendPacket(ui);

				return true;
			}
		}
		return false;
	}

	/**
	 * Calculate Henna modifiers of this L2PcInstance.<BR><BR>
	 */
	private void recalcHennaStats()
	{
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
        _hennaLUC = 0;
        _hennaCHA = 0;

		if(_hennaAttrSkill != null && !_hennaAttrSkill.isEmpty())
		{
			_hennaAttrSkill.forEach(this::removeSkill);
			_hennaAttrSkill.clear();
		}
		else
		{
			_hennaAttrSkill = new FastList<>(3);
		}

		for(int i = 0; i < 3; i++)
		{
			if(_henna[i] == null)
			{
				continue;
			}

			// Не даем переродившимся классам использовать 3-е профные краски
			if(!HennaTreeTable.getInstance().getAvialableHennaIds(getClassId().getId()).contains(_henna[i].getSymbolId()))
			{
				continue;
			}

			_hennaINT += _henna[i].getStatINT();
			_hennaSTR += _henna[i].getStatSTR();
			_hennaMEN += _henna[i].getStatMEN();
			_hennaCON += _henna[i].getStatCON();
			_hennaWIT += _henna[i].getStatWIT();
			_hennaDEX += _henna[i].getStatDEX();
            _hennaLUC += _henna[i].getStatLUC();
            _hennaCHA += _henna[i].getStatCHA();
			if(_henna[i].getAttrSkill() != null)
			{
				_hennaAttrSkill.add(_henna[i].getAttrSkill());
				addSkill(_henna[i].getAttrSkill());
			}
		}

		if(!isAwakened())
		{
			if(_hennaINT > 12)
			{
				_hennaINT = 12;
			}
			if(_hennaSTR > 12)
			{
				_hennaSTR = 12;
			}
			if(_hennaMEN > 12)
			{
				_hennaMEN = 12;
			}
			if(_hennaCON > 12)
			{
				_hennaCON = 12;
			}
			if(_hennaWIT > 12)
			{
				_hennaWIT = 12;
			}
			if(_hennaDEX > 12)
			{
				_hennaDEX = 12;
			}
            if(_hennaLUC > 12)
            {
                _hennaLUC = 12;
            }
            if(_hennaCHA > 12)
            {
                _hennaCHA = 12;
            }
		}
	}

	/**
	 * @param slot
	 * @return the Henna of this L2PcInstance corresponding to the selected slot.
	 */
	public L2HennaInstance getHenna(int slot)
	{
		if(slot < 1 || slot > 3)
		{
			return null;
		}

		return _henna[slot - 1];
	}

	/**
	 * @return the INT Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatINT()
	{
		return _hennaINT;
	}

	/**
	 * @return the STR Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatSTR()
	{
		return _hennaSTR;
	}

	/**
	 * @return the CON Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatCON()
	{
		return _hennaCON;
	}

	/**
	 * @return the MEN Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatMEN()
	{
		return _hennaMEN;
	}

	/**
	 * @return the WIT Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatWIT()
	{
		return _hennaWIT;
	}

	/**
	 * @return the DEX Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatDEX()
	{
		return _hennaDEX;
	}

    /**
     * @return the LUC Henna modifier of this L2PcInstance.
     */
    public int getHennaStatLUC()
    {
        return _hennaLUC;
    }

    /**
     * @return the CHA Henna modifier of this L2PcInstance.
     */
    public int getHennaStatCHA()
    {
        return _hennaCHA;
    }

	/**
	 * Return True if the L2PcInstance is autoAttackable.<BR><BR>
	 * <p/>
	 * <B><U> Actions</U> :</B><BR><BR>
	 * <li>Check if the attacker isn't the L2PcInstance Pet </li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same party </li>
	 * <li>Check if the L2PcInstance has Karma </li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender) </li><BR><BR>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker)
	{
		if(attacker == null)
		{
			return false;
		}

		// Check if the attacker isn't the L2PcInstance Pet
		if(attacker.equals(this) || attacker instanceof L2Summon && _summons.contains(attacker))
		{
			return false;
		}

		// Friendly mobs doesnt attack players
		if(attacker instanceof L2FriendlyMobInstance)
		{
			return false;
		}

		// Check if the attacker is a L2MonsterInstance
		if(attacker instanceof L2MonsterInstance)
		{
			return true;
		}

		// is AutoAttackable if both players are in the same duel and the duel is still going on
		if(attacker instanceof L2PcInstance && _duelState == DuelState.DUELLING && _duelId == ((L2PcInstance) attacker)._duelId)
		{
			return true;
		}

		// Check if the attacker is not in the same party. NOTE: Party checks goes before oly checks in order to prevent party member autoattack at oly.
		if(_party != null && _party.getMembers().contains(attacker))
		{
			return false;
		}

		if(_olympiadController.isOpponent(attacker.getActingPlayer()))
		{
			return true;
		}

		// Check if the attacker is in TvT and TvT is started
		if(EventManager.isStarted() && EventManager.isPlayerParticipant(this))
		{
			return true;
		}

		// Check if the attacker is a L2Playable
		if(attacker instanceof L2Playable)
		{
			if(isInsideZone(ZONE_PEACE))
			{
				return false;
			}

			// Get L2PcInstance
			L2PcInstance attackerPlayer = attacker.getActingPlayer();

			if(_clan != null)
			{
				CastleSiegeEngine castleSiegeEngine = CastleSiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if(castleSiegeEngine != null)
				{
					// Check if a castleSiegeEngine is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if(castleSiegeEngine.checkIsDefender(attackerPlayer._clan) && castleSiegeEngine.checkIsDefender(_clan))
					{
						return false;
					}

					// Check if a castleSiegeEngine is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if(castleSiegeEngine.checkIsAttacker(attackerPlayer._clan) && castleSiegeEngine.checkIsAttacker(_clan))
					{
						return false;
					}
				}

				// Check if clan is at war
				if(_clan != null && attackerPlayer._clan != null && _clan.isAtWarWith(attackerPlayer._clanId) && attackerPlayer._clan.isAtWarWith(_clanId) && !isAcademyMember())
				{
					return true;
				}
			}
			// Check if the L2PcInstance is in an arena, but NOT siege zone. NOTE: This check comes before clan/ally checks, but after party checks.
			// This is done because in arenas, clan/ally members can autoattack if they arent in party.
			if(isInsideZone(ZONE_PVP) && attackerPlayer.isInsideZone(ZONE_PVP) && !(isInsideZone(ZONE_SIEGE) && attackerPlayer.isInsideZone(ZONE_SIEGE)))
			{
				return true;
			}

			// Check if the attacker is not in the same clan
			if(_clan != null && _clan.isMember(attacker.getObjectId()))
			{
				return false;
			}

			// Check if the attacker is not in the same ally
			if(attacker instanceof L2PcInstance && getAllyId() != 0 && getAllyId() == attackerPlayer.getAllyId())
			{
				return false;
			}

			// Now check again if the L2PcInstance is in pvp zone, but this time at siege PvP zone, applying clan/ally checks
			if(isInsideZone(ZONE_PVP) && attackerPlayer.isInsideZone(ZONE_PVP) && isInsideZone(ZONE_SIEGE) && attackerPlayer.isInsideZone(ZONE_SIEGE))
			{
				return true;
			}
		}
		else if(attacker instanceof L2DefenderInstance)
		{
			if(_clan != null)
			{
				CastleSiegeEngine castleSiegeEngine = CastleSiegeManager.getInstance().getSiege(this);
				return castleSiegeEngine != null && castleSiegeEngine.checkIsAttacker(_clan);
			}
		}

		// Check if the L2PcInstance has Negative Reputation
		return hasBadReputation() || _pvpFlagController.isFlagged();

	}

	@Override
	public void setName(String value)
	{
		super.setName(value);
		if(Config.CACHE_CHAR_NAMES)
		{
			CharNameTable.getInstance().addName(this);
		}
	}

	@Override
	public L2PcInstance getActingPlayer()
	{
		return this;
	}

	@Override
	public void sendInfo(L2PcInstance activeChar)
	{
		if(isInBoat())
		{
			getLocationController().setXYZ(getBoat().getLocationController().getLoc());

			activeChar.sendPacket(new CI(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if(oldrelation != null && oldrelation != relation1)
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if(!_summons.isEmpty())
				{
					for(L2Summon pet : _summons)
					{
						activeChar.sendPacket(new RelationChanged(pet, relation1, isAutoAttackable(activeChar)));
					}
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if(oldrelation != null && oldrelation != relation2)
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if(!activeChar._summons.isEmpty())
				{
					for(L2Summon pet : activeChar._summons)
					{
						sendPacket(new RelationChanged(pet, relation2, activeChar.isAutoAttackable(this)));
					}
				}
			}
			activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), _inVehiclePosition));
		}
		else if(isInAirShip())
		{
			getLocationController().setXYZ(getAirShip().getLocationController().getLoc());

			activeChar.sendPacket(new CI(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if(oldrelation != null && oldrelation != relation1)
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if(!_summons.isEmpty())
				{
					for(L2Summon pet : _summons)
					{
						activeChar.sendPacket(new RelationChanged(pet, relation1, isAutoAttackable(activeChar)));
					}
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if(oldrelation != null && oldrelation != relation2)
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if(!activeChar._summons.isEmpty())
				{
					for(L2Summon pet : activeChar._summons)
					{
						sendPacket(new RelationChanged(pet, relation2, activeChar.isAutoAttackable(this)));
					}
				}
			}
			activeChar.sendPacket(new ExGetOnAirShip(this, getAirShip()));
		}
		else if(isInShuttle())
		{
			getLocationController().setXYZ(getShuttle().getLocationController().getLoc());

			activeChar.sendPacket(new CI(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if(oldrelation != null && oldrelation != relation1)
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if(!_summons.isEmpty())
				{
					for(L2Summon pet : _summons)
					{
						activeChar.sendPacket(new RelationChanged(pet, relation1, isAutoAttackable(activeChar)));
					}
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if(oldrelation != null && oldrelation != relation2)
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if(!activeChar._summons.isEmpty())
				{
					for(L2Summon pet : activeChar._summons)
					{
						sendPacket(new RelationChanged(pet, relation2, activeChar.isAutoAttackable(this)));
					}
				}
			}
			activeChar.sendPacket(new ExSuttleGetOn(activeChar.getObjectId(), getShuttle().getId(), _inVehiclePosition));
		}
		else
		{
			activeChar.sendPacket(new CI(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if(oldrelation != null && oldrelation != relation1)
			{
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if(!_summons.isEmpty())
				{
					for(L2Summon pet : _summons)
					{
						activeChar.sendPacket(new RelationChanged(pet, relation1, isAutoAttackable(activeChar)));
					}
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if(oldrelation != null && oldrelation != relation2)
			{
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if(!activeChar._summons.isEmpty())
				{
					for(L2Summon pet : activeChar._summons)
					{
						sendPacket(new RelationChanged(pet, relation2, activeChar.isAutoAttackable(this)));
					}
				}
			}
		}
		if(_mountType == 4)
		{
			// TODO: Remove when horse mounts fixed
			activeChar.sendPacket(new Ride(this, false, 0));
			activeChar.sendPacket(new Ride(this, true, _mountNpcId));
		}

		switch(_privateStoreType)
		{
			case SELL:
				activeChar.sendPacket(new PrivateStoreMsg(this));
				break;
			case SELL_PACKAGE:
				activeChar.sendPacket(new ExPrivateStoreWholeMsg(this));
				break;
			case BUY:
				activeChar.sendPacket(new PrivateStoreBuyMsg(this));
				break;
			case MANUFACTURE:
				activeChar.sendPacket(new RecipeShopMsg(this));
				break;
		}
	}

	@Override
	public boolean isPlayer()
	{
		return true;
	}

	@Override
	public RestrictionController getRestrictionController()
	{
		return _restrictionController;
	}

	@Override
	public InstanceController getInstanceController()
	{
		if(_instanceController == null)
		{
			_instanceController = new dwo.gameserver.model.actor.controller.player.InstanceController(this);
		}

		return _instanceController;
	}

	private boolean checkUseMagicConditions(L2Skill skill, boolean forceUse, boolean dontMove)
	{
		L2SkillType skillType = skill.getSkillType();

		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
		if(isOutOfControl() || isParalyzed() && !skill.ignoreSkillParalyze() || isStunned() && !skill.ignoreSkillStun() || isSleeping() || isFlyUp() || isKnockBacked())
		{
			sendActionFailed();
			return false;
		}

		// Check if the player is dead
		if(isDead())
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendActionFailed();
			return false;
		}

		if(_fishing && skillType != L2SkillType.PUMPING && skillType != L2SkillType.REELING && skillType != L2SkillType.FISHING)
		{
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}

		if(_observerController.isObserving())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendActionFailed();
			return false;
		}

		// Check if the caster is sitting
		if(_isSitting)
		{
			// Send a System Message to the caster
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);

			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendActionFailed();
			return false;
		}

		// Check if the skill type is TOGGLE
		if(skill.isToggle() && skill.canBeDispeled())
		{
			// Get effects of the skill
			L2Effect effect = getFirstEffect(skill.getId());

			if(effect != null)
			{
				effect.exit();

				// Send a Server->Client packet ActionFailed to the L2PcInstance
				sendActionFailed();
				return false;
			}
		}

		// Check if the player uses "Fake Death" skill
		// Note: do not check this before TOGGLE reset
		if(_isFakeDeath)
		{
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendActionFailed();
			return false;
		}

		//************************************* Check Target *******************************************
		// Create and set a L2Object containing the target of the skill
		L2Object target = null;
		L2TargetType skillTargetType = skill.getTargetType();
		Point3D worldPosition = _currentSkillWorldPosition;

		if(skillTargetType == L2TargetType.TARGET_GROUND && worldPosition == null)
		{
			_log.log(Level.INFO, "WorldPosition is null for skill: " + skill.getName() + ", player: " + getName() + '.');
			sendActionFailed();
			return false;
		}

		switch(skillTargetType)
		{
			// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_PARTY:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_PARTY_CLAN:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_AREA_SUMMON:
			case TARGET_AURA_CORPSE_MOB:
			case TARGET_SUBLIME:
			case TARGET_CORPSE_CLAN:
			case TARGET_CORPSE_COMMAND_CHANNEL:
			case TARGET_MENTOR:
				target = this;
				break;
			case TARGET_PET:
			case TARGET_SUMMON:
			case TARGET_SUMMON_AND_ME:
				target = !_summons.isEmpty() ? _summons.getFirst() : null; // TODO: СДЕЛАТЬ ПО-НОРМАЛЬНОМУ
				break;
			default:
				target = getTarget();
				break;
		}

		// Check the validity of the target
		if(target == null)
		{
			sendActionFailed();
			return false;
		}

		// skills can be used on Walls and Doors only during siege
		if(target instanceof L2DoorInstance)
		{
			boolean isCastle = ((L2DoorInstance) target).getCastle() != null && ((L2DoorInstance) target).getCastle().getCastleId() > 0 && ((L2DoorInstance) target).getCastle().getSiege().isInProgress();

			boolean isFort = ((L2DoorInstance) target).getFort() != null && ((L2DoorInstance) target).getFort().getFortId() > 0 && ((L2DoorInstance) target).getFort().getSiege().isInProgress() && !((L2DoorInstance) target).isCommanderDoor();

			if(!isCastle && !isFort && ((L2DoorInstance) target).isUnlockable() && skill.getSkillType() != L2SkillType.UNLOCK)
			{
				return false;
			}

			// разрешаем бить двери при осаде.
			if(isCastle || isFort)
			{
				return true;
			}
		}
		/** TODO: Synch isShowHp from l2j
		 * // skills can be used on Walls and Doors only during siege
		 if(target.isDoor())
		 {
		 if (((L2DoorInstance) target).getCastle() != null && ((L2DoorInstance) target).getCastle().getCastleId() > 0) // If its castle door
		 {
		 if (!((L2DoorInstance) target).getCastle().getSiege().isInProgress()) // Skills can be used on castle doors only during siege.
		 return false;
		 }
		 else if (((L2DoorInstance) target).getFort() != null && ((L2DoorInstance) target).getFort().getFortId() > 0 && !((L2DoorInstance) target).getTemplate().) // If its fort door
		 {
		 if (!((L2DoorInstance) target).getFort().getSiege().isInProgress()) // Skills can be used on fort doors only during siege.
		 return false;
		 }
		 }
		 */

		// Are the target and the player in the same duel?
		if(_isInDuel)
		{
			// Get L2PcInstance
			if(target instanceof L2Playable)
			{
				// Get L2PcInstance
				L2PcInstance cha = target.getActingPlayer();
				if(cha._duelId != _duelId)
				{
					sendMessage("You cannot do this while duelling.");
					sendActionFailed();
					return false;
				}
			}
		}

		//************************************* Check skill availability *******************************************

		// Check if this skill is enabled (ex : reuse time)
		if(isSkillDisabled(skill))
		{
			SystemMessage sm = null;

			if(_reuseTimeStampsSkills.containsKey(skill.getReuseHashCode()))
			{
				int remainingTime = (int) (_reuseTimeStampsSkills.get(skill.getReuseHashCode()).getRemaining() / 1000);
				int hours = remainingTime / 3600;
				int minutes = remainingTime % 3600 / 60;
				int seconds = remainingTime % 60;
				if(hours > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
					sm.addNumber(hours);
					sm.addNumber(minutes);
				}
				else if(minutes > 0)
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
					sm.addNumber(minutes);
				}
				else
				{
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
				}

				sm.addNumber(seconds);
			}
			else
			{
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill);
			}

			sendPacket(sm);
			return false;
		}

		//************************************* Check Consumables *******************************************

		// Check if spell consumes a Soul
		if(skill.getSoulConsumeCount() > 0)
		{
			if(_souls < skill.getSoulConsumeCount())
			{
				sendPacket(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL);
				sendActionFailed();
				return false;
			}
		}
		//************************************* Check casting conditions *******************************************

		// Check if all casting conditions are completed
		if(!skill.checkCondition(this, target, false))
		{
			// Send a Server->Client packet ActionFail to the L2PcInstance
			sendActionFailed();
			return false;
		}

		//************************************* Check Skill Type *******************************************

		// Check if this is offensive magic skill
		if(skill.isOffensive())
		{
			if(isInsidePeaceZone(this, target) && !getAccessLevel().allowPeaceAttack())
			{
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendActionFailed();
				return false;
			}

			if(_olympiadController.isParticipating() && !_olympiadController.isPlayingNow())
			{
				// if L2PcInstance is in Olympia and the match isn't already
				// start, send a Server->Client packet ActionFail
				sendActionFailed();
				return false;
			}

			if(target.getActingPlayer() != null && _siegeState != PlayerSiegeSide.NONE && isInsideZone(L2Character.ZONE_SIEGE) && target.getActingPlayer()._siegeState == _siegeState && !target.getActingPlayer().equals(this) && target.getActingPlayer()._activeSiegeId == _activeSiegeId)
			{
				sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				sendActionFailed();
				return false;
			}

			// Check if the target is attackable
			if(!target.isAttackable() && !getAccessLevel().allowPeaceAttack())
			{
				// If target is not attackable, send a Server->Client packet ActionFailed
				sendActionFailed();
				return false;
			}
			// Check for Event Mob's
			if(target instanceof L2EventMonsterInstance && ((L2EventMonsterInstance) target).eventSkillAttackBlocked())
			{
				sendActionFailed();
				return false;
			}

			// Check if a Forced ATTACK is in progress on non-attackable target
			if(!target.isAutoAttackable(this) && !forceUse)
			{
				switch(skillTargetType)
				{
					case TARGET_AURA:
					case TARGET_FRONT_AURA:
					case TARGET_BEHIND_AURA:
					case TARGET_CLAN:
					case TARGET_PARTY_CLAN:
					case TARGET_ALLY:
					case TARGET_PARTY:
					case TARGET_SELF:
					case TARGET_GROUND:
					case TARGET_AREA_SUMMON:
					case TARGET_AURA_CORPSE_MOB:
						break;
					default: // Send a Server->Client packet ActionFailed to the L2PcInstance
						sendActionFailed();
						return false;
				}
			}

			// Check if the target is in the skill cast range
			if(dontMove)
			{
				// Calculate the distance between the L2PcInstance and the target
				if(skillTargetType == L2TargetType.TARGET_GROUND)
				{
					int range = skill.getCastRange() + getTemplate().getCollisionRadius(this);
					if(!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), range, true, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);

						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendActionFailed();
						return false;
					}
				}
				if(!equals(target) && skill.getCastRange() > 0)
				{
					int range = skill.getCastRange() + getTemplate().getCollisionRadius(this);
					if(target instanceof L2Character)
					{
						range += ((L2Character) target).getTemplate().getCollisionRadius(this);
					}

					if(!isInsideRadius(target, range, true, false))
					{
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);

						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendActionFailed();
						return false;
					}
				}
			}
		}

		if(skill.getSkillType() == L2SkillType.INSTANT_JUMP)
		{
			// You cannot jump while rooted right ;)
			if(isMovementDisabled())
			{
				sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill.getId()));
				sendActionFailed();
				return false;
			}
			// And this skill cannot be used in peace zone, not even on NPCs!
			if(isInsideZone(L2Character.ZONE_PEACE))
			{
				// Sends a sys msg to client
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);

				// Send a Server->Client packet ActionFail to the L2PcInstance
				sendActionFailed();

				return false;
			}
		}
		// Check if the skill is defensive
		if(!skill.isOffensive() && target instanceof L2MonsterInstance && !forceUse && !skill.isNeutral())
		{
			// check if the target is a monster and if force attack is set.. if not then we don't want to cast.
			switch(skillTargetType)
			{
				case TARGET_PET:
				case TARGET_SUMMON:
				case TARGET_SUMMON_AND_ME:
				case TARGET_AURA:
				case TARGET_FRONT_AURA:
				case TARGET_BEHIND_AURA:
				case TARGET_CLAN:
				case TARGET_PARTY_CLAN:
				case TARGET_SELF:
				case TARGET_PARTY:
				case TARGET_ALLY:
				case TARGET_CORPSE_MOB:
				case TARGET_AREA_CORPSE_MOB:
				case TARGET_AURA_CORPSE_MOB:
				case TARGET_GROUND:
					break;
				default:
					switch(skillType)
					{
						case BEAST_FEED:
						case DELUXE_KEY_UNLOCK:
						case UNLOCK:
							break;
						default:
							sendActionFailed();
							return false;
					}
					break;
			}
		}

		// Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
		if(skillType == L2SkillType.DRAIN_SOUL)
		{
			if(!(target instanceof L2MonsterInstance))
			{
				sendPacket(SystemMessageId.INCORRECT_TARGET);
				sendActionFailed();
				return false;
			}
		}

		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch(skillTargetType)
		{
			case TARGET_PARTY:
			case TARGET_ALLY:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_CLAN:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_PARTY_CLAN:   // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case TARGET_AURA:
			case TARGET_FRONT_AURA:
			case TARGET_BEHIND_AURA:
			case TARGET_GROUND:
			case TARGET_SELF:
			case TARGET_AURA_CORPSE_MOB:
				break;
			default:
				if(!checkPvpSkill(target, skill) && !getAccessLevel().allowPeaceAttack())
				{
					sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
					sendActionFailed();
					return false;
				}
		}

		// TODO: Unhardcode skillId 844 which is the outpost construct skill
		if(skillTargetType == L2TargetType.TARGET_HOLY && !checkIfOkToCastSealOfRule(CastleManager.getInstance().getCastle(this), false, skill) || skillTargetType == L2TargetType.TARGET_FLAGPOLE && !checkIfOkToCastFlagDisplay(FortManager.getInstance().getFort(this), false, skill) || skillType == L2SkillType.SIEGEFLAG && !L2SkillSiegeFlag.checkIfOkToPlaceFlag(this, false, skill.getId() == 844) || skillType == L2SkillType.STRSIEGEASSAULT && !checkIfOkToUseStriderSiegeAssault())
		{
			sendActionFailed();
			abortCast();
			return false;
		}

		// GeoData Los Check here
		if(skill.getCastRange() > 0)
		{
			if(skillTargetType == L2TargetType.TARGET_GROUND)
			{
				if(!GeoEngine.getInstance().canSeeTarget(this, worldPosition))
				{
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendActionFailed();
					return false;
				}
			}
			if(!GeoEngine.getInstance().canSeeTarget(this, target))
			{
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendActionFailed();
				return false;
			}
		}
		// finally, after passing all conditions
		return true;
	}

	public boolean checkIfOkToUseStriderSiegeAssault()
	{
		Castle castle = CastleManager.getInstance().getCastle(this);
		Fort fort = FortManager.getInstance().getFort(this);

		if(castle == null && fort == null)
		{
			return false;
		}

		if(castle != null)
		{
			return checkIfOkToUseStriderSiegeAssault(castle);
		}
		return checkIfOkToUseStriderSiegeAssault(fort);
	}

	public boolean checkIfOkToUseStriderSiegeAssault(Castle castle)
	{
		String text = "";

		if(castle == null || castle.getCastleId() <= 0)
		{
			text = "You must be on castle ground to use strider siege assault";
		}
		else if(!castle.getSiege().isInProgress())
		{
			text = "You can only use strider siege assault during a siege.";
		}
		else if(!(getTarget() instanceof L2DoorInstance))
		{
			text = "You can only use strider siege assault on doors and walls.";
		}
		else if(!_isRidingStrider)
		{
			text = "You can only use strider siege assault when on strider.";
		}
		else
		{
			return true;
		}

		sendMessage(text);

		return false;
	}

	public boolean checkIfOkToUseStriderSiegeAssault(Fort fort)
	{
		String text = "";

		if(fort == null || fort.getFortId() <= 0)
		{
			text = "You must be on fort ground to use strider siege assault";
		}
		else if(!fort.getSiege().isInProgress())
		{
			text = "You can only use strider siege assault during a siege.";
		}
		else if(!(getTarget() instanceof L2DoorInstance))
		{
			text = "You can only use strider siege assault on doors and walls.";
		}
		else if(!_isRidingStrider)
		{
			text = "You can only use strider siege assault when on strider.";
		}
		else
		{
			return true;
		}

		sendMessage(text);

		return false;
	}

	public boolean checkIfOkToCastSealOfRule(Castle castle, boolean isCheckOnly, L2Skill skill)
	{
		if(castle == null || castle.getCastleId() <= 0)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
		else if(!castle.getArtefacts().contains(getTarget()))
		{
			sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if(!castle.getSiege().isInProgress())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
		else if(!Util.checkIfInRange(200, this, getTarget(), true))
		{
			sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
		}
		else if(castle.getSiege().getAttackerClan(_clan) == null)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
		else
		{
			if(!isCheckOnly)
			{
				sendPacket(SystemMessageId.OPPONENT_STARTED_ENGRAVING);
				castle.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING), false);
			}
			return true;
		}
		return false;
	}

	public boolean checkIfOkToCastFlagDisplay(Fort fort, boolean isCheckOnly, L2Skill skill)
	{
		if(fort == null || fort.getFortId() <= 0)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
		else if(!fort.getFlagPole().equals(getTarget()))
		{
			sendPacket(SystemMessageId.INCORRECT_TARGET);
		}
		else if(!fort.getSiege().isInProgress())
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
		else if(!Util.checkIfInRange(200, this, getTarget(), true))
		{
			sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
		}
		else if(fort.getSiege().getAttackerClan(_clan) == null)
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill));
		}
		else
		{
			if(!isCheckOnly)
			{
				fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.S1_TRYING_RAISE_FLAG), _clan.getName());
			}
			return true;
		}
		return false;
	}

	public boolean isInLooterParty(int LooterId)
	{
		L2PcInstance looter = WorldManager.getInstance().getPlayer(LooterId);

		// if L2PcInstance is in a CommandChannel
		if(isInParty() && _party.isInCommandChannel() && looter != null)
		{
			return _party.getCommandChannel().getMembers().contains(looter);
		}

		if(isInParty() && looter != null)
		{
			return _party.getMembers().contains(looter);
		}

		return false;
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 *
	 * @param target L2Object instance containing the target
	 * @param skill  L2Skill instance with the skill being casted
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object target, L2Skill skill)
	{
		return checkPvpSkill(target, skill, false);
	}

	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 *
	 * @param target      L2Object instance containing the target
	 * @param skill       L2Skill instance with the skill being casted
	 * @param srcIsSummon is L2Summon - caster?
	 * @return False if the skill is a pvpSkill and target is not a valid pvp target
	 */
	public boolean checkPvpSkill(L2Object target, L2Skill skill, boolean srcIsSummon)
	{
		if(skill == null)
		{
			return false;
		}
		/* Необходима проверка для дерева у хила и возможно еще для чего-то (у лыжи такая же проверка есть!) */
		if(target == null)
		{
			return true;
		}

		L2PcInstance targetPlayer = target.getActingPlayer();
		SkillDat skilldat = _currentSkill;
		SkillDat skilldatpet = _currentPetSkill;

		if(targetPlayer != null && !targetPlayer.equals(this) && !(_isInDuel && targetPlayer._duelId == _duelId) && !isInsideZone(ZONE_PVP) && !targetPlayer.isInsideZone(ZONE_PVP))// && !skilldat.isCtrlPressed())
		{
			if(skill.isPvpSkill())
			{
				if(_clan != null && targetPlayer._clan != null)
				{
					if(_clan.isAtWarWith(targetPlayer._clan.getClanId()) && targetPlayer._clan.isAtWarWith(_clan.getClanId()))
					{
						return true; // in clan war player can attack whites even with sleep etc.
					}
				}
				if(!targetPlayer._pvpFlagController.isFlagged() && targetPlayer._reputation >= 0)
				{
					return skill.isNoFlag();
				}
			}
			else if(skilldat != null && !skilldat.isCtrlPressed() && skill.isOffensive() && !srcIsSummon || skilldatpet != null && !skilldatpet.isCtrlPressed() && skill.isOffensive() && srcIsSummon)
			{
				if(_clan != null && targetPlayer._clan != null)
				{
					if(_clan.isAtWarWith(targetPlayer._clan.getClanId()) && targetPlayer._clan.isAtWarWith(_clan.getClanId()))
					{
						return true; // in clan war player can attack whites even without ctrl
					}
				}
				if(!targetPlayer._pvpFlagController.isFlagged() && targetPlayer._reputation >= 0)
				{
					return false;
				}
			}
			else if(skilldat != null && !skilldat.isCtrlPressed())
			{
				if(targetPlayer._pvpFlagController.isFlagged() && !targetPlayer.isInParty())
				{
					return false;
				}
				if(_clan != null && targetPlayer._clan != null)
				{
				}
			}
		}
		return true;
	}

	/**
	 * @return {@code true} if the L2PcInstance is a Mage.
	 */
	public boolean isMageClass()
	{
		return getClassId().isMage();
	}

	public boolean isMounted()
	{
		return _mountType > 0;
	}

	/**
	 * Set the type of Pet mounted (0 : none, 1 : Stridder, 2 : Wyvern) and send a Server->Client packet InventoryUpdate to the L2PcInstance.<BR><BR>
	 *
	 * @return
	 */
	public boolean checkLandingState()
	{
		// Check if char is in a no landing zone
		if(isInsideZone(ZONE_NOLANDING))
		{
			return true;
		}
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		return isInsideZone(ZONE_SIEGE) && !(_clan != null &&
			CastleManager.getInstance().getCastle(this).equals(CastleManager.getInstance().getCastleByOwner(_clan)) &&
			equals(_clan.getLeader().getPlayerInstance()));

	}

	/**
	 *
	 * @param npcId
	 * @param npcLevel
	 * @param mountType
	 * @return {@code false} if the change of mount type fails.
	 */
	public boolean setMount(int npcId, int npcLevel, int mountType)
	{
		switch(mountType)
		{
			case 0:
				setIsFlying(false);
				_isRidingStrider = false;
				break; // Dismounted
			case 1:
				_isRidingStrider = true;
				if(_isNoble)
				{
					L2Skill striderAssaultSkill = SkillTable.FrequentSkill.STRIDER_SIEGE_ASSAULT.getSkill();
					addSkill(striderAssaultSkill, false); // not saved to DB
				}
				break;
			case 2:
				setIsFlying(true);
				break; // Flying Wyvern
			case 3:
				/* not used any more*/
				break;
		}

		_mountType = mountType;
		_mountNpcId = npcId;
		_mountLevel = npcLevel;

		return true;
	}

	/**
	 * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern, 3: Wolf).
	 */
	public int getMountType()
	{
		return _mountType;
	}

	public void stopAllEffectsNotStayOnSubclassChange()
	{
		for(L2Effect effect : _effects.getAllEffects())
		{
			if(effect != null && !effect.getSkill().isStayOnSubclassChange())
			{
				effect.exit(true);
			}
		}
		updateAndBroadcastStatus(2);
	}

	/**
	 * Stop all toggle-type effects
	 */
	public void stopAllToggles()
	{
		_effects.stopAllToggles();
	}

	// baron etc

	public void stopCubics()
	{
		if(_cubics != null)
		{
			boolean removed = false;
			for(L2CubicInstance cubic : _cubics)
			{
				cubic.stopAction();
				removeCubic(cubic.getId());
				removed = true;
			}
            sendPacket(new ExUserInfoCubic(this));
            broadcastPacket(new CI(this));
		}
	}

	public void stopCubicsByOthers()
	{
		if(!_cubics.isEmpty())
		{
			Iterator<L2CubicInstance> iter = _cubics.iterator();
			L2CubicInstance cubic;
			boolean broadcast = false;
			while(iter.hasNext())
			{
				cubic = iter.next();
				if(cubic.givenByOther())
				{
					cubic.stopAction();
					cubic.cancelDisappear();
					iter.remove();
					broadcast = true;
				}
			}
			if(broadcast)
			{
                sendPacket(new ExUserInfoCubic(this));
                broadcastPacket(new CI(this));
			}
		}
	}

	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.<BR><BR>
	 */
	public void tempInventoryDisable()
	{
		_inventoryDisable = true;

		ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnable(), 1500);
	}

	/**
	 * @return {@code true} if the Inventory is disabled.
	 */
	public boolean isInventoryDisabled()
	{
		return _inventoryDisable;
	}

	public List<L2CubicInstance> getCubics()
	{
		return _cubics;
	}

	/**
	 * Add a L2CubicInstance to the L2PcInstance _cubics.
	 *
	 * @param id
	 * @param level
	 * @param cubicPower
	 * @param activationtime
	 * @param activationchance
	 * @param maxcount
	 * @param totalLifetime
	 * @param givenByOther
	 */
	public void addCubic(int id, int level, double cubicPower, int activationtime, int activationchance, int maxcount, int totalLifetime, boolean givenByOther)
	{
		synchronized(_cubics)
		{
			if(getCubic(id) != null)
			{
				_log.log(Level.WARN, "Player " + this + " already have cubic with same Id!");
				return;
			}
			L2CubicInstance cubic = new L2CubicInstance(this, id, level, (int) cubicPower, activationtime, activationchance, maxcount, totalLifetime, givenByOther);
			_cubics.add(cubic);
		}
	}

	/**
	 * @param id a L2CubicInstance from the L2PcInstance _cubics.
	 */
	public L2CubicInstance removeCubic(int id)
	{
		L2CubicInstance cubic = getCubic(id);
		if(cubic != null)
		{
			_cubics.remove(cubic);
		}
		return cubic;
	}

	/**
	 * @param id
	 * @return the L2CubicInstance corresponding to the Identifier of the L2PcInstance _cubics.
	 */
	public L2CubicInstance getCubic(int id)
	{
		for(L2CubicInstance cubic : _cubics)
		{
			if(cubic != null && cubic.getId() == id)
			{
				return cubic;
			}
		}
		return null;
	}

	/**
	 * @return {@code true} if player have cubic with defined id.
	 */
	public boolean hasCubic(int id)
	{
		return getCubic(id) != null;
	}

	/**
	 * @return the modifier corresponding to the enchant Effect of the Active Weapon (Min : 127).
	 */
	public int getEnchantEffect()
	{
		L2ItemInstance wpn = getActiveWeaponInstance();

		if(wpn == null)
		{
			return 0;
		}

		return Math.min(127, wpn.getEnchantLevel());
	}

	/**
	 * @return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.
	 */
	public L2Npc getLastFolkNPC()
	{
		return _lastFolkNpc;
	}

	/**
	 * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.<BR><BR>
	 *
	 * @param folkNpc
	 */
	public void setLastFolkNPC(L2Npc folkNpc)
	{
		_lastFolkNpc = folkNpc;
	}

	public void addAutoSoulShot(int itemId)
	{
		_activeSoulShots.add(itemId);
	}

	public boolean removeAutoSoulShot(int itemId)
	{
		return _activeSoulShots.remove(itemId);
	}

	public Set<Integer> getAutoSoulShot()
	{
		return _activeSoulShots;
	}

	/**
	 * @return заряжены-ли соски в оружие
	 */
	public boolean isSoulShotActivated()
	{
		return !_activeSoulShots.isEmpty();
	}

	public void rechargeAutoSoulShot(boolean physical, boolean magic, boolean summon)
	{
		L2ItemInstance item;
		IItemHandler handler;

		if(_activeSoulShots == null || _activeSoulShots.isEmpty())
		{
			return;
		}

		try
		{
			for(int itemId : _activeSoulShots)
			{
				item = _inventory.getItemByItemId(itemId);

				if(item != null && item.isEtcItem())
				{
					if(magic && (item.getEtcItem().isCharSpiritshot() && !summon || item.getEtcItem().isSummonSpiritshot() && summon) || physical && (item.getEtcItem().isCharSoulshot() && !summon || item.getEtcItem().isSummonSoulshot() && summon))
					{
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if(handler != null)
						{
							handler.useItem(this, item, false);
						}
					}
				}
				else
				{
					removeAutoSoulShot(itemId);
				}
			}
		}
		catch(NullPointerException npe)
		{
			_log.log(Level.ERROR, toString(), npe);
		}
	}

	/**
	 * Cancel autoshot for all shots matching crystaltype<BR>
	 * {@link L2Item#getCrystalType()}
	 *
	 * @param crystalType int type to disable
	 */
	public void disableAutoShotByCrystalType(CrystalGrade crystalType)
	{
		_activeSoulShots.stream().filter(itemId -> ItemTable.getInstance().getTemplate(itemId).getCrystalType() == crystalType).forEach(this::disableAutoShot);
	}

	/**
	 * Cancel autoshot use for shot itemId
	 *
	 * @param itemId int id to disable
	 * @return true if canceled.
	 */
	public boolean disableAutoShot(int itemId)
	{
		if(_activeSoulShots.contains(itemId))
		{
			removeAutoSoulShot(itemId);
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
			return true;
		}
		return false;
	}

	/**
	 * Cancel all autoshots for player
	 */
	public void disableAutoShotsAll()
	{
		for(int itemId : _activeSoulShots)
		{
			sendPacket(new ExAutoSoulShot(itemId, 0));
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId));
		}
		_activeSoulShots.clear();
	}

	public int getHoursInGame()
	{
		_hoursInGame++;
		return _hoursInGame;
	}

	public int getClanPrivileges()
	{
		return _clanPrivileges;
	}

	public void setClanPrivileges(int n)
	{
		_clanPrivileges = n;
	}

	// TODO: Переделать в Enum SocialClass
	public int getPledgeClass()
	{
		return _pledgeClass;
	}

	public void setPledgeClass(int classId)
	{
		_pledgeClass = classId;
		checkItemRestriction();
	}

	public int getPledgeType()
	{
		return _pledgeType;
	}

	public void setPledgeType(int typeId)
	{
		_pledgeType = typeId;
	}

	public int getApprentice()
	{
		return _apprentice;
	}

	public void setApprentice(int apprentice_id)
	{
		_apprentice = apprentice_id;
	}

	public int getSponsor()
	{
		return _sponsor;
	}

	public void setSponsor(int sponsor_id)
	{
		_sponsor = sponsor_id;
	}

	public int getBookMarkSlot()
	{
		return _bookmarkslot;
	}

	public void setBookMarkSlot(int slot)
	{
		_bookmarkslot = slot;
		sendPacket(new ExGetBookMarkInfo(this));
	}

	public int getTeleMode()
	{
		return _telemode;
	}

	public void setTeleMode(int mode)
	{
		_telemode = mode;
	}

	public void setLoto(int i, int val)
	{
		_loto[i] = val;
	}

	public int getLoto(int i)
	{
		return _loto[i];
	}

	public void setRace(int i, int val)
	{
		_race[i] = val;
	}

	public int getRace(int i)
	{
		return _race[i];
	}

	public boolean getMessageRefusal()
	{
		return _messageRefusal;
	}

	public void setMessageRefusal(boolean mode)
	{
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}

	public boolean getDietMode()
	{
		return _dietMode;
	}

	public void setDietMode(boolean mode)
	{
		_dietMode = mode;
	}

	public boolean getTradeRefusal()
	{
		return _tradeRefusal;
	}

	public void setTradeRefusal(boolean mode)
	{
		_tradeRefusal = mode;
	}

	public boolean isInDuel()
	{
		return _isInDuel;
	}

	public int getDuelId()
	{
		return _duelId;
	}

	public DuelState getDuelState()
	{
		return _duelState;
	}

	public void setDuelState(DuelState mode)
	{
		_duelState = mode;
	}

	/**
	 * Sets up the duel state using a non 0 duelId.
	 *
	 * @param duelId 0=not in a duel
	 */
	public void setIsInDuel(int duelId)
	{
		if(duelId > 0)
		{
			_isInDuel = true;
			_duelState = DuelState.DUELLING;
			_duelId = duelId;
		}
		else
		{
			if(_duelState == DuelState.DEAD)
			{
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_isInDuel = false;
			_duelState = DuelState.NODUEL;
			_duelId = 0;
		}
	}

	/**
	 * This returns a SystemMessage stating why
	 * the player is not available for duelling.
	 *
	 * @return S1_CANNOT_DUEL... message
	 */
	public SystemMessage getNoDuelReason()
	{
		SystemMessage sm = SystemMessage.getSystemMessage(_noDuelReason);
		sm.addPcName(this);
		_noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL;
		return sm;
	}

	/**
	 * Checks if this player might join / start a duel.
	 * To get the reason use getNoDuelReason() after calling this function.
	 *
	 * @return true if the player might join/start a duel.
	 */
	public boolean canDuel()
	{
		if(isInCombat() || _punishLevel == PlayerPunishLevel.JAIL)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_BATTLE;
			return false;
		}
		if(isDead() || isAlikeDead() || getCurrentHp() < getMaxHp() / 2 || getCurrentMp() < getMaxMp() / 2)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_HP_OR_MP_IS_BELOW_50_PERCENT;
			return false;
		}
		if(_isInDuel)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_ALREADY_ENGAGED_IN_A_DUEL;
			return false;
		}
		if(_olympiadController.isParticipating())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_PARTICIPATING_IN_THE_OLYMPIAD;
			return false;
		}
		if(isCursedWeaponEquipped())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_IN_A_CHAOTIC_STATE;
			return false;
		}
		if(_privateStoreType != PlayerPrivateStoreType.NONE)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE;
			return false;
		}
		if(isMounted() || isInBoat())
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_RIDING_A_BOAT_STEED_OR_STRIDER;
			return false;
		}
		if(_fishing)
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_DUEL_BECAUSE_C1_IS_CURRENTLY_FISHING;
			return false;
		}
		if(isInsideZone(ZONE_PVP) || isInsideZone(ZONE_PEACE) || isInsideZone(ZONE_SIEGE))
		{
			_noDuelReason = SystemMessageId.C1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_C1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA;
			return false;
		}
		return true;
	}

	public boolean isNoble()
	{
		return _isNoble;
	}

	public void setNoble(boolean val)
	{
		Collection<L2Skill> nobleSkillTree = SkillTreesData.getInstance().getNobleSkillTree().values();
		if(val)
		{
			for(L2Skill skill : nobleSkillTree)
			{
				addSkill(skill, false);
			}
		}
		else
		{
			nobleSkillTree.forEach(super::removeSkill);
		}
		_isNoble = val;

		sendSkillList();
        if (val && (getLevel() == ExperienceTable.getInstance().getMaxLevel()))
        {
            sendPacket(new ExAcquireAPSkillList(this));
        }
	}

	public int getLvlJoinedAcademy()
	{
		return _lvlJoinedAcademy;
	}

	public void setLvlJoinedAcademy(int lvl)
	{
		_lvlJoinedAcademy = lvl;
	}

	public boolean isAcademyMember()
	{
		return _lvlJoinedAcademy > 0;
	}

	public int getTeam()
	{
		return _team;
	}

	public void setTeam(int team)
	{
		_team = team;
		if(!_summons.isEmpty())
		{
			for(L2Summon pet : _summons)
			{
				pet.broadcastStatusUpdate();
			}
		}
	}

	public boolean isFishing()
	{
		return _fishing;
	}

	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}

	public int getAllianceWithVarkaKetra()
	{
		return _alliedVarkaKetra;
	}

	public void setAllianceWithVarkaKetra(int sideAndLvlOfAlliance)
	{
		// [-5,-1] varka, 0 neutral, [1,5] ketra
		_alliedVarkaKetra = sideAndLvlOfAlliance;
	}

	public boolean isAlliedWithVarka()
	{
		return _alliedVarkaKetra < 0;
	}

	public boolean isAlliedWithKetra()
	{
		return _alliedVarkaKetra > 0;
	}

	public void sendSkillList()
	{
		sendSkillList(0);
	}

	/**
	 * Проверяет и отправляет игроку список его умений
	 * @param addedSkillId 0 если никакого умения не было добавлено, 1 если было добавлено умение
	 */
	public void sendSkillList(int addedSkillId)
	{
		boolean isDisabled = false;
		SkillList skillList = new SkillList();

		for(L2Skill s : getAllSkills())
		{
			if(s == null)
			{
				continue;
			}
			if(s.getId() > 1565 && s.getId() < 1570)
			{
				continue; // Скиллы смены саб-классов
			}
			if(_transformation != null && !containsAllowedTransformSkill(s.getId()) && !s.allowOnTransform())
			{
				continue;
			}
			if(_clan != null)
			{
				isDisabled = s.isClanSkill() && _clan.getReputationScore() < 0;
			}

			boolean isEnchantable = SkillTable.getInstance().isEnchantable(s.getId());
			if(isEnchantable)
			{
				L2EnchantSkillLearn esl = EnchantSkillGroupsTable.getInstance().getSkillEnchantmentBySkillId(s.getId());
				if(esl != null)
				{
					// if player dont have min level to enchant
					if(s.getLevel() < esl.getBaseLevel())
					{
						isEnchantable = false;
					}
				}
				// if no enchant data
				else
				{
					isEnchantable = false;
				}
			}

			int replaceable = -1;
			if(s.isReplaceableSkills())
			{
				replaceable = s.getDisplayId();
			}

			skillList.addSkill(s.getDisplayId(), s.getLevel(), s.isPassive(), isDisabled, isEnchantable, replaceable);
		}

		// Добавляем инфу о новоизученном умении
		skillList.setNewSkillId(addedSkillId);

		sendPacket(skillList);
		sendPacket(new AcquireSkillList(this));
	}

	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>)
	 * for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only
	 * done by the calling of setActiveClass() method as that should be the only way to do so.
	 *
	 * @param classId
	 * @param classIndex
	 * @param classType
	 * @return boolean subclassAdded
	 */
	public boolean addSubClass(int classId, int classIndex, SubClassType classType)
	{
		if(!_subclassLock.tryLock())
		{
			return false;
		}
		try
		{
			if(getTotalSubClasses() == Config.MAX_SUBCLASS || classIndex == 0)
			{
				return false;
			}

			if(getSubClasses().containsKey(classIndex))
			{
				return false;
			}

			// Note: Never change _classIndex in any method other than setActiveClass().

			SubClass newClass = new SubClass();
			newClass.setClassId(classId);
			newClass.setClassIndex(classIndex);
			newClass.setClassType(classType);

			newClass.setLevel((byte) 0); // Установится минимальный урвоень

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;

			try
			{
				// Store the basic info about this new sub-class.
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Characters.SUBCLASSES_ADD);

				statement.setInt(1, getObjectId());
				statement.setInt(2, newClass.getClassId());
				statement.setLong(3, newClass.getExp());
				statement.setInt(4, newClass.getSp());
				statement.setInt(5, newClass.getLevel());
				statement.setInt(6, newClass.getClassIndex());
				statement.setInt(7, classType.ordinal());

				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not add character sub class for " + getName() + ": " + e.getMessage(), e);
				return false;
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}

			// Commit after database INSERT incase exception is thrown.
			getSubClasses().put(newClass.getClassIndex(), newClass);

			ClassId subTemplate = ClassId.getClassId(classId);
			// Берем 3 профу у дуалов
			if(subTemplate.level() >= 4)
			{
				subTemplate = subTemplate.getParent();
			}
			Map<Integer, L2SkillLearn> skillTree = SkillTreesData.getInstance().getCompleteClassSkillTree(subTemplate);
			TIntObjectHashMap<L2Skill> prevSkillList = new TIntObjectHashMap<>();

			for(L2SkillLearn skillInfo : skillTree.values())
			{
				if(skillInfo.getMinLevel() <= newClass.getLevel())
				{
					L2Skill prevSkill = prevSkillList.get(skillInfo.getSkillId());
					L2Skill newSkill = SkillTable.getInstance().getInfo(skillInfo.getSkillId(), skillInfo.getSkillLevel());

					if(prevSkill != null && prevSkill.getLevel() > newSkill.getLevel())
					{
						continue;
					}

					prevSkillList.put(newSkill.getId(), newSkill);
					skillInfo.getPrequisiteSkills().stream().filter(removable -> getSkills().containsKey(removable.getSkillId())).forEach(removable -> removeSkill(getSkills().get(removable.getSkillId()), true, true));
					storeSkill(newSkill, prevSkill, classIndex);
				}
			}

			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	/**
	 * 1. Completely erase all existance of the subClass linked to the classIndex.<BR>
	 * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR>
	 *
	 * @param classIndex
	 * @param newClassId
	 * @param newClassType
	 * @return boolean subclassAdded
	 */
	public boolean modifySubClass(int classIndex, int newClassId, SubClassType newClassType)
	{
		if(!_subclassLock.tryLock())
		{
			return false;
		}

		try
		{
			int oldClassId = getSubClasses().get(classIndex).getClassId();

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				// Remove all henna info stored for this sub-class.
				statement = con.prepareStatement(Characters.HENNAS_DELETE_ALL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				DatabaseUtils.closeStatement(statement);

				// Remove all shortcuts info stored for this sub-class.
				statement = con.prepareStatement(Characters.SHORTCUTS_DELETE);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.clearParameters();

				// Remove all effects info stored for this sub-class.
				statement = con.prepareStatement(Characters.EFFECTS_CLEAR);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.clearParameters();

				// Remove all skill info stored for this sub-class.
				statement = con.prepareStatement(Characters.SKILLS_DELETE_ALL);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
				statement.clearParameters();

				// Remove all basic info stored about this sub-class.
				statement = con.prepareStatement(Characters.SUBCLASSES_DELETE);
				statement.setInt(1, getObjectId());
				statement.setInt(2, classIndex);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not modify sub class for " + getName() + " to class index " + classIndex + ": " + e.getMessage(), e);

				// This must be done in order to maintain data consistency.
				getSubClasses().remove(classIndex);
				return false;
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}

			getSubClasses().remove(classIndex);
		}
		finally
		{
			_subclassLock.unlock();
		}

		return addSubClass(newClassId, classIndex, newClassType);
	}

	public boolean isSubClassActive()
	{
		return _classIndex > 0;
	}

	public boolean isDualClassActive()
	{
		return _classIndex > 0 && _subClasses.get(_classIndex).isDualClass();
	}

	public Map<Integer, SubClass> getSubClasses()
	{
		if(_subClasses == null)
		{
			_subClasses = new FastMap<>();
		}

		return _subClasses;
	}

	public SubClass getSubclass()
	{
		return _subClasses.get(_classIndex);
	}

	public SubClass getDualSubclass()
	{
		for(SubClass sub : _subClasses.values())
		{
			if(sub.isDualClass())
			{
				return sub;
			}
		}
		return null;
	}

	public int getTotalSubClasses()
	{
		return getSubClasses().size();
	}

	public int getBaseClassId()
	{
		return _baseClassId;
	}

	public void setBaseClassId(ClassId classId)
	{
		_baseClassId = classId.ordinal();
	}

	public int getActiveClassId()
	{
		return _activeClassId;
	}

	public int getClassIndex()
	{
		return _classIndex;
	}

	private void setClassIdTemplate(int classId)
	{
		_activeClassId = classId;

		L2PcTemplate t = ClassTemplateTable.getInstance().getTemplate(classId);

		if(t == null)
		{
			_log.log(Level.ERROR, "Missing template for classId: " + classId);
			throw new Error();
		}

		// Set the template of the L2PcInstance
		setTemplate(t);
	}

	/**
	 * Changes the character's class based on the given class index.
	 * <BR><BR>
	 * An index of zero specifies the character's original (base) class,
	 * while indexes 1-3 specifies the character's sub-classes respectively.
	 * <br><br>
	 * <font color="00FF00"/>WARNING: Use only on subclase change</font>
	 *
	 * @param classIndex
	 * @return
	 */
	public boolean setActiveClass(int classIndex)
	{
		if(!_subclassLock.tryLock())
		{
			return false;
		}

		try
		{
			// Cannot switch or change subclasses while transformed
			if(_transformation != null)
			{
				return false;
			}

			// Remove active item skills before saving char to database
			// because next time when choosing this class, weared items can
			// be different
			for(L2ItemInstance item : _inventory.getAugmentedItems())
			{
				if(item != null && item.isEquipped())
				{
					item.getAugmentation().removeBonus(this);
				}
			}

			// abort any kind of cast.
			abortCast();

			// Stop casting for any player that may be casting a force buff on
			// this l2pcinstance.
			getKnownList().getKnownCharacters().stream().filter(character -> character.getFusionSkill() != null && character.getFusionSkill().getTarget().equals(this)).forEach(L2Character::abortCast);

			/*
			* 1. Call store() before modifying _classIndex to avoid skill
			* effects rollover.
			* 2. Register the correct _classId against applied 'classIndex'.
			*/
			store(Config.SUBCLASS_STORE_SKILL_COOLTIME);
			_reuseTimeStampsSkills.clear();

			// clear charges
			_charges.set(0);
			stopChargeTask();

			if(!_summons.isEmpty())
			{
				for(L2Summon pet : _summons)
				{
					pet.getLocationController().decay();
				}
			}

			if(classIndex == 0)
			{
				setClassIdTemplate(_baseClassId);
			}
			else
			{
				try
				{
					setClassIdTemplate(getSubClasses().get(classIndex).getClassId());
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Could not switch " + getName() + "'s sub class to class index " + classIndex + ": " + e.getMessage(), e);
					return false;
				}
			}
			_classIndex = classIndex;

			if(isInParty())
			{
				_party.recalculatePartyLevel();
			}

			/*
			 * Update the character's change in class status.
			 *
			 * 1. Remove any active cubics from the player.
			 * 2. Renovate the characters table in the database with the new class info, storing also buff/effect data.
			 * 3. Remove all existing skills.
			 * 4. Restore all the learned skills for the current class from the database.
			 * 5. Restore effect/buff data for the new class.
			 * 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
			 * 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
			 * 8. Restore shortcut data related to this class.
			 * 9. Resend a class change animation effect to broadcast to all nearby players.
			 */

			getAllSkills().forEach(super::removeSkill);

			//stopAllEffectsExceptThoseThatLastThroughDeath();
			stopAllEffectsNotStayOnSubclassChange();
			stopCubics();

			_recipeBookController.restoreBook(false);

			restoreSkills();
			rewardSkills();
			regiveTemporarySkills();

			// Prevents some issues when changing between subclases that shares skills
			if(_disabledSkills != null && !_disabledSkills.isEmpty())
			{
				_disabledSkills.clear();
			}

			restoreEffects();
			updateEffectIcons();
			sendPacket(new EtcStatusUpdate(this));

			for(int i = 0; i < 3; i++)
			{
				_henna[i] = null;
			}

			restoreHenna();
			sendPacket(new HennaInfo(this));

			if(getCurrentHp() > getMaxHp())
			{
				setCurrentHp(getMaxHp());
			}
			if(getCurrentMp() > getMaxMp())
			{
				setCurrentMp(getMaxMp());
			}
			if(getCurrentCp() > getMaxCp())
			{
				setCurrentCp(getMaxCp());
			}

			refreshOverloaded();
			refreshExpertisePenalty();
			broadcastUserInfo();

			if(_clan != null)
			{
				broadcastClanMemberInfo();
			}

			sendPacket(new ExSubjobInfo(this));

			// Clear resurrect xp calculation
			_expBeforeDeath = 0;

			_shortcutController.restore();
			sendPacket(new ShortCutInit(this));

			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
			sendPacket(new SkillCoolTime(this));
			sendPacket(new ExStorageMaxCount(this));
			// Необходимо для обновления слотов талисманов!
			sendPacket(new ItemList(this, false));

			return true;
		}
		finally
		{
			_subclassLock.unlock();
		}
	}

	public boolean isLocked()
	{
		return _subclassLock.isLocked();
	}

	public void stopWarnUserTakeBreak()
	{
		if(_taskWarnUserTakeBreak != null)
		{
			_hoursInGame = 0;
			_taskWarnUserTakeBreak.cancel(true);
			_taskWarnUserTakeBreak = null;
		}
	}

	public void startWarnUserTakeBreak()
	{
		if(_taskWarnUserTakeBreak == null)
		{
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreak(), 7200000, 7200000);
		}
	}

    public void stopWarnChatTask()
    {
        if(_taskWarnChatTask != null)
        {
            _taskWarnChatTask.cancel(true);
            _taskWarnChatTask = null;
        }
    }
    
    public void startWarnChatTask()
    {
        if (_taskWarnChatTask == null)
        {
            _taskWarnChatTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(new WarnChatTask(), 1800000, 1800000);
        }
    }

	public void stopWaterTask()
	{
		if(_taskWater != null)
		{
			_taskWater.cancel(false);

			_taskWater = null;
			sendPacket(new SetupGauge(SetupGauge.BLUE_MINI, 0));
		}
	}

	public void startWaterTask()
	{
		if(!isDead() && _taskWater == null)
		{
			int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);

			sendPacket(new SetupGauge(SetupGauge.BLUE_MINI, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(), timeinwater, 1000);
		}
	}

	public boolean isInWater()
	{
		return _taskWater != null;
	}

	public void checkWaterState()
	{
		if(isInsideZone(ZONE_WATER))
		{
			startWaterTask();
		}
		else
		{
			stopWaterTask();
		}
	}

	public void onPlayerEnter()
	{
		startWarnUserTakeBreak();

		startWarnChatTask();

		// jail task
		updatePunishState();

		if(isGM())
		{
			if(isInvul())
			{
				sendMessage("Вход в режиме Неуязвимости.");
			}
			if(_appearance.getInvisible())
			{
				sendMessage("Вход в режиме Невидимости.");
			}
			if(_silenceMode)
			{
				sendMessage("Вход в режиме Тишины.");
			}
		}

		revalidateZone(true);

		notifyFriends();
		if(!isGM() && Config.DECREASE_SKILL_LEVEL)
		{
			checkPlayerSkills();
		}
	}

	public long getLastAccess()
	{
		return _lastAccess;
	}

	/**
	 * Запрос на воскрешение
	 * ЗАМЕТКА: при запросе на рес питомца приходит его ObjectID, иначе -1
	 * Сделано, т.к. сейчас питомцев несколько и нужно знать какого именно воскрешать
	 *
	 * @param reviver воскрешающий персонаж
	 * @param skill   скилл, которым воскрешают игрока\пета
	 * @param Pet     если больше -1, то воскрешаем питомца (приходит его ObjectId)
	 */
	public void reviveRequest(L2PcInstance reviver, L2Skill skill, int Pet)
	{
		if(isResurrectionBlocked())
		{
			return;
		}

		if(_reviveRequested == 1)
		{
			if(_revivePet)
			{
				reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			}
			else
			{
				if(Pet > -1)
				{
					reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
				}
				else
				{
					reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
				}
			}
			return;
		}
		// Если воскрешаем пета
		if(Pet > -1 && !_summons.isEmpty())
		{
			L2Summon pet = null;
			for(L2Summon tempPet : _summons)
			{
				if(tempPet != null && tempPet.getObjectId() == Pet)
				{
					pet = tempPet;
				}
			}
			if(pet != null && pet.isDead())
			{
				_reviveRequested = 1;
				int restoreExp = 0;
				if(isPhoenixBlessed())
				{
					_revivePower = 100;
				}
				else
				{
					_revivePower = isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE) ? 0 : Ressurection.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
				}

				restoreExp = (int) Math.round((_expBeforeDeath - getExp()) * _revivePower / 100);

				_revivePet = true;
				_petToRevive = Pet;

				if(isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE))
				{
					ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId());
					dlg.addTime(60000);
					sendPacket(dlg);
					return;
				}

				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					reviver.updateWorldStatistic(CategoryType.RESURRECTED_CHAR_COUNT, null, 1);
				}

				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_S3_XP.getId());
				dlg.addPcName(reviver);
				dlg.addString(Integer.toString(restoreExp));
				dlg.addString(Integer.toString((int) _revivePower));
				sendPacket(dlg);
			}
		}
		// Если воскрешают игрока
		else if(isDead())
		{
			_reviveRequested = 1;
			int restoreExp = 0;
			if(isPhoenixBlessed())
			{
				_revivePower = 100;
			}
			else
			{
				_revivePower = isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE) ? 0 : Ressurection.calculateSkillResurrectRestorePercent(skill.getPower(), reviver);
			}

			restoreExp = (int) Math.round((_expBeforeDeath - getExp()) * _revivePower / 100);

			_revivePet = false;

			if(isAffected(CharEffectList.EFFECT_FLAG_CHARM_OF_COURAGE))
			{
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId());
				dlg.addTime(60000);
				sendPacket(dlg);
				return;
			}

			if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
			{
				updateWorldStatistic(CategoryType.RESURRECTED_CHAR_COUNT, null, 1);
			}

			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_S3_XP.getId());
			dlg.addPcName(reviver);
			dlg.addString(String.valueOf(restoreExp));
			sendPacket(dlg);
		}
	}

	/**
	 * Ответ на запрос о воскрешении
	 * TODO: Приведи меня в порядок!
	 *
	 * @param answer
	 */
	public void reviveAnswer(int answer)
	{
		if(_reviveRequested != 1 || !isDead() && !_revivePet)
		{
			return;
		}

		L2Summon petToRevive = null;
		boolean petDead = true;

		if(_revivePet && !_summons.isEmpty())
		{
			for(L2Summon pet : _summons)
			{
				if(pet.getObjectId() == _petToRevive)
				{
					if(pet.isDead())
					{
						petToRevive = pet;
					}
					else
					{
						petDead = false;
					}
				}
			}
		}
		if(!petDead)
		{
			return;
		}

		if(answer == 0 && isPhoenixBlessed())
		{
			stopPhoenixBlessing(null);
			stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		if(answer == 1)
		{
			if(!_revivePet)
			{
				if(_revivePower == 0)
				{
					doRevive();
				}
				else
				{
					doRevive(_revivePower);
				}
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					updateWorldStatistic(CategoryType.RESURRECTED_BY_OTHER_COUNT, null, 1);
				}
			}
			else if(!_summons.isEmpty() && petToRevive != null && petToRevive.isDead())
			{
				if(_revivePower == 0)
				{
					petToRevive.doRevive();
				}
				else
				{
					petToRevive.doRevive(_revivePower);
				}
			}
		}
		_reviveRequested = 0;
		_revivePower = 0;
		_petToRevive = 0;
	}

	public boolean isReviveRequested()
	{
		return _reviveRequested == 1;
	}

	public boolean isRevivingPet()
	{
		return _revivePet;
	}

	public void removeReviving()
	{
		_reviveRequested = 0;
		_revivePower = 0;
	}

	public void onActionRequest()
	{
		if(isSpawnProtected())
		{
			sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
			CharSummonTable.getInstance().restoreSummon(this, 0);
			CharSummonTable.getInstance().restorePet(this);
			setSpawnProtection(false);
		}

		if(isTeleportProtected())
		{
			sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
			setTeleportProtection(false);
		}

		_idleFromTime = System.currentTimeMillis();
	}

	/**
	 * Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)
	 *
	 * @return int Expertise skill level.
	 */
	public int getExpertiseLevel()
	{
		int level = getSkillLevel(239);
		if(level < 0)
		{
			level = 0;
		}
		return level;
	}

	public void setIsTeleporting(boolean teleport, boolean useWatchDog)
	{
		super.setIsTeleporting(teleport);
		if(!useWatchDog)
		{
			return;
		}
		if(teleport)
		{
			if(_teleportWatchdog == null && Config.TELEPORT_WATCHDOG_TIMEOUT > 0)
			{
				synchronized(this)
				{
					if(_teleportWatchdog == null)
					{
						_teleportWatchdog = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportWatchdog(), Config.TELEPORT_WATCHDOG_TIMEOUT * 1000);
					}
				}
			}
		}
		else
		{
			if(_teleportWatchdog != null)
			{
				_teleportWatchdog.cancel(false);
				// ThreadPoolManager.getInstance().removeGeneral((Runnable)_teleportWatchdog);
				_teleportWatchdog = null;
			}
		}
	}

	/**
	 * Compares the stored with the currect position and update if needed.
	 *
	 * @param force Force update the position.
	 */
	public void updatePartyPosition(boolean force)
	{
		L2Party party = _party;
		if(party == null)
		{
			return;
		}

		int x = getX();
		int y = getY();
		int z = getZ();
		int tick = GameTimeController.getInstance().getGameTicks();

		if(force || tick - _lastPartyPositionTick > 10 && (Math.abs(x - _lastPartyPositionX) > 64 || Math.abs(y - _lastPartyPositionY) > 64 || Math.abs(z - _lastPartyPositionZ) > 64))
		{
			_lastPartyPositionTick = tick;
			_lastPartyPositionX = x;
			_lastPartyPositionY = y;
			_lastPartyPositionZ = z;
			party.broadcastPacket(this, new PartyMemberPosition(party));
		}
	}

	public void setLastServerPosition(int x, int y, int z)
	{
		_lastServerPosition.setXYZ(x, y, z);
	}

	public Point3D getLastServerPosition()
	{
		return _lastServerPosition;
	}

	public boolean checkLastServerPosition(int x, int y, int z)
	{
		return _lastServerPosition.equals(x, y, z);
	}

	public int getLastServerDistance(int x, int y, int z)
	{
		double dx = x - _lastServerPosition.getX();
		double dy = y - _lastServerPosition.getY();
		double dz = z - _lastServerPosition.getZ();

		return (int) Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	public void addExpAndSp(long addToExp, int addToSp, boolean useVitality)
	{
		getStat().addExpAndSp(addToExp, addToSp, useVitality);
	}

	public void removeExpAndSp(long removeExp, int removeSp)
	{
		getStat().removeExpAndSp(removeExp, removeSp, true);
	}

	public void removeExpAndSp(long removeExp, int removeSp, boolean sendMessage)
	{
		getStat().removeExpAndSp(removeExp, removeSp, sendMessage);
	}

	public void broadcastSnoop(ChatType type, String name, String _text)
	{
		if(!_snoopListener.isEmpty())
		{
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);

			_snoopListener.stream().filter(pci -> pci != null).forEach(pci -> pci.sendPacket(sn));
		}
	}

	public void addSnooper(L2PcInstance pci)
	{
		if(!_snoopListener.contains(pci))
		{
			_snoopListener.add(pci);
		}
	}

	public void removeSnooper(L2PcInstance pci)
	{
		_snoopListener.remove(pci);
	}

	public void addSnooped(L2PcInstance pci)
	{
		if(!_snoopedPlayer.contains(pci))
		{
			_snoopedPlayer.add(pci);
		}
	}

	public void removeSnooped(L2PcInstance pci)
	{
		_snoopedPlayer.remove(pci);
	}

	/*
		Добавляем байпас при текущией проверки html
	 */
	public void addBypass(String bypass)
	{
		if(bypass == null)
		{
			return;
		}
		_validBypass.add(bypass);
	}

	/*
		Добавляем байпасы для проверки при следующей html
	 */
	public void addBypassCopy(String bypass)
	{
		if(bypass == null)
		{
			return;
		}
		_validBypassCopy.add(bypass);
	}

	public void addBypass2(String bypass)
	{
		if(bypass == null)
		{
			return;
		}
		_validBypass2.add(bypass);
	}

	public boolean validateBypass(String cmd)
	{
		if(!Config.BYPASS_VALIDATION)
		{
			return true;
		}

		for(String bp : _validBypass)
		{
			if(bp == null)
			{
				continue;
			}

			if(bp.equals(cmd))
			{
				return true;
			}
			// защита от пробелов в запросе ( поподаются такие )
			if(bp.replaceAll(" ", "").equals(cmd))
			{
				return true;
			}
		}

		for(String bp : _validBypass2)
		{
			if(bp == null)
			{
				continue;
			}

			if(cmd.startsWith(bp))
			{
				return true;
			}
		}

		_log.log(Level.WARN, "[L2PcInstance] player [" + getName() + "] sent invalid bypass '" + cmd + "'.");
		return false;
	}

	/**
	 * Performs following tests:<br>
	 * <li> Inventory contains item
	 * <li> Item owner id == this.owner id
	 * <li> It isnt pet control item while mounting pet or pet summoned
	 * <li> It isnt active enchant item
	 * <li> It isnt cursed weapon/item
	 * <li> It isnt wear item
	 * <br>
	 *
	 * @param objectId item object id
	 * @param action   just for login porpouse
	 * @return
	 */
	public boolean validateItemManipulation(int objectId, String action)
	{
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);

		if(item == null || item.getOwnerId() != getObjectId())
		{
			_log.log(Level.INFO, getObjectId() + ": player tried to " + action + " item he is not owner of");
			return false;
		}

		// buggle from strider you're mounting
		if(_mountObjectID == objectId)
		{
			return false;
		}

		// Pet is summoned and not the item that summoned the pet
		if(!_summons.isEmpty())
		{
			boolean isPetControlItem = false;
			for(L2Summon pet : _summons)
			{
				if(pet.getObjectId() == objectId)
				{
					isPetControlItem = true;
				}
			}
			if(isPetControlItem)
			{
				return false;
			}
		}

		if(_activeEnchantItem != null && _activeEnchantItem.getObjectId() == objectId)
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, getObjectId() + ":player tried to " + action + " an enchant scroll he was using");
			}

			return false;
		}

		return !CursedWeaponsManager.getInstance().isCursed(item.getItemId());

	}

	public void clearBypass()
	{
		_validBypass.clear();
		_validBypass.addAll(_validBypassCopy);
		_validBypassCopy.clear();
		_validBypass2.clear();
	}

	/**
	 * @return is inBoat.
	 */
	public boolean isInBoat()
	{
		return _vehicle != null && _vehicle.isBoat();
	}

	/**
	 * @return
	 */
	public L2BoatInstance getBoat()
	{
		return (L2BoatInstance) _vehicle;
	}

	/**
	 * @return the inAirShip.
	 */
	public boolean isInAirShip()
	{
		return _vehicle != null && _vehicle.isAirShip();
	}

	/**
	 * @return Находится ли игрок в лифте
	 */
	public boolean isInShuttle()
	{
		return _vehicle != null && _vehicle.isShuttle();
	}

	/**
	 * @return
	 */
	public L2AirShipInstance getAirShip()
	{
		return (L2AirShipInstance) _vehicle;
	}

	public L2ShuttleInstance getShuttle()
	{
		return (L2ShuttleInstance) _vehicle;
	}

	public L2Vehicle getVehicle()
	{
		return _vehicle;
	}

	public void setVehicle(L2Vehicle v)
	{
		if(v == null && _vehicle != null)
		{
			_vehicle.removePassenger(this);
		}

		_vehicle = v;
	}

	public boolean isInVehicle()
	{
		return _vehicle != null;
	}

	public boolean isInCrystallize()
	{
		return _inCrystallize;
	}

	public void setInCrystallize(boolean inCrystallize)
	{
		_inCrystallize = inCrystallize;
	}

	/**
	 * @return
	 */
	public Point3D getInVehiclePosition()
	{
		return _inVehiclePosition;
	}

	public void setInVehiclePosition(Point3D pt)
	{
		_inVehiclePosition = pt;
	}

	private void cleanup()
	{
		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try
		{
			if(!_isOnline)
			{
				_log.log(Level.ERROR, "deleteMe() called on offline character " + this);
			}
			setOnlineStatus(false, true);
		}
		catch(Exception ignored)
		{
		}

		try
		{
			if(ConfigEvents.ENABLE_BLOCK_CHECKER_EVENT && _eventController.isInHandysBlockCheckerEventArena())
			{
				HandysBlockCheckerManager.getInstance().onDisconnect(this);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		try
		{
			_isOnline = false;
			abortAttack();
			abortCast();
			stopMove(null);
			setDebug(null);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// remove combat flag
		try
		{
			if(_inventory.getItemByItemId(9819) != null)
			{
				Fort fort = FortManager.getInstance().getFort(this);
				if(fort != null)
				{
					FortSiegeManager.getInstance().dropCombatFlag(this, fort.getFortId());
				}
				else
				{
					long slot = _inventory.getSlotFromItem(_inventory.getItemByItemId(9819));
					_inventory.unEquipItemInBodySlot(slot);
					destroyItem(ProcessType.COMBATFLAG, _inventory.getItemByItemId(9819), null, true);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		try
		{
			PartyMatchWaitingList.getInstance().removePlayer(this);
			if(_partyRoomId != 0)
			{
				PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyRoomId);
				if(room != null)
				{
					room.deleteMember(this);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		try
		{
			if(isFlying())
			{
				removeSkill(SkillTable.getInstance().getInfo(4289, 1));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// Recommendations must be saved before task (timer) is canceled
		try
		{
			storeRecommendations();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try
		{
			stopAllTimers();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		try
		{
			setIsTeleporting(false);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// Cancel Attak or Cast
		try
		{
			setTarget(null);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		try
		{
			if(_fusionSkill != null)
			{
				abortCast();
			}

			getKnownList().getKnownCharacters().stream().filter(character -> character != null && character.getFusionSkill() != null && character.getFusionSkill().getTarget().equals(this)).forEach(L2Character::abortCast);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		try
		{
			for(L2Effect effect : getAllEffects())
			{
				if(effect == null)
				{
					continue;
				}

				if(effect.getSkill().isToggle() && !effect.getSkill().isForceStorable())
				{
					effect.exit();
					continue;
				}

				switch(effect.getEffectType())
				{
					case SIGNET_GROUND:
					case SIGNET_EFFECT:
						effect.exit();
						break;
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// Remove from world regions zones
		L2WorldRegion oldRegion = getLocationController().getWorldRegion();

		if(oldRegion != null)
		{
			oldRegion.removeFromZones(this);
		}
		// Remove the L2PcInstance from the world
		try
		{
			getLocationController().decay();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// If a Party is in progress, leave it (and festival party)
		if(isInParty())
		{
			try
			{
				leaveParty();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "deleteMe()", e);
			}
		}

		// If the L2PcInstance has Pet, unsummon it
		if(!_summons.isEmpty())
		{
			try
			{
				for(L2Summon pet : _summons)
				{
					pet.setRestoreSummon(true);
					pet.getLocationController().decay();

					// dead pet wasnt unsummoned, broadcast npcinfo changes (pet
					// will be without owner name - means owner offline)
					if(pet != null)
					{
						pet.broadcastNpcInfo(0);
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "deleteMe()", e);
			}// returns pet to control item
		}

		if(_clan != null)
		{
			// set the status for pledge member list to OFFLINE
			try
			{
				L2ClanMember clanMember = _clan.getClanMember(getObjectId());
				if(clanMember != null)
				{
					clanMember.setPlayerInstance(null);
				}

			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "deleteMe()", e);
			}
		}

		if(getActiveRequester() != null)
		{
			// deals with sudden exit in the middle of transaction
			_activeRequester = null;
			cancelActiveTrade();
		}

		// If the L2PcInstance is a GM, remove it from the GM List
		if(isGM())
		{
			try
			{
				AdminTable.getInstance().deleteGm(this);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "deleteMe()", e);
			}
		}

		try
		{
			// Check if the L2PcInstance is in observer mode to set its position
			// to its position
			// before entering in observer mode
			if(_observerController.isObserving())
			{
				setXYZ(_lastObserverPositionX, _lastObserverPositionY, _lastObserverPositionZ, false);
			}

			if(_vehicle != null)
			{
				_vehicle.oustPlayer(this);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// remove player from instance and set spawn location if any
		try
		{
			int instanceId = getInstanceId();
			if(instanceId != 0 && !Config.RESTORE_PLAYER_INSTANCE)
			{
				Instance inst = InstanceManager.getInstance().getInstance(instanceId);
				if(inst != null)
				{
					inst.removePlayer(getObjectId());
					Location spawn = inst.getReturnLoc();
					if(spawn != null && spawn.getX() != 0 && spawn.getY() != 0 && spawn.getZ() != 0)
					{
						setXYZ(spawn, false);
						if(!_summons.isEmpty()) // dead pet
						{
							for(L2Summon pet : _summons)
							{
								pet.teleToInstance(spawn, 0);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// TvT Event removal
		try
		{
			EventManager.onLogout(this);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// Update database with items in its inventory and remove them from the
		// world
		try
		{
			_inventory.deleteMe();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		// Update database with items in its warehouse and remove them from the world
		try
		{
			clearWarehouse();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}
		if(Config.WAREHOUSE_CACHE)
		{
			WarehouseCacheManager.getInstance().remCacheTask(this);
		}

		try
		{
			_freight.deleteMe();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "getFreight().deleteMe()", e);
		}

		try
		{
			clearRefund();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		if(isCursedWeaponEquipped())
		{
			try
			{
				CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "deleteMe()", e);
			}
		}

		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try
		{
			getKnownList().removeAllKnownObjects();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "deleteMe()", e);
		}

		if(_clanId > 0)
		{
			_clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
		}
		//ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));

		for(L2PcInstance player : _snoopedPlayer)
		{
			player.removeSnooper(this);
		}

		for(L2PcInstance player : _snoopListener)
		{
			player.removeSnooped(this);
		}

		// Remove L2Object object from _allObjects of L2World
		WorldManager.getInstance().removeObject(this);
		WorldManager.getInstance().removeFromAllPlayers(this); // force remove in case of crash during teleport

		// update bbs
		try
		{
			RegionBBSManager.getInstance().changeCommunityBoard();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on deleteMe() changeCommunityBoard: " + e.getMessage(), e);
		}

		// Если игрок состоит в листе ожидания на автопоиск группы - удаляем его оттуда
		if(_isInPartyWaitingList)
		{
			PartySearchingManager.getInstance().deleteFromWaitingList(this, false);
		}

		try
		{
			notifyFriends();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on deleteMe() notifyFriends: " + e.getMessage(), e);
		}
	}

	/*  startFishing() was stripped of any pre-fishing related checks, namely the fishing zone check.
	 * Also worthy of note is the fact the code to find the hook landing position was also striped. The
	 * stripped code was moved into fishing.java. In my opinion it makes more sense for it to be there
	 * since all other skill related checks were also there. Last but not least, moving the zone check
	 * there, fixed a bug where baits would always be consumed no matter if fishing actualy took place.
	 * startFishing() now takes up 3 arguments, wich are acurately described as being the hook landing
	 * coordinates.
	 */
	public void startFishing(int _x, int _y, int _z)
	{
		stopMove(null);
		setIsImmobilized(true);
		_fishing = true;
		_fishx = _x;
		_fishy = _y;
		_fishz = _z;

		//Starts fishing
		int lvl = getRandomFishLvl();
		int grade = getRandomFishGrade();
		int group = getRandomFishGroup(grade);
		List<L2Fish> fishs = FishData.getInstance().getFish(lvl, group, grade);
		if(fishs == null || fishs.isEmpty())
		{
			sendMessage("Error - Fishes are not definied");
			endFishing(false);
			return;
		}
		int check = Rnd.get(fishs.size());
		// Use a copy constructor else the fish data may be over-written below
		_fish = fishs.get(check).clone();
		fishs.clear();
		fishs = null;
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		if(!GameTimeController.getInstance().isNight() && _lure.isNightLure())
		{
			_fish.setFishGroup(-1);
		}
		broadcastPacket(new ExFishingStart(this, _fish.getFishGroup(), _x, _y, _z, _lure.isNightLure()));
		sendPacket(new PlaySound(1, "SF_P_01", 0, 0, 0, 0, 0));
		startLookingForFishTask();
	}

	public void stopLookingForFishTask()
	{
		if(_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}

	public void startLookingForFishTask()
	{
		if(!isDead() && _taskforfish == null)
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;

			if(_lure != null)
			{
				int lureid = _lure.getItemId();
				isNoob = _fish.getFishGrade() == 0;
				isUpperGrade = _fish.getFishGrade() == 2;
				if(lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511) //low grade
				{
					checkDelay = _fish.getGutsCheckTime() * 133;
				}
				else if(lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 || lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 || lureid >= 8484 && lureid <= 8486) //medium grade, beginner, prize-winning & quest special bait
				{
					checkDelay = _fish.getGutsCheckTime() * 100;
				}
				else if(lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513) //high grade
				{
					checkDelay = _fish.getGutsCheckTime() * 66;
				}
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(_fish.getStartCombatTime(), _fish.getFishGuts(), _fish.getFishGroup(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}

	private int getRandomFishGrade()
	{
		switch(_lure.getItemId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			default:
				return 1;
		}
	}

	private int getRandomFishGroup(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch(group)
		{
			case 0: // fish for novices
				switch(_lure.getItemId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if(check <= 54)
						{
							type = 5;
						}
						else
						{
							type = check <= 77 ? 4 : 6;
						}
						break;
					case 7808: // purple lure, preferred by fat fish (type 4)
						if(check <= 54)
						{
							type = 4;
						}
						else
						{
							type = check <= 77 ? 6 : 5;
						}
						break;
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if(check <= 54)
						{
							type = 6;
						}
						else
						{
							type = check <= 77 ? 5 : 4;
						}
						break;
					case 8486: // prize-winning fishing lure for beginners
						if(check <= 33)
						{
							type = 4;
						}
						else
						{
							type = check <= 66 ? 5 : 6;
						}
						break;
				}
				break;
			case 1: // normal fish
				switch(_lure.getItemId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if(check <= 54)
						{
							type = 1;
						}
						else if(check <= 74)
						{
							type = 0;
						}
						else
						{
							type = check <= 94 ? 2 : 3;
						}
						break;
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if(check <= 54)
						{
							type = 0;
						}
						else if(check <= 74)
						{
							type = 1;
						}
						else
						{
							type = check <= 94 ? 2 : 3;
						}
						break;
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if(check <= 55)
						{
							type = 2;
						}
						else if(check <= 74)
						{
							type = 1;
						}
						else
						{
							type = check <= 94 ? 0 : 3;
						}
						break;
					case 8484: // prize-winning fishing lure
						if(check <= 33)
						{
							type = 0;
						}
						else
						{
							type = check <= 66 ? 1 : 2;
						}
						break;
				}
				break;
			case 2: // upper grade fish, luminous lure
				switch(_lure.getItemId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if(check <= 54)
						{
							type = 8;
						}
						else
						{
							type = check <= 77 ? 7 : 9;
						}
						break;
					case 8509: // purple lure, preferred by fat fish (type 7)
						if(check <= 54)
						{
							type = 7;
						}
						else
						{
							type = check <= 77 ? 9 : 8;
						}
						break;
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if(check <= 54)
						{
							type = 9;
						}
						else
						{
							type = check <= 77 ? 8 : 7;
						}
						break;
					case 8485: // prize-winning fishing lure
						if(check <= 33)
						{
							type = 7;
						}
						else
						{
							type = check <= 66 ? 8 : 9;
						}
						break;
				}
		}
		return type;
	}

	private int getRandomFishLvl()
	{
		int skilllvl = getSkillLevel(1315);
		L2Effect e = getFirstEffect(2274);
		if(e != null)
		{
			skilllvl = (int) e.getSkill().getPower();
		}
		if(skilllvl <= 0)
		{
			return 1;
		}
		int randomlvl;
		int check = Rnd.get(100);

		if(check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if(check <= 85)
		{
			randomlvl = skilllvl - 1;
			if(randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if(randomlvl > 27)
			{
				randomlvl = 27;
			}
		}

		return randomlvl;
	}

	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}

	public void endFishing(boolean win)
	{
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;

		if(_fishCombat == null)
		{
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		}
		_fishCombat = null;
		_lure = null;
		// Ends fishing
		broadcastPacket(new ExFishingEnd(win, this));
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
	}

	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}

	public int getFishx()
	{
		return _fishx;
	}

	public int getFishy()
	{
		return _fishy;
	}

	public int getFishz()
	{
		return _fishz;
	}

	public L2ItemInstance getLure()
	{
		return _lure;
	}

	public void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}

	public int getInventoryLimit()
	{
		int ivlim;
		if(isGM())
		{
			ivlim = Config.INVENTORY_MAXIMUM_GM;
		}
		else
		{
			ivlim = getRace() == Race.Dwarf ? Config.INVENTORY_MAXIMUM_DWARF : Config.INVENTORY_MAXIMUM_NO_DWARF;
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);

		return ivlim;
	}

	public int getWareHouseLimit()
	{
		int whlim;
		whlim = getRace() == Race.Dwarf ? Config.WAREHOUSE_SLOTS_DWARF : Config.WAREHOUSE_SLOTS_NO_DWARF;

		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);

		return whlim;
	}

	public int getPrivateSellStoreLimit()
	{
		int pslim;

		pslim = getRace() == Race.Dwarf ? Config.MAX_PVTSTORESELL_SLOTS_DWARF : Config.MAX_PVTSTORESELL_SLOTS_OTHER;

		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);

		return pslim;
	}

	public int getPrivateBuyStoreLimit()
	{
		int pblim;

		pblim = getRace() == Race.Dwarf ? Config.MAX_PVTSTOREBUY_SLOTS_DWARF : Config.MAX_PVTSTOREBUY_SLOTS_OTHER;
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);

		return pblim;
	}

	/**
	 * @return Returns the mountNpcId.
	 */
	public int getMountNpcId()
	{
		return _mountNpcId;
	}

	/**
	 * @return Returns the mountLevel.
	 */
	public int getMountLevel()
	{
		return _mountLevel;
	}

	public int getMountObjectID()
	{
		return _mountObjectID;
	}

	public void setMountObjectID(int newID)
	{
		_mountObjectID = newID;
	}

	/**
	 * @return the current skill in use or return null.
	 */
	public SkillDat getCurrentSkill()
	{
		return _currentSkill;
	}

	/**
	 * Create a new SkillDat object and set the player _currentSkill.<BR><BR>
	 *
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if(currentSkill == null)
		{
			_currentSkill = null;
			return;
		}
		_currentSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}

	/**
	 * @return the current pet skill in use or return null.
	 */
	public SkillDat getCurrentPetSkill()
	{
		return _currentPetSkill;
	}

	/**
	 * Create a new SkillDat object and set the player _currentPetSkill.<br><br>
	 *
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentPetSkill(L2Skill currentSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if(currentSkill == null)
		{
			_currentPetSkill = null;
			return;
		}

		_currentPetSkill = new SkillDat(currentSkill, ctrlPressed, shiftPressed);
	}

	public SkillDat getQueuedSkill()
	{
		return _queuedSkill;
	}

	/**
	 * Create a new SkillDat object and queue it in the player _queuedSkill.<BR><BR>
	 *
	 * @param queuedSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setQueuedSkill(L2Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed)
	{
		if(queuedSkill == null)
		{
			_queuedSkill = null;
			return;
		}

		_queuedSkill = new SkillDat(queuedSkill, ctrlPressed, shiftPressed);
	}

	/**
	 * @return текущий уровень наказания у игрока
	 */
	public PlayerPunishLevel getPunishLevel()
	{
		return _punishLevel;
	}

	public void setPunishLevel(int state)
	{
		switch(state)
		{
			case 0:
				_punishLevel = PlayerPunishLevel.NONE;
				break;
			case 1:
				_punishLevel = PlayerPunishLevel.CHAT;
				break;
			case 2:
				_punishLevel = PlayerPunishLevel.JAIL;
				break;
			case 3:
				_punishLevel = PlayerPunishLevel.CHAR;
				break;
			case 4:
				_punishLevel = PlayerPunishLevel.ACC;
				break;
		}
	}

	/**
	 * @return {@code true} если игрок в тюрьме
	 */
	public boolean isInJail()
	{
		return _punishLevel == PlayerPunishLevel.JAIL;
	}

	/**
	 * @return {@code true} если у игрока забанен чат
	 */
	public boolean isChatBanned()
	{
		return _punishLevel == PlayerPunishLevel.CHAT;
	}

	/**
	 * Sets punish level for player based on delay
	 *
	 * @param state
	 * @param delayInMinutes 0 - Indefinite
	 */
	public void setPunishLevel(PlayerPunishLevel state, int delayInMinutes)
	{
		long delayInMilliseconds = delayInMinutes * 60000L;
		switch(state)
		{
			case NONE: // Remove Punishments
				switch(_punishLevel)
				{
					case CHAT:
						_punishLevel = state;
						stopPunishTask(true);
						sendPacket(new EtcStatusUpdate(this));
						sendMessage("Бан чата снят.");
						break;
					case JAIL:
						_punishLevel = state;
						// Open a Html message to inform the player
						NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
						String jailInfos = HtmCache.getInstance().getHtm(getLang(), "jail_out.htm");
						if(jailInfos != null)
						{
							htmlMsg.setHtml(jailInfos);
						}
						else
						{
							htmlMsg.setHtml("<html><body>Вы теперь свободны, соблюдайте правила сервера!</body></html>");
						}
						sendPacket(htmlMsg);
						stopPunishTask(true);
						EventManager.onJail(this);
						teleToLocation(17836, 170178, -3507, true); // Floran
						break;
				}
				break;
			case CHAT: // Chat Ban
				// not allow player to escape jail using chat ban
				if(_punishLevel == PlayerPunishLevel.JAIL)
				{
					break;
				}
				_punishLevel = state;
				_punishTimer = 0;
				sendPacket(new EtcStatusUpdate(this));
				// Remove the task if any
				stopPunishTask(false);

				if(delayInMinutes > 0)
				{
					_punishTimer = delayInMilliseconds;

					// start the countdown
					_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(), _punishTimer);
					sendMessage("Ваш чат забанен на " + delayInMinutes + " минут.");
				}
				else
				{
					sendMessage("Ваш чат забанен навсегда.");
				}
				break;

			case JAIL: // Jail Player
				_punishLevel = state;
				_punishTimer = 0;
				// Remove the task if any
				stopPunishTask(false);

				if(delayInMinutes > 0)
				{
					_punishTimer = delayInMilliseconds;

					// start the countdown
					_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(), _punishTimer);
					sendMessage("Вы посажены в тюрьму на " + delayInMinutes + " минут.");
				}

				if(OlympiadManager.getInstance().isRegisteredInComp(this))
				{
					OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
				}

				// Open a Html message to inform the player
				NpcHtmlMessage htmlMsg = new NpcHtmlMessage(0);
				String jailInfos = HtmCache.getInstance().getHtm(getLang(), "jail_in.htm");
				if(jailInfos != null)
				{
					htmlMsg.setHtml(jailInfos);
				}
				else
				{
					htmlMsg.setHtml("<html><body>Вы были заключены в тюрьму Администрацией.</body></html>");
				}
				sendPacket(htmlMsg);
				getInstanceController().setInstanceId(0);

				teleToLocation(-114356, -249645, -2984, false); // Jail
				break;
			case CHAR: // Ban Character
				setAccessLevel(-100);
				logout();
				break;
			case ACC: // Ban Account
				setAccountAccesslevel(-100);
				logout();
				break;
			default:
				_punishLevel = state;
				break;
		}

		// store in database
		storeCharBase();
	}

	public long getPunishTimer()
	{
		return _punishTimer;
	}

	public void setPunishTimer(long time)
	{
		_punishTimer = time;
	}

	private void updatePunishState()
	{
		if(_punishLevel != PlayerPunishLevel.NONE)
		{
			// If punish timer exists, restart punishtask.
			if(_punishTimer > 0)
			{
				_punishTask = ThreadPoolManager.getInstance().scheduleGeneral(new PunishTask(), _punishTimer);
				sendMessage("You are still " + _punishLevel.string() + " for " + Math.round(_punishTimer / 60000.0f) + " minutes.");
			}
			if(_punishLevel == PlayerPunishLevel.JAIL)
			{
				// If player escaped, put him back in jail
				if(!isInsideZone(ZONE_JAIL))
				{
					teleToLocation(-114356, -249645, -2984, true);
				}
			}
		}
	}

	public void stopPunishTask(boolean save)
	{
		if(_punishTask != null)
		{
			if(save)
			{
				long delay = _punishTask.getDelay(TimeUnit.MILLISECONDS);
				if(delay < 0)
				{
					delay = 0;
				}
				_punishTimer = delay;
			}
			_punishTask.cancel(false);
			// ThreadPoolManager.getInstance().removeGeneral((Runnable)_punishTask);
			_punishTask = null;
		}
	}

	public void startFameTask(long delay, int fameFixRate)
	{
		if(getLevel() < 40 || getClassId().level() < 2)
		{
			return;
		}
		if(_fameTask == null)
		{
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FameTask(fameFixRate), delay, delay);
		}
	}

	public void stopFameTask()
	{
		if(_fameTask != null)
		{
			_fameTask.cancel(false);
			// ThreadPoolManager.getInstance().removeGeneral((Runnable)_fameTask);
			_fameTask = null;
		}
	}

	/**
	 * @return
	 */
	public int getPowerGrade()
	{
		return _powerGrade;
	}

	/**
	 * @param power
	 */
	public void setPowerGrade(int power)
	{
		_powerGrade = power;
	}

	public boolean isCursedWeaponEquipped()
	{
		return _cursedWeaponEquippedId != 0;
	}

	public int getCursedWeaponEquippedId()
	{
		return _cursedWeaponEquippedId;
	}

	public void setCursedWeaponEquippedId(int value)
	{
		_cursedWeaponEquippedId = value;
	}

	public boolean isCombatFlagEquipped()
	{
		return _combatFlagEquippedId;
	}

	public void setCombatFlagEquipped(boolean value)
	{
		_combatFlagEquippedId = value;
	}

	public void setIsRidingStrider(boolean mode)
	{
		_isRidingStrider = mode;
	}

	public boolean isRidingStrider()
	{
		return _isRidingStrider;
	}

	/**
	 * Returns the Number of Souls this L2PcInstance got.
	 *
	 * @return
	 */
	public int getSouls()
	{
		return _souls;
	}

	/**
	 * Absorbs a Soul from a Npc.
	 *
	 * @param skill
	 * @param npc
	 */
	public void absorbSoul(L2Skill skill, L2Npc npc)
	{
		if(_souls >= skill.getNumSouls())
		{
			sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
			return;
		}

		increaseSouls(1);

		if(npc != null)
		{
			broadcastPacket(new ExSpawnEmitter(this, npc), 500);
		}
	}

	/**
	 * Increase Souls
	 *
	 * @param count
	 */
	public void increaseSouls(int count)
	{
		if(count < 0 || count > 45)
		{
			return;
		}

		_souls += count;

		if(_souls > 45)
		{
			_souls = 45;
		}

		sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2).addNumber(count).addNumber(_souls));

		restartSoulTask();

		sendPacket(new EtcStatusUpdate(this));
	}

	/**
	 * Decreases existing Souls.
	 *
	 * @param count
	 * @param skill
	 * @return
	 */
	public boolean decreaseSouls(int count, L2Skill skill)
	{
		if(_souls <= 0 && skill.getSoulConsumeCount() > 0)
		{
			sendPacket(SystemMessageId.THERE_IS_NOT_ENOUGH_SOUL);
			return false;
		}

		_souls -= count;

		if(_souls < 0)
		{
			_souls = 0;
		}

		if(_souls == 0)
		{
			stopSoulTask();
		}
		else
		{
			restartSoulTask();
		}

		sendPacket(new EtcStatusUpdate(this));
		return true;
	}

	/**
	 * Clear out all Souls from this L2PcInstance
	 */
	public void clearSouls()
	{
		_souls = 0;
		stopSoulTask();
		sendPacket(new EtcStatusUpdate(this));
	}

	/**
	 * Starts/Restarts the SoulTask to Clear Souls after 10 Mins.
	 */
	private void restartSoulTask()
	{
		if(_soulTask != null)
		{
			_soulTask.cancel(false);
			_soulTask = null;
		}
		_soulTask = ThreadPoolManager.getInstance().scheduleGeneral(new SoulTask(), 600000);
	}

	/**
	 * Stops the Clearing Task.
	 */
	public void stopSoulTask()
	{
		if(_soulTask != null)
		{
			_soulTask.cancel(false);
			// ThreadPoolManager.getInstance().removeGeneral((Runnable)_soulTask);
			_soulTask = null;
		}
	}

	/**
	 * @param magicId
	 * @param level
	 * @param time
	 */
	public void shortBuffStatusUpdate(int magicId, int level, int time)
	{
		if(_shortBuffTask != null)
		{
			_shortBuffTask.cancel(false);
			_shortBuffTask = null;
		}
		_shortBuffTask = ThreadPoolManager.getInstance().scheduleGeneral(new ShortBuffTask(), time * 1000);
		_shortBuffTaskSkillId = magicId;

		sendPacket(new ShortBuffStatusUpdate(magicId, level, time));
	}

	public void setShortBuffTaskSkillId(int id)
	{
		_shortBuffTaskSkillId = id;
	}

	public void addTimeStampItem(L2ItemInstance item, long reuse)
	{
		_reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse));
	}

	public void addTimeStampItem(L2ItemInstance item, long reuse, long systime)
	{
		_reuseTimeStampsItems.put(item.getObjectId(), new TimeStamp(item, reuse, systime));
	}

	public long getItemRemainingReuseTime(int itemObjId)
	{
		if(_reuseTimeStampsItems.isEmpty() || !_reuseTimeStampsItems.containsKey(itemObjId))
		{
			return -1;
		}
		return _reuseTimeStampsItems.get(itemObjId).getRemaining();
	}

	public long getReuseDelayOnGroup(int group)
	{
		if(group > 0)
		{
			for(TimeStamp ts : _reuseTimeStampsItems.values())
			{
				if(ts.getSharedReuseGroup() == group && ts.hasNotPassed())
				{
					return ts.getRemaining();
				}
			}
		}
		return 0;
	}

	public FastMap<Integer, TimeStamp> getSkillReuseTimeStamps()
	{
		return _reuseTimeStampsSkills;
	}

	public long getSkillRemainingReuseTime(int skillReuseHashId)
	{
		if(_reuseTimeStampsSkills.isEmpty() || !_reuseTimeStampsSkills.containsKey(skillReuseHashId))
		{
			return -1;
		}
		return _reuseTimeStampsSkills.get(skillReuseHashId).getRemaining();
	}

	public boolean hasSkillReuse(int skillReuseHashId)
	{
		if(_reuseTimeStampsSkills.isEmpty() || !_reuseTimeStampsSkills.containsKey(skillReuseHashId))
		{
			return false;
		}
		return _reuseTimeStampsSkills.get(skillReuseHashId).hasNotPassed();
	}

	public TimeStamp getSkillReuseTimeStamp(int skillReuseHashId)
	{
		return _reuseTimeStampsSkills.get(skillReuseHashId);
	}

	/**
	 * Index according to skill this TimeStamp
	 * instance for restoration purposes only.
	 * @param skill
	 * @param reuse
	 * @param systime
	 */
	public void addTimeStamp(L2Skill skill, long reuse, long systime)
	{
		_reuseTimeStampsSkills.put(skill.getReuseHashCode(), new TimeStamp(skill, reuse, systime));
	}

	/**
	 * @return
	 */
	public int getAgathionId()
	{
		return _agathionId;
	}

	/**
	 * @param npcId
	 */
	public void setAgathionId(int npcId)
	{
		_agathionId = npcId;
	}

	/**
	 * Понижает количество личной славы на указанное число
	 *
	 * @param count количество требуемой личной славы
	 * @return {@code true} если у игрока хватает личной славы
	 */
	public boolean decreaseReputationsSelf(int count)
	{
		if(count > _fame)
		{
			sendPacket(SystemMessageId.NOT_ENOUGH_FAME_POINTS);
			return false;
		}
		setFame(_fame - count);
		return true;
	}

	/**
	 * Понижает количество славы клана на указанное число
	 *
	 * @param count количество требуемой славы клана
	 * @return {@code true} если у игрока есть клан, он является его лидером и славы хватает
	 */
	public boolean decreaseReputationsClan(int count)
	{
		if(!isClanLeader())
		{
			return false;
		}
		if(count > _clan.getReputationScore())
		{
			sendPacket(SystemMessageId.NOT_ENOUGH_FAME_POINTS); // TODO: Верное сообщение
			return false;
		}
		_clan.takeReputationScore(count, true);
		return true;
	}

	/**
	 * При взаимодействии с дверью, устанавливает таргет игрока на нее
	 *
	 * @param door дверь, с которой взаимодействуем
	 */
	public void gatesRequest(L2DoorInstance door)
	{
		_gatesRequest.setTarget(door);
	}

	/**
	 * "Ответ" на диалог двери
	 *
	 * @param answer ответ
	 * @param type   1 - открытие двери, 0 - закрытие
	 */
	public void gatesAnswer(int answer, int type)
	{
		if(_gatesRequest.getDoor() == null)
		{
			return;
		}

		if(answer == 1 && getTarget().equals(_gatesRequest.getDoor()) && type == 1)
		{
			_gatesRequest.getDoor().openMe();
		}
		else if(answer == 1 && getTarget().equals(_gatesRequest.getDoor()) && type == 0)
		{
			_gatesRequest.getDoor().closeMe();
		}

		_gatesRequest.setTarget(null);
	}

	public void checkItemRestriction()
	{
		for(int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++)
		{
			L2ItemInstance equippedItem = _inventory.getPaperdollItem(i);
			if(equippedItem != null && !equippedItem.getItem().checkCondition(equippedItem, this, this, false))
			{
				_inventory.unEquipItemInSlot(i);

				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(equippedItem);
				sendPacket(iu);

				if(equippedItem.getItem().getBodyPart() == L2Item.SLOT_BACK)
				{
					sendPacket(SystemMessageId.CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED);
					return;
				}

				if(equippedItem.getEnchantLevel() > 0)
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(equippedItem.getEnchantLevel()).addItemName(equippedItem));
				}
				else
				{
					sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(equippedItem));
				}
			}
		}
	}

	/**
	 * Устанавливает разрешенные скилы во время трансформации
	 *
	 * @param ids набор ID разрешенных скилов
	 */
	public void setTransformAllowedSkills(int[] ids)
	{
		_transformAllowedSkills = ids;
	}

	/**
	 * @param id ID скила
	 * @return Является ли данных скилл разрешенным во время трансформации
	 */
	public boolean containsAllowedTransformSkill(int id)
	{
		for(int _transformAllowedSkill : _transformAllowedSkills)
		{
			if(_transformAllowedSkill == id)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Начать задачу кормления животного
	 *
	 * @param npcId npcId животного
	 */
	protected void startFeed(int npcId)
	{
		synchronized(this)
		{
			L2Summon tempPet = null;
			if(!_summons.isEmpty())
			{
				for(L2Summon pet : _summons)
				{
					if(pet.getNpcId() == npcId)
					{
						tempPet = pet;
					}
				}

				if(tempPet != null)
				{
					setCurrentFeed(((L2PetInstance) tempPet).getCurrentFed());

					_controlItemId = tempPet.getControlObjectId();
					// Устанавливаем полоску голода, если персонаж верхом на пете
					if(_mountNpcId == tempPet.getNpcId())
					{
						sendPacket(new SetupGauge(SetupGauge.GREEN_MINI, _curFeed * 10000 / getFeedConsume(tempPet.getNpcId(), tempPet.getLevel()), getMaxFeed() * 10000 / getFeedConsume(tempPet.getNpcId(), tempPet.getLevel())));
					}

					if(!isDead())
					{
						_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(tempPet), 10000, 10000);
					}
				}
			}

			if(npcId > 0 && tempPet == null)
			{
				setCurrentFeed(getMaxFeed());
				sendPacket(new SetupGauge(SetupGauge.GREEN_MINI, _curFeed * 10000 / getFeedConsume(_mountNpcId, _mountLevel), getMaxFeed() * 10000 / getFeedConsume(_mountNpcId, _mountLevel)));
				if(!isDead())
				{
					_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
				}
			}
		}
	}

	/**
	 * Остановить задачу кормления животного
	 */
	protected void stopFeed()
	{
		synchronized(this)
		{
			if(_mountFeedTask != null)
			{
				_mountFeedTask.cancel(false);
				// ThreadPoolManager.getInstance().removeGeneral((Runnable)_mountFeedTask);
				_mountFeedTask = null;
				if(Config.DEBUG)
				{
					_log.log(Level.DEBUG, "Pet [#" + _mountNpcId + "] feed task stop");
				}
			}
		}
	}

	/******************************
	 * Методы для ездовых животных
	 ******************************/

	/**
	 * Очистить текущую информацию о животных
	 */
	private void clearPetData()
	{
		_petData = null;
	}

	/**
	 * @param npcId npcId питомца
	 * @return PetData указанного питомца
	 */
	private L2PetData getPetData(int npcId)
	{
		if(_petData == null)
		{
			_petData = PetDataTable.getInstance().getPetData(npcId);
		}
		return _petData;
	}

	/**
	 * @param npcId npcId питомца
	 * @return L2PetLevelData указанного питомца
	 */
	private L2PetLevelData getPetLevelData(int npcId, int level)
	{
		if(_leveldata == null)
		{
			_leveldata = PetDataTable.getInstance().getPetData(npcId).getPetLevelData(level);
		}
		return _leveldata;
	}

	/**
	 * @return текущий уровень сытности
	 */
	public int getCurrentFeed()
	{
		return _curFeed;
	}

	/**
	 * Установить степень сытности питомца
	 *
	 * @param num степень сытности
	 */
	public void setCurrentFeed(int num)
	{
		_curFeed = num > getMaxFeed() ? getMaxFeed() : num;
		if(isMounted())
		{
			sendPacket(new SetupGauge(SetupGauge.GREEN_MINI, _curFeed * 10000 / getFeedConsume(_mountNpcId, _mountLevel), getMaxFeed() * 10000 / getFeedConsume(_mountNpcId, _mountLevel)));
		}
	}

	/**
	 * @return потребление еды в единицу времени
	 */
	private int getFeedConsume(int npcId, int level)
	{
		// Поглощение еды зависит от боевого состояния персонажа
		if(isAttackingNow())
		{
			return getPetLevelData(npcId, level).getPetFeedBattle();
		}
		return getPetLevelData(npcId, level).getPetFeedNormal();
	}

	/**
	 * @return максимальный уровень сытности питомца
	 */
	private int getMaxFeed()
	{
		if(_mountNpcId > 0)
		{
			return getPetLevelData(_mountNpcId, _mountLevel).getPetMaxFeed();
		}
		for(L2Summon pet : _summons)
		{
			if(pet.isPet())
			{
				return getPetLevelData(pet.getNpcId(), pet.getLevel()).getPetMaxFeed();
			}
		}
		return 0;
	}

	private int getMaxFeed(int npcId, int level)
	{
		return getPetLevelData(npcId, level).getPetMaxFeed();
	}

	/**
	 * @return голоден ли питомец
	 */
	private boolean isHungry()
	{
		return _canFeed && _curFeed < getPetData(_mountNpcId).getHungryLimit() / 100.0f * getPetLevelData(_mountNpcId, _mountLevel).getPetMaxFeed();
	}

	/**
	 * При попадании в зону "Без Полетов" выполняем таск на дисмаунт с ездового животного
	 *
	 * @param delay задержка в секундах
	 */
	public void enteredNoLanding(int delay)
	{
		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new Dismount(), delay * 1000);
	}

	/**
	 * Если за время, отведенное в enteredNoLanding(), игрок вышел из зоны "без полетов" - отменяем таск
	 * на дисмаунт
	 */
	public void exitedNoLanding()
	{
		if(_dismountTask != null)
		{
			_dismountTask.cancel(true);
			_dismountTask = null;
		}
	}

	/**
	 * Сохранить данные о сытности питомца
	 *
	 * @param petId id питомца
	 */
	public void storePetFood(int petId)
	{
		if(_controlItemId != 0 && petId != 0)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?");
				statement.setInt(1, _curFeed);
				statement.setInt(2, _controlItemId);
				statement.executeUpdate();
				_controlItemId = 0;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Failed to store Pet [NpcId: " + petId + "] data", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
	}

	public void setIsInSiege(boolean b)
	{
		_isInSiege = b;
	}

	public boolean isInSiege()
	{
		return _isInSiege;
	}

	/**
	 * @param isInHideoutSiege sets the value of {@link #_isInHideoutSiege}.
	 */
	public void setIsInHideoutSiege(boolean isInHideoutSiege)
	{
		_isInHideoutSiege = isInHideoutSiege;
	}

	/**
	 * @return the value of {@link #_isInHideoutSiege}, {@code true} if the player is participing on a Hideout Siege, otherwise {@code false}.
	 */
	public boolean isInHideoutSiege()
	{
		return _isInHideoutSiege;
	}

	public FloodProtectors getFloodProtectors()
	{
		return _client.getFloodProtectors();
	}

	public boolean isFlyingMounted()
	{
		return _isFlyingMounted;
	}

	public void setIsFlyingMounted(boolean val)
	{
		_isFlyingMounted = val;
		setIsFlying(val);
	}

	/**
	 * @return the Number of Charges this L2PcInstance got.
	 */
	public int getCharges()
	{
		return _charges.get();
	}

	public void increaseCharges(int count, int max)
	{
		int _max = (int) calcStat(Stats.ENERGY_MASTERY, 0, null, null);

		if(_max > max)
		{
			max = _max;
		}

		if(_charges.get() >= max)
		{
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}

		// Charge clear task should be reset every time a charge is increased.
		restartChargeTask();

		if(_charges.addAndGet(count) >= max)
		{
			_charges.set(max);
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		}
		else
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_charges.get()));
		}

		sendPacket(new EtcStatusUpdate(this));
	}

	public boolean decreaseCharges(int count, boolean remaining)
	{
		if(remaining)
		{
			if(_charges.get() >= count)
			{
				_charges.addAndGet(-count);
				stopChargeTask();
			}
			else
			{
				_charges.addAndGet(-_charges.get());
				stopChargeTask();
			}
		}
		else
		{
			if(_charges.get() < count)
			{
				return false;
			}

			// Charge clear task should be reset every time a charge is decreased and stopped when charges become 0.
			if(_charges.addAndGet(-count) == 0)
			{
				stopChargeTask();
			}
			else
			{
				restartChargeTask();
			}
		}

		sendPacket(new EtcStatusUpdate(this));
		return true;
	}

	public void clearCharges()
	{
		_charges.set(0);
		sendPacket(new EtcStatusUpdate(this));
	}

	/**
	 * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
	 */
	private void restartChargeTask()
	{
		if(_chargeTask != null)
		{
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ChargeTask(), 600000);
	}

	/**
	 * Stops the Charges Clearing Task.
	 */
	public void stopChargeTask()
	{
		if(_chargeTask != null)
		{
			_chargeTask.cancel(false);
			// ThreadPoolManager.getInstance().removeGeneral((Runnable)_chargeTask);
			_chargeTask = null;
		}
	}

	public void teleportBookmarkModify(int Id, int icon, String tag, String name)
	{
		int count = 0;
		int size = tpbookmark.size();
		while(size > count)
		{
			if(tpbookmark.get(count).getId() == Id)
			{
				tpbookmark.get(count).setIcon(icon);
				tpbookmark.get(count).setTag(tag);
				tpbookmark.get(count).setName(name);

				ThreadConnection con = null;
				FiltredPreparedStatement statement = null;
				try
				{

					con = L2DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement(Characters.TP_BOOKMARK_UPDATE);

					statement.setInt(1, icon);
					statement.setString(2, tag);
					statement.setString(3, name);
					statement.setInt(4, getObjectId());
					statement.setInt(5, Id);

					statement.execute();
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Could not update character teleport bookmark data: " + e.getMessage(), e);
				}
				finally
				{
					DatabaseUtils.closeDatabaseCS(con, statement);
				}
			}
			count++;
		}

		sendPacket(new ExGetBookMarkInfo(this));

	}

	public void teleportBookmarkDelete(int Id)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.TP_BOOKMARK_DELETE);

			statement.setInt(1, getObjectId());
			statement.setInt(2, Id);

			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not delete character teleport bookmark data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		int count = 0;
		int size = tpbookmark.size();

		while(size > count)
		{
			if(tpbookmark.get(count).getId() == Id)
			{
				tpbookmark.remove(count);
				break;
			}
			count++;
		}

		sendPacket(new ExGetBookMarkInfo(this));
	}

	public void teleportBookmarkGo(int Id)
	{
		if(!teleportBookmarkCondition(0) || this == null)
		{
			return;
		}
		if(_inventory.getInventoryItemCount(13016, 0) == 0)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM);
			return;
		}
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(13016);
		sendPacket(sm);
		int count = 0;
		int size = tpbookmark.size();
		while(size > count)
		{
			if(tpbookmark.get(count).getId() == Id)
			{
				destroyItem(ProcessType.CONSUME, _inventory.getItemByItemId(13016).getObjectId(), 1, null, false);
				teleToLocation(tpbookmark.get(count).getLoc());
				break;
			}
			count++;
		}
		sendPacket(new ExGetBookMarkInfo(this));
	}

	public boolean teleportBookmarkCondition(int type)
	{
		if(isInCombat())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		}
		else if(_isInSiege || _siegeState != PlayerSiegeSide.NONE)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING);
			return false;
		}
		else if(_isInDuel)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		}
		else if(isFlying())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		}
		else if(_olympiadController.isParticipating())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		}
		else if(isParalyzed())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED);
			return false;
		}
		else if(isDead())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
			return false;
		}
		else if(isFlyUp())
		{
			//??
			return false;
		}
		else if(isKnockBacked())
		{
			//??
			return false;
		}
		else if(type == 1 && isInParty())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			return false;
		}
		else if(isInBoat() || isInAirShip() || isInShuttle() || isInJail() || isInsideZone(ZONE_NOSUMMONFRIEND))
		{
			if(type == 0)
			{
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			}
			else if(type == 1)
			{
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			}
			return false;
		}
		else if(isInWater())
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		}
		else if(type == 1 && (isInsideZone(ZONE_SIEGE) || isInsideZone(ZONE_CLANHALL) || isInsideZone(ZONE_JAIL) || isInsideZone(ZONE_CASTLE) || isInsideZone(ZONE_NOSUMMONFRIEND) || isInsideZone(ZONE_FORT)))
		{
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA));
			return false;
		}
		else if(isInsideZone(ZONE_NOBOOKMARK))
		{
			if(type == 0)
			{
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			}
			else if(type == 1)
			{
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			}
			return false;
		}
		else if(getInstanceId() > 0)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
			return false;
		}
		else
		{
			return true;
		}
	}

	public void teleportBookmarkAdd(Location loc, int icon, String tag, String name)
	{
		if(!teleportBookmarkCondition(1))
		{
			return;
		}

		if(tpbookmark.size() >= _bookmarkslot)
		{
			sendPacket(SystemMessageId.YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION);
			return;
		}

		if(_inventory.getInventoryItemCount(20033, 0) == 0)
		{
			sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
			return;
		}

		int count = 0;
		int id = 1;
		FastList<Integer> idlist = new FastList<>();

		int size = tpbookmark.size();

		while(size > count)
		{
			idlist.add(tpbookmark.get(count).getId());
			count++;
		}

		for(int i = 1; i < 10; i++)
		{
			if(!idlist.contains(i))
			{
				id = i;
				break;
			}
		}

		tpbookmark.add(new TeleportBookmark(id, loc, icon, tag, name));

		destroyItem(ProcessType.CONSUME, _inventory.getItemByItemId(20033).getObjectId(), 1, null, false);

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20033);
		sendPacket(sm);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.TP_BOOKMARK_ADD);

			statement.setInt(1, getObjectId());
			statement.setInt(2, id);
			statement.setInt(3, loc.getX());
			statement.setInt(4, loc.getY());
			statement.setInt(5, loc.getZ());
			statement.setInt(6, icon);
			statement.setString(7, tag);
			statement.setString(8, name);

			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not insert character teleport bookmark data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}

		sendPacket(new ExGetBookMarkInfo(this));
	}

	public void restoreTeleportBookmark()
	{
		if(tpbookmark == null)
		{
			tpbookmark = new ArrayList<>();
		}
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.TP_BOOKMARK_RESTORE);
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			Location loc = null;
			while(rset.next())
			{
				loc = new Location(rset.getInt("Id"), rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
				tpbookmark.add(new TeleportBookmark(rset.getInt("Id"), loc, rset.getInt("icon"), rset.getString("tag"), rset.getString("name")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Failed restoing character teleport bookmark.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void showQuestMovie(int id)
	{
		if(_movieId > 0) // already in movie
		{
			return;
		}
		abortAttack();
		abortCast();
		stopMove(null);
		_movieId = id;
		sendPacket(new ExStartScenePlayer(id));
	}

	public void showUsmVideo(int id)
	{
		abortAttack();
		abortCast();
		stopMove(null);
		sendPacket(new ExShowUsm(id));
	}

	public boolean isAllowedToEnchantSkills()
	{
		if(isLocked())
		{
			return false;
		}
		if(isTransformed() || isInStance())
		{
			return false;
		}
		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(this))
		{
			return false;
		}
		if(isCastingNow() || isCastingSimultaneouslyNow())
		{
			return false;
		}
		if(isInBoat() || isInAirShip() || isInShuttle())
		{
			return false;
		}
		return !_isJumping;
	}

	/**
	 * @return the _createDate of the L2PcInstance.
	 */
	public Calendar getCreateDate()
	{
		return _createDate;
	}

	/**
	 * Set the _createDate of the L2PcInstance.<BR><BR>
	 *
	 * @param createDate
	 */
	public void setCreateDate(Calendar createDate)
	{
		_createDate = createDate;
	}

	/**
	 * @return number of days to char birthday.<BR><BR>
	 */
	public int getDaysToBirthDay()
	{
		Calendar now = Calendar.getInstance();

		// "Characters with a February 29 creation date will receive a gift on February 28."
		if(_createDate.get(Calendar.DAY_OF_MONTH) == 29 && _createDate.get(Calendar.MONTH) == 1)
		{
			_createDate.add(Calendar.HOUR_OF_DAY, -24);
		}

		if(now.get(Calendar.MONTH) == _createDate.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == _createDate.get(Calendar.DAY_OF_MONTH) && now.get(Calendar.YEAR) != _createDate.get(Calendar.YEAR))
		{
			return 0;
		}

		int i;
		for(i = 1; i < 6; i++)
		{
			now.add(Calendar.HOUR_OF_DAY, 24);
			if(now.get(Calendar.MONTH) == _createDate.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == _createDate.get(Calendar.DAY_OF_MONTH) && now.get(Calendar.YEAR) != _createDate.get(Calendar.YEAR))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Уведомление друзей о входе в мир игрока
	 */
	private void notifyFriends()
	{
		FriendStatus pkt = new FriendStatus(getObjectId());
		for(int id : RelationListManager.getInstance().getFriendList(getObjectId()))
		{
			L2PcInstance friend = WorldManager.getInstance().getPlayer(id);
			if(friend != null)
			{
				friend.sendPacket(pkt);
			}
		}
	}

	/**
	 * @return the _silenceMode
	 */
	public boolean isSilenceMode()
	{
		return _silenceMode;
	}

	/**
	 * @param mode the _silenceMode to set
	 */
	public void setSilenceMode(boolean mode)
	{
		_silenceMode = mode;
		_silenceModeExcluded.clear(); // Clear the excluded list on each setSilenceMode
		sendPacket(new EtcStatusUpdate(this));
	}

	/**
	 * While at silenceMode, checks if this PC Instance blocks PMs for this user
	 *
	 * @param objId
	 * @return
	 */
	public boolean isSilenceMode(int objId)
	{
		if(Config.SILENCE_MODE_EXCLUDE && _silenceMode)
		{
			return !_silenceModeExcluded.contains(objId);
		}

		return _silenceMode;
	}

	public void addSilenceModeExcluded(int playerObjId)
	{
		_silenceModeExcluded.add(playerObjId);
	}

	private void storeRecipeShopList()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			L2ManufactureList list = _createList;

			if(list != null && list.size() > 0)
			{
				int _position = 1;
				statement = con.prepareStatement("DELETE FROM character_recipeshoplist WHERE charId=? ");
				statement.setInt(1, getObjectId());
				statement.execute();
				DatabaseUtils.closeStatement(statement);

				statement = con.prepareStatement("INSERT INTO character_recipeshoplist (charId, Recipeid, Price, Pos) VALUES (?, ?, ?, ?)");
				for(L2ManufactureItem item : list.getList())
				{
					statement.setInt(1, getObjectId());
					statement.setInt(2, item.getRecipeId());
					statement.setLong(3, item.getCost());
					statement.setInt(4, _position);
					statement.execute();
					statement.clearParameters();
					_position++;
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store recipe shop for playerID " + getObjectId() + ": ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void restoreRecipeShopList()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT Recipeid,Price FROM character_recipeshoplist WHERE charId=? ORDER BY Pos ASC");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			L2ManufactureList createList = new L2ManufactureList();
			while(rset.next())
			{
				createList.add(new L2ManufactureItem(rset.getInt("Recipeid"), rset.getLong("Price")));
			}
			_createList = createList;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore recipe shop list data for playerId: " + getObjectId(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public double getCollisionRadius()
	{
		return _mountType != 0 ? NpcTable.getInstance().getTemplate(_mountNpcId).getFCollisionRadius(this) : isTransformed() && !_transformation.isStance() ? _transformation.getCollisionRadius() : getBaseTemplate().getCollisionRadius(this);
	}

	public double getCollisionHeight()
	{
		return _mountType != 0 ? NpcTable.getInstance().getTemplate(_mountNpcId).getFCollisionHeight(this) : isTransformed() && !_transformation.isStance() ? _transformation.getCollisionHeight() : getBaseTemplate().getCollisionHeight(this);
	}

	public int getClientX()
	{
		return _clientX;
	}

	public void setClientX(int val)
	{
		_clientX = val;
	}

	public int getClientY()
	{
		return _clientY;
	}

	public void setClientY(int val)
	{
		_clientY = val;
	}

	public int getClientZ()
	{
		return _clientZ;
	}

	public void setClientZ(int val)
	{
		_clientZ = val;
	}

	public int getClientHeading()
	{
		return _clientHeading;
	}

	public void setClientHeading(int val)
	{
		_clientHeading = val;
	}

	/**
	 * @param z
	 * @return true if character falling now on the start of fall return false for correct coord sync!
	 */
	public boolean isFalling(int z)
	{
		if(isDead() || isFlying() || _isFlyingMounted || isInsideZone(ZONE_WATER))
		{
			return false;
		}

		if(System.currentTimeMillis() < _fallingTimestamp)
		{
			return true;
		}

		int deltaZ = getZ() - z;
		if(deltaZ <= getBaseTemplate().getFallHeight(this))
		{
			return false;
		}

		int damage = (int) Falling.calcFallDam(this, deltaZ);
		if(damage > 0)
		{
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage));
		}

		setFalling();

		return false;
	}

	/**
	 * Set falling timestamp
	 */
	public void setFalling()
	{
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}

	/**
	 * @return the _movieId
	 */
	public int getMovieId()
	{
		return _movieId;
	}

	public void setMovieId(int id)
	{
		_movieId = id;
	}

	/**
	 * Update last item auction request timestamp to current
	 */
	public void updateLastItemAuctionRequest()
	{
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
	}

	/**
	 * @return {@code true} if receiving item auction requests<br>
	 *         (last request was in 2 seconds before)
	 */
	public boolean isItemAuctionPolling()
	{
		return System.currentTimeMillis() - _lastItemAuctionInfoRequest < 2000;
	}

	public String getLang()
	{
		return variablesController.get("lang@", ConfigLocalization.MULTILANG_DEFAULT);
	}

	public void setLang(String lang)
	{
		variablesController.set("lang@", lang);
	}

	public long getOfflineStartTime()
	{
		return _offlineShopStart;
	}

	public void setOfflineStartTime(long time)
	{
		_offlineShopStart = time;
	}

	/**
	 * Remove player from BossZones (used on char logout/exit)
	 */
	public void removeFromBossZone()
	{
		try
		{
			for(L2BossZone _zone : GrandBossManager.getInstance().getZones())
			{
				_zone.removePlayer(this);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on removeFromBossZone(): " + e.getMessage(), e);
		}
	}

	/**
	 * Check all player skills for skill level. If player level is lower than
	 * skill learn level - 4, skill level is decreased to next possible level.
	 */
	public void checkPlayerSkills()
	{
		for(int id : getSkills().keySet())
		{
			int level = getSkillLevel(id);
			if(level >= 100) // enchanted skill
			{
				level = SkillTable.getInstance().getMaxLevel(id);
			}
			L2SkillLearn learn = SkillTreesData.getInstance().getClassSkill(id, level, getClassId());
			// not found - not a learn skill?
			if(learn == null)
			{
			}
			else
			{
				// player level is too low for such skill level
				if(getLevel() < learn.getMinLevel() - 4)
				{
					deacreaseSkillLevel(id);
				}
			}
		}
	}

	private void deacreaseSkillLevel(int id)
	{
		int nextLevel = -1;
		for(L2SkillLearn sl : SkillTreesData.getInstance().getCompleteClassSkillTree(getClassId()).values())
		{
			if(sl.getSkillId() == id && nextLevel < sl.getSkillLevel() && getLevel() >= sl.getMinLevel() - 4)
			{
				// next possible skill level
				nextLevel = sl.getSkillLevel();
			}
		}

		if(nextLevel == -1) // there is no lower skill
		{
			//_log.log(Level.INFO, "Removing skill id " + id + " level " + getSkillLevel(id) + " from player " + this);
			removeSkill(getSkills().get(id), true);
		}
		else // replace with lower one
		{
			//_log.log(Level.INFO, "Decreasing skill id " + id + " from " + getSkillLevel(id) + " to " + nextLevel + " for " + this);
			addSkill(SkillTable.getInstance().getInfo(id, nextLevel), true);
		}
	}

	public void setMultiSocialAction(int id, int targetId)
	{
		_multiSociaAction = id;
		_multiSocialTarget = targetId;
	}

	public int getMultiSociaAction()
	{
		return _multiSociaAction;
	}

	public int getMultiSocialTarget()
	{
		return _multiSocialTarget;
	}

	public List<TeleportBookmark> getTpbookmark()
	{
		return tpbookmark;
	}

	public int getBookmarkslot()
	{
		return _bookmarkslot;
	}

	/**
	 * @return максимальное количество слотов в квестовом инвентаре
	 */
	public int getQuestInventoryLimit()
	{
		return Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}

	public boolean canAttackCharacter(L2Character cha)
	{
		if(cha instanceof L2Attackable)
		{
			return true;
		}
		if(cha instanceof L2Playable)
		{
			if(cha.isInsideZone(L2Character.ZONE_PVP) && !cha.isInsideZone(L2Character.ZONE_SIEGE))
			{
				return true;
			}

			L2PcInstance target;
			target = cha instanceof L2Summon ? ((L2Summon) cha).getOwner() : (L2PcInstance) cha;

			if(_isInDuel && target._isInDuel && target._duelId == _duelId)
			{
				return true;
			}
			else if(isInParty() && target.isInParty())
			{
				if(_party.equals(target._party))
				{
					return false;
				}
				if((_party.getCommandChannel() != null || target._party.getCommandChannel() != null) && _party.getCommandChannel().equals(target._party.getCommandChannel()))
				{
					return false;
				}
			}
			else if(_clan != null && target._clan != null)
			{
				if(_clanId == target._clanId)
				{
					return false;
				}
				if((getAllyId() > 0 || target.getAllyId() > 0) && getAllyId() == target.getAllyId())
				{
					return false;
				}
			}
			else if(_clan == null || target._clan == null)
			{
				if(!target._pvpFlagController.isFlagged() && !target.hasBadReputation())
				{
					return false;
				}
			}
		}
		return true;
	}

	public boolean isInventoryUnder(double loadLimit, boolean includeQuestItems)
	{
		if(_inventory.getSize(false) <= getInventoryLimit() * loadLimit)
		{
			if(includeQuestItems)
			{
				if(_inventory.getSize(true) <= getQuestInventoryLimit() * loadLimit)
				{
					return true;
				}
			}
			else
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @param includeQuestInv проверять ли квестовый инвентарь?
	 * @return занят ли ивентарь меньше чем на 90%
	 */
	public boolean isInventoryUnder90(boolean includeQuestInv)
	{
		return isInventoryUnder(0.9, includeQuestInv);
	}

	public boolean havePetInvItems()
	{
		return _petItems;
	}

	public void setPetInvItems(boolean haveit)
	{
		_petItems = haveit;
	}

	private void checkPetInvItems()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id FROM `items` WHERE `owner_id`=? AND (`loc`='PET' OR `loc`='PET_EQUIP') LIMIT 1;");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();
			_petItems = rset.next() && rset.getInt("object_id") > 0;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not check Items in Pet Inventory for playerId: " + getObjectId(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/***
	 * Загрузка данных о рекоммендациях текущего игрока
	 */
	private void loadRecommendations()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT rec_have,rec_left FROM character_recommendation WHERE charId=? LIMIT 1");
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			if(rset.next())
			{
				setRecommendations(rset.getInt("rec_have"));
				setRecommendationsLeft(rset.getInt("rec_left"));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore Recommendations for player: " + getObjectId(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Update L2PcInstance Recommendations data.
	 */
	public void storeRecommendations()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_recommendation (charId,rec_have,rec_left) VALUES (?,?,?) ON DUPLICATE KEY UPDATE rec_have=?, rec_left=?");
			statement.setInt(1, getObjectId());
			statement.setInt(2, _recomendationHave);
			statement.setInt(3, _recomendationLeft);
			// Update part
			statement.setInt(4, _recomendationHave);
			statement.setInt(5, _recomendationLeft);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not update Recommendations for player: " + getObjectId(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/***
	 * Ежедневный сброс рекоммендаций для онлайн-игроков
	 */
	public void resetRecommendationData()
	{
		stopRecommendationGiveTask();
		setRecommendationsLeft(20);
		setRecommendations(_recomendationHave - 20);
		_recomendationTwoHoursGiven = 0;
		_recommendationGiveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RecommendationGiveTask(), 7200000, 3600000);
		sendUserInfo();
	}

	public void startRecommendationGiveTask()
	{
		// Загружаем данные о реках из базы данных
		loadRecommendations();

		// При первом входе в игру Lineage 2 вы автоматически получите права на 20 рекомендаций
		if(!variablesController.get("recommendFirstTime", Boolean.class, false))
		{
			// Добавляем 20 реков при первом входе
			setRecommendationsLeft(_recomendationLeft + 20);
			variablesController.set("recommendFirstTime", true);
			sendUserInfo();
		}

		// Создаем таск на получение реков при нахождении в игре
		_recommendationGiveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RecommendationGiveTask(), 7200000, 3600000);

		// Сохраняем данные для следующего входа
		storeRecommendations();
	}

	public void stopRecommendationGiveTask()
	{
		if(_recommendationGiveTask != null)
		{
			_recommendationGiveTask.cancel(false);
			_recommendationGiveTask = null;
		}
	}

	public String getLastPetitionGmName()
	{
		return _lastPetitionGmName;
	}

	public void setLastPetitionGmName(String gmName)
	{
		_lastPetitionGmName = gmName;
	}

	public L2ContactList getContactList()
	{
		return _contactList;
	}

	public boolean isHideInfo()
	{
		return _hideInfo;
	}

	public void setHideInfo(boolean hideInfo)
	{
		_hideInfo = hideInfo;
	}

	public PcAdmin getPcAdmin()
	{
		if(_PcAdmin == null)
		{
			_PcAdmin = new PcAdmin(this);
		}
		return _PcAdmin;
	}

	/**
	 * @param itemId ID предмета
	 * @return количество указанных предметов в инвентаре
	 */
	public long getItemsCount(int itemId)
	{
		long count = 0;
		for(L2ItemInstance item : _inventory.getItems())
		{
			if(item != null && item.getItemId() == itemId)
			{
				count += item.getCount();
			}
		}
		return count;
	}

	public int getGamePoints()
	{
		if(_gamePoints == null)
		{
			_gamePoints = AccountShareDataTable.getInstance().getAccountData(getAccountName(), "game_points", "0");
		}
		return _gamePoints.getIntValue(0);
	}

	public void reduceGamePoints(int points, boolean save)
	{
		setGamePoints(getGamePoints() - points, save);
	}

	public void setGamePoints(int points, boolean save)
	{
		_gamePoints.setValue(String.valueOf(points));
		if(save)
		{
			_gamePoints.updateInDb();
		}
	}

	/**
	 * Прогрузка схем бафов для игрока и пета
	 */
	public void restoreBBSBuf()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.BBS_SCHEMA_RESTORE);
			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			while(rset.next())
			{
				int idSkill = rset.getInt("skill_id");
				int lvlSkill = rset.getInt("skill_level");
				_bbsBuff.put(idSkill, lvlSkill);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore " + this + " BBS buff data: " + e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Сохранение схем бафера для игрока и пета
	 */
	public void storeBBSBuff()
	{
		Map<Integer, Integer> ceffects = new HashMap<>();
		ceffects.putAll(_bbsBuff);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			// Удаляем все скилы из таблицы ( вдруг дюпнут :) )
			statement = con.prepareStatement(Characters.BBS_SCHEMA_CLEAR);
			statement.setInt(1, getObjectId());
			statement.execute();
			statement.clearParameters();

			for(Map.Entry<Integer, Integer> entry : ceffects.entrySet())
			{
				statement = con.prepareStatement(Characters.BBS_SCHEMA_ADD);
				statement.setInt(1, getObjectId());
				statement.setInt(2, entry.getKey());
				statement.setInt(3, entry.getValue());
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store char effect data", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Обновление бафов в базе для игрока и пета
	 */
	public void updateBBSBuff()
	{
		List<L2Character> character = new ArrayList<>();
		Map<Integer, Integer> ceffects = new HashMap<>();

		character.add(this);

		if(character.isEmpty())
		{
			return;
		}
		for(L2Character cha : character)
		{
			for(L2Effect cureffect : cha.getAllEffects())
			{
				int idSklll = cureffect.getSkill().getId();
				int lvlSklll = cureffect.getSkill().getLevel();
				if(CommunityBuffTable.getInstance().isBBSSaveBuf(idSklll, lvlSklll))
				{
					ceffects.put(idSklll, lvlSklll);
				}
			}
		}
		if(ceffects.isEmpty())
		{
			return;
		}

		boolean saveBuff = false;
		if(ceffects.size() != _bbsBuff.size())
		{
			saveBuff = true;
		}
		for(Map.Entry<Integer, Integer> entry : ceffects.entrySet())
		{
			if(!_bbsBuff.containsKey(entry.getKey()))
			{
				saveBuff = true;
			}
		}
		if(!saveBuff)
		{
			return;
		}

		_bbsBuff.clear();
		_bbsBuff.putAll(ceffects);

		storeBBSBuff();
	}

	/**
	 * Кастуем бафы из схемы на игрока и пета
	 *
	 * @param forPet для петов?
	 */
	public void castBBSBuff(boolean forPet)
	{
		List<L2Character> character = new ArrayList<>();
		Map<Integer, Integer> ceffects = new HashMap<>();
		if(forPet)
		{
			if(!_summons.isEmpty())
			{
				character.addAll(_summons.stream().collect(Collectors.toList()));
			}
			ceffects.putAll(_bbsBuff);
		}
		else
		{
			character.add(this);
			ceffects.putAll(_bbsBuff);
		}
		if(character.isEmpty() || ceffects.isEmpty())
		{
			return;
		}

		for(Map.Entry<Integer, Integer> entry : ceffects.entrySet())
		{
			L2Skill skill = SkillTable.getInstance().getInfo(entry.getKey(), entry.getValue());
			if(skill == null)
			{
				continue;
			}
			for(L2Character cha : character)
			{
				skill.getEffects(cha, cha, ConfigCommunityBoardPVP.COMMUNITY_BOARD_PVP_BUFF_TIME_OVERRIDE);
			}
		}
	}

	/**
	 * @return стоимость бафов
	 */
	public int calcBBSBuff()
	{
		restoreBBSBuf();

		List<L2Character> character = new ArrayList<>();
		Map<Integer, Integer> ceffects = new HashMap<>();

		character.add(this);
		ceffects.putAll(_bbsBuff);

		if(character.isEmpty() || ceffects.isEmpty())
		{
			return 0;
		}

		int skillId;
		int skillLvl;
		int bbsGroup;
		int priceGroup;
		int result = 0;
		for(Map.Entry<Integer, Integer> entry : ceffects.entrySet())
		{
			skillId = entry.getKey();
			skillLvl = entry.getValue();
			bbsGroup = CommunityBuffTable.getInstance().getBBSGroupForBuf(skillId, skillLvl);
			if(bbsGroup == 0)
			{
				continue;
			}
			priceGroup = CommunityBuffTable.getInstance().getPriceGroup(bbsGroup);
			if(priceGroup < 0)
			{
				continue;
			}
			result += priceGroup;
		}
		return result;
	}

	/**
	 * Текущее количество PCCafe-поинтов
	 * @return int количество PCCafe-поинтов
	 */
	public int getPcBangPoints()
	{
		return _pcBangPoints;
	}

	/**
	 * Устанавливает количество PCCafe-поинтов
	 *
	 * @param i количество PCCafe-поинтов
	 */
	public void setPcBangPoints(int i)
	{
		_pcBangPoints = i;
	}

	/**
	 * Устанавливает флаг прибывания в системе поиска группы
	 * @param bool true\false
	 */
	public void setIsInPartyWaitingList(boolean bool)
	{
		_isInPartyWaitingList = bool;
	}

	/**
	 * Проверяет текущее состояние игрока
	 * в системе поиска группы
	 *
	 * @return true\false
	 */
	public boolean isInPartyWaitingList()
	{
		return _isInPartyWaitingList;
	}

	/**
	 * Используется ли игроком автолут вещей?
	 *
	 * @return boolean используется ли автолут
	 */
	public boolean getUseAutoLoot()
	{
		return variablesController.get("useAutoLoot@", Boolean.class, Config.ALLOW_AUTOLOOT_COMMAND);
	}


    public boolean getUseTitlePvpMod()
    {
        return variablesController.get("useTitlePvpMode@", Boolean.class, Config.TITLE_PVP_MODE);
    }
	/**
	 * Используется ли игроком автолут растений?
	 *
	 * @return boolean используется ли автолут
	 */
	public boolean getUseAutoLootHerbs()
	{
		return variablesController.get("useAutoLootHerbs@", Boolean.class, Config.ALLOW_AUTOLOOT_COMMAND);
	}

	/**
	 * @return {@code true} если персонаж может получать опыт
	 */
	public boolean isAbleToGainExp()
	{
		return variablesController.get("ableToGainExp@", Boolean.class, false);
	}

	/***
	 * @return время в мс которое персонаж не может перемещаться после разговора с NPC
	 */
	public long getNotMoveUntil()
	{
		return _notMoveUntil;
	}

	/***
	 * Устанавливает время в мс которое персонаж не может перемещаться после разговора с NPC
	 */
	public void setNotMoveUntil()
	{
		_notMoveUntil = System.currentTimeMillis() + Config.PLAYER_MOVEMENT_BLOCK_TIME;
	}

	/***
	 * @return множитель бонусов на получение EXP
	 */
	public double getExpBonusMultiplier()
	{
		double bonus = 1.0;
		double vitality = 1.0;
		double bonusExp = 1.0;
		double premium = 1.0;

		// Bonus from Vitality System
		vitality = getStat().getVitalityMultiplier();

		// Бонус ПА
		premium = isPremiumState() ? Config.PREMIUM_EXPSP_RATE : 1.0;

		// Bonus exp from skills
		bonusExp = calcStat(Stats.BONUS_EXP, 1.0, null, null);

		if(vitality > 1.0)
		{
			bonus += vitality - 1;
		}
		if(bonusExp > 1.0)
		{
			bonus += bonusExp - 1;
		}
		if(premium > 1.0)
		{
			bonus += premium - 1;
		}

		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, Config.MAX_BONUS_EXP);

		return bonus;
	}

	/**
	 * @param target игрок для проверки
	 *
	 * @return находится ли пресонаж в той же группе с заданным игроком
	 */
	public boolean isInSameParty(L2PcInstance target)
	{
		return _party != null && target._party != null && _party.equals(target._party);
	}

	/**
	 * @param target игрок для проверки
	 *
	 * @return находится ли пресонаж в том же коммандном каннале с заданным игроком
	 */
	public boolean isInSameChannel(L2PcInstance target)
	{
		L2Party activeCharP = _party;
		L2Party targetP = target._party;
		if(activeCharP != null && targetP != null)
		{
			L2CommandChannel chan = activeCharP.getCommandChannel();
			if(chan != null && chan.equals(targetP.getCommandChannel()))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @param target игрок для проверки
	 *
	 * @return находится ли пресонаж в том же клане с заданным игроком
	 */
	public boolean isInSameClan(L2PcInstance target)
	{
		return _clanId != 0 && _clanId == target._clanId;
	}

	/**
	 * @param target игрок для проверки
	 *
	 * @return находится ли пресонаж в том же альянсе с заданным игроком
	 */
	public boolean isInSameAlly(L2PcInstance target)
	{
		return getAllyId() != 0 && getAllyId() == target.getAllyId();
	}

	/**
	 * @param target игрок для проверки
	 *
	 * @return находится ли пресонаж в войне с заданным игроком
	 */
	public boolean isInSameClanWar(L2PcInstance target)
	{
		L2Clan aClan = _clan;
		L2Clan tClan = target._clan;

		if(aClan != null && tClan != null)
		{
			if(aClan.isAtWarWith(tClan.getClanId()) || tClan.isAtWarWith(aClan.getClanId()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Служит для маркировки цели значком и броадкаста его группе
	 *
	 * @param target цель для маркировки
	 * @param sign   ID значка
	 */
	public void makeSign(L2Character target, int sign)
	{
		// Не можем метить себя или своего питомца
		if(target.equals(this) || target instanceof L2Summon && !_summons.isEmpty() && _summons.contains(target))
		{
			return;
		}

		L2Party party = _party;
		if(party != null)
		{
			party.addSign(sign, target);
		}
	}

	/**
	 * Быстрый переход к маркированному врагу из панели действий
	 *
	 * @param sign ID значка
	 */
	public void targetSign(int sign)
	{
		L2Character target = null;
		if(_party != null)
		{
			TIntObjectIterator<L2Character> iter = _party.getTargetSignList().iterator();
			while(iter.hasNext())
			{
				iter.advance();
				if(iter.key() == sign)
				{
					target = iter.value();
					break;
				}
			}
		}

		// TODO: Скорее всего нужны еще проверки
		if(target == null || target instanceof L2PcInstance && !((L2PcInstance) target)._isTargetable)
		{
			sendActionFailed();
		}
		else if(_observerController.isObserving())
		{
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			sendActionFailed();
		}
		else if(target.getInstanceId() != getInstanceId() && getInstanceId() != -1 || target instanceof L2PcInstance && ((L2PcInstance) target)._appearance.getInvisible() && !isGM())
		{
			sendActionFailed();
		}
		else
		{
			setTarget(target);
		}
	}

	/**
	 * TODO: Возможно стоит сделать кеш, чтобы постоянно не пробегаться по списку
	 * @return общее количество использованных саммонами очков призыва
	 */
	public int getUsedSummonPoints()
	{
		int points = 0;
		for(L2Summon summon : _summons)
		{
			// Если в списке присуствует старый пет - забиваем все доступные очки призыва
			if(summon.getPointsToSummon() == -1)
			{
				return getMaxSummonPoints();
			}

			points += summon.getPointsToSummon();
		}
		return points;
	}

	/**
	 * @return максимальное количество возможных очков призыва
	 */
	public int getMaxSummonPoints()
	{
		return (int) getStat().calcStat(Stats.SUMMON_POINTS, 0, null, null);
	}

	/**
	 * @return уровень мастерства призыва кубиков
	 */
	public int getCubicMastery()
	{
		return (int) getStat().calcStat(Stats.CUBIC_MASTERY, 0, null, null);
	}

	/**
	 * @return является-ли текущий персонаж наставником
	 */
	public boolean isMentor()
	{
		return MentorManager.getInstance().isMentor(getObjectId());
	}

	/**
	 * @return является-ли текущий персонаж учеником
	 */
	public boolean isMentee()
	{
		return MentorManager.getInstance().isMentee(getObjectId());
	}

	/**
	 * @return последний пришедший пинг от клиента
	 */
	public int getPing()
	{
		return _ping;
	}

	/**
	 * Устанавливает текущий пинг для игрока
	 *
	 * @param ping значение пинга
	 */
	public void setPing(int ping)
	{
		_ping = ping;
	}

	/**
	 * @return есть-ли в инвентаре итем воскрешающий после смерти
	 */
	public boolean canUseFeatherOfBlessing()
	{
		if(isAffected(CharEffectList.EFFECT_FLAG_FEATHER_OF_BLESSING))
		{
			return false;
		}

		for(L2ItemInstance item : _inventory.getItems())
		{
			if(item.isFeatherOfBlessing())
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Использование предмета, воскрешающего пассивно
	 */
	public void useFeatherOfBlessing()
	{
		for(L2ItemInstance item : _inventory.getItems())
		{
			if(item.isFeatherOfBlessing())
			{
				SkillHolder[] skills = item.getItem().getSkills();
				if(skills != null)
				{
					for(SkillHolder sh : skills)
					{
						sh.getSkill().getEffects(this, this);
					}
				}
				destroyItem(ProcessType.SKILL, item, 1, this, true);
				break;
			}
		}
	}

	/**
	 * @return может-ли персонаж кастовать с двух рук
	 */
	public boolean isCanDualCast()
	{
		return _canDualCast;
	}

	/**
	 * Устанавливает возможность кастовать с двух рук
	 * @param val
	 */
	public void setCanDualCast(boolean val)
	{
		_canDualCast = val;
	}

	public void updateWorldStatistic(CategoryType category, Object subCategory, long valueAdd)
	{
		WorldStatisticsManager.getInstance().updateStat(getObjectId(), category, subCategory, valueAdd);
	}

	// Получаем бинды игрока.
	public byte[] getBindConfigData()
	{
		return _bindConfig;
	}

	// Устанавливаем бинды игрока.
	public void setBindConfigData(byte[] data)
	{
		_bindConfigSavedInDB = false;
		_bindConfig = data;
	}

	public void storeBindConfigData()
	{
		if(_bindConfigSavedInDB)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.BINDS_STORE);

			statement.setInt(1, getObjectId());
			statement.setBytes(2, _bindConfig);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not store char binds data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void restoreBindConfigData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.BINDS_RESTORE);

			statement.setInt(1, getObjectId());
			rset = statement.executeQuery();

			if(rset.next())
			{
				_bindConfig = rset.getBytes("bind_data");
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore char binds data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Стартует таймер отсчета до окончания ПА
	 */
	public void startPremiumTask()
	{
		stopPremiumTask();

		if(_premiumTask == null)
		{
			_premiumTask = ThreadPoolManager.getInstance().scheduleGeneral(new PremiumTaskEnd(), getPremiumTime() - System.currentTimeMillis());
		}
	}

	/**
	 * Останавливает таймер отсчета до окончания ПА
	 */
	public void stopPremiumTask()
	{
		if(_premiumTask != null)
		{
			_premiumTask.cancel(true);
			_premiumTask = null;
		}
	}

	/**
	 * Устанавливает дату окончания ПА
	 * @param time
	 */
	public void setPremiumEndTime(long time)
	{
		if(_premiumTime == null)
		{
			_premiumTime = new AccountShareData(getAccountName(), "premium_end_time", String.valueOf(time));
		}

		_premiumTime.setValue(String.valueOf(time));
	}

	/**
	 * @return время (в мс) окончания ПА
	 */
	public long getPremiumTime()
	{
		return _premiumTime.getLongValue();
	}

	/**
	 * @return {@code true} если у персонажа еще не кончился ПА
	 */
	public boolean isPremiumState()
	{
		return _premiumTime.getLongValue() > System.currentTimeMillis();
	}

	/**
	 * @return {@code true} если персонажа можно выделить
	 */
	public boolean isTargetable()
	{
		return _isTargetable;
	}

	/**
	 * Устанавливает возможность выделить персонажа для персонажа
	 * @param val true/false
	 */
	public void setTargetable(boolean val)
	{
		_isTargetable = val;
	}

	/***
	 * Проиграть игроку указанный звук
	 * @param sound QuestSound звуковая дорожка
	 */
	public void playSound(QuestSound sound)
	{
		sendPacket(sound.getPacket());
	}

	public void TvTIncreasePvPs()
	{
		++_tvtPvpKills;
	}

	public void TvTSetKills(int var)
	{
		_tvtPvpKills = var;
	}

	public int TvTGetPvPs()
	{
		return _tvtPvpKills;
	}

	public void TvTIncreasePvPsWithoutDie()
	{
		++_tvtPvpKillSteak;
	}

	public void TvTSetKillsWithoutDie(int var)
	{
		_tvtPvpKillSteak = var;
	}

	public int TvTGetPvPsWithoutDie()
	{
		return _tvtPvpKillSteak;
	}

	public boolean isCtfFlagEquipped()
	{
		return _ctfFlagEquipped;
	}

	public void setCtfFlagEquipped(boolean ctfFlagEquipped)
	{
		_ctfFlagEquipped = ctfFlagEquipped;
	}

	/***
	 * @return time stamp последнего действия игрока
	 */
	public long getIdleFromTime()
	{
		return System.currentTimeMillis() - _idleFromTime;
	}

	public Map<Integer, Long> getInstanceReuses()
	{
		return InstanceManager.getInstance().getAllInstanceTimes(getObjectId());
	}

	/***
	 * @return ObjectID последнего НПЦ, от которого был показан список с телепортами
	 */
	public int getLastTeleporterObjectId()
	{
		return _lastTeleporterObjectId;
	}

	/***
	 * Устанавливает флаг с ObjectID НПЦ, который показывает игроку список телепортов
	 * @param objectId ObjectID телепортирующего НПЦ + номер ТП листа у этого НПЦ
	 */
	public void setLastTeleporterObjectId(int objectId)
	{
		_lastTeleporterObjectId = objectId;
	}

	/***
	 * @return информацию о виталити для мейна и всех сабов персонажа
	 */
	public Map<Integer, VitalityHolder> getVitalityData()
	{
		return _vitalityData;
	}

	/***
	 * @return количество виталити для текущего classIndex игрока
	 */
	public VitalityHolder getVitalityDataForCurrentClassIndex()
	{
		// Если в прогруженных данных уже есть нужный classIndex
		if(_vitalityData.containsKey(_classIndex))
		{
			return _vitalityData.get(_classIndex);
		}
		// Если данных нет, то создаем новую запись для текущего classIndex
		else
		{
			_vitalityData.put(_classIndex, new VitalityHolder(PcStat.MAX_VITALITY_POINTS, Config.VITALITY_ITEMS_WEEKLY_LIMIT));

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Characters.VITALITY_ADD);

				statement.setInt(1, _charId);
				statement.setInt(2, _classIndex);
				statement.setInt(3, PcStat.MAX_VITALITY_POINTS);
				statement.setInt(4, Config.VITALITY_ITEMS_WEEKLY_LIMIT);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Could not insert vitality data for charId: " + _charId + " and classIndex: " + _classIndex, e);
				return null;
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
			return _vitalityData.get(_classIndex);
		}
	}

	public void setVitalityPoints(int points)
	{
		getStat().setVitalityPoints(points);
	}

	/**
	 * При использовании итема, восстанавливающего виталити, уменьшаем счетчик доступных
	 */
	public void decreaseVitalityItemsLeft()
	{
		int left = getVitalityDataForCurrentClassIndex().getVitalityItems();
		if(left > 0)
		{
			left--;
		}
		getVitalityDataForCurrentClassIndex().setVitalityItems(left);
		sendPacket(new ExVitalityEffectInfo(this));
	}

	public void updateVitalityPoints(float points, int killedLevel, boolean useRates, boolean quiet)
	{
		getStat().updateVitalityPoints(points, killedLevel, useRates, quiet);
	}

	public void loadVitalityData()
	{
		_vitalityData.clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.VITALITY_RESTORE);
			statement.setInt(1, _charId);
			rset = statement.executeQuery();
			while(rset.next())
			{
				_vitalityData.put(rset.getInt("class_index"), new VitalityHolder(rset.getInt("vitality_points"), rset.getInt("vitality_items")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not restore char vitality data: ", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public void saveVitalityData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.VITALITY_UPDATE);
			for(Map.Entry<Integer, VitalityHolder> entry : _vitalityData.entrySet())
			{
				statement.setLong(1, entry.getValue().getVitalityPoints());
				statement.setInt(2, entry.getValue().getVitalityItems());
				statement.setInt(3, _charId);
				statement.setInt(4, entry.getKey());
				statement.executeUpdate();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not save vitality data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public DeathPenaltyController getDeathPenaltyController()
	{
		return _deathPenaltyController;
	}

	public RecipeBookController getRecipeController()
	{
		return _recipeBookController;
	}

	public ShortcutController getShortcutController()
	{
		return _shortcutController;
	}

	public SummonFriendController getSummonFriendController()
	{
		return _summonFriendController;
	}

	public OlympiadController getOlympiadController()
	{
		return _olympiadController;
	}

	public ObserverController getObserverController()
	{
		return _observerController;
	}

	public StateController getStateController()
	{
		return _stateController;
	}

	public EventController getEventController()
	{
		return _eventController;
	}

	public CharacterVariablesController getVariablesController()
	{
		return variablesController;
	}

    public boolean isHairAccessoryEnabled()
    {
        return variablesController.get("hairAccessoryEnabled", Boolean.class, true);
    }

    public void setHairAccessoryEnabled(boolean enabled)
    {
        variablesController.set("hairAccessoryEnabled", enabled);
    }

    public int getWorldChatPoints()
    {
        return variablesController.get(WORLD_CHAT_VARIABLE_NAME, Integer.class, 1);
    }

    public void setWorldChatPoints(int points)
    {
        variablesController.set(WORLD_CHAT_VARIABLE_NAME, points);
    }

    /**
	 * Skill casting information (used to queue when several skills are cast in
	 * a short time)
	 */
	public static class SkillDat
	{
		private final L2Skill _skill;
		private final boolean _ctrlPressed;
		private final boolean _shiftPressed;

		protected SkillDat(L2Skill skill, boolean ctrlPressed, boolean shiftPressed)
		{
			_skill = skill;
			_ctrlPressed = ctrlPressed;
			_shiftPressed = shiftPressed;
		}

		public boolean isCtrlPressed()
		{
			return _ctrlPressed;
		}

		public boolean isShiftPressed()
		{
			return _shiftPressed;
		}

		public L2Skill getSkill()
		{
			return _skill;
		}

		public int getSkillId()
		{
			return _skill != null ? _skill.getId() : -1;
		}
	}

	private static class GatesRequest
	{
		private L2DoorInstance _target;

		public void setTarget(L2DoorInstance door)
		{
			_target = door;
		}

		public L2DoorInstance getDoor()
		{
			return _target;
		}
	}

	public class AIAccessor extends L2Character.AIAccessor
	{
		protected AIAccessor()
		{

		}

		public L2PcInstance getPlayer()
		{
			return L2PcInstance.this;
		}

		public void doPickupItem(L2Object object)
		{
			getPlayer().doPickupItem(object);
		}

		public void doInteract(L2Character target)
		{
			getPlayer().doInteract(target);
		}

		@Override
		public void doAttack(L2Character target)
		{
			super.doAttack(target);

			// cancel the recent fake-death protection instantly if the player
			// attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
		}

		@Override
		public void doCast(L2Skill skill)
		{
			super.doCast(skill);

			// cancel the recent fake-death protection instantly if the player
			// attacks or casts spells
			getPlayer().setRecentFakeDeath(false);
		}
	}

	/**
	 * Task for Herbs
	 */
	private class HerbTask implements Runnable
	{
		private final ProcessType _process;
		private final int _itemId;
		private final long _count;
		private final L2Object _reference;
		private final boolean _sendMessage;

		HerbTask(ProcessType process, int itemId, long count, L2Object reference, boolean sendMessage)
		{
			_process = process;
			_itemId = itemId;
			_count = count;
			_reference = reference;
			_sendMessage = sendMessage;
		}

		@Override
		public void run()
		{
			try
			{
				addItem(_process, _itemId, _count, _reference, _sendMessage);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "", e);
			}
		}
	}

	private class ShortBuffTask implements Runnable
	{
		@Override
		public void run()
		{
			sendPacket(new ShortBuffStatusUpdate(0, 0, 0));
			setShortBuffTaskSkillId(0);
		}
	}

	/**
	 * Sit down Task
	 */
	private class SitDownTask implements Runnable
	{
		@Override
		public void run()
		{
			setIsParalyzed(false);
		}
	}

	/**
	 * Stand up Task
	 */
	private class StandUpTask implements Runnable
	{
		@Override
		public void run()
		{
			setIsSitting(false);
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}

	private class GameGuardCheck implements Runnable
	{
		@Override
		public void run()
		{
			L2GameClient client = getClient();
			if(client != null && !client.isAuthedGG() && isOnline())
			{
				AdminTable.getInstance().broadcastMessageToGMs("Client " + client + " failed to reply GameGuard query and is being kicked!");
				_log.log(Level.INFO, "Client " + client + " failed to reply GameGuard query and is being kicked!");
				client.close(LogOutOk.STATIC_PACKET);
			}
		}
	}

	private class InventoryEnable implements Runnable
	{
		@Override
		public void run()
		{
			_inventoryDisable = false;
		}
	}

	protected class WarnUserTakeBreak implements Runnable
	{
		@Override
		public void run()
		{
			if(isOnline())
			{
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_BEEN_PLAYING_FOR_AN_EXTENDED_PERIOD_OF_TIME_PLEASE_CONSIDER_TAKING_A_BREAK).addNumber(getHoursInGame()));
			}
			else
			{
				stopWarnUserTakeBreak();
			}
		}
	}

    protected class WarnChatTask implements Runnable
    {
        @Override
        public void run()
        {
            if(isOnline())
            {
                sendPacket(SystemMessage.getSystemMessage(4207));
            }
            else
            {
                stopWarnChatTask();
            }
        }
    }

	private class WaterTask implements Runnable
	{
		@Override
		public void run()
		{
			double reduceHp = getMaxHp() / 100.0;

			if(reduceHp < 1)
			{
				reduceHp = 1;
			}

			reduceCurrentHp(reduceHp, L2PcInstance.this, false, false, null);
			// reduced hp, becouse not rest
			sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1).addNumber((int) reduceHp));
		}
	}

	private class LookingForFishTask implements Runnable
	{
		boolean _isNoob;
		boolean _isUpperGrade;
		int _fishGroup;
		double _fishGutsCheck;
		long _endTaskTime;

		protected LookingForFishTask(int startCombatTime, double fishGutsCheck, int fishGroup, boolean isNoob, boolean isUpperGrade)
		{
			_fishGutsCheck = fishGutsCheck;
			_endTaskTime = System.currentTimeMillis() + startCombatTime * 1000 + 10000;
			_fishGroup = fishGroup;
			_isNoob = isNoob;
			_isUpperGrade = isUpperGrade;
		}

		@Override
		public void run()
		{
			if(System.currentTimeMillis() >= _endTaskTime)
			{
				endFishing(false);
				return;
			}
			if(_fishGroup == -1)
			{
				return;
			}
			if(Rnd.getChance(_fishGutsCheck))
			{
				stopLookingForFishTask();
				startFishCombat(_isNoob, _isUpperGrade);
			}
		}
	}

	private class TeleportWatchdog implements Runnable
	{
		private final L2PcInstance _player;

		TeleportWatchdog()
		{
			_player = L2PcInstance.this;
		}

		@Override
		public void run()
		{
			if(_player == null || !_player.isTeleporting())
			{
				return;
			}
			_player.onTeleported();
		}
	}

	private class PunishTask implements Runnable
	{
		@Override
		public void run()
		{
			setPunishLevel(PlayerPunishLevel.NONE, 0);
		}
	}

	private class FameTask implements Runnable
	{
		private final L2PcInstance _player;
		private final int _value;

		protected FameTask(int value)
		{
			_player = L2PcInstance.this;
			_value = value;
		}

		@Override
		public void run()
		{
			if(_player == null || _player.isDead() && !Config.FAME_FOR_DEAD_PLAYERS)
			{
				return;
			}
			if((_player.getClient() == null || _player.getClient().isDetached()) && !Config.OFFLINE_FAME)
			{
				return;
			}
			_player.setFame(_player.getFame() + _value);
			_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_REPUTATION_SCORE).addNumber(_value));
			_player.sendUserInfo();
		}
	}

	private class SoulTask implements Runnable
	{
		@Override
		public void run()
		{
			clearSouls();
		}
	}

	/**
	 * Основной таск кормления питомца
	 */
	private class FeedTask implements Runnable
	{
		private final WeakReference<L2Summon> _petRef;
		private final boolean _feedMounted;

		public FeedTask(L2Summon pet)
		{
			_petRef = new WeakReference(pet);
			_feedMounted = false;
		}

		public FeedTask()
		{
			_petRef = null;
			_feedMounted = true;
		}

		@Override
		public void run()
		{
			if(getMountNpcId() <= 0)
			{
				if(_mountFeedTask != null)
				{
					_mountFeedTask.cancel(true);
				}

				return;
			}

			L2Summon pet = _petRef != null ? _petRef.get() : null;
			if(pet == null && !_feedMounted)
			{
				if(_mountFeedTask != null)
				{
					_mountFeedTask.cancel(true);
				}

				stopFeed();
				return;
			}

			try
			{
				if(pet != null && !pet.isPet() && !isMounted())
				{
					if(_mountFeedTask != null)
					{
						_mountFeedTask.cancel(true);
					}

					stopFeed();
					return;
				}

				int currentFeed;
				int feedConsume;
				if(_feedMounted)
				{
					currentFeed = getCurrentFeed();
					feedConsume = getFeedConsume(_mountNpcId, _mountLevel);
				}
				else
				{
					currentFeed = pet.getPetInstance().getCurrentFed();
					feedConsume = getFeedConsume(pet.getNpcId(), pet.getLevel());
				}

				if(currentFeed > feedConsume)
				{
					// кушаем корм
					if(pet == null)
					{
						setCurrentFeed(currentFeed - feedConsume);
					}
					else
					{
						pet.getPetInstance().setCurrentFed(currentFeed - feedConsume);
					}
				}
				else
				{
					// Если еды не хватает, отзываем животное
					setCurrentFeed(0);
					stopFeed();
					if(isMounted())
					{
						dismount();
					}
					else
					{
						pet.getLocationController().decay();
					}
					sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED);
				}

				int[] foodIds = {};
				foodIds = pet != null ? PetDataTable.getInstance().getPetData(pet.getNpcId()).getFood() : getPetData(_mountNpcId).getFood();

				if(foodIds.length == 0)
				{
					return;
				}
				L2ItemInstance food = null;
				for(int id : foodIds)
				{
					//TODO: possibly pet inv?
					food = getInventory().getItemByItemId(id);
					if(food != null)
					{
						break;
					}
				}

				if(food != null && isHungry())
				{
					IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
					if(handler != null)
					{
						handler.useItem(L2PcInstance.this, food, false);
						sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food.getItemId()));
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Mounted Pet [NpcId: " + getMountNpcId() + "] a feed task error has occurred", e);
			}
		}
	}

	/**
	 * Таск на дисмаунт с ездового животного
	 */
	private class Dismount implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				dismount();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception on dismount(): " + e.getMessage(), e);
			}
		}
	}

	private class ChargeTask implements Runnable
	{

		@Override
		public void run()
		{
			clearCharges();
		}
	}

	private class RecommendationGiveTask implements Runnable
	{
		@Override
		public void run()
		{
			int recommendationToGive;

			// Начисляем +10 рекоммендаций первые два часа онлайн, 3-ий и последующий начисляем по +1
			if(_recomendationTwoHoursGiven < 2)
			{
				recommendationToGive = 10;
				_recomendationTwoHoursGiven++;
			}
			else
			{
				recommendationToGive = 1;
			}

			setRecommendationsLeft(getRecommendationsLeft() + recommendationToGive);

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS);
			sm.addNumber(recommendationToGive);
			sendPacket(sm);
			sendUserInfo();
		}
	}

	private class PremiumTaskEnd implements Runnable
	{
		@Override
		public void run()
		{
			setPremiumEndTime(0);
			sendPacket(new ExBR_PremiumState(getObjectId(), 0));
			sendMessage("Срок действия Вашего премиум аккаунта истек.");
		}
	}

    public int getAbilityPoints()
    {
        return variablesController.get("ABILITY_POINTS", Integer.class, 0);
    }

    public void setAbilityPoints(int points)
    {
        variablesController.set("ABILITY_POINTS", points);
    }

    public int getAbilityPointsUsed()
    {
        return variablesController.get("ABILITY_POINTS_USED", Integer.class, 0);
    }

    public void setAbilityPointsUsed(int points)
    {
        variablesController.set("ABILITY_POINTS_USED", points);
    }

    private volatile Map<Class<? extends AbstractRequest>, AbstractRequest> _requests;
    
    /**
     * @param request
     * @return {@code true} if the request was registered successfully, {@code false} otherwise.
     */
    public boolean addRequest(AbstractRequest request)
    {
        if (_requests == null)
        {
            synchronized (this)
            {
                if (_requests == null)
                {
                    _requests = new ConcurrentHashMap<>();
                }
            }
        }
        return canRequest(request) && (_requests.putIfAbsent(request.getClass(), request) == null);
    }

    public boolean canRequest(AbstractRequest request)
    {
        return (_requests != null) && _requests.values().stream().allMatch(request::canWorkWith);
    }

    /**
     * @param clazz
     * @return {@code true} if request was successfully removed, {@code false} in case processing set is not created or not containing the request.
     */
    public boolean removeRequest(Class<? extends AbstractRequest> clazz)
    {
        return (_requests != null) && (_requests.remove(clazz) != null);
    }

    /**
     * @param <T>
     * @param requestClass
     * @return object that is instance of {@code requestClass} param, {@code null} if not instance or not set.
     */
    public <T extends AbstractRequest> T getRequest(Class<T> requestClass)
    {
        return _requests != null ? requestClass.cast(_requests.get(requestClass)) : null;
    }

    /**
     * @return {@code true} if player has any processing request set, {@code false} otherwise.
     */
    public boolean hasRequests()
    {
        return (_requests != null) && !_requests.isEmpty();
    }

    public boolean hasItemRequest()
    {
        return (_requests != null) && _requests.values().stream().anyMatch(AbstractRequest::isItemRequest);
    }

    /**
     * @param requestClass
     * @param classes
     * @return {@code true} if player has the provided request and processing it, {@code false} otherwise.
     */
    @SafeVarargs
    public final boolean hasRequest(Class<? extends AbstractRequest> requestClass, Class<? extends AbstractRequest>... classes)
    {
        if (_requests != null)
        {
            for (Class<? extends AbstractRequest> clazz : classes)
            {
                if (_requests.containsKey(clazz))
                {
                    return true;
                }
            }
            return _requests.containsKey(requestClass);
        }
        return false;
    }

    /**
     * @param objectId
     * @return {@code true} if item object id is currently in use by some request, {@code false} otherwise.
     */
    public boolean isProcessingItem(int objectId)
    {
        return (_requests != null) && _requests.values().stream().anyMatch(req -> req.isUsing(objectId));
    }

    /**
     * Removing all requests associated with the item object id provided.
     * @param objectId
     */
    public void removeRequestsThatProcessesItem(int objectId)
    {
        if (_requests != null)
        {
            _requests.values().removeIf(req -> req.isUsing(objectId));
        }
    }
}