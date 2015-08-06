package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleMercTicketManager;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class MercTicket implements IItemHandler
{
	/**
	 * handler for using mercenary tickets.  Things to do:
	 * 1) Check constraints:
	 * 1.a) Tickets may only be used in a castle
	 * 1.b) Only specific tickets may be used in each castle (different tickets for each castle)
	 * 1.c) only the owner of that castle may use them
	 * 1.d) tickets cannot be used during siege
	 * 1.e) Check if max number of tickets has been reached
	 * 1.f) Check if max number of tickets from this ticket's TYPE has been reached
	 * 2) If allowed, call the CastleMercTicketManager to add the item and spawn in the world
	 * 3) Remove the item from the person's inventory
	 */
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		int itemId = item.getItemId();
		L2PcInstance activeChar = (L2PcInstance) playable;
		Castle castle = CastleManager.getInstance().getCastle(activeChar);
		int castleId = -1;
		if(castle != null)
		{
			castleId = castle.getCastleId();
		}

		//add check that certain tickets can only be placed in certain castles
		if(CastleMercTicketManager.getInstance().getTicketCastleId(itemId) != castleId)
		{
			activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
			return false;
		}
		if(!activeChar.isCastleLord(castleId))
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES);
			return false;
		}
		if(castle.getSiege().isInProgress())
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}

		if(CastleMercTicketManager.getInstance().isAtCastleLimit(item.getItemId()))
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		if(CastleMercTicketManager.getInstance().isAtTypeLimit(item.getItemId()))
		{
			activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
			return false;
		}
		if(CastleMercTicketManager.getInstance().isTooCloseToAnotherTicket(activeChar.getX(), activeChar.getY(), activeChar.getZ()))
		{
			activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT);
			return false;
		}

		CastleMercTicketManager.getInstance().addTicket(item.getItemId(), activeChar, null);
		activeChar.destroyItem(ProcessType.CONSUME, item.getObjectId(), 1, null, false); // Remove item from char's inventory
		activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PLACE_CURRENT_LOCATION_DIRECTION).addItemName(item));
		return true;
	}
}
