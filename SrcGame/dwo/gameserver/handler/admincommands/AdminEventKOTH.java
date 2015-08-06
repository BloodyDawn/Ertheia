package dwo.gameserver.handler.admincommands;

import dwo.config.events.ConfigEventKOTH;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEvent;
import dwo.gameserver.instancemanager.events.KOTH.KOTHEventTeleporter;
import dwo.gameserver.instancemanager.events.KOTH.KOTHManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;

public class AdminEventKOTH implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_koth_add", "admin_koth_remove", "admin_koth_advance"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		switch(command)
		{
			case "admin_koth_add":
			{
				L2Object target = activeChar.getTarget();

				if(!(target instanceof L2PcInstance))
				{
					activeChar.sendMessage("You should select a player!");
					return true;
				}

				add(activeChar, (L2PcInstance) target);
				break;
			}
			case "admin_koth_remove":
				L2Object target = activeChar.getTarget();

				if(!(target instanceof L2PcInstance))
				{
					activeChar.sendMessage("You should select a player!");
					return true;
				}

				remove(activeChar, (L2PcInstance) target);
				break;
			case "admin_koth_advance":
				KOTHManager.getInstance().skipDelay();
				break;
		}

		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void add(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if(KOTHEvent.isPlayerParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}

		if(!KOTHEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}

		if(KOTHEvent.isStarted())
		{
			new KOTHEventTeleporter(playerInstance, KOTHEvent.getParticipantTeamCoordinates(playerInstance.getObjectId()), true, false);
		}
	}

	private void remove(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if(!KOTHEvent.removeParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}

		new KOTHEventTeleporter(playerInstance, ConfigEventKOTH.KOTH_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}