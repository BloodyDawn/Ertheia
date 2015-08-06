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
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.TextCommand;
import dwo.gameserver.handler.VoicedHandlerManager;
import javolution.util.FastList;
import javolution.util.FastMap;

import java.util.List;

/**
 * Voiced dialog commands handler.
 *
 * @author DS
 * @author Yorie
 */
public class VoiceCommand extends CommandHandler<String>
{
	@TextCommand
	public boolean voice(BypassHandlerParams params)
	{
		// only voice commands allowed
		if(!params.getArgs().isEmpty() && !params.getArgs().get(0).isEmpty() && params.getArgs().get(0).charAt(0) == '.')
		{
			String command = params.getArgs().get(0);

			if(command.isEmpty())
			{
				return false;
			}

			command = command.substring(1);

			List<String> args = params.getArgs().size() > 1 ? params.getArgs().subList(1, params.getArgs().size()) : new FastList<>();

			if(!command.isEmpty())
			{
				VoicedHandlerManager.getInstance().execute(new HandlerParams<>(params.getPlayer(), command, args, new FastMap<>()));
			}
		}
		return false;
	}
}