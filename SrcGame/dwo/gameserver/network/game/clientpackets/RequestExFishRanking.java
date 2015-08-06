package dwo.gameserver.network.game.clientpackets;

import org.apache.log4j.Level;

/**
 * @author  -Wooden-
 */

public class RequestExFishRanking extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// trigger
	}

	@Override
	protected void runImpl()
	{
		_log.log(Level.INFO, "C5: RequestExFishRanking");
	}

	@Override
	public String getType()
	{
		return "[C] D0:1F RequestExFishRanking";
	}
}