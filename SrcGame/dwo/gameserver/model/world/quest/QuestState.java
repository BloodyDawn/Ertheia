/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.world.quest;

import dwo.config.Config;
import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.GameTimeController;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.player.PlayerPunishLevel;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.QuestList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.TutorialCloseHtml;
import dwo.gameserver.network.game.serverpackets.TutorialEnableClientEvent;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;
import dwo.gameserver.network.game.serverpackets.TutorialShowQuestionMark;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowQuestMark;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Luis Arias
 */
public class QuestState
{
	protected static final Logger _log = LogManager.getLogger(QuestState.class);
	/**
	 * Player who engaged the quest
	 */
	protected final L2PcInstance _player;
	/**
	 * Quest associated to the QuestState
	 */
	private final String _questName;
	/**
	 * State of the quest
	 */
	protected QuestStateType _state;

	/** List of couples (variable for quest,value of the variable for quest) */
	private Map<String, String> _vars;
	private Map<String, String> _globalQuestVar;

	/** List of spawns for quest */
	private List<L2NpcInstance> _questSpawns;

	/** boolean flag letting QuestStateManager know to exit quest when cleaning up */
	private boolean _isExitQuestOnCleanUp;

	/**
	 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR/><BR/>
	 *
	 * <U><I>Actions :</U></I><BR/>
	 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
	 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
	 * <LI>Add drops gotten by the quest</LI>
	 * <BR/>
	 * @param quest : quest associated with the QuestState
	 * @param player : L2PcInstance pointing out the player
	 * @param state : state of the quest
	 */
	protected QuestState(Quest quest, L2PcInstance player, QuestStateType state)
	{
		_questName = quest.getName();
		_player = player;

		// Save the state of the quest for the player in the player's list of quest onwed
		_player.setQuestState(this);

		// set the state of the quest
		_state = state;
	}

	public String getQuestName()
	{
		return _questName;
	}

	/**
	 * @return Quest
	 */
	public Quest getQuest()
	{
		return QuestManager.getInstance().getQuest(_questName);
	}

	/**
	 * @return L2PcInstance
	 */
	public L2PcInstance getPlayer()
	{
		return _player;
	}

	/**
	 * @return the state of the quest
	 */
	public QuestStateType getState()
	{
		return _state;
	}

	public int getCond()
	{
		return getInt("cond");
	}

	public void setMemoStateEx(int index, int memoState)
	{
		set("memoStateEx" + index, String.valueOf(memoState));
	}

	public int getMemoState()
	{
		return getInt("memoState");
	}

	public void setMemoState(int memoState)
	{
		set("memoState", String.valueOf(memoState));
	}

	public void removeMemoState()
	{
		unset("memoState");
	}

	public int getMemoStateEx(int index)
	{
		return getInt("memoStateEx" + index);
	}

	public boolean getBool(String var)
	{
		return _vars != null && _vars.containsKey(var) && _vars.get(var) != null && (_vars.get(var).equals("1") || _vars.get(var).equalsIgnoreCase("true"));
	}

	/**
	 * @return {@code true} if quest just created, {@code false} otherwise
	 */
	public boolean isCreated()
	{
		return _state == QuestStateType.CREATED;
	}

	/**
	 * @return {@code true} if quest completed, {@code false} otherwise
	 */
	public boolean isCompleted()
	{
		return _state == QuestStateType.COMPLETED;
	}

	/**
	 * Return true if quest started, false otherwise
	 *
	 * @return boolean
	 */
	public boolean isStarted()
	{
		return _state == QuestStateType.STARTED;
	}

	/**
	 * Return state of the quest after its initialization.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Remove drops from previous state</LI>
	 * <LI>Set new state of the quest</LI>
	 * <LI>Add drop for new state</LI>
	 * <LI>Update information in database</LI>
	 * <LI>Send packet QuestList to client</LI>
	 * @param state
	 * @return object
	 */
	public Object setState(QuestStateType state)
	{
		// set new state if it is not already in that state
		if(_state != state)
		{
			boolean newQuest = isCreated();
			_state = state;

			if(newQuest)
			{
				Quest.createQuestInDb(this);
			}
			else
			{
				Quest.updateQuestInDb(this);
			}

			_player.sendPacket(new QuestList());
		}
		return state;
	}

	/**
	 * Служит для быстрого старта квеста
	 * без лишней балаботины в скриптах.
	 *
	 * Выставляет:
	 * State = STARTED
	 * Cond = 1
	 * Проигрывает SOUND_QUEST_START
	 */
	public Object startQuest()
	{
		setState(QuestStateType.STARTED);
		set("cond", "1");
		_player.sendPacket(new PlaySound("ItemSound.quest_accept"));
		return _state;
	}

	/**
	 * Add parameter used in quests.
	 *
	 * @param var : String pointing out the name of the variable for quest
	 * @param val : String pointing out the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String setInternal(String var, String val)
	{
		if(_vars == null)
		{
			_vars = new HashMap<>();
		}

		if(val == null)
		{
			val = "";
		}

		_vars.put(var, val);
		return val;
	}

	/**
	 * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Initialize class variable "vars" if is null</LI>
	 * <LI>Initialize parameter "val" if is null</LI>
	 * <LI>Add/Update couple (var,val) in class variable HashMap "vars"</LI>
	 * <LI>If the key represented by "var" exists in HashMap "vars", the couple (var,val) is updated in the database. The key is known as
	 * existing if the preceding value of the key (given as result of function put()) is not null.<BR>
	 * If the key doesn't exist, the couple is added/created in the database</LI>
	 *
	 * @param var : String indicating the name of the variable for quest
	 * @param val : String indicating the value of the variable for quest
	 * @return String (equal to parameter "val")
	 */
	public String set(String var, String val)
	{
		if(_vars == null)
		{
			_vars = new HashMap<>();
		}

		if(val == null)
		{
			val = "";
		}

		// HashMap.put() returns previous value associated with specified key, or null if there was no mapping for key.
		String old = _vars.put(var, val);

		if(old != null)
		{
			Quest.updateQuestVarInDb(this, var, val);
		}
		else
		{
			Quest.createQuestVarInDb(this, var, val);
		}

		if(var.equals("cond"))
		{
			try
			{
				int previousVal = 0;
				try
				{
					previousVal = Integer.parseInt(old);
				}
				catch(Exception ex)
				{
					previousVal = 0;
				}
				setCond(Integer.parseInt(val), previousVal);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, _player.getName() + ", " + _questName + " cond [" + val + "] is not an integer.  Value stored, but no packet was sent: " + e.getMessage(), e);
			}
		}
		return val;
	}

	/**
	 * Internally handles the progression of the quest so that it is ready for sending
	 * appropriate packets to the client<BR><BR>
	 * <U><I>Actions :</I></U><BR>
	 * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI>
	 * <LI>If not, check if quest progress steps have been skipped</LI>
	 * <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI>
	 * <LI>If no steps were skipped, flags do not need to be prepared...</LI>
	 * <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not
	 * considered, while skipped steps before the parameter, if any, maintain their info</LI>
	 * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
	 * @param old : int indicating the previously noted step
	 *
	 * For more info on the variable communicating the progress steps to the client, please see
	 */
	private void setCond(int cond, int old)
	{
		int completedStateFlags = 0; // initializing...

		// if there is no change since last setting, there is nothing to do here
		if(cond == old)
		{
			return;
		}

		// cond 0 and 1 do not need completedStateFlags.  Also, if cond > 1, the 1st step must
		// always exist (i.e. it can never be skipped).  So if cond is 2, we can still safely
		// assume no steps have been skipped.
		// Finally, more than 31 steps CANNOT be supported in any way with skipping.
		if(cond < 3 || cond > 31)
		{
			unset("__compltdStateFlags");
		}
		else
		{
			completedStateFlags = getInt("__compltdStateFlags");
		}

		// case 1: No steps have been skipped so far...
		if(completedStateFlags == 0)
		{
			// check if this step also doesn't skip anything.  If so, no further work is needed
			// also, in this case, no work is needed if the state is being reset to a smaller value
			// in those cases, skip forward to informing the client about the change...

			// ELSE, if we just now skipped for the first time...prepare the flags!!!
			if(cond > old + 1)
			{
				// set the most significant bit to 1 (indicates that there exist skipped states)
				// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
				// what the cond says)
				completedStateFlags = 0x80000001;

				// since no flag had been skipped until now, the least significant bits must all
				// be set to 1, up until "old" number of bits.
				completedStateFlags |= (1 << old) - 1;

				// now, just set the bit corresponding to the passed cond to 1 (current step)
				completedStateFlags |= 1 << cond - 1;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}
		// case 2: There were exist previously skipped steps
		else
		{
			// if this is a push back to a previous step, clear all completion flags ahead
			if(cond < old)
			{
				completedStateFlags &= (1 << cond) - 1; // note, this also unsets the flag indicating that there exist skips

				//now, check if this resulted in no steps being skipped any more
				if(completedStateFlags == (1 << cond) - 1)
				{
					unset("__compltdStateFlags");
				}
				else
				{
					// set the most significant bit back to 1 again, to correctly indicate that this skips states.
					// also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
					// what the cond says)
					completedStateFlags |= 0x80000001;
					set("__compltdStateFlags", String.valueOf(completedStateFlags));
				}
			}
			// if this moves forward, it changes nothing on previously skipped steps...so just mark this
			// state and we are done
			else
			{
				completedStateFlags |= 1 << cond - 1;
				set("__compltdStateFlags", String.valueOf(completedStateFlags));
			}
		}

		// send a packet to the client to inform it of the quest progress (step change)
		QuestList ql = new QuestList();
		_player.sendPacket(ql);

		Quest q = getQuest();
		if(cond > 0)
		{
			_player.sendPacket(new ExShowQuestMark(q.getQuestId(), cond));
		}
	}

	public String setCond(int val)
	{
		return set("cond", String.valueOf(val));
	}

	/**
	 * Remove the variable of quest from the list of variables for the quest.<BR><BR>
	 * <U><I>Concept : </I></U>
	 * Remove the variable of quest represented by "var" from the class variable HashMap "vars" and from the database.
	 *
	 * @param var : String designating the variable for the quest to be deleted
	 * @return String pointing out the previous value associated with the variable "var"
	 */
	public String unset(String var)
	{
		if(_vars == null)
		{
			return null;
		}

		String old = _vars.remove(var);

		if(old != null)
		{
			Quest.deleteQuestVarInDb(this, var);
		}
		return old;
	}

	/**
	 * Insert (or Update) in the database variables that need to stay persistant for this player after a reboot.
	 * This function is for storage of values that do not related to a specific quest but are
	 * global for all quests.  For example, player's can get only once the adena and XP reward for
	 * the first class quests, but they can make more than one first class quest.
	 *
	 * @param var   : String designating the name of the variable for the quest
	 * @param value : String designating the value of the variable for the quest
	 */
	public void saveGlobalQuestVar(String var, String value)
	{
		if(_globalQuestVar == null)
		{
			_globalQuestVar = new FastMap<>();
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO character_quest_global_data (charId,var,value) VALUES (?,?,?)");
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.setString(3, value);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not insert player's global quest variable: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_globalQuestVar.put(var, value);
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
	public String getGlobalQuestVar(String var)
	{
		if(_globalQuestVar == null)
		{
			_globalQuestVar = new FastMap<>();
		}
		if(_globalQuestVar.containsKey(var))
		{
			return _globalQuestVar.get(var);
		}

		String result = "";
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT value FROM character_quest_global_data WHERE charId = ? AND var = ?");
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			rs = statement.executeQuery();
			if(rs.first())
			{
				result = rs.getString(1);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not load player's global quest variable: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
			_globalQuestVar.put(var, result);
		}
		return result;
	}

	/**
	 * Permanently delete from the database one of the player's global quest variable that was previously saved.
	 *
	 * @param var : String designating the name of the variable
	 */
	public void deleteGlobalQuestVar(String var)
	{
		if(_globalQuestVar == null)
		{
			_globalQuestVar = new FastMap<>();
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId = ? AND var = ?");
			statement.setInt(1, _player.getObjectId());
			statement.setString(2, var);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "could not delete player's global quest variable: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_globalQuestVar.remove(var);
		}
	}

	/**
	 * Return the value of the variable of quest represented by "var"
	 *
	 * @param var : name of the variable of quest
	 * @return String
	 */
	public String get(String var)
	{
		if(_vars == null)
		{
			return null;
		}

		return _vars.get(var);
	}

	/**
	 * @param var : String designating the variable for the quest
	 * @param def значение по умолчанию, если переменная до этого не задавалась или не существует
	 * @return the value of the variable of quest represented by "var"
	 */
	public int getInt(String var, int def)
	{
		if(_vars == null)
		{
			return def;
		}

		String variable = _vars.get(var);
		if(variable == null || variable.isEmpty())
		{
			return def;
		}

		int varint = def;
		try
		{
			varint = Integer.parseInt(variable);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, _player.getName() + ": variable " + var + " isn't an integer: " + varint + " ! " + e.getMessage(), e);
		}

		return varint;
	}

	public int getInt(String var)
	{
		return getInt(var, 0);
	}

	/**
	 * Return the quantity of one sort of item hold by the player
	 *
	 * @param itemId : ID of the item wanted to be count
	 * @return long
	 */
	public long getQuestItemsCount(int itemId)
	{
		long count = 0;

		for(L2ItemInstance item : _player.getInventory().getItems())
		{
			if(item != null && item.getItemId() == itemId)
			{
				count += item.getCount();
			}
		}

		return count;
	}

	/**
	 * @param itemId the item Id of the item you're looking for
	 * @return true if item exists in player's inventory, false - if not
	 */
	public boolean hasQuestItems(int itemId)
	{
		return _player.getInventory().hasItems(itemId);
	}

	/**
	 * @param player this parameter contains a reference to the player to check.
	 * @param itemIds the item Ids of the items to verify.
	 * @return {code true} if all the items exists in player's inventory, otherwise {@code false}.
	 */
	public boolean hasQuestItems(L2PcInstance player, int... itemIds)
	{
		return player.getInventory().hasItems(itemIds);
	}

	/**
	 * @param itemId : ID of the item to check enchantment
	 * @return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
	 */
	public int getEnchantLevel(int itemId)
	{
		L2ItemInstance enchanteditem = _player.getInventory().getItemByItemId(itemId);

		if(enchanteditem == null)
		{
			return 0;
		}

		return enchanteditem.getEnchantLevel();
	}

	/**
	 * Дает игроку казанное количество Адены
	 * с учетом рейтов или без.
	 * @param count количество Адены
	 * @param applyRates будут-ли учитываться рейты?
	 */
	public void giveAdena(long count, boolean applyRates)
	{
		giveItems(PcInventory.ADENA_ID, count, applyRates ? 0 : 1);
	}

	/**
	 * Забирает у игрока указанное количество Адены
	 * @param count количество Адены
	 */
	public void takeAdena(long count)
	{
		takeItems(PcInventory.ADENA_ID, count);
	}

	/**
	 * Give reward to player using multiplier's
	 *
	 * @param itemId
	 * @param count
	 */
	public void rewardItems(int itemId, long count)
	{
		if(count <= 0)
		{
			return;
		}

		L2ItemInstance _tmpItem = ItemTable.getInstance().createDummyItem(itemId);

		if(_tmpItem == null)
		{
			return;
		}

		if(itemId == PcInventory.ADENA_ID)
		{
			count *= Config.RATE_QUEST_REWARD_ADENA;
		}
		else if(Config.RATE_QUEST_REWARD_USE_MULTIPLIERS)
		{
			if(_tmpItem.isEtcItem())
			{
				switch(_tmpItem.getEtcItem().getItemType())
				{
					case POTION:
						count *= Config.RATE_QUEST_REWARD_POTION;
						break;
					case SCRL_ENCHANT_WP:
					case SCRL_ENCHANT_AM:
					case SCROLL:
						count *= Config.RATE_QUEST_REWARD_SCROLL;
						break;
					case RECIPE:
						count *= Config.RATE_QUEST_REWARD_RECIPE;
						break;
					case MATERIAL:
						count *= Config.RATE_QUEST_REWARD_MATERIAL;
						break;
					default:
						count *= Config.RATE_QUEST_REWARD;
				}
			}
		}
		else
		{
			count *= Config.RATE_QUEST_REWARD;
		}

		// Add items to player's inventory
		L2ItemInstance item = _player.getInventory().addItem(ProcessType.QUEST, itemId, count, _player, _player.getTarget());

		if(item == null)
		{
			return;
		}

		// If item for reward is gold, send message of gold reward to client
		if(itemId == PcInventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addItemNumber(count);
			_player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if(count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				_player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				_player.sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(_player);
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}

	/**
	 * Give item/reward to the player
	 *
	 * @param itemId
	 * @param count
	 */
	public void giveItems(int itemId, long count)
	{
		giveItems(itemId, count, 0);
	}

	public void giveItem(int itemId)
	{
		giveItems(itemId, 1, 0);
	}

	public void giveItems(int itemId, long count, int enchantlevel)
	{
		if(count <= 0)
		{
			return;
		}

		// If item for reward is adena (ID=57), modify count with rate for quest reward if rates available
		if(itemId == PcInventory.ADENA_ID && !(enchantlevel > 0))
		{
			count *= Config.RATE_QUEST_REWARD_ADENA;
		}

		// Add items to player's inventory
		L2ItemInstance item = _player.getInventory().addItem(ProcessType.QUEST, itemId, count, _player, _player.getTarget());

		if(item == null)
		{
			return;
		}

		// set enchant level for item if that item is not adena
		if(enchantlevel > 0 && itemId != PcInventory.ADENA_ID)
		{
			item.setEnchantLevel(enchantlevel);
		}

		// If item for reward is gold, send message of gold reward to client
		if(itemId == PcInventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addItemNumber(count);
			_player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if(count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				_player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				_player.sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(_player);
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}

	public void giveItems(int itemId, long count, byte attributeId, int attributeLevel)
	{
		if(count <= 0)
		{
			return;
		}

		// Add items to player's inventory
		L2ItemInstance item = _player.getInventory().addItem(ProcessType.QUEST, itemId, count, _player, _player.getTarget());

		if(item == null)
		{
			return;
		}

		// set enchant level for item if that item is not adena
		if(attributeId >= 0 && attributeLevel > 0)
		{
			item.setElementAttr(attributeId, attributeLevel);
			if(item.isEquipped())
			{
				item.updateElementAttrBonus(_player);
			}

			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			_player.sendPacket(iu);
		}

		// If item for reward is gold, send message of gold reward to client
		if(itemId == PcInventory.ADENA_ID)
		{
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			smsg.addItemNumber(count);
			_player.sendPacket(smsg);
		}
		// Otherwise, send message of object reward to client
		else
		{
			if(count > 1)
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
				smsg.addItemName(item);
				smsg.addItemNumber(count);
				_player.sendPacket(smsg);
			}
			else
			{
				SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
				smsg.addItemName(item);
				_player.sendPacket(smsg);
			}
		}
		// send packets
		StatusUpdate su = new StatusUpdate(_player);
		su.addAttribute(StatusUpdate.CUR_LOAD, _player.getCurrentLoad());
		_player.sendPacket(su);
	}

	/**
	 * Drop Quest item using Config.RATE_QUEST_DROP
	 *
	 * @param itemId          : int Item Identifier of the item to be dropped
	 * @param count(minCount,maxCount) : long Quantity of items to be dropped
	 * @param neededCount     : Quantity of items needed for quest
	 * @param dropChance      : int Base chance of drop, same as in droplist
	 * @param sound           : boolean indicating whether to play sound
	 * @return boolean indicating whether player has requested number of items
	 */
	public boolean dropQuestItems(int itemId, int count, long neededCount, int dropChance, boolean sound)
	{
		return dropQuestItems(itemId, count, count, neededCount, dropChance, sound);
	}

	public boolean dropQuestItems(int itemId, int minCount, int maxCount, long neededCount, int dropChance, boolean sound)
	{
		dropChance *= Config.RATE_QUEST_DROP / (_player.getParty() != null ? _player.getParty().getMemberCount() : 1);

		// Так как в квеста в 100% а тут 1000000 = 100%
		dropChance *= 10000;

		long currentCount = getQuestItemsCount(itemId);

		if(neededCount > 0 && currentCount >= neededCount)
		{
			return true;
		}

		if(currentCount >= neededCount)
		{
			return true;
		}

		if(minCount == -1)
		{
			minCount = dropChance / L2DropData.MAX_CHANCE;
		}

		long itemCount = 0;
		int random = Rnd.get(L2DropData.MAX_CHANCE);

		while(random < dropChance)
		{
			// Get the item quantity dropped
			if(minCount < maxCount)
			{
				itemCount += Rnd.get(minCount, maxCount);
			}
			else if(minCount == maxCount)
			{
				itemCount += minCount;
			}
			else
			{
				itemCount++;
			}

			// Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
			dropChance -= L2DropData.MAX_CHANCE;
		}

		if(itemCount > 0)
		{
			// if over neededCount, just fill the gap
			if(neededCount > 0 && currentCount + itemCount > neededCount)
			{
				itemCount = neededCount - currentCount;
			}

			// Inventory slot check
			if(!_player.getInventory().validateCapacityByItemId(itemId))
			{
				return false;
			}

			// Give the item to Player
			_player.addItem(ProcessType.QUEST, itemId, itemCount, _player.getTarget(), true);

			if(sound)
			{
				playSound(currentCount + itemCount < neededCount ? QuestSound.ITEMSOUND_QUEST_ITEMGET : QuestSound.ITEMSOUND_QUEST_MIDDLE);
			}
		}

		return neededCount > 0 && currentCount + itemCount >= neededCount;
	}

	//TODO: More radar functions need to be added when the radar class is complete.
	// BEGIN STUFF THAT WILL PROBABLY BE CHANGED

	public void addRadar(int x, int y, int z)
	{
		_player.getRadar().addMarker(x, y, z);
	}

	public void removeRadar(int x, int y, int z)
	{
		_player.getRadar().removeMarker(x, y, z);
	}

	public void clearRadar()
	{
		_player.getRadar().removeAllMarkers();
	}

	// END STUFF THAT WILL PROBABLY BE CHANGED

	public void takeItems(L2PcInstance player, long count, int... itemIds)
	{
		PcInventory inv = player.getInventory();
		for(int itemId : itemIds)
		{
			if(inv.getItemByItemId(itemId) != null)
			{
				if(takeItemsAndConfirm(itemId, count))
				{
					return;
				}
			}
		}
	}

	/**
	 * Remove items from player's inventory when talking to NPC in order to have rewards.<BR><BR>
	 * <U><I>Actions :</I></U>
	 * <LI>Destroy quantity of items wanted</LI>
	 * <LI>Send new inventory list to player</LI>
	 *
	 * @param itemId : Identifier of the item
	 * @param count  : Quantity of items to destroy
	 */
	public void takeItems(int itemId, long count)
	{
		// Get object item from player's inventory list
		L2ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		if(item == null)
		{
			return;
		}

		// Tests on count value in order not to have negative value
		if(count < 0 || count > item.getCount())
		{
			count = item.getCount();
		}

		// Destroy the quantity of items wanted
		if(item.isEquipped())
		{
			L2ItemInstance[] unequiped = _player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for(L2ItemInstance itm : unequiped)
			{
				iu.addModifiedItem(itm);
			}
			_player.sendPacket(iu);
			_player.broadcastUserInfo();
		}
		_player.destroyItemByItemId(ProcessType.QUEST, itemId, count, _player, true);
	}

	public boolean takeItemsAndConfirm(int itemId, long count)
	{
		// Get object item from player's inventory list
		L2ItemInstance item = _player.getInventory().getItemByItemId(itemId);
		L2PcInstance player = _player;

		if(item == null)
		{
			return false; // item = null
		}

		// Tests on count value in order not to have negative value
		if(count < 0)
		{
			count = item.getCount();
		}

		if(count > item.getCount())
		{
			return false;
		}

		// Destroy the quantity of items wanted
		if(itemId == 57)
		{
			long adenaCount = _player.getAdenaCount();
			if(_player.reduceAdena(ProcessType.QUEST, count, _player, true))
			{
				long adenaCountCheck = _player.getAdenaCount();
				if(count > 0 && adenaCount != adenaCountCheck)
				{
					return true; // all ok
				}
				else if(count == 0)
				{
					return true; // requested 0
				}
				else
				{
					player.sendMessage("You will burn in Hell .... cheater !");
					player.setPunishLevel(PlayerPunishLevel.JAIL, 120);
					return false; // fucking cheater
				}
			}
			else
			{
				return false; // not enough items
			}
		}
		else
		{
			long itemsCount = _player.getInventory().getCountOf(itemId);
			if(_player.destroyItemByItemId(ProcessType.QUEST, itemId, count, _player, true))
			{
				long itemsCountCheck = _player.getInventory().getCountOf(itemId);
				if(count > 0 && itemsCount != itemsCountCheck)
				{
					return true; // all ok
				}
				else if(count == 0)
				{
					return true; // requested 0
				}
				else
				{
					player.sendMessage("You will burn in Hell .... cheater !");
					player.setPunishLevel(PlayerPunishLevel.JAIL, 120);
					return false; // fucking cheater
				}
			}
			else
			{
				return false; // not enough items
			}
		}
	}

	/**
	 * Send a packet in order to play a sound to the player.
	 * @param sound : the name of the sound to play
	 */
	@Deprecated
	public void playSound(String sound)
	{
		_player.sendPacket(new PlaySound(sound));
	}

	/**
	 * Send a packet in order to play a sound to the player.
	 * @param sound : the {@link QuestSound} object of the sound to play
	 */
	public void playSound(QuestSound sound)
	{
		getQuest().playSound(_player, sound);
	}

	/**
	 * Add XP and SP as quest reward
	 *
	 * @param exp
	 * @param sp
	 */
	public void addExpAndSp(int exp, int sp)
	{
		_player.addExpAndSp((int) _player.calcStat(Stats.EXPSP_RATE, exp * Config.RATE_QUEST_REWARD_XP, null, null), (int) _player.calcStat(Stats.EXPSP_RATE, sp * Config.RATE_QUEST_REWARD_SP, null, null));
	}

	/**
	 * Return random value
	 *
	 * @param max : max value for randomisation
	 * @return int
	 */
	public int getRandom(int max)
	{
		return Rnd.get(max);
	}

	/**
	 * @param loc
	 * @return number of ticks from GameTimeController
	 */
	public int getItemEquipped(int loc)
	{
		return _player.getInventory().getPaperdollItemId(loc);
	}

	/**
	 * Return the number of ticks from the GameTimeController
	 *
	 * @return int
	 */
	public int getGameTicks()
	{
		return GameTimeController.getInstance().getGameTicks();
	}

	/**
	 * Return true if quest is to exited on clean up by QuestStateManager
	 *
	 * @return boolean
	 */
	public boolean isExitQuestOnCleanUp()
	{
		return _isExitQuestOnCleanUp;
	}

	/**
	 * Return the QuestTimer object with the specified name
	 *
	 * @return QuestTimer<BR> Return null if name does not exist
	 */
	public void setIsExitQuestOnCleanUp(boolean isExitQuestOnCleanUp)
	{
		_isExitQuestOnCleanUp = isExitQuestOnCleanUp;
	}

	/**
	 * Start a timer for quest.<BR><BR>
	 *
	 * @param name<BR> The name of the timer. Will also be the value for event of onEvent
	 * @param time<BR> The milisecond value the timer will elapse
	 */
	public void startQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, _player, false);
	}

	public void startQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, _player, false);
	}

	public void startRepeatingQuestTimer(String name, long time)
	{
		getQuest().startQuestTimer(name, time, null, _player, true);
	}

	public void startRepeatingQuestTimer(String name, long time, L2Npc npc)
	{
		getQuest().startQuestTimer(name, time, npc, _player, true);
	}

	/**
	 * Return the QuestTimer object with the specified name
	 *
	 * @return QuestTimer<BR> Return null if name does not exist
	 */
	public QuestTimer getQuestTimer(String name)
	{
		return getQuest().getQuestTimer(name, null, _player);
	}

	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, 0);
	}

	public L2Npc addSpawn(int npcId, int despawnDelay)
	{
		return addSpawn(npcId, _player.getX(), _player.getY(), _player.getZ(), 0, false, despawnDelay);
	}

	public L2Npc addSpawn(int npcId, int x, int y, int z)
	{
		return addSpawn(npcId, x, y, z, 0, false, 0);
	}

	/**
	 * Add spawn for player instance
	 * Will despawn after the spawn length expires
	 * Uses player's coords and heading.
	 * Adds a little randomization in the x y coords
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, L2Character cha)
	{
		return addSpawn(npcId, cha, true, 0);
	}

	public L2Npc addSpawn(int npcId, L2Character cha, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), true, despawnDelay);
	}

	/**
	 * Add spawn for player instance
	 * Will despawn after the spawn length expires
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int despawnDelay)
	{
		return addSpawn(npcId, x, y, z, 0, false, despawnDelay);
	}

	/**
	 * Add spawn for player instance
	 * Inherits coords and heading from specified L2Character instance.
	 * It could be either the player, or any killed/attacked mob
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, L2Character cha, boolean randomOffset, int despawnDelay)
	{
		return addSpawn(npcId, cha.getX(), cha.getY(), cha.getZ(), cha.getHeading(), randomOffset, despawnDelay);
	}

	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, false);
	}

	/**
	 * Add spawn for player instance
	 * Return object id of newly spawned npc
	 */
	public L2Npc addSpawn(int npcId, int x, int y, int z, int heading, boolean randomOffset, int despawnDelay, boolean isSummonSpawn)
	{
		return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay, isSummonSpawn);
	}

	public String showHtmlFile(String fileName)
	{
		return getQuest().showHtmlFile(_player, fileName, false);
	}

	/**
	 * @param type the type of the quest, {@link QuestType}.
	 * @return this quest state.
	 */
	public QuestState exitQuest(QuestType type)
	{
		switch(type)
		{
			case REPEATABLE:
			case ONE_TIME:
				exitQuest(type == QuestType.REPEATABLE);
				break;
			case DAILY:
				exitQuest(false);
				setRestartTime();
				break;
		}
		return this;
	}

	/**
	 * Destroy element used by quest when quest is exited
	 *
	 * @param repeatable
	 * @return QuestState
	 */
	private QuestState exitQuest(boolean repeatable)
	{
		if(!isStarted())
		{
			return this;
		}

		// Say quest is completed
		setState(QuestStateType.COMPLETED);

		// Clean registered quest items
		int[] itemIdList = getQuest().getRegisteredItemIds();
		if(itemIdList != null)
		{
			for(int anItemIdList : itemIdList)
			{
				takeItems(anItemIdList, -1);
			}
		}

		// If quest is repeatable, delete quest from list of quest of the player and from database (quest CAN be created again => repeatable)
		Quest.deleteQuestInDb(this, repeatable);
		if(repeatable)
		{
			_player.delQuestState(_questName);
		}
		else
		{
			Quest.updateQuestInDb(this);
		}

		if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
		{
			_player.updateWorldStatistic(CategoryType.QUESTS_COMPLETED, null, 1);
		}

		HookManager.getInstance().notifyEvent(HookType.ON_QUEST_FINISH, _player.getHookContainer(), _player, repeatable);

		_vars = null;
		return this;
	}

	public void showQuestionMark(int number)
	{
		_player.sendPacket(new TutorialShowQuestionMark(number));
	}

	public void playTutorialVoice(String voice)
	{
		_player.sendPacket(new PlaySound(2, voice, 0, 0, _player.getX(), _player.getY(), _player.getZ()));
	}

	public void showTutorialHTML(String html)
	{
		String text = HtmCache.getInstance().getHtm(_player.getLang(), "default/" + html);
		if(text == null)
		{
			_log.log(Level.WARN, "missing html page default/" + html);
			text = "<html><body>File default/" + html + " not found or file is empty.</body></html>";
		}
		_player.sendPacket(new TutorialShowHtml(TutorialShowHtml.SERVER_SIDE, text));
	}

	public void closeTutorialHtml()
	{
		_player.sendPacket(new TutorialCloseHtml());
	}

	public void onTutorialClientEvent(int number)
	{
		_player.sendPacket(new TutorialEnableClientEvent(number));
	}

	public void dropItem(L2MonsterInstance npc, L2PcInstance player, int itemId, int count)
	{
		npc.dropItem(player, itemId, count);
	}

	public int rollDrop(int count, double calcChance)
	{
		if(calcChance <= 0 || count <= 0)
		{
			return 0;
		}
		return rollDrop(count, count, calcChance);
	}

	public int rollDrop(int min, int max, double calcChance)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0)
		{
			return 0;
		}
		int dropmult = 1;
		calcChance *= Config.RATE_QUEST_DROP;
		if(calcChance > 100)
		{
			if((int) Math.ceil(calcChance / 100) <= calcChance / 100)
			{
				calcChance = Math.nextUp(calcChance);
			}
			dropmult = (int) Math.ceil(calcChance / 100);
			calcChance /= dropmult;
		}
		return Rnd.getChance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
	}

	public boolean rollAndGive(int itemId, int min, int max, int limit, double calcChance)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0 || limit <= 0 || itemId <= 0)
		{
			return false;
		}
		long count = rollDrop(min, max, calcChance);
		if(count > 0)
		{
			L2ItemInstance already = _player.getInventory().getItemByItemId(itemId);
			long alreadyCount = 0;
			if(already != null)
			{
				alreadyCount = already.getCount();
			}
			if(alreadyCount + count > limit)
			{
				count = limit - alreadyCount;
			}
			if(count > 0)
			{
				giveItems(itemId, count);
				if(count + alreadyCount < limit)
				{
					playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
				}
				else
				{
					playSound(QuestSound.ITEMSOUND_QUEST_MIDDLE);
					return true;
				}
			}
		}
		return false;
	}

	public void rollAndGive(int itemId, int min, int max, double calcChance)
	{
		if(calcChance <= 0 || min <= 0 || max <= 0 || itemId <= 0)
		{
			return;
		}
		int count = rollDrop(min, max, calcChance);
		if(count > 0)
		{
			giveItems(itemId, count);
			playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
		}
	}

	public boolean rollAndGive(int itemId, int count, double calcChance)
	{
		if(calcChance <= 0 || count <= 0 || itemId <= 0)
		{
			return false;
		}
		int countToDrop = rollDrop(count, calcChance);
		if(countToDrop > 0)
		{
			giveItems(itemId, countToDrop);
			playSound(QuestSound.ITEMSOUND_QUEST_ITEMGET);
			return true;
		}
		return false;
	}

	/**
	 * NPC появляется с данным ID рядом с игроком
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId)
	{
		return spawnNpc(npcId, 0, null);
	}

	/**
	 * NPC появляется с данным ID рядом с игроком
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, int spawnLength)
	{
		return spawnNpc(npcId, spawnLength, null);
	}

	/**
	 * NPC появляется с указанным ID рядом с игроком, NPC говорят сообщение
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, String message)
	{
		return spawnNpc(npcId, 0, null);
	}

	/**
	 * NPC появляется с указанным ID рядом с игроком, NPC говорят сообщение
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, int spawnLength, String message)
	{
		int rnd = Rnd.get(1);
		if(rnd == 0)
		{
			rnd = -1;
		}
		int x = _player.getX() + Rnd.get(50, 100) * rnd;
		int y = _player.getY() + Rnd.get(50, 100) * rnd;
		return spawnNpc(npcId, x, y, _player.getZ(), spawnLength, message, 0);
	}

	/**
	 * Spawnuje Npc o podanym Id w podanej pozycji (x,y,z)
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, int x, int y, int z)
	{
		return spawnNpc(npcId, x, y, z, 0);
	}

	/**
	 * NPC появляется с данным Id в данном положении (х, у, z) исчезнет с течением времени (spawnLength)
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, int x, int y, int z, int spawnLength)
	{
		return spawnNpc(npcId, x, y, z, spawnLength, null, 0);
	}

	/**
	 * NPC появляется с данным Id в данном положении (х, у, z) исчезнет с течением времени (spawnLength)
	 *
	 * @param npcId
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, int x, int y, int z, int spawnLength, String message)
	{
		return spawnNpc(npcId, x, y, z, spawnLength, message, 0);
	}

	public L2NpcInstance spawnNpc(int npcId, Location location, int time, int refId)
	{
		int x = location.getX();
		int y = location.getY();
		int z = location.getZ();

		return spawnNpc(npcId, x, y, z, time, null, refId);
	}

	/**
	 * NPC появляется с данным Id в точке (х, у, г), в то время: spawnLength, которые говорят: сообщение.
	 *
	 * @param npcId
	 * @param x
	 * @param y
	 * @param z
	 * @return L2Spawn заспавненого Npc
	 */
	public L2NpcInstance spawnNpc(int npcId, int x, int y, int z, int spawnLength, String message, int instanceId)
	{
		NpcTable.getInstance();
		L2NpcTemplate temp = NpcTable.getInstance().getTemplate(npcId);
		if(temp == null)
		{
			_log.fatal("Nie ma npc o ID: " + npcId);
			return null;
		}
		L2NpcInstance mob = null;
		try
		{
			// Parametry
			Object[] parameters = {IdFactory.getInstance().getNextId(), temp};

			// Call the constructor of the L2NpcInstance
			// (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance,
			// L2SiegeGuardInstance, L2BoxInstance or L2FolkInstance)
			Object tmp = Class.forName("source.java.model.actor.instance." + temp.getType() + "Instance").getConstructor(int.class, L2NpcTemplate.class).newInstance(parameters);

			// Убедитесь, что L2NpcInstance
			if(!(tmp instanceof L2NpcInstance))
			{
				return mob;
			}

			mob = (L2NpcInstance) tmp;

			// Set the HP and MP of the L2NpcInstance to the max
			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());
			mob.setHeading(-1);
			mob.getInstanceController().setInstanceId(instanceId);
			mob.getLocationController().spawn(x, y, z);

			if(_questSpawns == null)
			{
				_questSpawns = new ArrayList<>();
			}
			// Спаун добавляем в список
			_questSpawns.add(mob);

			// deSpawn по истечении этого времени
			if(spawnLength != 0)
			{
				deSpawnNpcAfterTime(mob, spawnLength);
			}

			if(message != null)
			{
				monsterSay(mob, ChatType.ALL, message);
			}
		}
		catch(Exception e)
		{
			_log.fatal("Something is wrong in Quest Spawn: " + npcId, e);
		}
		return mob;
	}

	public void monsterSay(L2NpcInstance npc, ChatType chatType, String text)
	{
		if(npc == null)
		{
			return;
		}

		if(chatType == ChatType.TELL)
		{
			NS cs = new NS(npc.getObjectId(), chatType, npc.getNpcId(), text);
			_player.sendPacket(cs);
		}
		else
		{
			//npc.say(chatType, text);
		}
	}

	/**
	 * По истечении этого времени NPC исчезает (если был уже мертв)
	 *
	 * @param npc
	 * @param Time
	 * @return
	 */
	public boolean deSpawnNpcAfterTime(L2NpcInstance npc, int Time)
	{
		if(_questSpawns == null)
		{
			return false;
		}
		// NPC после этого времени, будут удалены
		ThreadPoolManager.getInstance().scheduleGeneral(new DeSpawnTask(npc, this), Time);
		return true;
	}

	public void removeSpawn(int npcId)
	{
		if(_questSpawns == null)
		{
			return;
		}

		Iterator<L2NpcInstance> it = _questSpawns.iterator();
		while(it.hasNext())
		{
			L2NpcInstance npc = it.next();
			if(npc.getTemplate().getIdTemplate() == npcId)
			{
				npc.getLocationController().delete();
				it.remove();
			}
		}
	}

	public void removeSpawn(L2NpcInstance npc)
	{
		if(_questSpawns == null)
		{
			return;
		}

		if(_questSpawns.remove(npc))
		{
			npc.getLocationController().delete();
		}
	}

	/**
	 * Wywala wszystkie zespawnowane Npc przez ten Quest
	 */
	public void removeAllSpawn()
	{
		if(_questSpawns == null)
		{
			return;
		}
		for(L2NpcInstance npc : _questSpawns)
		{
			// Wywalamy wszystkie Npc
			npc.getLocationController().delete();
		}
		// do tych L2Spawn nie ma juz zadnej referencji - GC zje je wszystkie :D
		_questSpawns = null;
	}

	/**
	 * Zwraca spawn questowy danego NPC
	 *
	 * @param npcId
	 * @return
	 */
	public L2NpcInstance getSpawn(int npcId)
	{
		if(_questSpawns == null)
		{
			return null;
		}
		for(L2NpcInstance npc : _questSpawns)
		{
			if(npc.getNpcId() == npcId)
			{
				return npc;
			}
		}
		return null;
	}

	public boolean isSpawnExists(int npcId)
	{
		return getSpawn(npcId) != null;
	}

	/**
	 * Set the restart time for the daily quests.<br>
	 * The time is hardcoded at {@link Quest#getResetHour()} hours, {@link Quest#getResetMinutes()} minutes of the following day.<br>
	 * It can be overridden in scripts (quests).
	 */
	public void setRestartTime()
	{
		Calendar reDo = Calendar.getInstance();
		if(reDo.get(Calendar.HOUR_OF_DAY) >= getQuest().getResetHour())
		{
			reDo.add(Calendar.DATE, 1);
		}
		reDo.set(Calendar.HOUR_OF_DAY, getQuest().getResetHour());
		reDo.set(Calendar.MINUTE, getQuest().getResetMinutes());
		set("restartTime", String.valueOf(reDo.getTimeInMillis()));
	}

	/**
	 * @return {@code true} if the quest is available, for example daily quests, {@code false} otherwise.
	 */
	public boolean isNowAvailable()
	{
		String val = get("restartTime");
		return val == null || !Util.isDigit(val) || Long.parseLong(val) <= System.currentTimeMillis();
	}
}
