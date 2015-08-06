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
import dwo.gameserver.instancemanager.HellboundManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * Hellbound commands handler.
 *
 * @author DS
 * @author Yorie
 */
public class Hellbound extends CommandHandler<String>
{
	@TextCommand
	public boolean hellbound(HandlerParams<String> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		if(HellboundManager.getInstance().isLocked())
		{
			activeChar.sendMessage("Остров Ада сейчас недоступен.");
			return true;
		}

		int maxTrust = HellboundManager.getInstance().getMaxTrust();
		activeChar.sendMessage("Уровень Хеллбаунда: " + HellboundManager.getInstance().getLevel() + " Очки доверия: " + HellboundManager.getInstance().getTrust() + (maxTrust > 0 ? "/" + maxTrust : ""));
		return true;
	}
}
