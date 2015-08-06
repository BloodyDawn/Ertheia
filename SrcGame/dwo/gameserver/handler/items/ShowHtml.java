package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.TutorialShowHtml;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 19.10.11
 * Time: 16:59
 */

public class ShowHtml implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		String html = "";
		switch(item.getItemId())
		{
			case 32777:
				html = "..\\L2text\\Guide_Ad.htm";
				break;
			case 32778:
				html = "..\\L2text\\Guide_Aw.htm";
				break;
		}
		playable.sendPacket(new TutorialShowHtml(2, html));
		return true;
	}
}


