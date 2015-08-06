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
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.player.FloodAction;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

public class RequestGetItemFromPet extends L2GameClientPacket
{
	private int _objectId;
	private long _amount;
	private int _unknown;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_amount = readQ();
		_unknown = readD();// = 0 for most trades
	}

	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if(player == null)
		{
			return;
		}

		if(!getClient().getFloodProtectors().getTransaction().tryPerformAction(FloodAction.GET_FTOM_PET_ITEM))
		{
			return;
		}

		L2PetInstance pet = (L2PetInstance) player.getItemPet();
		if(player.getActiveEnchantItem() != null || pet == null)
		{
			return;
		}
		if(_amount < 0)
		{
			Util.handleIllegalPlayerAction(player, "[RequestGetItemFromPet] Character " + player.getName() + " of account " + player.getAccountName() + " tried to get item with oid " + _objectId + " from pet but has count < 0!", Config.DEFAULT_PUNISH);
			return;
		}
		if(_amount == 0)
		{
			return;
		}

		if(pet.transferItem(ProcessType.PETTRANSFER, _objectId, _amount, player.getInventory(), player, pet) == null)
		{
			_log.log(Level.WARN, "Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
		}
	}

	@Override
	public String getType()
	{
		return "[C] 8C RequestGetItemFromPet";
	}
}
