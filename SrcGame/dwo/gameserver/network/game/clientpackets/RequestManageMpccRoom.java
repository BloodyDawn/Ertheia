package dwo.gameserver.network.game.clientpackets;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 10.03.13
 * Time: 16:00
 */
public class RequestManageMpccRoom extends L2GameClientPacket
{
	protected static final Logger _log = LogManager.getLogger(RequestManageMpccRoom.class);

	private int _unk;
	private int _unk1;
	private int _unk2;
	private int _unk3;
	private int _unk4;
	private String _string;

	@Override
	protected void readImpl()
	{
		_unk = readD();
		_unk1 = readD();
		_unk2 = readD();
		_unk3 = readD();
		_unk4 = readD();
		_string = readS();
	}

	@Override
	protected void runImpl()
	{
		_log.log(Level.INFO, "Реализуй меня! RequestManageMpccRoom D0:5B " + _unk + ' ' + _unk1 + ' ' + _unk2 + ' ' + _unk3 + ' ' + _unk4 + ' ' + _string);
	}

	@Override
	public String getType()
	{
		return "[C] D0:5B RequestManageMpccRoom";
	}
}
