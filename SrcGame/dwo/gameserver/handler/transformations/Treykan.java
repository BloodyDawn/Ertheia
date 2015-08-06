package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Treykan extends L2Transformation
{
	private static final int[] Skills = {619};

	public Treykan()
	{
		// id, colRadius, colHeight
		super(126, 25, 27.00);
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
