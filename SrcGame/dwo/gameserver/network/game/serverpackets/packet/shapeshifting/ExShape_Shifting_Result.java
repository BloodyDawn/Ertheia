package dwo.gameserver.network.game.serverpackets.packet.shapeshifting;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 21:04
 */
public class ExShape_Shifting_Result extends L2GameServerPacket
{
	public final int _result;
	public final int _targetID;
	public final int _supportID;
	public final int _period;

	public ExShape_Shifting_Result(int result, int targetID, int supportID, int period)
	{
		_result = result;
		_targetID = targetID;
		_supportID = supportID;
		_period = period;
	}

	@Override
	protected void writeImpl()
	{

		writeD(_result);
		writeD(_targetID);
		writeD(_supportID);
		writeD(_period);
	}
}
