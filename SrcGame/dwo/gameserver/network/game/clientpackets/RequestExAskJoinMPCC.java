package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.party.ExAskJoinMPCC;

public class RequestExAskJoinMPCC extends L2GameClientPacket
{
	private String _name;

	@Override
	protected void readImpl()
	{
		_name = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2PcInstance player = WorldManager.getInstance().getPlayer(_name);
		if(player == null)
		{
			return;
		}
		// invite yourself? ;)
		if(activeChar.isInParty() && player.isInParty() && activeChar.getParty().equals(player.getParty()))
		{
			return;
		}

		//activeChar is in a Party?
		if(activeChar.isInParty())
		{
			L2Party activeParty = activeChar.getParty();
			//activeChar is PartyLeader? && activeChars Party is already in a CommandChannel?
			if(activeParty.getLeader().equals(activeChar))
			{
				// if activeChars Party is in CC, is activeChar CCLeader?
				if(activeParty.isInCommandChannel() && activeParty.getCommandChannel().getLeader().equals(activeChar))
				{
					//in CC and the CCLeader
					//target in a party?
					if(player.isInParty())
					{
						//targets party already in a CChannel?
						if(player.getParty().isInCommandChannel())
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
						}
						else
						{
							//ready to open a new CC
							//send request to targets Party's PartyLeader
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage("Цель не состоит в группе.");
					}

				}
				else if(activeParty.isInCommandChannel() && !activeParty.getCommandChannel().getLeader().equals(activeChar))
				{
					//in CC, but not the CCLeader
					activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
				}
				else
				{
					//target in a party?
					if(player.isInParty())
					{
						//targets party already in a CChannel?
						if(player.getParty().isInCommandChannel())
						{
							activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
						}
						else
						{
							//ready to open a new CC
							//send request to targets Party's PartyLeader
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage("Цель не состоит в группе.");
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
			}
		}
	}

	@Override
	public String getType()
	{
		return "[C] D0:0D RequestExAskJoinMPCC";
	}

	private void askJoinMPCC(L2PcInstance requestor, L2PcInstance target)
	{
		boolean hasRight = false;
		if(requestor.isClanLeader() && requestor.getClan().getLevel() >= 5)
		{
			// Clan leader of lvl5 Clan or higher.
			hasRight = true;
		}
		else if(requestor.getInventory().getItemByItemId(8871) != null)
		{
			// 8871 Strategy Guide.
			// TODO: Should destroyed after successful invite?
			hasRight = true;
		}
		else if(requestor.getPledgeClass() >= 5 && requestor.getKnownSkill(391) != null)
		{
			// At least Baron or higher and the skill Clan Imperium
			hasRight = true;
		}

		if(!hasRight)
		{
			requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
			return;
		}

		// Get the target's party leader, and do whole actions on him.
		L2PcInstance targetLeader = target.getParty().getLeader();
		SystemMessage sm;
		if(targetLeader.isProcessingRequest())
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(targetLeader.getName());
			requestor.sendPacket(sm);
		}
		else
		{
			requestor.onTransactionRequest(targetLeader);
			targetLeader.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1).addString(requestor.getName()));
			targetLeader.sendPacket(new ExAskJoinMPCC(requestor.getName()));

			requestor.sendMessage("You invited " + targetLeader.getName() + " to your Command Channel.");
		}
	}
}