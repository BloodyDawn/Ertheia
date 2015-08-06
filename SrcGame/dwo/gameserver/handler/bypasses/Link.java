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
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import org.apache.log4j.Level;

/**
 * Hyper link handler.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class Link extends CommandHandler<String>
{
	@TextCommand
	public boolean link(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		if(params.getArgs().isEmpty())
		{
			return false;
		}

		try
		{
			String path = params.getArgs().get(0);

			if(path.contains(".."))
			{
				return false;
			}

			NpcHtmlMessage html = new NpcHtmlMessage(params.getTarget().getObjectId());
			html.setFile(params.getPlayer().getLang(), path);
			html.replace("%objectId%", String.valueOf(params.getTarget().getObjectId()));
			params.getPlayer().sendPacket(html);
			return true;
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
}