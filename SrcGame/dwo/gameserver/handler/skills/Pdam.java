package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.PhysicalDamage;
import dwo.gameserver.model.skills.base.formulas.calculations.Shield;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Pdam implements ISkillHandler
{
	private static final Logger _logDamage = LogManager.getLogger("Pdamage");

	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.PDAM, L2SkillType.FATAL
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(activeChar.isAlikeDead())
		{
			return;
		}

		if((skill.getFlyRadius() > 0 || skill.getFlyType() != null) && activeChar.isMovementDisabled())
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			activeChar.sendPacket(sm);
			return;
		}

		double damage = 0;

		boolean soul = activeChar.isSoulshotCharged(skill);

		L2Character target;
		for(L2Object obj : targets)
		{
			if(obj instanceof L2Character)
			{
				target = (L2Character) obj;
			}
			else
			{
				continue;
			}
			if(activeChar instanceof L2PcInstance && target instanceof L2PcInstance && ((L2PcInstance) target).isFakeDeath())
			{
				target.stopFakeDeath(true);
			}
			else if(target.isDead())
			{
				continue;
			}

			boolean dual = activeChar.isUsingDualWeapon();
			byte shld = Shield.calcShldUse(activeChar, target, skill);

			damage = skill.isStaticDamage() ? skill.getPower() : PhysicalDamage.calcPhysDam(activeChar, target, skill, shld, false, dual, soul);

			PhysicalDamage.physDamEffect(activeChar, target, skill, shld, damage, 1.0, _logDamage);

			if(skill.isSuicideAttack())
			{
				activeChar.doDie(activeChar);
				if(activeChar.isNpc())
				{
					L2Npc mob = L2Npc.class.cast(activeChar);
					L2PcInstance pcTarget = targets.length > 0 ? targets[0] instanceof L2PcInstance ? L2PcInstance.class.cast(targets[0]) : null : null;
					if(mob.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL) != null)
					{
						for(Quest quest : mob.getTemplate().getEventQuests(Quest.QuestEventType.ON_KILL))
						{
							quest.notifyKill(mob, pcTarget, false);
						}
					}
				}
			}
		}
		activeChar.ssUncharge(skill);
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}