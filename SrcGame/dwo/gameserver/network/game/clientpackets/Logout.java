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
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import org.apache.log4j.Level;

public class Logout extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{

	}

	@Override
	protected void runImpl()
	{
		// Dont allow leaving if player is fighting
		L2PcInstance player = getClient().getActiveChar();

		if(player == null)
		{
			return;
		}

		if(player.getActiveEnchantItem() != null || player.getActiveEnchantAttrItem() != null)
		{
			player.sendActionFailed();
			return;
		}

		if(player.isLocked())
		{
			_log.log(Level.WARN, "Player " + player.getName() + " tried to logout during class change.");
			player.sendActionFailed();
			return;
		}

		if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) && !(player.isGM() && Config.GM_RESTART_FIGHTING))
		{
			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "Player " + player.getName() + " tried to logout while fighting");
			}

			player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
			player.sendActionFailed();
			return;
		}

		if(player.getEventController().isParticipant())
		{
			player.sendMessage("A superior power doesn't allow you to leave the event");
			player.sendActionFailed();
			return;
		}

		// Remove player from Boss Zone
		player.removeFromBossZone();

		player.logout();
	}

	@Override
	public String getType()
	{
		return "[C] 09 Logout";
	}
}