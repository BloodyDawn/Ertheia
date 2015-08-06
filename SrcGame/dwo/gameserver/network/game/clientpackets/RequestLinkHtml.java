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

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import org.apache.log4j.Level;

/**
 * @author zabbix
 * Lets drink to code!
 */
public class RequestLinkHtml extends L2GameClientPacket
{
	private String _link;

	@Override
	protected void readImpl()
	{
		_link = readS();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance actor = getClient().getActiveChar();
		if(actor == null)
		{
			return;
		}

		if(_link.contains("..") || !_link.contains(".htm"))
		{
			_log.log(Level.WARN, "[RequestLinkHtml] hack? link contains prohibited characters: '" + _link + "', skipped");
			return;
		}
		try
		{
			NpcHtmlMessage msg = new NpcHtmlMessage(0);
			msg.setFile(actor.getLang(), _link);
			sendPacket(msg);
		}
		catch(Exception e)
		{
			_log.log(Level.WARN, "Bad RequestLinkHtml: ", e);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 22 RequestLinkHtml";
	}
}
