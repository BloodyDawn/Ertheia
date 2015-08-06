package dwo.gameserver.network.game.clientpackets;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.10.11
 * Time: 16:44
 */

public class RequestWebSessionID extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// TODO: Implement me
	}

	@Override
	protected void runImpl()
	{
		// TODO: Implement me
	}

	@Override
	public String getType()
	{
		return "[C] D0:AC Request24HzSessionID";
	}
}