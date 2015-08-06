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
package dwo.gameserver.handler.bypasses;

import dwo.gameserver.handler.BypassHandlerParams;
import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import org.apache.log4j.Level;

import java.util.StringTokenizer;

/**
 * Player help command handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class PlayerHelp extends CommandHandler<String>
{
	@TextCommand("player_help")
	public boolean playerHelp(BypassHandlerParams params)
	{
		try
		{
			if(params.getArgs().isEmpty())
			{
				return false;
			}

			String path = params.getArgs().get(0);
			if(path.contains(".."))
			{
				return false;
			}

			StringTokenizer st = new StringTokenizer(path);
			String[] cmd = st.nextToken().split("#");

			NpcHtmlMessage html;
			if(cmd.length > 1)
			{
				int itemId = Integer.parseInt(cmd[1]);
				html = new NpcHtmlMessage(1, itemId);
			}
			else
			{
				html = new NpcHtmlMessage(1);
			}

			html.setFile(params.getPlayer().getLang(), "help/" + cmd[0]);
			html.disableValidation();
			params.getPlayer().sendPacket(html);
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in " + e.getMessage(), e);
		}
		return true;
	}
}