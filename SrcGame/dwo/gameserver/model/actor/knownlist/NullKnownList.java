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

import dwo.gameserver.model.actor.L2Object;

public class NullKnownList extends ObjectKnownList
{
	public NullKnownList(L2Object activeObject)
	{
		super(activeObject);
	}

	@Override
	public boolean addKnownObject(L2Object object)
	{
		return false;
	}

	@Override
	public void removeAllKnownObjects()
	{
	}

	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		return false;
	}

	@Override
	public L2Object getActiveObject()
	{
		return super.getActiveObject();
	}
}