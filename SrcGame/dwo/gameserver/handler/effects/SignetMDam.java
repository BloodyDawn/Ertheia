package dwo.gameserver.handler.effects;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignetCasttime;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.util.geometry.Point3D;
import javolution.util.FastList;

public class SignetMDam extends L2Effect
{
	private L2EffectPointInstance _actor;

	public SignetMDam(Env env, EffectTemplate template)
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
		L2NpcTemplate template;
		if(getSkill() instanceof L2SkillSignetCasttime)
		{
			template = NpcTable.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
		}
		else
		{
			return false;
		}

		L2EffectPointInstance effectPoint = new L2EffectPointInstance(IdFactory.getInstance().getNextId(), template, getEffector());
		effectPoint.setCurrentHp(effectPoint.getMaxHp());
		effectPoint.setCurrentMp(effectPoint.getMaxMp());
		//L2World.getInstance().storeObject(effectPoint);

		int x = getEffector().getX();
		int y = getEffector().getY();
		int z = getEffector().getZ();

		if(getEffector() instanceof L2PcInstance && getSkill().getTargetType() == L2TargetType.TARGET_GROUND)
		{
			Point3D wordPosition = ((L2PcInstance) getEffector()).getCurrentSkillWorldPosition();

			if(wordPosition != null)
			{
				x = wordPosition.getX();
				y = wordPosition.getY();
				z = wordPosition.getZ();
			}
		}
		effectPoint.setIsInvul(true);
		effectPoint.getLocationController().spawn(x, y, z);

		_actor = effectPoint;
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
		if(getCount() >= getEffectTemplate().getTotalTickCount() - 2)
		{
			return false; // do nothing first 2 times
		}
		int mpConsume = getSkill().getMpConsume();

		L2PcInstance caster = (L2PcInstance) getEffector();

		boolean sps = caster.isSpiritshotCharged(getSkill());
		boolean bss = caster.isBlessedSpiritshotCharged(getSkill());

		FastList<L2Character> targets = new FastList<>();

		for(L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if(cha == null || cha.equals(caster))
			{
				continue;
			}

			if(cha instanceof L2Attackable || cha instanceof L2Playable)
			{
				if(cha.isAlikeDead())
				{
					continue;
				}

				if(!GeoEngine.getInstance().canSeeTarget(_actor, cha))
				{
					return false;
				}

				if(mpConsume > caster.getCurrentMp())
				{
					caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
					return false;
				}
				caster.reduceCurrentMp(mpConsume);

				if(cha instanceof L2Playable)
				{
					if(caster.canAttackCharacter(cha))
					{
						targets.add(cha);
						caster.getPvPFlagController().updateStatus(cha);
					}
				}
				else
				{
					targets.add(cha);
				}
			}
		}

		if(!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for(L2Character target : targets)
			{
				boolean mcrit = MagicalDamage.calcMCrit(caster.getMCriticalHit(target, getSkill()));
				byte shld = Shield.calcShldUse(caster, target, getSkill());
				int mdam = (int) MagicalDamage.calcMagicDam(caster, target, getSkill(), shld, sps, bss, mcrit);

				if(target instanceof L2Summon)
				{
					target.broadcastStatusUpdate();
				}

				if(mdam > 0)
				{
					CancelAttack.calcAtkBreak(target, mdam);
					caster.sendDamageMessage(target, mdam, mcrit, false, false);
					target.reduceCurrentHp(mdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}
		return getSkill().isToggle();
	}
}
