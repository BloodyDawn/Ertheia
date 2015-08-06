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
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.friend.L2FriendSay;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Recieve Private (Friend) Message - 0xCC
 *
 * Format: c SS
 *
 * S: Message
 * S: Receiving Player
 *
 * @author Tempy
 *
 */
public class RequestSendFriendMsg extends L2GameClientPacket
{
	private static Logger _logChat = LogManager.getLogger("chat");

	private String _message;
	private String _reciever;

	@Override
	protected void readImpl()
	{
		_message = readS();
		_reciever = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		if(_message == null || _message.isEmpty() || _message.length() > 300)
		{
			return;
		}

		L2PcInstance targetPlayer = WorldManager.getInstance().getPlayer(_reciever);
		if(targetPlayer == null || !RelationListManager.getInstance().isInFriendList(targetPlayer, activeChar))
		{
			activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}

		if(Config.LOG_CHAT)
		{
			_logChat.log(Level.INFO, "PRIV_MSG" + '[' + activeChar.getName() + " to " + _reciever + ']' + _message);
		}

		targetPlayer.sendPacket(new L2FriendSay(activeChar.getName(), _reciever, _message));
	}

	@Override
	public String getType()
	{
		return "[C] CC RequestSendMsg";
	}
}