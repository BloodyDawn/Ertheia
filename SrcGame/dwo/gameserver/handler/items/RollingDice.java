package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.Dice;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.Rnd;

public class RollingDice implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;
		int itemId = item.getItemId();

		if(activeChar.getOlympiadController().isParticipating())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		if(itemId == 4625 || itemId == 4626 || itemId == 4627 || itemId == 4628)
		{
			int number = rollDice(activeChar);
			if(number == 0)
			{
				activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER);
				return false;
			}

			Broadcast.toSelfAndKnownPlayers(activeChar, new Dice(activeChar.getObjectId(), item.getItemId(), number, activeChar.getX() - 30, activeChar.getY() - 30, activeChar.getZ()));

			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ROLLED_S2).addString(activeChar.getName()).addNumber(number);
			activeChar.sendPacket(sm);
			if(activeChar.isInsideZone(L2Character.ZONE_PEACE))
			{
				Broadcast.toKnownPlayers(activeChar, sm);
			}
			else if(activeChar.isInParty())
			{
				activeChar.getParty().broadcastPacket(activeChar, sm);
			}
		}
		return true;
	}

	private int rollDice(L2PcInstance player)
	{
		// Check if the dice is ready
		if(!player.getFloodProtectors().getRollDice().tryPerformAction(FloodAction.ROLLING_DICE))
		{
			return 0;
		}
		return Rnd.get(1, 6);
	}
}
