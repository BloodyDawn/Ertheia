package dwo.gameserver.handler.effects;

import dwo.config.Config;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.TransformationManager;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.actor.instance.L2DefenderInstance;
import dwo.gameserver.model.actor.instance.L2FortCommanderInstance;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2NpcInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2SiegeFlagInstance;
import dwo.gameserver.model.actor.instance.L2SiegeSummonInstance;
import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.util.Rnd;

/**
 * User: Bacek
 * Date: 28.06.13
 * Time: 14:14
 */
public class TransformDebuff extends L2Effect
{
	private int _dX = -1;
	private int _dY = -1;
	private L2Transformation _transformationCopy;

	public TransformDebuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DEBUFF;
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
			if(getEffected().isAlikeDead() || getEffected().isDead())
			{
				return false;
			}

			//  Если происходит обновление возвращаем true
			if(isRefreshTime())
			{
				onActionTime();
				return true;
			}

			if(getEffected() instanceof L2MonsterInstance)
			{
				// Если есть трансформация копируем ее
				if(((L2MonsterInstance) getEffected()).getTransformation() != null)
				{
					_transformationCopy = ((L2MonsterInstance) getEffected()).getTransformation();
					// Останавливаем ее
					getEffected().stopTransformation(false);
				}
				TransformationManager.getInstance().transformMonster(getSkill().getTransformId().get(Rnd.get(getSkill().getTransformId().size())), (L2MonsterInstance) getEffected());
			}
			else if(getEffected() instanceof L2PcInstance)
			{
				// Если есть трансформация копируем ее
				if(((L2PcInstance) getEffected()).getTransformation() != null)
				{
					_transformationCopy = ((L2PcInstance) getEffected()).getTransformation();
					// Останавливаем ее
					getEffected().stopTransformation(false);
				}
				TransformationManager.getInstance().transformPlayer(getSkill().getTransformId().get(Rnd.get(getSkill().getTransformId().size())), (L2PcInstance) getEffected());
			}

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

			onActionTime();
			return true;
		}
		return false;
	}

	@Override
	public void onExit()
	{
		// Останавливаем
		getEffected().stopTransformation(false);
		// Если была до дебафа возвращяем
		if(_transformationCopy != null)
		{
			if(getEffected() instanceof L2MonsterInstance)
			{
				_transformationCopy.createTransformationForMonster((L2MonsterInstance) getEffected()).start();
			}
			else if(getEffected() instanceof L2PcInstance)
			{
				_transformationCopy.createTransformationForPlayer((L2PcInstance) getEffected()).start();
			}
		}

		// Stop the task of the L2Effect, remove it and update client magic icon
		stopEffectTask();
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

		posX += _dX * 500;
		posY += _dY * 500;

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
}
