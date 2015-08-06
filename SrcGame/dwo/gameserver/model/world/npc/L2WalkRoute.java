package dwo.gameserver.model.world.npc;

import java.util.List;

/**
 * @author GKR
 */

public class L2WalkRoute
{
	private final int _id;
	private final List<L2NpcWalkerNode> _nodeList; // List of nodes
	private final boolean _repeatWalk; // Does repeat walk, after arriving into last point in list, or not
	private final byte _repeatType; // Repeat style: 0 - go back, 1 - go to first point (circle style), 2 - teleport to first point (conveyor style), 3 - random walking between points
	private boolean _stopAfterCycle; // Make only one cycle or endlessly
	private boolean _debug;

	public L2WalkRoute(int id, List<L2NpcWalkerNode> route, boolean repeat, boolean once, byte repeatType)
	{

		_id = id;
		_nodeList = route;
		_repeatType = repeatType;
		_repeatWalk = (_repeatType >= 0 && _repeatType <= 2 || _repeatType == 4) && repeat;
		_debug = false;
	}

	public int getId()
	{
		return _id;
	}

	public List<L2NpcWalkerNode> getNodeList()
	{
		return _nodeList;
	}

	public L2NpcWalkerNode getLastNode()
	{
		return _nodeList.get(_nodeList.size() - 1);
	}

	public boolean repeatWalk()
	{
		return _repeatWalk;
	}

	public boolean doOnce()
	{
		return _stopAfterCycle;
	}

	public byte getRepeatType()
	{
		return _repeatType;
	}

	public int getNodesCount()
	{
		return _nodeList.size();
	}

	public void setDebug(boolean val)
	{
		_debug = val;
	}

	public boolean debug()
	{
		return _debug;
	}
}