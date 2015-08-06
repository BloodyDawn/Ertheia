package dwo.gameserver.handler.items;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.09.12
 * Time: 20:18
 */

public class QuestStart implements IItemHandler
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
		String itemServerName = item.getItemServerName();
		int questId = item.getQuestId();

		if(itemServerName.isEmpty() || questId == 0)
		{
			return true;
		}

		String filename = "default/" + itemServerName + "001.htm";
		String content = HtmCache.getInstance().getHtm(activeChar.getLang(), filename);
		if(content == null)
		{
			filename = "default/" + itemServerName + "_q" + questId + "_01.htm";
			content = HtmCache.getInstance().getHtm(activeChar.getLang(), filename);

			if(content == null)
			{
				filename = "default/" + itemId + ".htm";
				content = HtmCache.getInstance().getHtm(activeChar.getLang(), filename);
			}
		}

		if(content != null)
		{
			NpcHtmlMessage itemReply = new NpcHtmlMessage(5, itemId);
			content = content.replace("<?quest_id?>", String.valueOf(questId));
			itemReply.setHtml(content);
			itemReply.disableValidation();
			activeChar.sendPacket(itemReply);
		}
		else
		{
			NpcHtmlMessage html = new NpcHtmlMessage(1);
			html.setHtml("<html><body>Текст не определен, обратитесь к Администрации:<br>" + filename + "</body></html>");
			activeChar.sendPacket(html);
		}

		return true;
	}
}