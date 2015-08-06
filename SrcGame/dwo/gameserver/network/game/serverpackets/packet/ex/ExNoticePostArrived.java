package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Migi
 */

public class ExNoticePostArrived extends L2GameServerPacket
{
	private static final ExNoticePostArrived STATIC_PACKET_TRUE = new ExNoticePostArrived(true);
	private static final ExNoticePostArrived STATIC_PACKET_FALSE = new ExNoticePostArrived(false);
	boolean _showAnim;

	public ExNoticePostArrived(boolean showAnimation)
	{
		_showAnim = showAnimation;
	}

	public static ExNoticePostArrived valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_showAnim ? 0x01 : 0x00);
	}
}
