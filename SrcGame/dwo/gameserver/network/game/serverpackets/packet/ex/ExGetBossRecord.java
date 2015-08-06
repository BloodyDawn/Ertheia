package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * @author KenM
 */

public class ExGetBossRecord extends L2GameServerPacket
{
	private TIntIntHashMap _bossRecordInfo;
	private int _ranking;
	private int _totalPoints;

	public ExGetBossRecord(int ranking, int totalScore, TIntIntHashMap list)
	{
		_ranking = ranking;
		_totalPoints = totalScore;
		_bossRecordInfo = list;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_ranking);
		writeD(_totalPoints);
		if(_bossRecordInfo == null)
		{
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
			writeD(0x00);
		}
		else
		{
			writeD(_bossRecordInfo.size()); //list size
			for(int bossId : _bossRecordInfo.keys())
			{
				writeD(bossId);
				writeD(_bossRecordInfo.get(bossId));
				writeD(0x00); //??
			}
		}
	}
}
