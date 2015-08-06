package dwo.gameserver.network.game.serverpackets.packet.mail;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Migi
 */

public class ExReplyWritePost extends L2GameServerPacket
{
	private static final ExReplyWritePost STATIC_PACKET_TRUE = new ExReplyWritePost(true);
	private static final ExReplyWritePost STATIC_PACKET_FALSE = new ExReplyWritePost(false);
	boolean _showAnim;

	public ExReplyWritePost(boolean showAnimation)
	{
		_showAnim = showAnimation;
	}

	public static ExReplyWritePost valueOf(boolean result)
	{
		return result ? STATIC_PACKET_TRUE : STATIC_PACKET_FALSE;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_showAnim ? 0x01 : 0x00);
	}
}
