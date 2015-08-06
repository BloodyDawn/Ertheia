package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DivineRogue extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 686, 687, 688, 689, 690, 691, 797};

	public DivineRogue()
	{
		// id, colRadius, colHeight
		super(254, 10, 28);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(686, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(687, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(688, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(689, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(690, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(691, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(797, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(686, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(687, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(688, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(689, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(690, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(691, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(797, 1), false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
