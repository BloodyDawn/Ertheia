package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.TradeItem;
import dwo.gameserver.model.player.TradeList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.trade.TradeOtherAdd;
import dwo.gameserver.network.game.serverpackets.packet.trade.TradeOwnAdd;
import org.apache.log4j.Level;

public class RequestAddTradeItem extends L2GameClientPacket
{
	private int _tradeId;
	private int _objectId;
	private long _count;

	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_count = readQ();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		TradeList trade = player.getActiveTradeList();
		if(trade == null)
		{
			_log.log(Level.WARN, "Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			return;
		}

		L2PcInstance partner = trade.getPartner();
		if(partner == null || WorldManager.getInstance().getPlayer(partner.getObjectId()) == null || partner.getActiveTradeList() == null)
		{
			// Trade partner not found, cancel trade
			if(partner != null)
			{
				_log.log(Level.WARN, "Character:" + player.getName() + " requested invalid trade object: " + _objectId);
			}
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.cancelActiveTrade();
			return;
		}

		if(trade.isConfirmed() || partner.getActiveTradeList().isConfirmed())
		{
			player.sendPacket(SystemMessageId.CANNOT_ADJUST_ITEMS_AFTER_TRADE_CONFIRMED);
			return;
		}

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disabled for your Access Level");
			player.cancelActiveTrade();
			return;
		}

		if(!player.validateItemManipulation(_objectId, "trade"))
		{
			player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			return;
		}

		TradeItem item = trade.addItem(_objectId, _count);
		if(item != null)
		{
			player.sendPacket(new TradeOwnAdd(item));
			trade.getPartner().sendPacket(new TradeOtherAdd(item));
		}
	}

	@Override
	public String getType()
	{
		return "[C] 16 AddTradeItem";
	}
}
