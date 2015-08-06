package dwo.scripts.ai.player;

import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.actor.controller.player.RecipeBookController;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;

public class CharDelete extends Quest
{
	public CharDelete(String name, String desc)
	{
		super(name, desc);
		addEventId(HookType.ON_CHAR_DELETE);
	}

	public static void main(String[] args)
	{
		new CharDelete("CharDelete", "ai");
	}

	@Override
	public void onCharDelete(int objid)
	{
		CharNameTable.getInstance().removeName(objid);

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(Characters.DELETE_CHARACTER_RELATIONS_BY_CHARID);
			statement.setInt(1, objid);
			statement.setInt(2, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_macroses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_quests WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_quest_global_data WHERE charId=?");
			statement.setInt(1, objid);
			statement.executeUpdate();
			statement.clearParameters();

			RecipeBookController.removeAllRecipes(objid);

			statement = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_skills WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_skills_save WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_subclasses WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM heroes WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM olympiad_nobles WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM pets WHERE item_obj_id IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM item_attributes WHERE itemId IN (SELECT object_id FROM items WHERE items.owner_id=?)");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM items WHERE owner_id=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_raid_points WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_recommendation WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement("DELETE FROM character_instance_time WHERE charId=?");
			statement.setInt(1, objid);
			statement.execute();
			statement.clearParameters();

			statement = con.prepareStatement(Characters.DELETE_CHAR_CHARID);
			statement.setInt(1, objid);
			statement.execute();
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "Error deleting character.", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}
}