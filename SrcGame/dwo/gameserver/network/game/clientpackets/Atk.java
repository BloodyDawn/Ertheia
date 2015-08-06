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

import dwo.gameserver.instancemanager.WorldManager;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.PlayerPrivateStoreType;

public class Atk extends L2GameClientPacket
{
	// cddddc
	private int _objectId;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _attackId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_attackId = readC();      // 0 for simple click   1 for shift-click
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		// avoid using expensive operations if not needed
		L2Object target;
		target = activeChar.getTargetId() == _objectId ? activeChar.getTarget() : WorldManager.getInstance().findObject(_objectId);
		if(target == null)
		{
			return;
		}

		// Players can't attack objects in the other instances
		// except from multiverse
		if(target.getInstanceId() != activeChar.getInstanceId() && activeChar.getInstanceId() != -1)
		{
			return;
		}

		// Only GMs can directly attack invisible characters
		if(target instanceof L2PcInstance && ((L2PcInstance) target).getAppearance().getInvisible() && !activeChar.isGM())
		{
			return;
		}

		// Если игрок ждет ответа на трейд-запрос, скидываем этот запрос
		if(activeChar.getActiveRequester() != null)
		{
			activeChar.cancelActiveTrade();
		}

		if(activeChar.getTarget() == target)
		{
			if(target.getObjectId() != activeChar.getObjectId() && activeChar.getPrivateStoreType() == PlayerPrivateStoreType.NONE && activeChar.getActiveRequester() == null)
			{
				target.onForcedAttack(activeChar);
			}
			else
			{
				activeChar.sendActionFailed();
			}
		}
		else
		{
			target.onAction(activeChar);
		}
	}

	@Override
	public String getType()
	{
		return "[C] 0A AttackRequest";
	}
}