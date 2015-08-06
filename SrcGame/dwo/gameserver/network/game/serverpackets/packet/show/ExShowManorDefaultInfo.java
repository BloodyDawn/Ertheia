package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * @author l3x
 */

public class ExShowManorDefaultInfo extends L2GameServerPacket
{
	private List<Integer> _crops;

	public ExShowManorDefaultInfo()
	{
		_crops = ManorData.getInstance().getAllCrops();
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_crops.size());
		for(int cropId : _crops)
		{
			writeD(cropId); // crop Id
			writeD(ManorData.getInstance().getSeedLevelByCrop(cropId)); // level
			writeD(ManorData.getInstance().getSeedBasicPriceByCrop(cropId)); // seed price
			writeD(ManorData.getInstance().getCropBasicPrice(cropId)); // crop price
			writeC(1); // Reward 1 Type
			writeD(ManorData.getInstance().getRewardItem(cropId, 1)); // Reward 1 Type Item Id
			writeC(1); // Reward 2 Type
			writeD(ManorData.getInstance().getRewardItem(cropId, 2)); // Reward 2 Type Item Id
		}
	}
}
