package dwo.gameserver.network.game.clientpackets;

import dwo.config.Config;
import dwo.gameserver.instancemanager.RelationListManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.player.formation.group.PartyLootType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.party.AskJoinParty;
import org.apache.log4j.Level;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private PartyLootType _itemDistribution;

	@Override
	protected void readImpl()
	{
		_name = readS();
		_itemDistribution = PartyLootType.values()[readD()];
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance requestor = getClient().getActiveChar();
		L2PcInstance target = WorldManager.getInstance().getPlayer(_name);

		if(requestor == null)
		{
			return;
		}

		if(target == null)
		{
			requestor.sendPacket(SystemMessageId.FIRST_SELECT_USER_TO_INVITE_TO_PARTY);
			return;
		}

		if(target.getClient() == null || target.getClient().isDetached())
		{
			requestor.sendMessage("Игрок находиться в режиме оффлайн торговли.");
			return;
		}

		if(!requestor.isGM() && target.getAppearance().getInvisible())
		{
			requestor.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
			return;
		}

		if(target.isInParty())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ALREADY_IN_PARTY).addString(target.getName()));
			return;
		}

		if(RelationListManager.getInstance().isBlocked(target, requestor))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_ADDED_YOU_TO_IGNORE_LIST).addCharName(target));
			return;
		}

		if(target.equals(requestor))
		{
			requestor.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
			return;
		}

		if(target.isCursedWeaponEquipped() || requestor.isCursedWeaponEquipped())
		{
			requestor.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}

		if(target.isInJail() || requestor.isInJail())
		{
			requestor.sendMessage("Игрок находится в Тюрьме.");
			return;
		}

		if(target.getClient().isDetached())
		{
			requestor.sendMessage("Игрока нет в мире.");
			return;
		}

		if(requestor.getOlympiadController().isParticipating() && !requestor.getOlympiadController().isComrade(target))
		{
			return;
		}

		requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_INVITED_TO_PARTY).addCharName(target));

		if(requestor.isInParty())
		{
			addTargetToParty(target, requestor);
		}
		else
		{
			createNewParty(target, requestor);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 29 RequestJoinParty";
	}

	/**
	 * @param target
	 * @param requestor
	 */
	private void addTargetToParty(L2PcInstance target, L2PcInstance requestor)
	{
		L2Party party = requestor.getParty();

		// summary of ppl already in party and ppl that get invitation
		if(!party.isLeader(requestor))
		{
			requestor.sendPacket(SystemMessageId.ONLY_LEADER_CAN_INVITE);
			return;
		}
		if(party.getMemberCount() >= 7)
		{
			requestor.sendPacket(SystemMessageId.PARTY_FULL);
			return;
		}
		if(party.getPendingInvitation() && !party.isInvitationRequestExpired())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);
			return;
		}

		if(target.isProcessingRequest())
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER).addString(target.getName()));
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, requestor.getName() + " already received a party invitation");
			}
		}
		else
		{
			requestor.onTransactionRequest(target);
			// in case a leader change has happened, use party's mode
			target.sendPacket(new AskJoinParty(requestor.getName(), party.getLootDistribution()));
			party.setPendingInvitation(true);

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "sent out a party invitation to:" + target.getName());
			}

		}
	}

	/**
	 * @param target
	 * @param requestor
	 */
	private void createNewParty(L2PcInstance target, L2PcInstance requestor)
	{
		if(target.isProcessingRequest())
		{
			requestor.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY);

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, requestor.getName() + " already received a party invitation");
			}
		}
		else
		{
			requestor.setParty(new L2Party(requestor, _itemDistribution));

			requestor.onTransactionRequest(target);
			target.sendPacket(new AskJoinParty(requestor.getName(), _itemDistribution));
			requestor.getParty().setPendingInvitation(true);

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "sent out a party invitation to:" + target.getName());
			}
		}
	}
}
