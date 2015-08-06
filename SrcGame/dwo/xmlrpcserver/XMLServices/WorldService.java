package dwo.xmlrpcserver.XMLServices;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.xmlrpcserver.XMLUtils;
import dwo.xmlrpcserver.model.Message.MessageType;
import dwo.xmlrpcserver.model.ServerRates;

public class WorldService extends Base
{
	/**
	 * Пустой метод для запроса на сервер, чтобы понять, запущен он или нет.
	 * @return
	 */
	public String idle()
	{
		return json(MessageType.OK);
	}

	/**
	 * @return количество игроков онлайн
	 */
	public String getOnlinePlayersCount()
	{
		return json(String.valueOf(WorldManager.getInstance().getAllPlayersCount()));
	}

	/**
	 * Список рейтов сервера.
	 * @return
	 */
	public String getRates()
	{
		return json(new ServerRates(Config.RATE_XP, Config.RATE_SP, Config.RATE_QUEST_REWARD, Config.RATE_QUEST_DROP, Config.RATE_DROP_ITEMS, Config.RATE_DROP_SPOIL, Config.RATE_DROP_ITEMS_BY_RAID));
	}

	/**
	 * @return сериализованные инстансы игроков онлайн
	 */
	public String getAllOnlinePlayersInfo()
	{
		StringBuilder result = new StringBuilder();
		result.append("");
		result.append("<charlist>");
		for(L2PcInstance pc : WorldManager.getInstance().getAllPlayersArray())
		{
			if(pc.isVisible())
			{
				result.append(XMLUtils.serializePlayer(pc, false));
			}
		}
		result.append("</charlist>");
		return json(result);
	}
}
