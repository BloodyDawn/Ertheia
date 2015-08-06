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
import dwo.gameserver.model.player.mail.MailMessageStatus;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExChangePostState;
import dwo.gameserver.network.game.serverpackets.packet.mail.ExUnReadMailCount;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.actor.L2Character.ZONE_PEACE;

/**
 * @author Migi, DS
 */
public class RequestDeleteSentPost extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 4; // length of the one item

	int[] _msgIds;

	@Override
	protected void readImpl()
	{
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_msgIds = new int[count];
		for(int i = 0; i < count; i++)
		{
			_msgIds[i] = readD();
		}
	}

	@Override
	public void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null || _msgIds == null || !Config.ALLOW_MAIL)
		{
			return;
		}

		if(!activeChar.isInsideZone(ZONE_PEACE))
		{
			activeChar.sendPacket(SystemMessageId.CANT_USE_MAIL_OUTSIDE_PEACE_ZONE);
			return;
		}

		for(int msgId : _msgIds)
		{
			MailMessage msg = MailManager.getInstance().getMessage(msgId);
			if(msg != null)
			{
				if(msg.getSenderId() != activeChar.getObjectId())
				{
					Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " tried to delete not own post!", Config.DEFAULT_PUNISH);
					return;
				}

				if(msg.hasAttachments() || msg.isDeletedBySender())
				{
					return;
				}

				msg.setDeletedBySender();
			}
		}
		activeChar.sendPacket(new ExChangePostState(false, _msgIds, MailMessageStatus.DELETED));
		activeChar.sendPacket(new ExUnReadMailCount(activeChar));
	}

	@Override
	public String getType()
	{
		return "[C] D0:6D RequestDeleteSentPost";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}