package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Teleporter2 extends L2Transformation
{
	private static final int[] Skills = {8248, 5491, 5656, 5657, 5658, 5659};

	public Teleporter2()
	{
		// id, colRadius, colHeight
		super(107, 8, 24);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5656, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5657, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5658, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5659, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(8248, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5656, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5657, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5658, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5659, 1), false, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
