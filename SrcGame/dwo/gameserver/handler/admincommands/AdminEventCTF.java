package dwo.gameserver.handler.admincommands;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.events.CTF.CTFEvent;
import dwo.gameserver.instancemanager.events.CTF.CTFEventTeleporter;
import dwo.gameserver.instancemanager.events.CTF.CTFManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;

public class AdminEventCTF implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_ctf_add", "admin_ctf_remove", "admin_ctf_advance"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		switch(command)
		{
			case "admin_ctf_add":
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
			case "admin_ctf_remove":
				L2Object target = activeChar.getTarget();

				if(!(target instanceof L2PcInstance))
				{
					activeChar.sendMessage("You should select a player!");
					return true;
				}

				remove(activeChar, (L2PcInstance) target);
				break;
			case "admin_ctf_advance":
				CTFManager.getInstance().skipDelay();
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
		if(CTFEvent.isPlayerParticipant(playerInstance.getObjectId()))
		{
			activeChar.sendMessage("Player already participated in the event!");
			return;
		}

		if(!CTFEvent.addParticipant(playerInstance))
		{
			activeChar.sendMessage("Player instance could not be added, it seems to be null!");
			return;
		}

		if(CTFEvent.isStarted())
		{
			new CTFEventTeleporter(playerInstance, CTFEvent.getParticipantTeamCoordinates(playerInstance.getObjectId()), true, false);
		}
	}

	private void remove(L2PcInstance activeChar, L2PcInstance playerInstance)
	{
		if(!CTFEvent.removeParticipant(playerInstance))
		{
			activeChar.sendMessage("Player is not part of the event!");
			return;
		}

		new CTFEventTeleporter(playerInstance, ConfigEventCTF.CTF_EVENT_PARTICIPATION_NPC_COORDINATES, true, true);
	}
}