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
package dwo.gameserver.model.actor.knownlist;

import dwo.gameserver.model.actor.L2Decoy;
import dwo.gameserver.model.actor.L2Object;

public class DecoyKnownList extends AttackableKnownList
{
	public DecoyKnownList(L2Decoy activeChar)
	{
		super(activeChar);
	}

	@Override
	public L2Decoy getActiveChar()
	{
		return (L2Decoy) super.getActiveChar();
	}

	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		if(object.equals(getActiveChar().getOwner()) || object.equals(getActiveChar().getTarget()))
		{
			return 6000;
		}
		return 3000;
	}

	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 1500;
	}
}
