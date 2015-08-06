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

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.util.Util;
import dwo.gameserver.util.arrays.L2FastMap;
import javolution.util.FastList;

import java.util.*;
import java.util.stream.Collectors;

public class CharKnownList extends ObjectKnownList
{
	private Map<Integer, L2PcInstance> _knownPlayers;
	private Map<Integer, L2Summon> _knownSummons;
	private Map<Integer, Integer> _knownRelations;

	public CharKnownList(L2Character activeChar)
	{
		super(activeChar);
	}

	@Override
	public boolean addKnownObject(L2Object object)
	{
		if(!super.addKnownObject(object))
		{
			return false;
		}
		if(object.isPlayer())
		{
			getKnownPlayers().put(object.getObjectId(), object.getActingPlayer());
			getKnownRelations().put(object.getObjectId(), -1);
		}
		else if(object.isSummon())
		{
			getKnownSummons().put(object.getObjectId(), (L2Summon) object);
		}

		return true;
	}

	/**
	 * Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attack or Cast and notify AI.
	 */
	@Override
	public void removeAllKnownObjects()
	{
		super.removeAllKnownObjects();
		getKnownPlayers().clear();
		getKnownRelations().clear();
		getKnownSummons().clear();

		// Set _target of the L2Character to null
		// Cancel Attack or Cast
		getActiveChar().setTarget(null);

		// Cancel AI Task
		if(getActiveChar().hasAI())
		{
			getActiveChar().setAI(null);
		}
	}

	@Override
	protected boolean removeKnownObject(L2Object object, boolean forget)
	{
		if(!super.removeKnownObject(object, forget))
		{
			return false;
		}

		if(!forget) // on forget objects removed by iterator
		{
			if(object.isPlayer())
			{
				getKnownPlayers().remove(object.getObjectId());
				getKnownRelations().remove(object.getObjectId());
			}
			else if(object.isSummon())
			{
				getKnownSummons().remove(object.getObjectId());
			}
		}

		// If object is targeted by the L2Character, cancel Attack or Cast
		if(object.equals(getActiveChar().getTarget()))
		{
			getActiveChar().setTarget(null);
		}

		return true;
	}

	@Override
	public void forgetObjects(boolean fullCheck)
	{
		if(!fullCheck)
		{
			Collection<L2PcInstance> plrs = getKnownPlayers().values();
			Iterator<L2PcInstance> pIter = plrs.iterator();
			L2PcInstance player;
			while(pIter.hasNext())
			{
				player = pIter.next();
				if(player == null)
				{
					pIter.remove();
				}
				else if(!player.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(player), getActiveObject(), player, true))
				{
					pIter.remove();
					removeKnownObject(player, true);
					getKnownRelations().remove(player.getObjectId());
					getKnownObjects().remove(player.getObjectId());
				}
			}

			Collection<L2Summon> sums = getKnownSummons().values();
			Iterator<L2Summon> sIter = sums.iterator();
			L2Summon summon;

			while(sIter.hasNext())
			{
				summon = sIter.next();
				if(summon == null)
				{
					sIter.remove();
				}
				else if(!getActiveChar().getPets().contains(summon) && (!summon.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(summon), getActiveObject(), summon, true)))
				{
					sIter.remove();
					removeKnownObject(summon, true);
					getKnownObjects().remove(summon.getObjectId());
				}
			}
			return;
		}
		// Go through knownObjects
		Collection<L2Object> objs = getKnownObjects().values();
		Iterator<L2Object> oIter = objs.iterator();
		L2Object object;
		while(oIter.hasNext())
		{
			object = oIter.next();
			if(object == null)
			{
				oIter.remove();
			}
			else if(!object.isVisible() || !Util.checkIfInShortRadius(getDistanceToForgetObject(object), getActiveObject(), object, true))
			{
				oIter.remove();
				removeKnownObject(object, true);

				if(object.isPlayer())
				{
					getKnownPlayers().remove(object.getObjectId());
					getKnownRelations().remove(object.getObjectId());
				}
				else if(object.isSummon())
				{
					getKnownSummons().remove(object.getObjectId());
				}
			}
		}
	}

	/**
	 * @return {@code true} if the L2PcInstance is in _knownPlayer of the L2Character.<BR><BR>
	 *
	 * @param player The L2PcInstance to search in _knownPlayer
	 */
	public boolean knowsThePlayer(L2PcInstance player)
	{
		return getActiveChar().equals(player) || getKnownPlayers().containsKey(player.getObjectId());
	}

	public L2Character getActiveChar()
	{
		return (L2Character) getActiveObject();
	}

	public Collection<L2Character> getKnownCharacters()
	{
		FastList<L2Character> result = getKnownObjects().values().stream().filter(obj -> obj instanceof L2Character).map(obj -> (L2Character) obj).collect(Collectors.toCollection(FastList::new));

		return result;
	}

	public Collection<L2Character> getKnownCharactersInRadius(long radius)
	{
        List<L2Character> result = new ArrayList<>();

        final Collection<L2Object> objs = getKnownObjects().values();
        for (L2Object obj : objs)
        {
            if (obj instanceof L2Character)
            {
                if (Util.checkIfInRange((int) radius, getActiveChar(), obj, true))
                {
                    result.add((L2Character) obj);
                }
            }
        }

        return result;
	}

	public Collection<L2Npc> getKnownNpcInRadius(int radius)
	{
		FastList<L2Npc> result = getKnownCharactersInRadius(radius).stream().filter(obj -> obj instanceof L2Npc).map(obj -> (L2Npc) obj).collect(Collectors.toCollection(FastList::new));

		return result;
	}

	public Map<Integer, L2PcInstance> getKnownPlayers()
	{
		if(_knownPlayers == null)
		{
			_knownPlayers = new L2FastMap<>(true);
		}
		return _knownPlayers;
	}

	public Map<Integer, Integer> getKnownRelations()
	{
		if(_knownRelations == null)
		{
			_knownRelations = new L2FastMap<>(true);
		}
		return _knownRelations;
	}

	public Map<Integer, L2Summon> getKnownSummons()
	{
		if(_knownSummons == null)
		{
			_knownSummons = new L2FastMap<>(true);
		}
		return _knownSummons;
	}

	public Collection<L2PcInstance> getKnownPlayersInRadius(long radius)
	{
		List<L2PcInstance> result = getKnownPlayers().values().stream().filter(player -> Util.checkIfInRange((int) radius, getActiveChar(), player, true)).collect(Collectors.toList());

		return result;
	}
}