package dwo.xmlrpcserver.XMLServices;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.AccountShareDataTable;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.AccountShareData;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.LogOutOk;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBR_PremiumState;
import dwo.gameserver.util.Util;
import dwo.xmlrpcserver.XMLUtils;
import dwo.xmlrpcserver.model.Message.MessageType;
import javolution.util.FastList;
import org.apache.log4j.Level;

import java.util.List;

public class PlayerService extends Base
{
	/**
	 * Добавляет персонажу заданный итем
	 * @param playerName имя игрока
	 * @param itemId ID предмета
	 * @param count количество предмета
	 * @return {@code OK} если добавление состоялось удачно, {@code FAIL} если по каким-то причинам добавление не состоялось
	 */
	public String addItemToPlayer(String playerName, int itemId, int count)
	{
		L2ItemInstance item = ItemTable.getInstance().createItem(ProcessType.DONATION, itemId, count, null);
		try
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(playerName);
			int playerId = CharNameTable.getInstance().getIdByName(playerName);
			if(playerId < 0)
			{
				return json(MessageType.FAILED);
			}
			if(player == null)
			{
				item.setOwnerId(playerId);
				item.setLocation(L2ItemInstance.ItemLocation.INVENTORY);
				item.updateDatabase();
				WorldManager.getInstance().removeObject(item);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + playerName + " donated ItemId: " + itemId + " Count: " + count + " [OFFLINE PLAYER]");
				return json(MessageType.OK);
			}
			else
			{
				player.addItem(ProcessType.DONATION, item, null, true);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + playerName + " donated ItemId: " + itemId + " Count: " + count + " [IP: " + player.getClient().getConnectionAddress().getHostAddress() + " HWID: " + (player.getClient().getHWID() != null ? player.getClient().getHWID() : "NONE") + ']');
				return json(MessageType.OK);
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * @param account имя аккаунта
	 * @param days количество дней, на которые установить премиум аккаунт
	 * @return результат операции
	 */
	public String addPremiumDaysForPlayer(String account, int days)
	{
		// Все аккаунты в базе в нижнем регистре !!!!!!!!!!!!!!!!!!!!
		account = account.toLowerCase();
		try
		{
			if(!Config.PREMIUM_ENABLED)
			{
				return json(MessageType.FAILED);
			}
			// Проверяем, есть ли персонаж онлайн
			boolean foundInWorld = false;
			for(L2PcInstance player : WorldManager.getInstance().getAllPlayersArray())
			{
				if(player.getAccountName().equals(account))
				{
					long currentPremiumTime = player.getPremiumTime();
					if(currentPremiumTime < System.currentTimeMillis())
					{
						currentPremiumTime = System.currentTimeMillis();
					}
					long endPremiumTime = currentPremiumTime + days * 86400000L;
					player.stopPremiumTask();
					player.setPremiumEndTime(endPremiumTime);
					player.startPremiumTask();
					player.sendPacket(new ExBR_PremiumState(player.getObjectId(), 1));

					long remainingTime = (endPremiumTime - System.currentTimeMillis()) / 1000;
					int dayz = (int) (remainingTime / 86400);
					remainingTime %= 86400;
					int hours = (int) (remainingTime / 3600);
					remainingTime %= 3600;
					int minutes = (int) (remainingTime / 60);
					foundInWorld = true;
					player.sendMessage("Ваш премиум аккаунт будет активен: " + dayz + " дней, " + hours + " часов, " + minutes + " минут.");
					logDonate.log(Level.INFO, "XML RPC Donate: Account " + account + " buyed premium for [DaysAdd: " + days + " PremiumTime: day " + dayz + " hours " + hours + " minutes " + minutes + "]   [IP: " + player.getClient().getConnectionAddress().getHostAddress() + " HWID: " + (player.getClient().getHWID() != null ? player.getClient().getHWID() : "NONE") + ']');
					break;
				}
			}
			// Если персонажа с таким аккаунтом не найдено в игре
			if(!foundInWorld)
			{
				AccountShareData data = AccountShareDataTable.getInstance().getAccountData(account, "player_premium_time");
				if(data == null)
				{
					data = AccountShareDataTable.getInstance().addAccountData(account, "player_premium_time", String.valueOf(System.currentTimeMillis() + days * 86400000L));
				}
				else
				{
					// Если у игрока был премиум раньше, но он кончился
					if(data.getLongValue() < System.currentTimeMillis())
					{
						data.setValue(String.valueOf(System.currentTimeMillis() + days * 86400000L));
					}
					// Если у игрока уже есть премиум и он купил еще сверху один
					else
					{
						data.setValue(String.valueOf(data.getLongValue() + days * 86400000L));
					}
				}
				data.updateInDb();
				logDonate.log(Level.INFO, "XML RPC Donate: Account " + account + " buyed premium for " + days + " days " + "[OFFLINE PLAYER]");
			}
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Устанавливает игроку указанный цвет ника
	 * @param charName имя игрока
	 * @param color цвет в RGB
	 * @return результат операции
	 */
	public String setNameColor(String charName, int color)
	{
		try
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(charName);
			if(player == null)
			{
				conn = L2DatabaseFactory.getInstance().getConnection();
				statement = conn.prepareStatement("UPDATE characters SET name_color=? WHERE char_name=?");
				statement.setInt(1, color);
				statement.setString(2, charName);
				statement.execute();
				databaseClose(false);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " changed name color to " + Integer.toHexString(color) + " RGB" + " [OFFLINE PLAYER]");
				return json(MessageType.OK);
			}
			else
			{
				player.getAppearance().setNameColor(color);
				player.broadcastUserInfo();
				player.store();
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " changed name color to " + Integer.toHexString(color) + " RGB" + " [IP: " + player.getClient().getConnectionAddress().getHostAddress() + " HWID: " + (player.getClient().getHWID() != null ? player.getClient().getHWID() : "NONE") + ']');
				return json(MessageType.OK);
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Устанавливает игроку указанный цвет титула
	 * @param charName имя игрока
	 * @param color цвет в RGB
	 * @return результат операции
	 */
	public String setTitleColor(String charName, int color)
	{
		try
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(charName);
			if(player == null)
			{
				conn = L2DatabaseFactory.getInstance().getConnection();
				statement = conn.prepareStatement("UPDATE characters SET title_color=? WHERE char_name=?");
				statement.setInt(1, color);
				statement.setString(2, charName);
				statement.execute();
				databaseClose(false);
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " title name color to " + Integer.toHexString(color) + " RGB" + " [OFFLINE PLAYER]");
				return json(MessageType.OK);
			}
			else
			{
				player.getAppearance().setTitleColor(color);
				player.broadcastUserInfo();
				player.store();
				logDonate.log(Level.INFO, "XML RPC Donate: Player " + charName + " changed title color to " + Integer.toHexString(color) + " RGB" + " [IP: " + player.getClient().getConnectionAddress().getHostAddress() + " HWID: " + (player.getClient().getHWID() != null ? player.getClient().getHWID() : "NONE") + ']');
				return json(MessageType.OK);
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Сброс кармы игрока, если она у него отрицательная
	 * @param charName имя игрока
	 * @return результат операции
	 */
	public String resetReputationToZero(String charName)
	{
		try
		{
			L2PcInstance player = WorldManager.getInstance().getPlayer(charName);
			if(player == null)
			{
				int currentReputation = 0;

				conn = L2DatabaseFactory.getInstance().getConnection();
				statement = conn.prepareStatement("SELECT reputation FROM characters WHERE char_name=?");
				statement.setString(1, charName);
				resultSet = statement.executeQuery();
				while(resultSet.next())
				{
					currentReputation = resultSet.getInt("reputation");
				}

				if(currentReputation < 0)
				{
					statement = conn.prepareStatement("UPDATE characters SET reputation=0 WHERE char_name=?");
					statement.setString(1, charName);
					statement.execute();
					databaseClose(true);
					return json(MessageType.OK);
				}
				else
				{
					databaseClose(true);
					return json(MessageType.FAILED);
				}
			}
			else
			{
				int currentReputation = player.getReputation();
				if(currentReputation < 0)
				{
					player.setReputation(0);
					return json(MessageType.OK);
				}
				else
				{
					return json(MessageType.FAILED);
				}
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Возвращает всех персонажей с заданным аккаунтом
	 * @param account имя аккаунта
	 * @return сериализованные инстансы игроков на аккаунте
	 */
	public String getAllCharsFromAccount(String account)
	{
		StringBuilder result = new StringBuilder();
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(Characters.SELECT_CHAR_ACCOUNT);
			statement.setString(1, account);
			resultSet = statement.executeQuery();
			L2PcInstance pc;
			while(resultSet.next())
			{
				pc = Util.loadPlayer(resultSet.getString(1), true);
				if(pc != null)
				{
					result.append(XMLUtils.serializePlayer(pc, true));
				}
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
		finally
		{
			databaseClose(true);
		}
		result.append("</templates>");
		return json(result.toString());
	}

	/**
	 * Возвращает список имен персонажей аккаунта.
	 * @param account имя аккаунта
	 * @return игроков на аккаунте
	 */
	public String listCharacterNames(String account)
	{
		List<String> names = new FastList<>();
		try
		{
			conn = L2DatabaseFactory.getInstance().getConnection();
			statement = conn.prepareStatement(Characters.SELECT_CHAR_ACCOUNT);
			statement.setString(1, account);
			resultSet = statement.executeQuery();
			while(resultSet.next())
			{
				names.add(resultSet.getString(1));
			}
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
		finally
		{
			databaseClose(true);
		}

		return json(names);
	}

	/**
	 * Сериализует персонажа с указанным именем
	 * @param charName имя персонажа
	 * @param full режим сериализации
	 * @return сериализованный инстанс игрока
	 */
	public String getPlayer(String charName, String full)
	{
		L2PcInstance pc;
		String result = "";
		pc = WorldManager.getInstance().getPlayer(charName) == null ? Util.loadPlayer(charName, true) : WorldManager.getInstance().getPlayer(charName);

		try
		{
			result += pc != null ? XMLUtils.serializePlayer(pc, Boolean.parseBoolean(full)) : "<char/>";
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, getClass().getSimpleName() + ": Error while getPlayer() : ", e);
		}
		finally
		{
			try
			{
				pc.getLocationController().delete();
			}
			catch(NullPointerException e)
			{
				log.log(Level.ERROR, getClass().getSimpleName() + ": NPE Error while getPlayer().deleteMe() : ", e);
			}
		}

		return json(result);
	}

	/**
	 * Восстановление игрока в ближайший город
	 * @param charName имя игрока
	 * @return результат
	 */
	public String unstuckPlayer(String charName)
	{
		if(WorldManager.getInstance().getPlayer(charName) == null)
		{
			L2PcInstance player = Util.loadPlayer(charName, true);

			try
			{
				if(player == null)
				{
					return json(MessageType.FAILED);
				}

				if(player.getClient() != null)
				{
					L2GameClient cl = player.getClient();
					player.setClient(null);
					cl.close(LogOutOk.STATIC_PACKET);
					player.teleToLocation(TeleportWhereType.TOWN);
				}
				else
				{
					return json(MessageType.FAILED);
				}
			}
			catch(Exception e)
			{
				log.log(Level.ERROR, getClass().getSimpleName() + ": Error while unstuckPlayer() : ", e);
			}
			finally
			{
				try
				{
					player.getLocationController().delete();
				}
				catch(NullPointerException e)
				{
					log.log(Level.ERROR, getClass().getSimpleName() + ": NPE Error while getPlayer().deleteMe() : ", e);
				}
			}
		}
		else
		{
			// Нельзя анстакать игрока, который онлайн
			return json(MessageType.FAILED);
		}
		return json(MessageType.OK);
	}
}