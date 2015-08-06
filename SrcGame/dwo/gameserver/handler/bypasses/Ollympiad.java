package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.events.EventManager;
import dwo.gameserver.model.actor.controller.player.ObserverController;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.olympiad.Olympiad;
import dwo.gameserver.model.world.olympiad.OlympiadGameManager;
import dwo.gameserver.model.world.olympiad.OlympiadGameTask;
import dwo.gameserver.model.world.olympiad.OlympiadManager;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExReceiveOlympiad;
import org.apache.log4j.Level;

/**
 * User: Bacek
 * Date: 09.05.13
 * Time: 19:39
 */
public class Ollympiad extends CommandHandler<String>
{
	@TextCommand("_olympiad")
	public boolean ollympiad(BypassHandlerParams params)
	{
		String command = params.getQueryArgs().get("command");
		L2PcInstance activeChar = params.getPlayer();
		try
		{
			if(command.startsWith("op_field_list"))
			{
				if(Olympiad.getInstance().inCompPeriod())
				{
					activeChar.sendPacket(new ExReceiveOlympiad());
				}
				else
				{
					activeChar.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS);
				}
			}
			else if(command.startsWith("move_op_field"))
			{
				if(OlympiadManager.getInstance().isRegisteredInComp(activeChar))
				{
					activeChar.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
				}
				else if(EventManager.isPlayerParticipant(activeChar))
				{
					activeChar.sendMessage("Вы не можете просмотривать игры Великой Олимпиады, участвуя в ивенте.");
				}
				else
				{
					if(!params.getQueryArgs().containsKey("field"))
					{
						return false;
					}

					try
					{
						int arenaId = Integer.parseInt(params.getQueryArgs().get("field")) - 1;
						OlympiadGameTask nextArena = OlympiadGameManager.getInstance().getOlympiadTask(arenaId);
						if(nextArena != null)
						{
							activeChar.getObserverController().enter(nextArena.getZone().getObserverSpawnLoc(), ObserverController.ObserveType.OLYMPIAD, arenaId);
						}
					}
					catch(Exception ignored)
					{
					}
				}
			}
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in " + e.getMessage(), e);
		}
		return true;
	}
}
