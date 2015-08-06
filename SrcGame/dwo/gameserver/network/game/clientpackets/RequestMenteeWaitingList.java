package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import org.apache.log4j.Level;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 17.10.11
 * Time: 18:14
 */

public class RequestMenteeWaitingList extends L2GameClientPacket
{
	private int unk1;
	private int unk2;
	private int unk3;

	@Override
	protected void readImpl()
	{
		unk1 = readD();
		unk2 = readD();
		unk3 = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		_log.log(Level.INFO, "[IMPLEMENT ME!] RequestMenteeWaitingList: " + unk1 + ' ' + unk2 + ' ' + unk3);
	}

	@Override
	public String getType()
	{
		return "[C] D0:BF RequestMenteeWaitingList";
	}
}
