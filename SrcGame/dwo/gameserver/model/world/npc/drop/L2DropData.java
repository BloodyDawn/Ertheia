package dwo.gameserver.model.world.npc.drop;

import java.util.Arrays;

public class L2DropData
{
	public static final int MAX_CHANCE = 1000000;

	private int id;
	private int min;
	private int max;
	private double chance;
	private String _questID;
	private String[] _stateID;

	/**
	 * Returns the ID of the item dropped
	 *
	 * @return int
	 */
	public int getItemId()
	{
		return id;
	}

	/**
	 * Sets the ID of the item dropped
	 *
	 * @param itemId : int designating the ID of the item
	 */
	public void setItemId(int itemId)
	{
		id = itemId;
	}

	/**
	 * Returns the minimum quantity of items dropped
	 *
	 * @return int
	 */
	public int getMinDrop()
	{
		return min;
	}

	/**
	 * Sets the value for minimal quantity of dropped items
	 *
	 * @param mindrop : int designating the quantity
	 */
	public void setMinDrop(int mindrop)
	{
		min = mindrop;
	}

	/**
	 * Returns the maximum quantity of items dropped
	 *
	 * @return int
	 */
	public int getMaxDrop()
	{
		return max;
	}

	/**
	 * Sets the value for maximal quantity of dopped items
	 *
	 * @param maxdrop : int designating the quantity of dropped items
	 */
	public void setMaxDrop(int maxdrop)
	{
		max = maxdrop;
	}

	/**
	 * Returns the chance of having a drop
	 *
	 * @return int
	 */
	public double getChance()
	{
		return chance;
	}

	/**
	 * Sets the chance of having the item for a drop
	 *
	 * @param chance : int designating the chance
	 */
	public void setChance(double chance)
	{
		this.chance = chance;
	}

	/**
	 * Returns the stateID.
	 *
	 * @return String[]
	 */
	public String[] getStateIDs()
	{
		return _stateID;
	}

	/**
	 * Adds states of the dropped item
	 *
	 * @param list : String[]
	 */
	public void addStates(String[] list)
	{
		_stateID = list;
	}

	/**
	 * Returns the questID.
	 *
	 * @return String designating the ID of the quest
	 */
	public String getQuestID()
	{
		return _questID;
	}

	/**
	 * Sets the questID
	 *
	 * @param questID String designating the questID to set.
	 */
	public void setQuestID(String questID)
	{
		_questID = questID;
	}

	/**
	 * Returns if the dropped item is requested for a quest
	 *
	 * @return boolean
	 */
	public boolean isQuestDrop()
	{
		return _questID != null && _stateID != null;
	}

	@Override
	public int hashCode()
	{
		int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
		{
			return true;
		}
		if(obj == null)
		{
			return false;
		}
		if(!(obj instanceof L2DropData))
		{
			return false;
		}
		L2DropData other = (L2DropData) obj;
		return id == other.id;
	}

	/**
	 * Returns a report of the object
	 *
	 * @return String
	 */
	@Override
	public String toString()
	{
		String out = "ItemID: " + id + " Min: " + min +
			" Max: " + max + " Chance: " + chance / 10000.0 + '%';
		if(isQuestDrop())
		{
			out += " QuestID: " + _questID + " StateID's: " + Arrays.toString(_stateID);
		}

		return out;
	}
}
