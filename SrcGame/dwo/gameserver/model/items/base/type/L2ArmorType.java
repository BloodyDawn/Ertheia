package dwo.gameserver.model.items.base.type;

public enum L2ArmorType implements L2ItemType
{
	NONE("None"),
	LIGHT("Light"),
	HEAVY("Heavy"),
	MAGIC("Magic"),
	SIGIL("Sigil"),
	SHIELD("Shield");

	final int _mask;
	final String _name;

	/**
	 * @param name : String designating the name of the ArmorType
	 */
	L2ArmorType(String name)
	{
		_mask = 1 << ordinal() + L2WeaponType.values().length;
		_name = name;
	}

	/**
	 * @return int : ID of the ArmorType after mask
	 */
	@Override
	public int mask()
	{
		return _mask;
	}

	/**
	 * @return name of the ArmorType
	 */
	@Override
	public String toString()
	{
		return _name;
	}
}
