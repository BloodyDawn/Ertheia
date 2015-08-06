package dwo.gameserver.model.world.quest;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.IHook;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.L2Trap;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2TrapInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.olympiad.CompetitionType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExNpcQuestHtmlMessage;
import dwo.gameserver.util.MinionList;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class Quest implements IHook
{
	protected static final Logger _log = LogManager.getLogger(Quest.class);
	// Болванки квестовых стейтов
	protected static final QuestStateType CREATED = QuestStateType.CREATED;
	protected static final QuestStateType STARTED = QuestStateType.STARTED;
	protected static final QuestStateType COMPLETED = QuestStateType.COMPLETED;
	private static final String DEFAULT_LOW_LEVEL_MSG = "<html><body>Вы должны достичь %level% уровня, чтобы получить возможность взять это задание.</body></html>";
	private static final String DEFAULT_HIGH_LEVEL_MSG = "<html><body>Ваш уровень должен быть меньше %level% уровня, чтобы взять это задание.</body></html>";
	private static final String QUEST_DELETE_FROM_CHAR_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=?";
	private static final String QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY = "DELETE FROM character_quests WHERE charId=? AND name=? AND var!=?";
	private static final int RESET_HOUR = 6;
	private static final int RESET_MINUTES = 30;
	private static Map<Integer, Map<Integer, Quest>> _allAskEvents = new FastMap<>();
	/**
	 * HashMap containing lists of timers from the name of the timer
	 */
	protected final FastMap<String, List<QuestTimer>> _allEventTimers = new FastMap<>();
	protected final List<Integer> _questInvolvedNpcs = new ArrayList<>();
	protected final Map<Integer, List<Integer>> _questInvolvedAskIds = new FastMap<>();
	private final ReentrantReadWriteLock _rwLock = new ReentrantReadWriteLock();
	private final WriteLock _writeLock = _rwLock.writeLock();
	private final ReadLock _readLock = _rwLock.readLock();
	private final QuestStateType _initialState = QuestStateType.CREATED;
	// NOTE: questItemIds will be overridden by child classes.  Ideally, it should be
	// protected instead of public.  However, quest scripts written in Jython will
	// have trouble with protected, as Jython only knows private and public...
	// In fact, protected will typically be considered private thus breaking the scripts.
	// Leave this as public as a workaround.
	public int[] questItemIds;
	public int minLevel;
	public int maxLevel = 100;
	// Альтернативная загрузка квестов с ядра
	boolean _altMethodCall = true;

	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 * Parameter ID = -1
	 * @param name : String corresponding to the name of the quest
	 * @param descr : String for the description of the quest
	 */
	public Quest(String name, String descr)
	{
		this(-1, name, descr);
	}

	/**
	 * (Constructor)Add values to class variables and put the quest in HashMaps.
	 *
	 * @param questId : int pointing out the ID of the quest
	 * @param name    : String corresponding to the name of the quest
	 * @param descr   : String for the description of the quest
	 */
	public Quest(int questId, String name, String descr)
	{
		_allEventTimers.shared();
		QuestManager.getInstance().addQuest(this);
	}

	public Quest()
	{
		_allEventTimers.shared();
		QuestManager.getInstance().addQuest(this);
	}

	public static Quest getQuestsForAskId(int npcId, int askId)
	{
		if(!_allAskEvents.containsKey(npcId))
		{
			return null;
		}

		if(!_allAskEvents.get(npcId).containsKey(askId))
		{
			return null;
		}

		return _allAskEvents.get(npcId).get(askId);
	}

	/**
	 * Add quests to the L2PCInstance of the player.<BR><BR>
	 * <U><I>Action : </U></I><BR>
	 * Add state of quests, drops and variables for quests in the HashMap _quest of L2PcInstance
	 *
	 * @param player : Player who is entering the world
	 */
	public static void playerEnter(L2PcInstance player)
	{
		ThreadConnection con = null;
		try
		{
			// Get list of quests owned by the player from database
			con = L2DatabaseFactory.getInstance().getConnection();

			FiltredPreparedStatement invalidQuestData = con.prepareStatement("DELETE FROM character_quests WHERE charId=? and name=?");
			FiltredPreparedStatement invalidQuestDataVar = con.prepareStatement("DELETE FROM character_quests WHERE charId=? and name=? and var=?");

			FiltredPreparedStatement statement = con.prepareStatement(Characters.SELECT_CHARACTER_QUESTS_NAME_VALUE);
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			ResultSet rs = statement.executeQuery();
			while(rs.next())
			{
				// Get ID of the quest and ID of its state
				String questId = rs.getString("name");
				QuestStateType statename = QuestStateType.valueOf(rs.getString("value"));

				// Search quest associated with the ID
				Quest q = QuestManager.getInstance().getQuest(questId);
				if(q == null)
				{
					_log.log(Level.INFO, "Unknown quest " + questId + " for player " + player.getName());
					if(Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestData.setInt(1, player.getObjectId());
						invalidQuestData.setString(2, questId);
						invalidQuestData.executeUpdate();
					}
					continue;
				}

				// Create a new QuestState for the player that will be added to the player's list of quests
				new QuestState(q, player, statename);
			}
			rs.close();
			invalidQuestData.close();
			DatabaseUtils.closeStatement(statement);

			// Get list of quests owned by the player from the DB in order to add variables used in the quest.
			statement = con.prepareStatement(Characters.SELECT_CHARACTER_QUESTS_NAME_VALUE_VAR);
			statement.setInt(1, player.getObjectId());
			statement.setString(2, "<state>");
			rs = statement.executeQuery();
			while(rs.next())
			{
				String questName = rs.getString("name");
				String var = rs.getString("var");
				String value = rs.getString("value");
				// Get the QuestState saved in the loop before
				QuestState qs = player.getQuestState(questName);
				if(qs == null)
				{
					_log.log(Level.INFO, "Lost variable " + var + " in quest " + questName + " for player " + player.getName());
					if(Config.AUTODELETE_INVALID_QUEST_DATA)
					{
						invalidQuestDataVar.setInt(1, player.getObjectId());
						invalidQuestDataVar.setString(2, questName);
						invalidQuestDataVar.setString(3, var);
						invalidQuestDataVar.executeUpdate();
					}
					continue;
				}
				// Add parameter to the quest
				qs.setInternal(var, value);
			}
			rs.close();
			invalidQuestDataVar.close();
			DatabaseUtils.closeStatement(statement);

		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not insert char quest:", e);
		}
		finally
		{
			DatabaseUtils.closeConnection(con);
		}
	}

	/**
	 * Сохраняем квесст игрока в базу
	 * @param qs    текущее состояние квеста
	 * @param var   имя переменной в квесте
	 * @param value значение переменной в квесте
	 */
	public static void createQuestVarInDb(QuestState qs, String var, String value)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_quests (charId,name,var,value) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE value=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.setString(4, value);
			statement.setString(5, value);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not insert char quest:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Update the value of the variable "var" for the quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * The selection of the right record is made with :
	 * <LI>charId = qs.getPlayer().getObjectID()</LI>
	 * <LI>name = qs.getQuest().getName()</LI>
	 * <LI>var = var</LI>
	 * <BR><BR>
	 * The modification made is :
	 * <LI>value = parameter value</LI>
	 *
	 * @param qs    : Quest State
	 * @param var   : String designating the name of the variable for quest
	 * @param value : String designating the value of the variable for quest
	 */
	public static void updateQuestVarInDb(QuestState qs, String var, String value)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_quests SET value=? WHERE charId=? AND name=? AND var = ?");
			statement.setString(1, value);
			statement.setInt(2, qs.getPlayer().getObjectId());
			statement.setString(3, qs.getQuestName());
			statement.setString(4, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not update char quest:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Delete a variable of player's quest from the database.
	 *
	 * @param qs  : object QuestState pointing out the player's quest
	 * @param var : String designating the variable characterizing the quest
	 */
	public static void deleteQuestVarInDb(QuestState qs, String var)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");
			statement.setInt(1, qs.getPlayer().getObjectId());
			statement.setString(2, qs.getQuestName());
			statement.setString(3, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not delete char quest:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Удаляем квест игрока из базы
	 * @param qs текущее состояние квеста
	 */
	public static void deleteQuestInDb(QuestState qs, boolean repeatable)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			if(repeatable)
			{
				statement = con.prepareStatement(QUEST_DELETE_FROM_CHAR_QUERY);
				statement.setInt(1, qs.getPlayer().getObjectId());
				statement.setString(2, qs.getQuestName());
				statement.execute();
			}
			else
			{
				statement = con.prepareStatement(QUEST_DELETE_FROM_CHAR_QUERY_NON_REPEATABLE_QUERY);
				statement.setInt(1, qs.getPlayer().getObjectId());
				statement.setString(2, qs.getQuestName());
				statement.setString(3, "<state>");
				statement.execute();
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not delete char quest:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Create a record in database for quest.<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * Use fucntion createQuestVarInDb() with following parameters :<BR>
	 * <LI>QuestState : parameter sq that puts in fields of database :
	 * <UL type="square">
	 * <LI>charId : ID of the player</LI>
	 * <LI>name : name of the quest</LI>
	 * </UL>
	 * </LI>
	 * <LI>var : string "&lt;state&gt;" as the name of the variable for the quest</LI>
	 * <LI>val : string corresponding at the ID of the state (in fact, initial state)</LI>
	 *
	 * @param qs : QuestState
	 */
	public static void createQuestInDb(QuestState qs)
	{
		createQuestVarInDb(qs, "<state>", qs.getState().toString());
	}

	/**
	 * Update informations regarding quest in database.<BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Get ID state of the quest recorded in object qs</LI>
	 * <LI>Test if quest is completed. If true, add a star (*) before the ID state</LI>
	 * <LI>Save in database the ID state (with or without the star) for the variable called "&lt;state&gt;" of the quest</LI>
	 *
	 * @param qs : QuestState
	 */
	public static void updateQuestInDb(QuestState qs)
	{
		updateQuestVarInDb(qs, "<state>", qs.getState().toString());
	}

	/**
	 * @param player действующий персонаж
	 * @return default html page "You are either not on a quest that involves this NPC.."
	 */
	public static String getNoQuestMsg(L2PcInstance player)
	{
		return HtmCache.getInstance().getHtm(player.getLang(), "default/noquest.htm");
	}

	/**
	 * Возвращает стандартную хтмлку о том,
	 * что игрок не достиг требуемого уровня
	 * @param level уровень, требующийся для взятия квеста
	 * @return форматированное сообщение
	 */
	public static String getLowLevelMsg(int level)
	{
		return DEFAULT_LOW_LEVEL_MSG.replace("%level%", String.valueOf(level));
	}

	/**
	 * Возвращает стандартную хтмлку о том,
	 * что игрок выше требуемого уровня
	 * @param level уровень, требующийся для взятия квеста
	 * @return форматированное сообщение
	 */
	public static String getHighLevelMsg(int level)
	{
		return DEFAULT_HIGH_LEVEL_MSG.replace("%level%", String.valueOf(level));
	}

	/**
	 * @return the reset hour for a daily quest, could be overridden on a script.
	 */
	public int getResetHour()
	{
		return RESET_HOUR;
	}

	/**
	 * @return the reset minutes for a daily quest, could be overridden on a script.
	 */
	public int getResetMinutes()
	{
		return RESET_MINUTES;
	}

	/**
	 * The function saveGlobalData is, by default, called at shutdown, for all quests, by the QuestManager.
	 * Children of this class can implement this function in order to convert their structures
	 * into <var, value> tuples and make calls to save them to the database, if needed.
	 * By default, nothing is saved.
	 */
	public void saveGlobalData()
	{

	}

	/**
	 * @return ID квеста
	 */
	public int getQuestId()
	{
		return -1;
	}

	/**
	 * Add a new QuestState to the database and return it.
	 * @param player : игрок
	 * @return QuestState : QuestState created
	 */
	public QuestState newQuestState(L2PcInstance player)
	{
		return new QuestState(this, player, _initialState);
	}

	// these are methods to call from java

	/**
	 * @return State начальное состояние квеста
	 */
	public QuestStateType getInitialState()
	{
		return _initialState;
	}

	/**
	 * Add a timer to the quest, if it doesn't exist already
	 *
	 * @param name:   name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time:   time in ms for when to fire the timer
	 * @param npc:    npc associated with this timer (can be null)
	 * @param player: player associated with this timer (can be null)
	 */
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player)
	{
		startQuestTimer(name, time, npc, player, false);
	}

	/**
	 * Add a timer to the quest, if it doesn't exist already.  If the timer is repeatable,
	 * it will auto-fire automatically, at a fixed rate, until explicitly canceled.
	 *
	 * @param name:       name of the timer (also passed back as "event" in onAdvEvent)
	 * @param time:       time in ms for when to fire the timer
	 * @param npc:        npc associated with this timer (can be null)
	 * @param player:     player associated with this timer (can be null)
	 * @param repeating: indicates if the timer is repeatable or one-time.
	 */
	public void startQuestTimer(String name, long time, L2Npc npc, L2PcInstance player, boolean repeating)
	{
		// Add quest timer if timer doesn't already exist
		List<QuestTimer> timers = _allEventTimers.get(name);
		// no timer exists with the same name, at all
		if(timers == null)
		{
			timers = new ArrayList<>();
			timers.add(new QuestTimer(this, name, time, npc, player, repeating));
			_allEventTimers.put(name, timers);
		}
		// a timer with this name exists, but may not be for the same set of npc and player
		else
		{
			// if there exists a timer with this name, allow the timer only if the [npc, player] set is unique
			// nulls act as wildcards
			if(getQuestTimer(name, npc, player) == null)
			{
				_writeLock.lock();
				try
				{
					timers.add(new QuestTimer(this, name, time, npc, player, repeating));
				}
				finally
				{
					_writeLock.unlock();
				}
			}
		}
	}

	public QuestTimer getQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		List<QuestTimer> timers = _allEventTimers.get(name);
		if(timers != null)
		{
			_readLock.lock();
			try
			{
				for(QuestTimer timer : timers)
				{
					if(timer != null)
					{
						if(timer.isMatch(this, name, npc, player))
						{
							return timer;
						}
					}
				}
			}
			finally
			{
				_readLock.unlock();
			}
		}
		return null;
	}

	public void cancelQuestTimers(String name)
	{
		List<QuestTimer> timers = _allEventTimers.get(name);
		if(timers != null)
		{
			_writeLock.lock();
			try
			{
				timers.stream().filter(timer -> timer != null).forEach(QuestTimer::cancel);
				timers.clear();
			}
			finally
			{
				_writeLock.unlock();
			}
		}
	}

	public void cancelQuestTimer(String name, L2Npc npc, L2PcInstance player)
	{
		QuestTimer timer = getQuestTimer(name, npc, player);
		if(timer != null)
		{
			timer.cancelAndRemove();
		}
	}

	public void removeQuestTimer(QuestTimer timer)
	{
		if(timer != null)
		{
			List<QuestTimer> timers = _allEventTimers.get(timer.getName());
			if(timers != null)
			{
				_writeLock.lock();
				try
				{
					timers.remove(timer);
				}
				finally
				{
					_writeLock.unlock();
				}
			}
		}
	}

	public boolean notifyAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAttack(npc, attacker, damage, isPet, skill);
		}
		catch(Exception e)
		{
			int npcObjectId = 0;
			if(npc != null)
			{
				npcObjectId = npc.getObjectId();
			}
			int skillId = 0;
			if(skill != null)
			{
				skillId = skill.getId();
			}

			_log.log(Level.ERROR, "Error while notifyAttack npc: " + npcObjectId + " skill: " + skillId + " QN: " + getName(), e);
			return showError(attacker, e);
		}
		return showResult(attacker, res, false);
	}

	public boolean notifyItemUse(L2Item item, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onItemUse(item, player);
		}
		catch(Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	public boolean notifySpellFinished(L2Npc instance, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onSpellFinished(instance, player, skill);
		}
		catch(Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	/**
	 * Notify quest script when something happens with a trap
	 *
	 * @param trap:    the trap instance which triggers the notification
	 * @param trigger: the character which makes effect on the trap
	 * @param action:  0: trap casting its skill. 1: trigger detects the trap. 2: trigger removes the trap
	 * @return bolearn
	 */
	public boolean notifyTrapAction(L2Trap trap, L2Character trigger, TrapAction action)
	{
		String res = null;
		try
		{
			res = onTrapAction(trap, trigger, action);
		}
		catch(Exception e)
		{
			if(trigger.getActingPlayer() != null)
			{
				return showError(trigger.getActingPlayer(), e);
			}
			_log.log(Level.ERROR, "Exception on onTrapAction() in notifyTrapAction(): QN " + getName() + ' ' + e.getMessage(), e);
			return true;
		}
		if(trigger.getActingPlayer() != null)
		{
			return showResult(trigger.getActingPlayer(), res, false);
		}
		return false;
	}

	public boolean notifySpawn(L2Npc npc)
	{
		try
		{
			onSpawn(npc);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on onSpawn() in notifySpawn(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}

	public boolean notifyEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			if(event != null)
			{
				res = onAdvEvent(event, npc, player);
			}
		}
		catch(Exception e)
		{
			if(player != null)
			{
				_log.log(Level.ERROR, "Error notifyEvent: quest = " + getName() + " event = " + event + " npc = " + npc.getNpcId() + " player= null", e);
			}
			else
			{
				_log.log(Level.ERROR, "Error notifyEvent: quest = " + getName() + " event = " + event + " npc = " + npc.getNpcId() + " player= " + player.getName(), e);
			}

			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	public boolean notifyAskReplyEvent(L2Npc npc, L2PcInstance player, int reply)
	{
		String res = null;
		try
		{
			if(npc != null)
			{
				res = onAskEvent(npc, player, reply);
			}
		}
		catch(Exception e)
		{
			if(player == null)
			{
				_log.log(Level.ERROR, "Error notifyAskReplyEvent: quest = " + getName() + " reply = " + reply + " npc = " + npc.getNpcId() + " player= null", e);
			}
			else
			{
				_log.log(Level.ERROR, "Error notifyAskReplyEvent: quest = " + getName() + " reply = " + reply + " npc = " + npc.getNpcId() + " player= " + player.getName(), e);
			}

			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	/**
	 * Происходит при убийстве НПЦ с участием игрока
	 * @param npc инстанс убитого нпц
	 * @param killer убийца нпц
	 * @param isPet если убийца питомец игрока
	 * @return результат
	 */
	public boolean notifyKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		String res = null;
		try
		{
			if(_altMethodCall)
			{
				QuestState st = killer.getQuestState(getName());
				if(st != null)
				{
					res = onKill(npc, st);
				}
				if(res == null || res.isEmpty())
				{
					res = onKill(npc, killer, isPet);
				}
			}
			else
			{
				res = onKill(npc, killer, isPet);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyKill: killer = " + killer.getName() + "  npc = " + npc.getNpcId() + " QN= " + getName(), e);
			return showError(killer, e);
		}
		return showResult(killer, res, false);
	}

	/**
	 * Происходит при фактической смерти НПЦ
	 * @param npc инстанс нпц
	 * @param killer убийца нпц
	 * @return результат
	 */
	public boolean notifyKill(L2Npc npc, L2Character killer)
	{
		try
		{
			onNpcDie(npc, killer);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on onNpcDie() in notifyKill(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}

	public boolean notifyAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		String res = null;
		try
		{
			if(_altMethodCall)
			{
				res = onAsk(player, npc, qs, reply);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyAsk: npc = " + npc + "  player= " + qs.getPlayer().getName(), e);
			return showError(qs.getPlayer(), e);
		}

		player.setLastQuestNpcObject(npc.getObjectId());

		return showResult(player, res, false);
	}

	public boolean notifyAsk(L2Npc npc, L2PcInstance player, int ask, int reply)
	{
		String res = null;
		try
		{
			if(_altMethodCall)
			{
				res = onAsk(player, npc, ask, reply);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyAsk: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}

		player.setLastQuestNpcObject(npc.getObjectId());

		return showResult(player, res, false);
	}

	public boolean notifyTalk(L2Npc npc, QuestState qs)
	{
		String res = null;
		try
		{
			if(_altMethodCall)
			{
				res = onTalk(npc, qs);
				if(res == null || res.isEmpty())
				{
					res = onTalk(npc, qs.getPlayer());
				}
			}
			else
			{
				res = onTalk(npc, qs.getPlayer());
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyTalk: npc = " + npc + "  player= " + qs.getPlayer().getName(), e);
			return showError(qs.getPlayer(), e);
		}
		qs.getPlayer().setLastQuestNpcObject(npc.getObjectId());
		return showResult(qs.getPlayer(), res, false);
	}

	public boolean notifySocialSee(L2Npc npc, L2PcInstance player, L2Object target, int socialId)
	{
		String res = null;
		try
		{
			res = onSocialSee(npc, player, target, socialId);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error onSocialSee: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onTeleportRequest(npc, player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyTeleportRequest: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyClassChangeRequest(L2Npc npc, L2PcInstance player, int classId)
	{
		try
		{
			onClassChangeRequest(npc, player, classId);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception in onClassChangeRequest(): " + e.getMessage(), e);
			return true;
		}
		return false;
	}

	public boolean notifyPledgeLevelUp(L2Npc npc, L2PcInstance player, int currentLevel)
	{
		String res = null;
		try
		{
			res = onPledgeLevelUp(npc, player, currentLevel);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error onPledgeLevelUp: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyPledgeDismiss(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onPledgeDismiss(npc, player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyPledgeDismiss: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyPledgeRevive(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onPledgeRevive(npc, player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyPledgeRevive: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	// override the default NPC dialogs when a quest defines this for the given NPC

	public boolean notifyCreateAcademy(L2Npc npc, L2PcInstance player, int result)
	{
		String res = null;
		try
		{
			res = onCreateAcademy(npc, player, result);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyCreateAcademy: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyCreateSubPledge(L2Npc npc, L2PcInstance player, String pledgeName, String pledgeMaster, int subPledgeType)
	{
		String res = null;
		try
		{
			res = onCreateSubPledge(npc, player, pledgeName, pledgeMaster, subPledgeType);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyCreateSubPledge: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyRenameSubPledge(L2Npc npc, L2PcInstance player, String pledgeName, int subPledgeType)
	{
		String res = null;
		try
		{
			res = onRenameSubPledge(npc, player, pledgeName, subPledgeType);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyRenameSubPledge: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyUpdateSubPledgeMaster(L2Npc npc, L2PcInstance player, String masterName, int subPledgeType)
	{
		String res = null;
		try
		{
			res = onUpdateSubPledgeMaster(npc, player, masterName, subPledgeType);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyUpdateSubPledgeMaster: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyTransferPledgeMaster(L2Npc npc, L2PcInstance player, String masterName)
	{
		String res = null;
		try
		{
			res = onTransferPledgeMaster(npc, player, masterName);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyTransferPledgeMaster: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyUpgradeSubpledgeMemberCount(L2Npc npc, L2PcInstance player, int subPledgeType)
	{
		String res = null;
		try
		{
			res = onUpgradeSubpledgeMemberCount(npc, player, subPledgeType);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyUpgradeSubpledgememberCount: npc = " + npc + "  player= " + player.getName() + " pledgeType= " + subPledgeType, e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyLearnSkill(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onLearnSkill(npc, player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyLearnSkill: npc = " + npc + "  player= " + player.getName(), e);
			return showError(player, e);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		return showResult(player, res, false);
	}

	public boolean notifyFirstTalk(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onFirstTalk(npc, player);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyFirstTalk: npc = " + npc + "  player= " + player.getName() + " QN=" + getName(), e);
			return showError(player, e);
		}
		// if the quest returns text to display, display it.
		if(res != null && !res.isEmpty())
		{
			player.setLastQuestNpcObject(npc.getObjectId());
			return showResult(player, res, true);
		}
		// else tell the player that

		player.sendActionFailed();
		// note: if the default html for this npc needs to be shown, onFirstTalk should
		// call npc.showChatWindow(player) and then return null.

		return true;
	}

	public boolean notifyAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onAcquireSkillList(npc, player);
		}
		catch(Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	public boolean notifyAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkillInfo(npc, player, skill);
		}
		catch(Exception e)
		{
			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	public boolean notifyAcquireSkill(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		String res = null;
		try
		{
			res = onAcquireSkill(player, skill);
			if(res.equals("true"))
			{
				return true;
			}
			else if(res.equals("false"))
			{
				return false;
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyAcquireSkill: skill = " + skill.getId() + "  npc = " + npc.getNpcId() + "  player= " + player.getName() + " QN= " + getName(), e);
			return showError(player, e);
		}
		return showResult(player, res, false);
	}

	/**
	 * Происходит, когда НПЦ видит использование скила игроком
	 * @param npc инстанс НПЦ
	 * @param caster игрок
	 * @param skill используемый скилл
	 * @param targets цели игрока при касте скила
	 * @param isPet если кастовал не игрок, а его питомец
	 * @return результат
	 */
	public boolean notifySkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnSkillSee(npc, caster, skill, targets, isPet));
		return true;
	}

	public boolean notifyFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		String res = null;
		try
		{
			res = onFactionCall(npc, caller, attacker, isPet);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error notifyFactionCall:  npc = " + npc.getNpcId() + " Npccaller = " + caller.getNpcId() + "  attacker= " + attacker.getName() + " ScriptName: " + getName(), e);
			return showError(attacker, e);
		}
		return showResult(attacker, res, false);
	}

	/**
	 * Происходит, когда игрока входит в зону аггра НПЦ
	 * @param npc инстанс нпц
	 * @param player игрок
	 * @param isPet был ли это питомец игрока
	 * @return результат
	 */
	public boolean notifyAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		ThreadPoolManager.getInstance().executeAi(new TmpOnAggroEnter(npc, player, isPet));
		return true;
	}

	/**
	 * @param eventName - name of event
	 * @param sender - NPC, who sent event
	 * @param receiver - NPC, who received event
	 * @param reference - L2Object to pass, if needed
	 */
	public void notifyEventReceived(String[] eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		try
		{
			onEventReceived(eventName, sender, receiver, reference);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on onEventReceived() in notifyEventReceived(): " + e.getMessage(), e);
		}
	}

	/**
	 * Происходит, когда кто-либо входит в определенную зону
	 * @param character действующее лицо
	 * @param zone зона в которую осуществился вход
	 * @return резеультат
	 */
	public boolean notifyEnterZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onEnterZone(character, zone);
		}
		catch(Exception e)
		{
			if(player != null)
			{
				_log.log(Level.ERROR, "Error notifyEnterZone: character= " + character.getName() + " scriptName=" + getName(), e);
				return showError(player, e);
			}
		}
		if(player != null)
		{
			return showResult(player, res, false);
		}
		return true;
	}

	/**
	 * Происходит, когда кто-либо выходит из определенной зоны
	 * @param character действующее лицо
	 * @param zone зона, из которой осуществлялся выход
	 * @return результат
	 */
	public boolean notifyExitZone(L2Character character, L2ZoneType zone)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onExitZone(character, zone);
		}
		catch(Exception e)
		{
			if(player != null)
			{
				_log.log(Level.ERROR, "Error notifyExitZone: character= " + character.getName(), e);
				return showError(player, e);
			}
		}
		if(player != null)
		{
			return showResult(player, res, false);
		}
		return true;
	}

	/**
	 * Происходит при удачном спойле с моба игроком
	 * @param character игрок
	 * @param target моб
	 * @return результат
	 */
	public boolean notifySuccessSpoil(L2Character character, L2Attackable target)
	{
		L2PcInstance player = character.getActingPlayer();
		String res = null;
		try
		{
			res = onSuccessSpoil(target, character);
		}
		catch(Exception e)
		{
			if(player != null)
			{
				return showError(player, e);
			}
		}

		return player == null || showResult(player, res, false);
	}

	/**
	 * @param npc
	 */
	public void notifyMoveFinished(L2Npc npc)
	{
		try
		{
			onMoveFinished(npc);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on onMoveFinished() in notifyMoveFinished(): " + e.getMessage(), e);
		}
	}

	/*************************************
	 * Переопределяемые в скриптах методы
	 *************************************/

	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		return null;
	}

	public String onAttack(L2Npc npc, L2PcInstance attacker, int damage, boolean isPet, L2Skill skill)
	{
		return onAttack(npc, attacker, damage, isPet);
	}

	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if(player != null)
		{
			QuestState qs = player.getQuestState(getName());
			if(qs != null)
			{
				return onEvent(event, qs);
			}
		}
		return null;
	}

	public String onAskEvent(L2Npc npc, L2PcInstance player, int reply)
	{
		// if not overridden by a subclass, then default to the returned value of the simpler (and older) onEvent override
		// if the player has a state, use it as parameter in the next call, else return null
		QuestState qs = player.getQuestState(getName());
		if(qs != null)
		{
			return onAsk(player, npc, qs, reply);
		}

		return null;
	}

	public String onEvent(String event, QuestState qs)
	{
		return null;
	}

	public String onAsk(L2PcInstance player, L2Npc npc, QuestState qs, int reply)
	{
		return null;
	}

	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		return null;
	}

	public String onKill(L2Npc npc, QuestState qs)
	{
		return null;
	}

	public String onKill(L2Npc npc, L2PcInstance killer, boolean isPet)
	{
		return null;
	}

	public String onNpcDie(L2Npc npc, L2Character killer)
	{
		return null;
	}

	public String onTalk(L2Npc npc, L2PcInstance talker)
	{
		return null;
	}

	public String onTalk(L2Npc npc, QuestState st)
	{
		return null;
	}

	public String onSocialSee(L2Npc npc, L2PcInstance player, L2Object target, int socialId)
	{
		return null;
	}

	public String onTeleportRequest(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public void onClassChangeRequest(L2Npc npc, L2PcInstance player, int classId)
	{
	}

	public String onPledgeLevelUp(L2Npc npc, L2PcInstance player, int currentPledgeLevel)
	{
		return null;
	}

	public String onPledgeDismiss(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public String onPledgeRevive(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public String onCreateAcademy(L2Npc npc, L2PcInstance player, int result)
	{
		return null;
	}

	public String onCreateSubPledge(L2Npc npc, L2PcInstance player, String pledgeName, String pledgeMaster, int pledgeType)
	{
		return null;
	}

	public String onRenameSubPledge(L2Npc npc, L2PcInstance player, String pledgeName, int pledgeType)
	{
		return null;
	}

	public String onUpdateSubPledgeMaster(L2Npc npc, L2PcInstance player, String masterName, int pledgeType)
	{
		return null;
	}

	public String onTransferPledgeMaster(L2Npc npc, L2PcInstance player, String masterName)
	{
		return null;
	}

	public String onUpgradeSubpledgeMemberCount(L2Npc npc, L2PcInstance player, int pledgeType)
	{
		return null;
	}

	public String onLearnSkill(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public String onAcquireSkillList(L2Npc npc, L2PcInstance player)
	{
		return null;
	}

	public String onAcquireSkillInfo(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	public String onAcquireSkill(L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	public String onItemUse(L2Item item, L2PcInstance player)
	{
		return null;
	}

	public String onSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		return null;
	}

	public String onSpellFinished(L2Npc npc, L2PcInstance player, L2Skill skill)
	{
		return null;
	}

	public String onTrapAction(L2Trap trap, L2Character trigger, TrapAction action)
	{
		return null;
	}

	public String onSpawn(L2Npc npc)
	{
		return null;
	}

	public String onFactionCall(L2Npc npc, L2Npc caller, L2PcInstance attacker, boolean isPet)
	{
		return null;
	}

	public String onAggroRangeEnter(L2Npc npc, L2PcInstance player, boolean isPet)
	{
		return null;
	}

	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}

	public String onExitZone(L2Character character, L2ZoneType zone)
	{
		return null;
	}

	/**
	 * @param eventName - name of event
	 * @param sender - NPC, who sent event
	 * @param receiver - NPC, who received event
	 * @param reference - L2Object to pass, if needed
	 */
	public String onEventReceived(String[] eventName, L2Npc sender, L2Npc receiver, L2Object reference)
	{
		return null;
	}

	/**
	 * This function is called whenever a NPC finishes moving
	 * @param npc registered NPC
	 */
	public String onMoveFinished(L2Npc npc)
	{
		return null;
	}

	public String onSuccessSpoil(L2Attackable target, L2Character activeChar)
	{
		return null;
	}

	@Override
	public void onDie(L2PcInstance player, L2Character killer)
	{
	}

	@Override
	public void onLevelIncreased(L2PcInstance player)
	{

	}

	@Override
	public void onRewardSkills(L2PcInstance player)
	{

	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{

	}

	@Override
	public void onEvent(String event, L2PcInstance player)
	{

	}

	@Override
	public void onSpawn(L2PcInstance player)
	{

	}

	@Override
	public boolean onPartyLeave(L2PcInstance player)
	{
		return true;
	}

	@Override
	public boolean onAction(L2PcInstance player, L2Playable target)
	{
		return true;
	}

	@Override
	public boolean onPotionUse(L2PcInstance player, L2EtcItem item)
	{
		return true;
	}

	@Override
	public boolean onForbiddenAction(L2PcInstance player)
	{
		return true;
	}

	@Override
	public boolean onIsInEventCheck(L2PcInstance player)
	{
		return false;
	}

	@Override
	public void onEventFinished(L2PcInstance player)
	{
	}

	@Override
	public void onDisconnect(L2PcInstance player)
	{
	}

	@Override
	public boolean onIsInvulCheck(L2PcInstance player, L2Character attacker)
	{
		return false;
	}

	@Override
	public void onAttack(L2PcInstance player, L2Character attacker, boolean summonAttacked)
	{
	}

	@Override
	public void onDlgAnswer(L2PcInstance player, int messageId, int answer, int requesterId)
	{
	}

	@Override
	public void onAddSkill(L2PcInstance player, L2Skill skill)
	{
	}

	@Override
	public void onSkillRemove(L2PcInstance player, L2Skill skill)
	{
	}

	@Override
	public void onSkillUse(L2PcInstance player, L2Skill skill)
	{

	}

	@Override
	public void onEnterZone(L2PcInstance player, L2ZoneType zoneType)
	{
	}

	@Override
	public void onExitZone(L2PcInstance player, L2ZoneType zoneType)
	{
	}

	@Override
	public void onDeleteMe(L2PcInstance player)
	{
	}

	@Override
	public void onCharCreate(L2GameClient client, L2PcInstance newChar)
	{
	}

	@Override
	public void onCharDelete(int charId)
	{
	}

	@Override
	public void onFishDie(L2PcInstance player, boolean die)
	{
	}

	@Override
	public void onBotTrackerWarning(L2PcInstance player)
	{
	}

	@Override
	public void onEnchantFinish(L2PcInstance player, boolean succeed)
	{
	}

	@Override
	public void onRevive(L2PcInstance player)
	{
	}

	@Override
	public void onHpChange(L2Character player, double damage, double fullDamage)
	{
	}

	@Override
	public void onEffectStart(L2Effect e)
	{
	}

	@Override
	public void onEffectStop(L2Effect e)
	{
	}

	@Override
	public void onDayNightChange(boolean isDay)
	{
	}

	@Override
	public void onQuestFinish(L2PcInstance player, boolean isRepetable)
	{
	}

	@Override
	public void onSummonDie(L2Summon summon, L2Character killer)
	{
	}

	@Override
	public void onSummonSpawn(L2Summon summon)
	{
	}

	@Override
	public void onSummonAttacked(L2Summon summon, L2Character attacker)
	{
	}

	@Override
	public void onSummonAction(L2Summon summon, L2Character owner)
	{
	}

	@Override
	public void onInventoryAdd(L2ItemInstance.ItemLocation container, L2ItemInstance item, L2Character owner)
	{
	}

	@Override
	public void onInventoryChange(L2ItemInstance.ItemLocation container, L2ItemInstance item, long count, L2Character owner)
	{
	}

	@Override
	public void onInventoryDelete(L2ItemInstance.ItemLocation container, L2ItemInstance item, L2Character owner)
	{
	}

	@Override
	public void onItemCrafted(Integer itemId, L2PcInstance crafter)
	{
	}

	@Override
	public void onSeePlayer(L2Npc watcher, L2PcInstance player)
	{
	}

	@Override
	public boolean onItemPickup(L2PcInstance player, L2ItemInstance item)
	{
		return true;
	}

	@Override
	public void onOlympiadBattleEnd(L2PcInstance player, CompetitionType type, boolean isWinner)
	{

	}

	@Override
	public void onChaosBattleEnd(L2PcInstance player, boolean isWinner)
	{

	}

	@Override
	public void onSiegeStart(Castle castle)
	{

	}

	@Override
	public void onSiegeEnd(Castle castle)
	{

	}

	@Override
	public void onEnterInstance(L2PcInstance player, Instance instance)
	{
	}

	/**
	 * @return имя квеста
	 */
	@Override
	public String getName()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Показываем игроку текст о случившейся ошибке, если его уровень доступа больше 1
	 * @param player игрок
	 * @param t ошибка
	 * @return результат
	 */
	public boolean showError(L2PcInstance player, Throwable t)
	{
		_log.log(Level.ERROR, getName(), t);
		if(t.getMessage() == null)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Error in Script: " + getName(), t);
		}

		if(player != null && player.getAccessLevel().isGm())
		{
			String res = "<html><body><title>Ошибка скрипта.</title>" + t.getMessage() + "</body></html>";
			return showResult(player, res, false);
		}
		return false;
	}

	/**
	 * Show a message to player.<BR><BR>
	 * <U><I>Concept : </I></U><BR>
	 * 3 cases are managed according to the value of the parameter "res" :<BR>
	 * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
	 * <LI><U>"res" starts with "<html>" :</U> the message hold in "res" is shown in a dialog box</LI>
	 * <LI><U>otherwise :</U> the message held in "res" is shown in chat box</LI>
	 *
	 * @param player  : L2Player
	 * @param res : String pointing out the message to show at the player
	 * @param isFirstTalk : флаг, дающий понять, что диалог показывается с события onFirstTalk()
	 * @return boolean
	 */
	public boolean showResult(L2PcInstance player, String res, boolean isFirstTalk)
	{
		if(res == null || res.isEmpty() || player == null)
		{
			return true;
		}

		if(res.endsWith(".htm") || res.endsWith(".html"))
		{
			showHtmlFile(player, res, isFirstTalk);
		}
		else if(res.contains("<html>"))
		{
			L2Object target = player.getTarget();
			NpcHtmlMessage npcReply = new NpcHtmlMessage(target == null ? 5 : target.getObjectId());
			npcReply.setHtml(res);
			npcReply.replace("%playername%", player.getName());
			player.sendPacket(npcReply);
			player.sendActionFailed();
		}
		else
		{
			player.sendMessage(res);
		}
		return false;
	}

	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this quest after a reboot.
	 * This function is for storage of values that do not related to a specific player but are
	 * global for all characters.  For example, if we need to disable a quest-gatekeeper until
	 * a certain time (as is done with some grand-boss gatekeepers), we can save that time in the DB.
	 *
	 * @param var   : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public void saveGlobalQuestVar(String var, String value)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO quest_global_data (quest_name,var,value) VALUES (?,?,?)");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not insert global quest variable:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Read from the database a previously saved variable for this quest.
	 * Due to performance considerations, this function should best be used only when the quest is first loaded.
	 * Subclasses of this class can define structures into which these loaded values can be saved.
	 * However, on-demand usage of this function throughout the script is not prohibited, only not recommended.
	 * Values read from this function were entered by calls to "saveGlobalQuestVar"
	 *
	 * @param var : String designating the name of the variable for the quest
	 * @return String : String representing the loaded value for the passed var, or an empty string if the var was invalid
	 */
	public String loadGlobalQuestVar(String var)
	{
		String result = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Characters.SELECT_QUEST_GLOBAL_DATA_VALUE);
			statement.setString(1, getName());
			statement.setString(2, var);
			rs = statement.executeQuery();
			if(rs.first())
			{
				result = rs.getString(1);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not load global quest variable:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
		return result;
	}

	/**
	 * Permanently delete from the database a global quest variable that was previously saved for this quest.
	 *
	 * @param var : String designating the name of the variable for the quest
	 */
	public void deleteGlobalQuestVar(String var)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ? AND var = ?");
			statement.setString(1, getName());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not delete global quest variable:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Permanently delete from the database all global quest variables that was previously saved for this quest.
	 */
	public void deleteAllGlobalQuestVars()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM quest_global_data WHERE quest_name = ?");
			statement.setString(1, getName());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not delete global quest variables:", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * @param player действующий персонаж
	 * @param questType тип квеста
	 * @return DEFAULT_ALREADY_COMPLETED_DAILY_MSG, если квест ежедневный или DEFAULT_ALREADY_COMPLETED_MSG если нет.
	 */
	public String getAlreadyCompletedMsg(L2PcInstance player, QuestType questType)
	{
		return questType == QuestType.DAILY ? HtmCache.getInstance().getHtm(player.getLang(), "default/finisheddailyquest.htm") : HtmCache.getInstance().getHtm(player.getLang(), "default/finishedquest.htm");
	}

	public String getNeedCompletedQuest(int questId)
	{
		StringBuilder sb = StringUtil.startAppend(150, "<html><body>");
		StringUtil.append(sb, "Чтобы взять этот квест необходимо завершить задание ");
		StringUtil.append(sb, "<font color=\"LEVEL\">\"<fstring>", String.valueOf(questId), "01</fstring></font>");
		StringUtil.append(sb, "\".</body></html>");
		return sb.toString();
	}

	public L2NpcTemplate addEventId(int npcId, QuestEventType eventType, int askId)
	{
		try
		{
			L2NpcTemplate t = NpcTable.getInstance().getTemplate(npcId);
			if(t != null)
			{
				t.addQuestEvent(eventType, this);
			}

			if(!_questInvolvedNpcs.contains(Integer.valueOf(npcId)))
			{
				_questInvolvedNpcs.add(npcId);
			}

			if(askId > Integer.MIN_VALUE)
			{
				if(!_questInvolvedAskIds.containsKey(npcId))
				{
					_questInvolvedAskIds.put(npcId, new FastList<>());
				}

				if(!_questInvolvedAskIds.get(npcId).contains(askId))
				{
					_questInvolvedAskIds.get(npcId).add(askId);
				}

				if(getQuestsForAskId(npcId, askId) != null)
				{
					_log.log(Level.ERROR, "Ask: " + askId + " duplicated for npcId: " + npcId);
				}

				if(!_allAskEvents.containsKey(npcId))
				{
					_allAskEvents.put(npcId, new FastMap<>());
				}

				if(!_allAskEvents.get(npcId).containsKey(askId))
				{
					_allAskEvents.get(npcId).put(askId, this);
				}
			}

			return t;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on addEventId(): " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.<BR><BR>
	 *
	 * @param npcId     : id of the NPC to register
	 * @param eventType : type of event being registered
	 * @return L2NpcTemplate : Npc Template corresponding to the npcId, or null if the id is invalid
	 */
	public L2NpcTemplate addEventId(int npcId, QuestEventType eventType)
	{
		return addEventId(npcId, eventType, Integer.MIN_VALUE);
	}

	/** Adds a player event hook for specified eventType, with default (100) execute order
	 * @param eventType Desired eventType hook
	 */
	public void addEventId(HookType eventType)
	{
		try
		{
			HookManager.getInstance().addHook(eventType, this);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on addEventId(): " + e.getMessage(), e);
		}
	}

	/** Adds a player event hook for specified eventType, with specified execute order
	 * @param eventType Desired eventType hook
	 * @param order Desired execution order. Events with lower order will be executed before those with higher
	 */
	public void addEventId(HookType eventType, int order)
	{
		try
		{
			HookManager.getInstance().addHook(eventType, this, order);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on addEventId(): " + e.getMessage(), e);
		}
	}

	public void removeEventId(HookType eventType)
	{
		try
		{
			HookManager.getInstance().removeHook(eventType, this);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on removeEventId(): " + e.getMessage(), e);
		}
	}

	/**
	 * Add the quest to the NPC's startQuest
	 *
	 * @param npcIds
	 * @return L2NpcTemplate : Start NPC
	 */
	public L2NpcTemplate[] addStartNpc(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.QUEST_START);
		}
		return value;
	}

	public L2NpcTemplate addStartNpc(int npcId)
	{
		return addEventId(npcId, QuestEventType.QUEST_START);
	}

	/**
	 * Add the quest to the NPC's first-talk (default action dialog)
	 * @param npcIds
	 * @return L2NpcTemplate : Start NPC
	 */
	public L2NpcTemplate[] addFirstTalkId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_FIRST_TALK);
		}
		return value;
	}

	public L2NpcTemplate addFirstTalkId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_FIRST_TALK);
	}

	/**
	 * Add the NPC to the AcquireSkill dialog
	 * @param npcIds
	 * @return L2NpcTemplate : NPC
	 */
	public L2NpcTemplate[] addLearnSkillId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_LEARN_SKILL);
		}
		return value;
	}

	public L2NpcTemplate addLearnSkillId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_LEARN_SKILL);
	}

	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Attack Events.<BR><BR>
	 * @param attackIds
	 * @return int : attackId
	 */
	public L2NpcTemplate[] addAttackId(int... attackIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[attackIds.length];
		int i = 0;
		for(int npcId : attackIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_ATTACK);
		}
		return value;
	}

	public L2NpcTemplate addAttackId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_ATTACK);
	}

	/**
	 * Add this quest to the list of quests that the passed mob will respond to for Kill Events.<BR><BR>
	 * @param killIds
	 * @return int : killId
	 */
	public L2NpcTemplate[] addKillId(int... killIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[killIds.length];
		int i = 0;
		for(int killId : killIds)
		{
			value[i++] = addEventId(killId, QuestEventType.ON_KILL);
		}
		return value;
	}

	public L2NpcTemplate addKillId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_KILL);
	}

	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Talk Events.<BR><BR>
	 * @param talkIds : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate[] addTalkId(int... talkIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[talkIds.length];
		int i = 0;
		for(int talkId : talkIds)
		{
			value[i++] = addEventId(talkId, QuestEventType.ON_TALK);
		}
		return value;
	}

	public L2NpcTemplate addTalkId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_TALK);
	}

	public L2NpcTemplate addAskId(int npcId, int askId)
	{
		return addEventId(npcId, QuestEventType.ON_ASK, askId);
	}

	public L2NpcTemplate[] addAskId(int[] npcIds, int askId)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_ASK, askId);
		}
		return value;
	}

	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Spawn Events.<BR><BR>
	 * @param spawnIds : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate[] addSpawnId(int... spawnIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[spawnIds.length];
		int i = 0;
		for(int npcId : spawnIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_SPAWN);
		}
		return value;
	}

	public L2NpcTemplate addSpawnId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_SPAWN);
	}

	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Skill-See Events.<BR><BR>
	 * @param npcIds : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate[] addSkillSeeId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_SKILL_SEE);
		}
		return value;
	}

	public L2NpcTemplate addSkillSeeId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_SKILL_SEE);
	}

	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Social see Events.<BR>
	 * @param npcId : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate addSocialSeeId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_SOCIAL_SEE);
	}

	public L2NpcTemplate[] addSocialSeeId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_SOCIAL_SEE);
		}
		return value;
	}

	public L2NpcTemplate addTeleportRequestId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_TELEPORT_REQUEST);
	}

	public L2NpcTemplate[] addTeleportRequestId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_TELEPORT_REQUEST);
		}
		return value;
	}

	public L2NpcTemplate[] addClassChangeRequest(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_CLASS_CHANGE_REQUESTED);
		}
		return value;
	}

	public L2NpcTemplate addClassChangeRequest(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_CLASS_CHANGE_REQUESTED);
	}

	public L2NpcTemplate[] addPledgeLevelUpEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_PLEDGE_LEVEL_UP);
		}
		return value;
	}

	public L2NpcTemplate addPledgeLevelUpEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_PLEDGE_LEVEL_UP);
	}

	public L2NpcTemplate[] addPledgeDismissEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_PLEDGE_DISMISS);
		}
		return value;
	}

	public L2NpcTemplate addPledgeDismissEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_PLEDGE_DISMISS);
	}

	public L2NpcTemplate[] addPledgeReviveEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_PLEDGE_REVIVE);
		}
		return value;
	}

	public L2NpcTemplate addPledgeReviveEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_PLEDGE_REVIVE);
	}

	public L2NpcTemplate addAcademyCreateEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_ACADEMY_CREATE);
	}

	public L2NpcTemplate[] addAcademyCreateEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_ACADEMY_CREATE);
		}
		return value;
	}

	public L2NpcTemplate addSubCreatePledgeEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_CREATE_SUBPLEDGE);
	}

	public L2NpcTemplate[] addCreateSubPledgeEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_CREATE_SUBPLEDGE);
		}
		return value;
	}

	public L2NpcTemplate addUpdateSubPledgeMasterEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_UPDATE_SUBPLEDGE_MASTER);
	}

	public L2NpcTemplate[] addUpdateSubPledgeMasterEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_UPDATE_SUBPLEDGE_MASTER);
		}
		return value;
	}

	public L2NpcTemplate addTransferPledgeMasterEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_PLEDGE_MASTER_TRANSFER);
	}

	public L2NpcTemplate[] addTransferPledgeMasterEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_PLEDGE_MASTER_TRANSFER);
		}
		return value;
	}

	public L2NpcTemplate addRenameSubPledgeEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_RENAME_SUBPLEDGE);
	}

	public L2NpcTemplate[] addRenameSubPledgeEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_RENAME_SUBPLEDGE);
		}
		return value;
	}

	public L2NpcTemplate addUpgradeSubpledgeCountEvent(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_UPGRADE_SUBPLEDGE_MEMBER_COUNT);
	}

	public L2NpcTemplate[] addUpgradeSubpledgeCountEvent(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_UPGRADE_SUBPLEDGE_MEMBER_COUNT);
		}
		return value;
	}

	public L2NpcTemplate[] addSpellFinishedId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_SPELL_FINISHED);
		}
		return value;
	}

	public L2NpcTemplate addSpellFinishedId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_SPELL_FINISHED);
	}

	public L2NpcTemplate[] addTrapActionId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_TRAP_ACTION);
		}
		return value;
	}

	public L2NpcTemplate addTrapActionId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_TRAP_ACTION);
	}

	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Faction Call Events.<BR><BR>
	 * @param npcIds : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate[] addFactionCallId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_FACTION_CALL);
		}
		return value;
	}

	public L2NpcTemplate addFactionCallId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_FACTION_CALL);
	}

	/**
	 * Add this quest to the list of quests that the passed npc will respond to for Character See Events.<BR><BR>
	 * @param npcIds : ID of the NPC
	 * @return int : ID of the NPC
	 */
	public L2NpcTemplate[] addAggroRangeEnterId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
		return value;
	}

	public L2NpcTemplate addAggroRangeEnterId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_AGGRO_RANGE_ENTER);
	}

	public L2ZoneType[] addEnterZoneId(int... zoneIds)
	{
		L2ZoneType[] value = new L2ZoneType[zoneIds.length];
		int i = 0;
		for(int zoneId : zoneIds)
		{
			try
			{
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
				if(zone != null)
				{
					zone.addQuestEvent(QuestEventType.ON_ENTER_ZONE, this);
				}
				value[i++] = zone;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception on addEnterZoneId(): " + e.getMessage(), e);
			}
		}

		return value;
	}

	public L2ZoneType addEnterZoneId(int zoneId)
	{
		try
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if(zone != null)
			{
				zone.addQuestEvent(QuestEventType.ON_ENTER_ZONE, this);
			}
			return zone;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on addEnterZoneId(): " + e.getMessage(), e);
			return null;
		}
	}

	public L2ZoneType[] addExitZoneId(int... zoneIds)
	{
		L2ZoneType[] value = new L2ZoneType[zoneIds.length];
		int i = 0;
		for(int zoneId : zoneIds)
		{
			try
			{
				L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
				if(zone != null)
				{
					zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
				}
				value[i++] = zone;
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception on addEnterZoneId(): " + e.getMessage(), e);
			}
		}

		return value;
	}

	public L2ZoneType addExitZoneId(int zoneId)
	{
		try
		{
			L2ZoneType zone = ZoneManager.getInstance().getZoneById(zoneId);
			if(zone != null)
			{
				zone.addQuestEvent(QuestEventType.ON_EXIT_ZONE, this);
			}
			return zone;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception on addExitZoneId(): " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Register onEventReceived trigger for NPC
	 * @param npcId id of NPC to register
	 * @return
	 */
	public L2NpcTemplate addEventReceivedId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_EVENT_RECEIVED);
	}

	/**
	 * Register onEventReceived trigger for NPC
	 * @param npcIds
	 * @return
	 */
	public L2NpcTemplate[] addEventReceivedId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_EVENT_RECEIVED);
		}
		return value;
	}

	/**
	 * Register onMoveFinished trigger for NPC
	 * @param npcId id of NPC to register
	 * @return
	 */
	public L2NpcTemplate addMoveFinishedId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_MOVE_FINISHED);
	}

	/**
	 * Register onMoveFinished trigger for NPC
	 * @param npcIds
	 * @return
	 */
	public L2NpcTemplate[] addMoveFinishedId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_MOVE_FINISHED);
		}
		return value;
	}

	public L2NpcTemplate addSpoilId(int npcId)
	{
		return addEventId(npcId, QuestEventType.ON_SUCCESS_SPOIL);
	}

	public L2NpcTemplate[] addSpoilId(int... npcIds)
	{
		L2NpcTemplate[] value = new L2NpcTemplate[npcIds.length];
		int i = 0;
		for(int npcId : npcIds)
		{
			value[i++] = addEventId(npcId, QuestEventType.ON_SUCCESS_SPOIL);
		}
		return value;
	}

	/***
	 * @param player
	 * @return a random party member's L2PcInstance for the passed player's party or the passed player if he has no party.
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player)
	{
		if(player == null)
		{
			return null;
		}
		return player.getParty() == null || player.getParty().getMembers().isEmpty() ? player : player.getParty().getRandomPartyMember();
	}

	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 *
	 * @param player: the instance of a player whose party is to be searched
	 * @param value:  the value of the "cond" variable that must be matched
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified
	 *         condition, or null if no match.
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String value)
	{
		return getRandomPartyMember(player, "cond", value);
	}

	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 * @param player the instance of a player whose party is to be searched
	 * @param var
	 * @param value a tuple specifying a quest condition that must be satisfied for a party member to be considered.
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified
	 *         condition, or null if no match.  If the var is null, any random party
	 *         member is returned (i.e. no condition is applied).
	 *         The party member must be within 1500 distance from the target of the reference
	 *         player, or if no target exists, 1500 distance from the player itself.
	 */
	public L2PcInstance getRandomPartyMember(L2PcInstance player, String var, String value)
	{
		// if no valid player instance is passed, there is nothing to check...
		if(player == null)
		{
			return null;
		}

		// for null var condition, return any random party member.
		if(var == null)
		{
			return getRandomPartyMember(player);
		}

		// normal cases...if the player is not in a party, check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if(party == null || party.getMembers().isEmpty())
		{
			temp = player.getQuestState(getName());
			if(temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value))
			{
				return player; // match
			}

			return null; // no match
		}

		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		List<L2PcInstance> candidates = new ArrayList<>();

		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if(target == null)
		{
			target = player;
		}

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember == null)
			{
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if(temp != null && temp.get(var) != null && temp.get(var).equalsIgnoreCase(value) && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		// if there was no match, return null...
		if(candidates.isEmpty())
		{
			return null;
		}
		// TODO where's the range check?
		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}

	/**
	 * Auxilary function for party quests.
	 * Note: This function is only here because of how commonly it may be used by quest developers.
	 * For any variations on this function, the quest script can always handle things on its own
	 *
	 * @param player: the instance of a player whose party is to be searched
	 * @param state:  the state in which the party member's queststate must be in order to be considered.
	 * @return L2PcInstance: L2PcInstance for a random party member that matches the specified
	 *         condition, or null if no match.  If the var is null, any random party
	 *         member is returned (i.e. no condition is applied).
	 */
	public L2PcInstance getRandomPartyMemberState(L2PcInstance player, QuestStateType state)
	{
		// if no valid player instance is passed, there is nothing to check...
		if(player == null)
		{
			return null;
		}

		// normal cases...if the player is not in a partym check the player's state
		QuestState temp = null;
		L2Party party = player.getParty();
		// if this player is not in a party, just check if this player instance matches the conditions itself
		if(party == null || party.getMembers().isEmpty())
		{
			temp = player.getQuestState(getName());
			if(temp != null && temp.getState() == state)
			{
				return player; // match
			}

			return null; // no match
		}

		// if the player is in a party, gather a list of all matching party members (possibly
		// including this player)
		List<L2PcInstance> candidates = new ArrayList<>();

		// get the target for enforcing distance limitations.
		L2Object target = player.getTarget();
		if(target == null)
		{
			target = player;
		}

		for(L2PcInstance partyMember : party.getMembers())
		{
			if(partyMember == null)
			{
				continue;
			}
			temp = partyMember.getQuestState(getName());
			if(temp != null && temp.getState() == state && partyMember.isInsideRadius(target, 1500, true, false))
			{
				candidates.add(partyMember);
			}
		}
		// if there was no match, return null...
		if(candidates.isEmpty())
		{
			return null;
		}

		// if a match was found from the party, return one of them at random.
		return candidates.get(Rnd.get(candidates.size()));
	}

	/**
	 * @param player инстанс текущего персонажа
	 * @param fileName имя файла диалога
	 * @param isFirstTalk @param isFirstTalk : флаг, дающий понять, что диалог показывается с события onFirstTalk() (questWindow всегда false)
	 * @return содержание диалога клиенту
	 */
	public String showHtmlFile(L2PcInstance player, String fileName, boolean isFirstTalk)
	{
		boolean isQuestWindow = true;
		if(isFirstTalk)
		{
			isQuestWindow = false;
		}
		int questId = getQuestId();

		// Create handler to file linked to the quest
		String content = getHtm(player.getLang(), fileName);

		L2Object target = player.getTarget();
		if(target != null)
		{
			content = content.replaceAll("%objectId%", String.valueOf(player.getTarget().getObjectId()));
		}

		//Send message to client if message not empty
		if(content != null)
		{
			if(isQuestWindow && questId > 0 && questId < 20000 && questId != 999)
			{
				ExNpcQuestHtmlMessage npcReply = new ExNpcQuestHtmlMessage(target == null ? 5 : target.getObjectId(), questId);
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				npcReply.replace("<\\?name\\?>", player.getName());
				npcReply.replace("<\\?quest_id\\?>", String.valueOf(questId));
				player.sendPacket(npcReply);
			}
			else
			{
				NpcHtmlMessage npcReply = new NpcHtmlMessage(target == null ? 5 : target.getObjectId());
				npcReply.setHtml(content);
				npcReply.replace("%playername%", player.getName());
				npcReply.replace("<\\?name\\?>", player.getName());
				npcReply.replace("<\\?quest_id\\?>", String.valueOf(questId));
				player.sendPacket(npcReply);
			}
			player.sendActionFailed();
		}

		return content;
	}

	/**
	 * TODO: Должен использоваться ТОЛЬКО квестами (не аи с -1)
	 * @param prefix player's language prefix.
	 * @param fileName the html file to be get.
	 * @return HTML file contents
	 */
	public String getHtm(String prefix, String fileName)
	{
		HtmCache hc = HtmCache.getInstance();
		String content = hc.getHtmQuest(prefix, "quests/" + getQuestId() + '/' + fileName);
		if(content == null)
		{
			content = hc.getHtm(prefix, "default/" + fileName);

			if(content == null)
			{
				return hc.getNoFoundHtml(getName() + '/' + fileName);
			}
			/*else
			{
				if (getQuestId() == -1)
				{
					_log.log(Level.WARN, "Custom AI-Script "+getName()+" using deprecated htm-getting method for it! Please, rework to OFF-System (getting from default folder)!");
				}
				else
				{
					_log.log(Level.WARN, "Script "+getName()+" using deprecated htm-getting method for it! Please, rework to OFF-System (getting from default folder)!");
				}
			} */
		}

		return content;
	}

	/**
	 * Add a temporary (quest) spawn
	 * @param npcId
	 * @param cha
	 * @return instance of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, false);
	}

	public L2Npc addSpawn(int npcId, Location loc)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0, false);
	}

	public L2Npc addSpawn(int npcId, Location loc, boolean randomOffset)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, randomOffset, 0, false);
	}

	public L2Npc addSpawn(int npcId, Location loc, int instanceId)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0, false, instanceId);
	}
    
    public L2Npc addSpawn(int npcId, Location loc, boolean isSummonSpawn, int instanceId)
    {
        return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, false, 0, isSummonSpawn, instanceId);
    }

    public L2Npc addSpawn(int x, int y, int z, boolean isSummonSpawn, int instanceId, int... npcIds)
    {
        return addSpawn(Rnd.get(npcIds), x, y, z, 0, false, 0, isSummonSpawn, instanceId);
    }

    public L2Npc addSpawn(int npcId, Location loc, boolean randomOffset, boolean isSummonSpawn, int instanceId)
    {
        return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, randomOffset, 0, isSummonSpawn, instanceId);
    }
    
	/**
	 * Add a temporary (quest) spawn
	 * @param npcId
	 * @param cha
	 * @param isSummonSpawn
	 * @return instance of newly spawned npc with summon animation
	 */
	public L2Npc addSpawn(int npcId, L2Character cha, boolean isSummonSpawn)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, 0, isSummonSpawn);
	}

	public L2Npc addSpawn(int npcId, L2Character cha, boolean isSummonSpawn, long despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), false, despawnDelay, isSummonSpawn);
	}

	public L2Npc addSpawn(int npcId, Location loc, int heading, boolean randomOffSet, long despawnDelay)
	{
		return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), heading, randomOffSet, despawnDelay, false);
	}

    public L2Npc addSpawn(int npcId, Location loc, boolean randomOffSet, long despawnDelay, boolean isSummonSpawn)
    {
        return addSpawn(npcId, loc.getX(), loc.getY(), loc.getZ(), 0, randomOffSet, despawnDelay, isSummonSpawn);
    }

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffSet, long despawnDelay)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffSet, despawnDelay, false);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn)
	{
		return addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn, 0);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, long despawnDelay, boolean isSummonSpawn, int instanceId)
	{
		L2Npc result = null;
		try
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if(template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code
				// reaches here, xyz have become 0!  Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc!  This will NOT work
				// with quest spawns!  For both of the above cases, we need a fail-safe spawn.  For this, we use the
				// default spawn location, which is at the player's loc.
				if(x == 0 && y == 0)
				{
					_log.log(Level.ERROR, "Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				if(randomOffset)
				{
					int offset;

					offset = Rnd.get(2); // Get the direction of the offset
					if(offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;

					offset = Rnd.get(2); // Get the direction of the offset
					if(offset == 0)
					{
						offset = -1;
					} // make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				L2Spawn spawn = new L2Spawn(template);
				spawn.setInstanceId(instanceId);
				spawn.setHeading(heading);
				spawn.setLocx(x);
				spawn.setLocy(y);
				spawn.setLocz(z);
				spawn.stopRespawn();
				result = spawn.spawnOne(isSummonSpawn);

				if(despawnDelay > 0)
				{
					result.scheduleDespawn(despawnDelay);
				}

				return result;
			}
		}
		catch(Exception e1)
		{
			_log.log(Level.ERROR, "Could not spawn Npc " + npcId, e1);
		}

		return null;
	}

	public L2Trap addTrap(int trapId, int x, int y, int z, int heading, L2Skill skill, int instanceId)
	{
		L2NpcTemplate TrapTemplate = NpcTable.getInstance().getTemplate(trapId);
		L2Trap trap = new L2TrapInstance(IdFactory.getInstance().getNextId(), TrapTemplate, instanceId, -1, skill);
		trap.setCurrentHp(trap.getMaxHp());
		trap.setCurrentMp(trap.getMaxMp());
		trap.setIsInvul(true);
		trap.setHeading(heading);
		//L2World.getInstance().storeObject(trap);
		trap.getLocationController().spawn(x, y, z);

		return trap;
	}

	public L2Npc addMinion(L2MonsterInstance master, int minionId)
	{
		return MinionList.spawnMinion(master, minionId);
	}

	public int[] getRegisteredItemIds()
	{
		return questItemIds;
	}

	public boolean unload()
	{
		return unload(true);
	}

	public boolean unload(boolean removeFromList)
	{
		saveGlobalData();
		// cancel all pending timers before reloading.
		// if timers ought to be restarted, the quest can take care of it
		// with its code (example: save global data indicating what timer must
		// be restarted).
		for(List<QuestTimer> timers : _allEventTimers.values())
		{
			_readLock.lock();
			try
			{
				for(QuestTimer timer : timers)
				{
					timer.cancel();
				}
			}
			finally
			{
				_readLock.unlock();
			}
			timers.clear();
		}
		_allEventTimers.clear();

		for(Integer npcId : _questInvolvedNpcs)
		{
			L2NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if(template != null)
			{
				template.removeQuest(this);
			}
		}
		_questInvolvedNpcs.clear();
		_questInvolvedAskIds.clear();

		if(removeFromList)
		{
			return QuestManager.getInstance().removeQuest(this);
		}
		return true;
	}

	public void setAltMethodCall(boolean altMethodCall)
	{
		_altMethodCall = altMethodCall;
	}

	/**
	 * @param player L2PcInstance игрока
	 * @return {@code true}, если квест можно взять, если нет то {@code false}
	 */
	public boolean canBeStarted(L2PcInstance player)
	{
		return true;
	}

	/**
	 * Происходит, когда игрок пытается начать квест из хендлера StartQuestItem
	 * @param player инстанс игрока
	 * @return html диалог при клике на предмет
	 */
	public String onStartFromItem(L2PcInstance player)
	{
		return null;
	}

	public boolean notifyStartFromItem(L2PcInstance player)
	{
		String res = null;
		try
		{
			res = onStartFromItem(player);
		}
		catch(Exception e)
		{
			showError(player, e);
		}
		return showResult(player, res, false);
	}

	public void setMinMaxLevel(int min, int max)
	{
		minLevel = min;
		maxLevel = max;
	}

	/**
	 * Служит для отправки статуса количества убитых мобов по NpcLogList
	 * @param player текущий игрок
	 */
	public void sendNpcLogList(L2PcInstance player)
	{
	}

	/**
	 * @param level уровень проверяемого игрока
	 * @return {@code true} если игрок соответствует условиям квеста по уровню
	 */
	public boolean isLevelSatisfy(int level)
	{
		return level <= maxLevel && level >= minLevel;
	}

	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player : the player whom to send the packet
	 * @param sound : the name of the sound to play
	 */
	public void playSound(L2PcInstance player, String sound)
	{
		player.sendPacket(QuestSound.getSound(sound));
	}

	/**
	 * Send a packet in order to play a sound to the player.
	 * @param player : the player whom to send the packet
	 * @param sound : the {@link QuestSound} object of the sound to play
	 */
	public void playSound(L2PcInstance player, QuestSound sound)
	{
		player.sendPacket(sound.getPacket());
	}

	/**
	 * This is used to register all monsters contained in mobs for a particular script
	 * event types defined in types.
	 * @param mobs
	 * @param types
	 */
	public void registerMobs(int[] mobs, QuestEventType... types)
	{
		for(int id : mobs)
		{
			for(QuestEventType type : types)
			{
				addEventId(id, type);
			}
		}
	}

	public void registerMobs(int[] mobs)
	{
		for(int id : mobs)
		{
			addEventId(id, QuestEventType.ON_ATTACK);
			addEventId(id, QuestEventType.ON_KILL);
			addEventId(id, QuestEventType.ON_SPAWN);
			addEventId(id, QuestEventType.ON_SPELL_FINISHED);
			addEventId(id, QuestEventType.ON_SKILL_SEE);
			addEventId(id, QuestEventType.ON_FACTION_CALL);
			addEventId(id, QuestEventType.ON_AGGRO_RANGE_ENTER);
		}
	}

	/**
	 * Scripts are loaded after first npc spawn so we must reRun it.
	 * @param npcIds ids of npcs registered onSpawn
	 */
	protected void onSpawnRerun(int... npcIds)
	{
		for(int npcId : npcIds)
		{
			Set<L2Spawn> spawns = SpawnTable.getInstance().getSpawns(npcId);
			for(L2Spawn spawn : spawns)
			{
				L2Npc lastSpawn = spawn.getLastSpawn();
				if(lastSpawn != null)
				{
					onSpawn(lastSpawn);
				}
			}
		}
	}

	/**
	 * Выполняет процедуру с заданными параметрами
	 * @param player игрок, который вызвал процедуру
	 * @param npc нпц, который участвует в процедуре
	 * @param isPet {@code true} если ивент был вызван призванным существом игрока
	 * @param includeParty если {@code true} то #actionForEachPlayer(L2PcInstance, L2Npc, boolean) будет применена ко всем игрокам в группе
	 * @param includeCommandChannel если {@code true} то {@link #actionForEachPlayer(L2PcInstance, L2Npc, boolean)} будет применена ко всем игрокам в канале
	 * @see #actionForEachPlayer(L2PcInstance, L2Npc, boolean)
	 */
	public void executeForEachPlayer(L2PcInstance player, L2Npc npc, boolean isPet, boolean includeParty, boolean includeCommandChannel)
	{
		if((includeParty || includeCommandChannel) && player.isInParty())
		{
			if(includeCommandChannel && player.getParty().isInCommandChannel())
			{
				player.getParty().getCommandChannel().forEachMember(member -> {
					actionForEachPlayer(member, npc, isPet);
					return true;
				});
			}
			else if(includeParty)
			{
				player.getParty().forEachMember(member -> {
					actionForEachPlayer(member, npc, isPet);
					return true;
				});
			}
		}
		else
		{
			actionForEachPlayer(player, npc, isPet);
		}
	}

	/**
	 * Переопределяемый в скриптах метод, который вызывается {@link #executeForEachPlayer(L2PcInstance, L2Npc, boolean, boolean, boolean)}
	 * @param player игрок, к которому будет применено действие
	 * @param npc нпц, который участвует в действии
	 * @param isPet isPet {@code true} если ивент был вызван призванным существом игрока
	 */
	public void actionForEachPlayer(L2PcInstance player, L2Npc npc, boolean isPet)
	{
	}

	public static enum TrapAction
	{
		TRAP_TRIGGERED,
		TRAP_DETECTED,
		TRAP_DISARMED
	}

	public static enum QuestEventType
	{
		ON_FIRST_TALK(false), // control the first dialog shown by NPCs when they are clicked (some quests must override the default npc action)
		QUEST_START(true), // onTalk action from start npcs
		ON_TALK(true), // onTalk action from npcs participating in a quest
		ON_ATTACK(true), // onAttack action triggered when a mob gets attacked by someone
		ON_KILL(true), // onKill action triggered when a mob gets killed.
		ON_SPAWN(true), // onSpawn action triggered when an NPC is spawned or respawned.
		ON_SKILL_SEE(true), // NPC or Mob saw a person casting a skill (regardless what the target is).
		ON_FACTION_CALL(true), // NPC or Mob saw a person casting a skill (regardless what the target is).
		ON_AGGRO_RANGE_ENTER(true), // a person came within the Npc/Mob's range
		ON_SPELL_FINISHED(true), // on spell finished action when npc finish casting skill
		ON_LEARN_SKILL(true), // при запросе на вывод окна изучаемых умений у НПЦ
		ON_ENTER_ZONE(true), // при входе в зону
		ON_EXIT_ZONE(true), // при выходе из зоны
		ON_TRAP_ACTION(true), // при совершении действий с ловушками
		ON_ITEM_USE(true), // при использовании предмета
		ON_SOCIAL_SEE(true), // при использовании социалки
		ON_SUCCESS_SPOIL(true), // при удачном спойле с моба
		ON_ASK(true),
		ON_TELEPORT_REQUEST(true), // при запросе на телепортацию
		ON_CLASS_CHANGE_REQUESTED(true), // при запросе смены класса персонажа
		ON_CREATE_PLEDGE(true), // при создании клана
		ON_CREATE_ALLIANCE(true), // при создании альянса
		ON_PLEDGE_DISMISS(true), // при запросе на роспуск клана
		ON_PLEDGE_REVIVE(true), // при запросе на отмену роспуска клана
		ON_ACADEMY_CREATE(true), // при запросе на создание академии
		ON_CREATE_SUBPLEDGE(true), // при запросе на создание гвардии
		ON_UPDATE_SUBPLEDGE_MASTER(true), // при смене начальника гвардии
		ON_PLEDGE_MASTER_TRANSFER(true), // при трансфере клан лидер
		ON_PLEDGE_MASTER_TRANSFER_CANCEL(true), // при отмене трансфера клан лидера
		ON_RENAME_SUBPLEDGE(true), // при переименовании гвардии
		ON_UPGRADE_SUBPLEDGE_MEMBER_COUNT(true), // при расширении гвардии
		ON_PLEDGE_LEVEL_UP(true), // при попытке поднять уровень клана
		ON_EVENT_RECEIVED(true), // onEventReceived action, triggered when NPC recieving an event, sent by other NPC
		ON_MOVE_FINISHED(true); // onMoveFinished action, triggered when NPC stops after moving

		// control whether this event type is allowed for the same npc template in multiple quests
		// or if the npc must be registered in at most one quest for the specified event
		private final boolean _allowMultipleRegistration;

		QuestEventType(boolean allowMultipleRegistration)
		{
			_allowMultipleRegistration = allowMultipleRegistration;
		}

		public boolean isMultipleRegistrationAllowed()
		{
			return _allowMultipleRegistration;
		}
	}

	protected static class Drop
	{
		public int condition;
		public int maxcount;
		public int chance;

		public List<Short> itemList = new ArrayList<>();

		public Drop(Integer _condition, Integer _maxcount, Integer _chance)
		{
			condition = _condition;
			maxcount = _maxcount;
			chance = _chance;
		}

		public Drop addItem(Short item)
		{
			itemList.add(item);
			return this;
		}
	}

	public class TmpOnSkillSee implements Runnable
	{
		private final L2Npc _npc;
		private final L2PcInstance _caster;
		private final L2Skill _skill;
		private final L2Object[] _targets;
		private final boolean _isPet;

		public TmpOnSkillSee(L2Npc npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
		{
			_npc = npc;
			_caster = caster;
			_skill = skill;
			_targets = targets;
			_isPet = isPet;
		}

		@Override
		public void run()
		{
			String res = null;
			try
			{
				res = onSkillSee(_npc, _caster, _skill, _targets, _isPet);
			}
			catch(Exception e)
			{
				showError(_caster, e);
			}
			showResult(_caster, res, false);
		}
	}

	public class TmpOnAggroEnter implements Runnable
	{
		private final L2Npc _npc;
		private final L2PcInstance _pc;
		private final boolean _isPet;

		public TmpOnAggroEnter(L2Npc npc, L2PcInstance pc, boolean isPet)
		{
			_npc = npc;
			_pc = pc;
			_isPet = isPet;
		}

		@Override
		public void run()
		{
			String res = null;
			try
			{
				res = onAggroRangeEnter(_npc, _pc, _isPet);
			}
			catch(Exception e)
			{
				showError(_pc, e);
			}
			showResult(_pc, res, false);
		}
	}
}