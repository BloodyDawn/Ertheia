package dwo.gameserver.model.items.itemcontainer;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance.ItemLocation;
import dwo.gameserver.model.player.formation.clan.L2Clan;

public class ClanWarehouse extends Warehouse
{
	private L2Clan _clan;

	public ClanWarehouse(L2Clan clan)
	{
		_clan = clan;
	}

	@Override
	public L2PcInstance getOwner()
	{
		return _clan.getLeader().getPlayerInstance();
	}

	@Override
	public ItemLocation getBaseLocation()
	{
		return ItemLocation.CLANWH;
	}

	@Override
	public String getName()
	{
		return "ClanWarehouse";
	}

	@Override
	public int getOwnerId()
	{
		return _clan.getClanId();
	}

	@Override
	public boolean validateCapacity(long slots)
	{
		return _items.size() + slots <= Config.WAREHOUSE_SLOTS_CLAN;
	}

	public String getLocationId()
	{
		return "0";
	}

	public void setLocationId(L2PcInstance dummy)
	{
	}

	public int getLocationId(boolean dummy)
	{
		return 0;
	}
}