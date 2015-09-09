package dwo.gameserver;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.cache.CrestCache;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.BuffStackGroupData;
import dwo.gameserver.datatables.sql.*;
import dwo.gameserver.datatables.xml.*;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.engine.geodataengine.PathFinding;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.handler.*;
import dwo.gameserver.instancemanager.*;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.instancemanager.castle.CastleMercTicketManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.instancemanager.fort.FortSiegeManager;
import dwo.gameserver.instancemanager.vehicle.AirShipManager;
import dwo.gameserver.instancemanager.vehicle.BoatManager;
import dwo.gameserver.instancemanager.vehicle.ShuttleManager;
import dwo.gameserver.instancemanager.votemanager.L2TopManager;
import dwo.gameserver.instancemanager.votemanager.MMOTopManager;
import dwo.gameserver.model.items.EnchantEffectTable;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.player.duel.DuelManager;
import dwo.gameserver.model.player.formation.group.PartyMatchRoomList;
import dwo.gameserver.model.player.formation.group.PartyMatchWaitingList;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.world.npc.spawn.AutoSpawnHandler;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.model.world.worldstat.WorldStatisticsManager;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.L2GamePacketHandler;
import dwo.gameserver.network.mmocore.SelectorConfig;
import dwo.gameserver.network.mmocore.SelectorThread;
import dwo.gameserver.taskmanager.manager.AutoAnnounceTaskManager;
import dwo.gameserver.taskmanager.manager.KnownListUpdateTaskManager;
import dwo.gameserver.taskmanager.manager.TaskManager;
import dwo.gameserver.util.Colors;
import dwo.gameserver.util.DeadLockDetector;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.network.IPv4Filter;
import dwo.xmlrpcserver.XMLRPCServer;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;

import static dwo.gameserver.util.Tools.printSection;

public class GameServerStartup
{
	public static final Calendar dateTimeServerStarted = Calendar.getInstance();
	private static final Logger _log = LogManager.getLogger(GameServerStartup.class);
	public static GameServerStartup gameServer;
	private final SelectorThread<L2GameClient> _selectorThread;
	private final L2GamePacketHandler _gamePacketHandler;
	private final DeadLockDetector _deadDetectThread;

	public GameServerStartup() throws Exception
	{
		long serverLoadStart = System.currentTimeMillis();

		gameServer = this;

		printSection( "Database Engine" );
		L2DatabaseFactory.getInstance();

		printSection( "IDFactory Engine" );
		IdFactory.getInstance();
		_log.log( Level.INFO, "IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size() );

		ThreadPoolManager.getInstance();

		printSection( "Engines" );
		ScriptsManager.getInstance();
		ServerPacketOpCodeManager.getInstance();

		printSection( "World Engine" );
        Colors.loadColors();
		GameTimeController.init();
		InstanceManager.getInstance();
		WorldManager.getInstance();
		MapRegionManager.getInstance();
		Announcements.getInstance();
		GlobalVariablesManager.getInstance();
		AccountShareDataTable.getInstance();
		DynamicSpawnData.getInstance();
		ResidenceFunctionData.getInstance();

		printSection( "Skills Engine" );
		BuffStackGroupData.getInstance();
		EnchantSkillGroupsTable.getInstance();
        SkillTable.getInstance().loadClient();
        SkillTable.getInstance().load( false );
		SkillTreesData.getInstance();

		printSection( "Items Engine" );
        ItemTable.getInstance().loadClient();
        ItemTable.getInstance().load( false );
        SummonItemsData.getInstance();
		EnchantBonusData.getInstance();
		BuyListData.getInstance();
		MultiSellData.getInstance();
		RecipeData.getInstance();
		//PrimeShopTable.getInstance(); TODO
		ArmorSetsTable.getInstance();
		FishData.getInstance();
		FishingRodsData.getInstance();
		EnchantItemData.getInstance();
		EnchantEffectTable.getInstance();
		CrystallizationData.getInstance();
		SoulCrystalData.getInstance();
		ShapeShiftingItemsData.getInstance();
		HennaTable.getInstance();
		HennaTreeTable.getInstance();
		AugmentationData.getInstance();
        ItemPriceData.getInstance();
        AbilityPointsData.getInstance();
        AlchemyDataTable.getInstance();

		printSection( "Characters Engine" );
		CharTemplateTable.getInstance();
		ClassTemplateTable.getInstance();
		CharNameTable.getInstance();
		ExperienceTable.getInstance();
		AdminTable.getInstance();
		RaidBossPointsManager.getInstance();
		RelationListManager.getInstance();
		PetDataTable.getInstance();
		CharSummonTable.getInstance();
		SummonPointsTable.getInstance();
		HitConditionBonus.getInstance();
		ObsceneFilterTable.getInstance();

		printSection( "Clans Engine" );
		ClanTable.getInstance();
		ClanTable.getInstance().restoreWars();
		ClanHallSiegeManager.getInstance();
		ClanHallManager.getInstance();
		AuctionManager.getInstance();

		printSection( "Geodata Engine" );
		GeoEngine.init();
		PathFinding.init();
		DoorGeoEngine.init();

		printSection( "NPCs Engine" );
		HerbDropTable.getInstance();
		NpcTable.getInstance();
		AutoChatDataTable.getInstance();
		NpcWalkerRoutesData.getInstance();
		WalkingManager.getInstance();
		ZoneManager.getInstance();
		StaticObjectsData.getInstance();
		ItemAuctionManager.getInstance();
		CastleManager.getInstance();
		FortManager.getInstance().init();
		SpawnTable.getInstance();
		AutoSpawnHandler.getInstance();
		HellboundManager.getInstance();
		RaidBossSpawnManager.getInstance();
		DayNightSpawnManager.getInstance().trim().notifyChangeMode();
		GrandBossManager.getInstance().initZones();
		FourSepulchersManager.getInstance().init();
		TeleportListTable.getInstance();
		BeautyShopData.getInstance();
		CustomDropListDataXml.getInstance();

		printSection("Residence Siege Engine");
		CastleSiegeManager.getInstance().getSieges();
		FortSpawnList.getInstance();
		FortSiegeManager.getInstance();
		CastleManorManager.getInstance();
		CastleMercTicketManager.getInstance();
		ManorData.getInstance();
		ResidenceSiegeMusicList.getInstance();

		printSection("Olympiad Engine");
		Olympiad.getInstance();
		HeroManager.getInstance();

		printSection("Cache Engine");
		CrestCache.getInstance();
		HtmCache.getInstance();

		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		CommunityBuffTable.getInstance();
		CommunityTeleportData.getInstance();

		printSection("Mods Engine");
		PcCafePointsManager.getInstance();

		if(Config.MMO_TOP_MANAGER_ENABLED)
		{
			MMOTopManager.getInstance();
		}
		if(Config.L2_TOP_MANAGER_ENABLED)
		{
			L2TopManager.getInstance();
		}

		printSection("Handlers Engine");
		ActionHandler.getInstance();
		ActionShiftHandler.getInstance();
		AdminCommandHandler.getInstance();
		BypassCommandManager.getInstance();
		ChatCommandManager.getInstance();
		EffectHandler.getInstance();
		ItemHandler.getInstance();
		SkillHandler.getInstance();
		TargetHandler.getInstance();
		TransformHandler.getInstance();
		UserCommandManager.getInstance();
		VoicedHandlerManager.getInstance();

		printSection("Transformations Engine");
		TransformationManager.getInstance();
		TransformationManager.getInstance().report();

		printSection("Jump Engine");
		CharJumpRoutesTable.getInstance();

		printSection("Commission Engine");
		CommissionManager.getInstance();

		printSection("ClanSearch Engine");
		ClanSearchManager.getInstance();

		printSection("Awakening Engine");
		AwakeningManager.getInstance();

		printSection("World Statistics Engine");
		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			WorldStatisticsManager.getInstance();
		}
		else
		{
			_log.log(Level.INFO, "World Statistic Engine Disabled");
		}

		printSection("Quests Engine");
		QuestManager.getInstance();
		DynamicQuestManager.getInstance();
		BoatManager.getInstance();
		AirShipManager.getInstance();
		ShuttleManager.getInstance();
		GraciaSeedsManager.getInstance();
		if(Config.ALLOW_WEDDING)
		{
			WeddingManager.getInstance();
		}
		AutoChatDataTable.getInstance().setAutoChatActive(true);

		printSection("Scripts Engine");
		ScriptsManager.getInstance().executeCoreScripts();

		TaskManager.getInstance();

		QuestManager.getInstance().report();

		if(Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance();
		}

		if(Config.AUTODESTROY_ITEM_AFTER > 0 || Config.HERB_AUTO_DESTROY_TIME > 0)
		{
			ItemsOnGroundAutoDestroyManager.getInstance();
		}

		CastleManager.getInstance().spawnDoors();
		FortManager.getInstance().spawnDoors();

		if(Config.ALLOW_MAIL)
		{
			MailManager.getInstance();
		}

		MentorManager.getInstance();
		DuelManager.getInstance();

		Runtime.getRuntime().addShutdownHook(GameServerShutdown.getInstance());

		_log.log(Level.INFO, "IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		EventManager.getEventsInstances();

		KnownListUpdateTaskManager.getInstance();

		if((Config.OFFLINE_TRADE_ENABLE || Config.OFFLINE_CRAFT_ENABLE) && Config.RESTORE_OFFLINERS)
		{
			OfflineTradersTable.restoreOfflineTraders();
		}

		if(Config.DEADLOCK_DETECTOR)
		{
			_deadDetectThread = new DeadLockDetector();
			_deadDetectThread.setDaemon(true);
			_deadDetectThread.start();
		}
		else
		{
			_deadDetectThread = null;
		}

		printSection("Finalization");
		System.runFinalization();
		System.gc();
		Util.printMemoryInfo();
		Util.printCpuInfo();
		Util.printOSInfo();
		Toolkit.getDefaultToolkit().beep();

		printSection("Server Thread");
		LoginServerThread.getInstance().start();

		SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		sc.TCP_NODELAY = Config.MMO_TCP_NODELAY;

		_gamePacketHandler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, _gamePacketHandler, _gamePacketHandler, _gamePacketHandler, new IPv4Filter());

		InetAddress bindAddress = null;
		if(!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch(UnknownHostException e1)
			{
				_log.log(Level.ERROR, "GameServerStartup: The GameServer bind address is invalid, using all avaliable IPs. Reason: " + e1.getMessage(), e1);
			}
		}

		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch(IOException e)
		{
			_log.log(Level.FATAL, "GameServerStartup:: Failed to open server socket. Reason: " + e.getMessage(), e);
			System.exit(1);
		}
		_selectorThread.start();
		_log.log(Level.INFO, "Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		long serverLoadEnd = System.currentTimeMillis();
		_log.log(Level.INFO, "Server Loaded in " + (serverLoadEnd - serverLoadStart) / 1000L + " seconds");

		AutoAnnounceTaskManager.getInstance();

		XMLRPCServer.getInstance();
	}

	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
}