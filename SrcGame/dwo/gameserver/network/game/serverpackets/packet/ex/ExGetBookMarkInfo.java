package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportBookmark;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author ShanSoft
 */

public class ExGetBookMarkInfo extends L2GameServerPacket
{
	private L2PcInstance player;

	public ExGetBookMarkInfo(L2PcInstance cha)
	{
		player = cha;
	}

	@Override
	protected void writeImpl()
	{
		writeD(0x00); // Dummy
		writeD(player.getBookmarkslot());
		writeD(player.getTpbookmark().size());

		for(TeleportBookmark tpbm : player.getTpbookmark())
		{
			writeD(tpbm.getId());
			writeD(tpbm.getLoc().getX());
			writeD(tpbm.getLoc().getY());
			writeD(tpbm.getLoc().getZ());
			writeS(tpbm.getName());
			writeD(tpbm.getIcon());
			writeS(tpbm.getTag());
		}
	}
}
