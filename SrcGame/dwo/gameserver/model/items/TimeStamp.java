package dwo.gameserver.model.items;

import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.base.L2Skill;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 28.10.11
 * Time: 20:53
 */

public class TimeStamp
{
	private final int _id1; // ID предмета или умения
	private final int _id2; // ObjId предмета или уровень умения
	private final long _reuse;
	private final long _stamp;
	private final int _group;

	/**
	 * @param skill умение, для которого будет создан отпечаток
	 * @param reuse время повторного использования для умения
	 */
	public TimeStamp(L2Skill skill, long reuse)
	{
		_id1 = skill.getId();
		_id2 = skill.getLevel();
		_reuse = reuse;
		_stamp = System.currentTimeMillis() + reuse;
		_group = -1;
	}

	/**
	 * @param skill the skill upon the stamp will be created.
	 * @param reuse the reuse time for this skill.
	 * @param systime overrides the system time with a customized one.
	 */
	public TimeStamp(L2Skill skill, long reuse, long systime)
	{
		_id1 = skill.getId();
		_id2 = skill.getLevel();
		_reuse = reuse;
		_stamp = systime;
		_group = -1;
	}

	/**
	 * @param item the item upon the stamp will be created.
	 * @param reuse the reuse time for this item.
	 */
	public TimeStamp(L2ItemInstance item, long reuse)
	{
		_id1 = item.getItemId();
		_id2 = item.getObjectId();
		_reuse = reuse;
		_stamp = System.currentTimeMillis() + reuse;
		_group = item.getSharedReuseGroup();
	}

	/**
	 * @param item the item upon the stamp will be created.
	 * @param reuse the reuse time for this item.
	 * @param systime overrides the system time with a customized one.
	 */
	public TimeStamp(L2ItemInstance item, long reuse, long systime)
	{
		_id1 = item.getItemId();
		_id2 = item.getObjectId();
		_reuse = reuse;
		_stamp = systime;
		_group = item.getSharedReuseGroup();
	}

	/**
	 * @return the time stamp, either the system time where this time stamp was created or the custom time assigned.
	 */
	public long getStamp()
	{
		return _stamp;
	}

	/**
	 * @return the first Id for the item, the item Id.
	 */
	public int getItemId()
	{
		return _id1;
	}

	/**
	 * @return the second Id for the item, the item object Id.
	 */
	public int getItemObjectId()
	{
		return _id2;
	}

	/**
	 * @return the skill Id.
	 */
	public int getSkillId()
	{
		return _id1;
	}

	/**
	 * @return the skill level.
	 */
	public int getSkillLvl()
	{
		return _id2;
	}

	/**
	 * @return the reuse set for this Item/Skill.
	 */
	public long getReuse()
	{
		return _reuse;
	}

	/**
	 * @return the shared reuse group for the item, -1 for skills.
	 */
	public int getSharedReuseGroup()
	{
		return _group;
	}

	/**
	 * @return the remaining time for this time stamp to expire.
	 */
	public long getRemaining()
	{
		return Math.max(_stamp - System.currentTimeMillis(), 0);
	}

	/**
	 * Check if the reuse delay has passed and if it has not then update the stored reuse time according to what is currently remaining on the delay.
	 * @return {@code true} if this time stamp has expired, {@code false} otherwise.
	 */
	public boolean hasNotPassed()
	{
		return System.currentTimeMillis() < _stamp;
	}
}
