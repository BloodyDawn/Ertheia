package dwo.gameserver.model.world.communitybbs.Manager;

import dwo.config.mods.ConfigCommunityBoardPVP;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.datatables.sql.PetNameTable;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.base.SubClass;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExBR_PremiumState;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowAll;
import dwo.gameserver.network.game.serverpackets.packet.party.PartySmallWindowDeleteAll;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExPledgeCount;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListAll;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 31.05.13
 * Time: 3:27
 */

public class ServiceBBSManager
{
	protected static final Logger _logDonate = LogManager.getLogger("donate");
	private static ServiceBBSManager _instance = new ServiceBBSManager();

	public static ServiceBBSManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new ServiceBBSManager();
		}
		return _instance;
	}

	public String parsecmd(String var, L2PcInstance activeChar, String content)
	{
		if(var == null || content == null || activeChar == null)
		{
			return null;
		}

		String var2 = null;
		if(var.contains(" "))
		{
			var2 = var.split(" ")[1];
		}

		if(var.startsWith("show"))
		{
			return TopBBSManager.getInstance().getHtml(activeChar, "services.htm");
		}
		if(var.startsWith("change_nick"))
		{
			return generateString(activeChar, "change_nick.htm", var2 != null ? changePlayerName(activeChar, var2) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_NAME_PRICE);
		}
		if(var.startsWith("change_sex"))
		{
			return generateString(activeChar, "change_sex.htm", var2 != null ? changeSex(activeChar) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_SEX_PRICE);
		}
		if(var.startsWith("change_clan_name"))
		{
			return generateString(activeChar, "change_clan_name.htm", var2 != null ? changeClanName(activeChar, var2) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_CLANNAME_PRICE);
		}
		if(var.startsWith("buy_nobles"))
		{
			return generateString(activeChar, "buy_nobles.htm", var2 != null ? buyNobles(activeChar) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_NOBLESS_PRICE);
		}
		if(var.startsWith("change_pet_name"))
		{
			return generateString(activeChar, "change_pet_name.htm", var2 != null ? changePetName(activeChar, var2) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_PET_NAME_PRICE);
		}
		if(var.startsWith("remove_penalty_clan"))
		{
			return generateString(activeChar, "remove_penalty_clan.htm", var2 != null ? removeClanPenalty(activeChar) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CLAN_PENALTY_PRICE);
		}
		if(var.startsWith("delete_char"))    // Моментальное удаление персонажа
		{
			return generateString(activeChar, "delete_char.htm", var2 != null ? removeCharacter(activeChar) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CHARACTER_PRICE);
		}
		if(var.startsWith("transfer_char"))    // Перенос персонажа
		{
			return generateString(activeChar, "transfer_char.htm", var2 != null ? transferCharacter(activeChar, var2) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_TRANSFER_CHARACTER_PRICE);

		}
        if (var.startsWith("change_color_title"))
        {
            return generateStringNickColor(activeChar, "change_color_nick.htm", "change_color_nick_button.htm", (var2 != null) ? changeTitleColor(activeChar, var2) : "", ConfigCommunityBoardPVP.CommunityBoardChangeNickColors);
        }
		if(var.startsWith("buy_window"))    // Дополнительные окна
		{
			return generateString(activeChar, "buy_window.htm", "buy_window_button.htm", var2 != null ? buyWindow(activeChar, var2) : "", ConfigCommunityBoardPVP.COMMUNITY_BOARD_WINDOW_PRICE);
		}
		return null;
	}

	private String generateString(L2PcInstance activeChar, String htm, String htm_button, String msg, Map array)
	{
		String str = "";
		String html = TopBBSManager.getInstance().getHtml(activeChar, "services/" + htm_button);
		for(Object o : array.entrySet())
		{
			str += html.replace("<?days?>", String.valueOf(((Map.Entry) o).getKey())).replace("<?price?>", String.valueOf(((Map.Entry) o).getValue()));
		}

		return generateString(activeChar, htm, msg, 0).replace("<?button?>", str);
	}

	private String generateString(L2PcInstance activeChar, String htm, String msg, int price)
	{
		String html = TopBBSManager.getInstance().getHtml(activeChar, "services/" + htm);
		// TODO выввод имени итема .concat(" ").concat(Config.COMMUNITY_BOARD_SERVICES_PRICE_ITEM)
		html = html.replace("<?price?>", String.valueOf(price));
		html = html.replace("<?msg?>", msg);
		return html;
	}

    private String generateStringNickColor(final L2PcInstance activeChar, final String htm, final String htm_button, final String msg, final List array) {
        String str = "";
        final String html = TopBBSManager.getInstance().getHtml(activeChar, "services/".concat(htm_button));
        for (int i = 0; i < array.size(); ++i) {
            final String color = array.get(i).toString();
            str += html.replace("<?type?>", String.valueOf(i)).replace("<?color?>", color);
        }
        return this.generateString(activeChar, htm, msg, ConfigCommunityBoardPVP.CommunityBoardChangeNickColorPrice).replace("<?button?>", str);
    }

    private String changeTitleColor(final L2PcInstance player, final String var) {
        if (!Util.isDigit(var)) {
            return "";
        }
        final int type = Integer.parseInt(var);
        if (type >= ConfigCommunityBoardPVP.CommunityBoardChangeNickColors.size()) {
            return "";
        }
        final int price = ConfigCommunityBoardPVP.CommunityBoardChangeNickColorPrice;
        if (player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < price) {
            return "У Вас не хватает средств на активацию этой функции.";
        }
        player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, price, player, null);
        final String color = ConfigCommunityBoardPVP.CommunityBoardChangeNickColors.get(type);
        final StringBuilder sb = new StringBuilder();
        sb.append("0x").append(color.substring(4, 6)).append(color.substring(2, 4)).append(color.substring(0, 2));
        player.getAppearance().setTitleColor(Integer.decode(sb.toString()));
        player.broadcastUserInfo();
        return "Цвет титула успешно изменен";
    }

	/***
	 * Смена имени игрока
	 * @param player инстанс игрока
	 * @param newName новое имя, на которое хочет сменить игрок свое
	 * @return {@code true} если операция прошла успешно
	 */
	private String changePlayerName(L2PcInstance player, String newName)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_NAME_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}

		// Проверяем новое имя персонажа на валидность
		if(Util.isValidNameEx(newName) != null)
		{
			switch(Util.isValidNameEx(newName))
			{
				case REASON_CREATION_FAILED:
					return "Указанное имя не может быть использовано.";
				case REASON_16_ENG_CHARS:
					return "В указанном имени слишком много букв.";
				case REASON_INCORRECT_NAME:
					return "Указано неверное имя.";
				case REASON_NAME_ALREADY_EXISTS:
					return "Указанное Вами имя уже используется.";
			}
		}

		String preName = player.getName();

		// Если все проверки пройдены - меняем имя
		player.setName(newName);
		player.store();
		CharNameTable.getInstance().addName(player);

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_NAME_PRICE, player, null);

		player.broadcastUserInfo();

		if(player.isInParty())
		{
			// Убираем окно списка группы у сопартийцев
			player.getParty().broadcastPacket(player, new PartySmallWindowDeleteAll());
			// И добавляем с уже новым именем
			player.getParty().getMembers().stream().filter(member -> !member.equals(player)).forEach(member -> member.sendPacket(new PartySmallWindowAll(member, player.getParty())));
		}

		// Уведомляем клан
		if(player.getClan() != null)
		{
			player.getClan().broadcastClanStatus();
		}
		RegionBBSManager.getInstance().changeCommunityBoard();

		_logDonate.log(Level.INFO, "СМЕНА ИМЕНИ: Игрок " + preName + " сменил имя на " + player.getName());
		return "Ваше имя было успешно изменено на " + newName + '.';
	}

	/***
	 * Смена пола персонажу
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String changeSex(L2PcInstance player)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_SEX_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}

		if(player.getRace() == Race.Kamael)
		{
			return "Для расы Камаель функция смены пола недоступна.";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_SEX_PRICE, player, null);

		// Меняем персонажу пол на противоположный
		player.getAppearance().setSex(!player.getAppearance().getSex());
		player.broadcastUserInfo();
		player.getLocationController().decay();
		player.getLocationController().spawn(player.getX(), player.getY(), player.getZ());
		return "Пол Вашего персонажа был успешно изменен.";
	}

	/***
	 * Смена имени клана
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String changeClanName(L2PcInstance player, String newName)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_CLANNAME_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}
		if(player.getClan() == null || !player.isClanLeader())
		{
			return "У Вас нет клана либо Вы не являетесь его лидером.";
		}
		if(ClanTable.getInstance().getClanByName(newName) != null)
		{
			return "Указанное Вами имя уже используется.";
		}
		if(!Util.isAlphaNumeric(newName) || newName.length() < 2)
		{
			return "Указано неверное имя.";
		}
		if(newName.length() > 16)
		{
			return "В указанном имени слишком много букв.";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_CLANNAME_PRICE, player, null);

		String preName = player.getClan().getName();

		// Меняем имя клана и сохраняем его в базу
		player.getClan().setName(newName);
		player.getClan().updateClanInDB();

		// Обновляем пакетку
		player.sendPacket(new PledgeShowInfoUpdate(player.getClan()));
		player.sendPacket(new PledgeShowMemberListAll(player.getClan(), player));
		player.sendPacket(new ExPledgeCount(player.getClan().getOnlineMembersCount()));

		_logDonate.log(Level.INFO, "СМЕНА ИМЕНИ КЛАНА: Игрок " + player.getName() + " сменил имя клана с " + preName + " на " + player.getClan().getName());
		return "Имя клана успешно изменено на: " + newName;
	}

	/***
	 * Покупка дворянинства
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String buyNobles(L2PcInstance player)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_NOBLESS_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}
		if(player.isSubClassActive())
		{
			return "Дворянином может стать только основной класс.";
		}
		if(player.isNoble())
		{
			return "Вы уже получили статус дворянина. Зачем Вам еще один? :)";
		}
		if(player.getSubClasses().size() < 1)
		{
			return "Чтобы стать дворянином, нужно иметь хотя бы один саб-класс.";
		}

		boolean has75Sub = false;
		for(SubClass sub : player.getSubClasses().values())
		{
			if(sub.getLevel() >= 76)
			{
				has75Sub = true;
				break;
			}
		}

		if(!has75Sub)
		{
			return "Что-то подсказывает нам, что Вашим саб-классам не хватает опыта. Нужно иметь хотя бы один подкласс 76 уровня!";
		}

		if(player.getLevel() < 76)
		{
			return "Вы еще юны для получения статуса дворянина. Достигните 76 уровня!";
		}

		if(player.getClassId().level() < 3)
		{
			return "Ваш стаж столяра не подходит под условия дворян. Получите квалификацию 3 или 4 профессии!";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_NOBLESS_PRICE, player, null);

		player.setNoble(true);
		player.sendUserInfo();

		// Даем 10 очков для того чтобы игрока мог участовать на Великой Олимпаде
		// Выдаем только, если период игр начат
		if(Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().generateNobleStats(player);
		}
		return "Поздравляем! Теперь Вы Дворянин!";
	}

	/***
	 * Покупка премиума
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно

	private String buyPremium(L2PcInstance player, String var)
	{
		if(!Util.isDigit(var))
		{
			return "";
		}

		int days = Integer.parseInt(var);

		if(!ConfigCommunityBoardPVP.COMMUNITY_BOARD_PREMIUM_PRICE.containsKey(days))
		{
			return "";
		}

		int price = ConfigCommunityBoardPVP.COMMUNITY_BOARD_PREMIUM_PRICE.get(days);

		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < price)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, price, player, null);

		// Расчитываем время премиум аккаунта
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
		return "Ваш премиум аккаунт будет активен: " + dayz + " дней, " + hours + " часов, " + minutes + " минут.";
	}*/

	/***
	 * Смена имени пета
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String changePetName(L2PcInstance player, String newName)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_PET_NAME_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}
		if(player.getPets().isEmpty())
		{
			return "Не найдено ни одного призванного существа. Призовите существо, которое хотите переименовать.";
		}

		L2PetInstance pet = null;
		for(L2Summon summon : player.getPets())
		{
			if(summon.isPet())
			{
				pet = (L2PetInstance) summon;
			}
		}
		if(pet == null)
		{
			return "Не найдено ни одного призванного существа. Призовите существо, которое хотите переименовать.";
		}

		// Проверяем новое имя на валидность
		if(PetNameTable.getInstance().doesPetNameExist(newName, pet.getTemplate().getNpcId()))
		{
			return "Питомец с таким именем уже существует.";
		}
		if(newName.length() < 3 || newName.length() > 16)
		{
			return "Длина имени питомца не должно превышать 16 символов.";
		}
		if(!PetNameTable.getInstance().isValidPetName(newName))
		{
			return "Имя питомца содержит запрещенный символ.";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_CHANGE_PET_NAME_PRICE, player, null);

		// Меняем имя питомцу
		pet.setName(newName);
		pet.updateAndBroadcastStatus(1);
		return "Имя Вашего питомца успешно изменено на " + newName;
	}

	/***
	 * Смена имени клана
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String removeClanPenalty(L2PcInstance player)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CLAN_PENALTY_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}
		if(player.getClan() == null || !player.isClanLeader())
		{
			return "У Вас нет клана либо Вы не являетесь его лидером.";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CLAN_PENALTY_PRICE, player, null);

		// Снимаем штрафы с клана игрока
		player.getClan().setCharPenaltyExpiryTime(0);
		player.getClan().setAllyPenaltyExpiryTime(0, 0);
		player.getClan().setDissolvingExpiryTime(0);
		player.getClan().updateClanInDB();
		return "С Вашего клана были успешно сняты все штрафы!";
	}

	/***
	 * Моментальное удаление персонажа
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String removeCharacter(L2PcInstance player)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CHARACTER_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}
		if(player.getClan() != null)
		{
			return "Вы состоите в клане. Выйдите из клана и повторите операцию.";
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_REMOVE_CHARACTER_PRICE, player, null);

		// Разлогиниваем персонажа
		player.logout();

		// Уведомляем хук на удаление инфы о персонаже
		HookManager.getInstance().notifyEvent(HookType.ON_CHAR_DELETE, null, player.getObjectId());
		return "";
	}

	/***
	 * Перенос персонажа на другой аккаунт
	 * @param player инстанс игрока
	 * @return {@code true} если операция прошла успешно
	 */
	private String transferCharacter(L2PcInstance player, String newAccount)
	{
		if(player.getItemsCount(ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM) < ConfigCommunityBoardPVP.COMMUNITY_BOARD_TRANSFER_CHARACTER_PRICE)
		{
			return "У Вас не хватает средств на активацию этой функции.";
		}

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;

		// Проверка на существование аккаунта, на который переносим персонажа
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM `accounts` WHERE `login`=?");
			statement.setString(1, newAccount);
			rset = statement.executeQuery();

			boolean checkIfExist = false;

			while(rset.next())
			{
				checkIfExist = true;
			}
			if(!checkIfExist)
			{
				return "Указанный Вами аккаунт для переноса не существует.";
			}
		}
		catch(SQLException e)
		{
			return "Произошла ошибка во время переноса персонажа. Операция отменена.";
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Проверка на свободное место на аккаунте
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `account_name`=?");
			statement.setString(1, newAccount);
			rset = statement.executeQuery();

			int charactersInAccount = 0;

			while(rset.next())
			{
				charactersInAccount++;
			}

			if(charactersInAccount >= 7)
			{
				return "На указанном аккаунте нет свободного места.";
			}
		}
		catch(SQLException e)
		{
			return "Произошла ошибка во время переноса персонажа. Операция отменена.";
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Забираем плату
		player.getInventory().destroyItemByItemId(ProcessType.COMMUNITY_BOARD, ConfigCommunityBoardPVP.COMMUNITY_BOARD_SERVICES_PRICE_ITEM, ConfigCommunityBoardPVP.COMMUNITY_BOARD_TRANSFER_CHARACTER_PRICE, player, null);

		// Переносим персонажа на другой аккаунт и сохраняем
		player.setAccountName(newAccount);
		player.storeCharBase();
		player.logout();
		return "Перенос успешно завершен.";
	}

	private String buyWindow(L2PcInstance player, String var)
	{
		// TODO
		return null;
	}
}
