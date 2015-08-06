/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExReplySentPost;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.actor.L2Character.ZONE_PEACE;

/**
 * @author Migi, DS
 */
public class RequestRequestSentPost extends L2GameClientPacket
{
	private int _msgId;

	@Override
	protected void readImpl()
	{
		_msgId = readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || !Config.ALLOW_MAIL)
		{
			return;
		}

		MailMessage msg = MailManager.getInstance().getMessage(_msgId);
		if(msg == null)
		{
			return;
		}

		if(!activeChar.isInsideZone(ZONE_PEACE) && msg.hasAttachments())
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}

		if(msg.getSenderId() != activeChar.getObjectId())
		{
			Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to read not own post!", Config.DEFAULT_PUNISH);
			return;
		}

		if(msg.isDeletedBySender())
		{
			return;
		}

		activeChar.sendPacket(new ExReplySentPost(msg));
	}

	@Override
	public String getType()
	{
		return "[C] D0:6E RequestSentPost";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}
