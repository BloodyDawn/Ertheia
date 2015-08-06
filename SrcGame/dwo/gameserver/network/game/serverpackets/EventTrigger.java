package dwo.gameserver.network.game.serverpackets;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 04.01.12
 * Time: 4:55
 */

public class EventTrigger extends L2GameServerPacket
{
	private int _trapId;
	private boolean _active;

	public EventTrigger(int trapId, boolean active)
	{
		_trapId = trapId;
		_active = active;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_trapId); // trap object id
		writeC(_active ? 1 : 0); // trap activity 1 or 0
	}
}
