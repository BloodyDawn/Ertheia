package dwo.gameserver.instancemanager.events.TvT;

import dwo.config.Config;
import dwo.config.events.ConfigEventTvT;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.MultiSellData;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.events.EventState;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.actor.instance.L2SummonInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TvTEvent
{
	protected static final Logger _log = LogManager.getLogger(TvTEvent.class);
	/** html path **/
	private static final String htmlPath = "mods/TvTEvent/";
	/**    The teams of the TvTEvent<br> */
	static TvTEventTeam[] _teams = new TvTEventTeam[2];
	/** The state of the TvTEvent<br> */
	private static EventState _state = EventState.INACTIVE;
	/** The spawn of the participation npc<br> */
	private static L2Spawn _npcSpawn;
	/** the npc instance of the participation npc<br> */
	private static L2Npc _lastNpcSpawn;
	/** Instance id<br> */
	private static int _TvTEventInstance;
	/** for first blood mode */
	private static int firstblood;
	/** for counter.. */
	private static long _startedTime;
	private static int _mapId;

	/**
	 * No instance of this class!<br>
	 */
	private TvTEvent()
	{
	}

	/**
	 * Teams initializing<br>
	 */
	public static void init()
	{
		TvTLocationManager.getInstance();
		_teams[0] = new TvTEventTeam(ConfigEventTvT.TVT_EVENT_TEAM_1_NAME, ConfigEventTvT.TVT_EVENT_TEAM_1_COORDINATES);
		_teams[1] = new TvTEventTeam(ConfigEventTvT.TVT_EVENT_TEAM_2_NAME, ConfigEventTvT.TVT_EVENT_TEAM_2_COORDINATES);
	}

	/**
	 * Starts the participation of the TvTEvent<br>
	 * 1. Get L2NpcTemplate by Config.TVT_EVENT_PARTICIPATION_NPC_ID<br>
	 * 2. Try to spawn a new npc of it<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startParticipation()
	{
		L2NpcTemplate tmpl = NpcTable.getInstance().getTemplate(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_ID);

		if(tmpl == null)
		{
			_log.log(Level.WARN, "TvTEventEngine[TvTEvent.startParticipation()]: L2NpcTemplate is a NullPointer -> Invalid npc id in Configs?");
			return false;
		}

		try
		{
			_npcSpawn = new L2Spawn(tmpl);

			_npcSpawn.setLocx(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0]);
			_npcSpawn.setLocy(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1]);
			_npcSpawn.setLocz(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2]);
			_npcSpawn.setAmount(1);

			if(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES.length > 3)
			{
				_npcSpawn.setHeading(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[3]);
			}
			else
			{
				_npcSpawn.setHeading(0);
			}

			_npcSpawn.setRespawnDelay(1);
			// later no need to delete spawn from db, we don't store it (false)
			SpawnTable.getInstance().addNewSpawn(_npcSpawn);
			_npcSpawn.init();
			_lastNpcSpawn = _npcSpawn.getLastSpawn();
			_lastNpcSpawn.setCurrentHp(_lastNpcSpawn.getMaxHp());
			_lastNpcSpawn.setTitle("Стенка на стенку");
			_lastNpcSpawn.getLocationController().decay();
			_lastNpcSpawn.getLocationController().spawn(_npcSpawn.getLastSpawn().getX(), _npcSpawn.getLastSpawn().getY(), _npcSpawn.getLastSpawn().getZ());
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_lastNpcSpawn, _lastNpcSpawn, 2025, 1, 1, 1));
			_lastNpcSpawn.broadcastPacket(new MagicSkillUse(_npcSpawn.getLastSpawn(), _npcSpawn.getLastSpawn(), 1034, 1, 1, 1));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "TvTEventEngine[TvTEvent.startParticipation()]: exception: " + e.getMessage(), e);
			return false;
		}

		setState(EventState.PARTICIPATING);
		return true;
	}

	private static int highestLevelPcInstanceOf(Map<Integer, L2PcInstance> players)
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

	/**
	 * Starts the TvTEvent fight<br>
	 * 1. Set state EventState.STARTING<br>
	 * 2. Close doors specified in Configs<br>
	 * 3. Abort if not enought participants(return false)<br>
	 * 4. Set state EventState.STARTED<br>
	 * 5. Teleport all participants to team spot<br><br>
	 *
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean startFight()
	{
		// Set state to STARTING
		setState(EventState.STARTING);
		_mapId = TvTLocationManager.getInstance().generateRandomMap();
		_startedTime = System.currentTimeMillis() + ConfigEventTvT.TVT_EVENT_RUNNING_TIME * 60 * 1000;
		int timeLeft = (int) ((_startedTime - System.currentTimeMillis()) / 1000);
		// Randomize and balance team distribution
		Map<Integer, L2PcInstance> allParticipants = new FastMap<>();
		allParticipants.putAll(_teams[0].getParticipatedPlayers());
		allParticipants.putAll(_teams[1].getParticipatedPlayers());
		_teams[0].cleanMe();
		_teams[1].cleanMe();

		L2PcInstance player;
		Iterator<L2PcInstance> iter;
		if(needParticipationFee())
		{
			iter = allParticipants.values().iterator();
			while(iter.hasNext())
			{
				player = iter.next();
				if(!hasParticipationFee(player))
				{
					iter.remove();
				}
			}
		}

		int[] balance = {0, 0};
		int priority = 0;
		int highestLevelPlayerId;
		L2PcInstance highestLevelPlayer;
		// XXX: allParticipants should be sorted by level instead of using highestLevelPcInstanceOf for every fetch
		while(!allParticipants.isEmpty())
		{
			// Priority team gets one player
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Exiting if no more players
			if(allParticipants.isEmpty())
			{
				break;
			}
			// The other team gets one player
			// XXX: Code not dry
			priority = 1 - priority;
			highestLevelPlayerId = highestLevelPcInstanceOf(allParticipants);
			highestLevelPlayer = allParticipants.get(highestLevelPlayerId);
			allParticipants.remove(highestLevelPlayerId);
			_teams[priority].addPlayer(highestLevelPlayer);
			balance[priority] += highestLevelPlayer.getLevel();
			// Recalculating priority
			priority = balance[0] > balance[1] ? 1 : 0;
		}

		// Check for enought participants
		if(_teams[0].getParticipatedPlayerCount() < ConfigEventTvT.TVT_EVENT_MIN_PLAYERS_IN_TEAMS || _teams[1].getParticipatedPlayerCount() < ConfigEventTvT.TVT_EVENT_MIN_PLAYERS_IN_TEAMS)
		{
			// Set state INACTIVE
			setState(EventState.INACTIVE);
			// Cleanup of teams
			_teams[0].cleanMe();
			_teams[1].cleanMe();
			// Unspawn the event NPC
			unSpawnNpc();
			return false;
		}

		Announcements.getInstance().announceToAll("Стенка на стенку: Случайная карта выбрана: " + TvTLocationManager.getInstance().getLocation(_mapId).getName());
		if(needParticipationFee())
		{
			iter = _teams[0].getParticipatedPlayers().values().iterator();
			while(iter.hasNext())
			{
				player = iter.next();
				if(!payParticipationFee(player))
				{
					iter.remove();
				}
			}
			iter = _teams[1].getParticipatedPlayers().values().iterator();
			while(iter.hasNext())
			{
				player = iter.next();
				if(!payParticipationFee(player))
				{
					iter.remove();
				}
			}
		}

		if(ConfigEventTvT.TVT_EVENT_IN_INSTANCE)
		{
			try
			{
				_TvTEventInstance = InstanceManager.getInstance().createDynamicInstance(ConfigEventTvT.TVT_EVENT_INSTANCE_FILE);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setAllowSummon(false);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setPvPInstance(true);
				InstanceManager.getInstance().getInstance(_TvTEventInstance).setEmptyDestroyTime(ConfigEventTvT.TVT_EVENT_START_LEAVE_TELEPORT_DELAY * 1000 + 60000L);
			}
			catch(Exception e)
			{
				_TvTEventInstance = 0;
				_log.log(Level.ERROR, "TvTEventEngine[TvTEvent.createDynamicInstance]: exception: " + e.getMessage(), e);
			}
		}

		// Opens all doors specified in Configs for tvt
		openDoors(ConfigEventTvT.TVT_DOORS_IDS_TO_OPEN);
		// Closes all doors specified in Configs for tvt
		closeDoors(ConfigEventTvT.TVT_DOORS_IDS_TO_CLOSE);
		// Set state STARTED
		setState(EventState.STARTED);
		NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
		int[] teamsPlayerCounts = getTeamsPlayerCounts();
		// Iterate over all teams
		try
		{
			for(TvTEventTeam team : _teams)
			{
				// Iterate over all participated player instances in this team
				// Teleporter implements Runnable and starts itself
				team.getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> {
					playerInstance.TvTSetKills(0);
					broadcastPacketToTeams(new ExBlockUpSetList(playerInstance, getParticipantTeamId(playerInstance.getObjectId()) == 1, false));

					String html = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Help.htm");
					if(html != null)
					{
						npcHtmlMessage.setHtml(html);
						npcHtmlMessage.replace("%min%", String.valueOf(ConfigEventTvT.TVT_EVENT_MIN_LVL));
						npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventTvT.TVT_EVENT_MAX_LVL));
						npcHtmlMessage.replace("%InInstance%", String.valueOf(ConfigEventTvT.TVT_EVENT_IN_INSTANCE ? "Да" : "Нет"));
						npcHtmlMessage.replace("%potions%", String.valueOf(ConfigEventTvT.TVT_EVENT_POTIONS_ALLOWED ? "Да" : "Нет"));
						npcHtmlMessage.replace("%scrolls%", String.valueOf(ConfigEventTvT.TVT_EVENT_SCROLL_ALLOWED ? "Да" : "Нет"));
						npcHtmlMessage.replace("%summon%", String.valueOf(ConfigEventTvT.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED ? "Да" : "Нет"));
						npcHtmlMessage.replace("%respawndelay%", String.valueOf(ConfigEventTvT.TVT_EVENT_RESPAWN_TELEPORT_DELAY));
						npcHtmlMessage.replace("%minkills%", String.valueOf(ConfigEventTvT.TVT_MIN_KILLS_TO_REWARD));
						npcHtmlMessage.replace("%minkillsys%", String.valueOf(ConfigEventTvT.TVT_ENABLE_MIN_KILLS_TO_REWARD ? "Да" : "Нет"));
						npcHtmlMessage.replace("%team1name%", ConfigEventTvT.TVT_EVENT_TEAM_1_NAME);
						npcHtmlMessage.replace("%team1playercount%", String.valueOf(teamsPlayerCounts[0]));
						npcHtmlMessage.replace("%team2name%", ConfigEventTvT.TVT_EVENT_TEAM_2_NAME);
						npcHtmlMessage.replace("%team2playercount%", String.valueOf(teamsPlayerCounts[1]));
						npcHtmlMessage.replace("%playercount%", String.valueOf(teamsPlayerCounts[0] + teamsPlayerCounts[1]));
						playerInstance.sendPacket(npcHtmlMessage);
					}
					else
					{
						_log.info("htm not found: " + htmlPath + "Help.htm");
					}
					// Teleporter implements Runnable and starts itself
					new TvTEventTeleporter(playerInstance, team.getCoordinates(), false, false);
				});
			}
			broadcastPacketToTeams(new ExBlockUpSetState(timeLeft, 0, 0));
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, e.getMessage(), e);
		}
		firstblood = 0;
		return true;
	}

	/**
	 * Calculates the TvTEvent reward<br>
	 * 1. If both teams are at a tie(points equals), send it as system message to all participants, if one of the teams have 0 participants left online abort rewarding<br>
	 * 2. Wait till teams are not at a tie anymore<br>
	 * 3. Set state EvcentState.REWARDING<br>
	 * 4. Reward team with more points<br>
	 * 5. Show win html to wining team participants<br><br>
	 *
	 * @return String: winning team name<br>
	 */
	public static String calculateRewards()
	{
		if(_teams[0].getPoints() == _teams[1].getPoints())
		{
			// Check if one of the teams have no more players left
			if(_teams[0].getParticipatedPlayerCount() == 0 || _teams[1].getParticipatedPlayerCount() == 0)
			{
				// set state to rewarding
				setState(EventState.REWARDING);
				// return here, the fight can't be completed
				return "Стенка на стенку: Ивент закончен. Никто не победил!";
			}

			// Both teams have equals points
			sysMsgToAllParticipants("Стенка на стенку: Ивент закончен, команды сыграли вничью.");
			if(ConfigEventTvT.TVT_REWARD_TEAM_TIE)
			{
				rewardTeam(_teams[0]);
				rewardTeam(_teams[1]);
				return "Стенка на стенку: Ивент закончен, команды сыграли вничью.";
			}
			else
			{
				return "Стенка на стенку: Ивент закончен, команды сыграли вничью.";
			}
		}

		// Set state REWARDING so nobody can point anymore
		setState(EventState.REWARDING);

		// Get team which has more points
		TvTEventTeam team = _teams[_teams[0].getPoints() > _teams[1].getPoints() ? 0 : 1];
		rewardTeam(team);
		return "Стенка на стенку: Ивент закончен. Команда " + team.getName() + " выиграла с " + team.getPoints() + " очками.";
	}

	private static void rewardTeam(TvTEventTeam team)
	{
		// Iterate over all participated player instances of the winning team
		for(L2PcInstance playerInstance : team.getParticipatedPlayers().values())
		{
			if(playerInstance == null)
			{
				continue;
			}
			if(playerInstance.TvTGetPvPs() < ConfigEventTvT.TVT_MIN_KILLS_TO_REWARD && ConfigEventTvT.TVT_ENABLE_MIN_KILLS_TO_REWARD)
			{
				playerInstance.sendMessage("У Вас не хватает очков для получения награды!.");
				continue;
			}

			// Iterate over all tvt event rewards
			for(int[] reward : ConfigEventTvT.TVT_EVENT_REWARDS)
			{
				int itemId = 0;
				switch(reward[0])
				{
					case MultiSellData.PC_BANG_POINTS:
						if(Config.PCBANG_ENABLED)
						{
							playerInstance.setPcBangPoints(playerInstance.getPcBangPoints() + reward[1]);
						}
						continue;
					case MultiSellData.CLAN_REPUTATION:
						if(playerInstance.getClan() != null)
						{
							playerInstance.getClan().addReputationScore(reward[1], true);
						}
						playerInstance.sendMessage("Стенка на стенку: Ваш клан получает " + reward[1] + " очков репутации!");
						continue;
					case MultiSellData.FAME:
						playerInstance.setFame(playerInstance.getFame() + reward[1]);
						playerInstance.sendMessage("Стенка на стенку: Вы получили " + reward[1] + " славы!");
						continue;
					default:
						itemId = reward[0];
						break;
				}

				// Check for stackable item, non stackabe items need to be added one by one
				if(ItemTable.getInstance().createDummyItem(itemId).isStackable())
				{
					playerInstance.addItem(ProcessType.EVENT, itemId, reward[1], playerInstance, true);
				}
				else
				{
					for(int i = 0; i < reward[1]; ++i)
					{
						playerInstance.addItem(ProcessType.EVENT, itemId, 1, playerInstance, true);
					}
				}
			}

			NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(playerInstance.getObjectId());
			String html = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Reward.htm");
			if(html != null)
			{
				npcHtmlMessage.setHtml(html);
			}
			playerInstance.sendPacket(new ExUserInfoInvenWeight(playerInstance));
			playerInstance.sendPacket(npcHtmlMessage);
		}
	}

	/**
	 * Stops the TvTEvent fight<br>
	 * 1. Set state EventState.INACTIVATING<br>
	 * 2. Remove tvt npc from world<br>
	 * 3. Open doors specified in Configs<br>
	 * 4. Teleport all participants back to participation npc location<br>
	 * 5. Teams cleaning<br>
	 * 6. Set state EventState.INACTIVE<br>
	 */
	public static void stopFight()
	{
		// Set state INACTIVATING
		setState(EventState.INACTIVATING);
		//Unspawn event npc
		unSpawnNpc();
		// Opens all doors specified in Configs for tvt
		openDoors(ConfigEventTvT.TVT_DOORS_IDS_TO_CLOSE);
		// Closes all doors specified in Configs for tvt
		closeDoors(ConfigEventTvT.TVT_DOORS_IDS_TO_OPEN);

		// Iterate over all teams
		for(TvTEventTeam team : _teams)
		{
			// Check for nullpointer
			team.getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> {
				playerInstance.TvTSetKills(0);
				playerInstance.sendPacket(new ExBlockUpSetState(_teams[0].getPoints() < _teams[1].getPoints()));
				broadcastPacketToTeams(new ExBlockUpSetList(playerInstance, getParticipantTeamId(playerInstance.getObjectId()) == 1, true));
				new TvTEventTeleporter(playerInstance, ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES, false, false);
			});
		}

		// Cleanup of teams
		_teams[0].cleanMe();
		_teams[1].cleanMe();
		// Set state INACTIVE
		setState(EventState.INACTIVE);

		ThreadPoolManager.getInstance().scheduleGeneral(() -> {
			if(InstanceManager.getInstance().getInstance(_TvTEventInstance) != null)
			{
				InstanceManager.getInstance().destroyInstance(_TvTEventInstance);
			}
		}, ConfigEventTvT.TVT_EVENT_START_LEAVE_TELEPORT_DELAY * 1000);
	}

	private static boolean checkDualbox(L2PcInstance player)
	{
		String hwid = player.getClient().getHWID();
		for(L2PcInstance plr : _teams[0].getParticipatedPlayers().values())
		{
			if(plr.getClient().getHWID() != null && plr.getClient().getHWID().equals(hwid))
			{
				return true;
			}
		}
		for(L2PcInstance plr : _teams[1].getParticipatedPlayers().values())
		{
			if(plr.getClient().getHWID() != null && plr.getClient().getHWID().equals(hwid))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds a player to a TvTEvent team<br>
	 * 1. Calculate the id of the team in which the player should be added<br>
	 * 2. Add the player to the calculated team<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean addParticipant(L2PcInstance playerInstance)
	{
		synchronized(TvTEvent.class)
		{
			// Check for nullpoitner
			if(playerInstance == null)
			{
				return false;
			}

			if(checkDualbox(playerInstance))
			{
				playerInstance.sendMessage("Извините, но в ивенте не разрешено участие нескольких персонажей с одного компьютера.");
				return false;
			}

			byte teamId = 0;

			// Check to which team the player should be added
			teamId = _teams[0].getParticipatedPlayerCount() == _teams[1].getParticipatedPlayerCount() ? (byte) Rnd.get(2) : (byte) (_teams[0].getParticipatedPlayerCount() > _teams[1].getParticipatedPlayerCount() ? 1 : 0);

			return _teams[teamId].addPlayer(playerInstance);
		}
	}

	public static int getMemberCount()
	{
		int count = 0;
		for(TvTEventTeam team : _teams)
		{
			if(team != null)
			{
				count += team.getParticipatedPlayerCount();
			}
		}
		return count;
	}

	/**
	 * Removes a TvTEvent player from it's team<br>
	 * 1. Get team id of the player<br>
	 * 2. Remove player from it's team<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return boolean: true if success, otherwise false<br>
	 */
	public static boolean removeParticipant(int playerObjectId)
	{
		// Get the teamId of the player
		byte teamId = getParticipantTeamId(playerObjectId);

		// Check if the player is participant
		if(teamId != -1)
		{
			// Remove the player from team
			_teams[teamId].removePlayer(playerObjectId);
			return true;
		}

		return false;
	}

	public static boolean needParticipationFee()
	{
		return ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[0] != 0 && ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[1] != 0;
	}

	public static boolean hasParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.getInventory().getInventoryItemCount(ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[0], -1) >= ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[1];
	}

	public static boolean payParticipationFee(L2PcInstance playerInstance)
	{
		return playerInstance.destroyItemByItemId(ProcessType.EVENT, ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[0], ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[1], _lastNpcSpawn, true);
	}

	public static String getParticipationFee()
	{
		int itemId = ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[0];
		int itemNum = ConfigEventTvT.TVT_EVENT_PARTICIPATION_FEE[1];

		if(itemId == 0 || itemNum == 0)
		{
			return "-";
		}

		return StringUtil.concat(String.valueOf(itemNum), " ", ItemTable.getInstance().getTemplate(itemId).getName());
	}

	/**
	 * Send a SystemMessage to all participated players<br>
	 * 1. Send the message to all players of team number one<br>
	 * 2. Send the message to all players of team number two<br><br>
	 *
	 * @param message as String<br>
	 */
	public static void sysMsgToAllParticipants(String message)
	{
		_teams[0].getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> playerInstance.sendMessage(message));

		_teams[1].getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> playerInstance.sendMessage(message));
	}

	public static void msgToAllParticipants(L2PcInstance killedPlayerInstance, String message)
	{
		_teams[0].getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> playerInstance.sendPacket(new Say2(killedPlayerInstance.getObjectId(), ChatType.BATTLEFIELD, killedPlayerInstance.getName(), message)));

		_teams[1].getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> playerInstance.sendPacket(new Say2(killedPlayerInstance.getObjectId(), ChatType.BATTLEFIELD, killedPlayerInstance.getName(), message)));
	}

	/**
	 * Close doors specified in Configs
	 */
	private static void closeDoors(List<Integer> doors)
	{
		for(int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorGeoEngine.getInstance().getDoor(doorId);

			if(doorInstance != null)
			{
				doorInstance.closeMe();
			}
		}
	}

	/**
	 * Open doors specified in Configs
	 */
	private static void openDoors(List<Integer> doors)
	{
		for(int doorId : doors)
		{
			L2DoorInstance doorInstance = DoorGeoEngine.getInstance().getDoor(doorId);

			if(doorInstance != null)
			{
				doorInstance.openMe();
			}
		}
	}

	/**
	 * UnSpawns the TvTEvent npc
	 */
	private static void unSpawnNpc()
	{
		// Delete the npc
		_lastNpcSpawn.getLocationController().delete();
		SpawnTable.getInstance().deleteSpawn(_lastNpcSpawn.getSpawn());
		// Stop respawning of the npc
		_npcSpawn.stopRespawn();
		_npcSpawn = null;
		_lastNpcSpawn = null;
	}

	/**
	 * Called when a player logs in<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onLogin(L2PcInstance playerInstance)
	{
		if(playerInstance == null || !isStarting() && !isStarted())
		{
			return;
		}

		byte teamId = getParticipantTeamId(playerInstance.getObjectId());

		if(teamId == -1)
		{
			return;
		}

		_teams[teamId].addPlayer(playerInstance);
		new TvTEventTeleporter(playerInstance, _teams[teamId].getCoordinates(), true, false);
	}

	/**
	 * Called when a player logs out<br><br>
	 *
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onLogout(L2PcInstance playerInstance)
	{
		if(playerInstance != null && (isStarting() || isStarted() || isParticipating()))
		{
			if(removeParticipant(playerInstance.getObjectId()))
			{
				playerInstance.setXYZ(ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[0] + Rnd.get(101) - 50, ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[1] + Rnd.get(101) - 50, ConfigEventTvT.TVT_EVENT_PARTICIPATION_NPC_COORDINATES[2], false);
			}
		}
	}

	/**
	 * Called on every bypass by npc of type L2TvTEventNpc<br>
	 * Needs synchronization cause of the max player check<br><br>
	 *
	 * @param command as String<br>
	 * @param playerInstance as L2PcInstance<br>
	 */
	public static void onBypass(String command, L2PcInstance playerInstance)
	{
		synchronized(TvTEvent.class)
		{
			if(playerInstance == null || !isParticipating())
			{
				return;
			}

			String htmContent;

			if(command.equals("tvt_event_participation"))
			{
				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);
				int playerLevel = playerInstance.getLevel();

				if(playerInstance.isCursedWeaponEquipped())
				{
					htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "CursedWeaponEquipped.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
					}
				}
				else if(OlympiadManager.getInstance().isRegistered(playerInstance))
				{
					htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Olympiad.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
					}
				}
				else if(playerInstance.hasBadReputation())
				{
					htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Karma.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
					}
				}
				else if(playerLevel < ConfigEventTvT.TVT_EVENT_MIN_LVL || playerLevel > ConfigEventTvT.TVT_EVENT_MAX_LVL)
				{
					htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Level.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%min%", String.valueOf(ConfigEventTvT.TVT_EVENT_MIN_LVL));
						npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventTvT.TVT_EVENT_MAX_LVL));
					}
				}
				else if(_teams[0].getParticipatedPlayerCount() == ConfigEventTvT.TVT_EVENT_MAX_PLAYERS_IN_TEAMS && _teams[1].getParticipatedPlayerCount() == ConfigEventTvT.TVT_EVENT_MAX_PLAYERS_IN_TEAMS)
				{
					htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "TeamsFull.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%max%", String.valueOf(ConfigEventTvT.TVT_EVENT_MAX_PLAYERS_IN_TEAMS));
					}
				}
				// TODO: Ограничение по HWID
			/*else if (Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP > 0
					&& !AntiFeedManager.getInstance().tryAddPlayer(AntiFeedManager.TVT_ID, playerInstance, Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP))
			{
				htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath+"IPRestriction.htm");
				if (htmContent != null)
				{
					npcHtmlMessage.setHtml(htmContent);
					npcHtmlMessage.replace("%max%", String.valueOf(Config.TVT_EVENT_MAX_PARTICIPANTS_PER_IP));
				}
			}*/
				else if(needParticipationFee() && !hasParticipationFee(playerInstance))
				{
					htmContent = HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "ParticipationFee.htm");
					if(htmContent != null)
					{
						npcHtmlMessage.setHtml(htmContent);
						npcHtmlMessage.replace("%fee%", getParticipationFee());
					}
				}
				else if(addParticipant(playerInstance))
				{
					npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Registered.htm"));
				}
				else
				{
					return;
				}

				playerInstance.sendPacket(npcHtmlMessage);
			}
			else if(command.equals("tvt_event_remove_participation"))
			{
				removeParticipant(playerInstance.getObjectId());

				NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(0);

				npcHtmlMessage.setHtml(HtmCache.getInstance().getHtm(playerInstance.getLang(), htmlPath + "Unregistered.htm"));
				playerInstance.sendPacket(npcHtmlMessage);
			}
		}
	}

	/**
	 * Called on every onAction in L2PcIstance<br><br>
	 *
	 * @param playerInstance as String<br>
	 * @param targetedPlayerObjectId as String<br>
	 * @return boolean: true if player is allowed to target, otherwise false<br>
	 */
	public static boolean onAction(L2PcInstance playerInstance, int targetedPlayerObjectId)
	{
		if(playerInstance == null || !isStarted())
		{
			return true;
		}

		if(playerInstance.isGM())
		{
			return true;
		}

		byte playerTeamId = getParticipantTeamId(playerInstance.getObjectId());
		byte targetedPlayerTeamId = getParticipantTeamId(targetedPlayerObjectId);

		if(playerTeamId != -1 && targetedPlayerTeamId == -1 || playerTeamId == -1 && targetedPlayerTeamId != -1)
		{
			return false;
		}

		return !(playerTeamId != -1 && targetedPlayerTeamId != -1 && playerTeamId == targetedPlayerTeamId && playerInstance.getObjectId() != targetedPlayerObjectId && !ConfigEventTvT.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED && !ConfigEventTvT.TVT_EVENT_TARGET_TEAM_MEMBERS_ALLOWED_EXCEPTIONS.contains(Integer.valueOf(playerInstance.getActiveClassId())));

	}

	/**
	 * Called on every scroll use<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return boolean: true if player is allowed to use scroll, otherwise false<br>
	 */
	public static boolean onScrollUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}
		return !(isPlayerParticipant(playerObjectId) && !ConfigEventTvT.TVT_EVENT_SCROLL_ALLOWED);

	}

	/**
	 * Called on every potion use<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return boolean: true if player is allowed to use potions, otherwise false<br>
	 */
	public static boolean onPotionUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}
		return !(isPlayerParticipant(playerObjectId) && !ConfigEventTvT.TVT_EVENT_POTIONS_ALLOWED);

	}

	/**
	 * Called on every escape use(thanks to nbd)<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return boolean: true if player is not in tvt event, otherwise false<br>
	 */
	public static boolean onEscapeUse(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}
		return !isPlayerParticipant(playerObjectId);

	}

	/**
	 * Called on every summon item use<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return boolean: true if player is allowed to summon by item, otherwise false<br>
	 */
	public static boolean onItemSummon(int playerObjectId)
	{
		if(!isStarted())
		{
			return true;
		}
		return !(isPlayerParticipant(playerObjectId) && !ConfigEventTvT.TVT_EVENT_SUMMON_BY_ITEM_ALLOWED);

	}

	/**
	 * Is called when a player is killed<br><br>
	 *
	 * @param killerCharacter as L2Character<br>
	 * @param killedPlayerInstance as L2PcInstance<br>
	 */
	public static void onKill(L2Character killerCharacter, L2PcInstance killedPlayerInstance)
	{
		if(killedPlayerInstance == null || !isStarted())
		{
			return;
		}

		byte killedTeamId = getParticipantTeamId(killedPlayerInstance.getObjectId());

		if(killedTeamId == -1)
		{
			return;
		}

		new TvTEventTeleporter(killedPlayerInstance, _teams[killedTeamId].getCoordinates(), false, false);

		if(killerCharacter == null)
		{
			return;
		}

		L2PcInstance killerPlayerInstance = null;

		if(killerCharacter instanceof L2PetInstance || killerCharacter instanceof L2SummonInstance)
		{
			killerPlayerInstance = ((L2Summon) killerCharacter).getOwner();

			if(killerPlayerInstance == null)
			{
				return;
			}
		}
		else if(killerCharacter instanceof L2PcInstance)
		{
			killerPlayerInstance = (L2PcInstance) killerCharacter;
		}
		else
		{
			return;
		}

		byte killerTeamId = getParticipantTeamId(killerPlayerInstance.getObjectId());

		if(killerTeamId != -1 && killedTeamId != -1 && killerTeamId != killedTeamId)
		{
			TvTEventTeam killerTeam = _teams[killerTeamId];

			killerTeam.increasePoints();
			killerPlayerInstance.TvTIncreasePvPs();
			killerPlayerInstance.TvTIncreasePvPsWithoutDie();

			if(firstblood == 0 && ConfigEventTvT.TVT_FIRST_BLOOD_MODE)
			{
				firstblood = 1;
				RewardPlayerWithoutDie(killerPlayerInstance, ConfigEventTvT.TVT_FIRST_BLOOD_REWARD_ID, ConfigEventTvT.TVT_FIRST_BLOOD_REWARD_COUNT);
				msgToAllParticipants(killerPlayerInstance, "First Blood");
			}

			for(L2PcInstance playerInstance : _teams[killerTeamId].getParticipatedPlayers().values())
			{
				if(playerInstance != null)
				{
					playerInstance.sendPacket(new Say2(killerPlayerInstance.getObjectId(), ChatType.TELL, killerPlayerInstance.getName(), "Вы убили " + killedPlayerInstance.getName() + '!'));
				}
			}

			if(killerPlayerInstance.TvTGetPvPsWithoutDie() >= 5)
			{
				if(killerPlayerInstance.TvTGetPvPsWithoutDie() == 5 && ConfigEventTvT.TVT_KILLING_SPREE_MODE)
				{
					RewardPlayerWithoutDie(killerPlayerInstance, ConfigEventTvT.TVT_KILLING_SPREE_REWARD_ID, ConfigEventTvT.TVT_KILLING_SPREE_REWARD_COUNT);
					msgToAllParticipants(killerPlayerInstance, "Словил раж убийства!");
				}
				else if(killerPlayerInstance.TvTGetPvPsWithoutDie() == 10 && ConfigEventTvT.TVT_UNSTOPPABLE_MODE)
				{
					RewardPlayerWithoutDie(killerPlayerInstance, ConfigEventTvT.TVT_UNSTOPPABLE_REWARD_ID, ConfigEventTvT.TVT_UNSTOPPABLE_REWARD_COUNT);
					msgToAllParticipants(killerPlayerInstance, "Непобедим!");
				}
				else if(killerPlayerInstance.TvTGetPvPsWithoutDie() == 15 && ConfigEventTvT.TVT_GOD_LIKE_MODE)
				{
					RewardPlayerWithoutDie(killerPlayerInstance, ConfigEventTvT.TVT_GOD_LIKE_REWARD_ID, ConfigEventTvT.TVT_GOD_LIKE_REWARD_COUNT);
					msgToAllParticipants(killerPlayerInstance, "Божит!");
				}
				killerPlayerInstance.startAbnormalEffect(AbnormalEffect.REAL_TARGET);
			}
			
			/*
			* @param timeLeft Time Left before Minigame's End
			* @param bluePoints Current Blue Team Points
			* @param redPoints Current Blue Team points
			* @param isRedTeam Is Player from Red Team?
			* @param player Player Instance
			* @param playerPoints Current Player Points
			*/

			try
			{
				int timeLeft = (int) ((_startedTime - System.currentTimeMillis()) / 1000);
				int[] points = new int[2];
				points[0] = _teams[0].getPoints();
				points[1] = _teams[1].getPoints();

				broadcastPacketToTeams(new ExBlockUpSetState(timeLeft, points[0], points[1]), new ExBlockUpSetState(timeLeft, points[0], points[1], getParticipantTeamId(killerPlayerInstance.getObjectId()) == 1, killerPlayerInstance, killerPlayerInstance.TvTGetPvPs()));
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, e.getMessage(), e);
			}
		}
	}

	public static void RewardPlayerWithoutDie(L2PcInstance player, int itemId, int count)
	{
		player.addItem(ProcessType.EVENT, itemId, count, player, true);
	}

	public static void broadcastPacketToTeams(L2GameServerPacket... packets)
	{
		for(TvTEventTeam team : _teams)
		{
			team.getParticipatedPlayers().values().stream().filter(playerInstance -> playerInstance != null).forEach(playerInstance -> {
				for(L2GameServerPacket packet : packets)
				{
					playerInstance.sendPacket(packet);
				}
			});
		}
	}

	/**
	 * Called on Appearing packet received (player finished teleporting)<br><br>
	 *
	 * @param playerInstance L2PcInstance
	 */
	public static void onTeleported(L2PcInstance playerInstance)
	{
		if(!isStarted() || playerInstance == null || !isPlayerParticipant(playerInstance.getObjectId()))
		{
			return;
		}

		if(playerInstance.isMageClass())
		{
			if(ConfigEventTvT.TVT_EVENT_MAGE_BUFFS != null && !ConfigEventTvT.TVT_EVENT_MAGE_BUFFS.isEmpty())
			{
				for(int i : ConfigEventTvT.TVT_EVENT_MAGE_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, ConfigEventTvT.TVT_EVENT_MAGE_BUFFS.get(i));
					if(skill != null)
					{
						skill.getEffects(playerInstance, playerInstance);
						if(playerInstance.hasSummon())
						{
							for(L2Summon pet : playerInstance.getPets())
							{
								skill.getEffects(pet, pet);
							}
						}
					}
				}
			}
		}
		else
		{
			if(ConfigEventTvT.TVT_EVENT_FIGHTER_BUFFS != null && !ConfigEventTvT.TVT_EVENT_FIGHTER_BUFFS.isEmpty())
			{
				for(int i : ConfigEventTvT.TVT_EVENT_FIGHTER_BUFFS.keys())
				{
					L2Skill skill = SkillTable.getInstance().getInfo(i, ConfigEventTvT.TVT_EVENT_FIGHTER_BUFFS.get(i));
					if(skill != null)
					{
						skill.getEffects(playerInstance, playerInstance);
						if(playerInstance.hasSummon())
						{
							for(L2Summon pet : playerInstance.getPets())
							{
								skill.getEffects(pet, pet);
							}
						}
					}
				}
			}
		}

		playerInstance.setCurrentCp(playerInstance.getMaxCp());
		playerInstance.setCurrentHp(playerInstance.getMaxHp());
		playerInstance.setCurrentMp(playerInstance.getMaxMp());

		playerInstance.broadcastStatusUpdate();
		playerInstance.broadcastUserInfo();
	}

	/*
	 * Return true if player valid for skill
	 */
	public static boolean checkForTvTSkill(L2PcInstance source, L2PcInstance target, L2Skill skill)
	{
		if(!isStarted())
		{
			return true;
		}
		// TvT is started
		int sourcePlayerId = source.getObjectId();
		int targetPlayerId = target.getObjectId();
		boolean isSourceParticipant = isPlayerParticipant(sourcePlayerId);
		boolean isTargetParticipant = isPlayerParticipant(targetPlayerId);

		// both players not participating
		if(!isSourceParticipant && !isTargetParticipant)
		{
			return true;
		}
		// one player not participating
		if(!(isSourceParticipant && isTargetParticipant))
		{
			return false;
		}
		// players in the different teams ?
		if(getParticipantTeamId(sourcePlayerId) != getParticipantTeamId(targetPlayerId))
		{
			if(!skill.isOffensive())
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets the TvTEvent state<br><br>
	 *
	 * @param state as EventState<br>
	 */
	private static void setState(EventState state)
	{
		synchronized(_state)
		{
			_state = state;
		}
	}

	/**
	 * Is TvTEvent inactive?<br><br>
	 *
	 * @return boolean: true if event is inactive(waiting for next event cycle), otherwise false<br>
	 */
	public static boolean isInactive()
	{
		boolean isInactive;

		synchronized(_state)
		{
			isInactive = _state == EventState.INACTIVE;
		}

		return isInactive;
	}

	/**
	 * Is TvTEvent in inactivating?<br><br>
	 *
	 * @return boolean: true if event is in inactivating progress, otherwise false<br>
	 */
	public static boolean isInactivating()
	{
		boolean isInactivating;

		synchronized(_state)
		{
			isInactivating = _state == EventState.INACTIVATING;
		}

		return isInactivating;
	}

	/**
	 * Is TvTEvent in participation?<br><br>
	 *
	 * @return boolean: true if event is in participation progress, otherwise false<br>
	 */
	public static boolean isParticipating()
	{
		boolean isParticipating;

		synchronized(_state)
		{
			isParticipating = _state == EventState.PARTICIPATING;
		}

		return isParticipating;
	}

	/**
	 * Is TvTEvent starting?<br><br>
	 *
	 * @return boolean: true if event is starting up(setting up fighting spot, teleport players etc.), otherwise false<br>
	 */
	public static boolean isStarting()
	{
		boolean isStarting;

		synchronized(_state)
		{
			isStarting = _state == EventState.STARTING;
		}

		return isStarting;
	}

	/**
	 * Is TvTEvent started?<br><br>
	 *
	 * @return boolean: true if event is started, otherwise false<br>
	 */
	public static boolean isStarted()
	{
		boolean isStarted;

		synchronized(_state)
		{
			isStarted = _state == EventState.STARTED;
		}

		return isStarted;
	}

	/**
	 * Is TvTEvent rewadrding?<br><br>
	 *
	 * @return boolean: true if event is currently rewarding, otherwise false<br>
	 */
	public static boolean isRewarding()
	{
		boolean isRewarding;

		synchronized(_state)
		{
			isRewarding = _state == EventState.REWARDING;
		}

		return isRewarding;
	}

	/**
	 * Returns the team id of a player, if player is not participant it returns -1<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return byte: team name of the given playerName, if not in event -1<br>
	 */
	public static byte getParticipantTeamId(int playerObjectId)
	{
		return (byte) (_teams[0].containsPlayer(playerObjectId) ? 0 : _teams[1].containsPlayer(playerObjectId) ? 1 : -1);
	}

	/**
	 * Returns the team of a player, if player is not participant it returns null <br><br>
	 *
	 * @param playerObjectId objectId as Integer<br>
	 * @return TvTEventTeam: team of the given playerObjectId, if not in event null <br>
	 */
	public static TvTEventTeam getParticipantTeam(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0] : _teams[1].containsPlayer(playerObjectId) ? _teams[1] : null;
	}

	/**
	 * Returns the enemy team of a player, if player is not participant it returns null <br><br>
	 *
	 * @param playerObjectId objectId as Integer<br>
	 * @return TvTEventTeam: enemy team of the given playerObjectId, if not in event null <br>
	 */
	public static TvTEventTeam getParticipantEnemyTeam(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[1] : _teams[1].containsPlayer(playerObjectId) ? _teams[0] : null;
	}

	/**
	 * Returns the team coordinates in which the player is in, if player is not in a team return null<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return int[]: coordinates of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getParticipantTeamCoordinates(int playerObjectId)
	{
		return _teams[0].containsPlayer(playerObjectId) ? _teams[0].getCoordinates() : _teams[1].containsPlayer(playerObjectId) ? _teams[1].getCoordinates() : null;
	}

	/**
	 * Is given player participant of the event?<br><br>
	 *
	 * @param playerObjectId as String<br>
	 * @return boolean: true if player is participant, ohterwise false<br>
	 */
	public static boolean isPlayerParticipant(int playerObjectId)
	{
		if(!isParticipating() && !isStarting() && !isStarted())
		{
			return false;
		}

		return _teams[0].containsPlayer(playerObjectId) || _teams[1].containsPlayer(playerObjectId);
	}

	/**
	 * Returns participated player count<br><br>
	 *
	 * @return int: amount of players registered in the event<br>
	 */
	public static int getParticipatedPlayersCount()
	{
		if(!isParticipating() && !isStarting() && !isStarted())
		{
			return 0;
		}

		return _teams[0].getParticipatedPlayerCount() + _teams[1].getParticipatedPlayerCount();
	}

	/**
	 * Returns teams names<br><br>
	 *
	 * @return String[]: names of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static String[] getTeamNames()
	{
		return new String[]{_teams[0].getName(), _teams[1].getName()};
	}

	/**
	 * Returns player count of both teams<br><br>
	 *
	 * @return int[]: player count of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getTeamsPlayerCounts()
	{
		return new int[]{_teams[0].getParticipatedPlayerCount(), _teams[1].getParticipatedPlayerCount()};
	}

	/**
	 * Returns points count of both teams
	 *
	 * @return int[]: points of teams, 2 elements, index 0 for team 1 and index 1 for team 2<br>
	 */
	public static int[] getTeamsPoints()
	{
		return new int[]{_teams[0].getPoints(), _teams[1].getPoints()};
	}

	public static int getTvTEventInstance()
	{
		return _TvTEventInstance;
	}

	public static int getCurrentMapId()
	{
		return _mapId;
	}
}