package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemRequest;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.TradeList;
import dwo.gameserver.util.Util;
import javolution.util.FastSet;
import org.apache.log4j.Level;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 20; // length of the one item

	private int _storePlayerId;
	private FastSet<ItemRequest> _items;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}
		_items = new FastSet<>();

		for(int i = 0; i < count; i++)
		{
			int objectId = readD();
			long cnt = readQ();
			long price = readQ();

			if(objectId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}

			_items.add(new ItemRequest(objectId, cnt, price));
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

		if(_items == null)
		{
			player.sendActionFailed();
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.BUY_ITEM))
		{
			player.sendMessage("Вы покупаете слишком часто.");
			return;
		}

		L2Object object = WorldManager.getInstance().getPlayer(_storePlayerId);
		if(object == null)
		{
			return;
		}

		if(player.isCursedWeaponEquipped())
		{
			return;
		}

		L2PcInstance storePlayer = (L2PcInstance) object;
		if(!player.isInsideRadius(storePlayer, INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		if(player.getInstanceId() != storePlayer.getInstanceId() && player.getInstanceId() != -1)
		{
			return;
		}

		if(!(storePlayer.getPrivateStoreType() == PlayerPrivateStoreType.SELL || storePlayer.getPrivateStoreType() == PlayerPrivateStoreType.SELL_PACKAGE))
		{
			return;
		}

		TradeList storeList = storePlayer.getSellList();
		if(storeList == null)
		{
			return;
		}

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("У Вас нет прав для выполнения этого запроса.");
			player.sendActionFailed();
			return;
		}

		if(storePlayer.getPrivateStoreType() == PlayerPrivateStoreType.SELL_PACKAGE)
		{
			if(storeList.getItemCount() > _items.size())
			{
				String msgErr = "[RequestPrivateStoreBuy] player " + getClient().getActiveChar().getName() + " tried to buy less items than sold by package-sell, ban this player for bot usage!";
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}

		int result = storeList.privateStoreBuy(player, _items);
		if(result > 0)
		{
			player.sendActionFailed();
			if(result > 1)
			{
				_log.log(Level.WARN, "PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			}
			return;
		}

		if(storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(PlayerPrivateStoreType.NONE);
			storePlayer.broadcastUserInfo();
		}
	}

	@Override
	public String getType()
	{
		return "[C] 79 RequestPrivateStoreBuy";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}