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

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExRpItemLink;
import org.apache.log4j.Level;

/**
 * @author KenM
 */

public class RequestExRqItemLink extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2GameClient client = getClient();
		if(client != null)
		{
			L2Object object = WorldManager.getInstance().findObject(_objectId);
			if(object instanceof L2ItemInstance)
			{
				L2ItemInstance item = (L2ItemInstance) object;
				if(item.isPublished())
				{
					client.sendPacket(new ExRpItemLink(item));
				}
				else
				{
					if(Config.DEBUG)
					{
						_log.log(Level.DEBUG, getClient() + " requested [C] DO:1E RequestExRqItemLink for item which wasnt published! ID:" + _objectId);
					}
				}
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] DO:1E RequestExRqItemLink";
	}
}
