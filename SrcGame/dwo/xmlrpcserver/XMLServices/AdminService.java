package dwo.xmlrpcserver.XMLServices;

import dwo.config.Config;
import dwo.gameserver.GameServerShutdown;
import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.datatables.xml.*;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.skills.SkillTable;
import dwo.xmlrpcserver.model.Message.MessageType;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.04.12
 * Time: 20:32
 */

public class AdminService extends Base
{
	/**
	 * Перезагрузка указанного инстанса
	 * @param instance инстанс для перезагрузки
	 * @return {@code OK} если перезагрузка удачна, {@code FAIL} если по каким-то причинам случилась ошибка
	 */
	public String reloadInstance(String instance)
	{
		try
		{
			switch(instance)
			{
				case "config":
					Config.loadAll();
					break;
				case "html":
					HtmCache.getInstance().reload();
					break;
				case "item":
					ItemTable.getInstance().reload();
					break;
				case "multisell":
					MultiSellData.getInstance().load();
					break;
				case "npc":
					NpcTable.getInstance().load();
					break;
				case "quest":
					QuestManager.getInstance().reloadAllQuests();
					break;
				case "skill":
					SkillTable.getInstance().reload();
					break;
				case "teleport":
					TeleportListTable.getInstance().load();
					break;
				case "zone":
					ZoneManager.getInstance().reload();
					break;
//				case "primeshop": TODO
//					PrimeShopTable.getInstance().load();
//					break;
				case "walker":
					NpcWalkerRoutesData.getInstance().load();
					break;
				case "jump":
					CharJumpRoutesTable.getInstance().load();
					break;
				case "access":
					AdminTable.getInstance().load();
					break;
			}
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Выключение\рестарт сервера
	 * @param restart {@code true} рестартовать сервер после выключения, {@code false} если сервер нужно просто выключить
	 * @return {@code OK} если перезагрузка удачна, {@code FAIL} если по каким-то причинам случилась ошибка
	 */
	public String restartServer(String restart)
	{
		try
		{
			GameServerShutdown.getInstance().startShutdown("XML-RPC", 300, Boolean.parseBoolean(restart));
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}

	/**
	 * Отмена выключения\перезагрузки сервера
	 * @return {@code OK} если отмена удачна, {@code FAIL} если по каким-то причинам случилась ошибка
	 */
	public String abortRestartServer()
	{
		try
		{
			GameServerShutdown.getInstance().abort("XML-RPC");
			return json(MessageType.OK);
		}
		catch(Exception e)
		{
			return json(MessageType.FAILED);
		}
	}
}