package dwo.gameserver.handler.admincommands;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.clanhall.ClanHallSiegeManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.clanhall.ClanHallSiegeEngine;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.network.game.serverpackets.CastleSiegeInfo;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

import java.util.Calendar;

public class AdminCHSiege implements IAdminCommandHandler
{
	private static final String[] COMMANDS = {
		"admin_chsiege_siegablehall", "admin_chsiege_startSiege", "admin_chsiege_endsSiege",
		"admin_chsiege_setSiegeDate", "admin_chsiege_addAttacker", "admin_chsiege_removeAttacker",
		"admin_chsiege_clearAttackers", "admin_chsiege_listAttackers", "admin_chsiege_forwardSiege"
	};

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		String[] split = command.split(" ");
		ClanHallSiegable hall = null;
		if(Config.ALT_DEV_NO_QUESTS)
		{
			activeChar.sendMessage("AltDevNoQuests = true; Clan Hall Sieges are disabled!");
			return false;
		}
		if(split.length < 2)
		{
			activeChar.sendMessage("You have to specify the hall id at least");
			return false;
		}
		if((hall = getHall(split[1], activeChar)) == null)
		{
			activeChar.sendMessage("Couldnt find he desired siegable hall (" + split[1] + ')');
			return false;
		}
		if(hall.getSiege() == null)
		{
			activeChar.sendMessage("The given hall dont have any attached siege!");
			return false;
		}

		if(split[0].equals(COMMANDS[1]))
		{
			if(hall.isInSiege())
			{
				activeChar.sendMessage("The requested clan hall is alredy in siege!");
			}
			else
			{
				L2Clan owner = ClanTable.getInstance().getClan(hall.getOwnerId());
				if(owner != null)
				{
					hall.free();
					owner.setClanhallId(0);
					hall.addAttacker(owner);
				}
				hall.getSiege().startSiege();
			}
		}
		else if(split[0].equals(COMMANDS[2]))
		{
			if(hall.isInSiege())
			{
				hall.getSiege().endSiege();
			}
			else
			{
				activeChar.sendMessage("The requested clan hall isnt in siege!");
			}
		}
		else if(split[0].equals(COMMANDS[3]))
		{
			if(!hall.isRegistering())
			{
				activeChar.sendMessage("Cannot change siege date while hall is in siege");
			}
			else if(split.length < 3)
			{
				activeChar.sendMessage("The date format is incorrect. Try again.");
			}
			else
			{
				String[] rawDate = split[2].split(";");
				if(rawDate.length < 2)
				{
					activeChar.sendMessage("You have to specify this format DD-MM-YYYY;HH:MM");
				}
				else
				{
					String[] day = rawDate[0].split("-");
					String[] hour = rawDate[1].split(":");
					if(day.length < 3 || hour.length < 2)
					{
						activeChar.sendMessage("Incomplete day, hour or both!");
					}
					else
					{
						int d;
						int month;
						int year;
						int h;
						int min;
						try
						{
							d = Integer.parseInt(day[0]);
							month = Integer.parseInt(day[1]) - 1;
							year = Integer.parseInt(day[2]);
							h = Integer.parseInt(hour[0]);
							min = Integer.parseInt(hour[1]);
						}
						catch(NumberFormatException e)
						{
							activeChar.sendMessage("Wrong day/month/year gave!");
							return false;
						}
						if(month == 2 && d > 28 || d > 31 || d <= 0 || month <= 0 || month > 12 || year < Calendar.getInstance().get(Calendar.YEAR))
						{
							activeChar.sendMessage("Wrong day/month/year gave!");
						}
						else if(h <= 0 || h > 24 || min < 0 || min >= 60)
						{
							activeChar.sendMessage("Wrong hour/minutes gave!");
						}
						else
						{
							Calendar c = Calendar.getInstance();
							c.set(Calendar.YEAR, year);
							c.set(Calendar.MONTH, month);
							c.set(Calendar.DAY_OF_MONTH, d);
							c.set(Calendar.HOUR_OF_DAY, h);
							c.set(Calendar.MINUTE, min);
							c.set(Calendar.SECOND, 0);

							if(c.getTimeInMillis() > System.currentTimeMillis())
							{
								activeChar.sendMessage(hall.getName() + " siege: " + c.getTime());
								hall.setNextSiegeDate(c.getTimeInMillis());
								hall.getSiege().updateSiege();
								hall.updateDb();
							}
							else
							{
								activeChar.sendMessage("The given time is in the past!");
							}
						}
					}
				}
			}
		}
		else if(split[0].equals(COMMANDS[4]))
		{
			if(hall.isInSiege())
			{
				activeChar.sendMessage("The clan hall is in siege, cannot add attackers now.");
				return false;
			}

			L2Clan attacker = null;
			if(split.length < 3)
			{
				L2Object rawTarget = activeChar.getTarget();
				L2PcInstance target = null;
				if(rawTarget == null)
				{
					activeChar.sendMessage("You must target a clan member of the attacker!");
				}
				else if(!(rawTarget instanceof L2PcInstance))
				{
					activeChar.sendMessage("You must target a player with clan!");
				}
				else if((target = (L2PcInstance) rawTarget).getClan() == null)
				{
					activeChar.sendMessage("Your target does not have any clan!");
				}
				else if(hall.getSiege().checkIsAttacker(target.getClan()))
				{
					activeChar.sendMessage("Your target's clan is alredy participating!");
				}
				else
				{
					attacker = target.getClan();
				}
			}
			else
			{
				L2Clan rawClan = ClanTable.getInstance().getClanByName(split[2]);
				if(rawClan == null)
				{
					activeChar.sendMessage("The given clan does not exist!");
				}
				else if(hall.getSiege().checkIsAttacker(rawClan))
				{
					activeChar.sendMessage("The given clan is alredy participating!");
				}
				else
				{
					attacker = rawClan;
				}
			}

			if(attacker != null)
			{
				hall.addAttacker(attacker);
			}
		}
		else if(split[0].equals(COMMANDS[5]))
		{
			if(hall.isInSiege())
			{
				activeChar.sendMessage("The clan hall is in siege, cannot remove attackers now.");
				return false;
			}

			if(split.length < 3)
			{
				L2Object rawTarget = activeChar.getTarget();
				L2PcInstance target = null;
				if(rawTarget == null)
				{
					activeChar.sendMessage("You must target a clan member of the attacker!");
				}
				else if(!(rawTarget instanceof L2PcInstance))
				{
					activeChar.sendMessage("You must target a player with clan!");
				}
				else if((target = (L2PcInstance) rawTarget).getClan() == null)
				{
					activeChar.sendMessage("Your target does not have any clan!");
				}
				else if(!hall.getSiege().checkIsAttacker(target.getClan()))
				{
					activeChar.sendMessage("Your target's clan is not participating!");
				}
				else
				{
					hall.removeAttacker(target.getClan());
				}
			}
			else
			{
				L2Clan rawClan = ClanTable.getInstance().getClanByName(split[2]);
				if(rawClan == null)
				{
					activeChar.sendMessage("The given clan does not exist!");
				}
				else if(!hall.getSiege().checkIsAttacker(rawClan))
				{
					activeChar.sendMessage("The given clan is not participating!");
				}
				else
				{
					hall.removeAttacker(rawClan);
				}
			}
		}
		else if(split[0].equals(COMMANDS[6]))
		{
			if(hall.isInSiege())
			{
				activeChar.sendMessage("The requested hall is in siege right now, cannot clear attacker list!");
			}
			else
			{
				ClanHallSiegeEngine siegable = hall.getSiege();
				siegable.getAttackers().clear();
			}
		}
		else if(split[0].equals(COMMANDS[7]))
		{
			activeChar.sendPacket(new CastleSiegeInfo(hall));
		}
		else if(split[0].equals(COMMANDS[8]))
		{
			ClanHallSiegeEngine siegable = hall.getSiege();
			siegable.cancelSiegeTask();
			switch(hall.getSiegeStatus())
			{
				case REGISTERING:
					siegable.prepareOwner();
					break;
				case WAITING_BATTLE:
					siegable.startSiege();
					break;
				case RUNNING:
					siegable.endSiege();
					break;
			}
		}
		sendSiegableHallPage(activeChar, split[1], hall);
		return false;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return COMMANDS;
	}

	private ClanHallSiegable getHall(String id, L2PcInstance gm)
	{
		int ch;
		try
		{
			ch = Integer.parseInt(id);
		}
		catch(NumberFormatException e)
		{
			gm.sendMessage("Wrong clan hall id, unparseable id!");
			return null;
		}

		ClanHallSiegable hall = ClanHallSiegeManager.getInstance().getSiegableHall(ch);

		if(hall == null)
		{
			gm.sendMessage("Couldnt find the clan hall.");
		}

		return hall;
	}

	private void sendSiegableHallPage(L2PcInstance activeChar, String hallId, ClanHallSiegable hall)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(5);
		msg.setFile(activeChar.getLang(), "mods/admin/siegablehall.htm");
		msg.replace("%clanhallId%", hallId);
		msg.replace("%clanhallName%", hall.getName());
		if(hall.getOwnerId() > 0)
		{
			L2Clan owner = ClanTable.getInstance().getClan(hall.getOwnerId());
			if(owner != null)
			{
				msg.replace("%clanhallOwner%", owner.getName());
			}
			else
			{
				msg.replace("%clanhallOwner%", "No Owner");
			}
		}
		else
		{
			msg.replace("%clanhallOwner%", "No Owner");
		}
		activeChar.sendPacket(msg);
	}
}