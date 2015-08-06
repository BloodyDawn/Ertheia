package dwo.gameserver.instancemanager.events.CTF;

import dwo.config.events.ConfigEventCTF;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import javolution.util.FastMap;

import java.util.Collection;
import java.util.Map;

public class CTFEventTeam
{
	private static long lastFlagTaken;
	private final String _name;
	private final Map<Integer, L2PcInstance> _registered = new FastMap<Integer, L2PcInstance>().shared();
	private final Map<Integer, L2PcInstance> _deadPlayers = new FastMap<Integer, L2PcInstance>().shared();
	private final int _id;
	private int[] _coordinates = new int[3];
	private byte _points;
	private L2ItemInstance flag;
	private boolean flagOnPlace = true;

	public CTFEventTeam(int id, String name, int[] coordinates)
	{
		_id = id;
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}

	public int getId()
	{
		return _id;
	}

	public boolean addPlayer(L2PcInstance player)
	{
		if(player == null)
		{
			return false;
		}
		if(player.getInventory().getItemByItemId(ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID) != null)
		{
			player.destroyItemByItemId(ProcessType.EVENT, ConfigEventCTF.CTF_EVENT_FLAG_ITEM_ID, 1, player, true);
		}
		return _registered.put(player.getObjectId(), player) != null;
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
		return _deadPlayers.put(player.getObjectId(), player) != null;
	}

	public void removePlayer(int playerObjectId)
	{
		_registered.remove(playerObjectId);
	}

	public boolean removeDeadPlayer(int playerObjectId)
	{
		return _deadPlayers.remove(playerObjectId) != null;
	}

	public void increasePoints()
	{
		++_points;
	}

	public void clean()
	{
		_registered.values().stream().filter(CTFEvent::isFlagOwner).forEach(player -> CTFEvent.removeFlagFromPlayer(player, false));
		_registered.clear();
		_deadPlayers.clear();
		_points = 0;
		flag = null;
		flagOnPlace = true;
	}

	public boolean containsRegisteredPlayer(int playerObjectId)
	{
		return _registered.containsKey(playerObjectId);
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

	public byte getPoints()
	{
		return _points;
	}

	public Map<Integer, L2PcInstance> getRegisteredPlayers()
	{
		return _registered;
	}

	public int getRegisteredPlayersCount()
	{
		return _registered.size();
	}

	public L2ItemInstance getFlag()
	{
		return flag;
	}

	public void setFlag(L2ItemInstance item)
	{
		flag = item;
	}

	public int getFlagObjectId()
	{
		return flag != null ? flag.getObjectId() : -1;
	}

	public boolean isFlagOnPlace()
	{
		return flagOnPlace;
	}

	public void setFlagOnPlace(boolean set)
	{
		flagOnPlace = set;
	}

	public long getLastFlagTaken()
	{
		return lastFlagTaken + ConfigEventCTF.CTF_EVENT_FLAG_EXPIRE_TIME;
	}

	public void setLastFlagTaken()
	{
		lastFlagTaken = System.currentTimeMillis();
	}

	public Collection<L2PcInstance> getDeadPlayers()
	{
		return _deadPlayers.values();
	}
}