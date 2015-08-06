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
package dwo.gameserver.handler.usercommands;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

import java.util.Calendar;

/**
 * Support for /mybirthday command.
 *
 * @author JIV
 * @author Yorie
 */
public class Birthday extends CommandHandler<Integer>
{
	@NumericCommand(126)
	public boolean myBurthday(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		Calendar date = activeChar.getCreateDate();

		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_BIRTHDAY_IS_S3_S4_S2);
		sm.addPcName(activeChar);
		sm.addString(Integer.toString(date.get(Calendar.YEAR)));
		sm.addString(Integer.toString(date.get(Calendar.MONTH) + 1));
		sm.addString(Integer.toString(date.get(Calendar.DATE)));

		activeChar.sendPacket(sm);

		return true;
	}
}
