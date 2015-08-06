package dwo.gameserver.model.player;

import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2AdminCommandAccessRight
{
	/** The admin command<br> */
	private String _adminCommand;
	/** The access levels which can use the admin command<br> */
	private int _accessLevel;
	private boolean _requireConfirm;

	public L2AdminCommandAccessRight(StatsSet set)
	{
		_adminCommand = set.getString("command");
		_requireConfirm = set.getBool("confirmDlg", false);
		_accessLevel = set.getInteger("accessLevel", 7);
	}

	public L2AdminCommandAccessRight(String command, boolean confirm, int level)
	{
		_adminCommand = command;
		_requireConfirm = confirm;
		_accessLevel = level;
	}

	/**
	 * Returns the admin command the access right belongs to<br><br>
	 *
	 * @return String: the admin command the access right belongs to<br>
	 */
	public String getAdminCommand()
	{
		return _adminCommand;
	}

	/**
	 * Checks if the given characterAccessLevel is allowed to use the admin command which belongs to this access right<br><br>
	 *
	 * @param characterAccessLevel
	 *
	 * @return boolean: true if characterAccessLevel is allowed to use the admin command which belongs to this access right, otherwise false<br>
	 */
	public boolean hasAccess(L2AccessLevel characterAccessLevel)
	{
		L2AccessLevel accessLevel = AdminTable.getInstance().getAccessLevel(_accessLevel);
		return characterAccessLevel.getLevel() == accessLevel.getLevel() || characterAccessLevel.hasChildAccess(accessLevel);
	}

	public boolean getRequireConfirm()
	{
		return _requireConfirm;
	}
}