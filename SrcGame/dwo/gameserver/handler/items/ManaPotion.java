package dwo.gameserver.handler.items;

import dwo.config.Config;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;

public class ManaPotion extends ItemSkills
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!Config.ALLOW_MANA_POTIONS)
		{
			playable.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return false;
		}
		return super.useItem(playable, item, forceUse);
	}
}