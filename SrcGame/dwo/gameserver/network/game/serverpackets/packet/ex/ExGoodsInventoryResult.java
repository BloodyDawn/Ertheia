package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 23.06.12
 * Time: 13:51
 */
public class ExGoodsInventoryResult extends L2GameServerPacket
{
	public static int SYS_FAILED = -1;      // Неверный запрос
	public static int SYS_ERROR = -2;       // Не удалось получить выбранный предмет из-за системной ошибки. Повторите попытку позднее.
	public static int SYS_SHORTAGE = -3;    //В инвентаре не хватает свободного места. Получение предмета возможно только при наличии не менее 20 процентов свободного места в инвентаре.
	public static int SYS_NOT_AVAILABLE = -4;  //В настоящее время сервер товаров недоступен. Повторите попытку позднее.
	public static int SYS_NOT_TRADE = -5; //Вы не можете пользоваться товарным инвентарем во время торговли, открытия личной торговой лавки или личной торговой мастерской.
	public static int SYS_NO_ITEMS = -6; //В товарном инвентаре нет предметов для доставки.
	/**
	 * SA Error
	 */
	// -101 В настоящее время товарный инвентарь недоступен из-за слишком большого количества пользователей. Повторите попытку позднее.
	// -102 В настоящее время получение предметов невозможно из-за слишком большого количества пользователей. Повторите попытку позднее.
	// -103 Предыдущий запрос еще не выполнен. Повторите попытку позднее.
	// -104
	// -105 Подписка на предмет уже отменена.
	// -107 На этом сервере невозможно получить выбранный предмет.
	// -108 Данный персонаж не может получить выбранный предмет.

	private int _msg;

	public ExGoodsInventoryResult(int msg)
	{
		_msg = msg;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_msg);
	}
}
