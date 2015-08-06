package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

public class GiveVitality implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.GIVE_VITALITY
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for(L2Object target : targets)
		{
			if(target instanceof L2PcInstance)
			{
				if(skill.hasEffects())
				{
					((L2PcInstance) target).stopSkillEffects(skill.getId());
					skill.getEffects(activeChar, (L2PcInstance) target);
					target.getActingPlayer().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
				}
				if(((L2PcInstance) target).getVitalityDataForCurrentClassIndex().getVitalityItems() > 0)
				{
					((L2PcInstance) target).updateVitalityPoints((float) skill.getPower(), -1, false, false);
					((L2PcInstance) target).decreaseVitalityItemsLeft();
					((L2PcInstance) target).broadcastUserInfo();
				}
				else
				{
					((L2PcInstance) target).sendMessage("Лимит достпуных для использования предметов жизненной энергии исчерпан."); // TODO: сообщение из клиента
				}
			}
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
