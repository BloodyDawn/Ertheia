/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.gameserver.model.skills;

import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author KenM
 */
public abstract class L2Transformation implements Cloneable, Runnable
{
	public static final int TRANSFORM_ZARICHE = 301;
	public static final int TRANSFORM_AKAMANAH = 302;
	protected static final int[] EMPTY_ARRAY = {};
	private final int _id;
	private final int _graphicalId;
	private final boolean _isStance;
	private double _collisionRadius;
	private double _collisionHeight;
	private L2PcInstance _player;
	private L2MonsterInstance _monster;

	/**
	 * @param id              Internal id that server will use to associate this transformation
	 * @param graphicalId     Client visible transformation id
	 * @param collisionRadius Collision Radius of the player while transformed
	 * @param collisionHeight Collision Height of the player while transformed
	 */
	protected L2Transformation(int id, int graphicalId, double collisionRadius, double collisionHeight)
	{
		_id = id;
		_graphicalId = graphicalId;
		_collisionRadius = collisionRadius;
		_collisionHeight = collisionHeight;
		_isStance = false;
	}

	/**
	 * @param id              Internal id(will be used also as client graphical id) that server will use to associate this transformation
	 * @param collisionRadius Collision Radius of the player while transformed
	 * @param collisionHeight Collision Height of the player while transformed
	 */
	protected L2Transformation(int id, double collisionRadius, double collisionHeight)
	{
		this(id, id, collisionRadius, collisionHeight);
	}

	/**
	 * @param id Internal id(will be used also as client graphical id) that server will use to associate this transformation
	 *           Used for stances
	 */
	protected L2Transformation(int id)
	{
		_id = id;
		_graphicalId = id;
		_isStance = true;
	}

	/**
	 * @return Returns the id.
	 */
	public int getId()
	{
		return _id;
	}

	/**
	 * @return Returns the graphicalId.
	 */
	public int getGraphicalId()
	{
		return _graphicalId;
	}

	/**
	 * Return true if this is a stance (vanguard/inquisitor)
	 *
	 * @return
	 */
	public boolean isStance()
	{
		return _isStance;
	}

	/**
	 * @return Returns the collisionRadius.
	 */
	public double getCollisionRadius()
	{
		if(_isStance)
		{
			if(_player != null)
			{
				return _player.getCollisionRadius();
			}
			else if(_monster != null)
			{
				return _monster.getCollisionRadius();
			}
		}
		return _collisionRadius;
	}

	/**
	 * @return Returns the collisionHeight.
	 */
	public double getCollisionHeight()
	{
		if(_isStance)
		{
			if(_player != null)
			{
				return _player.getCollisionHeight();
			}
			else if(_monster != null)
			{
				return _monster.getCollisionHeight();
			}
		}
		return _collisionHeight;
	}

	public void onTransform()
	{
		// В случае с мобами скилы им не даем
		if(_player != null)
		{
			if(_player.getTransformationId() != _id || _player.isCursedWeaponEquipped())
			{
				return;
			}
			transformedSkills();
		}
	}

	public void onUntransform()
	{
		// В случае с мобами скилы не удаляем, т.к. мы им их не даем при трансформе
		if(_player != null)
		{
			removeSkills();
		}
	}

	public void transformedSkills()
	{

	}

	public void removeSkills()
	{

	}

	public L2PcInstance getPlayer()
	{
		return _player;
	}

	private void setPlayer(L2PcInstance player)
	{
		_player = player;
	}

	public L2MonsterInstance getMonster()
	{
		return _monster;
	}

	private void setMonster(L2MonsterInstance monster)
	{
		_monster = monster;
	}

	public void start()
	{
		resume();
	}

	public void resume()
	{
		if(_player != null)
		{
			_player.transform(this);
		}
		else if(_monster != null)
		{
			_monster.transform(this);
		}
	}

	@Override
	public void run()
	{
		stop();
	}

	public void stop()
	{
		if(_player != null)
		{
			_player.untransform(true);
		}
		else if(_monster != null)
		{
			_monster.untransform(true);
		}
	}

	public L2Transformation createTransformationForPlayer(L2PcInstance player)
	{
		try
		{
			L2Transformation transformation = (L2Transformation) clone();
			transformation._player = player;
			return transformation;
		}
		catch(CloneNotSupportedException e)
		{
			// should never happen
			return null;
		}
	}

	public L2Transformation createTransformationForMonster(L2MonsterInstance monster)
	{
		try
		{
			L2Transformation transformation = (L2Transformation) clone();
			transformation._monster = monster;
			return transformation;
		}
		catch(CloneNotSupportedException e)
		{
			// should never happen
			return null;
		}
	}

	// Override if necessary

	public void onLevelUp()
	{

	}

	/**
	 * Returns true if transformation can do melee attack
	 */
	public boolean canDoMeleeAttack()
	{
		return true;
	}

	/**
	 * Returns true if transformation can start follow target when trying to cast an skill out of range
	 */
	public boolean canStartFollowToCast()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [_id=" + _id + ", _graphicalId=" + _graphicalId + ", _collisionRadius=" + _collisionRadius + ", _collisionHeight=" + _collisionHeight + ", _isStance=" + _isStance + ']';
	}
}
