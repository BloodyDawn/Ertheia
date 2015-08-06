/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.handler.actions;

import dwo.gameserver.handler.IActionHandler;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;

public class L2ItemInstanceActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(activeChar.getAccessLevel().isGm())
		{
			NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
			String html1 = StringUtil.concat("<html><body><center><font color=\"LEVEL\">Item Info</font></center><br><table border=0>", "<tr><td>Object ID: </td><td>", String.valueOf(target.getObjectId()), "</td></tr><tr><td>Item ID: </td><td>", String.valueOf(((L2ItemInstance) target).getItemId()), "</td></tr><tr><td>Owner ID: </td><td>", String.valueOf(((L2ItemInstance) target).getOwnerId()), "</td></tr><tr><td>Location: </td><td>", String.valueOf(((L2ItemInstance) target).getItemLocation()), "</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>", target.getClass().getSimpleName(), "</td></tr></table></body></html>");
			html.setHtml(html1);
			activeChar.sendPacket(html);
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2ItemInstance.class;
	}
}
