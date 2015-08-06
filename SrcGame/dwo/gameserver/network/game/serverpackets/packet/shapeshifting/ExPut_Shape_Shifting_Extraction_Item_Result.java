package dwo.gameserver.network.game.serverpackets.packet.shapeshifting;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:04
 */
public class ExPut_Shape_Shifting_Extraction_Item_Result extends L2GameServerPacket
{
	public final int _result;

	public ExPut_Shape_Shifting_Extraction_Item_Result(int result)
	{
		_result = result;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
	}
}
