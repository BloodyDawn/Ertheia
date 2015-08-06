package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.trade.TradeRequest;
import dwo.gameserver.util.Util;

public class RequestTrade extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			player.sendActionFailed();
			return;
		}

		L2Object target = WorldManager.getInstance().findObject(_objectId);
		// If there is no target, target is far away or
		// they are in different instances (except multiverse)
		// trade request is ignored and there is no system message.
		if(target == null || !player.getKnownList().knowsObject(target) || target.getInstanceId() != player.getInstanceId() && player.getInstanceId() != -1)
		{
			return;
		}

		// If target and acting player are the same, trade request is ignored
		// and the following system message is sent to acting player.
		if(target.getObjectId() == player.getObjectId())
		{
			player.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		if(!target.isPlayer())
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		L2PcInstance partner = target.getActingPlayer();
		if(partner.getOlympiadController().isParticipating() || player.getOlympiadController().isParticipating())
		{
			player.sendMessage("A user currently participating in the Olympiad cannot accept or request a trade.");
			return;
		}

		// Alt game - Reputation punishment
		if(player.hasBadReputation() || partner.hasBadReputation())
		{
			player.sendMessage("Chaotic players can't use Trade.");
			return;
		}

		if(Config.JAIL_DISABLE_TRANSACTION && (player.isInJail() || partner.isInJail()))
		{
			player.sendMessage("You cannot trade in Jail.");
			return;
		}

		if(player.getPrivateStoreType() != PlayerPrivateStoreType.NONE || partner.getPrivateStoreType() != PlayerPrivateStoreType.NONE)
		{
			player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
			return;
		}

		if(player.isProcessingTransaction())
		{
			player.sendPacket(SystemMessageId.ALREADY_TRADING);
			return;
		}

		if(partner.isProcessingRequest() || partner.isProcessingTransaction())
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(partner.getName()));
			return;
		}

		if(partner.getTradeRefusal())
		{
			player.sendMessage("Target is in trade refusal mode");
			return;
		}

		if(RelationListManager.getInstance().isBlocked(partner, player))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addCharName(partner));
			return;
		}

		if(Util.calculateDistance(player, partner, true) > 150)
		{
			player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
			return;
		}

		player.onTransactionRequest(partner);
		partner.sendPacket(new TradeRequest(player.getObjectId()));
		player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REQUEST_C1_FOR_TRADE).addString(partner.getName()));
	}

	@Override
	public String getType()
	{
		return "[C] 15 TradeRequest";
	}
}
