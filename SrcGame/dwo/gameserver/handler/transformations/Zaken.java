package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class Zaken extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 715, 716, 717, 718, 719};

	public Zaken()
	{
		// id, colRadius, colHeight
		super(305, 16, 32);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(715, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(716, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(717, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(718, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(719, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(715, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(716, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(717, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(718, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(719, 1), false, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
