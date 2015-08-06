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
package dwo.gameserver.model.actor.ai;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.NpcWalkerRoutesData;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2NpcWalkerInstance;
import dwo.gameserver.model.world.npc.L2NpcWalkerNode;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.util.List;

public class L2NpcWalkerAI extends L2CharacterAI implements Runnable
{
	private static final int DEFAULT_MOVE_DELAY = 0;
	/**
	 * home points for xyz
	 */
	int _homeX;
	int _homeY;
	int _homeZ;
	private long _nextMoveTime;
	private boolean _walkingToNextPoint;
	/**
	 * route of the current npc
	 */
	private List<L2NpcWalkerNode> _route;

	/**
	 * current node
	 */
	private int _currentPos;

	/**
	 * Constructor of L2CharacterAI.<BR>
	 * <BR>
	 *
	 * @param accessor The AI accessor of the L2Character
	 */
	public L2NpcWalkerAI(L2Character.AIAccessor accessor)
	{
		super(accessor);

		if(!Config.ALLOW_NPC_WALKERS)
		{
			return;
		}

		_route = NpcWalkerRoutesData.getInstance().getRouteForNpc(getActor().getNpcId());

		// Here we need 1 second initial delay cause getActor().hasAI() will return null...
		// Constructor of L2NpcWalkerAI is called faster then ai object is attached in L2NpcWalkerInstance
		if(_route != null)
		{
			ThreadPoolManager.getInstance().scheduleAiAtFixedRate(this, 1000, 1000);
		}
		else
		{
			_log.log(Level.WARN, getClass().getSimpleName() + ": Missing route data! Npc: " + _actor);
		}
	}

	@Override
	public void run()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtThink()
	{
		if(!Config.ALLOW_NPC_WALKERS)
		{
			return;
		}

		if(_walkingToNextPoint)
		{
			checkArrived();
			return;
		}

		if(_nextMoveTime < System.currentTimeMillis())
		{
			walkToLocation();
		}
	}

	/**
	 * If npc can't walk to it's target then just teleport to next point
	 *
	 * @param blocked_at_pos ignoring it
	 */
	@Override
	protected void onEvtArrivedBlocked(Location blocked_at_pos)
	{
		_log.log(Level.WARN, "NpcWalker ID: " + getActor().getNpcId() + ": Blocked at rote position [" + _currentPos + "], coords: " + blocked_at_pos + ". Teleporting to next point");

		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		getActor().teleToLocation(destinationX, destinationY, destinationZ, false);
		super.onEvtArrivedBlocked(blocked_at_pos);
	}

	private void checkArrived()
	{
		L2NpcWalkerNode currentNode = _route.get(_currentPos);

		int destinationX = currentNode.getMoveX();
		int destinationY = currentNode.getMoveY();
		int destinationZ = currentNode.getMoveZ();

		if(getActor().isInsideRadius(destinationX, destinationY, destinationZ, 5, false, false))
		{
			NpcStringId npcString = currentNode.getNpcString();
			boolean isHaveAnimation = currentNode.isAnimatedWhenArrived();
			String chat = null;
			if(npcString == null)
			{
				chat = currentNode.getChatText();
			}

			if(npcString != null || chat != null && !chat.isEmpty())
			{
				getActor().broadcastChat(chat, npcString);
			}

			if(isHaveAnimation)
			{
				getActor().broadcastSocialAction(Rnd.get(2, 9));
			}

			// time in millis
			long delay = currentNode.getDelay() * 1000;

			// sleeps between each move
			if(delay <= 0)
			{
				delay = DEFAULT_MOVE_DELAY;
				if(Config.DEVELOPER)
				{
					_log.log(Level.WARN, "Wrong Delay Set in Npc Walker Functions = " + delay + " secs, using default delay: " + DEFAULT_MOVE_DELAY + " secs instead.");
				}
			}

			_nextMoveTime = System.currentTimeMillis() + delay;
			_walkingToNextPoint = false;
		}
	}

	private void walkToLocation()
	{
		if(_currentPos < _route.size() - 1)
		{
			_currentPos++;
		}
		else
		{
			_currentPos = 0;
		}

		boolean moveType = _route.get(_currentPos).getRunning();

		/**
		 * false - walking
		 * true - Running
		 */
		if(moveType)
		{
			getActor().setRunning();
		}
		else
		{
			getActor().setWalking();
		}

		// now we define destination
		int destinationX = _route.get(_currentPos).getMoveX();
		int destinationY = _route.get(_currentPos).getMoveY();
		int destinationZ = _route.get(_currentPos).getMoveZ();

		// notify AI of MOVE_TO
		_walkingToNextPoint = true;

		setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(destinationX, destinationY, destinationZ, 0));
	}

	@Override
	public L2NpcWalkerInstance getActor()
	{
		return (L2NpcWalkerInstance) super.getActor();
	}

	public int getHomeX()
	{
		return _homeX;
	}

	public void setHomeX(int homeX)
	{
		_homeX = homeX;
	}

	public int getHomeY()
	{
		return _homeY;
	}

	public void setHomeY(int homeY)
	{
		_homeY = homeY;
	}

	public int getHomeZ()
	{
		return _homeZ;
	}

	public void setHomeZ(int homeZ)
	{
		_homeZ = homeZ;
	}

	public boolean isWalkingToNextPoint()
	{
		return _walkingToNextPoint;
	}

	public void setWalkingToNextPoint(boolean value)
	{
		_walkingToNextPoint = value;
	}
}
