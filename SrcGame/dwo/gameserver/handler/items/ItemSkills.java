package dwo.gameserver.handler.items;

import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;

public class ItemSkills extends ItemSkillsTemplate
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		L2PcInstance activeChar = playable.getActingPlayer();
		if(activeChar != null && activeChar.getOlympiadController().isParticipating())
		{
			activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}

		return super.useItem(playable, item, forceUse);
	}
}
