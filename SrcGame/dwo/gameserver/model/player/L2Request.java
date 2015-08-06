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
package dwo.gameserver.model.player;

import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * This class manages requests (transactions) between two L2PcInstance.
 *
 * @author kriau
 */
public class L2Request
{
	private static final int REQUEST_TIMEOUT = 15; //in secs

	protected L2PcInstance _player;
	protected L2PcInstance _partner;
	protected boolean _isRequestor;
	protected boolean _isAnswerer;
	protected L2GameClientPacket _requestPacket;

	public L2Request(L2PcInstance player)
	{
		_player = player;
	}

	protected void clear()
	{
		_partner = null;
		_requestPacket = null;
		_isRequestor = false;
		_isAnswerer = false;
	}

	/**
	 * Return the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	public L2PcInstance getPartner()
	{
		return _partner;
	}

	/**
	 * Set the L2PcInstance member of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).<BR><BR>
	 */
	private void setPartner(L2PcInstance partner)
	{
		synchronized(this)
		{
			_partner = partner;
		}
	}

	/**
	 * Return the packet originally incomed from requestor.<BR><BR>
	 */
	public L2GameClientPacket getRequestPacket()
	{
		return _requestPacket;
	}

	/**
	 * Set the packet incomed from requestor.<BR><BR>
	 */
	private void setRequestPacket(L2GameClientPacket packet)
	{
		synchronized(this)
		{
			_requestPacket = packet;
		}
	}

	/**
	 * Checks if request can be made and in success case puts both PC on request state.<BR><BR>
	 */
	public boolean setRequest(L2PcInstance partner, L2GameClientPacket packet)
	{
		synchronized(this)
		{
			if(partner == null)
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET));
				return false;
			}
			if(partner.getRequest().isProcessingRequest())
			{
				_player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(partner.getName()));
				return false;
			}
			if(isProcessingRequest())
			{
				_player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
				return false;
			}

			_partner = partner;
			_requestPacket = packet;
			setOnRequestTimer(true);
			_partner.getRequest().setPartner(_player);
			_partner.getRequest().setRequestPacket(packet);
			_partner.getRequest().setOnRequestTimer(false);
			return true;
		}
	}

	private void setOnRequestTimer(boolean isRequestor)
	{
		_isRequestor = isRequestor;
		_isAnswerer = !isRequestor;
		ThreadPoolManager.getInstance().scheduleGeneral(this::clear, REQUEST_TIMEOUT * 1000);
	}

	/**
	 * Clears PC request state. Should be called after answer packet receive.<BR><BR>
	 */
	public void onRequestResponse()
	{
		if(_partner != null)
		{
			_partner.getRequest().clear();
		}
		clear();
	}

	/**
	 * Return True if a transaction is in progress.<BR><BR>
	 */
	public boolean isProcessingRequest()
	{
		return _partner != null;
	}
}
