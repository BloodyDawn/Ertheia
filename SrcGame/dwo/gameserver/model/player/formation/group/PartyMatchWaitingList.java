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
package dwo.gameserver.model.player.formation.group;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import javolution.util.FastList;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Gnacik
 */
public class PartyMatchWaitingList
{
	private List<L2PcInstance> _members;

	private PartyMatchWaitingList()
	{
		_members = new FastList<>();
	}

	public static PartyMatchWaitingList getInstance()
	{
		return SingletonHolder._instance;
	}

	public void addPlayer(L2PcInstance player)
	{
		// player.setPartyWait(1);
		if(!_members.contains(player))
		{
			_members.add(player);
		}
	}

	public void removePlayer(L2PcInstance player)
	{
		//player.setPartyWait(0);
		if(_members.contains(player))
		{
			_members.remove(player);
		}
	}

	public List<L2PcInstance> getPlayers()
	{
		return _members;
	}

	public List<L2PcInstance> getWaitingList(int minLevel, int maxLevel, int[] classes)
	{
		List<L2PcInstance> res = _members.stream().filter($member -> $member.getLevel() >= minLevel && $member.getLevel() <= maxLevel).collect(Collectors.toList());
		return res;
	}

	private static class SingletonHolder
	{
		protected static final PartyMatchWaitingList _instance = new PartyMatchWaitingList();
	}
}