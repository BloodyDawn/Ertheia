package dwo.gameserver.handler.effects;

import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignet;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignetCasttime;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.MagicSkillUse;

public class SignetDebuff extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;

	public SignetDebuff(Env env, EffectTemplate template)
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
			return false;
		}

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

			boolean isAffected = false;

			if(getEffector() instanceof L2PcInstance && cha instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) getEffector();
				L2PcInstance target = (L2PcInstance) cha;
				if(cha.equals(player))
				{
					continue;
				}

				if(target.getPvPFlagController().isFlagged())
				{
					if(player.getParty() != null)
					{
						if(!player.isInSameParty(target))
						{
							isAffected = true;
						}
						if(!player.isInSameChannel(target))
						{
							isAffected = true;
						}
					}
					if(player.getClan() != null && !player.isInsideZone(L2Character.ZONE_PVP))
					{
						if(!player.isInSameClan(target))
						{
							isAffected = true;
						}

						if(!player.isInSameAlly(target))
						{
							isAffected = true;
						}
					}

					if(target.getParty() == null)
					{
						isAffected = true;
					}
				}
				else
				{
					if(player.getClan() != null && target.getClan() != null)
					{
						if(player.getClan().isAtWarWith(target.getClanId()))
						{
							isAffected = true;
						}
					}
				}
			}

			if(getEffector() instanceof L2PcInstance && cha instanceof L2Summon)
			{
				L2PcInstance player = (L2PcInstance) getEffector();
				L2PcInstance owner = ((L2Summon) cha).getOwner();

				if(owner.getPvPFlagController().isFlagged())
				{
					if(player.getParty() != null)
					{
						if(!player.isInSameParty(owner))
						{
							isAffected = true;
						}
						if(!player.isInSameChannel(owner))
						{
							isAffected = true;
						}
					}
					if(player.getClan() != null && !player.isInsideZone(L2Character.ZONE_PVP))
					{
						if(!player.isInSameClan(owner))
						{
							isAffected = true;
						}
						if(!player.isInSameAlly(owner))
						{
							isAffected = true;
						}
					}

					if(owner.getParty() == null)
					{
						isAffected = true;
					}
				}
				else
				{
					if(player.getClan() != null && owner.getClan() != null)
					{
						if(player.getClan().isAtWarWith(owner.getClanId()))
						{
							isAffected = true;
						}
					}
				}
			}

			if(cha instanceof L2Npc)
			{
				isAffected = true;
			}

			if(isAffected)
			{
				_skill.getEffects(_actor, cha);
				_actor.broadcastPacket(new MagicSkillUse(_actor, cha, _skill.getId(), _skill.getLevel(), 0, 0));
			}
		}
		return getSkill().isToggle();
	}
}
