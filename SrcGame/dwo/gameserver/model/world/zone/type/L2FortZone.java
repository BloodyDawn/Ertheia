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

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportWhereType;

/**
 * A castle zone
 *
 * @author durgus
 */

public class L2FortZone extends L2ZoneRespawn
{
	private int _fortId;

	public L2FortZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if(name.equals("fortId"))
		{
			_fortId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_FORT, true);
	}

	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_FORT, false);
	}

	@Override
	public void onDieInside(L2Character character)
	{

	}

	@Override
	public void onReviveInside(L2Character character)
	{
	}

	public void updateZoneStatusForCharactersInside()
	{
	}

	/**
	 * Removes all foreigners from the fort
	 *
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		TeleportWhereType type = TeleportWhereType.FORTRESS_BANISH;
		for(L2PcInstance temp : getPlayersInside())
		{
			if(temp.getClanId() == owningClanId && owningClanId != 0)
			{
				continue;
			}

			temp.teleToLocation(type);
		}
	}

	public int getFortId()
	{
		return _fortId;
	}
}