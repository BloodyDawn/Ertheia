package dwo.gameserver.handler.items;

import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

/***************************************
 * Project: godworld.ru
 * Date:	09.08.12 16:11
 * Name:    Bacek ( created / edited )
 **************************************/

public class BlessedSpiritShot implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		synchronized(this)
		{
			if(!(playable instanceof L2PcInstance))
			{
				return false;
			}

			if(!item.isEtcItem())
			{
				_log.log(Level.WARN, "Player " + playable.getName() + " trying use non-etcitem Soul Shot's. Need fix item: " + item.getItemId());
				return false;
			}

			L2PcInstance activeChar = (L2PcInstance) playable;
			L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
			L2Weapon weaponItem = activeChar.getActiveWeaponItem();
			int itemId = item.getItemId();

			// Check if Blessed SpiritShot can be used
			if(weaponInst == null || weaponItem == null || weaponItem.getSpiritShotCount() == 0)
			{
				if(!activeChar.getAutoSoulShot().contains(itemId))
				{
					activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
				}
				return false;
			}

			// Check if Blessed SpiritShot is already active (it can be charged over SpiritShot)
			if(weaponInst.getChargedSpiritshot() != L2ItemInstance.CHARGED_NONE)
			{
				return false;
			}

			// Проверяем совподает ли грейд
			if(item.getEtcItem().getSoulshotGradeForItem() != weaponItem.getSoulshotGradeForItem())
			{
				if(!activeChar.getAutoSoulShot().contains(itemId))
				{
					activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
				}
				return false;
			}

			// Consume Blessed SpiritShot if player has enough of them
			if(!activeChar.destroyItemWithoutTrace(item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
			{
				if(!activeChar.disableAutoShot(itemId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
				}
				return false;
			}

			// Charge Blessed SpiritShot
			weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT);

			// Получаем Ид скила и используем его
			if(item.getEtcItem().getSkills() != null)
			{
				// Шлем сообщение о юзе сосок
				activeChar.sendPacket(SystemMessageId.ENABLED_SPIRITSHOT);
				Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, item.getEtcItem().getSkills()[0].getSkillId(), item.getEtcItem().getSkills()[0].getSkillLvl(), 0, 0), 600);
				return true;
			}
			return false;
		}
	}
}
