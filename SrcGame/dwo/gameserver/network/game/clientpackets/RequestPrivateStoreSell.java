package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemRequest;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.player.TradeList;
import org.apache.log4j.Level;

import static dwo.gameserver.model.actor.L2Npc.INTERACTION_DISTANCE;

public class RequestPrivateStoreSell extends L2GameClientPacket
{
	private static final int BATCH_LENGTH = 28; // length of the one item

	private int _storePlayerId;
	private ItemRequest[] _items;

	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		int count = readD();
		if(count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * BATCH_LENGTH != _buf.remaining())
		{
			return;
		}
		_items = new ItemRequest[count];

		for(int i = 0; i < count; i++)
		{
			int objectId = readD();
			int itemId = readD();
			readH(); //TODO analyse this
			readH(); //TODO analyse this
			long cnt = readQ();
			long price = readQ();

			if(objectId < 1 || itemId < 1 || cnt < 1 || price < 0)
			{
				_items = null;
				return;
			}
			_items[i] = new ItemRequest(objectId, itemId, cnt, price);
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

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.SELL_ITEM))
		{
			player.sendMessage("Вы продаете слишком быстро.");
			return;
		}

		L2PcInstance object = WorldManager.getInstance().getPlayer(_storePlayerId);
		if(object == null)
		{
			return;
		}

		if(!player.isInsideRadius(object, INTERACTION_DISTANCE, true, false))
		{
			return;
		}

		if(player.getInstanceId() != object.getInstanceId() && player.getInstanceId() != -1)
		{
			return;
		}

		if(object.getPrivateStoreType() != PlayerPrivateStoreType.BUY)
		{
			return;
		}

		if(player.isCursedWeaponEquipped())
		{
			return;
		}

		TradeList storeList = object.getBuyList();
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

		if(!storeList.privateStoreSell(player, _items))
		{
			player.sendActionFailed();
			_log.log(Level.WARN, "PrivateStore sell has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + object.getName());
			return;
		}

		if(storeList.getItemCount() == 0)
		{
			object.setPrivateStoreType(PlayerPrivateStoreType.NONE);
			object.broadcastUserInfo();
		}
	}

	@Override
	public String getType()
	{
		return "[C] 96 RequestPrivateStoreSell";
	}

	@Override
	protected boolean triggersOnActionRequest()
	{
		return false;
	}
}