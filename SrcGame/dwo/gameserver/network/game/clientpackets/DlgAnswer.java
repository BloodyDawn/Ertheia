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

import dwo.config.Config;
import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.handler.AdminCommandHandler;
import dwo.gameserver.handler.IAdminCommandHandler;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.util.GMAudit;
import org.apache.log4j.Level;

/**
 * @author Dezmond_snz
 *         Format: cddd
 */
public class DlgAnswer extends L2GameClientPacket
{
	private int _messageId;
	private int _answer;
	private int _requesterId;

	@Override
	protected void readImpl()
	{
		_messageId = readD();
		_answer = readD();
		_requesterId = readD();
	}

	@Override
	public void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}

		HookManager.getInstance().notifyEvent(HookType.ON_DLGANSWER, null, getClient().getActiveChar(), _messageId, _answer, _requesterId);

		if(Config.DEBUG)
		{
			_log.log(Level.DEBUG, getType() + ": Answer accepted. Message ID " + _messageId + ", answer " + _answer + ", Requester ID " + _requesterId);
		}

		if(_messageId == SystemMessageId.RESSURECTION_REQUEST_BY_C1_FOR_S2_S3_XP.getId() || _messageId == SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId())
		{
			activeChar.reviveAnswer(_answer);
		}
		else if(_messageId == SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId() && _answer == 1)
		{
			activeChar.getSummonFriendController().summonMe(WorldManager.getInstance().getPlayer(_requesterId));
		}
		else if(_messageId == SystemMessageId.S1.getId())
		{
			String _command = activeChar.getPcAdmin().getAdminConfirmCmd();
			if(_command == null)
			{
				if(Config.ALLOW_WEDDING)
				{
					activeChar.engageAnswer(_answer);
				}
			}
			else
			{
				activeChar.getPcAdmin().setAdminConfirmCmd(null);
				if(_answer == 0)
				{
					return;
				}
				String command = _command.split(" ")[0];
				IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
				if(AdminTable.getInstance().hasAccess(command, activeChar.getAccessLevel()))
				{
					if(Config.GMAUDIT)
					{
						GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + ']', _command, activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
					}
					ach.useAdminCommand(_command, activeChar);
				}
			}
		}
		else if(_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			activeChar.gatesAnswer(_answer, 1);
		}
		else if(_messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			activeChar.gatesAnswer(_answer, 0);
		}
	}

	@Override
	public String getType()
	{
		return "[C] C5 DlgAnswer";
	}
}
