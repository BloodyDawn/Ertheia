package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DivineWizard extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 692, 693, 694, 695, 696, 697};

	public DivineWizard()
	{
		// id, colRadius, colHeight
		super(256, 10, 26);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(692, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(693, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(694, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(695, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(696, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(697, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(692, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(693, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(694, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(695, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(696, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(697, 1), false, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
