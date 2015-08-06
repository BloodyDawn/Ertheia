package dwo.gameserver.model.player;

import dwo.gameserver.datatables.xml.RecipeData;

public class L2ManufactureItem
{
	private int _recipeId;
	private long _cost;
	private boolean _isDwarven;

	public L2ManufactureItem(int recipeId, long cost)
	{
		_recipeId = recipeId;
		_cost = cost;
		_isDwarven = RecipeData.getInstance().getRecipeList(_recipeId).isDwarvenRecipe();
	}

	public int getRecipeId()
	{
		return _recipeId;
	}

	public long getCost()
	{
		return _cost;
	}

	public boolean isDwarven()
	{
		return _isDwarven;
	}
}
