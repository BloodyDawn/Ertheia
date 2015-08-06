package dwo.gameserver.handler.items;

import dwo.gameserver.datatables.xml.ShapeShiftingItemsData;
import dwo.gameserver.handler.IItemHandler;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.shapeshift.ShapeShiftData;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.shapeshifting.ExChoose_Shape_Shifting_Item;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.08.12
 * Time: 22:09
 */
public class ShapeShifting implements IItemHandler
{
	/*
		Ограничения по использованию для Оружия:
		1. Оружие, которое было использована для придания скина текущему оружию игрока будет удалено
		2. Текущее оружие и оружие для ремоделлинга должны быть одного типа(например Requiem Cutter и Vesper Cutter)
		3. Скин оружия будет удален, если будет вставлен кристалл души
		4. Использование Свитка Снятия Оков и Свитка Благословения приведет к удалению скина
		5. Для восстановления старого внешнего вида необходим камень восстановления
		6. Стоимость изменения скина 800.000 адены

		Ограничения по использованию для Брони:
		1. Броня, которая была использована для придания скина текущей броне игрока будет удалена
		2. Текущее оружие и оружие для ремоделлинга должны быть одного типа(например нельзя применить тяжелую броню к робе)
		3. Скин брони будет удален, если будет изменен тип сета у кузнеца
		4. Использование Свитка Снятия Оков и Свитка Благословения приведет к удалению скина
		5. Для восстановления старого внешнего вида необходим камень восстановления
		6. Стоимость изменения скина 125.000 адены
		7. Камаели могут менять только скин легкой брони

		Ограничения по использованию Камней для головных аксессуаров:
		1.Изменяют облик головных аксессуаров
		2.Могут использоваться только аксессуары с одинаковым количеством слотов для одевания
		TODO		3.Не могут использоваться временные предметы и Диадема Дворянина
		4.При использовании кулона скин пропадает
		TODO		5.Применяется половое различие при использовании скинов. Например если на бесполый головной аксессуар применить как скин женский головной аксессуар, то мужские персонажи не смогут его носить
	 */

	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if(!(playable instanceof L2PcInstance))
		{
			return false;
		}

		if(playable.isCastingNow())
		{
			return false;
		}

		L2PcInstance activeChar = (L2PcInstance) playable;
		if(activeChar.isEnchanting())
		{
			activeChar.sendPacket(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
			return false;
		}

		ShapeShiftData data = ShapeShiftingItemsData.getInstance().getShapeShiftItem(item.getItemId());
		if(data != null)
		{
			activeChar.setIsEnchanting(true);
			activeChar.setActiveShapeShiftingItem(item);
			activeChar.sendPacket(new ExChoose_Shape_Shifting_Item(data.getShapeType(), data.getShapeShiftingWindow(), item.getItemId()));
		}
		return true;
	}
}
