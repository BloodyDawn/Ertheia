package dwo.gameserver.model.skills;

import java.util.List;

public class L2ExtractableSkill
{
	private final int _hash;
	private final List<L2ExtractableProductItem> _product;

	/**
	 * Instantiates a new extractable skill.
	 * @param hash the hash
	 * @param products the products
	 */
	public L2ExtractableSkill(int hash, List<L2ExtractableProductItem> products)
	{
		_hash = hash;
		_product = products;
	}

	/**
	 * Gets the skill hash.
	 * @return the skill hash
	 */
	public int getSkillHash()
	{
		return _hash;
	}

	/**
	 * Gets the product items.
	 * @return the product items
	 */
	public List<L2ExtractableProductItem> getProductItems()
	{
		return _product;
	}
}