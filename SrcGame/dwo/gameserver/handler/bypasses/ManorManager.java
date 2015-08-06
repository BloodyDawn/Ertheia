package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.BuyListSeed;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowCropInfo;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowCropSetting;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowManorDefaultInfo;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowProcureCropDetail;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowSeedInfo;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowSeedSetting;
import dwo.gameserver.network.game.serverpackets.packet.show.ExShowSellCropList;
import org.apache.log4j.Level;

/**
 * Manor functions handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class ManorManager extends CommandHandler<String>
{
	@TextCommand("manor_menu_select")
	public boolean manorMenu(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		L2Npc manager = activeChar.getLastFolkNPC();
		if(!activeChar.isInsideRadius(manager, L2Npc.INTERACTION_DISTANCE, true, false))
		{
			return false;
		}

		try
		{
			Castle castle = manager.getCastle();

			if(CastleManorManager.getInstance().isUnderMaintenance())
			{
				activeChar.sendActionFailed();
				activeChar.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE);
				return true;
			}

			int ask = Integer.parseInt(params.getQueryArgs().get("ask"));
			int state = Integer.parseInt(params.getQueryArgs().get("state"));
			int time = Integer.parseInt(params.getQueryArgs().get("time"));

			int castleId;
			castleId = state < 0 ? castle.getCastleId() : state;

			switch(ask)
			{
				case 1: // Seed purchase
					if(castleId == castle.getCastleId())
					{
						activeChar.sendPacket(new BuyListSeed(activeChar.getAdenaCount(), castleId, castle.getSeedProduction(CastleManorManager.PERIOD_CURRENT)));
					}
					else
					{
						activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR).addString(manager.getCastle().getName()));
					}
					break;
				case 2: // Crop sales
					activeChar.sendPacket(new ExShowSellCropList(activeChar, castleId, castle.getCropProcure(CastleManorManager.PERIOD_CURRENT)));
					break;
				case 3: // Current seeds (Manor info)
					if(time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						activeChar.sendPacket(new ExShowSeedInfo(castleId, null));
					}
					else
					{
						activeChar.sendPacket(new ExShowSeedInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getSeedProduction(time)));
					}
					break;
				case 4: // Current crops (Manor info)
					if(time == 1 && !CastleManager.getInstance().getCastleById(castleId).isNextPeriodApproved())
					{
						activeChar.sendPacket(new ExShowCropInfo(castleId, null));
					}
					else
					{
						activeChar.sendPacket(new ExShowCropInfo(castleId, CastleManager.getInstance().getCastleById(castleId).getCropProcure(time)));
					}
					break;
				case 5: // Basic info (Manor info)
					activeChar.sendPacket(new ExShowManorDefaultInfo());
					break;
				case 6: // Buy harvester
					manager.showBuyList(activeChar, 0);
					break;
				case 7: // Edit seed setup
					if(castle.isNextPeriodApproved())
					{
						activeChar.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
					}
					else
					{
						activeChar.sendPacket(new ExShowSeedSetting(castle.getCastleId()));
					}
					break;
				case 8: // Edit crop setup
					if(castle.isNextPeriodApproved())
					{
						activeChar.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM);
					}
					else
					{
						activeChar.sendPacket(new ExShowCropSetting(castle.getCastleId()));
					}
					break;
				case 9: // Edit sales (Crop sales)
					activeChar.sendPacket(new ExShowProcureCropDetail(state));
					break;
				default:
					return false;
			}
			return true;
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, e);
		}
		return false;
	}
}