package dwo.gameserver.handler.effects;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;

/**
 * @author Forsaiken
 */

public class SignetAntiSummon extends L2Effect
{
	private L2EffectPointInstance _actor;

	public SignetAntiSummon(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_GROUND;
	}

	@Override
	public boolean onStart()
	{
		_actor = (L2EffectPointInstance) getEffected();
		return true;
	}

	@Override
	public void onExit()
	{
		if(_actor != null)
		{
			_actor.getLocationController().delete();
		}
	}

	@Override
	public boolean onActionTime()
	{
		if(getCount() == getEffectTemplate().getTotalTickCount() - 1)
		{
			return true; // do nothing first time
		}
		int mpConsume = getSkill().getMpConsume();

		L2PcInstance caster = (L2PcInstance) getEffector();

		for(L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if(cha == null)
			{
				continue;
			}

			if(!GeoEngine.getInstance().canSeeTarget(_actor, cha))
			{
				return false;
			}

			if(cha instanceof L2Playable)
			{
				if(caster.canAttackCharacter(cha))
				{
					L2PcInstance owner = null;
					owner = cha instanceof L2Summon ? ((L2Summon) cha).getOwner() : (L2PcInstance) cha;

					if(owner != null && !owner.getPets().isEmpty())
					{
						if(mpConsume > getEffector().getCurrentMp())
						{
							getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
							return false;
						}
						getEffector().reduceCurrentMp(mpConsume);

						for(L2Summon pet : owner.getPets())
						{
							pet.getLocationController().decay();
						}

						owner.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, getEffector());
					}
				}
			}
		}
		return getSkill().isToggle();
	}
}
