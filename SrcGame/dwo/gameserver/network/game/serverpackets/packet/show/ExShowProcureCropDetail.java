package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager.CropProcure;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.HashMap;
import java.util.Map;

/**
 * @author l3x
 */

public class ExShowProcureCropDetail extends L2GameServerPacket
{
	private int _cropId;

	private Map<Integer, CropProcure> _castleCrops;

	public ExShowProcureCropDetail(int cropId)
	{
		_cropId = cropId;
		_castleCrops = new HashMap<>();

		for(Castle c : CastleManager.getInstance().getCastles())
		{
			CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if(cropItem != null && cropItem.getAmount() > 0)
			{
				_castleCrops.put(c.getCastleId(), cropItem);
			}
		}
	}

	@Override
	public void writeImpl()
	{
		writeD(_cropId); // crop id
		writeD(_castleCrops.size()); // size

		for(Map.Entry<Integer, CropProcure> integerCropProcureEntry : _castleCrops.entrySet())
		{
			CropProcure crop = integerCropProcureEntry.getValue();
			writeD(integerCropProcureEntry.getKey()); // manor name
			writeQ(crop.getAmount()); // buy residual
			writeQ(crop.getPrice()); // buy price
			writeC(crop.getReward()); // reward type
		}
	}
}