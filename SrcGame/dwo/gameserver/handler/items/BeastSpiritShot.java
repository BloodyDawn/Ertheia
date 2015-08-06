package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.type.L2ActionType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

public class BeastSpiritShot implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(playable == null)
		{
			return false;
		}

		L2PcInstance activeOwner = playable.getActingPlayer();

		if(playable instanceof L2Summon)
		{
			activeOwner.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
			return false;
		}

		if(activeOwner.getPets().isEmpty())
		{
			activeOwner.sendPacket(SystemMessageId.PETS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
			return false;
		}

		if(!item.isEtcItem())
		{
			_log.log(Level.WARN, "Player " + playable.getName() + " trying use non-etcitem Soul Shot's. Need fix item: " + item.getItemId());
			return false;
		}

		int itemId = 0;
		boolean isBlessed = false;
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
			isBlessed = item.getEtcItem().getDefaultAction() == L2ActionType.summon_blessed_spiritshot;
			shotConsumption = pet.getSpiritShotsPerHit();
			shotCount = item.getCount();
			L2ItemInstance weaponInst = null;

			if(!(shotCount > shotConsumption))
			{
				// Not enough SpiritShots to use.
				if(!activeOwner.disableAutoShot(itemId))
				{
					activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET);
				}
				return false;
			}

			if(pet instanceof L2PetInstance)
			{
				weaponInst = pet.getActiveWeaponInstance();
			}

			if(weaponInst == null)
			{
				if(pet.getChargedSpiritShot() != L2ItemInstance.CHARGED_NONE)
				{
					return false;
				}

				if(isBlessed)
				{
					pet.setChargedSpiritShot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
				}
				else
				{
					pet.setChargedSpiritShot(L2ItemInstance.CHARGED_SPIRITSHOT);
				}
			}
			else
			{
				if(weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_SPIRITSHOT)
				{
					// SpiritShots are already active.
					return false;
				}

				if(isBlessed)
				{
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);
				}
				else
				{
					weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);
				}
			}
			if(!activeOwner.destroyItemWithoutTrace(item.getObjectId(), shotConsumption, null, false))
			{
				if(!activeOwner.disableAutoShot(itemId))
				{
					activeOwner.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITHOTS_FOR_PET);
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
