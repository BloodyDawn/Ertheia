package dwo.gameserver.network.game.serverpackets;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
	private int _eventId;

	public TutorialEnableClientEvent(int event)
	{
		_eventId = event;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_eventId);
	}
}
