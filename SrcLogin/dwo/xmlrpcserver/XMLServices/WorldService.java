package dwo.xmlrpcserver.XMLServices;

import dwo.loginserver.GameServerTable;
import dwo.xmlrpcserver.model.GameServerInfo;
import dwo.xmlrpcserver.model.Message;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

public class WorldService extends Base
{
	public String serverList()
	{
		List<GameServerInfo> gameserves = new FastList(0);

		gameserves.addAll(GameServerTable.getInstance().getRegisteredGameServers().values().stream().map(gsi -> new GameServerInfo(gsi.getId(), gsi.isAuthed(), GameServerTable.getInstance().getServerNameById(gsi.getId()), gsi.getExternalHost(), gsi.getPort())).collect(Collectors.toList()));

		return json(gameserves);
	}

	/**
	 * Пустой метод для запроса на сервер, чтобы понять, запущен он или нет.
	 * @return
	 */
	public String idle()
	{
		return json(Message.MessageType.OK);
	}
}
