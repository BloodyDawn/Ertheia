package dwo.gameserver.handler.effects;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.serverpackets.FlyToLocation;
import dwo.gameserver.network.game.serverpackets.FlyToLocation.FlyType;
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;
import dwo.gameserver.util.Util;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 10.03.12
 * Time: 20:42
 */

public class Warp extends L2Effect
{
	private int x;
	private int y;
	private int z;
	private L2Character _actor;

	public Warp(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.WARP;
	}

	@Override
	public boolean onStart()
	{
		_actor = isSelfEffect() ? getEffector() : getEffected();

		if(_actor.isMovementDisabled())
		{
			return false;
		}

		int _radius = getSkill().getFlyRadius();

		double angle = Util.convertHeadingToDegree(_actor.getHeading());
		double radian = Math.toRadians(angle);
		double course = Math.toRadians(getSkill().getFlyCourse());

		int x1 = (int) (Math.cos(Math.PI + radian + course) * _radius);
		int y1 = (int) (Math.sin(Math.PI + radian + course) * _radius);

		x = _actor.getX() + x1;
		y = _actor.getY() + y1;
		z = _actor.getZ();

		if(Config.GEODATA_ENABLED)
		{
			Location destiny = GeoEngine.getInstance().moveCheck(_actor.getX(), _actor.getY(), _actor.getZ(), x, y, z, _actor.getInstanceId());
			x = destiny.getX();
			y = destiny.getY();
			z = destiny.getZ();
		}

		// TODO: check if this AI intention is retail-like. This stops player's
		// previous movement
		_actor.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);

		_actor.broadcastPacket(new FlyToLocation(_actor, x, y, z, FlyType.WARP_BACK, getSkill().getFlySpeed(), getSkill().getFlyDelay(), getSkill().getFlyAnimationSpeed()));
		_actor.abortAttack();
		_actor.abortCast();

		_actor.setXYZ(x, y, z);
		_actor.broadcastPacket(new ValidateLocation(_actor));

		return true;
	}
}