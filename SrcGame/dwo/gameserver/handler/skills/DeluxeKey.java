package dwo.gameserver.handler.skills;

import dwo.gameserver.handler.ISkillHandler;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.L2SkillType;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author Julian
 */
public class DeluxeKey implements ISkillHandler
{
	private static final L2SkillType[] SKILL_IDS = {
		L2SkillType.DELUXE_KEY_UNLOCK
	};
	private static Logger _log = LogManager.getLogger(DeluxeKey.class);

	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{

		// TODO: НАФИГА этот дебаг ?
		// _log.log(Level.INFO, "Deluxe key casting succeded.");
	}

	@Override
	public L2SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
