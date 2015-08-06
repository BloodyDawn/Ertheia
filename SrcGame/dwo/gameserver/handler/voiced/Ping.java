package dwo.gameserver.handler.voiced;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.serverpackets.NetPing;

/**
 * Ping test command handler.
 *
 * @author ANZO
 * @author Yorie
 */
public class Ping extends CommandHandler<String>
{
	@TextCommand
	public boolean ping(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(!activeChar.getFloodProtectors().getTransaction().tryPerformAction(FloodAction.PING))
		{
			return false;
		}
		activeChar.sendMessage("Обработка запроса...");
		activeChar.sendPacket(new NetPing(activeChar));
		ThreadPoolManager.getInstance().scheduleGeneral(new AnswerTask(activeChar), 3000);

		return true;
	}

	static class AnswerTask implements Runnable
	{
		private final L2PcInstance _player;

		public AnswerTask(L2PcInstance player)
		{
			_player = player;
		}

		@Override
		public void run()
		{
			int ping = _player.getPing();
			if(ping == -1)
			{
				_player.sendMessage("Данные от клиента всё ещё не получены.");
			}
			else
			{
				_player.sendMessage("Текущий пинг до сервера: " + ping + " мс.");
			}
		}
	}
}
