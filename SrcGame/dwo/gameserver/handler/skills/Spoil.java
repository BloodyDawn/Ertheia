package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlEvent;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.formulas.calculations.MagicalDamage;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * @author _drunk_
 */

public class Spoil implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.SPOIL
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!(activeChar instanceof L2PcInstance))
		{
			return;
		}

		if(targets == null)
		{
			return;
		}

		for(L2Object tgt : targets)
		{
			if(!(tgt instanceof L2MonsterInstance))
			{
				continue;
			}

			L2MonsterInstance target = (L2MonsterInstance) tgt;

			if(target.isSpoil())
			{
				activeChar.sendPacket(SystemMessageId.ALREADY_SPOILED);
				continue;
			}

			// SPOIL SYSTEM by Lbaldi
			boolean spoil = false;
			if(!target.isDead())
			{
				spoil = MagicalDamage.calcMagicSuccess(activeChar, (L2Character) tgt, skill);

				if(spoil)
				{
					target.setSpoil(true);
					target.setIsSpoiledBy(activeChar);
					activeChar.sendPacket(SystemMessageId.SPOIL_SUCCESS);
				}
				else
				{
					activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
				}
				target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
