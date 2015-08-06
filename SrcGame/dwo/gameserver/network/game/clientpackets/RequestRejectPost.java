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
package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.mail.MailManager;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.model.player.mail.MailMessageStatus;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExChangePostState;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExUnReadMailCount;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.actor.L2Character.ZONE_PEACE;

/**
 * @author Migi, DS
 */
public class RequestRejectPost extends L2GameClientPacket
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
		if(!Config.ALLOW_MAIL || !Config.ALLOW_ATTACHMENTS)
		{
			return;
		}

		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.MAIL_CANCEL))
		{
			return;
		}

		if(!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}

		MailMessage msg = MailManager.getInstance().getMessage(_msgId);
		if(msg != null)
		{
			if(msg.getReceiverId() != activeChar.getObjectId())
			{
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to reject not own attachment!", Config.DEFAULT_PUNISH);
				return;
			}

			if(!msg.hasAttachments() || msg.isFourStars() || msg.isSentBySystem())
			{
				return;
			}

			MailManager.getInstance().sendMessage(new MailMessage(msg));

			activeChar.sendPacket(SystemMessageId.MAIL_SUCCESSFULLY_RETURNED);
			activeChar.sendPacket(new ExChangePostState(true, _msgId, MailMessageStatus.REJECTED));
			activeChar.sendPacket(new ExUnReadMailCount(activeChar));

			L2PcInstance sender = WorldManager.getInstance().getPlayer(msg.getSenderId());
			if(sender != null)
			{
				sender.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RETURNED_MAIL).addCharName(activeChar));
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:6B RequestRejectPostAttachment";
	}
}