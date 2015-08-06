package dwo.gameserver.model.world.residence.fort;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Fortress;
import dwo.gameserver.datatables.xml.FortSpawnList;
import dwo.gameserver.datatables.xml.ResidenceFunctionData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.datatables.xml.StaticObjectsData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.model.holders.FortFacilitySpawnHolder;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.fort.FortUpdater.UpdaterType;
import dwo.gameserver.model.world.residence.function.FunctionData;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.model.world.zone.type.L2FortZone;
import dwo.gameserver.model.world.zone.type.L2SiegeZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.procedure.TObjectProcedure;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Fort
{
	protected static final Logger _log = LogManager.getLogger(Fort.class);
	// Константы
	private static final long ONE_DAY = 24 * 60 * 60 * 1000;
	private static long ONE_WEEK = 7 * ONE_DAY;
	private static Map<FortFacilityType, Integer> _facilities;
	private int _fortId;
	private L2DoorInstance[] _doors;
	private L2StaticObjectInstance _flagPole;
	private String _name = "";
	private FortSiegeEngine _siegeEngine;
	private Calendar _siegeDate;
	private Calendar _lastOwnedTime;
	private L2FortZone _fortZone;
	private L2SiegeZone _zone;
	private L2Clan _fortOwner;
	private FortType _fortType = FortType.SMALL;
	private FortState _state = FortState.NOT_DECIDED;
	private int _castleId;
	private int _supplyLvL;
	private Map<FunctionType, FortFunction> _functions;
	private FastList<L2Skill> _residentialSkills = new FastList<>();
	private ScheduledFuture<?>[] _FortUpdater = new ScheduledFuture<?>[2];
	// Спаун НПЦ во время осад
	private boolean _isSuspiciousMerchantSpawned;
	private boolean _isControlRoomDeactivated;

	public Fort(int fortId)
	{
		_fortId = fortId;
		_functions = new FastMap<>();
		_facilities = new HashMap<>();
		load();
		_doors = DoorGeoEngine.getInstance().getFortDoors(fortId);
		loadFlagPoles();
		List<L2SkillLearn> residentialSkills = SkillTreesData.getInstance().getAvailableResidentialSkills(fortId, null);
		for(L2SkillLearn s : residentialSkills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
			if(sk != null)
			{
				_residentialSkills.add(sk);
			}
			else
			{
				_log.log(Level.WARN, "Fort Id: " + fortId + " has a null residential skill Id: " + s.getSkillId() + " level: " + s.getSkillLevel() + '!');
			}
		}

		if(_fortOwner != null)
		{
			setVisibleFlag(true);
			loadFunctions();
			loadFacilities();
		}

		spawnFortCitizens(); // Спаун стандартных НПЦ (заспаунены всегда)
		spawnOnSiegeDespawnNpcs(); // Спаун коммандиров-нпц, которые изчезают при начале осады

		if(_fortOwner != null && _state == FortState.NOT_DECIDED)
		{
			spawnOnSiegeEndNpcs();
			ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this), 1 * 60 * 60 * 1000); // Prepare 1hr task for special envoys despawn
		}
	}

	/**
	 * @param type тип функции форта
	 * @return указанный тип функции форта
	 */
	public FortFunction getFunction(FunctionType type)
	{
		return _functions.get(type);
	}

	/***
	 * @param type тип функции
	 * @return уровень указанной функции у форта
	 */
	public int getFunctionLevel(FunctionType type)
	{
		FortFunction function = _functions.get(type);
		if(function == null)
		{
			return 0;
		}
		return function.getFunctionData().getLevel();
	}

	public void endOfSiege(L2Clan clan)
	{
		ThreadPoolManager.getInstance().scheduleGeneral(new endFortressSiege(this, clan), 1000);
	}

	public void engrave(L2Clan clan)
	{
		setOwner(clan, true);
	}

	/**
	 * Выгоняем из зоны форта весь левый народ
	 */
	public void banishForeigners()
	{
		getFortZone().banishForeigners(_fortOwner.getClanId());
	}

	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return {@code true} если указанные координаты находятся в зоне форта
	 */
	public boolean checkIfInZone(int x, int y, int z)
	{
		return getZone().isInsideZone(x, y, z);
	}

	public L2SiegeZone getZone()
	{
		if(_zone == null)
		{
			for(L2SiegeZone zone : ZoneManager.getInstance().getAllZones(L2SiegeZone.class))
			{
				if(zone.getSiegeObjectId() == _fortId)
				{
					_zone = zone;
					break;
				}
			}
		}
		return _zone;
	}

	public L2FortZone getFortZone()
	{
		if(_fortZone == null)
		{
			for(L2FortZone zone : ZoneManager.getInstance().getAllZones(L2FortZone.class))
			{
				if(zone.getFortId() == _fortId)
				{
					_fortZone = zone;
					break;
				}
			}
		}
		return _fortZone;
	}

	/**
	 * @param obj object
	 * @return the objects distance to this fort
	 */
	public double getDistance(L2Object obj)
	{
		return getZone().getDistanceToZone(obj);
	}

	public void closeDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, false);
	}

	public void openDoor(L2PcInstance activeChar, int doorId)
	{
		openCloseDoor(activeChar, doorId, true);
	}

	public void openCloseDoor(L2PcInstance activeChar, int doorId, boolean open)
	{
		if(!activeChar.getClan().equals(_fortOwner))
		{
			return;
		}

		L2DoorInstance door = getDoor(doorId);
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

	/**
	 * установка нового владельца для текущего форта
	 * @param clan новый клан-владелец
	 * @param updateClansReputation {@code true} обновлять-ли очки репутации
	 * @return {@code true} если все прошло удачно
	 */
	public boolean setOwner(L2Clan clan, boolean updateClansReputation)
	{
		if(clan == null)
		{
			_log.log(Level.WARN, getClass().getSimpleName() + ": Updating Fort owner with null clan!!!");
			return false;
		}
		L2Clan oldowner = _fortOwner;
		if(oldowner != null && !clan.equals(oldowner))
		{
			// Снимаем штрафные очки репутации у бывшего владельца форта
			updateClansReputation(oldowner, true);
			try
			{
				L2PcInstance oldLeader = oldowner.getLeader().getPlayerInstance();
				if(oldLeader != null)
				{
					if(oldLeader.getMountType() == 2)
					{
						oldLeader.dismount();
					}
				}
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception in setOwner: " + e.getMessage(), e);
			}
			removeOwner();
		}

		// Выставляем базовый статус для пустого форта
		setFortState(FortState.NOT_DECIDED, 0);

		// Если у клана-победителя уже есть замок, то не отдаем форт обратно НПЦ
		if(clan.getCastleId() > 0)
		{
			getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.NPCS_RECAPTURED_FORTRESS));
			return false;
		}

		// Если у клана-победителя уже был форт, то отдаем старый НПЦ
		if(clan.getFortId() > 0)
		{
			FortManager.getInstance().getFortByOwner(clan).removeOwner();
		}

		// Даем очки репутации клану-победителю
		if(updateClansReputation)
		{
			updateClansReputation(clan, false);
		}

		spawnOnSiegeEndNpcs();
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleSpecialEnvoysDeSpawn(this), 1 * 60 * 60 * 1000); // Prepare 1hr task for special envoys despawn

		setSupplyLvL(0);
		resetFacilityData();
		setOwnerClan(clan);

		// Принудительно завершаем осаду, если она до сих пор идет
		if(getSiege().isInProgress())
		{
			getSiege().endSiege();
		}

		// Шлем пассивные скилы форта членам клана
		for(L2PcInstance member : clan.getOnlineMembers(0))
		{
			giveResidentialSkills(member);
			member.sendSkillList();
		}
		save();
		return true;
	}

	/***
	 * Удаление владельца у текущего форта
	 */
	public void removeOwner()
	{
		L2Clan clan = _fortOwner;
		if(clan != null)
		{
			for(L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}
			clan.setFortId(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			setOwnerClan(null);
			setSupplyLvL(0);
			resetFacilityData();
			removeAllFunctions();
			save();
		}
	}

	public void raiseSupplyLvL()
	{
		_supplyLvL++;
		if(_supplyLvL > Config.FS_MAX_SUPPLY_LEVEL)
		{
			_supplyLvL = Config.FS_MAX_SUPPLY_LEVEL;
		}
	}

	public int getSupplyLvL()
	{
		return _supplyLvL;
	}

	public void setSupplyLvL(int val)
	{
		if(val <= Config.FS_MAX_SUPPLY_LEVEL)
		{
			_supplyLvL = val;
		}
	}

	/**
	 * Показать/спрятать флаг форта
	 * @param val {@code true} показать флаг, {@code false} спрятать флаг
	 */
	public void setVisibleFlag(boolean val)
	{
		L2StaticObjectInstance flagPole = _flagPole;
		if(flagPole != null)
		{
			flagPole.setMeshIndex(val ? 1 : 0);
		}
	}

	/**
	 * Переинициализация дверей форта
	 */
	public void resetDoors()
	{
		for(L2DoorInstance door : _doors)
		{
			if(door.isOpened())
			{
				door.closeMe();
			}
			if(door.getCurrentHp() <= 0)
			{
				door.doRevive();
			}
			// Бустим HP дверьке при их прогрузке в форт
			if(getFacilityLevel(FortFacilityType.FORTRESS_DOOR_POWER_UP) > 0)
			{
				door.setHpLevel(200);  // TODO: Узнать точное значение буста при апгрейде двери
			}
			if(door.getCurrentHp() < door.getMaxHp())
			{
				door.setCurrentHp(door.getMaxHp());
			}
		}
	}

	/***
	 * Загрузка форта из базы данных
	 */
	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Fortress.SELECT);
			statement.setInt(1, _fortId);
			rs = statement.executeQuery();
			int ownerId = 0;

			while(rs.next())
			{
				_name = rs.getString("name");
				_siegeDate = Calendar.getInstance();
				_lastOwnedTime = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				_lastOwnedTime.setTimeInMillis(rs.getLong("lastOwnedTime"));
				ownerId = rs.getInt("owner");
				_fortType = FortType.values()[rs.getInt("fortType")];
				_state = FortState.values()[rs.getInt("state")];
				_castleId = rs.getInt("castleId");
				_supplyLvL = rs.getInt("supplyLvL");
			}

			if(ownerId > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(ownerId); // Try to find clan instance
				clan.setFortId(_fortId);
				setOwnerClan(clan);
				int runCount = getOwnedTime() / (Config.FS_UPDATE_FRQ * 60);
				long initial = System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis();
				while(initial > Config.FS_UPDATE_FRQ * 60000L)
				{
					initial -= Config.FS_UPDATE_FRQ * 60000L;
				}
				initial = Config.FS_UPDATE_FRQ * 60000L - initial;
				if(Config.FS_MAX_OWN_TIME <= 0 || getOwnedTime() < Config.FS_MAX_OWN_TIME * 3600)
				{
					_FortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, UpdaterType.PERIODIC_UPDATE), initial, Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
					if(Config.FS_MAX_OWN_TIME > 0)
					{
						_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, runCount, UpdaterType.MAX_OWN_TIME), 3600000, 3600000); // Schedule owner tasks to remove owener
					}
				}
				else
				{
					_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneral(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME), 60000); // Schedule owner tasks to remove owner
				}
			}
			else
			{
				setOwnerClan(null);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception while loading fort data: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/***
	 * Сохранение состояния форта в базу данных
	 */
	public void save()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Fortress.UPDATE);
			statement.setLong(1, _siegeDate.getTimeInMillis());
			statement.setLong(2, _lastOwnedTime.getTimeInMillis());
			statement.setInt(3, _fortOwner != null ? _fortOwner.getClanId() : 0);
			statement.setInt(4, _state.ordinal());
			statement.setInt(5, _castleId);
			statement.setInt(6, _supplyLvL);
			statement.setInt(7, _fortId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception while saving fort data:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Загрузка функций форта из базы данных
	 */
	private void loadFunctions()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Fortress.FUNCTION_SELECT);
			statement.setInt(1, _fortId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				FunctionData deco = ResidenceFunctionData.getInstance().getDeco(rs.getInt("deco_id"));
				_functions.put(deco.getType(), new FortFunction(deco, rs.getLong("endTime")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Fort.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Удаление функции форта из базы данных
	 * @param type тип функции форта
	 */
	public void removeFunction(FunctionType type)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Fortress.FUNCTION_DELETE);
			statement.setInt(1, _fortId);
			statement.setInt(2, _functions.get(type).getFunctionData().getId());
			statement.execute();
			_functions.get(type).stopTask();
			_functions.remove(type);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Fort.removeFunctions(int functionType): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Удаление всех функций форта
	 */
	private void removeAllFunctions()
	{
		_functions.keySet().forEach(this::removeFunction);
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
				statement = con.prepareStatement(Fortress.FUNCTION_DELETE);
				statement.setInt(1, _fortId);
				statement.setInt(2, _functions.get(type).getFunctionData().getId());
				statement.execute();
				_functions.get(type).stopTask();
				_functions.remove(type);
			}

			FunctionData deco = ResidenceFunctionData.getInstance().getDeco(type, level);
			long time = System.currentTimeMillis() + deco.getFunctionCostData().getDays() * ONE_DAY;

			statement = con.prepareStatement(Fortress.FUNCTION_INSERT);
			statement.setInt(1, _fortId);
			statement.setInt(2, deco.getId());
			statement.setLong(3, time);
			statement.execute();

			_functions.put(type, new FortFunction(deco, time));
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

	public void spawnDoors()
	{
		for(L2DoorInstance door : _doors)
		{
			door.getLocationController().spawn(door.getX(), door.getY(), door.getZ());
		}
	}

	/***
	 * Загрузка флагов для форта из таблицы статичных обьектов
	 */
	private void loadFlagPoles()
	{
		for(L2StaticObjectInstance obj : StaticObjectsData.getInstance().getStaticObjects())
		{
			if(obj.getType() == 3 && obj.getName().startsWith(_name))
			{
				_flagPole = obj;
				break;
			}
		}
		if(_flagPole == null)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + ": Cant find flagpole for Fort " + this);
		}
	}

	public int getFortId()
	{
		return _fortId;
	}

	public L2Clan getOwnerClan()
	{
		return _fortOwner;
	}

	public void setOwnerClan(L2Clan clan)
	{
		setVisibleFlag(clan != null);
		_fortOwner = clan;

		if(clan != null)
		{
			_lastOwnedTime.setTimeInMillis(System.currentTimeMillis());
		}
		else
		{
			_lastOwnedTime.setTimeInMillis(0);
		}

		setFortState(FortState.NOT_DECIDED, 0);

		// Уведомляем игроков о новом владельце форта
		if(clan != null)
		{
			clan.setFortId(_fortId); // Set has fort flag for new owner
			WorldManager.getInstance().forEachPlayer(new ForEachPlayerSendMessage(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_VICTORIOUS_IN_THE_FORTRESS_BATTLE_OF_S2).addString(clan.getName()).addCastleId(_fortId)));
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
			clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
			if(_FortUpdater[0] != null)
			{
				_FortUpdater[0].cancel(false);
			}
			if(_FortUpdater[1] != null)
			{
				_FortUpdater[1].cancel(false);
			}
			_FortUpdater[0] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.PERIODIC_UPDATE), Config.FS_UPDATE_FRQ * 60000L, Config.FS_UPDATE_FRQ * 60000L); // Schedule owner tasks to start running
			if(Config.FS_MAX_OWN_TIME > 0)
			{
				_FortUpdater[1] = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FortUpdater(this, clan, 0, UpdaterType.MAX_OWN_TIME), 3600000, 3600000); // Schedule owner tasks to remove owener
			}
		}
		else
		{
			if(_FortUpdater[0] != null)
			{
				_FortUpdater[0].cancel(false);
			}
			_FortUpdater[0] = null;
			if(_FortUpdater[1] != null)
			{
				_FortUpdater[1].cancel(false);
			}
			_FortUpdater[1] = null;
		}
	}

	public L2DoorInstance getDoor(int doorId)
	{
		if(doorId <= 0)
		{
			return null;
		}

		for(L2DoorInstance door : _doors)
		{
			if(door.getDoorId() == doorId)
			{
				return door;
			}
		}
		return null;
	}

	public L2DoorInstance[] getDoors()
	{
		return _doors;
	}

	/***
	 * @return ближайщую к обьекту дверь
	 * @param object обьект, от которого проверяем расстояние
	 * @param byName тип двери, которую мы ищем ("fort02_wall_door" и т.п.)
	 */
	@Deprecated
	public L2DoorInstance getNearestDoor(L2Object object, String byName)
	{
		double minDistance = Double.MAX_VALUE;
		double currentDistance;
		L2DoorInstance tempDoor = null;
		for(L2DoorInstance door : _doors)
		{
			currentDistance = Util.calculateDistance(object, door, true);
			if(byName != null ? door.getDoorTemplate().getDoorName().equals(byName) && currentDistance < minDistance : currentDistance < minDistance)
			{
				tempDoor = door;
			}
		}
		return tempDoor;
	}

	/***
	 * @param byName имя двери
	 * @return список дверей форта по указанному имени
	 */
	public List<L2DoorInstance> getDoors(String byName)
	{
		List<L2DoorInstance> temp = new ArrayList<>();
		for(L2DoorInstance door : _doors)
		{
			if(door.getDoorTemplate().getDoorName().equals(byName))
			{
				temp.add(door);
			}
		}
		return temp;
	}

	public L2StaticObjectInstance getFlagPole()
	{
		return _flagPole;
	}

	public FortSiegeEngine getSiege()
	{
		if(_siegeEngine == null)
		{
			_siegeEngine = new FortSiegeEngine(this);
		}
		return _siegeEngine;
	}

	public Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public void setSiegeDate(Calendar siegeDate)
	{
		_siegeDate = siegeDate;
	}

	public int getOwnedTime()
	{
		if(_lastOwnedTime.getTimeInMillis() == 0)
		{
			return 0;
		}

		return (int) ((System.currentTimeMillis() - _lastOwnedTime.getTimeInMillis()) / 1000);
	}

	public long getTimeTillRebelArmy()
	{
		if(_lastOwnedTime.getTimeInMillis() == 0)
		{
			return 0;
		}
		return _lastOwnedTime.getTimeInMillis() + Config.FS_MAX_OWN_TIME * 3600000L - System.currentTimeMillis();
	}

	public long getTimeTillNextFortUpdate()
	{
		if(_FortUpdater[0] == null)
		{
			return 0;
		}
		return _FortUpdater[0].getDelay(TimeUnit.SECONDS);
	}

	public String getName()
	{
		return _name;
	}

	public void updateClansReputation(L2Clan owner, boolean removePoints)
	{
		if(owner != null)
		{
			if(removePoints)
			{
				owner.takeReputationScore(Config.LOOSE_FORT_POINTS, true);
			}
			else
			{
				owner.addReputationScore(Config.TAKE_FORT_POINTS, true);
			}
		}
	}

	/**
	 * @return статус крепости
	 */
	public FortState getFortState()
	{
		return _state;
	}

	/***
	 * Установить отношения текущей крепости с указанным замком
	 * @param state тип отношения
	 * @param castleId ID замка
	 */
	public void setFortState(FortState state, int castleId)
	{
		_state = state;
		_castleId = castleId;
	}

	/**
	 * @return ID замка, которому присягнул текущий форт
	 */
	public int getCastleId()
	{
		return _castleId;
	}

	/**
	 * @return FortType тип форта
	 */
	public FortType getFortType()
	{
		return _fortType;
	}

	/**
	 * @return количество бараков в форте
	 */
	public int getFortSize()
	{
		return _fortType == FortType.SMALL ? 3 : 5;
	}

	/***
	 * Заспаунить НПЦ, которые пропадают через 10 минут после начала осады
	 */
	public void spawnSiege10MinMerchants()
	{
		if(_isSuspiciousMerchantSpawned)
		{
			return;
		}
		_isSuspiciousMerchantSpawned = true;

		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.DESPAWN_ON_SIEGE_AFTER_10MIN))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}

	/***
	 * Убрать НПЦ, которые должны изчезать через 10 минут после начала осады
	 */
	public void despawnSiege10MinMerchants()
	{
		if(!_isSuspiciousMerchantSpawned)
		{
			return;
		}
		_isSuspiciousMerchantSpawned = false;

		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.DESPAWN_ON_SIEGE_AFTER_10MIN))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				spawnDat.stopRespawn();
				spawnDat.getLastSpawn().getLocationController().delete();
			}
		}
	}

	/***
	 * Заспаунить НПЦ, которые пропадают при начале осады
	 */
	public void spawnOnSiegeDespawnNpcs()
	{
		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.DESPAWN_ON_SIEGE))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}

	/***
	 * Убрать НПЦ, которые должны изчезать перед началом осады
	 */
	public void despawnOnSiegeDespawnNpcs()
	{
		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.DESPAWN_ON_SIEGE))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				spawnDat.stopRespawn();
				spawnDat.getLastSpawn().getLocationController().delete();
			}
		}
	}

	/***
	 * Заспаунить НПЦ, которые должны появляться после осады
	 */
	public void spawnOnSiegeEndNpcs()
	{
		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.SPAWN_AFTER_SIEGE))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}

	/***
	 * Убрать НПЦ, которые должны появляться после осады
	 */
	public void despawnOnSiegeEndNpcs()
	{
		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.SPAWN_AFTER_SIEGE))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				spawnDat.stopRespawn();
				spawnDat.getLastSpawn().getLocationController().delete();
			}
		}
	}

	/***
	 * Заспаунить НПЦ, которые находятся в форте постоянно
	 */
	private void spawnFortCitizens()
	{
		for(FortFacilitySpawnHolder holder : FortSpawnList.getInstance().getSpawnForFort(this, FortSpawnType.SPAWN_ALWAYS))
		{
			for(L2Spawn spawnDat : holder.getSpawnList())
			{
				SpawnTable.getInstance().addNewSpawn(spawnDat);
				spawnDat.doSpawn();
				spawnDat.startRespawn();
			}
		}
	}

	public FastList<L2Skill> getResidentialSkills()
	{
		return _residentialSkills;
	}

	public void giveResidentialSkills(L2PcInstance player)
	{
		if(_residentialSkills != null && !_residentialSkills.isEmpty())
		{
			for(L2Skill sk : _residentialSkills)
			{
				player.addSkill(sk, false);
			}
		}
	}

	public void removeResidentialSkills(L2PcInstance player)
	{
		if(_residentialSkills != null && !_residentialSkills.isEmpty())
		{
			for(L2Skill sk : _residentialSkills)
			{
				player.removeSkill(sk, false, true);
			}
		}
	}

	/***
	 * Загрузка апгрейдов фортов из базы
	 */
	private void loadFacilities()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Fortress.FACILITY_SELECT);
			statement.setInt(1, _fortId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				_facilities.put(FortFacilityType.values()[rs.getInt("facility_type")], rs.getInt("facility_level"));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Fort.loadFacilities(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/***
	 * Добавить текущему форту возможность (апгрейд)
	 * @param type тип апгрейда
	 * @param level уровень апгрейда
	 */
	public void setFacilityLevel(FortFacilityType type, int level)
	{
		if(_facilities.containsKey(type))
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Fortress.FACILITY_UPDATE);
				statement.setInt(1, level);
				statement.setInt(2, _fortId);
				statement.setInt(3, type.ordinal());
				statement.executeUpdate();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: setFacilityLevel().UPDATE : " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
		else
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Fortress.FACILITY_INSERT);
				statement.setInt(1, _fortId);
				statement.setInt(2, type.ordinal());
				statement.setLong(3, level);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: setFacilityLevel().INSERT", e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
		_facilities.put(type, level);
	}

	/***
	 * Удаление всех апгрейдов у форта
	 */
	public void resetFacilityData()
	{
		_facilities.clear();
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Fortress.FACILITY_DELETE);
			statement.setInt(1, _fortId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: resetFacilityData(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/***
	 * @param type тип апгрейда
	 * @return уровень указанного типа апгрейда в текущем форте
	 */
	public int getFacilityLevel(FortFacilityType type)
	{
		if(_facilities.get(type) == null)
		{
			return 0;
		}

		return _facilities.get(type);
	}

	/***
	 * @return {@code true} если контрольная рубка была обесточена
	 */
	public boolean isControlRoomDeactivated()
	{
		return _isControlRoomDeactivated;
	}

	/***
	 * Выставляет состояние подачи электричества в контрольную рубку
	 * @param isControlRoomDeactivated {@code true}/{@code false}
	 */
	public void setControlRoomDeactivated(boolean isControlRoomDeactivated)
	{
		_isControlRoomDeactivated = isControlRoomDeactivated;

		// Проверяем условия для спауна знамени
		if(getSiege().isInProgress())
		{
			getSiege().checkToSpawnFlag();
		}
	}

	@Override
	public String toString()
	{
		return _name;
	}

	public static class ScheduleSpecialEnvoysDeSpawn implements Runnable
	{
		private Fort _fortInst;

		public ScheduleSpecialEnvoysDeSpawn(Fort pFort)
		{
			_fortInst = pFort;
		}

		@Override
		public void run()
		{
			try
			{
				// Если владельцы фортов не решили, выставляет статус форта как независимый
				if(_fortInst.getFortState() == FortState.NOT_DECIDED)
				{
					_fortInst.setFortState(FortState.INDEPENDENT, 0);
					_fortInst.save();
				}
				_fortInst.despawnOnSiegeEndNpcs();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: ScheduleSpecialEnvoysSpawn() for Fort " + _fortInst.getName() + ": " + e.getMessage(), e);
			}
		}
	}

	private static class endFortressSiege implements Runnable
	{
		private Fort _fort;
		private L2Clan _clan;

		public endFortressSiege(Fort f, L2Clan clan)
		{
			_fort = f;
			_clan = clan;
		}

		@Override
		public void run()
		{
			try
			{
				_fort.engrave(_clan);
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception in endFortressSiege " + e.getMessage(), e);
			}
		}
	}

	public class FortFunction
	{
		private long _expire;
		private FunctionData _deco;
		private ScheduledFuture _task;

		public FortFunction(FunctionData d, long expire)
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

				statement = con.prepareStatement(Fortress.FUNCTION_UPDATE);
				statement.setLong(1, _expire);
				statement.setInt(2, _fortId);
				statement.setInt(3, _deco.getId());
				statement.executeUpdate();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: Fort.updateDecoTime", e);
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
			if(_fortOwner == null || _fortOwner.getWarehouse().getAdenaCount() < _deco.getFunctionCostData().getCost())
			{
				removeFunction(_deco.getType());
			}
			else
			{
				_fortOwner.getWarehouse().destroyItemByItemId(ProcessType.CLAN, PcInventory.ADENA_ID, _deco.getFunctionCostData().getCost(), null, null);
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

	private class ForEachPlayerSendMessage implements TObjectProcedure<L2PcInstance>
	{
		SystemMessage _sm;

		private ForEachPlayerSendMessage(SystemMessage sm)
		{
			_sm = sm;
		}

		@Override
		public boolean execute(L2PcInstance character)
		{
			character.sendPacket(_sm);
			return true;
		}
	}
}