package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.model.player.TradeList;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.Util;

public class RequestTradeDone extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.TRADE))
		{
			player.sendMessage("You trading too fast.");
			return;
		}

		TradeList trade = player.getActiveTradeList();
		if(trade == null)
		{
			return;
		}
		if(trade.isLocked())
		{
			return;
		}

		if(_response == 1)
		{
			if(trade.getPartner() == null || WorldManager.getInstance().getPlayer(trade.getPartner().getObjectId()) == null)
			{
				// Trade partner not found, cancel trade
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
				return;
			}

			if(trade.getOwner().getActiveEnchantItem() != null || trade.getPartner().getActiveEnchantItem() != null)
			{
				return;
			}

			if(!player.getAccessLevel().allowTransaction())
			{
				player.cancelActiveTrade();
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				return;
			}

			if(player.getInstanceId() != trade.getPartner().getInstanceId() && player.getInstanceId() != -1)
			{
				player.cancelActiveTrade();
				return;
			}

			if(Util.calculateDistance(player, trade.getPartner(), true) > 150)
			{
				player.cancelActiveTrade();
				return;
			}

			trade.confirm();
		}
		else
		{
			player.cancelActiveTrade();
		}
	}

	@Override
	public String getType()
	{
		return "[C] 17 TradeDone";
	}
}
