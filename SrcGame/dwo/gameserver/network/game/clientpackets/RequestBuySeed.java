package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager;
import dwo.gameserver.instancemanager.castle.CastleManorManager.SeedProduction;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Util;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;
import static dwo.gameserver.model.items.itemcontainer.PcInventory.MAX_ADENA;

public class RequestBuySeed extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 12; // length of the one item

	private int _manorId;
	private Seed[] _seeds;

	@Override
	protected void readImpl()
	{
		_manorId = readD();

		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}

		_seeds = new Seed[count];
		for(int i = 0; i < count; i++)
		{
			int itemId = readD();
			long cnt = readQ();
			if(cnt < 1)
			{
				_seeds = null;
				return;
			}
			_seeds[i] = new Seed(itemId, cnt);
		}
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getManor().tryPerformAction(FloodAction.BUY_ITEM))
		{
			return;
		}

		if(_seeds == null)
		{
			player.sendActionFailed();
			return;
		}

		L2Object manager = player.getTarget();

		if(!manager.isNpc())
		{
			return;
		}

		if(!player.isInsideRadius(manager, INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastleById(_manorId);

		long totalPrice = 0;
		int slots = 0;
		int totalWeight = 0;

		for(Seed i : _seeds)
		{
			if(!i.setProduction(castle))
			{
				return;
			}

			totalPrice += i.getPrice();

			if(totalPrice > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to purchase over " + MAX_ADENA + " adena worth of goods.", Config.DEFAULT_PUNISH);
				return;
			}

			L2Item template = ItemTable.getInstance().getTemplate(i.getSeedId());
			totalWeight += i.getCount() * template.getWeight();
			if(!template.isStackable())
			{
				slots += i.getCount();
			}
			else if(player.getInventory().getItemByItemId(i.getSeedId()) == null)
			{
				slots++;
			}
		}

		if(!player.getInventory().validateWeight(totalWeight))
		{
			player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			return;
		}

		if(!player.getInventory().validateCapacity(slots))
		{
			player.sendPacket(SystemMessageId.SLOTS_FULL);
			return;
		}

		// test adena
		if(totalPrice < 0 || player.getAdenaCount() < totalPrice)
		{
			player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			return;
		}

		// Proceed the purchase
		for(Seed i : _seeds)
		{
			// take adena and check seed amount once again
			if(!player.reduceAdena(ProcessType.BUY, i.getPrice(), player, false) || !i.updateProduction(castle))
			{
				// failed buy, reduce total price
				totalPrice -= i.getPrice();
				continue;
			}

			// Add item to Inventory and adjust update packet
			player.addItem(ProcessType.BUY, i.getSeedId(), i.getCount(), manager, true);
		}

		// Adding to treasury for Manor Castle
		if(totalPrice > 0)
		{
			castle.addToTreasuryNoTax(totalPrice);
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addItemNumber(totalPrice));
		}
	}

	@Override
	public String getType()
	{
		return "[C] C4 RequestBuySeed";
	}

	private static class Seed
	{
		private final int _seedId;
		private final long _count;
		SeedProduction _seed;

		public Seed(int id, long num)
		{
			_seedId = id;
			_count = num;
		}

		public int getSeedId()
		{
			return _seedId;
		}

		public long getCount()
		{
			return _count;
		}

		public long getPrice()
		{
			return _seed.getPrice() * _count;
		}

		public boolean setProduction(Castle c)
		{
			_seed = c.getSeed(_seedId, CastleManorManager.PERIOD_CURRENT);
			// invalid price - seed disabled
			if(_seed.getPrice() <= 0)
			{
				return false;
			}
			// try to buy more than castle can produce
			if(_seed.getCanProduce() < _count)
			{
				return false;
			}
			// check for overflow
			return MAX_ADENA / _count >= _seed.getPrice();

		}

		public boolean updateProduction(Castle c)
		{
			synchronized(_seed)
			{
				long amount = _seed.getCanProduce();
				if(_count > amount)
				{
					return false; // not enough seeds
				}
				_seed.setCanProduce(amount - _count);
			}
			// Update Castle Seeds Amount
			if(Config.ALT_MANOR_SAVE_ALL_ACTIONS)
			{
				c.updateSeed(_seedId, _seed.getCanProduce(), CastleManorManager.PERIOD_CURRENT);
			}
			return true;
		}
	}
}