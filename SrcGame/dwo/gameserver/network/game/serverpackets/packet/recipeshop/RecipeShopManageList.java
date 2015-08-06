package dwo.gameserver.network.game.serverpackets.packet.recipeshop;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2ManufactureItem;
import dwo.gameserver.model.player.L2ManufactureList;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;

public class RecipeShopManageList extends L2GameServerPacket
{
	private L2PcInstance _seller;
	private boolean _isDwarven;
	private Collection<L2RecipeList> _recipes;

	public RecipeShopManageList(L2PcInstance seller, boolean isDwarven)
	{
		_seller = seller;
		_isDwarven = isDwarven;

		_recipes = _isDwarven && _seller.hasDwarvenCraft() ? _seller.getRecipeController().getDwarvenRecipeBook().values() : _seller.getRecipeController().getCommonRecipeBook().values();

		// clean previous recipes
		if(_seller.getCreateList() != null)
		{
			L2ManufactureList list = _seller.getCreateList();
			list.getList().stream().filter(item -> item.isDwarven() != _isDwarven || !seller.getRecipeController().hasRecipe(item.getRecipeId())).forEach(item -> list.getList().remove(item));
		}
	}

	@Override
	protected void writeImpl()
	{
		writeD(_seller.getObjectId());
		writeD((int) _seller.getAdenaCount());
		writeD(_isDwarven ? 0x00 : 0x01);

		if(_recipes == null)
		{
			writeD(0);
		}
		else
		{
			writeD(_recipes.size());//number of items in recipe book
			int i = 1;
			for(L2RecipeList list : _recipes)
			{
				writeD(list.getId());
				writeD(i);
				i++;
			}
		}

		if(_seller.getCreateList() == null)
		{
			writeD(0);
		}
		else
		{
			L2ManufactureList list = _seller.getCreateList();
			writeD(list.size());

			for(L2ManufactureItem item : list.getList())
			{
				writeD(item.getRecipeId());
				writeD(0x00);
				writeQ(item.getCost());
			}
		}
	}
}