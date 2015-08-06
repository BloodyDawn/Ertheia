package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class TeleportBookmark implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(playable == null || item == null || !(playable instanceof L2PcInstance))
		{
			return false;
		}

		L2PcInstance player = (L2PcInstance) playable;

		if(player.getBookMarkSlot() >= 9)
		{
			player.sendPacket(SystemMessageId.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT);
			return false;
		}

		player.destroyItem(ProcessType.CONSUME, item.getObjectId(), 1, null, false);

		player.setBookMarkSlot(player.getBookMarkSlot() + 3);
		player.sendPacket(SystemMessageId.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED);
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
		return true;
	}
}
