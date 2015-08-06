package dwo.gameserver.instancemanager;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.CommissionItemHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExResponseCommissionBuyItem;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExResponseCommissionItemList;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExResponseCommissionList;
import dwo.gameserver.network.game.serverpackets.packet.commission.ExResponseCommissionRegister;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * L2GOD Team
 * User: Bacek, Yorie
 * Date: 21.07.11
 * Time: 13:39
 */

public class CommissionManager
{
	private static Logger _log = LogManager.getLogger(CommissionManager.class);
	private final Map<Long, CommissionItemHolder> _allItems = new HashMap<>();
	private final Map<Long, CommissionItemHolder> _weapons = new HashMap<>();
	private final Map<Long, CommissionItemHolder> _armors = new HashMap<>();
	private final Map<Long, CommissionItemHolder> _accessory = new HashMap<>();
	private final Map<Long, CommissionItemHolder> _consumables = new HashMap<>();
	private final Map<Long, CommissionItemHolder> _consumablesPet = new HashMap<>();
	private final Map<Long, CommissionItemHolder> _other = new HashMap<>();
	private long maxLotID;

	private CommissionManager()
	{
		_log.log(Level.INFO, "Commission Manager: Initializing...");

		_allItems.clear();
		_weapons.clear();
		_armors.clear();
		_accessory.clear();
		_consumables.clear();
		_consumablesPet.clear();
		_other.clear();

		load();
		loadMaxLotId();
	}

	public static CommissionManager getInstance()
	{
		return SingletonHolder._instance;
	}

	private void load()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `items` `i`" +
				"JOIN `commission_list` `cl` ON `cl`.`item_object_id` = `i`.`object_id`" +
				"WHERE `i`.`loc` = 'COMMISSION'");
			rset = statement.executeQuery();
			while(rset.next())
			{
				CommissionItemHolder item = new CommissionItemHolder(rset);
				addToArray(item);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "commission Manager: Error loading from database:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			_log.log(Level.INFO, "Loaded " + _weapons.size() + " weapon lots.");
			_log.log(Level.INFO, "Loaded " + _armors.size() + " armor lots.");
			_log.log(Level.INFO, "Loaded " + _accessory.size() + " accessory lots.");
			_log.log(Level.INFO, "Loaded " + _consumables.size() + " consumables lots.");
			_log.log(Level.INFO, "Loaded " + _consumablesPet.size() + " consumablesPet lots.");
			_log.log(Level.INFO, "Loaded " + _other.size() + " other lots.");
			_log.log(Level.INFO, "Total items: " + _allItems.size());
		}
	}

	/**
	 * Загрузка максимального значения лота из базы
	 */
	private void loadMaxLotId()
	{
		maxLotID = 0L;
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT max(id_lot) FROM commission_list");
			rset = statement.executeQuery();
			if(rset.next())
			{
				maxLotID = rset.getLong(1);
			}
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "commission Manager: Error loading maxLotID from database:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
			_log.log(Level.INFO, "maxLotID =  " + maxLotID + '.');
		}
	}

	/**
	 * Удаляем лот из продажи
	 * @param lot лот
	 */
	public void removeLot(CommissionItemHolder lot)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.prepareStatement("DELETE FROM commission_list WHERE id_lot = ?");
			stmt.setLong(1, lot.getLotId());
			stmt.execute();
			stmt = con.prepareStatement("DELETE FROM `items` WHERE `object_id` = ?");
			stmt.setInt(1, lot.getItemObjectId());
			stmt.execute();
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "commission Manager: Error deleting message:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
			deleteFromArray(lot);
		}
	}

	/**
	 * Добавляем лот на продажу
	 * @param player Игрок
	 * @param objectId objID итема
	 * @param ItemName Имя предмета
	 * @param sellPrice Цена продажи
	 * @param count Количество
	 * @param days Сколько дней будет продаваться
	 */
	public void addLot(L2PcInstance player, int objectId, String ItemName, long sellPrice, long count, int days)
	{
		CommissionPeriod period = CommissionPeriod.extract((byte) days);
		if(period == null)
		{
			return;
		}
		if(player == null)
		{
			return;
		}

		int fee = 1000;

		if(player.getAdenaCount() < fee)
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		long timeEnd = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(period.getDays(), TimeUnit.DAYS);

		Inventory inventory = player.getInventory();
		L2ItemInstance item = inventory.getItemByObjectId(objectId);

		if(item != null)
		{
			ThreadConnection con;
			FiltredPreparedStatement stmt;

			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				stmt = con.prepareStatement("SELECT COUNT(*) FROM `commission_list` WHERE `char_name` = ?");
				stmt.setString(1, player.getName());
				ResultSet rset = stmt.executeQuery();
				if(rset.next())
				{
					int lotsRegistered = stmt.getResultSet().getInt(1);
					if(player.isGM() && lotsRegistered >= 99999 || lotsRegistered >= 10)
					{
						player.sendPacket(SystemMessageId.COMMISSION_COUNT_LIMIT);
						return;
					}
				}
				else
				{
					throw new SQLException();
				}
			}
			catch(SQLException e)
			{
				player.sendPacket(SystemMessageId.CANNOT_REGISTER_ITEM);
				return;
			}

			maxLotID++;
			if(item.getCount() <= count)
			{
				count = item.getCount();
			}

			// Удаляем итем и адену
			player.destroyItemByItemId(ProcessType.COMISSION_SELL, PcInventory.ADENA_ID, fee, player, true);
			player.getInventory().destroyItem(ProcessType.COMISSION_SELL, objectId, count, player, null);

			item.updateDatabase();
			L2ItemInstance commissionItem = new L2ItemInstance(IdFactory.getInstance().getNextId(), item.getItem());
			commissionItem.setOwnerId(player.getObjectId());
			commissionItem.setAugmentation(item.getAugmentation());
			commissionItem.setEnchantLevel(item.getEnchantLevel());
			commissionItem.setEnchantEffect(item.getEnchantEffect());
			commissionItem.setSkin(item.getSkin());

			if(item.isWeapon())
			{
				commissionItem.setElementAttr(item.getAttackElementType(), item.getAttackElementPower());
			}
			else
			{
				for(byte i = 0; i < 6; i++)
				{
					commissionItem.setElementAttr(i, item.getElementDefAttr(i));
				}
			}
			commissionItem.setLocation(L2ItemInstance.ItemLocation.COMMISSION);
			commissionItem.setCount(count);
			commissionItem.updateDatabase();

			CommissionItemHolder lots = new CommissionItemHolder(maxLotID, timeEnd, player.getName(), sellPrice, count, period.getDays(), commissionItem, ItemName.toLowerCase());
			// Добавляем в массив
			addToArray(lots);

			// Сохраняем лот
			try
			{
				con = L2DatabaseFactory.getInstance().getConnection();
				stmt = CommissionItemHolder.getStatement(lots, con);
				stmt.execute();
			}
			catch(SQLException e)
			{
				_log.log(Level.ERROR, "commission Manager: Error saving message:" + e.getMessage(), e);
			}
			finally
			{
				DatabaseUtils.closeDatabaseCS(con, stmt);
			}

			player.sendPacket(new ExResponseCommissionRegister());

			// Обновляем ItemList и List
			player.sendPacket(new ExResponseCommissionItemList(player));
			showPlayerLots(player, Window.Sell, 0, -1, -1, "");
		}
	}

	/**
	 * Показываем лоты игрока
	 * @param cha Игрок
	 * @param window тип окна
	 * @param depthType номер кнопки
	 * @param itemClass тип итема   // -1 все 0 общие 1 редкие
	 * @param grade грейд итема  //  -1 любой грейд 0 нг 1 д 2 с 3 в 4 а 5 s 6 s80 7 s84 8 r 9 r95 10 r99
	 * @param searchString имя итема в поиске
	 */
	public void showPlayerLots(L2PcInstance cha, Window window, int depthType, int itemClass, int grade, String searchString)
	{
		boolean wasSent = false;
		int size = 0;
		int itemClass_new = itemClass; // TODO: нужно будет сделать
		List<CommissionItemHolder> items = new ArrayList<>();
		for(CommissionItemHolder lot : getArray(depthType, window == Window.Main))
		{
			switch(window)
			{
				case MainSubsection:
					if(lot != null && lot.getType() == depthType)
					{
						if(grade == -1 || grade == lot.getItemGrade())
						{
							if(lot.getItemName().contains(searchString.toLowerCase()))
							{
								items.add(lot);
							}
						}
					}
					break;
				case Sell:
					if(lot != null && lot.getOwnerId() == cha.getObjectId())
					{
						items.add(lot);
					}
					break;
				case Main:
					if(itemClass == -1 || itemClass == itemClass_new)
					{
						if(grade == -1 || grade == lot.getItemGrade())
						{
							if(lot.getItemName().contains(searchString.toLowerCase()))
							{
								items.add(lot);
							}
						}
					}
					break;
			}
			if(items.size() > 200)
			{
				cha.sendPacket(new ExResponseCommissionList(items, size, window.ordinal() + 1));
				size++;
				items.clear();
				wasSent = true;
			}
		}
		if(size > 0 || !wasSent)
		{
			cha.sendPacket(new ExResponseCommissionList(items, size, window.ordinal() + 1));
		}
	}

	/**
	 * Забираем итем из комиссионки
	 * @param cha Игрок
	 * @param lotId Возвращаемый лот
	 */
	public void cancelLot(L2PcInstance cha, long lotId)
	{
		CommissionItemHolder lots = getCommissionLot(lotId);
		if(lots != null && cha.getName().equals(lots.getCharName()))
		{
			// Выдача итема
			L2ItemInstance item = cha.getInventory().addItem(ProcessType.COMISSION_SELL, lots.getItemId(), lots.getCount(), null, null);
			if(lots.getEnchantLevel() > 0)
			{
				item.setEnchantLevel(lots.getEnchantLevel());
			}
			if(item.isWeapon())
			{
				item.setElementAttr((byte) lots.getAttackElementType(), lots.getAttackElementPower());
			}
			else if(item.isArmor())
			{
				for(int i = 0; i < 6; i++)
				{
					item.setElementAttr((byte) i, lots.getElementDefAttr(i));
				}
			}
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			cha.sendPacket(iu);

			// Удаляем лот
			removeLot(lots);

			// Обновляем ItemList и List
			cha.sendPacket(new ExResponseCommissionItemList(cha));
			showPlayerLots(cha, Window.Sell, 0, -1, -1, "");
			cha.sendPacket(SystemMessageId.CANCELLATION_OF_SALE_FOR_THE_ITEM_IS_SUCCESSFUL);
		}
	}

	/**
	 * @param lotId ID лота
	 * @return итем с lotId из общего хранилища предметов
	 */
	public CommissionItemHolder getCommissionLot(long lotId)
	{
		return _allItems.containsKey(lotId) ? _allItems.get(lotId) : null;
	}

	/**
	 * @param locationSlot тип LocationSlot
	 * @param isMain в какое окно щлем список?
	 * @return список предметов в зависимости от его LocationSlot
	 */
	public CommissionItemHolder[] getArray(int locationSlot, boolean isMain)
	{
		if(isMain)
		{
			switch(locationSlot)
			{
				case -1:
					return _allItems.values().toArray(new CommissionItemHolder[_allItems.size()]);
				case 0:
					return _weapons.values().toArray(new CommissionItemHolder[_weapons.size()]);
				case 1:
					return _armors.values().toArray(new CommissionItemHolder[_armors.size()]);
				case 2:
					return _accessory.values().toArray(new CommissionItemHolder[_accessory.size()]);
				case 3:
					return _consumables.values().toArray(new CommissionItemHolder[_consumables.size()]);
				case 4:
					return _consumablesPet.values().toArray(new CommissionItemHolder[_consumablesPet.size()]);
				default:
					return _other.values().toArray(new CommissionItemHolder[_consumablesPet.size()]);
			}
		}
		else
		{
			if(locationSlot == 0)
			{
				return _allItems.values().toArray(new CommissionItemHolder[_allItems.size()]);
			}
			else if(locationSlot > 0 && locationSlot < 19)
			{
				return filterItems(_weapons, locationSlot);
			}
			else if(locationSlot > 18 && locationSlot < 29)
			{
				return filterItems(_armors, locationSlot);
			}
			else if(locationSlot > 28 && locationSlot < 36)
			{
				return filterItems(_accessory, locationSlot);
			}
			else if(locationSlot > 35 && locationSlot < 43)
			{
				return filterItems(_consumables, locationSlot);
			}
			else
			{
				return locationSlot > 42 && locationSlot < 44 ? filterItems(_consumablesPet, locationSlot) : filterItems(_other, locationSlot);
			}
		}
	}

	/**
	 * Wrapper for item filtering from map.
	 * @param items List of items.
	 * @param locationSlot Location slot ID.
	 * @return Filtered item list.
	 */
	private CommissionItemHolder[] filterItems(Map<Long, CommissionItemHolder> items, int locationSlot)
	{
		return filterItems(items.values().toArray(new CommissionItemHolder[items.size()]), locationSlot);
	}

	/**
	 * Filters item list by location slot ID.
	 * @param items List of items.
	 * @param locationSlot Location slot ID.
	 * @return Filtered item list.
	 */
	private CommissionItemHolder[] filterItems(CommissionItemHolder[] items, int locationSlot)
	{
		List<CommissionItemHolder> filtered = new ArrayList<>();
		for(CommissionItemHolder item : items)
		{
			if(locationSlot < 0 || item.getType() == locationSlot)
			{
				filtered.add(item);
			}
		}
		return filtered.toArray(new CommissionItemHolder[filtered.size()]);
	}

	/**
	 * Добавляет предмет в массив в зависимости от типа
	 * @param lot лот предмета
	 */
	public void addToArray(CommissionItemHolder lot)
	{
		_allItems.put(lot.getLotId(), lot);

		if(lot.getType() > 0 && lot.getType() < 19)
		{
			_weapons.put(lot.getLotId(), lot);
		}
		else if(lot.getType() > 18 && lot.getType() < 29)
		{
			_armors.put(lot.getLotId(), lot);
		}
		else if(lot.getType() > 28 && lot.getType() < 36)
		{
			_accessory.put(lot.getLotId(), lot);
		}
		else if(lot.getType() > 35 && lot.getType() < 43)
		{
			_consumables.put(lot.getLotId(), lot);
		}
		else if(lot.getType() > 42 && lot.getType() < 44)
		{
			_consumablesPet.put(lot.getLotId(), lot);
		}
		else
		{
			_other.put(lot.getLotId(), lot);
		}

		long expiration = lot.getTimeEnd();
		if(expiration < System.currentTimeMillis())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new ItemRefundTask(lot.getLotId()), 10000);
		}
		else
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new ItemRefundTask(lot.getLotId()), expiration - System.currentTimeMillis());
		}
	}

	/**
	 * Удаляет лот из массива в зависимости от типа
	 * @param lot лот предмета
	 */
	public void deleteFromArray(CommissionItemHolder lot)
	{
		_allItems.remove(lot.getLotId());

		if(lot.getType() > 0 && lot.getType() < 19)
		{
			_weapons.remove(lot.getLotId());
		}
		else if(lot.getType() > 18 && lot.getType() < 29)
		{
			_armors.remove(lot.getLotId());
		}
		else if(lot.getType() > 28 && lot.getType() < 36)
		{
			_accessory.remove(lot.getLotId());
		}
		else if(lot.getType() > 35 && lot.getType() < 43)
		{
			_consumables.remove(lot.getLotId());
		}
		else if(lot.getType() > 42 && lot.getType() < 44)
		{
			_consumablesPet.remove(lot.getLotId());
		}
		else
		{
			_other.remove(lot.getLotId());
		}
	}

	/**
	 * Покупка лота.
	 * @param activeChar персонаж-покупатель
	 * @param lotId Id лота
	 */
	public void buyCommissionItem(L2PcInstance activeChar, long lotId)
	{
		CommissionItemHolder lot = getCommissionLot(lotId);

		// Предмет уже купили
		if(lot == null)
		{
			activeChar.sendPacket(SystemMessageId.COMMISSION_REGISTERED_ITEM_WAS_SOLD);
			return;
		}

		// Попытка купить у самого себя
		if(lot.getOwnerId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(SystemMessageId.CANNOT_BUY_ITEM);
			return;
		}

		long fee = 0;
		/*
		 * Если цена предмета <= 1k, то налог не взымаем (просто не из чего будет отнимать)
		 * Если цена больше 1k, но, при подсчете взноса, взнос < 10k, то налог 1k
		 * В других случаях налог считается исходя из цены, количества и процента по дням (см. CommissionPeriod)
		 */
		if(lot.getPrice() * lot.getCount() > 1000)
		{
			fee = (long) (CommissionPeriod.extractNormal(lot.getDays()).getTaxRate() * lot.getPrice() * lot.getCount());
			if(fee < 10000)
			{
				fee = 1000;
			}
		}

		if(activeChar.getAdenaCount() >= lot.getPrice() * lot.getCount())
		{
			// Забираем адену у игрока.
			activeChar.getInventory().reduceAdena(ProcessType.COMISSION_BUY, lot.getPrice() * lot.getCount(), activeChar, null);

			// Выдаем итем.
			L2ItemInstance item = activeChar.getInventory().addItem(ProcessType.COMISSION_BUY, lot.getItemId(), lot.getCount(), activeChar, null);
			if(lot.getEnchantLevel() > 0)
			{
				item.setEnchantLevel(lot.getEnchantLevel());
			}
			if(item.isWeapon())
			{
				if(lot.getAttackElementPower() > 0)
				{
					item.setElementAttr((byte) lot.getAttackElementType(), lot.getAttackElementPower());
				}
			}
			else
			{
				for(int i = 0; i < 6; i++)
				{
					if(lot.getElementDefAttr(i) > 0)
					{
						item.setElementAttr((byte) i, lot.getElementDefAttr(i));
					}
				}
			}
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			activeChar.sendPacket(iu);

			// Удаляем лот из базы и массивов.
			removeLot(lot);

			// Высылаем по почте адену продавцу.
			MailMessage msg = new MailMessage(lot.getOwnerId(), "CommissionBuyTitle", lot, item, 5);
			msg.createAttachments();
			msg.getAttachments().addItem(ProcessType.COMISSION_SELL, PcInventory.ADENA_ID, lot.getPrice() * lot.getCount() - fee, null, null);
			MailManager.getInstance().sendMessage(msg);

			// Все прошло успешно.
			activeChar.sendPacket(new ExResponseCommissionBuyItem(1, lot.getItemId(), lot.getCount(), lot.getEnchantLevel()));
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			activeChar.sendPacket(new ExResponseCommissionBuyItem());
		}
	}

	/**
	 * Возврат итема по истечении времени.
	 * @param lotId Id лота
	 */
	public void refundLotToPlayer(long lotId)
	{
		CommissionItemHolder lot = getCommissionLot(lotId);

		if(lot == null)
		{
			return;
		}

		// Высылаем по почте адену продавцу.
		MailMessage msg = new MailMessage(lot.getOwnerId(), "CommissionDeleteTitle", lot, null, 4);
		msg.createAttachments();
		msg.getAttachments().addItem(ProcessType.COMISSION_REFUND, lot.getItemId(), lot.getCount(), null, null);
		MailManager.getInstance().sendMessage(msg);
		//Удаляем лот из базы и массивов.
		removeLot(lot);
	}

	public enum Window
	{
		Main,
		Sell,
		MainSubsection
	}

	public enum CommissionPeriod
	{
		ONE_DAY((byte) 1, 0.005),
		THREE_DAYS((byte) 3, 0.015),
		FIVE_DAYS((byte) 5, 0.025),
		SEVEN_DAYS((byte) 7, 0.035);

		private final byte _days;
		private final double _taxRate;

		private CommissionPeriod(byte days, double taxRate)
		{
			_days = days;
			_taxRate = taxRate;
		}

		public static CommissionPeriod extract(byte days)
		{
			switch(days)
			{
				case 0:
					return ONE_DAY;
				case 1:
					return THREE_DAYS;
				case 2:
					return FIVE_DAYS;
				case 3:
					return SEVEN_DAYS;
			}
			return null;
		}

		public static CommissionPeriod extractNormal(byte days)
		{
			switch(days)
			{
				case 1:
					return ONE_DAY;
				case 3:
					return THREE_DAYS;
				case 5:
					return FIVE_DAYS;
				case 7:
					return SEVEN_DAYS;
			}
			return null;
		}

		public double getTaxRate()
		{
			return _taxRate;
		}

		public byte getDays()
		{
			return _days;
		}
	}

	private static class SingletonHolder
	{
		protected static final CommissionManager _instance = new CommissionManager();
	}

	class ItemRefundTask implements Runnable
	{
		final long _lotId;

		public ItemRefundTask(long lotId)
		{
			_lotId = lotId;
		}

		@Override
		public void run()
		{
			refundLotToPlayer(_lotId);
		}
	}
}