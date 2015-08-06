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
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.StaticObject;
import dwo.gameserver.util.StringUtil;

public class L2StaticObjectInstanceActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(activeChar.getAccessLevel().isGm())
		{

			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel()));
			activeChar.setTarget(target);

			StaticObject su = new StaticObject((L2StaticObjectInstance) target);
			activeChar.sendPacket(su);

			NpcHtmlMessage html = new NpcHtmlMessage(target.getObjectId());
			String html1 = StringUtil.concat("<html><body><center><font color=\"LEVEL\">Static Object Info</font></center><br><table border=0><tr><td>Coords X,Y,Z: </td><td>", String.valueOf(target.getX()), ", ", String.valueOf(target.getY()), ", ", String.valueOf(target.getZ()), "</td></tr><tr><td>Object ID: </td><td>", String.valueOf(target.getObjectId()), "</td></tr><tr><td>Static Object ID: </td><td>", String.valueOf(((L2StaticObjectInstance) target).getStaticObjectId()), "</td></tr><tr><td>Mesh Index: </td><td>", String.valueOf(((L2StaticObjectInstance) target).getMeshIndex()), "</td></tr><tr><td><br></td></tr><tr><td>Class: </td><td>", target.getClass().getSimpleName(), "</td></tr></table></body></html>");
			html.setHtml(html1);
			activeChar.sendPacket(html);
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2StaticObjectInstance.class;
	}
}
