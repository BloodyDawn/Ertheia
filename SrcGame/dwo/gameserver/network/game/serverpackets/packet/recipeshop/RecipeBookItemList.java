package dwo.gameserver.network.game.serverpackets.packet.recipeshop;

import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;

import java.util.Collection;

public class RecipeBookItemList extends L2GameServerPacket
{
	private Collection<L2RecipeList> _recipes;
	private boolean _isDwarvenCraft;
	private int _maxMp;

	public RecipeBookItemList(boolean isDwarvenCraft, int maxMp)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_maxMp = maxMp;
	}

	public void addRecipes(Collection<L2RecipeList> recipeBook)
	{
		_recipes = recipeBook;
	}

	@Override
	protected void writeImpl()
	{
		writeD(_isDwarvenCraft ? 0x00 : 0x01); //0 = Dwarven - 1 = Common
		writeD(_maxMp);

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
	}
}