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
package dwo.gameserver.handler.admincommands;

import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * This class handles following admin commands:
 * - gmshop = shows menu
 * - buy id = shows shop with respective id
 *
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShop implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS = {
		"admin_buy", "admin_gmshop"
	};
	private static Logger _log = LogManager.getLogger(AdminShop.class);

	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if(activeChar == null || !activeChar.getPcAdmin().canUseAdminCommand())
		{
			return false;
		}

		if(command.startsWith("admin_buy"))
		{
			try
			{
				handleBuyRequest(activeChar, command.substring(10));
			}
			catch(IndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify buylist.");
			}
		}
		else if(command.equals("admin_gmshop"))
		{
			AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
		}
		return true;
	}

	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}

	private void handleBuyRequest(L2PcInstance activeChar, String command)
	{
		int val = -1;
		try
		{
			val = Integer.parseInt(command);
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "admin buylist failed:" + command);
		}

        /*L2TradeList list = BuylistTable.getInstance().getBuyList(val);

        if (list != null)
        {
            //activeChar.sendPacket(new BuyList(list, activeChar.getAdenaCount(), 0));
            activeChar.sendPacket(new ExBuySellList(activeChar, list, ProcessType.BUY, 0, false, activeChar.getAdenaCount()));
			activeChar.sendPacket(new ExBuySellList(activeChar, list, ProcessType.SELL, 0, false, activeChar.getAdenaCount()));
            if (Config.DEBUG)
                _log.log(Level.DEBUG, "GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM shop id " + val);
        }
        else
        {
            _log.log(Level.WARN, "no buylist with id:" + val);
        } */
		activeChar.sendActionFailed();
	}
}
