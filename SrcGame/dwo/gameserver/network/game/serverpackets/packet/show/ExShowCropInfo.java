package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.instancemanager.castle.CastleManorManager.CropProcure;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * @author l3x
 */

public class ExShowCropInfo extends L2GameServerPacket
{
	private List<CropProcure> _crops;
	private int _manorId;

	public ExShowCropInfo(int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_crops = crops;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		if(_crops == null)
		{
			writeD(0);
			return;
		}
		writeD(_crops.size());
		for(CropProcure crop : _crops)
		{
			writeD(crop.getId());          // Crop id
			writeQ(crop.getAmount());      // Buy residual
			writeQ(crop.getStartAmount()); // Buy
			writeQ(crop.getPrice());       // Buy price
			writeC(crop.getReward());      // Reward
			writeD(ManorData.getInstance().getSeedLevelByCrop(crop.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(ManorData.getInstance().getRewardItem(crop.getId(), 1));    // Rewrad 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(ManorData.getInstance().getRewardItem(crop.getId(), 2));    // Rewrad 2 Type Item Id
		}
	}
}
