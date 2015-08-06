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
package dwo.gameserver.network.game.clientpackets;

import dwo.gameserver.datatables.sql.ClanTable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.player.formation.clan.L2ClanMember;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS();
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}
		L2Clan playerClan = player.getClan();
		if(playerClan == null)
		{
			return;
		}

		L2Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);

		if(clan == null)
		{
			player.sendPacket(SystemMessageId.WRONG_DECLARATION_TARGET);
			player.sendActionFailed();
			return;
		}

		if(!playerClan.isAtWarWith(clan.getClanId()))
		{
			player.sendMessage("У Вас нет войны с эти кланом.");
			player.sendActionFailed();
			return;
		}

		// Check if player who does the request has the correct rights to do it
		if((player.getClanPrivileges() & L2Clan.CP_CL_PLEDGE_WAR) != L2Clan.CP_CL_PLEDGE_WAR)
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}

		//_log.log(Level.INFO, "RequestStopPledgeWar: By leader or authorized player: " + playerClan.getLeaderName() + " of clan: "
		//	+ playerClan.getName() + " to clan: " + _pledgeName);

		//        L2PcInstance leader = L2World.getInstance().getPlayer(clan.getLeaderName());
		//        if(leader != null && leader.isOnline() == 0)
		//        {
		//            player.sendMessage("Clan leader isn't online.");
		//            player.sendActionFailed();
		//            return;
		//        }

		//        if (leader.isProcessingRequest())
		//        {
		//            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
		//            sm.addString(leader.getName());
		//            player.sendPacket(sm);
		//            return;
		//        }

		for(L2ClanMember member : playerClan.getMembers())
		{
			if(member == null || member.getPlayerInstance() == null)
			{
				continue;
			}
			if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(member.getPlayerInstance()))
			{
				player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
				return;
			}
		}

		ClanWar war = playerClan.getClanWar(clan);
		if(war != null)
		{
			war.cancel(playerClan);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 4F RequestStopPledgeWar";
	}
}