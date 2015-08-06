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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.L2GameClient;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.mmocore.ReceivablePacket;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.BufferUnderflowException;

/**
 * Packets received by the game server from clients
 *
 * @author KenM
 */
public abstract class L2GameClientPacket extends ReceivablePacket<L2GameClient>
{
	protected static final Logger _log = LogManager.getLogger(L2GameClientPacket.class);

	@Override
	public boolean read()
	{
		try
		{
			readImpl();
            if(Config.PACKET_HANDLER_DEBUG)
            {
                switch (getClass().getSimpleName())
                {
                    case "MoveBackwardToLocation":
                    case "ValidatePosition":
                        break;
                    default:
                        System.out.println("[C] " + getClass().getSimpleName());
                }
            }

			return true;
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Client: " + getClient() + " - Failed reading: " + getType() + " ; " + e.getMessage(), e);

			if(e instanceof BufferUnderflowException) // only one allowed per client per minute
			{
				getClient().onBufferUnderflow();
			}
		}
		return false;
	}

	protected abstract void readImpl();

	@Override
	public void run()
	{
		try
		{
			runImpl();

            /* Removes onspawn protection - player has faster computer than average
             * Since GE: True for all packets
             * except RequestItemList and UseItem (in case the item is a Scroll of Escape (736)
             */
			if(triggersOnActionRequest())
			{
				L2PcInstance actor = getClient().getActiveChar();
				if(actor != null && (actor.isSpawnProtected() || actor.isInvul()))
				{
					actor.onActionRequest();
				}
			}
		}
		catch(Throwable t)
		{
			_log.log(Level.ERROR, "Client: " + getClient() + " - Failed running: " + getType() + " ; " + t.getMessage(), t);
			// in case of EnterWorld error kick player from game
			if(this instanceof EnterWorld)
			{
				getClient().closeNow();
			}
		}
	}

	protected abstract void runImpl();

	protected void sendPacket(L2GameServerPacket gsp)
	{
		getClient().sendPacket(gsp);
	}

	/**
	 * @return A String with this packet name for debuging purposes
	 */
	public abstract String getType();

	/**
	 * Overriden with true value on some packets that should disable spawn protection
	 * (RequestItemList and UseItem only)
	 */
	protected boolean triggersOnActionRequest()
	{
		return true;
	}
}