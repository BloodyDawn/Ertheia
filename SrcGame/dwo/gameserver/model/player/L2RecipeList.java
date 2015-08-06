package dwo.gameserver.model.player;

import dwo.gameserver.model.holders.ItemChanceHolder;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.skills.stats.StatsSet;
import javolution.util.FastList;

public class L2RecipeList
{
	private ItemHolder[] _recipes;
	private int _id;
	private int _craftLevel;
	private int _recipeId;
	private String _recipeName;
	private int _recipeSuccessRate;
	private FastList<ItemChanceHolder> _productList;
	private int _mpConsume;
	private boolean _isDwarvenRecipe;

	/**
	 * Конструктор, определяющий будующий рецепт
	 */
	public L2RecipeList(StatsSet set)
	{
		_recipes = new ItemHolder[0];
		_productList = new FastList<>();
		_id = set.getInteger("id");
		_craftLevel = set.getInteger("craftLevel");
		_recipeId = set.getInteger("recipeId");
		_recipeName = set.getString("recipeName");
		_recipeSuccessRate = set.getInteger("successRate");
		_mpConsume = set.getInteger("mpConsume");
		_isDwarvenRecipe = set.getBool("isDwarvenRecipe");
	}

	/**
	 * Добавляет L2RecipeInstance в L2RecipeList
	 * @param recipe добавляемый рецепт
	 */
	public void addRecipe(ItemHolder recipe)
	{
		int len = _recipes.length;
		ItemHolder[] tmp = new ItemHolder[len + 1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
	}

	/**
	 * Добавляет продукт для текущего рецепта
	 * @param product добавляемый продукт
	 */
	public void addProduct(ItemChanceHolder product)
	{
		_productList.add(product);
	}

	/**
	 * @return the Identifier of the Instance.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return the crafting level needed to use this L2RecipeList.
	 */
	public int getLevel()
	{
		return _craftLevel;
	}

	/**
	 * @return the Identifier of the L2RecipeList.
	 */
	public int getRecipeId()
	{
		return _recipeId;
	}

	/**
	 * @return the name of the L2RecipeList.
	 */
	public String getRecipeName()
	{
		return _recipeName;
	}

	/**
	 * @return the crafting success rate when using the L2RecipeList.
	 */
	public int getRecipeSuccessRate()
	{
		return _recipeSuccessRate;
	}

	/**
	 * @return {@code true} if this a Dwarven recipe or {@code false} if its a Common recipe
	 */
	public boolean isDwarvenRecipe()
	{
		return _isDwarvenRecipe;
	}

	/**
	 * @return the table containing all L2RecipeInstance (1 line of the recipe : Item-Quantity needed) of the L2RecipeList.
	 */
	public ItemHolder[] getRecipes()
	{
		return _recipes;
	}

	/**
	 * @return список продуктов, получаемых с рецепта
	 */
	public FastList<ItemChanceHolder> getProducts()
	{
		return _productList;
	}

	/**
	 * @return количество необходимой маны для крафта
	 */
	public int getMpConsume()
	{
		return _mpConsume;
	}
}