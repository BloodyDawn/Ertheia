package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class HorseAeore extends L2Transformation
{
	private static final int[] Skills = {9210, 5491, 9206};

	public HorseAeore()
	{
		// id, colRadius, colHeight
		super(136, 31, 29);
	}

	@Override
	public void transformedSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Dismount
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		// Horse Windwalk
		getPlayer().addSkill(SkillTable.getInstance().getInfo(9206, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		// Dismount
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9210, 1), false);
		// Horse Windwalk
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(9206, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
