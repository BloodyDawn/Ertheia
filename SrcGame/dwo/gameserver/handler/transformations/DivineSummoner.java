package dwo.gameserver.handler.transformations;

import dwo.gameserver.model.skills.L2Transformation;
import dwo.gameserver.model.skills.SkillTable;

public class DivineSummoner extends L2Transformation
{
	private static final int[] Skills = {619, 5491, 710, 711, 712, 713, 714};

	public DivineSummoner()
	{
		// id, colRadius, colHeight
		super(258, 10, 25);
	}

	@Override
	public void transformedSkills()
	{
		// Transfrom Dispel
		getPlayer().addSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().addSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(710, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(711, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(712, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(713, 1), false);
		getPlayer().addSkill(SkillTable.getInstance().getInfo(714, 1), false);
		getPlayer().setTransformAllowedSkills(Skills);
	}

	@Override
	public void removeSkills()
	{
		// Transfrom Dispel
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(619, 1), false);
		// Decrease Bow/Crossbow Attack Speed
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(5491, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(710, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(711, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(712, 1), false, false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(713, 1), false);
		getPlayer().removeSkill(SkillTable.getInstance().getInfo(714, 1), false, false);
		getPlayer().setTransformAllowedSkills(EMPTY_ARRAY);
	}
}
