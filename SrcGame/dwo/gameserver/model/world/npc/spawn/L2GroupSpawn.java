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
package dwo.gameserver.model.world.npc.spawn;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.LocationsTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;

import java.lang.reflect.Constructor;

/**
 * @author littlecrow
 *         A special spawn implementation to spawn controllable mob
 */
public class L2GroupSpawn extends L2Spawn
{
	private Constructor<?> _constructor;
	private L2NpcTemplate _template;

	public L2GroupSpawn(L2NpcTemplate mobTemplate) throws SecurityException, ClassNotFoundException, NoSuchMethodException
	{
		super(mobTemplate);
		_constructor = Class.forName("dwo.gameserver.model.actor.instance.L2ControllableMobInstance").getConstructors()[0];
		_template = mobTemplate;

		setAmount(1);
	}

	public L2Npc doGroupSpawn()
	{
		L2Npc mob = null;

		try
		{
			if(_template.isType("L2Pet") || _template.isType("L2Minion"))
			{
				return null;
			}

			Object[] parameters = {IdFactory.getInstance().getNextId(), _template};
			Object tmp = _constructor.newInstance(parameters);

			if(!(tmp instanceof L2Npc))
			{
				return null;
			}

			mob = (L2Npc) tmp;

			int newlocx;
			int newlocy;
			int newlocz;

			if(getLocx() == 0 && getLocy() == 0)
			{
				if(getLocationId() == 0)
				{
					return null;
				}

				int[] p = LocationsTable.getInstance().getRandomPoint(getLocationId());
				newlocx = p[0];
				newlocy = p[1];
				newlocz = p[2];
			}
			else
			{
				newlocx = getLocx();
				newlocy = getLocy();
				newlocz = getLocz();
			}

			mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp());

			if(getHeading() == -1)
			{
				mob.setHeading(Rnd.get(61794));
			}
			else
			{
				mob.setHeading(getHeading());
			}

			mob.setSpawn(this);
			mob.getLocationController().spawn(newlocx, newlocy, newlocz);
			mob.onSpawn();

			if(Config.DEBUG)
			{
				_log.log(Level.DEBUG, "spawned Mob ID: " + _template.getNpcId() + " ,at: " + mob.getX() + " x, " + mob.getY() + " y, " + mob.getZ() + " z");
			}

			return mob;

		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "NPC class not found: " + e.getMessage(), e);
			return null;
		}
	}
}