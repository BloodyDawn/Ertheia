/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.network.game.clientpackets.packet.pledge.clan;

import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.clientpackets.L2GameClientPacket;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.pledge.ExPledgeCount;
import dwo.gameserver.network.game.serverpackets.packet.pledge.JoinPledge;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowInfoUpdate;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListAdd;
import dwo.gameserver.network.game.serverpackets.packet.pledge.PledgeShowMemberListAll;

public class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		_answer = readD();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		L2PcInstance requestor = activeChar.getRequest().getPartner();
		if(requestor == null)
		{
			return;
		}

		if(_answer == 0)
		{
			activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION).addString(requestor.getName()));
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION).addString(activeChar.getName()));
		}
		else
		{
			int pledgeType = 0;
			if(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge)
			{
				pledgeType = ((RequestJoinPledge) requestor.getRequest().getRequestPacket()).getPledgeType();
			}
			else if(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledgeByName)
			{
				pledgeType = ((RequestJoinPledgeByName) requestor.getRequest().getRequestPacket()).getPledgeType();
			}
			else
			{
				return; // hax
			}

			L2Clan clan = requestor.getClan();
			// we must double check this cause during response time conditions can be changed, i.e. another player could join clan
			if(clan.checkClanJoinCondition(requestor, activeChar, pledgeType))
			{
				activeChar.sendPacket(new JoinPledge(requestor.getClanId()));
				activeChar.setPledgeType(pledgeType);
				if(pledgeType == L2Clan.SUBUNIT_ACADEMY)
				{
					activeChar.setPowerGrade(9); // академия
					activeChar.setLvlJoinedAcademy(activeChar.getLevel());
				}
				else
				{
					activeChar.setPowerGrade(6); // Новый игрок получает "Уровень основного клана" - Power Grade = 6
				}

				clan.addClanMember(activeChar);
				activeChar.setClanPrivileges(activeChar.getClan().getRankPrivs(activeChar.getPowerGrade()));

				activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);

				clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addString(activeChar.getName()));

				if(activeChar.getClan().getCastleId() > 0)
				{
					CastleManager.getInstance().getCastleByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
				}
				if(activeChar.getClan().getFortId() > 0)
				{
					FortManager.getInstance().getFortByOwner(activeChar.getClan()).giveResidentialSkills(activeChar);
				}
				activeChar.sendSkillList();

				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));

				// this activates the clan tab on the new member
				activeChar.sendPacket(new PledgeShowMemberListAll(clan, activeChar));
				activeChar.sendPacket(new ExPledgeCount(clan.getOnlineMembersCount()));
				activeChar.setClanJoinExpiryTime(0);
				activeChar.broadcastUserInfo();
			}
		}

		activeChar.getRequest().onRequestResponse();
	}

	@Override
	public String getType()
	{
		return "[C] 25 RequestAnswerJoinPledge";
	}
}
