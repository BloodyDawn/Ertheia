package dwo.gameserver.model.world.worldstat;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.items.base.proptypes.SoulshotGrade;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.12.11
 * Time: 2:55
 */

public class WorldStatistic
{
	private static final Logger _log = LogManager.getLogger(WorldStatistic.class);
	protected final Map<CrystalGrade, Long> _weaponMaxEnchanted = new HashMap<>(10);
	protected final Map<CrystalGrade, Long> _armorMaxEnchanted = new HashMap<>(10);
	protected final Map<CrystalGrade, Long> _weaponTryToEnchant = new HashMap<>(10);
	protected final Map<CrystalGrade, Long> _armorTryToEnchant = new HashMap<>(10);
	protected final Map<SoulshotGrade, Long> _ssConsumed = new HashMap<>(6);
	protected final Map<SoulshotGrade, Long> _spsConsumed = new HashMap<>(6);
	// Рейды
	protected final Map<Integer, Long> _raidKilled = new HashMap<>(12);
	protected int _charId;
	// Обычный вид
	protected long _expAdded;
	protected long _adenaAdded;
	protected long _timePlayed;
	protected long _timeInBattle;
	protected long _timeInParty;
	protected long _timeInFullParty;
	protected long _privateSellsCount;
	protected long _questsCompleted;
	protected long _resurrectedCharCount;
	protected long _resurrectedByOtherCount;
	protected long _dieCount;
	// Охота
	protected long _monstersKilled;
	protected long _expFromMonsters;
	protected long _maxDamageToMonster;
	protected long _allDamageToMonster;
	protected long _allDamageFromMonster;
	protected long _killedByMonstersCount;
	// Пвп
	protected long _pkCount;
	protected long _pvpCount;
	protected long _killedByPkCount;
	protected long _killedInPvpCount;
	protected long _maxDamageToPc;
	protected long _allDamageToPc;
	protected long _allDamageFromPc;
	private boolean _isUpdateNeeded = true;

	public void setCharId(int charId)
	{
		_charId = charId;
	}

	public boolean isNeedToUpdate()
	{
		return _isUpdateNeeded;
	}

	public void setNeedToUpdate(boolean val)
	{
		_isUpdateNeeded = val;
	}

	/**
	 * Обновление статистики в базе данных
	 */
	public void updateStatsInDb()
	{
		if(!_isUpdateNeeded)
		{
			return;
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.updateStats(false));
			statement.setLong(1, _expAdded);
			statement.setLong(2, _adenaAdded);

			statement.setLong(3, _timePlayed);
			statement.setLong(4, _timeInBattle);
			statement.setLong(5, _timeInParty);
			statement.setLong(6, _timeInFullParty);

			statement.setLong(7, getWeaponEnchantMax(CrystalGrade.D));
			statement.setLong(8, getWeaponEnchantMax(CrystalGrade.C));
			statement.setLong(9, getWeaponEnchantMax(CrystalGrade.B));
			statement.setLong(10, getWeaponEnchantMax(CrystalGrade.A));
			statement.setLong(11, getWeaponEnchantMax(CrystalGrade.S));
			statement.setLong(12, getWeaponEnchantMax(CrystalGrade.S80));
			statement.setLong(13, getWeaponEnchantMax(CrystalGrade.R));
			statement.setLong(14, getWeaponEnchantMax(CrystalGrade.R95));
			statement.setLong(15, getWeaponEnchantMax(CrystalGrade.R99));

			statement.setLong(16, getArmorEnchantMax(CrystalGrade.D));
			statement.setLong(17, getArmorEnchantMax(CrystalGrade.C));
			statement.setLong(18, getArmorEnchantMax(CrystalGrade.B));
			statement.setLong(19, getArmorEnchantMax(CrystalGrade.A));
			statement.setLong(20, getArmorEnchantMax(CrystalGrade.S));
			statement.setLong(21, getArmorEnchantMax(CrystalGrade.S80));
			statement.setLong(22, getArmorEnchantMax(CrystalGrade.R));
			statement.setLong(23, getArmorEnchantMax(CrystalGrade.R95));
			statement.setLong(24, getArmorEnchantMax(CrystalGrade.R99));

			statement.setLong(25, getWeaponEnchantTry(CrystalGrade.D));
			statement.setLong(26, getWeaponEnchantTry(CrystalGrade.C));
			statement.setLong(27, getWeaponEnchantTry(CrystalGrade.B));
			statement.setLong(28, getWeaponEnchantTry(CrystalGrade.A));
			statement.setLong(29, getWeaponEnchantTry(CrystalGrade.S));
			statement.setLong(30, getWeaponEnchantTry(CrystalGrade.S80));
			statement.setLong(31, getWeaponEnchantTry(CrystalGrade.R));
			statement.setLong(32, getWeaponEnchantTry(CrystalGrade.R95));
			statement.setLong(33, getWeaponEnchantTry(CrystalGrade.R99));

			statement.setLong(34, getArmorEnchantTry(CrystalGrade.D));
			statement.setLong(35, getArmorEnchantTry(CrystalGrade.C));
			statement.setLong(36, getArmorEnchantTry(CrystalGrade.B));
			statement.setLong(37, getArmorEnchantTry(CrystalGrade.A));
			statement.setLong(38, getArmorEnchantTry(CrystalGrade.S));
			statement.setLong(39, getArmorEnchantTry(CrystalGrade.S80));
			statement.setLong(40, getArmorEnchantTry(CrystalGrade.R));
			statement.setLong(41, getArmorEnchantTry(CrystalGrade.R95));
			statement.setLong(42, getArmorEnchantTry(CrystalGrade.R99));

			statement.setLong(43, _privateSellsCount);
			statement.setLong(44, _questsCompleted);

			statement.setLong(45, getSsConsumed(SoulshotGrade.SS_D));
			statement.setLong(46, getSsConsumed(SoulshotGrade.SS_C));
			statement.setLong(47, getSsConsumed(SoulshotGrade.SS_B));
			statement.setLong(48, getSsConsumed(SoulshotGrade.SS_A));
			statement.setLong(49, getSsConsumed(SoulshotGrade.SS_S));
			statement.setLong(50, getSsConsumed(SoulshotGrade.SS_R));

			statement.setLong(51, getSpsConsumed(SoulshotGrade.SS_D));
			statement.setLong(52, getSpsConsumed(SoulshotGrade.SS_C));
			statement.setLong(53, getSpsConsumed(SoulshotGrade.SS_B));
			statement.setLong(54, getSpsConsumed(SoulshotGrade.SS_A));
			statement.setLong(55, getSpsConsumed(SoulshotGrade.SS_S));
			statement.setLong(56, getSpsConsumed(SoulshotGrade.SS_R));

			statement.setLong(57, _resurrectedCharCount);
			statement.setLong(58, _resurrectedByOtherCount);
			statement.setLong(59, _dieCount);
			statement.setLong(60, _monstersKilled);
			statement.setLong(61, _expFromMonsters);
			statement.setLong(62, _maxDamageToMonster);
			statement.setLong(63, _allDamageToMonster);
			statement.setLong(64, _allDamageFromMonster);
			statement.setLong(65, _killedByMonstersCount);
			statement.setLong(66, getEpicKilled(25774));
			statement.setLong(67, getEpicKilled(25785));
			statement.setLong(68, getEpicKilled(29195));
			statement.setLong(69, getEpicKilled(25779));
			statement.setLong(70, getEpicKilled(25866));
			statement.setLong(71, getEpicKilled(29194));
			statement.setLong(72, getEpicKilled(29218));
			statement.setLong(73, getEpicKilled(29213));
			statement.setLong(74, getEpicKilled(29196));
			statement.setLong(75, getEpicKilled(25867));
			statement.setLong(76, getEpicKilled(29212));
			statement.setLong(77, getEpicKilled(29197));

			statement.setLong(78, _pkCount);
			statement.setLong(79, _pvpCount);
			statement.setLong(80, _killedByPkCount);
			statement.setLong(81, _killedInPvpCount);
			statement.setLong(82, _maxDamageToPc);
			statement.setLong(83, _allDamageToPc);
			statement.setLong(84, _allDamageFromPc);

			statement.setLong(85, _charId);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "WorldStatisticsManager: Failed update char World Statistics.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_isUpdateNeeded = false;
		}
	}

	/**
	 * Создание пустой записи статистики для игрока
	 */
	public void insertStatsInDb()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.INSERT_STATS);
			statement.setLong(1, _charId);
			statement.execute();

			statement = con.prepareStatement(dwo.gameserver.datatables.sql.queries.WorldStatistic.INSERT_GENERAL_STATS);
			statement.setLong(1, _charId);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "WorldStatisticsManager: Failed inserting char World Statistics.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
			_isUpdateNeeded = false;
		}
	}

	public long getAdenaAdded()
	{
		return _adenaAdded;
	}

	public void setAdenaAdded(long adena)
	{
		_isUpdateNeeded = true;
		_adenaAdded += adena;
	}

	public long getTimePlayed()
	{
		return _timePlayed;
	}

	public void setTimePlayed(long time)
	{
		_isUpdateNeeded = true;
		_timePlayed = time;
	}

	public long getTimeInBattle()
	{
		return _timeInBattle;
	}

	public void setTimeInBattle(long time)
	{
		_isUpdateNeeded = true;
		_timeInBattle += time;
	}

	public long getTimeInParty()
	{
		return _timeInParty;
	}

	public void setTimeInParty(long time)
	{
		_isUpdateNeeded = true;
		_timeInParty += time;
	}

	public long getTimeInFullParty()
	{
		return _timeInFullParty;
	}

	public void setTimeInFullParty(long time)
	{
		_isUpdateNeeded = true;
		_timeInFullParty += time;
	}

	public long getPrivateSellCount()
	{
		return _privateSellsCount;
	}

	public void setPrivateSellCount(long count)
	{
		_isUpdateNeeded = true;
		_privateSellsCount += count;
	}

	public long getQuestsCompleted()
	{
		return _questsCompleted;
	}

	public void setQuestsCompleted(long count)
	{
		_isUpdateNeeded = true;
		_questsCompleted += count;
	}

	public long getResurrectedCharCount()
	{
		return _resurrectedCharCount;
	}

	public void setResurrectedCharCount(long count)
	{
		_isUpdateNeeded = true;
		_resurrectedCharCount += count;
	}

	public long getResurrectedByOtherCount()
	{
		return _resurrectedByOtherCount;
	}

	public void setResurrectedByOtherCount(long count)
	{
		_isUpdateNeeded = true;
		_resurrectedByOtherCount += count;
	}

	public long getDieCount()
	{
		return _dieCount;
	}

	public void setDieCount(long count)
	{
		_isUpdateNeeded = true;
		_dieCount += count;
	}

	/**
	 * Устанавливает максимальную текущую удачную заточку оружия
	 * Пишем в коллекцию только если приходящее значение больше хранимого.
	 * @param grade грейд оружия
	 * @param value значение заточки
	 */
	public void setWeaponEnchantMax(CrystalGrade grade, long value)
	{
		_isUpdateNeeded = true;
		if(!_weaponMaxEnchanted.containsKey(grade) || _weaponMaxEnchanted.get(grade) < value)
		{
			_weaponMaxEnchanted.put(grade, value);
		}
	}

	/**
	 * @param grade грейд оружия
	 * @return максимальная удачная заточка оружия по указанному грейду
	 */
	public long getWeaponEnchantMax(CrystalGrade grade)
	{
		return _weaponMaxEnchanted.containsKey(grade) ? _weaponMaxEnchanted.get(grade) : 0;
	}

	/**
	 * Устанавливает максимальную текущую удачную заточку брони
	 * Пишем в коллекцию только если приходящее значение больше хранимого.
	 * @param grade грейд брони
	 * @param value значение заточки
	 */
	public void setArmorEnchantMax(CrystalGrade grade, long value)
	{
		_isUpdateNeeded = true;
		if(!_armorMaxEnchanted.containsKey(grade))
		{
			_armorMaxEnchanted.put(grade, 0L);
		}
		else if(_armorMaxEnchanted.get(grade) < value)
		{
			_armorMaxEnchanted.put(grade, value);
		}
	}

	/**
	 * @param grade грейд брони
	 * @return максимальная удачная заточка брони по указанному грейду
	 */
	public long getArmorEnchantMax(CrystalGrade grade)
	{
		if(_armorMaxEnchanted.containsKey(grade))
		{
			return _armorMaxEnchanted.get(grade);
		}
		else
		{
			_armorMaxEnchanted.put(grade, 0L);
			return 0;
		}
	}

	/**
	 * Уведомляет о попытке заточить оружие
	 * @param grade грейд оружия
	 */
	public void addWeaponEnchantTry(CrystalGrade grade)
	{
		_isUpdateNeeded = true;
		if(_weaponTryToEnchant.containsKey(grade))
		{
			long temp = _weaponTryToEnchant.get(grade);
			_weaponTryToEnchant.put(grade, temp + 1);
		}
		else
		{
			_weaponTryToEnchant.put(grade, 1L);
		}
	}

	/**
	 * Устанавливает значение количества пыток заточить оружие
	 * @param grade грейд оружия
	 */
	public void setWeaponEnchantTry(CrystalGrade grade, Long value)
	{
		_weaponTryToEnchant.put(grade, value);
	}

	/**
	 * @param grade грейд оружия
	 * @return количество попыток заточить оружие
	 */
	public long getWeaponEnchantTry(CrystalGrade grade)
	{
		if(_weaponTryToEnchant.containsKey(grade))
		{
			return _weaponTryToEnchant.get(grade);
		}
		else
		{
			_weaponTryToEnchant.put(grade, 0L);
			return 0;
		}
	}

	/**
	 * Уведомляет о попытке заточить бронь
	 * @param grade грейд оружия
	 */
	public void addArmorEnchantTry(CrystalGrade grade)
	{
		_isUpdateNeeded = true;
		if(_armorTryToEnchant.containsKey(grade))
		{
			long temp = _armorTryToEnchant.get(grade);
			_armorTryToEnchant.put(grade, temp + 1);
		}
		else
		{
			_armorTryToEnchant.put(grade, 1L);
		}
	}

	/**
	 * Устанавливет количество попыток заточки брони
	 * @param grade грейд оружия
	 * @param value значение статистики
	 */
	public void setArmorEnchantTry(CrystalGrade grade, Long value)
	{
		_armorTryToEnchant.put(grade, value);
	}

	/**
	 * @param grade грейд брони
	 * @return количество попыток заточить оружие
	 */
	public long getArmorEnchantTry(CrystalGrade grade)
	{
		if(_armorTryToEnchant.containsKey(grade))
		{
			return _armorTryToEnchant.get(grade);
		}
		else
		{
			_armorTryToEnchant.put(grade, 0L);
			return 0;
		}
	}

	/**
	 * Добавляет количество использованных зарядов духа по грейду
	 * @param grade грейд зарядов духа
	 * @param value количество использованных зарядов духа
	 */
	public void setSsConsumed(SoulshotGrade grade, long value)
	{
		_isUpdateNeeded = true;
		if(_ssConsumed.containsKey(grade))
		{
			long temp = _ssConsumed.get(grade);
			_ssConsumed.put(grade, temp + value);
		}
		else
		{
			_ssConsumed.put(grade, value);
		}
	}

	/**
	 * @param grade грейд зарядов духа
	 * @return количество использованных зарядов духа по грейду
	 */
	public long getSsConsumed(SoulshotGrade grade)
	{
		if(_ssConsumed.containsKey(grade))
		{
			return _ssConsumed.get(grade);
		}
		else
		{
			_ssConsumed.put(grade, 0L);
			return 0;
		}
	}

	/**
	 * Добавляет количество использованных зарядов души по грейду
	 * @param grade грейд зарядов души
	 * @param value количество использованных зарядов души
	 */
	public void setSpsConsumed(SoulshotGrade grade, long value)
	{
		_isUpdateNeeded = true;
		if(_spsConsumed.containsKey(grade))
		{
			long temp = _spsConsumed.get(grade);
			_spsConsumed.put(grade, temp + value);
		}
		else
		{
			_spsConsumed.put(grade, value);
		}
	}

	/**
	 * @param grade грейд зарядов души
	 * @return количество использованных зарядов души по грейду
	 */
	public long getSpsConsumed(SoulshotGrade grade)
	{
		if(_spsConsumed.containsKey(grade))
		{
			return _spsConsumed.get(grade);
		}
		else
		{
			_spsConsumed.put(grade, 0L);
			return 0;
		}
	}

	public long getExpAdded()
	{
		return _expAdded;
	}

	public void setExpAdded(long exp)
	{
		if(!validateLong(_expAdded, exp))
		{
			return;
		}

		_isUpdateNeeded = true;
		_expAdded += exp;
	}

	public long getMonstersKilled()
	{
		return _monstersKilled;
	}

	public void setMonstersKilled(long count)
	{
		_isUpdateNeeded = true;
		_monstersKilled += count;
	}

	public long getExpFromMonsters()
	{
		return _expFromMonsters;
	}

	public void setExpFromMonsters(long value)
	{
		if(!validateLong(_expFromMonsters, value))
		{
			return;
		}

		_isUpdateNeeded = true;
		_expFromMonsters += value;
	}

	public long getMaxDamageToMonster()
	{
		return _maxDamageToMonster;
	}

	public void setMaxDamageToMonster(long damage)
	{
		_isUpdateNeeded = true;
		if(_maxDamageToMonster < damage)
		{
			_maxDamageToMonster = damage;
		}
	}

	public void setAllDamageToMonster(long damage)
	{
		if(!validateLong(_allDamageToMonster, damage))
		{
			return;
		}

		_isUpdateNeeded = true;
		_allDamageToMonster += damage;
	}

	/**
	 * Используется для обновления статистик:
	 * DAMAGE_TO_MONSTERS и DAMAGE_TO_MONSTERS_MAX
	 * @param damage нанесенный игроком урон монстрам
	 */
	public void notifyDamageToMonster(long damage)
	{
		_isUpdateNeeded = true;
		if(_maxDamageToMonster < damage)
		{
			_maxDamageToMonster = damage;
		}
		_allDamageToMonster += damage;
	}

	public long getAllDamageFromMonster()
	{
		return _allDamageFromMonster;
	}

	public void setAllDamageFromMonster(long damage)
	{
		if(!validateLong(_allDamageFromMonster, damage))
		{
			return;
		}

		_isUpdateNeeded = true;
		_allDamageFromMonster += damage;
	}

	public long getKilledByMonsterCount()
	{
		return _killedByMonstersCount;
	}

	public void setKilledByMonsterCount(long count)
	{
		_killedByMonstersCount += count;
	}

	public long getKilledByPkCount()
	{
		return _killedByPkCount;
	}

	public void setKilledByPkCount(long count)
	{
		_isUpdateNeeded = true;
		_killedByPkCount += count;
	}

	public long getKilledInPvpCount()
	{
		return _killedInPvpCount;
	}

	public void setKilledInPvpCount(long count)
	{
		_isUpdateNeeded = true;
		_killedInPvpCount += count;
	}

	public long getPkCount()
	{
		return _pkCount;
	}

	public void setPkCount(long count)
	{
		_isUpdateNeeded = true;
		_pkCount += count;
	}

	public long getPvpCount()
	{
		return _pvpCount;
	}

	public void setPvpCount(long count)
	{
		_isUpdateNeeded = true;
		_pvpCount += count;
	}

	public long getMaxDamageToPc()
	{
		return _maxDamageToPc;
	}

	public void setMaxDamageToPc(long damage)
	{
		_isUpdateNeeded = true;
		if(_maxDamageToPc < damage)
		{
			_maxDamageToPc += damage;
		}
	}

	/**
	 * Используется для обновления статистик:
	 * DAMAGE_TO_PC и DAMAGE_TO_PC_MAX
	 * @param damage нанесенный игроком урон по другим персонажам
	 */
	public void notifyDamageToPc(long damage)
	{
		_isUpdateNeeded = true;
		if(_maxDamageToPc < damage)
		{
			_maxDamageToPc += damage;
		}
		_maxDamageToPc += damage;
	}

	public long getAllDamageToPc()
	{
		return _allDamageToPc;
	}

	public void setAllDamageToPc(long damage)
	{
		if(!validateLong(_allDamageToPc, damage))
		{
			return;
		}

		_isUpdateNeeded = true;
		_allDamageToPc += damage;
	}

	public long getAllDamageFromPc()
	{
		return _allDamageFromPc;
	}

	public void setAllDamageFromPc(long damage)
	{
		if(!validateLong(_allDamageFromPc, damage))
		{
			return;
		}

		_isUpdateNeeded = true;
		_allDamageFromPc += damage;
	}

	/**
	 * Добавляет количество убийстви указанного эпического босса
	 * @param npcId NpcId босса
	 */
	public void setRaidKilled(int npcId, long value)
	{
		_isUpdateNeeded = true;
		if(_raidKilled.containsKey(npcId))
		{
			long temp = _raidKilled.get(npcId);
			_raidKilled.put(npcId, temp + value);
		}
		else
		{
			_raidKilled.put(npcId, value);
		}
	}

	/**
	 * @param npcId NpcId босса
	 * @return количество убийств указанного босса
	 */
	public long getEpicKilled(int npcId)
	{
		if(_raidKilled.containsKey(npcId))
		{
			return _raidKilled.get(npcId);
		}
		else
		{
			_raidKilled.put(npcId, 0L);
			return 0;
		}
	}

	public boolean validateLong(long oldValue, long addValue)
	{
		return Long.MAX_VALUE - oldValue >= addValue;
	}
}