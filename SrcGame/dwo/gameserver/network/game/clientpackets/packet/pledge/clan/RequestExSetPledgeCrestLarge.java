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
package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.cache.CrestCache;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.CrestBuilderHolder;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExSetPledgeEmblemAck;
import org.apache.log4j.Level;

public class RequestExSetPledgeCrestLarge extends L2GameClientPacket
{
	private int _length;
	private int _part;
	private int _total;
	private byte[] _data;
	private int maxImageLenght = 14336;

	@Override
	protected void readImpl()
	{
		_part = readD();
		_total = readD();
		_length = readD();
		if(_length > maxImageLenght)
		{
			return;
		}
		_data = new byte[_length];
		readB(_data);
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2Clan clan = activeChar.getClan();
		if(clan == null)
		{
			return;
		}

		if(_length < 0 || _length > maxImageLenght)
		{
			return;
		}

		boolean updated = false;
		int crestLargeId = -1;
		if((activeChar.getClanPrivileges() & L2Clan.CP_CL_REGISTER_CREST) == L2Clan.CP_CL_REGISTER_CREST)
		{
			if(_length == 0 || _data == null)
			{
				if(clan.getCrestLargeId() == 0)
				{
					return;
				}

				crestLargeId = 0;
				activeChar.sendMessage("Значек был удален.");
				updated = true;
			}
			else
			{
				CrestBuilderHolder crestHolder;
				if(_length == maxImageLenght)
				{
					if(_part == 0)
					{
						CrestCache.getInstance().createCrestLargeBuffer(activeChar.getClanId(), _data);
					}
					else
					{
						crestHolder = CrestCache.getInstance().getCrestLargeBuffer(activeChar.getClanId());
						crestHolder.appendToBuffer(_data);
					}
					sendPacket(new ExSetPledgeEmblemAck(_part + 1));
					return;
				}
				if(_part == 0)
				{
					crestHolder = CrestCache.getInstance().createCrestLargeBuffer(activeChar.getClanId(), _data);
				}
				else
				{
					crestHolder = CrestCache.getInstance().getCrestLargeBuffer(activeChar.getClanId());
					crestHolder.appendToBuffer(_data);
				}

				if(clan.getCastleId() == 0 && clan.getClanhallId() == 0)
				{
					activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items"); //there is a system message for that but didnt found the id
					return;
				}

				crestLargeId = IdFactory.getInstance().getNextId();
				if(!CrestCache.getInstance().savePledgeCrestLarge(crestLargeId, crestHolder.getCrest()))
				{
					_log.log(Level.INFO, "Error saving large crest for clan " + clan.getName() + " [" + clan.getClanId() + ']');
					return;
				}

				activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
				updated = true;
			}
		}

		if(updated && crestLargeId != -1)
		{
			clan.changeLargeCrest(crestLargeId);
		}
		CrestCache.getInstance().removeCrestLargeBuffer(activeChar.getClanId());
	}

	@Override
	public String getType()
	{
		return "[C] D0:11 RequestExSetPledgeCrestLarge";
	}
}
