package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * sample

 * format
 * d
 *
 * @version $Revision: 1.1.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class ShowMinimap extends L2GameServerPacket
{
	private int _mapId;

	public ShowMinimap(int mapId)
	{
		_mapId = mapId;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_mapId);
		writeC(0x00); //SevenSigns.getInstance().getCurrentPeriod() TODO: узнать что в год шлется
	}
}
