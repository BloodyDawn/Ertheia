package dwo.gameserver.model.items;

import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.skills.base.L2Skill;
import javolution.util.FastMap;
import org.apache.commons.lang3.ArrayUtils;

import java.util.List;

public class L2ArmorSet
{
	private final int[] _chest;
	private final int[] _legs;
	private final int[] _head;
	private final int[] _gloves;
	private final int[] _feet;
	private final int[] _mw_legs;
	private final int[] _mw_head;
	private final int[] _mw_gloves;
	private final int[] _mw_feet;
	private final List<L2Skill> _skills;

	private final int[] _shield;
	private final int[] _mw_shield;
	private final int _shieldSkillId;

	private final int _skillPartsId;
	private final FastMap<Integer, SkillHolder> _enchantSkills;

	public L2ArmorSet(int[] chest, int[] legs, int[] head, int[] gloves, int[] feet, List<L2Skill> skillInfo, int skill_parts_id, int[] shield, int shield_skill_id, FastMap<Integer, SkillHolder> enchantskills, int[] mw_legs, int[] mw_head, int[] mw_gloves, int[] mw_feet, int[] mw_shield)
	{
		_chest = chest;
		_legs = legs;
		_head = head;
		_gloves = gloves;
		_feet = feet;
		_mw_legs = mw_legs;
		_mw_head = mw_head;
		_mw_gloves = mw_gloves;
		_mw_feet = mw_feet;
		_mw_shield = mw_shield;

		_skills = skillInfo;

		_shield = shield;
		_shieldSkillId = shield_skill_id;

		_skillPartsId = skill_parts_id;
		_enchantSkills = enchantskills;
	}

	/**
	 * Checks if player have equiped all items from set (not checking shield)
	 *
	 * @param player
	 *            whose inventory is being checked
	 * @return True if player equips whole set
	 */
	public boolean containAll(L2PcInstance player)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		int chest = 0;

		if(legsItem != null)
		{
			legs = legsItem.getItemId();
		}
		if(headItem != null)
		{
			head = headItem.getItemId();
		}
		if(glovesItem != null)
		{
			gloves = glovesItem.getItemId();
		}
		if(feetItem != null)
		{
			feet = feetItem.getItemId();
		}
		if(chestItem != null)
		{
			chest = chestItem.getItemId();
		}
		return containAll(chest, legs, head, gloves, feet);
	}

	public boolean containAll(int chest, int legs, int head, int gloves, int feet)
	{
		if(_chest.length != 0 && !ArrayUtils.contains(_chest, chest))
		{
			return false;
		}
		if(_legs.length != 0 && !ArrayUtils.contains(_legs, legs) && (_mw_legs.length == 0 || !ArrayUtils.contains(_mw_legs, legs)))
		{
			return false;
		}
		if(_head.length != 0 && !ArrayUtils.contains(_head, head) && (_mw_head.length == 0 || !ArrayUtils.contains(_mw_head, head)))
		{
			return false;
		}
		if(_gloves.length != 0 && !ArrayUtils.contains(_gloves, gloves) && (_mw_gloves.length == 0 || !ArrayUtils.contains(_mw_gloves, gloves)))
		{
			return false;
		}
		return !(_feet.length != 0 && !ArrayUtils.contains(_feet, feet) && (_mw_feet.length == 0 || !ArrayUtils.contains(_mw_feet, feet)));
	}

	/**
	 * Считаем, сколько частей из сета надето на игроке
	 * в текущий момент
	 * @param player игрок
	 * @return количество надетых частей сета
	 */
	public int countOfPieces(L2PcInstance player)
	{
		int count = 0;
		Inventory inv = player.getInventory();
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);

		if(legsItem != null && ArrayUtils.contains(_legs, legsItem.getItemId()))
		{
			count++;
		}
		if(headItem != null && ArrayUtils.contains(_head, headItem.getItemId()))
		{
			count++;
		}
		if(glovesItem != null && ArrayUtils.contains(_gloves, glovesItem.getItemId()))
		{
			count++;
		}
		if(feetItem != null && ArrayUtils.contains(_feet, feetItem.getItemId()))
		{
			count++;
		}
		if(chestItem != null && ArrayUtils.contains(_chest, chestItem.getItemId()))
		{
			count++;
		}
		return count;
	}

	public boolean containItem(long slot, int itemId)
	{
        if (slot == Inventory.PAPERDOLL_CHEST) {
            return ArrayUtils.contains(_chest, itemId);
        } else if (slot == Inventory.PAPERDOLL_LEGS) {
            return ArrayUtils.contains(_legs, itemId) || ArrayUtils.contains(_mw_legs, itemId);
        } else if (slot == Inventory.PAPERDOLL_HEAD) {
            return ArrayUtils.contains(_head, itemId) || ArrayUtils.contains(_mw_head, itemId);
        } else if (slot == Inventory.PAPERDOLL_GLOVES) {
            return ArrayUtils.contains(_gloves, itemId) || ArrayUtils.contains(_mw_gloves, itemId);
        } else if (slot == Inventory.PAPERDOLL_FEET) {
            return ArrayUtils.contains(_feet, itemId) || ArrayUtils.contains(_mw_feet, itemId);
        } else {
            return false;
        }
	}

	/**
	 * Позволяет проверить, является ли итем
	 * частью какого-либо сета
	 * @param itemId ID предмета
	 * @return является ли итем частью сета
	 */
	public boolean containItem(int itemId)
	{
		return ArrayUtils.contains(_chest, itemId) || ArrayUtils.contains(_legs, itemId) || ArrayUtils.contains(_mw_legs, itemId) || ArrayUtils.contains(_head, itemId) || ArrayUtils.contains(_mw_head, itemId) || ArrayUtils.contains(_gloves, itemId) || ArrayUtils.contains(_mw_gloves, itemId) || ArrayUtils.contains(_feet, itemId) || ArrayUtils.contains(_mw_feet, itemId);
	}

	public List<L2Skill> getSkill()
	{
		return _skills;
	}

	public boolean containShield(L2PcInstance player)
	{
		Inventory inv = player.getInventory();

		L2ItemInstance shieldItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		for(int a_shield : _shield)
		{
			if(shieldItem != null && shieldItem.getItemId() == a_shield)
			{
				return true;
			}
		}
		return false;
	}

	public boolean containShield(int shield_id)
	{
		if(_shield.length == 0)
		{
			return false;
		}
		// Проверяем, есть ли в обычных щитах
		for(int a_shield : _shield)
		{
			if(shield_id == a_shield)
			{
				return true;
			}
		}
		// Проверяем, есть ли в мастер щитах
		for(int a_shield : _mw_shield)
		{
			if(shield_id == a_shield)
			{
				return true;
			}
		}
		return false;
	}

	public int getShieldSkillId()
	{
		return _shieldSkillId;
	}

	public int getPartsSkillId()
	{
		return _skillPartsId;
	}

	public FastMap<Integer, SkillHolder> getEnchantskills()
	{
		return _enchantSkills;
	}

	/**
	 * Checks if all parts of set are enchanted to +6 or more
	 *
	 * @param player
	 * @return
	 */
	public boolean isEnchanted6(L2PcInstance player, int enchant)
	{
		// Player don't have full set
		if(!containAll(player))
		{
			return false;
		}

		Inventory inv = player.getInventory();

		L2ItemInstance chestItem = inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);

		if(chestItem == null || chestItem.getEnchantLevel() < enchant)
		{
			return false;
		}
		if(_legs.length != 0 && (legsItem == null || legsItem.getEnchantLevel() < enchant))
		{
			return false;
		}
		if(_gloves.length != 0 && (glovesItem == null || glovesItem.getEnchantLevel() < enchant))
		{
			return false;
		}
		if(_head.length != 0 && (headItem == null || headItem.getEnchantLevel() < enchant))
		{
			return false;
		}
		return !(_feet.length != 0 && (feetItem == null || feetItem.getEnchantLevel() < enchant));

	}
}
