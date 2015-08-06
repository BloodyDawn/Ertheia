package dwo.gameserver.network.game.clientpackets;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 10.03.13
 * Time: 15:49
 */
public class RequestOustFromMpccRoom extends L2GameClientPacket
{
	protected static final Logger _log = LogManager.getLogger(RequestOustFromMpccRoom.class);
	private int _unk;

	@Override
	protected void readImpl()
	{
		_unk = readD();
	}

	@Override
	protected void runImpl()
	{
		_log.log(Level.INFO, "Реализуй меня! RequestOustFromMpccRoom D0:5D " + _unk);
	}

	@Override
	public String getType()
	{
		return "[C] D0:5D RequestOustFromMpccRoom";
	}
}
