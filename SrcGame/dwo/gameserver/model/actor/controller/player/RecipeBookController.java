package dwo.gameserver.model.actor.controller.player;

import dwo.config.Config;
import dwo.gameserver.datatables.xml.RecipeData;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.model.player.L2ShortCut;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Player recipe list controller.
 *
 * @author Yorie
 */
public class RecipeBookController extends PlayerController
{
	private static final Logger log = LogManager.getLogger(RecipeBookController.class);
	private static final String LOAD_ALL_RECIPES = "SELECT `id`, `type`, `classIndex` FROM `character_recipebook` WHERE `charId` = ?";
	private static final String LOAD_COMMON_RECIPES = "SELECT `id`, `type`, `classIndex` FROM `character_recipebook` WHERE `charId` = ? AND `classIndex` = ? AND `type` = 1";
	private static final String ADD_RECIPE = "INSERT INTO `character_recipebook`(`charId`, `id`, `classIndex`, `type`) values(?, ?, ?, ?)";
	private static final String REMOVE_RECIPE = "DELETE FROM `character_recipebook` WHERE `charId` = ? AND `id` = ? AND `classIndex` = ?";
	private static final String REMOVE_PLAYER_RECIPES = "DELETE FROM `character_recipebook` WHERE `charId` = ?";
	private final Map<Integer, L2RecipeList> dwarvenRecipeBook = new FastMap<>();
	private final Map<Integer, L2RecipeList> commonRecipeBook = new FastMap<>();

	public RecipeBookController(L2PcInstance player)
	{
		super(player);
	}

	/**
	 * Removes all recipes for current player.
	 * This method should work with offline characters, so it's static and consumes character object ID.
	 */
	public static void removeAllRecipes(int objectId)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_PLAYER_RECIPES);
			statement.setInt(1, objectId);
			statement.execute();
		}
		catch(SQLException e)
		{
			log.log(Level.ERROR, "SQL exception while removing recipes for character " + objectId, e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public Map<Integer, L2RecipeList> getBook(RecipeBookType type)
	{
		switch(type)
		{
			case DWARVEN:
				return dwarvenRecipeBook;
			case COMMON:
			default:
				return commonRecipeBook;
		}
	}

	public Map<Integer, L2RecipeList> getDwarvenRecipeBook()
	{
		return getBook(RecipeBookType.DWARVEN);
	}

	public Map<Integer, L2RecipeList> getCommonRecipeBook()
	{
		return getBook(RecipeBookType.COMMON);
	}

	public int getRecipeLimit(RecipeBookType type)
	{
		switch(type)
		{
			case DWARVEN:
				return Config.DWARF_RECIPE_LIMIT + (int) player.getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
			case COMMON:
			default:
				return Config.COMMON_RECIPE_LIMIT + (int) player.getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		}
	}

	public int getDwarvenRecipeLimit()
	{
		return getRecipeLimit(RecipeBookType.DWARVEN);
	}

	public int getCommonRecipeLimit()
	{
		return getRecipeLimit(RecipeBookType.COMMON);
	}

	/**
	 * Checks if current player has recipe with ID @recipeId
	 * @param recipeId The Identifier of the L2RecipeList to check in the player's recipe books
	 * @return <b>TRUE</b> if player has the recipe on Common or Dwarven Recipe book else returns <b>FALSE</b>
	 */
	public boolean hasRecipe(int recipeId)
	{
		return dwarvenRecipeBook.containsKey(recipeId) || commonRecipeBook.containsKey(recipeId);
	}

	/**
	 * Checks if current player has recipe @recipe.
	 * @param recipe Recipe to search.
	 * @return True on success.
	 */
	public boolean hasRecipe(L2RecipeList recipe)
	{
		return hasRecipe(recipe.getId());
	}

	public void registerRecipe(L2RecipeList recipe, boolean store)
	{
		if(recipe.isDwarvenRecipe())
		{
			dwarvenRecipeBook.put(recipe.getId(), recipe);
		}
		else
		{
			commonRecipeBook.put(recipe.getId(), recipe);
		}

		if(store)
		{
			addRecipe(recipe.getId(), recipe.isDwarvenRecipe());
		}
	}

	/**
	 * Restore recipe book data for this L2PcInstance.
	 *
	 * @param restoreCommonRecipes If true, common recipes will be restored too.
	 */
	public void restoreBook(boolean restoreCommonRecipes)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(restoreCommonRecipes ? LOAD_ALL_RECIPES : LOAD_COMMON_RECIPES);

			statement.setInt(1, player.getObjectId());

			if(!restoreCommonRecipes)
			{
				statement.setInt(2, player.getClassIndex());
			}
			rset = statement.executeQuery();

			dwarvenRecipeBook.clear();

			L2RecipeList recipe;
			while(rset.next())
			{
				recipe = RecipeData.getInstance().getRecipeList(rset.getInt("id"));

				registerRecipe(recipe, false);
			}
		}
		catch(Exception e)
		{
			log.log(Level.ERROR, "Could not restore recipe book data:" + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	/**
	 * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from
	 * table _CommonRecipeBook, those table contain all L2RecipeList of the
	 * L2PcInstance <BR>
	 * <BR>
	 *
	 * @param recipeId The Identifier of the L2RecipeList to remove from the recipe book.
	 */
	public void removeRecipe(int recipeId)
	{
		if(dwarvenRecipeBook.containsKey(recipeId))
		{
			dwarvenRecipeBook.remove(recipeId);
			removeRecipe(recipeId, true);
		}
		else if(commonRecipeBook.containsKey(recipeId))
		{
			commonRecipeBook.remove(recipeId);
			removeRecipe(recipeId, false);
		}
		else
		{
			log.log(Level.WARN, "Attempted to remove unknown RecipeList: " + recipeId);
		}

		player.getShortcutController().removeShortcut(recipeId, L2ShortCut.ShortcutType.RECIPE);
	}

	private void removeRecipe(int recipeId, boolean isDwarf)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(REMOVE_RECIPE);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? player.getClassIndex() : 0);
			statement.execute();
		}
		catch(SQLException e)
		{
			log.log(Level.ERROR, "SQL exception while deleting recipe: " + recipeId + " from character " + player.getObjectId(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void addRecipe(int recipeId, boolean isDwarf)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(ADD_RECIPE);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, recipeId);
			statement.setInt(3, isDwarf ? player.getClassIndex() : 0);
			statement.setInt(4, isDwarf ? 1 : 0);
			statement.execute();
		}
		catch(SQLException e)
		{
			log.log(Level.ERROR, "SQL exception while inserting recipe: " + recipeId + " from character " + player.getObjectId(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public static enum RecipeBookType
	{
		COMMON,
		DWARVEN;

		public static RecipeBookType valueOf(int value)
		{
			return value < 0 || value >= RecipeBookType.values().length ? COMMON : values()[value];
		}
	}
}
