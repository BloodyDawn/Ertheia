package dwo.gameserver.instancemanager;

import dwo.gameserver.datatables.sql.queries.InzoneHistory;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.CharacterClassHolder;
import dwo.gameserver.model.holders.InstancePartyHistoryHolder;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.07.12
 * Time: 11:38
 */

public class InstanceHistoryManager
{
	private static final Logger _log = LogManager.getLogger(InstanceHistoryManager.class);

	private static final FastMap<Integer, InstancePartyHistoryHolder> _instancePartyHistoryData = new FastMap<>();
	private static final FastMap<Integer, List<Integer>> _instanceCharacterHistoryData = new FastMap<>();

	public InstanceHistoryManager()
	{
		_instancePartyHistoryData.shared();
		load();
	}

	public static InstanceHistoryManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		// Грузим основное хранилищие PartyId-ов c их содержимым
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(InzoneHistory.GET_PARTY_INZONE_HISTORY);
			rset = statement.executeQuery();
			InstancePartyHistoryHolder data;
			CharacterClassHolder charHolder;
			while(rset.next())
			{
				data = new InstancePartyHistoryHolder();
				// TODO: Прогрузка charId его classId в List<CharacterClassHolder>
				data.setInstanceId(rset.getInt("instance_id"));
				data.setInstanceUseTime(rset.getInt("instance_use_time"));
				data.setInstanceStatus(rset.getInt("instance_status"));
				_instancePartyHistoryData.put(rset.getInt("party_id"), data);
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _instancePartyHistoryData.size() + " party histories.");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not load party instance history: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Грузим ссылки charId <-> partyId
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(InzoneHistory.GET_CHARACTER_INZONE_HISTORY);
			List<Integer> charHistory;
			while(rset.next())
			{
				int charId = rset.getInt("char_id");
				int partyId = rset.getInt("party_id");
				charHistory = new ArrayList<>();
				if(_instanceCharacterHistoryData.containsKey(charId))
				{
					charHistory = _instanceCharacterHistoryData.get(charId);
					charHistory.add(partyId);
				}
				else
				{
					charHistory.add(partyId);
				}
				_instanceCharacterHistoryData.put(charId, charHistory);
			}
			_log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _instancePartyHistoryData.size() + " characters in history.");
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Could not load character party's instance's history: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Запись информации о посещении инстанса для группы игроков
	 * @param party группа игроков
	 */
	public void addHistoryForParty(L2Party party)
	{
		int partyId = IdFactory.getInstance().getNextId();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			// Добавляем каждому игроку ссылку в таблицу
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(InzoneHistory.ADD_CHARACTER_INZONE_HISTORY);
			for(L2PcInstance player : party.getMembers())
			{
				// Добавляем запись в базу
				statement.setInt(1, player.getObjectId());
				statement.setInt(2, partyId);
				statement.execute();

				// Добавляем запись в массив
				if(_instanceCharacterHistoryData.containsKey(player.getObjectId()))
				{
					_instanceCharacterHistoryData.get(player.getObjectId()).add(partyId);
				}
				else
				{
					List<Integer> temp = new ArrayList<>();
					temp.add(partyId);
					_instanceCharacterHistoryData.put(player.getObjectId(), temp);
				}
			}
			// TODO: Сохранение инфы для всей группы (кто в группе состоял, какие классы у него и т.п. - нужно продумать хранение)
			// statement = con.prepareStatement(InzoneHistory.ADD_PARTY_INZONE_HISTORY);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while addHistoryForParty(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Добавляет ссылку игроку в character_inzone_history на partyId
	 * @param player указанный игрок
	 * @param partyId ID группы, в которой состоит игрок
	 */
	private void addHistoryForCharacter(L2PcInstance player, int partyId)
	{

	}

	/**
	 * @param partyId ID группы
	 * @return InstancePartyHistoryHolder указанной группы
	 */
	public InstancePartyHistoryHolder getPartyInzoneHistory(int partyId)
	{
		return _instancePartyHistoryData.get(partyId);
	}

	/**
	 * @param charId ID персонажа
	 * @return List c partyId в которых состоял персонаж в инстах
	 */
	public List<Integer> getCharacterInzoneHistory(int charId)
	{
		return _instanceCharacterHistoryData.get(charId);
	}

	private static class SingletonHolder
	{
		protected static final InstanceHistoryManager _instance = new InstanceHistoryManager();
	}
}