package dwo.gameserver.util;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPunishLevel;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class IllegalPlayerAction implements Runnable
{
	public static final int PUNISH_BROADCAST = 1;
	public static final int PUNISH_KICK = 2;
	public static final int PUNISH_KICKBAN = 3;
	public static final int PUNISH_JAIL = 4;
	private static Logger _logAudit = LogManager.getLogger("audit");
	private String _message;
	private int _punishment;
	private L2PcInstance _actor;

	public IllegalPlayerAction(L2PcInstance actor, String message, int punishment)
	{
		_message = message;
		_punishment = punishment;
		_actor = actor;

		switch(punishment)
		{
			case PUNISH_KICK:
				_actor.sendMessage("You will be kicked for illegal action, GM informed.");
				break;
			case PUNISH_KICKBAN:
				_actor.setAccessLevel(-100);
				_actor.setAccountAccesslevel(-100);
				_actor.sendMessage("You are banned for illegal action, GM informed.");
				break;
			case PUNISH_JAIL:
				_actor.sendMessage("Illegal action performed!");
				_actor.sendMessage("You will be teleported to GM Consultation Service area and jailed.");
				break;
		}
	}

	@Override
	public void run()
	{
		_logAudit.log(Level.INFO, "AUDIT:" + _message + ',' + _actor + ' ' + _punishment);

		AdminTable.getInstance().broadcastMessageToGMs(_message);

		switch(_punishment)
		{
			case PUNISH_BROADCAST:
				return;
			case PUNISH_KICK:
				_actor.logout(false);
				break;
			case PUNISH_KICKBAN:
				_actor.logout();
				break;
			case PUNISH_JAIL:
				_actor.setPunishLevel(PlayerPunishLevel.JAIL, Config.DEFAULT_PUNISH_PARAM);
				break;
		}
	}
}
