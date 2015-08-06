package dwo.gameserver.network.game.clientpackets;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 10.03.13
 * Time: 15:56
 */
public class RequestJoinMpccRoom extends L2GameClientPacket
{
	protected static final Logger _log = LogManager.getLogger(RequestJoinMpccRoom.class);

	private int _unk;
	private int _unk1;

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_unk1 = readD();
	}

	@Override
	protected void runImpl()
	{
		_log.log(Level.INFO, "Реализуй меня! RequestJoinMpccRoom D0:5C " + _unk + ' ' + _unk1);
	}

	@Override
	public String getType()
	{
		return "[C] D0:5C RequestJoinMpccRoom";
	}
}
