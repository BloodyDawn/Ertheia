package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */

public class ExPrivateStoreWholeMsg extends L2GameServerPacket
{
	private final int _objectId;
	private final String _msg;

	public ExPrivateStoreWholeMsg(L2PcInstance player, String msg)
	{
		_objectId = player.getObjectId();
		_msg = msg;
	}

	public ExPrivateStoreWholeMsg(L2PcInstance player)
	{
		this(player, player.getSellList().getTitle());
	}

	@Override
	protected void writeImpl()
	{
		writeD(_objectId);
		writeS(_msg);
	}
}
