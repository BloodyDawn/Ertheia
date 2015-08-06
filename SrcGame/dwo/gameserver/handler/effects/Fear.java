package dwo.gameserver.handler.effects;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DefenderInstance;
import dwo.gameserver.model.actor.instance.L2FortCommanderInstance;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeFlagInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.effects.AbnormalEffect;
import dwo.gameserver.model.skills.effects.CharEffectList;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.zone.Location;

public class Fear extends L2Effect
{
	public static final int FEAR_RANGE = 500;

	private int _dX = -1;
	private int _dY = -1;

	public Fear(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.FEAR;
	}

	@Override
	public boolean onStart()
	{
		if(getEffected() instanceof L2NpcInstance || getEffected() instanceof L2DefenderInstance || getEffected() instanceof L2FortCommanderInstance || getEffected() instanceof L2SiegeFlagInstance || getEffected() instanceof L2SiegeSummonInstance || getEffected() instanceof L2GrandBossInstance)
		{
			return false;
		}

		if(!getEffected().isAfraid())
		{
			if(getEffected().isCastingNow() && getEffected().canAbortCast())
			{
				getEffected().abortCast();
			}

			if(getEffected().getX() > getEffector().getX())
			{
				_dX = 1;
			}
			if(getEffected().getY() > getEffector().getY())
			{
				_dY = 1;
			}
			getEffected().startAbnormalEffect(AbnormalEffect.SKULL_FEAR);
			getEffected().startFear();
			onActionTime();
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		getEffected().stopAbnormalEffect(AbnormalEffect.SKULL_FEAR);
		getEffected().stopFear(false);
	}

	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		int posZ = getEffected().getZ();

		if(posX > getEffector().getX())
		{
			_dX = 1;
		}
		if(posY > getEffector().getY())
		{
			_dY = 1;
		}

		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;

		if(Config.GEODATA_ENABLED)
		{
			Location destiny = GeoEngine.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ, getEffected().getInstanceId());
			posX = destiny.getX();
			posY = destiny.getY();
		}

		if(!getEffected().isPet())
		{
			getEffected().setRunning();
		}

		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(posX, posY, posZ, 0));
		return getSkill().isToggle();
	}

	@Override
	public int getEffectFlags()
	{
		return CharEffectList.EFFECT_FLAG_FEAR;
	}
}
