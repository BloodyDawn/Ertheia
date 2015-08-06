package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class LightPurpleManedHorse extends L2Transformation
{
	private static final int[] Skills = {9210, 5491, 9206};

	public LightPurpleManedHorse()
	{
		// id, colRadius, colHeight
		super(106, 31, 32.5);
	}

	@Override
	public void transformedSkills()
	{
		// Dismount
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Horse Windwalk
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9206, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Dismount
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Horse Windwalk
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9206, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
