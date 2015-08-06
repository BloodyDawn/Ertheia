package dwo.gameserver.network.game.clientpackets;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 10.03.13
 * Time: 16:05
 */
public class RequestListMpccWaiting extends L2GameClientPacket
{
	protected static final Logger _log = LogManager.getLogger(RequestListMpccWaiting.class);

	private int _unk;
	private int _unk1;
	private int _unk2;

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_unk1 = readD();
		_unk2 = readD();
	}

	@Override
	protected void runImpl()
	{
		_log.log(Level.INFO, "Реализуй меня! RequestListMpccWaiting D0:5A " + _unk + ' ' + _unk1 + ' ' + _unk2);
	}

	@Override
	public String getType()
	{
		return "[C] D0:5A RequestListMpccWaiting";
	}
}
