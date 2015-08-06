package dwo.gameserver.handler;

import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public interface IItemHandler
{
	Logger _log = LogManager.getLogger(IItemHandler.class);

	/**
	 * Запуск задачи, указанной в хендлере предмета
	 *
	 * @param playable    действующий персонаж
	 * @param item        используемый предмет
	 * @param forceUse    зажат ли Control
	 * @return {@code true} если использование предмета прошло удачно, {@code false} если нет
	 */
	boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse);
}
