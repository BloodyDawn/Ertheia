package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

public class ExVitalityEffectInfo extends L2GameServerPacket
{
	private int _vitalityPoints;
	private int _vitalityBonus;
	private int _vitalityItemsLeft;

	public ExVitalityEffectInfo(L2PcInstance player)
	{
		_vitalityPoints = player.getVitalityDataForCurrentClassIndex().getVitalityPoints();
		_vitalityBonus = _vitalityPoints > 0 ? Math.round(Config.RATE_VITALITY * 100) : 0;
		_vitalityItemsLeft = player.getVitalityDataForCurrentClassIndex().getVitalityItems();
	}

	@Override
	protected void writeImpl()
	{
		writeD(_vitalityPoints);
		writeD(_vitalityBonus);
		writeD(Config.VITALITY_ITEMS_WEEKLY_LIMIT);
		writeD(_vitalityItemsLeft);
	}
}
