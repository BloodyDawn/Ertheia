package dwo.gameserver.handler.items;

import dwo.config.scripts.ConfigWorldStatistic;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import dwo.gameserver.model.world.worldstat.CategoryType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;
import dwo.gameserver.util.Broadcast;
import org.apache.log4j.Level;

/***************************************
 * Project: godworld.ru
 * Date:	09.08.12 15:57
 * Name:    Bacek ( created / edited )
 **************************************/

public class SoulShots implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
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

		L2PcInstance activeChar = playable.getActingPlayer();
		L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		L2Weapon weaponItem = activeChar.getActiveWeaponItem();
		int itemId = item.getItemId();

		// Проверяем, может-ли вообще оружие использовать Заряды Духа
		if(weaponInst == null || weaponItem == null || weaponItem.getSoulShotCount() == 0)
		{
			if(!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_USE_SOULSHOTS);
			}
			return false;
		}

		// Проверяем совпадает ли грейд
		if(item.getEtcItem().getSoulshotGradeForItem() != weaponItem.getSoulshotGradeForItem())
		{
			if(!activeChar.getAutoSoulShot().contains(itemId))
			{
				activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
			}
			return false;
		}

		activeChar.soulShotLock.lock();
		try
		{
			// Проверяем не заряжен ли уже Заряд Духа в оружие
			if(weaponInst.getChargedSoulshot() != L2ItemInstance.CHARGED_NONE)
			{
				return false;
			}

			// Заряжем в оружие Заряд Духа если их хватает в инвентаре
			int SSCount = weaponItem.getSoulShotCount();
			if(weaponItem.getReducedSoulShot() > 0 && weaponItem.getReducedSoulShotChance())
			{
				SSCount = weaponItem.getReducedSoulShot();
			}
			if(!activeChar.destroyItemWithoutTrace(item.getObjectId(), SSCount, null, false))
			{
				if(!activeChar.disableAutoShot(itemId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS);
				}
				return false;
			}

			// Записываем в статистику использование Зарядов Души начиная с D грейда
			if(weaponItem.getCrystalType() != CrystalGrade.NONE)
			{
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					activeChar.updateWorldStatistic(CategoryType.SS_CONSUMED, weaponItem.getSoulshotGradeForItem(), SSCount);
				}
			}

			// Заряжаем следующий Заряд Духа
			weaponInst.setChargedSoulshot(L2ItemInstance.CHARGED_SOULSHOT);
		}
		finally
		{
			activeChar.soulShotLock.unlock();
		}

		// Получаем Ид скила и используем его
		if(item.getEtcItem().getSkills() != null)
		{
			// Шлем сообщение о юзе сосок
			activeChar.sendPacket(SystemMessageId.ENABLED_SOULSHOT);
			Broadcast.toSelfAndKnownPlayersInRadius(activeChar, new MagicSkillUse(activeChar, activeChar, item.getEtcItem().getSkills()[0].getSkillId(), item.getEtcItem().getSkills()[0].getSkillLvl(), 0, 0), 600);
			return true;
		}
		return false;
	}
}