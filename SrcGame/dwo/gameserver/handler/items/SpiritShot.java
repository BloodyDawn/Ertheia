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
 * Date:	09.08.12 15:56
 * Name:    Bacek ( created / edited )
 **************************************/

public class SpiritShot implements IItemHandler
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

			// Проверяем, может-ли вообще оружие использовать Заряды Души
			if(weaponInst == null || weaponItem.getSpiritShotCount() == 0)
			{
				if(!activeChar.getAutoSoulShot().contains(itemId))
				{
					activeChar.sendPacket(SystemMessageId.CANNOT_USE_SPIRITSHOTS);
				}
				return false;
			}

			// Проверяем не заряжен ли уже Заряд Души в оружие
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

			// Заряжем в оружие Заряд Души если их хватает в инвентаре
			if(!activeChar.destroyItemWithoutTrace(item.getObjectId(), weaponItem.getSpiritShotCount(), null, false))
			{
				if(!activeChar.disableAutoShot(itemId))
				{
					activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS);
				}
				return false;
			}
			// Записываем в статистику использование Зарядов Души начиная с D грейда
			if(weaponItem.getCrystalType() != CrystalGrade.NONE)
			{
				if(ConfigWorldStatistic.WORLD_STATISTIC_ENABLED)
				{
					activeChar.updateWorldStatistic(CategoryType.SPS_CONSUMED, weaponItem.getSoulshotGradeForItem(), weaponItem.getSpiritShotCount());
				}
			}

			// Charge Spirit shot
			weaponInst.setChargedSpiritshot(L2ItemInstance.CHARGED_SPIRITSHOT);

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