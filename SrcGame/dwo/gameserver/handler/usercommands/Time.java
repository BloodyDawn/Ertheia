package dwo.gameserver.handler.usercommands;

import dwo.gameserver.GameTimeController;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

import java.text.SimpleDateFormat;

/**
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Time extends CommandHandler<Integer>
{
	private static final SimpleDateFormat fmt = new SimpleDateFormat("H:mm.");

	@NumericCommand(77)
	public boolean time(HandlerParams<Integer> params)
	{
		int t = GameTimeController.getInstance().getGameTime();
		String h = String.valueOf(t / 60 % 24);
		String m;
		m = t % 60 < 10 ? "0" + t % 60 : String.valueOf(t % 60);

		if(GameTimeController.getInstance().isNight())
		{
			params.getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_NIGHT).addString(h).addString(m));
		}
		else
		{
			params.getPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TIME_S1_S2_IN_THE_DAY).addString(h).addString(m));
		}
		return true;
	}
}
