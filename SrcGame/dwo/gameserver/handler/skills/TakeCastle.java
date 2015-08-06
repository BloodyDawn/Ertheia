package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import org.apache.log4j.Level;

public class TakeCastle implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.TAKECASTLE
	};

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if(!activeChar.isPlayer())
		{
			return;
		}

		L2PcInstance player = activeChar.getActingPlayer();

		if(player.getClan() == null || player.getClan().getLeaderId() != player.getObjectId())
		{
			return;
		}

		Castle castle = CastleManager.getInstance().getCastle(player);
		if(castle == null || !player.checkIfOkToCastSealOfRule(castle, true, skill))
		{
			return;
		}

		try
		{
			if(skill.getId() == 19034)
			{
				castle.engrave(player.getClan(), targets[0], CastleSide.LIGHT);
			}
			else if(skill.getId() == 19035)
			{
				castle.engrave(player.getClan(), targets[0], CastleSide.DARK);
			}
		}
		catch(Exception e)
		{
			_log.log(Level.ERROR, "", e);
		}
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}