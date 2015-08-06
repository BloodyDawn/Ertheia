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
package dwo.gameserver.model.world.zone.type;

import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.type.AuctionableHall;
import dwo.gameserver.network.game.serverpackets.AgitDecoInfo;
import org.apache.log4j.Level;

/**
 * A clan hall zone
 *
 * @author durgus
 */

public class L2ClanHallZone extends L2ZoneRespawn
{
	private int _clanHallId;

	public L2ClanHallZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("clanHallId"))
		{
			_clanHallId = Integer.parseInt(value);
			// Register self to the correct clan hall
			ClanHall hall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			if(hall == null)
			{
				_log.log(Level.WARN, "L2ClanHallZone: Clan hall with id " + _clanHallId + " does not exist!");
			}
			else
			{
				hall.setZone(this);
			}
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			// Set as in clan hall
			character.setInsideZone(L2Character.ZONE_CLANHALL, true);

			AuctionableHall clanHall = ClanHallManager.getInstance().getAuctionableHallById(_clanHallId);
			if(clanHall == null)
			{
				return;
			}

			// Send function packet
			AgitDecoInfo deco = new AgitDecoInfo(clanHall);
			character.sendPacket(deco);
		}
	}

	@Override
	protected void onExit(L2Character character)
	{
		if(character instanceof L2PcInstance)
		{
			// Unset clanhall zone
			character.setInsideZone(L2Character.ZONE_CLANHALL, false);
		}
	}

	@Override
	public void onDieInside(L2Character character)
	{
	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	/**
	 * Removes all foreigners from the clan hall
	 *
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		TeleportWhereType type = TeleportWhereType.CLANHALL_BANISH;
		for(L2PcInstance temp : getPlayersInside())
		{
			if(temp.getClanId() == owningClanId && owningClanId != 0)
			{
				continue;
			}

			temp.teleToLocation(type);
		}
	}

	/**
	 * @return the clanHallId
	 */
	public int getClanHallId()
	{
		return _clanHallId;
	}
}