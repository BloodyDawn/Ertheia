package dwo.gameserver.model.world.residence.clanhall;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.xml.ResidenceFunctionData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.residence.function.FunctionData;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.model.world.zone.type.L2ClanHallZone;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public abstract class ClanHall
{
	protected static final Logger _log = LogManager.getLogger(ClanHall.class);
	protected static final int _chRate = 604800000;
	// Константы
	private static final long ONE_DAY = 24 * 60 * 60 * 1000;
	private static long ONE_WEEK = 7 * ONE_DAY;
	protected boolean _isFree = true;
	private int _id;
	private List<L2DoorInstance> _doors;
	private String _name;
	private int _ownerId;
	private String _desc;
	private String _location;
	private L2ClanHallZone _zone;
	private Map<FunctionType, ClanHallFunction> _functions;

	protected ClanHall(StatsSet set)
	{
		_id = set.getInteger("id");
		_name = set.getString("name");
		_ownerId = set.getInteger("ownerId");
		_desc = set.getString("desc");
		_location = set.getString("location");
		_functions = new FastMap<>();

		if(_ownerId > 0)
		{
			L2Clan clan = ClanTable.getInstance().getClan(_ownerId);
			if(clan != null)
			{
				clan.setClanhallId(_id);
			}
			else
			{
				free();
			}
		}
	}

	/**
	 * @return ID текущего КХ
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return название КХ
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * @return ClanID владельца
	 */
	public int getOwnerId()
	{
		return _ownerId;
	}

	/**
	 * @return описание КХ
	 */
	public String getDesc()
	{
		return _desc;
	}

	/**
	 * @return место расположения КХ
	 */
	public String getLocation()
	{
		return _location;
	}

	/**
	 * @return все двери текущего КХ
	 */
	public List<L2DoorInstance> getDoors()
	{
		if(_doors == null)
		{
			_doors = new FastList<>();
		}
		return _doors;
	}

	/**
	 * @param doorId ID двери
	 * @return дверь с указанным ID, если она принадлежит текущему КХ
	 */
	public L2DoorInstance getDoor(int doorId)
	{
		if(doorId <= 0)
		{
			return null;
		}
		for(L2DoorInstance door : getDoors())
		{
			if(door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}

	/**
	 * @param type тип функции КХ
	 * @return указанный тип функции КХ
	 */
	public ClanHallFunction getFunction(FunctionType type)
	{
		return _functions.get(type);
	}

	/***
	 * @param type тип функции
	 * @return уровень указанной функции у КХ
	 */
	public int getFunctionLevel(FunctionType type)
	{
		ClanHallFunction function = _functions.get(type);
		if(function == null)
		{
			return 0;
		}
		return function.getFunctionData().getLevel();
	}

	/**
	 *
	 * @param x координата X
	 * @param y координата Y
	 * @param z координата Z
	 * @return {@code true} если обьект внутри зоны
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getZone().isInsideZone(x, y, z);
	}

	/**
	 * @return L2ClanHallZone зону текущего КХ
	 */
	public L2ClanHallZone getZone()
	{
		return _zone;
	}

	/**
	 * Назначает текужему КХ зону
	 * @param zone зона
	 */
	public void setZone(L2ClanHallZone zone)
	{
		_zone = zone;
	}

	/**
	 * Освобождает текущий КХ
	 */
	public void free()
	{
		_ownerId = 0;
		_isFree = true;
		_functions.keySet().forEach(this::removeFunction);
		_functions.clear();
		updateDb();
	}

	/**
	 * Выставляет владельца для текущего КХ (если он свободен)
	 * @param clan клан-владелец
	 */
	public void setOwner(L2Clan clan)
	{
		if(_ownerId > 0 && (clan == null || clan.getClanId() != _ownerId))
		{
			return;
		}
		_ownerId = clan.getClanId();
		_isFree = false;
		clan.setClanhallId(_id);

		// Анонсируем членам клана о новом КХ
		clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		updateDb();
	}

	/**
	 * Открытие или закрытие двери
	 * @param activeChar действующий персонаж
	 * @param doorId ID двери
	 * @param open открыть\закрыть
	 */
	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if(activeChar != null && activeChar.getClanId() == _ownerId)
		{
			openCloseDoor(doorId, open);
		}
	}

	public void openCloseDoor(int doorId, boolean open)
	{
		openCloseDoor(getDoor(doorId), open);
	}

	public void openCloseDoor(L2DoorInstance door, boolean open)
	{
		if(door != null)
		{
			if(open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		}
	}

	public void openCloseDoors(L2PcInstance activeChar, boolean open)
	{
		if(activeChar != null && activeChar.getClanId() == _ownerId)
		{
			openCloseDoors(open);
		}
	}

	public void openCloseDoors(boolean open)
	{
		getDoors().stream().filter(door -> door != null).forEach(door -> {
			if(open)
			{
				door.openMe();
			}
			else
			{
				door.closeMe();
			}
		});
	}

	/**
	 * Выпроваживание чужих персонажей из КХ
	 */
	public void banishForeigners()
	{
		if(_zone != null)
		{
			_zone.banishForeigners(_ownerId);
		}
		else
		{
			_log.log(Level.WARN, getClass().getSimpleName() + ": Zone is null for clan hall: " + _id + ' ' + _name);
		}
	}

	/**
	 * Загрузка функций КХ из базы данных
	 */
	protected void loadFunctions()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("SELECT deco_id, endTime FROM clanhall_functions WHERE agit_id = ?");
			statement.setInt(1, _id);
			rs = statement.executeQuery();

			while(rs.next())
			{
				FunctionData deco = ResidenceFunctionData.getInstance().getDeco(rs.getInt("deco_id"));
				_functions.put(deco.getType(), new ClanHallFunction(deco, rs.getLong("endTime")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHall.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Удаление функции КХ из списка и из базы
	 * @param type тип функции КХ
	 */
	public void removeFunction(FunctionType type)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE agit_id = ? AND deco_id = ?");
			statement.setInt(1, _id);
			statement.setInt(2, _functions.get(type).getFunctionData().getId());
			statement.execute();
			_functions.get(type).stopTask();
			_functions.remove(type);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHall.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/***
	 * Обновление функции (поднятие уровня\добавление) в базе данных
	 * @param type тип функции
	 * @param level уровень функции
	 * @return {@code true} если добавление прошло удачно
	 */
	public boolean updateFunctions(FunctionType type, int level)
	{
		boolean delete = _functions.containsKey(type);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			if(delete)
			{
				statement = con.prepareStatement("DELETE FROM clanhall_functions WHERE agit_id = ? AND deco_id = ?");
				statement.setInt(1, _id);
				statement.setInt(2, _functions.get(type).getFunctionData().getId());
				statement.execute();
				_functions.get(type).stopTask();
				_functions.remove(type);
			}

			FunctionData deco = ResidenceFunctionData.getInstance().getDeco(type, level);
			long time = System.currentTimeMillis() + deco.getFunctionCostData().getDays() * ONE_DAY;

			statement = con.prepareStatement("INSERT INTO clanhall_functions (agit_id, deco_id, endTime) VALUES (?, ?, ?)");
			statement.setInt(1, _id);
			statement.setInt(2, deco.getId());
			statement.setLong(3, time);
			statement.execute();

			_functions.put(type, new ClanHallFunction(deco, time));
			return true;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: ClanHall.updateFunctions", e);
			return false;
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int getGrade()
	{
		return 0;
	}

	public long getPaidUntil()
	{
		return 0;
	}

	public int getLease()
	{
		return 0;
	}

	/***
	 * @return сколько дней осталось до очередной оплаты КХ
	 */
	public int getDaysToFree()
	{
		return 1;
	}

	public boolean isSiegableHall()
	{
		return false;
	}

	public abstract void updateDb();

	public class ClanHallFunction
	{
		private long _expire;
		private FunctionData _deco;
		private ScheduledFuture _task;

		public ClanHallFunction(FunctionData d, long expire)
		{
			_deco = d;
			_expire = expire;
			StartAutoTask();
		}

		public long getEndTime()
		{
			return _expire;
		}

		public FunctionData getFunctionData()
		{
			return _deco;
		}

		private void updateDecoRent()
		{
			_expire = System.currentTimeMillis() + _deco.getFunctionCostData().getDays() * ONE_DAY;

			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();

				statement = con.prepareStatement("UPDATE clanhall_functions SET endTime = ? WHERE agit_id = ? AND deco_id = ? ");
				statement.setLong(1, _expire);
				statement.setInt(2, _id);
				statement.setInt(3, _deco.getId());
				statement.executeUpdate();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: ClanHall.updateDecoTime", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}

		public void stopTask()
		{
			if(_task != null)
			{
				_task.cancel(true);
				_task = null;
			}
		}

		private void taskProcess()
		{
			L2Clan clan = ClanTable.getInstance().getClan(_ownerId);
			if(clan == null || clan.getWarehouse().getAdenaCount() < _deco.getFunctionCostData().getCost())
			{
				removeFunction(_deco.getType());
			}
			else
			{
				clan.getWarehouse().destroyItemByItemId(ProcessType.CLAN, PcInventory.ADENA_ID, _deco.getFunctionCostData().getCost(), null, null);
				updateDecoRent();
				_task = ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), _expire - System.currentTimeMillis());
			}
		}

		private void StartAutoTask()
		{
			_task = _expire <= System.currentTimeMillis() ? ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), 30000) : ThreadPoolManager.getInstance().scheduleGeneral(new AutoTask(), _expire - System.currentTimeMillis());
		}

		private class AutoTask implements Runnable
		{
			public AutoTask()
			{
			}

			@Override
			public void run()
			{
				taskProcess();
			}
		}
	}
}
