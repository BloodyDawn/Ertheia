package dwo.gameserver.handler.bypasses;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.TeleportListTable;
import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.TownManager;
import dwo.gameserver.instancemanager.castle.CastleSiegeManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.teleport.TeleportLocation;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.SystemMessageId;
import org.apache.log4j.Level;

import java.util.Calendar;

/**
 * Teleport command handler.
 *
 * @author Bacek
 * @author Keiichi
 * @author Yorie
 */
public class Teleport extends CommandHandler<String>
{
	@TextCommand("tp")
	public boolean teleport(BypassHandlerParams params)
	{
		L2PcInstance activeChar = params.getPlayer();
		L2Character target = params.getTarget();

		if(activeChar.getTransformationId() == 111 || activeChar.getTransformationId() == 112 || activeChar.getTransformationId() == 124)
		{
			return false;
		}

		int teleportListGroup = Integer.parseInt(params.getArgs().get(0));
		int teleportListIndex = Integer.parseInt(params.getArgs().get(1));

		// Проверяем текущего НПЦ с тем, который был сохранен как последний который показывал список (objectID+номер группы телепортов)
		if(activeChar.getLastTeleporterObjectId() != target.getObjectId() + teleportListGroup)
		{
			return false;
		}

		TeleportLocation[] list = TeleportListTable.getInstance().getTeleportLocationList(((L2NpcInstance) target).getNpcId(), teleportListGroup);
		if(list == null || list.length < teleportListIndex + 1)
		{
			log.log(Level.ERROR, "TELEPORT_LIST ERROR SIZE " + teleportListIndex + " FOR NPCID: " + ((L2NpcInstance) target).getNpcId() + " LINK: " + teleportListGroup);
			activeChar.sendActionFailed();
			return false;
		}
		if(!validateTeleport((L2NpcInstance) target, activeChar, list[teleportListIndex].getLocation()))
		{
			return false;
		}

		if(activeChar.isFlyingMounted())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ENTER_SEED_IN_FLYING_TRANSFORM);
			return false;
		}

		int price = list[teleportListIndex].getPrice();
		if(list[teleportListIndex].getItemId() == PcInventory.ADENA_ID)
		{
			price = activeChar.getLevel() <= 41 || Config.ALT_GAME_FREE_TELEPORT ? 0 : list[teleportListIndex].getPrice();
			if(list[teleportListIndex].getPrice() > 0 && price > 0)
			{
				int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
				int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
				if(day != 1 && day != 7 && (hour <= 8 || hour >= 24))
				{
					price /= 2;
				}
			}
		}
		if(Config.ALT_GAME_FREE_TELEPORT || activeChar.destroyItemByItemId(ProcessType.NPC, list[teleportListIndex].getItemId(), price, activeChar, true))
		{
			activeChar.teleToLocation(list[teleportListIndex].getLocation(), true);
		}
		return true;
	}

	public boolean validateTeleport(L2Npc npc, L2PcInstance player, Location loc)
	{
		if(CastleSiegeManager.getInstance().getSiege(loc.getX(), loc.getY(), loc.getZ()) != null)
		{
			player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
			return false;
		}
		if(player.hasBadReputation())
		{
			return false;
		}
		if(TownManager.townHasCastleInSiege(loc.getX(), loc.getY()) && npc.isInsideZone(L2Character.ZONE_TOWN))
		{
			player.sendPacket(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE);
			return false;
		}
		if(player.isCombatFlagEquipped())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
			return false;
		}
		return !player.isAlikeDead();

	}
}