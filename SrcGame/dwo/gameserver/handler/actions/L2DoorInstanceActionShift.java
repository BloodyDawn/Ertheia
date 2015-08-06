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
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.MyTargetSelected;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.StaticObject;

public class L2DoorInstanceActionShift implements IActionHandler
{
	@Override
	public boolean action(L2PcInstance activeChar, L2Object target, boolean interact)
	{
		if(activeChar.getAccessLevel().isGm())
		{
			activeChar.setTarget(target);
			activeChar.sendPacket(new MyTargetSelected(target.getObjectId(), activeChar.getLevel()));

			StaticObject su;
			L2DoorInstance door = (L2DoorInstance) target;
			// send HP amount if doors are inside castle/fortress zone

			su = door.getCastle() != null && door.getCastle().getCastleId() > 0 || door.getFort() != null && door.getFort().getFortId() > 0 || door.getClanHall() != null && door.getClanHall().isSiegableHall() && !door.isCommanderDoor() ? new StaticObject(door, true) : new StaticObject(door, false);

			activeChar.sendPacket(su);

			NpcHtmlMessage html = new NpcHtmlMessage(0);
			html.setFile(activeChar.getLang(), "mods/admin/doorinfo.htm");
			html.replace("%class%", target.getClass().getSimpleName());
			html.replace("%hp%", String.valueOf((int) door.getCurrentHp()));
			html.replace("%hpmax%", String.valueOf(door.getMaxHp()));
			html.replace("%objid%", String.valueOf(target.getObjectId()));
			html.replace("%doorid%", String.valueOf(door.getDoorId()));

			html.replace("%minx%", String.valueOf(door.getX()));
			html.replace("%miny%", String.valueOf(door.getY()));
			html.replace("%minz%", String.valueOf(door.getZ()));

			html.replace("%maxx%", String.valueOf(door.getX()));
			html.replace("%maxy%", String.valueOf(door.getY()));
			html.replace("%maxz%", String.valueOf(door.getZ()));
			html.replace("%unlock%", door.isUnlockable() ? "<font color=00FF00>ДА<font>" : "<font color=FF0000>НЕТ</font>");
			html.replace("%fort_id%", String.valueOf(door.getFort() != null ? String.valueOf(door.getFort().getFortId()) : "<font color=FF0000>НЕТ</font>"));
			html.replace("%castle_id%", String.valueOf(door.getCastle() != null ? String.valueOf(door.getCastle().getCastleId()) : "<font color=FF0000>НЕТ</font>"));
			html.replace("%ch_id%", String.valueOf(door.getClanHall() != null ? String.valueOf(door.getClanHall().getId()) : "<font color=FF0000>НЕТ</font>"));
			activeChar.sendPacket(html);
		}
		return true;
	}

	@Override
	public Class<? extends L2Object> getInstanceType()
	{
		return L2DoorInstance.class;
	}
}