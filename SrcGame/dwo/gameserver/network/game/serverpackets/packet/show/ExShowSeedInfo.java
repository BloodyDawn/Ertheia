package dwo.gameserver.network.game.serverpackets.packet.show;

import dwo.gameserver.datatables.xml.ManorData;
import dwo.gameserver.instancemanager.castle.CastleManorManager.SeedProduction;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.List;

/**
 * @author l3x
 */

public class ExShowSeedInfo extends L2GameServerPacket
{
	private List<SeedProduction> _seeds;
	private int _manorId;

	public ExShowSeedInfo(int manorId, List<SeedProduction> seeds)
	{
		_manorId = manorId;
		_seeds = seeds;
	}

	@Override
	protected void writeImpl()
	{
		writeC(0);
		writeD(_manorId); // Manor ID
		writeD(0);
		if(_seeds == null)
		{
			writeD(0);
			return;
		}
		writeD(_seeds.size());
		for(SeedProduction seed : _seeds)
		{
			writeD(seed.getId()); // Seed id
			writeQ(seed.getCanProduce()); // Left to buy
			writeQ(seed.getStartProduce()); // STARTED amount
			writeQ(seed.getPrice());        // Sell Price
			writeD(ManorData.getInstance().getSeedLevel(seed.getId())); // Seed Level
			writeC(1); // reward 1 Type
			writeD(ManorData.getInstance().getRewardItemBySeed(seed.getId(), 1)); // Reward 1 Type Item Id
			writeC(1); // reward 2 Type
			writeD(ManorData.getInstance().getRewardItemBySeed(seed.getId(), 2)); // Reward 2 Type Item Id
		}
	}
}
