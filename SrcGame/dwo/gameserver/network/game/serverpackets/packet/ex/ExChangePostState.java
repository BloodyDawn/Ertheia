package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.player.mail.MailMessageStatus;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author Migi
 */

public class ExChangePostState extends L2GameServerPacket
{
	private boolean _receivedBoard;
	private int[] _changedMsgIds;
	private MailMessageStatus _messageStatus;

	public ExChangePostState(boolean receivedBoard, int[] changedMsgIds, MailMessageStatus changeId)
	{
		_receivedBoard = receivedBoard;
		_changedMsgIds = changedMsgIds;
		_messageStatus = changeId;
	}

	public ExChangePostState(boolean receivedBoard, int changedMsgId, MailMessageStatus changeId)
	{
		_receivedBoard = receivedBoard;
		_changedMsgIds = new int[]{changedMsgId};
		_messageStatus = changeId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_receivedBoard ? 1 : 0);
		writeD(_changedMsgIds.length);
		for(int postId : _changedMsgIds)
		{
			writeD(postId); // postId
			writeD(_messageStatus.ordinal()); // state
		}
	}
}