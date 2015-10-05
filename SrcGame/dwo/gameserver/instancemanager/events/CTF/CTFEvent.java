package dwo.gameserver.instancemanager.events.CTF;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.events.EventState;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.Say2;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBlockUpSetList;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBlockUpSetState;
import dwo.gameserver.network.game.serverpackets.packet.info.ExUserInfoInvenWeight;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CTFEvent
{
	protected static final Logger _log = LogManager.getLogger(CTFEvent.class);
	private static final String htmlPath = "mods/CTFEvent/";
	protected static CTFEventTeam[] _team = new CTFEventTeam[2];
	private static EventState _state = EventState.INACTIVE;
	private static L2Spawn _npcSpawn;
	private static L2Spawn _t1FlagSpawn;
	private static L2Spawn _t2FlagSpawn;
	private static L2Npc _team1FlagSpawn;
	private static L2Npc _team2FlagSpawn;
	private static L2Npc _lastNpcSpawn;
	private static int _CTFEventInstance;
	private static long _startedTime;
	private static ScheduledFuture<?> _respawnTask;
	private static ScheduledFuture<?> _flagTask;
	private static ScheduledFuture<?> _idleTask;

	public static void init()
	{
		_team[0] = new CTFEventTeam(0, ConfigEventCTF.CTF_EVENT_TEAM_1_NAME, ConfigEventCTF.CTF_EVENT_TEAM_1_COORDINATES);
		_team[1] = new CTFEventTeam(1, ConfigEventCTF.CTF_EVENT_TEAM_2_NAME, ConfigEventCTF.CTF_EVENT_TEAM_2_COORDINATES);
		CTFHook hook = new CTFHook();
		HookManager.getInstance().addHook(HookType.ON_ITEM_PICKUP, hook);
	}

	public static boolean startParticipation()
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_ID);
		if(npc == null)
		{
			_log.log(Level.WARN, "CTFEventEngine[CTFEvent.startParticipation()]: L2NpcTemplate is Null, probably invalid npc id in the config?");
			return false;
		}

		try
		{
			_npcSpawn = new L2Spawn(npc);
			_npcSpawn.setLocx(ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);
			_npcSpawn.setHeading(0);
			_npcSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_npcSpawn);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("Захват флага");
			_lastNpcSpawn.isAggressive();
			_lastNpcSpawn.getLocationController().decay();
			_lastNpcSpawn.getLocationController().spawn(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 2025, 1, 1, 1));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CTFEventEngine[CTFEvent.startParticipation()]: exception: " + e);
			return false;
		}

		setState(EventState.PARTICIPATING);
		return true;
	}

	public static void spawnFlagNpcs(int instanceId)
	{
		L2NpcTemplate npc = NpcTable.getInstance().getTemplate(ConfigEventCTF.CTF_EVENT_TEAM_1_FLAG_NPC_ID);
		if(npc == null)
		{
			_log.log(Level.ERROR, "CTFEventEngine[CTFEvent.spawnFlagNpcs()]: L2NpcTemplate is Null, probably invalid npc id in the config?");
			return;
		}

		try
		{
			_t1FlagSpawn = new L2Spawn(npc);
			_t1FlagSpawn.setLocx(ConfigEventCTF.CTF_EVENT_TEAM_1_FLAG_COORDINATES[0]);
			_t1FlagSpawn.setLocy(ConfigEventCTF.CTF_EVENT_TEAM_1_FLAG_COORDINATES[1]);
			_t1FlagSpawn.setLocz(ConfigEventCTF.CTF_EVENT_TEAM_1_FLAG_COORDINATES[2]);
			_t1FlagSpawn.setInstanceId(instanceId);
			_t1FlagSpawn.setAmount(1);
			_t1FlagSpawn.setHeading(0);
			_t1FlagSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_t1FlagSpawn);
			_t1FlagSpawn.init();
			_team1FlagSpawn = _t1FlagSpawn.getLastSpawn();
			_team1FlagSpawn.setCurrentHp(_team1FlagSpawn.getMaxHp());
			_team1FlagSpawn.setTitle(_team[0].getName());
			_team1FlagSpawn.getLocationController().decay();
			_team1FlagSpawn.getLocationController().spawn(_t1FlagSpawn.getLastSpawn().getX(), _t1FlagSpawn.getLastSpawn().getY(), _t1FlagSpawn.getLastSpawn().getZ());

			npc = NpcTable.getInstance().getTemplate(ConfigEventCTF.CTF_EVENT_TEAM_2_FLAG_NPC_ID);
			if(npc == null)
			{
				_log.log(Level.ERROR, "CTFEventEngine[CTFEvent.spawnFlagNpcs()]: L2NpcTemplate is Null, probably invalid npc id in the config?");
				return;
			}
			_t2FlagSpawn = new L2Spawn(npc);
			_t2FlagSpawn.setLocx(ConfigEventCTF.CTF_EVENT_TEAM_2_FLAG_COORDINATES[0]);
			_t2FlagSpawn.setLocy(ConfigEventCTF.CTF_EVENT_TEAM_2_FLAG_COORDINATES[1]);
			_t2FlagSpawn.setLocz(ConfigEventCTF.CTF_EVENT_TEAM_2_FLAG_COORDINATES[2]);
			_t2FlagSpawn.setInstanceId(instanceId);
			_t2FlagSpawn.setAmount(1);
			_t2FlagSpawn.setHeading(0);
			_t2FlagSpawn.setRespawnDelay(1);
			SpawnTable.getInstance().addNewSpawn(_t2FlagSpawn);
			_t2FlagSpawn.init();
			_team2FlagSpawn = _t2FlagSpawn.getLastSpawn();
			_team2FlagSpawn.setCurrentHp(_team2FlagSpawn.getMaxHp());
			_team2FlagSpawn.setTitle(_team[1].getName());
			_team2FlagSpawn.getLocationController().decay();
			_team2FlagSpawn.getLocationController().spawn(_t2FlagSpawn.getLastSpawn().getX(), _t2FlagSpawn.getLastSpawn().getY(), _t2FlagSpawn.getLastSpawn().getZ());
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "CTFEventEngine[CTFEvent.startParticipation()]: exception: ", e);
		}
	}

	private static int highestLevelPlayerId(Map<Integer, L2PcInstance> players)
	{
		int maxLevel = Integer.MIN_VALUE;
		int maxLevelId = -1;
		for(L2PcInstance player : players.values())
		{
			if(player.getLevel() >= maxLevel)
			{
				maxLevel = player.getLevel();
				maxLevelId = player.getObjectId();
			}
		}
		return maxLevelId;
	}

	public static boolean startFight()
	{
		setState(EventState.STARTING);

		_startedTime = System.currentTimeMillis() + ConfigEventCTF.CTF_EVENT_RUNNING_TIME * 60 * 1000;
		int timeLeft = (int) ((_startedTime - System.currentTimeMillis()) / 1000);

		Map<Integer, L2PcInstance> allRegistered = new FastMap<>();
		allRegistered.putAll(_team[0].getRegisteredPlayers());
		allRegistered.putAll(_team[1].getRegisteredPlayers());
		_team[0].clean();
		_team[1].clean();
		int[] balance = {0, 0};
		int priority = 0;
		int highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
		while(!allRegistered.isEmpty())
		{
			highestLevelPlayerId = highestLevelPlayerId(allRegistered);
			highestLevelPlayer = allRegistered.get(highestLevelPlayerId);
			allRegistered.remove(highestLevelPlayerId);
			_team[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			if(allRegistered.isEmpty())
			{
				break;
			}
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPlayerId(allRegistered);
			highestLevelPlayer = allRegistered.get(highestLevelPlayerId);
			allRegistered.remove(highestLevelPlayerId);
			_team[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			priority = balance[0] > balance[1] ? 1 : 0;
		}

		if(_team[0].getRegisteredPlayersCount() < ConfigEventCTF.CTF_EVENT_MIN_PLAYERS_IN_TEAMS || _team[1].getRegisteredPlayersCount() < ConfigEventCTF.CTF_EVENT_MIN_PLAYERS_IN_TEAMS)
		{
			setState(EventState.INACTIVE);
			_team[0].clean();
			_team[1].clean();
			unSpawnNpc();
			stopFlagLoop();
			return false;
		}

		if(ConfigEventCTF.CTF_EVENT_IN_INSTANCE)
		{
			try
			{
				_CTFEventInstance = InstanceManager.getInstance().createDynamicInstance(ConfigEventCTF.CTF_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_CTFEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_CTFEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_CTFEventInstance).setEmptyDestroyTime(ConfigEventCTF.CTF_EVENT_START_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
			}
			catch(Exception e)
			{
				_CTFEventInstance = 0;
				_log.log(Level.ERROR, "CTFEventEngine[CTFEvent.createDynamicInstance]: exception: " + e);
			}
		}

		setState(EventState.STARTED);
		spawnFlagNpcs(_CTFEventInstance);
		if(ConfigEventCTF.CTF_EVENT_RESPAWN_TYPE == ConfigEventCTF.CTFEventRespawnType.MASS)
		{
			startRespawnLoop();
		}
		startFlagLoop();
		startIdleCheckerTask();
		for(CTFEventTeam team : _team)
		{
			team.getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> {
				broadcastPacketToTeams(new ExBlockUpSetList(player, getParticipantTeamId(player.getObjectId()) == 1, false));
				new CTFEventTeleporter(player, team.getCoordinates(), false, false);
			});
		}
		broadcastPacketToTeams(new ExBlockUpSetState(timeLeft, 0, 0));
		return true;
	}

	public static String calculateWinner()
	{
		if(_team[0].getPoints() == _team[1].getPoints())
		{
			if(_team[0].getRegisteredPlayersCount() == 0 || _team[1].getRegisteredPlayersCount() == 0)
			{
				setState(EventState.REWARDING);
				return "Захват флага: Ивент закончен. Ни одна команда не победила!";
			}
			sysMsgToAllParticipants("Захват флага: Ивент закончен, все команды проиграли.");
			if(ConfigEventCTF.CTF_REWARD_TEAM_TIE)
			{
				rewardTeam(0);
				rewardTeam(1);
				return "Захват флага: Ивент закончен, все команды проиграли.";
			}
			else
			{
				return "Захват флага: Ивент закончен, все команды проиграли.";
			}
		}

		setState(EventState.REWARDING);
		stopRespawnLoop();
		stopFlagLoop();
		stopIdleCheckerTask();
		CTFEventTeam winner = _team[_team[0].getPoints() > _team[1].getPoints() ? 0 : 1];
		CTFEventTeam loser = _team[_team[1].getPoints() > _team[0].getPoints() ? 0 : 1];
		if(winner.equals(_team[0]))
		{
			rewardTeam(0);
		}
		else
		{
			rewardTeam(1);
		}

		return "Захват флага: Ивент закнчен. Команда " + winner.getName() + " выиграла, заработав " + winner.getPoints() + " очко(в). " + "Проигравшие заработали только " + loser.getPoints() + " очко(в).";
	}

	private static void rewardTeam(int teamId)
	{
		CTFEventTeam team = _team[teamId];
		for(L2PcInstance player : team.getRegisteredPlayers().values())
		{
			if(player == null)
			{
				continue;
			}

			for(int[] reward : ConfigEventCTF.CTF_EVENT_REWARDS)
			{
				player.addItem(ProcessType.EVENT, reward[0], reward[1], player, true);
			}

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Reward.htm"));

			player.sendPacket(new ExUserInfoInvenWeight(player));
			player.sendPacket(npcHtmlMessage);
		}
	}

	public static void stopFight()
	{
		setState(EventState.INACTIVATING);
		unSpawnNpc();

		for(CTFEventTeam team : _team)
		{
			// Just in case
			team.getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> {
				// Just in case
				if(player.getInventory().getItemByItemId(ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID) != null)
				{
					player.getInventory().destroyItemByItemId(ProcessType.EVENT, ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID, 1, player, null);
				}

				player.sendPacket(new ExBlockUpSetState(_team[0].getPoints() < _team[1].getPoints()));
				broadcastPacketToTeams(new ExBlockUpSetList(player, getParticipantTeamId(player.getObjectId()) == 1, true));
				new CTFEventTeleporter(player, ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
			});
		}
		_team[0].clean();
		_team[1].clean();
		stopFlagLoop();
		setState(EventState.INACTIVE);
	}

	private static boolean checkDualbox(L2PcInstance player)
	{
		String hwid = player.getClient().getHWID();
		for(L2PcInstance plr : _team[0].getRegisteredPlayers().values())
		{
			if(plr.getClient().getHWID() != null && plr.getClient().getHWID().equals(hwid))
			{
				return true;
			}
		}
		for(L2PcInstance plr : _team[1].getRegisteredPlayers().values())
		{
			if(plr.getClient().getHWID() != null && plr.getClient().getHWID().equals(hwid))
			{
				return true;
			}
		}
		return false;
	}

	public static int getMemberCount()
	{
		int count = 0;
		for(CTFEventTeam team : _team)
		{
			if(team != null)
			{
				count += team.getRegisteredPlayersCount();
			}
		}
		return count;
	}

	public static boolean addParticipant(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}

		if(checkDualbox(player))
		{
			player.sendMessage("Извините, но в ивенте не разрешено участие нескольких персонажей с одного компьютера.");
			return false;
		}

		byte teamId = 0;

		teamId = _team[0].getRegisteredPlayersCount() == _team[1].getRegisteredPlayersCount() ? (byte) Rnd.get(2) : (byte) (_team[0].getRegisteredPlayersCount() > _team[1].getRegisteredPlayersCount() ? 1 : 0);

		return _team[teamId].addPlayer(player);
	}

	public static boolean removeParticipant(L2PcInstance player)
	{
		if(isFlagOwner(player))
		{
			removeFlagFromPlayer(player, true);
		}
		byte teamId = getParticipantTeamId(player.getObjectId());

		if(teamId != -1)
		{
			_team[teamId].removePlayer(player.getObjectId());
			return true;
		}

		return false;
	}

	public static boolean payParticipationFee(L2PcInstance player)
	{
		int itemId = ConfigEventCTF.CTF_EVENT_PARTICIPATION_FEE[0];
		int itemNum = ConfigEventCTF.CTF_EVENT_PARTICIPATION_FEE[1];
		if(itemId == 0 || itemNum == 0)
		{
			return true;
		}

		if(player.getInventory().getInventoryItemCount(itemId, -1) < itemNum)
		{
			return false;
		}

		return player.destroyItemByItemId(ProcessType.EVENT, itemId, itemNum, _lastNpcSpawn, true);
	}

	public static String getParticipationFee()
	{
		int itemId = ConfigEventCTF.CTF_EVENT_PARTICIPATION_FEE[0];
		int itemNum = ConfigEventCTF.CTF_EVENT_PARTICIPATION_FEE[1];

		if(itemId == 0 || itemNum == 0)
		{
			return "-";
		}

		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}

	public static void sysMsgToAllParticipants(String message)
	{
		Say2 cs = new Say2(0, ChatType.BATTLEFIELD, "CTF", message);
		for(CTFEventTeam team : _team)
		{
			team.getRegisteredPlayers().values().stream().filter(player -> player != null).forEach(player -> player.sendPacket(cs));
		}
	}

	private static void startRespawnLoop()
	{
		if(_respawnTask != null)
		{
			stopRespawnLoop();
		}
		_respawnTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RespawnTask(), ConfigEventCTF.CTF_EVENT_RESPAWN_DELAY * 1000, ConfigEventCTF.CTF_EVENT_RESPAWN_DELAY * 1000);
	}

	private static void stopRespawnLoop()
	{
		if(_respawnTask != null)
		{
			_respawnTask.cancel(false);
			_respawnTask = null;
		}
	}

	private static void startFlagLoop()
	{
		if(_flagTask != null)
		{
			stopFlagLoop();
		}
		_flagTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FlagTask(), 1 * 60 * 1000, 1 * 60 * 1000);
	}

	private static void stopFlagLoop()
	{
		if(_flagTask != null)
		{
			_flagTask.cancel(false);
			_flagTask = null;
		}
	}

	private static void startIdleCheckerTask()
	{
		if(_idleTask != null)
		{
			stopIdleCheckerTask();
		}
		_idleTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new IdleCheckerTask(), 5 * 60 * 1000, 60 * 1000);
	}

	private static void stopIdleCheckerTask()
	{
		if(_idleTask != null)
		{
			_idleTask.cancel(false);
			_idleTask = null;
		}
	}

	private static void unSpawnNpc()
	{
		if(_lastNpcSpawn != null)
		{
			_lastNpcSpawn.getLocationController().delete();
		}
		if(_team1FlagSpawn != null)
		{
			_team1FlagSpawn.getLocationController().delete();
		}
		if(_team2FlagSpawn != null)
		{
			_team2FlagSpawn.getLocationController().delete();
		}
		if(_npcSpawn != null)
		{
			_npcSpawn.stopRespawn();
		}
		_npcSpawn = null;
		if(_t1FlagSpawn != null)
		{
			_t1FlagSpawn.stopRespawn();
		}
		_t1FlagSpawn = null;
		if(_t2FlagSpawn != null)
		{
			_t2FlagSpawn.stopRespawn();
		}
		_t2FlagSpawn = null;
		_lastNpcSpawn = null;
		_team1FlagSpawn = null;
		_team2FlagSpawn = null;
	}

	public static void onToVillage(L2PcInstance player)
	{
		if(!isStarted() || player == null || getParticipantTeamId(player.getObjectId()) == -1)
		{
			return;
		}

		CTFEventTeam team = getParticipantTeam(player.getObjectId());

		// MASS
		if(ConfigEventCTF.CTF_EVENT_RESPAWN_TYPE == ConfigEventCTF.CTFEventRespawnType.MASS)
		{
			team.addDeadPlayer(player);
			player.sendMessage("Вы воскресните через " + _respawnTask.getDelay(TimeUnit.SECONDS) + " секунд(у).");
		}
		// MANUAL
		else if(ConfigEventCTF.CTF_EVENT_RESPAWN_TYPE == ConfigEventCTF.CTFEventRespawnType.MANUAL)
		{
			new CTFEventTeleporter(player, team.getCoordinates(), false, false);
		}
	}

	public static void onLogin(L2PcInstance player)
	{
		if(player == null || !isStarting() && !isStarted() || getParticipantTeamId(player.getObjectId()) == -1)
		{
			return;
		}

		CTFEventTeam team = getParticipantTeam(player.getObjectId());

		team.addPlayer(player);
		new CTFEventTeleporter(player, team.getCoordinates(), true, false);
	}

	public static void onLogout(L2PcInstance player)
	{
		if(player != null && (isStarting() || isStarted() || isParticipating()))
		{
			if(removeParticipant(player))
			{
				player.setXYZ(ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101) - 50, ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101) - 50, ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			}
		}
	}

	public static void onBypass(String command, L2PcInstance player)
	{
		if(player == null || !isParticipating())
		{
			return;
		}
		String htmContent;

		if(command.equals("CTF_event_participation"))
		{
			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
			int playerLevel = player.getLevel();

			if(player.isCursedWeaponEquipped())
			{
				htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "CursedWeaponEquipped.htm");
				if(htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if(OlympiadManager.getInstance().isRegistered(player))
			{
				htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Olympiad.htm");
				if(htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if(player.hasBadReputation())
			{
				htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Karma.htm");
				if(htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
				}
			}
			else if(playerLevel < ConfigEventCTF.CTF_EVENT_MIN_LVL || playerLevel > ConfigEventCTF.CTF_EVENT_MAX_LVL)
			{
				htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Level.htm");
				if(htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%min%", String.valueOf(ConfigEventCTF.CTF_EVENT_MIN_LVL));
					npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventCTF.CTF_EVENT_MAX_LVL));
				}
			}
			else if(_team[0].getRegisteredPlayersCount() == ConfigEventCTF.CTF_EVENT_MAX_PLAYERS_IN_TEAMS && _team[1].getRegisteredPlayersCount() == ConfigEventCTF.CTF_EVENT_MAX_PLAYERS_IN_TEAMS)
			{
				htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "TeamsFull.htm");
				if(htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventCTF.CTF_EVENT_MAX_PLAYERS_IN_TEAMS));
				}
			}
			else if(!payParticipationFee(player))
			{
				htmContent = HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "ParticipationFee.htm");
				if(htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%fee%", getParticipationFee());
				}
			}
			else if(addParticipant(player))
			{
				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Registered.htm"));
			}
			else
			{
				return;
			}

			player.sendPacket(npcHtmlMessage);
		}
		else if(command.equals("CTF_event_remove_participation"))
		{
			removeParticipant(player);

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

			npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(player.getLang(), htmlPath + "Unregistered.htm"));
			player.sendPacket(npcHtmlMessage);
		}
	}

	public static boolean isAutoAttackable(L2PcInstance player, L2Character attacker)
	{
		if(player == null || attacker == null || !(attacker instanceof L2PcInstance))
		{
			return true;
		}

		byte playerTeamId = getParticipantTeamId(player.getObjectId());
		byte attackerTeamId = getParticipantTeamId(attacker.getObjectId());

		if(playerTeamId != -1 && attackerTeamId == -1 || playerTeamId == -1 && attackerTeamId != -1)
		{
			return false;
		}

		return !(playerTeamId != -1 && attackerTeamId != -1 && playerTeamId == attackerTeamId && player.getObjectId() != attacker.getObjectId());

	}

	public static boolean onAction(L2PcInstance player, int targetedPlayerObjectId)
	{
		if(player == null || !isStarted() || player.isGM())
		{
			return true;
		}

		byte playerTeamId = getParticipantTeamId(player.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);

		if(playerTeamId != -1 && targetedPlayerTeamId == -1 || playerTeamId == -1 && targetedPlayerTeamId != -1)
		{
			return false;
		}

		return !(playerTeamId != -1 && targetedPlayerTeamId != -1 && playerTeamId == targetedPlayerTeamId && player.getObjectId() != targetedPlayerObjectId && !ConfigEventCTF.CTF_EVENT_TARGET_TEAM_MEMBERS_ALLOWED);

	}

	public static boolean onScrollUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !(isPlayerParticipant(playerObjectId) && !ConfigEventCTF.CTF_EVENT_SCROLL_ALLOWED);

	}

	public static boolean onPotionUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !(isPlayerParticipant(playerObjectId) && !ConfigEventCTF.CTF_EVENT_POTIONS_ALLOWED);

	}

	public static boolean onEscapeUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !isPlayerParticipant(playerObjectId);

	}

	public static boolean onItemSummon(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}

		return !(isPlayerParticipant(playerObjectId) && !ConfigEventCTF.CTF_EVENT_SUMMON_BY_ITEM_ALLOWED);

	}

	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayer)
	{
		if(killedPlayer == null || !isStarted() || !isPlayerParticipant(killedPlayer.getObjectId()) || getParticipantTeamId(killedPlayer.getObjectId()) == -1)
		{
			return;
		}

		CTFEventTeam team = getParticipantTeam(killedPlayer.getObjectId());

		if(ConfigEventCTF.CTF_EVENT_RESPAWN_TYPE == ConfigEventCTF.CTFEventRespawnType.CLASSIC)
		{
			new CTFEventTeleporter(killedPlayer, team.getCoordinates(), false, false);
		}
		if(!isStarted() || !isFlagOwner(killedPlayer))
		{
			return;
		}

		if(getParticipantTeamId(killedPlayer.getObjectId()) != -1)
		{
			dropFlag(killedPlayer);
		}
	}

	public static void broadcastPacketToTeams(L2GameServerPacket... packets)
	{
		for(CTFEventTeam team : _team)
		{
			team.getRegisteredPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> {
				for(L2GameServerPacket packet : packets)
				{
					playerInstance.sendPacket(packet);
				}
			});
		}
	}

	public static void onTeleported(L2PcInstance player)
	{
		if(player == null || !isStarted() || !isPlayerParticipant(player.getObjectId()))
		{
			return;
		}

		if(player.isMageClass())
		{
			if(ConfigEventCTF.CTF_EVENT_MAGE_BUFFS != null && !ConfigEventCTF.CTF_EVENT_MAGE_BUFFS.isEmpty())
			{
				for(int i : ConfigEventCTF.CTF_EVENT_MAGE_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, ConfigEventCTF.CTF_EVENT_MAGE_BUFFS.get(i));
					if(skill != null)
					{
						skill.getEffects(player, player);
					}
				}
			}
		}
		else
		{
			if(ConfigEventCTF.CTF_EVENT_FIGHTER_BUFFS != null && !ConfigEventCTF.CTF_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for(int i : ConfigEventCTF.CTF_EVENT_FIGHTER_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, ConfigEventCTF.CTF_EVENT_FIGHTER_BUFFS.get(i));
					if(skill != null)
					{
						skill.getEffects(player, player);
					}
				}
			}
		}

		player.setCurrentCp(player.getMaxCp());
		player.setCurrentHp(player.getMaxHp());
		player.setCurrentMp(player.getMaxMp());
	}

	public static void onFlagPickup(L2PcInstance player, L2ItemInstance item)
	{
		if(!isStarted() || !isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("Читер? Уали с ивента, бле :)");
			return;
		}

		CTFEventTeam pTeam = getParticipantTeam(player.getObjectId());
		CTFEventTeam eTeam = getParticipantEnemyTeam(player.getObjectId());
		int flagTeamId = getFlagTeamId(item);
		// the flag belongs to the player's team
		if(pTeam.getId() == flagTeamId)
		{
			item.pickupMe(player);
			player.getInventory().destroyItem(ProcessType.EVENT, item.getItemId(), item.getCount(), player, null);
			pTeam.setFlag(null);
			pTeam.setFlagOnPlace(true);
			player.setCtfFlagEquipped(false);
			player.stopAbnormalEffect(0x60000000);
			sysMsgToAllParticipants(pTeam.getName() + " вернули свой флаг обратно!");
		}
		// the flag do not belong to the player's team
		else
		{
			item.pickupMe(player);
			player.getInventory().destroyItem(ProcessType.EVENT, item.getItemId(), item.getCount(), player, null);
			addFlagToPlayer(player);
			sysMsgToAllParticipants(eTeam.getName() + " завладели флагом команды " + getParticipantEnemyTeam(player.getObjectId()).getName());
		}
	}

	public static boolean checkForSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if(!isStarted())
		{
			return true;
		}

		int sourcePlayerId = source.getObjectId();
		int targetPlayerId = target.getObjectId();
		boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);

		if(!isSourceParticipant && !isTargetParticipant)
		{
			return true;
		}
		if(!(isSourceParticipant && isTargetParticipant))
		{
			return false;
		}
		if(getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if(!skill.isOffensive())
			{
				return false;
			}
		}
		return true;
	}

	public static void addFlagToPlayer(L2PcInstance player)
	{
		if(player == null)
		{
			return;
		}
		if(!isPlayerParticipant(player.getObjectId()))
		{
			player.sendMessage("Читер? Уали с ивента, бле :)");
			return;
		}
		if(isFlagOwner(player))
		{
			player.sendMessage("У Вас уже есть флаг, зачем еще один?");
			return;
		}

		L2ItemInstance wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if(wpn == null)
		{
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
			if(wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
			}
		}
		else
		{
			player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
			wpn = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
			if(wpn != null)
			{
				player.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_R_HAND);
			}
		}

		L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.EVENT, ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID, 1, player, null);
		player.addItem(ProcessType.PICKUP, item, null, true);
		//add the flag in his hands
		player.getInventory().equipItem(item);
		player.setCtfFlagEquipped(true);
		player.startAbnormalEffect(0x60000000);
		getParticipantEnemyTeam(player.getObjectId()).setFlag(item);
		getParticipantEnemyTeam(player.getObjectId()).setFlagOnPlace(false);
		player.broadcastUserInfo();
		//note all players that the flag is taken

		getParticipantEnemyTeam(player.getObjectId()).getRegisteredPlayers().values().stream().filter(member -> member != null).forEach(member -> member.getRadar().addMarker(player.getX(), player.getY(), player.getZ()));

		getParticipantEnemyTeam(player.getObjectId()).setLastFlagTaken();

		sysMsgToAllParticipants(player.getName() + " завладели флагом команды " + getParticipantEnemyTeam(player.getObjectId()).getName());
	}

	public static void removeFlagFromPlayer(L2PcInstance player, boolean inEventRemoval)
	{
		InventoryUpdate iu = new InventoryUpdate();
		for(L2ItemInstance item : player.getInventory().getItemsByItemId(ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID))
		{
			if(item.isEquipped())
			{
				player.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			}
			player.destroyItem(ProcessType.EVENT, item, null, true);
			iu.addRemovedItem(item);
		}
		player.sendPacket(iu);
		player.setCtfFlagEquipped(false);
		player.stopAbnormalEffect(0x60000000);
		player.abortAttack();
		player.broadcastUserInfo();
		if(inEventRemoval)
		{
			getParticipantEnemyTeam(player.getObjectId()).setFlag(null);
			getParticipantEnemyTeam(player.getObjectId()).setFlagOnPlace(true);
		}
		getParticipantEnemyTeam(player.getObjectId()).getRegisteredPlayers().values().stream().filter(member -> member != null).forEach(member -> member.getRadar().removeAllMarkers());
	}

	private static void dropFlag(L2PcInstance player)
	{
		if(player == null || !isStarted() || !isPlayerParticipant(player.getObjectId()))
		{
			return;
		}

		for(L2ItemInstance item : player.getInventory().getItemsByItemId(ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID))
		{
			player.dropItem(ProcessType.EVENT, item.getObjectId(), item.getCount(), player.getX() + Rnd.get(100) - 50, player.getY() + Rnd.get(100) - 50, player.getZ(), null, true, false);
		}

		sysMsgToAllParticipants(player.getName() + " потерял флаг!");
		player.setCtfFlagEquipped(false);
		player.stopAbnormalEffect(0x60000000);
	}

	public static boolean isFlagOwner(L2PcInstance player)
	{
		return !(player == null || !isPlayerParticipant(player.getObjectId())) && (player.getActiveWeaponInstance() != null && player.getActiveWeaponInstance().getItemId() == ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID || player.isCtfFlagEquipped());
	}

	public static void addPointsToPlayerTeam(L2PcInstance player)
	{
		if(player == null || !isPlayerParticipant(player.getObjectId()))
		{
			return;
		}
		if(!isFlagOwner(player))
		{
			player.sendMessage("У Вас нет флага, бегите туда, где он есть!");
			return;
		}

		CTFEventTeam team = _team[getParticipantTeamId(player.getObjectId())];

		if(!team.isFlagOnPlace())
		{
			player.sendMessage("Ваш флаг не на месте");
			return;
		}

		removeFlagFromPlayer(player, true);
		team.increasePoints();

		try
		{
			int timeLeft = (int) ((_startedTime - System.currentTimeMillis()) / 1000);
			int[] points = new int[2];
			points[0] = _team[0].getPoints();
			points[1] = _team[1].getPoints();

			broadcastPacketToTeams(new ExBlockUpSetState(timeLeft, points[0], points[1]));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}

		sysMsgToAllParticipants("Команда " + team.getName() + " захватила флаг. Текущий счет: " + team.getName() + '-' + team.getPoints() + ", " + getParticipantEnemyTeam(player.getObjectId()).getName() + '-' + getParticipantEnemyTeam(player.getObjectId()).getPoints() + '.');
		if(team.getPoints() <= 20)
		{
			int skillId = 6469 + team.getPoints();
			player.broadcastPacket(new MagicSkillUse(player, skillId, 1, 1, 1));
		}

	}

	private static void setState(EventState state)
	{
		synchronized(_state)
		{
			_state = state;
		}
	}

	public static boolean isInactive()
	{
		boolean isInactive;

		synchronized(_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}

		return isInactive;
	}

	public static boolean isInactivating()
	{
		boolean isInactivating;

		synchronized(_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}

		return isInactivating;
	}

	public static boolean isParticipating()
	{
		boolean isParticipating;

		synchronized(_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}

		return isParticipating;
	}

	public static boolean isStarting()
	{
		boolean isStarting;

		synchronized(_state)
		{
			isStarting = _state == EventState.STARTING;
		}

		return isStarting;
	}

	public static boolean isStarted()
	{
		boolean isStarted;

		synchronized(_state)
		{
			isStarted = _state == EventState.STARTED;
		}

		return isStarted;
	}

	public static boolean isRewarding()
	{
		boolean isRewarding;

		synchronized(_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}

		return isRewarding;
	}

	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_team[0].containsRegisteredPlayer(playerObjectId) ? 0 : _team[1].containsRegisteredPlayer(playerObjectId) ? 1 : -1);
	}

	public static CTFEventTeam getParticipantTeam(int playerObjectId)
	{
		return _team[getParticipantTeamId(playerObjectId)];
	}

	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _team[0].containsRegisteredPlayer(playerObjectId) ? _team[0].getCoordinates() : _team[1].containsRegisteredPlayer(playerObjectId) ? _team[1].getCoordinates() : null;
	}

	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if(!isParticipating() && !isStarting() && !isStarted())
		{
			return false;
		}

		return _team[0].containsRegisteredPlayer(playerObjectId) || _team[1].containsRegisteredPlayer(playerObjectId);
	}

	public static int getParticipatedPlayersCount()
	{
		if(!isParticipating() && !isStarting() && !isStarted())
		{
			return 0;
		}

		return _team[0].getRegisteredPlayersCount() + _team[1].getRegisteredPlayersCount();
	}

	public static CTFEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return _team[0].containsRegisteredPlayer(playerObjectId) ? _team[1] : _team[1].containsRegisteredPlayer(playerObjectId) ? _team[0] : null;
	}

	public static byte getParticipantEnemyTeamId(int playerObjectId)
	{
		return (byte) (_team[0].containsRegisteredPlayer(playerObjectId) ? 0 : _team[1].containsRegisteredPlayer(playerObjectId) ? 1 : -1);
	}

	public static boolean teamFlagOnPlace(byte teamId)
	{
		return _team[teamId].isFlagOnPlace();
	}

	public static int[] getTeamsPlayerCounts()
	{
		return new int[]{_team[0].getRegisteredPlayersCount(), _team[1].getRegisteredPlayersCount()};
	}

	public static long[] getTeamsPoints()
	{
		return new long[]{_team[0].getPoints(), _team[1].getPoints()};
	}

	public static int getCTFEventInstance()
	{
		return _CTFEventInstance;
	}

	public static int getFlagTeamId(L2ItemInstance item)
	{
		if(item == null)
		{
			return -1;
		}
		if(item.getObjectId() == _team[0].getFlagObjectId())
		{
			return 0;
		}
		if(item.getObjectId() == _team[1].getFlagObjectId())
		{
			return 1;
		}
		_log.log(Level.WARN, "CTFEventEngine[CTFEvent.getFlagTeamId()]: Item team is -1");
		return -1;
	}

	public static class CTFHook extends AbstractHookImpl
	{
		@Override
		public boolean onItemPickup(L2PcInstance player, L2ItemInstance item)
		{
			if(item.getItemId() == ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID)
			{
				if(isFlagOwner(player))
				{
					player.sendMessage("You cannot pickup a flag while carrying one!");
					return false;
				}
				onFlagPickup(player, item);
				return false;
			}
			return super.onItemPickup(player, item);
		}
	}

	private static class RespawnTask implements Runnable
	{
		@Override
		public void run()
		{
			for(CTFEventTeam team : _team)
			{
				for(L2PcInstance dead : team.getDeadPlayers())
				{
					if(dead == null)
					{
						continue;
					}
					if(dead.isDead())
					{
						new CTFEventTeleporter(dead, team.getCoordinates(), true, false);
					}
					team.removeDeadPlayer(dead.getObjectId());
				}
			}
		}
	}

	private static class FlagTask implements Runnable
	{
		@Override
		public void run()
		{
			for(CTFEventTeam team : _team)
			{
				team.getRegisteredPlayers().values().stream().filter(player -> player != null && isFlagOwner(player) && System.currentTimeMillis() > getParticipantEnemyTeam(player.getObjectId()).getLastFlagTaken()).forEach(player -> {
					removeFlagFromPlayer(player, true);
					dropFlag(player);
					player.doDie(player);
					player.sendMessage("Вы не можете владеть флагом больше чем 2 минуты!");
				});
			}
		}
	}

	private static class IdleCheckerTask implements Runnable
	{
		@Override
		public void run()
		{
			if(ConfigEventCTF.CTF_EVENT_IDLE_TIME_CHECKER < 1)
			{
				stopIdleCheckerTask();
				return;
			}
			for(CTFEventTeam team : _team)
			{
				team.getRegisteredPlayers().values().stream().filter(player -> player != null && player.getIdleFromTime() > ConfigEventCTF.CTF_EVENT_IDLE_TIME_CHECKER * 60 * 1000).forEach(player -> {
					removeParticipant(player);
					player.sendMessage("Вы исключены из ивента, по-сколько были неактивны " + ConfigEventCTF.CTF_EVENT_IDLE_TIME_CHECKER + " минут(ы)!");
				});
			}
		}
	}
}