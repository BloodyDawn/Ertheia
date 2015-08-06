package dwo.gameserver.handler.usercommands;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.instancemanager.MapRegionManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.world.zone.type.L2RespawnZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * Location command handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Loc extends CommandHandler<Integer>
{
	@NumericCommand(0)
	public boolean location(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		int region;
		L2RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, L2RespawnZone.class);

		region = zone != null ? MapRegionManager.getInstance().getRestartRegion(activeChar, zone.getAllRespawnPoints().get(Race.Human)).getLocId() : MapRegionManager.getInstance().getMapRegionLocId(activeChar);

		SystemMessage sm;
		if(region > 0)
		{
			sm = SystemMessage.getSystemMessage(region);
			if(sm.getSystemMessageId().getParamCount() == 3)
			{
				sm.addNumber(activeChar.getX());
				sm.addNumber(activeChar.getY());
				sm.addNumber(activeChar.getZ());
			}
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.CURRENT_LOCATION_S1);
			sm.addString(activeChar.getX() + ", " + activeChar.getY() + ", " + activeChar.getZ());
		}

		activeChar.sendPacket(sm);
		return true;
	}
}
