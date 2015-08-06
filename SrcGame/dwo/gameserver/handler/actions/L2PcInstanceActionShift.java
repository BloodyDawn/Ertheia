package dwo.gameserver.handler.actions;

import dwo.gameserver.handler.AdminCommandHandler;
import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

public class L2PcInstanceActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(activeChar.isGM())
		{
			// Check if the gm already target this l2pcinstance
			if(activeChar.getTarget() != target)
			{
				// Set the target of the L2PcInstance activeChar
				activeChar.setTarget(target);

				// Send a Server->Client packet MyTargetSelected to the L2PcInstance activeChar
				activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), 0));
			}

			// Send a Server->Client packet ValidateLocation to correct the L2PcInstance position and heading on the client
			if(!activeChar.equals(target))
			{
				activeChar.sendPacket(new ValidateLocation((L2Character) target));
			}

			IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler("admin_character_info");
			if(ach != null)
			{
				ach.useAdminCommand("admin_character_info " + target.getName(), activeChar);
			}
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2PcInstance.class;
	}
}