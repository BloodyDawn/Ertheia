package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 30.12.12
 * Time: 0:04
 */
public class CraftVehicle extends L2Transformation
{
	private static final int[] Skills = {9206, 19118, 9210};

	public CraftVehicle()
	{
		// id, colRadius, colHeight
		super(146, 35.00, 35.50);
	}

	@Override
	public void transformedSkills()
	{
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9206, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(19118, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9206, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(19118, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
