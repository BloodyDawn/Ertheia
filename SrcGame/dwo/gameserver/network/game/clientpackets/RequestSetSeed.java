package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager.SeedProduction;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.util.Util;

import java.util.ArrayList;
import java.util.List;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class RequestSetSeed extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 20; // length of the one item

	private int _manorId;
	private Seed[] _items;

	@Override
	protected void readImpl()
	{
		_manorId = readD();
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_items = new Seed[count];
		for(int i = 0; i < count; i++)
		{
			int itemId = readD();
			long sales = readQ();
			long price = readQ();
			if(itemId < 1 || sales < 0 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new Seed(itemId, sales, price);
		}
	}

	@Override
	protected void runImpl()
	{
		if(_items == null)
		{
			return;
		}

		L2PcInstance player = getClient().getActiveChar();
		// check player privileges
		if(player == null || player.getClan() == null || (player.getClanPrivileges() & L2Clan.CP_CS_MANOR_ADMIN) == 0)
		{
			return;
		}

		// check castle owner
		Castle currentCastle = CastleManager.getInstance().getCastleById(_manorId);
		if(currentCastle.getOwnerId() != player.getClanId())
		{
			return;
		}

		L2Object manager = player.getTarget();

		if(!manager.isNpc())
		{
			return;
		}

		if(!((L2Npc) manager).getCastle().equals(currentCastle))
		{
			return;
		}

		if(!player.isInsideRadius(manager, INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		List<SeedProduction> seeds = new ArrayList<>(_items.length);
		for(Seed i : _items)
		{
			SeedProduction s = i.getSeed();
			if(s == null)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to overflow while setting manor.", Config.DEFAULT_PUNISH);
				return;
			}
			seeds.add(s);
		}

		currentCastle.setSeedProduction(seeds, CastleManorManager.PERIOD_NEXT);
		if(Config.ALT_MANOR_SAVE_ALL_ACTIONS)
		{
			currentCastle.saveSeedData(CastleManorManager.PERIOD_NEXT);
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:0A RequestSetSeed";
	}

	private static class Seed
	{
		private final int _itemId;
		private final long _sales;
		private final long _price;

		public Seed(int id, long s, long p)
		{
			_itemId = id;
			_sales = s;
			_price = p;
		}

		public SeedProduction getSeed()
		{
			if(_sales != 0 && MAX_ADENA / _sales < _price)
			{
				return null;
			}

			return CastleManorManager.getInstance().getNewSeedProduction(_itemId, _sales, _price, _sales);
		}
	}
}
