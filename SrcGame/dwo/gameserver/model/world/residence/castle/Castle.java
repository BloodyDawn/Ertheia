package dwo.gameserver.model.world.residence.castle;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.queries.Castles;
import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.datatables.xml.ResidenceFunctionData;
import dwo.gameserver.datatables.xml.SkillTreesData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager.CropProcure;
import dwo.gameserver.instancemanager.castle.CastleManorManager.SeedProduction;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2ArtefactInstance;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.world.npc.spawn.CastleTowerSpawn;
import dwo.gameserver.model.world.residence.function.FunctionData;
import dwo.gameserver.model.world.residence.function.FunctionType;
import dwo.gameserver.model.world.zone.type.L2CastleZone;
import dwo.gameserver.model.world.zone.type.L2ResidenceTeleportZone;
import dwo.gameserver.model.world.zone.type.L2SiegeZone;
import dwo.gameserver.network.game.serverpackets.PlaySound;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

public class Castle
{
	protected static final Logger _log = LogManager.getLogger(Castle.class);
	// Константы
	private static final long ONE_DAY = 24 * 60 * 60 * 1000;
	private static long ONE_WEEK = 7 * ONE_DAY;
	private List<CropProcure> _procure = new ArrayList<>();
	private List<SeedProduction> _production = new ArrayList<>();
	private List<CropProcure> _procureNext = new ArrayList<>();
	private List<SeedProduction> _productionNext = new ArrayList<>();
	private boolean _isNextPeriodApproved;
	private int _castleId;
	private L2DoorInstance[] _doors;
	private String _name = "";
	private int _ownerId;
	private CastleSiegeEngine _castleSiegeEngine;
	private Calendar _siegeDate;
	private boolean _isTimeRegistrationOver = true; // true if Castle Lords set the time, or 24h is elapsed after the siege
	private Calendar _siegeTimeRegistrationEndDate; // last siege end date + 1 day
	private long _treasury;
	private boolean _showNpcCrest;
	private L2SiegeZone _zone;
	private L2CastleZone _castleZone;
	private L2ResidenceTeleportZone _teleZone;
	private L2Clan _formerOwner;
	private List<L2ArtefactInstance> _artefacts = new ArrayList<>(1);
	private TIntIntHashMap _engrave = new TIntIntHashMap(1);
	private Map<FunctionType, CastleFunction> _functions;
	private FastList<L2Skill> _residentialSkills = new FastList<>();
	private CastleSide _castleSide = CastleSide.LIGHT;

	public Castle(int castleId)
	{
		_castleId = castleId;
		load();
		_doors = DoorGeoEngine.getInstance().getCastleDoors(castleId);
		_functions = new FastMap<>();

		List<L2SkillLearn> residentialSkills = SkillTreesData.getInstance().getAvailableResidentialSkills(castleId, _castleSide);
		for(L2SkillLearn s : residentialSkills)
		{
			L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());
			if(sk != null)
			{
				_residentialSkills.add(sk);
			}
			else
			{
				_log.log(Level.WARN, "Castle Id: " + castleId + " has a null residential skill Id: " + s.getSkillId() + " level: " + s.getSkillLevel() + '!');
			}
		}
		if(_ownerId != 0)
		{
			loadFunctions();
		}
	}

	/**
	 * @param type тип
	 * @return функцию с указанным ID
	 */
	public CastleFunction getFunction(FunctionType type)
	{
		return _functions.get(type);
	}

	/***
	 * @param type тип функции
	 * @return уровень указанной функции у замка
	 */
	public int getFunctionLevel(FunctionType type)
	{
		CastleFunction function = _functions.get(type);
		if(function == null)
		{
			return 0;
		}
		return function.getFunctionData().getLevel();
	}

	public void engrave(L2Clan clan, L2Object target, CastleSide takeSide)
	{
		synchronized(this)
		{
			if(target instanceof L2ArtefactInstance && !_artefacts.contains(target))
			{
				return;
			}
			_engrave.put(target.getObjectId(), clan.getClanId());
			if(_engrave.size() == _artefacts.size())
			{
				for(L2ArtefactInstance art : _artefacts)
				{
					if(_engrave.get(art.getObjectId()) != clan.getClanId())
					{
						return;
					}
				}
				_engrave.clear();
				setOwner(clan);
				setCastleSide(takeSide, true);
			}
		}
	}

	/**
	 * Добавить указанное количество адены в казну замка
	 * @param amount количество адены
	 */
	public void addToTreasury(long amount)
	{
		if(_ownerId <= 0)
		{
			return;
		}

		if(_name.equalsIgnoreCase("Schuttgart") || _name.equalsIgnoreCase("Goddard"))
		{
			Castle rune = CastleManager.getInstance().getCastle("rune");
			if(rune != null)
			{
				long runeTax = (long) (amount * rune.getTaxRate());
				if(rune._ownerId > 0)
				{
					rune.addToTreasury(runeTax);
				}
				amount -= runeTax;
			}
		}
		if(!_name.equalsIgnoreCase("aden") && !_name.equalsIgnoreCase("Rune") && !_name.equalsIgnoreCase("Schuttgart") && !_name.equalsIgnoreCase("Goddard")) // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
		{
			Castle aden = CastleManager.getInstance().getCastle("aden");
			if(aden != null)
			{
				long adenTax = (long) (amount * aden.getTaxRate()); // Find out what Aden gets from the current castle instance's income
				if(aden._ownerId > 0)
				{
					aden.addToTreasury(adenTax); // Only bother to really add the tax to the treasury if not npc owned
				}

				amount -= adenTax; // Subtract Aden's income from current castle instance's income
			}
		}

		addToTreasuryNoTax(amount);
	}

	/***
	 * Добавить указанное количество Адены в казну замка
	 * @param amount количество Адена
	 * @return {@code true} если операция прошла успешна
	 */
	public boolean addToTreasuryNoTax(long amount)
	{
		if(_ownerId <= 0)
		{
			return false;
		}

		if(amount < 0)
		{
			amount *= -1;
			if(_treasury < amount)
			{
				return false;
			}
			_treasury -= amount;
		}
		else
		{
			if(_treasury + amount > PcInventory.MAX_ADENA) // TODO is this valid after gracia final?
			{
				_treasury = PcInventory.MAX_ADENA;
			}
			else
			{
				_treasury += amount;
			}
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.UPDATE_TREASURY);
			statement.setLong(1, _treasury);
			statement.setInt(2, _castleId);
			statement.execute();
		}
		catch(Exception ignored)
		{
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
		return true;
	}

	/**
	 * Move non clan members off castle area and to nearest town.
	 */
	public void banishForeigners()
	{
		getCastleZone().banishForeigners(_ownerId);
	}

	/**
	 * @return {@code true} if object is inside the zone
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
				if(zone.getSiegeObjectId() == _castleId)
				{
					_zone = zone;
					break;
				}
			}
		}
		return _zone;
	}

	public L2CastleZone getCastleZone()
	{
		if(_castleZone == null)
		{
			for(L2CastleZone zone : ZoneManager.getInstance().getAllZones(L2CastleZone.class))
			{
				if(zone.getCastleId() == _castleId)
				{
					_castleZone = zone;
					break;
				}
			}
		}
		return _castleZone;
	}

	public L2ResidenceTeleportZone getTeleZone()
	{
		if(_teleZone == null)
		{
			for(L2ResidenceTeleportZone zone : ZoneManager.getInstance().getAllZones(L2ResidenceTeleportZone.class))
			{
				if(zone.getResidenceId() == _castleId)
				{
					_teleZone = zone;
					break;
				}
			}
		}
		return _teleZone;
	}

	public void oustAllPlayers()
	{
		getTeleZone().oustAllPlayers();
	}

	/**
	 * @param obj проверяемый обьект
	 * @return расстояние от указанного обьекта до зоны замка
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
		if(activeChar.getClanId() != _ownerId)
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

	/***
	 * Удаление всех апгрейдов замка (сброс по-умолчанию)
	 */
	public void removeAllUpgrades()
	{
		removeAllDoorUpgrades();
		removeTrapUpgrades();
		_functions.keySet().forEach(this::removeFunction);
		_functions.clear();
	}

	/***
	 * Назначить владельцем замка указанный клан
	 * @param clan назначаемый владельцем клан
	 */
	public void setOwner(L2Clan clan)
	{
		// Удаляем старого владельца замка
		if(_ownerId > 0 && (clan == null || clan.getClanId() != _ownerId))
		{
			L2Clan oldOwner = ClanTable.getInstance().getClan(_ownerId); // Try to find clan instance
			if(oldOwner != null)
			{
				if(_formerOwner == null)
				{
					_formerOwner = oldOwner;
					if(Config.REMOVE_CASTLE_CIRCLETS)
					{
						CastleManager.getInstance().removeCirclet(_formerOwner, _castleId);
					}

					// Удаляем у проигравшего замок клана плащи
					CastleManager.getInstance().removeCloaks(_formerOwner);
				}
				try
				{
					L2PcInstance oldleader = oldOwner.getLeader().getPlayerInstance();
					if(oldleader != null)
					{
						if(oldleader.getMountType() == 2)
						{
							oldleader.dismount();
						}
					}
				}
				catch(Exception e)
				{
					_log.log(Level.ERROR, "Exception in setOwner: " + e.getMessage(), e);
				}
				oldOwner.setCastleId(0); // Unset has castle flag for old owner
				for(L2PcInstance member : oldOwner.getOnlineMembers(0))
				{
					removeResidentialSkills(member);
					member.sendSkillList();
				}
			}
		}

		updateOwnerInDB(clan); // Update in database
		setShowNpcCrest(false);

		// Если у клана есть крепость - забираем ее
		if(clan != null && clan.getFortId() > 0)
		{
			FortManager.getInstance().getFortByOwner(clan).removeOwner();
		}

		if(getSiege().isInProgress()) // If siege in progress
		{
			getSiege().midVictory(); // Mid victory phase of siege
		}

		if(clan != null)
		{
			for(L2PcInstance member : clan.getOnlineMembers(0))
			{
				giveResidentialSkills(member);
				member.sendSkillList();
			}
		}
	}

	public void removeOwner(L2Clan clan)
	{
		if(clan != null)
		{
			_formerOwner = clan;
			if(Config.REMOVE_CASTLE_CIRCLETS)
			{
				CastleManager.getInstance().removeCirclet(_formerOwner, _castleId);
			}
			for(L2PcInstance member : clan.getOnlineMembers(0))
			{
				removeResidentialSkills(member);
				member.sendSkillList();
			}
			clan.setCastleId(0);
			clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
		}

		updateOwnerInDB(null);
		if(getSiege().isInProgress())
		{
			getSiege().midVictory();
		}

		_functions.keySet().forEach(this::removeFunction);
		_functions.clear();
	}

	/**
	 * Respawn all doors on castle grounds
	 */
	public void spawnDoor()
	{
		spawnDoor(false);
	}

	/**
	 * Respawn all doors on castle grounds
	 *
	 * @param isDoorWeak
	 */
	public void spawnDoor(boolean isDoorWeak)
	{
		for(int i = 0; i < _doors.length; i++)
		{
			L2DoorInstance door = _doors[i];
			if(door.getCurrentHp() <= 0)
			{
				door.getLocationController().decay(); // Kill current if not killed already
				door = DoorGeoEngine.getInstance().newInstance(door.getDoorId(), true);
				if(isDoorWeak)
				{
					door.setCurrentHp(door.getMaxHp() / 2);
				}
				door.getLocationController().spawn(door.getX(), door.getY(), door.getZ());
				_doors[i] = door;
			}
			else if(door.isOpened())
			{
				door.closeMe();
			}
		}
		loadDoorUpgrade(); // Check for any upgrade the doors may have
	}

	public void setDoorHpLevel(int doorId, int lv)
	{
		getDoor(doorId).setHpLevel(lv);
		saveDoorUpgrade(doorId, lv);
	}

	/***
	 * Загрузка замка из базы данных
	 */
	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Castles.LOAD);
			statement.setInt(1, _castleId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				_name = rs.getString("name");
				_siegeDate = Calendar.getInstance();
				_siegeDate.setTimeInMillis(rs.getLong("siegeDate"));
				_siegeTimeRegistrationEndDate = Calendar.getInstance();
				_siegeTimeRegistrationEndDate.setTimeInMillis(rs.getLong("regTimeEnd"));
				_isTimeRegistrationOver = rs.getBoolean("regTimeOver");
				_treasury = rs.getLong("treasury");
				_showNpcCrest = rs.getBoolean("showNpcCrest");
				_castleSide = CastleSide.valueOf(rs.getString("side"));
			}
			statement.clearParameters();

			statement = con.prepareStatement(Castles.LOAD_OWNER_CLANID);
			statement.setInt(1, _castleId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				_ownerId = rs.getInt("clan_id");
			}

			if(_ownerId > 0)
			{
				L2Clan clan = ClanTable.getInstance().getClan(_ownerId); // Try to find clan instance
				ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: loadCastleData(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
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

			statement = con.prepareStatement(Castles.FUNCTION_SELECT);
			statement.setInt(1, _castleId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				FunctionData deco = ResidenceFunctionData.getInstance().getDeco(rs.getInt("deco_id"));
				_functions.put(deco.getType(), new CastleFunction(deco, rs.getLong("endTime")));
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Castle.loadFunctions(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/**
	 * Удаление функции замка из списка и из базы
	 * @param type тип функции замка
	 */
	public void removeFunction(FunctionType type)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.FUNCTION_DELETE);
			statement.setInt(1, _castleId);
			statement.setInt(2, _functions.get(type).getFunctionData().getId());
			statement.execute();
			_functions.get(type).stopTask();
			_functions.remove(type);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: Castle.removeFunctions(int functionType): " + e.getMessage(), e);
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
				statement = con.prepareStatement(Castles.FUNCTION_DELETE);
				statement.setInt(1, _castleId);
				statement.setInt(2, _functions.get(type).getFunctionData().getId());
				statement.execute();
				_functions.get(type).stopTask();
				_functions.remove(type);
			}

			FunctionData deco = ResidenceFunctionData.getInstance().getDeco(type, level);
			long time = System.currentTimeMillis() + deco.getFunctionCostData().getDays() * ONE_DAY;

			statement = con.prepareStatement(Castles.FUNCTION_INSERT);
			statement.setInt(1, _castleId);
			statement.setInt(2, deco.getId());
			statement.setLong(3, time);
			statement.execute();

			_functions.put(type, new CastleFunction(deco, time));
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
	 * Загрузка апгрейдов дверей из базы данных
	 */
	private void loadDoorUpgrade()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rs = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.DOORUPGRADE_SELECT);
			statement.setInt(1, _castleId);
			rs = statement.executeQuery();

			while(rs.next())
			{
				int id = rs.getInt("id");
				int lv = rs.getInt("lv");

				L2DoorInstance door = DoorGeoEngine.getInstance().getDoor(id);
				if(door != null)
				{
					door.setHpLevel(lv);
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while loadCastleDoorUpgrade()", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rs);
		}
	}

	/***
	 * Удаление апгрейдов дверей из базы данных
	 */
	private void removeAllDoorUpgrades()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.DOORUPGRADE_DELETE);
			statement.setInt(1, _castleId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while removeAllDoorUpgrades()", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/***
	 * Сохранение апгрейда двери в базу данных
	 * @param doorId ID двери
	 * @param level уровень апгрейда двери
	 */
	private void saveDoorUpgrade(int doorId, int level)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;

		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.DOORUPGRADE_INSERTUPDATE);
			statement.setInt(1, _castleId);
			statement.setInt(2, doorId);
			statement.setInt(3, level);
			statement.setInt(4, level);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error while saveDoorUpgrade()");
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void updateOwnerInDB(L2Clan clan)
	{
		if(clan != null)
		{
			_ownerId = clan.getClanId(); // Update owner id property
		}
		else
		{
			_ownerId = 0; // Remove owner
			resetManor();
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			// ============================================================================
			// NEED TO REMOVE HAS CASTLE FLAG FROM CLAN_DATA
			// SHOULD BE CHECKED FROM CASTLE TABLE
			statement = con.prepareStatement(Castles.DELETE_OWNER_CLANID);
			statement.setInt(1, _castleId);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement(Castles.UPDATE_OWNER_CLANID);
			statement.setInt(1, _castleId);
			statement.setInt(2, _ownerId);
			statement.execute();
			// ============================================================================

			// Announce to clan memebers
			if(clan != null)
			{
				clan.setCastleId(_castleId); // Set has castle flag for new owner
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				clan.broadcastToOnlineMembers(new PlaySound(1, "Siege_Victory", 0, 0, 0, 0, 0));
				ThreadPoolManager.getInstance().scheduleGeneral(new CastleUpdater(clan, 1), 3600000); // Schedule owner tasks to start running
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: updateOwnerInDB(L2Clan clan): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public int getCastleId()
	{
		return _castleId;
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

	/***
	 * @param processType тип операции
	 * @return налог в процентах, который отойдет замку при совершении указанной операции
	 */
	public double getTaxRate(ProcessType processType)
	{
		switch(_castleSide)
		{
			case DARK:
				return processType == ProcessType.BUY ? 0.3 : 0.1;
		}
		return 0;
	}

	public int getTaxPercent()
	{
		return (int) (getTaxRate(ProcessType.BUY) * 100);
	}

	public double getTaxRate()
	{
		return 1 + getTaxRate(ProcessType.BUY);
	}

	public String getName()
	{
		return _name;
	}

	public int getOwnerId()
	{
		return _ownerId;
	}

	public CastleSiegeEngine getSiege()
	{
		if(_castleSiegeEngine == null)
		{
			_castleSiegeEngine = new CastleSiegeEngine(this);
		}
		return _castleSiegeEngine;
	}

	public Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public boolean getIsTimeRegistrationOver()
	{
		return _isTimeRegistrationOver;
	}

	public void setIsTimeRegistrationOver(boolean val)
	{
		_isTimeRegistrationOver = val;
	}

	public Calendar getTimeRegistrationOverDate()
	{
		if(_siegeTimeRegistrationEndDate == null)
		{
			_siegeTimeRegistrationEndDate = Calendar.getInstance();
		}
		return _siegeTimeRegistrationEndDate;
	}

	public long getTreasury()
	{
		return _treasury;
	}

	public boolean getShowNpcCrest()
	{
		return _showNpcCrest;
	}

	public void setShowNpcCrest(boolean showNpcCrest)
	{
		if(_showNpcCrest != showNpcCrest)
		{
			_showNpcCrest = showNpcCrest;
			updateShowNpcCrest();
		}
	}

	public List<SeedProduction> getSeedProduction(int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? _production : _productionNext;
	}

	public List<CropProcure> getCropProcure(int period)
	{
		return period == CastleManorManager.PERIOD_CURRENT ? _procure : _procureNext;
	}

	public void setSeedProduction(List<SeedProduction> seed, int period)
	{
		if(period == CastleManorManager.PERIOD_CURRENT)
		{
			_production = seed;
		}
		else
		{
			_productionNext = seed;
		}
	}

	public void setCropProcure(List<CropProcure> crop, int period)
	{
		if(period == CastleManorManager.PERIOD_CURRENT)
		{
			_procure = crop;
		}
		else
		{
			_procureNext = crop;
		}
	}

	public SeedProduction getSeed(int seedId, int period)
	{
		for(SeedProduction seed : getSeedProduction(period))
		{
			if(seed.getId() == seedId)
			{
				return seed;
			}
		}
		return null;
	}

	public CropProcure getCrop(int cropId, int period)
	{
		for(CropProcure crop : getCropProcure(period))
		{
			if(crop.getId() == cropId)
			{
				return crop;
			}
		}
		return null;
	}

	public long getManorCost(int period)
	{
		List<CropProcure> procure;
		List<SeedProduction> production;

		if(period == CastleManorManager.PERIOD_CURRENT)
		{
			procure = _procure;
			production = _production;
		}
		else
		{
			procure = _procureNext;
			production = _productionNext;
		}

		long total = 0;
		if(production != null)
		{
			for(SeedProduction seed : production)
			{
				total += ManorData.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
			}
		}
		if(procure != null)
		{
			for(CropProcure crop : procure)
			{
				total += crop.getPrice() * crop.getStartAmount();
			}
		}
		return total;
	}

	//save manor production data
	public void saveSeedData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.MANOR_DELETE_PRODUCTION);
			statement.setInt(1, _castleId);
			statement.execute();
			statement.clearParameters();

			if(_production != null)
			{
				int count = 0;
				StringBuilder query = new StringBuilder();
				query.append("INSERT INTO castle_manor_production VALUES "); // TODO: В queries
				String[] values = new String[_production.size()];
				for(SeedProduction s : _production)
				{
					values[count++] = "(" + _castleId + ',' + s.getId() + ',' + s.getCanProduce() + ',' + s.getStartProduce() + ',' + s.getPrice() + ',' + CastleManorManager.PERIOD_CURRENT + ')';
				}
				if(values.length > 0)
				{
					query.append(values[0]);
					for(int i = 1; i < values.length; i++)
					{
						query.append(',');
						query.append(values[i]);
					}
					statement = con.prepareStatement(query.toString());
					statement.execute();
					statement.clearParameters();
				}
			}

			if(_productionNext != null)
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_production VALUES "; // TODO: В queries
				String[] values = new String[_productionNext.size()];
				for(SeedProduction s : _productionNext)
				{
					values[count++] = "(" + _castleId + ',' + s.getId() + ',' + s.getCanProduce() + ',' + s.getStartProduce() + ',' + s.getPrice() + ',' + CastleManorManager.PERIOD_NEXT + ')';
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; i++)
					{
						query += ',' + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
					statement.clearParameters();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error adding seed production data for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	//save manor production data for specified period
	public void saveSeedData(int period)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Castles.MANOR_DELETE_PRODUCTION_PERIOD);
			statement.setInt(1, _castleId);
			statement.setInt(2, period);
			statement.execute();
			statement.clearParameters();

			List<SeedProduction> prod = null;
			prod = getSeedProduction(period);

			if(prod != null)
			{
				int count = 0;
				StringBuilder query = new StringBuilder();
				query.append("INSERT INTO castle_manor_production VALUES "); // TODO: В queries
				String[] values = new String[prod.size()];
				for(SeedProduction s : prod)
				{
					values[count++] = "(" + _castleId + ',' + s.getId() + ',' + s.getCanProduce() + ',' + s.getStartProduce() + ',' + s.getPrice() + ',' + period + ')';
				}
				if(values.length > 0)
				{
					query.append(values[0]);
					for(int i = 1; i < values.length; i++)
					{
						query.append(',').append(values[i]);
					}
					statement = con.prepareStatement(query.toString());
					statement.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error adding seed production data for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	//save crop procure data
	public void saveCropData()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Castles.MANOR_DELETE_PROCURE);
			statement.setInt(1, _castleId);
			statement.execute();
			statement.clearParameters();
			if(_procure != null && !_procure.isEmpty())
			{
				int count = 0;
				StringBuilder query = new StringBuilder();
				query.append("INSERT INTO castle_manor_procure VALUES "); // TODO: В queries
				String[] values = new String[_procure.size()];
				for(CropProcure cp : _procure)
				{
					values[count++] = "(" + _castleId + ',' + cp.getId() + ',' + cp.getAmount() + ',' + cp.getStartAmount() + ',' + cp.getPrice() + ',' + cp.getReward() + ',' + CastleManorManager.PERIOD_CURRENT + ')';
				}
				if(values.length > 0)
				{
					query.append(values[0]);
					for(int i = 1; i < values.length; i++)
					{
						query.append(',');
						query.append(values[i]);
					}
					statement = con.prepareStatement(query.toString());
					statement.execute();
					statement.clearParameters();
				}
			}
			if(_procureNext != null && !_procureNext.isEmpty())
			{
				int count = 0;
				String query = "INSERT INTO castle_manor_procure VALUES "; // TODO: В queries
				String[] values = new String[_procureNext.size()];
				for(CropProcure cp : _procureNext)
				{
					values[count++] = "(" + _castleId + ',' + cp.getId() + ',' + cp.getAmount() + ',' + cp.getStartAmount() + ',' + cp.getPrice() + ',' + cp.getReward() + ',' + CastleManorManager.PERIOD_NEXT + ')';
				}
				if(values.length > 0)
				{
					query += values[0];
					for(int i = 1; i < values.length; i++)
					{
						query += ',' + values[i];
					}
					statement = con.prepareStatement(query);
					statement.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error adding crop data for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	//	save crop procure data for specified period
	public void saveCropData(int period)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Castles.MANOR_DELETE_PROCURE_PERIOD);
			statement.setInt(1, _castleId);
			statement.setInt(2, period);
			statement.execute();
			statement.clearParameters();

			List<CropProcure> proc = null;
			proc = getCropProcure(period);

			if(proc != null && !proc.isEmpty())
			{
				int count = 0;
				StringBuilder query = new StringBuilder();
				query.append("INSERT INTO castle_manor_procure VALUES "); // TODO: В queries
				String[] values = new String[proc.size()];

				for(CropProcure cp : proc)
				{
					values[count++] = "(" + _castleId + ',' + cp.getId() + ',' + cp.getAmount() + ',' + cp.getStartAmount() + ',' + cp.getPrice() + ',' + cp.getReward() + ',' + period + ')';
				}
				if(values.length > 0)
				{
					query.append(values[0]);
					for(int i = 1; i < values.length; i++)
					{
						query.append(',');
						query.append(values[i]);
					}
					statement = con.prepareStatement(query.toString());
					statement.execute();
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error adding crop data for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void updateCrop(int cropId, long amount, int period)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.UPDATE_CROP);
			statement.setLong(1, amount);
			statement.setInt(2, cropId);
			statement.setInt(3, _castleId);
			statement.setInt(4, period);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error adding crop data for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void updateSeed(int seedId, long amount, int period)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Castles.UPDATE_SEED);
			statement.setLong(1, amount);
			statement.setInt(2, seedId);
			statement.setInt(3, _castleId);
			statement.setInt(4, period);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error adding seed production data for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public boolean isNextPeriodApproved()
	{
		return _isNextPeriodApproved;
	}

	public void setNextPeriodApproved(boolean val)
	{
		_isNextPeriodApproved = val;
	}

	public void updateClansReputation()
	{
		if(_formerOwner != null)
		{
			if(_formerOwner.equals(ClanTable.getInstance().getClan(_ownerId)))
			{
				_formerOwner.addReputationScore(Config.CASTLE_DEFENDED_POINTS, true);
			}
			else
			{
				int maxreward = Math.max(0, _formerOwner.getReputationScore());
				_formerOwner.takeReputationScore(Config.LOOSE_CASTLE_POINTS, true);
				L2Clan owner = ClanTable.getInstance().getClan(_ownerId);
				if(owner != null)
				{
					owner.addReputationScore(Math.min(Config.TAKE_CASTLE_POINTS, maxreward), true);
				}
			}
		}
		else
		{
			L2Clan owner = ClanTable.getInstance().getClan(_ownerId);
			if(owner != null)
			{
				owner.addReputationScore(Config.TAKE_CASTLE_POINTS, true);
			}
		}
	}

	public void updateShowNpcCrest()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.UPDATE_NPC_STRING);
			statement.setString(1, String.valueOf(_showNpcCrest));
			statement.setInt(2, _castleId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error saving showNpcCrest for castle " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public FastList<L2Skill> getResidentialSkills()
	{
		return _residentialSkills;
	}

	public void giveResidentialSkills(L2PcInstance player)
	{
		for(L2Skill sk : _residentialSkills)
		{
			player.addSkill(sk, false);
		}
	}

	public void removeResidentialSkills(L2PcInstance player)
	{
		for(L2Skill sk : _residentialSkills)
		{
			player.removeSkill(sk, false, true);
		}
	}

	/**
	 * Register Artefact to castle
	 * @param artefact
	 */
	public void registerArtefact(L2ArtefactInstance artefact)
	{
		_artefacts.add(artefact);
	}

	public List<L2ArtefactInstance> getArtefacts()
	{
		return _artefacts;
	}

	public void resetManor()
	{
		setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
		setCropProcure(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
		setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_CURRENT);
		setSeedProduction(new ArrayList<>(), CastleManorManager.PERIOD_NEXT);
		if(Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			saveCropData();
			saveSeedData();
		}
	}

	/**
	 * Обновляет в базе текущую сторону замка
	 * @param castleSide {@link CastleSide} сторона замка
	 */
	public void updateCastleSide(CastleSide castleSide)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.UPDATE_SIDE);
			statement.setString(1, castleSide.toString());
			statement.setInt(2, _castleId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error updating Castle Side " + _name + ": " + e.getMessage());
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * @return {@link CastleSide} текущего замка
	 */
	public CastleSide getCastleSide()
	{
		return _castleSide;
	}

	/**
	 * Устанавливает сторону замка
	 * @param side {@link CastleSide} сторона замка
	 */
	public void setCastleSide(CastleSide side, boolean saveToBase)
	{
		_castleSide = side;
		if(saveToBase)
		{
			updateCastleSide(_castleSide);
		}
	}

	public int getTrapUpgradeLevel(int towerIndex)
	{
		CastleTowerSpawn spawn = CastleSiegeManager.getInstance().getFlameTowers(_castleId).get(towerIndex);
		return spawn != null ? spawn.getUpgradeLevel() : 0;
	}

	public void setTrapUpgrade(int towerIndex, int level, boolean save)
	{
		if(save)
		{
			ThreadConnection con = null;
			FiltredPreparedStatement statement = null;
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement(Castles.TRAPUPGRADE_REPLACE);
				statement.setInt(1, _castleId);
				statement.setInt(2, towerIndex);
				statement.setInt(3, level);
				statement.execute();
			}
			catch(Exception e)
			{
				_log.log(Level.ERROR, "Exception: setTrapUpgradeLevel(int towerIndex, int level, int castleId): " + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, statement);
			}
		}
		CastleTowerSpawn spawn = CastleSiegeManager.getInstance().getFlameTowers(_castleId).get(towerIndex);
		if(spawn != null)
		{
			spawn.setUpgradeLevel(level);
		}
	}

	private void removeTrapUpgrades()
	{
		for(CastleTowerSpawn ts : CastleSiegeManager.getInstance().getFlameTowers(_castleId))
		{
			ts.setUpgradeLevel(0);
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(Castles.TRAPUPGRADE_DELETE);
			statement.setInt(1, _castleId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Exception: removeTrapUpgrades(): " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public class CastleFunction
	{
		private long _expire;
		private FunctionData _deco;
		private ScheduledFuture _task;

		public CastleFunction(FunctionData d, long expire)
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

				statement = con.prepareStatement(Castles.FUNCTION_UPDATE);
				statement.setLong(1, _expire);
				statement.setInt(2, _castleId);
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