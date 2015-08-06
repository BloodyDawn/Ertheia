package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExVoteSystemInfo extends L2GameServerPacket
{
	private int _recomLeft;
	private int _recomHave;
	private int _bonusTime;
	private int _bonusVal;
	private int _bonusType;

	public ExVoteSystemInfo(L2PcInstance player)
	{
		_recomLeft = player.getRecommendationsLeft();
		_recomHave = player.getRecommendations();

		// Невита нет в GOD, шлем статику
		_bonusTime = 3600;
		_bonusVal = 0;
		_bonusType = 10;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_recomLeft);
		writeD(_recomHave);
		writeD(_bonusTime);
		writeD(_bonusVal);
		writeD(_bonusType);
	}
}
