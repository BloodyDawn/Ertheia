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
import dwo.gameserver.network.game.serverpackets.packet.vehicle.boat.ValidateLocation;

/**
 * L2GOD Team
 * User: keiichi
 * Date: 31.03.13
 * Time: 11:30
 */
public class Chain extends L2Effect
{
	private int _x;
	private int _y;
	private int _z;

	public Chain(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		/* Вообще нахрен эта лажа нужна? Практического применения я так и не понял. */
		return L2EffectType.WARP_FORWARD;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() == null || getEffector() == null)
		{
			return false;
		}

		if(getEffected().isMovementDisabled())
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
		getEffected().startParalyze();

		// Получаем координаты
		_x = getEffector().getX();
		_y = getEffector().getY();
		_z = getEffector().getZ();

		if(Config.GEODATA_ENABLED)
		{
			Location destiny = GeoEngine.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), _x, _y, _z, getEffected().getInstanceId());
			_x = destiny.getX();
			_y = destiny.getY();
			_z = destiny.getZ();
		}

		getEffected().broadcastPacket(new FlyToLocation(getEffected(), _x, _y, _z, FlyToLocation.FlyType.WARP_FORWARD, getSkill().getFlySpeed(), getSkill().getFlyDelay(), getSkill().getFlyAnimationSpeed()));
		getEffected().setXYZ(_x, _y, _z);
		getEffected().broadcastPacket(new ValidateLocation(getEffected()));
		return true;
	}

	@Override
	public void onExit()
	{
		getEffected().stopParalyze(false);
		super.onExit();
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_PARALYZED;
	}
}
