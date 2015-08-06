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

package dwo.gameserver.handler.usercommands;

import dwo.gameserver.handler.CommandHandler;
import dwo.gameserver.handler.HandlerParams;
import dwo.gameserver.handler.NumericCommand;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.NpcHtmlMessage;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.StringUtil;

import java.text.SimpleDateFormat;

/**
 * Support for clan user commands.
 *
 * @author Tempy
 * @author Yorie
 */
public class Clan extends CommandHandler<Integer>
{
	/**
	 * Shows list of clan wars that was declared by this clan and not accepted.
	 * Syntax: /attacklist
	 */
	@NumericCommand(88)
	public boolean declaredClanWars(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		L2Clan clan = activeChar.getClan();

		if(clan == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_JOINED_IN_ANY_CLAN);
			return false;
		}

		activeChar.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON);

		for(ClanWar war : clan.getClanWars())
		{
			if(war.getPeriod() != ClanWar.ClanWarPeriod.PREPARATION || !war.isAttacker(clan))
			{
				continue;
			}

			L2Clan opposingClan = war.getOpposingClan();
			if(opposingClan == null)
			{
				continue;
			}

			if(opposingClan.getAllyId() > 0)
			{
				// Target With Ally
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(opposingClan.getName()).addString(opposingClan.getAllyName()));
			}
			else
			{
				// Target Without Ally
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(opposingClan.getName()));
			}
		}

		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);

		return true;
	}

	/**
	 * Shows list of clan wars that was declared on this clan, but not accepted.
	 * Syntax: /underattacklist
	 */
	@NumericCommand(89)
	public boolean receivedClanWars(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		L2Clan clan = activeChar.getClan();

		if(clan == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_JOINED_IN_ANY_CLAN);
			return false;
		}

		activeChar.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
		for(ClanWar war : clan.getClanWars())
		{
			if(war.getPeriod() != ClanWar.ClanWarPeriod.PREPARATION || !war.isOpposing(clan))
			{
				continue;
			}

			L2Clan attackerClan = war.getAttackersClan();
			if(attackerClan == null)
			{
				continue;
			}

			if(attackerClan.getAllyId() > 0)
			{
				// Target With Ally
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(attackerClan.getName()).addString(attackerClan.getAllyName()));
			}
			else
			{
				// Target Without Ally
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(attackerClan.getName()));
			}
		}
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);

		return true;
	}

	/**
	 * Shows list of started clan wars.
	 * Syntax: /warlist
	 */
	@NumericCommand(90)
	public boolean warList(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		L2Clan clan = activeChar.getClan();

		if(clan == null)
		{
			activeChar.sendPacket(SystemMessageId.NOT_JOINED_IN_ANY_CLAN);
			return false;
		}

		activeChar.sendPacket(SystemMessageId.WAR_LIST);
		for(ClanWar war : clan.getClanWars())
		{
			if(war.getPeriod() != ClanWar.ClanWarPeriod.MUTUAL)
			{
				continue;
			}

			L2Clan opposingClan = war.getOpposingClan();
			if(opposingClan == null)
			{
				continue;
			}

			if(opposingClan.getAllyId() > 0)
			{
				// Target With Ally
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(opposingClan.getName()).addString(opposingClan.getAllyName()));
			}
			else
			{
				// Target Without Ally
				activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(opposingClan.getName()));
			}
		}
		activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);

		return true;
	}

	@NumericCommand(100)
	public boolean clanPenalty(HandlerParams<Integer> params)
	{
		L2PcInstance activeChar = params.getPlayer();

		boolean penalty = false;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		StringBuilder htmlContent = StringUtil.startAppend(500, "<html><body>" +
			"<center><table width=270 border=0 bgcolor=111111>" +
			"<tr><td width=170>Penalty</td>" +
			"<td width=100 align=center>Expiration Date</td></tr>" +
			"</table><table width=270 border=0><tr>");

		if(activeChar.getClanJoinExpiryTime() > System.currentTimeMillis())
		{
			StringUtil.append(htmlContent, "<td width=170>Unable to join a clan.</td>" + "<td width=100 align=center>", format.format(activeChar.getClanJoinExpiryTime()), "</td>");
			penalty = true;
		}

		if(activeChar.getClanCreateExpiryTime() > System.currentTimeMillis())
		{
			StringUtil.append(htmlContent, "<td width=170>Unable to create a clan.</td>" + "<td width=100 align=center>", format.format(activeChar.getClanCreateExpiryTime()), "</td>");
			penalty = true;
		}

		if(activeChar.getClan() != null && activeChar.getClan().getCharPenaltyExpiryTime() > System.currentTimeMillis())
		{
			StringUtil.append(htmlContent, "<td width=170>Unable to invite a clan member.</td>" + "<td width=100 align=center>", format.format(activeChar.getClan().getCharPenaltyExpiryTime()), "</td>");
			penalty = true;
		}

		if(!penalty)
		{
			htmlContent.append("<td width=170>No penalty is imposed.</td>" + "<td width=100 align=center> </td>");
		}

		htmlContent.append("</tr></table><img src=\"L2UI.SquareWhite\" width=270 height=1>" + "</center></body></html>");

		NpcHtmlMessage penaltyHtml = new NpcHtmlMessage(0);
		penaltyHtml.setHtml(htmlContent.toString());
		activeChar.sendPacket(penaltyHtml);
		return true;
	}
}

