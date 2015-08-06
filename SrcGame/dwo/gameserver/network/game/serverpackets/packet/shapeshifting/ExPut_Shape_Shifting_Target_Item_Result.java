package dwo.gameserver.network.game.serverpackets.packet.shapeshifting;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:04
 */
public class ExPut_Shape_Shifting_Target_Item_Result extends L2GameServerPacket
{
	private int _result;
	private long _adena;

	public ExPut_Shape_Shifting_Target_Item_Result(int result, int adena)
	{
		_result = result;
		_adena = adena;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_result);
		writeQ(_adena);
	}
}
