package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class SujinChild extends L2Transformation
{
	private static final int[] Skills = {619};

	public SujinChild()
	{
		// id, colRadius, colHeight
		super(20003, 10, 11.00);
	}

	@Override
	public void transformedSkills()
	{
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
