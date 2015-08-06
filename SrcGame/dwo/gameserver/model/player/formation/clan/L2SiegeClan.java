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
package dwo.gameserver.model.player.formation.clan;

import dwo.gameserver.model.actor.L2Npc;
import javolution.util.FastList;

import java.util.List;

public class L2SiegeClan
{
	// ==========================================================================================
	// Instance
	// ===============================================================
	// Data Field
	private int _clanId;
	private List<L2Npc> _flag = new FastList<>();
	private int _numFlagsAdded;
	private SiegeClanType _type;

	public L2SiegeClan(int clanId, SiegeClanType type)
	{
		_clanId = clanId;
		_type = type;
	}

	// =========================================================
	// Constructor

	// =========================================================
	// Method - Public
	public int getNumFlags()
	{
		return _numFlagsAdded;
	}

	public void addFlag(L2Npc flag)
	{
		_numFlagsAdded++;
		getFlag().add(flag);
	}

	public boolean removeFlag(L2Npc flag)
	{
		if(flag == null)
		{
			return false;
		}
		boolean ret = getFlag().remove(flag);
		//check if null objects or duplicates remain in the list.
		//for some reason, this might be happening sometimes...
		// delete false duplicates: if this flag got deleted, delete its copies too.
		if(ret)
		{
			while(getFlag().remove(flag))
			{
				//
			}
		}

		flag.getLocationController().delete();
		_numFlagsAdded--;
		return ret;
	}

	public void removeFlags()
	{
		getFlag().forEach(this::removeFlag);
	}

	// =========================================================
	// Property
	public int getClanId()
	{
		return _clanId;
	}

	public List<L2Npc> getFlag()
	{
		if(_flag == null)
		{
			_flag = new FastList<>();
		}
		return _flag;
	}

	public SiegeClanType getType()
	{
		return _type;
	}

	public void setType(SiegeClanType setType)
	{
		_type = setType;
	}

	public enum SiegeClanType
	{
		OWNER,
		DEFENDER,
		ATTACKER,
		DEFENDER_PENDING
	}
}
