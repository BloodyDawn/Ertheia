package dwo.gameserver.model.skills;

import dwo.gameserver.datatables.xml.EnchantSkillGroupsTable;
import gnu.trove.map.hash.TIntIntHashMap;

public class L2EnchantSkillLearn
{
	private final int _id;
	private final int _baseLvl;
	private final TIntIntHashMap _enchantRoutes = new TIntIntHashMap();

	public L2EnchantSkillLearn(int id, int baseLvl)
	{
		_id = id;
		_baseLvl = baseLvl;
	}

	public static int getEnchantRoute(int level)
	{
		return (int) Math.floor(level / 100);
	}

	public static int getEnchantIndex(int level)
	{
		return level % 100 - 1;
	}

	public static int getEnchantType(int level)
	{
		return (level - 1) / 100 - 1;
	}

	public void addNewEnchantRoute(int route, int group)
	{
		_enchantRoutes.put(route, group);
	}

	/**
	 * @return id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return minLevel.
	 */
	public int getBaseLevel()
	{
		return _baseLvl;
	}

	public L2EnchantSkillGroup getFirstRouteGroup()
	{
		return EnchantSkillGroupsTable.getInstance().getEnchantSkillGroupById(_enchantRoutes.values()[0]);
	}

	public int[] getAllRoutes()
	{
		return _enchantRoutes.keys();
	}

	public int getGroupId()
	{
		return _enchantRoutes.values()[0];
	}

	public int getMinSkillLevel(int level)
	{
		if(level % 100 == 1)
		{
			return _baseLvl;
		}
		return level - 1;
	}

	public boolean isMaxEnchant(int level)
	{
		int enchantType = getEnchantRoute(level);
		if(enchantType < 1 || !_enchantRoutes.contains(enchantType))
		{
			return false;
		}
		int index = getEnchantIndex(level);

		return index + 1 >= EnchantSkillGroupsTable.getInstance().getEnchantSkillGroupById(_enchantRoutes.get(enchantType)).getEnchantGroupDetails().size();
	}

	public L2EnchantSkillGroup.EnchantSkillDetail getEnchantSkillDetail(int level)
	{
		int enchantType = getEnchantRoute(level);
		if(enchantType < 1 || !_enchantRoutes.contains(enchantType))
		{
			return null;
		}
		int index = getEnchantIndex(level);
		L2EnchantSkillGroup group = EnchantSkillGroupsTable.getInstance().getEnchantSkillGroupById(_enchantRoutes.get(enchantType));

		if(index < 0)
		{
			return group.getEnchantGroupDetails().get(0);
		}
		if(index >= group.getEnchantGroupDetails().size())
		{
			return group.getEnchantGroupDetails().get(EnchantSkillGroupsTable.getInstance().getEnchantSkillGroupById(_enchantRoutes.get(enchantType)).getEnchantGroupDetails().size() - 1);
		}
		return group.getEnchantGroupDetails().get(index);
	}
}