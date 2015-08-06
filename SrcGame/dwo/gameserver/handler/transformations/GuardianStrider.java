package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class GuardianStrider extends L2Transformation
{
	private static final int[] Skills = {839};

	public GuardianStrider()
	{
		// id, colRadius, colHeight
		super(123, 13, 40);
	}

	@Override
	public void transformedSkills()
	{
		// Dismount
		getPlayer().addSkill(SkillTable.getInstance().getInfo(839, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Dismount
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(839, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
