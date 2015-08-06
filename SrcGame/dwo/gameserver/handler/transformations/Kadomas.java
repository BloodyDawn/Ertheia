package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Kadomas extends L2Transformation
{
	private static final int[] Skills = {23154, 619};

	public Kadomas()
	{
		// id, colRadius, colHeight
		super(20000, 24.5, 14);
	}

	@Override
	public void transformedSkills()
	{
		//Kadomas Special Skill - Fireworks
		getPlayer().addSkill(SkillTable.getInstance().getInfo(23154, 1), false);
		// Transform Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		//Kadomas Special Skill - Fireworks
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(23154, 1), false);
		// Transform Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
