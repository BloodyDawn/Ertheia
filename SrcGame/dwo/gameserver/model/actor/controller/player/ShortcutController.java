package dwo.gameserver.model.actor.controller.player;

import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.player.L2ShortCut;
import dwo.gameserver.model.player.L2ShortCut.ShortcutType;
import dwo.gameserver.network.game.serverpackets.ShortCutInit;
import dwo.gameserver.network.game.serverpackets.ShortCutRegister;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Player shortcuts controller.
 *
 * @author Yorie
 */
public class ShortcutController extends PlayerController
{
	private static final Logger log = LogManager.getLogger(ShortcutController.class);
	private static final int MAX_SHORTCUTS_PER_BAR = 12;

	private static final String REGISTER_SHORTCUT = "REPLACE INTO `character_shortcuts`(`charId`, `slot`, `page`, `type`, `shortcut_id`, `level`, `class_index`) values (?, ?, ?, ?, ?, ?, ?)";
	private static final String DELETE_SHORTCUT = "DELETE FROM `character_shortcuts` WHERE `charId` = ? AND `slot` = ? AND `page` = ? AND `class_index` = ?";
	private static final String RESTORE_SHORTCUTS = "SELECT `charId`, `slot`, `page`, `type`, `shortcut_id`, `level` FROM `character_shortcuts` WHERE `charId` = ? AND `class_index` = ?";

	/**
	 * The list containing all shortCuts of this L2PcInstance
	 */
	private final Map<Integer, L2ShortCut> shortcuts = new TreeMap<>();

	public ShortcutController(L2PcInstance player)
	{
		super(player);
	}

	public List<L2ShortCut> list()
	{
		return new FastList<>(shortcuts.values());
	}

	/**
	 * Returns shortcut for selected @slot and @page.
	 * @param slot Shortcut slot.
	 * @param page Shortcut page.
	 */
	public L2ShortCut getShortcut(int slot, int page)
	{
		L2ShortCut shortcut = shortcuts.get(slot + page * MAX_SHORTCUTS_PER_BAR);

		if(!isValidShortcut(shortcut))
		{
			removeShortcut(shortcut);
			shortcut = null;
		}

		return shortcut;
	}

	/**
	 * Registers new shortcut instead of existing if it was set before.
	 * @param shortcut Shortcut object.
	 */
	public void registerShortcut(L2ShortCut shortcut)
	{
		synchronized(this)
		{
			if(!isValidShortcut(shortcut))
			{
				return;
			}

			if(shortcut.getType() == ShortcutType.ITEM)
			{
				shortcut.setSharedReuseGroup(player.getInventory().getItemByObjectId(shortcut.getId()).getSharedReuseGroup());
			}

			L2ShortCut oldShortcut = shortcuts.put(shortcut.getSlot() + shortcut.getPage() * MAX_SHORTCUTS_PER_BAR, shortcut);

			if(oldShortcut != null)
			{
				removeShortcut0(oldShortcut);
			}

			registerShortcut0(shortcut);
		}
	}

	private void registerShortcut0(L2ShortCut shortcut)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(REGISTER_SHORTCUT);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, shortcut.getType().ordinal());
			statement.setInt(5, shortcut.getId());
			statement.setInt(6, shortcut.getLevel());
			statement.setInt(7, player.getClassIndex());
			statement.execute();
		}
		catch(Exception e)
		{
			log.log(Level.WARN, "Could not store character shortcut: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	public void removeShortcut(L2ShortCut shortcut)
	{
		removeShortcut(shortcut.getSlot(), shortcut.getPage());
	}

	/**
	 * Removes shortcut from bar.
	 */
	public void removeShortcut(int slot, int page)
	{
		synchronized(this)
		{
			L2ShortCut old = shortcuts.remove(slot + page * MAX_SHORTCUTS_PER_BAR);

			if(old == null)
			{
				return;
			}

			removeShortcut0(old);

			player.sendPacket(new ShortCutInit(player));
		}
	}

	/**
	 * Removes shortcut by associated ID and type.
	 * @param shortcutId Shortcut ID.
	 * @param type Shortcut type.
	 */
	public void removeShortcut(int shortcutId, ShortcutType type)
	{
		player.getShortcutController().list().stream().filter(sc -> sc != null && sc.getId() == shortcutId && sc.getType() == type).forEach(this::removeShortcut);
	}

	/**
	 * @param shortcut Shortcut object.
	 */
	private void removeShortcut0(L2ShortCut shortcut)
	{
		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement(DELETE_SHORTCUT);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, shortcut.getSlot());
			statement.setInt(3, shortcut.getPage());
			statement.setInt(4, player.getClassIndex());
			statement.execute();
		}
		catch(Exception e)
		{
			log.log(Level.WARN, "Could not delete character shortcut: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCS(con, statement);
		}
	}

	/**
	 * Restores all player shortcuts from database.
	 */
	public void restore()
	{
		shortcuts.clear();

		ThreadConnection con = null;
		FiltredPreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(RESTORE_SHORTCUTS);
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, player.getClassIndex());

			rset = statement.executeQuery();

			while(rset.next())
			{
				int slot = rset.getInt("slot");
				int page = rset.getInt("page");
				int type = rset.getInt("type");
				int id = rset.getInt("shortcut_id");
				int level = rset.getInt("level");

				L2ShortCut sc = new L2ShortCut(slot, page, type, id, level, 1);
				shortcuts.put(slot + page * MAX_SHORTCUTS_PER_BAR, sc);
			}
		}
		catch(Exception e)
		{
			log.log(Level.WARN, "Could not restore character shortcuts: " + e.getMessage(), e);
		}
		finally
		{
			DatabaseUtils.closeDatabaseCSR(con, statement, rset);
		}

		// Verify shortcuts
		list().stream().filter(sc -> sc.getType() == ShortcutType.ITEM).forEach(sc -> {
			if(isValidShortcut(sc))
			{
				L2ItemInstance item = player.getInventory().getItemByObjectId(sc.getId());
				if(item.isEtcItem())
				{
					sc.setSharedReuseGroup(item.getEtcItem().getSharedReuseGroup());
				}
			}
			else
			{
				removeShortcut(sc);
			}
		});
	}

	/**
	 * Checks if shortcut if valid for current player.
	 * The validity of shortcut can mean existence of item or skill connected to shortcut.
	 *
	 * @param shortcut Shortcut object.
	 * @return True if shortcut is valid.
	 */
	protected boolean isValidShortcut(L2ShortCut shortcut)
	{
		if(shortcut != null && shortcut.getType() == ShortcutType.ITEM)
		{
			if(player.getInventory().getItemByObjectId(shortcut.getId()) == null)
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates all connected shortcuts.
	 *
	 * @param shortcutId Shortcut object ID.
	 * @param level Shortcut new level (skill level, etc.).
	 * @param type Shortcut type from L2Shortcut.
	 */
	public void updateShortcuts(int shortcutId, int level, ShortcutType type)
	{
		list().stream().filter(sc -> sc.getId() == shortcutId && sc.getType() == type && sc.getLevel() != level).forEach(sc -> {
			sc.setLevel(level);
			player.sendPacket(new ShortCutRegister(sc));
		});
	}
}
