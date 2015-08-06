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
package dwo.gameserver.handler.effects;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2FortCommanderInstance;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2RaidBossInstance;
import dwo.gameserver.model.actor.instance.L2SiegeFlagInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.FlyToLocation.FlyType;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ThrowUp extends L2Effect
{
	static final Logger _log = LogManager.getLogger(ThrowUp.class);

	private int _x;
	private int _y;
	private int _z;

	public ThrowUp(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.THROW_UP;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() == null || getEffector() == null)
		{
			return false;
		}

		if(getEffected() instanceof L2SiegeFlagInstance ||
			getEffected() instanceof L2FortCommanderInstance ||
			getEffected() instanceof L2NpcInstance ||
			getEffected() instanceof L2SiegeSummonInstance ||
			getEffected() instanceof L2RaidBossInstance ||
			getEffected() instanceof L2GrandBossInstance)
		{
			return false;
		}

		if(!L2Skill.checkForAreaOffensiveSkills(getEffector(), getEffected(), getSkill(), getEffector().isInsideZone(L2Character.ZONE_PVP) && !getEffector().isInsideZone(L2Character.ZONE_SIEGE)))
		{
			return false;
		}

		// Get current position of the L2Character
		int curX = getEffected().getX();
		int curY = getEffected().getY();
		int curZ = getEffected().getZ();

		// Calculate distance between effector and effected current position
		double dx = getEffector().getX() - curX;
		double dy = getEffector().getY() - curY;
		double dz = getEffector().getZ() - curZ;
		double distance = Math.sqrt(dx * dx + dy * dy);
		if(distance > 2000)
		{
			_log.log(Level.INFO, "EffectThrow was going to use invalid coordinates for characters, getEffected: " + curX + ',' + curY + " and getEffector: " + getEffector().getX() + ',' + getEffector().getY());
			return false;
		}
		int offset = Math.min((int) distance + getSkill().getFlyRadius(), 1400);

		double cos;
		double sin;

		// approximation for moving futher when z coordinates are different
		// TODO: handle Z axis movement better
		offset += Math.abs(dz);
		if(offset < 5)
		{
			offset = 5;
		}

		// If no distance
		if(distance < 1)
		{
			return false;
		}

		// Calculate movement angles needed
		sin = dy / distance;
		cos = dx / distance;

		// Calculate the new destination with offset included
		_x = getEffector().getX() - (int) (offset * cos);
		_y = getEffector().getY() - (int) (offset * sin);
		_z = getEffected().getZ();

		if(Config.GEODATA_ENABLED)
		{
			Location destiny = GeoEngine.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), _x, _y, _z, getEffected().getInstanceId());
			_x = destiny.getX();
			_y = destiny.getY();
		}
		getEffected().startStunning();
		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyType.THROW_UP, getSkill().getFlySpeed(), getSkill().getFlyDelay(), getSkill().getFlyAnimationSpeed()));
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopStunning(false);
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_STUNNED;
	}
}
