package dwo.gameserver.handler;

import dwo.gameserver.handler.admincommands.AdminAdmin;
import dwo.gameserver.handler.admincommands.AdminAnnouncements;
import dwo.gameserver.handler.admincommands.AdminBBS;
import dwo.gameserver.handler.admincommands.AdminBan;
import dwo.gameserver.handler.admincommands.AdminBuffs;
import dwo.gameserver.handler.admincommands.AdminCHSiege;
import dwo.gameserver.handler.admincommands.AdminCache;
import dwo.gameserver.handler.admincommands.AdminCamera;
import dwo.gameserver.handler.admincommands.AdminChangeAccessLevel;
import dwo.gameserver.handler.admincommands.AdminClan;
import dwo.gameserver.handler.admincommands.AdminClanSearch;
import dwo.gameserver.handler.admincommands.AdminCreateItem;
import dwo.gameserver.handler.admincommands.AdminCursedWeapons;
import dwo.gameserver.handler.admincommands.AdminDebug;
import dwo.gameserver.handler.admincommands.AdminDelete;
import dwo.gameserver.handler.admincommands.AdminDisconnect;
import dwo.gameserver.handler.admincommands.AdminDoorControl;
import dwo.gameserver.handler.admincommands.AdminEditChar;
import dwo.gameserver.handler.admincommands.AdminEditNpc;
import dwo.gameserver.handler.admincommands.AdminEffects;
import dwo.gameserver.handler.admincommands.AdminElement;
import dwo.gameserver.handler.admincommands.AdminEnchant;
import dwo.gameserver.handler.admincommands.AdminEventCTF;
import dwo.gameserver.handler.admincommands.AdminEventEngine;
import dwo.gameserver.handler.admincommands.AdminEventKOTH;
import dwo.gameserver.handler.admincommands.AdminEventTvT;
import dwo.gameserver.handler.admincommands.AdminExpSp;
import dwo.gameserver.handler.admincommands.AdminFightCalculator;
import dwo.gameserver.handler.admincommands.AdminFortSiege;
import dwo.gameserver.handler.admincommands.AdminGameCampain;
import dwo.gameserver.handler.admincommands.AdminGeodata;
import dwo.gameserver.handler.admincommands.AdminGm;
import dwo.gameserver.handler.admincommands.AdminGmChat;
import dwo.gameserver.handler.admincommands.AdminGraciaSeeds;
import dwo.gameserver.handler.admincommands.AdminHWID;
import dwo.gameserver.handler.admincommands.AdminHardwareInfo;
import dwo.gameserver.handler.admincommands.AdminHeal;
import dwo.gameserver.handler.admincommands.AdminHellbound;
import dwo.gameserver.handler.admincommands.AdminHelpPage;
import dwo.gameserver.handler.admincommands.AdminInstance;
import dwo.gameserver.handler.admincommands.AdminInstanceZone;
import dwo.gameserver.handler.admincommands.AdminInvul;
import dwo.gameserver.handler.admincommands.AdminKick;
import dwo.gameserver.handler.admincommands.AdminKill;
import dwo.gameserver.handler.admincommands.AdminLevel;
import dwo.gameserver.handler.admincommands.AdminLogin;
import dwo.gameserver.handler.admincommands.AdminMammon;
import dwo.gameserver.handler.admincommands.AdminManor;
import dwo.gameserver.handler.admincommands.AdminMenu;
import dwo.gameserver.handler.admincommands.AdminMessages;
import dwo.gameserver.handler.admincommands.AdminMobGroup;
import dwo.gameserver.handler.admincommands.AdminPackets;
import dwo.gameserver.handler.admincommands.AdminPathNode;
import dwo.gameserver.handler.admincommands.AdminPetition;
import dwo.gameserver.handler.admincommands.AdminPledge;
import dwo.gameserver.handler.admincommands.AdminPolymorph;
import dwo.gameserver.handler.admincommands.AdminPrimeShop;
import dwo.gameserver.handler.admincommands.AdminQuest;
import dwo.gameserver.handler.admincommands.AdminReload;
import dwo.gameserver.handler.admincommands.AdminRepairChar;
import dwo.gameserver.handler.admincommands.AdminRes;
import dwo.gameserver.handler.admincommands.AdminRide;
import dwo.gameserver.handler.admincommands.AdminShop;
import dwo.gameserver.handler.admincommands.AdminShowQuests;
import dwo.gameserver.handler.admincommands.AdminShutdown;
import dwo.gameserver.handler.admincommands.AdminSiege;
import dwo.gameserver.handler.admincommands.AdminSkill;
import dwo.gameserver.handler.admincommands.AdminSpawn;
import dwo.gameserver.handler.admincommands.AdminSummon;
import dwo.gameserver.handler.admincommands.AdminTarget;
import dwo.gameserver.handler.admincommands.AdminTargetSay;
import dwo.gameserver.handler.admincommands.AdminTeleport;
import dwo.gameserver.handler.admincommands.AdminTest;
import dwo.gameserver.handler.admincommands.AdminUnblockIp;
import dwo.gameserver.handler.admincommands.AdminVitality;
import dwo.gameserver.handler.admincommands.AdminZone;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AdminCommandHandler implements IHandler<IAdminCommandHandler, String>
{
	private static Logger _log = LogManager.getLogger(AdminCommandHandler.class);

	private final Map<String, IAdminCommandHandler> _handlers;

	private AdminCommandHandler()
	{
		_handlers = new HashMap<>();
		registerHandler(new AdminAdmin());
		registerHandler(new AdminAnnouncements());
		registerHandler(new AdminBan());
		registerHandler(new AdminBBS());
		registerHandler(new AdminBuffs());
		registerHandler(new AdminCache());
		registerHandler(new AdminCamera());
		registerHandler(new AdminChangeAccessLevel());
		registerHandler(new AdminClan());
		registerHandler(new AdminCreateItem());
		registerHandler(new AdminCursedWeapons());
		registerHandler(new AdminDebug());
		registerHandler(new AdminDelete());
		registerHandler(new AdminDisconnect());
		registerHandler(new AdminDoorControl());
		registerHandler(new AdminEditChar());
		registerHandler(new AdminEditNpc());
		registerHandler(new AdminEffects());
		registerHandler(new AdminElement());
		registerHandler(new AdminEnchant());
		registerHandler(new AdminEventEngine());
		registerHandler(new AdminEventCTF());
		registerHandler(new AdminEventKOTH());
		registerHandler(new AdminEventTvT());
		registerHandler(new AdminExpSp());
		registerHandler(new AdminFightCalculator());
		registerHandler(new AdminFortSiege());
		registerHandler(new AdminHardwareInfo());
		registerHandler(new AdminGeodata());
		registerHandler(new AdminGm());
		registerHandler(new AdminGmChat());
		registerHandler(new AdminGraciaSeeds());
		registerHandler(new AdminHeal());
		registerHandler(new AdminHellbound());
		registerHandler(new AdminHelpPage());
		registerHandler(new AdminInstance());
		registerHandler(new AdminInstanceZone());
		registerHandler(new AdminInvul());
		registerHandler(new AdminKick());
		registerHandler(new AdminKill());
		registerHandler(new AdminLevel());
		registerHandler(new AdminLogin());
		registerHandler(new AdminMammon());
		registerHandler(new AdminManor());
		registerHandler(new AdminMenu());
		registerHandler(new AdminMessages());
		registerHandler(new AdminMobGroup());
		registerHandler(new AdminPathNode());
		registerHandler(new AdminPetition());
		registerHandler(new AdminPledge());
		registerHandler(new AdminPolymorph());
		registerHandler(new AdminPrimeShop());
		registerHandler(new AdminQuest());
		registerHandler(new AdminReload());
		registerHandler(new AdminRepairChar());
		registerHandler(new AdminRes());
		registerHandler(new AdminRide());
		registerHandler(new AdminShop());
		registerHandler(new AdminShowQuests());
		registerHandler(new AdminShutdown());
		registerHandler(new AdminSiege());
		registerHandler(new AdminCHSiege());
		registerHandler(new AdminSkill());
		registerHandler(new AdminSpawn());
		registerHandler(new AdminSummon());
		registerHandler(new AdminTarget());
		registerHandler(new AdminTargetSay());
		registerHandler(new AdminTeleport());
		registerHandler(new AdminTest());
		registerHandler(new AdminUnblockIp());
		registerHandler(new AdminVitality());
		registerHandler(new AdminZone());
		registerHandler(new AdminHWID());
		registerHandler(new AdminGameCampain());
		registerHandler(new AdminPackets());
		registerHandler(new AdminClanSearch());
		_log.log(Level.INFO, "Loaded " + size() + " Admin Command Handlers");
	}

	public static AdminCommandHandler getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void registerHandler(IAdminCommandHandler handler)
	{
		String[] ids = handler.getAdminCommandList();
		for(String id : ids)
		{
			_handlers.put(id, handler);
		}
	}

	@Override
	public void removeHandler(IAdminCommandHandler handler)
	{
		synchronized(this)
		{
			String[] ids = handler.getAdminCommandList();
			for(String id : ids)
			{
				_handlers.remove(id);
			}
		}
	}

	@Override
	public IAdminCommandHandler getHandler(String adminCommand)
	{
		String command = adminCommand;
		if(adminCommand.contains(" "))
		{
			command = adminCommand.substring(0, adminCommand.indexOf(' '));
		}
		return _handlers.get(command);
	}

	@Override
	public int size()
	{
		return _handlers.size();
	}

	private static class SingletonHolder
	{
		protected static final AdminCommandHandler _instance = new AdminCommandHandler();
	}
}
