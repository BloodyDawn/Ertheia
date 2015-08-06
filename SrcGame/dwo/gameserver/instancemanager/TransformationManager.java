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
package dwo.gameserver.instancemanager;

import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2Transformation;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author KenM
 */

public class TransformationManager
{
	private static final Logger _log = LogManager.getLogger(TransformationManager.class);
	private Map<Integer, L2Transformation> _transformations;

	private TransformationManager()
	{
		_transformations = new HashMap<>();
	}

	public static TransformationManager getInstance()
	{
		return SingletonHolder._instance;
	}

	public void report()
	{
		_log.log(Level.INFO, "TransformationManager: Loaded " + _transformations.size() + " transformations.");
	}

	public boolean transformPlayer(int id, L2PcInstance player)
	{
		L2Transformation template = getTransformationById(id);
		if(template != null)
		{
			template.createTransformationForPlayer(player).start();
			return true;
		}
		else
		{
			return false;
		}
	}

	public boolean transformMonster(int id, L2MonsterInstance monster)
	{
		L2Transformation template = getTransformationById(id);
		if(template != null)
		{
			template.createTransformationForMonster(monster).start();
			return true;
		}
		else
		{
			return false;
		}
	}

	public L2Transformation getTransformationById(int id)
	{
		return _transformations.get(id);
	}

	public L2Transformation registerHandler(L2Transformation transformation)
	{
		if(_transformations.containsKey(transformation.getId()))
		{
			_log.log(Level.ERROR, "TransformationManager: duplicated id: " + transformation.getId());
		}

		return _transformations.put(transformation.getId(), transformation);
	}

	public Collection<L2Transformation> getAllTransformations()
	{
		return _transformations.values();
	}

	private static class SingletonHolder
	{
		protected static final TransformationManager _instance = new TransformationManager();
	}
}
