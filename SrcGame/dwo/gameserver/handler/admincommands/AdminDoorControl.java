package dwo.gameserver.handler.admincommands;

import dwo.gameserver.engine.geodataengine.door.DoorGeoEngine;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.residence.castle.Castle;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This class handles following admin commands:
 * - openall = open all coloseum door
 * - closeall = close all coloseum door
 * - open = open selected door
 * - close = close selected door
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_open", "admin_close", "admin_openall", "admin_closeall", "admin_deleteall", "admin_door_check",
		"admin_door_repaint"
	};
	private static Logger _log = LogManager.getLogger(AdminDoorControl.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		try
		{
			if(command.startsWith("admin_open "))
			{
				int doorId = Integer.parseInt(command.substring(11));
				if(DoorGeoEngine.getInstance().getDoor(doorId) != null)
				{
					DoorGeoEngine.getInstance().getDoor(doorId).openMe();
				}
				else
				{
					CastleManager.getInstance().getCastles().stream().filter(castle -> castle.getDoor(doorId) != null).forEach(castle -> castle.getDoor(doorId).openMe());
				}
			}
			else if(command.startsWith("admin_close "))
			{
				int doorId = Integer.parseInt(command.substring(12));
				if(DoorGeoEngine.getInstance().getDoor(doorId) != null)
				{
					DoorGeoEngine.getInstance().getDoor(doorId).closeMe();
				}
				else
				{
					CastleManager.getInstance().getCastles().stream().filter(castle -> castle.getDoor(doorId) != null).forEach(castle -> castle.getDoor(doorId).closeMe());
				}
			}
			if(command.equals("admin_closeall"))
			{
				for(L2DoorInstance door : DoorGeoEngine.getInstance().getDoors())
				{
					door.closeMe();
				}
				for(Castle castle : CastleManager.getInstance().getCastles())
				{
					for(L2DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			if(command.equals("admin_openall"))
			{
				for(L2DoorInstance door : DoorGeoEngine.getInstance().getDoors())
				{
					door.openMe();
				}
				for(Castle castle : CastleManager.getInstance().getCastles())
				{
					for(L2DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			if(command.equals("admin_deleteall"))
			{
				for(L2DoorInstance door : DoorGeoEngine.getInstance().getDoors())
				{
					door.getLocationController().delete();
				}
				for(Castle castle : CastleManager.getInstance().getCastles())
				{
					for(L2DoorInstance door : castle.getDoors())
					{
						door.getLocationController().delete();
					}
				}
			}
			if(command.equals("admin_open"))
			{
				L2Object target = activeChar.getTarget();
				if(target instanceof L2DoorInstance)
				{
					((L2DoorInstance) target).openMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}

			if(command.equals("admin_close"))
			{
				L2Object target = activeChar.getTarget();
				if(target instanceof L2DoorInstance)
				{
					((L2DoorInstance) target).closeMe();
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}

			if(command.equals("admin_door_check"))
			{
				L2Object target = activeChar.getTarget();
				if(target instanceof L2DoorInstance)
				{
					activeChar.sendMessage("Painted? " + ((L2DoorInstance) target).isPainted());
					activeChar.sendMessage("AlikeDead? " + ((L2DoorInstance) target).isAlikeDead());
					activeChar.sendMessage("Open? " + ((L2DoorInstance) target).isOpened());
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}

			if(command.endsWith("admin_door_repaint"))
			{
				L2Object target = activeChar.getTarget();
				if(target instanceof L2DoorInstance)
				{
					DoorGeoEngine.getInstance().updateDoor((L2DoorInstance) target);
				}
				else
				{
					activeChar.sendMessage("Incorrect target.");
				}
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, getClass().getSimpleName() + " Error while parse command: " + command, e);
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}