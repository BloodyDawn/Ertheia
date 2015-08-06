package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.instancemanager.castle.CastleManorManager.CropProcure;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author l3x
 */

public class ExShowSellCropList extends L2GameServerPacket
{
	private final Map<Integer, L2ItemInstance> _cropsItems;
	private final Map<Integer, CropProcure> _castleCrops;
	private int _manorId = 1;

	public ExShowSellCropList(L2PcInstance player, int manorId, List<CropProcure> crops)
	{
		_manorId = manorId;
		_castleCrops = new HashMap<>();
		_cropsItems = new HashMap<>();

		List<Integer> allCrops = ManorData.getInstance().getAllCrops();
		for(int cropId : allCrops)
		{
			L2ItemInstance item = player.getInventory().getItemByItemId(cropId);
			if(item != null)
			{
				_cropsItems.put(cropId, item);
			}
		}

		crops.stream().filter(crop -> _cropsItems.containsKey(crop.getId()) && crop.getAmount() > 0).forEach(crop -> _castleCrops.put(crop.getId(), crop));
	}

	@Override
	public void writeImpl()
	{
		writeD(_manorId); // manor id
		writeD(_cropsItems.size()); // size

		for(L2ItemInstance item : _cropsItems.values())
		{
			writeD(item.getObjectId()); // Object id
			writeD(item.getItemId()); // crop id
			writeD(ManorData.getInstance().getSeedLevelByCrop(item.getItemId())); // seed level
			writeC(1);
			writeD(ManorData.getInstance().getRewardItem(item.getItemId(), 1)); // reward 1 id
			writeC(1);
			writeD(ManorData.getInstance().getRewardItem(item.getItemId(), 2)); // reward 2 id

			if(_castleCrops.containsKey(item.getItemId()))
			{
				CropProcure crop = _castleCrops.get(item.getItemId());
				writeD(_manorId); // manor
				writeQ(crop.getAmount()); // buy residual
				writeQ(crop.getPrice()); // buy price
				writeC(crop.getReward()); // reward
			}
			else
			{
				writeD(0xFFFFFFFF); // manor
				writeQ(0); // buy residual
				writeQ(0); // buy price
				writeC(0); // reward
			}
			writeQ(item.getCount()); // my crops
		}
	}
}