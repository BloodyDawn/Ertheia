package dwo.gameserver.engine.databaseengine.idfactory;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.queries.ChaosFestival;
import dwo.gameserver.datatables.sql.queries.clan.ClanSkills;
import dwo.gameserver.datatables.sql.queries.clan.ClanWars;
import dwo.gameserver.datatables.sql.queries.clan.СlanSubpledges;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.FiltredStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.player.formation.clan.ClanWar;
import dwo.gameserver.util.database.DatabaseUtils;
import gnu.trove.list.array.TIntArrayList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class IdFactory
{
	public static final int FIRST_OID = 0x10000000;
	public static final int LAST_OID = 0x7FFFFFFF;
	public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;
	@Deprecated
	protected static final String[] ID_UPDATES = {
		"UPDATE items                 SET owner_id = ?    WHERE owner_id = ?",
		"UPDATE items                 SET object_id = ?   WHERE object_id = ?",
		"UPDATE character_quests      SET charId = ?     WHERE charId = ?",
		"UPDATE character_contacts     SET charId = ?     WHERE charId = ?",
		"UPDATE character_contacts     SET friendId = ?   WHERE charId = ?",
		"UPDATE character_relations   SET charId = ?     WHERE charId = ?",
		"UPDATE character_relations   SET friendId = ?   WHERE friendId = ?",
		"UPDATE character_hennas      SET charId = ? WHERE charId = ?",
		"UPDATE character_recipebook  SET charId = ? WHERE charId = ?",
		"UPDATE character_recipeshoplist  SET charId = ? WHERE charId = ?",
		"UPDATE character_shortcuts   SET charId = ? WHERE charId = ?",
		"UPDATE character_shortcuts   SET shortcut_id = ? WHERE shortcut_id = ? AND type = 1", // items
		"UPDATE character_macroses    SET charId = ? WHERE charId = ?",
		"UPDATE character_skills      SET charId = ? WHERE charId = ?",
		"UPDATE character_skills_save SET charId = ? WHERE charId = ?",
		"UPDATE character_subclasses  SET charId = ? WHERE charId = ?",
		"UPDATE character_binds		  SET obj_id = ? WHERE obj_id = ?",
		"UPDATE characters            SET charId = ? WHERE charId = ?",
		"UPDATE characters            SET clanid = ?      WHERE clanid = ?",
		"UPDATE clan_data             SET clan_id = ?     WHERE clan_id = ?",
		"UPDATE siege_clans           SET clan_id = ?     WHERE clan_id = ?",
		"UPDATE clan_data             SET ally_id = ?     WHERE ally_id = ?",
		"UPDATE clan_data             SET leader_id = ?   WHERE leader_id = ?",
		"UPDATE pets                  SET item_obj_id = ? WHERE item_obj_id = ?",
		"UPDATE character_hennas     SET charId = ? WHERE charId = ?",
		"UPDATE itemsonground         SET object_id = ?   WHERE object_id = ?",
		"UPDATE auction_bid          SET bidderId = ?      WHERE bidderId = ?",
		"UPDATE auction_watch        SET charObjId = ?     WHERE charObjId = ?",
		"UPDATE olympiad_fights        SET charOneId = ?     WHERE charOneId = ?",
		"UPDATE olympiad_fights        SET charTwoId = ?     WHERE charTwoId = ?",
		"UPDATE heroes_diary        SET charId = ?     WHERE charId = ?",
		"UPDATE olympiad_nobles        SET charId = ?     WHERE charId = ?",
		"UPDATE character_offline_trade SET charId = ?     WHERE charId = ?",
		"UPDATE character_offline_trade_items SET charId = ? WHERE charId = ?",
		"UPDATE clanhall             SET ownerId = ?       WHERE ownerId = ?"
	};
	protected static final String[] ID_CHECKS = {
		"SELECT owner_id    FROM items                 WHERE object_id >= ?   AND object_id < ?",
		"SELECT object_id   FROM items                 WHERE object_id >= ?   AND object_id < ?",
		"SELECT charId      FROM character_quests      WHERE charId >= ?     AND charId < ?",
		"SELECT charId      FROM character_contacts    WHERE charId >= ?     AND charId < ?",
		"SELECT contactId   FROM character_contacts    WHERE contactId >= ?  AND contactId < ?",
		"SELECT charId      FROM character_relations     WHERE charId >= ?     AND charId < ?",
		"SELECT charId      FROM character_relations     WHERE friendId >= ?   AND friendId < ?",
		"SELECT charId      FROM character_hennas      WHERE charId >= ? AND charId < ?",
		"SELECT charId      FROM character_recipebook  WHERE charId >= ?     AND charId < ?",
		"SELECT charId      FROM character_recipeshoplist  WHERE charId >= ?     AND charId < ?",
		"SELECT charId      FROM character_shortcuts   WHERE charId >= ? AND charId < ?",
		"SELECT charId      FROM character_macroses    WHERE charId >= ? AND charId < ?",
		"SELECT charId      FROM character_skills      WHERE charId >= ? AND charId < ?",
		"SELECT charId      FROM character_skills_save WHERE charId >= ? AND charId < ?",
		"SELECT charId      FROM character_subclasses  WHERE charId >= ? AND charId < ?",
		"SELECT charId      FROM character_binds  WHERE obj_id >= ? AND obj_id < ?",
		"SELECT charId      FROM characters            WHERE charId >= ?      AND charId < ?",
		"SELECT clanid      FROM characters            WHERE clanid >= ?      AND clanid < ?",
		"SELECT clan_id     FROM clan_data             WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT clan_id     FROM siege_clans           WHERE clan_id >= ?     AND clan_id < ?",
		"SELECT ally_id     FROM clan_data             WHERE ally_id >= ?     AND ally_id < ?",
		"SELECT leader_id   FROM clan_data             WHERE leader_id >= ?   AND leader_id < ?",
		"SELECT item_obj_id FROM pets                  WHERE item_obj_id >= ? AND item_obj_id < ?",
		"SELECT object_id   FROM itemsonground        WHERE object_id >= ?   AND object_id < ?"
	};
	protected static final IdFactory _instance;
	private static final String[][] ID_EXTRACTS = {
		{"characters", "charId"}, {"items", "object_id"}, {"clan_data", "clan_id"}, {"itemsonground", "object_id"},
		{"messages", "messageId"}
	};
	private static final String[] TIMESTAMPS_CLEAN = {
		"DELETE FROM character_instance_time WHERE time <= ?",
		"DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?",
		"DELETE FROM character_item_reuse_save WHERE systime <= ?"
	};
	public static Logger _log = LogManager.getLogger(IdFactory.class);
	protected boolean _initialized;

	protected IdFactory()
	{
		setAllCharacterOffline();
		if(Config.DATABASE_CLEAN_UP)
		{
			cleanUpDB();
			if(Config.ALLOW_WEDDING)
			{
				cleanInvalidWeddings();
			}
		}
		cleanUpTimeStamps();
	}

	static
	{
		switch(Config.IDFACTORY_TYPE)
		{
			case BitSet:
				_instance = new BitSetIDFactory();
				break;
			case Stack:
				_instance = new StackIDFactory();
				break;
			default:
				_instance = null;
				break;
		}
	}

	public static IdFactory getInstance()
	{
		return _instance;
	}

	/**
	 * Sets all character offline
	 */
	private void setAllCharacterOffline()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("UPDATE characters SET online = 0");
			_log.log(Level.INFO, "Updated characters online status.");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not update characters online status: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Cleans up Database
	 */
	private void cleanUpDB()
	{
		ThreadConnection con = null;
		FiltredStatement stmt = null;
		try
		{
			long cleanupStart = System.currentTimeMillis();
			int cleanCount = 0;
			con = L2DatabaseFactory.getInstance().getConnection();
			stmt = con.createStatement();
			// Misc/Account Related
			// Please read the descriptions above each before uncommenting them. If you are still
			// unsure of what exactly it does, leave it commented out. This is for those who know
			// what they are doing. :)

			// Deletes only accounts that HAVE been logged into and have no characters associated
			// with the account.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM accounts WHERE accounts.lastactive > 0 AND accounts.login NOT IN (SELECT account_name FROM characters);");

			// Deletes any accounts that don't have characters. Whether or not the player has ever
			// logged into the account.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM accounts WHERE accounts.login NOT IN (SELECT account_name FROM characters);");

			// Deletes banned accounts that have not been logged into for xx amount of days
			// (specified at the end of the script, default is set to 90 days). This prevents
			// accounts from being deleted that were accidentally or temporarily banned.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM accounts WHERE accounts.accessLevel < 0 AND DATEDIFF(CURRENT_DATE( ) , FROM_UNIXTIME(`lastactive`/1000)) > 90;");
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM characters WHERE characters.account_name NOT IN (SELECT login FROM accounts);");

			// If the character does not exist...
			cleanCount += stmt.executeUpdate("DELETE FROM world_statistic WHERE world_statistic.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_contacts WHERE character_contacts.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_contacts WHERE character_contacts.contactId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_relations WHERE character_relations.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_relations WHERE character_relations.friendId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_quests WHERE character_quests.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_recipeshoplist WHERE character_recipeshoplist.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_skills WHERE character_skills.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_raid_points WHERE character_raid_points.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_instance_time WHERE character_instance_time.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_binds WHERE character_binds.obj_id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT charId FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data) AND items.owner_id != -1;");
			cleanCount += stmt.executeUpdate("DELETE FROM items WHERE items.owner_id = -1 AND loc LIKE 'MAIL' AND loc_data NOT IN (SELECT messageId FROM messages WHERE senderId = -1);");
			cleanCount += stmt.executeUpdate("DELETE FROM item_auction_bid WHERE item_auction_bid.playerObjId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM item_attributes WHERE item_attributes.itemId NOT IN (SELECT object_id FROM items);");
			cleanCount += stmt.executeUpdate("DELETE FROM item_elementals WHERE item_elementals.itemId NOT IN (SELECT object_id FROM items);");
			cleanCount += stmt.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM heroes WHERE heroes.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles_eom WHERE olympiad_nobles_eom.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_recommendation WHERE character_recommendation.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_data WHERE clan_data.clan_id NOT IN (SELECT clanid FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charOneId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charTwoId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM heroes_diary WHERE heroes_diary.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_offline_trade WHERE character_offline_trade.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_offline_trade_items WHERE character_offline_trade_items.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_quest_global_data WHERE character_quest_global_data.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM character_tpbookmark WHERE character_tpbookmark.charId NOT IN (SELECT charId FROM characters);");
			cleanCount += stmt.executeUpdate(ChaosFestival.CLEAR_OLD_LOGS);

			// If the clan does not exist...
			// TODO: Очистка Id из таблицы статистики клана, как только будет реализована
			cleanCount += stmt.executeUpdate("DELETE FROM character_account WHERE character_account.account_name NOT IN (SELECT account_name FROM characters);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate(ClanSkills.DELETE_CLAN_SKILLS);
			cleanCount += stmt.executeUpdate(СlanSubpledges.DELETE_SUBPLEDGES_1);
			cleanCount += stmt.executeUpdate(ClanWars.DELETE_CLAN_WARS_2);
			cleanCount += stmt.executeUpdate(ClanWars.DELETE_CLAN_WARS_3);
			cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE period = 'PEACE' AND period_start_time <= " + (System.currentTimeMillis() - ClanWar.PEACE_DURATION) / 1000);
			cleanCount += stmt.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.agit_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
			cleanCount += stmt.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM clan_notices WHERE clan_notices.clan_id NOT IN (SELECT clan_id FROM clan_data);");
			cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
			// Untested, leaving commented out until confirmation that it's safe/works properly. Was
			// initially removed because of a bug. Search for idfactory.java changes in the trac for
			// further info.
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM auction WHERE auction.id IN (SELECT id FROM clanhall WHERE ownerId <> 0) AND auction.sellerId=0;");
			// cleanCount +=
			// stmt.executeUpdate("DELETE FROM auction_bid WHERE auctionId NOT IN (SELECT id FROM auction)");

			// Forum Related
			cleanCount += stmt.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
			cleanCount += stmt.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT charId FROM characters) AND forums.forum_parent=3;");
			cleanCount += stmt.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
			cleanCount += stmt.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");

			// Update needed items after cleaning has taken place.
			stmt.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
			stmt.executeUpdate(СlanSubpledges.UPDATE_SUBPLEDGES);
			stmt.executeUpdate("UPDATE characters SET clanid=0, clan_privs=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0, clan_join_expiry_time=0, clan_create_expiry_time=0 WHERE characters.clanid > 0 AND characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
			stmt.executeUpdate("UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE clanhall.ownerId NOT IN (SELECT clan_id FROM clan_data);");
			stmt.executeUpdate("UPDATE fort SET owner=0 WHERE owner NOT IN (SELECT clan_id FROM clan_data);");

			_log.log(Level.INFO, "Cleaned " + cleanCount + " elements from database in " + (System.currentTimeMillis() - cleanupStart) / 1000 + " s");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not clean up database: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}
	}

	private void cleanInvalidWeddings()
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			statement.executeUpdate("DELETE FROM mods_wedding WHERE (player1Id OR player2Id) NOT IN (SELECT charId FROM characters)");
			_log.log(Level.INFO, "Cleaned up invalid Weddings.");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "Could not clean up invalid Weddings: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	private void cleanUpTimeStamps()
	{
		ThreadConnection con = null;
		FiltredPreparedStatement stmt = null;
		try
		{
			int cleanCount = 0;
			con = L2DatabaseFactory.getInstance().getConnection();
			for(String line : TIMESTAMPS_CLEAN)
			{
				stmt = con.prepareStatement(line);
				stmt.setLong(1, System.currentTimeMillis());
				cleanCount += stmt.executeUpdate();
			}

			_log.log(Level.INFO, "Cleaned " + cleanCount + " expired timestamps from database.");
		}
		catch(SQLException e)
		{
			_log.log(Level.ERROR, "", e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, stmt);
		}
	}

	/**
	 * @return temp
	 * @throws SQLException
	 */
	protected int[] extractUsedObjectIDTable() throws Exception
	{
		ThreadConnection con = null;
		FiltredStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			TIntArrayList temp = new TIntArrayList();

			String ensureCapacityQuery = "SELECT ";
			String extractUsedObjectIdsQuery = "";

			for(String[] tblClmn : ID_EXTRACTS)
			{
				ensureCapacityQuery += "(SELECT COUNT(*) FROM " + tblClmn[0] + ") + ";
				extractUsedObjectIdsQuery += "SELECT " + tblClmn[1] + " FROM " + tblClmn[0] + " UNION ";
			}
			ensureCapacityQuery = ensureCapacityQuery.substring(0, ensureCapacityQuery.length() - 3); // Remove the last " + "
			extractUsedObjectIdsQuery = extractUsedObjectIdsQuery.substring(0, extractUsedObjectIdsQuery.length() - 7); // Remove the last " UNION "

			try(ResultSet rs = statement.executeQuery(ensureCapacityQuery))
			{
				rs.next();
				temp.ensureCapacity(rs.getInt(1));
			}

			try(ResultSet rs = statement.executeQuery(extractUsedObjectIdsQuery))
			{
				while(rs.next())
				{
					temp.add(rs.getInt(1));
				}
			}

			temp.sort();

			return temp.toArray();
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public abstract int getNextId();

	/**
	 * return a used Object ID back to the pool
	 *
	 * @param id ID
	 */
	public abstract void releaseId(int id);

	public abstract int size();
}
