package dwo.gameserver.model.items.base.type;

public enum L2EtcItemType implements L2ItemType
{
	NONE(1, "none"),
	ARROW(2, "arrow"),
	POTION(3, "potion"),
	SCRL_ENCHANT_WP(4, "scrl_enchant_wp"),
	SCRL_ENCHANT_AM(5, "scrl_enchant_am"),
	SCROLL(6, "scroll"),
	RECIPE(7, "recipe"),
	MATERIAL(8, "material"),
	PET_COLLAR(9, "pet_collar"),
	CASTLE_GUARD(10, "castle_guard"),
	LOTTO(11, "lotto"),
	RACE_TICKET(12, "race_ticket"),
	DYE(13, "dye"),
	SEED(14, "seed"),
	CROP(15, "crop"),
	MATURECROP(16, "maturecrop"),
	HARVEST(17, "harvest"),
	SEED2(18, "seed2"),
	TICKET_OF_LORD(19, "ticket_of_lord"),
	LURE(20, "lure"),
	BLESS_SCRL_ENCHANT_WP(21, "bless_scrl_enchant_wp"),
	BLESS_SCRL_ENCHANT_AM(22, "bless_scrl_enchant_am"),
	COUPON(23, "coupon"),
	ELIXIR(24, "elixir"),
	SCRL_ENCHANT_ATTR(25, "scrl_enchant_attr"),
	BOLT(26, "bolt"),
	SCRL_INC_ENCHANT_PROP_WP(27, "scrl_inc_enchant_prop_wp"),
	SCRL_INC_ENCHANT_PROP_AM(28, "scrl_inc_enchant_prop_am"),
	ANCIENT_CRYSTAL_ENCHANT_WP(29, "ancient_crystal_enchant_wp"),
	ANCIENT_CRYSTAL_ENCHANT_AM(30, "ancient_crystal_enchant_am"),
	RUNE_SELECT(31, "rune_select"),
	RUNE(32, "rune"),

	// L2J CUSTOM, BACKWARD COMPATIBILITY
	SHOT(33, "Shot"),
	HERB(34, "Herb"),
	UNKNOWN(35, "Unknown"),
	BLESSED(36, "Blessed"),
	GENERAL(37, "General"),
	COMMON(38, "Common");

	final int _id;
	final String _name;

	/**
	 * Constructor of the L2EtcItemType.
	 * @param id : int designating the ID of the EtcItemType
	 * @param name : String designating the name of the EtcItemType
	 */
	L2EtcItemType(int id, String name)
	{
		_id = id;
		_name = name;
	}

	/**
	 * @return int : ID of the item after applying the mask.
	 */
	@Override
	public int mask()
	{
		return 0;
	}

	/**
	 * @return String name of the EtcItemType
	 */
	@Override
	public String toString()
	{
		return _name;
	}
}
