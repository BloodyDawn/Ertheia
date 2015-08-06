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
import org.apache.log4j.Level;

/**
 * Show chat window command.
 *
 * @author L2J
 * @author GODWORLD
 * @author Yorie
 */
public class ChatLink extends CommandHandler<String>
{
	@TextCommand
	public boolean chat(BypassHandlerParams params)
	{
		if(!(params.getTarget() instanceof L2Npc))
		{
			return false;
		}

		int val = 0;
		try
		{
			val = Integer.parseInt(params.getArgs().get(0));
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "", e);
		}
		((L2Npc) params.getTarget()).showChatWindow(params.getPlayer(), val);
		return true;
	}
}