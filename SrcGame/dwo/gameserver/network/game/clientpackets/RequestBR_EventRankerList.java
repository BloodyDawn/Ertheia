package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.network.game.serverpackets.packet.ex.ExBR_LoadEventTopRankers;

/**
 * Halloween rank list client packet.
 * <p/>
 * Format: (ch)ddd
 */
public class RequestBR_EventRankerList extends L2GameClientPacket
{
	private int _eventId;
	private int _day;
	private int _ranking;

	@Override
	protected void readImpl()
	{
		_eventId = readD();
		_day = readD(); // 0 - current, 1 - previous
		_ranking = readD();
	}

	@Override
	protected void runImpl()
	{
		// TODO count, bestScore, myScore
		int count = 0;
		int bestScore = 0;
		int myScore = 0;
		getClient().sendPacket(new ExBR_LoadEventTopRankers(_eventId, _day, count, bestScore, myScore));
	}

	@Override
	public String getType()
	{
		return "[C] D0:7D BrEventRankerList";
	}
}
