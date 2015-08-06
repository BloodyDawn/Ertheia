package dwo.gameserver.handler.items;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

public class Book implements IItemHandler
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

		String filename = "help/" + itemId + ".htm";
		String content = HtmCache.getInstance().getHtm(activeChar.getLang(), filename);

		if(content == null)
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><body>Текст не определен, обратитесь к Администрации:<br>" + filename + "</body></html>");
			activeChar.sendPacket(html);
		}
		else
		{
			NpcHtmlMessage itemReply = new NpcHtmlMessage(5, itemId);
			itemReply.setHtml(content);
			itemReply.disableValidation();
			activeChar.sendPacket(itemReply);
		}

		activeChar.sendActionFailed();
		return true;
	}
}
