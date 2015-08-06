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
package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

/***************************************
 * Project: godworld.ru
 * Date:	09.08.12 16:36
 * Name:    Bacek ( created / edited )
 **************************************/

/**
 * Beast SoulShot Handler
 *
 * @author Tempy
 */
public class BeastSoulShot implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(playable == null)
		{
			return false;
		}

		if(!item.isEtcItem())
		{
			_log.log(Level.WARN, "Player " + playable.getName() + " trying use non-etcitem Soul Shot's. Need fix item: " + item.getItemId());
			return false;
		}

		L2PcInstance activeOwner = null;
		if(playable instanceof L2Summon)
		{
			activeOwner = ((L2Summon) playable).getOwner();
			activeOwner.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return false;
		}
		if(playable instanceof L2PcInstance)
		{
			activeOwner = (L2PcInstance) playable;
		}

		if(activeOwner == null)
		{
			return false;
		}

		if(activeOwner.getPets().isEmpty())
		{
			activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}

		int itemId = 0;
		short shotConsumption = 0;
		long shotCount = 0;

		for(L2Summon pet : activeOwner.getPets())
		{
			if(pet.isDead())
			{
				//activeOwner.sendPacket(SystemMessageId.SOULSHOTS_AND_SPIRITSHOTS_ARE_NOT_AVAILABLE_FOR_A_DEAD_PET);
				continue;
			}
			itemId = item.getItemId();
			shotConsumption = pet.getSoulShotsPerHit();
			shotCount = item.getCount();

			L2ItemInstance weaponInst = null;

			if(!(shotCount > shotConsumption))
			{
				// Not enough Soulshots to use.
				if(!activeOwner.disableAutoShot(itemId))
				{
					activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
				}
				return false;
			}
			if(pet instanceof L2PetInstance)
			{
				weaponInst = pet.getActiveWeaponInstance();
			}
			if(weaponInst == null)
			{
				if(pet.getChargedSoulShot() != L2ItemInstance.CHARGED_NONE)
				{
					continue;
				}

				pet.setChargedSoulShot(L2ItemInstance.CHARGED_SOULSHOT);
			}
			else
			{
				if(weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_SOULSHOT)
				{
					// SoulShots are already active.
					continue;
				}
				weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
			}
			// If the player doesn't have enough beast soulshot remaining, remove any auto soulshot task.
			if(!activeOwner.destroyItemWithoutTrace(item.getObjectId(), shotConsumption, null, false))
			{
				if(!activeOwner.disableAutoShot(itemId))
				{
					activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
				}
				return false;
			}

			// Получаем Ид скила и используем его
			if(item.getEtcItem().getSkills() != null)
			{
				// Шлем сообщение о юзе сосок
				activeOwner.sendPacket(SystemMessageId.PET_USE_SPIRITSHOT);
				Broadcast.toSelfAndKnownPlayersInRadius(activeOwner, new MagicSkillUse(pet, pet, item.getEtcItem().getSkills()[0].getSkillId(), item.getEtcItem().getSkills()[0].getSkillLvl(), 0, 0), 600);
			}
		}
		return true;
	}
}
