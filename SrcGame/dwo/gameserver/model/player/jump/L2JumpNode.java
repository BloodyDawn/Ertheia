package dwo.gameserver.model.player.jump;

import dwo.gameserver.model.holders.JumpHolder;

import java.util.List;

public class L2JumpNode
{
	private int _routeId;
	private L2JumpType _type;
	private List<JumpHolder> _jump;

	public L2JumpNode(int routeId, L2JumpType type, List<JumpHolder> jump)
	{
		_routeId = routeId;
		_type = type;
		_jump = jump;
	}

	public int getRouteId()
	{
		return _routeId;
	}

	public L2JumpType getType()
	{
		return _type;
	}

	public List<JumpHolder> getJump()
	{
		return _jump;
	}
}