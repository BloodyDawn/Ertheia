package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public class GiveReco implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.GIVE_RECO
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		for(L2Object obj : targets)
		{
			if(obj instanceof L2PcInstance)
			{
				L2PcInstance target = (L2PcInstance) obj;
				int power = (int) skill.getPower();
				int reco = target.getRecommendations();

				if(reco + power >= 255)
				{
					power = 255 - reco;
				}

				if(power > 0)
				{
					target.setRecommendations(reco + power);

					target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_OBTAINED_S1_RECOMMENDATIONS).addNumber(power));
					target.sendUserInfo();
					//target.sendPacket(new ExVoteSystemInfo(target));
				}
				else
				{
					target.sendPacket(SystemMessageId.NOTHING_HAPPENED);
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