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
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Support for /mount command.
 *
 * @author Tempy
 * @author Yorie
 */
public class Mount extends CommandHandler<Integer>
{
	@NumericCommand(61)
	public boolean mount(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();
		L2Summon mountable = null;

		if(!activeChar.getPets().isEmpty())
		{
			for(L2Summon pet : activeChar.getPets())
			{
				if(pet.isMountable())
				{
					mountable = pet;
					break;
				}
			}
		}
		return activeChar.mountPlayer(mountable);
	}
}
