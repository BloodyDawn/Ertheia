package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

/**
 * L2GOD Team
 * User: Keiichi
 * Date: 13.07.2011
 * Time: 10:22:47
 */

public class CucuruTime extends L2Transformation
{
	private static final int[] SKILLS = {9210};

	public CucuruTime()
	{
		// id, colRadius, colHeight
		super(137, 30, 40);
	}

	@Override
	public void transformedSkills()
	{
		// Dismount
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		getPlayer().setTransformAllowedSkills(SKILLS);
	}

	@Override
	public void removeSkills()
	{
		// Dismount
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
