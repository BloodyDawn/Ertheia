package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author KenM
 */
public class ExSpawnEmitter extends L2GameServerPacket
{
	private final int _playerObjectId;
	private final int _npcObjectId;

	public ExSpawnEmitter(int playerObjectId, int npcObjectId)
	{
		_playerObjectId = playerObjectId;
		_npcObjectId = npcObjectId;
	}

	public ExSpawnEmitter(L2PcInstance player, L2Npc npc)
	{
		this(player.getObjectId(), npc.getObjectId());
	}

	@Override
	protected void writeImpl()
	{
		writeD(_npcObjectId);
		writeD(_playerObjectId);
		writeD(0x00); // ?
	}
}
