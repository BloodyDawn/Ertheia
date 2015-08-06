package dwo.gameserver.network.game.serverpackets.packet.ex;

import dwo.config.Config;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

/**
 * @author -Wooden-
 */

public class ExStorageMaxCount extends L2GameServerPacket
{
	private L2PcInstance _activeChar;
	private int _inventory;
	private int _warehouse;
	private int _clan;
	private int _privateSell;
	private int _privateBuy;
	private int _receipeD;
	private int _recipe;
	private int _inventoryExtraSlots;
	private int _inventoryQuestItems;

	public ExStorageMaxCount(L2PcInstance character)
	{
		_activeChar = character;
		_inventory = _activeChar.getInventoryLimit();
		_warehouse = _activeChar.getWareHouseLimit();
		_privateSell = _activeChar.getPrivateSellStoreLimit();
		_privateBuy = _activeChar.getPrivateBuyStoreLimit();
		_clan = Config.WAREHOUSE_SLOTS_CLAN;
		_receipeD = _activeChar.getRecipeController().getDwarvenRecipeLimit();
		_recipe = _activeChar.getRecipeController().getCommonRecipeLimit();
		_inventoryExtraSlots = (int) _activeChar.getStat().calcStat(Stats.INV_LIM, 0, null, null);
		_inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_inventory);
		writeD(_warehouse);
		writeD(_clan);
		writeD(_privateSell);
		writeD(_privateBuy);
		writeD(_receipeD);
		writeD(_recipe);
		writeD(_inventoryExtraSlots); // Belt inventory slots increase count
		writeD(_inventoryQuestItems);
        writeD(40);//TODO
        writeD(40);
	}
}