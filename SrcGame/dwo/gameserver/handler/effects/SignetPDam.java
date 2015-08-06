package dwo.gameserver.handler.effects;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.instance.L2EffectPointInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.CancelAttack;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.l2skills.L2SkillSignetCasttime;
import dwo.gameserver.model.skills.base.proptypes.L2TargetType;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.effects.L2Effect;
import dwo.gameserver.model.skills.effects.L2EffectType;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.network.game.serverpackets.MagicSkillLaunched;
import dwo.gameserver.util.geometry.Point3D;
import javolution.util.FastList;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 14.06.12
 * Time: 8:35
 */

public class SignetPDam extends L2Effect
{
	private L2EffectPointInstance _actor;

	public SignetPDam(Env env, EffectTemplate template)
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
		L2PcInstance caster = (L2PcInstance) getEffector();

		FastList<L2Character> targets = new FastList<>();

		for(L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{

			if(!L2Skill.checkForAreaOffensiveSkills(caster, cha, getSkill(), getEffector().isInsideZone(L2Character.ZONE_PVP) && !getEffector().isInsideZone(L2Character.ZONE_SIEGE)))
			{
				continue;
			}

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

		if(!targets.isEmpty())
		{
			caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), targets.toArray(new L2Character[targets.size()])));
			for(L2Character target : targets)
			{
				boolean pcrit = PhysicalDamage.calcCrit(caster.getStat().getCriticalHit(target, null), false, target);
				byte shld = Shield.calcShldUse(caster, target, getSkill());
				int pdam = (int) PhysicalDamage.calcPhysDam(caster, target, getSkill(), shld, false, false, pcrit);

				if(target instanceof L2Summon)
				{
					target.broadcastStatusUpdate();
				}

				if(pdam > 0)
				{
					CancelAttack.calcAtkBreak(target, pdam);
					caster.sendDamageMessage(target, pdam, pcrit, false, false);
					target.reduceCurrentHp(pdam, caster, getSkill());
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, caster);
			}
		}

		return getSkill().isToggle();
	}
}