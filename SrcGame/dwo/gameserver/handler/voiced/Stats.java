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
package dwo.gameserver.handler.voiced;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.util.StringUtil;

/**
 * Event info for player.
 *
 * @author L2J
 * @author Yorie
 */
public class Stats extends CommandHandler<String>
{
	@TextCommand
	public boolean stats(HandlerParams<String> params)
	{
		if(params.getArgs().isEmpty())
		{
			return false;
		}

		L2PcInstance pc = WorldManager.getInstance().getPlayer(params.getArgs().get(0));
		L2PcInstance activeChar = params.getPlayer();

		if(pc != null)
		{
			NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			StringBuilder replyMSG = StringUtil.startAppend(300 + pc.getEventController().getKillsCount() * 50, "<html><body>" + "<center><font color=\"LEVEL\">[ ДВИЖОК ИВЕНТОВ ]</font></center><br>" + "<br>Статистика для игрока <font color=\"LEVEL\">", pc.getName(), "</font><br>" + "Всего убийств <font color=\"FF0000\">", String.valueOf(pc.getEventController().getKillsCount()), "</font><br>" + "<br>Detailed list: <br>");

			for(String plr : pc.getEventController().getKills())
			{
				StringUtil.append(replyMSG, "<font color=\"FF0000\">", plr, "</font><br>");
			}

			replyMSG.append("</body></html>");

			adminReply.setHtml(replyMSG.toString());
			activeChar.sendPacket(adminReply);
		}

		return true;
	}
}
