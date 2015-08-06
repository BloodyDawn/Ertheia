package dwo.gameserver.handler.effects;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignet;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignetCasttime;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 15.07.12
 * Time: 7:43
 */

public class BlackHole extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;

	public BlackHole(Env env, EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_EFFECT;
	}

	@Override
	public boolean onStart()
	{
		if(getSkill() instanceof L2SkillSignet)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignet) getSkill())._effectId, ((L2SkillSignet) getSkill())._effectLevel);
		}
		else if(getSkill() instanceof L2SkillSignetCasttime)
		{
			_skill = SkillTable.getInstance().getInfo(((L2SkillSignetCasttime) getSkill())._effectId, ((L2SkillSignet) getSkill())._effectLevel);
		}
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
		if(_skill == null)
		{
			return true;
		}

		for(L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if(!(cha instanceof L2MonsterInstance))
			{
				continue;
			}

			_skill.getEffects(_actor, cha);
		}

		_actor.broadcastPacket(new MagicSkillUse(_actor, getEffected(), _skill.getId(), _skill.getLevel(), 0, 0));
		return getSkill().isToggle();
	}
}
