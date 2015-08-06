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
package dwo.gameserver.instancemanager.events.KOTH;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import javolution.util.FastMap;

import java.util.Map;

/**
 * @author Nik
 * Some of the code is taken and modified from FBIagent's TvT Event,
 * to make this event more comfortable for the users when they set
 * the configuration for the event, and for the players when they
 * participate in it.
 */
public class KOTHEventTeam
{
	public Map<Integer, L2PcInstance> _deadPlayers = new FastMap<>();
	private String _name;
	private int[] _coordinates = new int[3];
	private long _points;
	private Map<Integer, L2PcInstance> _registered = new FastMap<>();
	private Map<Integer, L2PcInstance> _playersOnTheHill = new FastMap<>();

	public KOTHEventTeam(String name, int[] coordinates)
	{
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}

	public boolean addPlayer(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}

		synchronized(_registered)
		{
			_registered.put(player.getObjectId(), player);
		}

		return true;
	}

	public boolean addPlayerOnTheHill(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}
		if(!containsRegisteredPlayer(player.getObjectId()))
		{
			return false;
		}

		synchronized(_playersOnTheHill)
		{
			_playersOnTheHill.put(player.getObjectId(), player);
		}

		return true;
	}

	public boolean addDeadPlayer(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}
		if(!containsRegisteredPlayer(player.getObjectId()))
		{
			return false;
		}

		synchronized(_deadPlayers)
		{
			_deadPlayers.put(player.getObjectId(), player);
		}

		return true;
	}

	public void removePlayer(int playerObjectId)
	{
		synchronized(_registered)
		{
			_registered.remove(playerObjectId);
		}
	}

	public boolean removePlayerOnTheHill(int playerObjectId)
	{
		synchronized(_playersOnTheHill)
		{
			_playersOnTheHill.remove(playerObjectId);
		}

		return true;
	}

	public boolean removeDeadPlayer(int playerObjectId)
	{
		synchronized(_deadPlayers)
		{
			_deadPlayers.remove(playerObjectId);
		}

		return true;
	}

	public void increasePoints(long points)
	{
		_points += points;
	}

	public void clean()
	{
		_registered.clear();
		_registered = new FastMap<>();
		_playersOnTheHill.clear();
		_playersOnTheHill = new FastMap<>();
		_deadPlayers.clear();
		_deadPlayers = new FastMap<>();
		_points = 0;
	}

	public boolean containsRegisteredPlayer(int playerObjectId)
	{
		boolean containsPlayer;

		synchronized(_registered)
		{
			containsPlayer = _registered.containsKey(playerObjectId);
		}

		return containsPlayer;
	}

	// getters and setters
	public String getName()
	{
		return _name;
	}

	public int[] getCoordinates()
	{
		return _coordinates;
	}

	public long getPoints()
	{
		return _points;
	}

	public Map<Integer, L2PcInstance> getRegisteredPlayers()
	{
		Map<Integer, L2PcInstance> registered = null;

		synchronized(_registered)
		{
			registered = _registered;
		}

		return registered;
	}

	public int getRegisteredPlayersCount()
	{
		int registeredPlayersCount;

		synchronized(_registered)
		{
			registeredPlayersCount = _registered.size();
		}

		return registeredPlayersCount;
	}

	public int getPlayersOnTheHillCount()
	{
		int PlayersOnTheHillCount;

		synchronized(_playersOnTheHill)
		{
			PlayersOnTheHillCount = _playersOnTheHill.size();
		}

		return PlayersOnTheHillCount;
	}
}
