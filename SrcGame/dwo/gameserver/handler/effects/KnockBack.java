package dwo.gameserver.handler.effects;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
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

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 22.07.2011
 * Time: 1:01:30
 */

public class KnockBack extends L2Effect
{
	private int _x;
	private int _y;
	private int _z;

	public KnockBack(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.KNOCK_BACK;
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

		// Обездвиживаем цель
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, getEffector());
		getEffected().startKnockBack();

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

		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyType.PUSH_HORIZONTAL, getSkill().getFlySpeed(), getSkill().getFlyDelay(), getSkill().getFlyAnimationSpeed()));
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));

		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopKnockBack(false);
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_PARALYZED;
	}
}
