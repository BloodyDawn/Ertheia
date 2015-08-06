package dwo.gameserver.model.player;

import dwo.gameserver.datatables.xml.AdminTable;
import dwo.gameserver.model.skills.stats.StatsSet;

public class L2AccessLevel
{
	/** Child access levels */
	L2AccessLevel _childsAccessLevel;
	/** The access level<br> */
	private int _accessLevel;
	/** The access level name<br> */
	private String _name;
	/** Child access levels */
	private int _child;
	/** The name color for the access level<br> */
	private int _nameColor;
	/** The title color for the access level<br> */
	private int _titleColor;
	/** Flag to determine if the access level has gm access<br> */
	private boolean _isGm;
	/** Flag for peace zone attack */
	private boolean _allowPeaceAttack;
	/** Flag for fixed res */
	private boolean _allowFixedRes;
	/** Flag for transactions */
	private boolean _allowTransaction;
	/** Flag for AltG commands */
	private boolean _allowAltG;
	/** Flag to give damage */
	private boolean _giveDamage;
	/** Flag to take aggro */
	private boolean _takeAggro;
	/** Flag to gain exp in party */
	private boolean _gainExp;

	public L2AccessLevel(StatsSet set)
	{
		_accessLevel = set.getInteger("level");
		_name = set.getString("name");
		_nameColor = Integer.decode("0x" + set.getString("nameColor", "FFFFFF"));
		_titleColor = Integer.decode("0x" + set.getString("titleColor", "FFFFFF"));
		_child = set.getInteger("childAccess", 0);
		_isGm = set.getBool("isGM", false);
		_allowPeaceAttack = set.getBool("allowPeaceAttack", false);
		_allowFixedRes = set.getBool("allowFixedRes", false);
		_allowTransaction = set.getBool("allowTransaction", true);
		_allowAltG = set.getBool("allowAltg", false);
		_giveDamage = set.getBool("giveDamage", true);
		_takeAggro = set.getBool("takeAggro", true);
		_gainExp = set.getBool("gainExp", true);
	}

	public L2AccessLevel()
	{
		_accessLevel = 0;
		_name = "User";
		_nameColor = Integer.decode("0xFFFFFF");
		_titleColor = Integer.decode("0xFFFFFF");
		_child = 0;
		_isGm = false;
		_allowPeaceAttack = false;
		_allowFixedRes = false;
		_allowTransaction = true;
		_allowAltG = false;
		_giveDamage = true;
		_takeAggro = true;
		_gainExp = true;
	}

	/**
	 * Returns the access level<br>
	 * <br>
	 * @return int: access level<br>
	 */
	public int getLevel()
	{
		return _accessLevel;
	}

	/**
	 * Returns the access level name<br>
	 * <br>
	 * @return String: access level name<br>
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Returns the name color of the access level<br>
	 * <br>
	 * @return int: the name color for the access level<br>
	 */
	public int getNameColor()
	{
		return _nameColor;
	}

	/**
	 * Returns the title color color of the access level<br>
	 * <br>
	 * @return int: the title color for the access level<br>
	 */
	public int getTitleColor()
	{
		return _titleColor;
	}

	/**
	 * Retuns if the access level has gm access or not<br>
	 * <br>
	 * @return boolean: true if access level have gm access, otherwise false<br>
	 */
	public boolean isGm()
	{
		return _isGm;
	}

	/**
	 * Returns if the access level is allowed to attack in peace zone or not<br>
	 * <br>
	 * @return boolean: true if the access level is allowed to attack in peace zone, otherwise false<br>
	 */
	public boolean allowPeaceAttack()
	{
		return _allowPeaceAttack;
	}

	/**
	 * Retruns if the access level is allowed to use fixed res or not<br>
	 * <br>
	 * @return true if the access level is allowed to use fixed res, otherwise false<br>
	 */
	public boolean allowFixedRes()
	{
		return _allowFixedRes;
	}

	/**
	 * Returns if the access level is allowed to perform transactions or not<br>
	 * <br>
	 * @return boolean: true if access level is allowed to perform transactions, otherwise false<br>
	 */
	public boolean allowTransaction()
	{
		return _allowTransaction;
	}

	/**
	 * Returns if the access level is allowed to use AltG commands or not<br>
	 * <br>
	 * @return boolean: true if access level is allowed to use AltG commands, otherwise false<br>
	 */
	public boolean allowAltG()
	{
		return _allowAltG;
	}

	/**
	 * Returns if the access level can give damage or not<br>
	 * <br>
	 * @return boolean: true if the access level can give damage, otherwise false<br>
	 */
	public boolean canGiveDamage()
	{
		return _giveDamage;
	}

	/**
	 * Returns if the access level can take aggro or not<br>
	 * <br>
	 * @return boolean: true if the access level can take aggro, otherwise false<br>
	 */
	public boolean canTakeAggro()
	{
		return _takeAggro;
	}

	/**
	 * Returns if the access level can gain exp or not<br>
	 * <br>
	 * @return boolean: true if the access level can gain exp, otherwise false<br>
	 */
	public boolean canGainExp()
	{
		return _gainExp;
	}

	/**
	 * Returns if the access level contains allowedAccess as child<br>
	 * @param accessLevel as AccessLevel<br>
	 * @return boolean: true if a child access level is equals to allowedAccess, otherwise false<br>
	 */
	public boolean hasChildAccess(L2AccessLevel accessLevel)
	{
		if(_childsAccessLevel == null)
		{
			if(_child <= 0)
			{
				return false;
			}

			_childsAccessLevel = AdminTable.getInstance().getAccessLevel(_child);
		}
		return accessLevel._accessLevel == _childsAccessLevel._accessLevel || _childsAccessLevel.hasChildAccess(accessLevel);
	}
}