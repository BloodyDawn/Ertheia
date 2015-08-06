package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DivineWarrior extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 676, 677, 678, 679, 798};

	public DivineWarrior()
	{
		// id, colRadius, colHeight
		super(253, 14.5, 29);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(676, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(677, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(678, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(679, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(798, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(675, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(676, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(677, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(678, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(679, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(798, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
